import {Component, View, Inject, NgScope} from 'app/app';
import {InstanceObject} from 'idoc/idoc-context';
import {AddRelationAction} from 'idoc/actions/add-relation-action';
import {ActionExecutor} from 'services/actions/action-executor';
import config from 'sandbox/idoc/dialogs/add-relation/add-relation-config.json!';
import _ from 'lodash';
import addRelationTemplate from 'add-relation-stub-template!text';

const ID = '1';

@Component({
  selector: 'add-relation-stub'
})
@View({
  template: addRelationTemplate
})
@Inject(ActionExecutor, AddRelationAction, NgScope)
export class AddRelationStub {
  constructor(actionExecutor, addRelationAction, $scope) {
    this.config = config;
    this.$scope = $scope;
    this.addRelationAction = addRelationAction;
    this.actionExecutor = actionExecutor;
  }

  openDialog() {
    _.defaultsDeep(this.addRelationAction, this.config);
    this.actionExecutor.execute(this.addRelationAction, {
      scope: this.$scope,
      currentObject: new InstanceObject(ID)
    });
  }
}