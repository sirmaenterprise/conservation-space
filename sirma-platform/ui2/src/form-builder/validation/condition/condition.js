import {Injectable, Inject} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {ConditionEvaluator} from 'form-builder/validation/condition-evaluator';

/**
 * Validates and executes conditions defined in xml definitions.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ConditionEvaluator)
export class Condition extends FieldValidator {

  constructor(conditionEvaluator) {
    super();
    this.conditionEvaluator = conditionEvaluator;
  }

  validate(fieldName, validatorDef, validationModel, flatModel) {
    return this.conditionEvaluator.evaluate(fieldName, validatorDef, validationModel, flatModel);
  }

}
