import {MixedSearchCriteria} from 'search/components/common/mixed-search-criteria';
import {InstanceRestService} from 'services/rest/instance-service';
import {SearchMediator, EVENT_BEFORE_SEARCH, EVENT_SEARCH} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {InstanceObject} from 'models/instance-object';
import {FTS_CHANGE_EVENT} from 'search/components/search';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubSearchService} from 'test/services/rest/search-service-mock';
import {stubContextualFactory} from 'test/services/context/contextual-objects-factory.stub';

describe('MixedSearchCriteria', () => {

  let mixedSearchCriteria;
  beforeEach(() => {
    mixedSearchCriteria = getComponentInstance();
  });

  function getComponentInstance() {
    let mixedSearchCriteria = new MixedSearchCriteria(PromiseStub, stubInstanceService(), stubContextualFactory());
    mixedSearchCriteria.config.searchMediator = new SearchMediator(stubSearchService(), new QueryBuilder({}));
    return mixedSearchCriteria;
  }

  function stubInstanceService(instance = {}) {
    let instanceServiceStub = stub(InstanceRestService);
    instanceServiceStub.load.returns(PromiseStub.resolve({data: instance}));
    return instanceServiceStub;
  }

  it('should obtain the search mediator from the configuration', () => {
    mixedSearchCriteria.initialize();
    expect(mixedSearchCriteria.searchMediator).to.equal(mixedSearchCriteria.config.searchMediator);
  });

  describe('resolveContext', () => {
    it('should not resolve any context if there is no provided context', () => {
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.not.exist;
    });

    it('should resolve the root context of the current context object if useRootContext property is specified', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'parent',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.deep.equal({'id': 'root', 'readAllowed': true});
    });

    it('should resolve no context if there is no root context and if useRootContext property is specified', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'child',
        'readAllowed': true
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.not.exist;
    });

    it('should not resolve root context if there are no read permissions', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': false
      }, {
        'id': 'child',
        'readAllowed': false
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.not.exist;
    });

    it('should resolve root context if there are no read permissions for current context', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': false
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.deep.equal({'id': 'root', 'readAllowed': true});
    });

    it('should resolve the first context with read permissions if the current context has none', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'parent',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': false
      }]);

      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.deep.equal({'id': 'root', 'readAllowed': true});
    });

    it('should resolve the first context with read permissions if the root & current context has none', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': false
      }, {
        'id': 'parent',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': false
      }]);

      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext).to.deep.equal({'id': 'parent', 'readAllowed': true});
    });

    it('should resolve CURRENT_OBJECT as context if there is context but should not use the root one', () => {
      mixedSearchCriteria.config.useRootContext = false;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.resolvedContext.id).to.equal(CURRENT_OBJECT);
    });
  });

  describe('setupCriteria()', () => {
    it('should create default tree if the provided is empty', () => {
      mixedSearchCriteria.initialize();
      let tree = mixedSearchCriteria.searchMediator.queryBuilder.tree;
      // Should've assigned at least a type rule
      expect(tree.rules[0].rules[0].field).to.equal(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.BASIC_MODE);
    });

    it('should assign predefined types & resolved context in the default tree if present & resolved', () => {
      mixedSearchCriteria.config.predefinedTypes = ['emf:Document'];
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }]);
      mixedSearchCriteria.initialize();
      let tree = mixedSearchCriteria.searchMediator.queryBuilder.tree;
      expect(tree.rules[0].rules[0].value).to.deep.equal(['emf:Document']);
      expect(tree.rules[0].rules[1].rules[1].value).to.deep.equal(['root']);
    });

    it('should assign inner search criteria if the provided lacks such one', () => {
      let incompleteTree = SearchCriteriaUtils.getSearchTree({objectType: []});
      incompleteTree.rules[0].rules.splice(1);
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(incompleteTree);
      mixedSearchCriteria.initialize();
      expect(incompleteTree.rules[0].rules[1].rules[0].field).to.equal(SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
      expect(incompleteTree.rules[0].rules[1].rules[1].field).to.equal(SearchCriteriaUtils.ANY_RELATION);
    });

    it('should assign the resolved context in the criteria tree if no rule for context is present', () => {
      mixedSearchCriteria.config.useRootContext = true;
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': false
      }]);
      let incompleteTree = SearchCriteriaUtils.getSearchTree({objectType: []});
      incompleteTree.rules[0].rules.splice(1);
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(incompleteTree);
      mixedSearchCriteria.initialize();
      expect(incompleteTree.rules[0].rules[1].rules[1].value).to.deep.equal(['root']);
    });

    it('should setup search bar model based on the criteria tree', () => {
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarModel.objectType.field).to.equal(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
      expect(mixedSearchCriteria.searchBarModel.freeText.field).to.equal(SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
      expect(mixedSearchCriteria.searchBarModel.context).to.not.exist;
    });

    it('should assign current object as context in the search bar model if present in the criteria tree', () => {
      let tree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: [CURRENT_OBJECT]});
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(tree);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarModel.context.id).to.equal(CURRENT_OBJECT);
    });

    it('should properly position free text rule if it is not the first rule in the inner criteria condition', () => {
      let incompleteTree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: []});
      // Removes the free text rule
      incompleteTree.rules[0].rules[1].rules.splice(0, 1);
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(incompleteTree);
      mixedSearchCriteria.initialize();
      expect(incompleteTree.rules[0].rules[1].rules[0].field).to.equal(SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
      expect(incompleteTree.rules[0].rules[1].rules[1].field).to.equal(SearchCriteriaUtils.ANY_RELATION);
    });

    it('should load the instance which is selected as context from the search criteria tree into the search bar model', () => {
      let tree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: ['emf:123']});
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(tree);
      let loadedInstance = {id: 'emf:123', headers: {}};
      mixedSearchCriteria.instanceRestService = stubInstanceService(loadedInstance);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarModel.context).to.deep.equal(loadedInstance);
    });

    it('should not setup search bar model based if it should not render the search bar', () => {
      mixedSearchCriteria.config.advancedOnly = true;
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarModel).to.not.exist;
    });

    it('should render advanced search if there is criteria that cannot be rendered in the search bar', () => {
      // Multiple values for any relation
      let tree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: ['emf:123', 'emf:456']});
      assertAdvancedMode(tree);

      tree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: ['emf:123']});
      tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('emf:unsupported-field'));
      assertAdvancedMode(tree);

      // Second rule for any relation
      tree = SearchCriteriaUtils.getSearchTree({objectType: [], freeText: '', context: ['emf:123']});
      tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.getDefaultAnyRelationRule());
      assertAdvancedMode(tree);
    });

    function assertAdvancedMode(tree) {
      mixedSearchCriteria = getComponentInstance();
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(tree);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
    }
  });

  describe('configureSearchBar()', () => {
    it('should yield proper configuration for the search bar', () => {
      mixedSearchCriteria.config.predefinedTypes = ['emf:Document'];
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarConfig.multiple).to.be.true;
      expect(mixedSearchCriteria.searchBarConfig.predefinedTypes).to.deep.equal(['emf:Document']);
      expect(mixedSearchCriteria.searchBarConfig.enableCurrentObject).to.be.false;
      expect(mixedSearchCriteria.searchBarConfig.disabled).to.be.false;
    });

    it('should enable current object if there is resolved context', () => {
      mixedSearchCriteria.context = mockContext([{
        'id': 'root',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }]);
      mixedSearchCriteria.initialize();
      expect(mixedSearchCriteria.searchBarConfig.enableCurrentObject).to.be.true;
    });

    it('should register mediator listeners to update the disabled state of the search bar', () => {
      mixedSearchCriteria.initialize();
      let beforeSearchListeners = mixedSearchCriteria.searchMediator.listeners[EVENT_BEFORE_SEARCH];
      let afterSearchListeners = mixedSearchCriteria.searchMediator.listeners[EVENT_SEARCH];

      beforeSearchListeners[0]();
      expect(mixedSearchCriteria.searchBarConfig.disabled).to.be.true;
      afterSearchListeners[0]();
      expect(mixedSearchCriteria.searchBarConfig.disabled).to.be.false;
    });
  });

  describe('registerSearchLoadListener()', () => {
    it('should register a mediator listener when a saved search is loaded', () => {
      mixedSearchCriteria.initialize();
      let openListeners = mixedSearchCriteria.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      expect(openListeners.length).to.equal(1);
    });

    it('should set the new search mode and rebuild the query builders', () => {
      mixedSearchCriteria.initialize();
      let openListeners = mixedSearchCriteria.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      let openListener = openListeners[0];
      mixedSearchCriteria.setupCriteria = sinon.spy();

      openListener({
        searchMode: SearchCriteriaUtils.ADVANCED_MODE
      });

      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
      expect(mixedSearchCriteria.setupCriteria.calledOnce).to.be.true;
    });
  });

  describe('onContextChange', () => {
    it('should update the criteria tree for the search bar with the provided context', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.onContextChange({id: 'emf:123'});
      let anyRelation = QueryBuilder.getFirstRule(mixedSearchCriteria.searchMediator.queryBuilder.tree, SearchCriteriaUtils.ANY_RELATION);
      expect(anyRelation.value).to.deep.equal(['emf:123']);
    });

    it('should set empty rule value for any relation in the search tree if the context is not defined', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.onContextChange();
      let anyRelation = QueryBuilder.getFirstRule(mixedSearchCriteria.searchMediator.queryBuilder.tree, SearchCriteriaUtils.ANY_RELATION);
      expect(anyRelation.value).to.deep.equal([]);
    });
  });

  describe('closeAdvancedSearch()', () => {
    it('should switch the search mode, modify the mediator to use the search bar query builder & trigger a search', () => {
      let complexTree = SearchCriteriaUtils.getSearchTree({
        objectType: [],
        freeText: '',
        context: ['emf:123', 'emf:456']
      });
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(complexTree);
      mixedSearchCriteria.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
      mixedSearchCriteria.clearResults = sinon.spy();
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.closeAdvancedSearch();
      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.BASIC_MODE);
      expect(mixedSearchCriteria.searchMediator.queryBuilder).to.equal(mixedSearchCriteria.basicSearchQueryBuilder);
      expect(mixedSearchCriteria.clearResults.calledOnce).to.be.true;
    });

    it('should construct a search bar query builder if there is none', () => {
      let complexTree = SearchCriteriaUtils.getSearchTree({
        objectType: [],
        freeText: '',
        context: ['emf:123', 'emf:456']
      });
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(complexTree);
      mixedSearchCriteria.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.closeAdvancedSearch();
      expect(mixedSearchCriteria.searchMediator.queryBuilder).to.equal(mixedSearchCriteria.basicSearchQueryBuilder);
      expect(mixedSearchCriteria.basicSearchQueryBuilder.tree.rules[0].rules[0].field).to.equal(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
    });

    it('should fire a FTS change event to force recalculation of the order by options', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.searchMediator.trigger = sinon.spy();
      mixedSearchCriteria.searchBarModel.freeText.value = 'free text';
      mixedSearchCriteria.closeAdvancedSearch();
      expect(mixedSearchCriteria.searchMediator.trigger.calledWith(FTS_CHANGE_EVENT, 'free text')).to.be.true;
    });

    it('should not construct anything if the criteria is configured for advanced only', () => {
      let complexTree = SearchCriteriaUtils.getSearchTree({
        objectType: [],
        freeText: '',
        context: ['emf:123', 'emf:456']
      });
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(complexTree);
      mixedSearchCriteria.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
      mixedSearchCriteria.config.advancedOnly = true;
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.closeAdvancedSearch();
      expect(mixedSearchCriteria.searchMediator.queryBuilder).to.not.equal(mixedSearchCriteria.basicSearchQueryBuilder);
    });
  });

  describe('onFreeTextChange()', () => {
    it('should fire a FTS change event to notify for changes in the free text field', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.searchMediator.trigger = sinon.spy();
      mixedSearchCriteria.onFreeTextChange('free text');
      expect(mixedSearchCriteria.searchMediator.trigger.calledWith(FTS_CHANGE_EVENT, 'free text')).to.be.true;
    });
  });

  describe('closeExternalSearch', () => {
    it('should switch back to the search bar', () => {
      mixedSearchCriteria.config.searchMode = SearchCriteriaUtils.EXTERNAL_MODE;
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.clearResults = sinon.spy();

      mixedSearchCriteria.closeExternalSearch();
      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.BASIC_MODE);
      expect(mixedSearchCriteria.searchMediator.queryBuilder).to.equal(mixedSearchCriteria.basicSearchQueryBuilder);
      expect(mixedSearchCriteria.clearResults.calledOnce).to.be.true;
    });
  });

  describe('onSearch()', () => {
    it('should perform a search', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.onSearch();
      expect(mixedSearchCriteria.searchMediator.service.search.calledOnce).to.be.true;
    });

    it('should reset paging arguments before executing the search', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.searchMediator.arguments.pageNumber = 4;
      mixedSearchCriteria.onSearch();
      expect(mixedSearchCriteria.searchMediator.arguments.pageNumber).to.equal(1);
    });
  });

  describe('loadSavedSearch()', () => {
    it('should trigger an event with the saved search as payload', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.searchMediator.trigger = sinon.spy();
      let savedSearch = {id: 'emf:123', properties: {}};
      mixedSearchCriteria.loadSavedSearch(savedSearch);
      expect(mixedSearchCriteria.searchMediator.trigger.calledOnce).to.be.true;
      expect(mixedSearchCriteria.searchMediator.trigger.getCall(0).args[0]).to.equal(OPEN_SAVED_SEARCH_EVENT);
      expect(mixedSearchCriteria.searchMediator.trigger.getCall(0).args[1].id).to.equal('emf:123');
    });
  });

  describe('changeMode()', () => {
    it('should switch to advanced mode, clone the search bar criteria tree & clean the search results', () => {
      let tree = SearchCriteriaUtils.getSearchTree({objectType: ['emf:Document']});
      mixedSearchCriteria.config.searchMediator.queryBuilder.init(tree);
      mixedSearchCriteria.clearResults = sinon.spy();
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.changeMode();
      expect(mixedSearchCriteria.config.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
      expect(mixedSearchCriteria.searchMediator.queryBuilder.tree).to.not.equal(mixedSearchCriteria.basicSearchQueryBuilder.tree);
      expect(mixedSearchCriteria.searchMediator.queryBuilder.tree).to.deep.equal(mixedSearchCriteria.basicSearchQueryBuilder.tree);
      expect(mixedSearchCriteria.clearResults.calledOnce).to.be.true;
    });

    it('should assign any object as default object type for the advanced search if no type is present in the tree', () => {
      mixedSearchCriteria.initialize();
      mixedSearchCriteria.changeMode();
      expect(mixedSearchCriteria.searchMediator.queryBuilder.tree.rules[0].rules[0].value).to.deep.equal([SearchCriteriaUtils.ANY_OBJECT]);
    });
  });

  function mockContext(context) {
    return {
      getCurrentObject: sinon.spy(() => {
        let instance = new InstanceObject(context[0].id);
        instance.setContextPath(context);
        return PromiseStub.resolve(instance);
      })
    };
  }
});
