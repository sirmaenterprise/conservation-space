import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {AddRelationPanel} from 'idoc/dialogs/add-relation-panel';
import {ActionsService} from 'services/rest/actions-service';
import {StatusCodes} from 'services/rest/status-codes';
import {NotificationService} from 'services/notification/notification-service';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import {MULTIPLE_SELECTION} from 'search/search-selection-modes';

@Injectable()
@Inject(DialogService, ActionsService, NotificationService, Eventbus, PromiseAdapter)
export class AddRelationService {

  constructor(dialogService, actionsService, notificationService, eventbus, promiseAdapter) {
    this.dialogService = dialogService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
  }

  openDialog(id, scope, context, transition) {
    return this.promiseAdapter.promise((resolve) => {
      let searchConfig = this.getSearchConfig(id, transition);
      searchConfig.context = context;

      let dialogConfig = this.getDialogConfig(id, transition, searchConfig, resolve);
      this.dialogService.create(AddRelationPanel, searchConfig, dialogConfig);
      scope.$watch(()=> {
        return searchConfig.config.selectedItemsIds;
      }, (items) => {
        dialogConfig.buttons[0].disabled = !(items && items.length > 0);
      });
    });
  }

  getSearchConfig(id, transition) {
    return {
      config: {
        useRootContext: true,
        selection: transition.configuration.selection || MULTIPLE_SELECTION,
        predefinedTypes: transition.configuration.predefinedTypes || [],
        exclusions: [id],
        triggerSearch: true
      }
    };
  }

  getDialogConfig(id, transition, searchConfig, resolve) {
    return {
      header: transition.label,
      helpTarget: 'picker',
      largeModal: true,
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.ok',
        cls: 'btn-primary',
        onButtonClick: (buttonId, componentScope, dialogConfig) => {
          dialogConfig.buttons[0].disabled = true;
          let data = this.buildRelationRequestData(transition, searchConfig.config.selectedItemsIds);
          var promise = this.addRelationHandler(id, data, dialogConfig);
          resolve(promise);
        }
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };
  }

  addRelationHandler(id, data, dialogConfig) {
    return this.actionsService.addRelation(id, data).then((response)=> {
      this.callback(response, dialogConfig);
    });
  }

  callback(response, dialogConfig) {
    if (response.status === StatusCodes.SUCCESS) {
      this.notificationService.success(dialogConfig.header);
      // we already have notification, so send only the object
      this.eventbus.publish(new InstanceRefreshEvent({
        response: response
      }));
    }
    dialogConfig.dismiss();
  }

  buildRelationRequestData(transition, selectedItems) {
    let data = transition.configuration.implicitParams;
    let relationToObjects = {};
    for (let relation of transition.configuration.relation) {
      relationToObjects[relation] = selectedItems;
    }
    data.userOperation = transition.action;
    data.relations = relationToObjects;
    return data;
  }
}