'use strict';

var InstanceCreatePanelSandboxPage = require('./instance-create-panel').InstanceCreatePanelSandboxPage;
var Notification = require('../components/notification').Notification;

const TYPES = '.types';
const SUB_TYPES = '.sub-types';

describe('InstanceCreatePanel', function () {

  let instanceCreatePanelSandboxPage = new InstanceCreatePanelSandboxPage();

  var instanceCreatePanel;
  beforeEach(function () {
    instanceCreatePanelSandboxPage.open();
    instanceCreatePanelSandboxPage.openCreateDialog();
    instanceCreatePanel = instanceCreatePanelSandboxPage.getInstanceCreatePanel();
  });

  it('should have buttons for create and close', function () {
    let createBtn = instanceCreatePanel.getCreateButton();
    let closeBtn = instanceCreatePanel.getCloseButton();
    let createCheckbox = instanceCreatePanel.getCreateCheckbox();

    expect(createBtn.isDisplayed()).to.eventually.be.true;
    expect(closeBtn.isDisplayed()).to.eventually.be.true;
    expect(createCheckbox.isDisplayed()).to.eventually.be.true;
  });

  it('should always enable create button when create checkbox is unchecked', function () {
    instanceCreatePanel.getTypesDropdown().selectOption("Tag");
    expect(instanceCreatePanel.getCreateButton().isEnabled()).to.eventually.be.true;
  });

  it('should enable create button when all mandatory fields are populated when create checkbox is checked', function () {
    instanceCreatePanel.getTypesDropdown().selectOption("Tag");
    instanceCreatePanel.selectCreateAnotherInstance();
    instanceCreatePanel.fillDescription('content');

    let createBtn = instanceCreatePanel.getCreateButton();
    expect(createBtn.isEnabled()).to.eventually.be.true;

    instanceCreatePanel.createInstance();
    // should show notification on successful creation
    new Notification().waitUntilOpened();
  });

  it('should auto-select default item and show sub types dropdown', function () {
    expect(instanceCreatePanel.getTypesDropdown().isVisible(TYPES)).to.eventually.be.true;
    expect(instanceCreatePanel.getTypesDropdown().getSelectedLabel()).to.eventually.equal('Document');

    expect(instanceCreatePanel.getSubTypesDropdown().isVisible(SUB_TYPES)).to.eventually.be.true;
    expect(instanceCreatePanel.getSubTypesDropdown().getPlaceholder(SUB_TYPES).isPresent()).to.eventually.be.true;
  });

  it('should not show sub types when the selected top level class has definition', function () {
    instanceCreatePanel.getTypesDropdown().selectOption("Tag");
    expect(instanceCreatePanel.getSubTypesDropdown().isVisible(SUB_TYPES)).to.eventually.be.false;
  });

});
