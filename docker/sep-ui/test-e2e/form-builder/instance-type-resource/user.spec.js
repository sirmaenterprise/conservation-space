'use strict';

let UserField = require('../form-control.js').User;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const HIDDEN_USER = '#userHidden';
const PREVIEW_USER = '#userPreview';
const PREVIEW_EMPTY_USER = '#userPreviewEmpty';
const TOOLTIP = 'seip-hint';

let page = new SandboxPage();

describe('User control', ()=> {
  let userField, userPreviewField;
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    userField = new UserField();
    userPreviewField = new UserField($(PREVIEW_USER));
    page.open('/sandbox/form-builder/user');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      let fields = element.all(by.className(TOOLTIP));
      expect($(`${PREVIEW_USER}-wrapper i`).isDisplayed(), 'user information edit').to.eventually.be.true;
      expect($(`${PREVIEW_EMPTY_USER}-wrapper i`).isDisplayed(), 'preview empty edit').to.eventually.be.false;
      expect($(`${HIDDEN_USER}-wrapper i`).isPresent(), 'hidden edit').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect($(`${PREVIEW_USER}-wrapper i`).isDisplayed(), 'user information preview').to.eventually.be.false;
      expect($(`${PREVIEW_EMPTY_USER}-wrapper i`).isPresent(), 'preview empty preview').to.eventually.be.false;
      expect($(`${HIDDEN_USER}-wrapper i`).isDisplayed(), 'hidden preview').to.eventually.be.false;
    });
  });

  describe('in preview mode', ()=> {
    it('should display value correctly', ()=> {
      formWrapper.togglePreviewMode();

      expect(userField.isPresent(TOOLTIP)).to.eventually.be.false;
      expect(userPreviewField.isHeaderPresent()).to.eventually.be.true;
      expect(userPreviewField.getHeaderText()).to.eventually.equal('Header-1');
    });
  });

  describe('Field value is available in the DOM if field is with displayType=hidden', ()=> {
    it('Hidden user field should not be present', ()=> {
      expect(userField.isPresent(HIDDEN_USER)).to.eventually.be.false;
    });

    it('should be displayed when preview is toggled', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isPresent(HIDDEN_USER)).to.eventually.be.true;
    });
  });

  describe('Checking behaviour when previewEmpty value is switched to true/false', ()=> {
    it('Preview empty field should not be present when the preview is toggled.', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isPresent(PREVIEW_EMPTY_USER)).to.eventually.be.false;
    });

    it('Field should be visible when preview is toggled.', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isVisible(PREVIEW_USER)).to.eventually.be.true;
    });
  });
});