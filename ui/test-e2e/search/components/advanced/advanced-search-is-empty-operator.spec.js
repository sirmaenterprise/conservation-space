var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchBooleanCriteria = require('./advanced-search.js').AdvancedSearchBooleanCriteria;

describe('AdvancedSearchIsEmptyOperator', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
  });

  describe('When "Is empty" is selected as operator for a string property', () => {
    it('should display a select component for true/false', () => {
      return testIsEmptyOperatorForProperty(advancedSearchSection, 'title');
    });
  });

  describe('When "Is empty" is selected as operator for a code list property', () => {
    it('should display a select component for true/false', () => {
      return testIsEmptyOperatorForProperty(advancedSearchSection, 'status');
    });
  });

  describe('When "Is empty" is selected as operator for a date/time property', () => {
    it('should display a select component for true/false', () => {
      return testIsEmptyOperatorForProperty(advancedSearchSection, 'createdOn');
    });
  });

  describe('When "Is empty" is selected as operator for a numeric property', () => {
    it('should display a select component for true/false', () => {
      return testIsEmptyOperatorForProperty(advancedSearchSection, 'numeric');
    });
  });

  describe('When "Is empty" is selected as operator for an object(relation) property', () => {
    it('should display a select component for true/false', () => {
      return testIsEmptyOperatorForProperty(advancedSearchSection, 'hasChild');
    });
  });

  function testIsEmptyOperatorForProperty(searchSection, property) {
    return searchSection.selectPropertyAndOperator(property, 'empty').then((row) => {
      var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
      return expect(booleanCriteria.isDisplayed()).to.eventually.be.true;
    });
  }

});
