import {SearchToolbar} from 'search/components/common/search-toolbar';
import {SearchMediator, EVENT_CRITERIA_RESET, EVENT_CRITERIA_PROPERTY_CHANGED} from 'search/search-mediator';
import {TranslateService} from 'services/i18n/translate-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {stubSearchService} from 'test/services/rest/search-service-mock';

import {ORDER_RELEVANCE} from 'search/order-constants';
import {EMF_CREATED_ON, EMF_MODIFIED_ON, EMF_TYPE} from 'instance/instance-properties';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {ANY_RELATION, CRITERIA_FTS_RULE_FIELD, SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {FTS_CHANGE_EVENT} from 'search/components/search';

import {stub} from 'test/test-utils';

describe('SearchToolbar', () => {

  let toolbar;
  beforeEach(() => {
    toolbar = constructToolbar();
  });

  it('should create a default configuration for search toolbar', () => {
    expect(toolbar.config.disabled).to.be.false;
    expect(toolbar.config.searchMediator).to.exist;
    expect(toolbar.config.renderOrderBar).to.be.true;
    expect(toolbar.config.renderResultBar).to.be.true;
  });

  it('should create a default configuration for the nested results toolbar', () => {
    expect(toolbar.resultsToolbar.searchMediator).to.equal(toolbar.config.searchMediator);
    expect(toolbar.resultsToolbar.message).to.not.exist;
  });

  it('should create a configuration for the nested results toolbar without messages', () => {
    toolbar = constructToolbar(false);
    toolbar.config.useMinimalResultToolbar = true;
    toolbar.ngOnInit();
    expect(toolbar.resultsToolbar.searchMediator).to.equal(toolbar.config.searchMediator);
    expect(toolbar.resultsToolbar.message.renderType).to.be.false;
    expect(toolbar.resultsToolbar.message.renderFts).to.be.false;
    expect(toolbar.resultsToolbar.message.renderContext).to.be.false;
  });

  it('should set order by and order direction based on the search mediator arguments', () => {
    toolbar = constructToolbar(false);
    toolbar.config.searchMediator.arguments = {
      orderBy: EMF_TYPE,
      orderDirection: 'direction'
    };
    toolbar.ngOnInit();
    expect(toolbar.orderToolbar.orderBy).to.equal(EMF_TYPE);
    expect(toolbar.orderToolbar.orderDirection).to.equal('direction');
  });

  it('should prepare mediator & trigger search if no params are provided', () => {
    toolbar.orderToolbar.orderBy = 'order';
    toolbar.orderToolbar.orderDirection = 'direction';
    toolbar.config.searchMediator.search = sinon.spy();

    toolbar.onOrderChanged();
    expect(toolbar.config.searchMediator.search.calledOnce).to.be.true;
    expect(toolbar.config.searchMediator.arguments.orderBy).to.eq('order');
    expect(toolbar.config.searchMediator.arguments.orderDirection).to.eq('direction');
  });

  it('should prepare mediator & trigger search if params are provided', () => {
    toolbar.config.searchMediator.search = sinon.spy();

    toolbar.onOrderChanged({
      orderBy: 'order',
      orderDirection: 'direction'
    });
    expect(toolbar.config.searchMediator.search.calledOnce).to.be.true;
    expect(toolbar.config.searchMediator.arguments.orderBy).to.eq('order');
    expect(toolbar.config.searchMediator.arguments.orderDirection).to.eq('direction');
  });

  it('should correctly fetch the free text rule from the search tree', () => {
    let rule = toolbar.getFreeTextRule();
    let expected = SearchCriteriaUtils.buildRule(CRITERIA_FTS_RULE_FIELD, 'fts', 'contains', 'free text search');

    expect(rule.field).to.eq(expected.field);
    expect(rule.type).to.eq(expected.type);
    expect(rule.value).to.eq(expected.value);
    expect(rule.operator).to.eq(expected.operator);
  });

  it('should correctly fetch order by relevance', () => {
    let orderByData = [{id: ORDER_RELEVANCE}, {id: 2}, {id: 3}];
    expect(toolbar.getOrderByRelevance(orderByData)).to.deep.eq({id: ORDER_RELEVANCE});

    orderByData = [{id: 1}, {id: 2}, {id: 3}];
    expect(toolbar.getOrderByRelevance(orderByData)).to.eq(undefined);
  });

  describe('registerOpenSavedSearchListener()', () => {

    it('should register a mediator listener for opening a search', () => {
      let openSearchListeners = toolbar.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      expect(openSearchListeners.length).to.equal(1);
    });

    it('should configure order by and order direction arguments when a saved search is opened', () => {
      let openSearchListener = toolbar.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];
      openSearchListener({
        orderBy: EMF_CREATED_ON,
        orderDirection: 'asc'
      });
      expect(toolbar.config.searchMediator.arguments.orderBy).to.equal(EMF_CREATED_ON);
      expect(toolbar.config.searchMediator.arguments.orderDirection).to.equal('asc');
    });

    it('should trigger a search when a saved search is opened', () => {
      toolbar.config.searchMediator.search = sinon.spy();
      let openSearchListener = toolbar.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];
      openSearchListener({});
      expect(toolbar.config.searchMediator.search.calledOnce).to.be.true;
    });

    it('should reinitialize the query builder with the saved search criteria', () => {
      toolbar.config.restrictions = SearchCriteriaUtils.buildRule('fd3', 'tp3', 'op3', 'vl3');
      let openSearchListener = toolbar.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT][0];
      let criteriaTree = {
        condition: SearchCriteriaUtils.OR_CONDITION,
        rules: [
          // insert the condition but not at the root level
          SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, [
            SearchCriteriaUtils.buildRule('fd1', 'tp1', 'op1', 'vl1'),
            SearchCriteriaUtils.buildRule('fd2', 'tp2', 'op2', 'vl2'),
          ])
        ]
      };
      openSearchListener({
        searchMode: 'new-search-mode',
        criteria: criteriaTree
      });

      // append the restriction criteria to root level
      criteriaTree.rules.push(toolbar.config.restrictions);
      // condition should be AND after restrictions are applied
      criteriaTree.condition = SearchCriteriaUtils.AND_CONDITION;
      let sanitized = QueryBuilder.sanitizeSearchTree(criteriaTree);

      // search mode should be extracted from the provided saved search payload
      expect(toolbar.config.searchMediator.searchMode).to.equal('new-search-mode');
      // restriction criteria should always be added at the topmost (root) level of the criteria
      expect(QueryBuilder.sanitizeSearchTree(toolbar.config.searchMediator.queryBuilder.tree)).to.deep.equal(sanitized);
    });
  });

  describe('getOrderBy()', () => {
    it('should extract the first non disabled order value if predefined is not valid', () => {
      let orderByData = [
        {id: '1', disabled: false},
        {id: '2', disabled: false},
        {id: '3', disabled: false},
        {id: '4', disabled: false},
      ];
      expect(toolbar.getOrderBy(orderByData, '200')).to.eq('1');
    });

    it('should extract a predefined order value if present and is valid', () => {
      let orderByData = [
        {id: '1', disabled: false},
        {id: '2', disabled: false},
        {id: '3', disabled: false},
        {id: '4', disabled: false},
      ];
      expect(toolbar.getOrderBy(orderByData, '2')).to.eq('2');
    });

    it('should extract first valid order value if predefined is valid but disabled', () => {
      let orderByData = [
        {id: '1', disabled: true},
        {id: '2', disabled: false},
        {id: '3', disabled: false},
        {id: '4', disabled: false},
      ];
      expect(toolbar.getOrderBy(orderByData, '1')).to.eq('2');
    });

    it('should extract the first non disabled order by value', () => {
      let orderByData = [
        {id: '1', disabled: true},
        {id: '2', disabled: true},
        {id: '3', disabled: false},
        {id: '4', disabled: false},
      ];
      expect(toolbar.getOrderBy(orderByData)).to.eq('3');
    });
  });

  describe('registerFtsChangeListener', () => {
    it('should register an event listener for when a order by dynamic change is required', () => {
      expect(toolbar.config.searchMediator.listeners[FTS_CHANGE_EVENT]).to.exist;
    });

    it('should set order by value to default when text is undefined', () => {
      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT);
      expect(toolbar.orderToolbar.orderBy).to.eq(EMF_MODIFIED_ON);
    });

    it('should set order by value to default when text is empty', () => {
      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT, '');
      expect(toolbar.orderToolbar.orderBy).to.eq(EMF_MODIFIED_ON);
    });

    it('should set order by value to "relevance" when text is not empty', () => {
      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT, 'Not Empty');
      expect(toolbar.orderToolbar.orderBy).to.eq(ORDER_RELEVANCE);
    });

    it('should restore previous order by value when fts is altered & then cleared', () => {
      toolbar.orderToolbar.orderBy = EMF_TYPE;
      // mock the relevance option as disabled
      toolbar.orderToolbar.orderByData[0].disabled = true;

      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT, 'Not Empty');
      expect(toolbar.orderToolbar.orderBy).to.eq(ORDER_RELEVANCE);

      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT, '');
      expect(toolbar.orderToolbar.orderBy).to.eq(EMF_TYPE);
    });

    it('should restore "emf:modifiedOn" order by value when "relevance" is selected on initialization', () => {
      // configure predefined conditions for relevance
      // to mock it as selected by default on initialization
      toolbar.orderToolbar.orderBy = ORDER_RELEVANCE;
      toolbar.orderToolbar.orderByData[0].disabled = false;

      toolbar.config.searchMediator.trigger(FTS_CHANGE_EVENT, '');
      expect(toolbar.orderToolbar.orderBy).to.eq(EMF_MODIFIED_ON);
    });
  });

  describe('registerPropertyChangeListener', () => {
    it('should disable the relevancy option if the old property type is freeText but the new one is not', () => {
      toolbar.orderToolbar.orderBy = ORDER_RELEVANCE;
      toolbar.orderToolbar.orderByData[0].disabled = false;

      toolbar.config.searchMediator.trigger(EVENT_CRITERIA_PROPERTY_CHANGED, {
        oldProperty: CRITERIA_FTS_RULE_FIELD,
        newProperty: 'new'
      });
      expect(toolbar.orderToolbar.orderBy).to.equal(EMF_MODIFIED_ON);
      expect(toolbar.orderToolbar.orderByData[0].disabled).to.be.true;
    });

    it('should not disable the relevancy option if the old property type is not freeText', () => {
      toolbar.orderToolbar.orderBy = ORDER_RELEVANCE;
      toolbar.orderToolbar.orderByData[0].disabled = false;

      toolbar.config.searchMediator.trigger(EVENT_CRITERIA_PROPERTY_CHANGED, {
        oldProperty: 'old',
        newProperty: 'new'
      });
      expect(toolbar.orderToolbar.orderBy).to.equal(ORDER_RELEVANCE);
      expect(toolbar.orderToolbar.orderByData[0].disabled).to.be.false;
    });

    it('should not disable the relevancy option if the new and old property type is freeText', () => {
      toolbar.orderToolbar.orderBy = ORDER_RELEVANCE;
      toolbar.orderToolbar.orderByData[0].disabled = false;

      toolbar.config.searchMediator.trigger(EVENT_CRITERIA_PROPERTY_CHANGED, {
        oldProperty: CRITERIA_FTS_RULE_FIELD,
        newProperty: CRITERIA_FTS_RULE_FIELD
      });
      expect(toolbar.orderToolbar.orderBy).to.equal(ORDER_RELEVANCE);
      expect(toolbar.orderToolbar.orderByData[0].disabled).to.be.false;
    });
  });

  describe('registerCriteriaResetListener()', () => {
    it('should subscribe for criteria reset which will disable the relevance', () => {
      toolbar.orderToolbar.orderBy = ORDER_RELEVANCE;
      toolbar.orderToolbar.orderByData[0].disabled = false;

      toolbar.config.searchMediator.trigger(EVENT_CRITERIA_RESET);
      expect(toolbar.orderToolbar.orderBy).to.equal(EMF_MODIFIED_ON);
      expect(toolbar.orderToolbar.orderByData[0].disabled).to.be.true;
    });
  });
});

function constructToolbar(init = true) {
  let toolbar = new SearchToolbar(stub(TranslateService));
  toolbar.config.searchMediator = new SearchMediator(stubSearchService(), getSearchQuery());
  if (init) {
    toolbar.ngOnInit();
  }
  return toolbar;
}

function getSearchQuery() {
  // construct the default template for the tree
  let tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
  tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule([1, 2, 3]));
  tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
  // attach fts & relation rules to the constructed tree
  tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule(CRITERIA_FTS_RULE_FIELD, 'fts', 'contains', 'free text search'));
  tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule(ANY_RELATION, 'object', 'set_to', ['emf:123456']));
  return new QueryBuilder(tree);
}