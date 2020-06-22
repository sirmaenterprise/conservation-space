import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(Logger, TemplateService, NotificationService, TranslateService, PromiseAdapter)
export class SetTemplateAsPrimaryAction extends InstanceAction {

  constructor(logger, templateService, notificationService, translateService, promiseAdapter) {
    super(logger);
    this.templateService = templateService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition, context) {
    return this.templateService.setTemplateAsPrimary(context.currentObject.getId()).then(() => {
      let message = this.translateService.translateInstant('idoc.template.set_as_primary.success');
      this.notificationService.success(message);
      if (context.idocContext) {
        return this.refreshInstance(context.currentObject, context);
      }

      return this.promiseAdapter.resolve();
    });
  }

}