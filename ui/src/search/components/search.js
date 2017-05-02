import _ from 'lodash';

import {Component, View, Inject, NgElement, NgScope, NgCompile} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';

import {SearchService} from 'services/rest/search-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {
  SearchMediator,
  EVENT_BEFORE_SEARCH,
  EVENT_SEARCH,
  EVENT_SEARCH_CRITERIA_CHANGED,
  EVENT_BEFORE_SEARCH_CRITERIA_CHANGED
} from 'search/search-mediator';
import {CURRENT_OBJECT} from 'search/resolvers/contextual-rules-resolver';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {SearchResults} from 'search/components/common/search-results';
import {NO_SELECTION} from 'search/search-selection-modes';
import {SearchToolbar} from 'search/components/common/search-toolbar';
import {Pagination} from 'search/components/common/pagination';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import {HEADER_DEFAULT} from 'instance-header/header-constants';

import template from './search.html!text';

export const SEARCH_PROPERTIES = ['id', HEADER_DEFAULT];

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
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-search',
  properties: {
    'config': 'config',
    'context': 'context'
  }
})
@View({template: template})
@Inject(NgCompile, NgScope, NgElement, Configuration, SearchService, TranslateService, PromiseAdapter, PluginsService, NamespaceService)
export class Search extends Configurable {

  constructor($compile, $scope, $element, configuration, searchService, translateService, promiseAdapter, pluginsService, namespaceService) {
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
      renderCriteria: true,
      renderToolbar: true,
      renderPagination: true,
      properties: SEARCH_PROPERTIES
    });

    this.configuration = configuration;
    this.searchService = searchService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.namespaceService = namespaceService;

    // Some configurations may need to perform async operations
    this.configure().then(() => {
      if (this.config.renderCriteria) {
        this.compileCriteria($compile, $scope, $element, pluginsService);
      } else if (this.config.triggerSearch) {
        // If configured to trigger a search and there is no criteria component to be rendered which may change the
        // criteria tree -> trigger a search
        this.searchMediator.search();
      }
    });
  }

  compileCriteria(compiler, scope, element, pluginsService) {
    // create an extensions service that does all that
    pluginsService.loadComponentModules('search-criteria', 'name').then((modules) => {
      var type = this.config.criteriaType || 'basic';
      var component = modules[type].component;
      var _html = `<${component} config="search.criteriaConfig" context="search.context"></${component}>`;
      var compiled = compiler(_html)(scope)[0];
      element.find('.search-criteria-wrapper').append(compiled);
    });
  }

  configure() {
    this.addContextualItems();
    this.configureSearchMediator();

    this.config.results.config.searchMediator = this.searchMediator;
    return this.transformPredefinedTypes().then(() => {
      this.configureCriteria();
      this.configureToolbar();
      this.configureResults();
      this.configurePagination();
      this.registerCallbacks();
      this.configureLoadingHandlers();
      this.registerOpenSavedSearchListener();
      this.registerCriteriaReadyListener();
      return this.promiseAdapter.resolve();
    });
  }

  /**
   * Assigns contextual items and default location criteria if the search is opened within a context.
   */
  addContextualItems() {
    if (this.context) {
      this.config.contextualItems = [{
        id: CURRENT_OBJECT,
        properties: {
          title: this.translateService.translateInstant('context.current.object')
        }
      }];
    }
  }

  configureSearchMediator() {
    if (!this.config.searchMediator) {
      var queryBuilder = new QueryBuilder(this.config.criteria);
      this.config.searchMediator = new SearchMediator(this.searchService, queryBuilder, this.context, {}, this.config.searchMode);
    }
    this.searchMediator = this.config.searchMediator;
    this.searchMediator.arguments.properties = this.searchMediator.arguments.properties || this.config.properties;
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
      predefinedTypes: this.config.predefinedTypes,
      renderHelp: this.config.renderHelp,
      useRootContext: this.config.useRootContext
    };
  }

  configureToolbar() {
    var toolbar = this.config.toolbar || {};
    var configPromise;
    if (toolbar.configFactory) {
      configPromise = toolbar.configFactory();
    } else {
      configPromise = this.createDefaultOrderByConfig(toolbar);
    }
    configPromise.then(config => {
      this.toolbarConfig = config || {};
      this.toolbarConfig.saveSearch = {
        searchId: toolbar.searchId,
        searchTitle: toolbar.searchTitle,
        render: !this.isExternalSearchMode() && !!this.config.renderCriteria,
        searchMediator: this.searchMediator
      };
      this.searchMediator.arguments.orderBy = toolbar.orderBy;
      this.searchMediator.arguments.orderDirection = toolbar.orderDirection;
      this.toolbarCallback = (params) => {
        this.searchMediator.arguments.orderBy = params.orderBy;
        this.searchMediator.arguments.orderDirection = params.orderDirection;
        this.searchMediator.search();
      };
    });
  }

  /**
   * @param toolbar holds configuration data, the order may be already populated from loaded saved search
   */
  createDefaultOrderByConfig(toolbar) {
    var data = [
      {id: 'emf:modifiedOn', text: 'objects.properties.modifiedOn'},
      {id: 'emf:modifiedBy', text: 'objects.properties.modifiedBy'},
      {id: 'dcterms:title', text: 'objects.properties.title'},
      {id: 'emf:type', text: 'objects.properties.type'},
      {id: 'emf:createdOn', text: 'objects.properties.createdOn'},
      {id: 'emf:createdBy', text: 'objects.properties.createdBy'}
    ];

    var promises = data.map(item => {
      return this.translateService.translate(item.text)
        .then(label => {
          item.text = label;
        });
    });

    return this.promiseAdapter.promise(resolve => {
      this.promiseAdapter.all(promises).then(() => {
        resolve({
          orderByData: data,
          orderBy: toolbar.orderBy || 'emf:modifiedOn',
          orderDirection: toolbar.orderDirection
        });
      });
    });
  }

  configurePagination() {
    var pageSize = this.configuration.get(Configuration.SEARCH_PAGE_SIZE);

    if (this.config.paginationConfig === undefined) {
      this.config.paginationConfig = {
        showFirstLastButtons: true,
        page: 1,
        pageSize: pageSize,
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
    var pagerMaxPages = this.configuration.get(Configuration.SEARCH_PAGER_MAX_PAGES);
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

      var data = response.data.values;
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

  registerOpenSavedSearchListener() {
    this.searchMediator.registerListener(OPEN_SAVED_SEARCH_EVENT, (savedSearch) => {
      if (savedSearch.orderBy && savedSearch.orderBy.length > 0) {
        this.toolbarConfig.orderBy = savedSearch.orderBy;
        this.searchMediator.arguments.orderBy = savedSearch.orderBy;
      }
      if (savedSearch.orderDirection && savedSearch.orderDirection.length > 0) {
        this.toolbarConfig.orderDirection = savedSearch.orderDirection;
        this.searchMediator.arguments.orderDirection = savedSearch.orderDirection;
      }

      this.searchMediator.searchMode = savedSearch.searchMode;
      this.searchMediator.queryBuilder.init(savedSearch.criteria);
      this.searchMediator.search();
    });
  }

  /**
   * Allows any criteria component to initialize itself and modify the search criteria before triggering the search.
   */
  registerCriteriaReadyListener() {
    if (this.config.triggerSearch && this.config.renderCriteria) {
      this.searchMediator.registerListener(CRITERIA_READY_EVENT, () => {
        this.searchMediator.search();
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

}
