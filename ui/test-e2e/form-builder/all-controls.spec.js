var SandboxPage = require('../page-object').SandboxPage;

var page = new SandboxPage();

describe('All controls', () => {
  beforeEach(() => {
    page.open('/sandbox/form-builder/all-controls');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });
  describe('tooltip', () =>{
    it('should be visible in empty form fields as well as in filled', () =>{
      var emptyPreviewFieldHint = element(by.css('.textarea-wrapper.preview-field-wrapper .seip-hint'));
      var filledPreviewFieldHint = element(by.css('.input-text-wrapper.preview-field-wrapper .seip-hint'))
      expect(emptyPreviewFieldHint.isDisplayed()).to.eventually.be.true;
      expect(filledPreviewFieldHint.isDisplayed()).to.eventually.be.true;
    });
  });
})