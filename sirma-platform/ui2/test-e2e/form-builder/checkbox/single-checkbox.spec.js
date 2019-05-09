'use strict';

let CheckboxField = require('../form-control.js').CheckboxField;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_CHECKBOX = '#singleCheckboxEdit-wrapper';
const EDITABLE_CHECKBOX_PREVIEW_WRAPPER = EDITABLE_CHECKBOX + ' label.preview-wrapper';
const DISABLED_CHECKBOX = '#singleCheckboxDisabled-wrapper';
const DISABLED_CHECKBOX_PREVIEW_WRAPPER = DISABLED_CHECKBOX + ' label.preview-wrapper';
const HIDDEN_CHECKBOX = '#singleCheckboxHidden-wrapper';
const HIDDEN_CHECKBOX_PREVIEW_WRAPPER = HIDDEN_CHECKBOX + ' label.preview-wrapper';
const PREVIEW_CHECKBOX = '#singleCheckboxPreview-wrapper';
const PREVIEW_CHECKBOX_PREVIEW_WRAPPER = PREVIEW_CHECKBOX + ' label.preview-wrapper';
const SYSTEM_CHECKBOX = '#singleCheckboxSystem-wrapper';

let page = new SandboxPage();

describe('Single checkbox field', () => {

  let formWrapper;
  let editable;
  let disabled;
  let preview;
  let hidden;
  let system;

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

      expect($(`${EDITABLE_CHECKBOX} i`).isDisplayed(), 'editable checkbox').to.eventually.be.true;
      expect($(`${PREVIEW_CHECKBOX} i`).isDisplayed(), 'preview checkbox with no tooltip text set').to.eventually.be.false;
      expect($(`${DISABLED_CHECKBOX} i`).isDisplayed(), 'disabled checkbox').to.eventually.be.true;
      expect($(`${HIDDEN_CHECKBOX} i`).isPresent(), 'hidden checkbox').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect($(`${EDITABLE_CHECKBOX} i`).isDisplayed(), 'editable checkbox in preview').to.eventually.be.false;
      expect($(`${PREVIEW_CHECKBOX} i`).isDisplayed(), 'preview checkbox with no tooltip text set in preview').to.eventually.be.false;
      expect($(`${DISABLED_CHECKBOX} i`).isDisplayed(), 'disabled checkbox in preview').to.eventually.be.false;
      expect($(`${HIDDEN_CHECKBOX} i`).isDisplayed(), 'hidden checkbox in preview').to.eventually.be.false;
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
        expect(disabled.isDisabled()).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        expect(hidden.isPresent()).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        expect(system.isPresent()).to.eventually.be.false;
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
      it('should not be present', () => {
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(system.isPresent()).to.eventually.be.false;
      });
    });
  });
});
