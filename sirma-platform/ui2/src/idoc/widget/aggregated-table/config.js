import {View, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {WidgetConfig} from 'idoc/widget/widget';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceSelect} from 'components/select/instance/instance-select';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {FORBIDDEN} from 'idoc/idoc-constants';
import _ from 'lodash';

import template from './config.html!text';
import './config.css!';

export const PROPERTY_TYPE_PICKER = 'PICKER';

@WidgetConfig
@View({
  template: template
})
@Inject(ObjectSelectorHelper, PropertiesSelectorHelper, TranslateService, NgTimeout)
export class AggregatedTableConfig extends Configurable {

  constructor(objectSelectorHelper, propertiesSelectorHelper, translateService, $timeout) {
    super({
      selection: MULTIPLE_SELECTION,
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      selectedProperties: {},
      showFooter: true
    });
    this.$timeout = $timeout;
    this.config.grid = this.config.grid || AggregatedTableConfig.GRID_ON;
    this.config.columnsOrder = this.config.columnsOrder || AggregatedTableConfig.NUMBERS_FIRST;
    this.propertiesSelectorHelper = propertiesSelectorHelper;

    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);
    let searchCriteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      selection: this.config.selection,
      criteria: searchCriteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch: triggerSearch,
      excludeOptions: [SELECT_OBJECT_CURRENT],
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        this.onObjectSelectorChanged(onSelectorChangedPayload);
      },
      searchMode: this.config.searchMode
    };

    this.objectSelectorConfig.selectedItems = [];
    objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);

    this.groupBySelectorConfig = {
      width: '100%',
      placeholder: translateService.translateInstant('search.select.group.by.placeholder'),
      data: []
    };

    this.createGroupBySelectorModel(this.config, this.groupBySelectorConfig, this.context);
  }

  onObjectSelectorChanged(onSelectorChangedPayload) {
    let previousSelectObjectMode = this.config.selectObjectMode;
    let previousSelectedObjects = this.config.selectedObjects;
    let previousSelectedTypes = SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria);
    let selectObjectMode = onSelectorChangedPayload.selectObjectMode;
    this.config.selectObjectMode = selectObjectMode;
    this.config.criteria = _.cloneDeep(onSelectorChangedPayload.searchCriteria);
    this.config.searchMode = onSelectorChangedPayload.searchMode;

    delete this.config.selectedObjects;

    this.objectSelectorConfig.triggerSearch = !!onSelectorChangedPayload.searchCriteria;

    if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
      this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
      // reload definitions only if selected object is changed
      if (!_.isEqual(previousSelectedObjects, this.config.selectedObjects)) {
        this.createGroupBySelectorModel(this.config, this.groupBySelectorConfig, this.context);
        return;
      }
    } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && !_.isEqual(previousSelectedTypes, SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria))) {
      // reload definitions only if selected types are changed
      this.createGroupBySelectorModel(this.config, this.groupBySelectorConfig, this.context);
      return;
    }
    if (previousSelectObjectMode !== selectObjectMode) {
      this.createGroupBySelectorModel(this.config, this.groupBySelectorConfig, this.context);
    }
  }

  setSelectedObjects(objectsArray) {
    if (objectsArray) {
      this.config.selectedObjects = objectsArray.map((value)=> {
        return value.id;
      });
    }
  }

  /**
   * Convert definition properties models to model appropriate model for select component. From definitions for all
   * selected objects are extracted codelist and data properties models and then converted.
   * @param config
   * @param groupBySelectorConfig
   * @param context
   */
  createGroupBySelectorModel(config, groupBySelectorConfig, context) {
    this.propertiesSelectorHelper.getDefinitionsArray(config, groupBySelectorConfig, context).then(() => {
      let groupByProperties;
      if (groupBySelectorConfig.definitions && groupBySelectorConfig.definitions.length > 0) {
        let firstDefinitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(groupBySelectorConfig.definitions[0]);
        groupByProperties = firstDefinitionProperties.filter((property) => {
          let isGroupByProperty = true;
          for (let i = 0; i < groupBySelectorConfig.definitions.length && isGroupByProperty; i++) {
            isGroupByProperty = this.isGroupByProperty(groupBySelectorConfig.definitions[i], property.name);
          }
          return isGroupByProperty;
        }).map((property) => {
          return _.clone(property);
        });
      }
      this.groupBySelectorConfig.data = AggregatedTableConfig.convertToSelectorModel(groupByProperties || []);
      if (this.config.groupBy) {
        this.onGroupByChanged(this.config.groupBy.name);
      }
    });
  }

  isGroupByProperty(definition, propertyName) {
    let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
    return properties.some((property) => {
      if ((property.controlDefinition && property.controlDefinition.identifier === PROPERTY_TYPE_PICKER) || property.codelist) {
        return property.name === propertyName;
      }
    });
  }

  /**
   * Converts properties view model to model usable from select component.
   *
   * !!! Some properties are returned with 'FORBIDDEN' as their URI and thus they are omited from the list.
   *
   * @param properties view models for collected properties for grouping
   * @returns {*}
   */
  static convertToSelectorModel(properties) {
    return properties.filter((property) => {
      if (property.uri !== FORBIDDEN) {
        return property;
      }
    }).map((property) => {
      return {
        id: property.uri,
        text: property.label
      };
    });
  }

  onGroupByChanged(propertyName) {
    if (propertyName) {
      let property = this.groupBySelectorConfig.data.reduce((filtered, current) => {
        if (current.id === propertyName) {
          filtered.push(current);
        }
        return filtered;
      }, [])[0];
      if (property) {
        this.config.groupBy = {name: property.id, label: property.text};
      } else {
        this.config.groupBy = undefined;
      }
    }
    if (this.config.groupBy) {
      // timeout is added because instance-select recreates the element after the data is loaded so setting default value must be done on the next digest cycle
      this.$timeout(() => {
        this.groupBy = this.config.groupBy.name;
      });
    }
  }
}

AggregatedTableConfig.GRID_ON = 'grid-on';
AggregatedTableConfig.NUMBERS_FIRST = 'numbers-first';
AggregatedTableConfig.VALUES_FIRST = 'values-first';