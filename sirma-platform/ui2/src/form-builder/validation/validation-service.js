import {Injectable, Inject} from 'app/app';
import _ from 'lodash';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PluginsService, NotificationService, TranslateService, Eventbus, Logger, Configuration, PromiseAdapter)
export class ValidationService {

  constructor(pluginsService, notificationService, translateService, eventbus, logger, configuration, promiseAdapter) {
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.pluginsService = pluginsService;
    this.eventbus = eventbus;
    this.logger = logger;
    this.promiseAdapter = promiseAdapter;
    this.debugMode = configuration.get(Configuration.RNC_DEBUG_ENABLED);
  }

  init() {
    if (!this.registeredValidators) {
      return this.pluginsService.loadPluginServiceModules('field-validators', 'name').then((modules) => {
        this.registeredValidators = modules;
        this.registeredValidatorDefinitions = this.pluginsService.getPluginDefinitions('field-validators', 'name');
      });
    }
    return this.promiseAdapter.resolve(this.registeredValidators);
  }

  /**
   * Validates form model by executing all registered validators.
   * When one of the angular validators fails the others registered for the same field might fail because the view
   * value will not be applied to the model.
   *
   * @param validationModel
   *          The form validation model.
   * @param viewModel
   *          Required argument that contains the forms hierarchical view model as returned by the backend service or
   *          the view model converted to a flat one where every field object is mapped to field's identifier.
   *          Internally this validator works with a flat model.
   * @param id
   *          The object id for which this form is initialized. This id is passed with the AfterFormValidationEvent and
   *          can be watched in this component clients.
   * @param execution
   *          When current validation is invoked. According to this some validators might be executed once, for example
   *          before form to be rendered or everytime.
   * @param formControl
   *          The form controller. It's used only by validators which need it (like related-codelist-validator).
   * @param definitionId
   *          The definitionId of the current instance.
   * @param instanceId
   *          The id of the current instance.
   * @returns {boolean} If the for is valid or not.
   */
  validate(validationModel, viewModel, id, execution, formControl, definitionId, instanceId) {
    let flatModel = viewModel;
    let inaccessibleMandatoryFields = [];
    let commonValidationParams = {
      validForm: true,
      asyncValidators: [],
      syncValidators: []
    };
    let fieldsNames = Object.keys(flatModel);
    for (let i = 0, length = fieldsNames.length; i < length; i++) {
      let fieldName = fieldsNames[i];
      let fieldViewModel = flatModel[fieldName];
      let validators = fieldViewModel.validators;
      if (validators && validators.length) {
        let data = {
          validators,
          registeredValidators: this.registeredValidators,
          registeredValidatorDefinitions: this.registeredValidatorDefinitions,
          execution,
          fieldName,
          validationModel,
          flatModel,
          formControl,
          translateService: this.translateService,
          isValidField: true,
          definitionId,
          instanceId
        };

        ValidationService.differentiateFieldValidators(data, commonValidationParams);

        // if a field can not be edited in a given state, it must not be mandatory.
        if (this.debugMode && ValidationService.isInaccessibleEmptyMandatoryField(fieldViewModel, validationModel)) {
          inaccessibleMandatoryFields.push(fieldViewModel.identifier);
        }
      }
    }
    if (inaccessibleMandatoryFields.length > 0) {
      this.logger.error('This object might have uncompleted mandatory fields that are not currently accessible. This is caused by wrong defintion file: ' + JSON.stringify(inaccessibleMandatoryFields));
    }
    return this.promiseAdapter.all(commonValidationParams.asyncValidators).then(() => {
      commonValidationParams.syncValidators.forEach(syncValidatorCallback => syncValidatorCallback());
      validationModel.isValid = commonValidationParams.validForm;
      this.publishAfterFormValidationEvent(commonValidationParams.validForm, id, validationModel, viewModel);
      return commonValidationParams.validForm;
    }).catch(this.logger.error);
  }

  /**
   * Validates <code>instanceObjects</code> at same time.
   * @param instanceObjects arrays with instance objects for validation.
   * @return object with validation results. Example of returned object:
   *
   * <pre>
   *     {
   *        "emf:001": {
   *           isValid: true
   *        },
   *        "emf:002": {
   *           isValid: false
   *        }
   *     }
   * </pre>
   */
  validateAll(instanceObjects = []) {
    // Filters all instance without id.
    let filteredInstanceObjects = instanceObjects.filter((instanceObject) => {
      return instanceObject.getId();
    });

    // Collects validators for all filtered instances.
    let validators = filteredInstanceObjects.map((instanceObject) => {
      let instanceObjectModels = instanceObject.getModels();
      // return reference of validation (which is promise).
      return this.validate(instanceObjectModels.validationModel,
        instanceObjectModels.viewModel.flatModelMap, instanceObject.getId(), undefined, null, instanceObjectModels.definitionId, instanceObjectModels.id);
    });

    // Executes all validations at same time.
    return this.promiseAdapter.all(validators).then((results) => {
      let validationResults = {};
      results.forEach((isValid, index) => {
        validationResults[filteredInstanceObjects[index].getId()] = {isValid};
      });
      return validationResults;
    });
  }

  /**
   * Determines whether a validator is sync or async and pushes it to its array.
   * Synchronous validators are pushed in a callback with additional logic,
   * waiting to be executed after the async validators. Async validators are executed
   * on the spot with their promise stored in the asyncValidators array waiting to be resolved
   * and additional validation logic to be applied.
   * @param data field validator data
   * @param commonValidationParams object consisting of common to every field validation parameters (sync and async validation arrays and validForm parameter).
   */
  static differentiateFieldValidators(data, commonValidationParams) {
    for (let i = 0; i < data.validators.length; i++) {
      let validator = data.validators[i];
      let currentValidator = data.registeredValidators[validator.id];
      if (ValidationService.shouldExecuteValidator(validator, data.execution)) {
        let currentValidatorDefinition = data.registeredValidatorDefinitions[validator.id];
        if (currentValidatorDefinition.async) {
          commonValidationParams.asyncValidators.push(currentValidator.validate(data.fieldName, validator, data.validationModel, data.flatModel, data.formControl, data.definitionId, data.instanceId).then((validatorResult) => {
            ValidationService.afterControlValidation(validator, validatorResult, data, commonValidationParams);
          }).catch(error => console.error(error)));
        } else {
          commonValidationParams.syncValidators.push(() => {
            let validatorResult = currentValidator.validate(data.fieldName, validator, data.validationModel, data.flatModel, data.formControl, data.definitionId, data.instanceId);
            ValidationService.afterControlValidation(validator, validatorResult, data, commonValidationParams);
          });
        }
      }
    }
  }

  /**
   * Common logic to be executed after validator execution.
   *
   * @param validator validator that was executed
   * @param validatorResult after validation result.
   * @param data form field validation data.
   * @param commonValidationParams common validation-service params.
   */
  static afterControlValidation(validator, validatorResult, data, commonValidationParams) {
    if (validatorResult !== undefined) {
      data.isValidField = data.isValidField && validatorResult;
      data.validationModel[data.fieldName]
      && ValidationService.setMessages(validatorResult, data.validationModel[data.fieldName], validator, data.translateService);
      // check for mandatory fields too
      data.validationModel[data.fieldName] && ValidationService.setValidity(data.fieldName, data.validationModel, data.isValidField);
      commonValidationParams.validForm = commonValidationParams.validForm && data.isValidField;
    }
  }

  /**
   * Sets or removes the field error message depending on some circumstances when the validator is async.
   * If there are some error messages and the field is valid, just removes the current error.
   * If the field isn't valid adds the error and sets the field validity to false.
   * If the field is valid and there aren't any error messages sets the field validity to true.
   *
   * @param valid the boolean that holds if the value is valid
   * @param fieldName the field name
   * @param validationModel the validation model
   * @param validatorDef the definition of the validator
   * @param translateService the translateService
   */
  static processAsyncValidation(valid, fieldName, validationModel, validatorDef, translateService) {
    let fieldValidationModel = validationModel[fieldName];
    if (fieldValidationModel.messages.length && valid) {
      ValidationService.setMessages(valid, validationModel[fieldName], validatorDef, translateService);
    } else if (!valid) {
      ValidationService.setMessages(valid, validationModel[fieldName], validatorDef, translateService);
      ValidationService.setValidity(fieldName, validationModel, valid);
    } else if (!fieldValidationModel.messages.length && valid) {
      ValidationService.setValidity(fieldName, validationModel, valid);
    }
  }

  /**
   * If a field is mandatory, has no value and is not editable, then it is considered inaccessible and have to be logged
   * in order to allow easy detection of problems with definitions and conditions.
   *
   * @param viewModel
   * @param validationModel
   * @returns {boolean}
   */
  static isInaccessibleEmptyMandatoryField(viewModel, validationModel) {
    let isMandatory = viewModel.isMandatory;
    let displayType = viewModel.displayType;
    let preview = viewModel.preview;
    let rendered = viewModel.rendered;
    let value = validationModel[viewModel.identifier] && validationModel[viewModel.identifier].value;
    let notRendered = (rendered !== undefined && !rendered);
    let inaccessible = (displayType === ValidationService.DISPLAY_TYPE_HIDDEN
      || displayType === ValidationService.DISPLAY_TYPE_SYSTEM
      || displayType === ValidationService.DISPLAY_TYPE_READ_ONLY
      || preview || notRendered);
    return !value && (isMandatory !== undefined && isMandatory) && inaccessible;
  }

  static shouldExecuteValidator(validator, execution) {
    return !execution || !validator.execution || validator.execution === execution;
  }

  /**
   * Sets or removes some validation messages as defined in failed validators on the fields validation model.
   *
   * @param setMessage
   *          If the messages should be set.
   * @param fieldValidationModel
   *          The field's validation model.
   * @param validatorDefinition
   *          The validator's definition.
   * @param translateService
   */
  static setMessages(setMessage, fieldValidationModel, validatorDefinition, translateService) {
    // add message only if there isn't such
    let messageIndex = _.findIndex(fieldValidationModel.messages, (item) => {
      return item['id'] === validatorDefinition.id;
    });
    let messageContained = messageIndex !== -1;

    if (!setMessage && !messageContained) {
      fieldValidationModel.messages.push({
        id: validatorDefinition.id,
        message: translateService.translateInstant(validatorDefinition.message) || validatorDefinition.message,
        level: validatorDefinition.level
      });
    } else if (setMessage && messageContained) {
      // try delete only if there is such message in the model
      fieldValidationModel.messages.splice(messageIndex, 1);
    }
  }

  /**
   * Sets the validation status on given field.
   *
   * @param fieldName
   *          The field name.
   * @param validationModel
   *          The field's validationModel.
   * @param isValid
   *          If the validation was successful or not.
   */
  static setValidity(fieldName, validationModel, isValid) {
    validationModel[fieldName].valid = isValid;
  }

  publishAfterFormValidationEvent(validForm, id, validationModel, viewModel) {
    this.eventbus.publish(new AfterFormValidationEvent({
      isValid: validForm,
      id,
      validationModel,
      viewModel
    }));
  }
}

// TODO: move these in DefinitionModelProperty
ValidationService.DISPLAY_TYPE_HIDDEN = 'HIDDEN';
ValidationService.DISPLAY_TYPE_SYSTEM = 'SYSTEM';
ValidationService.DISPLAY_TYPE_READ_ONLY = 'READ_ONLY';
ValidationService.DISPLAY_TYPE_EDITABLE = 'EDITABLE';
