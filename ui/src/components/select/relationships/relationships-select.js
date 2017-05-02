import _ from 'lodash';

import {Component, Inject, NgElement, NgScope} from 'app/app';
import {RelationshipsService} from 'services/rest/relationships-service';

import {Select} from 'components/select/select';

@Component({
  selector: 'seip-relationships-select',
  properties: {
    'config': 'config'
  }
})
@Inject(NgElement, NgScope, '$timeout', RelationshipsService)
export class RelationshipsSelect extends Select {

  constructor($element, $scope, $timeout, service) {
    super($element, $scope, $timeout);
    this.service = service;
  }

  createActualConfig() {
    let dataConverter = (response) => {
      let values = response.data || [];

      return values.map((item)=> {
        return {id: item.id, text: item.title};
      });
    };

    let opts = {};
    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader: (term) => {
        opts.q = term !== null ? term.data.q : undefined;
        return this.service.find(opts);
      },
      dataConverter: dataConverter
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }
}