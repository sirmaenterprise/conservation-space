"use strict";

var InstanceList = require('../../../instance/instance-list').InstanceList;
var SandboxPage = require('../../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/search/components/common/results';

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
    var searchResults = new SearchResults(selector);
    searchResults.waitUntilOpened();
    return searchResults;
  }
}

/**
 * Page object for the search results form.
 *
 * @author Mihail Radkov
 */
class SearchResults {

  constructor(selector) {
    if (!selector) {
      throw new Error('Cannot instantiate PO without wrapper selector!');
    }
    this.selector = selector;
    this.resultItems = new InstanceList($(`${selector} .instance-list`));
  }

  /**
   * Waits until the search results form is loaded and visible.
   */
  waitUntilOpened() {
    var resultsForm = $(this.selector).$('form');
    browser.wait(EC.visibilityOf(resultsForm), DEFAULT_TIMEOUT);
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
    this.resultItems.waitForResults();
    return this.resultItems.getItems().then((items) => {
      return items[index].select();
    });
  }

  getResultsInputs() {
    return $(this.selector).all(by.css('input'));
  }

  getResults() {
    return this.resultItems.getItems();
  }

  get resultCount() {
    return this.resultItems.getItemsCount();
  }
}
SearchResults.COMPONENT_SELECTOR = '.search-results';

module.exports = {
  SearchResultsSandboxPage,
  SearchResults
};