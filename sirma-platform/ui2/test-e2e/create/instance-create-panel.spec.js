'use strict';

let InstanceCreatePanelSandboxPage = require('./instance-create-panel').InstanceCreatePanelSandboxPage;
let Notification = require('../components/notification').Notification;
let Search = require('../search/components/search.js').Search;
let ObjectPickerDialog = require('../picker/object-picker').ObjectPickerDialog;

const TYPES = '.types';
const SUB_TYPES = '.sub-types';

describe('InstanceCreatePanel', function () {

  let instanceCreatePanelSandboxPage = new InstanceCreatePanelSandboxPage();

  let instanceCreatePanel;
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

  it('should enable create button when all mandatory fields are populated when create checkbox is checked', function () {
    instanceCreatePanel.getTypesDropdown().selectOption('Tag');
    instanceCreatePanel.selectCreateAnotherInstance();
    instanceCreatePanel.fillDescription('content');

    expect(instanceCreatePanel.getCreateButton().isEnabled()).to.eventually.be.true;

    instanceCreatePanel.createInstance(true);
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
    instanceCreatePanel.getTypesDropdown().selectOption('Tag');
    expect(instanceCreatePanel.getSubTypesDropdown().isVisible(SUB_TYPES)).to.eventually.be.false;
  });

  it('should display message if context is invalid', () => {
    instanceCreatePanel.chooseContext(5);
    instanceCreatePanel.chooseInvalidContext();
    let errorMessage = instanceCreatePanel.getContextSelector().getErrorMessage();
    browser.wait(EC.textToBePresentInElement(errorMessage, 'Context is invalid'), DEFAULT_TIMEOUT);
  });

  describe('when there is mandatory object property', () => {

    it('should suggest relations based on context for multy and single valued mandatory fields', () => {
      // Given I've opened the create instance dialog
      // And I've selected document where there is a mandatory object property "references"
      instanceCreatePanel.selectSubType('Pickers document');
      // When The form is rendered
      let form = instanceCreatePanel.getForm();
      let referencesField = form.getObjectControlField('references');
      // Then I expect the "references" field to have the default value plus suggested relations from the context
      expect(referencesField.getSelectedObjectsCount()).to.eventually.equal(3);
      referencesField.isShowAllButtonVisible();
      expect(referencesField.getHiddenObjectsCount()).to.eventually.equal('2');
      referencesField.showAll();
      expect(referencesField.getSelectedObjectsCount()).to.eventually.equal(5);

      // And I expect the Reviewer field  which was initially empty to have single value suggested.
      let reviewerField = form.getObjectControlField('reviewer');
      expect(reviewerField.getSelectedObjectsCount()).to.eventually.equal(1);
      checkObjectHeader(reviewerField, 0, 'Header-2');
      // When I change the Reviewer field value
      toggleObjectSelection(reviewerField, [0]);
      // Then I expect the field to have the new value
      checkObjectHeader(reviewerField, 0, 'Object #1 Breadcrumb header');
    });

    it('should not suggest value for single value field that already has value', () => {
      // Given I've opened the create instance dialog
      // And I've selected document where there is a mandatory object property Supervisor
      instanceCreatePanel.selectSubType('Pickers document');
      // When The form is rendered
      let form = instanceCreatePanel.getForm();
      let supervisorField = form.getObjectControlField('supervisor');
      // Then I expect the Supervisor field to have the default value - no suggestion
      expect(supervisorField.getSelectedObjectsCount()).to.eventually.equal(1);
      checkObjectHeader(supervisorField, 0, 'Header-1');
    });

    // If an object property field is mandatory, then it's a subject to suggestion which loads and populates relations in
    // the field. If a HIDDEN condition is applied to the field, it will clears the field's value. Next if the HIDDEN
    // condition get's deactivated and field becomes visible again, the suggestion should be triggered again and the
    // field should be populated with values again.
    // If the user makes changes in the field, they would be lost after the HIDDEN condition is applied, but the
    // suggestion would still work.
    it('should suggest relations after HIDDEN condition has been deactivated and the fields become visible', () => {
      // Given I have opened instance create dialog
      // And I have selected the Pickers document type
      instanceCreatePanel.selectSubType('Pickers document');
      // When The form is rendered
      let form = instanceCreatePanel.getForm();
      // Then I expect the Documents field to be visible
      let objectControl = form.getObjectControlField('documents');
      // And I expect there to have selected 4 suggested relations
      objectControl.waitForSelectedItems(3);
      expect(objectControl.getHiddenObjectsCount()).to.eventually.equal('1');
      // When I activate condition that hides the Documents field
      let triggerField = form.getCheckboxField('hideDocumentsRelation');
      triggerField.toggleCheckbox();
      // And I deactivate the condition that hides the Documents field
      triggerField.toggleCheckbox();
      // Then I expect the field to have 4 suggested relations
      objectControl.waitForSelectedItems(3);
      expect(objectControl.getHiddenObjectsCount()).to.eventually.equal('1');

      // When I remove two items from the field and add a new one.
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      toggleObjectSelection(objectControl, [0]);
      // Then I expect to have 3 visible items selected in the field.
      objectControl.waitForSelectedItems(3);
      // When I activate a condition that hides the Documents field.
      triggerField.toggleCheckbox();
      // And I deactivate the condition that hides the Documents field.
      triggerField.toggleCheckbox();
      // Then I expect to have suggested the initial values again.
      objectControl.waitForSelectedItems(3);
      expect(objectControl.getHiddenObjectsCount()).to.eventually.equal('1');
      checkObjectHeader(objectControl, 0, 'Header-2');
      checkObjectHeader(objectControl, 1, 'Header-3');
      checkObjectHeader(objectControl, 2, 'Header-4');
    });

    it('should suggest relations when a field is made visible and mandatory by condition', () => {
      // Given I have opened instance create dialog
      // And I have selected the Pickers document type
      instanceCreatePanel.selectSubType('Pickers document');
      // When The form is rendered
      let form = instanceCreatePanel.getForm();
      // Then I expect Depends On field to be hidden
      browser.wait(EC.invisibilityOf($('#dependsOn-wrapper')), DEFAULT_TIMEOUT);
      // When I activate condition which makes the field mandatory and visible
      let triggerField = form.getCheckboxField('hideDocumentsRelation');
      triggerField.toggleCheckbox();
      // Then I expect the field to become visible
      // And I expect there to have 4 suggested relations
      let objectControl = form.getObjectControlField('dependsOn');
      objectControl.waitForSelectedItems(3);
      expect(objectControl.getHiddenObjectsCount()).to.eventually.equal('1');
      // When I deactivate the condition
      triggerField.toggleCheckbox();
      // Then I expect the field to become hidden
      expect(objectControl.isVisible()).to.eventually.be.false;
      // When I activate the condition again
      triggerField.toggleCheckbox();
      // Then I expect the field to become visible
      // And I expect there to have 4 suggested relations
      objectControl.waitForSelectedItems(3);
      expect(objectControl.getHiddenObjectsCount()).to.eventually.equal('1');
    });
  });

});

function checkObjectHeader(objectControl, ind, header) {
  objectControl.getSelectedObjects().then((selectedObjects) => {
    expect(selectedObjects[ind].getHeader()).to.eventually.equal(header);
  });
}

function toggleObjectSelection(objectControl, indexes) {
  objectControl.selectInstance();
  let objectPickerDialog = new ObjectPickerDialog();
  let search = new Search($(Search.COMPONENT_SELECTOR));
  search.getCriteria().getSearchBar().search();
  let results = search.getResults();
  // zero based, so item 2 is the third element in result list
  indexes.forEach((ind) => {
    results.clickResultItem(ind);
  });
  objectPickerDialog.ok();
  objectPickerDialog.waitUntilClosed();
}
