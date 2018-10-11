import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Validate that a value's length is less than provided expected value.
 */
@Injectable()
export class LessStringLengthThan extends FieldValidator {

  validate(fieldName, validatorDef, validationModel) {
    let isValid = false;
    let viewValue = this.getViewValue(fieldName, validationModel);
    if(viewValue) {
      isValid = viewValue.length < validatorDef.context.value;
    }
    return isValid;
  }

}