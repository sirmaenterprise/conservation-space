import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {ActionsService} from 'services/rest/actions-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';

const OPERATION = 'activateTemplate';

@Injectable()
@Inject(Logger, ActionsService, NotificationService, TranslateService)
export class ActivateTemplateAction extends InstanceAction {

  constructor(logger, actionsService, notificationService, translateService) {
    super(logger);
    this.actionService = actionsService;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  execute(actionDefinition, context) {
    let payload = {
      operation: OPERATION,
      userOperation: actionDefinition.action
    };

    return this.actionService.activateTemplate(context.currentObject.getId(), payload).then(() => {
      let message = this.translateService.translateInstant("template.activation.success");
      this.notificationService.success(message);
      return this.refreshInstance(context.currentObject, context);
    }).catch(() => {
      let message = this.translateService.translateInstant("template.activation.error");
      this.notificationService.error(message);
    });
  }

}