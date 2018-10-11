import _ from 'lodash';
import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {InstanceRestService} from 'services/rest/instance-service';
import {InstanceByTypeFilter} from 'filters/instance-by-type-filter';
import {HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT} from 'instance-header/header-constants';
import 'header-container/header-container';

import template from './instance-list.html!text';
import './instance-list.css!';

@Component({
  selector: 'seip-instance-list',
  properties: {
    config: 'config',
    identifiers: 'identifiers',
    instances: 'instances',
    selectedItems: 'selected-items',
    typesFilter: 'types-filter'
  }
})
@View({
  template: template
})
@Inject(InstanceRestService)
export class InstanceList extends Configurable {

  constructor(instanceService) {
    super({
      selectableItems: false,
      singleSelection: true,
      selectionHandler: _.noop,
      selectAll: true,
      deselectAll: true,
      exclusions: [],
      selectedItems: []
    });

    this.instanceService = instanceService;
    this.instances = this.instances || [];
    this.filteredInstances = [];
  }

  ngOnInit() {
    this.excluded = {};
    _.each(this.config.exclusions, (id) => this.excluded[id] = true);

    if ((!this.instances || !this.instances.length) && this.identifiers && this.identifiers.length) {
      this.instanceService.loadBatch(this.identifiers, this.getInstanceServiceConfig()).then(this.loadInstances.bind(this));
      return;
    }

    this.loadHeaders(this.instances);
  }

  loadHeaders(instances) {
    if (!instances || !instances.length) {
      // No instances present, conclude loading
      this.finishedLoading = true;
      return;
    }

    var noHeader = _.find(instances, (instance) => {
      return !instance.headers || !instance.headers.default_header;
    });

    if (!noHeader) {
      // Headers are present, conclude loading
      this.finishedLoading = true;
      return;
    }

    this.instanceService.loadBatch(instances.map(instance => instance.id),
      this.getInstanceServiceConfig()).then((response) => this.loadInstances(response));
  }

  loadInstances(response) {
    this.refreshInstances(response);
    // Response completed, conclude loading
    this.finishedLoading = true;
  }

  /**
   * Refreshes the internal instance array with those in the provided service response (if present). This operation
   * preserves the array reference.
   */
  refreshInstances(response) {
    var instances = response && response.data;
    if (!instances || !instances.length) {
      return;
    }

    this.instances.splice(0);
    this.instances.push(...instances);
  }

  onClick(instance) {
    if (this.excluded[instance.id] || !this.config.selectableItems) {
      return;
    }
    this.config.selectionHandler(instance);
  }

  getInstanceServiceConfig() {
    return {
      params: {
        properties: [HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT]
      }
    };
  }

  isSelected(instance) {
    return _.findIndex(this.selectedItems, (item) => item.id === instance.id) > -1;
  }

  get selectionControlType() {
    return this.config.singleSelection ? 'radio' : 'checkbox';
  }

  isSelectDeselectEnabled() {
    return this.filteredInstances.length > 0 && !this.config.singleSelection && this.config.selectableItems;
  }

  selectAll() {
    _.each(this.filteredInstances, (object) => {
      if (!this.isSelected(object)) {
        this.onClick(object);
      }
    });
  }

  deselectAll() {
    let instances = this.filteredInstances;
    for (let i = instances.length - 1; i >= 0; --i) {
      let current = instances[i];
      if (this.isSelected(current)) {
        this.onClick(current);
      }
    }
  }
}