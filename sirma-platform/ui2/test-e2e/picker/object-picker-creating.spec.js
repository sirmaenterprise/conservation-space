var ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;
var CheckboxField = require('../form-builder/form-control').CheckboxField;
var InstanceCreatePanel = require('../create/instance-create-panel').InstanceCreatePanel;
var CREATE_EXTENSION = require('./object-picker').CREATE_EXTENSION;

describe('Creating in Object picker', () => {

  var picker;
  var createTab;
  var instanceCreatePanel;

  var page = new ObjectPickerSandbox();

  beforeEach(() => {
    page.open();
    picker = page.getEmbeddedPicker();

    createTab = picker.getExtensionTab(CREATE_EXTENSION);
    createTab.click();
    var createTabContent = picker.getExtension(CREATE_EXTENSION);
    instanceCreatePanel = new InstanceCreatePanel(createTabContent);
  });

  it('should add created instance into the basket', () => {
    instanceCreatePanel.getSubTypesDropdown().selectOption('Approval document');
    // getForm forces a wait until all fields are rendered fully
    instanceCreatePanel.getForm();
    instanceCreatePanel.createInstance();
    picker.waitForBasketCount(1);
  });

  it('should display a create tab', () => {
    expect(createTab.isDisplayed()).to.eventually.be.true;
  });

  it('should have the create button visible and disabled', () => {
    expect(instanceCreatePanel.isCreateButtonDisplayed()).to.eventually.be.true;
    expect(instanceCreatePanel.isCreateButtonDisabled()).to.eventually.equal('true');
  });

  it('should have the cancel button hidden', () => {
    expect(instanceCreatePanel.getCloseButton().isPresent()).to.eventually.be.false;
  });
});