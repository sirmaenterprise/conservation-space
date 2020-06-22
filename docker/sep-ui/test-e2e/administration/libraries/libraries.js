'use strict';

let InstanceList = require('../../instance/instance-list').InstanceList;
let SandboxPage = require('../../page-object').SandboxPage;
let MultySelectMenu = require('../../form-builder/form-control.js').MultySelectMenu;

class LibrariesSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/administration/libraries/');
  }

  getLibrariesPanel() {
    let librariesPanel = new LibrariesPanel($('.browse-libraries'));
    librariesPanel.waitUntilOpened();
    return librariesPanel;
  }

  getOntologyFilter() {
    return $('#ontology-filter').getText();
  }

  getTitleFilter() {
    return $('#title-filter').getText();
  }
}

class LibrariesPanel {
  constructor(element) {
    this.element = element;
    this.libs = new InstanceList(element.$('.instance-list'));
  }

  getOntologyFilterField() {
    return new MultySelectMenu(this.element.$('.ontology-filter'));
  }

  getTitleFilterField() {
    return this.element.$('.title-filter');
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }
}

module.exports = {
  LibrariesSandboxPage,
  LibrariesPanel
};