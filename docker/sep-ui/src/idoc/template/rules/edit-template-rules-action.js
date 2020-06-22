import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {Logger} from 'services/logging/logger';
import {TemplateRuleEditorService} from 'idoc/template/rules/template-rule-edit-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {TEMPLATE_RULE_PROPERTY, FOR_OBJECT_TYPE, TEMPLATE_PURPOSE, IS_PRIMARY_TEMPLATE} from '../template-constants';

@Injectable()
@Inject(Logger, TemplateRuleEditorService, InstanceRestService)
export class EditTemplateRuleAction extends InstanceAction {

  constructor(logger, templateRuleEditorService, instanceRestService) {
    super(logger);
    this.templateRuleEditorService = templateRuleEditorService;
    this.instanceRestService = instanceRestService;
  }

  execute(actionDefinition, context) {
    let currentObject = context.currentObject;

    let template = this.getTemplateData(currentObject);

    if (!template.forObjectType) {
      // happens when the instance is partially loaded
      return this.instanceRestService.loadInstanceObject(currentObject.getId()).then(fullyLoadedInstanceObject => {
        template = this.getTemplateData(fullyLoadedInstanceObject);
        return this.openEditor(currentObject, template, context);
      });
    }

    return this.openEditor(currentObject, template, context);
  }

  openEditor(currentObject, template, context) {
    return this.templateRuleEditorService.openRuleEditor(template, context.scope).then(() => {
      if (context.idocContext) {
        return this.refreshInstance(currentObject, context);
      }
    });
  }

  getTemplateData(instanceObject) {
    return {
      instanceId: instanceObject.getId(),
      forObjectType: instanceObject.getPropertyValue(FOR_OBJECT_TYPE),
      rules: instanceObject.getPropertyValue(TEMPLATE_RULE_PROPERTY),
      purpose: instanceObject.getPropertyValue(TEMPLATE_PURPOSE),
      primary: instanceObject.getPropertyValue(IS_PRIMARY_TEMPLATE)
    };
  }

}