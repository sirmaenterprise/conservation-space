'use strict';

let PageObject = require('../../page-object').PageObject;
let TestUtils = require('../../test-utils');

class UserAvatar extends PageObject {

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitForDefaultIcon() {
    browser.wait(() => {
      return this.isDefaultIcon();
    }, DEFAULT_TIMEOUT);
  }

  isDefaultIcon() {
    return TestUtils.hasClass(this.element, 'default-avatar');
  }
}

module.exports.UserAvatar = UserAvatar;