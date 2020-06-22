import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import 'recent-objects/recent-objects-list';

import './picker-recent-objects.css!css';
import template from './picker-recent-objects.html!text';

const EMPTY_RECENT_OBJECTS_MESSAGE = 'picker.recent.none';

/**
 * Wrapper component for configuring {@link RecentObjectsList} to be displayed as a picker extension.
 *
 * If the recent object should visualize only specific object types then <code>config.predefinedTypes</code>
 * should be provisioned with full URIs.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'picker-recent-objects',
  properties: {
    config: 'config'
  }
})
@View({template})
@Inject()
export class PickerRecentObjects extends Configurable {
  constructor() {
    super({
      predefinedTypes: [],
      selectableItems: true,
      singleSelection: true,
      linkRedirectDialog: true,
      emptyListMessage: EMPTY_RECENT_OBJECTS_MESSAGE
    });
  }

  ngOnInit() {
    this.recentObjectsListConfig = {
      propertiesToLoad: this.config.propertiesToLoad,
      exclusions: this.config.exclusions,
      selectableItems: this.config.selectableItems,
      singleSelection: this.config.singleSelection,
      selectionHandler: this.config.selectionHandler,
      emptyListMessage: this.config.emptyListMessage,
      linkRedirectDialog: this.config.linkRedirectDialog,
      filterByWritePermissions: this.config.filterByWritePermissions
    };

    this.assignIdentifiersFilter();
    this.selectedItems = this.config.selectedItems;
    this.typesFilter = this.config.predefinedTypes;
  }

  assignIdentifiersFilter() {
    if (this.config.restrictionFilter) {
      this.recentObjectsListConfig.identifiersFilter = this.config.restrictionFilter;
    }
  }
}
