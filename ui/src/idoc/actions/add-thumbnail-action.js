import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Logger} from 'services/logging/logger';
import {DialogService} from 'components/dialog/dialog-service';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

const PREDEFINED_TYPES = ['emf:Image'];

@Injectable()
@Inject(DialogService, PickerService, ActionsService, NotificationService, TranslateService, Logger, PromiseAdapter)
export class AddThumbnailAction extends InstanceAction {

  constructor(dialogService, pickerService, actionsService, notificationService, translateService, logger, promiseAdapter) {
    super(logger);
    this.dialogService = dialogService;
    this.pickerService = pickerService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, actionContext) {
    var object = actionContext.currentObject;

    // Show confirmation dialog if the object has a thumbnail
    if (object.getThumbnail()) {
      return this.showConfirmationDialog(actionContext);
    } else {
      return this.addThumbnail(actionContext);
    }
  }

  showConfirmationDialog(context) {
    return this.promiseAdapter.promise((resolve) => {
      var dialogConfig = this.getConfirmationDialogConfig(context, resolve);
      var message = this.translateService.translateInstant('action.add.thumbnail.existing');
      this.dialogService.confirmation(message, undefined, dialogConfig);
    });
  }

  addThumbnail(context) {
    return this.selectDocument(context).then((selectedDocument) => {
      let request = {
        instanceId: context.currentObject.getId(),
        thumbnailObjectId: selectedDocument.id
      };
      return this.actionsService.addThumbnail(request);
    }).then(() => {
      this.afterAddingThumbnail();
    });
  }

  selectDocument(context) {
    var pickerConfiguration = this.getPickerConfiguration();
    var dialogConfigReference = {};
    var pickerPromise = this.pickerService.configureAndOpen(pickerConfiguration, context.idocContext, dialogConfigReference);

    this.registerSelectedItemsWatcher(context.scope, pickerConfiguration, dialogConfigReference);

    return pickerPromise.then((selectedItems) => {
      return selectedItems[0];
    });
  }

  afterAddingThumbnail() {
    var message = this.translateService.translateInstant('action.add.thumbnail.success');
    this.notificationService.success(message);
  }

  registerSelectedItemsWatcher(scope, pickerConfiguration, dialogConfig) {
    if (scope) {
      // Disable the OK button until something is selected.
      dialogConfig.buttons[0].disabled = true;

      var unbindFunction = scope.$watchCollection(()=> {
        return this.pickerService.getSelectedItems(pickerConfiguration);
      }, (selectedItems) => {
        if (selectedItems.length > 0) {
          dialogConfig.buttons[0].disabled = false;
          unbindFunction();
        }
      });
    }
  }

  getConfirmationDialogConfig(context, resolve) {
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
        if (buttonId === DialogService.YES) {
          var promise = this.addThumbnail(context);
          resolve(promise);
        }
        dialogConfig.dismiss();
      }
    };
  }

  getPickerConfiguration() {
    var config = {
      header: 'action.add.thumbnail.header',
      extensions: {}
    };
    config.extensions[SEARCH_EXTENSION] = {
      predefinedTypes: PREDEFINED_TYPES
    };
    return config;
  }
}