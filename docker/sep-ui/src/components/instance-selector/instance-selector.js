import {View, Component, Inject, NgTimeout} from 'app/app';
import {Logger} from 'services/logging/logger';
import {Configurable} from 'components/configurable';
import {PickerService, SEARCH_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB, NO_HEADER} from 'instance-header/header-constants';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Configuration} from 'common/application-config';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {HeadersService} from 'instance-header/headers-service';
import {FormWrapper} from 'form-builder/form-wrapper';
import {EventEmitter} from 'common/event-emitter';
import {ModelUtils} from 'models/model-utils';
import {RelationshipsService} from 'services/rest/relationships-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import _ from 'lodash';
import {getNestedObjectValue} from 'common/object-utils';
import 'instance-header/static-instance-header/static-instance-header';
import 'components/select/instance/instance-suggest-select';

import 'font-awesome/css/font-awesome.css!';
import './instance-selector.css!';
import template from './instance-selector.html!text';

export const INSTANCE_SELECTOR_PROPERTIES = ['id', 'modifiedOn', HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB];
const LIMIT = -1;

/**
 * This component renders a list of objects represented by their headers. It requires a property value model in the
 * following format:
 * value: {
 *   results: [], // should contain list with object ids - initially this contains only limited amount of ids
 *   total: number, // total count of objects that this property contains
 *   offset: number //
 * }
 * Instance headers are loaded asynchronously after component is initialized. Initially only the configured number of
 * objects are returned from the backend are displayed. Once user clicks show more or select buttons, then the rest of
 * the objects and their headers are loaded and rendered. When the user makes changes they are represented in a changeset
 * which shows added and removed objects. The changeset is represented by two arrays: "add" and "remove" and is the only
 * thing that is sent to server when the instance is going to be saved.
 *
 * @param instanceModelProperty The InstanceModelProperty that this component should wrap and represent.
 * @param config:
 * {
 *    mode: ['edit'|'preview'|'print'], // the view mode applicable for the field which is different from the formViewMode
 *    selection: ['single'|'multiple'], // this is a config for the picker - if single or multiple objects could be selected
 *    objectId: 'objectId',
 *    propertyName: 'currentPropertyName', // it's the relation type and used when "show more" is selected to request the rest of the objects
 *    excludeCurrentObject: [true|false],
 *    visibleItemsCount: number, // comes from configuration and defines the initial visible items count
 *    instanceHeaderType: ['compact_header'|'default_header'|'breadcrumb_header'],
 *    predefinedTypes: [], // this is configuration for the picker and defines which types should be allowed for selection
 *    formViewMode: 'EDIT|PRINT|PREVIEW',
 *    eventEmiter: an EventEmiter instance, used to notify parent components when its rendered.
 *    isNewInstance: true|false|undefined // flag that shows if the instance is new or is already persisted
 * }
 *
 */
@Component({
  selector: 'seip-instance-selector',
  properties: {
    'instanceModelProperty': 'instance-model-property',
    'config': 'config'
  }
})
@View({
  template
})
@Inject(PickerService, InstanceRestService, IdocContextFactory, PromiseAdapter, Logger, Configuration, HeadersService, NgTimeout, RelationshipsService)
export class InstanceSelector extends Configurable {
  constructor(pickerService, instanceRestService, idocContextFactory, promiseAdapter, logger, configuration, headersService, $timeout, relationshipsService) {
    super({
      selection: SINGLE_SELECTION,
      mode: MODE_EDIT,
      visibleItemsCount: 0,
      excludedObjects: []
    });

    this.logger = logger;
    this.pickerService = pickerService;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
    this.idocContextFactory = idocContextFactory;
    this.headersService = headersService;
    this.showingMore = false;
    this.isLoading = false;
    this.eventEmitter = new EventEmitter();
    this.selectConfig = {};
    this.selectionPool = new Map();
    this.$timeout = $timeout;
    this.relationshipsService = relationshipsService;
    this.configuration = configuration;
    this.setInitialVisibleItems();
    this.eventHandlers = [];
  }

  ngOnInit() {
    this.eventHandlers.push(this.instanceModelProperty.subscribe('propertyChanged', this.onValueChanged.bind(this)));
    this.eventHandlers.push(this.eventEmitter.subscribe('destroy', this.ngOnDestroy.bind(this)));

    // set instance suggest configuration
    this.createSelectConfiguration();

    // Properties that don't have values come with value=undefined and all the logic below should account this. In order
    // to prevent many checks, the value is initialized with empty results and total=0
    if (!this.instanceModelProperty.value) {
      this.instanceModelProperty.value = ModelUtils.getEmptyObjectPropertyValue();
    } else {
      ModelUtils.normalizeObjectPropertyValue(this.instanceModelProperty);
    }

    this.resolveHeaderType();

    // Used to control the amount of visible items according to whether show more/less buttons are clicked, items are
    // removed or added. This is needed because the property's result array is used directly in the template and controls
    // the iteration count.
    this.displayObjectsCount = 0;

    if (this.config.formViewMode === FormWrapper.FORM_VIEW_MODE_PRINT) {
      this.showingMore = true;
    }
    this.calculateItemsCount();

    let compiledHeadersCount = 0;
    this.loadedSubscription = this.eventEmitter.subscribe('loaded', () => {
      if (compiledHeadersCount + 1 === this.displayObjectsCount || compiledHeadersCount + 1 === this.displayObjectsInSelectCount || compiledHeadersCount + 1 === this.instanceModelProperty.value.total) {
        this.loadedSubscription.unsubscribe();
        this.publishSelectorRendered();
      }
      compiledHeadersCount += 1;
    });

    if (this.config.formViewMode === FormWrapper.FORM_VIEW_MODE_PRINT) {
      return this.loadObjects().then(() => {
        return this.loadHeaders(this.instanceModelProperty.value.results).then((result) => {
          // empty object properties need to also publish loaded event.
          if (!result) {
            this.publishSelectorRendered();
            this.loadedSubscription.unsubscribe();
          }
        });
      });
    }
    this.eventHandlers.push(this.eventEmitter.subscribe('selecting', this.handleSuggestedSelection.bind(this)));
    this.eventHandlers.push(this.eventEmitter.subscribe('unselecting', this.handleSuggestedRemoval.bind(this)));


    // By default this component would receive only predefined number of relations in the value.results array where the
    // number depends on a configuration. Still it's possible in the property model to be populated a fare amount of
    // suggested relations when the instance is about to be created. As the suggest actually loads the related instances
    // with their headers so they are pre-populated in the value.headers map and are not loaded once again here.
    if (Object.keys(this.instanceModelProperty.value.headers).length) {
      return this.loadHeaders(this.instanceModelProperty.value.results).then(() => {
        this.updateSelectionPool();
      });
    } else {
      this.loadHeaders(this.instanceModelProperty.value.results).then(results => {
        if (results) {
          this.instanceModelProperty.value.headers = results;
          this.updateSelectionPool();
        } else {
          // empty object properties need to also publish loaded event.
          this.publishSelectorRendered();
          this.loadedSubscription.unsubscribe();
        }
      });
    }

    // set proper loaded flag depending if the needed headers are present in the model.
    this.setLoadedFlag();
  }

  createSelectConfiguration() {
    this.selectConfig.definitionId = this.config.definitionId;
    this.selectConfig.propertyName = this.config.propertyName;
    this.selectConfig.eventEmitter = this.eventEmitter;
    this.selectConfig.selectionPool = this.selectionPool;
    this.selectConfig.displayObjectsInSelectCount = this.displayObjectsInSelectCount;
  }

  setInitialVisibleItems() {
    if (this.isEditable()) {
      this.displayObjectsInSelectCount = this.getInitialLoadLimit();
      this.config.visibleItemsCount = 0;
    } else {
      this.displayObjectsInSelectCount = 0;
      this.config.visibleItemsCount = this.getInitialLoadLimit();
    }
  }

  getInitialLoadLimit() {
    return this.configuration.get(Configuration.OBJECT_PROP_INITIAL_LOAD_LIMIT);
  }

  // set proper loaded flag depending if the needed headers are present in the model.
  setLoadedFlag() {
    this.config.loaded = InstanceSelector.filterObjectsWithLoadedHeaders(this.instanceModelProperty.value.results, this.instanceModelProperty.value.headers, this.headerType).length === 0;
  }

  /**
   * Updates the selection pool. Wrapped in timeout to inform angular for upcoming changes.
   */
  updateSelectionPool() {
    return this.$timeout(() => {
      if (this.instanceModelProperty.value.results.length < this.instanceModelProperty.value.total) {
        this.loadObjects().then(() => {
          // recursion is needed, because selection can be modified during promise wait time
          this.updateSelectionPool();
        });
      } else {
        // first remove all removed items
        _.remove(this.getSelectionPoolEntries(), (item) => {
          return this.instanceModelProperty.value.results.indexOf(item) === -1;
        })
          .forEach((item) => {
            this.selectionPool.delete(item);
          });

        // then update the pool
        this.instanceModelProperty.value.results.forEach(item => {
          let headers = this.instanceModelProperty.value.headers && this.instanceModelProperty.value.headers[item];
          if (headers || !this.selectionPool.get(item)) {
            this.selectionPool.set(item, {
              id: item,
              headers
            });
          }
        });
      }
    }).then(() => {
      this.eventEmitter.publish('updatedSelection', [this.headerType, this.displayObjectsInSelectCount]);
      this.setLoadedFlag();
    });
  }

  onValueChanged(propertyChanged) {
    if (Object.keys(propertyChanged)[0] === 'value' && this.instanceModelProperty.value) {
      let resultsLength = this.instanceModelProperty.value.results.length;
      let limit = this.getInitialLoadLimit();

      if (!this.showingMore && !this.isEditable()) {
        this.displayObjectsCount = resultsLength > limit ? limit : resultsLength;
        this.loadHeaders(this.instanceModelProperty.value.results, this.displayObjectsCount).then(() => {
          this.updateSelectionPool();
        });
      } else if (this.isEditable()) {
        this.loadHeaders(this.instanceModelProperty.value.results, this.displayObjectsInSelectCount).then(() => {
          this.updateSelectionPool();
        });
      } else {
        this.displayObjectsCount = resultsLength;
        this.loadHeaders(this.instanceModelProperty.value.results, this.displayObjectsCount).then(() => {
          this.updateSelectionPool();
        });
      }
    }
  }

  publishSelectorRendered() {
    if (this.config && this.config.eventEmitter) {
      this.config.eventEmitter.publish('instanceSelectorRendered', {identifier: this.config.fieldIdentifier});
    }
  }

  setIsLoading(isLoading) {
    this.isLoading = isLoading;
  }

  resolveHeaderType() {
    if (this.config.instanceHeaderType && this.config.instanceHeaderType !== NO_HEADER) {
      this.headerType = this.config.instanceHeaderType;
    } else {
      this.headerType = HEADER_BREADCRUMB;
    }
  }

  calculateItemsCount() {
    if (this.config.selection === SINGLE_SELECTION) {
      this.displayObjectsCount = 1;
    }
    // There are more items than should be visible by default, so we limit them.
    else if (!this.showingMore && this.instanceModelProperty.value.total > this.config.visibleItemsCount) {
      this.displayObjectsCount = this.config.visibleItemsCount;
    }
    // There are less items than the minimum allowed default, so we show them all.
    else {
      this.displayObjectsCount = this.instanceModelProperty.value.total;
    }
  }

  /**
   * Calculates count of the selected objects that are not currently visible.
   * @returns {number}
   */
  getHiddenObjectsCount() {
    let count = this.instanceModelProperty.value.total - this.displayObjectsCount - this.displayObjectsInSelectCount;
    return count > 0 ? count : 0;
  }

  loadObjects() {
    // When new instance is created, all related objects are returned and no need to preload them.
    if (this.config.isNewInstance) {
      return this.promiseAdapter.resolve();
    }

    // when sub property is selected objectId should be replaced with owningRelatedObjectId
    if (this.config.owningRelatedObjectId) {
      this.config.objectId = this.config.owningRelatedObjectId;
      this.config.propertyName = this.config.subPropertyName;
    }

    this.setIsLoading(true);

    // if there are more items then fetch them all
    return this.instanceRestService.getInstanceProperty(this.config.objectId, this.config.propertyName, 0, LIMIT)
      .then((response) => {
        // Merge selectedObjects with result because the service would return the backend state for the given property
        // but in the UI some objects might be removed or added. So we need to preserve the UI state and still get the
        // rest from the backend.
        let selectedObjects = _.cloneDeep(response.data || []);
        if (this.instanceModelProperty.value.remove) {
          selectedObjects = _.pull(selectedObjects, ...this.instanceModelProperty.value.remove);
        }
        if (this.instanceModelProperty.value.add) {
          selectedObjects.push(...this.instanceModelProperty.value.add);
        }

        // Updating the results triggers propertyChange in the model and the headers are updated automatically.
        this.instanceModelProperty.value.results.splice(0, this.instanceModelProperty.value.results.length, ...selectedObjects);
        this.instanceModelProperty.value.total = this.instanceModelProperty.value.results.length;

        if (this.showingMore) {
          if (this.isEditable()) {
            this.displayObjectsCount = this.instanceModelProperty.value.total - this.displayObjectsInSelectCount;
          } else {
            this.displayObjectsCount = this.instanceModelProperty.value.total;
          }
        }

        this.setIsLoading(false);
      })
      .catch((error) => {
        this.setIsLoading(false);
        this.logger.error(error);
      });
  }

  showMore() {
    this.showingMore = true;
    // If not all selected objects are loaded, then trigger loading
    if (this.instanceModelProperty.value.results.length < this.instanceModelProperty.value.total) {
      this.loadObjects().then(() => {
        this.updateSelectionPool();
      });
    } else {
      // otherwise load missing headers and set total count to displayObjectsCount in order to display them all
      this.loadHeaders(this.instanceModelProperty.value.results, undefined).then(() => {
        this.displayObjectsCount = this.instanceModelProperty.value.total;
        this.updateSelectionPool();
      });

    }
  }

  showLess() {
    this.displayObjectsCount = this.config.visibleItemsCount;
    this.showingMore = false;
  }

  /**
   * Returns an array of instance id's where the specified id with header type is not present in the loaded headers map.
   *
   * @param ids id's to filter through
   * @param loadedHeaders map of loaded headers.
   * @param headerType type of headers that are needed to be present in the map.
   */
  static filterObjectsWithLoadedHeaders(ids, loadedHeaders, headerType) {
    let loaded = loadedHeaders || {};
    return ids.filter((id) => {
      return !loaded[id] || !getNestedObjectValue(loaded, [id, headerType]);
    });
  }

  /**
   * Instance selector is provided with identifiers of the selected objects only. Their headers should be loaded before
   * component to be able to render itself.
   * @param ids List of instance ids for which to load headers
   * @param limit limit of loaded headers count
   * @returns Promise which resolves when headers are loaded.
   */
  loadHeaders(ids, limit) {
    let headerIds = ids || [];
    let toLoad;
    if (limit) {
      if (this.isEditable()) {
        toLoad = _.takeRight(headerIds, limit);
      } else {
        toLoad = headerIds.slice(0, limit);
      }
    } else {
      toLoad = headerIds;
    }

    let idsWithMissingHeaders = InstanceSelector.filterObjectsWithLoadedHeaders(toLoad, this.instanceModelProperty.value.headers, this.headerType);
    let nothingForLoad = toLoad.length === 0 || idsWithMissingHeaders.length === 0;
    if (nothingForLoad) {
      return this.promiseAdapter.resolve();
    }

    this.setIsLoading(true);
    return this.headersService.loadHeaders(idsWithMissingHeaders, this.headerType, this.instanceModelProperty.value.headers).then(results => {
      this.instanceModelProperty.value.results.forEach(instanceId => {
        if (!results[instanceId]) {
          this.logger.warn(`No header found for instance with id: ${instanceId} !`);
        }
      });

      this.setIsLoading(false);
      this.config.loaded = true;

      return results;
    }).catch((error) => {
      this.setIsLoading(false);
      this.logger.error(error);
    });
  }

  isShowMoreButtonVisible() {
    return this.instanceModelProperty.value && this.instanceModelProperty.value.total - this.displayObjectsInSelectCount > this.displayObjectsCount
      && InstanceSelector.isMultipleSelection(this.config.selection);
  }

  isShowLessButtonVisible() {
    return this.displayObjectsCount > this.getInitialLoadLimit()
      && InstanceSelector.isMultipleSelection(this.config.selection);
  }

  static isMultipleSelection(selection) {
    return selection === MULTIPLE_SELECTION;
  }

  static removeItemFromTargetIfExists(targetArray, sourceArray, index) {
    let removedItemIndex = targetArray.indexOf(sourceArray[index]);
    if (removedItemIndex !== -1) {
      targetArray.splice(removedItemIndex, 1);
    }
  }

  /**
   * Remove selected item from the instanceModelProperty value and from the selection pool by index or id. If selected objects are already loaded, then
   * update visible items count. Otherwise load another object if necessary.
   * @param itemIndex index of removed item in selectionPoolEntries
   * @param id of removed instance object
   */
  removeSelectedItem(itemIndex, id) {
    let index = itemIndex;
    let itemId = id;
    if (itemId) {
      index = this.instanceModelProperty.value.results.indexOf(itemId);
      this.selectionPool.delete(itemId);
    } else {
      itemId = this.instanceModelProperty.value.results[index];
      this.selectionPool.delete(itemId);
    }

    // maintain duplications free array
    if (this.instanceModelProperty.value.remove.indexOf(itemId) === -1) {
      // in single selection mode, add the default value to be removed, else the property was originally empty.
      if (this.config.selection === SINGLE_SELECTION && this.instanceModelProperty.defaultValue) {
        this.instanceModelProperty.value.remove.splice(0, 1, this.instanceModelProperty.defaultValue.results[0]);
      } else {
        this.instanceModelProperty.value.remove.push(itemId);
      }
    }
    // If the selected object has been previously added to the selection, it has to be removed from there as well.
    InstanceSelector.removeItemFromTargetIfExists(this.instanceModelProperty.value.add, this.instanceModelProperty.value.results, index);

    // If control is expanded, then removing item should result in decreasing the visible objects count as well.
    if (this.showingMore) {
      this.displayObjectsCount--;
    }
    // If control is not expanded, then when item gets removed:
    else if (this.displayObjectsCount === this.config.visibleItemsCount) {
      // - If there are more selected objects than configuration count (total > config) and they are loaded (result === total),
      // then the visible objects would be shifted when the object gets removed in order to fill the minimum visible
      // items count.
      // - If there are more selected objects than configuration count (total > config) and they are not loaded (result < total),
      // then load the rest and push them in the results.
      // - If there are less selected objects than configuration count, then just decrement total and visible objects count.
      if (this.instanceModelProperty.value.total > this.config.visibleItemsCount
        && this.instanceModelProperty.value.results.length < this.instanceModelProperty.value.total) {
        return this.loadObjects().then(() => {
          this.executeCallback(this.config.onSelectionChangedCallback);
        });
      } else if (this.instanceModelProperty.value.total < this.config.visibleItemsCount) {
        this.displayObjectsCount--;
      }
    }

    this.instanceModelProperty.value.results.splice(index, 1);
    this.instanceModelProperty.value.total--;

    this.executeCallback(this.config.onSelectionChangedCallback);
  }

  /**
   * Opens object picker dialog using the current iDoc context or without it if the component is used in contextless
   * scenario.
   */
  select() {
    let currentContext = this.idocContextFactory.getCurrentContext();
    this.openPicker(currentContext);
  }

  openPicker(currentContext) {
    // If there is no instance id then no need to load objects. This is the case when create/upload operations are executed.
    if (!this.config.objectId) {
      this.initPicker(currentContext);
    } else {
      // instanceModelProperty can be modified outside of the picker (via X button next to the header) so selectedItems
      // should be updated before opening the picker
      this.loadObjects().then(() => {
        this.initPicker(currentContext);
      });
    }
  }

  /**
   * Handles selection from select2. Prepares selection set as it comes from the picker and reuses picker selection logic.
   * @param selected
   */
  handleSuggestedSelection(selected) {
    this.displayObjectsInSelectCount++;
    if (this.config.selection === SINGLE_SELECTION) {
      this.selectionPool.clear();
    } else {
      this.updateSelectionPool();
    }

    this.selectionPool.set(selected[0].id, selected[0]);
    this.handleSelection(Array.from(this.selectionPool.values()));
  }

  handleSuggestedRemoval(removedId) {
    if (this.getInitialLoadLimit() < this.displayObjectsInSelectCount) {
      this.displayObjectsInSelectCount--;
    }
    this.removeSelectedItem(undefined, removedId);
    this.eventEmitter.publish('updatedSelection', [this.headerType, this.displayObjectsInSelectCount]);
  }

  /**
   * Handles selection coming from the picker. Some of the items might have been in the result as default values and
   * others to be newly selected from the picker. The property value and the selection pool need to be updated with the
   * result of the selection properly.
   *
   * @param selectedItems
   */
  handlePickerSelection(selectedItems) {
    this.selectionPool.clear();

    // reset the visible items in holder to initial size
    this.setInitialVisibleItems();

    // Only object ids are needed
    let selectedIds = selectedItems.map((item) => {
      this.selectionPool.set(item.id, item);
      return item.id;
    });
    this.handleSelection(selectedItems, selectedIds);
  }

  handleSelection(selectedItems, selectedIds) {
    let _selectedIds = selectedIds || selectedItems.map((item) => {
      return item.id;
    });

    this.instanceModelProperty.value.total = _selectedIds.length;

    let isSingleSelection = this.config.selection === SINGLE_SELECTION;
    ModelUtils.updateObjectPropertyValue(this.instanceModelProperty, isSingleSelection, _selectedIds);

    // No need to load headers as they are present in the selected objects
    this.applyHeadersFromSelection(selectedItems);
    this.calculateItemsCount();
    this.executeCallback(this.config.onSelectionChangedCallback);

    // propertyChanged event used to be fired here for value with this.instanceModelProperty.value as payload but its
    // not needed I think as the value change is detected in the instance property model and the event is fired.
  }

  applyHeadersFromSelection(selectedItems) {
    selectedItems.forEach((selectedItem) => {
      let instanceId = selectedItem.id;
      if (!this.instanceModelProperty.value.headers[instanceId] && selectedItem.headers) {
        ModelUtils.setObjectPropertyHeader(this.instanceModelProperty, null, instanceId, selectedItem.headers[this.headerType], this.headerType);
      }
    });
  }

  executeCallback(callback) {
    if (typeof callback === 'function') {
      callback();
    }
  }

  getSelectionPoolEntries() {
    return [...this.selectionPool.keys()];
  }

  /* Makes a copy array of the selected objects IDs and removes all shown in select2.
   * The array is used to handle show more button items properly
   */
  getSelectionPoolEntriesInHolder() {
    let arr = [...this.selectionPool.keys()];
    arr.splice(-this.displayObjectsInSelectCount, this.displayObjectsInSelectCount);
    return arr;
  }

  getUniqueItems(oldSelectedItems, newSelectedItems) {
    return oldSelectedItems.filter(function (item) {
      return !newSelectedItems.some(function (newItem) {
        return item.id === newItem.id;
      });
    });
  }

  isEditMode() {
    return this.config.mode === MODE_EDIT;
  }

  isEditable() {
    return this.isEditMode() && this.config.formViewMode && FormWrapper.isEditMode(this.config.formViewMode);
  }

  getSearchCriteria(uri = []) {
    return {
      rules: [SearchCriteriaUtils.getDefaultObjectTypeRule(uri)]
    };
  }

  initPicker(currentContext) {
    this.relationshipsService.getRelationInfo(this.config.fieldUri).then((response) => {
      let selectedItems = this.getSelectionPoolEntries().map((item) => {
        return {id: item};
      });

      let pickerConfig = {
        extensions: {},
        tabs: {}
      };

      let restriction;
      if (!this.config.pickerRestrictions) {
        restriction = this.getSearchCriteria(response.data.rangeClass);
      } else {
        restriction = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria('AND');
        let typesRule = SearchCriteriaUtils.getDefaultObjectTypeRule([response.data.rangeClass]);
        restriction.rules[0].rules.push(typesRule);
        restriction.rules.unshift(this.config.pickerRestrictions);
      }

      pickerConfig.extensions[SEARCH_EXTENSION] = {
        predefinedTypes: this.config.predefinedTypes,
        restrictions: restriction,
        useRootContext: true,
        results: {
          config: {
            selectedItems,
            selection: this.config.selection,
            exclusions: this.config.excludedObjects
          }
        },
        arguments: {
          properties: INSTANCE_SELECTOR_PROPERTIES
        }
      };

      pickerConfig.tabs[BASKET_EXTENSION] = {
        label: this.config.label
      };

      this.pickerService.configureAndOpen(pickerConfig, currentContext).then(this.handlePickerSelection.bind(this));
    });
  }

  ngOnDestroy() {
    this.loadedSubscription && this.loadedSubscription.unsubscribe();
    this.eventHandlers.forEach((handler) => {
      handler && handler.unsubscribe();
    });
  }
}
