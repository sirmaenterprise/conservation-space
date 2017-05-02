var Dialog = require('../../components/dialog/dialog');

const DIALOG = '.modal-dialog';

describe('Router', function () {
  var modalDialog;

  beforeEach(function() {
    browser.get('sandbox/adapters/router/#/view1');
    browser.ignoreSynchronization = true;
  });

  afterEach(function() {
    browser.ignoreSynchronization = false;
  });

  it('should allow route configuration using plugin registry', function () {
    $('#second-view-link').click();

    modalDialog = new Dialog($(DIALOG));
    modalDialog.waitUntilOpened();
    expect(modalDialog.isPresent()).to.eventually.be.true;
    modalDialog.getOkButton().click();
    browser.wait(EC.textToBePresentInElement($('#content'), 'This is the second view'), DEFAULT_TIMEOUT);
  });

  it('should allow manual navigation using code', function () {
    $('#second-view-button').click();

    modalDialog = new Dialog($(DIALOG));
    modalDialog.waitUntilOpened();
    expect(modalDialog.isPresent()).to.eventually.be.true;
    modalDialog.getOkButton().click();
    browser.wait(EC.textToBePresentInElement($('#content'), 'This is the second view'), DEFAULT_TIMEOUT);
  });

  it('should not navigate if cancel is clicked', function () {
    $('#second-view-link').click();

    modalDialog = new Dialog($(DIALOG));
    modalDialog.waitUntilOpened();
    expect(modalDialog.isPresent()).to.eventually.be.true;
    modalDialog.getCancelButton().click();
    browser.wait(EC.textToBePresentInElement($('#first-view-content'), 'This is the first view'), DEFAULT_TIMEOUT);
  });

  it('should not show confirmation dialog on navigate if shouldInterrupt=false', function () {
    browser.ignoreSynchronization = false;
    browser.get('sandbox/adapters/router/#/view2');
    $('#first-view-link').click();

    modalDialog = new Dialog($(DIALOG));
    expect(modalDialog.isPresent()).to.eventually.be.false;

    browser.wait(EC.textToBePresentInElement($('#first-view-content'), 'This is the first view'), DEFAULT_TIMEOUT);
  });

});
