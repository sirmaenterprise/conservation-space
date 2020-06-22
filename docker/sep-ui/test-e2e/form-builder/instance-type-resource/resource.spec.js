'use strict';

let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const TOOLTIP = 'seip-hint';

let page = new SandboxPage();

describe('Resource control', ()=> {

  let formWrapper;

  beforeEach(()=> {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/resource');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be visualized properly depending on mode and content', ()=> {
      browser.wait(EC.visibilityOf($(`.${TOOLTIP}`)), DEFAULT_TIMEOUT);
      let fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable date').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview date').to.eventually.be.true;
      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable date').to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview date').to.eventually.be.false;
    });
  })
});