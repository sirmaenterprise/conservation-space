import {Injectable, Inject} from 'app/app';
import {FormWrapper} from 'form-builder/form-wrapper';
import {DialogService} from 'components/dialog/dialog-service';
import {SaveIdocDialog} from 'idoc/save-idoc-dialog/save-idoc-dialog';
import {ValidationService} from 'form-builder/validation/validation-service';

@Injectable()
@Inject(DialogService)
export class SaveDialogService {

  constructor(dialogService) {
    this.dialogService = dialogService;
  }

  /**
   * Opens dialog with form builder to render details of all the objects for which there are models passed.
   * @param config Contains: models, context, onFormValidated
   * @returns {Promise}
   */
  openDialog(config) {
    return new Promise((resolve, reject) => {
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
          invalidObjects: config.models,
          onFormValidated: config.onFormValidated,
          okButton: okButton
        }
      };
      this.dialogService.create(SaveIdocDialog, properties, dialogConfig);
    });
  }
}

