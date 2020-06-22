import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {Logger} from 'services/logging/logger';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import 'jquery-file-download';


@Injectable()
@Inject(ActionsService, Logger, NotificationService, TranslateService)
export class EditOfflineAction extends InstanceAction {

  constructor(actionsService, logger, notificationService, translateService) {
    super(logger);
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  execute(action, context) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.lock(currentObjectId, this.buildActionPayload(action, context.currentObject, action.action)).finally(() => {
      this.refreshInstance({id: currentObjectId}, context);
      this.checkPermissionsForEditAction(context);

      this.actionsService.downloadForEditOffline(currentObjectId).then(url => {
        $.fileDownload(url, {httpMethod: 'POST'})
          .done(() => {
            this.notificationService.success(this.translateService.translateInstant('download.content.success'));
          })
          .fail((response) => {
            this.notificationService.error(this.translateService.translateInstant('download.content.failure') + response);
            return this.actionsService.unlock(currentObjectId, this.buildActionPayload(action, context.currentObject, action.action)).then(() => {
              this.refreshInstance({id: currentObjectId}, context);
              this.checkPermissionsForEditAction(context);
            });
          });
      });
    });
  }
}
