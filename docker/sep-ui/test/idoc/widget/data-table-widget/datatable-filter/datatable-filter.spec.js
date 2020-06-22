import {DatatableFilter} from 'idoc/widget/datatable-widget/datatable-filter/datatable-filter';
import {PluginsService} from 'services/plugin/plugins-service';
import {stub} from 'test/test-utils';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

const CRITERIA_MAP = {
  'emf:type': SearchCriteriaUtils.buildRule('emf:type', 'codeList', AdvancedSearchCriteriaOperators.IN.id, ['emf:project', 'emf:case']),
  'emf:title': SearchCriteriaUtils.buildRule('emf:title', 'string', AdvancedSearchCriteriaOperators.CONTAINS.id, ['test'])
};

const FILTER_CRITERIA = {condition: SearchCriteriaUtils.AND_CONDITION, rules: [
  CRITERIA_MAP['emf:type'],
  CRITERIA_MAP['emf:title']
]};

describe('DatatableFilter', () => {

  let datatableFilter;

  before(() => {
    let pluginsServiceStub = stub(PluginsService);
    datatableFilter = new DatatableFilter(undefined, undefined, undefined, undefined, pluginsServiceStub);
    datatableFilter.config = {
      filterCriteria: FILTER_CRITERIA,
      headers: [{
        type: 'codeList',
        uri: 'emf:type'
      }]
    };
  });

  it('should build criteria map based on the filter criteria', () => {
    datatableFilter.buildCriteriaMap();
    expect(Object.keys(datatableFilter.criteriaMap).length).to.equals(1);
    expect(datatableFilter.criteriaMap['emf:type'].value).to.eql(['emf:project', 'emf:case']);
  });

  it('should convert map to criteria', () => {
    let criteria = DatatableFilter.mapToCriteria(CRITERIA_MAP);
    expect(criteria.condition).to.equals(SearchCriteriaUtils.AND_CONDITION);
    expect(criteria.rules.length).to.equals(2);
    expect(criteria.rules[0].field).oneOf(['emf:type', 'emf:title']);
  });

  it('should convert criteria to map', () => {
    let criteriaMap = DatatableFilter.criteriaToMap(FILTER_CRITERIA);
    expect(criteriaMap).to.have.all.keys(['emf:type', 'emf:title']);
  });
});
