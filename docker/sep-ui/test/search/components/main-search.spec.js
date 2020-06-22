import {MainSearch} from 'search/components/main-search';
import {JsonUtil} from 'common/json-util';
import {Router} from 'adapters/router/router';
import {EVENT_SEARCH} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SavedSearchLoadedEvent} from 'search/components/saved/events';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';

describe('MainSearch', () => {

  it('should construct a search configuration', () => {
    let mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig).to.exist;
  });

  it('should configure the main search to render contextual help', () => {
    let mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig.renderHelp).to.be.true;
  });

  it('should construct a search mediator', () => {
    let mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig.searchMediator).to.exist;
  });

  it('should call animate and scroll to top on EVENT_SEARCH', () => {
    let animateSpy = sinon.spy($.fn, 'animate');
    let stateSpy = StateParamsAdapterMock.mockAdapter();
    let mainSearch = getMainSearchInstance(stateSpy);

    stateSpy.setStateParam('id', {});
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, {query: {}});

    expect(animateSpy.calledOnce).to.be.true;
    expect(animateSpy.getCall(0).args[0]).to.deep.equal({scrollTop: 0});
    // Unwrap animate stub
    $.fn.animate.restore();
  });

  it('should navigate after second search', () => {
    let mainSearch = getMainSearchInstance(stub(StateParamsAdapter));

    let event = {
      arguments: {},
      query: {
        tree: {id: '123', rules: []}
      }
    };
    expect(mainSearch.ignoreInitialSearch).to.be.true;
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, event);
    expect(mainSearch.router.navigate.called).to.be.false;
    expect(mainSearch.ignoreInitialSearch).to.be.false;

    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, event);
    expect(mainSearch.router.navigate.calledOnce).to.be.true;

    let parameters = mainSearch.router.navigate.getCall(0).args[1];
    let providedTree = JsonUtil.decode(parameters.tree);
    // Should be sanitized from IDs
    expect(providedTree).to.deep.equal({rules: []});
  });

  it('should sanitize search arguments before navigating', () => {
    let mainSearch = getMainSearchInstance(stub(StateParamsAdapter));
    mainSearch.ignoreInitialSearch = false;
    let event = {
      arguments: {
        pageNumber: 1,
        properties: ['emf:property']
      },
      query: {
        tree: {id: '123'}
      }
    };
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, event);
    expect(mainSearch.router.navigate.calledOnce).to.be.true;

    let parameters = mainSearch.router.navigate.getCall(0).args[1];
    let providedArguments = JsonUtil.decode(parameters.args);
    expect(providedArguments).to.deep.equal({pageNumber: 1});
  });

  it('should navigate with correct search mode when no saved search is present in search params', () => {
    let stateSpy = StateParamsAdapterMock.mockAdapter();
    let mainSearch = getMainSearchInstance(stateSpy);
    // mock and provide an explicit search mode
    stateSpy.setStateParam('mode', 'param-mode');
    mainSearch.ignoreInitialSearch = false;

    let event = {
      arguments: {
        pageNumber: 1
      },
      query: {
        tree: {id: '123'}
      }
    };
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, event);
    expect(mainSearch.router.navigate.calledOnce).to.be.true;

    let parameters = mainSearch.router.navigate.getCall(0).args[1];
    expect(parameters.mode).to.eq('param-mode');
  });

  it('should navigate with correct search mode when saved search is present in search params', () => {
    let stateSpy = StateParamsAdapterMock.mockAdapter();
    let mainSearch = getMainSearchInstance(stateSpy);
    // mock and provided saved search
    stateSpy.setStateParam('id', {});
    mainSearch.ignoreInitialSearch = false;

    let event = {
      arguments: {
        pageNumber: 1
      },
      query: {
        tree: {id: '123'}
      }
    };
    mainSearch.mediator.searchMode = 'mediator-mode';
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH, event);
    expect(mainSearch.router.navigate.calledOnce).to.be.true;

    let parameters = mainSearch.router.navigate.getCall(0).args[1];
    expect(parameters.mode).to.eq('mediator-mode');
  });

  it('should not trigger search if url parameters are not provided', () => {
    let mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.renderSearch).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.undefined;
  });

  it('should trigger search if a saved search instance id is provided', () => {
    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.renderSearch).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.true;
  });

  it('should trigger search if query parameters are provided', () => {
    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('freeText', 'foo bar');
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.renderSearch).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.true;
  });

  it('should correctly set & decode search arguments & tree', () => {
    let args = {pageSize: 25, pageNumber: 1};
    let tree = {condition: 'AND', rules: []};

    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('args', JsonUtil.encode(args));
    stateParamServiceMock.setStateParam('tree', QueryBuilder.encodeSearchTree(tree));
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.searchConfig.arguments).to.deep.equal(args);
    expect(QueryBuilder.isEqual(mainSearch.mediator.queryBuilder.tree, tree)).to.be.true;
  });

  it('should set appropriate search modes if saved search is provided', () => {
    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.searchConfig.searchMode).to.be.equals('basic');
    expect(mainSearch.mediator.searchMode).to.be.equals('basic');
  });

  it('should set the provided search mode', () => {
    let mode = 'advanced';
    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('mode', mode);
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.searchConfig.searchMode).to.be.equals(mode);
    expect(mainSearch.mediator.searchMode).to.be.equals(mode);
  });

  it('should create toolbar & savedSearch config if saved search is loaded', () => {
    let stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    let mainSearch = getMainSearchInstance(stateParamServiceMock);

    let searchArguments = {
      orderBy: 'title',
      orderDirection: 'asc'
    };

    let savedSearch = {
      searchId: 'emf:123',
      searchTitle: 'searchTitle'
    };

    expect(mainSearch.searchConfig.arguments).to.deep.equal(searchArguments);
    expect(mainSearch.searchConfig.savedSearch).to.deep.equal(savedSearch);
    expect(mainSearch.eventbus.publish.calledOnce).to.be.true;

    let event = mainSearch.eventbus.publish.getCall(0).args[0];
    expect(event instanceof SavedSearchLoadedEvent).to.be.true;
    expect(event.getData().id).to.eq('emf:123');
  });

  it('should construct correct resultsToolbarConfig', () => {
    let mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());

    expect(mainSearch.resultsToolbarConfig).to.exist;
    // Should be holding the same search mediator reference
    expect(mainSearch.resultsToolbarConfig.searchMediator).to.equal(mainSearch.mediator);
  });
});

function getMainSearchInstance(stateParamServiceMock) {
  let mainSearch = new MainSearch(stateParamServiceMock, searchServiceMock(), instanceServiceMock(), {
    publish: sinon.spy()
  }, stubRouter());
  mainSearch.ngOnInit();
  return mainSearch;
}

function stubRouter(state = '') {
  let routerStub = stub(Router);
  routerStub.getCurrentState.returns(state);
  return routerStub;
}

function searchServiceMock() {
  return {
    search: () => {
      return PromiseStub.resolve();
    }
  };
}

function instanceServiceMock() {
  return {
    load: () => {
      return PromiseStub.resolve({
        data: {
          id: 'emf:123',
          properties: {
            title: 'searchTitle',
            searchType: 'basic',
            searchCriteria: '{"criteria" : {"id":"id"},"orderBy":"title","orderDirection":"asc"}'
          }
        }
      });
    }
  };
}