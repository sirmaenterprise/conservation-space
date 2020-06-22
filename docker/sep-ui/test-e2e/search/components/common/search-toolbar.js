"use strict";

var SandboxPage = require('../../../page-object').SandboxPage;
let PageObject = require('../../../page-object').PageObject;
let OrderToolbar = require('./order-toolbar.js').OrderToolbar;
let ResultsToolbar = require('./results-toolbar.js').ResultsToolbar;

const SANDBOX_URL = 'sandbox/search/components/common/toolbar';

class SearchToolbarSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  clearFtsField() {
    return $('#toolbar-controls #clear-fts').click();
  }

  setFtsField() {
    return $('#toolbar-controls #set-fts').click();
  }

  loadSavedSearch() {
    return $('#toolbar-controls #load-saved').click();
  }

  getSearchToolbar() {
    return new SearchToolbar($(SearchToolbar.COMPONENT_SELECTOR));
  }
}

/**
 * Page object for the search toolbar.
 *
 * @author Mihail Radkov
 */
class SearchToolbar extends PageObject {

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getResultsToolbar() {
    return new ResultsToolbar(this.element);
  }

  getOrderToolbar() {
    return new OrderToolbar(this.element);
  }
}

SearchToolbar.COMPONENT_SELECTOR = '.search-toolbar';

module.exports = {
  SearchToolbar,
  SearchToolbarSandbox
};