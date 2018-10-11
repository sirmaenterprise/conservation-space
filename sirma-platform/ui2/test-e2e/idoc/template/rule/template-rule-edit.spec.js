"use strict";
let TemplateRuleEditSandboxPage = require('./template-rule-edit').TemplateRuleEditSandboxPage;

describe('TemplateRuleEditDialog', function() {

  let page = new TemplateRuleEditSandboxPage();

  beforeEach(function() {
    page.open();
  });

  it('should allow adding of new rule with boolean and codelist properties', function() {
    // Given I have opened a template rule edit dialog without having an existing rule
    let dialog = page.openForNewRule();

    let form = dialog.getForm();

    // When I enter values in the codelist field
    form.getCodelistField('department', true).selectFromMenuByIndex(1);
    form.getCodelistField('department', true).selectFromMenuByIndex(2);

    // And select a boolean property
    form.getCheckboxField('primary').toggleCheckbox();

    // And save the dialog
    dialog.ok();
    dialog.waitUntilClosed();

    // Then I should see a success notification
    dialog.getNotification().waitUntilOpened();

    // And the new rule should be saved
    browser.executeScript('return window.savedRules').then(function (data) {
      expect(data).to.eql({
        id: 'emf:template',
        rule: '(department == "ENG" || department == "INF") && primary == true'
      });
    });
  });

  it('should allow editing of existing rule', function() {
    // Given I have opened a template rule edit dialog passing an existing rule
    let dialog = page.openForExistingRule();

    let form = dialog.getForm();

    // I should see the form prepopulated with the rule data
    expect(form.getCodelistField('department', true).getSelectedValue()).to.eventually.eql(['ENG', 'INF']);

    expect(form.getCheckboxField('primary').isChecked()).to.eventually.equal('true');

    dialog.ok();
    dialog.waitUntilClosed();
  });

  it('should display a warning if there are no suitable fields for creating a rule', function() {
    // Given I have opened a template rule for definition that has only non-mandatory fields
    let dialog = page.openForDefinitionWithNoEligibleFields();

    // I should see a warning message
    browser.wait(EC.visibilityOf(dialog.getMessage()), DEFAULT_TIMEOUT);

    // And the Ok button should be disabled
    browser.wait(EC.not(EC.elementToBeClickable(dialog.getOkButton())),DEFAULT_TIMEOUT);

    dialog.cancel();
    dialog.waitUntilClosed();
  });

  it('should display a warning if when editing a primary template and there are existing secondary templates for the same template classifier', function() {
    // Given I have opened a template rule for definition that has only non-mandatory fields
    let dialog = page.openForPrimaryTemplateWithSecondaryTemplates();

    // I should see a warning message
    browser.wait(EC.visibilityOf(dialog.getMessage()), DEFAULT_TIMEOUT);

    let form = dialog.getForm();

    // And I should see the form prepopulated with the rule data
    expect(form.getCodelistField('department', true).getSelectedValue()).to.eventually.eql(['ENG', 'INF']);

    // And the Ok button should be disabled
    browser.wait(EC.not(EC.elementToBeClickable(dialog.getOkButton())),DEFAULT_TIMEOUT);

    dialog.cancel();
    dialog.waitUntilClosed();
  });

});