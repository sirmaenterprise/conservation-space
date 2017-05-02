var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchStringCriteria = require('./advanced-search.js').AdvancedSearchStringCriteria;
var hasClass = require('../../../test-utils.js').hasClass;

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

  describe('When a rule is removed', () => {
    it('should no longer be visible in the tree', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.removeButton.click();
        return advancedSearchSection.getCriteriaRowsForGroup(1, 0);
      }).then((rows) => {
        expect(rows.length).to.equal(0);
      });
    });

    it('should no longer be present in the model', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.removeButton.click();
        return page.getTreeModel();
      }).then((treeModel) => {
        var removedCriteria = treeModel.rules[0].rules[1].rules[0];
        expect(removedCriteria).to.not.exist;
      });
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

  describe('When criteria property is changed', () => {
    it('should change the property & type value in the tree model', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        // Change the property to load test operators
        return row.changeProperty('test-property');
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var field = treeModel.rules[0].rules[1].rules[0].field;
        expect(field).to.equal('test-property');
        var type = treeModel.rules[0].rules[1].rules[0].type;
        expect(type).to.equal('test-type');
      });
    });

    it('should preserve operators if the property type is the same', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('test-property').then(() => {
          row.waitForOperatorSelectToRender();
          return row.changeOperator('test-operator2');
        }).then(() => {
          return row.changeProperty('test-property2');
        }).then(() => {
          row.waitForOperatorSelectToRender();
          return row.getSelectedOperatorValue();
        }).then((selectedOperator) => {
          expect(selectedOperator).to.equals('test-operator2');
        });
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var operator = treeModel.rules[0].rules[1].rules[0].operator;
        expect(operator).to.equal('test-operator2');
      });
    });

    it('should replace operators if the property type is not the same', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        // Change the property to load test operators
        return row.changeProperty('test-property').then(() => {
          row.waitForOperatorSelectToRender();
          return row.getSelectedOperatorValue();
        }).then((selectedOperator) => {
          return expect(selectedOperator).to.equal('test-operator');
        });
      }).then(() => {
        return page.getTreeModel();
      }).then((treeModel) => {
        var operator = treeModel.rules[0].rules[1].rules[0].operator;
        expect(operator).to.equal('test-operator');
      });
    });

    it('should keep the value extension if the property type is the same', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        // Change the property to load test operators
        return row.changeProperty('description').then(() => {
          return row.getValueExtension('string');
        });
      }).then((stringExtension) => {
        expect(stringExtension.getMenu().isDisplayed()).to.eventually.be.true;
      });
    });

    it('should replace the value extension if the property type is not the same', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        // Change the property to load test operators
        return row.changeProperty('test-property').then(() => {
          return row.getValueExtension('test');
        });
      }).then((testExtension) => {
        expect(testExtension.testElement.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should reset any entered value', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.getValueExtension('string').enterValue('123');
        return row.changeProperty('description').then(()=> {
          return row.getValueExtension('string').getValue();
        });
      }).then((selectedValue) => {
        expect(selectedValue).to.deep.equal([]);
      });
    });

    it('should filter out any operators that are not defined in the property', () => {
      var criteriaRow;
      return advancedSearchSection.selectProperty('property-with-operators').then((row) => {
        criteriaRow = row;
        return criteriaRow.getSelectedOperatorValue();
      }).then((selectedOperator) => {
        expect(selectedOperator).to.equal('contains');
        return criteriaRow.getOperatorSelectValues();
      }).then((operators) => {
        expect(operators).to.deep.equal(['contains', 'equals']);
      });
    });
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

  describe('When condition is changed to OR', () => {
    it('should change the condition value in the tree model', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.or();
        page.getTreeModel().then((treeModel) => {
          var condition = treeModel.rules[0].rules[1].condition;
          expect(condition).to.equal('OR');
        });
      });
    });

    it('should render the OR button as active', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.or();
        expect(hasClass(controls.orButton, 'btn-primary')).to.eventually.be.true;
        expect(hasClass(controls.andButton, 'btn-primary')).to.eventually.be.false;
      });
    });
  });

  describe('When condition is changed to AND', () => {
    it('should change the condition value in the tree model', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.and();
        page.getTreeModel().then((treeModel) => {
          var condition = treeModel.rules[0].rules[1].condition;
          expect(condition).to.equal('AND');
        });
      });
    });

    it('should render the AND button as active', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.and();
        expect(hasClass(controls.andButton, 'btn-primary')).to.eventually.be.true;
        expect(hasClass(controls.orButton, 'btn-primary')).to.eventually.be.false;
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