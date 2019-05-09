import _ from 'lodash';

import {Component, Inject, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchService} from 'services/rest/search-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {
  EVENT_BEFORE_SEARCH,
  EVENT_BEFORE_SEARCH_CRITERIA_CHANGED,
  EVENT_SEARCH,
  EVENT_SEARCH_CRITERIA_CHANGED,
  SearchMediator
} from 'search/search-mediator';
import {NO_SELECTION} from 'search/search-selection-modes';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {ORDER_DESC, ORDER_RELEVANCE} from 'search/order-constants';
import {EMF_MODIFIED_ON} from 'instance/instance-properties';

import 'search/components/common/mixed-search-criteria';
import 'search/components/common/search-toolbar';
import 'search/components/common/search-results';
import 'search/components/common/pagination';

import template from './search.html!text';

export const SEARCH_PROPERTIES = ['id', HEADER_DEFAULT];
export const FTS_CHANGE_EVENT = 'fts-changed-event';

/**
 * Configurable component for defining user queries and performing searches in the system. The main purpose is to
 * combine and configure the rest of the search components so they could work as a whole:
 * - Search criteria component
 * - Search toolbar
 * - Search results
 * - Pagination
 *
 * The component supports multiple configurations (either for it or for the rest of the components) with default values
 * and the possibility for overriding them with externally provided configuration object.
 *
 * If no mediator is provided via the configuration, a default one will be assigned with default criteria and search
 * service.
 *
 * Because the search can render different criteria forms, it register a listener for CRITERIA_READY_EVENT which is
 * thrown when concrete criteria is initialized and ready to perform searches. This listener is registered only when
 * <code>config.triggerSearch</code> and <code>config.renderCriteria</code> are true and it executes a search only once.
 *
 * To provide custom search arguments as query parameters use <code>config.arguments</code> Example:
 * <code>
 * config.arguments = {
 *    properties: ['id', 'default_header'],
 *    filterByWritePermissions: true,
 *    orderBy: 'relevance',
 *    orderDirection: 'desc'
 * }
 * </code>
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-search',
  properties: {
    'config': 'config',
    'context': 'context'
  }
})
@View({template})
@Inject(Configuration, SearchService, PromiseAdapter, NamespaceService)
export class Search extends Configurable {

  constructor(configuration, searchService, promiseAdapter, namespaceService) {
    super({
      results: {
        config: {
          selection: NO_SELECTION,
          selectedItems: [],
          placeholder: 'search'
        },
        data: []
      },
      criteria: {},
      triggerSearch: true,
      renderCriteria: true,
      renderToolbar: true,
      renderPagination: true,
      useFixedToolbar: false,
      arguments: {
        properties: SEARCH_PROPERTIES
      },
      advancedOnly: false
    });

    this.configuration = configuration;
    this.searchService = searchService;
    this.promiseAdapter = promiseAdapter;
    this.namespaceService = namespaceService;
  }

  ngOnInit() {
    // Some configurations may need to perform async operations
    this.configure().then(() => {
      if (this.config.renderCriteria) {
        this.render = true;
      } else if (this.config.triggerSearch) {
        // If configured to trigger a search and there is no criteria component to be rendered which may change the
        // criteria tree -> trigger a search
        this.searchMediator.search();
      }
    });
  }

  configure() {
    this.configureSearchMediator();
    this.configureDefaultOrdering();

    this.config.results.config.searchMediator = this.searchMediator;
    return this.transformPredefinedTypes().then(() => {
      this.configureCriteria();
      this.configureToolbar();
      this.configureResults();
      this.configurePagination();
      this.registerCallbacks();
      this.configureLoadingHandlers();
      this.registerCriteriaReadyListener();
      return this.promiseAdapter.resolve();
    });
  }

  configureSearchMediator() {
    if (!this.config.searchMediator) {
      let queryBuilder = new QueryBuilder(this.config.criteria);
      this.config.searchMediator = new SearchMediator(this.searchService, queryBuilder, this.context, {}, this.config.searchMode);
    }
    this.searchMediator = this.config.searchMediator;
    this.searchMediator.arguments = _.defaultsDeep(this.searchMediator.arguments, this.config.arguments);
  }

  configureDefaultOrdering() {
    // Skip if there is provided order by
    if (this.searchMediator.arguments.orderBy) {
      return;
    }

    let ftsRule = QueryBuilder.getFirstRule(this.getSearchTree(), SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
    if (QueryBuilder.ruleHasValues(ftsRule)) {
      this.searchMediator.arguments.orderBy = ORDER_RELEVANCE;
    } else {
      // TODO: ? maybe this is not needed!
      this.searchMediator.arguments.orderBy = EMF_MODIFIED_ON;
    }

    this.searchMediator.arguments.orderDirection = this.searchMediator.arguments.orderDirection || ORDER_DESC;
  }

  /**
   * Performs transform operations upon any configured predefined type.
   */
  transformPredefinedTypes() {
    if (this.config.predefinedTypes && this.config.predefinedTypes.length > 0) {
      return this.namespaceService.toFullURI(this.config.predefinedTypes).then((response) => {
        this.config.predefinedTypes.splice(0).forEach((removedUri) => {
          this.config.predefinedTypes.push(response.data[removedUri]);
        });
      });
    }
    return this.promiseAdapter.resolve();
  }

  configureCriteria() {
    this.criteriaConfig = {
      level: this.config.level || 1,
      searchMediator: this.searchMediator,
      contextualItems: this.config.contextualItems,
      searchMode: this.config.searchMode,
      restrictions: this.config.restrictions,
      predefinedTypes: this.config.predefinedTypes,
      renderHelp: this.config.renderHelp,
      useRootContext: this.config.useRootContext,
      savedSearch: this.createSaveSearchConfig(),
      advancedOnly: this.config.advancedOnly
    };
  }

  configureToolbar() {
    this.toolbarConfig = {
      disabled: false,
      searchMediator: this.searchMediator,
      restrictions : this.config.restrictions,
      useMinimalResultToolbar: !this.config.useFixedToolbar
    };
  }

  createSaveSearchConfig() {
    let savedSearch = this.config.savedSearch || {};

    return {
      searchId: savedSearch.searchId,
      searchTitle: savedSearch.searchTitle,
      render: !this.isExternalSearchMode() && !!this.config.renderCriteria,
      searchMediator: this.searchMediator,
      savedSearchSelect: {
        searchMediator: this.searchMediator,
        render: !this.config.predefinedTypes || this.config.predefinedTypes.length < 1
      }
    };
  }

  configurePagination() {
    let pageSize = this.configuration.get(Configuration.SEARCH_PAGE_SIZE);

    if (this.config.paginationConfig === undefined) {
      this.config.paginationConfig = {
        showFirstLastButtons: true,
        page: 1,
        pageSize,
        pageRotationStep: this.getPageRotationStep()
      };
    }

    // This configuration is not forced in the backend so we must provide it from here.
    this.searchMediator.arguments.pageSize = pageSize;

    this.paginationCallback = (params) => {
      this.searchMediator.arguments.pageNumber = params.pageNumber;
      this.searchMediator.search();
    };
  }

  /**
   * Calculates the page rotation step used in the pagination component. This defines how much of pages to be next
   * to the central page button.
   *
   * Example: If the step is calculated to 2 then the pagination component will render maximum of 5 page buttons.
   *
   * @returns the page rotation step as integer or undefined if the configuration is missing
   */
  getPageRotationStep() {
    let pagerMaxPages = this.configuration.get(Configuration.SEARCH_PAGER_MAX_PAGES);
    if (pagerMaxPages) {
      return Math.round(pagerMaxPages / 2) - 1;
    }
  }

  configureResults() {
    this.searchMediator.registerListener(EVENT_SEARCH, (eventData) => {
      let response = eventData.response;

      this.config.results.total = response.data.resultSize || 0;
      this.config.results.data.splice(0);

      this.config.paginationConfig.page = this.searchMediator.arguments.pageNumber || 1;

      let data = response.data.values;
      if (!data || !data.length) {
        return;
      }

      this.config.results.data.push(...data);
    });
  }

  registerCallbacks() {
    let callbacks = this.config.callbacks;
    if (_.isEmpty(callbacks)) {
      return;
    }

    let names = [
      EVENT_BEFORE_SEARCH, EVENT_SEARCH,
      EVENT_BEFORE_SEARCH_CRITERIA_CHANGED, EVENT_SEARCH_CRITERIA_CHANGED
    ];

    names.forEach((name) => this.registerCallback(name, callbacks[name]));
  }

  registerCallback(eventName, callbacks) {
    if (!callbacks) {
      return;
    }

    let list = Array.isArray(callbacks) ? callbacks : [callbacks];
    list.forEach((cb) => this.searchMediator.registerListener(eventName, cb));
  }

  configureLoadingHandlers() {
    this.searchMediator.registerListener(EVENT_BEFORE_SEARCH, () => this.disableComponents(true));
    this.searchMediator.registerListener(EVENT_SEARCH, () => this.disableComponents(false));
  }

  disableComponents(state) {
    this.criteriaConfig.disabled = state;
    if (this.toolbarConfig) {
      this.toolbarConfig.disabled = state;
    }
    this.config.paginationConfig.disabled = state;
  }

  /**
   * Allows any criteria component to initialize itself and modify the search criteria before triggering the search.
   */
  registerCriteriaReadyListener() {
    if (this.config.triggerSearch && this.config.renderCriteria) {
      let initialized = false;
      this.searchMediator.registerListener(CRITERIA_READY_EVENT, () => {
        if (!initialized) {
          this.searchMediator.search();
          initialized = true;
        }
      });
    }
  }

  isExternalSearchMode() {
    return this.searchMediator.searchMode === SearchCriteriaUtils.EXTERNAL_MODE;
  }

  getSelectedItems() {
    return this.config.results.config.selectedItems;
  }

  getSearchTree() {
    return this.searchMediator.queryBuilder.tree;
  }

  ngOnDestroy() {
    // Prevents duplicated listeners if the same mediator is reused later.
    this.searchMediator.listeners = {};
  }

  showToolbar() {
    return this.searchMediator.searchMode !== SearchCriteriaUtils.EXTERNAL_MODE;
  }
}
