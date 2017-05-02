import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {HelpDialog} from 'user/help-request/help-request-dialog';
import {HelpRequestRestService} from 'services/rest/help-request-service';

@Injectable()
@Inject(DialogService, NotificationService, TranslateService, HelpRequestRestService)
export class HelpRequestService {

  constructor(dialogService, notificationService, translateService, helpRequestRestService) {
    this.dialogService = dialogService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.helpRequestRestService = helpRequestRestService;
  }

  /**
   * Create dialog and show it.
   */
  openDialog() {
    let dialogConfiguration = this.getDialogConfiguration();
    this.dialogService.create(HelpDialog, {
      config: {
        // invoke the callback with the OK button reference and data passed with the event
        afterFormValidation: (data) => this.afterFormValidation(dialogConfiguration, data),
        dialogConfiguration: dialogConfiguration
      }
    }, dialogConfiguration);
  }

  /**
   * Configuration of dialog buttons.
   *
   * @returns {{header: string, buttons: *[]}}
   */
  getDialogConfiguration() {
    return {
      header: 'help.request.dialog.header',
      modalCls: 'help-request',
      buttons: [{
        id: DialogService.OK,
        label: 'help.request.dialog.send.btn',
        cls: 'btn-primary',
        disabled: true,
        onButtonClick: (button, dialogScope, dialogConfig) => {
          this.sendRequest(dialogScope, dialogConfig);
        }
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };
  }

  /**
   * Fetch values from help request dialog and send it to server.
   * @param dialogScope
   * @param dialogConfig
   */
  sendRequest(dialogScope, dialogConfig) {
    var data = dialogScope.helpDialog.prepareRequestModel();
    this.helpRequestRestService.sendHelpRequest(data).then(() => {
      this.notificationService.success(this.translateService.translateInstant('user.menu.help.request.message.success'));
      dialogConfig.dismiss();
    }, () => {
      this.notificationService.error(this.translateService.translateInstant('user.menu.help.request.message.error'));
    });
  }

  /**
   * Validate form.
   * @param dialogConfiguration
   * @param data
   */
  afterFormValidation(dialogConfiguration, data) {
    dialogConfiguration.buttons[0].disabled = !data[0].isValid;
  }
}