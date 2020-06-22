import _ from 'lodash';
import {View, Component} from 'app/app';
import 'instance/instance-list';
import {PickerService} from 'services/picker/picker-service';

import template from 'sandbox/instance/instance-list/instance-list-stub.html!text';

@Component({
  selector: 'instance-list-stub'
})
@View({
  template
})
export class InstanceListStub {

  constructor() {
    this.picker = new PickerService();
    this.itemsInput = $('#items');
    this.selectedItems = [];

    this.instances = _.range(1, 5)
      .map((id) => {
        return {
          id,
          headers: {
            default_header: `Default header for #${id}`
          }
        };
      });

    this.selectable = {
      selectableItems: true,
      selectionHandler: this.singleSelectionHandler.bind(this)
    }

    this.multiselectable = {
      selectableItems: true,
      singleSelection: false,
      selectionHandler: this.multiSelectionHandler.bind(this)
    }

    this.exclusions = {
      selectableItems: true,
      selectionHandler: this.singleSelectionHandler.bind(this),
      exclusions: [1]
    }
  }

  /**
   * Sets the input field to the array of selected object ids
   * @param items the array of selected objects
   */
  setItemsInputValue(items) {
    let filtered = _.cloneDeep(items);
    filtered.forEach((item, index, array) => {
      array[index] = item.id;
    });

    this.itemsInput.val(JSON.stringify(filtered));
  }

  /**
   * Multi selection handler which adds an item if it does not exist or removes already existing item. Mimicking the effect of a checkbox
   * @param item the item to be added or removed
   */
  multiSelectionHandler(item) {
    this.picker.handleSelection(this.selectedItems, false, item);
    this.setItemsInputValue(this.selectedItems);
  }

  /**
   * Single selection handler adds a single items at the beginning of the array
   * @param item the item to be added at array's head
   */
  singleSelectionHandler(item) {
    this.selectedItems = [];
    this.picker.handleSelection(this.selectedItems, true, item);
    this.setItemsInputValue(this.selectedItems);
  }
}