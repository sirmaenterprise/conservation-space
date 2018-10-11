'use strict';

let DatatableFilterSandboxPage = require('./datatable-filter').DatatableFilterSandboxPage;
let Select = require('../../../../components/select/select').Select;

describe('DatatableFilter', () => {

  var datatableFilterSandboxPage;

  before(() => {
    datatableFilterSandboxPage = new DatatableFilterSandboxPage();
  });

  it('should be initialized with initial criteria if such exists', () => {
    datatableFilterSandboxPage.open();

    let datatableFilter = datatableFilterSandboxPage.getDatatableFilter('.datatable-filter-initial');

    expect(datatableFilter.getSelectFieldValue(1)).to.eventually.eql(['OT210027']);
    expect(datatableFilter.getSelectFieldValue(2)).to.eventually.eql(['ENG', 'INF']);
    expect(datatableFilter.getStringFieldValue(3)).to.eventually.equals('Initial value');
    expect(datatableFilter.getSelectFieldValue(4)).to.eventually.eql(['4']);
    datatableFilterSandboxPage.getTodayFormattedDate().then((expectedDate) => {
      expect(datatableFilter.getDateFieldValue(5)).to.eventually.equals(expectedDate);
    });
    expect(datatableFilter.getSelectFieldValue(6)).to.eventually.be.empty;
    expect(datatableFilter.getSelectFieldValue(7)).to.eventually.eql(['false']);
  });

  it('should add criteria rules based on selected filters', () => {
    datatableFilterSandboxPage.open();

    let datatableFilter = datatableFilterSandboxPage.getDatatableFilter('.datatable-filter-active');

    datatableFilter.setSelectFieldValue(1, 'OT210027');
    expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('1');
    expect(datatableFilterSandboxPage.getJsonRuleValue(1)).to.eventually.eql(['OT210027']);

    datatableFilter.setSelectFieldValue(2, 'ENG');
    datatableFilter.setSelectFieldValue(2, 'INF');
    expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('2');
    expect(datatableFilterSandboxPage.getJsonRuleValue(2)).to.eventually.eql(['ENG', 'INF']);

    datatableFilter.setStringFieldValue(3, 'Filter value');
    expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('3');
    expect(datatableFilterSandboxPage.getStringRuleValue(3)).to.eventually.eql('Filter value');

    datatableFilter.setSelectFieldValue(4, '3');
    expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('4');
    expect(datatableFilterSandboxPage.getJsonRuleValue(4)).to.eventually.eql(['3']);

    protractor.promise.all([datatableFilterSandboxPage.getTodayFormattedDate(), datatableFilterSandboxPage.getTodayISODate()]).then((results) => {
      let expectedResult = [];
      let expectedDate = new Date(results[1]);
      expectedDate.setHours(0, 0, 0, 0);
      expectedResult.push(expectedDate.toISOString());
      expectedDate.setHours(23, 59, 59, 999);
      expectedResult.push(expectedDate.toISOString());

      datatableFilter.setDateFieldValue(5, results[0]);
      expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('5');
      expect(datatableFilterSandboxPage.getJsonRuleValue(5)).to.eventually.eql(expectedResult);
    });

    datatableFilter.setSelectFieldValue(7, 'true');
    datatableFilter.setSelectFieldValue(7, 'false');
    expect(datatableFilterSandboxPage.getNumberOfRules()).to.eventually.equals('6');
    expect(datatableFilterSandboxPage.getJsonRuleValue(6)).to.eventually.eql(['true', 'false']);
  });

  it('object property filter should be empty if there are no available (aggregated) values', () => {
    let emptySelect = new Select('.datatable-filter-active .filter-cell:nth-child(6) .seip-select-wrapper');
    expect(emptySelect.getNumberOfDropdownOptions()).to.eventually.equals(0);
  });
});
