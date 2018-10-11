var PollingUtilsSandboxPage = require('./polling-utils').PollingUtilsSandboxPage;

describe('PollingUtils', function () {
  var page = new PollingUtilsSandboxPage();

  beforeEach(function () {
    page.open();
  });

  it.skip('should execute a polling task', () => {
    page.startPolling();
    browser.wait(EC.textToBePresentInElement(page.getCount(), '5'), DEFAULT_TIMEOUT);
  });

  it.skip('should stop polling task execution when the browser loses focus', () => {
    page.startPolling();
    browser.sleep(200);

    // Polling will stop when alert is open
    page.showPopup();
    browser.wait(EC.alertIsPresent(), DEFAULT_TIMEOUT);
    var alertDialog = browser.switchTo().alert();
    expect(alertDialog.getText()).to.eventually.be.lte(3);
    browser.sleep(1000);

    // Polling will continue to run when alert is closed
    alertDialog.accept();
    browser.wait(EC.textToBePresentInElement(page.getCount(), '5'), DEFAULT_TIMEOUT);
  });
});