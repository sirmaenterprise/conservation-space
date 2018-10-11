import {Inject, Component, View} from 'app/app';
import {ORDER_DESC, ORDER_ASC} from 'search/order-constants';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchService} from 'services/rest/search-service';
import {FTS_CHANGE_EVENT} from 'search/components/search';
import {SearchMediator, EVENT_SEARCH} from 'search/search-mediator';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import 'search/components/common/search-toolbar';

import 'font-awesome';
import template from 'search-toolbar-template!text';

@Component({
  selector: 'seip-search-toolbar-stub'
})
@View({
  template: template
})
@Inject('$timeout', SearchService)
export class SearchToolbarStub {

  constructor($timeout, service) {
    this.$timeout = $timeout;
    this.service = service;
    this.query = new QueryBuilder({});
    this.mediator = new SearchMediator(this.service, this.query);

    this.searchParams = {
      orderBy: 'title',
      orderDirection: ORDER_DESC
    };

    this.config = {
      disabled: false,
      toolbar: this.searchParams,
      searchMediator: this.mediator
    };
  }

  loadSavedSearch() {
    this.mediator.trigger(OPEN_SAVED_SEARCH_EVENT, {
      orderBy: 'emf:type',
      orderDirection: ORDER_ASC
    });
  }

  onFtsChange(text) {
    this.mediator.trigger(FTS_CHANGE_EVENT, text);
  }

  ngAfterViewInit() {
    this.mediator.trigger(EVENT_SEARCH, this.getSearchResponse());
  }

  getSearchResponse() {
    return SearchMediator.buildSearchResponse(this.query, this.getSearchResults(), this.getSearchArguments());
  }

  getSearchArguments() {
    return {
      pageSize: 5,
      pageNumber: 1
    };
  }

  getSearchResults() {
    return {
      data: {
        values: [],
        resultSize: 10
      }
    };
  }
}