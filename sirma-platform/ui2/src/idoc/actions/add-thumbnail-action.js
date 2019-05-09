import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Logger} from 'services/logging/logger';
import {DialogService} from 'components/dialog/dialog-service';
import {PickerService, SEARCH_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRestService, EDIT_OPERATION_NAME} from 'services/rest/instance-service';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {ThumbnailUpdatedEvent} from 'idoc/actions/events/thumbnail-updated-event';
import {Eventbus} from 'services/eventbus/eventbus';

import _ from 'lodash';
import {InstanceObjectWrapper} from 'models/instance-object-wrapper';

const PREDEFINED_TYPES = ['emf:Image'];
const THUMBNAIL_URI = 'emf:hasThumbnail';

@Injectable()
@Inject(DialogService, PickerService, ActionsService, NotificationService, TranslateService, Logger, PromiseAdapter, InstanceRestService, Eventbus)
export class AddThumbnailAction extends InstanceAction {

  constructor(dialogService, pickerService, actionsService, notificationService, translateService, logger, promiseAdapter, instanceRestService, eventbus) {
    super(logger);
    this.dialogService = dialogService;
    this.pickerService = pickerService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.eventbus = eventbus;
  }

  execute(action, actionContext) {
    let instanceObject;
    return this.loadInstanceObjectProperties(actionContext).then((loadedInstanceObject) => {
      instanceObject = loadedInstanceObject;
      return this.confirmActionExecution(instanceObject);
    }).then(() => {
      return this.selectThumbnail(actionContext.scope, instanceObject);
    }).then((selectionData) => {
      if (!_.isEqual(selectionData.oldThumbnail, selectionData.newThumbnail)) {
        return this.addThumbnail(instanceObject, selectionData.newThumbnail);
      }
      return this.promiseAdapter.reject();
    }).then((result) => {
      let message = this.translateService.translateInstant('action.add.thumbnail.success');
      this.notificationService.success(message);
      this.eventbus.publish(new ThumbnailUpdatedEvent());

      return result;
    });
  }

  loadInstanceObjectProperties(actionContext) {
    if (actionContext.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
      return actionContext.idocContext.getCurrentObject();
    } else {
      return this.instanceRestService.loadInstanceObject(actionContext.currentObject.getId(), EDIT_OPERATION_NAME);
    }
  }

  /**
   * Checks if current object have thumbnail. If yes, confirm dialog will be shown to notify user that current object
   * already have thumbnail and will wait user to confirm or cancel action.
   */
  confirmActionExecution(instanceObject) {
    let thumbnail = AddThumbnailAction.getThumbnail(instanceObject);
    return thumbnail ? this.openConfirmDialog() : this.promiseAdapter.resolve();
  }

  openConfirmDialog() {
    return this.promiseAdapter.promise((resolve, reject) => {
      let dialogConfig = this.getConfirmationDialogConfig(resolve, reject);
      let message = this.translateService.translateInstant('action.add.thumbnail.existing');
      this.dialogService.confirmation(message, undefined, dialogConfig);
    });
  }

  getConfirmationDialogConfig(resolve, reject) {
    return {
      buttons: [{
        id: DialogService.YES,
        label: 'dialog.button.yes',
        cls: 'btn-primary'
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel'
      }],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        dialogConfig.dismiss();
        if (buttonId === DialogService.YES) {
          resolve();
        } else {
          reject();
        }
      }
    };
  }

  selectThumbnail(scope, instanceObject) {
    let basketLabel = AddThumbnailAction.getBasketLabel(instanceObject);
    let pickerConfiguration = AddThumbnailAction.getPickerConfiguration(basketLabel);
    let dialogConfigReference = {};
    let oldThumbnail = AddThumbnailAction.getThumbnail(instanceObject);
    this.pickerService.setSelectedItems(pickerConfiguration, oldThumbnail);
    let pickerPromise = this.pickerService.configureAndOpen(pickerConfiguration, new InstanceObjectWrapper(this.promiseAdapter, instanceObject), dialogConfigReference);
    this.registerSelectedItemsWatcher(scope, pickerConfiguration, dialogConfigReference);
    return pickerPromise.then((newThumbnail) => {
      return {
        oldThumbnail: oldThumbnail ? oldThumbnail[0] : oldThumbnail,
        newThumbnail: newThumbnail ? newThumbnail[0] : newThumbnail
      };
    });
  }

  static getPickerConfiguration(basketLabel) {
    let config = {
      header: 'action.add.thumbnail.header',
      extensions: {},
      tabs: {}
    };
    config.extensions[SEARCH_EXTENSION] = {
      predefinedTypes: PREDEFINED_TYPES,
      results: {
        config: {
          selectedItems: []
        }
      }
    };

    config.tabs[BASKET_EXTENSION] = {
      label: basketLabel
    };
    return config;
  }

  registerSelectedItemsWatcher(scope, pickerConfiguration, dialogConfig) {
    if (scope) {
      // Disable the OK button until something is selected.
      dialogConfig.buttons[0].disabled = true;
      let unbindFunction = scope.$watchCollection(() => {
        return this.pickerService.getSelectedItems(pickerConfiguration);
      }, (selectedItems) => {
        if (selectedItems.length > 0) {
          dialogConfig.buttons[0].disabled = false;
          unbindFunction();
        }
      });
    }
  }

  addThumbnail(instanceObject, thumbnail) {
    let request = {
      instanceId: instanceObject.getId(),
      thumbnailObjectId: thumbnail.id
    };
    return this.actionsService.addThumbnail(request);
  }

  static getBasketLabel(instanceObject) {
    return instanceObject.getViewModelFieldByUri(THUMBNAIL_URI).label;
  }

  static getThumbnail(instanceObject) {
    let propertyValue = instanceObject.getPropertyValueByUri(THUMBNAIL_URI);
    if (propertyValue) {
      let value = propertyValue.getValue();
      if (value.length > 0) {
        return [{id: value[0]}];
      }
    }
  }
}
