import {Injectable, Inject} from 'app/app';
import {ModelManagementValidationService} from 'administration/model-management/services/model-managament-validation-service';

/**
 * Processor taking care of executing an action of type {@link ModelValidateAttributeAction}
 * The execution of the processor takes care of calculating the new state of the provided
 * model based on the context.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementValidationService)
export class ModelValidateAttributeActionProcessor {

  constructor(modelManagementValidationService) {
    this.modelManagementValidationService = modelManagementValidationService;
  }

  execute(action) {
    this.validate(action);
  }

  restore(action) {
    this.validate(action);
  }

  validate(action) {
    this.validateAttribute(action.getModel(), action.getContext());
  }

  validateAttribute(attribute, context) {
    this.modelManagementValidationService.validateAttribute(attribute, context);
  }
}