import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/services/rest/properties-service.data.json!';

export const DEFINITION_RANGE_FIELD = 'range';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class PropertiesRestService {

  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['properties'];
  }

  getSearchableProperties(types) {
    return this.promiseAdapter.promise((resolve) => {
      var properties = [];
      if (types instanceof Array) {
        types.forEach((type) => {
          var propertiesMapping = data.types[type];
          var typeProperties = propertiesMapping.map((property) => {
            return data.properties[property];
          });
          properties = _.union(properties, typeProperties);
        });
      } else {
        var propertiesMapping = data.types[types || 'all'];
        properties = propertiesMapping.map((property) => {
          return data.properties[property];
        });
      }

      this.$timeout(() => {
        resolve(properties);
      }, this.config.timeout);
    });
  }

  checkFieldUniqueness(definitionId, instanceId, fieldName, value) {
    let unique = data.unique[value];
    // it can't be undefined when obtained from the backend
    if (unique == undefined) {
      unique = true;
    }
    return this.promiseAdapter.resolve({data: {unique: unique}});
  }

  evaluateValues(definitionId, bindings) {
    return _.clone(data.evaluation[definitionId]);
  }

  loadObjectPropertiesSuggest(parentId, type, multivalued) {
    let selectionType = multivalued ? 'multivalue' : 'singlevalue';
    let response = data.suggest[selectionType][parentId];
    return this.promiseAdapter.promise((resolve) => {
      // add some delay to emulate slower response
      setTimeout(() => {
        resolve({
          data: response
        });
      }, 200);
    });
  }
}
