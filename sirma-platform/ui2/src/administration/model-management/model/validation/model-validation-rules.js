/**
 * Represents all validation rules which might be used during a validation process.
 * Validation rules are meant to be used as a reference point providing the data
 * needed to define the behaviour during the validation process.
 *
 * @author Svetlozar Iliev
 */
export class ModelValidationRules {

  constructor() {
    this.rules = [];
  }

  getRules() {
    return this.rules;
  }

  setRules(rules) {
    this.rules = rules;
    return this;
  }

  seal() {
    Object.freeze(this);
  }
}