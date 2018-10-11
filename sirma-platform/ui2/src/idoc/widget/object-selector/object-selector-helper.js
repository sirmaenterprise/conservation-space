import _ from 'lodash';
import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchService} from 'services/rest/search-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchMediator} from 'search/search-mediator';
import {InstanceRestService} from 'services/rest/instance-service';
import {SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

export const EMPTY_SELECTION = 'select.object.none';
export const EMPTY_RESULTS = 'select.object.results.none';
export const MULTIPLE_RESULTS = 'select.object.automatically.more.than.one.message';
export const UNDEFINED_CRITERIA = 'select.object.undefined.criteria';
const SELECTOR_PROPERTIES = ['id'];

/**
 * Provides convenient way to resolve selected objects from {@link ObjectSelector} which is used in widgets.
 */
@Injectable()
@Inject(PromiseAdapter, SearchService, InstanceRestService)
export class ObjectSelectorHelper {

  constructor(promiseAdapter, searchService, instanceRestService) {
    this.promiseAdapter = promiseAdapter;
    this.searchService = searchService;
    this.instanceRestService = instanceRestService;
  }

  /**
   * Retrieves the selected object based on the provided configuration & context.
   *
   * If for some reason an object couldn't be obtained OR there are multiple selected objects, a corresponding error
   * message key will be provided in the rejection.
   *
   * For detailed parameter information see {@link #getSelectedObjects()}
   *
   * @param config - the configuration object from a widget
   * @param context - the iDoc context. Used for fetching the currently opened object and for resolving contextual objects
   * @param searchArguments - map with query parameters to be appended to the search URL
   * @param selectorArguments - map with arguments controlling the selection resolving behaviour
   * @returns Promise with the selected object's ID if resolved or an error message if rejected
   */
  getSelectedObject(config, context, searchArguments, selectorArguments) {
    return this.getSelectedObjects(config, context, searchArguments, selectorArguments).then((response) => {
      if (response.total === 1) {
        return response.results[0];
      } else {
        return this.promiseAdapter.reject({
          multipleResults: true,
          reason: MULTIPLE_RESULTS
        });
      }
    });
  }

  /**
   * Retrieves selection based on the provided widget configuration and iDoc context. Supports:
   * 1) Context selection - resolves with the current object ID
   * 2) Manual selection - resolves with selected objects from selectedObject, selectedObjects & selectedItems
   *                       fields (only one is used). NOTE: selectedItems is an array of objects and should not be
   *                       used if possible
   * 3) Automatic selection - performs a search based on the search criteria in the configuration. Resolves with
   *                          default pagination. If includeCurrent is set to true it will return results with the
   *                          current object on top of all identifiers.
   *
   * Could be provided with search arguments that are appended as query parameters (for example overriding the default
   * pagination)
   *
   * If for some reason an object couldn't be obtained, a corresponding error message key will be provided as a reject.
   *
   * Example response structure:
   *  {
   *    total: 250,
   *    results: ['emf:1' ,'emf:2']
   *  }
   *
   * @param config - the configuration object from a widget
   * @param context - the iDoc context. Used for fetching the currently opened object and for resolving contextual objects
   * @param searchArguments - map with query parameters to be appended to the search URL
   * @param selectorArguments - map with arguments controlling the selection resolving behaviour
   * @returns Promise with the selected objects' IDs if resolved or an error message if rejected
   */
  getSelectedObjects(config, context, searchArguments, selectorArguments) {
    return this.promiseAdapter.promise((resolve, reject) => {
      if (config.selectObjectMode === SELECT_OBJECT_CURRENT) {
        this.getContextSelection(context, selectorArguments).then(resolve, reject);
      } else if (config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
        this.getManualSelection(config).then(resolve, reject);
      } else if (config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        this.getAutomaticSelection(config, context, searchArguments).then(resolve, reject);
      } else {
        reject({
          noSelection: true,
          reason: EMPTY_SELECTION
        });
      }
    }).then((selection) => {
      if (selection.total < 1) {
        return this.promiseAdapter.reject({
          noSelection: true,
          reason: EMPTY_SELECTION
        });
      }
      return selection;
    });
  }

  /**
   * Returns a promise which resolves to an array of object identifiers related to the current context, particularly
   * with the current object.
   *
   * @param context - the context for resolving the selection from
   * @param selectorArguments - map with arguments controlling the selection resolving behaviour
   */
  getContextSelection(context, selectorArguments) {
    selectorArguments = selectorArguments || {};
    return context.getCurrentObject().then((currentObject) => {
      if (selectorArguments.ignoreNotPersisted && !currentObject.isPersisted()) {
        return this.toResponse([]);
      } else {
        return this.toResponse([currentObject.getId()]);
      }
    });
  }

  /**
   * Based on the given configuration, this resolves with either the single selection or with the multiple. Response
   * is always an array for unification.
   *
   * Supported fields in the configuration are: selectedObject, selectedObjects & selectedItems
   *
   * @param config - the configuration object containing the manual selection
   * @returns Promise resolving with the manual selection
   */
  getManualSelection(config) {
    var selection = [];
    if (config.selectedObject) {
      selection.push(config.selectedObject);
    } else if (config.selectedObjects) {
      selection.push(...config.selectedObjects);
    } else if (config.selectedItems) {
      // DO NOT USE THIS !! USE selectedObjects !!
      selection.push(...config.selectedItems);
    }
    return this.promiseAdapter.resolve(this.toResponse(selection));
  }

  /**
   * Performs a search based on the criteria in the provided configuration object. This method also supports resolving
   * the current object from the context as part of the search result.
   *
   * In the case of no results from the search service, the returned promise will be rejected with the appropriate
   * error message but only if the configuration has <code>false</code> for <code>config.includeCurrent</code>.
   *
   * This does NOT increment the total amount of search results if the current object is included!
   *
   * @param config - the configuration object from a widget
   * @param context - the iDoc context. Used for fetching the currently opened object and for resolving contextual objects
   * @param searchArguments - map with query parameters to be appended to the search URL
   * @returns a promise resolved with the selected objects, rejects if none are returned
   */
  getAutomaticSelection(config, context, searchArguments) {
    let promises = [];
    if (config.includeCurrent) {
      promises.push(this.getContextSelection(context));
    }
    promises.push(this.searchForObjects(config.criteria, context, searchArguments, config.searchMode));

    return this.promiseAdapter.all(promises).then(selections => {
      let results = [];
      if (config.includeCurrent) {
        var contextSelection = selections.shift();
        results.push(...contextSelection.results);
      }
      var searchSelection = selections[0];
      results.push(...searchSelection.results);
      // Total is considered only from the search results because the current object may be among them so its ignored.
      return this.toResponse(results, searchSelection.total);
    }).catch((error) => {
      // Giving chance for current object if there are no search results
      if (config.includeCurrent) {
        return this.getContextSelection(context);
      } else {
        return this.promiseAdapter.reject(error);
      }
    });
  }

  /**
   * Retrieves the objects based on the provided criteria & context from the search service.
   * The search returns only the instance IDs if no other properties set is specified.
   *
   * If no results are returned or the provided search criteria is not defined, the promise will be rejected
   * with corresponding message key.
   *
   * @param criteria - the search criteria to be applied when searching
   * @param context - the iDoc context
   * @param searchArguments - map with query parameters to be appended to the search URL
   * @param searchMode - tells the search service what kind of a search to perform
   * @returns promise with the objects' IDs and the total amount of results or error message if rejected
   */
  searchForObjects(criteria, context, searchArguments, searchMode) {
    if (!SearchCriteriaUtils.isCriteriaDefined(criteria)) {
      return this.promiseAdapter.reject({
        reason: UNDEFINED_CRITERIA
      });
    }

    searchArguments = searchArguments || {};
    searchArguments.properties = searchArguments.properties || SELECTOR_PROPERTIES;

    // TODO: Remove searchMode when the basic search is routed to use the advanced search. The search mode is need only to show the correct search view.
    var mediator = this.getSearchMediator(searchMode, criteria, context, searchArguments);
    return mediator.search().then((searchResult) => {
      var values = searchResult.response.data.values;
      if (values && values.length > 0) {
        var results = values.map((value) => value.id);
        var total = searchResult.response.data.resultSize;
        return this.toResponse(results, total);
      }
      return this.promiseAdapter.reject({
        noResults: true,
        reason: EMPTY_RESULTS
      });
    });
  }

  toResponse(responseArray, total) {
    return {
      results: responseArray,
      total: total || responseArray.length
    };
  }

  /**
   * Selected items headers & other relevant data are resolved asynchronously when ready.
   *
   * @param config - widget configuration
   * @param selectedItemsReference a reference to selected items array which can be passed to the search before the headers are resolved
   * @returns Promise resolving with selected items with resolved headers
   */
  getSelectedItems(config, selectedItemsReference) {
    let selectedObjects = [];
    if (config.selectedObject) {
      selectedObjects.push(config.selectedObject);
    } else if (config.selectedObjects) {
      selectedObjects.push(...config.selectedObjects);
    }

    let selectedItems = selectedItemsReference || [];

    selectedObjects.forEach((selectedObject) => {
      selectedItems.push({
        id: selectedObject,
        properties: {},
        headers: {}
      });
    });

    if (selectedObjects.length > 0) {
      return this.loadSelectedItems(selectedObjects, selectedItems);
    } else {
      return this.promiseAdapter.resolve(selectedItems);
    }
  }

  /**
   * Loads the instances for the given selectedObjects' IDs and populate the second array selectedItems with the
   * fetched data.
   *
   * If some of the selected objects is deleted or miss a header it is considered a deleted object and is removed
   * from the list with items.
   *
   * @param selectedObjects - the list of IDs to load
   * @param selectedItems - the list of instances to enrich with data
   */
  loadSelectedItems(selectedObjects, selectedItems) {
    return this.instanceRestService.loadBatch(selectedObjects).then((response) => {
      response.data.forEach((instance) => {
        // Some instances may be deleted so traversing by index is not reliable
        var selectedItem = _.find(selectedItems, (item)=> {
          return instance.id === item.id;
        });
        _.merge(selectedItem, instance);
      });

      // Remove all that are without headers or "deleted"
      _.remove(selectedItems, (item) => {
        return item.headers[HEADER_DEFAULT] === undefined || item.headers[HEADER_DEFAULT].length < 1;
      });

      return selectedItems;
    });
  }

  /**
   * Removes selected object ids from widget's config selectedObject(s).
   * To be used when requested objects are no longer existing in the system (deleted).
   * @param config
   * @param idsToRemove array with object ids to be removed from widget's manually selected object(s)
   */
  removeSelectedObjects(config, idsToRemove) {
    if (config.selectedObject) {
      if (idsToRemove.indexOf(config.selectedObject) !== -1) {
        config.selectedObject = undefined;
      }
    } else if (config.selectedObjects) {
      _.remove(config.selectedObjects, (sharedObjectId) => {
        return idsToRemove.indexOf(sharedObjectId) !== -1;
      });
    }
  }

  groupSelectedObjects(config, context, groupBy, returnEmpty) {
    if (config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && !SearchCriteriaUtils.isCriteriaDefined(config.criteria)) {
      return this.promiseAdapter.reject({
        reason: UNDEFINED_CRITERIA
      });
    }

    let searchArguments = {
      groupBy: groupBy,
      maxSize: 0,
      pageSize: 0
    };
    if (config.selectObjectMode === SELECT_OBJECT_MANUALLY && config.selectedObjects.length) {
      searchArguments.selectedObjects = config.selectedObjects;
    }

    return this.promiseAdapter.promise((resolve, reject) => {
      var mediator = this.getSearchMediator(config.searchMode, config.criteria, context, searchArguments);
      return mediator.search().then((searchResult) => {
        if (!returnEmpty && (_.isEmpty(searchResult.response.data.aggregated) || _.values(searchResult.response.data.aggregated).every(_.isEmpty))) {
          reject({
            reason: EMPTY_RESULTS
          });
        } else {
          resolve({
            aggregated: searchResult.response.data.aggregated
          });
        }
      });
    });
  }

  getSearchMediator(searchMode, criteria, context, searchArguments) {
    return new SearchMediator(this.searchService, new QueryBuilder(criteria), context, searchArguments, searchMode);
  }

  /**
   * Converts widget configuration to configuration for filtering. Resulting configuration is a clone!
   * If mode is manually a criteria is build with selected instance URIs and mode is set to automatically.
   * Filter criteria is appended with AND to the existing (or newly build) criteria.
   * @param config - widget configuration
   * @param filterCriteria
   * @returns new configuration with appended filtering crieria
   */
  static getFilteringConfiguration(config, filterCriteria) {
    let result = _.cloneDeep(config);
    if (config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
      result.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      result.criteria = SearchCriteriaUtils.buildRule('instanceId', undefined, AdvancedSearchCriteriaOperators.IN.id, config.selectedObjects);
    } else {
      if (!SearchCriteriaUtils.isCriteriaDefined(config.criteria)) {
        return result;
      }
    }
    let rules = [result.criteria];
    if (filterCriteria) {
      rules.push(filterCriteria);
    }
    result.criteria = SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, rules);
    return result;
  }
}
