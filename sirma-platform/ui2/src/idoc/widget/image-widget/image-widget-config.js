import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configuration} from 'common/application-config';
import {SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import imageWidgetConfigTemplate from './image-widget-config.html!text';
import './image-widget-config.css!css';

const TYPES_CONFIG = 'widget.image.types';

@WidgetConfig
@View({
  template: imageWidgetConfigTemplate
})
@Inject(ObjectSelectorHelper, Configuration)
class ImageWidgetConfig {
  constructor(objectSelectorHelper, configuration) {
    this.objectSelectorHelper = objectSelectorHelper;
    this.config.selectObjectMode = this.config.selectObjectMode || SELECT_OBJECT_MANUALLY;

    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);

    this.config.criteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      selection: MULTIPLE_SELECTION,
      criteria: this.config.criteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch: triggerSearch,
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        var selectObjectMode = onSelectorChangedPayload.selectObjectMode;
        this.config.selectObjectMode = selectObjectMode;
        this.config.criteria = onSelectorChangedPayload.searchCriteria;
        this.config.searchMode = onSelectorChangedPayload.searchMode;
        this.config.orderBy = onSelectorChangedPayload.orderBy;
        this.config.orderDirection = onSelectorChangedPayload.orderDirection;
        delete this.config.selectedObjects;
        if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
        } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.searchResults);
        }
      },
      orderBy: this.config.orderBy,
      orderDirection: this.config.orderDirection,
      searchMode: this.config.searchMode,
      predefinedTypes: configuration.getArray(TYPES_CONFIG)
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