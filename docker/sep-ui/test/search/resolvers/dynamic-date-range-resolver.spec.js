import {DynamicDateRangeResolver} from 'search/resolvers/dynamic-date-range-resolver';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('DynamicDateRangeResolver', () => {

  var dynamicDateRangeResolver;
  beforeEach(() => {
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    dynamicDateRangeResolver = new DynamicDateRangeResolver(promiseAdapter);
  });

  it('should not resolve rules different than "Is within"', () => {
    var tree = {
      condition: 'OR', rules: [{
        field: 'createdOn',
        operator: AdvancedSearchCriteriaOperators.IS_BETWEEN.id,
        value: ['from', 'to']
      }]
    };
    dynamicDateRangeResolver.resolve(tree);
    expect(tree.rules[0].value).to.deep.equal(['from', 'to']);
  });

  it('should clear rule value if the offset configuration is incorrect', () => {
    var tree = {
      condition: 'OR', rules: [{
        field: 'createdOn',
        operator: AdvancedSearchCriteriaOperators.IS_WITHIN.id,
        value: ['last', '5']
      }]
    };
    dynamicDateRangeResolver.resolve(tree);
    expect(tree.rules[0].value).to.deep.equal([]);
  });

  it('should resolve dynamic date range rule', () => {
    var tree = {
      condition: 'OR', rules: [{
        field: 'createdOn',
        operator: AdvancedSearchCriteriaOperators.IS_WITHIN.id,
        value: ['last', '5', 'days']
      }]
    };
    dynamicDateRangeResolver.resolve(tree);
    expect(tree.rules[0].value).to.exist;
    expect(tree.rules[0].value.length).to.equal(2);
  });

  it('should resolve embedded search trees', () => {
    var embeddedTree = {
      condition: 'OR',
      rules: [{
        field: 'createdOn',
        operator: AdvancedSearchCriteriaOperators.IS_WITHIN.id,
        value: ['last', '5', 'days']
      }]
    };
    var tree = {
      condition: 'OR',
      rules: [{
        field: 'hasParent',
        operator: AdvancedSearchCriteriaOperators.SET_TO_QUERY.id,
        value: embeddedTree
      }]
    };
    dynamicDateRangeResolver.resolve(tree);
    expect(embeddedTree.rules[0].value).to.exist;
    expect(embeddedTree.rules[0].value.length).to.equal(2);
  });
});