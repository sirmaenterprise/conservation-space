import {View, Component, Inject, NgScope} from 'app/app';
import {CreatePanelService} from 'services/create/create-panel-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {RefreshWidgetsCommand} from 'idoc/actions/events/refresh-widgets-command';
import {Eventbus} from 'services/eventbus/eventbus';
import createNewTemplate from './create-new.html!text';

@Component({
  selector: 'seip-create-new'
})
@View({
  template: createNewTemplate
})
@Inject(CreatePanelService, NgScope, WindowAdapter, IdocContextFactory, DialogService, TranslateService, Eventbus)
export class CreateNew {

  constructor(createPanelService, $scope, windowAdapter, idocContextFactory, dialogService, translateService, eventbus) {
    this.createPanelService = createPanelService;
    this.$scope = $scope;
    this.windowAdapter = windowAdapter;
    this.idocContextFactory = idocContextFactory;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.eventbus = eventbus;
  }

  createNew() {
    let context = this.idocContextFactory.getCurrentContext();
    if(!context) {
      this.openCreateInstanceDialog(null);
      return;
    }
    context.getCurrentObject().then((object) => {
      if(object.isPersisted()) {
        this.openCreateInstanceDialog(object.id);
      } else {
        this.dialogService.confirmation(this.translateService.translateInstant('unsaved.data'), null, {
          buttons: [
            {id: CreateNew.CONFIRM, label: this.translateService.translateInstant('dialog.button.ok'), cls: 'btn-primary'},
            {id: CreateNew.CANCEL, label: this.translateService.translateInstant('dialog.button.cancel')}
          ],
          onButtonClick: (buttonID, componentScope, dialogConfig) => {
            if (buttonID === CreateNew.CONFIRM) {
              this.openCreateInstanceDialog(object.models.parentId);
            }
            dialogConfig.dismiss();
          }
        });
      }
    });
  }

  openCreateInstanceDialog(parentId) {
    let params = {
      parentId: parentId,
      returnUrl: this.windowAdapter.location.href,
      operation: 'create',
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

CreateNew.CONFIRM = 'CONFIRM';
CreateNew.CANCEL = 'CANCEL';