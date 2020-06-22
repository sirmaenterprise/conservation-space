import {SubQueryOperatorFilter} from 'search/components/advanced/filters/sub-query-operator-filter';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

describe('SubQueryOperatorFilter', function () {

  let filter = new SubQueryOperatorFilter();

  it('should not filter operators if on level 1', function () {
    expect(filter.filter({level: 1}, {}, AdvancedSearchCriteriaOperators.SET_TO_QUERY)).to.be.true;
  });

  it('should not filter non sub-query operators', function () {
    expect(filter.filter({level: 2}, {}, {id: 'test'})).to.be.true;
  });

  it('should filter sub-query operators if level is greater than 2', function () {
    expect(filter.filter({level: 3}, {}, AdvancedSearchCriteriaOperators.SET_TO_QUERY)).to.be.false;
    expect(filter.filter({level: 3}, {}, AdvancedSearchCriteriaOperators.NOT_SET_TO_QUERY)).to.be.false;
  });
});