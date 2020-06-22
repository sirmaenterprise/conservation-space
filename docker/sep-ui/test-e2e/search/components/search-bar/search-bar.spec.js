'use strict';

var SearchBarSandbox = require('./search-bar').SearchBarSandbox;
var SearchBar = require('./search-bar').SearchBar;

var ObjectPickerDialog = require('../../../picker/object-picker').ObjectPickerDialog;
var StaticInstanceHeader = require('../../../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;

describe('SearchBar', () => {

  var sandboxPage = new SearchBarSandbox();
  var searchBar;
  beforeEach(() => {
    sandboxPage.open();
    searchBar = sandboxPage.getSearchBar();
  });

  describe('Object type menu', () => {
    it('should allow to select an object type', () => {
      expect(searchBar.typeSelectElement.isDisplayed()).to.eventually.be.true;
    });

    it('should choose All object types by default', () => {
      expect(searchBar.getSingleTypeSelect().getSelectedValue()).to.eventually.equal('anyObject');
    });

    it('should allow to choose different object type', () => {
      var documentType = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document';
      searchBar.getSingleTypeSelect().selectFromMenu(undefined, documentType, false);
      expect(searchBar.getSingleTypeSelect().getSelectedValue()).to.eventually.equal(documentType);
    });
  });

  describe('User input', () => {
    it('should allow to type a free text query', () => {
      expect(searchBar.freeTextInput.isDisplayed()).to.eventually.be.true;
      searchBar.typeFreeText('foo bar');
      expect(searchBar.getFreeTextValue()).to.eventually.equal('foo bar');
    });

    it('should trigger a search if Enter is pressed', () => {
      searchBar.typeFreeText(protractor.Key.ENTER);
      expect(sandboxPage.isSearchTriggered()).to.eventually.be.true;
    });

    it('should trigger a search with the search icon', () => {
      return searchBar.searchIconButton.click().then(() => {
        expect(sandboxPage.isSearchTriggered()).to.eventually.be.true;
      });
    });
  });

  describe('Choosing context', () => {
    it('should allow to select a context with the object picker', () => {
      expect(searchBar.contextButton.isDisplayed()).to.eventually.be.true;
      searchBar.openContextPicker();

      var pickerDialog = new ObjectPickerDialog();
      expect(pickerDialog.isPresent()).to.eventually.be.true;

      var objectPicker = pickerDialog.getObjectPicker();
      objectPicker.waitUntilOpened();
    });

    it('should have no context selected by default', () => {
      shouldHaveContext(searchBar, false);
    });

    it('should render the selected context\'s header', () => {
      searchBar.openContextPicker();
      var pickerDialog = new ObjectPickerDialog();
      var objectPicker = pickerDialog.getObjectPicker();

      var search = objectPicker.getSearch();
      search.getResults().clickResultItem(0);
      pickerDialog.ok();

      shouldHaveContext(searchBar, true);

      var header = new StaticInstanceHeader(searchBar.selectedContext);
      // Should not be clickable in order to avoid unwanted navigation
      expect(header.isClickable()).to.eventually.be.false;
    });

    it('should allow to clear the selected context', () => {
      searchBar.openContextPicker();
      var pickerDialog = new ObjectPickerDialog();
      var objectPicker = pickerDialog.getObjectPicker();

      var search = objectPicker.getSearch();
      search.getResults().clickResultItem(0);
      pickerDialog.ok();

      return searchBar.clearContextButton.isDisplayed().then((isDisplayed) => {
        expect(isDisplayed).to.be.true;
        searchBar.clearContextButton.click();
        shouldHaveContext(searchBar, false);
      });
    });

    it('should allow to assign a context from the outside', () => {
      sandboxPage.assignContext();
      expect(searchBar.selectedContext.isPresent()).to.eventually.be.true;
    });
  });

  describe('Context Menu', () => {
    it('should show context menu options when dropdown is clicked', () => {
      searchBar.openContextMenu();
      expect(searchBar.contextMenuElement.isDisplayed()).to.eventually.be.true;
    });

    it('should be able to select current object from the menu', () => {
      searchBar.openContextMenu().selectContextFromMenu('current_object');
      shouldHaveContext(searchBar, true);
      expect(searchBar.getSelectedContext()).to.eventually.equal('current_object');
      expect(searchBar.selectedContext.getText()).to.eventually.equal('Current object');
    });

    it('should allow to clear the selected current object', () => {
      searchBar.openContextMenu().selectContextFromMenu('current_object');

      return searchBar.clearContextButton.isDisplayed().then((isDisplayed) => {
        expect(isDisplayed).to.be.true;
        searchBar.clearContextButton.click();
        shouldHaveContext(searchBar, false);
      });
    });

    it('should allow to select recently used objects as context', () => {
      sandboxPage.addRecentObject();
      searchBar.openContextMenu();
      expect(searchBar.getContextMenuOptions()).to.eventually.deep.equal(['current_object', '1']);

      searchBar.selectContextFromMenu('1');
      shouldHaveContext(searchBar, true);
      expect(searchBar.getSelectedContext()).to.eventually.equal('1');
    });

    it('should not allow to select recently used objects if there ain\'t any', () => {
      sandboxPage.removeRecentObject();
      expect(searchBar.openContextMenu().getContextMenuOptions()).to.eventually.deep.equal(['current_object']);
    });

    it('should render notification for no recent objects', () => {
      sandboxPage.removeRecentObject();
      expect(searchBar.openContextMenu().getEmptyRecentObjectsMessage().isDisplayed()).to.eventually.be.true;
    });
  });

  function shouldHaveContext(bar, state) {
    // Double checking by class and element
    expect(bar.hasContext()).to.eventually.equal(state);
    expect(bar.selectedContext.isPresent()).to.eventually.equal(state);
    expect(bar.clearContextButton.isPresent()).to.eventually.equal(state);
  }
});
