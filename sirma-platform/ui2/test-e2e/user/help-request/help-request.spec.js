let HelpRequestSandbox = require('./help-request').HelpRequestSandbox;

describe('HelpRequest', ()=> {
  let page = new HelpRequestSandbox();

  beforeEach(() => {
    page.open();
    browser.ignoreSynchronization = true;
  });

  afterEach(function() {
    browser.ignoreSynchronization = false;
  });

  it('should open create issue dialog without context', (done) => {
    let dialog = page.openDialog();
    let contextSelector = dialog.getContextSelector();
    let type = dialog.getTypesDropdown();

    expect(contextSelector.getSelectButton().isEnabled()).to.eventually.be.false;
    browser.wait(EC.stalenessOf(contextSelector.getClearContextButton()), DEFAULT_TIMEOUT);
    browser.wait(EC.textToBePresentInElement(contextSelector.getContextPathText(), 'No Context'), DEFAULT_TIMEOUT);

    expect(type.isDisabled()).to.eventually.be.true;
    expect(type.getSelectedLabel()).to.eventually.equal('Issue');

    dialog.setValue('description-wrapper', 'test');
    dialog.create();
    dialog.waitUntillClosed();

    browser.getAllWindowHandles().then(function (handles) {
      browser.switchTo().window(handles[1]).then(function () {
        browser.driver.getCurrentUrl().then(function (url) {
          expect(decodeURIComponent(url).includes('#/idoc/1?mode=edit')).to.be.true;
          done();
        });
      });
    });
  });

});
