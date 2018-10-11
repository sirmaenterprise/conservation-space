import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import 'instance/instance-list';

import './picker-basket.css!css';
import template from './picker-basket.html!text';

const EMPTY_BASKET_MESSAGE = 'picker.basket.none';

/**
 * Wrapper component designed to configure {@link InstanceList} for displaying current picker selection.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'picker-basket',
  properties: {
    config: 'config'
  }
})
@View({
  template: template
})
export class PickerBasket extends Configurable {
  constructor() {
    super({
      selectAll: false,
      selectableItems: true,
      singleSelection: true,
      linkRedirectDialog: true,
      emptyListMessage: EMPTY_BASKET_MESSAGE
    });

    this.basketListConfig = {
      selectAll: this.config.selectAll,
      selectableItems: this.config.selectableItems,
      singleSelection: this.config.singleSelection,
      selectionHandler: this.config.selectionHandler,
      emptyListMessage: this.config.emptyListMessage,
      linkRedirectDialog: this.config.linkRedirectDialog
    };

    this.selectedItems = this.config.selectedItems;
  }
}
