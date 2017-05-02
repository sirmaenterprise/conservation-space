import {Component, View, Inject, NgScope} from 'app/app';
import {DynamicDateRange} from 'search/components/advanced/dynamic-date-range/dynamic-date-range';

import template from 'dynamic-date-range-stub-template!text';

@Component({
  selector: 'dynamic-date-range-stub'
})
@View({
  template: template
})
@Inject(NgScope)
export class DynamicDateRangeStub {

  constructor($scope) {

    this.default = {
      config: {}
    };

    this.predefined = {
      config: {
        disabled: true
      },
      step: 'next',
      offset: 5,
      offsetType: 'weeks'
    };

    $scope.$watchCollection(() => {
      return [this.default.step, this.default.offset, this.default.offsetType];
    }, () => {
      var dateOffset = {
        dateStep: this.default.step,
        offset: this.default.offset,
        offsetType: this.default.offsetType
      };
      this.evaluated = DynamicDateRange.buildDateRange(dateOffset);
    });
  }

  ngOnInit() {
    this.render = true;
  }

  toggleState() {
    this.default.config.disabled = !this.default.config.disabled;
  }


}