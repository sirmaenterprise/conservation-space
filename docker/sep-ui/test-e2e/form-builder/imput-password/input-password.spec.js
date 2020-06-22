'use strict';

let InputPassword = require('../form-control.js').InputPassword;
let SandboxPage = require('../../page-object').SandboxPage;

let page = new SandboxPage();

describe('InputPassword', () => {

  let inputPassword;

  beforeEach(() => {
    inputPassword = new InputPassword($('#inputPasswordEdit-wrapper'));
    page.open('/sandbox/form-builder/input-password');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  it('should make password field editable on focus', () => {
    // on first load of the page should be readonly field
    expect(inputPassword.isEditable()).to.eventually.equal('true');
    inputPassword.click();
    expect(inputPassword.isEditable()).to.eventually.equal(null);

    inputPassword.setValue(null, '123456');
    expect(inputPassword.getValue()).to.eventually.equal('123456');
  });

});