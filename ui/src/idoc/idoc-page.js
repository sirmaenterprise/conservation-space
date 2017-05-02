import {View, Component, Inject, NgElement, NgScope, NgTimeout, NgInterval} from 'app/app';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import './idoc-tab-body';
import 'instance-header/instance-header';
import {UrlUtils} from 'common/url-utils';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {ActionsMenu} from 'idoc/actions-menu/actions-menu';
import {ConfigureIdocTabs} from 'idoc/idoc-tabs/configure-idoc-tabs';
import {Toolbar} from 'components/toolbars/toolbar';
//TODO this is not used anymore and might be removed
import 'idoc/info-area/info-area';
import {TOPIC as INFO_AREA_TOPIC} from 'idoc/info-area/info-area';
import 'components/ui-preference/ui-preference';
import 'idoc/idoc-tabs/idoc-tabs';
import 'idoc/sidebar/sidebar';
import {TabsConfig} from 'idoc/idoc-tabs/idoc-tabs-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {Router} from 'adapters/router/router';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import uuid from 'common/uuid';
import {NOT_FOUND} from 'error/error-page';
import {ActionExecutor} from 'services/actions/action-executor';
import 'idoc/template/select-idoc-template';
import {SaveIdocAndPreviewAction} from 'idoc/actions/save-idoc-and-preview-action';
import {SaveIdocAndContinueAction} from 'idoc/actions/save-idoc-and-continue-action';
import {CancelSaveIdocAction} from 'idoc/actions/cancel-save-idoc-action';
import {EditIdocAction} from 'idoc/actions/edit-idoc-action';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER, MODE_PREVIEW, STATE_PARAM_TAB} from 'idoc/idoc-constants';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Logger} from 'services/logging/logger';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {PermissionsChangedEvent} from 'idoc/system-tabs/permissions/permissions-changed-event';
import {BeforeIdocContentModelUpdateEvent} from 'idoc/events/before-idoc-content-model-update-event';
import {IdocContentModelUpdateEvent} from 'idoc/events/idoc-content-model-update-event';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {LOCK, UNLOCK} from 'idoc/actions/action-constants';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {CustomEventDispatcher} from 'services/dom/custom-event-dispatcher';
import 'components/help/contextual-help';
import {HelpService, HELP_INSTANCE_TYPE} from 'services/help/help-service';
import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import _ from 'lodash';
import 'font-awesome/css/font-awesome.css!';
import base64 from 'common/lib/base64';
import template from './idoc-page.html!text';

export const STATE_PARAM_MODE = 'mode';
export const STATE_PARAM_ID = 'id';
export const PERMISSIONS_TAB_ID = 'permissions-tab';

// CKEditor generates empty paragraphs after each widget or at the end of each layout column with one of the following contents
const EMPTY_PARAGRAPH_CONTENTS = ['&nbsp;', '<br>'];
const VERSION_ORIGINAL_INSTANCE_MESSAGE_ID = 'version-original-instance-message';
// An array containing selectors (class) of widgets that does not support versioning yet and should work as they work in the current instance
const UNVERSIONED_WIDGETS = ['aggregated-table'];

@Component({
  selector: 'idoc-page'
})
@View({template})
@Inject(NgElement, NgScope, Eventbus, StateParamsAdapter, LocationAdapter, Router,
  ActionExecutor, SaveIdocAndPreviewAction, SaveIdocAndContinueAction, CancelSaveIdocAction, EditIdocAction, NgTimeout, IdocContextFactory, NotificationService,
  TranslateService, Logger, ActionsService, NgInterval, IdocDraftService, PromiseAdapter, Configuration, PluginsService, HelpService, DynamicElementsRegistry,
  CustomEventDispatcher, ModelingIdocContextBuilder)
export class IdocPage {

  constructor($element, $scope, eventbus, stateParamsAdapter, locationAdapter, router,
              actionExecutor, saveIdocAndPreviewAction, saveIdocAndContinueAction, cancelSaveIdocAction, editIdocAction, $timeout, idocContextFactory, notificationService,
              translateService, logger, actionsService, $interval, idocDraftService, promiseAdapter, configuration, pluginsService, helpService, dynamicElementsRegistry,
              customEventDispatcher, modelingIdocContextBuilder) {
    this.$element = $element;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$interval = $interval;
    this.eventbus = eventbus;
    this.actionsService = actionsService;
    this.editAllowed = false;
    this.idocDraftService = idocDraftService;

    this.notificationService = notificationService;
    this.translateService = translateService;
    this.logger = logger;

    this.stateParamsAdapter = stateParamsAdapter;
    this.locationAdapter = locationAdapter;
    this.router = router;
    this.actionExecutor = actionExecutor;
    this.saveIdocAndPreviewAction = saveIdocAndPreviewAction;
    this.saveIdocAndContinueAction = saveIdocAndContinueAction;
    this.cancelSaveIdocAction = cancelSaveIdocAction;
    this.editIdocAction = editIdocAction;
    this.promiseAdapter = promiseAdapter;
    this.configuration = configuration;
    this.pluginsService = pluginsService;
    this.dynamicElementsRegistry = dynamicElementsRegistry;

    // Get the mode parameter trough the utils where the url is decoded first. The state params adapter gets confused
    // when it is provided with encoded url (for example when idoc is printed it is rendered trough phantomjs where the
    // page url is encoded and the angular state service brakes because of this.
    // TODO: Probably we have to change this everywhere the state service is used!
    let viewMode = UrlUtils.getParameter(locationAdapter.url(), STATE_PARAM_MODE);
    this.context = idocContextFactory.createNewContext(this.stateParamsAdapter.getStateParam(STATE_PARAM_ID), viewMode);
    this.wrappedContext = modelingIdocContextBuilder.wrapIdocContext(this.context);

    this.headerType = HEADER_DEFAULT;
    this.isHeaderVisible = true;

    this.disableEditButton = false;

    this.fixedContainerUIPreferenceConfig = this.generateUIPreferenceConfig();
    this.idocBodyUIPreferenceConfig = this.generateUIPreferenceConfig();
    this.idocBodyUIPreferenceConfig.sourceElements.top = '.idoc-wrapper .fixed-container';

    // Probably when router lib gets updated, this fix won't be needed anymore! Until then we should unsubscribe from
    // this event to prevent multiple invocation of the refresh function.
    if (this.instanceRefreshEventSubscription) {
      this.eventbus.unsubscribe(this.instanceRefreshEventSubscription);
    }
    this.instanceRefreshEventSubscription = this.eventbus.subscribe(InstanceRefreshEvent, (data) => {
      this.refresh(data[0].actionDefinition, data[0].response.data);
    });

    this.events = [
      this.instanceRefreshEventSubscription,
      this.eventbus.subscribe(IdocReadyEvent, () => {
        this.idocIsReady = true;
        window.status = 'export-ready';
        customEventDispatcher.dispatchEvent(document.body, 'export-ready');
      }),
      this.eventbus.subscribe(PermissionsChangedEvent, () => {
        this.checkPermissionsForEditAction();
      })
    ];

    this.loadData().then((currentObject) => {
      this.currentObject = currentObject;
      this.actionContext = {
        // used only for save/edit/cancel operations
        idocPageController: this,
        // used only for save/edit/cancel operations
        idocContext: this.context,
        // next action context properties are those that are used and must be passed for the actions menu
        currentObject: this.currentObject,
        scope: this.$scope,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER,
        renderMenu: () => {
          return this.context.isPreviewMode();
        }
      };

      this.tabsConfig = new TabsConfig(eventbus, this.currentObject);

      this.tabsConfig.activeTabId = UrlUtils.getUrlFragment(this.locationAdapter.url());
      if (this.tabsConfig.activeTabId) {
        this.requestedTabs = [this.tabsConfig.activeTabId];
      } else {
        this.requestedTabs = UrlUtils.getParameterArray(this.locationAdapter.url(), STATE_PARAM_TAB);
        if (this.requestedTabs) {
          this.tabsConfig.activeTabId = this.requestedTabs[0];
        }
      }

      if (this.currentObject.isPersisted()) {
        let content = this.currentObject.getContent();
        if (this.currentObject.isVersion()) {
          // update content if there are dynamic queries
          let queriesResults = this.currentObject.getPropertyValue('queriesResults');
          let manuallySelected = this.currentObject.getPropertyValue('manuallySelected');
          if (queriesResults || manuallySelected) {
            content = this.updateContentForVersion(content, queriesResults ? JSON.parse(queriesResults) : {}, manuallySelected ? JSON.parse(manuallySelected) : {});
          }
          this.$timeout(() => {
            eventbus.publish({
              channel: this.context.getUUID(),
              topic: INFO_AREA_TOPIC,
              data: {
                id: VERSION_ORIGINAL_INSTANCE_MESSAGE_ID,
                message: this.translateService.translateInstantWithInterpolation('version.original.instance.message',
                  {originalInstanceURL: UrlUtils.buildIdocUrl(this.currentObject.getPropertyValue('originalInstanceId'))})
              }
            });
          }, 0);
        }
        this.appendContent(content);
      }

      this.checkPermissionsForEditAction().then(() => {
        if (this.context.isEditMode() && this.currentObject.isPersisted()) {
          this.idocDraftService.loadDraft(this.context).then((result) => {
            if (result.loaded) {
              this.appendContent(result.content);
            }
            this.startDraftInterval();
            this.eventbus.publish(new AfterIdocLoadedEvent(this.actionContext));
          });
        } else {
          this.eventbus.publish(new AfterIdocLoadedEvent(this.actionContext));
        }
      });

      this.configureContextualHelp(this.currentObject, helpService);
    });

    this.updateContentCallback = (content, templateInstance) => {
      // surrogate templates like the blank template don't have id
      if (templateInstance.templateInstanceId) {
        this.currentObject.getModels().validationModel.hasTemplate.value = [];

        this.currentObject.getModels().validationModel.hasTemplate.value.push({
          id: templateInstance.templateInstanceId
        });
      }
      this.appendContent(content);
      this.currentObject.setContent(content);
    };
  }

  refresh(actionDefinition, response) {
    if (actionDefinition) {
      this.notificationService.success(this.translateService.translateInstant('action.notification.success') + actionDefinition.label);
    }

    if (response.content) {
      this.currentObject.setContent(response.content);
    }
    this.context.reloadObjectDetails(response.id)
      .then(() => this.afterInstanceRefreshHandler && this.afterInstanceRefreshHandler())
      .catch(this.logger.error);
  }

  loadData() {
    return this.loadCurrentObject().then((currentObject) => {
      if (currentObject.isPersisted() && !currentObject.isVersion()) {
        return this.loadSystemTabs().then(() => {
          return currentObject;
        });
      } else {
        return currentObject;
      }
    });
  }

  loadSystemTabs() {
    this.systemTabs = [];
    return this.pluginsService.loadComponentModules('idoc-system-tabs').then((modules) => {
      Object.keys(modules).forEach((moduleId) => {
        let module = modules[moduleId];
        let component = module['component'];
        let tab = {
          id: module['id'],
          title: this.translateService.translateInstant(module['name']),
          system: true,
          locked: true,
          content: '<' + component + ' context="idocTabBody.context"></' + component + '>'
        };
        this.systemTabs.push(tab);
      });
    });
  }

  loadCurrentObject() {
    return this.context.getCurrentObject().then((currentObject) => {

      if (!currentObject.getModels()) {
        if (this.context.isEditMode()) {
          this.router.navigate('error', {'key': NOT_FOUND});
        } else {
          // idoc is opened without instance. Happens for path #/idoc/
          this.router.navigate('userDashboard');
          return this.promiseAdapter.reject();
        }
      }

      return currentObject;
    });
  }

  startDraftInterval() {
    this.stopDraftInterval();
    this.draftInterval = this.$interval(() => {
      this.idocDraftService.saveDraft(this.context, this.getIdocContent());
    }, this.configuration.get(Configuration.DRAFT_AUTOSAVE_INTERVAL) || 60000);
  }

  stopDraftInterval() {
    if (this.draftInterval) {
      this.$interval.cancel(this.draftInterval);
      this.draftInterval = undefined;
    }
  }

  /**
   * Generate tabs and append iDoc content
   *
   * @param content either from template or from saved iDoc
   */
  appendContent(content) {
    this.disableSaveButton(true);
    let isPrintMode = this.context.isPrintMode();
    let templateDom = $(content);
    let activeTabExist = false;

    this.tabsConfig.tabs.splice(0, this.tabsConfig.tabs.length);
    this.tabsConfig.activeTabId = UrlUtils.getUrlFragment(this.locationAdapter.url());
    let sectionsContent = templateDom.children('section');
    this.setTabsCounter(templateDom, sectionsContent);

    this.dynamicElementsRegistry.reload(templateDom, isPrintMode && this.requestedTabs);

    sectionsContent.each((index, section) => {
      let tab = this.buildTabModel(section);
      // set the default tab as active tab
      if (!this.tabsConfig.activeTabId && tab.default) {
        this.tabsConfig.activeTabId = tab.id;
      }
      let isCurrentActiveTab = this.tabsConfig.activeTabId === tab.id;
      // All added tabs should be rendered in print mode
      if (isCurrentActiveTab || isPrintMode) {
        tab.shouldRender = true;
      }
      activeTabExist = activeTabExist || isCurrentActiveTab;
      // If mode is not print all tabs should be added
      // If mode is print and there are no requested tabs all tabs should be added
      // If mode is print and there are requested tabs only requested tabs should be added
      if (!isPrintMode || !this.requestedTabs || this.requestedTabs.indexOf(tab.id) !== -1) {
        this.tabsConfig.tabs.push(tab);
      }
    });
    if (!isPrintMode && this.systemTabs && this.systemTabs.length) {
      for (let systemTab of this.systemTabs) {
        this.tabsConfig.tabs.push(_.clone(systemTab));
        activeTabExist = activeTabExist || this.tabsConfig.activeTabId === systemTab.id;
      }
    }
    this.setDefaultActiveTab(activeTabExist);
    this.disableSaveButton(false);

    // preventing big detached DOM memory leak
    templateDom.remove();
  }

  /**
   * Updates idoc content to prepare it when displaying a version.
   * Automatically selected objects are changed with manually selected versioned URIs.
   * Manually selected objects are changed with their versioned URIs.
   * @param content
   * @param queriesMap
   * @param uriToVersionUriMap
   * @returns {*}
   */
  updateContentForVersion(content, queriesMap, uriToVersionUriMap) {
    let contentDOM = $(content);
    let widgets = contentDOM.find('.widget');
    widgets.each((index, widget) => {
      let widgetConfigEncoded = $(widget).attr('config');
      if (widgetConfigEncoded && this.isWidgetVersioned($(widget))) {
        let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
        if (SELECT_OBJECT_MANUALLY === widgetConfig.selectObjectMode) {
          if (widgetConfig.selectedObjects) {
            widgetConfig.selectedObjects = widgetConfig.selectedObjects.map((selectedObjectURI) => {
              return uriToVersionUriMap[selectedObjectURI] || selectedObjectURI;
            });
          } else if (widgetConfig.selectedObject) {
            widgetConfig.selectedObject = uriToVersionUriMap[widgetConfig.selectedObject] || widgetConfig.selectedObject;
          }
        } else if (SELECT_OBJECT_AUTOMATICALLY === widgetConfig.selectObjectMode && widgetConfig.criteria) {
          if (widgetConfig.selection === SINGLE_SELECTION && queriesMap[widgetConfig.criteria.id] && queriesMap[widgetConfig.criteria.id].length === 1) {
            widgetConfig.selectedObject = queriesMap[widgetConfig.criteria.id][0];
          } else {
            widgetConfig.selectedObjects = queriesMap[widgetConfig.criteria.id] || [];
          }
          widgetConfig.selectObjectMode = SELECT_OBJECT_MANUALLY;
        }
        $(widget).attr('config', base64.encode(JSON.stringify(widgetConfig)));
      }
    });
    return contentDOM.prop('outerHTML');
  }

  /**
   * Check if widget is eligible for versioning. If not widget works as in the current instance.
   * @param widgetDOM
   * @returns {boolean}
   */
  isWidgetVersioned(widgetDOM) {
    return _.findIndex(UNVERSIONED_WIDGETS, (widgetClassToBeSkipped) => {
        return widgetDOM.hasClass(widgetClassToBeSkipped);
      }) === -1;
  }

  buildTabModel(htmlSectionContent) {
    let jqSection = $(htmlSectionContent);

    if (this.context.isPrintMode()) {
      this.trimTrailingEmptyParagraphs(jqSection);
    }

    return {
      'id': jqSection.data('id') || uuid(),
      'title': jqSection.data('title'),
      'default': !!jqSection.data('default'),
      'showNavigation': !!jqSection.data('show-navigation'),
      'showComments': this.currentObject.isVersion() ? false : !!jqSection.data('show-comments'),
      'revision': jqSection.data('revision') || ConfigureIdocTabs.DEFAULT_REVISION,
      'locked': !!jqSection.data('locked'),
      'content': jqSection.html()
    };
  }

  /**
   * Removes empty paragraphs which are automatically added by CKEditor at the end of each layout column and at the end of the document.
   * This method trim such paragraphs to avoid them being printed on a new blank page in certain situations.
   * @param sectionElement current tab's section element
   */
  trimTrailingEmptyParagraphs(sectionElement) {
    // Remove last empty paragraph from each layout column
    sectionElement.find('.layout-column-editable').each((index, layoutColumn) => {
      this.trimTrailingEmptyParagraph($(layoutColumn));
    });
    // remove last empty paragraph from the tab
    this.trimTrailingEmptyParagraph(sectionElement);
  }

  trimTrailingEmptyParagraph(parentElement) {
    let lastParagraph = parentElement.find(' > p:last-child');
    if (lastParagraph.length > 0 && EMPTY_PARAGRAPH_CONTENTS.indexOf(lastParagraph.html()) !== -1) {
      lastParagraph.remove();
    }
  }

  setTabsCounter(templateDom, sections) {
    var tabsCounter = templateDom.data('tabs-counter');
    if (!Number.isInteger(tabsCounter)) {
      tabsCounter = sections.length + 1;
    }
    this.tabsConfig.tabsCounter = tabsCounter;
  }

  getIdocContent() {
    this.eventbus.publish(new BeforeIdocContentModelUpdateEvent());
    this.eventbus.publish(new IdocContentModelUpdateEvent());
    this.eventbus.publish(new AfterIdocContentModelUpdateEvent());
    let content = `<div data-tabs-counter="${this.tabsConfig.tabsCounter}">`;
    this.tabsConfig.tabs.forEach(tab => {
      if (!tab.system) {
        content += `<section data-id="${tab.id}" data-title="${tab.title}" data-default="${tab.default}" ` +
          `data-show-navigation="${tab.showNavigation}" data-show-comments="${tab.showComments}" data-revision="${tab.revision}" data-locked="${tab.locked}">${tab.content}</section>`;
      }
    });
    content += '</div>';
    return content;
  }

  /**
   * Sets default active tab to first tab if current active tab does not exist
   * @param activeTabExist true if current active tab exists
   */
  setDefaultActiveTab(activeTabExist) {
    // if there is no active tab or active tab is set to a non existing tab then set it to the first tab
    if (this.tabsConfig.tabs.length > 0 && (!this.tabsConfig.activeTabId || !activeTabExist)) {
      this.tabsConfig.activeTabId = this.tabsConfig.tabs[0].id;
    }
  }

  setViewMode(mode) {
    this.context.setMode(mode);
    this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, mode);
  }

  /**
   * Toggles the idoc header section
   */
  toggleHeader() {
    this.isHeaderVisible = !this.isHeaderVisible;
  }

  checkPermissionsForEditAction() {
    // don't check when creating new idoc
    if (this.currentObject.isPersisted()) {
      this.editAllowed = false;
      let config = ActionsMenu.getActionsLoaderConfig(this.actionContext.currentObject, this.actionContext.placeholder);

      return this.actionsService.getActions(this.currentObject.id, config).then((response) => {
        let actions = response.data;

        for (let action of actions) {
          if (action.userOperation === EditIdocAction.NAME && !action.disabled) {
            this.editAllowed = true;
            break;
          }
        }
        // if someone is not allowed to edit and tries to change mode to edit in the url, redirect to preview mode
        if (!this.editAllowed && this.context.isEditMode()) {
          this.setViewMode(MODE_PREVIEW);
          this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {
            notify: true,
            skipRouteInterrupt: true
          });
          return this.promiseAdapter.resolve();
        } else if (this.context.isEditMode() && !this.currentObject.isLocked()) {
          return this.actionsService.lock(this.currentObject.getId(), {operation: LOCK}).then(() => {
            return this.context.reloadObjectDetails(this.currentObject.getId())
              .then(() => this.afterInstanceRefreshHandler && this.afterInstanceRefreshHandler())
              .catch(this.logger.error);
          });
        }
      });
    } else {
      return this.promiseAdapter.resolve();
    }
  }

  configureContextualHelp(currentObject, helpService) {
    var models = currentObject.getModels();
    if (models.definitionId && models.instanceType !== HELP_INSTANCE_TYPE) {
      var helpTarget = `object.${models.definitionId}`;
      var instanceId = helpService.getHelpInstanceId(helpTarget);
      if (instanceId) {
        this.helpTarget = helpTarget;
      }
    }
  }

  disableSaveButton(state) {
    $('.seip-btn-save, .seip-btn-just-save').attr('disabled', state);
    this.animateSaveButton(state);
  }

  animateSaveButton(state) {
    let saveButtonSpan = $('.seip-btn-save span, .seip-btn-just-save span');
    saveButtonSpan.each((index, element) => {
      let span = $(element);
      if (state) {
        span.css('width', span.width());
        span.text('');
        span.attr('class', 'fa fa-spinner fa-spin');
      } else {
        span.text(this.translateService.translateInstant(element.dataset.label));
        span.attr('class', '');
      }
    });
  }

  showTab(tab) {
    return !this.context.isPrintMode() && this.tabsConfig.activeTabId === tab.id;
  }

  generateUIPreferenceConfig() {
    return {
      sourceElements: {
        left: '.idoc-wrapper .sidebar'
      },
      fillAvailableWidth: true
    };
  }

  ngOnDestroy() {
    if (this.context.isEditMode() && this.currentObject.isPersisted()) {
      this.actionsService.unlock(this.currentObject.getId(), {operation: UNLOCK});
    }
    for (let event of this.events) {
      event.unsubscribe();
    }

    this.stopDraftInterval();
    this.dynamicElementsRegistry.destroy();
  }
}
