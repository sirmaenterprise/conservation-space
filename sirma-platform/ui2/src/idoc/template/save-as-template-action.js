import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {TemplateConfigDialogService} from 'idoc/template/template-config-dialog-service';
import {TemplateService} from 'services/rest/template-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {StatusCodes} from 'services/rest/status-codes';
import {UrlUtils} from 'common/url-utils';

/**
 * Action handler for creating a template out of current iDoc content.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(TemplateConfigDialogService, TemplateService, TranslateService, NotificationService)
export class SaveAsTemplateAction extends ActionHandler {

  constructor(templateConfigDialogService, templateService, translateService, notificationService) {
    super();
    this.templateConfigDialogService = templateConfigDialogService;
    this.templateService = templateService;
    this.translateService = translateService;
    this.notificationService = notificationService;
  }

  execute(action, actionContext) {
    return this.templateConfigDialogService.openDialog(actionContext.scope, actionContext.currentObject, undefined, false, TemplateConfigDialogService.SAVE_AS_TEMPLATE_KEY).then((templateData) => {

      this.templateService.create(templateData).then((response) => {
        var templateInstanceId = response.data;
        var templateLink = `<a href="${UrlUtils.buildIdocUrl(templateInstanceId)}">${templateData.title}</a>`;
        this.notificationService.success({
          opts: {
            closeButton: true,
            hideOnHover: false
          },
          message: templateLink + this.translateService.translateInstant('idoc.template.status.success')
        });
      });
    });
  }
}