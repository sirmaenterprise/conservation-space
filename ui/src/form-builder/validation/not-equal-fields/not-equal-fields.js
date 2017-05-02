import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Validate that a one field's value is different from another field's value.
 */
@Injectable()
export class NotEqualFields extends FieldValidator {

  /**
   * Fields that has no value are not validated and are considered to be valid.
   */
  validate(fieldName, validatorDef, validationModel) {
    let isValid = true;
    let viewValue = this.getViewValue(fieldName, validationModel);
    if (!viewValue) {
      return isValid;
    }
    let otherFieldName = validatorDef.context.value;
    let otherField = validationModel[otherFieldName];
    if (otherField) {
      isValid = otherField.value !== viewValue;
    }
    return isValid;
  }

}