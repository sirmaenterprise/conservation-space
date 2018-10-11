import {Injectable, Inject, NgTimeout} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {PickerService, SEARCH_EXTENSION, CREATE_EXTENSION, UPLOAD_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Router} from 'adapters/router/router';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {DialogService} from 'components/dialog/dialog-service';
import {StatusCodes} from 'services/rest/status-codes';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRestService, EDIT_OPERATION_NAME} from 'services/rest/instance-service';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';

const OPERATION = 'move';

/**
 * Action handler for moving an instance to another context.
 *
 * @author nvelkov
 */
@Injectable()
@Inject(PickerService, DialogService, TranslateService, ActionsService, StateParamsAdapter, Router, Logger, NotificationService, PromiseAdapter, InstanceRestService, NgTimeout)
export class MoveAction extends InstanceAction {

  constructor(pickerService, dialogService, translateService, actionsService, stateParamsAdapter, router, logger, notificationService, promiseAdapter, instanceRestService, $timeout) {
    super(logger);
    this.pickerService = pickerService;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.actionsService = actionsService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.router = router;
    this.notificationService = notificationService;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.$timeout = $timeout;
  }

  /**
   * Move the current instance to a new context regardless of whether it's already in another context.
   * @param actionDefinition the action definition
   * @param actionContext the action context
   */
  execute(actionDefinition, actionContext) {
    let pickerConfig = {
      header: 'action.move.header',
      extensions: {},
      tabs: {}
    };
    pickerConfig.extensions[SEARCH_EXTENSION] = {
      useRootContext: true,
      results: {
        config: {
          exclusions: [actionContext.currentObject.getId()],
          selectedItems: []
        }
      },
      arguments: {
        filterByWritePermissions: true
      }
    };
    // Do not use root context for create tab when moving
    pickerConfig.extensions[CREATE_EXTENSION] = {
      useContext: false
    };
    // Do not use root context for upload tab when moving
    pickerConfig.extensions[UPLOAD_EXTENSION] = {
      useContext: false
    };

    pickerConfig.tabs[BASKET_EXTENSION] = {
      label: 'property.has.parent'
    };

    return this.promiseAdapter.promise((resolve, reject) => {

      this.getParent(actionContext).then((parent) => {
        let actionPayload = this.buildActionPayload(actionDefinition, actionContext.currentObject, OPERATION);
        let oldParent;
        let selectedItem;
        if (parent && parent.getValue().length === 1) {
          oldParent = parent.getValue()[0];
          selectedItem = [{id: parent.getValue()[0]}];
        }
        this.pickerService.setSelectedItems(pickerConfig, selectedItem);
        return this.pickerService.configureAndOpen(pickerConfig, actionContext.idocContext).then((newSelectedItems) => {
          let newParent = newSelectedItems[0];
          // Proceed with the operation only if old parent and new parent are different.
          // For now we can't have instance without parent. But when it is implemented, the check if new parent is set have to be removed.
          if (newParent && this.isParentChanged(oldParent, newParent)) {
            actionPayload.destination = newParent.id;
            this.dialogService.confirmation(this.translateService.translateInstant('action.move.confirmation'), null,
              this.getConfirmationConfiguration(actionContext, actionPayload, resolve, reject));
          } else {
            reject();
          }
        }).catch(() => {
          reject();
        });
      });
    });
  }

  getParent(actionContext) {
    return this.promiseAdapter.promise((resolve) => {
      if (actionContext.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
        resolve(actionContext.currentObject.getPropertyValue('hasParent'));
      } else {
        return this.instanceRestService.loadInstanceObject(actionContext.currentObject.getId(), EDIT_OPERATION_NAME).then(instanceObject => {
          resolve(instanceObject.getPropertyValue('hasParent'));
        });
      }
    });
  }

  /**
   * Handle the button click. If the clicked button is the confirmation button, go on with the move operation,
   * otherwise cancel it.
   * @param buttonID the button id
   * @param dialogConfig the dialog configuration
   * @param context the context
   * @param actionPayload the action payload
   */
  handleButtonClick(buttonID, dialogConfig, context, actionPayload, resolve, reject) {
    if (buttonID === DialogService.YES) {
      let currentObjectId = context.currentObject.getId();
      this.actionsService.move(currentObjectId, actionPayload, {skipInterceptor: true})
        .then((response) => {
          this.instanceRestService.loadContextPath(currentObjectId, {skipInterceptor: true})
            .then((contextPath) => {
              context.currentObject.setContextPath(contextPath.data);
              this.notificationService.success(this.translateService.translateInstant('action.move.completed'));

              this.$timeout(() => {
                resolve(response.data);
              }, 0);
            });
        }).catch((error) => {
        if (error.status === StatusCodes.NOT_ALLOWED) {
          this.notificationService.warning(this.translateService.translateInstant('action.move.not.allowed.error'));
        } else if (error.status === StatusCodes.PRECONDITION_FAILED) {
          this.notificationService.warning(error.data.message);
        } else {
          this.notificationService.error(this.translateService.translateInstant('action.move.error'));
        }
        reject();
      });
    } else {
      reject();
    }
    dialogConfig.dismiss();
  }

  /**
   * Get a confirmation dialog configuration based on the context and action payload.
   * @param actionContext the context
   * @param actionPayload the action payload
   * @returns {*} a configuration containing the buttons and button click handler
   */
  getConfirmationConfiguration(actionContext, actionPayload, resolve, reject) {
    return {
      buttons: [
        {
          id: DialogService.YES,
          label: 'dialog.button.yes',
          cls: 'btn-primary'
        },
        {
          id: DialogService.NO,
          label: 'dialog.button.no'
        }
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => this.handleButtonClick(buttonID, dialogConfig,
        actionContext, actionPayload, resolve, reject)
    };
  }

  isParentChanged(oldParent, newParent) {
    if (newParent) {
      if (oldParent) {
        return newParent.id !== oldParent.id;
      }
      return true;
    }
    return !!oldParent;
  }
}