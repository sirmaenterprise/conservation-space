import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {UserService} from 'services/identity/user-service';
import {ChangePasswordDialog} from 'user/change-password/change-password-dialog';
import {StatusCodes} from 'services/rest/status-codes';

@Injectable()
@Inject(DialogService, NotificationService, TranslateService, UserService)
export class ChangePasswordService {

  constructor(dialogService, notificationService, translateService, userService) {
    this.dialogService = dialogService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.userService = userService;
  }

  openDialog() {
    let dialogConfiguration = this.getDialogConfiguration();
    this.dialogService.create(ChangePasswordDialog, {
      config: {
        // invoke the callback with the OK button reference and data passed with the event
        afterFormValidation: (data) => this.afterFormValidation(dialogConfiguration.buttons[0], data)
      }
    }, dialogConfiguration);
  }

  getDialogConfiguration() {
    return {
      showHeader: true,
      header: 'change.password.dialog.header',
      buttons: [{
        id: DialogService.OK,
        label: 'change.password.dialog.change.btn',
        cls: 'btn-primary',
        disabled: true,
        onButtonClick: (button, dialogScope, dialogConfig) => {
          this.changePasswordHandler(dialogScope, dialogConfig);
        }
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };
  }

  afterFormValidation(button, data) {
    let isValidForm = data[0].isValid;
    button.disabled = !isValidForm;
  }

  changePasswordHandler(dialogScope, dialogConfig) {
    let dialog = dialogScope.changePasswordDialog;
    let currentPassword = dialog.getCurrentPassword();
    let newPassword = dialog.getNewPassword();

    this.userService.changePassword(currentPassword.value, newPassword.value).then((response) => {
      if (response.status === StatusCodes.SUCCESS) {
        this.notificationService.success(this.translateService.translateInstant('change.password.dialog.success'));
        dialogConfig.dismiss();
      }
    }, (response) => {
      // TODO: remove this handling when messages in notifications are added
      if (response.status === StatusCodes.BAD_REQUEST && response.data.messages) {
        let data = response.data;

        if (data.messages.passwordWrongMessage) {
          this.notificationService.error(this.translateService.translateInstant('change.password.dialog.wrong'));
        } else {
          this.notificationService.error(data.messages.passwordValidationMessage);
        }
      } else {
        this.notificationService.error(this.translateService.translateInstant('error.generic'));
      }
    });
  }

}