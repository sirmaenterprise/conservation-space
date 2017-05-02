'use strict';

var BasicSearchCriteria = require('./common/basic-search-criteria.js').BasicSearchCriteria;
var Pagination = require('./common/pagination');
var SearchResults = require('./common/search-results').SearchResults;
var Toolbar = require('./common/search-toolbar').SearchToolbar;

// TODO: Refactor to use element instead of selector CMF-19383
class Search {
  constructor(selector) {
    this.selector = selector;
    this.criteria = new BasicSearchCriteria($(this.selector + ' .seip-basic-search-criteria'));
    this.pagination = new Pagination(this.selector + ' .seip-pagination');
    this.results = new SearchResults(this.selector + ' .search-results');
    this.toolbar = new Toolbar(this.selector + ' .search-toolbar');
  }

  pressEnterInsideFTS() {
    this.criteria.freeTextField.sendKeys(protractor.Key.ENTER);
    return this;
  }

  /**
   * Executes the search with selected criteria.
   *
   * @param expectEmptyResult If search returns an empty list, then the form is present but not visible, so no need to
   * wait because the check will hang.
   * @returns {Search}
   */
  clickSearch(expectEmptyResult) {
    this.criteria.search();
    if (!expectEmptyResult) {
      this.results.waitUntilOpened();
    }
    return this;
  }

  selectOrderBy(orderBy) {
    this.toolbar.selectOrderBy(orderBy);
    return this;
  }

  toggleOrderDirection() {
    this.toolbar.toggleOrderDirection();
    return this;
  }

  clickLastPageButton() {
    this.pagination.goToLastPage();
    return this;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    this.criteria.waitUntilVisible();
  }

  get element() {
    if (!this._element) {
      this._element = $(this.selector);
    }
    return this._element;
  }
}
Search.COMPONENT_SELECTOR = '.seip-search-wrapper';

module.exports = Search;