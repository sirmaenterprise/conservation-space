"use strict";

const NAVIGATION_SELECTOR = '.nav.nav-left';
const NAVIGATION_TAB_SELECTOR = '.nav.nav-left li';
const EXTENSIONS_SELECTOR = '.extensions';

class ExtensionsPanel {

  constructor(element) {
    if (!element) {
      throw new Error('Picker element must be provided to the constructor!');
    }
    this.element = element;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.element.$(EXTENSIONS_SELECTOR)), DEFAULT_TIMEOUT);
  }

  waitUntilNavigationIsVisible() {
    browser.wait(EC.visibilityOf(this.element.$(NAVIGATION_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getExtensionTabs() {
    this.waitUntilNavigationIsVisible();
    return this.element.all(by.css(NAVIGATION_TAB_SELECTOR));
  }

  getExtensionTab(name) {
    this.waitUntilNavigationIsVisible();
    let extensionTab = this.element.$(`${NAVIGATION_TAB_SELECTOR}.${name}`);
    browser.wait(EC.visibilityOf(extensionTab), DEFAULT_TIMEOUT);
    return extensionTab;
  }

  switchToExtension(name) {
    this.waitUntilNavigationIsVisible();
    this.element.$(`${NAVIGATION_SELECTOR} .${name}`).click();
    return this.getExtension(name);
  }

  isExtensionVisible(name) {
    return this.element.$(`${EXTENSIONS_SELECTOR} .${name}`).isDisplayed();
  }

  getExtension(name) {
    let extension = this.element.$(`${EXTENSIONS_SELECTOR} .${name}`);
    browser.wait(EC.visibilityOf(extension), DEFAULT_TIMEOUT);
    return extension;
  }

  isNavigationPresent() {
    return this.element.$(NAVIGATION_SELECTOR).isPresent();
  }

}

module.exports = ExtensionsPanel;