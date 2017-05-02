import {Injectable, Inject} from 'app/app';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {Router} from 'adapters/router/router';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Configuration} from 'common/application-config';
import {InstanceObject} from 'idoc/idoc-context';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';

export const CREATE_INSTANCE_EXTENSION_POINT = 'create-instance';

/**
 * Service component which is responsible for configuring the instance create panel with a proper basic bootstrap configuration
 * needed for the component to function properly. This service provides default behavior for create, cancel and open buttons of
 * the create instance panel component
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ExtensionsDialogService, Router, SessionStorageService, WindowAdapter, Configuration, InstanceRestService, Eventbus)
export class CreatePanelService {

  constructor(extensionsDialogService, router, sessionStorageService, windowAdapter, configuration, instanceRestService, eventbus) {
    this.router = router;
    this.windowAdapter = windowAdapter;
    this.sessionStorageService = sessionStorageService;
    this.extensionsDialogService = extensionsDialogService;
    this.configuration = configuration;
    this.instanceRestService = instanceRestService;
    this.eventbus = eventbus;
  }

  /**
   * Constructs a dialog configuration based on the given options
   * @param opts options from which to construct the dialog configuration
   * @returns base dialog configuration
   */
  getInstanceDialogConfig(opts) {
    return {
      suggestedPropertiesMap: new Map(),
      models: CreatePanelService.getModelsObject(opts),
      dialogConfig: CreatePanelService.getDialogConfig(opts)
    };
  }

  /**
   * Constructs a dialog extensions configuration based on the given options, properties, dialog configuration
   * @param opts the dialog options
   * @param properties the dialog properties
   * @param config the dialog configuration
   * @returns dialog extensions configuration
   */
  getDialogExtensionsConfig(opts, properties, config) {
    return {
      extensionPoint: CREATE_INSTANCE_EXTENSION_POINT,
      exclusions: opts.exclusions,
      extensions: {
        'instance-create-panel': properties.config,
        'file-upload-panel': {
          maxFileSize: this.configuration.get(Configuration.UPLOAD_MAX_FILE_SIZE),
          contextSelectorDisabled: CreatePanelService.resolveContextSelectorDisabled(opts, false),
          parentId: opts.parentId,
          classFilter: opts.predefinedTypes,
          definitionFilter: opts.predefinedSubTypes,
          onCancel: () => this.cancelButtonHandler(config.models, config.dialogConfig)
        }
      }
    };
  }

  /**
   * Opens the create instance panel dialog with a given configuration
   * @param opts the configuration
   */
  openCreateInstanceDialog(opts) {
    let config = this.getInstanceDialogConfig(opts);
    let properties = this.getPropertiesConfig(opts, config.models, config.dialogConfig, config.suggestedPropertiesMap);
    let extensionsConfig = this.getDialogExtensionsConfig(opts, properties, config);

    return this.extensionsDialogService.openDialog(extensionsConfig, undefined, config.dialogConfig);
  }

  /**
   * Builds the final dialog configuration from a given number of smaller configurations
   */
  getPropertiesConfig(opts, models, dialogConfig, suggestedPropertiesMap) {
    var onCreate = () => {
      if (opts.onCreate) {
        return opts.onCreate(models, dialogConfig, suggestedPropertiesMap);
      }
      return this.createButtonHandler(models, dialogConfig, suggestedPropertiesMap);
    };

    return {
      config: {
        parentId: opts.parentId,
        operation: opts.operation,
        renderMandatory: true,
        showAllProperties: false,
        suggestedProperties: suggestedPropertiesMap,
        classFilter: opts.predefinedTypes,
        definitionFilter: opts.predefinedSubTypes,
        // This property should contain an empty object which will be populated with the model later inside the dialog.
        // This is a live model to which the form fields are bound to. It is passed as a reference and the same instance
        // should be used.
        formConfig: {
          models: models
        },
        onCreate: onCreate,
        onOpen: () => {
          this.openButtonHandler(models, dialogConfig);
        },
        onCancel: () => {
          this.cancelButtonHandler(models, dialogConfig);
        },
        createButtonLabel: opts.createButtonLabel,
        forceCreate: opts.forceCreate,
        controls: opts.controls,
        purpose: opts.purpose,
        // Used for overriding the create instance panel with existing data
        instanceData: opts.instanceData,
        instanceType: opts.instanceType,
        instanceSubType: opts.instanceSubType
      }
    };
  }

  static getModelsObject(opts) {
    return {
      definitionId: null,
      viewModel: null,
      validationModel: null,
      returnUrl: opts.returnUrl,
      parentId: opts.parentId,
      contextSelectorDisabled: CreatePanelService.resolveContextSelectorDisabled(opts, false)
    };
  }

  static getDialogConfig(opts) {
    opts = opts || {};
    return {
      header: opts.header || 'instance.create.dialog.header',
      helpTarget: opts.helpTarget || 'dialog.create',
      backdrop: 'static',
      largeModal: true
    };
  }

  /**
   * Provides a default implementation for the create button of the create instance panel
   */
  createButtonHandler(models, dialogConfig, suggestedPropertiesMap) {
    let instanceObject = new InstanceObject(models.definitionId, models);
    let data = {
      definitionId: instanceObject.getModels().definitionId,
      parentId: instanceObject.getModels().parentId,
      properties: instanceObject.getChangeset(false)
    };

    return this.instanceRestService.create(data).then((response) => {
      instanceObject.revertChanges();
      this.assignSuggestedPropertiesValues(models, suggestedPropertiesMap);

      let instance = response.data;
      this.eventbus.publish(new InstanceCreatedEvent({
        currentObject: instance
      }));
      return instance;
    });
  }

  assignSuggestedPropertiesValues(models, suggestedPropertiesMap) {
    suggestedPropertiesMap.forEach((suggestedValues, propertyName) => {
      if (models.validationModel[propertyName].value instanceof Array) {
        models.validationModel[propertyName].value = models.validationModel[propertyName].value.concat(suggestedValues);
      } else {
        models.validationModel[propertyName].value = suggestedValues;
      }
    });
  }

  /**
   * Provides a default implementation for the open button of the create instance panel
   */
  openButtonHandler(models, dialogConfig) {
    //validation model and view model have to be serialized to its old JSON form in order to be saved.
    if (models) {
      if ((models.validationModel && models.validationModel instanceof InstanceModel)) {
        models.validationModel = models.validationModel.serialize();
      }
      if (models.viewModel && models.viewModel instanceof DefinitionModel) {
        models.viewModel = models.viewModel.serialize();
      }
    }
    // - get values model
    // - store the model inside the session store or serialize it in order to pass it as request parameter
    // - call the router to navigate to idoc page
    this.sessionStorageService.set('models', models);
    dialogConfig.dismiss();
    this.router.navigate('idoc', {mode: 'edit'}, {reload: true, inherit: false});
  }

  /**
   * Provides a default implementation for the cancel button of the create instance panel
   */
  cancelButtonHandler(models, dialogConfig) {
    // While in UI2 - EMF UI integration phase, the cancel should redirect back to the EMF UI
    if (models.returnUrl) {
      this.windowAdapter.location.href = models.returnUrl;
    }
    dialogConfig.dismiss();
  }

  /**
   * If config contains contextSelectorDisabled return it otherwise defaultValue.
   * @param config
   * @param defaultValue the default value
   * @returns
   */
  static resolveContextSelectorDisabled(config, defaultValue) {
    return config.contextSelectorDisabled === undefined || config.contextSelectorDisabled === null ? defaultValue : config.contextSelectorDisabled;
  }
}
