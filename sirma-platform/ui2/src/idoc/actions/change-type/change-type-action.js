import {Injectable, Inject} from 'app/app';
import {ChangeTypeDialogService} from 'idoc/actions/change-type/change-type-dialog-service';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceObject} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {RefreshWidgetsCommand} from 'idoc/actions/events/refresh-widgets-command';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelsService} from 'services/rest/models-service';
import {RDF_TYPE} from 'instance/instance-properties';
import {IDOC_STATE} from 'idoc/idoc-constants';
import {EventEmitter} from 'common/event-emitter';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';

/**
 * Action which enables object type to be changed from one to another following the class definition hierarchy or any
 * other custom defined constraints. The action opens pre-configured ChangeTypeDialog providing a custom instance loading
 * callback function decorating the default behavior of the InstanceCreateConfiguration component.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ChangeTypeDialogService, InstanceRestService, Eventbus, NamespaceService, PromiseAdapter, ModelsService, ActionsService, Router, StateParamsAdapter)
export class ChangeTypeAction {

  constructor(changeTypeDialogService, instanceRestService, eventbus, namespaceService, promiseAdapter, modelsService, actionsService, router, stateParamsAdapter) {
    this.instanceRestService = instanceRestService;
    this.changeTypeDialogService = changeTypeDialogService;
    this.namespaceService = namespaceService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
    this.modelsService = modelsService;
    this.actionsService = actionsService;
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
  }

  execute(action, actionContext) {
    const instanceId = actionContext.currentObject.getId();
    let instanceResponse;
    return this.instanceRestService.load(instanceId).then((instance) => {
      instanceResponse = instance;
      return this.modelsService.getExistingInContextInfo(instanceResponse.definitionId);
    }).then((existingInContext) => {
      let parentId = instanceResponse.parentId;
      let purpose = instanceResponse.getPurpose();
      let definitionId = instanceResponse.definitionId;
      let rdfTypeProperty = instanceResponse.getPropertyValue(RDF_TYPE);
      let rdfType = rdfTypeProperty && rdfTypeProperty.getFirstValue();

      return this.executeAction(parentId, instanceId, rdfType, definitionId, existingInContext, purpose, action);
    });
  }

  executeAction(parentId, instanceId, rdfType, definitionId, existingInContext, purpose, actionDefinition) {
    let eventEmitter = new EventEmitter();

    let config = {
      parentId,
      instanceId,
      purpose,
      eventEmitter,
      existingInContext,
      instanceType: null,
      instanceSubType: definitionId,
      contextSelectorSelectionMode: existingInContext,
      onChangeType: (models) => {
        return this.changeType(instanceId, models, actionDefinition);
      }
    };

    return this.namespaceService.toFullURI([rdfType]).then(({data: fullUrisMap}) => {
      config.instanceType = fullUrisMap[rdfType];
      return this.changeTypeDialogService.openDialog(config);
    });
  }

  changeType(instanceId, models, actionDefinition) {
    let instanceObject = new InstanceObject(instanceId, models);
    let data = {
      id: instanceId,
      definitionId: instanceObject.getModels().definitionId,
      parentId: instanceObject.getModels().parentId || null,
      properties: instanceObject.getChangeset(false)
    };

    this.eventbus.publish(new BeforeIdocSaveEvent(data));

    return this.actionsService.changeType(instanceId, data).then((instanceResponse) => {
      this.eventbus.publish(new InstanceCreatedEvent({
        currentObject: instanceResponse.data
      }));

      // when current object is opened, then reload view in order to get the actual data and template
      if (this.isCurrentObjectDashboard(instanceId)) {
        this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {reload: true});
      } else {
        this.eventbus.publish(new InstanceRefreshEvent({
          response: instanceResponse
        }));
        // when operation is executed from a widget, I need to refresh all visible widgets, because
        // there is a chance that the object with its new type should appear in another widget
        this.eventbus.publish(new RefreshWidgetsCommand());
      }
      return instanceResponse;
    });
  }

  isCurrentObjectDashboard(instanceId) {
    return this.router.getCurrentState() === IDOC_STATE && this.router.getCurrentParams().id === instanceId;
  }
}