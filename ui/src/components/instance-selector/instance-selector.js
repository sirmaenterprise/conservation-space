import _ from 'lodash';
import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB, NO_HEADER} from 'instance-header/header-constants';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {InstanceRestService} from 'services/rest/instance-service';
import {DialogService} from 'components/dialog/dialog-service';
import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {IdocContextFactory} from  'services/idoc/idoc-context-factory';

import 'font-awesome/css/font-awesome.css!';
import './instance-selector.css!';
import template from './instance-selector.html!text';

const INSTANCE_SELECTOR_PROPERTIES = ['id', HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB];

@Component({
  selector: 'seip-instance-selector',
  properties: {
    'instanceModelProperty': 'instance-model-property',
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(PickerService, InstanceRestService, IdocContextFactory)
export class InstanceSelector extends Configurable {
  constructor(pickerService, instanceRestService, idocContextFactory) {
    super({
      selection: SINGLE_SELECTION,
      mode: MODE_EDIT
    });

    this.pickerService = pickerService;
    this.idocContextFactory = idocContextFactory;
    this.instanceRestService = instanceRestService;

    if (this.config.instanceHeaderType && this.config.instanceHeaderType !== NO_HEADER) {
      this.headerType = this.config.instanceHeaderType;
    } else {
      this.headerType = HEADER_COMPACT;
    }
  }

  /**
   * Remove selected item from the instanceModelProperty value by index
   * @param index
   */
  removeSelectedItem(index) {
    let instanceModelPropertyArray = _.clone(this.instanceModelProperty.value);
    instanceModelPropertyArray.splice(index, 1);
    this.instanceModelProperty.value = instanceModelPropertyArray;
  }

  /**
   * Opens object picker dialog using the current iDoc context or without it if the component is used in
   * contextless scenario.
   */
  select() {
    let currentContext = this.idocContextFactory.getCurrentContext();
    if (currentContext) {
      currentContext.getCurrentObject().then((currentObject) => {
        this.openPicker(currentObject , currentContext);
      });
    } else {
      this.openPicker();
    }
  }

  openPicker(currentObject, currentContext) {
    // instanceModelProperty can be modified outside of the picker (via X button next to the header) so selectedItems should be updated before opening the picker
    var selectedItems = _.cloneDeep(this.instanceModelProperty.value);
    var pickerConfig = this.getPickerConfig(selectedItems, currentObject);
    this.pickerService.configureAndOpen(pickerConfig, currentContext).then((selectedItems) => {
      this.instanceModelProperty.value = selectedItems || [];
    });
  }

  isEditMode() {
    return this.config.mode === MODE_EDIT;
  }

  showPreviewDelimiter(index) {
    return !this.isEditMode() && index !== this.instanceModelProperty.value.length - 1;
  }

  getPickerConfig(selectedItems, currentObject) {
    var pickerConfig = {
      extensions: {}
    };
    pickerConfig.extensions[SEARCH_EXTENSION] = {
      predefinedTypes: this.config.predefinedTypes,
      results: {
        config: {
          selection: this.config.selection,
          selectedItems: selectedItems,
          exclusions: currentObject ? [currentObject.id] : []
        }
      },
      properties: INSTANCE_SELECTOR_PROPERTIES
    };
    return pickerConfig;
  }

}
