import {Component, View, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {InstanceCreatePanel} from 'create/instance-create-panel';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import showDocumentDialogTemplate from './instance-create-panel-bootstrap.html!text';

@Component({
  selector: 'instance-create-panel-bootstrap',
  properties: {
    'config': 'config'
  }
})
@View({
  template: showDocumentDialogTemplate
})
@Inject(DialogService, InstanceRestService, PromiseAdapter)
export class InstanceCreatePanelBootstrap {

  constructor(dialogService, instanceRestService, promiseAdapter) {
    this.dialogService = dialogService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
  }

  showCreateDialog() {
    var parent = {
      id: 'pid',
      definitionId: 'OT210027',
      properties: {}
    };

    this.instanceRestService.create(parent).then(()=> {
      let dialogConfig = {
        header: 'Document Creation',
        showClose: true
      };
      let config = {
        config: {
          parentId: 'test_id',
          parentType: 'ptype',
          operation: 'create',
          renderMandatory: true,
          formConfig: {
            models: {
              parentId: 'test_id',
              validationModel: {
                isValid: false
              }
            }
          },
          onCreate: () => {
            return this.promiseAdapter.resolve({
              headers: {
                breadcrumb_header: 'header'
              }
            });
          },
          onOpen: () => {
            dialogConfig.dismiss();
          },
          onCancel: () => {
            dialogConfig.dismiss();
          }
        }
      };
      this.dialogService.create(InstanceCreatePanel, config, dialogConfig);
    });
  }
}
