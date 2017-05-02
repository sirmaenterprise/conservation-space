var InputField = require('./form-control.js').InputField;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_INPUT = '#inputTextEdit';
const EDITABLE_INPUT_WRAPPER = '#inputTextEdit-wrapper';
const EDITABLE_INPUT_PREVIEW_FIELD = EDITABLE_INPUT + '.preview-field';
const DISABLED_INPUT = '#inputTextDisabled';
const DISABLED_INPUT_PREVIEW_FIELD = DISABLED_INPUT + '.preview-field';
const HIDDEN_INPUT = '#inputTextHidden';
const HIDDEN_INPUT_PREVIEW_FIELD = HIDDEN_INPUT + '.preview-field';
const PREVIEW_INPUT = '#inputTextPreview';
const PREVIEW_INPUT_PREVIEW_FIELD = PREVIEW_INPUT + '.preview-field';
const SYSTEM_INPUT = '#inputTextSystem';
const SYSTEM_INPUT_PREVIEW_FIELD = SYSTEM_INPUT + '.preview-field';
const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('InputText', () => {

  var inputField;
  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    inputField = new InputField();
    page.open('/sandbox/form-builder/input-text');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', () => {
    it('should be displayed correctly in edit mode and hidden in preview mode', () => {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable input-text').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview input-text').to.eventually.be.true;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'disabled input-text').to.eventually.be.true;
      expect(fields.get(3).element(by.css('i')).isDisplayed(), 'hidden input-text').to.eventually.be.false;
      expect(fields.get(4).element(by.css('i')).isDisplayed(), 'system input-text').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(3).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(4).element(by.css('i')).isDisplayed()).to.eventually.be.false;
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        expect(inputField.getValue(EDITABLE_INPUT)).to.eventually.equal('inputTextEdit');
        inputField.clearValue(EDITABLE_INPUT);
        inputField.setValue(EDITABLE_INPUT, 'test');
        expect(inputField.getValue(EDITABLE_INPUT)).to.eventually.equal('test');
      });

      it('should trim the value if only spaces are present in the input when its blurred', () => {
        inputField.clearValue(EDITABLE_INPUT);
        inputField.setValue(EDITABLE_INPUT,'                     ');
        inputField.blurField(EDITABLE_INPUT);
        expect(inputField.getValue(EDITABLE_INPUT)).to.eventually.equal('');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        expect(element(by.css('span.preview-field')).isPresent()).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        expect(inputField.getValue(DISABLED_INPUT)).to.eventually.equal('inputTextDisabled');
        expect(inputField.isDisabled(DISABLED_INPUT)).to.eventually.equal('true');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        expect(inputField.isVisible(HIDDEN_INPUT)).to.eventually.be.false;
        expect(inputField.getHtml(HIDDEN_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        expect(inputField.isVisible(SYSTEM_INPUT)).to.eventually.be.false;
        expect(inputField.getHtml(SYSTEM_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextSystem');
      });
    });

    it('should be mandatory', () => {
      expect(new InputField($(EDITABLE_INPUT_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      inputField.clearValue(EDITABLE_INPUT);
      inputField.getMessages(EDITABLE_INPUT_WRAPPER).then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        expect(inputField.isVisible(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextEdit');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        expect(inputField.isVisible(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextPreview');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        expect(inputField.isVisible(DISABLED_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(DISABLED_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(DISABLED_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextDisabled');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        expect(inputField.isVisible(HIDDEN_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(HIDDEN_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(HIDDEN_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextHidden');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        expect(inputField.isVisible(SYSTEM_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(SYSTEM_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        expect(inputField.getHtml(SYSTEM_INPUT_PREVIEW_FIELD)).to.eventually.equal('inputTextSystem');
      });
    });
  });
});
