import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Validate that a value is equal to provided expected value. The equality is verified regardless of the value provided
 * value type. The actual value is converted to the expected value type if possible.
 * This validator returns false if:
 * - Actual value cannot be successfully converted to expected type;
 * - Actual value is of expected type or is converted to that type but is not equal to expected value;
 *
 * @author svelikov
 */
@Injectable()
export class Equal extends FieldValidator {

  validate(fieldName, validatorDef, validationModel) {
    let viewValue = this.getViewValue(fieldName, validationModel);
    let value = validatorDef.context.value;
    return viewValue === value;
  }

}