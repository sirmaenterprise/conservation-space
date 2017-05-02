import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {SearchService} from 'services/rest/search-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator, EVENT_SEARCH} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {InstanceRestService} from 'services/rest/instance-service';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {SavedSearchLoadedEvent} from 'search/components/saved/events';
import {Eventbus} from 'services/eventbus/eventbus';
import template from './main-search.html!text';
import './main-search.css!css';

const URL_PARAM_ID = 'id';
const URL_PARAM_TEXT = 'metaText';

@Component({
  selector: 'seip-main-search',
  properties: {
    'config': 'config'
  }
})
@View({template: template})
@Inject(StateParamsAdapter, SearchService, InstanceRestService, Eventbus)
export class MainSearch extends Configurable {

  constructor(stateParamsAdapter, searchService, instanceRestService, eventbus) {
    super({});
    this.instanceRestService = instanceRestService;
    this.searchService = searchService;
    this.stateParamAdapter = stateParamsAdapter;
    this.eventbus = eventbus;

    this.render = false;
    this.createSearchConfig();
    this.initSearch();
  }

  createSearchConfig() {
    this.builder = new QueryBuilder();
    this.mediator = new SearchMediator(this.searchService, this.builder);
    this.mediator.registerListener(EVENT_SEARCH, () => {
      $("body, html").animate({scrollTop: 0}, "fast");
    });

    this.searchConfig = {
      searchMediator: this.mediator,
      criteriaType: SearchCriteriaUtils.MIXED_MODE,
      results: {
        config: {
          renderMenu: true
        }
      },
      renderHelp: true
    };
  }

  initSearch() {
    var params = this.stateParamAdapter.getStateParams() || {};
    if (params[URL_PARAM_ID]) {
      this.loadSavedSearch(params[URL_PARAM_ID]);
    } else if (params[URL_PARAM_TEXT]) {
      this.builder.init(this.getSearchTree(params[URL_PARAM_TEXT]));
      this.render = true;
      this.searchConfig.triggerSearch = true;
    } else {
      this.render = true;
    }
  }

  loadSavedSearch(id) {
    this.instanceRestService.load(id).then((response)=> {
      var data = response.data;
      var criteria = data.properties.searchCriteria;
      var parsed = JSON.parse(criteria);

      this.searchConfig.toolbar = {
        orderBy: parsed.orderBy,
        orderDirection: parsed.orderDirection,
        searchId: data.id,
        searchTitle: data.properties.title
      };
      this.searchConfig.searchMode = data.properties.searchType;
      this.mediator.searchMode = data.properties.searchType;

      this.builder.init(parsed.criteria);
      this.searchConfig.triggerSearch = true;
      this.render = true;
      this.eventbus.publish(new SavedSearchLoadedEvent(data));
    });
  }

  getSearchTree(freeText) {
    var root = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    var typesRule = SearchCriteriaUtils.getDefaultObjectTypeRule();
    var ftsRule = SearchCriteriaUtils.buildRule("freeText", "fts", "contains", freeText);
    var innerCondition = SearchCriteriaUtils.buildCondition(undefined, [ftsRule]);
    root.rules[0].rules.push(typesRule);
    root.rules[0].rules.push(innerCondition);
    return root;
  }
}