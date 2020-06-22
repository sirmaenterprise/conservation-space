import {Component, View, Inject} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {CloneAction} from 'idoc/actions/clone-action';
import {InstanceRestService} from 'services/rest/instance-service';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';


import template from './clone-action-bootstrap.html!text';

@Component({
  selector: 'clone-action-bootstrap'

})
@View({
  template
})

@Inject(ActionExecutor, CloneAction, InstanceRestService, IdocContextFactory)
export class CloneActionBootstrap {

  constructor(actionExecutor, cloneAction, instanceRestService, idocContextFactory) {
    this.actionExecutor = actionExecutor;
    this.cloneAction = cloneAction;
    this.instanceRestService = instanceRestService;
    this.idocContextFactory = idocContextFactory;
    this.cloneAction.extensionPoint = 'actions';
    this.cloneAction.name = 'cloneAction';
    this.registerTestInstances();
  }

  registerTestInstances() {
    this.registerInstance('emf:0', 'CA_0');
    this.registerInstance('emf:1', 'CA_1');
    this.registerInstance('emf:2', 'CA_2', '2');
    this.registerInstance('emf:3', 'CA_3');
  }

  registerInstance(instanceId, definitionId, parentId) {
    let object = CloneActionBootstrap.initObject(instanceId, definitionId, parentId);
    object = parentId ? CloneActionBootstrap.addParent(object, parentId) : object;
    this.instanceRestService.update(instanceId, object);
  }

  executeAction(instanceId) {
    let currentContext = this.idocContextFactory.getCurrentContext(instanceId);
    currentContext.getCurrentObject().then((instanceObject) => {
      let actionContext = {
        placeholder: 'idoc.actions.menu',
        idocContext: currentContext,
        currentObject: instanceObject
      };
      this.actionExecutor.execute(this.cloneAction, actionContext);
    });
  }

  static addParent(object, parentId) {
    object.parentId = parentId;
    object.properties['hasParent'] = {
      results: [parentId]
    };
    return object;
  }

  static initObject(id, definitionId) {
    return {
      definitionId,
      id,
      properties: {
        'title': 'Title',
        'rdf:type': {
          results: ['emf:Document']
        }
      }
    };
  }
}