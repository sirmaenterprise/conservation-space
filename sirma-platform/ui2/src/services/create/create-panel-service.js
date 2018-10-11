import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {FileUploadPanel} from 'create/file-upload-panel';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {Router} from 'adapters/router/router';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Configuration} from 'common/application-config';
import {InstanceObject} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {UrlUtils} from 'common/url-utils';
import {ModelUtils} from 'models/model-utils';
import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';

export const CREATE_INSTANCE_EXTENSION_POINT = 'create-instance';
export const INSTANCE_CREATE_PANEL = 'instance-create-panel';
export const FILE_UPLOAD_PANEL = 'file-upload-panel';

/**
 * Service component which is responsible for configuring the instance create panel with a proper basic bootstrap configuration
 * needed for the component to function properly. This service provides default behavior for create, cancel and open buttons of
 * the create instance panel component
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ExtensionsDialogService, Router, SessionStorageService, WindowAdapter, Configuration, InstanceRestService, Eventbus, DialogService, TranslateService)
export class CreatePanelService {

  constructor(extensionsDialogService, router, sessionStorageService, windowAdapter, configuration, instanceRestService, eventbus, dialogService, translateService) { //NOSONAR
    this.router = router;
    this.windowAdapter = windowAdapter;
    this.sessionStorageService = sessionStorageService;
    this.extensionsDialogService = extensionsDialogService;
    this.dialogService = dialogService;
    this.configuration = configuration;
    this.instanceRestService = instanceRestService;
    this.eventbus = eventbus;
    this.instanceCreated = false;
    this.translateService = translateService;
  }

  /**
   * Constructs a dialog configuration based on the given options
   *
   * @param opts options from which to construct the dialog configuration
   * @returns base dialog configuration
   */
  getInstanceDialogConfig(opts) {
    return {
      suggestedPropertiesMap: new Map(),
      models: CreatePanelService.getModelsObject(opts),
      dialogConfig: this.getDialogConfig(opts)
    };
  }

  /**
   * Constructs a dialog extensions for instance-create-panel & file-upload panel
   * configuration based on the given options, createConfig & uploadConfig
   *
   * @param opts the dialog options
   * @param createConfig the create panel config
   * @param uploadConfig the upload panel config
   * @returns dialog extensions configuration
   */
  getDialogExtensionsConfig(opts, createConfig, uploadConfig) {
    return {
      extensionPoint: CREATE_INSTANCE_EXTENSION_POINT,
      exclusions: opts.exclusions,
      defaultTab: opts.defaultTab,
      extensions: {
        [INSTANCE_CREATE_PANEL]: createConfig.config,
        [FILE_UPLOAD_PANEL]: uploadConfig.config
      }
    };
  }

  /**
   * Opens the create instance panel dialog with a given configuration
   * @param opts the configuration
   */
  openCreateInstanceDialog(opts) {
    let config = this.getInstanceDialogConfig(opts);
    let uploadConfig = this.getUploadPanelConfig(opts, config);
    let createConfig = this.getCreatePanelConfig(opts, config);
    let extensionsConfig = this.getDialogExtensionsConfig(opts, createConfig, uploadConfig);

    return this.extensionsDialogService.openDialog(extensionsConfig, undefined, config.dialogConfig);
  }

  openUploadInstanceDialog(opts) {
    let config = this.getInstanceDialogConfig(opts);
    let uploadConfig = this.getUploadPanelConfig(opts, config);
    this.dialogService.create(FileUploadPanel, uploadConfig, config.dialogConfig);
  }

  /**
   * Builds the final upload panel configuration from a given number of smaller configurations
   */
  getUploadPanelConfig(opts, config) {
    return {
      config: {
        maxFileSize: this.configuration.get(Configuration.UPLOAD_MAX_FILE_SIZE),
        contextSelectorDisabled: CreatePanelService.resolveContextSelectorDisabled(opts, false),
        files: opts.files,
        parentId: opts.parentId,
        controls: opts.controls,
        purpose: opts.purpose || [ModelsService.PURPOSE_UPLOAD],
        classFilter: opts.predefinedTypes,
        definitionFilter: opts.predefinedSubTypes,
        fileObject: opts.fileObject,
        onCancel: () => this.cancelButtonHandler(config.models, config.dialogConfig),
        eventEmitter: opts.eventEmitter,
        contextSelectorSelectionMode: opts.contextSelectorSelectionMode
      }
    };
  }

  /**
   * Builds the final create panel configuration from a given number of smaller configurations
   */
  getCreatePanelConfig(opts, config) {
    var onCreate = () => {
      if (opts.onCreate) {
        this.instanceCreated = true;
        return opts.onCreate(config.models, config.dialogConfig, config.suggestedPropertiesMap);
      }
      this.instanceCreated = true;
      return this.createButtonHandler(config.models, config.dialogConfig, config.suggestedPropertiesMap);
    };

    return {
      config: {
        parentId: opts.parentId,
        operation: opts.operation,
        renderMandatory: true,
        showAllProperties: false,
        suggestedProperties: config.suggestedPropertiesMap,
        classFilter: opts.predefinedTypes,
        definitionFilter: opts.predefinedSubTypes,
        // This property should contain an empty object which will be populated with the model later inside the dialog.
        // This is a live model to which the form fields are bound to. It is passed as a reference and the same instance
        // should be used.
        formConfig: {
          models: config.models
        },
        onCreate,
        onOpen: (instance) => {
          this.openButtonHandler(instance, config.dialogConfig);
        },
        onOpenInNewTab: (instance) => {
          this.openInNewTabHandler(instance, config.dialogConfig);
        },
        onCancel: () => {
          this.cancelButtonHandler(config.models, config.dialogConfig, opts.onClosed);
        },
        createButtonLabel: opts.createButtonLabel,
        forceCreate: opts.forceCreate,
        disableCreate: opts.disableCreate,
        existingInContext: opts.existingInContext,
        openInNewTab: opts.openInNewTab,
        controls: opts.controls,
        purpose: opts.purpose || [ModelsService.PURPOSE_CREATE],
        // Used for overriding the create instance panel with existing data
        instanceData: opts.instanceData,
        instanceType: opts.instanceType,
        instanceSubType: opts.instanceSubType,
        showTemplateSelector: opts.showTemplateSelector,
        instanceCreatedCallback: opts.instanceCreatedCallback,
        eventEmitter: opts.eventEmitter,
        contextSelectorSelectionMode: opts.contextSelectorSelectionMode
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
      contextSelectorDisabled: CreatePanelService.resolveContextSelectorDisabled(opts, false),
      eventEmitter: opts.eventEmitter,
      contextSelectorSelectionMode: opts.contextSelectorSelectionMode
    };
  }

  getDialogConfig(opts = {}) {
    return {
      header: opts.header || 'instance.create.dialog.header',
      helpTarget: opts.helpTarget || 'dialog.create',
      backdrop: 'static',
      onClose: () => {
        if (typeof opts.onClosed === 'function') {
          opts.onClosed({instanceCreated: this.instanceCreated});
        }
        this.instanceCreated = false;
      },
      // Override default buttons
      buttons: null
    };
  }

  /**
   * Creates the instance based on the given model and reverts the model changes
   * for future use in case the create another option is checked.
   *
   * @param models the model which will be used to create a new instance.
   *    Underlying viewModel and validation model must be wrapped in its respected wrapper classes.
   * @param dialogConfig
   * @param suggestedPropertiesMap
   *
   * @returns the created instance returned after the instance create.
   */
  createButtonHandler(models, dialogConfig, suggestedPropertiesMap) {
    let data = {
      definitionId: models.definitionId,
      parentId: models.parentId,
      properties: InstanceObject.getModelChangeSet(false, models.validationModel, models.viewModel)
    };
    this.eventbus.publish(new BeforeIdocSaveEvent(data));

    return this.instanceRestService.create(data).then((response) => {
      InstanceObject.revertModelChanges(models.validationModel);
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
      // TODO: This probably should be preserved for old instances?!
      if (models.validationModel[propertyName].value instanceof Array) {
        models.validationModel[propertyName].value = models.validationModel[propertyName].value.concat(suggestedValues);
      } else {
        // The suggest service returns instances instead of id's.
        let ids = suggestedValues.map((currentValue) => {
          return currentValue.id;
        });
        ModelUtils.updateObjectProperty(models.validationModel, propertyName, ids);
      }
    });
  }

  openInNewTabHandler(instance, dialogConfig) {
    dialogConfig.dismiss();
    let url = this.windowAdapter.location.origin + UrlUtils.buildIdocUrl(instance.id, undefined, {mode: 'edit'});
    let newWin = window.open(url, '_blank');
    if (!newWin) {
      this.dialogService.notification(this.translateService.translateInstant('notification.popup.disabled') + '<br/>' + instance.headers['compact_header']);
    }
  }

  /**
   * Provides a default implementation for the open button of the create instance panel
   */
  openButtonHandler(instance, dialogConfig) {
    dialogConfig.dismiss();
    this.router.navigate('idoc', {id: instance.id, mode: 'edit'}, {reload: true, inherit: false});
  }

  /**
   * Provides a default implementation for the cancel button of the create instance panel
   */
  cancelButtonHandler(models, dialogConfig, onClosed) {
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
