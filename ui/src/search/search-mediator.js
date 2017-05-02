import {Inject} from 'app/app';
import _ from 'lodash';
import {ContextualRulesResolver} from 'search/resolvers/contextual-rules-resolver';

export const EVENT_BEFORE_SEARCH = 'before-search';
export const EVENT_SEARCH = 'search';
export const EVENT_BEFORE_SEARCH_CRITERIA_CHANGED = 'before-search-criteria-changed';
export const EVENT_SEARCH_CRITERIA_CHANGED = 'search-criteria-changed';
export const EVENT_CLEAR = 'search-clear';

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
      let searchResult = SearchMediator.buildSearchResponse(query, response, this.searchMode);
      this.trigger(EVENT_SEARCH, searchResult);
      return searchResult;
    }, (error) => {
      let errorResult = SearchMediator.buildErrorSearchResponse(query, error, this.searchMode);
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
    let eventData = {rule: rule, parentId: parentId, query: query};

    this.trigger(EVENT_BEFORE_SEARCH_CRITERIA_CHANGED, eventData);
    this.queryBuilder.add(rule, parentId);
    this.trigger(EVENT_SEARCH_CRITERIA_CHANGED, eventData);
  }

  removeCriteria(rule) {
    let query = this.queryBuilder;
    let eventData = {rule: rule, query: query};

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
      searchMode: searchMode,
      context: context
    };
  }

  static buildSearchResponse(query, response, searchMode) {
    return {
      query: query,
      response: response,
      searchMode: searchMode
    };
  }

  static buildErrorSearchResponse(query, error, searchMode) {
    // Null object pattern for components that do not explicitly handle errors
    var emptyResults = SearchMediator.buildEmptySearchResults();
    var response = SearchMediator.buildSearchResponse(query, emptyResults, searchMode);
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
}
