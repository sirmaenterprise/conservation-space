import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import _ from 'lodash';
import 'search/components/search';
import 'components/extensions-panel/extensions-panel';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {EVENT_SEARCH} from 'search/search-mediator';
import {SINGLE_SELECTION, NO_SELECTION} from 'search/search-selection-modes';
import 'instance-header/static-instance-header/static-instance-header';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import objectSelectorTemplate from 'idoc/widget/object-selector/object-selector.html!text';

export const SELECT_OBJECT_CURRENT = 'current';
export const SELECT_OBJECT_MANUALLY = 'manually';
export const SELECT_OBJECT_AUTOMATICALLY = 'automatically';

@Component({
  selector: 'seip-object-selector',
  properties: {
    config: 'config',
    context: 'context'
  }
})
@View({
  template: objectSelectorTemplate
})
@Inject(NgScope, PickerService)
export class ObjectSelector extends Configurable {
  constructor($scope, pickerService) {
    super({
      useRootContext: false,
      selection: SINGLE_SELECTION,
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      showIncludeCurrent: false,
      includeCurrent: false,
      selectedItems: [],
      excludeOptions: [],
      criteria: {},
      renderOptions: true,
      renderCriteria: true,
      tabsConfig: {},
      paginationConfig: undefined,
      renderToolbar: true,
      renderPagination: true,
      exclusions: []
    });
    this.$scope = $scope;
    this.pickerService = pickerService;
  }

  ngOnInit() {
    // This criteria is send back when using onObjectSelectorChanged listener.
    // It is updated only when a search is performed.
    this.searchCriteria = _.cloneDeep(this.config.criteria);
    this.includeCurrent = this.config.includeCurrent;

    let searchCallbacks = {};
    searchCallbacks[EVENT_SEARCH] = (searchResult) => {
      this.searchCriteria = _.cloneDeep(searchResult.query.tree);
      this.config.searchMode = searchResult.searchMode;

      if (searchResult.response.config && searchResult.response.config.params) {
        this.config.orderBy = searchResult.response.config.params.orderBy;
        this.config.orderDirection = searchResult.response.config.params.orderDirection;
      }

      this.onObjectSelectorChanged();
    };

    // Clone for the search configurations. Using the one from this.config.criteria or this.searchCriteria will cause
    // criteria which is up to date to leaks in and cause bugs.
    var criteriaClone = _.cloneDeep(this.config.criteria);
    this.searchConfig = this.getSearchConfiguration(this.config, criteriaClone, searchCallbacks);
    this.pickerSearchConfig = this.getPickerSearchConfiguration(this.config, criteriaClone, searchCallbacks);

    // configuration for extensions panel for picker extension point
    this.pickerConfig = {
      extensions: {},
      inclusions: this.config.tabsConfig.inclusions
    };
    this.pickerConfig.extensions[SEARCH_EXTENSION] = this.pickerSearchConfig;
    this.pickerService.assignDefaultConfigurations(this.pickerConfig);

    this.$scope.$watch(() => {
      return this.config.selectObjectMode;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.onSelectObjectModeChanged();
        this.onObjectSelectorChanged();
      }
    });

    this.onSelectObjectModeChanged();
  }

  /**
   * Based on the provided flat configuration, it creates a configuration tree which can be used by the search
   * component.
   * @param config - the flat configuration
   * @param criteria - the search criteria to be used by the search component
   * @param searchCallbacks - callbacks to be assigned to the constructed configuration
   * @returns constructed tree configuration
   */
  getSearchConfiguration(config, criteria, searchCallbacks) {
    return {
      useRootContext: config.useRootContext,
      criteria,
      searchMode: config.searchMode,
      renderCriteria: config.renderCriteria,
      paginationConfig: config.paginationConfig,
      renderToolbar: config.renderToolbar,
      renderPagination: config.renderPagination,
      results: {
        config: {
          selection: NO_SELECTION
        },
        data: []
      },
      arguments: {
        orderBy: config.orderBy,
        orderDirection: config.orderDirection,
        orderByCodelistNumbers: config.orderByCodelistNumbers
      },
      callbacks: searchCallbacks,
      triggerSearch: config.triggerSearch,
      predefinedTypes: config.predefinedTypes
    };
  }

  /**
   * Based on the provided flat configuration, it creates a configuration tree which can be used by the search
   * component in a picker mode.
   * @param config - the flat configuration
   * @param criteria - the search criteria to be used by the search component
   * @param searchCallbacks - callbacks to be assigned to the constructed configuration
   * @returns constructed tree configuration for picker mode
   */
  getPickerSearchConfiguration(config, criteria, searchCallbacks) {
    var pickerSearchConfig = this.getSearchConfiguration(config, criteria, searchCallbacks);
    pickerSearchConfig.results.config.selectedItems = config.selectedItems;
    pickerSearchConfig.results.config.selection = config.selection;
    pickerSearchConfig.results.config.exclusions = config.exclusions;
    return pickerSearchConfig;
  }

  /**
   * Called when select object mode is changed. Update/clear variables and search configuration.
   */
  onSelectObjectModeChanged() {
    // Unregister watcher
    if (this.manuallySelectSelectedItemsWatcher) {
      this.manuallySelectSelectedItemsWatcher();
    }
    switch (this.config.selectObjectMode) {
    case SELECT_OBJECT_MANUALLY:
      this.manuallySelectSelectedItemsWatcher = this.$scope.$watch(() => {
        this.config.selectedItems = this.pickerSearchConfig.results.config.selectedItems;
        return this.pickerSearchConfig.results.config.selectedItems;
      }, () => {
        this.onObjectSelectorChanged();
      }, true);
      SearchCriteriaUtils.replaceCriteria(this.pickerSearchConfig.criteria, this.searchCriteria);
      this.pickerSearchConfig.searchMode = this.config.searchMode;
      this.config.includeCurrent = false;
      break;
    case SELECT_OBJECT_AUTOMATICALLY:
      this.config.selectedItems.splice(0);
      SearchCriteriaUtils.replaceCriteria(this.searchConfig.criteria, this.searchCriteria);
      this.searchConfig.searchMode = this.config.searchMode;
      this.config.includeCurrent = this.includeCurrent;
      break;
    case SELECT_OBJECT_CURRENT:
      delete this.searchCriteria;
      this.config.selectedItems.splice(0);
      this.config.includeCurrent = false;
      this.initCurrentObject();
      break;
    default:
      break;
    }
  }

  getCurrentConfiguration() {
    if (this.config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
      return this.pickerSearchConfig;
    }
    return this.searchConfig;
  }

  /**
   * Called when any of the following actions occur: Select object mode is changed, search is performed, selected items
   * (if select object mode is manually) are changed
   */
  onObjectSelectorChanged() {
    if (_.isFunction(this.config.onObjectSelectorChanged)) {
      var currentConfiguration = this.getCurrentConfiguration();
      var onSelectorChangedPayload = {
        selectObjectMode: this.config.selectObjectMode,
        searchCriteria: this.searchCriteria,
        selectedItems: this.config.selectedItems,
        searchResults: currentConfiguration.results.data,
        searchMode: this.config.searchMode,
        orderBy: this.config.orderBy,
        orderDirection: this.config.orderDirection
      };
      this.config.onObjectSelectorChanged(onSelectorChangedPayload);
    }
  }

  initCurrentObject() {
    if (!this.currentObject && this.context) {
      return this.context.getCurrentObject().then((currentObject) => {
        this.currentObject = currentObject;
      });
    }
  }

  showOption(option) {
    return this.config.excludeOptions.indexOf(option) === -1;
  }

  isAutomatically() {
    return this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY;
  }
}
