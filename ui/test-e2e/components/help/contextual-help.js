'use strict';

var PageObject = require('../../page-object.js').PageObject;
var SandboxPage = require('../../page-object.js').SandboxPage;

const SANDBOX_URL = '/sandbox/components/help/contextual/';
const EXISTING_HELP_SELECTOR = '#existing_help';
const MISSING_HELP_SELECTOR = '#missing_help';

/**
 * Utility class for interacting with the contextual help sandbox page - opening & fetching elements. Reflects what
 * is available in the page.
 *
 * @author Mihail Radkov
 */
class ContextualHelpSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.existingHelpWrapper), DEFAULT_TIMEOUT);
  }

  getExistingContextualHelp() {
    var existingElement = this.existingHelpWrapper.$(ContextualHelp.COMPONENT_SELECTOR);
    return new ContextualHelp(existingElement);
  }

  getMissingContextualHelp() {
    var missingElement = this.missingHelpWrapper.$(ContextualHelp.COMPONENT_SELECTOR);
    return new ContextualHelp(missingElement);
  }

  get existingHelpWrapper() {
    return $(EXISTING_HELP_SELECTOR);
  }

  get missingHelpWrapper() {
    return $(MISSING_HELP_SELECTOR);
  }
}

/**
 * Page object for interacting with the contextual help component.
 *
 * @author Mihail Radkov
 */
class ContextualHelp extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  isRendered() {
    return this.helpButton.isPresent();
  }

  openHelp() {
    return this.helpButton.click();
  }

  get helpButton() {
    return this.element.$('i');
  }
}
ContextualHelp.COMPONENT_SELECTOR = '.contextual-help';

module.exports = {
  ContextualHelpSandboxPage,
  ContextualHelp
};