import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

describe('SearchCriteriaUtils', () => {

  describe('defaultValue()', () => {
    it('should assign the provided default value to the rule if it lacks a value', () => {
      let rule = SearchCriteriaUtils.buildRule();
      rule.value = undefined;
      SearchCriteriaUtils.defaultValue(rule, 'abc');
      expect(rule.value).to.equal('abc');
    });

    it('should assign the provided default value to the rule if its is empty', () => {
      let rule = SearchCriteriaUtils.buildRule();
      rule.value = '';
      SearchCriteriaUtils.defaultValue(rule, 'abc');
      expect(rule.value).to.equal('abc');
    });

    it('should not assign the provided default value to the rule if it has values', () => {
      let rule = SearchCriteriaUtils.buildRule();
      rule.value = '123';
      SearchCriteriaUtils.defaultValue(rule, 'abc');
      expect(rule.value).to.equal('123');
    });
  });

  describe('getFieldPropertyValueFromCriteria() & getOperatorPropertyValueFromCriteria()', () => {

    it('should return empty array if criteria is not existent', () => {
      expect(SearchCriteriaUtils.getFieldPropertyValueFromCriteria(null)).to.deep.eq([]);
    });

    it('should return empty array if criteria is not existent', () => {
      expect(SearchCriteriaUtils.getOperatorPropertyValueFromCriteria(null)).to.deep.eq([]);
    });

    it('should return array of all operators values in the criteria', () => {
      expect(SearchCriteriaUtils.getOperatorPropertyValueFromCriteria({
        condition: 'AND',
        rules: [
          {operator: '1', value: 1},
          {operator: '2', value: 2},
          {operator: '3', value: 3}
        ]
      })).to.deep.eq(['1','2','3']);
    });

    it('should return array of all field values in the criteria', () => {
      expect(SearchCriteriaUtils.getFieldPropertyValueFromCriteria({
        condition: 'AND',
        rules: [
          {field: '1', value: 1},
          {field: '2', value: 2},
          {field: '3', value: 3}
        ]
      })).to.deep.eq(['1','2','3']);
    });
  });

  describe('getFieldValuesFromCriteria() & getTypeValuesFromCriteria()', () => {

    it('should return empty array if criteria is not existent', () => {
      expect(SearchCriteriaUtils.getFieldValuesFromCriteria(null)).to.deep.eq([]);
    });

    it('should return empty array if criteria is not existent', () => {
      expect(SearchCriteriaUtils.getTypeValuesFromCriteria(null)).to.deep.eq([]);
    });

    it('should return array of all values for a given field value', () => {
      expect(SearchCriteriaUtils.getFieldValuesFromCriteria({
        condition: 'AND',
        rules: [
          {field: '1', value: 1},
          {field: '1', value: 2},
          {field: '2', value: 3},
          {field: '2', value: 4},
          {field: '3', value: 5},
          {field: '1', value: 6}
        ]
      }, '1')).to.deep.eq([1,2,6]);
    });

    it('should return array of all values for a given type value', () => {
      expect(SearchCriteriaUtils.getTypeValuesFromCriteria({
        condition: 'AND',
        rules: [
          {type: '1', value: 1},
          {type: '1', value: 2},
          {type: '2', value: 3},
          {type: '2', value: 4},
          {type: '3', value: 5},
          {type: '1', value: 6}
        ]
      }, '1')).to.deep.eq([1,2,6]);
    });
  });

  describe('replaceCriteria', () => {
    it('should not replace if one of the criteria object is undefined', () => {
      var original = {
        rules: [{id: '123'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, undefined);
      expect(original.rules.length).to.equal(1);
      expect(original.rules[0].id).to.equal('123');
    });

    it('should replace the criteria with the provided one', () => {
      var original = {
        rules: [{id: '123'}]
      };
      var target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
      expect(original.rules[0].id).to.equal('456');
      expect(original.rules[1].id).to.equal('789');
    });

    it('should replace the criteria with the provided one even if there are no rules in the original', () => {
      var original = {};
      var target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
    });
  });

  describe('replaceCriteria', () => {
    it('should not replace if one of the criteria object is undefined', () => {
      let original = {
        rules: [{id: '123'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, undefined);
      expect(original.rules.length).to.equal(1);
      expect(original.rules[0].id).to.equal('123');
    });

    it('should replace the criteria with the provided one', () => {
      let original = {
        rules: [{id: '123'}]
      };
      let target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
      expect(original.rules[0].id).to.equal('456');
      expect(original.rules[1].id).to.equal('789');
    });

    it('should replace the criteria with the provided one even if there are no rules in the original', () => {
      let original = {};
      let target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
    });
  });

  describe('isCriteriaDefined()', () => {
    it('should determine that undefined is not a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined(undefined)).to.be.false;
    });

    it('should determine that {} is not a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined({})).to.be.false;
    });

    it('should determine that criteria with rules is a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined({rules: []})).to.be.true;
    });
  });

  describe('isCriteriaEmpty()', () => {
    it('should return true if rules doesn\'t have values', () => {
      expect(SearchCriteriaUtils.isCriteriaEmpty({
        condition: 'AND',
        rules: [{
          value: []
        }, {
          value: ''
        }]
      })).to.be.true;
    });

    it('should return false if any of the rules have value', () => {
      expect(SearchCriteriaUtils.isCriteriaEmpty({
        condition: 'AND',
        rules: [{
          value: []
        }, {
          value: [1]
        }]
      })).to.be.false;

      expect(SearchCriteriaUtils.isCriteriaEmpty({
        condition: 'AND',
        rules: [{
          value: false
        }, {
          value: ''
        }]
      })).to.be.false;
    });
  });

  describe('isCriteriaTopmost(tree, criteria)', () => {
    it('should be false if the tree is undefined or incomplete', () => {
      expect(SearchCriteriaUtils.isCriteriaTopmost()).to.be.false;
      expect(SearchCriteriaUtils.isCriteriaTopmost({rules: []})).to.be.false;
      expect(SearchCriteriaUtils.isCriteriaTopmost({rules: [{rules: []}]})).to.be.false;
    });
  });

  describe('assignRestrictions(criteria, restrictions)', () => {

    it('should assign the provided restriction rule as top level rule', () => {
      let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      let restrictions = SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value');

      SearchCriteriaUtils.assignRestrictions(criteria, restrictions);
      expect(criteria.rules.length).to.equal(2);
      expect(criteria.rules[1].field).to.equal('restricted_field');
    });

    it('should assign the provided restriction condition as top level rule', () => {
      let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      let restrictions = SearchCriteriaUtils.buildCondition();
      restrictions.rules.push(SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value'));

      SearchCriteriaUtils.assignRestrictions(criteria, restrictions);
      expect(criteria.rules.length).to.equal(2);
      expect(criteria.rules[1].rules.length).to.equal(1);
      expect(criteria.rules[1].rules[0].field).to.equal('restricted_field');
    });

    it('should not assign top level rule when restrictions are empty', () => {
      let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

      SearchCriteriaUtils.assignRestrictions(criteria, SearchCriteriaUtils.buildRule('restricted_field', '', '', ''));
      expect(criteria.rules.length).to.equal(1);
    });

    it('should assign default rules if provided criteria is empty', () => {
      let criteria = SearchCriteriaUtils.buildCondition();
      let restrictions = SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value');

      SearchCriteriaUtils.assignRestrictions(criteria, restrictions);
      expect(criteria.rules.length).to.equal(2);
    });
  });

  describe('getSearchTree(tree, params)', () => {

    it('should create criteria tree if search bar parameters are provided', () => {
      let restrictions = SearchCriteriaUtils.buildRule('test','test','test','test');

      let tree = SearchCriteriaUtils.getSearchTree({
        freeText: 'foo bar',
        objectType: 'emf:Type',
        context: 'emf:123',
        restrictions
      });

      expect(tree).to.exist;

      expect(tree.rules).to.exist;
      expect(tree.rules.length).to.eq(2);

      expect(tree.rules[0].rules).to.exist;
      expect(tree.rules[0].rules.length).to.eq(2);

      let restrictionRule = tree.rules[1];
      expect(restrictionRule.field).to.eq(restrictions.field);
      expect(restrictionRule.value).to.eq(restrictions.value);
      expect(restrictionRule.operator).to.eq(restrictions.operator);

      let objectTypeRule = tree.rules[0].rules[0];
      expect(objectTypeRule.value).to.deep.equal(['emf:Type']);

      let innerConditionRules = tree.rules[0].rules[1].rules;
      expect(innerConditionRules).to.exist;
      expect(innerConditionRules.length).to.equal(2);

      let ftsRule = innerConditionRules[0];
      expect(ftsRule.field).to.equal('freeText');
      expect(ftsRule.type).to.equal('fts');
      expect(ftsRule.value).to.equal('foo bar');

      let contextRule = innerConditionRules[1];
      expect(contextRule.field).to.equal(SearchCriteriaUtils.ANY_RELATION);
      expect(contextRule.operator).to.equal(AdvancedSearchCriteriaOperators.SET_TO.id);
      expect(contextRule.value).to.deep.equal(['emf:123']);
    });

    it('should not assign inner condition if no free text or context parameters are provided', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        objectType: 'emf:Type'
      });
      expect(tree.rules[0].rules[1]).to.not.exist;
    });

    it('should not construct free text rule if no free text parameter is provided', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        context: 'emf:123'
      });

      let innerConditionRules = tree.rules[0].rules[0].rules;
      expect(innerConditionRules.length).to.equal(1);
      expect(innerConditionRules[0].field).to.equal(SearchCriteriaUtils.ANY_RELATION);
    });

    it('should not construct context rule if no context is provided', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        freeText: 'foo bar'
      });

      let innerConditionRules = tree.rules[0].rules[0].rules;
      expect(innerConditionRules.length).to.equal(1);
      expect(innerConditionRules[0].field).to.equal('freeText');
    });

    it('should construct tree from freeText & objectType parameters', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        freeText: 'foo bar',
        objectType: 'emf:Type'
      });
      expect(tree.rules[0].rules[0].field).to.equal(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);

      let innerConditionRules = tree.rules[0].rules[1].rules;
      expect(innerConditionRules.length).to.equal(1);
      expect(innerConditionRules[0].field).to.equal('freeText');
    });

    it('should construct tree from context & freeText parameters', () => {
      let tree = SearchCriteriaUtils.getSearchTree({
        context: 'emf:123',
        freeText: 'foo bar'
      });
      let innerConditionRules = tree.rules[0].rules[0].rules;

      expect(innerConditionRules.length).to.equal(2);
      expect(innerConditionRules[0].field).to.equal('freeText');
      expect(innerConditionRules[1].field).to.equal(SearchCriteriaUtils.ANY_RELATION);
    });

    it('should create empty criteria if no parameters are provided', () => {
      let tree = SearchCriteriaUtils.getSearchTree({});
      expect(tree).to.exist;
      expect(tree.rules).to.exist;
      expect(tree.rules[0].rules.length).to.equal(0);
    });
  });
});
