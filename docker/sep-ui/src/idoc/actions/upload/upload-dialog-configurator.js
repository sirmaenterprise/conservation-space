/**
 * Created by tdossev on 16.2.2018 г..
 */
import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {FileUpload} from 'file-upload/file-upload';
import {Configuration} from 'common/application-config';
import {EventEmitter} from 'common/event-emitter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

/**
 * Dialog creator helper for upload operations
 */
@Injectable()
@Inject(DialogService, Configuration, PromiseAdapter)
export class UploadDialogConfigurator {

  constructor(dialogService, configuration, promiseAdapter) {
    this.dialogService = dialogService;
    this.configuration = configuration;
    this.promiseAdapter = promiseAdapter;
  }

  createDialog(actionDefinition, currentObject, url, skipEntityUpdate, showNotification) {
    return this.promiseAdapter.promise((resolve) => {
      let uploadConfig = {
        config: {
          userOperation: actionDefinition.action,
          id: currentObject.id,
          url,
          skipEntityUpdate,
          showNotification,
          header: currentObject.headers.breadcrumb_header,
          maxFileSize: this.configuration.get(Configuration.UPLOAD_MAX_FILE_SIZE),
          eventEmitter: new EventEmitter(),
          onClosed: resolve
        }
      };

      let dialogConfig = {
        modalCls: 'file-upload-dialog',
        header: actionDefinition.label,
        largeModal: true,
        buttons: [{
          id: DialogService.CLOSE,
          label: 'dialog.button.cancel'
        }],
        onButtonClick(buttonId, componentScope, dialogConfig) {
          dialogConfig.dismiss();
        }
      };

      this.dialogService.create(FileUpload, uploadConfig, dialogConfig);
    });
  }
}
