import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {TemplateConfigDialogService} from 'idoc/template/template-config-dialog-service';
import {TemplateService} from 'services/rest/template-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {UrlUtils} from 'common/url-utils';

@Injectable()
@Inject(TemplateConfigDialogService, TemplateService, TranslateService, NotificationService, InstanceRestService)
export class CloneTemplateAction extends ActionHandler {

  constructor(templateConfigDialogService, templateService, translateService, notificationService, instanceRestService) {
    super();
    this.templateConfigDialogService = templateConfigDialogService;
    this.templateService = templateService;
    this.translateService = translateService;
    this.notificationService = notificationService;
    this.instanceRestService = instanceRestService;
  }

  execute(action, actionContext) {
    let templateDialogData;

    return this.instanceRestService.load(actionContext.currentObject.getId())
      .then(data => {
        let fullyLoadedObject = data.data;
        return this.templateConfigDialogService.openDialog(actionContext.scope, fullyLoadedObject, undefined,
          true, TemplateConfigDialogService.CLONE_TEMPLATE_KEY);
      })
      .then(templateData => {
        templateDialogData = templateData;
        return this.templateService.create(templateData);
      })
      .then(response => {
        let templateInstanceId = response.data;
        let templateLink = `<a href="${UrlUtils.buildIdocUrl(templateInstanceId)}">${templateDialogData.title}</a>`;
        this.notificationService.success({
          opts: {
            closeButton: true,
            hideOnHover: false
          },
          message: this.translateService.translateInstant('idoc.template.clone.success') + ' ' + templateLink
        });
      });
  }
}