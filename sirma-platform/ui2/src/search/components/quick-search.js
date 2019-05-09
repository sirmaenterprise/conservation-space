import {Component, View, Inject} from 'app/app';
import {QueryBuilder} from 'search/utils/query-builder';
import {Configuration} from 'common/application-config';
import {Configurable} from 'components/configurable';
import {Router} from 'adapters/router/router';
import {IdocContext} from 'idoc/idoc-context';
import {Eventbus} from 'services/eventbus/eventbus';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {InstanceRestService} from 'services/rest/instance-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {IDOC_STATE} from 'idoc/idoc-constants';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import _ from 'lodash';

import 'search/components/search-bar/search-bar';

import './quick-search.css!css';
import template from './quick-search.html!text';

export const SEARCH_STATE = 'search';
export const OPEN_SEARCH_STATE = 'open-search';

/**
 * Search component that wraps & controls the <code>SearchBar</code> component providing easy access to the search
 * functionality.
 *
 * This component is context aware and will always use the current context's root for the search bar.
 * Search context is refreshed under two conditions (if enabled with Configuration.SEARCH_CONTEXT_UPDATE):
 * 1) When an iDoc is loaded
 * 2) When an action is executed and the context needs to be refreshed
 *
 * It supports updating the search bar model on two occasions by reading the query parameters:
 * 1) On initialization, if state is the SEARCH_STATE
 * 2) On every successful state change, if state is again the SEARCH_STATE
 * This allows for proper back & forward navigation throughout executed searches from the quick search
 *
 * The component provides an event function to the <code>SearchBar</code> component which if invoked will navigate to
 * the search view with the provided query parameters.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-quick-search',
  properties: {
    'config': 'config'
  }
})
@View({template})
@Inject(Configuration, Router, Eventbus, IdocContextFactory, InstanceRestService)
export class QuickSearch extends Configurable {

  constructor(configuration, router, eventbus, idocContextFactory, instanceRestService) {
    super({});
    this.configuration = configuration;
    this.router = router;
    this.eventbus = eventbus;
    this.idocContextFactory = idocContextFactory;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    this.createSearchBarConfig();
    this.setDefaultSearchBarModel();

    this.events = [
      this.eventbus.subscribe(AfterIdocLoadedEvent, () => this.onInstanceLoad()),
      this.eventbus.subscribe(InstanceRefreshEvent, () => this.onInstanceLoad()),
      this.eventbus.subscribe(RouterStateChangeSuccessEvent, (event) => this.onStateChanged(event))
    ];
  }

  createSearchBarConfig() {
    this.searchBarConfig = {
      enableCurrentObject: this.isIdocState()
    };
  }

  isIdocState() {
    return this.router.getCurrentState() === IDOC_STATE;
  }

  /**
   * Extracts query parameters to initialize the search bar model if the current state is the main search.
   */
  readSearchStateParams(params) {
    if (this.router.getCurrentState() === SEARCH_STATE && params.tree) {
      let tree = QueryBuilder.decodeSearchTree(params.tree);
      this.searchBarModel.freeText = this.getFreeText(tree);
      this.searchBarModel.objectType = this.getObjectType(tree);

      let context = this.getContext(tree);
      if (context) {
        if (this.shouldRefreshContext(context)) {
          this.loadContextInstance(context).then((context) => {
            this.searchBarModel.context = context;
          });
        }
      } else {
        delete this.searchBarModel.context;
      }
    }
  }

  loadContextInstance(id) {
    // Load only the minimal required fields for the context instance
    return this.instanceRestService.load(id, {
      params: {
        properties: [HEADER_BREADCRUMB]
      }
    }).then((response) => {
      return response.data;
    });
  }

  onStateChanged(event) {
    let params = this.getCurrentStateParams(event);

    if (params.mode !== SearchCriteriaUtils.ADVANCED_MODE) {
      // process only when not inside advanced search mode
      this.updateSearchBarConfig();
      this.readSearchStateParams(params);
      this.resetContext();
    } else {
      // reset search bar model to default
      this.setDefaultSearchBarModel();
    }
  }

  resetContext() {
    this.idocContext = this.idocContextFactory.getCurrentContext();
  }

  updateSearchBarConfig() {
    let isIdocState = this.isIdocState();
    // ensure current object is cleared properly
    if (!isIdocState && this.isCurrentObject()) {
      // clear current object
      delete this.searchBarModel.context;
    }
    this.searchBarConfig.enableCurrentObject = isIdocState;
  }

  setDefaultSearchBarModel() {
    // search bar default model
    this.searchBarModel = {
      // reset search bar object type to default
      objectType: SearchCriteriaUtils.ANY_OBJECT
    };
  }

  onSearch(params) {
    if (this.shouldFetchCurrentObject(params)) {
      // fetch the current context when current object is present
      this.idocContext = this.idocContextFactory.getCurrentContext();
      // set the context to the id of the current object
      params.context = this.idocContext.getCurrentObjectId();
      delete this.searchBarModel.context;
      this.performSearch(params);
    } else {
      // simply perform search
      this.performSearch(params);
    }
  }

  performSearch(params) {
    let tree = SearchCriteriaUtils.getSearchTree(params);
    this.router.navigate(SEARCH_STATE, {tree: QueryBuilder.encodeSearchTree(tree)}, {reload: true, inherit: false});
  }

  loadSavedSearch(savedSearch) {
    this.router.navigate(OPEN_SEARCH_STATE, {id: savedSearch.id}, {reload: true});
  }

  changeMode(mode) {
    this.router.navigate(SEARCH_STATE, {mode}, {reload: true});
  }

  onInstanceLoad() {
    let shouldUpdateContext = this.configuration.get(Configuration.SEARCH_CONTEXT_UPDATE);
    if (shouldUpdateContext) {
      this.fetchContext();
    }
  }

  fetchContext() {
    this.idocContext = this.idocContextFactory.getCurrentContext();
    if (!this.idocContext) {
      return;
    }
    this.fetchRootContext(this.idocContext);
  }

  fetchRootContext(currentContext) {
    currentContext.getCurrentObject().then((currentObject) => {
      let rootContext = IdocContext.getRootContextWithReadAccess(currentObject.getContextPath());
      if (rootContext) {
        if (rootContext.id === currentObject.getId()) {
          this.searchBarModel.context = currentObject;
        } else if (this.shouldRefreshContext(rootContext.id)) {
          currentContext.loadObject(rootContext.id).then((rootContextInstance) => {
            this.searchBarModel.context = rootContextInstance;
          });
        }
      }
    });
  }

  shouldFetchCurrentObject(params) {
    return params.context === CURRENT_OBJECT;
  }

  /**
   * Determines if the search bar context should be refreshed/reloaded based on the provided instance ID.
   * @param instanceId - the instance id to check against
   * @returns {boolean} true if their instance ID differ or the search bar context is undefined; false otherwise
   */
  shouldRefreshContext(instanceId) {
    return !this.searchBarModel.context || this.searchBarModel.context.id !== instanceId;
  }

  getObjectType(tree) {
    return this.extractValues(tree, SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD) || SearchCriteriaUtils.ANY_OBJECT;
  }

  getFreeText(tree) {
    return this.extractValues(tree, SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
  }

  getContext(tree) {
    return this.extractValues(tree, SearchCriteriaUtils.ANY_RELATION);
  }

  /**
   * Extracts the most recent state params used to navigate
   * to the current state from a RouterStateChangeSuccessEvent
   *
   * @param event the payload provided with RouterStateChangeSuccessEvent
   */
  getCurrentStateParams(event) {
    // ensure that a valid event payload has been passed as a parameter
    return event && event.length && event[0].length >= 2 ? event[0][2] : {};
  }

  isCurrentObject() {
    return this.searchBarModel.context && this.searchBarModel.context.id === CURRENT_OBJECT;
  }

  /**
   * De-register subscriptions when the component is destroyed.
   */
  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }

  /**
   * Extracts values for a given field from an existing search tree
   *
   * @param tree the search tree
   * @param field the field's values to be extracted
   */
  extractValues(tree, field) {
    let rule = QueryBuilder.getFirstRule(tree, field);
    // check if rule has a value associated with it
    if (QueryBuilder.ruleHasValues(rule)) {
      // search bar model only cares about single valued fields
      return _.isArray(rule.value) ? rule.value[0] : rule.value;
    }
  }
}
