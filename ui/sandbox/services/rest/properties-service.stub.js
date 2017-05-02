import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/services/rest/properties-service.data.json!';
import _ from 'lodash';

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
          properties = _.union(properties,typeProperties);
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
}