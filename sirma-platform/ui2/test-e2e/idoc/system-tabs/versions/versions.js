"use strict";
var SandboxPage = require('../../../page-object').SandboxPage;

class VersionsSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/system-tabs/versions/');
  }

  getVersionsPanel() {
    var versionsPanel = new VersionsPanel($('.versions'));
    versionsPanel.waitUntilOpened();
    return versionsPanel;
  }
}

class VersionsPanel {
  constructor(element) {
    this.element = element;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getVersion(number) {
    return new VersionsEntry(this.element.$('tbody  tr:nth-child(' + number + ')'));
  }

  selectVersion(number) {
    let checkbox = this.getVersion(number).getCheckbox();
    browser.wait(EC.elementToBeClickable(checkbox), DEFAULT_TIMEOUT);
    checkbox.click();
  }

  getCompareButton() {
    return this.element.$('.compare-versions');
  }
}

class VersionsEntry {

  constructor(element) {
    this.element = element;
  }

  getCheckbox() {
    return this.element.$('label.checkbox');
  }
}

class VersionTab {

  constructor(element) {
    this.element = element;
  }

  versionLink(version) {
    let link = this.element.$$(`tbody tr:nth-child(${version}) span`).get(1);
    browser.wait(EC.elementToBeClickable(link), DEFAULT_TIMEOUT);
    return link;
  }

  getVersionLink(version) {
    return this.element.$$(`tbody tr:nth-child(${version}) span`).get(1);
  }
}

module.exports = {
  VersionsSandboxPage,
  VersionsPanel,
  VersionTab
};