'use strict';

/**
 * Base class for page objects enforcing implementations to define a waitUntilOpened function and to provide a
 * wrapper element.
 *
 * Implementations should take care that the provided wrapper element is not going to be a stale reference during
 * the page object lifespan.
 *
 * @author Mihail Radkov
 */
class PageObject {

  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without wrapper element!');
    }
    this.element = element;

    if (typeof this.waitUntilOpened !== 'function') {
      throw new Error('Must override waitUntilOpened function!');
    }
    this.waitUntilOpened();
  }
}

class SandboxPage {

  open(url, hash) {
    if (this.lastUrl && this.lastUrl === url) {
      // the new hash has to be applied before the sandbox restart to be taken into account
      this.applyHash(hash);
      browser.executeScript('window.restartSandbox()');
    } else {
      this.lastUrl = url;
      var actualUrl = url;
      if (hash) {
        actualUrl += '#/' + hash;
      }
      browser.get(actualUrl);
    }
  }

  applyHash(hash) {
    if (hash) {
      browser.setLocation('/' + hash);
    } else {
      browser.setLocation('/');
    }
  }

}

module.exports = {
  SandboxPage,
  PageObject
};