var SearchResultsSandboxPage = require('./search-results').SearchResultsSandboxPage;
var DropdownMenu = require('../../../components/dropdownmenu/dropdown-menu').DropdownMenu;

describe('Search results form', () => {

  var searchResults;
  var page = new SearchResultsSandboxPage();

  beforeEach(()=> {
    page.open();
  });

  describe('When the form is configured for no selection', () => {
    it('Then there are no selectable options', () => {
      searchResults = page.getFormWithoutSelection();
      searchResults.getResultsInputs().then(function (items) {
        expect(items.length).to.equal(0);
      });
    });
  });

  describe('When the form is configured for single selection', () => {

    beforeEach(()=> {
      searchResults = page.getFormWithSingleSelection();
    });

    it('Then there are selectable radio buttons', () => {
      searchResults.getResultsInputs().then(function (items) {
        expect(items.length).to.equal(3);
        items.forEach((item)=> {
          expect(item.getAttribute('type')).to.eventually.equal('radio');
        });
      });
    });

    it('Then only one item can be selected', () => {
      searchResults.clickResultItem(0);
      searchResults.clickResultItem(1);
      searchResults.clickResultItem(2);

      searchResults.getResultsInputs().then(function (items) {
        expect(items[0].getAttribute('checked')).to.eventually.be.null;
        expect(items[1].getAttribute('checked')).to.eventually.be.null;
        expect(items[2].getAttribute('checked')).to.eventually.equal('true');
      });
    });
  });

  describe('When the form is configured for multiple selection', () => {

    beforeEach(()=> {
      searchResults = page.getFormWithMultiSelection();
    });

    it('Then there are selectable checkbox buttons', () => {
      searchResults.getResultsInputs().then(function (items) {
        expect(items.length).to.equal(3);
        items.forEach((item)=> {
          expect(item.getAttribute('type')).to.eventually.equal('checkbox');
        });
      });
    });

    it('Then more than one item can be selected', () => {
      searchResults.clickResultItem(2);

      searchResults.getResultsInputs().then(function (items) {
        expect(items[0].getAttribute('checked')).to.eventually.equal('true');
        expect(items[1].getAttribute('checked')).to.eventually.equal('true');
        expect(items[2].getAttribute('checked')).to.eventually.equal('true');
      });
    });

    it('Then deselecting one item should not deselect more than one', ()=> {
      // Selecting third item
      searchResults.clickResultItem(2);
      // Deselecting second item
      searchResults.clickResultItem(1);

      return searchResults.getResultsInputs().then(function (items) {
        expect(items[0].getAttribute('checked')).to.eventually.equal('true');
        expect(items[1].getAttribute('checked')).to.eventually.be.null;
        expect(items[2].getAttribute('checked')).to.eventually.equal('true');
      });
    });
  });

  describe('When the results are configured with actions', () => {
    it('Then the action menu should be visible by clicking the trigger button', () => {
      searchResults = page.getFormWithActions();

      searchResults.getResults().then((results) => {
        var actions = results[0].element.$('.actions-menu');
        var dropdownMenu = new DropdownMenu(actions);
        expect(dropdownMenu.getTriggerButton().isDisplayed()).to.eventually.be.true;
        dropdownMenu.open();
        expect(dropdownMenu.getActionContainer().isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When the results have disabled items', () => {
    it('should not allow selecting the disabled items', () => {
      searchResults = page.getFormWithMultiSelectionAndDisabledItems();

      searchResults.clickResultItem(1);
      return searchResults.getResultsInputs().then(function (items) {
        expect(items[0].getAttribute('checked')).to.eventually.be.null;
      });
    });
  });
});