"use strict";

var PageObject = require('../../../page-object').PageObject;
var InstanceList = require('../../../instance/instance-list').InstanceList;
var SandboxPage = require('../../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/search/components/common/search-results';

const NO_SELECTION_FORM = '#no-selection';
const NO_SELECTION_FORM_WITH_ACTIONS = '#no-selection-with-actions';
const SINGE_SELECTION_FORM = '#single-selection';
const MULTI_SELECTION_FORM = '#multiple-selection';
const MULTI_SELECTION_FORM_WITH_DISABLED_ITEMS = '#multiple-selection-disabled-items';

class SearchResultsSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  getFormWithoutSelection() {
    return this.getForm(NO_SELECTION_FORM);
  }

  getFormWithActions() {
    return this.getForm(NO_SELECTION_FORM_WITH_ACTIONS);
  }

  getFormWithSingleSelection() {
    return this.getForm(SINGE_SELECTION_FORM);
  }

  getFormWithMultiSelection() {
    return this.getForm(MULTI_SELECTION_FORM);
  }

  getFormWithMultiSelectionAndDisabledItems() {
    return this.getForm(MULTI_SELECTION_FORM_WITH_DISABLED_ITEMS);
  }

  getForm(selector) {
    var searchResults = new SearchResults($(selector));
    searchResults.waitUntilOpened();
    return searchResults;
  }
}

/**
 * Page object for the search results form.
 *
 * @author Mihail Radkov
 */
class SearchResults extends PageObject {

  constructor(element) {
    super(element);
    this.resultItems = new InstanceList(this.element.$('.instance-list'));
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.resultsForm), DEFAULT_TIMEOUT);
  }

  /**
   * Waits until there are search results
   */
  waitForResults() {
    this.resultItems.waitForResults();
  }

  isResultSelected(index) {
    this.resultItems.waitForResults();
    return this.resultItems.getItems().then((items) => {
      return items[index].isSelected();
    });
  }

  /**
   * Clicks the index-ed item in the results list of instance items.
   */
  clickResultItem(index) {
    this.waitForResults();
    return this.resultItems.getItems().then((items) => {
      return items[index].select();
    });
  }

  getResultsInputs() {
    return this.element.all(by.css('input'));
  }

  getResults() {
    return this.resultItems.getItems();
  }

  getResultsCount() {
    return this.resultItems.getItemsCount();
  }

  get resultsForm() {
    return this.element.$('form');
  }
}

SearchResults.COMPONENT_SELECTOR = '.search-results';

module.exports = {
  SearchResultsSandboxPage,
  SearchResults
};