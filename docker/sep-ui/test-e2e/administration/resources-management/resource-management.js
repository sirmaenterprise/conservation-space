'use strict';

let PageObject = require('../../page-object').PageObject;
let SandboxPage = require('../../page-object').SandboxPage;
let Dialog = require('../../components/dialog/dialog');
let PropertiesSelector = require('../../idoc/widget/properties-selector/properties-selector').PropertiesSelector;
let SaveIdocDialog = require('../../idoc/save-idoc-dialog').SaveIdocDialog;
let ObjectControl = require('../../form-builder/form-control.js').ObjectControl;
let Pagination = require('../../search/components/common/pagination');


class ResourceManagementSandboxPage extends SandboxPage {

  open() {
    browser.get('/sandbox/administration/resources-management');
  }

  getResourceManagement() {
    return new ResourceManagement($('.resource-management'));
  }

}

class ResourceManagement extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  openPropertiesConfiguration() {
    this.element.$('.config-btn').click();
    let dialog = new Dialog($(Dialog.COMPONENT_SELECTOR));
    dialog.waitUntilOpened();
    return dialog;
  }

  getPropertiesSelector() {
    let propertiesSelector = new PropertiesSelector();
    propertiesSelector.waitUntilOpened();
    return propertiesSelector;
  }

  getUsers() {
    let rows = this.element.$$('tbody tr').first();
    browser.wait(EC.visibilityOf(rows), DEFAULT_TIMEOUT);
    return this.element.$$('tbody tr');
  }

  columnsCount() {
    let columns = this.element.$$('th').first();
    browser.wait(EC.visibilityOf(columns), DEFAULT_TIMEOUT);
    return this.element.$$('th').count();
  }

  openEditUser(userIndex) {
    let user = this.getUsers().get(userIndex);
    user.$$('td:last-child a').first().click();
    browser.wait(EC.visibilityOf($('#invalidObjectsList')), DEFAULT_TIMEOUT);
    return new SaveIdocDialog();
  }

  getPropertyValue(userIndex, propertyIndex) {
    let user = this.getUsers().get(userIndex);
    // increase by one because nth-child selector starts from one not zero
    return user.$('td:nth-child(' + (propertyIndex + 1) + ')').getText();
  }

  getObjectProperty(row, column) {
    let user = this.getUsers().get(row);
    return new ObjectControl(user.$('td:nth-child(' + (column + 1) + ')'));
  }

  getCreateUserButton() {
    let btn = this.element.$('.btn-primary');
    browser.wait(EC.visibilityOf(btn), DEFAULT_TIMEOUT);
    return btn;
  }

  getPagination() {
    return new Pagination(this.element.$(Pagination.COMPONENT_SELECTOR));
  }

}


module.exports = {
  ResourceManagementSandboxPage,
  ResourceManagement
};