var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchBooleanCriteria = require('../advanced-search.js').AdvancedSearchBooleanCriteria;

describe('AdvancedSearchBooleanCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When a boolean property is selected', () => {
    it('should display a select component with boolean values', () => {
      return advancedSearchSection.selectPropertyAndOperator('checkbox', 'is').then((row) => {
        var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
        return expect(booleanCriteria.isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When a boolean value is selected', () => {
    it('should be visible in the selection', () => {
      return advancedSearchSection.selectPropertyAndOperator('checkbox', 'is').then((row) => {
        var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
        return booleanCriteria.selectBooleanValue('false').then(() => {
          return booleanCriteria.getSelectedValue();
        });
      }).then((selectedValue) => {
        expect(selectedValue).to.equal('false');
      });
    });

    it('should update the tree model', () => {
      return advancedSearchSection.selectPropertyAndOperator('checkbox', 'is_not').then((row) => {
        var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
        return booleanCriteria.selectBooleanValue('true');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var operator = treeModel.rules[0].rules[1].rules[0].operator;
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(operator).to.equal('is_not');
        expect(value).to.equal('true');
      });
    });
  });

  describe('When the advanced search form is disabled', () => {
    it('should render the boolean select as disabled', () => {
      return advancedSearchSection.selectPropertyAndOperator('checkbox', 'is').then((row) => {
        return page.toggleEnabledState().then(() => {
          var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
          return expect(booleanCriteria.isDisabled()).to.eventually.be.true;
        });
      });
    });
  });

});