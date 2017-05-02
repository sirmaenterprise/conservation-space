import {Component, View, Inject} from 'app/app';
import {Configuration} from 'common/application-config';

import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';

import 'search/components/common/basic-search-criteria';
import 'search/components/advanced/advanced-search';
import 'components/help/contextual-help';

import './mixed-search-criteria.css!css';
import template from 'search/components/common/mixed-search-criteria.html!text';

/**
 * Component representing switching between basic and advanced search. Swaps the query builder in the search
 * mediator depending on the current search mode.
 *
 * Provided configuration and context properties are passed down to the currently rendered search component.
 * Providing a search mediator in the configuration is mandatory!
 */
@Component({
  selector: 'seip-mixed-search-criteria',
  properties: {
    config: 'config',
    context: 'context'
  }
})

@View({
  template: template
})
@Inject(Configuration)
export class MixedSearchCriteria extends SearchCriteriaComponent {

  constructor(configuration) {
    super({
      searchMode: SearchCriteriaUtils.BASIC_MODE,
      renderHelp: false
    });
    this.configService = configuration;

    this.initQueryBuilders();
    this.registerSearchLoadListener();

    this.config.searchMediator.searchMode = this.config.searchMode;
  }

  initQueryBuilders() {
    if (this.isBasicSearchMode()) {
      this.basicSearchQueryBuilder = this.config.searchMediator.queryBuilder;
      this.advancedSearchQueryBuilder = new QueryBuilder({});
      this.externalSearchQueryBuilder = new QueryBuilder({});
    } else if (this.isAdvancedSearchMode()) {
      this.advancedSearchQueryBuilder = this.config.searchMediator.queryBuilder;
      this.basicSearchQueryBuilder = new QueryBuilder({});
      this.externalSearchQueryBuilder = new QueryBuilder({});
    } else {
      this.basicSearchQueryBuilder = new QueryBuilder({});
      this.advancedSearchQueryBuilder = new QueryBuilder({});
      this.externalSearchQueryBuilder = this.config.searchMediator.queryBuilder;
    }
  }

  registerSearchLoadListener() {
    this.config.searchMediator.registerListener(OPEN_SAVED_SEARCH_EVENT, (savedSearch) => {
      this.config.searchMode = savedSearch.searchMode;
      this.initQueryBuilders();
    });
  }

  onSwitch() {
    if (this.isBasicSearchMode()) {
      this.config.searchMediator.queryBuilder = this.basicSearchQueryBuilder;
      this.config.searchMediator.searchMode = SearchCriteriaUtils.BASIC_MODE;
    } else if (this.isAdvancedSearchMode()) {
      this.config.searchMediator.queryBuilder = this.advancedSearchQueryBuilder;
      this.config.searchMediator.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
    } else {
      this.config.searchMediator.queryBuilder = this.externalSearchQueryBuilder;
      this.config.searchMediator.searchMode = SearchCriteriaUtils.EXTERNAL_MODE;
    }
    this.clearResults();
  }

  isBasicSearchMode() {
    return this.config.searchMode === SearchCriteriaUtils.BASIC_MODE;
  }

  isAdvancedSearchMode() {
    return this.config.searchMode === SearchCriteriaUtils.ADVANCED_MODE;
  }

  isExternalSearchMode() {
    return this.config.searchMode === SearchCriteriaUtils.EXTERNAL_MODE;
  }

  isExternalSearchPresent() {
    return this.configService.get('eai.dam.enabled') || this.configService.get('eai.cms.enabled');
  }
}