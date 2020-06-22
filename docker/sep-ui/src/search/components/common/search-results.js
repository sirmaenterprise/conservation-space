import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import {NO_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import 'instance/instance-list';

import './search-results.css!';
import template from './search-results.html!text';

/**
 * Container for displaying search results with different selection modes.
 *
 * Internally uses {@link InstanceList}.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-search-results',
  properties: {
    'config': 'config',
    'results': 'results',
    'selectedItems': 'selected-items'
  }
})
@View({
  template
})
export class SearchResults extends Configurable {

  constructor() {
    super({
      linkRedirectDialog: false,
      selection: NO_SELECTION,
      exclusions: [],
      renderMenu: false
    });

    this.searchResultsListConfig = {
      selectableItems: this.config.selection !== NO_SELECTION,
      singleSelection: this.config.selection === SINGLE_SELECTION,
      linkRedirectDialog: this.config.linkRedirectDialog,
      selectionHandler: this.config.selectionHandler,
      exclusions: this.config.exclusions,
      renderMenu: this.config.renderMenu,
      placeholder: this.config.placeholder
    };
  }
}