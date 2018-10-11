import {Inject, Injectable} from 'app/app';
import {BootstrapService} from 'services/bootstrap-service';
import {RestClient} from 'services/rest-client';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';

export const SERVICE_URL = '/help';
export const HELP_INSTANCE_TYPE = 'help';

/**
 * Service responsible for loading and mapping help instances used among the application's contextual help points.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(RestClient, Eventbus)
export class HelpService extends BootstrapService {

  constructor(restClient, eventbus) {
    super();
    this.restClient = restClient;
    this.eventbus = eventbus;

    this.helpTargetMapping = {};
    this.registerEventListeners();
  }

  initialize() {
    return this.restClient.get(SERVICE_URL).then((response) => {
      this.helpTargetMapping = response.data;
    });
  }

  getHelpInstanceId(helpTarget) {
    return this.helpTargetMapping[helpTarget];
  }

  registerEventListeners() {
    this.eventbus.subscribe(InstanceCreatedEvent, (payload) => {
      this.handleEventInstance(payload[0].currentObject);
    });
    this.eventbus.subscribe(ActionExecutedEvent, (actionPayload) => {
      this.handleEventInstance(actionPayload.context.currentObject);
    });
  }

  handleEventInstance(instance) {
    if (instance && instance.instanceType === HELP_INSTANCE_TYPE) {
      this.initialize();
    }
  }
}