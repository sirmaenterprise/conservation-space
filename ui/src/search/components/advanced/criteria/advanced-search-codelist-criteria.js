import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Select} from 'components/select/select';
import {CodelistRestService} from 'services/rest/codelist-service';
import _ from 'lodash';

import template from './advanced-search-codelist-criteria.html!text';

/**
 * Component for choosing code list values in the advanced search criteria form.
 *
 * The component relies heavily on providing a criteria object and the property for which will display code values. If
 * the property has multiple code lists they will be fetched and merged.
 *
 * The component can be configured, example configuration:
 *  config: {
 *    disabled: false
 *  }
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-codelist-criteria',
  properties: {
    'config': 'config',
    'property': 'property',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
@Inject(NgScope, CodelistRestService, PromiseAdapter)
export class AdvancedSearchCodelistCriteria extends Configurable {
  constructor($scope, codelistRestService, promiseAdapter) {
    super({
      disabled: false
    });
    this.$scope = $scope;
    this.codelistRestService = codelistRestService;
    this.promiseAdapter = promiseAdapter;

    this.createSelectConfig();
    this.registerDisabledWatcher();
  }

  createSelectConfig() {
    this.loadCodeValues().then((values) => {
      this.selectConfig = {
        multiple: true,
        data: values,
        disabled: this.config.disabled,
        selectOnClose: true
      };
    });
  }

  loadCodeValues() {
    var promises = [];
    this.property.codeLists.forEach((codeList) => {
      promises.push(this.codelistRestService.getCodelist({
        codelistNumber: codeList
      }));
    });
    return this.promiseAdapter.all(promises).then((responses) => {
      return this.mergeCodeValues(responses);
    });
  }

  mergeCodeValues(responses) {
    var values = [];
    var uniqueValuesSet = new Set();

    responses.forEach((response) => {
      if (response.data) {
        response.data.forEach((codeValue) => {

          var value = codeValue.value;
          if (!uniqueValuesSet.has(value)) {
            values.push(this.convertValue(codeValue));
            uniqueValuesSet.add(value);
          }

        });
      }
    });

    return _.sortBy(values, (value) => {
      return value.text;
    });
  }

  convertValue(codeValue) {
    return {
      id: codeValue.value,
      text: codeValue.label
    };
  }

  registerDisabledWatcher() {
    this.$scope.$watch(()=> {
      return this.config.disabled;
    }, (state) => {
      if (this.selectConfig) {
        this.selectConfig.disabled = state;
      }
    });
  }
}