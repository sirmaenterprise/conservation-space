import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configurable} from 'components/configurable';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_CURRENT
} from 'idoc/widget/object-selector/object-selector';
import {TranslateService} from 'services/i18n/translate-service';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'components/select/instance/instance-select';
import {FORBIDDEN} from 'idoc/idoc-constants';
import _ from 'lodash';

import template from './chart-view-widget-config.html!text';
import './chart-view-widget-config.css!';

@WidgetConfig
@View({template})
@Inject(ObjectSelectorHelper, PropertiesSelectorHelper, TranslateService)
export class ChartViewWidgetConfig extends Configurable {
  constructor(objectSelectorHelper, propertiesSelectorHelper, translateService) {
    super({
      selection: MULTIPLE_SELECTION,
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      selectedProperties: {}
    });

    this.propertiesSelectorHelper = propertiesSelectorHelper;

    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);
    let searchCriteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      selection: this.config.selection,
      criteria: searchCriteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch,
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
      data: [{id: this.config.groupBy, text: this.config.groupBy}]
    };

    let chartTypes = PluginRegistry.get('chart-view-charts').map((chart) => {
      return {
        id: chart.name,
        text: translateService.translateInstant(chart.label)
      };
    });

    this.chartTypeSelectorConfig = {
      width: '100%',
      data: chartTypes,
      defaultToFirstValue: true,
      defaultValue: this.config.chartType
    };

    this.createGroupBySelectorModel();
  }

  onObjectSelectorChanged(onSelectorChangedPayload) {
    let previousSelectObjectMode = this.config.selectObjectMode;
    let previousSelectedObjects = this.config.selectedObjects;
    let previousSelectedTypes = SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria);
    let selectObjectMode = onSelectorChangedPayload.selectObjectMode;
    this.config.selectObjectMode = selectObjectMode;
    this.config.criteria = _.cloneDeep(onSelectorChangedPayload.searchCriteria);
    this.config.searchMode = onSelectorChangedPayload.searchMode;

    this.config.orderBy = onSelectorChangedPayload.orderBy;
    this.config.orderDirection = onSelectorChangedPayload.orderDirection;
    delete this.config.selectedObjects;

    this.objectSelectorConfig.triggerSearch = !!onSelectorChangedPayload.searchCriteria;

    // update selected objects if mode is set to manually
    if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
      this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
    }

    if (previousSelectObjectMode !== selectObjectMode
      || !_.isEqual(previousSelectedObjects, this.config.selectedObjects)
      || !_.isEqual(previousSelectedTypes, SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria))) {
      this.createGroupBySelectorModel();
    }
  }

  setSelectedObjects(objectsArray) {
    if (objectsArray) {
      this.config.selectedObjects = objectsArray.map((value)=> {
        return value.id;
      });
    }
  }

  createGroupBySelectorModel() {
    this.propertiesSelectorHelper.getDefinitionsArray(this.config, {}, this.context).then((definitions) => {
      let commonProperties = [];
      if (!definitions || definitions.length === 0) {
        this.groupBySelectorConfig.data = commonProperties;
        return commonProperties;
      }

      let firstDefinitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definitions[0]);
      firstDefinitionProperties.forEach((firstDefinitionProperty) => {
        if (this.isGroupByProperty(firstDefinitionProperty)) {
          let labels = [firstDefinitionProperty.label];
          let isCommonProperty = true;
          for (let i = 1; i < definitions.length && isCommonProperty; i++) {
            let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definitions[i]);
            let property = _.find(properties, (property) => {
              return property.uri === firstDefinitionProperty.uri;
            });
            if (!property) {
              isCommonProperty = false;
            } else if (labels.indexOf(property.label) === -1) {
              labels.push(property.label);
            }
          }
          if (isCommonProperty) {
            commonProperties.push({
              id: firstDefinitionProperty.uri,
              text: labels.join(', ')
            });
          }
        }
      });

      this.groupBySelectorConfig.data = commonProperties;
      return commonProperties;
    });
  }

  isGroupByProperty(property) {
    return property.uri !== FORBIDDEN && ((property.controlDefinition && property.controlDefinition.identifier === 'PICKER') || property.codelist);
  }
}
