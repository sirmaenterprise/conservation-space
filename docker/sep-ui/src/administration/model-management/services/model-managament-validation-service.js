import {Injectable, Inject} from 'app/app';
import {ModelManagementRuleEvaluationService} from 'administration/model-management/services/rules/model-management-rule-evaluation-service';

/**
 * Validates a provided instance of {@link ModelAttribute}.
 *
 * If even one attribute is invalid then the whole parent model is considered invalid.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(ModelManagementRuleEvaluationService)
export class ModelManagementValidationService {

  constructor(modelManagementRuleEvaluationService) {
    this.modelManagementRuleEvaluationService = modelManagementRuleEvaluationService;
  }

  /**
   * Performs complete validation of a given model attribute. See {@link validateActualAttribute}
   * and {@link validateRelatedAttributes}. The validation can be performed in a different than
   * the default context (which is extracted from the provided attribute). Such validation can
   * be done by providing custom context which represents a model node which is direct owner of
   * model attributes.
   *
   * @param attribute - attribute to be validated, can also represent the context for validation.
   * @param context - the context for which the validation should be executed. Optional, if not provided
   *                  the context is extracted from the provided attribute as first argument
   */
  validateAttribute(attribute, context) {
    context = context || attribute.getParent();
    attribute = context.getAttribute(attribute.getId());
    this.validateActualAttribute(attribute, context);
    this.validateRelatedAttributes(attribute, context);
  }

  /**
   * Performs a validation upon the specified attribute. The attribute is validated using
   * the meta data which is attached to it which contains validation model used to resolve
   * the state of the attribute based on it's value.
   *
   * @param attribute - attribute to be validated
   * @param context - attribute context (direct parent)
   */
  validateActualAttribute(attribute, context) {
    this.validateInternal(attribute, context);
  }

  /**
   * Validates only attributes which are affected by the specified attribute as argument. The
   * actual attribute that is passed as argument is not validated. See {@link validateActualAttribute}
   *
   * @param attribute - attribute used to resolve affected attributes to validate
   * @param context - attribute context (direct parent)
   */
  validateRelatedAttributes(attribute, context) {
    let affected = attribute.getMetaData().getValidationModel().getAffected();
    affected.forEach(key => this.validateInternal(context.getAttribute(key), context));
  }

  validateInternal(attribute, context) {
    let validationModel = attribute.getMetaData().getValidationModel();
    let validationRules = validationModel.getValidationRules();

    let validation = attribute.getValidation();
    let restrictions = attribute.getRestrictions();

    this.resetErrors(attribute);
    this.resetRestrictions(attribute);

    validationRules.getRules().forEach(rule => {
      let value = attribute.getValue().getValue();

      if (this.modelManagementRuleEvaluationService.evaluateRule(rule, context, value)) {
        this.addError(validation, rule.getErrorLabel());
        this.setRestrictions(restrictions, rule.getOutcome());
      }
    });

    if (restrictions.isMandatory() && attribute.getValue().isEmpty()) {
      this.addError(validation, 'administration.models.management.validation.mandatory');
    }

    // Insert other validations here
  }

  resetErrors(attribute) {
    attribute.getValidation().clearErrors();
  }

  resetRestrictions(attribute) {
    attribute.getRestrictions().copyFrom(attribute.getMetaData().getValidationModel().getRestrictions());
  }

  addError(validationModel, errorLabel) {
    if (errorLabel && errorLabel.length) {
      validationModel.addError(errorLabel);
    }
  }

  setRestrictions(model, outcome) {
    model.setUpdateable(outcome.isUpdateable());
    model.setMandatory(outcome.isMandatory());
    model.setVisible(outcome.isVisible());
  }

  isEditable(attribute) {
    return attribute && attribute.getMetaData().getValidationModel().getRestrictions().isUpdateable();
  }
}