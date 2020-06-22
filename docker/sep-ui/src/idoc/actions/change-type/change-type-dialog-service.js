import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {ChangeTypeDialog} from 'idoc/actions/change-type/change-type-dialog';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ActionsService} from 'services/rest/actions-service';
import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {TemplateDataPanel} from 'idoc/template/template-data-panel';
import {InstanceUtils} from 'instance/utils';
import {CONTEXT_VALIDATED} from 'create/instance-create-panel';
import {
  ADD_CONTEXT_ERROR_MESSAGE_COMMAND,
  CONTEXT_CHANGED_EVENT
} from 'components/contextselector/context-selector';

/**
 * Service responsible for configuring and open a ChangeTypeDialog.
 *
 * @author svelikov
 */
@Injectable()
@Inject(DialogService, PromiseAdapter, InstanceContextService, ActionsService, TranslateService, NotificationService)
export class ChangeTypeDialogService {

  constructor(dialogService, promiseAdapter, instanceContextService, actionsService, translateService, notificationService) {
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.instanceContextService = instanceContextService;
    this.actionsService = actionsService;
    this.translateService = translateService;
    this.notificationService = notificationService;
  }

  openDialog(config) {
    return this.promiseAdapter.promise((resolve, reject) => {

      let properties = {
        config: {
          controls: {
            showCreate: true,
            showCancel: true,
            showCreateMore: false
          },
          formConfig: {
            models: {
              id: config.instanceId,
              parentId: config.parentId
            }
          },
          forceCreate: false,
          allowTypeChange: true,
          existingInContext: config.existingInContext,
          instanceId: config.instanceId,
          instanceType: config.instanceType,
          // instanceSubType: config.instanceSubType, Don't pass this for now because instance create dialog cannot handle it properly!
          purpose: config.purpose || ModelsService.PURPOSE_CREATE,
          instanceLoader: this.loadInstance(config.instanceId),
          showTemplateSelector: true,
          templatePurpose: this.getTemplatePurpose(config.purpose),
          eventEmitter: config.eventEmitter
        }
      };

      if (config.formConfig) {
        Object.keys(config.formConfig).forEach(
          formConfigProperty => properties.config[formConfigProperty] = config.formConfig[formConfigProperty]
        );
      } else {
        Object.keys(DialogService.DEFAULT_FORM_WRAPPER_CONFIG).forEach(
          formConfigProperty => properties.config[formConfigProperty] = DialogService.DEFAULT_FORM_WRAPPER_CONFIG[formConfigProperty]
        );
      }

      let changeTypeButton = this.getChangeTypeButton(config.onChangeType, properties.config.formConfig.models);

      // Change type button could be disabled in result of two distinguished events - model validation and context
      // validation. As long as both events carry different payloads I need to either sync them or have a way to share
      // a common context used also in button disabling condition. Syncing events which are not guaranteed to be fired
      // would be hard and that's why a shared context is used to carry required data and to be updated after every
      // event.
      let eventContext = {
        isContextValid: false,
        isAllowedInContext: false,
        isModelValid: false
      };

      this.registerContextChangeHandler(properties.config);
      this.registerContextValidatedHandler(properties.config, changeTypeButton, eventContext);

      properties.config.onFormLoaded = this.onFormLoaded(properties.config, changeTypeButton, eventContext);

      this.dialogService.create(ChangeTypeDialog, properties, this.getDialogConfig(changeTypeButton, this.getCancelButton()));
    });
  }

  loadInstance(id) {
    return (definitionId) => {
      return this.actionsService.getChangeTypeInstance(id, definitionId);
    };
  }

  onFormLoaded(config, changeTypeButton, eventContext) {
    return (event) => {
      event.models.validationModel.subscribe('modelValidated', () => {
        this.toggleChangeTypeButton(changeTypeButton, config, null, eventContext);
      });
    };
  }

  registerContextValidatedHandler(config, changeTypeButton, eventContext) {
    this.contextValidatedSubscription = config.eventEmitter.subscribe(CONTEXT_VALIDATED, (data) => {
      this.toggleChangeTypeButton(changeTypeButton, config, data, eventContext);
    });
  }

  toggleChangeTypeButton(changeTypeButton, config, evt, eventContext) {
    let isContextValid = evt && !evt.errorMessage && !!evt.models.length;

    eventContext.isModelValid = this.isModelValid(config);
    eventContext.isContextValid = isContextValid !== null && isContextValid !== eventContext.isContextValid ? isContextValid : eventContext.isContextValid;
    eventContext.isAllowedInContext = config.existingInContext && !InstanceContextService.validateExistenceInContext(config.parentId, config.existingInContext);

    let isDisabled = !(eventContext.isModelValid && eventContext.isContextValid && eventContext.isAllowedInContext);

    changeTypeButton.disabled = isDisabled;
  }

  registerContextChangeHandler(config) {
    config.eventEmitter.subscribe(CONTEXT_CHANGED_EVENT, (contextId) => {
      if (config.formConfig) {
        config.formConfig.models.parentId = contextId;
      }
      let errorMessage = InstanceContextService.validateExistenceInContext(contextId, config.existingInContext);
      if (errorMessage) {
        config.eventEmitter.publish(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, errorMessage);
      }
    });
  }

  isModelValid(config) {
    if (!config.formConfig.models || !config.formConfig.models.validationModel) {
      return false;
    }
    return config.formConfig.models.definitionId === null || config.formConfig.models.validationModel.isValid;
  }

  confirmOperation() {
    return this.promiseAdapter.promise((resolve, reject) => {
      this.dialogService.confirmation(this.translateService.translateInstant('idoc.change.type.confirmation'), undefined, {
        modalCls: 'change-type-confirmation',
        buttons: [
          this.dialogService.createButton(DialogService.YES, 'dialog.button.yes', true),
          this.dialogService.createButton(DialogService.NO, 'dialog.button.no')
        ],
        onButtonClick: (buttonId, componentScope, dialogConfig) => {
          if (buttonId === DialogService.YES) {
            resolve();
          }
          dialogConfig.dismiss();
        }
      });
    });
  }

  getDialogConfig(changeTypeButton, cancelButton) {
    return {
      header: 'idoc.change.type.dialog.header',
      showClose: true,
      backdrop: 'static',
      modalCls: 'change-type-modal-dialog',
      largeModal: true,
      onClose: () => {
        this.promiseAdapter.reject();
      },
      buttons: [changeTypeButton, cancelButton]
    };
  }

  getChangeTypeButton(callback, models) {
    let buttonDefinition;
    let onConfirm = () => {
      buttonDefinition.disabled = true;
      return callback(models);
    };

    buttonDefinition = {
      id: 'SAVE',
      label: 'idoc.change.type.dialog.button.save',
      cls: 'btn-primary',
      disabled: true,
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        return this.confirmOperation()
          .then(onConfirm)
          .then((instance) => {
            this.promiseAdapter.resolve();
            dialogConfig.dismiss();
            this.notificationService.success(this.translateService.translateInstant('idoc.change.type.notification.success') + instance.data.headers.compact_header);
          });
      }
    };
    return buttonDefinition;
  }

  getCancelButton() {
    return {
      id: DialogService.CANCEL,
      label: 'idoc.change.type.dialog.button.cancel',
      dismiss: true
    };
  }

  getTemplatePurpose(purpose) {
    let templatePurpose;
    if (purpose) {
      templatePurpose = InstanceUtils.isCreatable(purpose) ? TemplateDataPanel.CREATABLE : TemplateDataPanel.UPLOADABLE;
    } else {
      templatePurpose = TemplateDataPanel.CREATABLE;
    }
    return templatePurpose;
  }
}