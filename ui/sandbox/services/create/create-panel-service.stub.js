import {Injectable, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter, InstanceRestService, Eventbus)
export class CreatePanelService {

  constructor(promiseAdapter, instanceRestService, eventbus) {
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
  }

  getInstanceDialogConfig() {
    return {};
  }

  getPropertiesConfig(opts) {
    let config = {
      config: {
        parentId: 'test_id',
        parentType: 'caseinstance',
        operation: 'create',
        classFilter: opts.predefinedTypes,
        forceCreate: opts.forceCreate,
        controls: opts.controls,
        formConfig: {
          models: {
            parentId: 'test_id',
            validationModel: {
              isValid: false
            }
          }
        },
        onCreate: () => {
          let createdInstance = {
            id: '1',
            headers: {
              breadcrumb_header: 'header'
            }
          };
          this.eventbus.publish(new InstanceCreatedEvent({
            currentObject: createdInstance
          }));
          return this.promiseAdapter.resolve(createdInstance);
        }
      }
    };
    return config;
  }
}