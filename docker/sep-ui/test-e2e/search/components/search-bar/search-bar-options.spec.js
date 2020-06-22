'use strict';

var SearchBarSandbox = require('./search-bar').SearchBarSandbox;
var SearchBar = require('./search-bar').SearchBar;

describe('SearchBar', () => {

  var sandboxPage;
  var searchBar;
  beforeEach(() => {
    sandboxPage = new SearchBarSandbox();
    sandboxPage.open();
    searchBar = sandboxPage.getSearchBar();
  });

  it('should have the option to open search bar options dropdown panel', () => {
    expect(searchBar.optionsButton.isDisplayed()).to.eventually.be.true;
  });

  it('should not open the search bar options by default', () => {
    expect(searchBar.searchBarOptionsElement.isPresent()).to.eventually.be.false;
  });

  it('should show and hide search bar options panel when toggled', () => {
    searchBar.toggleOptions().waitUntilVisible();
    searchBar.toggleOptions().waitUntilClosed();
    searchBar.toggleOptions().waitUntilVisible();
  });

  it('should hide the search bar options when clicked outside of the panel', () => {
    searchBar.toggleOptions().waitUntilVisible();
    sandboxPage.assignContext();
    searchBar.getSearchBarOptions().waitUntilClosed();
  });

  it('should hide the search bar options if a search is triggered from the bar', () => {
    searchBar.toggleOptions().waitUntilVisible();
    searchBar.searchIconButton.click();
    searchBar.getSearchBarOptions().waitUntilClosed();
  });

  describe('SearchBarOptions', () => {

    let options;
    beforeEach(() => {
      options = searchBar.toggleOptions();
      options.waitUntilVisible();
    });

    it('should render saved searches by default', () => {
      options.waitUntilSearchesVisibility();
    });

    it('should allow to filter/search saved searches', () => {
      expect(options.savedSearchesFilterInput.isDisplayed()).to.eventually.be.true;

      options.filterSavedSearches();
      expect(options.getSavedSearchesHeaders().count()).to.eventually.equal(10);
    });

    it('should be hidden when a saved search is selected', () => {
      options.filterSavedSearches();
      options.getSavedSearchesHeaders().get(0).click();
      options.waitUntilClosed();
    });

    it('should allow to choose the advanced search mode', () => {
      expect(options.advancedSearchModeLink.isDisplayed()).to.eventually.be.true;
    });

    it('should be hidden when the advanced mode is chosen', () => {
      options.advancedSearchModeLink.click();
      options.waitUntilClosed();
    });
  });

});
