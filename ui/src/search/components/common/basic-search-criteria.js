import _ from 'lodash';
import uuid from 'common/uuid';
import {Keys} from 'common/keys';
import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {Configuration} from 'common/application-config';
import {BasicSearchCriteriaTransformer} from 'search/components/common/basic-search-criteria-transformer';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {QueryBuilder} from 'search/utils/query-builder';
import {CURRENT_OBJECT} from 'search/resolvers/contextual-rules-resolver';

import 'components/datetimepicker/datetimepicker';
import 'components/select/resource/resource-select';
import 'components/select/object/object-select';
import 'components/select/object/object-type-select';
import 'components/select/relationships/relationships-select';
import 'search/components/saved/saved-search-select/saved-search-select';

import 'font-awesome/css/font-awesome.css!';
import './basic-search-criteria.css!css';
import template from './basic-search-criteria.html!text';

export const FIELDS = [
  'freeText', 'context', 'relationships', 'types',
  'createdFromDate', 'createdToDate', 'createdBy'
];
const DATE_CRITERIA_TYPE_FROM = 'from';
const DATE_CRITERIA_TYPE_TO = 'to';

/**
 * Configurable component with predefined web components (form) for performing a search.
 *
 * The component transforms advanced tree model to a more basic flat which can be used by the predefined form. It
 * directly binds the rules' values with the components. For more information see {@link BasicSearchCriteriaTransformer}
 *
 * If iDoc context is provided and there is no predefined criteria, it will automatically assign the contextual
 * "Current object", otherwise the context select will remain empty.
 *
 * Component is considered ready/initialized when the default or predefined criteria is assigned and transformed.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-basic-search-criteria',
  properties: {
    'config': 'config',
    'context': 'context'
  }
})
@View({
  template: template
})
@Inject(NgScope, NgElement, TranslateService, Configuration)
export class BasicSearchCriteria extends SearchCriteriaComponent {

  constructor($scope, $element, translateService, configuration) {
    super();
    this.$scope = $scope;
    this.$element = $element;
    this.translateService = translateService;
    this.configuration = configuration;

    this.initialize();
  }

  /**
   * Initializes the component and determines the state of the root context and the current object if given
   */
  initialize() {
    if (this.context && this.config.useRootContext) {
      this.context.getCurrentObject().then((currentObject) => {
        // Current context path of the instanceObject
        this.currentObject = currentObject;
        this.construct();
      });
    } else {
      this.construct();
    }
  }

  /**
   * Converts the criteria to a mapping and configures the form elements configurations.
   */
  construct() {
    this.setCriteria();
    this.configure();
    this.registerCriteriaModelWatchers();
    this.afterInit();
    this.initialized = true;
  }

  registerCriteriaModelWatchers() {
    // Shallow watcher (reference) for external changes (e.g. like loading saved searches).
    this.$scope.$watch(()=> {
      return this.config.searchMediator.queryBuilder.tree;
    }, (newValue, oldValue) => {
      // Reference check
      if (newValue !== oldValue) {
        this.setCriteria();
      }
    });

    // *Deep* watcher for internal model changes. Will transfer the basic search form values to the external model.
    this.$scope.$watch(() => {
      return this.criteriaMapping;
    }, (newValue, oldValue) => {
      if (!this.compareMappings(newValue, oldValue)) {
        var tree = this.config.searchMediator.queryBuilder.tree;
        BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(newValue, tree);
      }
    }, true);
  }

  /**
   * Performs comparison on the provided internal model rule mappings. The comparison checks the rules values and is
   * type aware of them - does separate checks for array and strings. If just one mapping value is different or empty
   * then they are considered to NOT be equal.
   *
   * @param firstMapping - the first mapping which is used to compare against the second one
   * @param secondMapping - the second mapping which is compared to the first one
   * @returns {boolean} true if the values are equal or false if not
   */
  compareMappings(firstMapping, secondMapping) {
    if (!firstMapping || !secondMapping) {
      return false;
    }

    var mappingsAreEqual = true;
    var firstMappingsKeys = Object.keys(firstMapping);

    for (var i = 0; i < firstMappingsKeys.length; i++) {
      var field = firstMappingsKeys[i];
      var firstMappingValue = firstMapping[field].value;
      var secondMappingValue = secondMapping[field].value;

      mappingsAreEqual = this.compareValues(firstMappingValue, secondMappingValue);
      if (!mappingsAreEqual) {
        break;
      }
    }
    return mappingsAreEqual;
  }

  compareValues(firstValue, secondValue) {
    // Sometimes one could be an empty array and the other null - this is an issue from select2.
    if (Array.isArray(firstValue) || Array.isArray(secondValue)) {
      var firstValueAsArray = firstValue || [];
      var secondValueAsArray = secondValue || [];
      if (!_.isEqual(firstValueAsArray, secondValueAsArray)) {
        return false;
      }
    } else if (firstValue !== secondValue) {
      return false;
    }
    return true;
  }

  setCriteria() {
    // The criteria mapping contains the internal model of the basic search form.
    this.criteriaMapping = BasicSearchCriteria.getCriteriaMapping();

    var queryBuilder = this.config.searchMediator.queryBuilder;
    var tree = queryBuilder.tree;

    if (!tree) {
      queryBuilder.init({});
      tree = queryBuilder.tree;
    }

    if (tree.rules.length < 1) {
      // Assign any defaults to the criteria mapping
      this.addDefaultCriteria(this.criteriaMapping);

      // The incoming criteria is empty so a default will be assigned.
      var defaultAdvCriteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      // Original reference is preserved!
      Object.assign(tree, defaultAdvCriteria);

      // Populating the external model
      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(this.criteriaMapping, defaultAdvCriteria);
    } else {
      // The incoming search criteria contains rules so the form will be populated with it.
      BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(this.criteriaMapping, tree);
    }
  }

  /**
   * Constructs a mapping of the basic search criteria fields names to their representing rule object.
   */
  static getCriteriaMapping() {
    var mapping = {};
    FIELDS.forEach((field) => {
      mapping[field] = {id: uuid(), field: field, operation: 'in', value: []};
    });
    // Those should be strings.
    mapping.freeText.value = '';
    mapping.createdFromDate.value = '';
    mapping.createdToDate.value = '';
    return mapping;
  }

  addDefaultCriteria(mapping) {
    this.addDefaultContextCriteria(mapping);
    this.appendPredefinedTypes(mapping);
  }

  /**
   *  Assigns default context criteria "current object" if the provided criteria does not have rules.
   *  If the isRootContext configuration is specified, the context is set as the root of the current context
   *  otherwise if the current context has no root, the context is left empty
   */
  addDefaultContextCriteria(mapping) {
    if (this.context) {
      //when root context is not specified just set to current object
      if (!this.config.useRootContext) {
        mapping.context.value = [CURRENT_OBJECT];
      } else {
        this.addRootContext(mapping);
      }
    }
  }

  addRootContext(mapping) {
    if (!this.currentObject) {
      return;
    }

    // Current context path of the instanceObject
    let contextPath = this.currentObject.getContextPath();

    if (contextPath) {
      // Root path is the last element of the contextPath
      let parentPath = contextPath[contextPath.length - 1];
      // Current path is the first element of the contextPath
      let currentPath = contextPath[0];

      //Assign root id only if the root is not the current object
      if (parentPath && parentPath.id !== currentPath.id) {
        mapping.context.value = [parentPath.id];
      }
    }
  }

  /**
   * Assigns default predefined object types (if any) to the provided mapping. Usefull for widget configurations.
   */
  appendPredefinedTypes(mapping) {
    var types = this.config.predefinedTypes;
    if (types && types.length > 0) {
      mapping.types.value = this.config.predefinedTypes;
    }
  }

  clear() {
    this.clearCriteria();
    this.clearResults();
    this.resetDateFields(this.createdFromDate, this.createdToDate);
  }

  resetDateFields(...fields) {
    var date = new Date();
    fields.map(field => field.data('DateTimePicker')).forEach(picker => picker.viewDate(date));
  }

  clearCriteria() {
    let queryBuilder = this.config.searchMediator.queryBuilder;
    // Removing any rules so setCriteria will assign defaults.
    queryBuilder.tree.rules.splice(0);

    this.setCriteria();
  }

  configure() {
    this.typesConfig = {
      classFilter: this.config.predefinedTypes
    };

    this.relationshipsConfig = this.createSelect2Config('select.relationships.placeholder');

    this.contextConfig = this.createSelect2Config('select.search.context.placeholder');
    if (this.config.contextualItems) {
      this.contextConfig.predefinedItems = this.config.contextualItems;
    }

    this.configureDatePickers();

    this.userSelectConfig = this.createSelect2Config('select.users.placeholder');

    this.savedSearchSelectConfig = {
      searchMediator: this.config.searchMediator
    };
  }

  onFtsKeyPress(event) {
    if (Keys.isEnter(event.keyCode)) {
      this.search();
    }
  }

  configureDatePickers() {
    this.createdOnConfigs = {
      from: this.createDatePickerConfig(DATE_CRITERIA_TYPE_FROM, 'search.createdfrom.placeholder'),
      to: this.createDatePickerConfig(DATE_CRITERIA_TYPE_TO, 'search.createdto.placeholder')
    };
    this.createdOnConfigs.from.defaultValue = this.criteriaMapping.createdFromDate.value || '';
    this.createdOnConfigs.to.defaultValue = this.criteriaMapping.createdToDate.value || '';
  }

  createDatePickerConfig(type, placeholder) {
    var typeFrom = type === DATE_CRITERIA_TYPE_FROM;

    var onChange = (event) => {
      if (typeFrom) {
        this.createdToDate.data('DateTimePicker').minDate(event.date);
      } else {
        this.createdFromDate.data('DateTimePicker').maxDate(event.date);
      }
    };

    var pickerConfig = {
      hideTime: true,
      useCurrent: false,
      listeners: {
        'dp.change': [onChange]
      },
      placeholder: placeholder,
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT)
    };

    if (!typeFrom) {
      _.merge(pickerConfig, {
        beforeModelUpdate: (date) => date.hours(23).minutes(59).seconds(59)
      });
    }
    return pickerConfig;
  }

  createSelect2Config(placeholder) {
    return {
      multiple: true,
      placeholder: placeholder ? this.translateService.translateInstant(placeholder) : ''
    };
  }

  get createdFromDate() {
    return this.$element.find('.criteria-field-created-from .datetime');
  }

  get createdToDate() {
    return this.$element.find('.criteria-field-created-to .datetime');
  }
}