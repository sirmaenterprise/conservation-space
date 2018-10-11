var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;

describe('AdvancedSearch Groups', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When new group is added', () => {
    it('should insert a new criteria group with criteria controls with AND condition', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addGroupButton.click();
        return advancedSearchSection.getCriteriaControlsForGroup(2, 0);
      }).then((controls)=> {
        expect(controls).to.exist;
        return page.getTreeModel();
      }).then((treeModel) => {
        var defaultConditionCriteria = treeModel.rules[0].rules[1].rules[1];
        expect(defaultConditionCriteria).to.exist;
        expect(defaultConditionCriteria.rules).to.exist;
        expect(defaultConditionCriteria.condition).to.equal('AND');
      });
    });

    it('should insert a new criteria row in the new group by default', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addGroupButton.click();
        return advancedSearchSection.getCriteriaRowForGroup(2, 0, 0);
      }).then((row)=> {
        expect(row).to.exist;
        return row.getSelectedPropertyValue();
      }).then((selectedProperty)=> {
        expect(selectedProperty).to.equal('title');
        return page.getTreeModel();
      }).then((treeModel) => {
        var defaultCriteria = treeModel.rules[0].rules[1].rules[1].rules[0];
        expect(defaultCriteria).to.exist;
      });
    });
  });

  it('should not be able to add more nested groups than configured to do so', () => {
    return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
      controls.addGroupButton.click();
      return advancedSearchSection.getCriteriaControlsForGroup(2, 0);
    }).then((controls)=> {
      controls.addGroupButton.click();
      return advancedSearchSection.getCriteriaControlsForGroup(3, 0)
    }).then((controls)=> {
      return controls.addGroupButton.getAttribute('disabled');
    }).then((disabled)=> {
      expect(disabled).to.equal('true');
    });
  });

  describe('When a group is removed', () => {
    it('should no longer be visible in the tree', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        // Adding a group to be removed later
        controls.addGroupButton.click();
        return advancedSearchSection.getCriteriaControlsForGroup(2, 0);
      }).then((controls)=> {
        controls.removeGroupButton.click();
        return advancedSearchSection.getCriteriaControlsForGroup(2, 0);
      }).then((controls)=> {
        expect(controls).to.not.exist;
      });
    });

    it('should not be present in the tree model', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        // Adding a group to be removed later
        controls.addGroupButton.click();
        return advancedSearchSection.getCriteriaControlsForGroup(2, 0);
      }).then((controls)=> {
        controls.removeGroupButton.click();
        return page.getTreeModel();
      }).then((treeModel) => {
        var removedGroup = treeModel.rules[0].rules[1].rules[1];
        expect(removedGroup).to.not.exist;
      });
    });
  });

  it('should not be able to remove the root group in a search section', () => {
    return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
      return controls.removeGroupButton.getAttribute('disabled');
    }).then((disabled)=> {
      expect(disabled).to.equal('true');
    });
  });
});