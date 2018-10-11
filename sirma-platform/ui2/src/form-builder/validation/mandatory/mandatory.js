import {Injectable, Inject} from 'app/app';
import {ModelUtils} from 'models/model-utils';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {ConditionEvaluator} from 'form-builder/validation/condition-evaluator';

/**
 * This validator is added only to the fields marked as mandatory or have a condition of type MANDATORY or OPTIONAL in
 * the definition.
 * If a field is mandatory, then verify that it has a value and return error if it doesn't have.
 * If it has condition, evaluate it and return the result.
 *
 * model: {
 *   'id': 'mandatory',
 *   'error': 'the error message',
 *   // The rules array is applied only if the field has MANDATORY or OPTIONAL condition.
 *   'rules': [{
 *     'id': 'mandatoryFields',
 *     'renderAs': 'MANDATORY',
 *     'expression': '[functional] IN ('MDG')'
 *   }]
 * }
 *
 * @author svelikov
 */
@Injectable()
@Inject(ConditionEvaluator)
export class Mandatory extends FieldValidator {

  constructor(conditionEvaluator) {
    super();
    this.conditionEvaluator = conditionEvaluator;
  }

  validate(fieldName, validatorDef, validationModel, flatModel) {
    if (validatorDef.rules) {
      this.conditionEvaluator.evaluate(fieldName, validatorDef, validationModel, flatModel);
    }
    return this.isValid(fieldName, flatModel, validationModel);
  }

  isValid(fieldName, flatModel, validationModel) {
    if (!flatModel[fieldName].isMandatory) {
      return true;
    }
    let value;
    let hasValue;
    if (ModelUtils.isPicker(flatModel[fieldName])) {
      value = this.getViewValue(fieldName, validationModel);
      if (value && value.results !== undefined) {
        hasValue = value.results.length > 0;
      } else {
        hasValue = this.isValidTextField(fieldName, validationModel);
      }
    } else {
      hasValue = this.isValidTextField(fieldName, validationModel);
    }
    return hasValue;
  }

  isValidTextField(fieldName, validationModel) {
    return Mandatory.hasValue(this.getViewValueAsString(fieldName, validationModel));
  }

  static hasValue(viewValue) {
    return !!(viewValue && viewValue.trim() !== '');
  }
}