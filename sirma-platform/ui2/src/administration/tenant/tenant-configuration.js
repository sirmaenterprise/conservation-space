import {View, Component, Inject, NgScope} from 'app/app';
import {ConfigurationRestService} from 'services/rest/configurations-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {ConfigurationsUpdateEvent} from 'common/configuration-events';
import {StatusCodes} from 'services/rest/status-codes';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {CommandChain} from 'common/command-chain/command-chain';
import {BooleanConverter, NumberConverter, StringConverter} from 'common/convert/type-converter';
import {DefinitionModel} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {FormControl} from 'form-builder/form-control';

import 'components/filter/input-filter';
import 'form-builder/form-wrapper';

import template from './tenant-configuration.html!text';

/**
 * Administration component which provides a form for loading and changing configuration values for the tenant in which
 * the administrator is currently logged in. It loads the configurations and converts them to a model that is provided
 * to an instance of the form builder component.
 *
 * It allows editing of all configuration properties including the sensitive ones. The group configuration properties
 * are skipped due to the fact that they do not have a database entry.
 *
 * The component uses either the raw value (the one in the database entry) or the default one (that in the configuration
 * definition annotation) of the configurations. It performs conversion to the according value type (string, number or
 * boolean).
 *
 * When the form is saved it sends only the modified values to reduce the amount of sent data to the server and to avoid
 * reloading unchanged configurations.
 *
 * On successful configuration update, the component fires an event notifying that the configurations are updated.
 *
 * @author Svetlozar Iliev
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-tenant-configuration'
})
@View({template})
@Inject(NgScope, Eventbus, ConfigurationRestService, DialogService, NotificationService, TranslateService)
export class TenantConfiguration {

  constructor($scope, eventbus, configService, dialogService, notificationService, translateService) {
    this.$scope = $scope;
    this.eventbus = eventbus;
    this.configService = configService;
    this.dialogService = dialogService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.converterChain = new CommandChain([new BooleanConverter(), new NumberConverter(), new StringConverter()]);

    this.initialize();
    this.prepareLabels();
    this.loadTenantConfig();
    this.initFilterConfig();
  }

  initialize() {
    this.formReady = false;
    this.filterKeyWord = '';
    // formViewMode is needed to be set because it triggers a validation cycle
    // before the form-wrapped is done initializing the validation service
    this.wrapperConfig = {
      formViewMode: FormControl.VIEW_MODES.EDIT
    };
    this.formConfig = {};
    this.tenantConfig = [];
  }

  prepareLabels() {
    this.labels = {
      confirmation: {
        message: this.translateService.translateInstant('administration.tenant.confirmation.message'),
        title: this.translateService.translateInstant('administration.tenant.confirmation.title'),
        default: this.translateService.translateInstant('administration.tenant.notification.defaults')
      },
      notification: {
        success: this.translateService.translateInstant('administration.tenant.notification.updated.success'),
        fail: this.translateService.translateInstant('administration.tenant.notification.updated.fail')
      }
    };
  }

  loadTenantConfig() {
    this.configService.loadConfigurations().then((response) => {
      this.tenantConfig = this.transformConfigurations(response.data);
      this.initializeFormConfig();
      this.formReady = true;
    });
  }

  initFilterConfig() {
    this.filterConfig = {
      inputPlaceholder: 'administration.tenant.filter.placeholder'
    };
  }

  /**
   * Transforms the configurations values by extracting & converting from either the default or raw value of the
   * configuration. Additionally it removes any group configuration properties.
   *
   * @param configurations - the configurations for transforming
   * @returns array of the transformed and filtered configurations
   */
  transformConfigurations(configurations) {
    var filtered = configurations.filter((configuration) => {
      // Remove all group configuration properties because they do not have a database entry and cannot be directly configured.
      return !configuration.dependsOn;
    });
    filtered.forEach((configuration) => {
      configuration.value = this.getConfigurationValue(configuration);
    });
    return filtered;
  }

  /**
   * Extracts & converts the configuration value for rendering in the configuration form.
   *
   * If the configuration has a raw value (the value that is stored in the database) it will use that one. If the
   * configuration lacks a raw value it will assign the default one.
   *
   * Finally the value is converted to its according type - string, number or boolean.
   *
   * @param configuration - the provided configuration for extraction
   * @returns the extracted and converted value
   */
  getConfigurationValue(configuration) {
    let value = (configuration.rawValue === undefined) ? configuration.defaultValue : configuration.rawValue;
    if (!value) {
      // There are cases where the configuration has no default or raw value but the configuration loading will assign one.
      value = configuration.value;
    }

    if (value) {
      // Convert the value to appropriate type (raw & default are returned as string because they are not converted)
      // This is needed so the form builder can treat correctly the value and render it as simply string
      value = this.converterChain.execute(value);
    }

    return value;
  }

  saveModifiedConfigurations() {
    var configForUpdating = this.extractConfigurationsForUpdate();
    if (configForUpdating.length > 0) {
      this.showSaveConfirmation(configForUpdating);
    }
  }

  showSaveConfirmation(configForUpdating) {
    let dialogConfig = {
      buttons: [
        {id: DialogService.YES, label: 'dialog.button.yes', cls: 'btn-primary'},
        {id: DialogService.CANCEL, label: 'dialog.button.cancel'}
      ],
      onButtonClick: (button, dialogScope, dialogConfig) => {
        if (button === DialogService.YES) {
          this.updateConfigurations(configForUpdating);
        }
        dialogConfig.dismiss();
      }
    };
    var message = this.buildConfirmationMessage(configForUpdating);
    this.dialogService.confirmation(message, this.labels.confirmation.title, dialogConfig);
  }

  /**
   * Constructs a message for notifying the user which are the changes configurations.
   */
  buildConfirmationMessage(configForUpdating) {
    var modifiedKeys = configForUpdating.map((config) => {
      return config.key;
    }).join('<br>');
    return `${this.labels.confirmation.message} <br>${modifiedKeys}`;
  }

  /**
   * Updates the provided configurations with the modified values are passes them to the backend.
   *
   * On successful request it notifies the user of the response and triggers an event to notify any components that
   * the configurations are updated.
   *
   * @param configForUpdating - the configuration array for updating
   */
  updateConfigurations(configForUpdating) {
    this.updateConfigurationsWithModel(configForUpdating);

    this.configService.updateConfigurations(configForUpdating).then((response) => {
      this.eventbus.publish(new ConfigurationsUpdateEvent(response.data));
      this.displayResultNotification(response.status);
    });
  }

  /**
   * Filters the configuration fields and regions based on a key word entered by the user.
   * The fields and regions are only hidden from the view and are not disabled or removed from the view model
   */
  filterConfigurations() {
    let model = this.formConfig.models.viewModel.fields;
    model.forEach((region) => {
      let fields = region.fields;
      //filter the fields and change visibility at the same time
      let filtered = fields.filter((field) => {
        field.displayType = (field.identifier.indexOf(this.filterKeyWord) !== -1) ? ValidationService.DISPLAY_TYPE_EDITABLE : ValidationService.DISPLAY_TYPE_HIDDEN;
        return field.displayType === ValidationService.DISPLAY_TYPE_HIDDEN;
      });
      //Region should be hidden if all of his fields are hidden as well
      region.displayType = filtered.length === fields.length ? ValidationService.DISPLAY_TYPE_HIDDEN : ValidationService.DISPLAY_TYPE_EDITABLE;
    });
    //Reload the form builder view model
    this.wrapperConfig.shouldReload = true;
  }

  /**
   * Extracts the configurations that are modified in the form builder but with the original value.
   * To reflect the changes provide the returned array to {@link #updateConfigurationsWithModel(...)}.
   *
   * This does not return modified configurations because the user could cancel the operation thus not being
   * able to revert any changes.
   *
   * @returns {Array} of those configurations that are modified in the form builder
   */
  extractConfigurationsForUpdate() {
    let modifiedConfig = [];
    let model = this.formConfig.models.validationModel;

    this.tenantConfig.forEach((configuration) => {
      let currentModel = model[configuration.key];
      //convert the current model value to appropriate type so it can be easily compared with given configuration value
      let value = currentModel.value ? this.converterChain.execute(currentModel.value) : currentModel.value;
      if (currentModel && configuration.value !== value) {
        modifiedConfig.push(configuration);
      }
    });
    return modifiedConfig;
  }

  /**
   * Updates the provided configurations with their modified values in the form builder.
   *
   * @param configForUpdating - the configuration array to be updated
   */
  updateConfigurationsWithModel(configForUpdating) {
    let model = this.formConfig.models.validationModel;
    configForUpdating.forEach((configuration) => {
      let currentModel = model[configuration.key];
      //convert the configuration value back from the validation model using the chain
      configuration.value = currentModel.value ? this.converterChain.execute(currentModel.value) : currentModel.value;
    });
  }

  /**
   * Resets the changes in the form by rebuilding it. Original values are not modified by the form builder making
   * it easy to revert the changes.
   */
  cancelConfigurationChanges() {
    //Clear the keyword on cancel
    this.filterKeyWord = '';
    this.initializeFormConfig();
    this.wrapperConfig.shouldReload = true;
    this.notificationService.success({
      title: this.labels.confirmation.title,
      message: this.labels.confirmation.default
    });
  }

  initializeFormConfig() {
    this.setupFormConfig();
    this.setupViewAndModel();
  }

  setupFormConfig() {
    this.formConfig.models = {};
    this.wrapperConfig.collapsibleRegions = true;
  }

  setupViewAndModel() {
    let regionMapping = {};
    let validationModel = {};
    let viewModel = {
      fields: []
    };

    this.tenantConfig.forEach((configuration) => {
      let key = this.extractBaseKey(configuration);

      if (!regionMapping[key]) {
        let region = this.createRegion(key);
        viewModel.fields.push(region);
        regionMapping[key] = region;
      }

      regionMapping[key].fields.push(this.createField(configuration));
      validationModel[configuration.key] = this.createValidation(configuration);
    });
    this.sortRegionFieldsAlphabetically(regionMapping);

    //view model is an instance of Definition Model throughout the whole application.
    //validation model is an instance of Instance model throught the whole application
    this.formConfig.models.viewModel = new DefinitionModel(viewModel);
    this.formConfig.models.validationModel = new InstanceModel(validationModel);
    this.sortAlphabeticallyByIdentifier(this.formConfig.models.viewModel.fields);
  }

  /**
   * Constructs a region object used in the form builder to group configurations of the same kind.
   */
  createRegion(regionKey) {
    return {
      identifier: regionKey,
      isMandatory: true,
      collapsed: true,
      label: this.capitalizeFirstLetter(regionKey),
      displayType: ValidationService.DISPLAY_TYPE_EDITABLE,
      fields: []
    };
  }

  createField(configuration) {
    let constraint = this.extractConstraints(configuration);

    // TODO: CMF-21302 - The backend must provide a flag if given configuration should be mandatory or not!
    let isMandatory = configuration.value !== undefined;

    let newField = {
      previewEmpty: true,
      identifier: configuration.key,
      disabled: false,
      displayType: ValidationService.DISPLAY_TYPE_EDITABLE,
      tooltip: configuration.label,
      dataType: constraint.type,
      label: configuration.key,
      isMandatory,
      validators: [{
        id: 'regex',
        context: {
          pattern: constraint.regexp
        },
        message: 'administration.tenant.message.format',
        level: 'error'
      }]
    };

    if (isMandatory) {
      newField.validators.push({
        id: 'mandatory',
        message: 'validation.field.mandatory',
        level: 'error'
      });
    }
    return newField;
  }

  createValidation(configuration) {
    return {
      defaultValue: configuration.value,
      value: configuration.value,
      messages: []
    };
  }

  extractConstraints(configuration) {
    if (configuration.password) {
      return {
        type: 'password',
        regexp: '.+'
      };
    }

    let extracted = configuration.javaType.replace(/(\.?\w+\.)+/, '');

    switch (extracted) {
    case 'Long':
    case 'Integer':
      return {
        type: 'text',
        regexp: '-?[0-9]+'
      };
    case 'Float':
      return {
        type: 'text',
        regexp: '-?[0-9]+\.?[0-9]+'
      };
    case 'Boolean':
      return {
        type: 'boolean',
        regexp: 'true|false'
      };
    case 'Date':
      return {
        type: 'date',
        regexp: '.+'
      };
    case 'DateTime':
      return {
        type: 'datetime',
        regexp: '.+'
      };
    case 'URI':
      return {
        type: 'text',
        regexp: '(https?|ftp|file|jdbc:[a-zA-Z0-9.]+)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]'
      };
    default:
      return {
        type: 'text',
        regexp: '.+'
      };
    }
  }

  displayResultNotification(status) {
    let config = {title: this.labels.confirmation.title};
    if (status === StatusCodes.SUCCESS) {
      config.message = this.labels.notification.success;
      this.notificationService.success(config);
    } else {
      config.message = this.labels.notification.fail;
      this.notificationService.error(config);
    }
  }

  /**
   * Extracts the base key of the configuration (the part before first dot).
   *
   * Example: for "audit.enabled" it will return "audit"
   *
   * @param configuration - the configuration which key will be extracted
   * @returns the extracted base key as string
   */
  extractBaseKey(configuration) {
    return configuration.key.split('.')[0];
  }

  capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  isFormValid() {
    if (this.formConfig.models && this.formConfig.models.validationModel) {
      return this.formConfig.models.validationModel.isValid;
    }
    return false;
  }

  sortAlphabeticallyByIdentifier(collection) {
    collection.sort((lhs, rhs) => {
      return (lhs.identifier < rhs.identifier) ? -1 : (lhs.identifier > rhs.identifier) ? 1 : 0;
    });
  }

  sortRegionFieldsAlphabetically(regionMapping) {
    for (let region in regionMapping) {
      if (regionMapping[region].hasOwnProperty('fields')) {
        this.sortAlphabeticallyByIdentifier(regionMapping[region].fields);
      }
    }
  }
}
