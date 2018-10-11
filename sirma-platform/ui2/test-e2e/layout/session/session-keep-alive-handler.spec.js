var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('SessionKeepAliveHandler', function() {
  var status;
  var data;

  beforeEach(function() {
    page.open('sandbox/layout/session');
    status = $('.status');
    data = $('.data');

    browser.wait(EC.elementToBeClickable(status), DEFAULT_TIMEOUT);
    browser.wait(EC.elementToBeClickable(data), DEFAULT_TIMEOUT);
  });

  it('should call logout on session timeout', function() {
    browser.wait(EC.textToBePresentInElementValue(status, 'logout-called'), DEFAULT_TIMEOUT);
  });

  it('should ping on user activity', function() {
    data.sendKeys('test');

    browser.wait(EC.textToBePresentInElementValue(data, 'test'), DEFAULT_TIMEOUT);
    browser.wait(EC.textToBePresentInElementValue(status, 'ping'), DEFAULT_TIMEOUT);
    browser.wait(EC.textToBePresentInElementValue(status, 'logout-called'), DEFAULT_TIMEOUT);
  });

  it('should ping when user activity event is fired', function() {
    $('#trigger').click();
    browser.wait(EC.textToBePresentInElementValue(status, 'ping'), DEFAULT_TIMEOUT);
    browser.wait(EC.textToBePresentInElementValue(status, 'logout-called'), DEFAULT_TIMEOUT);
  });
});