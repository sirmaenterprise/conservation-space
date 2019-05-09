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
export const PRINT_RESTRICT_NOTIFICATION = 'action.print.idoc.restricted.notification';

@Injectable()
@Inject(Logger, PrintService, ActionsService, TranslateService, NotificationService, AuthenticationService, PromiseAdapter)
export class PrintTabAction extends ExportHandler {

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
    return this.promiseAdapter.promise((resolve, reject) => {
      if (this.hasSystemContent(context)) {
        this.notificationService.info(this.translateService.translateInstant(PRINT_RESTRICT_NOTIFICATION));
        reject();
        return;
      }
      resolve();
      return this.printService.print(UrlUtils.buildIdocUrl(context.currentObject.getId(), this.getActiveTabId(), {
        'mode': MODE_PRINT
      }));
    });
  }
}
