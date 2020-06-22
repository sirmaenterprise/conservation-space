import {Search, SEARCH_PROPERTIES} from 'search/components/search';
import {Configuration} from 'common/application-config';
import {NamespaceService} from 'services/rest/namespace-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {EVENT_BEFORE_SEARCH, EVENT_SEARCH, SearchMediator} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import {ORDER_DESC, ORDER_RELEVANCE} from 'search/order-constants';
import {EMF_MODIFIED_ON} from 'instance/instance-properties';
import _ from 'lodash';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubSearchService} from 'test/services/rest/search-service-mock';

function getSearchInstance(initialize = false, config = {}) {
  let configuration = mockConfigurationService();
  let searchService = stubSearchService();
  let namespaceService = mockNamespaceService();
  let search = new Search(configuration, searchService, PromiseStub, namespaceService);
  search.config = _.defaultsDeep(config, search.config);
  if (initialize) {
    search.ngOnInit();
  }
  return search;
}

function mockConfigurationService() {
  let configurationStub = stub(Configuration);
  configurationStub.get.returns(5);
  return configurationStub;
}

function mockNamespaceService() {
  let stubbedNamespaceService = stub(NamespaceService);
  stubbedNamespaceService.toFullURI = sinon.spy((uris) => {
    let map = {};
    uris.forEach((uri) => {
      map[uri] = uri;
    });
    return PromiseStub.resolve({
      data: map
    })
  });
  return stubbedNamespaceService;
}

describe('Search', () => {

  it('should create a search mediator with default order arguments if not provided', () => {
    let search = getSearchInstance(true);
    expect(search.config.searchMediator).to.exist;
    expect(search.searchMediator).to.exist;
    // Should be the same reference
    expect(search.searchMediator).to.equal(search.config.searchMediator);
    expect(search.searchMediator.arguments.orderBy).to.equal(EMF_MODIFIED_ON);
    expect(search.searchMediator.arguments.orderDirection).to.equal(ORDER_DESC);
  });

  it('should configure default ranking ordering if the criteria tree contains a FTS rule', () => {
    let criteriaTree = SearchCriteriaUtils.buildCondition();
    criteriaTree.rules.push(SearchCriteriaUtils.getDefaultFreeTextRule('test'));
    let search = getSearchInstance(true, {
      criteria: criteriaTree,
      arguments: {
        orderDirection: 'forward'
      }
    });
    expect(search.searchMediator.arguments.orderBy).to.equal(ORDER_RELEVANCE);
  });

  it('should respect the provided order direction', () => {
    let search = getSearchInstance(true, {
      arguments: {
        orderDirection: 'forward'
      }
    });
    expect(search.searchMediator.arguments.orderDirection).to.equal('forward');
  });

  it('should use the provided search mediator instead of constructing one', () => {
    let mediator = new SearchMediator({}, new QueryBuilder({}));
    mediator.arguments.orderBy = 'order-by';
    mediator.arguments.orderDirection = 'forward';

    let search = getSearchInstance();
    search.config.searchMediator = mediator;
    search.ngOnInit();

    // Should be the same reference
    expect(search.searchMediator).to.equal(search.config.searchMediator);
    expect(search.searchMediator).to.equal(mediator);
    expect(search.searchMediator.arguments.orderBy).to.equal('order-by');
    expect(search.searchMediator.arguments.orderDirection).to.equal('forward');
  });

  it('should assign default properties set if missing', () => {
    let search = getSearchInstance(true);
    expect(search.searchMediator.arguments).to.exist;
    expect(search.searchMediator.arguments.properties).to.deep.equal(SEARCH_PROPERTIES);
  });

  it('should trigger a search if configured to not render any criteria component', () => {
    let search = getSearchInstance();
    search.config.renderCriteria = false;
    search.config.triggerSearch = true;
    search.ngOnInit();
    expect(search.searchService.search.calledOnce).to.be.true;
  });

  describe('registerCallbacks()', () => {

    it('should not call mediator if config.callbacks is falsy or empty', () => {
      let search = getSearchInstance(true);
      search.searchMediator.listeners = {};

      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      search.config.callbacks = null;
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      search.config.callbacks = {};
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;
    });

    it('should not call mediator if config.callbacks[event] is falsy or empty', () => {
      let search = getSearchInstance(true);
      search.searchMediator.listeners = {};

      search.config.callbacks = {EVENT_SEARCH: undefined};
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      search.config.callbacks[EVENT_SEARCH] = null;
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;

      search.config.callbacks[EVENT_SEARCH] = [];
      search.registerCallbacks();
      expect(_.isEmpty(search.searchMediator.listeners)).to.be.true;
    });

    it('should handle single function mapping', () => {
      let search = getSearchInstance(true);
      search.searchMediator.listeners = {};

      search.config.callbacks = {};
      search.config.callbacks[EVENT_SEARCH] = _.noop;

      search.registerCallbacks();
      expect(search.searchMediator.listeners[EVENT_SEARCH]).to.deep.eq([_.noop]);
    });

    it('should handle array of callbacks', () => {
      let search = getSearchInstance(true);
      search.searchMediator.listeners = {};

      search.config.callbacks = {};
      search.config.callbacks[EVENT_SEARCH] = [_.noop, _.noop];

      search.registerCallbacks();
      expect(search.searchMediator.listeners[EVENT_SEARCH]).to.deep.eq([_.noop, _.noop]);
    });
  });

  describe('configureCriteria()', () => {
    it('should create saved search config from predefined configuration', () => {
      let search = getSearchInstance();
      search.config = Object.assign(search.config, {
        predefinedTypes: [],
        renderCriteria: true,
        savedSearch: {
          searchId: 1,
          searchTitle: 'Title'
        }
      });
      search.ngOnInit();

      expect(search.criteriaConfig.savedSearch).to.deep.equal({
        searchId: 1,
        searchTitle: 'Title',
        render: true,
        searchMediator: search.searchMediator,
        savedSearchSelect: {
          searchMediator: search.searchMediator,
          render: true
        }
      });
    });

    it('should construct configuration object for the rendered criteria component', () => {
      let search = getSearchInstance();
      search.config = Object.assign(search.config, {
        level: 2,
        restrictions: {restricted: 'restricted'},
        contextualItems: ['emf:123'],
        searchMode: 'very-advanced',
        predefinedTypes: ['emf:Meme'],
        renderHelp: true
      });
      search.ngOnInit();

      expect(search.criteriaConfig).to.exist;
      expect(search.criteriaConfig.level).to.equal(2);
      expect(search.criteriaConfig.restrictions).to.deep.equal({restricted: 'restricted'});
      expect(search.criteriaConfig.contextualItems).to.deep.equal(['emf:123']);
      expect(search.criteriaConfig.searchMode).to.equal('very-advanced');
      expect(search.criteriaConfig.predefinedTypes).to.deep.equal(['emf:Meme']);
      expect(search.criteriaConfig.renderHelp).to.be.true;
      expect(search.criteriaConfig.searchMediator).to.exist;
      // Should be the same reference
      expect(search.criteriaConfig.searchMediator).to.equal(search.searchMediator);
    });

    it('should hold saved search config with render and searchMediator attribute', () => {
      let search = getSearchInstance(true);
      expect(search.criteriaConfig.savedSearch.render).to.exist;
      expect(search.criteriaConfig.savedSearch.searchMediator).to.exist;
      // Should be the same mediator form the search component
      expect(search.criteriaConfig.savedSearch.searchMediator).to.equal(search.searchMediator);
    });

    it('should not render the save search select if there are predefined types', () => {
      let search = getSearchInstance();
      search.config.predefinedTypes = [1, 2, 3];
      search.ngOnInit();
      expect(search.criteriaConfig.savedSearch.savedSearchSelect.render).to.be.false;
    });

    it('should render the save search select if predefined types are empty', () => {
      let search = getSearchInstance();
      search.config.predefinedTypes = [];
      search.ngOnInit();
      expect(search.criteriaConfig.savedSearch.savedSearchSelect.render).to.be.true;
    });

    it('should render the save search select if there are no predefined types specified', () => {
      let search = getSearchInstance(true);
      expect(search.criteriaConfig.savedSearch.savedSearchSelect.render).to.be.true;
    });

    it('should render the save search component if the search is not external and should render the criteria', () => {
      let search = getSearchInstance();
      search.config.renderCriteria = true;
      search.ngOnInit();
      expect(search.criteriaConfig.savedSearch.render).to.be.true;
    });

    it('should not render the save search component if the search is external', () => {
      let search = getSearchInstance();
      search.config.searchMode = SearchCriteriaUtils.EXTERNAL_MODE;
      search.ngOnInit();
      expect(search.criteriaConfig.savedSearch.render).to.be.false;
    });

    it('should not render the save search component if the search criteria should not be rendered', () => {
      let search = getSearchInstance();
      search.config.renderCriteria = false;
      search.ngOnInit();
      expect(search.criteriaConfig.savedSearch.render).to.be.false;
    });
  });

  describe('configureToolbar()', () => {
    it('should create toolbar configuration', () => {
      let search = getSearchInstance(false, {
        useFixedToolbar: true,
        restrictions: {restricted: 'restricted'}
      });
      search.ngOnInit();
      expect(search.toolbarConfig.searchMediator).to.exist;
      expect(search.toolbarConfig.useMinimalResultToolbar).to.deep.eq(false);
      expect(search.toolbarConfig.restrictions).to.deep.eq({restricted: 'restricted'});
    });
  });

  describe('configurePagination()', () => {
    it('should configure pagination configuration as expected', () => {
      let search = getSearchInstance(true);
      expect(search.config.paginationConfig).to.deep.equal({
        showFirstLastButtons: true,
        page: 1,
        pageSize: 5,
        pageRotationStep: 2
      });
    });

    it('should configure pagination component to be at first page by default', () => {
      let search = getSearchInstance(true);
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should assign page number as an argument in the mediator', () => {
      let search = getSearchInstance(true);
      search.searchMediator.arguments.pageNumber = 1;
      search.paginationCallback({pageNumber: 2});
      expect(search.searchMediator.arguments.pageNumber).to.equal(2);
    });

    it('should perform search after page change', () => {
      let search = getSearchInstance(true);
      search.paginationCallback({pageNumber: 2});
      expect(search.searchService.search.calledOnce).to.be.true;
    });

    it('should assign the page size from the configurations', () => {
      let search = getSearchInstance();
      search.configuration.get.withArgs(Configuration.SEARCH_PAGE_SIZE).returns(123);
      search.ngOnInit();
      expect(search.config.paginationConfig.pageSize).to.equal(123);
    });

    it('should assign the page rotation step from the configurations', () => {
      let search = getSearchInstance();
      search.configuration.get.withArgs(Configuration.SEARCH_PAGER_MAX_PAGES).returns(5);
      search.ngOnInit();
      expect(search.config.paginationConfig.pageRotationStep).to.equal(2);
    });

    it('should not assign the page rotation step if there is no configuration', () => {
      let search = getSearchInstance();
      search.configuration.get.returns(undefined);
      search.ngOnInit();
      expect(search.config.paginationConfig.pageRotationStep).to.equal(undefined);
    });
  });

  describe('configureResults()', () => {
    it('should restore page number after search if present as an argument in the mediator', () => {
      let search = getSearchInstance(true, {
        results: {total: 0, data: []},
        paginationConfig: {showFirstLastButtons: true, page: 1}
      });
      search.config.paginationConfig.page = 2;
      search.searchMediator.arguments.pageNumber = 1;

      search.searchMediator.trigger(EVENT_SEARCH, getEventData());
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should assign first page after search if not present as an argument in the mediator', () => {
      let search = getSearchInstance(true, {
        results: {total: 0, data: []},
        paginationConfig: {showFirstLastButtons: true, page: 1}
      });
      search.config.paginationConfig.page = 2;

      delete search.searchMediator.arguments.pageNumber;
      search.searchMediator.trigger(EVENT_SEARCH, getEventData());
      expect(search.config.paginationConfig.page).to.equal(1);
    });

    it('should use the whole instance object as data', () => {
      let resultsConfig = {total: 0, data: []};
      let search = getSearchInstance(true, {
        results: resultsConfig,
        paginationConfig: {showFirstLastButtons: true, page: 1}
      });
      let eventData = getEventData();
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

  describe('configureLoadingHandlers()', () => {
    it('should disable the components before search', () => {
      let search = getSearchInstance(true);
      assignConfigurations(search, false);
      search.searchMediator.trigger(EVENT_BEFORE_SEARCH);

      expect(search.criteriaConfig.disabled).to.be.true;
      expect(search.toolbarConfig.disabled).to.be.true;
      expect(search.config.paginationConfig.disabled).to.be.true;
    });

    it('should enable the components after search', () => {
      let search = getSearchInstance(true);
      search.searchMediator.trigger(EVENT_SEARCH, {response: SearchMediator.buildEmptySearchResults()});

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
      let search = getSearchInstance(true, {triggerSearch: true});
      expect(search.searchMediator.listeners[CRITERIA_READY_EVENT]).to.exist;
    });

    it('should not register an event listener for when a criteria component is ready if not configured to trigger a search', () => {
      let search = getSearchInstance(true, {triggerSearch: false});
      expect(search.searchMediator.listeners[CRITERIA_READY_EVENT]).to.not.exist;
    });

    it('should not register an event listener for when a criteria component is ready if not configured to render the criteria', () => {
      let search = getSearchInstance(true, {triggerSearch: false, renderCriteria: false});
      search.config.triggerSearch = true;
      search.registerCriteriaReadyListener();
      expect(search.searchMediator.listeners[CRITERIA_READY_EVENT]).to.not.exist;
    });

    it('should trigger a search when a criteria component is ready', () => {
      let search = getSearchInstance(true, {triggerSearch: true});
      search.searchMediator.trigger(CRITERIA_READY_EVENT);
      expect(search.searchService.search.calledOnce).to.be.true;
    });

    it('should NOT trigger a search more than once', () => {
      let search = getSearchInstance(true, {triggerSearch: true});
      search.searchMediator.trigger(CRITERIA_READY_EVENT);
      search.searchMediator.trigger(CRITERIA_READY_EVENT);
      expect(search.searchService.search.calledOnce).to.be.true;
    });
  });

  describe('transformPredefinedTypes', () => {
    it('should convert short URIs to full URIs', () => {
      let search = getSearchInstance(true, {predefinedTypes: ['short:uri']});
      search.namespaceService.toFullURI = sinon.spy(() => PromiseStub.resolve({
        data: {
          'short:uri': 'http://full#uri'
        }
      }));
      search.ngOnInit();
      expect(search.namespaceService.toFullURI.calledOnce).to.be.true;
      expect(search.config.predefinedTypes).to.deep.equal(['http://full#uri']);
    });

    it('should not convert any URIs if there is no predefined types', () => {
      let search = getSearchInstance(true);
      expect(search.namespaceService.toFullURI.called).to.be.false;

      search.config.predefinedTypes = [];
      search.transformPredefinedTypes();
      expect(search.namespaceService.toFullURI.called).to.be.false;
    });

    it('should resolve only the original types', () => {
      let search = getSearchInstance(true, {predefinedTypes: ['short:uri']});
      search.namespaceService.toFullURI = sinon.spy(() => PromiseStub.resolve({
        data: {
          'short:uri': 'http://full#uri',
          'short:uri2': 'http://full#uri2'
        }
      }));
      search.ngOnInit();
      expect(search.config.predefinedTypes).to.deep.equal(['http://full#uri']);
    });
  });

  it('should return the selected items', () => {
    let items = ['1', '2'];
    let search = getSearchInstance(true, {
      results: {
        config: {
          selectedItems: items
        }
      }
    });
    expect(search.getSelectedItems()).to.deep.equal(items);
  });

  it('should return the search tree', () => {
    let search = getSearchInstance(true);
    let tree = {condition: 'AND', rules: []};
    search.searchMediator.queryBuilder.tree = tree;
    expect(search.getSearchTree()).to.deep.equal(tree);
  });

  it('should clean up registered listeners in the mediator when destroyed', () => {
    let search = getSearchInstance(true, {
      listeners: {
        EVENT_SEARCH: () => {
        }
      }
    });
    expect(search.searchMediator.listeners).to.not.deep.equal({});
    search.ngOnDestroy();
    expect(search.searchMediator.listeners).to.deep.equal({});
  });

  it('should show toolbar if the search mode is not external', () => {
    let search = getSearchInstance(true, {
      searchMode: SearchCriteriaUtils.BASIC_MODE
    });
    expect(search.showToolbar()).to.be.true;
  });

  it('should not show toolbar if the search mode is external', () => {
    let search = getSearchInstance(true, {
      searchMode: SearchCriteriaUtils.EXTERNAL_MODE
    });
    expect(search.showToolbar()).to.be.false;
  });
});
