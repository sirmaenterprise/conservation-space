import {Component, View, Inject, NgScope} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {InstanceObject} from 'models/instance-object';
import {SaveAsTemplateAction} from 'idoc/template/save-as-template-action';

import template from './save-as-template-action-bootstrap.html!text';

@Component({
  selector: 'save-as-template-action-bootstrap'
})
@View({
  template: template
})
@Inject(NgScope, ActionExecutor, SaveAsTemplateAction)
export class SaveAsTemplateActionBootstrap {

  constructor($scope, actionExecutor, saveAsTemplateAction) {
    this.$scope = $scope;
    this.actionExecutor = actionExecutor;
    this.saveAsTemplateAction = saveAsTemplateAction;
  }

  openDialog() {
    var actionContext = {
      scope: this.$scope,
      currentObject: this.getCurrentObjectMock()
    };
    this.actionExecutor.execute(this.saveAsTemplateAction, actionContext);
  }

  getCurrentObjectMock() {
    var object = new InstanceObject('instanceId');
    object.models = {
      definitionId: 'OT210027'
    };
    return object;
  }
}