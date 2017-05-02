import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {ExportHandler} from 'idoc/actions/export-handler';
import {PrintService} from 'services/print/print-service';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {UrlUtils} from 'common/url-utils';
import {MODE_PRINT} from 'idoc/idoc-constants';
import $ from 'jquery';

export const NOTIFICATION_LABEL_ID = 'action.print.idoc.notification';

@Injectable()
@Inject(Logger, PrintService, ActionsService, TranslateService, NotificationService, AuthenticationService)
export class PrintTabAction extends ExportHandler {

  constructor(logger, printService, actionsService, translateService, notificationService, authenticationService) {
    super(logger, actionsService, translateService, notificationService, authenticationService);
    this.printService = printService;
  }

  afterExportHandler(result) {
  }

  getNotificationLabelId() {
    return NOTIFICATION_LABEL_ID;
  }

  execute(action, context) {
    let idocPrintUrl = UrlUtils.buildIdocUrl(context.currentObject.getId(), this.getActiveTabId(), {
      'mode': MODE_PRINT
    });
    return this.printService.print(idocPrintUrl);
  }
}