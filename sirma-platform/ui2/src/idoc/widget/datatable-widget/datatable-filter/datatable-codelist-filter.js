import {View, Component, Inject, NgTimeout} from 'app/app';
import _ from 'lodash';
import {Configurable} from 'components/configurable';
import {CodelistService} from 'services/codelist/codelist-service';
import 'components/select/select';

import template from './datatable-codelist-filter.html!text';
import './datatable-codelist-filter.css!';

@Component({
  selector: 'seip-datatable-codelist-filter',
  properties: {
    config: 'config',
    header: 'header',
    value: 'value'
  },
  events: ['onFilter']
})
@View({
  template
})
@Inject(CodelistService, NgTimeout)
export class DatatableCodelistFilter extends Configurable {
  constructor(codelistService, $timeout) {
    super({});
    this.codelistService = codelistService;
    this.$timeout = $timeout;
  }

  ngOnInit() {
    this.createSelectConfig();
  }

  createSelectConfig() {
    this.loadCodeValues().then((values) => {
      this.selectConfig = {
        multiple: true,
        data: values,
        defaultValue: this.value,
        isDisabled: this.config.isDisabled
      };
    });
  }

  loadCodeValues() {
    return this.codelistService.aggregateCodelists(this.header.codeLists, this.isExistingValue.bind(this), CodelistService.convertToSelectValue).then((values) => {
      return _.sortBy(values, (value) => {
        return value.text;
      });
    });
  }

  onChanged() {
    this.$timeout(() => {
      this.onFilter();
    });
  }

  /**
   * All values for given codelists are extracted but in the combobox are displayed only these which actually exist as value for any of the instances in the DTW.
   * This way the autosuggest displays a tidy list with values which exist somewhere in the DTW results.
   * @param value full codevalue as returned by the server
   * @returns {boolean} true if given value is amongst aggregated values
   */
  isExistingValue(codevalue) {
    if (this.config.aggregated) {
      let availableValues = this.config.aggregated[this.header.uri];
      return !!(availableValues && availableValues[codevalue.value]);
    }
    return true;
  }
}
