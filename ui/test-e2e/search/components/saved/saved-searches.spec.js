'use strict';

var Search = require('../search.js');
var AdvancedSearch = require('../advanced/advanced-search').AdvancedSearch;
var AdvancedSearchStringCriteria = require('../advanced/advanced-search').AdvancedSearchStringCriteria;
var SearchToolbar = require('../common/search-toolbar').SearchToolbar;
var Dialog = require('../../../components/dialog/dialog');
var SandboxPage = require('../../../page-object').SandboxPage;

describe('Saved searches', () => {

  var searchWrapper;
  var page = new SandboxPage();

  beforeEach(() => {
    page.open('sandbox/search/components/saved/');
    browser.wait(EC.visibilityOf($('.btn.seip-search')), DEFAULT_TIMEOUT);
    searchWrapper = new Search(Search.COMPONENT_SELECTOR);
    searchWrapper.waitUntilOpened();
  });

  describe('When basic search form is opened', () => {
    it('should render the save search component in the search toolbar', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      expect(saveSearch.titleInput.isDisplayed()).to.eventually.be.true;
      expect(saveSearch.saveButton.isDisplayed()).to.eventually.be.true;
    });

    it('should render the load saved search select component in the search form', () => {
      var savedSearchSelect = searchWrapper.criteria.getSavedSearchSelect();
      expect(savedSearchSelect.openSelectMenuButton.isDisplayed()).to.eventually.be.true;
    });

    it('should not be able to save the search if there is no title', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      return expect(saveSearch.isDisabled()).to.eventually.be.true;
    });

    it('should be able to save the search if there is a title', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      return saveSearch.titleInput.sendKeys('Search title').then(() => {
        return expect(saveSearch.isDisabled()).to.eventually.be.false;
      });
    });

    it('should display a confirmation dialog when saving a search and close after saving', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      return saveSearch.save('Search title', false).then(() => {
        var confirmationDialog = new Dialog($('.modal-dialog'));
        expect(confirmationDialog.isPresent()).to.eventually.be.false;
      });
    });

    it('should display a confirmation dialog if updating a search and close after updating', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      return saveSearch.save('Search title', false).then(() => {
        // This time we will update. Passing true will press the update button.
        return saveSearch.save('Search title', true).then(() => {
          var confirmationDialog = new Dialog($('.modal-dialog'));
          expect(confirmationDialog.isPresent()).to.eventually.be.false;
        });
      });
    });
  });

  describe('When a basic saved search is loaded', () => {
    beforeEach(() => {
      var savedSearchSelect = searchWrapper.criteria.getSavedSearchSelect();
      return savedSearchSelect.selectSavedSearch('savedFilter1');
    });

    it('should render the saved search criteria', () => {
      var basicCriteria = searchWrapper.criteria;
      var expectedTypes = [
        'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document',
        'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book'
      ];
      var typesSelect = basicCriteria.typesSelectElement;
      return Promise.all([
        expect(basicCriteria.getFreeTextValue()).to.eventually.equal('My presentation'),
        expect(basicCriteria.getSelectedValue(typesSelect)).to.eventually.deep.equal(expectedTypes)
      ]);
    });

    it('should restore the toolbar criteria', () => {
      var toolbar = searchWrapper.toolbar;
      return Promise.all([
        expect(toolbar.getOrderByValue()).to.eventually.equal('dcterms:title'),
        expect(toolbar.getSortByValue()).to.eventually.equal(SearchToolbar.ASCENDING)
      ]);
    });

    it('should render the saved search title in the save search component', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      return expect(saveSearch.getTitleValue()).to.eventually.equal('Saved Filter 1');
    });

    it('should offer to update it when saving again', () => {
      var saveSearch = searchWrapper.toolbar.getSaveSearchGroup();
      // Passing true will press the update button.
      return saveSearch.save('Search title', true).then(() => {
        var confirmationDialog = new Dialog($('.modal-dialog'));
        expect(confirmationDialog.isPresent()).to.eventually.be.false;
      });
    });
  });

  describe('When an advanced saved search is loaded', () => {
    beforeEach(() => {
      var savedSearchSelect = searchWrapper.criteria.getSavedSearchSelect();
      return savedSearchSelect.selectSavedSearch('savedFilter2');
    });

    it('should switch to the advanced search and render the criteria', () => {
      var advancedSearch = new AdvancedSearch($(AdvancedSearch.COMPONENT_SELECTOR));
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