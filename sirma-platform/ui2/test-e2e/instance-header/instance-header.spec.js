'use strict';

let InstanceHeaderSandboxPage = require('./instance-header').InstanceHeaderSandboxPage;
let ObjectPickerDialog = require('../picker/object-picker').ObjectPickerDialog;
let Search = require('../search/components/search.js').Search;

describe('InstanceHeader', function () {

  let instanceHeaderPage = new InstanceHeaderSandboxPage();

  beforeEach(function () {
    instanceHeaderPage.open();
  });

  it('should show iDoc header when it is loaded', function () {
    instanceHeaderPage.loadHeader();
    let header = instanceHeaderPage.getIconHeader();
    expect(header.getHeaderLinkText()).to.eventually.equal('(Common document) Offer (България)\nHeader-3');
  });

  it('should apply text property changes in header', function () {
    instanceHeaderPage.changeTitle('Offer from John Doe');
    let header = instanceHeaderPage.getIconHeader();
    expect(header.getHeaderLinkText()).to.eventually.equal('(Common document) Offer from John Doe (България)\nHeader-3');
  });

  it('should apply date property changes in header', function () {
    instanceHeaderPage.changeDueDate();
    let header = instanceHeaderPage.getIconHeader();
    expect(header.getDueDate()).to.eventually.equal('Nov/25/2017');
  });

  it('should apply codelist property changes in header', function () {
    instanceHeaderPage.changeCountry('Австралия');
    let header = instanceHeaderPage.getIconHeader();
    expect(header.getCountry()).to.eventually.equal('Австралия');
  });

  it.skip('should apply object property changes in header', function () {
    // Given I have instance with header where references object property is bound.
    // And There are 6 instances referenced.
    // And The initial objects count configuration is set to 3.
    // When I open the instance.
    let header = instanceHeaderPage.getIconHeader();
    let headerReferencesField = header.getReferences();
    // Then I expect in the header, references field to be visible.
    // And There are should be 3 visible and 3 hidden selected items.
    expect(headerReferencesField.getSelectedObjectsCount()).to.eventually.equal(3);
    expect(headerReferencesField.getHiddenObjectsCount()).to.eventually.equal('2');
    // When I remove 1 item from the references object property.
    let referencesField = instanceHeaderPage.getReferencesField();
    referencesField.removeInstance(0);
    // Then I expect items in bound references field to become 5: 3 visible and 2 hidden.
    expect(headerReferencesField.getSelectedObjectsCount()).to.eventually.equal(3);
    expect(headerReferencesField.getHiddenObjectsCount()).to.eventually.equal('1');
    // When I select 2 more items in references object property.
    toggleObjectSelection(referencesField, [0, 5]);
    // Then I expect items in bound references field to become 6: 3 visible and 6 hidden.
    expect(headerReferencesField.getSelectedObjectsCount()).to.eventually.equal(3);
    expect(headerReferencesField.getHiddenObjectsCount()).to.eventually.equal('3');
    // When I remove all selected items from the references object property.
    referencesField.removeInstance(0);
    referencesField.removeInstance(0);
    referencesField.removeInstance(0);
    referencesField.removeInstance(0);
    referencesField.removeInstance(0);
    referencesField.removeInstance(0);
    // Then I expect references field in the header to become empty.
    expect(headerReferencesField.getSelectedObjectsCount()).to.eventually.equal(0);
  });

  it('should not allow script injection', function () {
    instanceHeaderPage.changeTitle(`<script>javascript:document.getElementById('injectionTarget').innerHTML = 'Injected text';</script>`);
    expect(element(by.id('injectionTarget')).getText()).to.eventually.be.empty;
  });

  describe('Dates', ()=> {
    it('should format dates according to its data-format attribute', ()=> {
      instanceHeaderPage.loadHeader();
      // dueDate has its data-format property stored in an inner span
      let dueDate = instanceHeaderPage.getIconHeader().getField('dueDate');
      // createdOn has its data-format property stored in its element
      let createdOn = instanceHeaderPage.getIconHeader().getField('createdOn');

      expect(dueDate.getAttribute('data-format')).to.eventually.equal('MMM/DD/YYYY');
      expect(dueDate.getText()).to.eventually.equal('Dec/22/2015');
      expect(createdOn.getAttribute('data-format')).to.eventually.equal('MM.DD.YYYY');
      expect(createdOn.getText()).to.eventually.equal('12.22.2015');
    });
  });

});

function toggleObjectSelection(objectControl, indexes) {
  objectControl.selectInstance();
  let objectPickerDialog = new ObjectPickerDialog();
  let search = new Search($(Search.COMPONENT_SELECTOR));
  search.getCriteria().getSearchBar().search();
  let results = search.getResults();
  // zero based, so item 2 is the third element in result list
  indexes.forEach((ind) => {
    results.clickResultItem(ind);
  });
  objectPickerDialog.ok();
  objectPickerDialog.waitUntilClosed();
}
