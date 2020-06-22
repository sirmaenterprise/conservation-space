import {Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import {ResourceRestService} from 'services/rest/resources-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';

@Component({
  selector: 'seip-resource-select',
  properties: {'config': 'config'}
})
@Inject(NgElement, NgScope, NgTimeout, ResourceRestService, PromiseAdapter)
export class ResourceSelect extends Select {

  constructor($element, $scope, $timeout, resourceRestService, promiseAdapter) {
    super($element, $scope, $timeout);
    this.resourceRestService = resourceRestService;
    this.promiseAdapter = promiseAdapter;
  }

  createActualConfig() {
    if (_.isUndefined(this.config.includeUsers)) {
      this.config.includeUsers = true;
    }
    if (_.isUndefined(this.config.includeGroups)) {
      this.config.includeGroups = false;
    }

    let dataLoader = (params) => {
      let options = {
        includeUsers: this.config.includeUsers,
        includeGroups: this.config.includeGroups,
        term: (params && params.data && params.data.q) || ''
      };
      return this.resourceRestService.getResources(options);
    };

    let dataConverter = (response) => {
      let data = response.data;
      if (!data || !data.items) {
        return [];
      }
      return data.items.map((resource) => {
        return this.convertResource(resource);
      });
    };

    let mapper = (ids) => {
      return this.promiseAdapter.promise((resolve) => {
        let result = [];
        let promises = [];

        ids.forEach(id => {
          promises.push(this.resourceRestService.getResource(id));
        });

        this.promiseAdapter.all(promises).then((resources) => {
          resources.forEach((response) => {
            let data = response.data;
            let resource = this.convertResource(data);
            result.push(resource);
          });

          resolve(result);
        });
      });
    };

    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader,
      dataConverter,
      mapper
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }

  convertResource(resource) {
    if (this.config.resourceConverter) {
      return this.config.resourceConverter(resource);
    }
    return {
      id: resource.id,
      text: resource.label,
      type: resource.type,
      value: resource.value
    };
  }
}