/**
 * Represents a validation model rule.
 *
 * Each attribute can have one or more rules in it's validation model. If after evaluation a rule is fulfilled attribute value
 * can be change or error message can be shown. Rule has following properties:
 * --- values - shows for which attribute values the rule is applicable
 * --- condition - shows if "AND" or "OR" operation should be applied on expressions. Property value is "AND" by default.
 * --- expressions - {@link ModelRuleExpression} array with rule expressions
 * --- errorLabel - (optional) label of the error message which should be displayed if rule is fulfilled
 * --- outcome - {@link ModelRuleOutcome} (optional) shows how attribute values should be changed if rule is fulfilled.
 *
 * @author Stela Djulgerova
 */
export class ModelRule {

  getValues() {
    return this.values;
  }

  setValues(values) {
    this.values = values;
    return this;
  }

  getCondition() {
    return this.condition;
  }

  setCondition(condition) {
    this.condition = condition;
    return this;
  }

  getExpressions() {
    return this.expressions;
  }

  setExpressions(expressions) {
    this.expressions = expressions;
    return this;
  }

  getErrorLabel() {
    return this.errorLabel;
  }

  setErrorLabel(errorLabel) {
    this.errorLabel = errorLabel;
    return this;
  }

  getOutcome() {
    return this.outcome;
  }

  setOutcome(outcome) {
    this.outcome = outcome;
    return this;
  }
}