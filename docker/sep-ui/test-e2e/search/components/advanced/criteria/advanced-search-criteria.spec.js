var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;

describe('AdvancedSearch Criteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
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
});