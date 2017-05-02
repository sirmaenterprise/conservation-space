import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {QueryBuilder} from 'search/utils/query-builder';
import _ from 'lodash';

/**
 * Transforms search criteria trees from and to different structures recognizable from the basic search form.
 *
 * Due to the fact advanced search criteria cannot be directly presented in the basic search form it must be
 * transformed to a structure of conditions and rules that can and vice versa (basic to advanced).
 *
 * @author Mihail Radkov
 */
export class BasicSearchCriteriaTransformer {

  /**
   * Converts the provided basic search criteria mapping to an advanced search criteria tree.
   *
   * The provided advanced search tree is repopulated with the converted rules. If no criteria exists for conversion,
   * only the object type rule is converted for any object.
   *
   * @param criteriaMapping - the mapping with basic search criteria rules
   * @param tree - the advanced search tree for population
   */
  static convertBasicToAdvancedCriteria(criteriaMapping, tree) {
    if (!criteriaMapping || !tree) {
      return;
    }

    // Assigning object types
    var typeRule = SearchCriteriaUtils.buildRule("types", "", "equals", criteriaMapping.types.value);
    if (!typeRule.value || typeRule.value.length < 1) {
      typeRule.value = [SearchCriteriaUtils.ANY_OBJECT];
    }
    tree.rules[0].rules[0] = typeRule;

    // Reset inner criteria related to the object types
    tree.rules[0].rules[1] = SearchCriteriaUtils.buildCondition();
    var innerCriteria = tree.rules[0].rules[1].rules;

    // Assigning free text search rule
    if (QueryBuilder.ruleHasValues(criteriaMapping.freeText)) {
      var ftsRule = SearchCriteriaUtils.buildRule("freeText", "fts", "contains", criteriaMapping.freeText.value);
      innerCriteria.push(ftsRule);
    }

    // Assigning created on rule
    BasicSearchCriteriaTransformer.convertCreatedOnToAdvancedCriteria(criteriaMapping, innerCriteria);

    // Assigning created by rule
    if (QueryBuilder.ruleHasValues(criteriaMapping.createdBy)) {
      var createdByRule = SearchCriteriaUtils.buildRule("emf:createdBy", "object", "set_to", criteriaMapping.createdBy.value);
      // Marks that this createdBy rule should not be treated as a relationship to context rule when performing the backwards transformation.
      createdByRule.renderSeparately = true;
      innerCriteria.push(createdByRule);
    }

    // Relations to context rules
    BasicSearchCriteriaTransformer.convertRelationsAndContextToAdvancedCriteria(criteriaMapping, innerCriteria);
  }


  /**
   * Converts the createdFromDate and createdToDate from the provided basic search criteria mapping and constructs an
   * advanced criteria rule.
   *
   * If both the rules are empty - nothing is converted.
   *
   * @param criteriaMapping - the provided basic search criteria mapping which contains the date rules
   * @param criteriaRules - array where the constructed rule is pushed
   */
  static convertCreatedOnToAdvancedCriteria(criteriaMapping, criteriaRules) {
    var hasCreatedFromValue = QueryBuilder.ruleHasValues(criteriaMapping.createdFromDate);
    var hasCreatedToValue = QueryBuilder.ruleHasValues(criteriaMapping.createdToDate);
    if (hasCreatedFromValue || hasCreatedToValue) {
      var createdOnValue = [criteriaMapping.createdFromDate.value || '', criteriaMapping.createdToDate.value || ''];
      var createdOnRule = SearchCriteriaUtils.buildRule("emf:createdOn", "dateTime", "between", createdOnValue);
      criteriaRules.push(createdOnRule);
    }
  }

  /**
   * Converts multiple rules based on the provided basic search criteria mapping.
   *
   * If the mapping contains relations it constructs the same amount of rules with context as values. If the context
   * is empty it assigns anyObject as value.
   *
   * If the mappings does not contain relations but has context - it constructs single rule for anyObject as field and
   * the context as value.
   *
   * If the mapping contains no relations and context values - nothing is converted.
   *
   * @param criteriaMapping - the provided basic search criteria mapping which contains the rules
   * @param criteriaRules - array where the constructed rules are pushed
   */
  static convertRelationsAndContextToAdvancedCriteria(criteriaMapping, criteriaRules) {
    var hasRelations = QueryBuilder.ruleHasValues(criteriaMapping.relationships);
    var hasContext = QueryBuilder.ruleHasValues(criteriaMapping.context);
    if (hasRelations) {
      var context = [SearchCriteriaUtils.ANY_OBJECT];
      if (hasContext) {
        context = criteriaMapping.context.value;
      }
      var relationToContextRules = criteriaMapping.relationships.value.map((relationValue) => {
        return SearchCriteriaUtils.buildRule(relationValue, "object", "set_to", context);
      });
      criteriaRules.push(...relationToContextRules);
    } else if (!hasRelations && hasContext) {
      var anyRelation = SearchCriteriaUtils.buildRule(SearchCriteriaUtils.ANY_RELATION, "object", "set_to", criteriaMapping.context.value);
      criteriaRules.push(anyRelation);
    }
  }

  /**
   * Converts the provided advanced search tree to a format that can be recognized by the basic search form. The
   * converted rules are assigned as values to the provided basic search criteria mapping.
   *
   * @param criteriaMapping - the basic search criteria mapping
   * @param tree - the advanced search tree
   */
  static convertAdvancedToBasicCriteria(criteriaMapping, tree) {
    if (!criteriaMapping || !tree) {
      return;
    }

    // Resetting the mapping.
    Object.keys(criteriaMapping).forEach((key) => {
      criteriaMapping[key].value = '';
    });

    // Ensuring those rules' values are arrays.
    criteriaMapping.context.value = [];
    criteriaMapping.relationships.value = [];
    criteriaMapping.createdBy.value = [];

    // TYPES
    var typeRule = tree.rules[0].rules[0];
    if (!typeRule || BasicSearchCriteriaTransformer.isRuleForAnyObject(typeRule)) {
      criteriaMapping.types.value = [];
    } else {
      criteriaMapping.types.value = typeRule.value;
    }

    var innerCriteriaCondition = tree.rules[0].rules[1];
    if (!innerCriteriaCondition || innerCriteriaCondition.rules.length < 1) {
      // Nothing else to convert
      return;
    }

    innerCriteriaCondition.rules.forEach((rule) => {
      if(!rule.value) {
        rule.value = '';
      }

      if (rule.field === 'freeText') {
        criteriaMapping.freeText.value = rule.value;
      } else if (rule.field === 'emf:createdOn') {
        criteriaMapping.createdFromDate.value = rule.value[0];
        criteriaMapping.createdToDate.value = rule.value[1];
      } else if (rule.field === 'emf:createdBy' && rule.renderSeparately) {
        criteriaMapping.createdBy.value = rule.value;
      } else if (rule.field === SearchCriteriaUtils.ANY_RELATION && !BasicSearchCriteriaTransformer.isRuleForAnyObject(rule)) {
        criteriaMapping.context.value.push(...rule.value);
      } else {
        if (!BasicSearchCriteriaTransformer.isRuleForAnyObject(rule)) {
          criteriaMapping.context.value.push(...rule.value);
        }
        criteriaMapping.relationships.value.push(rule.field);
      }
    });
    // Some rules may have the same objects for value and cause duplicates
    criteriaMapping.context.value = _.uniq(criteriaMapping.context.value);
  }

  static isRuleForAnyObject(rule) {
    return rule.value.length === 1 && rule.value[0] === SearchCriteriaUtils.ANY_OBJECT;
  }

}