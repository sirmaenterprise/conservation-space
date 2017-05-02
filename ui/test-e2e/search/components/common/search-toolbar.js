"use strict";

var SaveSearch = require('../saved/save-search').SaveSearch;
var SingleSelectMenu = require('../../../form-builder/form-control.js').SingleSelectMenu;

const ORDER_BY = '.order-by';
const SORT_BY = '.sort-by';
const SELECT = 'select';
const RESULT_COUNT = '.results-count .count';

// TODO: Refactor PO to use element instead of a selector and reuse other POs CMF-19383

/**
 * Page object for the search toolbar.
 *
 * @author Mihail Radkov
 */
class SearchToolbar {

  constructor(selector) {
    if (!selector) {
      throw new Error('Cannot instantiate PO without wrapper selector!');
    }
    this.selector = selector;
  }

  waitUntilOpened() {
    var orderBy = this.getOrderBy().$(SELECT);
    browser.wait(EC.visibilityOf(orderBy), DEFAULT_TIMEOUT);
  }

  getCountElement() {
    return $(this.selector).$(RESULT_COUNT);
  }

  getOrderBy() {
    return $(this.selector).$(ORDER_BY);
  }

  getOrderByValue() {
    var menu = new SingleSelectMenu(this.getOrderBy());
    return menu.getSelectedValue();
  }

  getAvailableOrderByValues() {
    return new SingleSelectMenu(this.getOrderBy()).getMenuValues();
  }

  getSortBy() {
    return $(this.selector).$(SORT_BY);
  }

  getSortByValue() {
    var sortBy = this.getSortBy();
    return sortBy.getAttribute('class').then((classes) => {
      if (classes.indexOf(SearchToolbar.ASCENDING)) {
        return SearchToolbar.ASCENDING;
      }
      return SearchToolbar.DESCENDING;
    });
  }

  getSaveSearchGroup() {
    var element = $(this.selector).$(SaveSearch.COMPONENT_SELECTOR);
    return new SaveSearch(element);
  }

  toggleOrderDirection() {
    $(this.selector).$(SORT_BY).click();
  }

  selectOrderBy(orderBy) {
    $(`${this.selector} option[value=${orderBy}]`).click();
  }
}
SearchToolbar.COMPONENT_SELECTOR = '.search-toolbar';
SearchToolbar.ASCENDING = 'ascending';
SearchToolbar.DESCENDING = 'descending';

module.exports = {SearchToolbar};