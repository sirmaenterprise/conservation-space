import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {TEMPLATE_DEFINITION_TYPE} from './template-constants';
import {TemplateConfigDialogService} from './template-config-dialog-service';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {Router} from 'adapters/router/router';
import {WindowAdapter} from 'adapters/angular/window-adapter';

/**
 * Handles "create new template" action. Opens a dialog for entering template metadata and then redirects to
 * a blank idoc view for creating the template.
 *
 * The implementation assumes that the action can be executed only on a library object.
 */
@Injectable()
@Inject(TemplateConfigDialogService, DefinitionService, InstanceRestService, SessionStorageService, Router, PromiseAdapter, WindowAdapter)
export class CreateTemplateAction extends ActionHandler {

  constructor(templateConfigDialogService, definitionService, instanceRestService, sessionStorageService, router, promiseAdapter, windowAdapter) {
    super();
    this.templateConfigDialogService = templateConfigDialogService;
    this.definitionService = definitionService;
    this.instanceRestService = instanceRestService;
    this.sessionStorageService = sessionStorageService;
    this.router = router;
    this.promiseAdapter = promiseAdapter;
    this.windowAdapter = windowAdapter;
  }

  execute(action, actionContext) {
    return this.templateConfigDialogService.openDialog(actionContext.scope, null, actionContext.currentObject.getId()).then((templateData) => {

      return this.promiseAdapter.all([
        this.definitionService.getDefinitions(TEMPLATE_DEFINITION_TYPE),
        this.instanceRestService.loadDefaults(TEMPLATE_DEFINITION_TYPE)
      ]).then(result => {
        let definitionData = result[0].data[TEMPLATE_DEFINITION_TYPE];
        let defaults = result[1].data;

        var models = {};

        let intermediateObject = new InstanceObject(TEMPLATE_DEFINITION_TYPE, definitionData);
        intermediateObject.mergePropertiesIntoModel(defaults.properties);

        this.copyTemplateDataToModel(intermediateObject, templateData);

        models.headers = defaults.headers;
        models.instanceType = defaults.instanceType;
        models.definitionId = TEMPLATE_DEFINITION_TYPE;
        models.definitionLabel = definitionData.definitionLabel;
        models.instanceId = defaults.id;

        models.validationModel = intermediateObject.getModels().validationModel.serialize();
        models.viewModel = intermediateObject.getModels().viewModel.serialize();

        models.parentId = actionContext.currentObject.getId();

        models.returnUrl = this.windowAdapter.location.href;

        this.sessionStorageService.set('models', models);
        this.router.navigate('idoc', {mode: 'edit'}, {reload: true, inherit: false});
      });
    });
  }

  copyTemplateDataToModel(instanceObject, templateData) {
    instanceObject.setPropertiesValue({
      'forObjectType': templateData.forType,
      'title': templateData.title,
      'templatePurpose': templateData.purpose,
      'isPrimaryTemplate': templateData.primary
    });
  }
}