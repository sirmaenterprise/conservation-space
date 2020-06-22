'use strict';

var PageObject = require('../../page-object').PageObject;
var SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/common/polling-utils';


class PollingUtilsSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  startPolling() {
    $('#startPolling').click();
  }

  showPopup() {
    $('#showPopup').click();
  }

  getCount() {
    return $('#count');
  }
}

module.exports = {
  PollingUtilsSandboxPage
};