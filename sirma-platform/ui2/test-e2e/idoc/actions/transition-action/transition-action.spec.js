'use strict';

let TransitionActionSandboxPage = require('./transition-action-sandbox-page').TransitionActionSandboxPage;


describe('TransitionAction', () => {

  let sandboxPage = new TransitionActionSandboxPage();

  beforeEach(function () {
    sandboxPage.open();
  });

  it('should open dialog with mandatory fields for the state only and invalid fields before transition', () => {
    // Given I have an object in preview mode.
    // And The object has an invalid optional field.
    // And The object has 2 fields which are mandatory for the APPROVE state.
    // When I execute Approve action.
    let dialog = sandboxPage.approve();

    // Then I expect a dialog with the mandatory and invalid fields to be opened
    let form = dialog.getForm();
    expect(form.getAllFields().then(fields => {
      return fields.length;
    })).to.eventually.equal(3);
    expect(form.getMandatoryFieldsCount()).to.eventually.equal(2);
    expect(form.getInputText('optionalInvalidField').hasError()).to.eventually.be.true;

    // When I complete the fields.
    let field = form.getInputText('optionalInvalidField');
    field.clearValue();
    field.setValue(null, 'test');

    // And I accept the changes.
    let notification = dialog.accept(true);

    // Then I expect the transition to be executed.
    browser.wait(EC.textToBePresentInElement($('#actionName'), 'approve'), DEFAULT_TIMEOUT);

    // And I expect notification for success to be risen.
    notification.waitUntilOpened();
    notification.isSuccess();
  });

  it('should open dialog with the mandatory fields even the fields are completed and the model is valid', () => {
    // Given I have an object in preview mode.
    // And The object the object model is valid.
    // And The object has 1 field which is mandatory for the RESTARTED state.
    // When I execute Restart action.
    let dialog = sandboxPage.restart();

    // Then I expect a dialog with the mandatory field to be opened
    let form = dialog.getForm();
    expect(form.getAllFields().then(fields => {
      return fields.length;
    })).to.eventually.equal(1);
    expect(form.getMandatoryFieldsCount()).to.eventually.equal(1);
    expect(form.getInputText('mandatoryForState').hasError()).to.eventually.be.false;

    // And I accept.
    let notification = dialog.accept(true);

    // Then I expect the transition to be executed.
    browser.wait(EC.textToBePresentInElement($('#actionName'), 'restart'), DEFAULT_TIMEOUT);

    // And I expect notification for success to be risen.
    notification.waitUntilOpened();
    notification.isSuccess();
  });

  // The reject loads object model without invalid or empty mandatory fields.
  it('should execute transition immediately when no mandatory and invalid fields exist', () => {
    // Given I have an object in preview mode.
    // And The object doesn't have invalid or mandatory fields for the REJECT transition.
    // When I execute Reject action.
    let notification = sandboxPage.reject();

    // Then I expect the action to be executed immediately without any dialog to be opened.
    browser.wait(EC.textToBePresentInElement($('#actionName'), 'reject'), DEFAULT_TIMEOUT);

    // And I expect notification for success to be risen.
    notification.waitUntilOpened();
    notification.isSuccess();
  });

});