import {Injectable, Inject} from 'app/app';
import {ModelManagementValidationService} from 'administration/model-management/services/model-managament-validation-service';

/**
 * Processor taking care of executing an action of type {@link ModelValidateAttributesAction}
 * The execution of the processor takes care of calculating the new state of the provided
 * model based on the context.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementValidationService)
export class ModelValidateAttributesActionProcessor {

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
    action.getModel().getAttributes().forEach(attribute => this.validateAttribute(attribute, action.getModel()));
  }

  validateAttribute(attribute, context) {
    this.modelManagementValidationService.validateAttribute(attribute, context);
  }
}