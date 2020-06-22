import _ from 'lodash';

/**
 * Represents a base validation model for the model classes. Each model which
 * requires a validation to be carried out should hold an instance of this class.
 *
 * The validation model itself is unaware of the possible validation errors.
 *
 * @author Svetlozar Iliev
 */
export class ModelValidation {

  constructor() {
    this.errors = [];
  }

  isValid() {
    return !this.hasError();
  }

  isInvalid() {
    return !this.isValid();
  }

  clearErrors() {
    this.errors = [];
  }

  addError(errorLabel) {
    this.errors.push(errorLabel);
  }

  hasError() {
    return this.errors.length > 0;
  }

  getErrors() {
    return this.errors;
  }

  copyFrom(src) {
    this.errors = _.clone(src.errors);
    return this;
  }
}