import {AdvancedSearch} from 'search/components/advanced/advanced-search';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';

import {AdvancedSearchMocks} from './advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('AdvancedSearch', () => {

  var advancedSearch;
  var criteria;

  beforeEach(() => {
    criteria = AdvancedSearchMocks.getAdvancedSearchCriteria();
    AdvancedSearch.prototype.config = {
      searchMediator: new SearchMediator({}, new QueryBuilder(criteria))
    };
    advancedSearch = new AdvancedSearch(mock$scope(), {});
  });

  afterEach(() => {
    AdvancedSearch.prototype.config = undefined;
  });

  it('should assign loader for properties', () => {
    var getSpy = sinon.spy();
    var propertiesRestServiceMock = {
      getSearchableProperties: getSpy
    };

    advancedSearch = new AdvancedSearch(mock$scope(), propertiesRestServiceMock);
    expect(advancedSearch.loaders).to.exist;
    expect(advancedSearch.loaders.properties).to.exist;

    advancedSearch.loaders.properties();
    expect(getSpy.calledOnce).to.be.true;
  });

  it('should assign criteria by default if missing', ()=> {
    AdvancedSearch.prototype.config = {
      searchMediator: new SearchMediator({}, new QueryBuilder({}))
    };
    advancedSearch = new AdvancedSearch(mock$scope());

    var tree = advancedSearch.config.searchMediator.queryBuilder.tree;
    expect(tree).to.exist;
    expect(tree.rules).to.exist;
    expect(tree.condition).to.equal(SearchCriteriaUtils.OR_CONDITION);
    expect(tree.rules[0]).to.exist;
  });

  it('should not replace provided criteria with default', ()=> {
    var expected = {id: '1', field: '2', operator: '3', value: '4'};
    var tree = advancedSearch.config.searchMediator.queryBuilder.tree;
    expect(tree.rules[0].rules[0]).to.include(expected);
  });

  it('should obtain the criteria from the query builder in the search mediator', ()=> {
    var tree = advancedSearch.config.searchMediator.queryBuilder.tree;
    expect(advancedSearch.criteria).to.deep.equal(tree);
  });

  it('should register a watcher for the criteria tree', () => {
    var newTree = {rules: [{rules: []}]};
    advancedSearch.config.searchMediator.queryBuilder.tree = newTree;
    advancedSearch.$scope.$digest();
    expect(advancedSearch.criteria).to.deep.equal(newTree);
  });

  it('should assign default criteria if the new tree is empty', () => {
    var newTree = {};
    advancedSearch.config.searchMediator.queryBuilder.tree = newTree;
    advancedSearch.$scope.$digest();
    expect(advancedSearch.criteria).to.not.deep.equal(newTree);
    expect(advancedSearch.criteria.rules[0]).to.exist;
  });

  describe('clear()', ()=> {
    it('should clear the search criteria and reset the search tree', ()=> {
      advancedSearch.criteria = {
        rules: [{
          id: '123'
        }]
      };
      advancedSearch.config.searchMediator.queryBuilder.tree = advancedSearch.criteria;

      advancedSearch.clear();

      var tree = advancedSearch.config.searchMediator.queryBuilder.tree;
      expect(tree.rules.length).to.equal(0);
      expect(advancedSearch.criteria).to.deep.equal({});
    });

    it('should assign default criteria after clearing the search criteria', ()=> {
      advancedSearch.criteria = {};
      advancedSearch.config.searchMediator.queryBuilder.tree = {};
      advancedSearch.clear();
      advancedSearch.$scope.$digest();
      expect(advancedSearch.criteria.rules[0].rules).to.exist;
      expect(advancedSearch.config.searchMediator.queryBuilder.tree.rules[0].rules).to.exist;
    });

    it('should delegate to clear the search results', () => {
      advancedSearch.clearResults = sinon.spy();
      advancedSearch.clear();
      expect(advancedSearch.clearResults.calledOnce).to.be.true;
    });
  });
});