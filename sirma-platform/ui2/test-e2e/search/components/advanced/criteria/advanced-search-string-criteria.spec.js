var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchStringCriteria = require('../advanced-search.js').AdvancedSearchStringCriteria;

describe('AdvancedSearchStringCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When a string property is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('title').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.eq(['contains', 'does_not_contain', 'equals', 'does_not_equal', 'starts_with',
            'does_not_start_with', 'ends_with', 'does_not_end_with', 'empty']);
        });
      });
    });

    it('should display a select component for tagging', () => {
      return advancedSearchSection.selectPropertyAndOperator('title', 'contains').then((row) => {
        var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn);
        return expect(stringCriteria.getMenu().isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When a tag is entered', () => {
    it('should update the model', () => {
      return advancedSearchSection.selectPropertyAndOperator('title', 'contains').then((row) => {
        var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn);
        return stringCriteria.enterValue('test-value');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.deep.equal(['test-value']);
      });
    });
  });

  describe('When the form is disabled', () => {
    it('should render the select as disabled', () => {
      // Disable the form
      return page.toggleEnabledState().then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
          var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn);
          return expect(stringCriteria.isDisabled()).to.eventually.be.true;
        });
      });
    });

    it('should render the input field as disabled', () => {
      return advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document").then(() => {
        return advancedSearchSection.selectProperty('single-valued-property').then((row) => {
          var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn, true);
          // Disable the form
          return page.toggleEnabledState().then(() => {
            expect(stringCriteria.isDisabled()).to.eventually.be.true;
          });
        });
      });
    });
  });

  describe('When the property is single valued', () => {

    beforeEach(() => {
      return advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document")
    });

    it('should render an input field instead of a select component', () => {
      return advancedSearchSection.selectProperty('single-valued-property').then((row) => {
        var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn, true);
        expect(stringCriteria.inputFieldWrapper.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should update the criteria model when the input value is changed', () => {
      var testValue = 'A single valued string property';
      return advancedSearchSection.selectProperty('single-valued-property').then((row) => {
        var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn, true);
        return stringCriteria.enterValue(testValue);
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.equal(testValue);
      });
    });
  });
});