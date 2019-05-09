import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelsService} from 'services/rest/models-service';
import {DEFINITION_ID, PARENT} from 'instance/instance-properties';
import {SELECTION_MODE_IN_CONTEXT, SELECTION_MODE_WITHOUT_CONTEXT} from 'components/contextselector/context-selector';
import {ModelUtils} from 'models/model-utils';

export const ERROR_EXISTING_WITHOUT_CONTEXT = 'validation.error.existing_without_context';
export const ERROR_EXISTING_IN_CONTEXT = 'validation.error.existing_in_context';

/**
 * Service for interaction with context (parent) of a {@link InstanceObject}.
 */
@Injectable()
@Inject(ModelsService, PromiseAdapter)
export class InstanceContextService {
  constructor(modelsService, promiseAdapter) {
    this.modelsService = modelsService;
    this.promiseAdapter = promiseAdapter;
  }

  /**
   * Fetch value of property "existingInContext". It is described in definition of instance.
   */
  getExistingInContext(instanceObject) {
    return this.modelsService.getExistingInContextInfo(instanceObject.getModels()[DEFINITION_ID]);
  }

  /**
   * Validate existence of instance.
   * Instances can be restricted to exist:
   * <ul>
   *   <li> in context (it must have parent);
   *   <li> without context (it must not have parent);
   *   <li> can exist with and without context.
   * </ul>
   * The validation is executed on tree steps:
   * <ul>
   *   <li> fetches restriction. It is defined in field with name "existingInContext". The field is part of definition not of instance.
   *   <li> extract parent of instance if any.
   *   <li> take decision according both steps above.
   *   </ul>
   * @returns the results of decision.
   *    If the is mismatched between configuration and actual existence of instance, then error object with follow structure will be returned:
   *   <pre>
   *   {
   *      id: "emf:1111", // id of requested instance object.
   *      isValid: false,  // result of validation
   *      existingInContext: "WITHOUT_CONTEXT", // value extracted from first step
   *      errorMessage: 'validation.error.existing_without_context' // error message key
   *    }
   *    </pre>
   *    If there is not mismatch, object with follow structure will be returned:
   *    <pre>
   *        {
   *           id: "emf:1111", // id of requested instance object.
   *           isValid: true, // result of validation
   *           existingInContext: "WITHOUT_CONTEXT", // value extracted from first step
   *           errorMessage: undefined
   *        }
   *    </pre>
   */
  validateExistingInContext(instanceObject) {
    return this.getExistingInContext(instanceObject).then((existingInContext) => {
      let errorMessage = InstanceContextService.validateExistenceInContext(InstanceContextService.getParent(instanceObject), existingInContext);
      return this.promiseAdapter.resolve({
        id: instanceObject.getId(),
        isValid: errorMessage ? false : true,
        existingInContext,
        errorMessage
      });
    });
  }

  /**
   * Validates <code>instanceObjects</code> at same time.
   * @param instanceObjects
   * @return result of validation with follow structure:
   *
   * <pre>
   *     {
   *       "emf:1111":  {
   *                      id: "emf:1111", // id of requested instance object.
   *                      isValid: false,  // result of validation
   *                      existingInContext: "WITHOUT_CONTEXT", // value extracted from first step
   *                      errorMessage: 'validation.error.existing_without_context' // error message key
   *                     },
   *        "emf:1111":  {
   *                       id: "emf:1111", // id of requested instance object.
   *                       isValid: true, // result of validation
   *                       existingInContext: "WITHOUT_CONTEXT", // value extracted from first step
   *                       errorMessage: undefined
   *                      }
   *     }
   * </pre>
   */
  validateExistingInContextAll(instanceObjects = []) {
    let existingInContextValidators = Object.values(instanceObjects).map((instanceObject) => {
      return this.validateExistingInContext(instanceObject);
    });
    return this.promiseAdapter.all(existingInContextValidators).then((validationResults) => {
      let result = {};
      validationResults.forEach((validationResult) => {
        result[validationResult.id] = validationResult;
      });
      return result;
    });
  }

  /**
   * Returns the id of <code>instanceObject</code>'s parent, if any.
   */
  static getParent(instanceObject) {
    let hasParentValue = instanceObject.getPropertyValue(PARENT);
    if (hasParentValue) {
      let value = hasParentValue.getValue();
      if (value && value.length > 0) {
        return value[0];
      }
    }
  }

  /**
   * Updates the <code>instanceObject</code> parent with the provided <code>newParentId</code>.
   */
  static updateParent(instanceObject, newParentId) {
    ModelUtils.updateObjectPropertyValue(instanceObject.getModels().validationModel[PARENT], true, newParentId ? [newParentId] : undefined);
  }

  /**
   * Checks are both arguments accordance each other. If not properly error message key will be returned.
   * If <code>parent</code> is not null, then <code>existingInContext</code> can not be "WITHOUT_CONTEXT".
   * If <code>parent</code> is null, then <code>existingInContext</code> can not be "IN_CONTEXT".
   */
  static validateExistenceInContext(parentId, existingInContext) {
    if (parentId) {
      if (existingInContext === SELECTION_MODE_WITHOUT_CONTEXT) {
        return ERROR_EXISTING_IN_CONTEXT;
      }
    } else {
      if (existingInContext === SELECTION_MODE_IN_CONTEXT) {
        return ERROR_EXISTING_WITHOUT_CONTEXT;
      }
    }
  }
}
