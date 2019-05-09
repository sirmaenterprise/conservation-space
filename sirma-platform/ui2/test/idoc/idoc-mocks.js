import {InstanceRestService} from 'services/rest/instance-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {IdocPage} from 'idoc/idoc-page';
import {TabsConfig} from 'idoc/idoc-tabs/idoc-tabs-config';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from '../adapters/angular/promise-adapter-mock';
import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import {PluginsService} from 'services/plugin/plugins-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {stub} from 'test/test-utils';

export class IdocMocks {

  static generateIntialContent() {
    return '<div data-tabs-counter="3"><section data-id="id_0" data-title="Title 0" data-default="true" data-show-navigation="true" data-show-comments="true" data-user-defined="false">Content 0</section>' +
      '<section data-id="id_1" data-title="Title 1" data-default="false" data-show-navigation="true" data-show-comments="false">Content 1</section>' +
      '<section data-id="id_2" data-title="Title 2" data-default="false" data-show-navigation="false" data-show-comments="false" data-user-defined="true">Content 2</section>' +
      '<section data-id="id_3" data-title="Title 3" data-default="false" data-show-navigation="false" data-show-comments="true" data-user-defined="true">Content 3</section></div>';
  }

  static mockElement() {
    let element = {
      length: 1,
      empty: () => {
      },
      append: () => {
      },
      find: () => {
        return element;
      },
      width: () => {
      },
      closest: () => {
        return {
          attr: () => {
            return 'id';
          }
        };
      },
      addClass: () => {
      },
      removeClass: () => {
      },
      hasClass: () => {
      },
      attr: () => {
      },
      end: () => {
        return element
      },
      remove: () => {
        return element
      }, prepend: () => {
        return element;
      }
    };
    return element;
  }

  static mockInstanceRestService(id) {
    let restClient = {};
    let instanceRestService = new InstanceRestService(restClient);
    if (id) {
      instanceRestService.load = () => {
        return PromiseStub.resolve();
      };
      instanceRestService.loadBatch = () => {
        return PromiseStub.resolve([]);
      };
      instanceRestService.loadModel = (id) => {
        let writeAllowed = true;
        if (IdocMocks.NO_EDIT_ALLOWED_IDOC_ID === id) {
          writeAllowed = false;
        }
        return PromiseStub.resolve({
          definitionId: id,
          instanceId: 'definitionId',
          writeAllowed: writeAllowed
        });
      };
      instanceRestService.loadView = () => {
        return PromiseStub.resolve({
          data: {}
        });
      }
    }
    return instanceRestService;
  }

  static mockEventBus() {
    let eventBus = {};
    eventBus.publish = () => {
    };
    eventBus.subscribe = () => {
      return {
        unsubscribe: () => {
        }
      }
    };
    return eventBus;
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

  static mockWindowAdapter(href) {
    return {
      location: {
        href: href || 'initialLocation'
      }
    };
  }

  static mockTranslateService() {
    return {
      translateInstant: () => {
        return 'translated message';
      },
      translateInstantWithInterpolation: () => {
        return 'translated message';
      },
      changeLanguage: (lang) => {
      }
    }
  }

  static mockRouter() {
    let router = {};
    router.navigate = () => {
    };
    return router;
  }

  static mockLogger() {
    let logger = {};
    logger.error = () => {
    };
    return logger;
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

  static mockAuthenticationService() {
    return {
      getToken: sinon.spy()
    };
  }

  static mockActionsService() {
    return {
      getActions: () => {
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
      },
      lock: () => PromiseStub.resolve(),
      unlock: () => PromiseStub.resolve(),
      activateTemplate: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    };
  }

  static mockIdocContextFactory(id, sessionStorageService) {
    let pluginsService = stub(PluginsService);
    pluginsService.loadPluginServiceModules.returns([]);
    return new IdocContextFactory(IdocMocks.mockInstanceRestService(id), sessionStorageService,
      PromiseAdapterMock.mockAdapter(), IdocMocks.mockEventBus(), pluginsService);
  }

  static mockIdocDraftService() {
    return {
      saveDraft: () => PromiseStub.resolve(),
      loadDraft: () => PromiseStub.resolve({}),
      deleteDraft: () => PromiseStub.resolve()
    };
  }

  static mockConfiguration() {
    return {
      get: sinon.stub()
    };
  }

  static mockPluginsService() {
    return {
      loadComponentModules() {
        return PromiseStub.resolve({});
      },
      getDefinitions() {
        return [];
      }
    }
  }

  static mockHelpService() {
    return {
      getHelpInstanceId: () => {
      }
    }
  }

  static mockUserService() {
    return {
      getCurrentUser: () => PromiseStub.resolve({})
    }
  }

  static instantiateIdocPage(id, mode) {
    let pageUrl = '/#/idoc?mode=' + mode;
    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', JSON.stringify(IdocMocks.generateModels()));
    let idocPage = new IdocPage(undefined, IdocMocks.mockScope(), IdocMocks.mockEventBus(), IdocMocks.mockStateParamsAdapter(id, mode),
      IdocMocks.mockLocationAdapter(pageUrl), IdocMocks.mockRouter(), {}, IdocMocks.mockTimeout(), IdocMocks.mockIdocContextFactory(id, sessionStorageService),
      {}, IdocMocks.mockTranslateService(), {}, IdocMocks.mockActionsService(), IdocMocks.mockInterval(), IdocMocks.mockIdocDraftService(), PromiseAdapterMock.mockAdapter(),
      IdocMocks.mockConfiguration(), IdocMocks.mockPluginsService(), IdocMocks.mockHelpService(), {reload: sinon.spy()}, {}, stub(ModelingIdocContextBuilder), IdocMocks.mockUserService(), stub(SessionStorageService));
    idocPage.tabsConfig = IdocMocks.generateTabsConfig();
    return idocPage;
  }

  /**
   * Instantiate iDoc with wrong params (needed for test case)
   * @param id - idoc ID
   * @returns IdocPage
   */
  static instantiateWrongIdocPage(id) {
    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', null);
    let router = IdocMocks.mockRouter();
    router.navigate = sinon.spy();
    return new IdocPage(undefined, IdocMocks.mockScope(), IdocMocks.mockEventBus(), IdocMocks.mockStateParamsAdapter(id, 'edit'),
      IdocMocks.mockLocationAdapter(''), router, {}, IdocMocks.mockTimeout(), IdocMocks.mockIdocContextFactory(id, sessionStorageService),
      {}, IdocMocks.mockTranslateService(), {}, IdocMocks.mockActionsService(), IdocMocks.mockInterval(), IdocMocks.mockIdocDraftService(), PromiseAdapterMock.mockAdapter(),
      IdocMocks.mockConfiguration(), IdocMocks.mockPluginsService(), IdocMocks.mockHelpService(), {}, {}, stub(ModelingIdocContextBuilder), {}, stub(SessionStorageService));
  }

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

  static generateTabsConfig() {
    let tabsConfig = new TabsConfig(IdocMocks.mockEventBus());
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
}

IdocMocks.NO_EDIT_ALLOWED_IDOC_ID = 'emf:654321';