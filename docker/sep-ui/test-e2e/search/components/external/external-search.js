'use strict';

var SandboxPage = require('../../../page-object').SandboxPage;
var PageObject = require('../../../page-object').PageObject;
var SingleSelectMenu = require('../../../form-builder/form-control.js').SingleSelectMenu;

const SANDBOX_URL = '/sandbox/search/components/external/';

class ExternalSearchSandboxPage extends SandboxPage {

  openSandbox() {
    return super.open(SANDBOX_URL);
  }

  getSearch() {
    // This is required here to avoid "TypeError: Search is not a constructor"
    var Search = require('../search').Search;
    return new Search($('.seip-search-wrapper'));
  }
}

/**
 * PO for interacting with the external search screen and its inner components.
 *
 * @author Mihail Radkov
 */
class ExternalSearch extends PageObject {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.searchButton), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.systemSelectElement), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.searchCriteriaElement), DEFAULT_TIMEOUT);
  }

  getSelectedSystem() {
    return this.getSystemSelect().getSelectedValue();
  }

  getAvailableSystems() {
    return this.getSystemSelect().getMenuValues();
  }

  changeSystem(system) {
    return this.getSystemSelect().selectOption(system);
  }

  getSystemSelect() {
    return new SingleSelectMenu(this.systemSelectElement);
  }

  search() {
    return this.searchButton.click();
  }

  clear() {
    return this.clearButton.click();
  }

  get systemSelectElement() {
    return this.element.$('.external-systems');
  }

  get searchCriteriaElement() {
    return this.element.$('.external-search-criteria');
  }

  get searchButton() {
    return this.element.$('.btn.seip-search');
  }

  get clearButton() {
    return this.element.$('.btn.clear-criteria');
  }

  get externalResults() {
    return this.element.$('.template-results');
  }
}
ExternalSearch.COMPONENT_SELECTOR = '.external-search';

/**
 * PO for interacting with the external search results form.
 *
 * @author Mihail Radkov
 */
class ExternalSearchResults extends PageObject {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitForResults() {
    browser.wait(EC.visibilityOf(this.selectAllButton), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.resultsForm), DEFAULT_TIMEOUT);
  }

  selectAll() {
    return this.selectAllButton.click();
  }

  areAllResultsSelected() {
    return this.checkCheckboxesCheckedState(this.resultsCheckboxes, 'true');
  }

  deselectAll() {
    return this.deselectAllButton.click();
  }

  areAllResultsDeselected() {
    return this.checkCheckboxesCheckedState(this.resultsCheckboxes, 'false');
  }

  /**
   * Checks if all given checkboxes are in the desired check state.
   * @param checkboxes - the checkbox elements to check
   * @param desiredState true as string or false as string
   * @returns {Promise.<T>} resolving to true if all are in the state or false if not
   */
  checkCheckboxesCheckedState(checkboxes, desiredState) {
    var areCheckedPromises = checkboxes.map((checkbox) => {
      return checkbox.getAttribute('checked');
    });
    return Promise.all(areCheckedPromises).then((states) => {
      var allAreInDesiredState = true;
      states.forEach((state) => {
        if (state !== desiredState) {
          allAreInDesiredState = false;
        }
      });
      return allAreInDesiredState;
    });
  }

  importOrUpdate() {
    return this.importSelectedButton.click();
  }

  canImportOrUpdate() {
    return this.importSelectedButton.getAttribute('disabled').then((attribute) => {
      return !(!!attribute);
    });
  }

  get resultsForm() {
    return this.element.$('.results');
  }

  get resultsCheckboxes() {
    return this.element.all(by.css('.external-result input'));
  }

  get selectAllButton() {
    return this.element.$('#selectAll');
  }

  get deselectAllButton() {
    return this.element.$('#deselectAll');
  }

  get importSelectedButton() {
    return this.element.$('.btn.process-all');
  }
}

module.exports = {
  ExternalSearchSandboxPage,
  ExternalSearch,
  ExternalSearchResults
};