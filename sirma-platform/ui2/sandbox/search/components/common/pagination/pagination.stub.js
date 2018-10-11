import {Component, View, Inject} from 'app/app';
import {Pagination} from 'search/components/common/pagination';
import paginationTemplateStub from 'pagination-template!text';

@Component({
  selector: 'seip-pagination-stub'
})
@View({
  template: paginationTemplateStub
})
@Inject('$timeout')
export class PaginationStub {
  constructor($timeout) {
    this.$timeout = $timeout;

    this.twoPagesNoButtons = {
      total: 30,
      config: {
        showFirstLast: false
      }
    };
    this.twoPagesNoButtons.callback = this.getCallback(this.twoPagesNoButtons);

    this.twoPagesResults = {
      total: 30,
      config: {
        showFirstLastButtons: true
      }
    };
    this.twoPagesResults.callback = this.getCallback(this.twoPagesResults);

    this.multipleResults = {
      total: 375,
      config: {
        pageSize: 50
      }
    };
    this.multipleResults.callback = this.getCallback(this.multipleResults);
  }

  getCallback(results) {
    return () => {
      results.config.disabled = true;
      // Small timeout for visible loading
      this.$timeout(() => {
        results.config.disabled = false;
      }, 500);
    }
  }
}