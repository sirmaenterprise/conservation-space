import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Disables the field to which this validator is applied if its value is equal to the value of the target field.
 *
 * @author svelikov
 */
@Injectable()
export class DisabledIfEqual extends FieldValidator {

  validate(fieldName, validatorDef, validationModel, flatModel) {
    let viewValue = this.getViewValue(fieldName, validationModel);
    let targetFieldValue = this.getViewValue(validatorDef.context.target, validationModel);
    let areEqual = viewValue && targetFieldValue && (viewValue === targetFieldValue);
    areEqual = areEqual || false;
    flatModel[fieldName].disabled = areEqual;
    return !areEqual;
  }

}