import {Component, Inject, NgScope, View} from 'app/app';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import {
  EVENT_BEFORE_SEARCH,
  EVENT_BEFORE_SEARCH_CRITERIA_CHANGED,
  EVENT_SEARCH,
  EVENT_SEARCH_CRITERIA_CHANGED,
  SearchMediator
} from 'search/search-mediator';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import _ from 'lodash';
import 'components/select/select';
import 'search/components/common/pagination';
import 'search/components/common/order-toolbar';
import 'search/components/common/results-toolbar';
import 'external-search/components/results-with-actions';
import 'search/components/advanced/advanced-search-section';

import {ReloadSearchEvent} from 'external-search/actions/reload-search-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionsService} from 'services/rest/actions-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {DialogService} from 'components/dialog/dialog-service';
import {Configuration} from 'common/application-config';
import {EAIService} from 'services/rest/eai-service';
import {ExternalSearchService} from 'services/rest/external-search-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {RDF_TYPE} from 'instance/instance-properties';
import {HEADER_DEFAULT} from 'instance-header/header-constants';

import './external-search.css!css';
import externalSearchTemplate from './external-search.html!text';

export const DEFAULT_ORDER_DIRECTION = 'desc';
export const SEARCH_PROPERTIES = ['id', HEADER_DEFAULT, RDF_TYPE, 'title'];

@Component({
  selector: 'seip-external-search',
  properties: {
    'config': 'config'
  }
})
@View({
  template: externalSearchTemplate
})

/**
 * Component for performing searches in external systems based on custom models and properties.
 * Relies on {@link EAIService} to load the models and on {@link ExternalSearchService} to execute the searches.
 *
 * @authors Simeon Iliev, Mihail Radkov
 */
@Inject(NgScope, ExternalSearchService, EAIService, TranslateService, PromiseAdapter, DialogService, Configuration, ActionsService, Eventbus, NotificationService)
export class ExternalSearch extends SearchCriteriaComponent {

  constructor($scope, externalSearchService, eaiService, translateService, promiseAdapter, dialogService, configuration, actionsService, eventbus, notificationService) {
    super({
      disabled: false,
      results: {
        config: {
          selection: MULTIPLE_SELECTION,
          selectedItems: []
        },
        data: []
      },
      criteria: {}
    });

    this.$scope = $scope;
    this.externalSearchService = externalSearchService;
    this.eaiService = eaiService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.dialogService = dialogService;
    this.configuration = configuration;
    this.actionsService = actionsService;
    this.eventbus = eventbus;
    this.notificationService = notificationService;
  }

  ngOnInit() {
    this.configureMediator();
    this.configureSection();
    this.configureResults();
    this.configurePagination();

    this.registerCallbacks();
    this.configureLoadingHandlers();
    this.configureSearchListener();
    this.configureEventSubscriptions();

    this.loadSystems();
    this.registerSystemWatcher();
  }

  loadSystems() {
    this.eaiService.getRegisteredSystems().then((response) => {
      var systems = this.systemsConverter(response);
      this.systemsSelectConfig = {
        multiple: false,
        data: systems,
        disabled: this.config.disabled,
        selectOnClose: true,
        defaultValue: systems[0] ? systems[0].id : undefined
      };
    });
  }

  systemsConverter(response) {
    if (response.data) {
      return response.data.map((system) => {
        return {
          id: system,
          text: system
        };
      });
    }
    return [];
  }

  registerSystemWatcher() {
    this.$scope.$watch(() => {
      return this.system;
    }, (newVal, oldVal) => {
      if (newVal && newVal !== oldVal) {
        this.clearResults();
        this.clearSelection();
        this.onSystemChange(newVal);
      }
    });
  }

  onSystemChange(system) {
    delete this.criteria;
    delete this.sectionLoaders;

    var promises = [
      this.eaiService.getModels(system),
      this.externalSearchService.getSystemConfiguration(system)
    ];

    this.promiseAdapter.all(promises).then((responses) => {
      var models = responses[0].data;
      var searchConfiguration = responses[1].data;

      if (!models || models.length < 1) {
        return;
      }

      this.updateSearchToolbar(searchConfiguration);

      // Getting the first model type's properties to build a default form out of them.
      var firstModelType = models[0];
      this.eaiService.getProperties(system, firstModelType.id).then((response) => {
        var firstTypeProperties = response.data;
        this.sectionLoaders = this.getSectionLoaders(system, models, firstTypeProperties);

        this.criteria = this.buildDefaultSearchCriteria(firstModelType, firstTypeProperties);
        this.searchMediator.queryBuilder.init(this.criteria);
      });
    });
  }

  getSectionLoaders(system, models, firstTypeProperties) {
    return {
      properties: (modelsRequest) => {
        if (modelsRequest.length < 1) {
          return this.promiseAdapter.resolve([]);
        }
        // Currently the external search works with only one type
        var requestedModel = modelsRequest[0];
        var firstModelType = models[0];
        if (requestedModel === firstModelType.id) {
          // Optimization to avoid loading them again
          return this.promiseAdapter.resolve(firstTypeProperties);
        }
        return this.eaiService.getProperties(system, requestedModel);
      }, models: () => {
        return this.promiseAdapter.resolve(models);
      }
    };
  }

  buildDefaultSearchCriteria(type, properties) {
    var root = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

    var typeCriteria = SearchCriteriaUtils.getDefaultObjectTypeRule([type.id]);
    var innerSectionCriteria = SearchCriteriaUtils.getDefaultCriteriaCondition();

    var section = root.rules[0];
    section.rules = [typeCriteria, innerSectionCriteria];

    innerSectionCriteria.rules = [];
    properties.forEach((property) => {
      let operator = property.operators && property.operators[0];
      var rule = SearchCriteriaUtils.buildRule(property.id, property.type, operator);
      innerSectionCriteria.rules.push(rule);
    });

    return root;
  }

  updateSearchToolbar(searchConfiguration) {
    if (searchConfiguration && searchConfiguration.order) {
      this.configureToolbar(searchConfiguration.order);
    }
  }

  /**
   * Constructs new mediator that is different from the provided because the external search use another search service
   * and different arguments.
   * Additionally we cannot the replace that in the configuration because it will break the other search modes.
   */
  configureMediator() {
    this.searchMediator = new SearchMediator(this.externalSearchService, new QueryBuilder({}));
  }

  configureSection() {
    // The section needs only the mediator because external search does not support anything else (context, predefined types etc.)
    this.sectionConfig = {
      searchMediator: this.searchMediator,
      disabled: this.config.disabled,
      locked: AdvancedSearchComponents.getAllComponents()
    };
  }

  search() {
    this.searchMediator.arguments.context = this.system;
    this.searchMediator.arguments.pageNumber = 1;
    this.searchMediator.arguments.pageSize = this.getPageSize();
    this.searchMediator.arguments.orderBy = this.currentOrderBy;
    this.searchMediator.arguments.orderDirection = this.currentOrderDirection;
    this.searchMediator.arguments.properties = SEARCH_PROPERTIES;
    this.searchWithNotification();
  }

  searchWithNotification() {
    this.searchMediator.search(true).then((searchResponse) => {
      // Custom solution for custom notification. Registering for after search event will show the notification if
      // the results are cleared or the search mode is changed because the SearchCriteriaComponent.clearResults() throws
      // this event with an empty result set to clear all results
      if (searchResponse.response.data.resultSize === 0) {
        this.notificationService.info(this.translateService.translateInstant('external.search.no.results'));
      }
    });
  }

  clear() {
    this.onSystemChange(this.system);
    this.clearResults();
    this.clearSelection();
  }

  clearResults() {
    // Cannot use the super's implementation because we configure new mediator that is different from that in the config.
    var emptySearchResults = SearchMediator.buildEmptySearchResults();
    var emptyResponse = SearchMediator.buildSearchResponse({}, emptySearchResults);
    this.searchMediator.trigger(EVENT_SEARCH, emptyResponse);
  }

  clearSelection() {
    this.config.results.config.selectedItems = [];
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }

  /*
   ****************************************************************************************************
   * EVERYTHING FROM BELOW IS CODE DUPLICATION WITH HIGH AMOUNT OF TECHNICAL DEPT AND MUST BE REMOVED *
   * AFTER THE DUPLICATED SEARCH RESULTS COMPONENT IS DELETED AND THIS REFACTORED TO USE THE STANDARD *
   ****************************************************************************************************
   */

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

  configureSearchListener() {
    this.searchMediator.registerListener(EVENT_SEARCH, () => {
      this.lastSearchArguments = this.searchMediator.arguments;
    });
  }

  configureEventSubscriptions() {
    this.events = [this.eventbus.subscribe(ReloadSearchEvent, () => {
      if (this.lastSearchArguments) {
        this.searchMediator.arguments = this.lastSearchArguments;
        this.searchWithNotification();
      }
    })];
  }

  disableComponents(state) {
    this.config.disabled = state;
    if (this.systemsSelectConfig) {
      this.systemsSelectConfig.disabled = state;
    }
    this.sectionConfig.disabled = state;
    if (this.toolbarConfig) {
      this.toolbarConfig.orderToolbar.disabled = state;
    }
    this.paginationConfig.disabled = state;
  }

  configureToolbar(orderConfiguration) {
    this.toolbarConfig = {
      orderToolbar: {
        orderByData: orderConfiguration.properties,
        orderBy: orderConfiguration.default
      },
      resultsToolbar: {
        searchMediator: this.searchMediator,
        message: {
          renderContext: false,
          renderType: false,
          renderFts: false
        }
      }
    };

    this.toolbarCallback = (params) => {
      if (this.config.results.config.selectedItems.length > 0) {
        this.confirmToolbarAction(params);
      } else {
        this.configureToolbarProperties(params);
        this.searchWithNotification();
        this.clearSelection();
      }
    };

    this.configureToolbarProperties({
      orderBy: orderConfiguration.default,
      orderDirection: DEFAULT_ORDER_DIRECTION
    });
  }

  configureResults() {
    this.searchMediator.registerListener(EVENT_SEARCH, (eventData) => {
      let response = eventData.response;
      this.config.results.total = response.data.resultSize || 0;
      this.config.results.data.splice(0);

      this.paginationConfig.page = this.searchMediator.arguments.pageNumber || 1;

      if (response.data.message) {
        let notification = {};
        notification.message = response.data.message.replace('\r\n', '<br>');
        notification.opts = {closeButton: true};
        this.notificationService.error(notification);
      }

      var data = response.data.values;
      if (!data || !data.length) {
        return;
      }

      this.config.results.data.push(...data.map((item) => {
        this.actionsService.getActions(item.id, {placeholder: null, contextId: null}).then((response) => {
          for (let op in response.data) {
            if (response.data[op].userOperation === 'updateInt' || response.data[op].userOperation === 'import') {
              item.selectable = true;
            }
          }
        });

        // importInstanceType is used when creating imported instance for the first time on the server, but we don't
        // need any other property, because we already have them, also at this point some of the properties have wrong
        // format (for example all of the object properties)
        if (item.properties['rdf:type'] && item.properties['rdf:type'].results) {
          item.importInstanceType = item.properties['rdf:type'].results[0];
        }

        let header = item.headers.default_header || item.properties.title;

        // if not removed, they will be send back to the server, but we already have them there
        delete item.headers;
        delete item.properties;

        return {
          id: item.dbId,
          default_header: header,
          data: item,
          type: item.instanceType || 'objectinstance',
          thumbnail: item.thumbnailImage
        };
      }));
    });
  }

  configurePagination() {
    this.paginationConfig = {
      showFirstLastButtons: true,
      page: 1,
      pageSize: this.getPageSize()
    };

    this.paginationCallback = (params) => {
      if (this.config.results.config.selectedItems.length > 0) {
        this.confirmPaginationAction(params);
      } else {
        this.searchMediator.arguments.pageNumber = params.pageNumber;
        this.searchMediator.arguments.pageSize = this.paginationConfig.pageSize;
        this.currentPage = params.pageNumber;
        this.clearSelection();
        this.searchWithNotification();
      }
    };
  }

  confirmToolbarAction(params) {
    this.dialogService.confirmation(this.translateService.translateInstant('unsaved.selection'), null, {
      buttons: [
        {id: 'confirm', label: this.translateService.translateInstant('dialog.button.ok'), cls: 'btn-primary'},
        {id: 'cancel', label: this.translateService.translateInstant('dialog.button.cancel')}
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => {
        if (buttonID === 'confirm') {
          this.configureToolbarProperties(params);
          this.clearSelection();
          this.searchWithNotification();
        } else {
          this.toolbarConfig.orderToolbar.orderBy = this.currentOrderBy;
        }
        dialogConfig.dismiss();
      }
    });
  }

  configureToolbarProperties(params) {
    this.currentOrderBy = params.orderBy;
    this.currentOrderDirection = params.orderDirection;
    this.searchMediator.arguments.orderBy = params.orderBy;
    this.searchMediator.arguments.orderDirection = params.orderDirection;
  }

  confirmPaginationAction(params) {
    this.dialogService.confirmation(this.translateService.translateInstant('unsaved.selection'), null, {
      buttons: [
        {id: 'confirm', label: this.translateService.translateInstant('dialog.button.ok'), cls: 'btn-primary'},
        {id: 'cancel', label: this.translateService.translateInstant('dialog.button.cancel')}
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => {
        if (buttonID === 'confirm') {
          this.configurePaginationProperties(params);
          this.clearSelection();
          this.searchWithNotification();
        } else {
          this.paginationConfig.page = this.currentPage;
        }
        dialogConfig.dismiss();
      }
    });
  }

  /**
   * Adds pagination properties when switching between pages.
   */
  configurePaginationProperties(params) {
    this.currentPage = params.pageNumber;
    this.searchMediator.arguments.pageNumber = params.pageNumber;
    this.searchMediator.arguments.pageSize = this.paginationConfig.pageSize;
  }

  getPageSize() {
    return this.configuration.get(Configuration.SEARCH_PAGE_SIZE);
  }

}