import _ from 'lodash';
import {Component, Inject, NgScope, NgElement, Event} from 'app/app';
import application from 'app/app';
import base64 from 'common/lib/base64';
import shortid from 'shortid';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {UserService} from 'services/identity/user-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {ActionExecutedEvent} from 'services/actions/events';
import {SaveIdocAndContinueAction} from 'idoc/actions/save-idoc-and-continue-action';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';

import widgetTemplate from './widget.html!text';
import 'font-awesome/css/font-awesome.css!';
import './widget.css!css';

const WIDGET_CONFIG_LABEL = 'widget.config.label';
const MODE_EDIT_LOCKED = 'edit-locked';

export var NEW_ATTRIBUTE = 'data-new';

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

export var WidgetRegistry = {
  getWidgets: _.memoize(function () {
    var widgets = [];
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
@Inject(NgScope, NgElement, DialogService, TranslateService, Eventbus, UserService, LocalStorageService, PromiseAdapter, PluginsService, ActionExecutor)
class BaseWidget {
  constructor($scope, element, dialogService, translateService, eventbus, userService, localStorageService, promiseAdapter, pluginsService, actionExecutor) {
    this.$scope = $scope;
    this.element = element;
    this.control = new WidgetControl(element, this);
    this.config = this.control.getConfig();
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

    $scope.$on(WidgetRemovedEvent.EVENT_NAME, () => {
      var actualWidgetScope = element.children().scope();
      if (actualWidgetScope) {
        // delete widget and deregister it from all objects it is linked in the shared objects registry
        this.context.getSharedObjectsRegistry().onWidgetDelete(this.control.getId());
        actualWidgetScope.$destroy();
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
        this.openConfigDialog().then(() => {
          // the "newly inserted widget" indicator is not needed anymore
          element.removeAttr(NEW_ATTRIBUTE);

          this.renderActualWidget();

          this.positionCaretAtNextEditableArea();
        }, this.remove.bind(this));
      } else {
        this.loadUserSettings().then(() => {
          this.renderActualWidget();
          //This event needs to be published, since the widget is not visible,
          //and it will not fire the event, which  will block the save option
          if (this.config.expanded === false) {
            this.eventbus.publish(new WidgetReadyEvent({
              widgetId: this.control.getId()
            }));
          }
        });
      }
    });

    this.actionExecutedListener = eventbus.subscribe(ActionExecutedEvent, (action) => {
      if (action.action instanceof SaveIdocAndContinueAction && this.containsDynamicData()) {
        this.renderActualWidget();
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
          this.controlActions.push(value);
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
    const directiveName = this.control.getWidgetSelector();
    const widgetTag = `<${directiveName} control="::baseWidget.control" config="::baseWidget.config" context="::baseWidget.context"></${directiveName}>`;
    var template = this.control.getDefinition().inline ? widgetTag : widgetTemplate.replace('{body}', this.config.expanded ? widgetTag : '');

    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.element.empty();
    this.innerScope = this.$scope.$new();

    var compiledElement = application.$compile(template)(this.innerScope);

    this.element.append(compiledElement);

    // a digest is required if the compilation happens outside angularjs digest cycle
    if (!application.$rootScope.$$phase) {
      application.$rootScope.$digest();
    }
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
    var control = this.control;

    // FIXME: if we use PrimiseAdapter here DTW's config isn't updated
    return new Promise((resolve, reject) => {
      System.import(control.getConfigModule()).then(() => {
        var clonedConfig = _.cloneDeep(this.config);
        var properties = {
          config: clonedConfig,
          definition: control.getDefinition(),
          context: this.context
        };
        // promise shouldn't resolve before the dialog is closed otherwise the editor lose focus
        var shouldReject = true;
        var dialogConfig = {
          modalCls: control.getConfigSelector(),
          header: this.translateService.translateInstant(WIDGET_CONFIG_LABEL),
          helpTarget: 'widget.' + properties.definition.name,
          buttons: [{
            id: DialogService.OK,
            label: 'idoc.widget.config.button.save',
            cls: 'btn-primary',
            onButtonClick: function (buttonId, componentScope, dialogConfig) {
              dialogConfig.dismiss();
              control.onConfigConfirmed(clonedConfig);
              control.saveConfig(clonedConfig);
              shouldReject = false;
            }
          }, {
            id: DialogService.CANCEL,
            label: 'idoc.widget.config.button.cancel'
          }],
          onButtonClick: function (buttonId, componentScope, dialogConfig) {
            dialogConfig.dismiss();
          },
          onClose: () => {
            if (shouldReject) {
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
    this.$scope.$broadcast(WidgetRemovedEvent.EVENT_NAME, new WidgetRemovedEvent());

    this.element.off();

    this.positionCaretAtNextEditableArea();

    // The parent of the widget is CKEditor widget wrapper (.cke_widget_wrapper) and it should also be removed
    // in order to completely remove the widget
    this.element.parent().remove();
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
    var contextAttribute = element.attr('context');
    if (!_.isUndefined(contextAttribute)) {
      this.context = scope.$eval(contextAttribute);
    }

    // gets the context from the parent component
    if (!this.context) {
      // currently idocTabBody is the parent of the tab content and the context should be get from it
      // if the immediate parent changes, this code should also be changed
      var contextExpression = '::editor.context';
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

  getWidgetPanelClass() {
    if (this.config.showWidgetBorders !== undefined && !this.config.showWidgetBorders && this.context.isPreviewMode()) {
      return BaseWidget.NO_BORDER;
    }
  }

  /**
   * Toggles between the widget settings for expanding and collapsing.
   */
  toggleExpand() {
    this.config.expanded = !this.config.expanded;
    if (this.context.isEditMode()) {
      this.control.saveConfig(this.config);
    }
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
      let range = editor.createRange();
      let editorEl = new CKEDITOR.dom.element(this.element.parent().get(0));
      range.moveToClosestEditablePosition(editorEl, true);
      editor.getSelection().selectRanges([range]);
      editor.focus();
    }
  }

  ngOnDestroy() {
    this.actionExecutedListener.unsubscribe();
  }
}

function registerConfigChangedListener(element, listenerFunction) {
  var observer = new MutationObserver(mutations => {
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

export class WidgetControl {
  constructor(element, widget) {
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

  getBefore(propertyName) {
    var lastSeparator = this.getDefinition()[propertyName].lastIndexOf('/');
    return this.getDefinition()[propertyName].substr(0, lastSeparator);
  }

  getAfter(propertyName) {
    var lastSeparator = this.getDefinition()[propertyName].lastIndexOf('/');
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
    return this.baseWidget.$scope.$parent.editor;
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