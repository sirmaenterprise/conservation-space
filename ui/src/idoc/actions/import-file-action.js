import {Injectable, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {ActionHandler} from 'services/actions/action-handler';
import {DialogService} from 'components/dialog/dialog-service';
import {ImportService} from 'services/rest/import-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {ContextSelector} from 'components/contextselector/context-selector';

@Injectable()
@Inject(TranslateService, NotificationService, DialogService, ImportService, InstanceRestService)
export class ImportFileAction extends ActionHandler {
  constructor(translateService, notificationService, dialogService, importService, instanceRestService) {
    super();
    this.translateService = translateService;
    this.notificationService = notificationService;
    this.dialogService = dialogService;
    this.importService = importService;
    this.instanceRestService = instanceRestService;
  }

  execute(action, context) {
    this.disabled = true;
    this.context = context;
    let config = { params: { properties: ['hasParent'] } };
    return this.instanceRestService.load(context.currentObject.id, config).then((data) => {
      this.confirmFileValidation(this.provideContext(data.data));
    });
  }


  confirmFileValidation(currentObjectId) {
    var buttons = [
      { id: DialogService.CONFIRM, label: 'dialog.button.validatefile', cls: 'btn-primary', dismiss: true, disabled: false },
      { id: DialogService.CANCEL, label: 'dialog.button.cancel' }
    ];

    var componentProperties = {
      config: {
        parentId: currentObjectId
      }
    };

    var dialogConfig = {
      modalCls: 'file-import-dialog',
      header: 'dialog.header.select.context',
      largeModal: false,
      buttons: buttons,
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.CONFIRM) {
          this.contextId = componentScope.contextSelector.config.parentId;
          this.validateFile();
        }
        dialogConfig.dismiss();
      }
    };
    this.dialogService.create(ContextSelector, componentProperties, dialogConfig);
  }


  validateFile() {
    this.notificationService.info(this.translateService.translateInstant('import.validation.message'));
    return this.importService.readFile(this.context.currentObject.id, this.contextId).then((response) => {
      if (response.data.data.length > 0) {
        this.disabled = false;
      }
      this.confirmImport(response);
    });
  }

  confirmImport(response) {
    let confirmationMessage = this.translateService.translateInstant('import.file.confirmation.message');
    this.dialogService.confirmation(confirmationMessage + response.data.report.headers.breadcrumb_header, null, {
      buttons: [
        { id: DialogService.CONFIRM, label: this.translateService.translateInstant('dialog.button.importfile'), cls: 'btn-primary', disabled: this.disabled },
        { id: DialogService.CANCEL, label: this.translateService.translateInstant('dialog.button.cancel') }
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => {
        if (buttonID === DialogService.CONFIRM) {
          this.notificationService.info(this.translateService.translateInstant('idoc.info.data.import.triggered'));
          this.importService.importFile(this.context.currentObject.id, this.contextId, response.data).then(() => {
            this.notificationService.success(this.translateService.translateInstant('external.operation.success'));
          });
        }
        dialogConfig.dismiss();
      }
    });
  }

  /**
  * Check if the current object has parent and provide it as context.
  */
  provideContext(currentObject) {
    if (currentObject.properties.hasParent && currentObject.properties.hasParent.length > 0) {
      return currentObject.properties.hasParent[0].id;
    }
    return null;
  }
}