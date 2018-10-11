import {Component, View, Inject} from 'app/app';
import {EVENT_CRITERIA_RESET, EVENT_CRITERIA_PROPERTY_CHANGED} from 'search/search-mediator';
import {Configurable} from 'components/configurable';
import {QueryBuilder} from 'search/utils/query-builder';
import {TranslateService} from 'services/i18n/translate-service';
import {ORDER_DESC, ORDER_RELEVANCE} from 'search/order-constants';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {DCTERMS_TITLE, EMF_CREATED_ON, EMF_MODIFIED_ON, EMF_TYPE} from 'instance/instance-properties';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {FTS_CHANGE_EVENT} from 'search/components/search';
import _ from 'lodash';

import 'search/components/common/order-toolbar';
import 'search/components/common/results-toolbar';

import 'font-awesome/css/font-awesome.css!';
import './search-toolbar.css!';
import toolbarTemplate from './search-toolbar.html!text';

/**
 * Search toolbar component wrapping all auxiliary to the main search components.
 * The search toolbar currently wraps the {@ResultsToolbar} and {@OrderToolbar}
 * components and operates with them, configuring & modifying them accordingly.
 *
 * Search toolbar reacts to 3 main search related events, which depend on the
 * search mediator which must be provided as a configuration to this component.
 * The main events on which the component operates are the following:
 *
 * FTS_CHANGE_EVENT - each time an fts change has been detected, this event is
 * triggered and the search toolbar controls the {@OrderToolbar} currently
 * selected option & options data. Namely while a text is present inside the fts
 * then the relevance option will be enabled and all searches will be performed
 * using the relevance order option. If fts is cleared or no text is present then
 * the search toolbar will revert the default selected option for {@OrderToolbar}
 * to either the previously selected or the default - modifiedOn option
 *
 * OPEN_SAVED_SEARCH_EVENT - once a saved search is opened this event will alert
 * the search toolbar which will configure the {@OrderToolbar} and the searchMediator
 * prepare them and trigger the actual saved search which was delivered by the event
 *
 * EVENT_CLEAR - when this event is received, the search toolbar will take care of
 * resetting the {@OrderToolbar} order & direction appropriately. It is taking in
 * consideration whether the fts field is empty and what is the current state of
 * the relevance option - disabled / enabled.
 *
 * EVENT_CRITERIA_PROPERTY_CHANGED - if a criteria property in the search tree is changed
 * with another different than freeText, then the toolbar will disable the relevancy
 * option if it was enabled before that.
 *
 * Initial order by and order direction are obtained from the search mediator arguments
 * if present. If not, default ones will be assigned.
 *
 * Further more the search toolbar is constructing by default the order data
 * which is provided to the {@OrderToolbar} for display. Search toolbar accepts
 * simplistic configuration which is used to control the render or disable state
 * of each component that is is wrapping.
 *
 * Example configuration for SearchToolbar:
 * {
 *    disabled: false,
 *    renderOrderBar: true,
 *    renderResultBar: true,
 *    useMinimalResultToolbar: false,
 *    searchMediator: {...}
 *  }
 *
 * @author Mihail Radkov
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-search-toolbar',
  properties: {
    'config': 'config'
  }
})
@View({
  template: toolbarTemplate
})
@Inject(TranslateService)
export class SearchToolbar extends Configurable {

  constructor(translateService) {
    super({
      disabled: false,
      renderOrderBar: true,
      renderResultBar: true,
      useMinimalResultToolbar: false
    });
    this.translateService = translateService;
  }

  ngOnInit() {
    this.constructResultsToolbarConfig();
    this.constructOrderToolbarConfig();
    this.registerFtsChangeListener();
    this.registerPropertyChangeListener();
    this.registerCriteriaResetListener();
    this.registerOpenSavedSearchListener();
  }

  constructResultsToolbarConfig() {
    this.resultsToolbar = {
      searchMediator: this.config.searchMediator
    };

    if (this.config.useMinimalResultToolbar) {
      this.resultsToolbar.message = {
        renderType: false,
        renderContext: false,
        renderFts: false
      };
    }
  }

  constructOrderToolbarConfig() {
    let mediatorArguments = this.config.searchMediator.arguments;

    let orderByData = this.createOrderByOptions();
    let orderDirection = mediatorArguments.orderDirection || ORDER_DESC;
    let orderBy = this.getOrderBy(orderByData, mediatorArguments.orderBy);

    this.orderToolbar = {
      orderBy: orderBy,
      orderByData: orderByData,
      orderDirection: orderDirection
    };
    this.relevancyOption = orderByData[0];
  }

  createOrderByOptions() {
    let ftsRule = this.getFreeTextRule();
    let disableRelevance = !QueryBuilder.ruleHasValues(ftsRule);

    let orderByOptions = [
      //enable the relevance as a first option if the ftsContent is not empty or undefined
      {id: ORDER_RELEVANCE, text: 'objects.properties.relevance', disabled: disableRelevance},
      {id: EMF_MODIFIED_ON, text: 'objects.properties.modifiedOn', disabled: false},
      {id: DCTERMS_TITLE, text: 'objects.properties.title', disabled: false},
      {id: EMF_TYPE, text: 'objects.properties.type', disabled: false},
      {id: EMF_CREATED_ON, text: 'objects.properties.createdOn', disabled: false}
    ];

    //translate the labels for all options
    orderByOptions.forEach(item => {
      item.text = this.translateService.translateInstant(item.text);
    });

    return orderByOptions;
  }

  registerFtsChangeListener() {
    this.preserveCurrentOrder();

    this.config.searchMediator.registerListener(FTS_CHANGE_EVENT, (text) => {
      this.toggleRelevancyState(text);
      this.assignSearchMediatorArguments();
    });
  }

  registerPropertyChangeListener() {
    this.config.searchMediator.registerListener(EVENT_CRITERIA_PROPERTY_CHANGED, (event) => {
      if (event.newProperty !== SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD && event.oldProperty === SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD) {
        this.toggleRelevancyState();
        this.assignSearchMediatorArguments();
      }
    });
  }

  registerCriteriaResetListener() {
    this.config.searchMediator.registerListener(EVENT_CRITERIA_RESET, () => {
      this.toggleRelevancyState();
      this.assignSearchMediatorArguments();
    });
  }

  registerOpenSavedSearchListener() {
    this.config.searchMediator.registerListener(OPEN_SAVED_SEARCH_EVENT, (savedSearch) => {
      if (savedSearch.orderBy && savedSearch.orderBy.length > 0) {
        this.orderToolbar.orderBy = this.getOrderBy(this.orderToolbar.orderByData, savedSearch.orderBy);
      }
      if (savedSearch.orderDirection && savedSearch.orderDirection.length > 0) {
        this.orderToolbar.orderDirection = savedSearch.orderDirection;
      }

      this.assignSearchMediatorArguments();
      this.config.searchMediator.searchMode = savedSearch.searchMode;
      this.config.searchMediator.queryBuilder.init(savedSearch.criteria);
      SearchCriteriaUtils.assignRestrictions(savedSearch.criteria, this.config.restrictions);
      this.config.searchMediator.search();
    });
  }

  toggleRelevancyState(text) {
    let hasFts = text && text.length > 0;

    if (hasFts && this.relevancyOption.disabled) {
      this.preserveCurrentOrder();
      this.enableRelevance();
    } else if (!hasFts && !this.relevancyOption.disabled) {
      this.disableRelevance(this.previousOrder);
    }
  }

  preserveCurrentOrder() {
    let relevanceId = this.relevancyOption.id;
    // use current order by option as a previous option only if it's not
    // relevance otherwise use modified on as a default previous order by option
    this.previousOrder = this.orderToolbar.orderBy !== relevanceId ? this.orderToolbar.orderBy : EMF_MODIFIED_ON;
  }

  /**
   * Enable the the relevance option and set the order by option to relevance.
   */
  enableRelevance() {
    this.relevancyOption.disabled = false;
    this.orderToolbar.orderBy = ORDER_RELEVANCE;
  }

  /**
   * Disable the relevance option and set the order by option to the provided one.
   *
   * @param orderBy the provided fallback order value
   */
  disableRelevance(orderBy) {
    this.relevancyOption.disabled = true;
    this.orderToolbar.orderBy = orderBy;
  }

  onOrderChanged(params) {
    this.assignSearchMediatorArguments(params);
    this.config.searchMediator.search();
  }

  getSearchTree() {
    return this.config.searchMediator.queryBuilder.tree;
  }

  getFreeTextRule() {
    return QueryBuilder.getFirstRule(this.getSearchTree(), SearchCriteriaUtils.CRITERIA_FTS_RULE_FIELD);
  }

  /**
   * Checks that the order by value is present in the available order by options and returns it if so.
   * If the provided order by is not present, the first one from the available options is returned as default.
   *
   * @param orderByData - the available options
   * @param orderBy - the checked value
   */
  getOrderBy(orderByData, orderBy) {
    let predefinedOrderByExists = !!_.find(orderByData, (orderByOption) => {
      // select the order only if matching & not actually disabled
      return orderByOption.id === orderBy && !orderByOption.disabled;
    });
    if (predefinedOrderByExists) {
      return orderBy;
    }
    // find the first order by option which is not disabled
    return _.find(orderByData, order => {
      return !order.disabled;
    }).id;
  }

  getOrderByRelevance(orderByData) {
    return _.find(orderByData, (option) => {
      return option.id === ORDER_RELEVANCE;
    });
  }

  assignSearchMediatorArguments(params) {
    let args = params || {
        orderBy: this.orderToolbar.orderBy,
        orderDirection: this.orderToolbar.orderDirection
      };
    this.config.searchMediator.arguments.orderBy = args.orderBy;
    this.config.searchMediator.arguments.orderDirection = args.orderDirection;
  }
}