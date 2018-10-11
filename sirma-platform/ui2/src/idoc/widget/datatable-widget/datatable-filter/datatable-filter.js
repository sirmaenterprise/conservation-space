import {View, Component, Inject, NgElement, NgCompile, NgScope} from 'app/app';
import _ from 'lodash';
import {TranslateService} from 'services/i18n/translate-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {PluginsService} from 'services/plugin/plugins-service';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {Logger} from 'services/logging/logger';

import template from './datatable-filter.html!text';
import './datatable-filter.css!';

const FIELD_TYPES_FOR_AGGREGATION = ['codeList', 'object', 'boolean'];
const TYPE_TO_OPERATOR_MAP = {
  codeList: AdvancedSearchCriteriaOperators.IN.id,
  string: AdvancedSearchCriteriaOperators.CONTAINS.id,
  object: AdvancedSearchCriteriaOperators.SET_TO.id,
  dateTime: AdvancedSearchCriteriaOperators.IS.id,
  boolean: AdvancedSearchCriteriaOperators.IN.id,
  numeric: AdvancedSearchCriteriaOperators.CONTAINS.id
};

@Component({
  selector: 'seip-datatable-filter',
  properties: {
    context: 'context',
    config: 'config',
    widgetConfig: 'widgetConfig'
  },
  events: ['onFilter']
})
@View({
  template
})
@Inject(NgElement, NgScope, NgCompile, PromiseAdapter, PluginsService, ObjectSelectorHelper, Logger, TranslateService)
export class DatatableFilter {

  constructor($element, $scope, $compile, promiseAdapter, pluginsService, objectSelectorHelper, logger, translateService) {
    this.$element = $element;
    this.$scope = $scope;
    this.$compile = $compile;
    this.promiseAdapter = promiseAdapter;
    this.objectSelectorHelper = objectSelectorHelper;
    this.logger = logger;
    this.translateService = translateService;

    this.filtersLoader = pluginsService.loadComponentModules('filter-controls', 'type');
  }

  ngOnInit() {
    this.config.isDisabled = () => {
      return this.config.disabled;
    };

    this.$scope.$watch(() => {
      return this.config.headers;
    }, () => {
      this.buildCriteriaMap();

      this.promiseAdapter.all([this.filtersLoader, this.getAggregationLoader()]).then((results) => {
        let modules = results[0];
        this.config.aggregated = results[1].aggregated;

        let template = '';

        this.config.headers.forEach((header, index) => {
          let module = modules[header.type];
          let moduleTemplate = module ? `<${module.component}
            header="::datatableFilter.config.headers[${index}]"
            value="datatableFilter.criteriaMap['${header.uri}'].value"
            config="::datatableFilter.config"
            on-filter="::datatableFilter.performFilter()"></${module.component}>` : '';
          template += `<div class="filter-cell ${header.name}-wrapper" data-filter-cell-name="${header.name}" ng-style="::{'width': datatableFilter.widgetConfig.styles.columns['${header.name}'].width}">${moduleTemplate}</div>`;
        });

        this.$element.empty();
        if (this.innerScope) {
          this.innerScope.$destroy();
        }

        this.innerScope = this.$scope.$new();

        this.$compile(template)(this.innerScope, (compiledElement) => {
          this.$element.append(compiledElement);
        });
      }).catch((error) => {
        if (error.reason) {
          this.logger.error(this.translateService.translateInstant(error.reason));
        } else {
          this.logger.error(error);
        }
      });
    });
  }

  /**
   * Collect fields which should be aggregated in order to remove all unnecessary values from autosuggests.
   * @returns {Array} with field uris which should be aggregated
   */
  getFieldsForAggregation() {
    let result = [];
    this.config.headers.forEach((header) => {
      if (FIELD_TYPES_FOR_AGGREGATION.indexOf(header.type) !== -1) {
        result.push(header.uri);
      }
    });
    return result;
  }

  getAggregationLoader() {
    let groupingConfig = ObjectSelectorHelper.getFilteringConfiguration(this.widgetConfig);
    let fieldsForAggregation = this.getFieldsForAggregation();
    return fieldsForAggregation && fieldsForAggregation.length > 0 ?
      this.objectSelectorHelper.groupSelectedObjects(groupingConfig, this.context, this.getFieldsForAggregation(), true) :
      this.promiseAdapter.resolve({});
  }

  performFilter() {
    let filterCriteria = DatatableFilter.mapToCriteria(this.criteriaMap);
    this.onFilter({filterCriteria});
  }

  buildCriteriaMap() {
    let initialCriteriaMap = DatatableFilter.criteriaToMap(this.config.filterCriteria);
    this.criteriaMap = {};
    this.config.headers.forEach((header) => {
      if (header.type) {
        let initialRule = initialCriteriaMap[header.uri];
        let initialValue;
        if (initialRule) {
          initialValue = initialRule.value;
        }

        let operator = TYPE_TO_OPERATOR_MAP[header.type];
        if (operator) {
          this.criteriaMap[header.uri] = SearchCriteriaUtils.buildRule(header.uri, header.type, operator, initialValue);
        }
      }
    });
  }

  /**
   * Converts flat (only one level) search criteria to criteria map.
   * Use only with filter criteria build with mapToCriteria method!
   * @param search criteria
   * @returns map with field name as key and rule as value
   */
  static criteriaToMap(criteria) {
    let criteriaMap = {};
    if (criteria && _.isArray(criteria.rules)) {
      criteria.rules.forEach((rule) => {
        criteriaMap[rule.field] = rule;
      });
    }
    return criteriaMap;
  }

  /**
   * Converts criteria map to criteria by combining rules with AND condition
   * @param criteriaMap
   * @returns search criteria
   */
  static mapToCriteria(criteriaMap) {
    let rules = [];
    Object.keys(criteriaMap).forEach((criteriaField) => {
      let rule = criteriaMap[criteriaField];
      if ((rule.value && rule.value.length > 0) || typeof rule.value === 'number') {
        rules.push(rule);
      }
    });

    let filterCriteria;
    if (rules.length > 0) {
      filterCriteria = SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, rules);
    }
    return filterCriteria;
  }
}
