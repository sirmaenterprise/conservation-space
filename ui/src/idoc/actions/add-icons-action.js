import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {DialogService} from 'components/dialog/dialog-service';
import {ClassIconsUpload} from 'administration/class-icons-upload/class-icons-uploader';
import {Configuration} from 'common/application-config';
import {ActionsService} from 'services/rest/actions-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

export const UPLOAD_BUTTON_ID = 'UPLOAD';

@Injectable()
@Inject(DialogService, Logger, Configuration, ActionsService, PromiseAdapter)
export class AddIconsAction extends InstanceAction {

  constructor(dialogService, logger, configuration, actionsService, promiseAdapter) {
    super(logger);
    this.dialogService = dialogService;
    this.configuration = configuration;
    this.actionsService = actionsService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, actionContext) {
    return this.promiseAdapter.promise((resolve) => {
      this.classId = actionContext.currentObject.id;
      var buttons = [
        {
          id: UPLOAD_BUTTON_ID,
          label: 'dialog.button.upload',
          cls: 'btn-primary',
          dismiss: true,
          disabled: true
        },
        {
          id: DialogService.CANCEL,
          label: 'dialog.button.cancel'
        }];

      var uploadConfig = {
        config: {
          maxFileSize: this.configuration.get(Configuration.UPLOAD_MAX_FILE_SIZE),
          buttons: buttons
        }
      };

      var dialogConfig = {
        modalCls: 'file-upload-dialog',
        header: action.label,
        largeModal: true,
        buttons: buttons,
        onButtonClick: (buttonId, componentScope, dialogConfig) => {
          this.handleButtonClickedEvent(buttonId, componentScope, dialogConfig, resolve);
        }
      };
      this.dialogService.create(ClassIconsUpload, uploadConfig, dialogConfig);
    });
  }

  handleButtonClickedEvent(buttonId, componentScope, dialogConfig, resolve) {
    if (buttonId === UPLOAD_BUTTON_ID) {
      var icons = componentScope.classIconsUpload.icons;
      var promise = this.processSelectedIcons(this.classId, icons);
      resolve(promise);
    }
    dialogConfig.dismiss();
  }

  processSelectedIcons(id, icons) {
    icons = icons.filter((icon)=> {
      return icon.file;
    }).map((icon)=> {
      return {size: icon.size, image: icon.file};
    });
    return this.actionsService.addIcons(id, icons);
  }
}