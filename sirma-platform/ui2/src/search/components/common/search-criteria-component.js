import {SearchComponent} from 'search/components/common/search-component';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator, EVENT_SEARCH, EVENT_CLEAR} from 'search/search-mediator';

export const CRITERIA_READY_EVENT = 'criteria-ready';

/**
 * Base class for all search criteria components.
 */
export class SearchCriteriaComponent extends SearchComponent {

  constructor(config) {
    super(config || {});
  }

  addCriteria(criteria) {
    this.config.searchMediator.addCriteria(criteria);
  }

  search() {
    this.resetPaging();
    this.config.searchMediator.search();
  }

  resetPaging() {
    this.config.searchMediator.arguments.pageNumber = 1;
  }

  clearResults() {
    this.config.searchMediator.trigger(EVENT_CLEAR);
    var emptySearchResults = SearchMediator.buildEmptySearchResults();
    var emptyResponse = SearchMediator.buildSearchResponse({}, emptySearchResults);
    this.config.searchMediator.trigger(EVENT_SEARCH, emptyResponse);
  }

  afterInit() {
    // Notify any registered listener that the component is initialized and no further changes will be made to the criteria.
    this.config.searchMediator.trigger(CRITERIA_READY_EVENT);
  }

}