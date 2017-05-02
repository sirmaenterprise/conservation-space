'use strict';

let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;
let CheckboxField = require('../../form-builder/form-control').CheckboxField;
let MultySelectMenu = require('../../form-builder/form-control').MultySelectMenu;

const SANDBOX_PAGE_URL = '/sandbox/administration/role-actions-table';

class RoleActionsTableSandboxPage extends SandboxPage {

  open() {
    browser.get(SANDBOX_PAGE_URL);
  }

  getRoleActionsTable() {
    return new RoleActionsTable($('.role-actions-table'));
  }

}

class RoleActionsTable extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getActionRow(actionId) {
    return this.element.$('tr#' + actionId);
  }

  getFilterInput() {
    return this.element.$('input.filter-input');
  }

  filterActions(filter) {
    let filterInput = this.getFilterInput();
    this.waitForElementToBePresent(filterInput);
    filterInput.click();
    return filterInput.sendKeys(filter);
  }

  enterEditMode() {
    this.getEditButton().click();
  }

  cancelEditMode() {
    this.getCancelButton().click();
  }

  saveChanges() {
    this.getSaveButton().click();
  }

  enableRoleAction(roleId, actionId) {
    let row = new RoleActionsTableRow(this.getActionRow(actionId));
    let roleCheckbox = row.getRoleCheckbox(roleId);
    roleCheckbox.toggleCheckbox();
  }

  isEnabled(roleId, actionId) {
    let row = new RoleActionsTableRow(this.getActionRow(actionId));
    let roleCheckbox = row.getRoleCheckbox(roleId);
    return roleCheckbox.isChecked();
  }

  selectFilters(roleId, actionId, filters) {
    let row = new RoleActionsTableRow(this.getActionRow(actionId));
    let roleFilters = row.getRoleFilters(roleId, actionId, true);
    if (filters instanceof Array) {
      filters.forEach((filter) => {
        roleFilters.selectFromMenuByValue(filter);
      });
    } else {
      roleFilters.selectFromMenuByValue(filters);
    }
  }

  getSelectedFilters(roleId, actionId) {
    let row = new RoleActionsTableRow(this.getActionRow(actionId));
    let roleFilters = row.getRoleFilters(roleId, actionId);
    return roleFilters.getValues();
  }

  getEditButton() {
    let editBtn = this.element.$('.edit-btn');
    this.waitForElementToBePresent(editBtn);
    return editBtn;
  }

  getSaveButton() {
    let saveBtn = this.element.$('.save-btn');
    this.waitForElementToBePresent(saveBtn);
    return saveBtn;
  }

  getCancelButton() {
    let cancelBtn = this.element.$('.cancel-btn');
    this.waitForElementToBePresent(cancelBtn);
    return cancelBtn;
  }

  waitForElementToBePresent(element) {
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
  }

}

class RoleActionsTableRow {

  constructor(element) {
    this.element = element;
  }

  getRoleCheckbox(roleId) {
    return new CheckboxField(this.element.$('.' + roleId));
  }

  getRoleFilters(roleId, actionId, editMode) {
    if (editMode) {
      return new MultySelectMenu(this.element.$('.' + roleId + actionId + '-select'));
    }
    return new FiltersBox(this.element.$('.' + roleId + actionId + '-box'));
  }

}

class FiltersBox {

  constructor(element) {
    this.element = element;
  }

  getValues() {
    return this.element.$('li').getText();
  }

}

module.exports = {
  RoleActionsTableSandboxPage,
  RoleActionsTable
};