'use strict';

var Search = require('../search.js').Search;
var AdvancedSearchStringCriteria = require('../advanced/advanced-search').AdvancedSearchStringCriteria;
var Dialog = require('../../../components/dialog/dialog');
var SandboxPage = require('../../../page-object').SandboxPage;

describe('Saved searches', () => {

  var search;
  var page = new SandboxPage();

  beforeEach(() => {
    page.open('sandbox/search/components/saved/');
    search = new Search($(Search.COMPONENT_SELECTOR));
  });

  describe('When an advanced saved search is loaded', () => {

    let advancedSearch;

    beforeEach(() => {
      let criteria = search.getCriteria();
      criteria.getSearchBar().toggleOptions().openAdvancedSearch();
      advancedSearch = criteria.getAdvancedSearch();
      let savedSearchSelect = advancedSearch.getSavedSearchSelect();
      return savedSearchSelect.selectSavedSearch('savedFilter2');
    });

    it('should render the save search component in the search toolbar', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      expect(saveSearch.titleInput.isDisplayed()).to.eventually.be.true;
      expect(saveSearch.saveButton.isDisplayed()).to.eventually.be.true;
    });

    it('should render saved search select component in the search form', () => {
      var savedSearchSelect = advancedSearch.getSavedSearchSelect();
      expect(savedSearchSelect.openSelectMenuButton.isDisplayed()).to.eventually.be.true;
    });

    it('should render the saved search title in the save search component', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      expect(saveSearch.getTitleValue()).to.eventually.equal('Saved Filter 2');
    });

    it('should not be able to save the search if there is no title', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      return saveSearch.titleInput.clear().then(() => {
        expect(saveSearch.isDisabled()).to.eventually.be.true;
      });
    });

    it('should be able to save the search if there is a title', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      return saveSearch.titleInput.sendKeys('Search title').then(() => {
        expect(saveSearch.isDisabled()).to.eventually.be.false;
      });
    });

    it('should display a confirmation dialog when saving a search and close after saving', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      return saveSearch.save('Search title', false).then(() => {
        var confirmationDialog = new Dialog($('.modal-dialog'));
        expect(confirmationDialog.isPresent()).to.eventually.be.false;
      });
    });

    it('should offer to update it when saving again', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      // Passing true will press the update button.
      saveSearch.save('Search title', true);
      var confirmationDialog = new Dialog($('.modal-dialog'));
      expect(confirmationDialog.isPresent()).to.eventually.be.false;
    });

    it('should display a confirmation dialog if updating a search and close after updating', () => {
      var saveSearch = advancedSearch.getSaveSearchGroup();
      return saveSearch.save('Search title', false).then(() => {
        // This time we will update. Passing true will press the update button.
        saveSearch.save('Search title', true);
        var confirmationDialog = new Dialog($('.modal-dialog'));
        expect(confirmationDialog.isPresent()).to.eventually.be.false;
      });
    });

    it('should switch to the advanced search and render the criteria', () => {
      var section = advancedSearch.getSection(0);
      return section.getObjectTypeSelectValue().then((selectedTypes) => {
        expect(selectedTypes).to.deep.equal(['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document']);
        return section.getCriteriaRowForGroup(1, 0, 0);
      }).then((criteriaRow) => {
        var searchCriteria = new AdvancedSearchStringCriteria(criteriaRow.valueColumn);
        return searchCriteria.getValue();
      }).then((stringValue) => {
        expect(stringValue).to.deep.equal(['My presentation']);
      });
    });
  });
});