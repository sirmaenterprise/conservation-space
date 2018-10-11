'user strict';

var SandboxPage = require('../../../../page-object').SandboxPage;
var MultySelectMenu = require('../../../../form-builder/form-control.js').MultySelectMenu;
var InputField = require('../../../../form-builder/form-control.js').InputField;

const FILTER_FIELD_SELECTOR = '.filter-cell > div';

class DatatableFilterSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/widget/datatable-widget/datatable-filter');
    this.numberOfRules = $('.datatable-filter-bootstrap .filter-criteria-rules-number');
  }

  getDatatableFilter(selector) {
    return new DatatableFilter(selector);
  }

  getNumberOfRules() {
    return this.numberOfRules.getText();
  }

  getStringRuleValue(numberOfRule) {
    return element.all(By.css('.datatable-filter-bootstrap > div.filter-criteria-rule-value')).get(numberOfRule - 1).getText();
  }

  getJsonRuleValue(numberOfRule) {
    return this.getStringRuleValue(numberOfRule).then((result) => {
      return JSON.parse(result);
    });
  }

  getTodayFormattedDate() {
    return element(By.id('todayFormatted')).getText();
  }

  getTodayISODate() {
    return element(By.id('todayISO')).getText();
  }
}

class DatatableFilter {

  constructor(selector) {
    this.filterElement = $(selector);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.filterElement.$('.filter-cell')), DEFAULT_TIMEOUT);
  }

  setSelectFieldValue(numberOfColumn, value) {
    let multiSelectMenu = new MultySelectMenu(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    multiSelectMenu.selectFromMenu('', value);
  }

  getSelectFieldValue(numberOfColumn) {
    let multiSelectMenu = new MultySelectMenu(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    return multiSelectMenu.getSelectedValue();
  }

  setStringFieldValue(numberOfColumn, value) {
    let inputField = new InputField(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    inputField.setValue(undefined, value);
    inputField.getInputElement().sendKeys(protractor.Key.ENTER);
  }

  getStringFieldValue(numberOfColumn) {
    let inputField = new InputField(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    return inputField.getValue();
  }

  setDateFieldValue(numberOfColumn, value) {
    let datetimeInputField = new InputField(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    browser.wait(EC.presenceOf(datetimeInputField.getInputElement()), DEFAULT_TIMEOUT);
    datetimeInputField.setValue(undefined, value);
  }

  getDateFieldValue(numberOfColumn) {
    let datetimeInputField = new InputField(this.getFilterCell(numberOfColumn).$(FILTER_FIELD_SELECTOR));
    browser.wait(EC.presenceOf(datetimeInputField.getInputElement()), DEFAULT_TIMEOUT);
    return datetimeInputField.getValue();
  }

  getFilterCell(numberOfColumn) {
    return this.filterElement.$(`.filter-cell:nth-child(${numberOfColumn})`);
  }
}

module.exports.DatatableFilterSandboxPage = DatatableFilterSandboxPage;