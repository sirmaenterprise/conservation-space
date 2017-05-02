import {Injectable, Inject} from 'app/app';
import _ from 'lodash';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {InstanceModel} from 'models/instance-model';

@Injectable()
@Inject(PluginsService, NotificationService, TranslateService, Eventbus, Logger, Configuration)
export class ValidationService {

  constructor(pluginsService, notificationService, translateService, eventbus, logger, configuration) {
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.pluginsService = pluginsService;
    this.eventbus = eventbus;
    this.logger = logger;
    this.debugMode = configuration.get(Configuration.RNC_DEBUG_ENABLED);
  }

  init() {
    if (!this.registeredValidators) {
      return this.pluginsService.loadPluginServiceModules('field-validators', 'name').then((modules) => {
        this.registeredValidators = modules;
      });
    }
    return Promise.resolve(this.registeredValidators);
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
   * @returns {boolean} If the for is valid or not.
   */
  validate(validationModel, viewModel, id, execution, formControl) {
    let flatModel = viewModel;
    let inaccessibleMandatoryFields = [];
    let validForm = true;
    let fieldsNames = Object.keys(flatModel);
    for (let i = 0, length = fieldsNames.length; i < length; i++) {
      let fieldName = fieldsNames[i];
      let fieldViewModel = flatModel[fieldName];
      let validators = fieldViewModel.validators;
      if (validators) {
        let data = {
          validators: validators,
          registeredValidators: this.registeredValidators,
          execution: execution,
          fieldName: fieldName,
          validationModel: validationModel,
          flatModel: flatModel,
          formControl: formControl,
          translateService: this.translateService,
          isValidField: true
        };

        ValidationService.applyValidators(data);

        // if a field can not be edited in a given state, it must not be mandatory.
        if (this.debugMode && ValidationService.isInaccessibleEmptyMandatoryField(fieldViewModel, validationModel)) {
          inaccessibleMandatoryFields.push(fieldViewModel.identifier);
        }

        // check for mandatory fields too
        validationModel[fieldName] && ValidationService.setValidity(fieldName, validationModel, data.isValidField);
        validForm = validForm && data.isValidField;
      }
    }
    if (inaccessibleMandatoryFields.length > 0) {
      this.logger.error('This object might have uncompleted mandatory fields that are not currently accessible. This is caused by wrong defintion file: ' + JSON.stringify(inaccessibleMandatoryFields));
    }

    validationModel.isValid = validForm;

    this.eventbus.publish(new AfterFormValidationEvent({
      isValid: validForm,
      id: id,
      validationModel: validationModel,
      viewModel: viewModel
    }));
    return validForm;
  }

  static applyValidators(data) {
    for (let i = 0; i < data.validators.length; i++) {
      let validator = data.validators[i];
      let currentValidator = data.registeredValidators[validator.id];
      if (ValidationService.shouldExecuteValidator(validator, data.execution)) {
        let validatorResult = currentValidator.validate(data.fieldName, validator, data.validationModel, data.flatModel, data.formControl);
        // Usually the condition validator don't return explicit value. So the field
        // validation status is altered after a validator returns explicit value.
        if (validatorResult !== undefined) {
          data.isValidField = data.isValidField && validatorResult;
          data.validationModel[data.fieldName]
          && ValidationService.setMessages(validatorResult, data.validationModel[data.fieldName], validator, data.translateService);
        }
      }
    }
  }

  static shouldExecuteValidator(validator, execution) {
    return !execution || !validator.execution || validator.execution === execution;
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
    var isMandatory = viewModel.isMandatory;
    var displayType = viewModel.displayType;
    var preview = viewModel.preview;
    var rendered = viewModel.rendered;
    var value = validationModel[viewModel.identifier] && validationModel[viewModel.identifier].value;
    var notRendered = (rendered !== undefined && !rendered);
    var inaccessible = (displayType === ValidationService.DISPLAY_TYPE_HIDDEN
    || displayType === ValidationService.DISPLAY_TYPE_SYSTEM
    || displayType === ValidationService.DISPLAY_TYPE_READ_ONLY
    || preview || notRendered);
    return !value && (isMandatory !== undefined && isMandatory) && inaccessible;
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
    let messageIndex = _.findIndex(fieldValidationModel.messages, (item)=> {
      return item['id'] === validatorDefinition.id;
    });
    let messageContained = messageIndex === -1 ? false : true;

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
}

// TODO: move these in DefinitionModelProperty
ValidationService.DISPLAY_TYPE_HIDDEN = 'HIDDEN';
ValidationService.DISPLAY_TYPE_SYSTEM = 'SYSTEM';
ValidationService.DISPLAY_TYPE_READ_ONLY = 'READ_ONLY';
ValidationService.DISPLAY_TYPE_EDITABLE = 'EDITABLE';
