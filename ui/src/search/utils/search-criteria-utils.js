import uuid from 'common/uuid';
import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';

export const BASIC_MODE = 'basic';
export const ADVANCED_MODE = 'advanced';
export const MIXED_MODE = 'mixed';
export const EXTERNAL_MODE = 'external';

export const OR_CONDITION = 'OR';
export const AND_CONDITION = 'AND';

export const ANY_OBJECT = 'anyObject';
export const ANY_RELATION = 'anyRelation';
export const ANY_FIELD = 'anyField';

export const CRITERIA_TYPES_RULE_FIELD = 'types';

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
  static getDefaultAdvancedSearchCriteria() {
    return {
      id: uuid(),
      // Sections rules
      condition: OR_CONDITION,
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
      field: field,
      type: type,
      operator: operator,
      value: value
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
      operator: 'equals',
      value: predefinedTypes || []
    };
  }

  static getTypesFromCriteria(criteria) {
    var types = [];
    if (!criteria) {
      return types;
    }

    var typesWalker = new DynamicTreeWalkListener().addOnRule((rule) => {
      if (CRITERIA_TYPES_RULE_FIELD !== rule.field) {
        return;
      }

      if (Array.isArray(rule.value)) {
        types.push(...rule.value);
      } else if (rule.value) {
        types.push(rule.value);
      }
    });

    QueryBuilder.walk(criteria, typesWalker);
    return types;
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
   * Determines if the provided criteria object is defined or not. A defined criteria is considered to be any non
   * empty object.
   *
   * @param criteria - the search criteria to be checked
   * @returns true if the criteria is defined or false otherwise
   */
  static isCriteriaDefined(criteria) {
    return !!criteria && !!criteria.rules;
  }

}

SearchCriteriaUtils.BASIC_MODE = BASIC_MODE;
SearchCriteriaUtils.ADVANCED_MODE = ADVANCED_MODE;
SearchCriteriaUtils.EXTERNAL_MODE = EXTERNAL_MODE;
SearchCriteriaUtils.MIXED_MODE = MIXED_MODE;

SearchCriteriaUtils.OR_CONDITION = OR_CONDITION;
SearchCriteriaUtils.AND_CONDITION = AND_CONDITION;

SearchCriteriaUtils.ANY_OBJECT = ANY_OBJECT;
SearchCriteriaUtils.ANY_RELATION = ANY_RELATION;
SearchCriteriaUtils.ANY_FIELD = ANY_FIELD;

SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD = CRITERIA_TYPES_RULE_FIELD;

SearchCriteriaUtils.SINGLE = SINGLE;
SearchCriteriaUtils.MULTIPLE = MULTIPLE;
