import {Injectable, Inject} from 'app/app';
import {FormWrapper} from 'form-builder/form-wrapper';
import {DialogService} from 'components/dialog/dialog-service';
import {SaveIdocDialog} from 'idoc/save-idoc-dialog/save-idoc-dialog';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {EventEmitter} from 'common/event-emitter';
import {InstanceObject} from 'models/instance-object';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {ADD_CONTEXT_ERROR_MESSAGE_COMMAND} from 'components/contextselector/context-selector';
import {CONTEXT_CHANGED_EVENT} from 'components/contextselector/context-selector';

@Injectable()
@Inject(DialogService, PromiseAdapter, InstanceContextService)
export class SaveDialogService {

  constructor(dialogService, promiseAdapter, instanceContextService) {
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.instanceContextService = instanceContextService;
  }

  /**
   * Opens dialog with form builder to render details of all the objects for which there are models passed.
   * @param config Contains: models, context, onFormValidated
   * @returns {Promise}
   */
  openDialog(config) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let okButton = {
        id: 'SAVE',
        label: 'idoc.savedialog.button.save',
        cls: 'btn-primary',
        disabled: true,
        onButtonClick: (buttonId, componentScope, dialogConfig) => {
          resolve();
          dialogConfig.dismiss();
        }
      };

      let cancelButton = {
        id: DialogService.CANCEL,
        label: 'idoc.savedialog.button.cancel',
        dismiss: true
      };
      let dialogConfig = {
        header: 'idoc.savedialog.header',
        showClose: true,
        backdrop: 'static',
        modalCls: 'save-idoc-dialog',
        largeModal: true,
        onClose: () => {
          reject();
        },
        buttons: []
      };
      dialogConfig.buttons.push(okButton);
      dialogConfig.buttons.push(cancelButton);
      let properties = {
        config: {
          invalidObjects: config.models
        }
      };

      // Depending on the usage, each dialog might have its own form configuration.
      if (config.formConfig) {
        Object.keys(config.formConfig).forEach(
          formConfigProperty => properties.config[formConfigProperty] = config.formConfig[formConfigProperty]
        );
      } else {
        Object.keys(DialogService.DEFAULT_FORM_WRAPPER_CONFIG).forEach(
          formConfigProperty => properties.config[formConfigProperty] = DialogService.DEFAULT_FORM_WRAPPER_CONFIG[formConfigProperty]
        );
      }

      let validationRegistry = new ValidationRegistry(this.promiseAdapter, this.instanceContextService, okButton);
      validationRegistry.init(config.models).then(() => {
        properties.config.onFormValidated = validationRegistry.onFormValidated();
        this.dialogService.create(SaveIdocDialog, properties, dialogConfig);
      });
    });
  }
}

/**
 * Helper class which hold and process all validation.
 *
 */
class ValidationRegistry {
  constructor(promiseAdapter, instanceContextService, okButton) {
    this.promiseAdapter = promiseAdapter;
    this.instanceContextService = instanceContextService;
    this.okButton = okButton;
    this.instancesWithInvalidContext = new Set();
    this.instancesWithInvalidValidation = [];
  }

  init(invalidInstanceObjectsModels) {
    // Registers all instance objects which field validations are failed.
    Object.values(invalidInstanceObjectsModels).forEach((invalidInstanceObjectModel) => {
      this.instancesWithInvalidValidation[invalidInstanceObjectModel.models.id] = false;
    });

    // Registers all instance objects which have incorrect existence in context.
    return this._registerInstanceObjectsWithInvalidContext(invalidInstanceObjectsModels);
  }

  onFormValidated() {
    return (data) => {
      this.updateInvalidObject(data[0].id, data[0].isValid);
    };
  }

  /**
   * Executes validation for existing in context. Registers all instance objects
   * which have incorrect existence in context.
   */
  _registerInstanceObjectsWithInvalidContext(invalidInstanceObjectsModels) {
    let invalidInstanceObjects = this._modelsToInstanceObject(invalidInstanceObjectsModels);
    return this.instanceContextService.validateExistingInContextAll(invalidInstanceObjects).then((validationResults) => {
      invalidInstanceObjects.forEach((invalidInstanceObject) => {
        let invalidInstanceObjectId = invalidInstanceObject.getId();
        let validationResult = validationResults[invalidInstanceObjectId];
        let invalidInstanceObjectModel = invalidInstanceObjectsModels[invalidInstanceObjectId];
        invalidInstanceObjectModel.contextSelectorEnabled = !validationResult.isValid;
        if (!validationResult.isValid) {
          invalidInstanceObjectModel.contextSelectorConfig = this._createContextSelectorConfig(validationResult.existingInContext, invalidInstanceObject);
        }
      });
    });
  }


  /**
   * Create configuration for context selector.
   */
  _createContextSelectorConfig(existingInContext, invalidInstanceObject) {
    let parentId = InstanceContextService.getParent(invalidInstanceObject);
    let eventEmitter = new EventEmitter();
    this._registerContextSelectionHandler(eventEmitter, invalidInstanceObject, existingInContext);
    return {
      eventEmitter,
      parentId,
      contextSelectorSelectionMode: existingInContext
    };
  }

  /**
   * Register context change event listener for <code>invalidInstanceObject</code>.
   */
  _registerContextSelectionHandler(eventEmitter, invalidInstanceObject, existingInContext) {
    let invalidInstanceObjectId = invalidInstanceObject.getId();
    eventEmitter.subscribe(CONTEXT_CHANGED_EVENT, (contextId) => {
      // Update parent of instance with new one.
      InstanceContextService.updateParent(invalidInstanceObject, contextId);
      let errorMessage = InstanceContextService.validateExistenceInContext(contextId, existingInContext);
      if (errorMessage) {
        // if existing in context is not valid register object as invalid and display error message.
        this.instancesWithInvalidContext.add(invalidInstanceObjectId);
        eventEmitter.publish(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, errorMessage);
      } else {
        this.instancesWithInvalidContext.delete(invalidInstanceObjectId);
      }
      this._validate();
    });
  }

  /**
   * Update registration for {@link InstanceObject} with id <code>instanceObjectId</code>.
   * @param instanceObjectId - the id of instance object which validation status have to be updated.
   * @param isValid - status of validation. true if all fields of instance object are valid.
   */
  updateInvalidObject(instanceObjectId, isValid) {
    this.instancesWithInvalidValidation[instanceObjectId] = isValid;
    this._validate();
  }

  /**
   * Checks validation status of all registered instance objects. If validation of some registered {@link InstanceObject} object is invalid okButton (Save) will
   * be disabled.
   */
  _validate() {
    if (this.instancesWithInvalidContext.size === 0) {
      this.okButton.disabled = Object.keys(this.instancesWithInvalidValidation).some((id) => {
        return !this.instancesWithInvalidValidation[id];
      });
    } else {
      this.okButton.disabled = true;
    }
  }

  /**
   * Converts <code>invalidInstanceObjectsModels</code> to array with {@link InstanceObject} objects.
   */
  _modelsToInstanceObject(invalidInstanceObjectsModels) {
    return Object.entries(invalidInstanceObjectsModels).map(([invalidInstanceId, {models: invalidInstanceModel}]) => {
      return new InstanceObject(invalidInstanceId, invalidInstanceModel);
    });
  }
}

DialogService.DEFAULT_FORM_WRAPPER_CONFIG = {
  formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT,
  renderMandatory: true
};
