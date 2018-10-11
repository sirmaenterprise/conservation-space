'use strict';

let InputField = require('../form-control.js').InputField;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_TEXTAREA_WRAPPER = '#textareaEdit-wrapper';
const DISABLED_TEXTAREA_WRAPPER = '#textareaDisabled-wrapper';
const HIDDEN_TEXTAREA_WRAPPER = '#textareaHidden-wrapper';
const SYSTEM_TEXTAREA_WRAPPER = '#textareaSystem-wrapper';
const PREVIEW_TEXTAREA_WRAPPER = '#textareaPreview-wrapper';
const EDITABLE_TEXTAREA_PREVIEW_FIELD = '#textareaEdit.preview-field';
const DISABLED_TEXTAREA_PREVIEW_FIELD = '#textareaDisabled.preview-field';
const PREVIEW_TEXTAREA_PREVIEW_FIELD = '#textareaPreview.preview-field';
const HIDDEN_TEXTAREA_PREVIEW_FIELD = '#textareaHidden.preview-field';

let page = new SandboxPage();

describe('Textarea', () => {

  let inputField;
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    inputField = new InputField();
    page.open('/sandbox/form-builder/textarea');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      expect($(`${EDITABLE_TEXTAREA_WRAPPER} i`).isDisplayed(), 'editable textarea').to.eventually.be.true;
      expect($(`${PREVIEW_TEXTAREA_WRAPPER} i`).isDisplayed(), 'preview textarea').to.eventually.be.true;
      expect($(`${DISABLED_TEXTAREA_WRAPPER} i`).isDisplayed(), 'disabled textarea').to.eventually.be.true;
      expect($(`${HIDDEN_TEXTAREA_WRAPPER} i`).isPresent(), 'hidden textarea').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect($(`${EDITABLE_TEXTAREA_WRAPPER} i`).isDisplayed(), 'editable textarea in preview').to.eventually.be.false;
      expect($(`${PREVIEW_TEXTAREA_WRAPPER} i`).isDisplayed(), 'preview textarea in preview').to.eventually.be.false;
      expect($(`${DISABLED_TEXTAREA_WRAPPER} i`).isDisplayed(), 'disabled textarea in preview').to.eventually.be.false;
      expect($(`${HIDDEN_TEXTAREA_WRAPPER} i`).isDisplayed(), 'hidden textarea in preview').to.eventually.be.false;
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let textarea = new InputField($(EDITABLE_TEXTAREA_WRAPPER));
        expect(textarea.getValue()).to.eventually.equal('textareaEdit');
        textarea.clearValue();
        textarea.setValue(null, 'test');
        expect(textarea.getValue()).to.eventually.equal('test');
      });

      it('should trim the value if only spaces are present in the input when its blurred', () => {
        let textarea = new InputField($(EDITABLE_TEXTAREA_WRAPPER));
        textarea.clearValue();
        textarea.setValue(null, '                     ');
        textarea.blurField();
        expect(textarea.getValue()).to.eventually.equal('');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        expect(element(by.css('span.preview-field')).isPresent()).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        let textarea = new InputField($(DISABLED_TEXTAREA_WRAPPER));
        expect(textarea.getValue()).to.eventually.equal('textareaDisabled');
        expect(textarea.isDisabled()).to.eventually.to.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        let textarea = new InputField($(HIDDEN_TEXTAREA_WRAPPER));
        expect(textarea.isPresent()).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        let textarea = new InputField($(SYSTEM_TEXTAREA_WRAPPER));
        expect(textarea.isPresent()).to.eventually.be.false;
      });
    });

    it('should be mandatory', () => {
      expect(new InputField($(EDITABLE_TEXTAREA_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      let textarea = new InputField($(EDITABLE_TEXTAREA_WRAPPER));
      textarea.clearValue();
      textarea.getMessages().then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('in form preview mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let textarea = new InputField($(EDITABLE_TEXTAREA_WRAPPER));
        expect(textarea.isVisible()).to.eventually.be.true;
        expect(inputField.getText(EDITABLE_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaEdit');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let textarea = new InputField($(PREVIEW_TEXTAREA_WRAPPER));
        expect(textarea.isVisible()).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let textarea = new InputField($(DISABLED_TEXTAREA_WRAPPER));
        expect(textarea.isVisible()).to.eventually.be.true;
        expect(inputField.getText(DISABLED_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaDisabled');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let textarea = new InputField($(HIDDEN_TEXTAREA_WRAPPER));
        expect(textarea.isVisible()).to.eventually.be.true;
        expect(inputField.getText(HIDDEN_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        formWrapper.togglePreviewMode();
        let textarea = new InputField($(SYSTEM_TEXTAREA_WRAPPER));
        expect(textarea.isPresent()).to.eventually.be.false;
      });
    });
  });
});
