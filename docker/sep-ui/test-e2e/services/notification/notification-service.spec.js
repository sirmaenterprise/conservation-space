'use strict';

let SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/services/notification/';
const shortMessage = '.short-';
const longMessage = '.long-';
let button;

describe('Test Notification Service to properly place notification popups', () => {

  let page = new SandboxPage();

  beforeEach(()=> {
    page.open(SANDBOX_URL);
    browser.wait(EC.visibilityOf($('.btn')), DEFAULT_TIMEOUT);
  });

  it('should create a success popup at the bottom', ()=> {
    checkToast(shortMessage + 'success', 'toast-bottom-right', 'toast-success');
  });

  it('should create a success popup at the top', ()=> {
    checkToast(longMessage + 'success', 'toast-top-right', 'toast-success');
  });

  it('should create a success popup at the bottom when the visible message doesn\'t exceed 500 symbols', () => {
    checkToast('.long-html-success', 'toast-bottom-right', 'toast-success');
  });

  it('should create a info popup at the bottom', ()=> {
    checkToast(shortMessage + 'info', 'toast-bottom-right', 'toast-info');
  });

  it('should create a info popup at the top', ()=> {
    checkToast(longMessage + 'info', 'toast-top-right', 'toast-info');
  });

  it('should create a warning popup at the bottom', ()=> {
    checkToast(shortMessage + 'warning', 'toast-bottom-right', 'toast-warning');
  });

  it('should create a warning popup at the top', ()=> {
    checkToast(longMessage + 'warning', 'toast-top-right', 'toast-warning');
  });

  it('should create a error popup at the bottom', ()=> {
    checkToast(shortMessage + 'error', 'toast-bottom-right', 'toast-error');
  });

  it('should create a error popup at the top', ()=> {
    checkToast(longMessage + 'error', 'toast-top-right', 'toast-error');
  });
});

function checkToast(buttonSelector, containerClass, toastClass) {
  button = $(buttonSelector);
  button.click();

  browser.wait(EC.visibilityOf($('#toast-container')), DEFAULT_TIMEOUT);
  expect($('#toast-container').getAttribute('class')).to.eventually.have.string(containerClass);
  expect($('.toast').getAttribute('class')).to.eventually.have.string(toastClass);
}