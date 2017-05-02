import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {DialogService} from 'components/dialog/dialog-service';
import {FileUpload} from 'file-upload/file-upload';
import {Configuration} from 'common/application-config';

@Injectable()
@Inject(DialogService, Configuration, Logger)
export class UploadNewVersionAction extends InstanceAction {

  constructor(dialogService, configuration, logger) {
    super(logger);

    this.dialogService = dialogService;
    this.configuration = configuration;
  }

  execute(actionDefinition, context) {
    var currentObject = context.currentObject;
    var uploadConfig = {
      config: {
        userOperation: actionDefinition.action,
        id: currentObject.id,
        header: currentObject.headers.breadcrumb_header,
        maxFileSize: this.configuration.get(Configuration.UPLOAD_MAX_FILE_SIZE)
      }
    };

    var dialogConfig = {
      modalCls: 'file-upload-dialog',
      header: actionDefinition.label,
      largeModal: true,
      buttons: [{
        id: DialogService.CLOSE,
        label: 'dialog.button.cancel'
      }],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };

    this.dialogService.create(FileUpload, uploadConfig, dialogConfig);
  }

}