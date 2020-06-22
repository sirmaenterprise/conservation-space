import {Component, Inject, View} from 'app/app';
import {Router} from 'adapters/router/router';
import {Configurable} from 'components/configurable';
import {SEARCH_STATE} from 'search/components/quick-search';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {SearchService} from 'services/rest/search-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {EVENT_SEARCH, SearchMediator} from 'search/search-mediator';
import {InstanceRestService} from 'services/rest/instance-service';
import {SavedSearchLoadedEvent} from 'search/components/saved/events';
import {Eventbus} from 'services/eventbus/eventbus';
import 'search/components/common/results-toolbar';
import 'search/components/search';

import template from './main-search.html!text';
import './main-search.css!css';

const URL_PARAM_ID = 'id';

@Component({
  selector: 'seip-main-search',
  properties: {
    'config': 'config'
  }
})
@View({template})
@Inject(StateParamsAdapter, SearchService, InstanceRestService, Eventbus, Router)
export class MainSearch extends Configurable {

  constructor(stateParamsAdapter, searchService, instanceRestService, eventbus, router) {
    super({});
    this.instanceRestService = instanceRestService;
    this.searchService = searchService;
    this.stateParamAdapter = stateParamsAdapter;
    this.eventbus = eventbus;
    this.router = router;
  }

  ngOnInit() {
    this.initialize();
    this.createSearchConfig();
    this.createToolbarConfig();
    this.initSearch();
    this.subscribeToSearchEvent();
  }

  initialize() {
    this.builder = new QueryBuilder();
    this.mediator = new SearchMediator(this.searchService, this.builder);
  }

  subscribeToSearchEvent() {
    this.ignoreInitialSearch = true;
    this.mediator.registerListener(EVENT_SEARCH, (event) => {
      $('body, html').animate({scrollTop: 0}, 'fast');
      let params = this.stateParamAdapter.getStateParams() || {};
      // Ignoring the first search avoids breaking the back button navigation
      if (!this.ignoreInitialSearch) {
        let tree = QueryBuilder.encodeSearchTree(event.query.tree);
        let args = QueryBuilder.encodeSearchArguments(event.arguments);
        // try to fallback to search mode from the mediator when saved search
        let mode = !params[URL_PARAM_ID] ? params.mode : this.mediator.searchMode;
        this.router.navigate(SEARCH_STATE, {args, tree, mode}, {reload: false});
      }
      if (this.ignoreInitialSearch) {
        this.ignoreInitialSearch = false;
      }
    });
  }

  createSearchConfig() {
    this.searchConfig = {
      searchMediator: this.mediator,
      results: {
        config: {
          renderMenu: true
        }
      },
      renderHelp: true,
      useFixedToolbar: true,
      advancedOnly: true
    };
  }

  createToolbarConfig() {
    this.resultsToolbarConfig = {
      searchMediator: this.mediator
    };
  }

  initSearch() {
    let params = this.stateParamAdapter.getStateParams() || {};
    if (params[URL_PARAM_ID]) {
      this.loadSavedSearch(params[URL_PARAM_ID]);
    } else if (Object.keys(params).length > 0) {
      this.searchConfig.triggerSearch = true;

      if (params.tree) {
        this.builder.init(QueryBuilder.decodeSearchTree(params.tree));
      }

      if (params.args) {
        this.searchConfig.arguments = QueryBuilder.decodeSearchArguments(params.args);
      }

      if (params.mode) {
        this.searchConfig.searchMode = params.mode;
        this.mediator.searchMode = params.mode;
      }
      this.renderSearch = true;
    } else {
      this.renderSearch = true;
    }
  }

  loadSavedSearch(id) {
    this.instanceRestService.load(id).then((response) => {
      let data = response.data;
      let criteria = data.properties.searchCriteria;
      let parsed = JSON.parse(criteria);

      // configure ordering arguments
      this.searchConfig.arguments = {
        orderBy: parsed.orderBy,
        orderDirection: parsed.orderDirection
      };
      // configure saved search params
      this.searchConfig.savedSearch = {
        searchId: data.id,
        searchTitle: data.properties.title
      };

      this.searchConfig.searchMode = data.properties.searchType;
      this.mediator.searchMode = data.properties.searchType;

      this.builder.init(parsed.criteria);
      this.searchConfig.triggerSearch = true;
      this.renderSearch = true;
      this.eventbus.publish(new SavedSearchLoadedEvent(data));
    });
  }
}