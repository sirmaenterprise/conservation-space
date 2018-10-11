'use strict';

var AuditLogSandboxPage = require('./audit-log.js').AuditLogSandboxPage;

describe('AuditLog page test', () => {

  var sandboxPage = new AuditLogSandboxPage();
  var auditLog;
  beforeEach(() => {
    sandboxPage.open();
    auditLog = sandboxPage.getAuditLog();
  });

  it('should render all elements in the page', () => {
    var advancedSearchSection = auditLog.getAdvancedSearchSection();
    expect(advancedSearchSection.element.isPresent()).to.eventually.be.true;
    expect(auditLog.getSearchButton().isPresent()).to.eventually.be.true;
    expect(auditLog.getClearButton().isPresent()).to.eventually.be.true;
    expect(auditLog.getAuditTable().element.isPresent()).to.eventually.be.true;
  });

  it('should clear results when clear button is pressed', () => {
    let resultsTable = auditLog.getAuditTable();

    expect(resultsTable.isEmpty()).to.eventually.be.true;
    auditLog.clickSearchButton();
    expect(resultsTable.isEmpty()).to.eventually.be.false;
    auditLog.clickClearButton();
    expect(resultsTable.isEmpty()).to.eventually.be.true;
  });

  it('should render a pagination', () => {
    let resultsTable = auditLog.getAuditTable();

    expect(resultsTable.isEmpty()).to.eventually.be.true;
    auditLog.clickSearchButton();
    expect(resultsTable.isEmpty()).to.eventually.be.false;
    expect(resultsTable.isEmpty()).to.eventually.be.false;
    let pagination = auditLog.getPagination();
    pagination.waitUntilVisible();
  });

  it('should have proper column count', () => {
    auditLog.clickSearchButton();
    let resultsTable = auditLog.getAuditTable();

    expect(resultsTable.getColums().count()).to.eventually.be.equals(9);
  });

  it('audit entry should be properly rendered', () => {
    auditLog.clickSearchButton();
    let resultsTable = auditLog.getAuditTable();

    resultsTable.getEntry(23).then((entry) => {
      expect(entry.getAction()).to.eventually.be.equals('Edit');
      expect(entry.getId()).to.eventually.be.equals('emf:123456');
      expect(entry.getStateLabel()).to.eventually.be.equals('ACTIVE');
      expect(entry.getSubTypeLabel()).to.eventually.be.equals('userDefinition');
      expect(entry.getObjectTypeLabel()).to.eventually.be.equals('label');
      expect(entry.getTitle()).to.eventually.be.equals('Guest');
      expect(entry.getUserName()).to.eventually.be.equals('System@hristoui2.bg');
      expect(entry.getContext()).to.eventually.be.equals('Project');
    });
  });

  it('should render total results count', () => {
    auditLog.clickSearchButton();
    expect(auditLog.getTotalResults().isDisplayed()).to.eventually.be.true;
    expect(auditLog.getTotalResultsCount()).to.eventually.be.equals("250");
  });

});