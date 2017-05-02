import {Injectable, Inject} from 'app/app';
import {CreatePanelService} from 'services/create/create-panel-service';
import {InstanceObject} from 'idoc/idoc-context';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelsService} from 'services/rest/models-service';

@Injectable()
@Inject(CreatePanelService, InstanceRestService, Eventbus, NamespaceService, PromiseAdapter)
export class CloneAction {

  constructor(createPanelService, instanceRestService, eventbus, namespaceService, promiseAdapter) {
    this.instanceRestService = instanceRestService;
    this.createPanelService = createPanelService;
    this.namespaceService = namespaceService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
  }

  execute(action, actionContext) {
    var originalInstanceId = actionContext.currentObject.getId();

    var requests = [
      // Original instance is needed so we could load the parent ID (if any)
      this.instanceRestService.load(originalInstanceId),
      // Getting cloned instance properties
      this.instanceRestService.cloneProperties(originalInstanceId)
    ];

    this.promiseAdapter.all(requests).then((responses) => {
      var parentId = this.getParentId(responses[0].data);
      var instanceData = responses[1].data;
      var rdfType = instanceData.properties['rdf:type'][0].id;

      this.namespaceService.toFullURI([rdfType]).then((fullUrisMap) => {
        var fullUri = fullUrisMap.data[rdfType];
        this.createPanelService.openCreateInstanceDialog({
          // Overriding with predefined data
          parentId: parentId,
          instanceData: instanceData,
          instanceType: fullUri,
          instanceSubType: instanceData.definitionId,
          // This purpose will make sure that all possible types are returned because we could clone either
          // creatable OR uploadable object types.
          purpose: [ModelsService.PURPOSE_CREATE, ModelsService.PURPOSE_UPLOAD],
          // Overriding the dialog look & behaviour
          controls: {
            showCreateMore: false
          },
          forceCreate: true,
          header: 'idoc.clone.header',
          helpTarget: 'idoc.action.clone',
          exclusions: ['file-upload-panel'],
          createButtonLabel: 'instance.create.panel.clone',
          onCreate: (models) => {
            return this.createButtonHandler(originalInstanceId, models);
          }
        });
      });
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

    return this.instanceRestService.cloneInstance(originalInstanceId, data).then((response) => {
      let instance = response.data;
      this.eventbus.publish(new InstanceCreatedEvent({
        currentObject: instance
      }));
      return instance;
    });
  }

  getParentId(instance) {
    var hasParent = instance.properties.hasParent;
    if (hasParent && hasParent.length > 0) {
      return hasParent[0].id;
    }
  }
}