import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {ExportHandler} from 'idoc/actions/export-handler';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

export const NOTIFICATION_LABEL_ID = 'action.export.pdf.idoc.notification';

@Injectable()
@Inject(Logger, ActionsService, TranslateService, NotificationService, AuthenticationService, InstanceRestService, PromiseAdapter)
export class ExportPDFAction extends ExportHandler {

  constructor(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, promiseAdapter) {
    super(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, promiseAdapter);
  }

  afterExportHandler(response) {
    this.downloadBlob(response, `${response.config.data['fileName']}.pdf`);
  }

  getNotificationLabelId() {
    return NOTIFICATION_LABEL_ID;
  }

  execute(action, context) {
    delete context.activeTabId;
    super.execute(action, context);
    return this.exportPDF(action, context);
  }
}