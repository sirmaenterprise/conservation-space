import {Component, View, Inject, NgElement} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/select/select';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import template from './advanced-search-string-criteria.html!text';
import './advanced-search-string-criteria.css!css';

@Component({
  selector: 'seip-advanced-search-string-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria',
    'property': 'property'
  },
  events: ['onChange']
})
@View({template})
@Inject(NgElement, TooltipAdapter, TranslateService)
export class AdvancedSearchStringCriteria extends Configurable {
  constructor($element, tooltipAdapter, translateService) {
    super({
      debounce: 200,
      disabled: false
    });
    this.$element = $element;
    this.translateService = translateService;
    this.tooltipAdapter = tooltipAdapter;
  }

  ngOnInit() {
    this.assignModelOptions();
    this.assignMultipleModel();
    this.createSelectConfig();
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

  assignModelOptions() {
    // options for de-bouncing the user input on both multiple and single input
    this.modelOptions = {debounce: {'default': this.config.debounce, 'blur': 0}};
  }

  createSelectConfig() {
    this.selectConfig = {
      multiple: true,
      tags: this.criteria.value,
      isDisabled: () => this.config.disabled,
      selectOnClose: true
    };
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

  onValueChange() {
    if (this.onChange) {
      this.onChange();
    }
  }
}
