import {Component, View, Inject, NgScope} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {InstanceObject} from 'models/instance-object';
import {AddIconsAction} from 'idoc/actions/add-icons-action';
import template from './class-icons-upload-bootstrap-template.html!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'class-icons-upload-bootstrap'
})
@View({
  template: template
})
@Inject(NgScope, ActionExecutor, AddIconsAction)
export class ClassIconsUploadStub {

  constructor($scope, actionExecutor, addIconsAction) {
    this.$scope = $scope;
    this.actionExecutor = actionExecutor;
    this.addIconsAction = addIconsAction;
    this.addIconsAction.label = 'Add Icons';
  }

  openDialog() {
    var actionContext = {
      scope: this.$scope,
      currentObject: this.getCurrentObjectMock()
    };
    this.actionExecutor.execute(this.addIconsAction, actionContext);
  }

  getCurrentObjectMock() {
    var object = new InstanceObject('id');
    object.models = {
      definitionId: 'emf:SubType1'
    };
    object.headers = {};
    return object;
  }
}