var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;

describe('AdvancedSearch Rules', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When new rule is added', () => {
    it('should add a new criteria row with property & operator selects', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addRuleButton.click();
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((row)=> {
        expect(row).to.exist;
        return row.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('title');
          return row.getSelectedOperatorValue().then((selectedOperator) => {
            expect(selectedOperator).to.equal('contains');
          });
        });
      }).then(()=> {
        return page.getTreeModel();
      }).then((treeModel) => {
        var newCriteria = treeModel.rules[0].rules[1].rules[1];
        expect(newCriteria).to.exist;
        expect(newCriteria.field).to.equal('title');
        expect(newCriteria.type).to.equal('string');
        expect(newCriteria.operator).to.equal('contains');
      });
    });
  });

  describe('When a rule is removed', () => {
    it('should not be able to remove the first row', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.removeButton.click();
        return advancedSearchSection.getCriteriaRowsForGroup(1, 0);
      }).then((rows) => {
        expect(rows.length).to.equal(1);
      });
    });

    it('should be able to remove any after the first one', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addRuleButton.click();
        return advancedSearchSection.getCriteriaRowsForGroup(1, 0);
      }).then((rows) => {
        expect(rows.length).to.equal(2);
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((row)=> {
        expect(row).to.exist;
        row.removeButton.click();
        return advancedSearchSection.getCriteriaRowsForGroup(1, 0);
      }).then((rows) => {
        expect(rows.length).to.equal(1);
      });
    });

    it('should no longer be present in the model', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addRuleButton.click();
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((row)=> {
        expect(row).to.exist;
        row.removeButton.click();
        return page.getTreeModel();
      }).then((treeModel) => {
        var removedCriteria = treeModel.rules[0].rules[1].rules[1];
        expect(removedCriteria).to.not.exist;
      });
    });
  });
});