import {Injectable, Inject} from "app/app";
import {ActionsService} from "services/rest/actions-service";
import {InstanceAction} from "idoc/actions/instance-action";
import {Logger} from "services/logging/logger";
import {PickerService, SEARCH_EXTENSION, CREATE_EXTENSION} from 'services/picker/picker-service';
import {StateParamsAdapter} from "adapters/router/state-params-adapter";
import {Router} from "adapters/router/router";
import {TranslateService} from "services/i18n/translate-service";
import {NotificationService} from 'services/notification/notification-service';
import {DialogService} from "components/dialog/dialog-service";
import {StatusCodes} from 'services/rest/status-codes';
import {SearchCriteriaUtils} from "search/utils/search-criteria-utils";
import {STATE_PARAM_ID} from "idoc/idoc-page";

const OPERATION = 'move';

/**
 * Action handler for moving an instance to another context.
 *
 * @author nvelkov
 */
@Injectable()
@Inject(PickerService, DialogService, TranslateService, ActionsService, StateParamsAdapter, Router, Logger, NotificationService)
export class MoveAction extends InstanceAction {

  constructor(pickerService, dialogService, translateService, actionsService, stateParamsAdapter, router, logger, notificationService) {
    super(logger);
    this.pickerService = pickerService;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.actionsService = actionsService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.router = router;
    this.notificationService = notificationService;
  }

  /**
   * Refresh the view after the instance has been moved.
   */
  afterInstanceRefreshHandler() {
    this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {reload: true});
  }

  /**
   * Move the current instance to a new context regardless of whether it's already in another context.
   * @param actionDefinition the action definition
   * @param actionContext the action context
   */
  execute(actionDefinition, actionContext) {
    var pickerConfig = {
      header: 'action.move.header',
      extensions: {}
    };
    pickerConfig.extensions[SEARCH_EXTENSION] = {
      useRootContext: true,
      results: {
        config: {
          exclusions: [actionContext.currentObject.getId()]
        }
      }
    };
    // Do not use root context for create tab when moving
    pickerConfig.extensions[CREATE_EXTENSION] = {
      useRootContext: false
    };

    var actionPayload = this.buildActionPayload(actionDefinition, actionContext.currentObject, OPERATION);
    return this.pickerService.configureAndOpen(pickerConfig, actionContext.idocContext).then((selectedItems)=> {
      let selectedItem = selectedItems[0];
      //Proceed with the operation only if a new context has been selected from the object picker.
      if (selectedItem && selectedItem.id) {
        actionPayload.destination = selectedItem.id;
        this.dialogService.confirmation(this.translateService.translateInstant('action.move.confirmation'), null,
          this.getConfirmationConfiguration(actionContext, actionPayload));
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
  handleButtonClick(buttonID, dialogConfig, context, actionPayload) {
    if (buttonID === DialogService.YES) {
      this.actionsService.move(context.currentObject.getId(), actionPayload, {
        skipInterceptor: true
      }).then((response) => {
        if (context.idocContext) {
          this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, response.data.id);
          this.refreshInstance(response.data, context);
        }
        this.notificationService.success(this.translateService.translateInstant('action.move.completed'));
      }).catch((error) => {
        if (error.status === StatusCodes.NOT_ALLOWED) {
          this.notificationService.error(this.translateService.translateInstant('action.move.not.allowed.error'));
        } else {
          this.notificationService.error(this.translateService.translateInstant('action.move.error'));
        }
      });
    }
    dialogConfig.dismiss();
  }

  /**
   * Get a confirmation dialog configuration based on the context and action payload.
   * @param actionContext the context
   * @param actionPayload the action payload
   * @returns {*} a configuration containing the buttons and button click handler
   */
  getConfirmationConfiguration(actionContext, actionPayload) {
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
        actionContext, actionPayload)
    };
  }
}