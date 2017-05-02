var InputField = require('./form-control.js').InputField;
var FormWrapper = require('./form-wrapper').FormWrapper
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_TEXTAREA = '#textareaEdit';
const EDITABLE_TEXTAREA_EDIT_FIELD = EDITABLE_TEXTAREA + '.edit-field';
const EDITABLE_TEXTAREA_PREVIEW_FIELD = EDITABLE_TEXTAREA + '.preview-field';
const EDITABLE_TEXTAREA_WRAPPER = '#textareaEdit-wrapper';
const DISABLED_TEXTAREA = '#textareaDisabled';
const DISABLED_TEXTAREA_PREVIEW_FIELD = DISABLED_TEXTAREA + '.preview-field';
const PREVIEW_TEXTAREA = '#textareaPreview';
const PREVIEW_TEXTAREA_PREVIEW_FIELD = PREVIEW_TEXTAREA + '.preview-field';
const HIDDEN_TEXTAREA = '#textareaHidden';
const HIDDEN_TEXTAREA_EDIT_FIELD = HIDDEN_TEXTAREA + '.edit-field';
const HIDDEN_TEXTAREA_PREVIEW_FIELD = HIDDEN_TEXTAREA + '.preview-field';
const SYSTEM_TEXTAREA = '#textareaSystem';
const SYSTEM_TEXTAREA_EDIT_FIELD = SYSTEM_TEXTAREA + '.edit-field';
const SYSTEM_TEXTAREA_PREVIEW_FIELD = SYSTEM_TEXTAREA + '.preview-field';

const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('Textarea', () => {

  var inputField;
  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    inputField = new InputField();
    page.open('/sandbox/form-builder/textarea');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable textarea').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview textarea').to.eventually.be.true;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'disabled textarea').to.eventually.be.true;
      expect(fields.get(3).element(by.css('i')).isDisplayed(), 'hidden textarea').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(3).element(by.css('i')).isDisplayed()).to.eventually.be.false;
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        expect(inputField.getValue(EDITABLE_TEXTAREA)).to.eventually.equal('textareaEdit');
        inputField.clearValue(EDITABLE_TEXTAREA);
        inputField.setValue(EDITABLE_TEXTAREA, 'test');
        expect(inputField.getValue(EDITABLE_TEXTAREA)).to.eventually.equal('test');
      });

      it('should trim the value if only spaces are present in the input when its blurred', () => {
        inputField.clearValue(EDITABLE_TEXTAREA);
        inputField.setValue(EDITABLE_TEXTAREA,'                     ');
        inputField.blurField(EDITABLE_TEXTAREA);
        expect(inputField.getValue(EDITABLE_TEXTAREA)).to.eventually.equal('');
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
        expect(inputField.getValue(DISABLED_TEXTAREA)).to.eventually.equal('textareaDisabled');
        expect(inputField.isDisabled(DISABLED_TEXTAREA)).to.eventually.equal('true');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        expect(inputField.isVisible(HIDDEN_TEXTAREA)).to.eventually.be.false;
        expect(inputField.getHtml(HIDDEN_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        expect(inputField.isVisible(SYSTEM_TEXTAREA)).to.eventually.be.false;
        expect(inputField.getHtml(SYSTEM_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaSystem');
      });
    });

    it('should be mandatory', () => {
      expect(new InputField($(EDITABLE_TEXTAREA_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      inputField.clearValue(EDITABLE_TEXTAREA);
      inputField.getMessages(EDITABLE_TEXTAREA_WRAPPER).then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('in form preview mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(EDITABLE_TEXTAREA_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(EDITABLE_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaEdit');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(PREVIEW_TEXTAREA_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(DISABLED_TEXTAREA_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(DISABLED_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaDisabled');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(HIDDEN_TEXTAREA_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(HIDDEN_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(SYSTEM_TEXTAREA)).to.eventually.be.false;
        expect(inputField.getHtml(SYSTEM_TEXTAREA_PREVIEW_FIELD)).to.eventually.equal('textareaSystem');
      });
    });
  });
});
