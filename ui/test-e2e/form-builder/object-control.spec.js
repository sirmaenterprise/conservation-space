var ObjectControl = require('./form-control.js').ObjectControl;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('Object control', () => {
  var objectControl;
  var formWrapper;

  beforeEach(() => {
    objectControl = new ObjectControl();
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/object-control');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(),'single value').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(),'multi-value').to.eventually.be.true;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed()).to.eventually.be.false;
    });
  });
  it('select button should be visible in edit mode', () => {
    expect(objectControl.isPresent('#objectProperty-wrapper .select-instance-btn')).to.eventually.be.true;
  });

  it('remove instance button should be visible in edit mode', () => {
    expect(objectControl.isPresent('#objectProperty-wrapper .remove-instance-btn')).to.eventually.be.true;
  });

  it('select button should not be visible in preview mode', () => {
    formWrapper.togglePreviewMode();
    expect(objectControl.isPresent('#objectProperty-wrapper .select-instance-btn')).to.eventually.be.false;
  });

  it('remove instance button should not be visible in preview mode', () => {
    formWrapper.togglePreviewMode();
    expect(objectControl.isPresent('#objectProperty-wrapper .remove-instance-btn')).to.eventually.be.false;
  });
});
