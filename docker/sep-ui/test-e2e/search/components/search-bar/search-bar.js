'use strict';

var SandboxPage = require('../../../page-object').SandboxPage;
var PageObject = require('../../../page-object').PageObject;
var SingleSelectMenu = require('../../../form-builder/form-control.js').SingleSelectMenu;
var MultySelectMenu = require('../../../form-builder/form-control.js').MultySelectMenu;
var StaticInstanceHeader = require('../../../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;
var hasClass = require('../../../test-utils').hasClass;

const SANDBOX_URL = 'sandbox/search/components/search-bar/';

class SearchBarSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    browser.wait(EC.visibilityOf(this.searchBarElement), DEFAULT_TIMEOUT);
  }

  getSearchBar() {
    return new SearchBar(this.searchBarElement);
  }

  addRecentObject() {
    return $('#add_recent_object').click();
  }

  removeRecentObject() {
    return $('#remove_recent_object').click();
  }

  assignContext() {
    return $('#assign_context').click();
  }

  isSearchTriggered() {
    return $('#triggered').isPresent();
  }

  get searchBarElement() {
    return $('#middle_search_bar');
  }

}

/**
 * Page object for the search bar component. Provides getters for the nested elements/components.
 *
 * @author Mihail Radkov
 */
class SearchBar extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.typeSelectElement), DEFAULT_TIMEOUT);
  }

  getSingleTypeSelect() {
    return new SingleSelectMenu(this.typeSelectElement);
  }

  getMultiTypeSelect() {
    return new MultySelectMenu(this.typeSelectElement);
  }

  typeFreeText(freeText) {
    this.freeTextInput.sendKeys(freeText);
    return this;
  }

  getFreeTextValue() {
    return this.freeTextInput.getAttribute('value');
  }

  clearFreeText() {
    this.freeTextInput.clear();
    return this;
  }

  search() {
    this.searchIconButton.click();
    return this;
  }

  toggleOptions() {
    this.optionsButton.click();
    return this.getSearchBarOptions();
  }

  getSearchBarOptions() {
    return new SearchBarOptions(this.searchBarOptionsElement);
  }

  hasContext() {
    return hasClass(this.contextButton, 'has-context');
  }

  getSelectedContext() {
    return this.contextButton.getAttribute('data-value');
  }

  openContextPicker() {
    this.contextButton.click();
  }

  openContextMenu() {
    this.contextMenuButton.click();
    browser.wait(EC.visibilityOf(this.contextMenuElement), DEFAULT_TIMEOUT);
    return this;
  }

  selectContextFromMenu(id) {
    this.contextMenuElement.$(`.search-context-item[data-value='${id}']`).click();
    browser.wait(EC.invisibilityOf(this.contextMenuElement), DEFAULT_TIMEOUT);
    return this;
  }

  getContextMenuOptions() {
    return this.contextMenuElement.$$(`.search-context-item[data-value]`).map((option) => {
      return option.getAttribute('data-value');
    });
  }

  getEmptyRecentObjectsMessage() {
    return this.contextMenuElement.$('.empty-recent-objects');
  }

  get typeSelectElement() {
    return this.element.$('.search-types');
  }

  get freeTextInput() {
    return this.element.$('.free-text');
  }

  get searchIconButton() {
    return this.element.$('.search-icon');
  }

  get optionsButton() {
    return this.element.$('.search-options-btn');
  }

  get contextButton() {
    return this.element.$('.search-context');
  }

  get selectedContext() {
    return this.element.$('.selected-context');
  }

  get clearContextButton() {
    return this.element.$('.clear-context');
  }

  get contextMenuButton() {
    return this.element.$('.search-context-menu-btn');
  }

  get searchBarOptionsElement() {
    return this.element.$(SearchBarOptions.COMPONENT_SELECTOR);
  }

  get contextMenuElement() {
    return this.element.$('.search-context-menu');
  }
}

SearchBar.COMPONENT_SELECTOR = '.search-bar';

/**
 * Page object for the search bar options component. Provides getters for the nested elements/components.
 *
 * @author Svetlozar Iliev
 */
class SearchBarOptions extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilClosed() {
    browser.wait(EC.invisibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilSearchesVisibility() {
    browser.wait(EC.visibilityOf(this.savedSearchesList), DEFAULT_TIMEOUT);
  }

  getSavedSearchesHeaders() {
    return this.savedSearchesList.all(by.css(StaticInstanceHeader.COMPONENT_SELECTOR));
  }

  filterSavedSearches(filterTerms = '') {
    this.savedSearchesFilterInput.sendKeys(filterTerms);
    this.waitUntilSearchesVisibility();
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }

  openAdvancedSearch() {
    this.advancedSearchModeLink.click();
    return this;
  }

  get savedSearchesFilterInput() {
    return this.element.$('.saved-search-filter');
  }

  get advancedSearchModeLink() {
    return this.element.$('.advanced-mode');
  }

  get savedSearchesList() {
    return this.element.$('.saved-searches-list');
  }

}

SearchBarOptions.COMPONENT_SELECTOR = '.search-bar-options';

module.exports = {
  SearchBarSandbox,
  SearchBar,
  SearchBarOptions
};