/**
 * Created by tdossev on 19.1.2017 г..
 */
import {Injectable, Inject} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {Logger} from 'services/logging/logger';
import {InstanceAction} from 'idoc/actions/instance-action';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';

/**
 * Action handler for publish operation
 */
@Injectable()
@Inject(ActionsService, Logger, NotificationService, TranslateService)
export class PublishAction extends InstanceAction {

  constructor(actionsService, logger, notificationService, translateService) {
    super(logger);
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  execute(action, context) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.publish(currentObjectId, this.buildActionPayload(action, context.currentObject, action.action)).then((response) => {
      let instance = response.data.headers.compact_header;
      this.checkPermissionsForEditAction(context);
      this.notificationService.success(this.translateService.translateInstant('action.create.revision') + ` ${instance}`);
    });
  }
}