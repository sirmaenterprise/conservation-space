import {Injectable, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {UrlUtils} from 'common/url-utils';

@Injectable()
@Inject(PromiseAdapter, InstanceRestService, Eventbus, ExtensionsDialogService, WindowAdapter)
export class CreatePanelService {

  constructor(promiseAdapter, instanceRestService, eventbus, extensionsDialogService, windowAdapter) {
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.extensionsDialogService = extensionsDialogService;
    this.windowAdapter = windowAdapter;
  }

  getInstanceDialogConfig(opts) {
    return {
      suggestedPropertiesMap: new Map(),
      models: CreatePanelService.getModelsObject(opts),
      dialogConfig: this.getDialogConfig(opts)
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
      largeModal: true,
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

  getCreatePanelConfig(opts, config) {
    let instanceType = opts.instanceType;
    if (Array.isArray(opts.predefinedTypes) && opts.predefinedTypes.length > 0) {
      instanceType = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#' + opts.predefinedTypes[0].split(':')[1];
    }

    return {
      config: {
        parentId: opts.parentId || 'test_id',
        operation: opts.operation || 'create',
        renderMandatory: true,
        showAllProperties: false,
        suggestedProperties: config.suggestedPropertiesMap,
        classFilter: opts.predefinedTypes,
        definitionFilter: opts.predefinedSubTypes,
        formConfig: {
          models: config.models
        },
        onCreate: () => {
          let createdInstance = {
            id: '1',
            headers: {
              breadcrumb_header: 'header'
            }
          };
          this.eventbus.publish(new InstanceCreatedEvent({
            currentObject: createdInstance
          }));
          return this.promiseAdapter.resolve(createdInstance);
        },
        onOpen: (instance) => {
          this.openButtonHandler(instance, config.dialogConfig);
        },
        onOpenInNewTab: (instance) => {
          this.openInNewTabHandler(instance, config.dialogConfig);
        },
        onCancel: () => {
          config.dialogConfig.dismiss();
        },
        createButtonLabel: opts.createButtonLabel,
        forceCreate: opts.forceCreate,
        openInNewTab: opts.openInNewTab,
        controls: opts.controls,
        purpose: opts.purpose,
        // Used for overriding the create instance panel with existing data
        instanceData: opts.instanceData,
        instanceType,
        instanceSubType: opts.instanceSubType,
        showTemplateSelector: opts.showTemplateSelector,
        instanceCreatedCallback: opts.instanceCreatedCallback,
        eventEmitter: opts.eventEmitter,
        contextSelectorSelectionMode: opts.contextSelectorSelectionMode
      }
    };
  }

  openInNewTabHandler(instance, dialogConfig) {
    dialogConfig.dismiss();
    let url = this.windowAdapter.location.origin + UrlUtils.buildIdocUrl(instance.id, undefined, {mode: 'edit'});
    window.open(url, '_blank');
  }

  getUploadPanelConfig(opts) {
    return {
      config: {
        maxFileSize: 4048,
        contextSelectorDisabled: false,
        parentId: opts.parentId,
        controls: opts.controls,
        purpose: opts.purpose,
        classFilter: opts.predefinedTypes,
        definitionFilter: opts.predefinedSubTypes,
        eventEmitter: opts.eventEmitter,
        contextSelectorSelectionMode: opts.contextSelectorSelectionMode
      }
    };
  }

  getDialogExtensionsConfig(opts, createConfig, uploadConfig) {
    return {
      extensionPoint: 'create-instance',
      exclusions: opts.exclusions,
      extensions: {
        'instance-create-panel': createConfig.config,
        'file-upload-panel': uploadConfig.config
      }
    };
  }

  openCreateInstanceDialog(opts) {
    let config = this.getInstanceDialogConfig(opts);
    let uploadConfig = this.getUploadPanelConfig(opts, config);
    let createConfig = this.getCreatePanelConfig(opts, config);
    let extensionsConfig = this.getDialogExtensionsConfig(opts, createConfig, uploadConfig);
    return this.extensionsDialogService.openDialog(extensionsConfig, undefined, config.dialogConfig);
  }

  static resolveContextSelectorDisabled(config, defaultValue) {
    return config.contextSelectorDisabled === undefined || config.contextSelectorDisabled === null ? defaultValue : config.contextSelectorDisabled;
  }
}