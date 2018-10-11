import {Injectable, Inject} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {Logger} from 'services/logging/logger';
import {InstanceAction} from 'idoc/actions/instance-action';
import {AuthenticationService} from 'services/security/authentication-service';

/**
 * Action handler for lock operation
 */
@Injectable()
@Inject(ActionsService, Logger)
export class LockAction extends InstanceAction {

  constructor(actionsService, logger) {
    super(logger);
    this.actionsService = actionsService;
  }

  execute(action, context) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.lock(currentObjectId, this.buildActionPayload(action, context.currentObject, action.action)).finally(() => {
      this.refreshInstance({id: currentObjectId}, context);
      this.checkPermissionsForEditAction(context);
    });
  }
}