import {View, Inject} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {InstanceRestService} from 'services/rest/instance-service';
import {UrlUtils} from 'common/url-utils';
import {MODE_PRINT, VERSION_PART_ID} from 'idoc/idoc-constants';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import 'user/activity/user-activity-entry';
import 'search/components/common/pagination';
import template from './widget.html!text';
import './widget.css!';

@Widget
@View({
  template
})
@Inject(ObjectSelectorHelper, Eventbus, InstanceRestService, LocationAdapter)
export class RecentActivities {

  constructor(objectSelectorHelper, eventbus, instanceRestService, locationAdapter) {
    this.objectSelectorHelper = objectSelectorHelper;
    this.eventbus = eventbus;
    this.instanceRestService = instanceRestService;

    this.control.onConfigConfirmed = this.onConfigConfirmed.bind(this);

    // Always show all results in print mode
    if (UrlUtils.getParameter(locationAdapter.url(), 'mode') === MODE_PRINT) {
      this.config.pageSize = 'all';
    }

    //TODO update when the backend is fixed to return the correct date
    this.context.getCurrentObject().then((object) => {
      if (object.isVersion()) {
        this.modifiedOn = object.models.validationModel.modifiedOn.defaultValue;
      }
    });

    this.paginationConfig = {
      pageSize: this.config.pageSize,
      showFirstLastButtons: true
    };

    // TODO: think of another way of passing this to the Pagination
    // if not bind *this* is Pagination
    this.onPageChange = this.loadNextPage.bind(this);
    this.currentPage = 1;
  }

  ngOnInit() {
    this.onConfigConfirmed(this.config);
  }

  get resultsTotal() {
    if (!this.searchRequest) {
      return 0;
    }
    return this.searchRequest.offset + this.searchRequest.limit;
  }

  loadNextPage(data) {
    this.currentPage = data.pageNumber;
    this.load(this.config);
  }

  onConfigConfirmed(config) {
    if (config.selectedItems) {
      // avoid saving the full instances
      config.selectedItems = config.selectedItems.map((item) => {
        return {id: item.id};
      });
    }

    // Resetting the current page number
    this.currentPage = 1;

    this.paginationConfig.pageSize = config.pageSize;
    this.load(config);
  }

  load(config) {
    delete this.errorMessage;
    if (this.context.isModeling() && config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      this.fireWidgetReadyEvent();
      return;
    }
    this.paginationConfig.disabled = true;

    let selectorArguments = {ignoreNotPersisted: true};
    this.objectSelectorHelper.getSelectedObjects(config, this.context, undefined, selectorArguments).then((selection) => {
      let limit = config.pageSize;
      let offset;
      if (limit !== 'all') {
        offset = (this.currentPage - 1) * limit;
      } else {
        //When the pageSize is all, the backend expects data like this.
        limit = -1;
        offset = 0;
      }
      return this.loadAuditData(selection.results, limit, offset);
    }).catch((error) => {
      this.displayError(error);
      this.fireWidgetReadyEvent();
    }).finally(() => {
      this.paginationConfig.disabled = false;
      this.subscribeForUserActivityRendered();
    });
  }

  createDateRange() {
    return {
      end: this.modifiedOn
    };
  }

  loadAuditData(objects, limit, offset) {
    // Because manually selected objects are saved as objects instead of IDs and we need to resolve this case
    let identifiers = this.getIdentifiers(objects);
    let dateRange = this.createDateRange();
    return this.instanceRestService.loadAuditDataForInstances(identifiers, limit, offset, dateRange).then((response) => {
      if (!response.data || response.data.length === 0) {
        this.displayError('widget.recent-activities.none');
        this.fireWidgetReadyEvent();
      } else {
        this.searchRequest = response;
      }
    });
  }

  subscribeForUserActivityRendered() {
    if (this.userActivityRenderedSubscription) {
      this.userActivityRenderedSubscription.unsubscribe();
    }

    let compiledUserActivities = 0;
    this.userActivityRenderedSubscription = this.control.subscribe('userActivityRendered', () => {
      if (compiledUserActivities + 1 >= this.searchRequest.data.length) {
        this.userActivityRenderedSubscription.unsubscribe();
        this.fireWidgetReadyEvent();
      }
      compiledUserActivities += 1;
    });
  }

  getIdentifiers(objects) {
    // Check if array is consisted of objects
    // Currently the widget doesn't support versions so we extract only the main ids
    if (objects[0].id) {
      return objects.map(object => object.id.replace(VERSION_PART_ID, ''));
    } else {
      return objects.map(id => id.replace(VERSION_PART_ID, ''));
    }
  }

  fireWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  displayError(message) {
    delete this.searchRequest;
    this.errorMessage = message.reason || 'select.object.results.none';
  }

  renderPagination() {
    return !this.errorMessage && this.paginationConfig && this.resultsTotal && this.config.pageSize !== 'all';
  }
}