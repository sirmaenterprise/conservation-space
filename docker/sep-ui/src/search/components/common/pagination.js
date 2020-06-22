import _ from 'lodash';
import {Configurable} from 'components/configurable';
import {Component, Inject, NgScope, View} from 'app/app';
import './pagination.css!';
import paginationTemplate from './pagination.html!text';

/**
 * Reusable and configurable pagination component.
 *
 * The component relies on correct configuration for <code>pageSize</code> to be able to correctly make pag number
 * criteria. It's important to provide callback function to the component so it can actually change pages.
 *
 * An additional configuration <code>pageRotationStep</code> allows to specify the step when rotating pages.
 * Example: if <code>pageRotationStep=2</code> and the pages count is bigger than 5, then the active page
 * button will be surrounded by total of 4 more buttons.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-pagination',
  properties: {
    'config': 'config',
    'total': 'total',
    'page': 'page',
    'callback': 'callback'
  }
})
@View({
  template: paginationTemplate
})
@Inject(NgScope)
export class Pagination extends Configurable {

  constructor($scope) {
    const defaultConfiguration = {
      pageSize: 25,
      pageRotationStep: 2,
      showFirstLastButtons: false,
      disabled: false
    };
    super(defaultConfiguration);
    this.$scope = $scope;
    // Defaults
    this.total = this.total || 0;
    this.page = this.page || 1;
    // Initial slice
    this.pages = this.getPagesSlice();
    this.bindSearch();
  }

  /**
   * Decides if the pagination component should be visible or not. If the total count of
   * pages is below 2 or there are no results, the component isn't visible.
   *
   * @returns {boolean}
   */
  showPagination() {
    return this.getTotalPages() > 1;
  }

  /**
   * Tells if the first and last page buttons should be rendered or not. If the max visible pages are less
   * than the total pages then there is a need for additional navigational buttons.
   *
   * @returns {boolean}
   */
  showFirstLastButtons() {
    if (this.config.showFirstLastButtons) {
      return true;
    }
    return this.getMaxVisiblePages() < this.getTotalPages();
  }

  disableFirstPageButton() {
    return this.page === 1 || this.config.disabled;
  }

  disableLastPageButton() {
    return this.page === this.getTotalPages() || this.config.disabled;
  }

  /**
   * Goes to the specified page number and invokes the callback with the new page number.
   *
   * @param number - the new page number
   */
  goToPage(number) {
    if (this.page !== number && !this.config.disabled) {
      this.page = number;
      if (this.callback) {
        this.callback({
          pageNumber: this.page
        });
      }
      this.pages = this.getPagesSlice();
    }
  }

  goToLastPage() {
    this.goToPage(this.getTotalPages());
  }

  /**
   * Calculates the visible page slice based on the total page count and page rotation step.
   *
   * @returns {Array}
   */
  getPagesSlice() {
    var totalPages = this.getTotalPages();
    var step = this.config.pageRotationStep;
    var visiblePages = this.getMaxVisiblePages();

    var start = Math.max(1, Math.min(this.page - step, totalPages + 1 - step, totalPages + 1 - visiblePages));
    var end = Math.min(start + visiblePages, totalPages + 1);

    return _.range(start, end);
  }

  /**
   * Calculates the total number of pages based on the result count and the configured page size.
   *
   * @returns {number}
   */
  getTotalPages() {
    return Math.ceil(this.total / this.config.pageSize);
  }

  /**
   * Binding after the component is re-enabled to recalculate the page slice.
   */
  bindSearch() {
    this.$scope.$watch(() => {
      return this.config.disabled;
    }, (newValue, oldValue) => {
      if (!newValue && oldValue) {
        this.pages = this.getPagesSlice();
      }
    });

    this.$scope.$watchCollection(() => {
      return [this.config.pageSize, this.total];
    }, () => {
      this.pages = this.getPagesSlice();
    });
  }

  getMaxVisiblePages() {
    return this.config.pageRotationStep * 2 + 1;
  }
}