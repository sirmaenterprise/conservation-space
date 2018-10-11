import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {ExportHandler, EXPORT_RESTRICT_NOTIFICATION} from 'idoc/actions/export-handler';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

export const NOTIFICATION_LABEL_ID = 'action.export.word.idoc.notification';

@Injectable()
@Inject(Logger, ActionsService, TranslateService, NotificationService, AuthenticationService, InstanceRestService, PromiseAdapter)
export class ExportTabWordAction extends ExportHandler {

  constructor(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, promiseAdapter) {
    super(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, promiseAdapter);
  }

  afterExportHandler(response) {
    this.downloadFile(response);
  }

  getNotificationLabelId() {
    return NOTIFICATION_LABEL_ID;
  }

  execute(action, context) {
    if (this.hasSystemContent(context)) {
      this.notificationService.info(this.translateService.translateInstant(EXPORT_RESTRICT_NOTIFICATION));
      return;
    }
    context.activeTabId = this.getActiveTabId();
    super.execute(action, context);
    return this.exportWord(action, context);
  }
}