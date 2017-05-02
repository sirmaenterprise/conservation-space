import {MainSearch} from 'search/components/main-search';
import {EVENT_SEARCH} from 'search/search-mediator';
import {SavedSearchLoadedEvent} from 'search/components/saved/events';
import {PromiseStub} from "test/promise-stub";
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';

describe('MainSearch', () => {

  it('should construct a search configuration', () => {
    var mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig).to.exist;
  });

  it('should configure the main search to render contextual help', () => {
    var mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig.renderHelp).to.be.true;
  });

  it('should construct a search mediator', () => {
    var mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.searchConfig.searchMediator).to.exist;
  });

  it('should scroll to the top after a search is completed', () => {
    var animateSpy = sinon.spy($.fn, "animate");
    var stateParamService = {
      getStateParams: () => {
        return {}
      }
    };
    var mainSearch = getMainSearchInstance(stateParamService);
    mainSearch.searchConfig.searchMediator.trigger(EVENT_SEARCH);
    expect(animateSpy.calledOnce).to.be.true;
    expect(animateSpy.getCall(0).args[0]).to.deep.equal({scrollTop: 0});
    // Unwrap
    $.fn.animate.restore();
  });

  it('should not trigger search if url parameters are not provided', () => {
    var mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    expect(mainSearch.render).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.undefined;
  });


  it('should trigger search if metaText is provided', () => {
    var stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('metaText', 'test');
    var mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.render).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.true;
  });

  it('should trigger search if id is provided', () => {
    var stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    var mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.render).to.be.true;
    expect(mainSearch.searchConfig.triggerSearch).to.be.true;
  });

  it('should set appropriate search modes if id is provided', () => {
    var stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    var mainSearch = getMainSearchInstance(stateParamServiceMock);

    expect(mainSearch.searchConfig.searchMode).to.be.equals('basic');
    expect(mainSearch.mediator.searchMode).to.be.equals('basic');
  });

  it('should create toolbar if saved search is loaded', () => {
    var stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('id', 'emf:123');
    var mainSearch = getMainSearchInstance(stateParamServiceMock);
    var expected = {
      orderBy: 'title',
      orderDirection: 'asc',
      searchId: 'emf:123',
      searchTitle: 'searchTitle'
    };

    expect(mainSearch.searchConfig.toolbar).to.deep.equal(expected);
    expect(mainSearch.eventbus.publish.calledOnce).to.be.true;

    var event = mainSearch.eventbus.publish.getCall(0).args[0];
    expect(event instanceof SavedSearchLoadedEvent).to.be.true;
    expect(event.getData().id).to.eq('emf:123');
  });

  it('should create empty criteria if metaText is not provided', () => {
    var mainSearch = getMainSearchInstance(StateParamsAdapterMock.mockAdapter());
    var tree = mainSearch.searchConfig.searchMediator.queryBuilder.tree;
    expect(tree).to.exist;
    expect(tree.rules).to.exist;
    expect(tree.rules.length).to.equal(0);
  });

  it('should create criteria tree if metaText is provided', () => {
    var stateParamServiceMock = StateParamsAdapterMock.mockAdapter();
    stateParamServiceMock.setStateParam('metaText', 'test');

    var mainSearch = getMainSearchInstance(stateParamServiceMock);
    var tree = mainSearch.searchConfig.searchMediator.queryBuilder.tree;

    expect(tree).to.exist;
    expect(tree.rules).to.exist;
    expect(tree.rules[0].rules).to.exist;
    expect(tree.rules[0].rules.length).to.equal(2);
    expect(tree.rules[0].rules[1].rules).to.exist;

    var ftsRule = tree.rules[0].rules[1].rules[0];
    expect(ftsRule.field).to.equal('freeText');
    expect(ftsRule.type).to.equal('fts');
    expect(ftsRule.value).to.equal('test');
  });
});

function getMainSearchInstance(stateParamServiceMock) {
  return new MainSearch(stateParamServiceMock, searchServiceMock(), instanceServiceMock(), {
    publish: sinon.spy()
  });
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