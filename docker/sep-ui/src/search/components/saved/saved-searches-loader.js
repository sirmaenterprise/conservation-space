import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {DCTERMS_TITLE} from 'instance/instance-properties';

export const SAVED_SEARCH_URI = 'emf:SavedSearch';
export const SAVED_SEARCH_PROPERTIES = ['id', 'title', 'searchType', 'searchCriteria', HEADER_BREADCRUMB];

/**
 * Capsulates logic for filtering and loading of saved searches.
 * Any filtering term are used as a free text criteria.
 *
 * @author Mihail Radkov
 */
export class SavedSearchesLoader {

  constructor(searchService) {
    this.searchService = searchService;
  }

  /**
   * Filters & loads saved searches matching the provided search terms.
   *
   * @param terms - user query
   * @returns a Promise resolving with the filtered saved searches
   */
  filterSavedSearches(terms) {
    if (!this.searchMediator) {
      this.initSearchMediator();
    }

    this.searchTree.rules.splice(1);

    if (terms && terms.length > 0) {
      this.searchTree.rules[1] = this.getFreeTextRule(terms);
    }

    this.searchMediator.abortLastSearch();

    return this.searchMediator.search(true).then((searchResponse) => {
      return searchResponse.response.data;
    });
  }

  getFreeTextRule(terms) {
    return SearchCriteriaUtils.buildRule(DCTERMS_TITLE, 'string', AdvancedSearchCriteriaOperators.CONTAINS.id, terms);
  }

  initSearchMediator() {
    this.searchTree = this.getSearchTree();
    let searchArguments = {
      properties: SAVED_SEARCH_PROPERTIES
    };
    this.searchMediator = new SearchMediator(this.searchService, new QueryBuilder(this.searchTree), undefined, searchArguments);
  }

  getSearchTree() {
    let condition = SearchCriteriaUtils.buildCondition();

    let savedSearchTypeRule = SearchCriteriaUtils.getDefaultObjectTypeRule([SAVED_SEARCH_URI]);
    condition.rules.push(savedSearchTypeRule);

    return condition;
  }

  /**
   * Converts the provided saved search instance into a flat model used in the search components.
   */
  static convertSavedSearch(savedSearch) {
    let searchCriteria = savedSearch.properties.searchCriteria;
    let parsedCriteria = searchCriteria ? JSON.parse(searchCriteria) : {};
    return {
      id: savedSearch.id,
      text: savedSearch.properties.title,
      searchMode: savedSearch.properties.searchType,
      orderBy: parsedCriteria.orderBy,
      orderDirection: parsedCriteria.orderDirection,
      criteria: parsedCriteria.criteria
    };
  }

}