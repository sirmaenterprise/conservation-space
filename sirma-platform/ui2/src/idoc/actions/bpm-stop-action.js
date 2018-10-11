import {Injectable, Inject} from 'app/app';
import {BpmTransitionAction} from 'idoc/actions/bpm-action';
import {TransitionAction} from 'idoc/actions/transition-action';
import {BpmService} from 'services/rest/bpm-service';
import {Logger} from 'services/logging/logger';
import {ActionsService} from 'services/rest/actions-service';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';

const OPERATION = 'bpmStop';

/**
 * Action handler for BpmStart.
 */
@Injectable()
@Inject(ActionsService, ValidationService, SaveDialogService, InstanceRestService, Eventbus, Logger, PromiseAdapter, BpmService, TranslateService, NotificationService)
export class BpmStopAction extends BpmTransitionAction {

  constructor(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService) {
    super(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
  }

  /**
   * Executes the bpmTransition.
   *
   * @param context The action context
   * @param actionDefinition the action definition
   * @param models Models for the object for which this transition should be performed.
   */
  executeTransition(context, actionDefinition, models) {
    let currentObjectId = context.currentObject.getId();
    let actionPayload = this.bpmService.buildBPMActionPayload(currentObjectId, actionDefinition, models, OPERATION);
    return this.bpmService.stopBpm(currentObjectId, actionPayload).then((response) => {
        this.notifyOnUpdate(actionDefinition, response);
        return response;
    });
  }

}
