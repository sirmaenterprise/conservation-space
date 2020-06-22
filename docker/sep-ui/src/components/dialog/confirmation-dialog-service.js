import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';

/**
 * Encapsulates logic for configuring and opening a confirmation dialog wrapped in a Promise.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(DialogService, PromiseAdapter, TranslateService)
export class ConfirmationDialogService {

  constructor(dialogService, promiseAdapter, translateService) {
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
  }

  /**
   * Opens a confirmation dialog and returns a promise which:
   * 1) will be resolved if the confirmation is accepted
   * 2) will be rejected if the confirmation is cancelled
   *
   * @param messages object carrying translated labels or translation keys. Expected structure is:
   * <code>
   * {
   *   message: '', // the message to visualize to the user in the confirmation. Mandatory
   *   header: '', // message to be displayed as title of the confirmation dialog. Optional
   *   confirmLabel: '', // label for the confirmation button. Optional, defaults to Yes
   *   cancelLabel: '' // label for the cancellation button. Optional, defaults to No
   * }
   * </code>
   */
  confirm(messages) {
    // Headers are not translated in the dialog service.
    let header = messages.header ? this.translateService.translateInstant(messages.header) : undefined;
    return this.promiseAdapter.promise((resolve, reject) => {
      this.dialogService.confirmation(messages.message, header, {
        buttons: [
          this.dialogService.createButton(DialogService.YES, messages.confirmLabel || 'dialog.button.yes', true),
          this.dialogService.createButton(DialogService.NO, messages.cancelLabel || 'dialog.button.no')
        ],
        onButtonClick: (buttonId, componentScope, dialogConfig) => {
          dialogConfig.dismiss();
          if (buttonId === DialogService.YES) {
            resolve();
          } else {
            reject();
          }
        }
      });
    });
  }
}