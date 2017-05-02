import {Search, SEARCH_PROPERTIES} from 'search/components/search';
import {SearchMediator, EVENT_BEFORE_SEARCH, EVENT_SEARCH} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {BASIC_MODE, ADVANCED_MODE, EXTERNAL_MODE} from 'search/utils/search-criteria-utils';
import {CURRENT_OBJECT} from 'search/resolvers/contextual-rules-resolver';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import _ from 'lodash';

import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

function mockSearch(config, keepListeners) {
  Search.prototype.compileCriteria = _.noop;
  Search.prototype.config = config;
  let search = getSearchInstance();
  search.searchMediator.listeners = keepListeners ? search.searchMediator.listeners : {};
  return search;
}

function getSearchInstance() {
  var configuration = mockConfigurationService();
  var searchService = {};
  var translateService = mockTranslateService();
  var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
  var pluginsService = {};
  var namespaceService = mockNamespaceService();
  return new Search(undefined, undefined, undefined, configuration, searchService, translateService, promiseAdapter, pluginsService, namespaceService);
}

function mockConfigurationService() {
  return {
    get: () => {
      return 5;
    }
  };
}

function mockTranslateService() {
  return {
    translate: () => Promise.resolve(),
    translateInstant: () => Promise.resolve()
  };
}

function mockNamespaceService() {
  return {
    toFullURI: sinon.spy((uris) => {
      // Resolve immediately
      var map = {};
      uris.forEach((uri) => {
        map[uri] = uri;
      });
      return PromiseStub.resolve({
        data: map
      })
    })
  };
}

describe('Search', () => {
  beforeEach(() => {
    Search.prototype.config = undefined;
  });

  afterEach(() => {
    Search.prototype.config = undefined;
  });

  it('should create a search mediator if not provided', () => {
    Search.prototype.compileCriteria = _.noop;
    var search = getSearchInstance();
    expect(search.config.searchMediator).to.exist;
    expect(search.searchMediator).to.exist;
    // Should be the same reference
    expect(search.searchMediator).to.equal(search.config.searchMediator);
  });

  it('should use the provided search mediator', () => {
    var mediator = new SearchMediator();
    Search.prototype.compileCriteria = _.noop;
    Search.prototype.config = {
      searchMediator: mediator
    };
    var search = getSearchInstance();
    expect(search.config.searchMediator).to.exist;
    expect(search.searchMediator).to.exist;
    // Should be the same reference
    expect(search.searchMediator).to.equal(search.config.searchMediator);
    expect(search.searchMediator).to.equal(mediator);
    Search.prototype.config = undefined;
  });

  it('should assign default properties set if missing', () => {
    Search.prototype.compileCriteria = _.noop;
    var search = getSearchInstance();
    expect(search.searchMediator.arguments).to.exist;
    expect(search.searchMediator.arguments.properties).to.deep.equal(SEARCH_PROPERTIES);
  });

  it('should trigger a search if configured to not render any criteria component', () => {
    var searchMediator = new SearchMediator({});
    searchMediator.search = sinon.spy();

    var search = mockSearch({searchMediator, renderCriteria: false, triggerSearch: true}, true);
    expect(searchMediator.search.called).to.be.true;
  });

  describe('registerCallbacks()', () => {

    it('should not call mediator if config.callbacks is falsy or empty', () => {
      let config = {};
      let search = mockSearch(config);

      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      config.callbacks = null;
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      config.callbacks = {};
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;
    });

    it('should not call mediator if config.callbacks[event] is falsy or empty', () => {
      let config = {callbacks: {EVENT_SEARCH: undefined}};
      let search = mockSearch(config);

      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      config.callbacks[EVENT_SEARCH] = null;
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      config.callbacks[EVENT_SEARCH] = [];
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;
    });

    it('should handle single function mapping', () => {
      let config = {callbacks: {}};
      config.callbacks[EVENT_SEARCH] = _.noop;
      let search = mockSearch(config);

      search.registerCallbacks();
      expect(search.searchMediator.listeners[EVENT_SEARCH]).to.deep.eq([_.noop]);
    });

    it('should handle array of callbacks', () => {
      let config = {callbacks: {}};
      config.callbacks[EVENT_SEARCH] = [_.noop, _.noop];
      let search = mockSearch(config);

      search.registerCallbacks();
      expect(search.searchMediator.listeners[EVENT_SEARCH]).to.deep.eq([_.noop, _.noop]);
    });
  });

  describe('compileCriteria()', () => {
    it('should not be called if renderCriteria is configured to false', ()=> {
      Search.prototype.config = {renderCriteria: false};
      Search.prototype.compileCriteria = sinon.spy();
      let search = getSearchInstance();
      expect(search.compileCriteria).to.not.be.called;
    });
  });

  describe('configureCriteria()', () => {
    it('should construct configuration object for the rendered criteria component', () => {
      var search = mockSearch({
        level: 2,
        contextualItems: ['emf:123'],
        searchMode: 'very-advanced',
        predefinedTypes: ['emf:Meme'],
        renderHelp: true
      });

      expect(search.criteriaConfig).to.exist;
      expect(search.criteriaConfig.level).to.equal(2);
      expect(search.criteriaConfig.contextualItems).to.deep.equal(['emf:123']);
      expect(search.criteriaConfig.searchMode).to.equal('very-advanced');
      expect(search.criteriaConfig.predefinedTypes).to.deep.equal(['emf:Meme']);
      expect(search.criteriaConfig.renderHelp).to.be.true;
      expect(search.criteriaConfig.searchMediator).to.exist;
      // Should be the same reference
      expect(search.criteriaConfig.searchMediator).to.equal(search.searchMediator);
    });
  });

  describe('configureToolbar()', () => {
    it('should support a config factory', () => {
      var search = mockSearch({
        toolbar: {
          configFactory: () => {
            return {then: (cb) => cb({text: 'this is the config'})}
          }
        }
      });

      expect(search.toolbarConfig.text).to.equal('this is the config');
    });

    it('should create default config if no factory is provided', () => {
      var spy = sinon.spy();
      var search = mockSearch({});
      search.createDefaultOrderByConfig = () => {
        spy();
        return {then: (cb) => cb({text: 'unimportant'})}
      };

      search.configureToolbar();
      expect(spy.calledOnce).to.be.true;
    });

    it('should configure a callback function', () => {
      var search = mockSearch({});
      search.searchMediator.search = sinon.spy();
      search.createDefaultOrderByConfig = () => {
        // Resolve immediately
        return PromiseStub.resolve();
      };
      search.toolbarCallback = undefined;
      search.configureToolbar();
      expect(search.toolbarCallback).to.exist;

      var toolbarParams = {
        orderBy: 'description',
        orderDirection: 'ascending'
      };
      search.toolbarCallback(toolbarParams);
      expect(search.searchMediator.search.called).to.be.true;
      expect(search.searchMediator.arguments).to.contains(toolbarParams);
    });

    it('should holds saved search config', () => {
      var search = mockSearch({});
      expect(search.toolbarConfig.saveSearch).to.exist;
    });

    it('should holds saved search config with render and searchMediator attribute', () => {
      var search = mockSearch({});
      expect(search.toolbarConfig.saveSearch.render).to.exist;
      expect(search.toolbarConfig.saveSearch.searchMediator).to.exist;
      // Should be the same mediator!
      expect(search.toolbarConfig.saveSearch.searchMediator).to.equal(search.searchMediator);
    });

    it('should render the save search component if the search is not external and should render the criteria', () => {
      var search = mockSearch({
        renderCriteria: true
      });
      expect(search.toolbarConfig.saveSearch.render).to.be.true;
    });

    it('should not render the save search component if the search is external', () => {
      var search = mockSearch({});
      search.searchMediator.searchMode = EXTERNAL_MODE;
      search.configureToolbar();
      expect(search.toolbarConfig.saveSearch.render).to.be.false;
    });

    it('should not render the save search component if the search criteria should not be rendered', () => {
      var search = mockSearch({
        renderCriteria: false
      });
      expect(search.toolbarConfig.saveSearch.render).to.be.false;
    });
  });

  describe('configurePagination()', ()=> {
    it('should configure pagination configuration as expected', ()=> {
      var search = mockSearch({});
      expect(search.config.paginationConfig).to.deep.equal({
        showFirstLastButtons: true,
        page: 1,
        pageSize: 5,
        pageRotationStep: 2
      });
    });

    it('should configure pagination component to be at first page by default', ()=> {
      var search = mockSearch({});
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should assign page number as an argument in the mediator', ()=> {
      var search = mockSearch({});
      search.searchMediator.search = sinon.spy();
      search.searchMediator.arguments.pageNumber = 1;
      search.paginationCallback({pageNumber: 2});
      expect(search.searchMediator.arguments.pageNumber).to.equal(2);
    });

    it('should perform search after page change', ()=> {
      var search = mockSearch({});
      search.searchMediator.search = sinon.spy();
      search.paginationCallback({pageNumber: 2});
      expect(search.searchMediator.search.calledOnce).to.be.true;
    });

    it('should assign the page size from the configurations', () => {
      var search = mockSearch({});
      search.configuration.get = () => {
        return 123;
      };
      search.config.paginationConfig = undefined;
      search.configurePagination();
      expect(search.config.paginationConfig.pageSize).to.equal(123);
    });

    it('should assign the page rotation step from the configurations', () => {
      var search = mockSearch({});
      search.configuration.get = () => {
        return 5;
      };
      search.config.paginationConfig = undefined;
      search.configurePagination();
      expect(search.config.paginationConfig.pageRotationStep).to.equal(2);
    });

    it('should not assign the page rotation step if there is no configuration', () => {
      var search = mockSearch({});
      search.configuration.get = () => {
        return undefined;
      };
      search.config.paginationConfig = undefined;
      search.configurePagination();
      expect(search.config.paginationConfig.pageRotationStep).to.equal(undefined);
    });
  });

  describe('configureResults()', ()=> {
    it('should restore page number after search if present as an argument in the mediator', ()=> {
      var resultsConfig = {total: 0, data: []};
      var search = mockSearch({results: resultsConfig, paginationConfig: {showFirstLastButtons: true, page: 1}}, true);
      search.config.paginationConfig.page = 2;
      search.searchMediator.arguments.pageNumber = 1;
      var eventData = getEventData();
      // Reassigning the listener
      search.searchMediator.trigger(EVENT_SEARCH, eventData);
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should assign first page after search if not present as an argument in the mediator', ()=> {
      var resultsConfig = {total: 0, data: []};
      var search = mockSearch({results: resultsConfig, paginationConfig: {showFirstLastButtons: true, page: 1}}, true);
      search.config.paginationConfig.page = 2;
      delete search.searchMediator.arguments.pageNumber;
      var eventData = getEventData();
      // Reassigning the listener
      search.searchMediator.trigger(EVENT_SEARCH, eventData);
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should use the whole instance object as data', () => {
      var resultsConfig = {total: 0, data: []};
      var search = mockSearch({results: resultsConfig, paginationConfig: {showFirstLastButtons: true, page: 1}}, true);
      // Reassigning the listener
      var eventData = getEventData();
      search.searchMediator.trigger(EVENT_SEARCH, eventData);
      expect(resultsConfig.total).to.equal(1);
      expect(resultsConfig.data.length).to.equal(1);
      expect(resultsConfig.data[0]).to.deep.equal(eventData.response.data.values[0]);
    });

    function getEventData() {
      return {
        response: {
          data: {
            resultSize: 1,
            values: [{
              id: '1', instanceType: 'type', headers: {default_header: 'header'}, thumbnailImage: 'thumbnail'
            }]
          }
        }
      };
    }
  });

  describe('configureLoadingHandlers()', ()=> {
    it('should disable the components before search', ()=> {
      var search = mockSearch({}, false);
      assignConfigurations(search, false);
      search.searchMediator.search = sinon.spy();
      search.configureLoadingHandlers();
      search.searchMediator.trigger(EVENT_BEFORE_SEARCH);

      expect(search.criteriaConfig.disabled).to.be.true;
      expect(search.toolbarConfig.disabled).to.be.true;
      expect(search.config.paginationConfig.disabled).to.be.true;
    });

    it('should enable the components after search', ()=> {
      var search = mockSearch({}, false);
      assignConfigurations(search, true);
      search.searchMediator.search = sinon.spy();
      search.configureLoadingHandlers();
      search.searchMediator.trigger(EVENT_SEARCH);

      expect(search.criteriaConfig.disabled).to.be.false;
      expect(search.toolbarConfig.disabled).to.be.false;
      expect(search.config.paginationConfig.disabled).to.be.false;
    });

    function assignConfigurations(search, disabledState) {
      search.criteriaConfig = {
        disabled: disabledState
      };
      search.toolbarConfig = {
        disabled: disabledState
      };
      search.paginationConfig = {
        disabled: disabledState
      };
    }
  });

  describe('registerCriteriaReadyListener', () => {
    it('should register an event listener for when a criteria component is ready if configured to trigger a search & render criteria', () => {
      var searchMediator = new SearchMediator({});
      var search = mockSearch({searchMediator, triggerSearch: true}, true);
      expect(searchMediator.listeners[CRITERIA_READY_EVENT]).to.exist;
    });

    it('should not register an event listener for when a criteria component is ready if not configured to trigger a search', () => {
      var searchMediator = new SearchMediator({});
      var search = mockSearch({searchMediator, triggerSearch: false}, true);
      expect(searchMediator.listeners[CRITERIA_READY_EVENT]).to.not.exist;
    });

    it('should not register an event listener for when a criteria component is ready if not configured to render the criteria', () => {
      var searchMediator = new SearchMediator({});
      var search = mockSearch({searchMediator, triggerSearch: false, renderCriteria: false}, true);
      search.config.triggerSearch = true;
      search.registerCriteriaReadyListener();
      expect(searchMediator.listeners[CRITERIA_READY_EVENT]).to.not.exist;
    });

    it('should trigger a search when a criteria component is ready', () => {
      var searchMediator = new SearchMediator({});
      searchMediator.search = sinon.spy();

      var search = mockSearch({searchMediator, triggerSearch: true}, true);
      expect(searchMediator.search.called).to.be.false;

      searchMediator.trigger(CRITERIA_READY_EVENT);
      expect(searchMediator.search.calledOnce).to.be.true;
    });
  });

  describe('addContextualItems()', () => {
    it('should populate contextual items if context is provided', () => {
      var config = {
        contextualItems: [],
        criteria: {}
      };
      var search = mockSearch(config);
      search.context = {};
      search.addContextualItems();
      expect(search.config.contextualItems).to.exist;
      expect(search.config.contextualItems[0]).to.exist;
    });

    it('should not populate contextual items if context is not provided', () => {
      var config = {
        contextualItems: []
      };
      var search = mockSearch(config);
      search.context = undefined;
      search.addContextualItems();
      expect(search.config.contextualItems).to.exist;
      expect(search.config.contextualItems.length).to.equal(0);
    });
  });

  describe('registerOpenSavedSearchListener()', () => {
    it('should register a mediator listener for opening a search', () => {
      var search = mockSearch({}, true);
      var openSearchListeners = search.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      expect(openSearchListeners).to.exist;
      expect(openSearchListeners.length).to.equal(1);
    });

    it('should configure order by and order direction arguments when a saved search is opened', () => {
      var search = mockSearch({}, true);
      var openSearchListener = search.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];

      search.searchMediator.search = sinon.spy();

      expect(openSearchListener).to.exist;
      openSearchListener({
        orderBy: 'emf:createdOn',
        orderDirection: 'asc'
      });

      expect(search.searchMediator.arguments.orderBy).to.equal('emf:createdOn');
      expect(search.searchMediator.arguments.orderDirection).to.equal('asc');
    });

    it('should trigger a search when a saved search is opened', () => {
      var search = mockSearch({}, true);
      var openSearchListener = search.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];

      // Easier for testing instead of mocking the search service...
      search.searchMediator.search = sinon.spy();

      openSearchListener({});

      expect(search.searchMediator.search.calledOnce).to.be.true;
    });

    it('should reinitialize the query builder with the saved search criteria', () => {
      var search = mockSearch({}, true);
      var openSearchListener = search.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];

      // Easier for testing instead of mocking the search service...
      search.searchMediator.search = sinon.spy();

      var criteriaTree = {
        condition: 'OR',
        rules: []
      };
      openSearchListener({
        searchMode: 'new-search-mode',
        criteria: criteriaTree
      });

      expect(search.searchMediator.searchMode).to.equal('new-search-mode');
      expect(search.searchMediator.queryBuilder.tree).to.deep.equal(criteriaTree);
    });
  });

  describe('transformPredefinedTypes', () => {
    it('should convert short URIs to full URIs', () => {
      let search = mockSearch({});
      search.config.predefinedTypes = ['short:uri'];
      search.namespaceService = {
        toFullURI: sinon.spy(() => {
          return PromiseStub.resolve({
            data: {
              'short:uri': 'http://full#uri'
            }
          })
        })
      };
      search.transformPredefinedTypes();
      expect(search.namespaceService.toFullURI.calledOnce).to.be.true;
      expect(search.config.predefinedTypes).to.deep.equal(['http://full#uri']);
    });

    it('should not convert any URIs if there is no predefined types', () => {
      let search = mockSearch({});
      search.config.predefinedTypes = undefined;
      search.namespaceService = {
        toFullURI: sinon.spy()
      };
      search.transformPredefinedTypes();
      expect(search.namespaceService.toFullURI.called).to.be.false;

      search.config.predefinedTypes = [];
      search.transformPredefinedTypes();
      expect(search.namespaceService.toFullURI.called).to.be.false;
    });

    it('should resolve only the original types', () => {
      let search = mockSearch({});
      search.config.predefinedTypes = ['short:uri'];
      search.namespaceService = {
        toFullURI: sinon.spy(() => {
          return PromiseStub.resolve({
            data: {
              'short:uri': 'http://full#uri',
              'short:uri2': 'http://full#uri2'
            }
          })
        })
      };
      search.transformPredefinedTypes();
      expect(search.namespaceService.toFullURI.calledOnce).to.be.true;
      expect(search.config.predefinedTypes).to.deep.equal(['http://full#uri']);
    });
  });

  it('should return the selected items', () => {
    let search = mockSearch({});
    let items = ['1', '2'];
    search.config.results = {
      config: {
        selectedItems: items
      }
    };
    expect(search.getSelectedItems()).to.deep.equal(items);
  });

  it('should return the search tree', () => {
    let search = mockSearch({});
    let tree = {condition: 'AND', rules: []};
    search.searchMediator.queryBuilder.tree = tree;
    expect(search.getSearchTree()).to.deep.equal(tree);
  });

  it('should clean up registered listeners in the mediator when destroyed', () => {
    let search = mockSearch({
      listeners: {
        EVENT_SEARCH: () => {
        }
      }
    }, true);
    expect(search.searchMediator.listeners).to.not.deep.equal({});
    search.ngOnDestroy();
    expect(search.searchMediator.listeners).to.deep.equal({});
  });
});
