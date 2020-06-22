import {Injectable} from 'app/app';

/**
 * Provider for related codelist fields filters and data loaders. The filter configurations are kept in a map where the
 * key is composed from an instanceId and a field name to prevent collisions. If a key appears in the map, then the
 * value for that key is overridden.
 *
 * @author svelikov
 */
@Injectable()
export class CodelistFilterProvider {

  constructor() {
    this.filterConfigs = {};
  }

  static buildMapKey(instanceId, fieldName) {
    return `${instanceId}_${fieldName}`;
  }

  setFilterConfig(instanceId, fieldName, filter) {
    this.filterConfigs[CodelistFilterProvider.buildMapKey(instanceId, fieldName)] = filter;
  }

  getFilterConfig(instanceId, fieldName) {
    return this.filterConfigs[CodelistFilterProvider.buildMapKey(instanceId, fieldName)];
  }

}