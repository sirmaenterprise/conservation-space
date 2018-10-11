import {Component, View, Inject, NgScope} from 'app/app';
import {TemplateRuleEditorService} from 'idoc/template/rules/template-rule-edit-service';

import template from './template-rule-edit-bootstrap.html!text';

@Component({
  selector: 'template-rule-edit-bootstrap'
})
@View({
  template: template
})
@Inject(NgScope, TemplateRuleEditorService)
export class TemplateRuleEditBootstrap {

  constructor($scope, templateRuleEditorService) {
    this.$scope = $scope;
    this.templateRuleEditorService = templateRuleEditorService;
  }

  editNewRule() {
    this.templateRuleEditorService.openRuleEditor(createTemplate('emf:template', 'TEMPLATE_RULE_DEF', undefined, 'creatable', false), this.$scope);
  }

  editExistingRule() {
    this.templateRuleEditorService.openRuleEditor(createTemplate('emf:template', 'TEMPLATE_RULE_DEF','primary == true && department == "ENG" || department == "INF"', 'creatable', false), this.$scope);
  }

  editRuleForDefinitionWithNoEligibleFields() {
    this.templateRuleEditorService.openRuleEditor(createTemplate('emf:template', 'tag', undefined, 'creatable', true), this.$scope);
  }

  editExistingRuleForPrimaryAndSecondary() {
    this.templateRuleEditorService.openRuleEditor(createTemplate('emf:template', 'TEMPLATE_RULE_DEF','primary == true && department == "ENG" || department == "INF"', 'creatable', true), this.$scope);
  }

}

function createTemplate(id, forObjectType, rules, purpose, primary) {
  return {
    instanceId: id,
    forObjectType: forObjectType,
    rules: rules,
    purpose: purpose,
    primary: primary
  };
}