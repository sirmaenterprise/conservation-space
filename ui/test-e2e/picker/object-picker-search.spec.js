var ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;
var Search = require('../search/components/search');
var Dialog = require('../components/dialog/dialog');

describe('Search in Object picker', () => {

  var page = new ObjectPickerSandbox();
  var search;

  beforeEach(() => {
    page.open();
  });

  describe('When the picker is opened', () => {
    beforeEach(() => {
      page.getEmbeddedPicker().waitUntilOpened();
      search = new Search(Search.COMPONENT_SELECTOR);
      search.waitUntilOpened();
    });

    it('should have "Current object" selected by default', () => {
      var contextElement = search.criteria.contextSelectElement;
      var selectedContext = search.criteria.getSelectedValue(contextElement);
      expect(selectedContext).to.eventually.deep.equal(['current_object']);
    });

    it('should automatically trigger a search when opened', () => {
      search.results.waitUntilOpened();
    });
  });

  describe('When selecting items in a picker dialog', () => {

    var objectPickerDialog;
    beforeEach(() => {
      objectPickerDialog = page.openPickerDialog();
      objectPickerDialog.getObjectPicker().waitUntilOpened();
      var searchSelector = `${Dialog.COMPONENT_SELECTOR} ${Search.COMPONENT_SELECTOR}`;
      search = new Search(searchSelector);
      search.waitUntilOpened();
    });

    it('should not resolve selected items on Cancel', () => {
      search.results.clickResultItem(0);
      objectPickerDialog.cancel();

      var selectionInputValue = page.getSelectionInputValue();
      expect(selectionInputValue).to.eventually.equal('');
    });

    it('should resolve selected items on OK', () => {
      search.results.clickResultItem(0);
      objectPickerDialog.ok();
      objectPickerDialog.waitUntilClosed();

      var selectionInputValue = page.getSelectionInputValue();
      expect(selectionInputValue).to.eventually.contains('"id":"aa873a4d-ccb2-4878-8a68-6be03deb2e7d"');
      expect(selectionInputValue).to.eventually.contains('"title":"Object 0"');
    });

    it('should display the root context if there is a parent', () => {
      var contextValue = search.criteria.getSelectedValue(search.criteria.contextSelectElement);
      //Given the object structure 1 -> 2 -> 3. Object with id = 3 should be the root context of 1 and 2
      expect(contextValue).to.eventually.deep.equal(['3']);
    });
  });

});