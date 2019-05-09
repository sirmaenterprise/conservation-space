import {PromiseStub} from 'test/promise-stub';
import {IdocPage} from 'idoc/idoc-page';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Router} from 'adapters/router/router';
import {Logger} from 'services/logging/logger';
import {ActionsService} from 'services/rest/actions-service';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {HelpService} from 'services/help/help-service';
import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import {CustomEventDispatcher} from 'services/dom/custom-event-dispatcher';
import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import {UserService} from 'security/user-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ActionExecutor} from 'services/actions/action-executor';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {InstanceRestService} from 'services/rest/instance-service';
import {TabsConfig} from 'idoc/idoc-tabs/idoc-tabs-config';
import {stub} from 'test/test-utils';

const NO_EDIT_ALLOWED_IDOC_ID = 'emf:654321';

export class IdocPageTestHelper {

  /**
   * Instantiates a fully stubbed IdocPage.
   *
   * @param id Instance id
   * @param mode View mode: edit|preview|print
   * @param idocContext IdocContext instance or stub which should be used.
   * @return {*|IdocPage}
   */
  static instantiateIdocPage(id, mode, idocContext) {
    let pageUrl = '/#/idoc?mode=' + mode;

    let $element = null;

    let eventbus = stub(Eventbus);

    let router = stub(Router);

    let logger = stub(Logger);

    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', JSON.stringify(IdocPageTestHelper.generateModels()));

    let notificationService = stub(NotificationService);

    let translateService = stub(TranslateService);
    translateService.translateInstant.returns('translated message');
    translateService.translateInstantWithInterpolation.returns('translated message');

    let actionsService = stub(ActionsService);
    actionsService.getActions = () => {
      return new Promise((resolve) => {
        resolve({
          data: [{
            action: 'editDetails',
            userOperation: 'editDetails',
            serverOperation: 'editDetails',
            disabled: false,
            actionPath: '/'
          }]
        });
      });
    };
    actionsService.lock.returns(() => PromiseStub.resolve());
    actionsService.unlock.returns(() => PromiseStub.resolve());
    actionsService.activateTemplate.returns(() => PromiseStub.resolve());

    let idocDraftService = stub(IdocDraftService);
    idocDraftService.saveDraft.returns(PromiseStub.resolve());
    idocDraftService.deleteDraft.returns(PromiseStub.resolve());

    let configuration = stub(Configuration);

    let pluginsService = stub(PluginsService);
    pluginsService.loadComponentModules.returns(PromiseStub.resolve({}));
    pluginsService.getDefinitions.returns([]);

    let helpService = stub(HelpService);

    let dynamicElementsRegistry = stub(DynamicElementsRegistry);

    let customEventDispatcher = stub(CustomEventDispatcher);

    let modelingIdocContextBuilder = stub(ModelingIdocContextBuilder);

    let userService = stub(UserService);
    userService.getCurrentUser.returns(PromiseStub.resolve({}));

    let actionExecutor = stub(ActionExecutor);

    let $scope = IdocPageTestHelper.mockScope();

    let stateParamsAdapter = IdocPageTestHelper.mockStateParamsAdapter(id, mode);

    let locationAdapter = IdocPageTestHelper.mockLocationAdapter(pageUrl);

    let $timeout = IdocPageTestHelper.mockTimeout();

    let idocContextFactory = IdocPageTestHelper.mockIdocContextFactory(id, sessionStorageService, idocContext);

    let $interval = IdocPageTestHelper.mockInterval();

    let idocPage = new IdocPage($element, $scope, eventbus, stateParamsAdapter, locationAdapter, router, actionExecutor,
      $timeout, idocContextFactory, notificationService, translateService, logger, actionsService, $interval,
      idocDraftService, PromiseStub, configuration, pluginsService, helpService, dynamicElementsRegistry,
      customEventDispatcher, modelingIdocContextBuilder, userService, sessionStorageService);

    idocPage.tabsConfig = IdocPageTestHelper.generateTabsConfig();

    return idocPage;
  }

  /**
   * Generates fake model of an instance object.
   */
  static generateModels() {
    const DEFINITION_ID = 'OT210027';
    return {
      definitionId: DEFINITION_ID,
      parentId: 'parentId',
      returnUrl: 'returnUrl',
      viewModel: {
        fields: []
      },
      validationModel: {
        'field1': {
          defaultValue: 'value1',
          value: 'value1'
        },
        'field2': {
          defaultValue: 'value2',
          value: 'value2'
        },
        'title': {
          defaultValue: 'title',
          value: 'Title'
        }
      }
    };
  }

  static mockIdocContextFactory(id, sessionStorageService, idocContext) {
    let eventbus = stub(Eventbus);
    let pluginsService = stub(PluginsService);
    pluginsService.loadPluginServiceModules.returns(PromiseStub.resolve([]));
    let router = stub(Router);

    let factory = new IdocContextFactory(IdocPageTestHelper.mockInstanceRestService(id), sessionStorageService, PromiseStub, eventbus, router, pluginsService);
    if (idocContext) {
      sinon.stub(factory, 'createNewContext').returns(idocContext);
    }

    return factory;
  }

  static mockInstanceRestService(id) {
    let instanceRestService = stub(InstanceRestService);
    if (id) {
      instanceRestService.load.returns(PromiseStub.resolve({data: {}}));
      instanceRestService.loadBatch.returns(PromiseStub.resolve([]));
      instanceRestService.loadContextPath.returns(PromiseStub.resolve({data: []}));
      instanceRestService.loadView.returns(PromiseStub.resolve({data: {}}));
      instanceRestService.loadModel = (id) => {
        let writeAllowed = true;
        if (NO_EDIT_ALLOWED_IDOC_ID === id) {
          writeAllowed = false;
        }

        return PromiseStub.resolve({
          writeAllowed,
          definitionId: id,
          instanceId: 'definitionId',
          data: {}
        });
      };
    }
    return instanceRestService;
  }

  static mockScope() {
    return {
      $watch: () => {
      },
      $watchCollection: () => {
      },
      $new: () => {
      },
      $digest: () => {
      },
      $apply: () => {
      },
      $destroy: () => {
      },
      $$phase: undefined
    };
  }

  static mockStateParamsAdapter(id, mode) {
    let stateParams = {
      id, mode
    };
    return new StateParamsAdapter(stateParams);
  }

  static mockLocationAdapter(url) {
    let locationAdapter = {};
    locationAdapter.hash = () => {
      return '0';
    };
    locationAdapter.url = () => {
      return url;
    };
    return locationAdapter;
  }

  static mockTimeout() {
    let timeoutMock = (func) => {
      if (func instanceof Function) {
        func();
      }
    };
    timeoutMock.cancel = () => {
    };
    return timeoutMock;
  }

  static mockInterval() {
    let intervalMock = (func) => {
      if (func) {
        func();
      }
    };
    intervalMock.cancel = () => {
    };
    return intervalMock;
  }

  static generateTabsConfig() {
    let tabsConfig = new TabsConfig(stub(Eventbus));
    let tabs = [];
    for (let i = 0; i <= 3; i++) {
      tabs.push({
        id: 'id_' + i,
        title: 'Title ' + i,
        content: 'Content ' + i
      });
    }
    tabs[0].default = true;
    tabs[0].showNavigation = true;
    tabs[0].showComments = true;
    tabs[0].revision = 'exportable';
    tabs[0].locked = false;
    tabs[0].userDefined = false;
    tabs[1].default = false;
    tabs[1].showNavigation = true;
    tabs[1].showComments = false;
    tabs[1].revision = 'exportable';
    tabs[1].locked = false;
    tabs[1].userDefined = false;
    tabs[2].default = false;
    tabs[2].showNavigation = false;
    tabs[2].showComments = false;
    tabs[2].revision = 'exportable';
    tabs[2].locked = true;
    tabs[2].userDefined = true;
    tabs[3].default = false;
    tabs[3].showNavigation = false;
    tabs[3].showComments = true;
    tabs[3].revision = 'cloneable';
    tabs[3].locked = false;
    tabs[3].userDefined = true;
    tabsConfig.tabs = tabs;
    tabsConfig.tabsCounter = 3;
    //tabsConfig.activeTabId = 'id_0';
    return tabsConfig;
  }

  static generateIntialContent() {
    return '<div data-tabs-counter="3"><section data-id="id_0" data-title="Title 0" data-default="true" data-show-navigation="true" data-show-comments="true" data-user-defined="false">Content 0</section>' +
      '<section data-id="id_1" data-title="Title 1" data-default="false" data-show-navigation="true" data-show-comments="false">Content 1</section>' +
      '<section data-id="id_2" data-title="Title 2" data-default="false" data-show-navigation="false" data-show-comments="false" data-user-defined="true">Content 2</section>' +
      '<section data-id="id_3" data-title="Title 3" data-default="false" data-show-navigation="false" data-show-comments="true" data-user-defined="true">Content 3</section></div>';
  }
}