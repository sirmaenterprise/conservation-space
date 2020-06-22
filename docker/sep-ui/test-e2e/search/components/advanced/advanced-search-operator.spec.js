var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchBooleanCriteria = require('./advanced-search.js').AdvancedSearchBooleanCriteria;

describe('AdvancedSearch Operators', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
  });

  describe('When operator is changed', () => {
    it('should change the operator value in the tree model', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        // Change the property to load test operators
        return row.changeProperty('test-property').then(() => {
          row.waitForOperatorSelectToRender();
          // Change to another operator that is not the first
          return row.changeOperator('test-operator2');
        });
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var operator = treeModel.rules[0].rules[1].rules[0].operator;
        expect(operator).to.equal('test-operator2');
      });
    });
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