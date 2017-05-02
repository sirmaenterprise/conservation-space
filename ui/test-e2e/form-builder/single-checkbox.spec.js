var CheckboxField = require('./form-control.js').CheckboxField;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_CHECKBOX = '#singleCheckboxEdit-wrapper';
const EDITABLE_CHECKBOX_PREVIEW_WRAPPER = EDITABLE_CHECKBOX + ' label.preview-wrapper';
const DISABLED_CHECKBOX = '#singleCheckboxDisabled-wrapper';
const DISABLED_CHECKBOX_PREVIEW_WRAPPER = DISABLED_CHECKBOX + ' label.preview-wrapper';
const HIDDEN_CHECKBOX = '#singleCheckboxHidden-wrapper';
const HIDDEN_CHECKBOX_PREVIEW_WRAPPER = HIDDEN_CHECKBOX + ' label.preview-wrapper';
const PREVIEW_CHECKBOX = '#singleCheckboxPreview-wrapper';
const PREVIEW_CHECKBOX_PREVIEW_WRAPPER = PREVIEW_CHECKBOX + ' label.preview-wrapper';
const SYSTEM_CHECKBOX = '#singleCheckboxSystem-wrapper';
const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('Single checkbox field', () => {

  var formWrapper;
  var editable;
  var disabled;
  var preview;
  var hidden;
  var system;

  beforeEach(() => {
    page.open('/sandbox/form-builder/single-checkbox');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
    formWrapper = new FormWrapper($('.container'));
    editable = new CheckboxField($(EDITABLE_CHECKBOX));
    disabled = new CheckboxField($(DISABLED_CHECKBOX));
    preview = new CheckboxField($(PREVIEW_CHECKBOX));
    hidden = new CheckboxField($(HIDDEN_CHECKBOX));
    system = new CheckboxField($(SYSTEM_CHECKBOX));
  });

  describe('tooltips', ()=> {
    it('should be visualized properly depending on mode and content', ()=> {
      browser.wait(EC.visibilityOf($('.seip-hint')), DEFAULT_TIMEOUT);
      var fields = element.all(by.className(TOOLTIP));

      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable checkbox').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview checkbox with no tooltip text set').to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'disabled checkbox').to.eventually.be.true;
      expect(fields.get(3).element(by.css('i')).isDisplayed(), 'hidden checkbox').to.eventually.be.false;

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
        expect(editable.isChecked()).to.eventually.equal('true');
        editable.toggleCheckbox().then(() => {
          expect(editable.isChecked()).to.eventually.equal(null);
        });
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible, checked and disabled', () => {
        expect(preview.isChecked()).to.eventually.equal('true');
        expect(preview.hasCssClass($(PREVIEW_CHECKBOX_PREVIEW_WRAPPER), 'state-disabled'), 'The checkbox should be disabled').to.eventually.be.true;
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and checked', () => {
        expect(disabled.isChecked()).to.eventually.equal('true');
        expect(disabled.isDisabled()).to.eventually.equal('true');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        expect(hidden.isVisible()).to.eventually.be.false;
        expect(hidden.isChecked()).to.eventually.equal('true');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        expect(system.isVisible()).to.eventually.be.false;
        expect(system.isChecked()).to.eventually.equal('true');
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and checked', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(editable.isChecked()).to.eventually.equal('true');
        expect(editable.hasCssClass($(EDITABLE_CHECKBOX_PREVIEW_WRAPPER), 'state-disabled')).to.eventually.be.true;
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(preview.isChecked()).to.eventually.equal('true');
        expect(preview.hasCssClass($(PREVIEW_CHECKBOX_PREVIEW_WRAPPER), 'state-disabled')).to.eventually.be.true;
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(disabled.isChecked()).to.eventually.equal('true');
        expect(disabled.hasCssClass($(DISABLED_CHECKBOX_PREVIEW_WRAPPER), 'state-disabled')).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(hidden.isChecked()).to.eventually.equal('true');
        expect(hidden.hasCssClass($(HIDDEN_CHECKBOX_PREVIEW_WRAPPER), 'state-disabled')).to.eventually.be.true;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(system.isVisible()).to.eventually.be.false;
        expect(system.isChecked()).to.eventually.equal('true');
      });
    });
  });
});
