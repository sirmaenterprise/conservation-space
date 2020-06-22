import {Component, View, Inject, NgScope} from 'app/app';
import _ from 'lodash';
import {EVENT_CRITERIA_RESET} from 'search/search-mediator';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import 'components/select/object/object-type-select';
import 'search/components/advanced/advanced-search-criteria';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import {SearchCriteriaUtils, ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {TranslateService} from 'services/i18n/translate-service';

import './advanced-search-section.css!css';
import template from './advanced-search-section.html!text';

/**
 * Component combining an object type select and advanced search criteria.
 *
 * The component depends on providing a criteria object as component property. The criteria should have a condition
 * and an id. If no rules are defined, default ones will be added. Example criteria structure:
 * <code>
 * {
 *    condition: 'AND',
 *    rules: [{
 *      // A rule for the object type select
 *      field: 'types',
 *      value: []
 *    }, {
 *      // A condition for the nested criteria rules rendered as the tree
 *      condition: 'AND',
 *      rules:[]
 *    }]
 * }
 * </code>
 *
 * The component depends also on a provided search mediator to perform adding and removing of criteria.
 *
 * The provided configuration object is passed down to the criteria. Example configuration for this component:
 *  {
 *    searchMediator: {..},
 *    disabled: false
 *  }
 *
 * The component relies on providing a <code>properties</code> loader function through the <code>loaders</code> property
 * so it could fetch the available properties,
 *
 * When the object type select is initialized, it returns the retrieved and converted object types via the callback
 * function where value change is triggered to fetch the properties and render the criteria.
 *
 * The component supports initial criteria, but if the object type is changed it will be reset with default criteria.
 * Additionally if the model is changed externally, it will rebuild the form and assign default values if necessary.
 *
 * Component can be considered initialized when the default criteria is assigned, although object type select & nested
 * criteria rows could modify the criteria tree but will not change the outcome of the search.
 * If predefined criteria is provided to the advanced search it will not be modified.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-section',
  properties: {
    'config': 'config',
    'context': 'context',
    'criteria': 'criteria',
    'loaders': 'loaders'
  }
})
@View({template})
@Inject(NgScope, TranslateService)
export class AdvancedSearchSection extends SearchCriteriaComponent {

  constructor($scope, translateService) {
    super({});
    this.$scope = $scope;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.assignDefaultCriteria();
    this.transformTypesModel();
    this.registerModelWatchers();
    this.createObjectTypeConfig();
    this.afterInit();
  }

  /**
   * Ensures that the model is an array in the case where it could be single valued string. If the value is not an
   * array, the select component will convert it to one and it will cause an extra digest cycle which will reset the
   * whole search tree because it will be considered a change.
   * Additionally it ensures that the type rule will have a correct field value -> field="type"
   */
  transformTypesModel() {
    if (!Array.isArray(this.criteria.rules[0].value)) {
      this.criteria.rules[0].value = [this.criteria.rules[0].value];
    }
    // Ensuring the types rule will always have field value correct.
    this.criteria.rules[0].field = SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD;
  }

  registerModelWatchers() {
    this.$scope.$watch(() => {
      // If the criteria was changed externally, it may need to be assigned with default criteria condition and
      // rules to avoid errors
      this.assignDefaultCriteria();
      return this.criteria.rules[0].value;
    }, (newType, oldType) => {
      this.criteriaReset(newType, oldType);
      this.resetToAnyObject(newType, oldType);
      this.onObjectTypeChange();
    });
  }

  /**
   * Assigns default object type criteria and default criteria if the provided criteria lacks them.
   */
  assignDefaultCriteria() {
    if (!this.criteria.rules || this.criteria.rules.length < 1) {
      let predefinedType = this.getPredefinedType();
      let defaultTypeCriteria = SearchCriteriaUtils.getDefaultObjectTypeRule(predefinedType);
      let defaultCriteria = SearchCriteriaUtils.getDefaultCriteriaCondition();
      this.criteria.rules = [];
      this.config.searchMediator.addCriteria(defaultTypeCriteria, this.criteria.id);
      this.config.searchMediator.addCriteria(defaultCriteria, this.criteria.id);
    } else if (this.criteria.rules.length < 2) {
      // If the criteria lacks inner condition
      this.config.searchMediator.addCriteria(SearchCriteriaUtils.getDefaultCriteriaCondition(), this.criteria.id);
    }
  }

  getPredefinedType() {
    if (this.config.predefinedTypes && this.config.predefinedTypes.length > 0) {
      return this.config.predefinedTypes;
    }
  }

  createObjectTypeConfig() {
    this.objectTypeSelectConfig = {
      defaultToFirstValue: true,
      defaultValue: this.criteria.rules[0].value,
      isDisabled: () => this.isLockedOrDisabled(AdvancedSearchComponents.TYPE),
      multiple: true,
      classFilter: this.config.predefinedTypes,
      predefinedData: this.getImplicitTypes(),
      publishCallback: types => this.objectTypeSelectCallback(types),
      dataLoader: this.loaders.models
    };
  }

  /**
   * Triggered after the object types are obtained, it sets the available object types and triggers the initial change.
   */
  objectTypeSelectCallback(objectTypes) {
    this.objectTypes = objectTypes;

    // Triggers initial change after object types are obtained
    this.onObjectTypeChange();
  }

  /**
   * Retrieves the properties for the new type and retrieves its properties. If there is no object type set in the
   * tree model, the first one from the set will be used by default.
   */
  onObjectTypeChange() {
    if (this.objectTypes) {
      // If there is no defined object type yet - assign the first one by default
      var modelValue = this.criteria.rules[0].value;
      if (!modelValue) {
        this.criteria.rules[0].value = [this.objectTypes[0].id];
        // Returning here to ignore double property loading when the model watcher is triggered after the value assignment.
        return;
      } else if (modelValue.length > 1 && _.includes(modelValue, ANY_OBJECT)) {
        // If with Any object there is a concrete object type - remove Any object
        this.trimImplicitTypes(modelValue);
      }

      // Deleting any previous properties will force the form to rebuild
      delete this.criteriaProperties;

      var selectedObjectTypes = this.getSelectedObjectTypes();
      var types = this.getObjectTypeValues(selectedObjectTypes);
      if (_.includes(types, ANY_OBJECT)) {
        types = null;
      }

      this.loaders.properties(types).then((properties) => {
        if (properties.length > 0) {
          this.criteriaProperties = properties;
        }
      });
    }
  }

  resetToAnyObject(newType, oldType) {
    // Should reset to ANY_OBJECT if the last selected type is ANY_OBJECT
    // Select 2 does not preserve order so we need to check if the new model contains ANY_OBJECT and the old one does not
    if (newType && oldType && _.includes(newType, ANY_OBJECT) && !_.includes(oldType, ANY_OBJECT)) {
      this.criteria.rules[0].value = ANY_OBJECT;
    }
  }

  criteriaReset(newType, oldType) {
    // Should reset the criteria only if the new type is different (it should not reset if restoring criteria)
    if (newType && newType.length > 0 && _.difference(newType, oldType).length > 0) {
      this.resetCriteria();
      this.config.searchMediator.trigger(EVENT_CRITERIA_RESET);
    }
  }

  trimImplicitTypes(array) {
    _.remove(array, (type) => {
      return type === ANY_OBJECT;
    });
  }

  getImplicitTypes() {
    // Do not add implicit types if predefined types are specified and are not empty
    if (this.config.predefinedTypes && this.config.predefinedTypes.length > 0) {
      return [];
    }
    return [{
      text: this.translateService.translateInstant('search.advanced.value.anyObject'),
      id: ANY_OBJECT
    }];
  }

  /**
   * Removes any previous criteria and assigns a default one to be visualized in the criteria component.
   */
  resetCriteria() {
    if (this.criteria.rules[1]) {
      this.config.searchMediator.removeCriteria(this.criteria.rules[1]);
    }
    var defaultCriteria = SearchCriteriaUtils.getDefaultCriteriaCondition();
    this.config.searchMediator.addCriteria(defaultCriteria, this.criteria.id);
  }

  /**
   * Gets the selected object types <b>objects</b> from the available object types.
   */
  getSelectedObjectTypes() {
    return _.filter(this.objectTypes, (objectType) => {
      return _.findIndex(this.criteria.rules[0].value, (value) => {
        return value === objectType.id;
      }) !== -1;
    });
  }

  /**
   * Maps all objectTypes into an array by id
   */
  getObjectTypeValues(objectTypes) {
    return _(objectTypes).map('id').value();
  }
}
