import {Component, Inject, View} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import _ from 'lodash';

import 'search/components/advanced/advanced-search-criteria-row';

import './code-lists-search.css!css';
import template from './code-lists-search.html!text';

const EQUALS = AdvancedSearchCriteriaOperators.EQUALS;
const CONTAINS = AdvancedSearchCriteriaOperators.CONTAINS;

/**
 * Component for filtering controlled vocabularies via advanced search row.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'code-lists-search',
  properties: {
    'codeLists': 'code-lists'
  },
  events: ['onFilter']
})
@View({
  template
})
@Inject(TranslateService)
export class CodeListsSearch {

  constructor(translateService) {
    this.translateService = translateService;
  }

  ngOnInit() {
    this.getSearchConfig();
  }

  onValue() {
    this.filter();
  }

  onOperator() {
    this.filter();
  }

  onField() {
    this.reset();
    this.filter();
  }

  filter() {
    let field = this.criteria.field;
    let value = this.criteria.value || '';
    let operator = this.criteria.operator;

    // the metadata for current field
    let meta = this.fieldsMap[field];
    // string value comparison based on the operator
    let comparator = this.getComparator(operator);

    // filter code lists based on the metadata of current field
    let filtered = _.filter(this.codeLists, code => {
      let current = code[meta.target] || code;
      return !_.isObject(current) ? this.compareValue(current, value, comparator) :
        this.compareObject(current, value, meta.fields, comparator);
    });

    this.onFilter({
      filtered: filtered.map(filtered => filtered.id)
    });
  }

  /**
   * Check if a given object has a primitive value present
   * assigned to any of it's properties
   *
   * @param object - object to check for matches
   * @param value - either array or string values
   * @param fields - object properties to compare with, if empty check all
   * @param comparator - callback to compare matching values
   */
  compareObject(object, value, fields, comparator) {
    let contains = false;
    if (_.isObject(object)) {
      // need at least one matching comparison
      contains = Object.keys(object).some(key => {
        let found = false;
        let property = object[key];

        if (property && !_.isObject(property)) {
          let noFields = !fields || !fields.length;
          // compare the value only when it is a primitive
          let has = this.compareValue(property, value, comparator);
          // when no fields are specified check all of the properties
          found = noFields ? has : _.contains(fields, key) ? has : false;
        }
        // quit as early as possible when match is found skip recursion
        return found || this.compareObject(property, value, fields, comparator);
      });
    }
    return contains;
  }

  /**
   * Compare a given property & a value using a comparator
   *
   * @param property - the property to check against
   * @param value - either string | array - compares elements
   * @param comparator - taking the two arguments to compare
   */
  compareValue(property, value, comparator) {
    // make sure property is an actual string
    property = (property).toString().toLowerCase();
    if (_.isArray(value)) {
      return value.some(v => {
        // find at least one matching comparison
        return comparator(property, v.toLowerCase());
      });
    }
    // invoke the comparator on a single value
    return comparator(property, value.toLowerCase());
  }

  reset() {
    // manually reset the value
    this.criteria.value = '';
    let field = this.criteria.field;
    // reset the operator to the default operator for the field
    this.criteria.operator = this.fieldsMap[field].operators[0];
  }

  getSearchConfig() {
    this.criteria = {};
    this.searchConfig = {
      renderRemoveButton: false
    };
    this.fieldsMap = this.getSearchFields();
    this.translateFieldsMap(this.fieldsMap);
    this.searchFields = _.values(this.fieldsMap);
  }

  getComparator(operator) {
    return (left, right) => {
      // equals should ideally be only applied for values which exist and are not empty
      return operator === EQUALS.id && right.length ? left === right : left.indexOf(right) >= 0;
    };
  }

  getSearchFields() {
    return {
      all: {
        id: 'all',
        fields: [],
        type: 'string',
        singleValued: true,
        text: 'code.lists.search.all.fields',
        operators: [CONTAINS.id, EQUALS.id],
        title: 'code.lists.search.all.fields.tooltip'
      },
      id: {
        id: 'id',
        type: 'string',
        target: 'id',
        fields: ['id'],
        singleValued: true,
        text: 'code.lists.manage.id',
        operators: [EQUALS.id, CONTAINS.id],
        title: 'code.lists.search.id.tooltip'
      },
      name: {
        id: 'name',
        type: 'string',
        fields: ['name'],
        singleValued: true,
        target: 'descriptions',
        text: 'code.lists.manage.name',
        operators: [CONTAINS.id, EQUALS.id],
        title: 'code.lists.search.name.tooltip'
      },
      comment: {
        id: 'comment',
        type: 'string',
        singleValued: true,
        fields: ['comment'],
        target: 'descriptions',
        text: 'code.lists.manage.comment',
        operators: [CONTAINS.id, EQUALS.id],
        title: 'code.lists.search.comment.tooltip'
      },
      value: {
        id: 'value',
        type: 'string',
        target: 'values',
        fields: ['id', 'name'],
        singleValued: true,
        text: 'code.lists.search.values',
        operators: [CONTAINS.id, EQUALS.id],
        title: 'code.lists.search.values.tooltip'
      },
      extra: {
        id: 'extra',
        type: 'string',
        target: 'extras',
        fields: ['1', '2', '3'],
        singleValued: true,
        text: 'code.lists.search.extras',
        operators: [CONTAINS.id, EQUALS.id],
        title: 'code.lists.search.extras.tooltip'
      }
    };
  }

  translateFieldsMap(fields) {
    Object.keys(fields).forEach(k => {
      let field = fields[k];
      field.text = this.translateService.translateInstant(field.text);
      field.title = this.translateService.translateInstant(field.title);
    });
  }
}