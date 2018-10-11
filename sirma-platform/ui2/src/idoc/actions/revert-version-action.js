import {Injectable, Inject} from "app/app";
import {InstanceAction} from "idoc/actions/instance-action";
import {Logger} from "services/logging/logger";
import {ActionsService} from "services/rest/actions-service";
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from "adapters/router/state-params-adapter";
import {STATE_PARAM_ID, IDOC_STATE} from 'idoc/idoc-constants';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';

/**
 * Handles action execution for revert operation on version instances. This action will replace the data of the current
 * object with the data from the version.
 * Note that not all properties are replaced with this from the version. All object and some system properties will
 * remain the same as they were in the current object.
 * If the action is executed successfully it will redirect to the current object page.
 * The current object, which data will be replaced will be locked while this action is executed.
 *
 * @author A. Kunchev
 */
@Injectable()
@Inject(Logger, ActionsService, Router, StateParamsAdapter, NotificationService, TranslateService)
export class RevertVersionAction extends InstanceAction {

  constructor(logger, actionService, router, stateParamsAdapter, notificationService, translateService) {
    super(logger);
    this.actionService = actionService;
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  execute(actionDefinition, actionContext) {
    return this.actionService.revertVersion(actionContext.currentObject.getId(), {userOperation: actionDefinition.action})
      .then(response => {
        this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, response.data.id);
        this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {reload: true});
        let messageToShow = this.translateService.translateInstant('action.notification.success') + actionDefinition.label;
        this.notificationService.success(messageToShow);
      });
  }

}