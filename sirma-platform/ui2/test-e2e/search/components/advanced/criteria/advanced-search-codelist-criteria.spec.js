var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchCodelistCriteria = require('../advanced-search.js').AdvancedSearchCodelistCriteria;

describe('AdvancedSearchCodelistCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When a code list property is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('status').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.eq(['in', 'not_in', 'empty']);
        });
      });
    });

    it('should display a select component with available code values', () => {
      return advancedSearchSection.selectPropertyAndOperator('status', 'in').then((row) => {
        var codelistCriteria = new AdvancedSearchCodelistCriteria(row.valueColumn);
        return expect(codelistCriteria.isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When a code value is selected', () => {
    it('should be visible in the selection', () => {
      return advancedSearchSection.selectPropertyAndOperator('status', 'in').then((row) => {
        var codelistCriteria = new AdvancedSearchCodelistCriteria(row.valueColumn);
        return codelistCriteria.selectCodeValue('APPROVED').then(() => {
          return codelistCriteria.selectCodeValue('DELETED');
        }).then(() => {
          return codelistCriteria.getSelectedValues();
        });
      }).then((selectedValues) => {
        expect(selectedValues).to.deep.equal(['APPROVED', 'DELETED']);
      });
    });

    it('should update the tree model', () => {
      return advancedSearchSection.selectPropertyAndOperator('status', 'in').then((row) => {
        var codelistCriteria = new AdvancedSearchCodelistCriteria(row.valueColumn);
        return codelistCriteria.selectCodeValue('APPROVED');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.deep.equal(['APPROVED']);
      });
    });
  });

  // Predefined criteria tests are in the main spec

  describe('When the advanced search form is disabled', () => {
    it('should render the code list select as disabled', () => {
      return advancedSearchSection.selectPropertyAndOperator('status', 'in').then((row) => {
        return page.toggleEnabledState().then(() => {
          var codelistCriteria = new AdvancedSearchCodelistCriteria(row.valueColumn);
          return expect(codelistCriteria.isDisabled()).to.eventually.be.true;
        });
      });
    });
  });

});