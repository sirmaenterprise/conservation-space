/**
 * Represents a rule expression.
 *
 * Each validation rule {@link ModelRule} can have one or more expression.
 * Depending on it's operation, expression shows which values in the related attribute are expected.
 * Expression has following properties:
 * --- field - shows related attribute name
 * --- operation - shows expression operation (can be "in", "not_in", "equals" etc.)
 * --- values - shows expected related attribute values
 *
 * @author Stela Djulgerova
 */
export class ModelRuleExpression {

  constructor(field, operation, values) {
    this.field = field;
    this.operation = operation;
    this.values = values || [];
  }

  getField() {
    return this.field;
  }

  setField(field) {
    this.field = field;
    return this;
  }

  getOperation() {
    return this.operation;
  }

  setOperation(operation) {
    this.operation = operation;
    return this;
  }

  getValues() {
    return this.values;
  }

  setValues(values) {
    this.values = values || [];
    return this;
  }
}