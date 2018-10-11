var InputField = require('./form-control.js').InputField;
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_INPUT = '#inputTextEdit';
const EDITABLE_TEXTAREA = '#textareaEdit';
const EDITABLE_INPUT_WRAPPER = '#inputTextEdit-wrapper';
const EDITABLE_TEXTAREA_WRAPPER = '#textareaEdit-wrapper';
const EDITABLE_DATETIME_WRAPPER = '#datefieldEditable-wrapper';
const EDITABLE_SINGLE_SELECT_WRAPPER = '#singleSelectEdit-wrapper';
const EDITABLE_MULTI_SELECT_WRAPPER = '#multiSelectEdit-wrapper'

var page = new SandboxPage();

describe('Form widget', () => {

  var toggleLabelLeft;
  var toggleLabelAbove;
  var toggleLabelHidden;

  function checkIfLabelIsHidden(selector) {
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
    element(by.id('toggleLabelHide')).click();
    expect(element(by.css(selector)).element(by.css('label')).isDisplayed()).to.eventually.be.false;
  }

  function checkFieldPlaceholder(selector, expectedPlaceholder) {
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
    var inputField = new InputField();
    inputField.clearValue(selector);
    expect(inputField.getAttributeValue(selector, 'placeholder')).to.eventually.equal(expectedPlaceholder);
  }

  describe('given label position is configured [label on left]', () => {
    it('then the widget wrapper should have label-left css marker class', () => {
      page.open('/sandbox/form-builder/single-select');
      toggleLabelLeft = element(by.id('toggleLabelLeft'));
      browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
      toggleLabelLeft.click();
      expect(element(by.css('.form-wrapper .label-left:first-child')).isPresent()).to.eventually.be.true;
    });
  });

  describe('given label position is configured [label above]', () => {
    it('then the widget wrapper should have label-above css marker class', () => {
      page.open('/sandbox/form-builder/single-select');
      toggleLabelAbove = element(by.id('toggleLabelAbove'));
      browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
      toggleLabelAbove.click();
      expect(element(by.css('.form-wrapper .label-above:first-child')).isPresent()).to.eventually.be.true;
    });
  });

  describe('given label position is configured [hidden label]', () => {
    it('then the widget wrapper should have label-hidden css marker class', () => {
      page.open('/sandbox/form-builder/single-select');
      toggleLabelHidden = element(by.id('toggleLabelHide'));
      browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
      toggleLabelHidden.click();
      expect(element(by.css('.form-wrapper .label-hidden:first-child')).isPresent()).to.eventually.be.true;
    });

    it('the single select field label should not be visible', () => {
      page.open('/sandbox/form-builder/single-select');
      checkIfLabelIsHidden(EDITABLE_SINGLE_SELECT_WRAPPER);
    });

    it('the input text field label should not be visible', () => {
      page.open('/sandbox/form-builder/input-text');
      checkIfLabelIsHidden(EDITABLE_INPUT_WRAPPER);
    });

    it('the input text label should be set as placeholder', () => {
      page.open('/sandbox/form-builder/input-text', 'label-position=label-hidden');
      checkFieldPlaceholder(EDITABLE_INPUT, 'Editable input text');
    });

    it('the textarea field label should not be visible', () => {
      page.open('/sandbox/form-builder/textarea');
      checkIfLabelIsHidden(EDITABLE_TEXTAREA_WRAPPER);
    });

    it('the textarea label should be set as placeholder', () => {
      page.open('/sandbox/form-builder/textarea', 'label-position=label-hidden');
      checkFieldPlaceholder(EDITABLE_TEXTAREA, 'Editable textarea');
    });

    it('the datetime field label should not be visible', () => {
      page.open('/sandbox/form-builder/datetime');
      checkIfLabelIsHidden(EDITABLE_DATETIME_WRAPPER);
    });

    it('the multy select field label should not be visible', () => {
      page.open('/sandbox/form-builder/multy-select');
      checkIfLabelIsHidden(EDITABLE_MULTI_SELECT_WRAPPER);
    });

  });
});