var ExternalSearchSandboxPage = require('./external-search').ExternalSearchSandboxPage;
var ExternalSearchResults = require('./external-search').ExternalSearchResults;
var AdvancedSearchSection = require('../advanced/advanced-search').AdvancedSearchSection;
var AdvancedSearchStringCriteria = require('../advanced/advanced-search.js').AdvancedSearchStringCriteria;
var OrderToolbar = require('../common/order-toolbar').OrderToolbar;
var Pagination = require('../common/pagination');
var Dialog = require('../../../components/dialog/dialog');

describe('ExternalSearch', () => {

  var externalSearch;
  beforeEach(() => {
    var page = new ExternalSearchSandboxPage();
    page.openSandbox();
    externalSearch = page.getSearch().getCriteria().getExternalSearch()
  });

  describe('When there are available external systems', () => {
    it('should display them in a drop down menu', () => {
      expect(externalSearch.getAvailableSystems()).to.eventually.deep.equal(['CMS', 'DAM']);
      expect(externalSearch.getSelectedSystem()).to.eventually.equal('CMS');
    });
  });

  describe('When the external search is loaded (for CMS by default)', () => {

    var searchSection;
    beforeEach(() => {
      searchSection = new AdvancedSearchSection(externalSearch.searchCriteriaElement);
    });

    it('should load the models types for the related system', () => {
      expect(searchSection.getObjectTypeSelectValue()).to.eventually.deep.equal(['culturalObject']);
    });

    it('should render a default criteria form for the related system and model type with restricted fields', () => {
      searchSection.getCriteriaRowsForGroup(1, 0).then((criteriaRows) => {
        expect(criteriaRows.length).to.equal(3);
        assertCriteriaRow(criteriaRows[0], 'objectNumber', 'equals');
        assertCriteriaRow(criteriaRows[1], 'objectId', 'contains');
        assertCriteriaRow(criteriaRows[2], 'title', 'contains');
      });
    });

    it('should load the related order by properties for the system', () => {
      assertOrderBy('Object ID', ['Title', 'Object ID']);
    });
  });

  describe('When the system is changed (loading DAM)', () => {
    var searchSection;
    beforeEach(() => {
      externalSearch.changeSystem('DAM');
      searchSection = new AdvancedSearchSection(externalSearch.searchCriteriaElement);
    });

    it('should load the related models types in the criteria', () => {
      expect(searchSection.getObjectTypeSelectValue()).to.eventually.deep.equal(['image']);
    });

    it('should render another default criteria form with restricted fields related to the system and model type', () => {
      searchSection.getCriteriaRowsForGroup(1, 0).then((criteriaRows) => {
        expect(criteriaRows.length).to.equal(4);
        assertCriteriaRow(criteriaRows[0], 'objectNumber', 'equals');
        assertCriteriaRow(criteriaRows[1], 'objectId', 'contains');
        assertCriteriaRow(criteriaRows[2], 'title', 'contains');
        assertCriteriaRow(criteriaRows[3], 'imageId', 'contains');
      });
    });

    it('should load the related order by properties in the search toolbar', () => {
      assertOrderBy('Artist', ['Artist', 'Image ID']);
    });
  });

  describe('When a search is executed', () => {

    var externalSearchResults;
    beforeEach(() => {
      externalSearch.search();
      externalSearchResults = new ExternalSearchResults(externalSearch.externalResults);
      externalSearchResults.waitForResults();
    });

    it('should allow to select and deselect all results', () => {
      externalSearchResults.selectAll();
      expect(externalSearchResults.areAllResultsSelected()).to.eventually.be.true;
      externalSearchResults.deselectAll();
      expect(externalSearchResults.areAllResultsDeselected()).to.eventually.be.true;
    });

    it('should allow to import selected results', () => {
      return externalSearchResults.selectAll().then(() => {
        expect(externalSearchResults.canImportOrUpdate()).to.eventually.be.true;
      });
    });

    it('should not allow to import if there are no selected results', () => {
      expect(externalSearchResults.canImportOrUpdate()).to.eventually.be.false;
    });
  });

  describe('When there are selected results', () => {
    beforeEach(() => {
      externalSearch.search();
      var externalSearchResults = new ExternalSearchResults(externalSearch.externalResults);
      externalSearchResults.waitForResults();
      externalSearchResults.selectAll();
    });

    it('should display information dialog when changing pages', () => {
      var pagination = new Pagination($(Pagination.COMPONENT_SELECTOR));
      pagination.goToPage(2);
      new Dialog($(Dialog.COMPONENT_SELECTOR)).waitUntilOpened();
    });

    it('should display information dialog when changing the order by', () => {
      var searchToolbar = new OrderToolbar($(OrderToolbar.COMPONENT_SELECTOR));
      searchToolbar.waitUntilOpened();
      searchToolbar.toggleOrderDirection();
      new Dialog($(Dialog.COMPONENT_SELECTOR)).waitUntilOpened();
    });
  });

  describe('When the search is cleared', () => {
    it('should remove any entered values', () => {
      var searchSection = new AdvancedSearchSection(externalSearch.searchCriteriaElement);
      return searchSection.getCriteriaRowForGroup().then((row) => {
        return new AdvancedSearchStringCriteria(row.valueColumn).enterValue('test');
      }).then(() => {
        return externalSearch.clear();
      }).then(() => {
        externalSearch.waitUntilOpened();
        searchSection = new AdvancedSearchSection(externalSearch.searchCriteriaElement);
        return searchSection.getCriteriaRowForGroup().then((row) => {
          var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn);
          expect(stringCriteria.getValue()).to.eventually.deep.equal([]);
        });
      });
    });
  });

  function assertCriteriaRow(row, field, operator) {
    expect(row.getSelectedPropertyValue()).to.eventually.equal(field);
    expect(row.getOperatorSelectValues()).to.eventually.deep.equal([operator]);
  }

  function assertOrderBy(defaultValue, availableValues) {
    var orderToolbar = new OrderToolbar($(OrderToolbar.COMPONENT_SELECTOR));

    orderToolbar.waitUntilOpened();
    expect(orderToolbar.getOrderByOption()).to.eventually.equal(defaultValue);
    orderToolbar.getOrderByOptions().then((options) => {
      expect(options).to.deep.eq(availableValues);
    });
  }
});