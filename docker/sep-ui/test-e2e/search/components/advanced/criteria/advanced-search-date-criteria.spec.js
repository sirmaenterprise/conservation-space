var AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchDateCriteria = require('../advanced-search.js').AdvancedSearchDateCriteria;
var DatetimeField = require('../../../../form-builder/form-control.js').DatetimeField;
var DynamicDateRange = require('../dynamic-date-range/dynamic-date-range.js').DynamicDateRange;

function getTodayAt(hours, minutes, seconds, millis) {
  var today = new Date();
  today.setHours(hours);
  today.setMinutes(minutes);
  today.setSeconds(seconds);
  today.setMilliseconds(millis);
  return today;
}

describe('AdvancedSearchDateCriteria', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType( "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When a date property is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('createdOn').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.eq(['after', 'before', 'is', 'between', 'within', 'empty']);
        });
      });
    });

    it('should set "Is after" for operator by default', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('createdOn').then(() => {
          row.waitForOperatorSelectToRender();
          return row.getSelectedOperatorValue();
        }).then((selectedOperator) => {
          expect(selectedOperator).to.equal('after');
        });
      });
    });
  });

  describe('When "Is after" is selected for operator', () => {
    it('should display single date picker', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'after').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        expect(dateCriteria.datePicker.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should update the tree model when a date is selected', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'after').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        var datePicker = new DatetimeField(dateCriteria.datePicker);
        datePicker.setToday();
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.exist;
      });
    });
  });

  describe('When "Is" is selected for operator', () => {
    it('should display single date picker', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'is').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        expect(dateCriteria.datePicker.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should update the tree model when a date is selected', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'is').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        var datePicker = new DatetimeField(dateCriteria.datePicker);
        datePicker.setToday();
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.exist;
        expect(value.length).to.equal(2);

        var start = getTodayAt(0, 0, 0, 0).toISOString();
        var end = getTodayAt(23, 59, 59, 999).toISOString();

        expect(value[0]).to.eq(start);
        expect(value[1]).to.eq(end);
      });
    });
  });

  describe('When "Is between" is selected for operator', () => {
    it('should display two date pickers', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'between').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        expect(dateCriteria.datePickerFrom.isDisplayed()).to.eventually.be.true;
        expect(dateCriteria.datePickerTo.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should update the tree model when a range is selected', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'between').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        var datePickerFrom = new DatetimeField(dateCriteria.datePickerFrom);
        datePickerFrom.setToday();
        var datePickerTo = new DatetimeField(dateCriteria.datePickerTo);
        datePickerTo.setToday();
        return page.getTreeModel();
      }).then((treeModel) => {
        var value = treeModel.rules[0].rules[1].rules[0].value;
        expect(value).to.exist;
        expect(value.length).to.equal(2);
      });
    });
  });

  describe('When "Is within" is selected for operator', () => {
    it('should render the dynamic date range form', () => {
      return advancedSearchSection.selectPropertyAndOperator('createdOn', 'within').then((row) => {
        var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
        var dynamicDateRange = new DynamicDateRange(dateCriteria.dynamicDateRangeElement);
        return expect(dynamicDateRange.isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('When the advanced search form is disabled', () => {
    it('should render the date pickers as disabled', () => {
      // Insert second row
      return advancedSearchSection.insertRow().then(() => {
        // Insert third row
        return advancedSearchSection.insertRow();
      }).then(() => {
        return advancedSearchSection.selectPropertyAndOperator('createdOn', 'after');
      }).then(() => {
        return advancedSearchSection.selectPropertyAndOperator('createdOn', 'is', 1, 0, 1);
      }).then(() => {
        return advancedSearchSection.selectPropertyAndOperator('createdOn', 'between', 1, 0, 2);
      }).then(() => {
        return page.toggleEnabledState();
      }).then(() => {
        return advancedSearchSection.getCriteriaRowsForGroup(1, 0).then((rows) => {
          rows.forEach((row) => {
            var dateCriteria = new AdvancedSearchDateCriteria(row.valueColumn);
            expect(dateCriteria.isDisabled()).to.eventually.be.true;
          });
        });
      });
    });
  });

});