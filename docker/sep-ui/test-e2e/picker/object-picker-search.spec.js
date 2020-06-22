'use strict';

let ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;

describe('Search in Object picker', () => {

  let page = new ObjectPickerSandbox();
  let search;

  beforeEach(() => {
    page.open();
  });

  describe('When the picker is opened', () => {
    beforeEach(() => {
      let embeddedPicker = page.getEmbeddedPicker();
      search = embeddedPicker.getSearch();
    });

    it('should have "Current object" selected by default', () => {
      expect(search.getCriteria().getSearchBar().getSelectedContext()).to.eventually.equal('current_object');
    });

    it('should automatically trigger a search when opened', () => {
      search.getResults().waitForResults();
    });
  });

  describe('When selecting items in a picker dialog', () => {

    let objectPickerDialog;
    beforeEach(() => {
      objectPickerDialog = page.openPickerDialog();
      search = objectPickerDialog.getObjectPicker().getSearch();
    });

    it('should not resolve selected items on Cancel', () => {
      search.getResults().clickResultItem(0);
      objectPickerDialog.cancel();

      let selectionInputValue = page.getSelectionInputValue();
      expect(selectionInputValue).to.eventually.equal('');
    });

    it('should resolve selected items on OK', () => {
      search.getResults().clickResultItem(0);
      objectPickerDialog.ok();
      objectPickerDialog.waitUntilClosed();

      let selectionInputValue = page.getSelectionInputValue();
      expect(selectionInputValue).to.eventually.contains('"id":"aa873a4d-ccb2-4878-8a68-6be03deb2e7d"');
      expect(selectionInputValue).to.eventually.contains('"title":"Object 0"');
    });

    it('should display the root context if there is a parent', () => {
      expect(search.getCriteria().getSearchBar().getSelectedContext()).to.eventually.equal('1');
    });
  });

});