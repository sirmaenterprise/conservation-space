var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchNumericCriteria = require('../advanced-search.js').AdvancedSearchNumericCriteria;

describe('AdvancedSearchNumericCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When a numeric property is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('numeric').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.eq(['equals', 'does_not_equal', 'greater_than', 'less_than', 'between', 'empty']);
        });
      });
    });
  });

  describe('When the criteria set is changed to predefined', () => {
    it('should display a predefined value inside the numeric criteria', () => {
      page.changeCriteriaSet('predefined');
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 3);
      }).then((firstRow) => {
        return firstRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('numeric');
          var valueCriteria = new AdvancedSearchNumericCriteria(firstRow.valueColumn);
          return valueCriteria.getSingleInputValue();
        }).then((stringValue) => {
          expect(stringValue).to.deep.equal('999');
        });
      });
    });
  });

  describe('When a numeric property is selected with operator different than between', () => {
    it('should display a select component with numeric values when equals operator is selected', () => {
      return Promise.all[testIsSingleNumericInputDisplayed('equals'),
        testIsSingleNumericInputDisplayed('does_not_equal'),
        testIsSingleNumericInputDisplayed('greater_than'),
        testIsSingleNumericInputDisplayed('less_than')];
    });
  });

  describe('When the property is initially loaded it should display value', () => {
    it('should display and contain initialized value', () => {
      return testIsSingleNumericInputContainingValue('equals', '999', '999');
    });
  });

  describe('When a numeric property is selected with operator between', () => {
    it('should display a select component with numeric value on the right', () => {
      return advancedSearchSection.selectPropertyAndOperator('numeric', 'between').then((row) => {
        var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
        return expect(numericCriteria.betweenRightInputSelect.isDisplayed() &&
          numericCriteria.betweenLeftInputSelect.isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When a numeric value is entered into the single input field', () => {
    it('should display and contain that value', () => {
      return Promise.all[testIsSingleNumericInputContainingValue('equals', '123', '123'),
        testIsSingleNumericInputContainingValue('does_not_equal', '321', '321'),
        testIsSingleNumericInputContainingValue('greater_than', '567', '567'),
        testIsSingleNumericInputContainingValue('less_than', '986', '986')];
    });

    it('should update the tree model for the single input', () => {
      return Promise.all[testIsSingleNumericInputUpdatingTree('equals', '123', 123),
        testIsSingleNumericInputUpdatingTree('does_not_equal', '321', 321),
        testIsSingleNumericInputUpdatingTree('greater_than', '567', 567),
        testIsSingleNumericInputUpdatingTree('less_than', '986', 986)];
    });
  });

  describe('When a numeric value is entered into the double input field', () => {
    it('should display and contain that value for both input fields', () => {
      return Promise.all[testIsBetweenNumericInputContainingValue('111', '111', false),
        testIsBetweenNumericInputContainingValue('111', '111', true)];
    });

    it('should update the tree model for both input fields', () => {
      return Promise.all[testIsBetweenNumericInputUpdatingTree('111', 111, false),
        testIsBetweenNumericInputUpdatingTree('111', 111, true)];
    });
  });

  describe('When the advanced search form is disabled', () => {
    it('should render the single input field as disabled when equals is selected', () => {
      return testIsSingleNumericInputDisabled('equals');
    });

    it('should render the single input field as disabled when does not equal is selected', () => {
      return testIsSingleNumericInputDisabled('does_not_equal');
    });

    it('should render the single input field as disabled when greater is selected', () => {
      return testIsSingleNumericInputDisabled('greater_than');
    });

    it('should render the single input field as disabled when less is selected', () => {
      return testIsSingleNumericInputDisabled('less_than');
    });

    it('should render the left input field as disabled', () => {
      return advancedSearchSection.selectPropertyAndOperator('numeric', 'between').then((row) => {
        return page.toggleEnabledState().then(() => {
          var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
          return numericCriteria.isBetweenLeftInputDisabled().then((state) => {
            return expect(state).to.equal('true');
          });
        });
      });
    });

    it('should render the right input field as disabled', () => {
      return advancedSearchSection.selectPropertyAndOperator('numeric', 'between').then((row) => {
        return page.toggleEnabledState().then(() => {
          var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
          return numericCriteria.isBetweenRightInputDisabled().then((state) => {
            return expect(state).to.equal('true');
          });
        });
      });
    });
  });

  describe('When a numeric property is selected and non numeric value is entered', () => {
    it('should not display the invalid value for single input field', () => {
      return Promise.all[testIsSingleNumericInputContainingValue('equals', 'invalid', ''),
        testIsSingleNumericInputContainingValue('does_not_equal', '+321', '321'),
        testIsSingleNumericInputContainingValue('greater_than', '-567', '-567'),
        testIsSingleNumericInputContainingValue('less_than', '--986', '-986')];
    });

    it('should not display the invalid value for double input field', () => {
      return Promise.all[testIsBetweenNumericInputContainingValue('invalid', '', true),
        testIsBetweenNumericInputContainingValue('+321', '321', false),
        testIsBetweenNumericInputContainingValue('-567', '-567', true),
        testIsBetweenNumericInputContainingValue('--986', '-986', false)];
    });
  });

  function testIsSingleNumericInputDisplayed(operator) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', operator).then((row) => {
      var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
      return expect(numericCriteria.singleInputSelect.isDisplayed()).to.eventually.be.true;
    })
  }

  function testIsSingleNumericInputDisabled(operator) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', operator).then((row) => {
      return page.toggleEnabledState().then(() => {
        var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
        return numericCriteria.isSingleInputDisabled().then((state) => {
          return expect(state).to.equal('true');
        });
      });
    });
  }

  function testIsSingleNumericInputContainingValue(operator, input, expected) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', operator).then((row) => {
      var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
      return numericCriteria.setSingleInputValue(input).then(() => {
        return numericCriteria.getSingleInputValue();
      });
    }).then((inputValue) => {
      expect(inputValue).to.equal(expected);
    });
  }

  function testIsSingleNumericInputUpdatingTree(operator, input, expected) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', operator).then((row) => {
      var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
      return numericCriteria.setSingleInputValue(input);
    }).then(() => {
      return page.getTreeModel();
    }).then((treeModel) => {
      var operator = treeModel.rules[0].rules[1].rules[0].operator;
      var value = treeModel.rules[0].rules[1].rules[0].value;
      expect(operator).to.equal(operator);
      expect(value).to.equal(expected);
    });
  }

  function getAndSetABetweenField(numericCriteria, input, rightInput) {
    return (rightInput) ? numericCriteria.setBetweenRightInputValue(input) :
      numericCriteria.setBetweenLeftInputValue(input);
  }

  function testIsBetweenNumericInputContainingValue(input, expected, rightInput) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', 'between').then((row) => {
      var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
      return getAndSetABetweenField(numericCriteria, input, rightInput).then(() => {
        return (rightInput) ? numericCriteria.getBetweenRightInputValue()
          : numericCriteria.getBetweenLeftInputValue();
      });
    }).then((inputValue) => {
      expect(inputValue).to.equal(expected);
    });
  }

  function testIsBetweenNumericInputUpdatingTree(input, expected, rightInput) {
    return advancedSearchSection.selectPropertyAndOperator('numeric', 'between').then((row) => {
      var numericCriteria = new AdvancedSearchNumericCriteria(row.valueColumn);
      return getAndSetABetweenField(numericCriteria, input, rightInput)
    }).then(() => {
      return page.getTreeModel();
    }).then((treeModel) => {
      var operator = treeModel.rules[0].rules[1].rules[0].operator;
      var index = (rightInput) ? 1 : 0;
      var value = treeModel.rules[0].rules[1].rules[0].value[index];
      expect(operator).to.equal('between');
      expect(value).to.equal(expected);
    });
  }

});
