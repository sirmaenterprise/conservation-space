/**
 * Abstract class for every type form field validator. Forces implementations to implement the validate function.
 * Provides common methods helpful for all implemented validators.
 *
 * Every validator validate method accepts following arguments:
 * @param fieldName
 *          The field name to which current validator is attached.
 * @param validatorDef
 *          The validator definition is a plain object with the following properties:
 *          {
 *            id: 'The validator id'
 *            context: {
 *              can contain specific data for every concrete validator like: target field ids, expected values or else
 *            },
 *            execution: 'whenToBeExecuted', Currently 'beforeRender' is supported and if not provided, will be executed always
 *            message: 'Some text to be displayed with the field when validator fails'
 *            level: 'error|warn|info|success'
 *          }
 * @param validationModel
 *          The current field validation model.
 * @param flatModel
 *          The current object view model which is flattened in a way that every concrete field model is mapped to the
 *          field identifier. If the validator needs data from this model it can access it using a field id.
 *
 * @author svelikov
 */
export class FieldValidator {

  constructor() {
    if (typeof this.validate !== 'function') {
      throw new TypeError('A field validator must override the \'validate\' function!');
    }
  }

  getViewValue(fieldName, validationModel) {
    // because some fields like regions might not have validation models
    return validationModel[fieldName] && validationModel[fieldName].value;
  }

  getViewValueAsNumber(fieldName, validationModel) {
    let viewValue = this.getViewValue(fieldName, validationModel);
    return viewValue ? (viewValue * 1) : viewValue;
  }

  getViewValueAsString(fieldName, validationModel) {
    let viewValue = this.getViewValue(fieldName, validationModel);
    return viewValue === null || viewValue === undefined ? null : viewValue + '';
  }

  removeValidator(validatorId, fieldViewModel) {
    let index = -1;
    for(let i = 0; i < fieldViewModel.validators.length; i++) {
      if (fieldViewModel.validators[i].id === validatorId) {
        index = i;
        break;
      }
    }
    if (index !== -1) {
      fieldViewModel.validators.splice(index, 1);
    }
  }

  /**
   * Mark that the field has been invalid. This flag doesn't change once set. It's useful sometimes to know if the field
   * was errorneous back in time.
   */
  setWasInvalid(isValid, fieldValidationModel) {
    if (!isValid && fieldValidationModel._wasInvalid === undefined) {
      fieldValidationModel._wasInvalid = !isValid;
    }
  }
}