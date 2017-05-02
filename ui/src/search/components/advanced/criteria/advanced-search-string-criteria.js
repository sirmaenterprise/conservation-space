import {Component, View, Inject, NgScope, NgElement} from "app/app";
import {Configurable} from "components/configurable";
import {Select} from "components/select/select";
import {TooltipAdapter} from "adapters/tooltip-adapter";
import {TranslateService} from "services/i18n/translate-service";
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import template from "./advanced-search-string-criteria.html!text";

@Component({
  selector: 'seip-advanced-search-string-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria',
    'property': 'property'
  }
})
@View({
  template: template
})
@Inject(NgScope, NgElement, TooltipAdapter, TranslateService)
export class AdvancedSearchStringCriteria extends Configurable {
  constructor($scope, $element, tooltipAdapter, translateService) {
    super({
      disabled: false
    });
    this.$scope = $scope;
    this.$element = $element;
    this.translateService = translateService;
    this.tooltipAdapter = tooltipAdapter;

    this.assignMultipleModel();
    this.createSelectConfig();
    this.registerDisabledWatcher();
    this.registerTooltipHandler();
  }

  /**
   * Makes sure the criteria value will be in the correct type -> array.
   */
  assignMultipleModel() {
    if (this.getMode() === SearchCriteriaUtils.MULTIPLE) {
      if (!this.criteria.value) {
        this.criteria.value = [];
      }
      // Ensuring previous single values will be treated correctly.
      if (!Array.isArray(this.criteria.value)) {
        this.criteria.value = [this.criteria.value];
      }
    }
  }

  createSelectConfig() {
    this.selectConfig = {
      multiple: true,
      tags: this.criteria.value,
      disabled: this.config.disabled,
      selectOnClose: true
    };
  }

  registerDisabledWatcher() {
    this.$scope.$watch(()=> {
      return this.config.disabled;
    }, (state) => {
      this.selectConfig.disabled = state;
    });
  }

  registerTooltipHandler() {
    this.tooltipLabel = this.translateService.translateInstant('search.advanced.property.anyField.warning');
    this.$element.on('mouseenter', this.showTooltip(this.$element));
  }

  showTooltip(element) {
    if (this.criteria.field === SearchCriteriaUtils.ANY_FIELD) {
      this.tooltipAdapter.tooltip(element, {title: this.tooltipLabel}, true);
    }
  }

  getMode() {
    if (this.criteria.operator === AdvancedSearchCriteriaOperators.EMPTY.id) {
      return AdvancedSearchCriteriaOperators.EMPTY.id;
    } else if (this.property.singleValued) {
      return SearchCriteriaUtils.SINGLE;
    }
    return SearchCriteriaUtils.MULTIPLE;
  }

}