import {Injectable, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';
import {UploadCompletedEvent} from 'file-upload/events';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {SavedSearchLoadedEvent, SavedSearchCreatedEvent, SavedSearchUpdatedEvent} from 'search/components/saved/events';
import _ from 'lodash';

/**
 * Mediator between fired events and the recent objects service.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(Eventbus, RecentObjectsService)
export class RecentObjectsListener {

  constructor(eventbus, recentObjectsService) {
    this.eventbus = eventbus;
    this.recentObjectsService = recentObjectsService;

    this.registerEvents();
  }

  registerEvents() {
    this.eventbus.subscribe(AfterIdocLoadedEvent, this.handleIdocEvent.bind(this));
    this.eventbus.subscribe(InstanceCreatedEvent, this.handleIdocEvent.bind(this));
    this.eventbus.subscribe(ActionExecutedEvent, this.handleActionEvent.bind(this));
    this.eventbus.subscribe(UploadCompletedEvent, this.addRecentObject.bind(this));

    this.eventbus.subscribe(SavedSearchLoadedEvent, this.addRecentObject.bind(this));
    this.eventbus.subscribe(SavedSearchCreatedEvent, this.addRecentObject.bind(this));
    this.eventbus.subscribe(SavedSearchUpdatedEvent, this.addRecentObject.bind(this));

    this.eventbus.subscribe(AfterIdocDeleteEvent, this.removeFromList.bind(this));
  }

  handleIdocEvent(payload) {
    this.addRecentObject(payload[0].currentObject);
  }

  handleActionEvent(payload) {
    var data = payload.response && payload.response.data;
    if (!_.isArray(data)) {
      data = [payload.context.currentObject];
    }

    _.each(data, (instance) => this.addRecentObject(instance));
  }

  removeFromList(instances) {
    // There is only one instance even when an entire hierarchy is deleted
    let instance = instances[0];
    this.recentObjectsService.removeRecentObject(instance);
  }

  addRecentObject(instance) {
    if (instance && instance.id) {
      this.recentObjectsService.addRecentObject(instance);
    }
  }

}