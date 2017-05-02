import {SearchToolbar, ORDER_DESC} from 'search/components/common/search-toolbar';
import template from 'search-toolbar-template!text';
import {Inject, Component, View} from 'app/app';
import 'font-awesome';

@Component({
  selector: 'seip-search-toolbar-stub'
})
@View({
  template: template
})
@Inject('$timeout')
export class SearchToolbarStub {

  constructor($timeout) {
    this.$timeout = $timeout;

    this.searchParams = {
      orderBy: 'title',
      orderDirection: ORDER_DESC
    };

    this.results = {
      total: 3
    };

    this.config = {
      orderByData: [{
        id: 'title',
        text: 'Title'
      }, {
        id: 'status',
        text: 'Status'
      }],
      orderBy: 'title',
      searchMediator : {
        arguments : {}
      },
      savedSearch: {
        render: true
      }
    };

    this.callback = (params) => {
      if (this.searchParams.orderBy !== params.orderBy || this.searchParams.orderDirection !== params.orderDirection) {
        this.searchParams = params;
        this.config.disabled = true;
        // Small delay for visible loading
        this.$timeout(() => {
          this.config.disabled = false;
        }, 500);
      }
    };
  }
}