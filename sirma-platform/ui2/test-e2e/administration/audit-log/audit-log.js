'use strict';

var SandboxPage = require('../../page-object').SandboxPage;
var PageObject = require('../../page-object').PageObject;
var Pagination = require('../../search/components/common/pagination');

var AdvancedSearchSection = require('../../search/components/advanced/advanced-search').AdvancedSearchSection;

const URL = 'sandbox/administration/audit-log/';
const CLEAR_BUTTON = '.clear-criteria';
const SEARCH_BUTTON = '.seip-search';
const AUDIT_TABLE = '.audit-table';
const ADVANCED_SEARCH_SECTION = require('../../search/components/advanced/advanced-search').ADVANCED_SEARCH_SECTION_SELECTOR;

//Audit entry selectors.
const AUDIT_ACTION = '.audit-action';
const AUDIT_DATE = '.audit-date';
const AUDIT_OBJECT_ID = '.audit-object-id';
const AUDIT_STATE_LABEL = '.audit-state-label';
const AUDIT_SUB_TYPE_LABEL = '.audit-sub-type-label';
const AUDIT_OBJECT_TYPE_LABEL = '.audit-object-type-label';
const AUDIT_TITLE = '.audit-title';
const AUDIT_USER_NAME = '.audit-user-name';
const AUDIT_CONTEXT = '.audit-context';
const AUDIT_TOTAL_RESULTS = '.total-results';

class AuditLogSandboxPage extends SandboxPage {

  open() {
    super.open(URL);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($(AuditLog.AUDIT_LOG_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getAuditLog() {
    return new AuditLog($(AuditLog.AUDIT_LOG_SELECTOR));
  }
}

class AuditLog extends PageObject {
  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getAdvancedSearchSection() {
    return new AdvancedSearchSection($(ADVANCED_SEARCH_SECTION));
  }

  getClearButton() {
    return this.element.$(CLEAR_BUTTON);
  }

  getSearchButton() {
    return this.element.$(SEARCH_BUTTON);
  }

  getAuditTable() {
    return new AuditTable(this.element.$(AUDIT_TABLE));
  }

  getPagination() {
    return new Pagination($(Pagination.COMPONENT_SELECTOR));
  }

  clickSearchButton() {
    var searchButton = this.getSearchButton();
    searchButton.click();
    browser.wait(EC.elementToBeClickable(searchButton), DEFAULT_TIMEOUT);
  }

  clickClearButton() {
    this.getClearButton().click();
  }

  getTotalResults() {
    return this.element.$(AUDIT_TOTAL_RESULTS);
  }

  getTotalResultsCount() {
    return this.getTotalResults().$('.badge').getText();
  }

}
AuditLog.AUDIT_LOG_SELECTOR = '.seip-audit-log';

class AuditTable extends PageObject {
  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($(AUDIT_TABLE)), DEFAULT_TIMEOUT);
  }

  isEmpty() {
    return this.element.all(by.tagName('tbody > tr')).count().then((count) => {
      return count === 0 ? true : false;
    });
  }

  getColums() {
    return this.element.all(by.tagName('th'));
  }

  getEntry(rowNumber) {
    return this.element.all(by.tagName('tbody > tr')).then((items) => {
      return new AuditEntry(items[rowNumber]);
    });
  }
}

class AuditEntry extends PageObject {
  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($(AUDIT_TABLE)), DEFAULT_TIMEOUT);
  }

  getAction() {
    return this.element.$(AUDIT_ACTION).getText();
  }

  getDate() {
    return this.element.$(AUDIT_DATE).getText();
  }

  getId() {
    return this.element.$(AUDIT_OBJECT_ID).getText();
  }

  getStateLabel() {
    return this.element.$(AUDIT_STATE_LABEL).getText();
  }

  getSubTypeLabel() {
    return this.element.$(AUDIT_SUB_TYPE_LABEL).getText();
  }

  getObjectTypeLabel() {
    return this.element.$(AUDIT_OBJECT_TYPE_LABEL).getText();
  }

  getTitle() {
    return this.element.$(AUDIT_TITLE).getText();
  }

  getUserName() {
    return this.element.$(AUDIT_USER_NAME).getText();
  }

  getContext() {
    return this.element.$(AUDIT_CONTEXT).getText();
  }
}

module.exports = {
  AuditLogSandboxPage,
  AuditLog,
  AuditEntry,
  AuditTable
};