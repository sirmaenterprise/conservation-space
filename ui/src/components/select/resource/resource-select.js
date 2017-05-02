import { Select } from 'components/select/select';
import { View,Component,Inject,NgElement, NgScope } from 'app/app';
import { ResourceRestService } from 'services/rest/resources-service';
import _ from 'lodash';

@Component({
  selector: 'seip-resource-select',
  properties: {'config': 'config'}
})
@Inject(NgElement, NgScope, '$timeout', ResourceRestService)
export class ResourceSelect extends Select {

  constructor($element, $scope, $timeout, resourceRestService) {
    super($element, $scope, $timeout);
    this.resourceRestService = resourceRestService;
  }

  createActualConfig() {
    if (_.isUndefined(this.config.includeUsers)) {
      this.config.includeUsers = true;
    }
    if (_.isUndefined(this.config.includeGroups)) {
      this.config.includeGroups = false;
    }

    let loader = (params) => {
      let options = {
        includeUsers: this.config.includeUsers,
        includeGroups: this.config.includeGroups,
        term: (params && params.data && params.data.q) || ''
      };
      return this.resourceRestService.getResources(options);
    };

    let converter = (response) => {
      let data = response.data;
      if(!data || !data.items) {
        return [ ];
      }
      return data.items.map((item) => {
        return this.transformResultFromRest(item);
      });
    };

    let mapper = (ids) => {
      return new Promise((resolve) => {
        let result = [];
        let promises = [];

        ids.forEach(id => {
          promises.push(this.resourceRestService.getResource(id));
        });

        Promise.all(promises).then((resources) => {
          resources.forEach((response) => {
            let data = response.data;
            let resource = this.transformResultFromRest(data);
            result.push(resource);
          });

          resolve(result);
        });
      });
    };

    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader: loader,
      dataConverter: converter,
      mapper: mapper
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }

  transformResultFromRest(data) {
    return {
      id: data.id,
      text: data.label,
      type: data.type,
      value: data.value
    }
  }
}