import {Injectable, Inject} from 'app/app';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import {InstanceVersionComparator} from 'instance/instance-version-comparator';
import {EMF_HAS_TEMPLATE, EMF_TEMPLATE_VERSION} from 'instance/instance-properties';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {STATE_PARAM_MODE, STATE_PARAM_ID, MODE_PREVIEW, IDOC_STATE} from 'idoc/idoc-constants';
import {StatusCodes} from 'services/rest/status-codes';
import {TEMPLATE_ID, TEMPLATE_VERSION} from 'idoc/template/template-constants';

@Injectable()
@Inject(TemplateService, NotificationService, TranslateService, PromiseAdapter, Router, StateParamsAdapter, InstanceRestService)
export class UpdateTemplateAction {

  constructor(templateService, notificationService, translateService, promiseAdapter, router, stateParamsAdapter, instanceRestService) {
    this.templateService = templateService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
    this.instanceRestService = instanceRestService;
  }

  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      if (context.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
        let templateId = context.currentObject.getPropertyValue(TEMPLATE_ID);
        let templateVersion = context.currentObject.getPropertyValue(TEMPLATE_VERSION);
        this.processTemplateIdAndVersion(templateId, templateVersion, context, resolve, reject);
      } else {
        let config = {
          params: {
            properties: [EMF_HAS_TEMPLATE, EMF_TEMPLATE_VERSION]
          }
        };
        this.instanceRestService.loadBatch([context.currentObject.getId()], config).then((result) => {
          let templateId = result.data[0].properties[EMF_HAS_TEMPLATE].results[0];
          let templateVersion = result.data[0].properties[EMF_TEMPLATE_VERSION];
          this.processTemplateIdAndVersion(templateId, templateVersion, context, resolve, reject);
        });
      }
    });
  }

  processTemplateIdAndVersion(templateId, templateVersion, context, resolve, reject) {
    // Here we check for template Id, as it can not be processed without one.
    if (templateId) {
      if (templateVersion) {
        this.checkIsNewerTemplateVersion(templateVersion, context, resolve, reject);
      } else {
        this.updateInstanceTemplate(context, resolve, reject);
      }
    } else {
      this.showTemplateUpdateNotificationWarning();
      reject();
    }
  }

  updateInstanceTemplate(context, resolve, reject) {
    return this.templateService.updateSingleInstanceTemplate(context.currentObject.getId()).then((result) => {
      if (result.status === StatusCodes.NO_CONTENT) {
        this.showTemplateUpdateNotificationWarning();
        reject();
      } else {
        // If action is executed from idoc page action menu, then we should reload the idoc to show new idoc state
        if (context.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
          this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, MODE_PREVIEW);
          this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, context.currentObject.getId());
          this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {reload: true});
        }
        let message = this.translateService.translateInstant('idoc.template.instance.single.update.success');
        this.notificationService.success(message);
        resolve();
      }
    });
  }

  checkIsNewerTemplateVersion(templateVersion, context, resolve, reject) {
    this.templateService.getActualTemplateVersion(context.currentObject.getId()).then((result) => {
      if (InstanceVersionComparator.compare(result.data, templateVersion) === 1) {
        this.updateInstanceTemplate(context, resolve, reject);
      } else {
        this.showTemplateUpdateNotificationWarning();
        reject();
      }
    });
  }

  showTemplateUpdateNotificationWarning() {
    let message = this.translateService.translateInstant('idoc.template.instance.single.update.failure');
    this.notificationService.warning(message);
  }
}