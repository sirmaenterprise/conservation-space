'use strict';

let InputField = require('../form-control.js').InputField;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_INPUT_WRAPPER = '#inputTextEdit-wrapper';
const PREVIEW_INPUT_WRAPPER = '#inputTextPreview-wrapper';
const DISABLED_INPUT_WRAPPER = '#inputTextDisabled-wrapper';
const HIDDEN_INPUT_WRAPPER = '#inputTextHidden-wrapper';
const HIDDEN_INPUT = '#inputTextHidden';
const SYSTEM_INPUT = '#inputTextSystem';
const SYSTEM_INPUT_PREVIEW_FIELD = SYSTEM_INPUT + '.preview-field';

let page = new SandboxPage();

describe('InputText', () => {

  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/input-text');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', () => {
    it('should be displayed correctly in edit mode and hidden in preview mode', () => {
      let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
      let previewField = new InputField($(PREVIEW_INPUT_WRAPPER));
      let disabledField = new InputField($(DISABLED_INPUT_WRAPPER));

      editableField.isTooltipIconVisible();
      previewField.isTooltipIconVisible();
      disabledField.isTooltipIconVisible();

      formWrapper.togglePreviewMode();

      editableField.isTooltipIconHidden();
      previewField.isTooltipIconHidden();
      disabledField.isTooltipIconHidden();
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
        expect(editableField.getValue()).to.eventually.equal('inputTextEdit');
        editableField.clearValue();
        editableField.setValue(null, 'test');
        expect(editableField.getValue()).to.eventually.equal('test');
      });

      it('should trim the value if only spaces are present in the input when its blurred', () => {
        let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
        editableField.clearValue();
        editableField.setValue(null, '                     ');
        editableField.blurField();
        expect(editableField.getValue()).to.eventually.equal('');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        let previewField = new InputField($(PREVIEW_INPUT_WRAPPER));
        expect(previewField.getPreviewElement().isDisplayed()).to.eventually.be.true;
        expect(previewField.getPreviewValue()).to.eventually.equal('inputTextPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        let disabledField = new InputField($(DISABLED_INPUT_WRAPPER));
        expect(disabledField.getValue()).to.eventually.equal('inputTextDisabled');
        expect(disabledField.isDisabled()).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        expect($(HIDDEN_INPUT).isPresent()).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        expect($(SYSTEM_INPUT).isPresent()).to.eventually.be.false;
      });
    });

    it('should be mandatory', () => {
      let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
      expect(editableField.isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
      editableField.clearValue();
      editableField.getMessages().then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        let editableField = new InputField($(EDITABLE_INPUT_WRAPPER));
        expect(editableField.isEditable()).to.eventually.be.true;
        formWrapper.togglePreviewMode();
        expect(editableField.isPreview()).to.eventually.be.true;
        expect(editableField.getPreviewValue()).to.eventually.equal('inputTextEdit');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        let previewField = new InputField($(PREVIEW_INPUT_WRAPPER));
        expect(previewField.isPreview()).to.eventually.be.true;
        formWrapper.togglePreviewMode();
        expect(previewField.isPreview()).to.eventually.be.true;
        expect(previewField.getPreviewValue()).to.eventually.equal('inputTextPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        let disabledField = new InputField($(DISABLED_INPUT_WRAPPER));
        expect(disabledField.getInputElement().isDisplayed()).to.eventually.be.true;
        formWrapper.togglePreviewMode();
        expect(disabledField.isPreview()).to.eventually.be.true;
        expect(disabledField.getPreviewValue()).to.eventually.equal('inputTextDisabled');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        expect($(HIDDEN_INPUT).isPresent()).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        let hiddenField = new InputField($(HIDDEN_INPUT_WRAPPER));
        expect(hiddenField.isPreview()).to.eventually.be.true;
        expect(hiddenField.getPreviewValue()).to.eventually.equal('inputTextHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        expect($(SYSTEM_INPUT_PREVIEW_FIELD).isPresent()).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect($(SYSTEM_INPUT_PREVIEW_FIELD).isPresent()).to.eventually.be.false;
      });
    });
  });
});
