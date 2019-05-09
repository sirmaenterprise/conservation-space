import {View, Inject, NgScope} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configurable} from 'components/configurable';
import {
  SELECT_OBJECT_CURRENT,
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_AUTOMATICALLY
} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_TEXT_LEFT} from 'form-builder/form-wrapper';
import {NO_LINK} from 'idoc/widget/object-data-widget/object-data-widget';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import 'idoc/widget/properties-selector/properties-selector';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import _ from 'lodash';
import objectDataWidgetConfigTemplate from 'idoc/widget/object-data-widget/object-data-widget-config.html!text';
import 'idoc/widget/object-data-widget/object-data-widget-config.css!';

@WidgetConfig
@View({
  template: objectDataWidgetConfigTemplate
})
@Inject(NgScope, ObjectSelectorHelper, PropertiesSelectorHelper)
export class ObjectDataWidgetConfig extends Configurable {
  constructor($scope, objectSelectorHelper, propertiesSelectorHelper) {
    super({
      // Select object to be displayed tab
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      // Object details to be displayed tab
      selectedProperties: {},
      // Selected properties display types.
      selectedPropertiesData: {},
      showCodelistPropertyOptions: true,
      selection: SINGLE_SELECTION,
      // Widget display options tab
      labelPosition: LABEL_POSITION_LEFT,
      showFieldPlaceholderCondition: LABEL_POSITION_HIDE,
      labelTextAlign: LABEL_TEXT_LEFT,
      showMore: true,
      showRegionsNames: true,
      instanceLinkType: NO_LINK
    });

    this.propertiesSelectorHelper = propertiesSelectorHelper;

    // Trigger search if there is initial criteria, initial criteria is not current object and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0) && this.config.selectObjectMode !== SELECT_OBJECT_CURRENT;

    let searchCriteria = this.config.criteria || {};

    this.$scope = $scope;

    this.objectSelectorConfig = {
      criteria: searchCriteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch,
      searchMode: this.config.searchMode,
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        this.onObjectSelectorChanged(onSelectorChangedPayload);
      }
    };

    this.objectSelectorConfig.selectedItems = [];
    objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);

    this.propertiesSelectorConfig = {
      selectedProperties: this.config.selectedProperties,
      selectedPropertiesData: this.config.selectedPropertiesData,
      showCodelistPropertyOptions: this.config.showCodelistPropertyOptions
    };

    this.propertiesSelectorHelper.getDefinitionsArray(this.config, this.propertiesSelectorConfig, this.context);
  }

  onObjectSelectorChanged(onSelectorChangedPayload) {
    let previousSelectObjectMode = this.config.selectObjectMode;
    let previousSelectedObject = this.config.selectedObject;
    let previousSelectedTypes = SearchCriteriaUtils.getTypesFromCriteria(this.config.criteria);
    let selectObjectMode = onSelectorChangedPayload.selectObjectMode;
    this.config.selectObjectMode = selectObjectMode;
    this.config.criteria = _.cloneDeep(onSelectorChangedPayload.searchCriteria);
    this.config.searchMode = onSelectorChangedPayload.searchMode;

    delete this.config.selectedObject;

    this.objectSelectorConfig.triggerSearch = !!onSelectorChangedPayload.searchCriteria;

    if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
      this.setSelectedObject(onSelectorChangedPayload.selectedItems);
      // reload definitions only if selected object is changed
      if (previousSelectedObject !== this.config.selectedObject) {
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

  setSelectedObject(objectsArray) {
    if (objectsArray && objectsArray.length === 1) {
      this.config.selectedObject = objectsArray[0].id;
    }
  }
}
