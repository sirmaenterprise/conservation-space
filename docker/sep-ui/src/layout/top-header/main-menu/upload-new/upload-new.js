import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {CreatePanelService} from 'services/create/create-panel-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {RefreshWidgetsCommand} from 'idoc/actions/events/refresh-widgets-command';
import {Eventbus} from 'services/eventbus/eventbus';
import {FileUploadDnDSupport} from 'file-upload/dnd-support/file-upload-dnd-support';
import './upload-new.css!css';
import template from './upload-new.html!text';

@Component({
  selector: 'seip-upload-new'
})
@View({
  template
})
@Inject(CreatePanelService, NgScope, WindowAdapter, IdocContextFactory, DialogService, TranslateService, Eventbus, NgElement)
export class UploadNew {

  constructor(createPanelService, $scope, windowAdapter, idocContextFactory, dialogService, translateService, eventbus, $element) {
    this.createPanelService = createPanelService;
    this.$scope = $scope;
    this.windowAdapter = windowAdapter;
    this.idocContextFactory = idocContextFactory;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.eventbus = eventbus;
    FileUploadDnDSupport.addDropSupport($element, (files) => {
      this.uploadNew(files);
    }, {animationClass: 'upload-new-drop-over'});
  }

  uploadNew(files) {
    let context = this.idocContextFactory.getCurrentContext();
    if (!context) {
      this.openUploadInstanceDialog(null, files);
      return;
    }
    context.getCurrentObject().then((object) => {
      if (object.isPersisted()) {
        this.openUploadInstanceDialog(object.id, files);
      } else {
        this.dialogService.confirmation(this.translateService.translateInstant('unsaved.data'), null, {
          buttons: [
            {
              id: UploadNew.CONFIRM,
              label: this.translateService.translateInstant('dialog.button.ok'),
              cls: 'btn-primary'
            },
            {id: UploadNew.CANCEL, label: this.translateService.translateInstant('dialog.button.cancel')}
          ],
          onButtonClick: (buttonID, componentScope, dialogConfig) => {
            if (buttonID === UploadNew.CONFIRM) {
              this.openUploadInstanceDialog(object.models.parentId, files);
            }
            dialogConfig.dismiss();
          }
        });
      }
    });
  }

  openUploadInstanceDialog(parentId, files) {
    let params = {
      parentId,
      files,
      returnUrl: this.windowAdapter.location.href,
      operation: 'upload',
      defaultTab: 'file-upload-panel',
      scope: this.$scope,
      onClosed: (result) => {
        if (result.instanceCreated) {
          this.eventbus.publish(new RefreshWidgetsCommand());
        }
      }
    };
    this.createPanelService.openCreateInstanceDialog(params);
  }
}

UploadNew.CONFIRM = 'CONFIRM';
UploadNew.CANCEL = 'CANCEL';