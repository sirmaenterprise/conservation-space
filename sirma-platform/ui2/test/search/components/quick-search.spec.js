import {QuickSearch, SEARCH_STATE} from 'search/components/quick-search';
import {Configuration} from 'common/application-config';
import {Router} from 'adapters/router/router';
import {Eventbus} from 'services/eventbus/eventbus';
import {QueryBuilder} from 'search/utils/query-builder';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {IDOC_STATE} from 'idoc/idoc-constants';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('QuickSearch', () => {

  let quickSearch;
  beforeEach(() => {
    quickSearch = new QuickSearch(stubConfiguration(), stubRouter(), stubEventBus(), stubIdocContextFactory(), stubInstanceRestService());
    quickSearch.ngOnInit();
  });

  describe('ngOnInit()', () => {
    it('should create a model for the search bar', () => {
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
    });

    it('should configure the search bar', () => {
      expect(quickSearch.searchBarConfig.enableCurrentObject).to.be.false;
    });

    it('should enable the current object if the state is the idoc one', () => {
      quickSearch = new QuickSearch(stubConfiguration(), stubRouter(IDOC_STATE), stubEventBus(), stubIdocContextFactory(), stubInstanceRestService());
      quickSearch.ngOnInit();
      expect(quickSearch.searchBarConfig.enableCurrentObject).to.be.true;
    });

    it('should subscribe for iDoc load & refresh events to retrieve the context', () => {
      expect(quickSearch.eventbus.subscribe.calledWith(AfterIdocLoadedEvent)).to.be.true;
      expect(quickSearch.eventbus.subscribe.calledWith(InstanceRefreshEvent)).to.be.true;
      expect(quickSearch.eventbus.subscribe.calledWith(RouterStateChangeSuccessEvent)).to.be.true;
      expect(quickSearch.searchBarConfig).to.exist;
      expect(quickSearch.events).to.exist;
    });

    it('should subscribe for successful state change to update search bar model', () => {
      expect(quickSearch.eventbus.subscribe.calledWith(RouterStateChangeSuccessEvent)).to.be.true;
    });
  });

  describe('readSearchStateParams()', () => {
    it('should not read params if the current state is not the search one', () => {
      quickSearch.router = stubRouter('dashboard');
      let event = stubRouterStateChangeEvent({mode: 'not-advanced'});
      quickSearch.onStateChanged(event);

      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
    });

    it('should read parameters into the search bar model', () => {
      quickSearch.router = stubRouter(SEARCH_STATE);
      let event = stubRouterStateChangeEvent({
        tree: constructEncodedSearchTree({
          objectType: 'emf:Document',
          freeText: 'abc',
          context: 'emf:123'
        })
      });
      let contextInstance = {id: 'emf:123'};
      quickSearch.instanceRestService = stubInstanceRestService(contextInstance);

      quickSearch.onStateChanged(event);
      expect(quickSearch.instanceRestService.load.calledWith('emf:123')).to.be.true;
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: 'emf:Document',
        freeText: 'abc',
        context: contextInstance
      });
    });

    it('should request to load only the minimum for the context instance', () => {
      quickSearch.router = stubRouter(SEARCH_STATE);
      let event = stubRouterStateChangeEvent({
        tree: constructEncodedSearchTree({
          context: 'emf:123'
        })
      });
      quickSearch.instanceRestService = stubInstanceRestService({});

      quickSearch.onStateChanged(event);
      let requestArguments = quickSearch.instanceRestService.load.getCall(0).args[1];
      expect(requestArguments.params.properties).to.deep.equal([HEADER_BREADCRUMB]);
    });

    it('should not resolve context instance if no context is provided', () => {
      quickSearch.router = stubRouter(SEARCH_STATE);
      let event = stubRouterStateChangeEvent({
        tree: constructEncodedSearchTree({
          objectType: 'emf:Document',
          freeText: 'abc'
        })
      });

      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: 'emf:Document',
        freeText: 'abc'
      });
    });

    it('should not reload the context instance if it is the same as before', () => {
      quickSearch.router = stubRouter(SEARCH_STATE);
      let event = stubRouterStateChangeEvent({
        tree: constructEncodedSearchTree({
          context: 'emf:123'
        })
      });
      quickSearch.instanceRestService = stubInstanceRestService({});
      quickSearch.searchBarModel.context = {id: 'emf:123'};

      quickSearch.onStateChanged(event);
      expect(quickSearch.instanceRestService.load.called).to.be.false;
    });

    it('should clear any previously selected context if the query params does not carry one', () => {
      quickSearch.router = stubRouter(SEARCH_STATE);
      let event = stubRouterStateChangeEvent({
        tree: constructEncodedSearchTree({
          objectType: 'emf:Document',
          freeText: 'abc'
        })
      });
      quickSearch.searchBarModel.context = {id: 'emf:123'};
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel.context).to.not.exist;
    });
  });

  describe('onSearch()', () => {
    it('should call the router with the correct arguments for searching', () => {
      let params = {
        context: 'test',
        freeText: 'foo bar'
      };
      quickSearch.onSearch(params);
      let tree = constructEncodedSearchTree(params);

      expect(quickSearch.router.navigate.calledOnce).to.be.true;
      expect(quickSearch.router.navigate.calledWith('search', {tree: tree}, {reload: true, inherit: false})).to.be.true;
    });

    it('should properly resolve if context is current object', () => {
      let params = {
        context: CURRENT_OBJECT
      };

      let tree = constructEncodedSearchTree({
        context: 'emf:current'
      });

      let currentObject = new InstanceObject('emf:current');
      let context = stubIdocContext(currentObject);
      quickSearch.idocContextFactory = stubIdocContextFactory(context);
      quickSearch.onSearch(params);

      expect(quickSearch.rootContext).to.not.exist;
      expect(quickSearch.router.navigate.calledOnce).to.be.true;
      expect(quickSearch.router.navigate.calledWith('search', {tree: tree}, {
        reload: true,
        inherit: false
      })).to.be.true;
    });

    it('should properly resolve if context is not current object', () => {
      let params = {
        context: 'emf:12345'
      };
      quickSearch.onSearch(params);
      let tree = constructEncodedSearchTree(params);

      expect(quickSearch.router.navigate.calledOnce).to.be.true;
      expect(quickSearch.router.navigate.calledWith('search', {tree: tree}, {reload: true, inherit: false})).to.be.true;
    });
  });

  describe('loadSavedSearch()', () => {
    it('should call the router with the correct arguments for loading the search', () => {
      let savedSearchId = 'emf:123';
      let savedSearch = {id: savedSearchId, headers: {}};
      quickSearch.loadSavedSearch(savedSearch);

      let expectedParams = {
        id: savedSearchId
      };
      expect(quickSearch.router.navigate.calledOnce).to.be.true;
      expect(quickSearch.router.navigate.calledWith('open-search', expectedParams, {reload: true})).to.be.true;
    });
  });

  describe('changeMode()', () => {
    it('should call the router with the correct arguments for changing the mode', () => {
      let mode = 'advanced';
      quickSearch.changeMode(mode);

      let expectedParams = {mode}
      expect(quickSearch.router.navigate.calledOnce).to.be.true;
      expect(quickSearch.router.navigate.calledWith('search', expectedParams, {
        reload: true
      })).to.be.true;
    });
  });

  describe('onInstanceLoad()', () => {
    beforeEach(() => {
      let rootContext = new InstanceObject('emf:root');
      let currentObject = new InstanceObject('emf:child');
      currentObject.setContextPath([
        {id: 'emf:root', readAllowed: true}
      ]);

      let idocContext = stubIdocContext(currentObject, rootContext);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);
    });

    it('should update the context if the configuration is enabled', () => {
      quickSearch.configuration = stubConfiguration(true);
      quickSearch.onInstanceLoad();
      expect(quickSearch.searchBarModel.context.id).to.equal('emf:root');
    });

    it('should not update the context if the configuration is disabled', () => {
      quickSearch.configuration = stubConfiguration(false);
      quickSearch.onInstanceLoad();
      expect(quickSearch.searchBarModel.context).to.not.exist;
    });
  });

  describe('fetchContext()', () => {
    it('should not fetch the root context if there is no context', () => {
      quickSearch.idocContextFactory = stubIdocContextFactory();
      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
    });

    it('should fetch the root context if there is a context', () => {
      let rootContext = new InstanceObject('emf:root');
      let currentObject = new InstanceObject('emf:child');
      currentObject.setContextPath([
        {id: 'emf:root', readAllowed: true}
      ]);

      let idocContext = stubIdocContext(currentObject, rootContext);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);

      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel).to.deep.equal({
        context: rootContext,
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
      expect(idocContext.loadObject.calledWith('emf:root')).to.be.true;
    });

    it('should not fetch the root context if there is no context path', () => {
      let currentObject = new InstanceObject('emf:child');
      currentObject.setContextPath([]);

      let idocContext = stubIdocContext(currentObject);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);

      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
      expect(idocContext.loadObject.calledWith('emf:root')).to.be.false;
    });

    it('should not fetch the root context if there is no context with readAccess', () => {
      let rootContext = new InstanceObject('emf:root');
      let currentObject = new InstanceObject('emf:child');
      currentObject.setContextPath([
        {id: 'emf:root', readAllowed: false}
      ]);

      let idocContext = stubIdocContext(currentObject, rootContext);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);

      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel).to.deep.equal({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
      expect(idocContext.loadObject.calledWith('emf:root')).to.be.false;
    });

    it('should not load object if the root context matches current object', () => {
      var rootContext = new InstanceObject('emf:root');
      var currentObject = new InstanceObject('emf:root');
      currentObject.setContextPath([
        {id: 'emf:root', readAllowed: true}
      ]);

      var idocContext = stubIdocContext(currentObject, rootContext);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);

      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel.context).to.exist;
      expect(quickSearch.searchBarModel.context).to.deep.equal(currentObject);
      expect(idocContext.loadObject.calledWith('emf:root')).to.be.false;
    });

    it('should not load object if the root context is still the same', () => {
      var rootContext = new InstanceObject('emf:root');
      var currentObject = new InstanceObject('emf:child');
      currentObject.setContextPath([
        {id: 'emf:root', readAllowed: true}
      ]);
      quickSearch.searchBarModel.context = new InstanceObject('emf:root');

      var idocContext = stubIdocContext(currentObject, rootContext);
      quickSearch.idocContextFactory = stubIdocContextFactory(idocContext);

      quickSearch.fetchContext();
      expect(quickSearch.searchBarModel.context).to.exist;
      expect(quickSearch.searchBarModel.context).to.deep.equal(rootContext);
      expect(idocContext.loadObject.calledWith('emf:root')).to.be.false;
    });
  });

  describe('Acquire specified criteria from tree', () => {

    it('should properly extract fts criteria from search tree when fts is present', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        freeText: 'some-text'
      });
      expect(quickSearch.getFreeText(tree)).to.eq('some-text');
    });

    it('should properly extract context criteria from search tree when context is present', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        context: 'emf:123'
      });
      expect(quickSearch.getContext(tree)).to.eq('emf:123');
    });

    it('should properly extract type criteria from search tree when type is present', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        objectType: 'emf:Case'
      });
      expect(quickSearch.getObjectType(tree)).to.eq('emf:Case');
    });

    it('should properly extract type criteria from search tree when type is not present', () => {
      let tree = SearchCriteriaUtils.getSearchTree({});
      expect(quickSearch.getObjectType(tree)).to.eq(SearchCriteriaUtils.ANY_OBJECT);
    });
  });

  describe('ngOnDestroy()', () => {
    it('should de-register subscriptions in the eventbus', () => {
      quickSearch.ngOnDestroy();
      for (let event of quickSearch.events) {
        expect(event.unsubscribe.calledOnce).to.be.true;
      }
    });
  });

  describe('onStateChange()', () => {
    it('should not reset the search bar model if search mode is not advanced', () => {
      quickSearch.searchBarModel = {
        context: 'emf:12345',
        objectType: 'emf:Case'
      };

      let event = stubRouterStateChangeEvent({mode: 'not-advanced'});
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel).to.deep.eq({
        context: 'emf:12345',
        objectType: 'emf:Case'
      });
    });

    it('should reset the search bar model if search mode is advanced', () => {
      quickSearch.searchBarModel = {
        context: 'emf:12345',
        objectType: 'emf:Case'
      };

      let event = stubRouterStateChangeEvent({mode: SearchCriteriaUtils.ADVANCED_MODE});
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel).to.deep.eq({
        objectType: SearchCriteriaUtils.ANY_OBJECT
      });
    });
  });

  describe('updateSearchBarConfig()', () => {
    it('should enable current object in the search bar if the current state is the idoc', () => {
      quickSearch.router = stubRouter(IDOC_STATE);
      quickSearch.searchBarModel.context = {
        id: 'emf:12345'
      };

      let event = stubRouterStateChangeEvent({mode: 'not-advanced'});
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel.context).to.exist;
      expect(quickSearch.searchBarConfig.enableCurrentObject).to.be.true;
    });

    it('should disable current object in the search bar if the current state is not the idoc', () => {
      quickSearch.router = stubRouter('other');
      quickSearch.searchBarModel.context = {
        id: 'emf:12345'
      };

      let event = stubRouterStateChangeEvent({mode: 'not-advanced'});
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel.context).to.exist;
      expect(quickSearch.searchBarConfig.enableCurrentObject).to.be.false;
    });

    it('should disable current object in the search bar if the current state is not the idoc & remove current object as a context', () => {
      quickSearch.router = stubRouter('other');
      quickSearch.searchBarModel.context = {
        id: CURRENT_OBJECT
      };

      let event = stubRouterStateChangeEvent({mode: 'not-advanced'});
      quickSearch.onStateChanged(event);
      expect(quickSearch.searchBarModel.context).to.not.exist;
      expect(quickSearch.searchBarConfig.enableCurrentObject).to.be.false;
    });
  });

  function stubRouterStateChangeEvent(toParams, fromParams) {
    return [
      ['event-stub', 'new-state', toParams, 'old-state', fromParams]
    ];
  }

  function stubConfiguration(shouldUpdateContext = true) {
    let config = stub(Configuration);
    config.get.withArgs(Configuration.SEARCH_CONTEXT_UPDATE).returns(shouldUpdateContext);
    return config;
  }

  function stubStateParamsAdapter(params = {}) {
    let stateParamsStub = stub(StateParamsAdapter);
    stateParamsStub.getStateParams.returns(params);
    return stateParamsStub;
  }

  function stubRouter(state = '') {
    let routerStub = stub(Router);
    routerStub.getCurrentState.returns(state);
    return routerStub;
  }

  function stubIdocContextFactory(idocContext) {
    let service = stub(IdocContextFactory);
    service.getCurrentContext.returns(idocContext);
    return service;
  }

  function stubIdocContext(currentObject, rootContext) {
    let context = stub(IdocContext);
    context.getCurrentObject = sinon.spy(() => {
      return PromiseStub.resolve(currentObject);
    });
    context.loadObject = sinon.spy(() => {
      return PromiseStub.resolve(rootContext);
    });
    context.getCurrentObjectId = sinon.spy(() => {
      return currentObject.getId();
    });
    return context;
  }

  function stubEventBus() {
    let eventBusStub = stub(Eventbus);
    eventBusStub.subscribe = sinon.spy(() => {
      return {
        unsubscribe: sinon.spy()
      };
    });
    return eventBusStub;
  }

  function stubInstanceRestService(instance = {}) {
    let instanceServiceStub = stub(InstanceRestService);
    instanceServiceStub.load.returns(PromiseStub.resolve({
      data: instance
    }));
    return instanceServiceStub;
  }

  function constructEncodedSearchTree(params) {
    return QueryBuilder.encodeSearchTree(SearchCriteriaUtils.getSearchTree(params));
  }
});