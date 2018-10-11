import {View, Component, Inject, NgElement, NgScope, NgTimeout, NgInterval} from 'app/app';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import {SessionStorageService} from 'services/storage/session-storage-service';
import './idoc-tab-body';
import 'instance-header/instance-header';
import {UrlUtils} from 'common/url-utils';
import {ModelUtils} from 'models/model-utils';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {ConfigureIdocTabs} from 'idoc/idoc-tabs/configure-idoc-tabs';
import {IdocTabOpenedEvent} from 'idoc/idoc-tabs/idoc-tab-opened-event';
import {Toolbar} from 'components/toolbars/toolbar';
//TODO this is not used anymore and might be removed
import 'idoc/info-area/info-area';
import {TOPIC as INFO_AREA_TOPIC} from 'idoc/info-area/info-area';
import 'components/ui-preference/ui-preference';
import 'idoc/idoc-tabs/idoc-tabs';
import 'idoc/idoc-actions/idoc-actions';
import 'idoc/sidebar/sidebar';
import {TabsConfig} from 'idoc/idoc-tabs/idoc-tabs-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {Router} from 'adapters/router/router';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import uuid from 'common/uuid';
import {NOT_FOUND} from 'error/error-page';
import {ActionExecutor} from 'services/actions/action-executor';
import 'idoc/template/idoc-template-selector';
import {
  IDOC_PAGE_ACTIONS_PLACEHOLDER,
  MODE_PREVIEW,
  STATE_PARAM_TAB,
  STATE_PARAM_MODE,
  STATE_PARAM_ID,
  PERMISSIONS_TAB_ID,
  SHOW_TEMPLATE_SELECTOR
} from 'idoc/idoc-constants';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {TEMPLATE_ID} from 'idoc/template/template-constants';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {PermissionsChangedEvent} from 'idoc/system-tabs/permissions/permissions-changed-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Logger} from 'services/logging/logger';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {BeforeIdocContentModelUpdateEvent} from 'idoc/events/before-idoc-content-model-update-event';
import {IdocContentModelUpdateEvent} from 'idoc/events/idoc-content-model-update-event';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {SanitizeIdocContentCommand} from 'idoc/events/sanitize-idoc-content-command';
import {LOCK, UNLOCK} from 'idoc/actions/action-constants';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {CustomEventDispatcher} from 'services/dom/custom-event-dispatcher';
import 'components/help/contextual-help';
import {HelpService, HELP_INSTANCE_TYPE} from 'services/help/help-service';
import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import {UserService} from 'services/identity/user-service';
import _ from 'lodash';
import base64 from 'common/lib/base64';
import 'font-awesome/css/font-awesome.css!';
import template from './idoc-page.html!text';

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
  ActionExecutor, NgTimeout, IdocContextFactory, NotificationService,
  TranslateService, Logger, ActionsService, NgInterval, IdocDraftService, PromiseAdapter, Configuration, PluginsService, HelpService, DynamicElementsRegistry,
  CustomEventDispatcher, ModelingIdocContextBuilder, UserService, SessionStorageService)
export class IdocPage {

  constructor($element, $scope, eventbus, stateParamsAdapter, locationAdapter, router,
              actionExecutor, $timeout, idocContextFactory, notificationService,
              translateService, logger, actionsService, $interval, idocDraftService, promiseAdapter, configuration, pluginsService, helpService, dynamicElementsRegistry,
              customEventDispatcher, modelingIdocContextBuilder, userService, sessionStorageService) {
    this.$element = $element;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$interval = $interval;
    this.eventbus = eventbus;
    this.actionsService = actionsService;
    this.idocDraftService = idocDraftService;

    this.notificationService = notificationService;
    this.translateService = translateService;
    this.logger = logger;

    this.stateParamsAdapter = stateParamsAdapter;
    this.locationAdapter = locationAdapter;
    this.router = router;
    this.actionExecutor = actionExecutor;
    this.promiseAdapter = promiseAdapter;
    this.configuration = configuration;
    this.pluginsService = pluginsService;
    this.dynamicElementsRegistry = dynamicElementsRegistry;
    this.userService = userService;

    // Get the mode parameter trough the utils where the url is decoded first. The state params adapter gets confused
    // when it is provided with encoded url (for example when idoc is printed it is rendered trough phantomjs where the
    // page url is encoded and the angular state service brakes because of this.
    // TODO: Probably we have to change this everywhere the state service is used!
    let viewMode = UrlUtils.getParameter(locationAdapter.url(), STATE_PARAM_MODE);
    this.context = idocContextFactory.createNewContext(this.stateParamsAdapter.getStateParam(STATE_PARAM_ID), viewMode);
    this.wrappedContext = modelingIdocContextBuilder.wrapIdocContext(this.context);

    this.actionsConfig = {
      disableSaveButton: true
    };

    this.sessionStorageService = sessionStorageService;
    this.headerType = HEADER_DEFAULT;
    this.isHeaderVisible = true;

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
        if (this.tabsConfig.getActiveTab()) {
          this.tabsConfig.getActiveTab().loaded = true;
          this.disableSaveButton(false);
        }
        this.idocIsReady = true;
        window.status = 'export-ready';
        customEventDispatcher.dispatchEvent(document.body, 'export-ready');
      }),
      this.eventbus.subscribe(PermissionsChangedEvent, () => {
        this.context.reloadObjectDetails(this.currentObject.getId());
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
      if (!this.context.isPrintMode()) {
        this.events.push(this.tabsConfig.eventbus.subscribe(IdocTabOpenedEvent, (tabInfo) => {
          if (!tabInfo.loaded) {
            this.dynamicElementsRegistry.reload();
          }
        }));
      }
      this.tabsConfig.activeTabId = UrlUtils.getUrlFragment(this.locationAdapter.url());
      if (this.tabsConfig.activeTabId) {
        this.requestedTabs = [this.tabsConfig.activeTabId];
      } else {
        this.requestedTabs = UrlUtils.getParameterArray(this.locationAdapter.url(), STATE_PARAM_TAB);
        if (this.requestedTabs) {
          this.tabsConfig.activeTabId = this.requestedTabs[0];
        }
      }

      if (!this.hasEditPermission()) {
        return;
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

        this.promiseAdapter.resolve(this.appendContent(content)).then(() => {
          if (this.context.isEditMode()) {
            return this.appendDraftContent();
          }
        }).then(() => {
          this.eventbus.publish(new AfterIdocLoadedEvent(this.actionContext));
        });
      }

      this.configureContextualHelp(this.currentObject, helpService);
    });

    this.context.setShowTemplateSelector(false);

    if (this.sessionStorageService.get(SHOW_TEMPLATE_SELECTOR)) {
      this.context.setShowTemplateSelector(this.sessionStorageService.get(SHOW_TEMPLATE_SELECTOR));
      this.sessionStorageService.remove(SHOW_TEMPLATE_SELECTOR);
    }
  }

  templateSelectedCallback(event) {
    this.disableSaveButton(true);
    this.selectedTemplate = event.template.id;
  }

  disableSaveButton(state) {
    this.actionsConfig.disableSaveButton = state;
  }

  hasEditPermission() {
    if (!this.currentObject.getWriteAllowed() && this.context.isEditMode() && this.currentObject.isPersisted()) {
      this.setViewMode(MODE_PREVIEW);
      this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {
        notify: true,
        skipRouteInterrupt: true
      });
      return false;
    }
    return true;
  }

  /**
   * checks if edit mode an the document is not locked
   */
  lockIdoc() {
    if (this.context.isEditMode() && !this.currentObject.isLocked()) {
      return this.actionsService.lock(this.currentObject.getId(), {operation: LOCK});
    } else {
      return this.promiseAdapter.resolve();
    }
  }

  appendDraftContent() {
    return this.lockIdoc().then(() => {
      this.context.reloadObjectDetails(this.currentObject.getId()).catch(this.logger.error);
      return this.idocDraftService.loadDraft(this.context).then((result) => {
        if (result.loaded) {
          this.appendContent(result.content);
        }
        this.startDraftInterval();
      });
    });
  }

  initViewFromTemplate(event) {
    // Added specific check for the default(blank) template id. If it is
    // selected when the instance is created or it is changed by executing an
    // action, the logic will detect that and handle it properly, instead of
    // setting the primary template to the instance.
    if (event.template.templateInstanceId) {
      ModelUtils.updateObjectPropertyValue(this.currentObject.getModels().validationModel[TEMPLATE_ID], true, [event.template.templateInstanceId]);
    } else {
      ModelUtils.updateObjectPropertyValue(this.currentObject.getModels().validationModel[TEMPLATE_ID], true, [event.template.id]);
    }
    this.appendContent(event.content);
  }

  refresh(actionDefinition, response) {
    if (actionDefinition) {
      this.notificationService.success(this.translateService.translateInstant('action.notification.success') + actionDefinition.label);
    }

    if (response.content) {
      this.currentObject.setContent(response.content);
    }
    this.context.reloadObjectDetails(response.id).catch(this.logger.error);
  }

  loadData() {
    return this.loadCurrentObject().then((currentObject) => {
      if (currentObject.isPersisted() && !currentObject.isVersion()) {
        return this.loadSystemTabs(currentObject).then(() => {
          return currentObject;
        });
      } else {
        return currentObject;
      }
    });
  }

  /**
   * System tabs are defined as plugins and are loaded after idoc is loaded. For every tab is built its component tag
   * which later is compiled and appended to the DOM.
   * System tabs could be filtered using a <b>filter</b> function defined in the tab plugin definition. The filter
   * function accepts context object with the currentObject reference and must return a boolean value which is
   * considered as a flag if the tab should be included or not. If the filter function is missing, then the tab will be
   * included.
   * @param currentObject
   * @returns {*}
   */
  loadSystemTabs(currentObject) {
    this.systemTabs = [];
    return this.userService.getCurrentUser().then((userInfo) => {
      return this.pluginsService.loadComponentModules('idoc-system-tabs').then((modules) => {

        let filterContext = {
          currentUser: userInfo,
          currentObject: currentObject
        };
        Object.keys(modules)
          .filter((moduleId) => {
            if (_.isFunction(modules[moduleId].filter)) {
              return !!modules[moduleId].filter(filterContext);
            }
            return true;
          })
          .forEach((moduleId) => {
            let module = modules[moduleId];
            let component = module['component'];
            let tab = {
              id: module['id'],
              title: this.translateService.translateInstant(module['name']),
              system: true,
              locked: true,
              content: '<' + component + ' context="idocTabBody.context.getWrappedObject()"></' + component + '>',
              tabNotificationExtensions: module.tabNotificationExtensions
            };
            this.systemTabs.push(tab);
          });
      });
    });
  }

  loadCurrentObject() {
    return this.context.getCurrentObject().then((currentObject) => {

      if (!currentObject.getModels()) {
        if (this.context.isEditMode()) {
          this.router.navigate('error', {'key': NOT_FOUND}, {location: 'replace'});
        } else {
          // idoc is opened without instance. Happens for path #/idoc/
          this.router.navigate('userDashboard', undefined, {location: 'replace'});
          return this.promiseAdapter.reject();
        }
      }

      return currentObject;
    });
  }

  startDraftInterval() {
    this.stopDraftInterval();
    this.draftInterval = this.$interval(() => {
      this.idocDraftService.saveDraft(this.context, this.getIdocContent(false));
    }, this.configuration.get(Configuration.DRAFT_AUTOSAVE_INTERVAL) || 60000);
  }

  stopDraftInterval() {
    if (this.draftInterval) {
      this.$interval.cancel(this.draftInterval);
      this.draftInterval = undefined;
    }
  }

  /**
   * Generate tabs and append iDoc content.
   *
   * @param content either from template or from saved iDoc
   */
  appendContent(content) {
    // manage save button state only if the idoc page has been initialized.
    if (this.idocIsReady) {
      this.disableSaveButton(true);
    }
    let isPrintMode = this.context.isPrintMode();
    let templateDom = $(content);
    let activeTabExist = false;

    this.tabsConfig.tabs.splice(0, this.tabsConfig.tabs.length);
    this.tabsConfig.activeTabId = UrlUtils.getUrlFragment(this.locationAdapter.url());
    let sectionsContent = templateDom.children('section');
    this.setTabsCounter(templateDom, sectionsContent);

    sectionsContent.each((index, section) => {
      let tab = this.buildTabModel(section);
      // set the default tab as active tab
      if (!this.tabsConfig.activeTabId && tab.default) {
        this.tabsConfig.activeTabId = tab.id;
      }
      // new tabs need their data-id attribute set
      if (!section.getAttribute('data-id')) {
        section.setAttribute('data-id', tab.id);
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
    if (this.idocIsReady) {
      this.disableSaveButton(false);
    }
    this.dynamicElementsRegistry.reload();

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
      'userDefined': !!jqSection.data('user-defined'),
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

  getIdocContent(sanitizeContent) {
    this.eventbus.publish(new BeforeIdocContentModelUpdateEvent());
    this.eventbus.publish(new IdocContentModelUpdateEvent());
    this.eventbus.publish(new AfterIdocContentModelUpdateEvent());
    let content = `<div data-tabs-counter="${this.tabsConfig.tabsCounter}">`;
    this.tabsConfig.tabs.forEach(tab => {
      if (sanitizeContent) {
        this.eventbus.publish(new SanitizeIdocContentCommand({
          unsanitizedContent: tab.content,
          tabId: tab.id
        }));
      }

      if (!tab.system) {
        let escapedTitle = _.escape(tab.title);
        content += `<section data-id="${tab.id}" data-title="${escapedTitle}" data-default="${tab.default}" ` +
          `data-show-navigation="${tab.showNavigation}" data-show-comments="${tab.showComments}" data-revision="${tab.revision}" data-locked="${tab.locked}" data-user-defined="${tab.userDefined}">${tab.content}</section>`;
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

  removeTemplateSelector() {
    this.context.setShowTemplateSelector(false);
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
    this.removeTemplateSelector();
  }
}
