import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configurable} from 'components/configurable';
import {SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import 'idoc/widget/properties-selector/properties-selector';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';
import './datatable-widget-config.css!';
import template from './datatable-widget-config.html!text';

export const DEFAULT_PAGE_SIZE = 10;

@WidgetConfig
@View({
  template
})
@Inject(ObjectSelectorHelper, PropertiesSelectorHelper, TranslateService)
export class DatatableWidgetConfig extends Configurable {
  constructor(objectSelectorHelper, propertiesSelectorHelper, translateService) {
    super({
      selection: MULTIPLE_SELECTION,
      // Select object to be displayed tab
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      // Object details to be displayed tab
      selectedProperties: {},
      selectedPropertiesData: {},
      selectedSubPropertiesData: {},
      showObjectPropertyOptions: true,
      instanceHeaderType: HEADER_COMPACT,
      pageSize: DEFAULT_PAGE_SIZE,
      displayTableHeaderRow: true,
      renderOptions : true,
      hideWidgerToolbar: true,
      renderCriteria: true,
      renderToolbar : true,
      renderPagination : true,
      enableExportToMsExcel : false
    });

    this.propertiesSelectorHelper = propertiesSelectorHelper;

    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);

    let searchCriteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      renderOptions : this.config.renderOptions,
      selection: this.config.selection,
      criteria: searchCriteria,
      renderCriteria: this.config.renderCriteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch,
      renderToolbar : this.config.renderToolbar,
      renderPagination : this.config.renderPagination,
      excludeOptions: [SELECT_OBJECT_CURRENT],
      orderBy:  this.config.orderBy,
      orderDirection : this.config.orderDirection,
      orderByCodelistNumbers : this.config.orderByCodelistNumbers,
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        this.onObjectSelectorChanged(onSelectorChangedPayload);
      },
      searchMode: this.config.searchMode,
      tabsConfig: this.config.tabsConfig
    };

    this.objectSelectorConfig.selectedItems = [];
    objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);

    this.propertiesSelectorConfig = {
      selectedProperties: this.config.selectedProperties,
      selectedPropertiesData: this.config.selectedPropertiesData,
      selectedSubPropertiesData: this.config.selectedSubPropertiesData,
      showObjectPropertyOptions: this.config.showObjectPropertyOptions
    };

    this.config.grid = this.config.grid || DatatableWidgetConfig.GRID_ON;
    this.propertiesSelectorHelper.getDefinitionsArray(this.config, this.propertiesSelectorConfig, this.context);
    this.pageSizeMenuConfig = {
      data: [
        { id: 0, text: translateService.translateInstant('common.results.all') },
        { id: 5, text: '5' },
        { id: 10, text: '10' },
        { id: 20, text: '20' },
        { id: 50, text: '50' },
        { id: 100, text: '100' }
      ]
    };
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
    this.config.orderByCodelistNumbers = onSelectorChangedPayload.orderByCodelistNumbers;
    delete this.config.selectedObjects;

    this.objectSelectorConfig.triggerSearch = !!onSelectorChangedPayload.searchCriteria;

    if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
      this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
      // reload definitions only if selected object is changed
      if (previousSelectedObjects !== this.config.selectedObjects) {
        this.propertiesSelectorHelper.getDefinitionsArray(this.config, this.propertiesSelectorConfig, this.context);
        return;
      }
    } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      // reload definitions only if selected types are changed
      if (!_.isEqual(previousSelectedTypes, SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria))) {
        this.propertiesSelectorHelper.getDefinitionsArray(this.config, this.propertiesSelectorConfig, this.context);
        return;
      }
    }
    if (previousSelectObjectMode !== selectObjectMode) {
      this.propertiesSelectorHelper.getDefinitionsArray(this.config, this.propertiesSelectorConfig, this.context);
    }
  }

  setSelectedObjects(objectsArray) {
    if (objectsArray) {
      this.config.selectedObjects = objectsArray.map((value)=> {
        return value.id;
      });
    }
  }
}

DatatableWidgetConfig.GRID_ON = 'grid-on';
