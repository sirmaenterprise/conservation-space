'use strict';

let RoleActionsTableSandboxPage = require('./role-actions-table').RoleActionsTableSandboxPage;

describe('Manage actions per role', () => {

  let sandbox = new RoleActionsTableSandboxPage();
  let roleActionsTable;

  beforeEach(() => {
    sandbox.open();
    roleActionsTable = sandbox.getRoleActionsTable();
  });

  it('should display only the filtered actions in the table', () => {
    roleActionsTable.filterActions('Approve');
    let approveActionRow = roleActionsTable.getActionRow('approve');
    let lockActionRow = roleActionsTable.getActionRow('lock');

    expect(approveActionRow.isDisplayed()).to.eventually.be.true;
    expect(lockActionRow.isPresent()).to.eventually.be.false;
  });

  it('should display more than one result if actions with similar labels exist', () => {
    roleActionsTable.filterActions('lock');
    let unlockActionRow = roleActionsTable.getActionRow('unlock');
    let lockActionRow = roleActionsTable.getActionRow('lock');
    let approveActionRow = roleActionsTable.getActionRow('approve');

    expect(unlockActionRow.isDisplayed()).to.eventually.be.true;
    expect(lockActionRow.isDisplayed()).to.eventually.be.true;
    expect(approveActionRow.isPresent()).to.eventually.be.false;
  });

  it('should have edit button in preview mode', () => {
    expect(roleActionsTable.getEditButton().isDisplayed()).to.eventually.be.true;
  });

  it('should have save and cancel buttons in edit mode', () => {
    roleActionsTable.enterEditMode();
    expect(roleActionsTable.getSaveButton().isDisplayed()).to.eventually.be.true;
    expect(roleActionsTable.getCancelButton().isDisplayed()).to.eventually.be.true;

    roleActionsTable.cancelEditMode();
    expect(roleActionsTable.getEditButton().isDisplayed()).to.eventually.be.true;
  });

  it('should save changed roles actions when save button is pressed', () => {
    roleActionsTable.enterEditMode();
    roleActionsTable.enableRoleAction('CONSUMER', 'approve');
    roleActionsTable.selectFilters('CONSUMER', 'approve', 'CREATEDBY');
    roleActionsTable.saveChanges();
    expect(roleActionsTable.isEnabled('CONSUMER', 'approve')).to.eventually.equal('true');
    expect(roleActionsTable.getSelectedFilters('CONSUMER', 'approve')).to.eventually.deep.equal('CREATEDBY');
  });

  it('should revert made changes when cancel button is pressed', () => {
    roleActionsTable.enterEditMode();
    roleActionsTable.enableRoleAction('CONSUMER', 'approve');
    roleActionsTable.selectFilters('CONSUMER', 'approve', 'CREATEDBY');
    roleActionsTable.cancelEditMode();
    expect(roleActionsTable.isEnabled('CONSUMER', 'approve')).to.eventually.equal(null);
    expect(roleActionsTable.getSelectedFilters('CONSUMER', 'approve')).to.eventually.deep.equal('Select filters');
  });

});
