var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchObjectTypeCriteria = require('../advanced-search.js').AdvancedSearchObjectTypeCriteria;

describe('AdvancedSearchObjectTypeCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When a object type property is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('objectType').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.eq(['in']);
        });
      });
    });

    it('should display a select component for selecting an object type', () => {
      return advancedSearchSection.selectPropertyAndOperator('objectType', 'in').then((row) => {
        var criteria = new AdvancedSearchObjectTypeCriteria(row.valueColumn);
        return expect(criteria.codeValueSelect.isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When type is selected entered', () => {
    it('should update the model', () => {
      return advancedSearchSection.selectPropertyAndOperator('objectType', 'in').then((row) => {
        var criteria = new AdvancedSearchObjectTypeCriteria(row.valueColumn);
        return criteria.selectOption('Book');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.deep.equal('EO007005');
      });
    });
  });

  describe('When the form is disabled', () => {
    it('should render the select as disabled', () => {
      return advancedSearchSection.selectPropertyAndOperator('objectType', 'in').then((row) => {
        // Disable the form
        return page.toggleEnabledState()
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0);
      }).then((row) => {
        var criteria = new AdvancedSearchObjectTypeCriteria(row.valueColumn);
        return expect(criteria.isDisabled()).to.eventually.be.true;
      });
    });
  });
});