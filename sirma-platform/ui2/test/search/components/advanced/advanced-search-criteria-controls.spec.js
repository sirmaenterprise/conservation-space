import {AdvancedSearchCriteriaControls} from 'search/components/advanced/advanced-search-criteria-controls';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import _ from 'lodash';

import {AdvancedSearchMocks} from './advanced-search-mocks'

describe('AdvancedSearchCriteriaControls', () => {

  var advancedSearchCriteriaControls;
  var criteria;

  beforeEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchCriteriaControls.prototype.criteria = undefined;
    AdvancedSearchCriteriaControls.prototype.config = undefined;

    criteria = AdvancedSearchMocks.getCriteria();
    advancedSearchCriteriaControls = new AdvancedSearchCriteriaControls();
    advancedSearchCriteriaControls.config.searchMediator = new SearchMediator({}, new QueryBuilder(criteria));
    advancedSearchCriteriaControls.criteria = criteria;
  });

  describe('addRule()', () => {
    it('should insert a new rule into the provided criteria', () => {
      var innerCriteria = criteria.rules[1];
      advancedSearchCriteriaControls.criteria = innerCriteria;

      advancedSearchCriteriaControls.addRule();
      expect(innerCriteria.rules.length).to.equal(2);
      expect(innerCriteria.rules[1]).to.include.keys(['id', 'field', 'type', 'operator', 'value']);
    });
  });

  describe('addRules()', () => {
    it('should insert new condition and rules into the provided criteria', () => {
      advancedSearchCriteriaControls.addRules();
      expect(criteria.rules.length).to.equal(3);
      expect(criteria.rules[2]).to.include.keys(['id', 'condition', 'rules']);
    });

    it('should assign an empty rule in the new conditions rules', () => {
      advancedSearchCriteriaControls.addRules();
      expect(criteria.rules[2].rules.length).to.equal(1);
      expect(criteria.rules[2].rules[0]).to.include.keys(['id', 'field', 'type', 'operator', 'value']);
    });
  });

  describe('remove()', () => {
    it('should remove all rules & condition from the provided criteria', () => {
      var rulesForRemoval = criteria.rules[1];
      advancedSearchCriteriaControls.criteria = rulesForRemoval;

      var expected = _.cloneDeep(criteria);
      expected.rules.splice(1, 1);

      advancedSearchCriteriaControls.remove();
      expect(criteria).to.deep.equal(expected);
    });
  });

  describe('and()', () => {
    it('should change the condition to AND in the provided criteria', () => {
      var expected = _.cloneDeep(criteria);
      expected.condition = SearchCriteriaUtils.AND_CONDITION;

      advancedSearchCriteriaControls.and();
      expect(criteria).to.deep.equal(expected);
    });
  });

  describe('or()', () => {
    it('should change the condition to OR in the provided criteria', () => {
      var innerCriteria = criteria.rules[1];
      advancedSearchCriteriaControls.criteria = innerCriteria;

      var expected = _.cloneDeep(innerCriteria);
      expected.condition = SearchCriteriaUtils.OR_CONDITION;

      advancedSearchCriteriaControls.or(innerCriteria);
      expect(innerCriteria).to.deep.equal(expected);
    });
  });

  describe('isConjunction()', () => {
    it('should tell if the condition is a conjunction', () => {
      advancedSearchCriteriaControls.criteria.condition = SearchCriteriaUtils.AND_CONDITION;
      expect(advancedSearchCriteriaControls.isConjunction()).to.be.true;
    });

    it('should tell if the condition is not a conjunction', () => {
      advancedSearchCriteriaControls.criteria.condition = SearchCriteriaUtils.OR_CONDITION;
      expect(advancedSearchCriteriaControls.isConjunction()).to.be.false;
    });
  });

  describe('areConditionButtonsDisabled()', () => {
    it('should tell that the condition buttons are not disabled if the component is configured to not be disabled', () => {
      advancedSearchCriteriaControls.config.disabled = false;
      expect(advancedSearchCriteriaControls.areConditionButtonsDisabled()).to.be.false;
    });

    it('should tell if the condition buttons are disabled if the component is configured to be disabled', () => {
      advancedSearchCriteriaControls.config.disabled = true;
      expect(advancedSearchCriteriaControls.areConditionButtonsDisabled()).to.be.true;
    });

    it('should tell if the condition buttons are disabled if the component is configured to be locked', () => {
      advancedSearchCriteriaControls.config.disabled = false;
      advancedSearchCriteriaControls.config.locked = [AdvancedSearchComponents.CONDITION];
      expect(advancedSearchCriteriaControls.areConditionButtonsDisabled()).to.be.true;
    });
  });

  describe('isAddRuleButtonDisabled()', () => {
    it('should tell that the add rule button is not disabled if the component is configured to not be disabled', () => {
      advancedSearchCriteriaControls.config.disabled = false;
      expect(advancedSearchCriteriaControls.isAddRuleButtonDisabled()).to.be.false;
    });

    it('should tell if the add rule button are disabled if the component is configured to be disabled', () => {
      advancedSearchCriteriaControls.config.disabled = true;
      expect(advancedSearchCriteriaControls.isAddRuleButtonDisabled()).to.be.true;
    });

    it('should tell if the add rule button are disabled if the component is configured to be locked', () => {
      advancedSearchCriteriaControls.config.disabled = false;
      advancedSearchCriteriaControls.config.locked = [AdvancedSearchComponents.ADD_RULE];
      expect(advancedSearchCriteriaControls.isAddRuleButtonDisabled()).to.be.true;
    });
  });

  describe('isRemoveGroupButtonDisabled()', () => {
    it('should tell that the button is disabled when the current criteria is a root criteria', () => {
      advancedSearchCriteriaControls.criteriaLevel = 1;
      expect(advancedSearchCriteriaControls.isRemoveGroupButtonDisabled()).to.be.true;
    });

    it('should tell that the button is not disabled when the current criteria is not a root criteria', () => {
      advancedSearchCriteriaControls.criteriaLevel = 2;
      expect(advancedSearchCriteriaControls.isRemoveGroupButtonDisabled()).to.be.false;
    });

    it('should tell that the button is disabled when the the component is configured to be disabled', () => {
      advancedSearchCriteriaControls.criteriaLevel = 2;
      advancedSearchCriteriaControls.config.disabled = true;
      expect(advancedSearchCriteriaControls.isRemoveGroupButtonDisabled()).to.be.true;
    });

    it('should tell that the button is disabled when the the component is configured to be locked', () => {
      advancedSearchCriteriaControls.criteriaLevel = 2;
      advancedSearchCriteriaControls.config.disabled = false;
      advancedSearchCriteriaControls.config.locked = [AdvancedSearchComponents.REMOVE_GROUP];
      expect(advancedSearchCriteriaControls.isRemoveGroupButtonDisabled()).to.be.true;
    });
  });

  describe('isAddGroupButtonDisabled()', () => {
    it('should tell if the add group button should be disabled', () => {
      advancedSearchCriteriaControls.config.maxLevels = 3;
      advancedSearchCriteriaControls.criteriaLevel = 3;
      expect(advancedSearchCriteriaControls.isAddGroupButtonDisabled()).to.be.true;
      advancedSearchCriteriaControls.criteriaLevel = 4;
      expect(advancedSearchCriteriaControls.isAddGroupButtonDisabled()).to.be.true;
    });

    it('should tell if the add group button should not be disabled', () => {
      advancedSearchCriteriaControls.config.maxLevels = 3;
      advancedSearchCriteriaControls.criteriaLevel = 2;
      expect(advancedSearchCriteriaControls.isAddGroupButtonDisabled()).to.be.false;
    });

    it('should tell if the add group button should not be disabled if the component is configured that way', () => {
      advancedSearchCriteriaControls.config.maxLevels = 3;
      advancedSearchCriteriaControls.criteriaLevel = 2;
      advancedSearchCriteriaControls.config.disabled = true;
      expect(advancedSearchCriteriaControls.isAddGroupButtonDisabled()).to.be.true;
    });

    it('should tell if the add group button is disabled when the the component is configured to be locked', () => {
      advancedSearchCriteriaControls.config.maxLevels = 3;
      advancedSearchCriteriaControls.criteriaLevel = 2;
      advancedSearchCriteriaControls.config.disabled = false;
      advancedSearchCriteriaControls.config.locked = [AdvancedSearchComponents.ADD_GROUP];
      expect(advancedSearchCriteriaControls.isAddGroupButtonDisabled()).to.be.true;
    });
  });

});