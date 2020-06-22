import _ from 'lodash';

export const EVENT_BEFORE_SEARCH = 'before-search';
export const EVENT_SEARCH = 'search';
export const EVENT_BEFORE_SEARCH_CRITERIA_CHANGED = 'before-search-criteria-changed';
export const EVENT_SEARCH_CRITERIA_CHANGED = 'search-criteria-changed';
export const EVENT_CLEAR = 'search-clear';
export const EVENT_CRITERIA_PROPERTY_CHANGED = 'criteria-property-changed';
export const EVENT_CRITERIA_RESET = 'criteria-reset';

export class SearchMediator {

  constructor(service, queryBuilder, context, argumentsMap, searchMode) {
    this.service = service;
    this.queryBuilder = queryBuilder;
    this.listeners = {};
    this.context = context;
    this.arguments = argumentsMap || {};
    this.searchMode = searchMode;
  }

  /**
   * Performs the search with the search tree in the query builder. In the event of an error, it builds an empty search
   * response to avoid errors in components using this mediator.
   *
   * Resolves the response with the original query to avoid using resolved query because it may contain different
   * criteria.
   *
   * Triggers before and after search events to any registered listeners.
   *
   * @returns object containing the original query, search response and mode.
   */
  search(skipInterceptor) {
    let query = this.queryBuilder;
    this.trigger(EVENT_BEFORE_SEARCH, query);

    var searchRequest = SearchMediator.buildSearchRequest(query, this.arguments, this.searchMode, this.context);
    searchRequest.skipInterceptor = skipInterceptor;

    var search = this.service.search(searchRequest);
    this.lastSearchRequest = search.timeout;

    return search.promise.then((response) => {
      let searchResult = SearchMediator.buildSearchResponse(query, response, this.arguments, this.searchMode);
      this.trigger(EVENT_SEARCH, searchResult);
      return searchResult;
    }, (error) => {
      let errorResult = SearchMediator.buildErrorSearchResponse(query, error, this.arguments, this.searchMode);
      this.trigger(EVENT_SEARCH, errorResult);
      return errorResult;
    });
  }

  abortLastSearch() {
    if (this.lastSearchRequest && this.lastSearchRequest.resolve) {
      this.lastSearchRequest.resolve();
    }
  }

  addCriteria(rule, parentId) {
    let query = this.queryBuilder;
    let eventData = {rule, parentId, query};

    this.trigger(EVENT_BEFORE_SEARCH_CRITERIA_CHANGED, eventData);
    this.queryBuilder.add(rule, parentId);
    this.trigger(EVENT_SEARCH_CRITERIA_CHANGED, eventData);
  }

  removeCriteria(rule) {
    let query = this.queryBuilder;
    let eventData = {rule, query};

    this.trigger(EVENT_BEFORE_SEARCH_CRITERIA_CHANGED, eventData);
    this.queryBuilder.remove(rule);
    this.trigger(EVENT_SEARCH_CRITERIA_CHANGED, eventData);
  }

  registerListener(eventName, listener) {
    let listeners = this.listeners;
    let array = listeners[eventName];
    if (!array) {
      array = [];
      listeners[eventName] = array;
    }

    array.push(listener);
  }

  trigger(eventName, data) {
    let listeners = this.listeners[eventName];
    if (!listeners) {
      return;
    }
    listeners.forEach((listener) => listener(data));
  }

  static buildSearchRequest(query, argumentsMap, searchMode, context) {
    // Preventing modification of the original query tree
    let queryCopy = _.cloneDeep(query);
    return {
      query: queryCopy,
      arguments: argumentsMap,
      searchMode,
      context
    };
  }

  static buildSearchResponse(query, response, argumentsMap, searchMode) {
    return {
      query,
      response,
      arguments: argumentsMap,
      searchMode
    };
  }

  static buildErrorSearchResponse(query, error, argumentsMap, searchMode) {
    // Null object pattern for components that do not explicitly handle errors
    var emptyResults = SearchMediator.buildEmptySearchResults();
    var response = SearchMediator.buildSearchResponse(query, emptyResults, argumentsMap, searchMode);
    response.error = error;
    return response;
  }

  static buildEmptySearchResults() {
    return {
      data: {
        values: [],
        resultSize: 0
      }
    };
  }

  /**
   * Checks if a search results response is potentially empty. An empty search results response is
   * such that has 0 resultSize or/and 0 elements inside it's values array
   *
   * @param response the results response
   * @returns {boolean} true if empty, false otherwise
   */
  static isSearchResultEmpty(response) {
    return !(response && response.data && response.data.resultSize && response.data.values.length);
  }

  /**
   * Checks if a search query builder is potentially empty.
   * And empty query builder is such that has no valid tree
   *
   * @param response the query builder
   * @returns {boolean} true if empty, false otherwise
   */
  static isSearchQueryEmpty(query) {
    return _.isEmpty(query);
  }

  /**
   * Checks if a search payload is potentially empty.
   * And empty payload is such that has either an
   * empty query builder or an empty search results response
   *
   * @param response the search payload
   * @returns {boolean} true if empty, false otherwise
   */
  static isSearchPayloadEmpty(payload) {
    if (_.isEmpty(payload)) {
      return true;
    }
    return !SearchMediator.isSearchQueryEmpty(payload.query) ? SearchMediator.isSearchResultEmpty(payload.response) : true;
  }
}
