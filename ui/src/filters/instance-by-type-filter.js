import {Inject,Filter} from 'app/app';
import _ from 'lodash';

/*
 * Filter a list of instances by their semantic type.
 * The filter accepts an array of instances to filter and an array of types to filter by.
 */
@Filter
export class InstanceByTypeFilter {

  filter(instances, types) {
    if (!instances || !instances.length || !types || !types.length) {
      return instances;
    }

    return instances.filter((instance) => InstanceByTypeFilter.hasAnyType(types, instance));
  }

  static hasAnyType(types, instance) {
    if (!instance.properties || !instance.properties.semanticHierarchy) {
      return false;
    }

    return _.intersection(types, instance.properties.semanticHierarchy).length > 0;
  }
}
