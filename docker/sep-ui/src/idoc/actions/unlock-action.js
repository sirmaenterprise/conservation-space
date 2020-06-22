import {Injectable, Inject} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {Logger} from 'services/logging/logger';
import {InstanceAction} from 'idoc/actions/instance-action';

/**
 * Action handler for unlock operation
 */
@Injectable()
@Inject(ActionsService, Logger)
export class UnlockAction extends InstanceAction {

  constructor(actionsService, logger) {
    super(logger);
    this.actionsService = actionsService;
  }

  execute(action, context) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.unlock(currentObjectId, this.buildActionPayload(action, context.currentObject, action.action)).then(() => {
      this.refreshInstance({id: currentObjectId}, context);
      this.checkPermissionsForEditAction(context);
    });
  }
}