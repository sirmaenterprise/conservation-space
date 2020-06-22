import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {DialogService} from  'components/dialog/dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(Logger, TemplateService, NotificationService, TranslateService, DialogService, PromiseAdapter)
export class UpdateExistingObjectsAction extends InstanceAction {

  constructor(logger, templateService, notificationService, translateService, dialogService, promiseAdapter) {
    super(logger);
    this.templateService = templateService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      this.dialogService.confirmation(this.translateService.translateInstant('idoc.template.instance.update.confirm'), null, {
        buttons: [
          {
            id: DialogService.YES,
            label: this.translateService.translateInstant('dialog.button.yes'),
            cls: 'btn-primary'
          },
          {id: DialogService.NO, label: this.translateService.translateInstant('dialog.button.no')}
        ],
        onButtonClick: (buttonID, componentScope, dialogConfig) => {
          dialogConfig.dismiss();
          if (buttonID === DialogService.YES) {
            return this.templateService.updateInstanceTemplate(context.currentObject.getId()).then(() => {
              let message = this.translateService.translateInstant('idoc.template.instance.update.success');
              this.notificationService.success(message);
              resolve();
            });
          } else {
            reject();
          }
        }
      });
    });
  }
}