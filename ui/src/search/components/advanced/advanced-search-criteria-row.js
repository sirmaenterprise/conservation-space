import {Component, View, Inject, NgScope, NgElement, NgCompile, NgTimeout} from 'app/app';
import {SearchComponent} from 'search/components/common/search-component';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {Select} from 'components/select/select';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {AdvancedSearchFilterExecutor} from './filters/advanced-search-filter-executor';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import _ from 'lodash';
import 'font-awesome/css/font-awesome.css!';
import './advanced-search-criteria-row.css!css';
import template from './advanced-search-criteria-row.html!text';

export const ADVANCED_CRITERIA_EXTENSION_POINT = 'advanced-search-criteria';

/**
 * Component representing a single criteria rule in the criteria tree.
 *
 * The component creates separate selects for the field and operator model and for the value model it resolves
 * different extensions based on the selected field's type.
 *
 * If for the selected type there is no extension available, it will resolve the default <b>string</b> extension.
 * Every extension must provide it's type and possible operators. The operators labels will be translated.
 *
 * The component DEPENDS on providing a list of properties (the fields) as a component property and a search mediator
 * in the configuration. Example configuration:
 *  {
 *    searchMediator: {...},
 *    disabled: false,
 *  }
 *
 * The search mediator is required for adding and removing criteria, because of its handling of those operations - it
 * throws before and after change events and uses the query builder beneath it to do the real adding and removing.
 * Directly modifying the criteria tree is discouraged.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-criteria-row',
  properties: {
    'config': 'config',
    'context': 'context',
    'criteria': 'criteria',
    'properties': 'properties'
  }
})
@View({
  template: template
})
@Inject(NgScope, NgElement, PromiseAdapter, PluginsService, TranslateService, NgCompile, NgTimeout, AdvancedSearchFilterExecutor)
export class AdvancedSearchCriteriaRow extends SearchComponent {

  constructor($scope, $element, promiseAdapter, pluginsService, translateService, $compile, $timeout, advancedSearchFilterExecutor) {
    super({
      disabled: false
    });

    this.$scope = $scope;
    this.$element = $element;
    this.promiseAdapter = promiseAdapter;
    this.pluginsService = pluginsService;
    this.translateService = translateService;
    this.$compile = $compile;
    this.$timeout = $timeout;
    this.advancedSearchFilterExecutor = advancedSearchFilterExecutor;

    this.createPropertySelectConfig();
    this.registerModelWatchers();
  }

  createPropertySelectConfig() {
    this.propertySelectConfig = {
      defaultToFirstValue: true,
      defaultValue: this.criteria.field,
      data: this.properties,
      multiple: false,
      disabled: this.isLockedOrDisabled(AdvancedSearchComponents.PROPERTY),
      selectOnClose: true
    };
  }

  /**
   * Register watchers for the criteria property and the configuration's enabled state.
   */
  registerModelWatchers() {
    this.$scope.$watch(()=> {
      return this.criteria.field;
    }, (newField, oldField) => {
      if (newField && newField.length > 0) {
        this.onPropertyChange(oldField);
      }
    });
    // Enable/disable watcher for the selects
    this.$scope.$watch(() => {
      return this.config.disabled;
    }, () => {
      if (this.propertySelectConfig) {
        this.propertySelectConfig.disabled = this.isLockedOrDisabled(AdvancedSearchComponents.PROPERTY);
      }
      if (this.operatorSelectConfig) {
        this.operatorSelectConfig.disabled = this.isLockedOrDisabled(AdvancedSearchComponents.OPERATOR);
      }
    });
  }

  remove() {
    this.config.searchMediator.removeCriteria(this.criteria);
  }

  onPropertyChange(oldProperty) {
    this.property = _.find(this.properties, (property) => {
      return property.id === this.criteria.field;
    });

    this.criteria.type = this.property.type;

    this.getCriteriaExtension(this.property.type).then((extension) => {
      var operators = this.translateOperators(extension);
      this.advancedSearchFilterExecutor.filterOperators(this.config, this.property, operators).then((operators) => {
        // Updating the operators only if the resolved extension's type is different from the previous one
        // or the new operators are not the same.
        if (this.extensionType !== extension.type) {
          this.updateOperatorsSelect(operators);
          this.extensionType = extension.type;
        } else if (this.operatorSelectConfig && !_.isEqual(this.operatorSelectConfig.data, operators)) {
          this.updateOperatorsSelect(operators);
        }

        // If the property is changed, the value should be reset
        // Concrete criteria extensions should build their own model once compiled & initialized!
        if (oldProperty && oldProperty.length > 0 && oldProperty !== this.criteria.field) {
          this.criteria.value = '';
        }

        this.compileExtension(extension);
      });
    });
  }

  getCriteriaExtension(type) {
    return this.pluginsService.loadComponentModules(ADVANCED_CRITERIA_EXTENSION_POINT, 'type').then((modules) => {
      var criteriaExtension = modules[type];
      if (!criteriaExtension) {
        // Default extension
        criteriaExtension = modules['string'];
      }
      return criteriaExtension;
    });
  }

  translateOperators(extension) {
    if (!extension.operators) {
      return [];
    }
    return extension.operators.map((mappedOperator) => {
      var operator = mappedOperator;
      if (_.isString(operator)) {
        operator = AdvancedSearchCriteriaOperators[operator.toUpperCase()];
      }

      return {
        id: operator.id,
        // Transforming label -> text for the select component
        text: this.translateService.translateInstant(operator.label)
      };
    });
  }

  updateOperatorsSelect(operators) {
    if (this.operatorSelectConfig) {
      delete this.operatorSelectConfig;
      var preservePreviousOperator = _.find(operators, (operator) => {
        return operator.id === this.criteria.operator;
      });
      if (!preservePreviousOperator) {
        // Set the first by default because the select does not react adequate...
        this.criteria.operator = operators[0].id;
      }
      // Gotta be out of the current digest cycle to recreate the select.
      this.$timeout(() => {
        this.createOperatorSelectConfig(operators);
      });
    } else {
      this.createOperatorSelectConfig(operators);
    }
  }

  createOperatorSelectConfig(operators) {
    this.operatorSelectConfig = {
      defaultToFirstValue: true,
      defaultValue: this.criteria.operator,
      data: operators,
      multiple: false,
      hideSearchBox: true,
      disabled: this.isLockedOrDisabled(AdvancedSearchComponents.OPERATOR),
      selectOnClose: true
    };
  }

  compileExtension(extension) {
    if (this.extensionScope) {
      // Ensuring previous scope is removed.
      this.extensionScope.$destroy();
    }
    this.extensionScope = this.$scope.$new();

    var component = extension.component;
    var html = `<${component} config="advancedSearchCriteriaRow.config"`;
    html += ` property="advancedSearchCriteriaRow.property"`;
    html += ` context="advancedSearchCriteriaRow.context"`;
    html += ` criteria="advancedSearchCriteriaRow.criteria"></${component}>`;
    var compiled = this.$compile(html)(this.extensionScope)[0];

    var valueColumn = this.$element.find('.criteria-column.criteria-value');
    valueColumn.empty();
    valueColumn.append(compiled);
  }

  isRemoveRuleButtonDisabled() {
    return this.isLockedOrDisabled(AdvancedSearchComponents.REMOVE_RULE);
  }
}