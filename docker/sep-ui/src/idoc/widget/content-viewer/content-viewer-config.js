import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configurable} from 'components/configurable';
import 'idoc/widget/object-selector/object-selector';
import {SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';

import './content-viewer-config.css!';
import template from './content-viewer-config.html!text';

export const DEFAULT_CONTENT_TYPE = 'primaryContent';
export const OCR_CONTENT_TYPE = 'ocr';

@WidgetConfig
@View({
  template
})
@Inject(ObjectSelectorHelper, TranslateService)
export class ContentViewerConfig extends Configurable {

  constructor(objectSelectorHelper, translateService) {
    super({
      selectObjectMode: SELECT_OBJECT_MANUALLY
    });

    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);
    this.config.criteria = this.config.criteria || {};

    this.objectSelectorConfig = {
      criteria: this.config.criteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch,
      // This callback is called when selectObjectMode is changed, when a search is performed or when selectedItems are changed
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        this.onObjectSelectorChanged(onSelectorChangedPayload);
      },
      searchMode: this.config.searchMode
    };

    this.objectSelectorConfig.selectedItems = [];
    objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);

    this.config.contentType = this.config.contentType || DEFAULT_CONTENT_TYPE;
    this.contentTypeConfig = {
      data: [
        {
          id: DEFAULT_CONTENT_TYPE,
          text: translateService.translateInstant('content.viewer.configure.display.original')
        },
        {
          id: OCR_CONTENT_TYPE,
          text: translateService.translateInstant('content.viewer.configure.display.ocr')
        }
      ]
    };
  }

  onObjectSelectorChanged(onSelectorChangedPayload) {
    let selectObjectMode = onSelectorChangedPayload.selectObjectMode;
    this.config.selectObjectMode = selectObjectMode;
    this.config.criteria = _.cloneDeep(onSelectorChangedPayload.searchCriteria);
    this.config.searchMode = onSelectorChangedPayload.searchMode;

    delete this.config.selectedObject;

    if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
      this.setSelectedObject(onSelectorChangedPayload.selectedItems);
    } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      this.setSelectedObject(onSelectorChangedPayload.searchResults);
    }
  }

  setSelectedObject(objectsArray) {
    if (objectsArray && objectsArray.length === 1) {
      this.config.selectedObject = objectsArray[0].id;
    }
  }
}