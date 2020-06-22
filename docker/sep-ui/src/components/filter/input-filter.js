import {Component, View, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {KEY_ENTER} from 'common/keys';

import './input-filter.css!css';
import template from './input-filter.html!text';

/**
 * Filter component which is a wrapper for an input field and a button
 * it can trigger two type of events: when a specified key has been
 * pressed or the button was clicked.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-input-filter',
  properties: {
    config: 'config'
  },
  events: ['onButtonClicked', 'onKeyPressed']
})
@View({template})
@Inject(NgScope, NgElement)
export class InputFilter extends Configurable {

  constructor($scope, $element) {
    super({
      filterKey: KEY_ENTER,
      buttonLabel: 'filter.button',
      inputPlaceholder: 'filter.placeholder'
    });

    this.$scope = $scope;
    this.$element = $element.find('input');
    this.ngModel = $element.controller('ngModel');
    if (this.form) {
      //adding the ngmodel controller to the form controller
      this.form.$addControl(this.ngModel);
    }
    this.bindToModel();
  }

  /**
   * Adds two way binding between model and the filter input field via model watcher and input event.
   */
  bindToModel() {
    if (this.ngModel) {
      this.$element.on('input', () => {
        let newValue = this.$element.val();
        let currentValue = this.ngModel.$viewValue;
        if (newValue !== currentValue) {
          this.ngModel.$setViewValue(newValue);
        }
      });

      let viewValueChecker = (newValue) => {
        let currentValue = this.$element.val();
        if (newValue !== currentValue) {
          this.$element.val(newValue);
          this.$element.trigger('input');
        }
      };
      this.$scope.$watch(() => this.ngModel.$viewValue, viewValueChecker);
    }
  }

  /**
   * Filters configurations when the specified filter key is pressed
   *
   * @param event the event by which to determine which key was pressed
   **/
  filterOnKey(event) {
    if (event.keyCode === this.config.filterKey) {
      this.onKeyPressed();
    }
  }
}
