import {Component, View, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {SearchService} from 'services/rest/search-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import _ from 'lodash';

import './saved-search-select.css!css';
import template from './saved-search-select.html!text';

export const SAVED_SEARCH_URI = 'emf:SavedSearch';
export const OPEN_SAVED_SEARCH_EVENT = 'open-saved-search';
const SAVED_SEARCH_PROPERTIES = ['id', 'title', 'searchType', 'searchCriteria'];

/**
 * Wrapper component with a select for searching and loading saved searches.
 *
 * Constructs a new search mediator that triggers search only for instances with type emf:SavedSearch.
 *
 * It requires a provided search mediator in the configuration property of the component to notify other components
 * that a search has been loaded.
 *
 * @author Tsvetomir Dimitrov
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-saved-search-select',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgElement, NgScope, NgTimeout, SearchService, NamespaceService)
export class SavedSearchSelect extends Configurable {

  constructor($element, $scope, $timeout, searchService, namespaceService) {
    super({});
    this.$scope = $scope;
    this.$element = $element;
    this.$timeout = $timeout;
    this.searchService = searchService;

    // Get the full saved search URI
    namespaceService.toFullURI([SAVED_SEARCH_URI]).then((response) => {
      this.savedSearchUri = response.data[SAVED_SEARCH_URI];
    });

    this.applyClosingListener();
  }

  ngOnInit() {
    var searchArguments = {
      properties: SAVED_SEARCH_PROPERTIES
    };
    this.searchMediator = new SearchMediator(this.searchService, new QueryBuilder({}), undefined, searchArguments);
    this.config.selectConfig = _.defaults(this.config, this.getDefaultConfig());
  }

  getDefaultConfig() {
    var loader = (params) => this.savedSearchLoader(params);
    var converter = (response) => this.savedSearchConverter(response);
    var selectEvent = (event) => this.openSavedSearch(event);
    return {
      delay: 500,
      multiple: false,
      dataLoader: loader,
      dataConverter: converter,
      listeners: {
        'select2:select': selectEvent
      }
    };
  }

  savedSearchLoader(params) {
    var terms = params && params.data && params.data.q || '';

    if (!this.searchTree) {
      // Lazily assign the search tree. There is no need to construct it every time the loader is invoked.
      this.searchTree = this.getSearchTree();
      this.searchMediator.queryBuilder.init(this.searchTree);
    }

    // Leave only the object type rule.
    this.searchTree.rules.splice(1);

    if (terms && terms.length > 0) {
      var ftsRule = this.getFreeTextRule(terms);
      this.searchTree.rules[1] = ftsRule;
    }

    this.searchMediator.abortLastSearch();

    return this.searchMediator.search(true).then((searchResponse) => {
      return searchResponse.response;
    });
  }

  savedSearchConverter(response) {
    var converted = [];
    (response.data.values || []).map((item) => {
      var searchCriteria = item.properties.searchCriteria;
      if (searchCriteria) {
        var parsed = JSON.parse(searchCriteria);
        converted.push(this.buildSelectEntry(item, parsed));
      }
    });
    return converted;
  }

  buildSelectEntry(item, criteria) {
    return {
      id: item.id,
      text: item.properties.title,
      searchMode: item.properties.searchType,
      orderBy: criteria.orderBy,
      orderDirection: criteria.orderDirection,
      criteria: criteria.criteria
    };
  }

  getSearchTree() {
    var condition = SearchCriteriaUtils.buildCondition();

    var savedSearchTypeRule = SearchCriteriaUtils.getDefaultObjectTypeRule([this.savedSearchUri]);
    condition.rules.push(savedSearchTypeRule);

    return condition;
  }

  getFreeTextRule(terms) {
    return SearchCriteriaUtils.buildRule("freeText", "fts", AdvancedSearchCriteriaOperators.CONTAINS.id, terms);
  }

  openSavedSearch(event) {
    // Transform the event payload ?
    this.config.searchMediator.trigger(OPEN_SAVED_SEARCH_EVENT, event.params.data);
    // Clearing the select's model in case the same search is selected again - it needs to trigger ng-change
    this.selectedSearch = undefined;
  }

  applyClosingListener() {
    this.$element.on('select2:closing', ()=> {
      this.$scope.$apply(this.config.visible = false);
    });
  }

  triggerSavedSearchSelect() {
    this.config.visible = true;
    this.$timeout(()=> {
      this.select.select2('open');
    });
  }

  get select() {
    return this.$element.find('select');
  }
}