import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {NamespaceService} from 'services/rest/namespace-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'recent-objects/recent-objects-list';

import './picker-recent-objects.css!css';
import template from './picker-recent-objects.html!text';

const EMPTY_RECENT_OBJECTS_MESSAGE = 'picker.recent.none';

/**
 * Wrapper component for configuring {@link RecentObjectsList} to be displayed as a picker extension.
 *
 * Additionally it watches for changes in the search criteria and determines what object types can be selected
 * from the recently used objects list.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'picker-recent-objects',
  properties: {
    config: 'config'
  }
})
@View({
  template: template
})
@Inject(NgScope, NamespaceService)
export class PickerRecentObjects extends Configurable {
  constructor($scope, namespaceService) {
    super({
      selectableItems: true,
      singleSelection: true,
      emptyListMessage: EMPTY_RECENT_OBJECTS_MESSAGE
    });

    this.recentObjectsListConfig = {
      selectableItems: this.config.selectableItems,
      singleSelection: this.config.singleSelection,
      selectionHandler: this.config.selectionHandler,
      emptyListMessage: this.config.emptyListMessage,
      exclusions: this.config.exclusions
    };

    this.selectedItems = this.config.selectedItems;
    this.typesFilter = [];

    this.registerCriteriaWatcher($scope, namespaceService);
  }

  /**
   * Watches for changes in the search criteria and determines what object types can be selected
   * in {@link InstanceList} when changed.
   */
  registerCriteriaWatcher($scope, namespaceService) {
    var isUri = namespaceService.isUri.bind(namespaceService);
    $scope.$watch(() => {
      return this.config.criteria;
    }, (tree) => {
      this.typesFilter = SearchCriteriaUtils.getTypesFromCriteria(tree).filter(isUri);
    }, true);
  }
}
