import _ from 'lodash';
import {Component, Inject, NgScope, NgElement, Event, NgTimeout} from 'app/app';
import application from 'app/app';
import base64 from 'common/lib/base64';
import shortid from 'shortid';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {UserService} from 'security/user-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {EventEmitter} from 'common/event-emitter';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import 'idoc/widget/common-configurations/widget-common-display-configurations';
import {ColorPicker} from 'components/color-picker/color-picker';

import widgetTemplate from './widget.html!text';
import 'font-awesome/css/font-awesome.css!';
import './widget.css!css';

const WIDGET_CONFIG_LABEL = 'widget.config.label';
const MODE_EDIT_LOCKED = 'edit-locked';

export const NEW_ATTRIBUTE = 'data-new';

// Not using the one from app.js because it throws undefined for it
function getClassName(target) {
  var className = target.name;

  // for some reason the classes don't have name in IE. Let's pollyfill it
  if (!target.name) {
    className = /^function\s+([\w\$]+)\s*\(/.exec(target.toString())[1];
    target.name = className;
  }

  return className;
}

export const WidgetRegistry = {
  getWidgets: _.memoize(function () {
    let widgets = [];
    PluginRegistry.get('idoc-widget').forEach((widgetDefinition) => {
      widgets[widgetDefinition.name] = widgetDefinition;
    });
    return widgets;
  })
};

export function Widget(target) {
  var config = {};
  config.selector = _.kebabCase(getClassName(target));
  config.properties = {
    'context': 'context',
    'control': 'control',
    'config': 'config'
  };

  Component(config)(target);
}

@Event()
export class WidgetRemovedEvent {

}

export function WidgetConfig(target) {
  var config = {};
  config.selector = _.kebabCase(getClassName(target));
  config.properties = {
    'config': 'config',
    'definition': 'definition',
    'context': 'context'
  };

  Component(config)(target);
}

@Component({
  selector: '[widget]'
})
@Inject(NgScope, NgElement, DialogService, TranslateService, Eventbus, UserService, LocalStorageService, PromiseAdapter, PluginsService, ActionExecutor, NgTimeout, DynamicElementsRegistry)
class BaseWidget {
  constructor($scope, element, dialogService, translateService, eventbus, userService, localStorageService, promiseAdapter, pluginsService, actionExecutor, $timeout, dynamicElementsRegistry) {
    this.$scope = $scope;
    this.element = element;
    this.$timeout = $timeout;
    this.control = new WidgetControl(element, this);
    let currentConfig = this.control.getConfig();
    let defaultConfig = {
      showWidgetHeader: this.getShowWidgetHeaderDefault(currentConfig),
      showWidgetBorders: true,
      showWidgetHeaderBorders: true
    };
    this.config = _.defaultsDeep(currentConfig, defaultConfig);
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.eventbus = eventbus;
    this.localStorageService = localStorageService;
    this.userService = userService;
    this.promiseAdapter = promiseAdapter;
    this.pluginsService = pluginsService;
    this.extractContext(element, $scope);
    this.actionExecutor = actionExecutor;
    this.controlActions = [];
    this.subscriptions = [];
    // Close select2 when it's used in widgets. Otherwise dropdowns stay open even when lost focus
    // This happened on mousedown event because dropdown should be closed when user drag scrollbar
    $(this.control.element).on('mousedown', function (event) {
      let select2Container = $(event.target).closest('.select2-container');
      // If mousedown event is over the other select2 element it should stay open
      if (select2Container.length) {
        $('.seip-select').not(select2Container.prev()).select2('close');
      } else {
        $('.seip-select').select2('close');
      }
    });

    this.widgetRemoveHandler = $scope.$on(WidgetRemovedEvent.EVENT_NAME, () => {
      let actualWidgetScope = element.children().scope();
      if (actualWidgetScope) {
        // delete widget and deregister it from all objects it is linked in the shared objects registry
        this.context.getSharedObjectsRegistry().onWidgetDelete(this.control.getId());
        // https://github.com/angular-ui/bootstrap/issues/1128
        // https://github.com/angular/angular.js/issues/5658
        actualWidgetScope.$evalAsync(() => {
          actualWidgetScope.$destroy();
        });
      }
    });

    registerConfigChangedListener(element[0], () => {
      ///title change does not need digest.
      if (this.control.skipChangeHandler) {
        this.control.skipChangeHandler = false;
        return;
      }
      // delete removed properties
      // TODO: remove properties recursively
      let removedProperties = _.difference(Object.keys(this.config), Object.keys(this.control.getConfig()));
      removedProperties.forEach((property) => {
        delete this.config[property];
      });
      _.assign(this.config, this.control.getConfig());

      if (!application.$rootScope.$$phase) {
        application.$rootScope.$digest();
      }
    });

    if (this.context.isPrintMode() || this.config.expanded === undefined) {
      //In print mode we overide this config so that the widgets will be expanded and will be printed properly,
      // or there is no setting so the widget should be expanded.
      this.config.expanded = true;
    }

    this.initWidgetControlActions();
    System.import(this.control.getWidgetModule()).then(() => {
      this.setWidgetId(element);
      if (this.isNewWidget(element)) {
        let openConfigPromise = this.control.skipConfig() ? this.promiseAdapter.resolve() : this.openConfigDialog();
        openConfigPromise.then(() => {
          // the "newly inserted widget" indicator is not needed anymore
          element.removeAttr(NEW_ATTRIBUTE);

          this.renderActualWidget();

          this.positionCaretAtNextEditableArea();
        }, this.remove.bind(this));
      }
    });

    this.forceRender = true;
    this.processLazyLoadRenderEvent(element);

    if (this.context.isPrintMode()) {
      this.element.trigger('render');
    }

    // if widget has id, this means that it must be registered to the element registry.
    if (this.control.getId()) {
      dynamicElementsRegistry.handleWidget(this.element.attr('widget'), this.control.getId());
    }

    this.isWidgetReady = false;
    let widgetReadySubscription = eventbus.subscribe(WidgetReadyEvent, (event) => {
      if (event[0].widgetId === this.control.getId()) {
        this.element.addClass('initialized');
        widgetReadySubscription.unsubscribe();
        this.isWidgetReady = true;
      }
    });

    // locking the undo manager while widget is loading.
    // no updating required, because snapshot is saved immediately after unlock,
    // keeping proper track of snapshot history
    this.$scope.editor.editor.fire('lockSnapshot', {dontUpdate: true});
  }

  /**
   * Obtains showWidgetHeader default value based on current widget config.
   * This is done for backward compatibility with ODW.
   * Until now ODW has config showWidgetBorders, which if not set, hide both widget header and borders.
   * After unification these configurations are separated into two different configurations.
   * For existing ODWs if showWidgetBorders is defined and showWidgetHeader is undefined then both are equal to showWidgetBorders.
   * @param config
   * @returns {boolean}
   */
  getShowWidgetHeaderDefault(config) {
    return config.showWidgetHeader === undefined && config.showWidgetBorders !== undefined ? config.showWidgetBorders : true;
  }

  processLazyLoadRenderEvent(element) {
    this.element.on('render', () => {
      if (this.forceRender) {
        System.import(this.control.getWidgetModule()).then(() => {
          this.setWidgetId(element);
          this.loadUserSettings().then(() => {
            this.renderActualWidget();
          });
        });
        this.forceRender = false;
      } else {
        this.$scope.$evalAsync(() => {
          this.renderActualWidget();
        });
      }
      if (this.config.expanded === false) {
        this.eventbus.publish(new WidgetReadyEvent({
          widgetId: this.control.getId()
        }));
      }
    });
  }

  /**
   * Returns true if widget displays data which is a result of dynamic query
   * @returns {boolean}
   */
  containsDynamicData() {
    return this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY;
  }

  initWidgetControlActions() {
    this.pluginsService.loadPluginServiceModules('widget-control-actions', 'name').then((modules) => {
      let directiveName = this.control.getWidgetSelector();
      let definitions = this.pluginsService.getPluginDefinitions('widget-control-actions', 'name');
      let actions = modules;
      for (let key in actions) {
        //check if given control action handles widget with given directiveName
        if (this.contains(definitions[key].handles, directiveName)) {
          let value = actions[key];
          value.definition = definitions[key];
          if (!value.definition.filter || value.definition.filter(this.context, this.config) === true) {
            this.controlActions.push(value);
          }
        }
      }
    });
  }

  contains(a, b) {
    return a.indexOf(b) !== -1;
  }

  executeAction(item) {
    let widgetData = {};
    widgetData.config = this.config;
    widgetData.context = this.context;
    this.actionExecutor.execute(item, widgetData);
  }

  renderActualWidget() {
    this.loadHeaderExtensions().then((headerExtensionsModules) => {
      const directiveName = this.control.getWidgetSelector();
      const widgetTag = `<${directiveName} control="::baseWidget.control" config="::baseWidget.config" context="::baseWidget.context"></${directiveName}>`;
      let template = this.control.getDefinition().inline ? widgetTag : widgetTemplate.replace('{body}', this.config.expanded ? widgetTag : '');

      if (this.innerScope) {
        this.innerScope.$destroy();
      }
      this.element.empty();
      this.innerScope = this.$scope.$new();

      let compiledHeaderExtensions = this.compileHeaderExtensions(headerExtensionsModules, this.innerScope);

      let compiledElement = application.$compile(template)(this.innerScope);

      this.element.append(compiledElement);
      this.appendHeaderExtensions(compiledHeaderExtensions);

      // a digest is required if the compilation happens outside angularjs digest cycle
      if (!application.$rootScope.$$phase) {
        application.$rootScope.$digest();
      }
    });
  }

  compileHeaderExtensions(headerExtensionsModules, scope) {
    let headerExtensionTemplate = '';
    Object.keys(headerExtensionsModules).forEach((moduleKey) => {
      let module = headerExtensionsModules[moduleKey];
      headerExtensionTemplate += `<${module.name} control="::baseWidget.control" config="::baseWidget.config" context="::baseWidget.context"></${module.name}>`;
    });
    return application.$compile(headerExtensionTemplate)(scope);
  }

  appendHeaderExtensions(compiledHeaderExtensions) {
    this.$timeout(() => {
      let headerExtensionsElement = this.element.find('.header-extensions').eq(0);
      headerExtensionsElement.empty();
      headerExtensionsElement.append(compiledHeaderExtensions);
    });
  }

  loadHeaderExtensions() {
    let filterFunc = (definition) => {
      // if there are explicit widget modes specified in the extension the extension will be loaded only for them
      return (!definition.widgetModes || definition.widgetModes.indexOf(this.getMode()) !== -1) && definition.widgetName === this.control.getName();
    };
    return this.pluginsService.loadComponentModules('widget-header-extensions', 'name', filterFunc);
  }

  loadUserSettings() {
    //If we are in MODE_PRINT we dont need this check, since all widgets need to be expanded.
    if (this.context.isPrintMode()) {
      return this.promiseAdapter.resolve();
    }
    return this.userService.getCurrentUser().then((user) => {
      this.currentUserId = user.id;
      this.idocId = this.context.getId();
      this.widgetId = this.control.getId();
      let storage = this.localStorageService.getJson(LocalStorageService.WIDGET_PREFERENCES, {});
      let userPrefernces = storage[this.currentUserId];
      if (userPrefernces !== undefined) {
        let preference = userPrefernces[this.idocId];
        if (preference !== undefined && preference[this.widgetId] !== undefined) {
          this.config.expanded = preference[this.widgetId].expanded;
        }
      }
    });
  }

  saveConfigWithoutReload(config) {
    this.control.skipChangeHandler = true;
    this.control.saveConfig(config || this.config);
  }

  configure() {
    this.openConfigDialog().then(_.noop, _.noop);
  }

  openConfigDialog() {
    let control = this.control;

    // FIXME: if we use PrimiseAdapter here DTW's config isn't updated
    return new Promise((resolve, reject) => {
      System.import(control.getConfigModule()).then(() => {
        let clonedConfig = _.cloneDeep(this.config);
        let properties = {
          config: clonedConfig,
          definition: control.getDefinition(),
          context: this.context
        };
        // promise shouldn't resolve before the dialog is closed otherwise the editor lose focus
        let shouldReject = true;
        let headerTitle = this.translateService.translateInstantWithInterpolation(WIDGET_CONFIG_LABEL, {
          widgetType: this.translateService.translateInstant(properties.definition.label)
        });
        if (this.config.title) {
          headerTitle = headerTitle.concat(': ', this.config.title);
        }

        let dialogConfig = {
          modalCls: control.getConfigSelector(),
          header: headerTitle,
          helpTarget: 'widget.' + properties.definition.name,
          buttons: [{
            id: DialogService.OK,
            label: 'idoc.widget.config.button.save',
            cls: 'btn-primary',
            onButtonClick(buttonId, componentScope, dialogConfig) {
              dialogConfig.dismiss();
              control.onConfigConfirmed(clonedConfig);
              control.saveConfig(clonedConfig);
              shouldReject = false;
            }
          }, {
            id: DialogService.CANCEL,
            label: 'idoc.widget.config.button.cancel'
          }],
          onButtonClick(buttonId, componentScope, dialogConfig) {
            dialogConfig.dismiss();
          },
          onClose: () => {
            if (shouldReject) {
              setTimeout(() => {
                // if config dialog is closed without creating new widget, unlock the undo manager
                this.$scope.editor.editor.fire('unlockSnapshot');
              }, 0);
              reject();
            } else {
              resolve();
            }
          }
        };

        this.dialogService.create(control.getConfigSelector(), properties, dialogConfig);
      });
    });
  }

  remove() {
    this.positionCaretAtNextEditableArea();
    this.$scope.$broadcast(WidgetRemovedEvent.EVENT_NAME, new WidgetRemovedEvent());
    let editor = this.control.getEditor();
    // there is no editor in some sandbox pages
    if (editor) {
      let widgetsRepository = editor.editor.widgets;
      widgetsRepository.del(widgetsRepository.getByElement(new CKEDITOR.dom.element(this.element.get(0))));
    }
    this.ngOnDestroy();
  }

  /**
   * The expression for the widget context is passed in the 'context' attribute.
   * Since the widget directive is implemented as an attribute directive, it must manually
   * evaluate the expression passed to the context attribute onto current scope.
   *
   * @param element widget element
   * @param scope widget scope
   */
  extractContext(element, scope) {
    // allows providing of context outside of the widget. I.e. for testing purposes
    let contextAttribute = element.attr('context');
    if (!_.isUndefined(contextAttribute)) {
      this.context = scope.$eval(contextAttribute);
    }

    // gets the context from the parent component
    if (!this.context) {
      // currently idocTabBody is the parent of the tab content and the context should be get from it
      // if the immediate parent changes, this code should also be changed
      let contextExpression = '::editor.context';
      this.context = scope.$eval(contextExpression);
    }
  }

  isNewWidget(element) {
    return element.attr(NEW_ATTRIBUTE);
  }

  setWidgetId(element) {
    if (!element.attr('id')) {
      element.attr('id', shortid.generate());
    }
  }

  getWidgetPanelClasses() {
    let widgetMode = this.getMode();
    let widgetPanelClasses = [`mode-${widgetMode}`];

    if (!this.config.showWidgetHeader) {
      widgetPanelClasses.push('no-header');
    }

    if (!this.config.showWidgetHeaderBorders) {
      widgetPanelClasses.push('no-header-borders');
    }

    if (!this.config.showWidgetBorders) {
      widgetPanelClasses.push('no-borders');
    }

    if (this.config.hideIcons) {
      widgetPanelClasses.push('no-icons');
    }
    return widgetPanelClasses;
  }

  /**
   * Toggles between the widget settings for expanding and collapsing.
   */
  toggleExpand() {
    this.config.expanded = !this.config.expanded;
    if (this.context.isEditMode()) {
      this.control.skipChangeHandler = true;
      this.control.saveConfig(this.config);
    }

    this.control.publish('widgetExpanded', this.config.expanded);
    this.renderActualWidget();
    this.saveUserPrefernces();
  }

  saveUserPrefernces() {
    let storage = _.set({}, [this.currentUserId, this.idocId, this.widgetId, 'expanded'], this.config.expanded);
    this.localStorageService.mergeValues(LocalStorageService.WIDGET_PREFERENCES, storage);
  }

  /**
   * Gets widget mode which may be different than idoc context mode.
   * There are planned two more different modes:
   *  - document is in edit mode but tab is locked and only widgets' content can be edited.
   *  - document is in preview mode but certain widget can be in edit mode i.e. its content and configuration can be edited.
   * @returns {*}
   */
  getMode() {
    let widgetMode = this.context.getMode();
    // editor might be missing in some sandboxes
    let editor = this.control.getEditor();
    if (this.context.isEditMode() && editor && editor.tab.locked) {
      widgetMode = MODE_EDIT_LOCKED;
    }
    return widgetMode;
  }

  positionCaretAtNextEditableArea() {
    if (this.control.getEditor()) {
      let editor = this.control.getEditor().editor;
      editor.fire('lockSnapshot', {dontUpdate: true});
      let range = editor.createRange();
      let editorEl = new CKEDITOR.dom.element(this.element.parent().get(0));
      range.moveToClosestEditablePosition(editorEl, true);
      editor.getSelection().selectRanges([range]);
      editor.focus();
      editor.fire('unlockSnapshot');
    }
  }

  /**
   * Returns styles for background color and color. Color is either black or white calculated for best contrast with background color.
   * @param backgroundColor
   * @returns {*} an object containing backgroundColor and color.
   */
  getColorStyle(backgroundColor) {
    let colorStyle;
    if (!this.context.isPrintMode() && backgroundColor) {
      colorStyle = {
        backgroundColor,
        color: ColorPicker.getContrastBWColor(backgroundColor)
      };
    }
    return colorStyle;
  }

  ngOnDestroy() {
    _.forEach(this.subscriptions, function (subscription) {
      subscription.unsubscribe();
    });
    this.element.off('render');
    $(this.control.element).off('mousedown');

    // Save snapshot when widget is removed. This is needed for undo/redo operations
    if (this.$scope.editor) {
      this.$scope.editor.editor.fire('saveSnapshot', {force: true});
    }
    this.widgetRemoveHandler();
  }
}

function registerConfigChangedListener(element, listenerFunction) {
  let observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'config') {
        listenerFunction.call();
      }
    });
  });

  observer.observe(element, {
    attributes: true,
    childList: false,
    characterData: false
  });
}

export class WidgetControl extends EventEmitter {
  constructor(element, widget) {
    super();
    this.element = element;
    this.onConfigConfirmed = _.noop;
    this.baseWidget = widget;
  }

  getName() {
    return this.element.attr('widget');
  }

  getWidgetModule() {
    return this.getBefore('class');
  }

  getWidgetClass() {
    return this.getAfter('class');
  }

  getWidgetSelector() {
    return _.kebabCase(this.getWidgetClass());
  }

  getConfigModule() {
    return this.getBefore('config');
  }

  getConfigClass() {
    return this.getAfter('config');
  }

  getConfigSelector() {
    return _.kebabCase(this.getConfigClass());
  }

  skipConfig() {
    return this.getDefinition()['skipConfig'];
  }

  getBefore(propertyName) {
    let lastSeparator = this.getDefinition()[propertyName].lastIndexOf('/');
    return this.getDefinition()[propertyName].substr(0, lastSeparator);
  }

  getAfter(propertyName) {
    let lastSeparator = this.getDefinition()[propertyName].lastIndexOf('/');
    return this.getDefinition()[propertyName].substr(lastSeparator + 1);
  }

  getId() {
    return this.element.attr('id');
  }

  getDefinition() {
    return WidgetRegistry.getWidgets()[this.getName()];
  }

  saveValue(value) {
    this.storeDataInAttribute(value, 'value');
  }

  getValue() {
    return this.getDataFromAttribute('value');
  }

  saveConfig(config) {
    this.storeDataInAttribute(config, 'config');
    this.publish('configSaved', config);
  }

  getBaseWidget() {
    return this.baseWidget;
  }

  getConfig() {
    return this.getDataFromAttribute('config');
  }

  setDataValue(config) {
    this.storeDataInAttribute(config, 'data-value');
  }

  getDataValue() {
    return this.getDataFromAttribute('data-value');
  }

  storeDataInAttribute(data, attr) {
    if (_.isEmpty(data)) {
      this.element.removeAttr(attr);
    } else {
      this.element.attr(attr, this.serialize(data));
    }
  }

  getDataFromAttribute(attr) {
    return this.deserialize(this.element.attr(attr));
  }

  getEditor() {
    return this.baseWidget.$scope.editor;
  }

  /**
   * Encodes a js object for storing it in html attribute.
   * @return Serialized object or null if the value is not defined, null or is an empty object.
   */
  serialize(value) {
    return base64.encode(JSON.stringify(value));
  }

  /**
   * Decodes a previously encoded js object. Null-safe - returns an empty object
   * if the config attribute is not present.
   */
  deserialize(value) {
    if (value) {
      return JSON.parse(base64.decode(value));
    }

    return {};
  }
}

BaseWidget.FORM_VIEW_MODE_EDIT = 'EDIT';
BaseWidget.FORM_VIEW_MODE_PREVIEW = 'PREVIEW';
BaseWidget.NO_BORDER = 'no-border';
