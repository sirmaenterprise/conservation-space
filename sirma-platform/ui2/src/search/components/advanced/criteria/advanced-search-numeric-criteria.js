import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import './advanced-search-numeric-criteria.css!css';
import template from './advanced-search-numeric-criteria.html!text';

@Component({
  selector: 'seip-advanced-search-numeric-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
@Inject(NgScope)
export class AdvancedSearchNumericCriteria extends Configurable {
  constructor($scope) {
    super({
      disabled: false
    });
    this.$scope = $scope;
  }

  ngOnInit() {
    this.assignModel();
    this.registerValueWatcher();
    this.registerOperatorWatcher();
    this.regExp = new RegExp(/^\-?[0-9]{0,9}\.?[0-9]{0,6}$/);
  }

  assignModel() {
    if (!this.criteria.value) {
      this.numberModel = this.resetModelValue();
      this.criteria.value = this.resetModelValue();
    } else {
      this.numberModel = this.criteria.value;
    }
  }

  registerOperatorWatcher() {
    this.$scope.$watch(() => {
      return this.criteria.operator;
    }, (newOperator, oldOperator) => {
      if (newOperator !== oldOperator) {
        if (this.isEmpty()) {
          // Avoiding to reset numberModel cause it will trigger reset to a numeric model.
          this.criteria.value = null;
        } else {
          this.numberModel = this.resetModelValue();
          this.criteria.value = this.resetModelValue();
        }
      }
    });
  }

  registerValueWatcher() {
    this.$scope.$watch(() => {
      return this.numberModel;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        if (this.isBetween()) {
          for (let i = 0; i < this.criteria.value.length; ++i) {
            this.numberModel[i] = this.updateNumberModel(newValue[i], oldValue[i]);
            this.criteria.value[i] = this.parseAsNumber(this.numberModel[i]);
          }
        } else {
          this.numberModel = this.updateNumberModel(newValue, oldValue);
          this.criteria.value = this.parseAsNumber(this.numberModel);
        }
      }
    }, true);
  }

  updateNumberModel(newValue, oldValue) {
    return this.regExp.test(newValue) ? newValue : oldValue;
  }

  parseAsNumber(newValue) {
    if (newValue === '-' || newValue === '.' || newValue === '') {
      return 0;
    }

    if (this.regExp.test(newValue)) {
      return +newValue;
    }
  }

  isBetween() {
    return this.criteria.operator === AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
  }

  isEmpty() {
    return this.criteria.operator === AdvancedSearchCriteriaOperators.EMPTY.id;
  }

  resetModelValue() {
    if (this.isBetween()) {
      return ['', ''];
    } else {
      return '';
    }
  }
}