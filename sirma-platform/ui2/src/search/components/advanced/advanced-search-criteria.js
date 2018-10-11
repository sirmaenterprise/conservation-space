import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';

import 'search/components/advanced/advanced-search-criteria-controls';
import 'search/components/advanced/advanced-search-criteria-row';

import './advanced-search-criteria.css!css';
import template from './advanced-search-criteria.html!text';

/**
 * Component representing a search tree structure with two different types of criteria visualization. Examples:
 *  - criteria rule:
 *    {
 *      field: 'title', // This is the field for which a query is built.
 *      type: 'string' // The field's type - string, date, long etc.
 *      operator: 'contains', // The relation between the field and the value - equals, in, does not contain etc.
 *      value: 'november' // The value. It could be an array or simple string
 *    }
 *  - criteria condition:
 *    {
 *      operator: 'OR', // defines the relation between all rules
 *      rules: []
 *    }
 *
 * The component is a configurable and passes down any provided configuration to the inner criteria.
 * Example configuration:
 *  {
 *    searchMediator: {...},
 *    disabled: false,
 *    maxLevels: 5 // Determines the maximal levels in the criteria tree
 *  }
 *
 * There is a component property criteria-level which determines the current level of the tree.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-criteria',
  properties: {
    'config': 'config',
    'context': 'context',
    'criteria': 'criteria',
    'properties': 'properties',
    'criteriaLevel': 'criteria-level'
  }
})
@View({
  template: template
})
export class AdvancedSearchCriteria extends Configurable {

  constructor() {
    super({
      maxLevels: 3,
      disabled: false
    });
  }

  isRule() {
    return !this.criteria.condition;
  }

  getNextLevel() {
    return this.criteriaLevel + 1;
  }
}