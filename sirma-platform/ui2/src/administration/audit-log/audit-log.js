import {View, Component, Inject} from 'app/app';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {
  SearchMediator,
  EVENT_BEFORE_SEARCH,
  EVENT_SEARCH
} from 'search/search-mediator';
import {NO_SELECTION} from 'search/search-selection-modes';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'components/select/select';
import 'search/components/advanced/advanced-search-section';
import 'search/components/common/pagination';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {AuditLogService} from 'services/rest/audit-log-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {MomentAdapter} from 'adapters/moment-adapter';

import template from './audit-log.html!text';
import './audit-log.css!css';

@Component({
  selector: 'seip-audit-log',
  properties: {
    'config': 'config'
  }
})
@View({template: template})
@Inject(AuditLogService, TranslateService, PromiseAdapter, Configuration, MomentAdapter)
export class AuditLog extends SearchCriteriaComponent {

  constructor(auditLogService, translateService, promiseAdapter, configuration, momentAdapter) {
    super({
      disabled: false,
      results: {
        config: {
          selection: NO_SELECTION,
          selectedItems: []
        },
        data: []
      },
      criteria: {}
    });
    this.auditLogService = auditLogService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.configuration = configuration;
    this.momentAdapter = momentAdapter;
  }

  ngOnInit() {
    this.datePattern = this.configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + this.configuration.get(Configuration.UI_TIME_FORMAT);

    // Search predefined fields
    this.auditFields = [
      {id: 'actionid', text: 'audit.action', 'type': 'action'},
      {
        id: 'objectsystemid',
        text: 'audit.object',
        'type': 'object',
        operators: [AdvancedSearchCriteriaOperators.SET_TO.id, AdvancedSearchCriteriaOperators.NOT_SET_TO.id, AdvancedSearchCriteriaOperators.EMPTY.id]
      },
      {id: 'eventdate', text: 'audit.eventdate', 'type': 'dateTime'},
      {id: 'username', text: 'audit.userdisplayname', 'type': 'authority'}
    ];

    this.auditFields.forEach((field) => {
      field.text = this.translateService.translateInstant(field.text);
    });

    // Result table head fields
    this.auditTableFields = [
      {id: 'eventdate', text: 'audit.eventdate'},
      {id: 'actionid', text: 'audit.action'},
      {id: 'username', text: 'audit.userdisplayname'},
      {id: 'objecttitle', text: 'audit.title'},
      {id: 'objectid', text: 'audit.objectid'},
      {id: 'objecttype', text: 'audit.type'},
      {id: 'objectsubtype', text: 'audit.subtype'},
      {id: 'objectstate', text: 'audit.state'},
      {id: 'context', text: 'audit.context'}
    ];

    this.auditTableFields.forEach((field) => {
      field.text = this.translateService.translateInstant(field.text);
    });

    this.assignSearchMediator();
    this.assignCriteria();
    this.assignLoaders();
    this.configureResults();
    this.configurePagination();
    this.configureLoadingHandlers();
  }

  assignSearchMediator() {
    this.config.searchMediator = new SearchMediator(this.auditLogService, new QueryBuilder({}));
  }

  assignLoaders() {
    this.loaders = {
      properties: () => {
        return this.promiseAdapter.resolve(this.auditFields);
      }
    };
  }

  assignCriteria() {
    let defaultCriteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    this.config.searchMediator.queryBuilder.init(defaultCriteria);
    this.criteria = this.config.searchMediator.queryBuilder.tree.rules[0];
  }

  configureResults() {
    this.config.searchMediator.registerListener(EVENT_SEARCH, (eventData) => {
      let response = eventData.response;

      this.config.results.total = response.data.total || 0;
      this.config.results.data.splice(0);
      this.config.paginationConfig.page = this.config.searchMediator.arguments.pageNumber || 1;

      let data = response.data.records;
      if (!data || !data.length) {
        return;
      }

      this.config.results.data.push(...data);
    });
  }

  configureLoadingHandlers() {
    this.config.searchMediator.registerListener(EVENT_BEFORE_SEARCH, () => this.disableComponents(true));
    this.config.searchMediator.registerListener(EVENT_SEARCH, () => this.disableComponents(false));
  }

  disableComponents(state) {
    this.config.disabled = state;
    this.config.paginationConfig.disabled = state;
  }

  configurePagination() {
    var pageSize = this.configuration.get(Configuration.SEARCH_PAGE_SIZE);

    if (!this.config.paginationConfig) {
      this.config.paginationConfig = {
        showFirstLastButtons: true,
        page: 1,
        pageSize: pageSize
      };
    }

    // This configuration is not forced in the backend so we must provide it from here.
    this.config.searchMediator.arguments.pageSize = pageSize;

    this.paginationCallback = (params) => {
      this.config.searchMediator.arguments.pageNumber = params.pageNumber;
      this.config.searchMediator.search();
    };
  }

  clearResults() {
    // Cannot use the super's implementation because we configure new mediator that is different from that in the config.
    var emptySearchResults = SearchMediator.buildEmptySearchResults();
    var emptyResponse = SearchMediator.buildSearchResponse({}, emptySearchResults);
    this.config.searchMediator.trigger(EVENT_SEARCH, emptyResponse);
  }

  parseDate(date) {
    return this.momentAdapter.format(date, this.datePattern);
  }

  clear() {
    this.assignCriteria();
    this.clearResults();
  }

}