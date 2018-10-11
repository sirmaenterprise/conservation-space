import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Validate that a one field's value is equal to another field's value.
 */
@Injectable()
export class EqualFields extends FieldValidator {

  validate(fieldName, validatorDef, validationModel) {
    let isValid = false;
    let viewValue = this.getViewValue(fieldName, validationModel);
    let otherFieldName = validatorDef.context.value;
    let otherField = validationModel[otherFieldName];
    if (otherField) {
      isValid = otherField.value === viewValue;
    }
    return isValid;
  }

}