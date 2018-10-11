'use strict';

let ResourceManagementSandboxPage = require('./resource-management').ResourceManagementSandboxPage;

describe('Resource management', () => {

  let sandbox = new ResourceManagementSandboxPage();
  let resourceManagement;

  beforeEach(() => {
    sandbox.open();
    resourceManagement = sandbox.getResourceManagement();
  });

  it('should have create user button', () => {
    resourceManagement.getCreateUserButton();
  });

  it('should be able to configure properties to display', () => {
    resourceManagement.openPropertiesConfiguration();
  });

  it('should change table columns when properties are changed', () => {
    expect(resourceManagement.columnsCount()).to.eventually.equal(6);

    let dialog = resourceManagement.openPropertiesConfiguration();
    resourceManagement.getPropertiesSelector().selectProperty('jobtitle');
    dialog.ok();

    expect(resourceManagement.getPropertyValue(2, 5)).to.eventually.equal('Designer');
    expect(resourceManagement.columnsCount()).to.eventually.equal(7);
  });

  it('should update resource data after edit', () => {
    let saveDialog = resourceManagement.openEditUser(1);
    let firstNameField = saveDialog.getForm().getInputText('firstName');
    firstNameField.setValue(null, 'John');
    saveDialog.ok();

    expect(resourceManagement.getPropertyValue(1, 1)).to.eventually.equal('John');
  });

  it('should display fields of type email as text', () => {
    let dialog = resourceManagement.openPropertiesConfiguration();
    resourceManagement.getPropertiesSelector().selectProperty('emailAddress');
    dialog.ok();

    expect(resourceManagement.getPropertyValue(0, 5)).to.eventually.equal('admin@zimx.sirmaplatform.com');
  });

  it('should render fields of type object property', () => {
    // Should have 2 related objects selected
    let memberOf = resourceManagement.getObjectProperty(0, 4);
    expect(memberOf.getSelectedObjectsCount()).to.eventually.equal(2);

    // Check the next page of the table
    let pagination = resourceManagement.getPagination();
    pagination.goToPage(2);
    // Should have 3 related objects selected and one hidden
    memberOf = resourceManagement.getObjectProperty(2, 4);
    expect(memberOf.getSelectedObjectsCount()).to.eventually.equal(3);
    expect(memberOf.getHiddenObjectsCount()).to.eventually.equal('1');
    memberOf.showAll();
    expect(memberOf.getSelectedObjectsCount()).to.eventually.equal(4);
    memberOf.showLess();
    expect(memberOf.getSelectedObjectsCount()).to.eventually.equal(3);
    expect(memberOf.getHiddenObjectsCount()).to.eventually.equal('1');
  });
});