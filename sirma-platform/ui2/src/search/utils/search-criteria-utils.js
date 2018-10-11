import _ from 'lodash';
import uuid from 'common/uuid';
import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

export const BASIC_MODE = 'basic';
export const ADVANCED_MODE = 'advanced';
export const EXTERNAL_MODE = 'external';

export const OR_CONDITION = 'OR';
export const AND_CONDITION = 'AND';

export const ANY_OBJECT = 'anyObject';
export const ANY_RELATION = 'anyRelation';
export const ANY_FIELD = 'anyField';

export const CRITERIA_TYPES_RULE_FIELD = 'types';
export const CRITERIA_FTS_RULE_FIELD = 'freeText';

// Single selection mode for criteria rules
export const SINGLE = 'single';
// Multiple selection mode for criteria rules
export const MULTIPLE = 'multiple';

/**
 * Utility class for constructing default criteria conditions and rules.
 *
 * Contains widely used constants among the search components.
 *
 * @author Mihail Radkov
 */
export class SearchCriteriaUtils {

  /**
   * Generates the default advanced search criteria between sections. Includes one section with default condition.
   */
  static getDefaultAdvancedSearchCriteria(condition) {
    return {
      id: uuid(),
      // Sections rules
      condition: condition || OR_CONDITION,
      rules: [
        {
          id: uuid(),
          // Object type <-> groups rules
          condition: AND_CONDITION,
          rules: []
        }
      ]
    };
  }

  /**
   * Generates an empty criteria rule with unique id.
   */
  static getDefaultRule() {
    return SearchCriteriaUtils.buildRule('', '', '', '');
  }

  static buildRule(field, type, operator, value) {
    return {
      id: uuid(),
      field,
      type,
      operator,
      value
    };
  }

  /**
   * Generates an empty criteria condition with unique id and a pre-set criteria rule.
   */
  static getDefaultCriteriaCondition() {
    // Assigning by default an empty rule
    return SearchCriteriaUtils.buildCondition(AND_CONDITION, [SearchCriteriaUtils.getDefaultRule()]);
  }

  static buildCondition(condition, rules) {
    return {
      id: uuid(),
      condition: condition || AND_CONDITION,
      rules: rules || []
    };
  }

  /**
   * Generates an empty object type criteria rule with unique id.
   */
  static getDefaultObjectTypeRule(predefinedTypes) {
    return {
      id: uuid(),
      field: CRITERIA_TYPES_RULE_FIELD,
      type: '',
      operator: AdvancedSearchCriteriaOperators.EQUALS.id,
      value: predefinedTypes || []
    };
  }

  static getDefaultFreeTextRule(value = '') {
    return SearchCriteriaUtils.buildRule(SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD, 'fts', AdvancedSearchCriteriaOperators.CONTAINS.id, value);
  }

  static getDefaultAnyRelationRule(value = []) {
    return SearchCriteriaUtils.buildRule(SearchCriteriaUtils.ANY_RELATION, 'object', AdvancedSearchCriteriaOperators.SET_TO.id, value);
  }

  /**
   * Assigns the provided default value as the rule's if it lacks one or it's empty.
   */
  static defaultValue(rule, defaultValue) {
    if (!rule.value || rule.value.length < 1) {
      rule.value = defaultValue;
    }
  }

  /**
   * Constructs a search tree from a given set of parameters, currently
   * supported are freeText, context and objectType. Context & objectType
   * parameters can be specified either as single valued or an array of values
   * whereas freeText is strictly single valued string parameter. Restrictions
   * parameter is also supported but it is required that a valid search criteria
   * rule is provided to the configuration parameters.
   *
   * Example configuration parameters that could be provided to this method:
   *
   * {
   *  objectType: 'emf:Type',
   *  freeText: 'some-text',
   *  context: ['first','second'],
   *  restrictions: {field: 'emf:Test', type: 'object', value: [1,2,3]}
   * }
   *
   * @param params the parameters for which to build the tree
   * @returns constructed search tree
   */
  static getSearchTree(params = {}) {
    let root = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

    if (params.objectType) {
      let objectType = _.isArray(params.objectType) ? params.objectType : [params.objectType];
      let typesRule = SearchCriteriaUtils.getDefaultObjectTypeRule(objectType);
      root.rules[0].rules.push(typesRule);
    }

    let innerCondition = SearchCriteriaUtils.buildCondition();
    if (params.freeText) {
      let ftsRule = SearchCriteriaUtils.getDefaultFreeTextRule(params.freeText);
      innerCondition.rules.push(ftsRule);
    }

    if (params.context) {
      let context = _.isArray(params.context) ? params.context : [params.context];
      let contextRule = SearchCriteriaUtils.getDefaultAnyRelationRule(context);
      innerCondition.rules.push(contextRule);
    }

    if (innerCondition.rules.length > 0) {
      root.rules[0].rules.push(innerCondition);
    }

    // assign restrictions to the root criteria if any are present
    SearchCriteriaUtils.assignRestrictions(root, params.restrictions);
    return root;
  }

  /**
   * Extracts all values for each field property in each rule for the provided criteria
   *
   * @param criteria the search criteria
   */
  static getFieldPropertyValueFromCriteria(criteria) {
    return SearchCriteriaUtils.getPropertyValuesFromCriteria(criteria, 'field');
  }

  /**
   * Extracts all values for each operator property in each rule for the provided criteria
   *
   * @param criteria the search criteria
   */
  static getOperatorPropertyValueFromCriteria(criteria) {
    return SearchCriteriaUtils.getPropertyValuesFromCriteria(criteria, 'operator');
  }

  /**
   * Extracts all rule values for all type fields equal to {@CRITERIA_TYPES_RULE_FIELD} in the search criteria
   * For more detailed documentation refer to {@SearchCriteriaUtils#getFieldValuesFromCriteria}
   *
   * @param criteria the search criteria
   */
  static getTypesFromCriteria(criteria) {
    return SearchCriteriaUtils.getFieldValuesFromCriteria(criteria, CRITERIA_TYPES_RULE_FIELD);
  }

  /**
   * Extracts all rule values for the provided field from the search criteria
   *
   * @param criteria the search criteria
   * @param field the field type for which to extract values
   */
  static getFieldValuesFromCriteria(criteria, field) {
    return SearchCriteriaUtils.getRuleValuesFromCriteria(criteria, field, 'field');
  }

  /**
   * Extracts all rule values for the provided type from the search criteria
   *
   * @param criteria the search criteria
   * @param type the type for which to extract values
   */
  static getTypeValuesFromCriteria(criteria, type) {
    return SearchCriteriaUtils.getRuleValuesFromCriteria(criteria, type, 'type');
  }

  static getPropertyValuesFromCriteria(criteria, property) {
    let values = [];
    if (!criteria) {
      return values;
    }

    let typesWalker = new DynamicTreeWalkListener().addOnRule((rule) => {
      values.push(rule[property]);
    });

    QueryBuilder.walk(criteria, typesWalker);
    return values;
  }

  static getRuleValuesFromCriteria(criteria, field, property) {
    let values = [];
    if (!criteria) {
      return values;
    }

    let typesWalker = new DynamicTreeWalkListener().addOnRule((rule) => {
      if (field !== rule[property]) {
        return;
      }

      if (Array.isArray(rule.value)) {
        values.push(...rule.value);
      } else if (rule.value) {
        values.push(rule.value);
      }
    });

    QueryBuilder.walk(criteria, typesWalker);
    return values;
  }

  /**
   * Replaces the criteria rules in the original criteria object with those from the provided one. This operation
   * preserves the original reference.
   */
  static replaceCriteria(originalCriteria, targetCriteria) {
    if (originalCriteria && targetCriteria) {
      if (originalCriteria.rules) {
        originalCriteria.rules.splice(0);
      }
      Object.assign(originalCriteria, targetCriteria);
    }
  }

  /**
   * Assigns the provided restriction rule or condition with rules into the given search criteria.
   *
   * The restriction rule/condition is assigned at root level to ensure it cannot be manipulated with the search form(s)
   * To enforce the restriction to be applied to the search results, the root condition is changed to AND.
   *
   * If the given criteria is empty, it will be initialized with the default search tree used in the search components.
   *
   * @param criteria - the search criteria to populate restrictions with. Reference is preserved.
   * @param restrictions - a search rule or condition with rules which are assigned in the given criteria
   */
  static assignRestrictions(criteria, restrictions) {
    if (!restrictions || SearchCriteriaUtils.isCriteriaEmpty(restrictions)) {
      return;
    }

    if (SearchCriteriaUtils.isCriteriaEmpty(criteria)) {
      let defaultCriteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      SearchCriteriaUtils.replaceCriteria(criteria, defaultCriteria);
    }

    // Insert restriction at root criteria level
    criteria.condition = SearchCriteriaUtils.AND_CONDITION;
    criteria.rules.push(restrictions);
  }

  /**
   * Determines if the provided criteria object is defined or not. A defined criteria is considered to be any non
   * empty object.
   *
   * @param criteria - the search criteria to be checked
   * @returns true if the criteria is defined or false otherwise
   */
  static isCriteriaDefined(criteria) {
    return !!criteria && !!criteria.rules;
  }

  /**
   * Check if any of the rules has value.
   * @param criteria
   * @returns {boolean} true if it is empty criteria i.e. there is no value in any of the rules
   */
  static isCriteriaEmpty(criteria) {
    let isEmpty = true;
    let listener = new DynamicTreeWalkListener()
      .addOnRule((rule) => {
        let value = rule.value;
        if (_.isObject(value) || _.isArray(value)) {
          if (!_.isEmpty(value)) {
            isEmpty = false;
          }
        } else if (!SearchCriteriaUtils.isEmptyPrimitive(value)) {
          isEmpty = false;
        }
      });
    QueryBuilder.walk(criteria, listener);
    return isEmpty;
  }

  static isEmptyPrimitive(value) {
    return value === undefined || value === null || value === '';
  }

  /**
   * Checks if the specified criteria is the topmost criteria inside the search tree.
   * This method requires a compete search tree. Specified by getDefaultAdvancedSearchCriteria()
   * @param tree the search tree
   * @param criteria the criteria to be checked
   * @returns {boolean} true if the criteria is topmost, false otherwise
   */
  static isCriteriaTopmost(tree, criteria) {
    if (tree && tree.rules[0] && tree.rules[0].rules[1]) {
      let rules = tree.rules[0].rules[1].rules;
      //check if the current criteria is the first criteria
      return rules && criteria.id === rules[0].id;
    }
    return false;
  }
}

SearchCriteriaUtils.BASIC_MODE = BASIC_MODE;
SearchCriteriaUtils.ADVANCED_MODE = ADVANCED_MODE;
SearchCriteriaUtils.EXTERNAL_MODE = EXTERNAL_MODE;

SearchCriteriaUtils.OR_CONDITION = OR_CONDITION;
SearchCriteriaUtils.AND_CONDITION = AND_CONDITION;

SearchCriteriaUtils.ANY_OBJECT = ANY_OBJECT;
SearchCriteriaUtils.ANY_RELATION = ANY_RELATION;
SearchCriteriaUtils.ANY_FIELD = ANY_FIELD;

SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD = CRITERIA_TYPES_RULE_FIELD;
SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD = CRITERIA_FTS_RULE_FIELD;

SearchCriteriaUtils.SINGLE = SINGLE;
SearchCriteriaUtils.MULTIPLE = MULTIPLE;
