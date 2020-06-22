import {Injectable, Inject} from 'app/app';
import {CreatePanelService} from 'services/create/create-panel-service';
import {InstanceObject} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelsService} from 'services/rest/models-service';
import {RDF_TYPE} from 'instance/instance-properties';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {
  ADD_CONTEXT_ERROR_MESSAGE_COMMAND,
  CONTEXT_CHANGED_EVENT
} from 'components/contextselector/context-selector';
import {EventEmitter} from 'common/event-emitter';

@Injectable()
@Inject(CreatePanelService, InstanceRestService, Eventbus, NamespaceService, PromiseAdapter, ModelsService)
export class CloneAction {

  constructor(createPanelService, instanceRestService, eventbus, namespaceService, promiseAdapter, modelsService) {
    this.instanceRestService = instanceRestService;
    this.createPanelService = createPanelService;
    this.namespaceService = namespaceService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
    this.modelsService = modelsService;
  }

  execute(action, actionContext) {
    const originalInstanceId = actionContext.currentObject.getId();

    let requests = [
      // Original instance is needed so we could load the parent ID (if any)
      this.instanceRestService.load(originalInstanceId),
      // Getting cloned instance properties
      this.instanceRestService.cloneProperties(originalInstanceId)
    ];

    return this.promiseAdapter.promise((resolve, reject) => {
      let instanceData;
      let clonedProperties;
      this.promiseAdapter.all(requests).then(([{data: instance}, {data: instanceProperties}]) => {
        instanceData = instance;
        clonedProperties = instanceProperties;
        return this.modelsService.getExistingInContextInfo(instanceData.definitionId);
      }).then((existingInContext) => {
        let parentId = this.getParentId(instanceData);
        let rdfType = clonedProperties.properties[RDF_TYPE].results[0];
        this.executeAction(parentId, originalInstanceId, rdfType, clonedProperties, existingInContext, resolve, reject);
      });
    });
  }

  executeAction(parentId, originalInstanceId, rdfType, clonedProperties, existingInContext, resolve, reject) {
    let eventEmitter = new EventEmitter();
    this.registerContextChangeHandler(eventEmitter, existingInContext);
    this.namespaceService.toFullURI([rdfType]).then(({data: fullUrisMap}) => {
      let fullUri = fullUrisMap[rdfType];
      this.createPanelService.openCreateInstanceDialog({
        // Overriding with predefined data
        parentId,
        instanceData: clonedProperties,
        instanceType: fullUri,
        instanceSubType: clonedProperties.definitionId,
        contextSelectorSelectionMode: existingInContext,
        // This purpose will make sure that all possible types are returned because we could clone either
        // creatable OR uploadable object types.
        purpose: [ModelsService.PURPOSE_CREATE, ModelsService.PURPOSE_UPLOAD],
        // Overriding the dialog look & behaviour
        controls: {
          showCreateMore: false
        },
        eventEmitter,
        forceCreate: true,
        existingInContext,
        header: 'idoc.clone.header',
        helpTarget: 'idoc.action.clone',
        exclusions: ['file-upload-panel'],
        createButtonLabel: 'instance.create.panel.clone',
        showTemplateSelector: false,
        onCreate: (models) => {
          return this.createButtonHandler(originalInstanceId, models);
        },
        onClosed: (result) => {
          if (result.instanceCreated) {
            resolve();
          } else {
            reject();
          }
        }
      });
    });
  }

  registerContextChangeHandler(eventEmitter, existingInContext) {
    eventEmitter.subscribe(CONTEXT_CHANGED_EVENT, (contextId) => {
      let errorMessage = InstanceContextService.validateExistenceInContext(contextId, existingInContext);
      if (errorMessage) {
        eventEmitter.publish(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, errorMessage);
      }
    });
  }

  createButtonHandler(originalInstanceId, models) {
    let instanceObject = new InstanceObject(models.instanceId, models);
    let data = {
      id: models.instanceId,
      definitionId: instanceObject.getModels().definitionId,
      parentId: instanceObject.getModels().parentId,
      properties: instanceObject.getChangeset(false)
    };

    this.eventbus.publish(new BeforeIdocSaveEvent(data));
    return this.instanceRestService.cloneInstance(originalInstanceId, data).then((response) => {
      let instance = response.data;
      this.eventbus.publish(new InstanceCreatedEvent({
        currentObject: instance
      }));
      return instance;
    });
  }

  getParentId(instance) {
    let hasParent = instance.properties.hasParent;
    if (hasParent && hasParent.results && hasParent.results.length > 0) {
      return hasParent.results[0];
    }
  }
}