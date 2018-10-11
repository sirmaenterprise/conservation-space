var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchStringCriteria = require('./advanced-search.js').AdvancedSearchStringCriteria;

describe('AdvancedSearch', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When pressing the clear button', () => {
    it('should clear any present criteria', () => {
      // Change to some other criteria
      page.changeCriteriaSet('predefined');
      advancedSearch.clear();

      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.getSelectedPropertyValue();
      }).then((selectedProperty) => {
        // Should not be the description property
        expect(selectedProperty).to.equal('title');
        return page.getTreeModel();
      }).then((treeModel) => {
        // the root group should have only one rule
        expect(treeModel.rules[0].rules[1].rules.length).to.equal(1);
        // the only rule should be the title
        var property = treeModel.rules[0].rules[1].rules[0].field;
        expect(property).to.equal('title');
      });
    });
  });

  describe('When object type is changed', () => {
    it('should recreate the search section with a default criteria', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addRuleButton.click();
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((row)=> {
        return row.changeProperty('test-property');
      }).then(()=> {
        return advancedSearchSection.addObjectType('OT210027');
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0);
      }).then((firstRow) => {
        return firstRow.getSelectedPropertyValue();
      }).then((selectedProperty) => {
        expect(selectedProperty).to.equal('title');
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((secondRow) => {
        expect(secondRow).to.not.exist;
      });
    });

    it('should change the object type value in the model', () => {
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.addRuleButton.click();
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((row)=> {
        return row.changeProperty('test-property');
      }).then(()=> {
        return advancedSearchSection.addObjectType('OT210027');
      }).then(() => {
        return advancedSearchSection.removeObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var objectType = treeModel.rules[0].rules[0].value;
        expect(objectType).to.deep.equal(['OT210027']);
        var operator = treeModel.rules[0].rules[0].operator;
        expect(operator).to.equal('equals');
      });
    });

    it('should fetch the object type specific properties', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.getPropertySelectValues();
      }).then((availableProperties) => {
        expect(availableProperties).to.contains('test-property');
        return advancedSearchSection.addObjectType('OT210027');
      }).then(() => {
        return advancedSearchSection.removeObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0);
      }).then((newRow) => {
        return newRow.getPropertySelectValues();
      }).then((availableProperties) => {
        expect(availableProperties).to.not.contains('test-property');
      });
    });

    it('should automatically remove Any object', () => {
      // Resetting the criteria, because Document is selected in beforeEach()
      return advancedSearch.clear().then(() => {
        return advancedSearchSection.getObjectTypeSelectValue();
      }).then((selectedObjectTypes) => {
        // Default type
        expect(selectedObjectTypes).to.deep.equal(['anyObject']);
        return advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document").then(() => {
          return advancedSearchSection.getObjectTypeSelectValue();
        });
      }).then((selectedObjectTypes) => {
        expect(selectedObjectTypes).to.deep.equal(['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document']);
      });
    });

    it('should not be able to remove Any object if it is the only selected object type', () => {
      // Resetting the criteria, because Document is selected in beforeEach()
      return advancedSearch.clear().then(() => {
        return advancedSearchSection.removeObjectType('anyObject');
      }).then(() => {
        return advancedSearchSection.getObjectTypeSelectValue();
      }).then((selectedObjectTypes) => {
        expect(selectedObjectTypes).to.deep.equal(['anyObject']);
      });
    });

    it('should reset all types to Any Object if Any Object is selected', () => {
      // Reset the types field to any object if any object is selected again
      return advancedSearch.clear().then(() => {
        advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
        advancedSearchSection.addObjectType("anyObject");
        return advancedSearchSection.getObjectTypeSelectValue();
      }).then((selectedObjectTypes) => {
        expect(selectedObjectTypes).to.deep.equal(['anyObject']);
      });
    });
  });

  describe('When the form is configured to be locked', () => {

    beforeEach(() => {
      return page.toggleLockedState();
    });

    it('should lock the object type select if configured', () => {
      expect(advancedSearchSection.getObjectTypeMenu().isDisabled()).to.eventually.be.true;
    });

    it('should lock all specified components in the criteria controls', () => {
      return advancedSearchSection.getCriteriaControlsForGroup().then((controls) => {
        expect(controls.andButton.isEnabled()).to.eventually.be.false;
        expect(controls.orButton.isEnabled()).to.eventually.be.false;
        expect(controls.addRuleButton.isEnabled()).to.eventually.be.false;
        expect(controls.addGroupButton.isEnabled()).to.eventually.be.false;
        // That button should always be disabled but just in case - checking it
        expect(controls.removeGroupButton.isEnabled()).to.eventually.be.false;
      });
    });

    it('should lock all specified components in the criteria row', () => {
      return advancedSearchSection.getCriteriaRowForGroup().then((row) => {
        expect(row.getPropertySelectMenu().isDisabled()).to.eventually.be.true;
        expect(row.getOperatorSelectMenu().isDisabled()).to.eventually.be.true;
        expect(row.removeButton.isEnabled()).to.eventually.be.false;

        // In this lock configuration, the value column should not be disabled!
        var stringCriteria = new AdvancedSearchStringCriteria(row.valueColumn);
        expect(stringCriteria.isDisabled()).to.eventually.be.false;
      });
    });
  });
});