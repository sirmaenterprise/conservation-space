'use strict';

let ChangeTypeActionSandboxPage = require('./change-type-action-sandbox-page').ChangeTypeActionSandboxPage;
let IdocPage = require('../../idoc-page').IdocPage;

// Testing change type action UI behavior. Only change type dialog elements are verified without being strict about
// actual data in type and subtype menus, context and template selector. Also loaded idoc after operation is not verified
// as in order to do that, backend behavior needs to be stubbed properly in the sandbox.

describe('ChangeTypeAction', () => {

  let changeTypeActionSandboxPage;
  let idocPage;

  beforeEach(() => {
    changeTypeActionSandboxPage = new ChangeTypeActionSandboxPage();
    idocPage = new IdocPage();
  });

  it('should open change type dialog', () => {
    // Given I have created an object
    // When I open the object in preview mode
    idocPage.open(false, 'emf:345678');

    // When I execute changeType action
    let changeTypeDialog = executeChangeType();

    // Then I expect a change type dialog to be opened
    expect(changeTypeDialog.getDialogTitle()).to.eventually.equal('Change Type Dialog');

    // And I expect to see the context selector with parent object selected
    let contextSelector = changeTypeDialog.getContextSelector();
    expect(contextSelector.getContextPathText()).to.eventually.equal('Header-emf:123456');
    expect(contextSelector.isClearContextButtonVisible(), 'Clear context button should be visible!').to.be.true;
    expect(contextSelector.isSelectContextButtonVisible(), 'Select context button should be visible!').to.be.true;

    // And I expect to see a Change type button which is disabled
    expect(changeTypeDialog.isChangeTypeButtonDisabled(), 'Change type button should be disabled!').to.eventually.be.true;

    // And I expect to see a Cancel button which is enabled
    expect(changeTypeDialog.isCancelButtonClickable(), 'Cancel button should be clickable!').to.be.true;

    // And I expect to see the types dropdown preselected with the current object type
    let typesDropdown = changeTypeDialog.getTypesDropdown();
    expect(typesDropdown.getSelectedLabel()).to.eventually.equal('Document');

    // And I expect to see the subtype dropdown which should not have selection
    let subtypesDropdown = changeTypeDialog.getSubTypesDropdown();
    expect(subtypesDropdown.getSelectedLabel()).to.eventually.equal('');
  });

  it('should allow type change and render the form and a template selector', () => {
    // Given I have created an object
    // And I have opened the object in preview mode
    idocPage.open(false, 'emf:345678');

    // And I have executed the change type action
    let changeTypeDialog = executeChangeType();

    // When I change the type
    let typesDropdown = changeTypeDialog.getTypesDropdown();
    typesDropdown.selectOption('Issue');

    // Then I expect the type to be selected
    expect(typesDropdown.getSelectedLabel()).to.eventually.equal('Issue');

    // And I expect object details for selected type to be rendered
    let formWrapper = changeTypeDialog.getForm();
    expect(formWrapper.getAllFields().then(fields => {
      return fields.length;
    })).to.eventually.equal(1);
    let descriptionField = formWrapper.getInputText('description');
    expect(descriptionField.isMandatory()).to.eventually.be.true;

    // And I expect to see a select template dropdown
    let templateSelector = changeTypeDialog.getTemplateSelector();
    expect(templateSelector.getSelectedLabel()).to.eventually.equal('Blank');

    // When I fill in the mandatory fields
    descriptionField.setValue(null, 'description');

    // Then I expect Change type button to be enabled
    expect(changeTypeDialog.isChangeTypeButtonDisabled()).to.eventually.be.false;

    // When I select cancel
    // Then I expect the dialog to be closed
    changeTypeDialog.cancel();

    // When I execute the change type action again
    executeChangeType();

    // Then I expect the dialog to be opened again and dropdowns to be reset
    expect(typesDropdown.getSelectedLabel()).to.eventually.equal('Document');
    let subtypesDropdown = changeTypeDialog.getSubTypesDropdown();
    expect(subtypesDropdown.getSelectedLabel()).to.eventually.equal('');

    // When I change the subtype
    subtypesDropdown.selectOption('Recommendations for deployment');

    // Then I expect to see the selected object details
    expect(formWrapper.getAllFields().then(fields => {
      return fields.length;
    })).to.eventually.equal(3);

    // And I expect to see a select template dropdown
    templateSelector = changeTypeDialog.getTemplateSelector();
    expect(templateSelector.getSelectedLabel()).to.eventually.equal('Blank');

    // And I expect Change type button to be disabled because there are empty mandatory fields
    expect(changeTypeDialog.isChangeTypeButtonDisabled()).to.eventually.be.true;

    // When I fill in the mandatory fields
    descriptionField.setValue(null, 'description');
    let nameField = formWrapper.getInputText('name');
    nameField.setValue(null, 'name');

    // Then I expect Change type button to be enabled
    expect(changeTypeDialog.isChangeTypeButtonDisabled()).to.eventually.be.false;

    // When I select Change type button
    // Then I expect a confirmation dialog to be opened
    let confirmation = changeTypeDialog.changeType();

    // When I select cancel in confirmation
    confirmation.cancel();

    // Then I expect change type dialog to be opened
    expect(changeTypeDialog.getDialogTitle()).to.eventually.equal('Change Type Dialog');

    // When I select change type button and confirm operation
    // Then I expect confirmation to be closed
    confirmation = changeTypeDialog.changeType();
    let notification = confirmation.confirm();

    // And I expect a success notification to be displayed
    expect(notification.isSuccess()).to.eventually.be.true;

    // And I expect idoc page in preview mode to be loaded
    idocPage.waitForPreviewMode();
  });

  function executeChangeType() {
    let actionsToolbar = idocPage.getActionsToolbar();
    let actionsMenu = actionsToolbar.getActionsMenu();
    actionsMenu.executeAction('.changeType');
    return changeTypeActionSandboxPage.getChangeTypeDialog();
  }

});