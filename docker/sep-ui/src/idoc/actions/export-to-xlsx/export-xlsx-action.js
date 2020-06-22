import { Injectable, Inject } from 'app/app';
import { ExportService } from 'services/rest/export-service';
import { TranslateService } from 'services/i18n/translate-service';
import { NotificationService } from 'services/notification/notification-service';
import { ExportHandler } from 'idoc/actions/export-handler';
import { SearchResolverService } from 'services/resolver/search-resolver-service';
import { ActionsService } from 'services/rest/actions-service';
import { AuthenticationService } from 'security/authentication-service';
import _ from 'lodash';

export const NOTIFICATION_LABEL_ID = 'action.export.xlsx.idoc.notification';

@Injectable()
@Inject(ExportService, TranslateService, NotificationService, SearchResolverService, AuthenticationService, ActionsService)
export class ExportXlsxAction extends ExportHandler {

  constructor(exportService, translateService, notificationService, searchResolverService, authenticationService, actionsService) {
    super(translateService, notificationService, authenticationService, actionsService);
    this.exportService = exportService;
    this.translateService = translateService;
    this.searchResolverService = searchResolverService;
    this.authenticationService = authenticationService;
    this.notificationService = notificationService;
  }

  execute(item, data) {
    let notification = this.translateService.translateInstant(this.getNotificationLabelId());
    this.notificationService.info(notification);
    this.prepareData(data);
    let criteria = _.cloneDeep(data.config.criteria);
    return this.searchResolverService.resolve(criteria, data.context).then((resolver) => {
      this.criteria = resolver[0];
      this.dataForExport.criteria = this.criteria;
      return this.exportService.exportXlsx(this.dataForExport).then((response) => {
        this.afterExportHandler(response);
      });
    });
  }

  prepareData(data) {
    this.dataForExport = {
      selectedObjects: data.config.selectedObjects,
      selectedProperties: data.config.selectedProperties,
      instanceHeaderType: data.config.instanceHeaderType,
      filename: data.config.title,
      orderBy: data.config.orderBy || '',
      orderDirection: data.config.orderDirection || '',
      selectObjectMode: data.config.selectObjectMode,
      selectedHeaders: data.config.columnHeaders,
      //In future this have to be configured by widget configuration.
      showInstanceId: true
    };
    if (data.context.instanceId) {
      this.dataForExport.instanceId = data.context.instanceId;
    } else {
      this.dataForExport.instanceId = data.context.getCurrentObjectId();
    }
    if (!this.dataForExport.filename) {
      this.dataForExport.filename = 'Workbook';
    }else{
      this.dataForExport.filename = this.dataForExport.filename.replace(/[`~!@#$%^&*()|+\=?;:'",.<>\{\}\[\]\\\/]/gi, '-');
    }
  }

  afterExportHandler(response) {
    this.downloadFile(response);
  }

  getNotificationLabelId() {
    return NOTIFICATION_LABEL_ID;
  }
}
