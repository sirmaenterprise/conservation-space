import {Component, View} from 'app/app';
import {SearchComponent} from 'search/components/common/search-component';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';

import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'font-awesome/css/font-awesome.css!';
import './advanced-search-criteria-controls.css!css';
import template from './advanced-search-criteria-controls.html!text';

/**
 * Component responsible for inserting new rules and conditions into the criteria tree or removing them.
 * It uses the search mediator in the config to do those operations.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-criteria-controls',
  properties: {
    'config': 'config',
    'criteria': 'criteria',
    'criteriaLevel': 'criteria-level'
  }
})
@View({
  template: template
})
export class AdvancedSearchCriteriaControls extends SearchComponent {

  constructor() {
    super({
      disabled: false
    });
  }

  addRule() {
    var rule = SearchCriteriaUtils.getDefaultRule();
    this.config.searchMediator.addCriteria(rule, this.criteria.id);
  }

  addRules() {
    var defaultCondition = SearchCriteriaUtils.getDefaultCriteriaCondition();
    this.config.searchMediator.addCriteria(defaultCondition, this.criteria.id);
  }

  and() {
    this.criteria.condition = SearchCriteriaUtils.AND_CONDITION;
  }

  or() {
    this.criteria.condition = SearchCriteriaUtils.OR_CONDITION;
  }

  remove() {
    this.config.searchMediator.removeCriteria(this.criteria);
  }

  /**
   * Determines if the logical conditions between the rules is a conjunction (or AND in other words) or disjunction (OR).
   * @returns {boolean} true if it is conjunction
   */
  isConjunction() {
    return this.criteria.condition === SearchCriteriaUtils.AND_CONDITION;
  }

  areConditionButtonsDisabled() {
    return this.isLockedOrDisabled(AdvancedSearchComponents.CONDITION);
  }

  isAddRuleButtonDisabled() {
    return this.isLockedOrDisabled(AdvancedSearchComponents.ADD_RULE);
  }

  isRemoveGroupButtonDisabled() {
    return this.criteriaLevel === 1 || this.isLockedOrDisabled(AdvancedSearchComponents.REMOVE_GROUP);
  }

  isAddGroupButtonDisabled() {
    return this.config.maxLevels <= this.criteriaLevel || this.isLockedOrDisabled(AdvancedSearchComponents.ADD_GROUP);
  }
}