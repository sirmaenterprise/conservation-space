import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {ExportHandler} from 'idoc/actions/export-handler';
import {PrintService} from 'services/print/print-service';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AuthenticationService} from 'security/authentication-service';
import {UrlUtils} from 'common/url-utils';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {MODE_PRINT} from 'idoc/idoc-constants';

export const NOTIFICATION_LABEL_ID = 'action.print.idoc.notification';

@Injectable()
@Inject(Logger, PrintService, ActionsService, TranslateService, NotificationService, AuthenticationService, PromiseAdapter)
export class PrintAction extends ExportHandler {

  constructor(logger, printService, actionsService, translateService, notificationService, authenticationService, promiseAdapter) {
    super(logger, actionsService, translateService, notificationService, authenticationService);
    this.printService = printService;
    this.promiseAdapter = promiseAdapter;
  }

  afterExportHandler(result) {
  }

  getNotificationLabelId() {
    return NOTIFICATION_LABEL_ID;
  }

  execute(action, context) {
    let idocPrintUrl = UrlUtils.buildIdocUrl(context.currentObject.getId(), null, {
      'mode': MODE_PRINT
    });
    this.printService.print(idocPrintUrl);
    return this.promiseAdapter.resolve();
  }
}