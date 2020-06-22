import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_AUTOMATICALLY
} from 'idoc/widget/object-selector/object-selector';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import template from './business-process-diagram-widget-config.html!text';
import './business-process-diagram-widget-config.css!';

export const TYPES = 'emf:BusinessProcess';

@WidgetConfig
@View({template})
@Inject(ObjectSelectorHelper)
export class BusinessProcessDiagramWidgetConfig {

  constructor(objectSelectorHelper) {
    this.objectSelectorHelper = objectSelectorHelper;
    this.config.selectObjectMode = this.config.selectObjectMode || SELECT_OBJECT_MANUALLY;
    this.typesConfig = TYPES;
    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);

    this.config.criteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      selection: SINGLE_SELECTION,
      criteria: this.config.criteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch,
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        let selectObjectMode = onSelectorChangedPayload.selectObjectMode;
        this.config.selectObjectMode = selectObjectMode;
        this.config.criteria = onSelectorChangedPayload.searchCriteria;
        this.config.searchMode = onSelectorChangedPayload.searchMode;
        delete this.config.selectedObjects;
        if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
        } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.searchResults);
        }
      },
      searchMode: this.config.searchMode,
      predefinedTypes: [this.typesConfig]
    };

    this.objectSelectorConfig.selectedItems = [];
    this.objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);
  }

  setSelectedObjects(objectsArray) {
    if (objectsArray) {
      this.config.selectedObjects = objectsArray.map((value) => {
        return value.id;
      });
    }
  }

}
