"use strict";
var SingleSelectMenu = require('../../../form-builder/form-control').SingleSelectMenu;
var Dialog = require('../../../components/dialog/dialog');
var Notification = require('../../../components/notification').Notification;
var SandboxPage = require('../../../page-object').SandboxPage;

class PermissionsSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/system-tabs/permissions/');
  }

  getPermissionsPanel() {
    var permissionsPanel = new PermissionsPanel($('.permissions'));
    permissionsPanel.waitUntilOpened();
    return permissionsPanel;
  }
}

class PermissionsPanel {
  constructor(element) {
    this.element = element;
  }

  edit() {
    this.element.$('.edit-permissions').click();
  }

  cancel() {
    this.element.$('.cancel').click();
  }

  save() {
    this.element.$('.save-permissions').click();
    browser.wait(EC.visibilityOf(this.element.$('.edit-permissions')), DEFAULT_TIMEOUT);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  addEntry() {
    this.element.$('.add-user').click();

    return new PermissionsEntry(this.element, 1);
  }

  getEntry(number) {
    return new PermissionsEntry(this.element, number);
  }

  getCount() {
    return this.element.$$('.user-row').then(function (rows) {
      return rows.length;
    });
  }

  getPermissionAssignmentRows() {
    return this.element.$$('.user-row');
  }

  sortAscending() {
    this.element.$('.up').click();
  }

  sortDescending() {
    this.element.$('.down').click();
  }

  restoreChildrenPermissions() {
    this.element.$('.restore-children-permissions').click();

    var dialog = new Dialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    dialog.getOkButton().click();
    dialog.waitUntilClosed();

    return new Notification().waitUntilOpened();
  }
}

class PermissionsEntry {
  constructor(permissionsPanel, rowNumber) {
    this.element = permissionsPanel.$('tbody tr:nth-child(' + rowNumber + ')');
  }

  getAuthority() {
    return new SingleSelectMenu(this.element.$('.select-user'));
  }

  getSpecialPermissions() {
    return new SingleSelectMenu(this.element.$('.select-special'));
  }
}

module.exports = {
  PermissionsSandboxPage,
  PermissionsPanel
};