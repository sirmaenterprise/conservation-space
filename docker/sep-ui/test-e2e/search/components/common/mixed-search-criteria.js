"use strict";

let SandboxPage = require('../../../page-object').SandboxPage;
let PageObject = require('../../../page-object').PageObject;
let SearchBar = require('../search-bar/search-bar').SearchBar;
let AdvancedSearch = require('../advanced/advanced-search.js').AdvancedSearch;
let ExternalSearch = require('../external/external-search.js').ExternalSearch;

const SANDBOX_URL = 'sandbox/search/components/common/mixed-search-criteria';

class MixedSearchCriteriaSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  setSimpleCriteria() {
    $('#simple_criteria').click();
  }

  setComplexCriteria() {
    $('#complex_criteria').click();
  }

  getCriteria() {
    return new MixedSearchCriteria($(MixedSearchCriteria.COMPONENT_SELECTOR));
  }
}

class MixedSearchCriteria extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getSearchBar() {
    return new SearchBar(this.element.$(SearchBar.COMPONENT_SELECTOR));
  }

  getAdvancedSearch() {
    return new AdvancedSearch(this.element.$(AdvancedSearch.COMPONENT_SELECTOR));
  }

  getExternalSearch() {
    return new ExternalSearch(this.element.$(ExternalSearch.COMPONENT_SELECTOR));
  }

  closeAdvancedSearch() {
    this.getAdvancedSearchCloseButton().click();
    return this;
  }

  closeExternalSearch() {
    this.getExternalSearchCloseButton().click();
    return this;
  }

  getAdvancedSearchCloseButton() {
    return this.element.$('.advanced-search-close-btn');
  }

  getExternalSearchCloseButton() {
    return this.element.$('.external-search-close-btn');
  }

}

MixedSearchCriteria.COMPONENT_SELECTOR = '.mixed-search-criteria';

module.exports = {MixedSearchCriteriaSandbox, MixedSearchCriteria};