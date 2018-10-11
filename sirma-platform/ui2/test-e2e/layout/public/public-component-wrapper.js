'use strict';

let SANDBOX_URL = '/sandbox/layout/public';
let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;

const MAIN_LOGO_SELECTOR = '.main-logo';
const POWERED_BY_LOGO_SELECTOR = '.powered-by-logo';

class PublicComponentWrapperSandbox extends SandboxPage {

  open(componentId) {
    if (!componentId) {
      componentId = PublicComponentWrapperSandbox.HOME_COMPONENT_ID;
    }

    super.open(SANDBOX_URL, componentId);
    browser.wait(EC.visibilityOf($(PublicComponentWrapper.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getPublicComponentWrapper() {
    let publicComponentWrapper = new PublicComponentWrapper($(PublicComponentWrapper.COMPONENT_SELECTOR));
    publicComponentWrapper.waitUntilOpened();
    return publicComponentWrapper;
  }

}

class PublicComponentWrapper extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    this.wairForVisibilityOfElement(this.element);
  }

  waitForMainLogo() {
    this.wairForVisibilityOfElement(this.element.$(MAIN_LOGO_SELECTOR));
  }

  waitForHomeComponent() {
    this.wairForVisibilityOfElement(this.element.$('.home-component'));
  }

  waitForInfoComponent() {
    this.wairForVisibilityOfElement(this.element.$('.info-component'));
  }

  waitForPoweredByLogo() {
    this.wairForVisibilityOfElement(this.element.$(POWERED_BY_LOGO_SELECTOR));
  }

  wairForVisibilityOfElement(element) {
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
  }

}

PublicComponentWrapper.COMPONENT_SELECTOR = '.public-component-wrapper';

PublicComponentWrapperSandbox.HOME_COMPONENT_ID = '#home';
PublicComponentWrapperSandbox.INFO_COMPONENT_ID = '#info';

module.exports.PublicComponentWrapperSandbox = PublicComponentWrapperSandbox;
module.exports.PublicComponentWrapper = PublicComponentWrapper;