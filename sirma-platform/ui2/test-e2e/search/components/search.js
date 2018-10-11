'use strict';

let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;

let MixedSearchCriteria = require('./common/mixed-search-criteria').MixedSearchCriteria;
let Toolbar = require('./common/search-toolbar').SearchToolbar;
let SearchResults = require('./common/search-results').SearchResults;
let Pagination = require('./common/pagination');

class SearchSandbox extends SandboxPage {
  open() {
    super.open('/sandbox/search/components/search');
    return this;
  }

  getSearch() {
    return new Search($(Search.COMPONENT_SELECTOR));
  }
}

/**
 * PO wrapping the search component & providing access to the underlying components - criteria, toolbar etc. by
 * returning their corresponding PO.
 *
 * @author Mihail Radkov
 */
class Search extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getCriteria() {
    return new MixedSearchCriteria(this.element.$(MixedSearchCriteria.COMPONENT_SELECTOR));
  }

  getToolbar() {
    return new Toolbar(this.element.$(Toolbar.COMPONENT_SELECTOR));
  }

  getResults() {
    return new SearchResults(this.element.$(SearchResults.COMPONENT_SELECTOR));
  }

  getPagination() {
    return new Pagination(this.element.$(Pagination.COMPONENT_SELECTOR));
  }
}

Search.COMPONENT_SELECTOR = '.seip-search-wrapper';

module.exports = {Search, SearchSandbox};