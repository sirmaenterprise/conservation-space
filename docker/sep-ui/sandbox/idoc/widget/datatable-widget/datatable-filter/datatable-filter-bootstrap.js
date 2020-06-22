import {Component, View, Inject} from 'app/app';
import {DatatableFilter} from 'idoc/widget/datatable-widget/datatable-filter/datatable-filter';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {MomentAdapter} from 'adapters/moment-adapter';

import template from './datatable-filter-bootstrap.html!text';

@Component({
  selector: 'datatable-filter-bootstrap'
})
@View({
  template
})
@Inject(MomentAdapter)
class DatatableFilterBootstrap {

  constructor(momentAdapter) {
    this.momentAdapter = momentAdapter;

    this.todayISO = new Date().toISOString();
    this.todayFormatted = this.momentAdapter.parse(this.todayISO).format('MMMM/DD/YYYY');

    this.widgetConfig = {
      selectObjectMode: 'manually',
      selectedObjects: ['emf:123456']
    };

    // Filter config with initial criteria
    this.filterConfigInitial = {
      headers: this.getHeaders(),
      filterCriteria: {condition: 'AND', rules: [
        {field: 'emf:type', type: 'codeList', operator: AdvancedSearchCriteriaOperators.IN.id, value: ['OT210027']},
        {field: 'emf:department', type: 'codeList', operator: AdvancedSearchCriteriaOperators.IN.id, value: ['ENG','INF']},
        {field: 'dcterms:title', type: 'string', operator: AdvancedSearchCriteriaOperators.CONTAINS.id, value: 'Initial value'},
        {field: 'emf:hasParent', type: 'object', operator: AdvancedSearchCriteriaOperators.SET_TO.id, value: ['4']},
        {field: 'emf:createdOn', type: 'dateTime', operator: AdvancedSearchCriteriaOperators.IS.id, value: this.getDateFilterInitialValue()},
        {field: 'emf:dependsOn', type: 'object', operator: AdvancedSearchCriteriaOperators.SET_TO.id, value: ['4']},
        {field: 'emf:integrated', type: 'object', operator: AdvancedSearchCriteriaOperators.IN.id, value: [false]}
      ]}
    };

    this.filterConfig = {
      headers: this.getHeaders()
    };

    this.context = {};
  }

  onFilter(criteria) {
    this.filterCriteria = criteria;
  }

  getDateFilterInitialValue() {
    let date = this.momentAdapter.parse(new Date().toISOString());
    let startDate = date.clone().startOf('day');
    let endDate = date.clone().endOf('day');
    return [startDate, endDate];
  }

  getHeaders() {
    return [{
      type: 'codeList',
      uri: 'emf:type',
      codeLists: [210]
    }, {
      type: 'codeList',
      uri: 'emf:department',
      codeLists: [503]
    }, {
      type: 'string',
      uri: 'dcterms:title'
    }, {
      type: 'object',
      uri: 'emf:hasParent'
    }, {
      type: 'dateTime',
      uri: 'emf:createdOn'
    }, {
      type: 'object',
      uri: 'emf:dependsOn'
    }, {
      type: 'boolean',
      uri: 'emf:integrated'
    }];
  }
}
