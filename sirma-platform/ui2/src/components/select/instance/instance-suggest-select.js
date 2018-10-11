import {Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import {RelationsService} from 'services/rest/relations-service';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

import _ from 'lodash';

export const OBJECT_SELECT_PROPERTIES = ['id', 'title', 'altTitle'];

@Component({
  selector: 'seip-instance-suggest-select',
  properties: {
    'config': 'config'
  }
})

@Inject(NgElement, NgScope, NgTimeout, RelationsService)
export class InstanceSuggestSelect extends Select {

  constructor($element, $scope, $timeout, relationsService) {
    super($element, $scope, $timeout);
    this.relationsService = relationsService;
  }

  ngOnInit() {
    this.$element.on('select2:select', (event) => {
      this.config.eventEmitter.publish('selecting', [{id: event.params.data.id, headers: event.params.data.data}]);
      this.$element.val(null).trigger('change');
      this.$element.select2('close');
    });
  }

  createActualConfig() {
    let converter = (response) => {
      let values = response.data.values;
      if (!values) {
        return [];
      }

      return values.map((item) => {
        let isDisabled = false;
        if ([...this.config.selectionPool.keys()].indexOf(item.id) !== -1) {
          isDisabled = true;
        }
        return {
          id: item.id,
          text: item.properties.altTitle,
          disabled: isDisabled
        };
      });
    };

    let loader = (params) => {
      let searchTerm = params && params.data && params.data.q || '';

      // When Safari calls the super method in the constructor, the relation service is not injected jet.
      // Safari throws error and stops instantiation.
      // So we return a promise to allow the instantiate of the supper class and then to build the
      // actual config. Used Promise instead our PromiseAdaprer for the same reason - at this point
      // PromiseAdapter will not be injected jet.
      if (!this.relationsService && NavigatorAdapter.isSafari()) {
        return Promise.resolve();
      }

      return this.relationsService.suggest(this.config.definitionId, this.config.propertyName, searchTerm, OBJECT_SELECT_PROPERTIES);
    };

    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader: loader,
      dataConverter: converter,
      formatSelection: (item) => {
        return `<span></span>`;
      },
      dropdownAutoWidth: true,
      width: 'auto'
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }

  bindToModel() {
    // not implemented
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }
}