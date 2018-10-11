'use strict';

let InputField = require('../form-control.js').InputField;
let FormWrapper = require('../form-wrapper').FormWrapper;

const EDITABLE_INPUT = '#emailControlEdit';
const EDITABLE_INPUT_PREVIEW_FIELD = EDITABLE_INPUT + '.preview-field';
const PREVIEW_INPUT = '#emailControlPreview';
const PREVIEW_INPUT_PREVIEW_FIELD = PREVIEW_INPUT + '.preview-field';

describe('EmailControl', () => {

  let inputField;
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    inputField = new InputField();
    browser.get('/sandbox/form-builder/email-control');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('in form edit mode', () => {

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have full email as value', () => {
        expect(element(by.css(EDITABLE_INPUT)).isPresent()).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.equal('project8-tenant-id@sirma.bg');
      });
    });

  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have full email as value', () => {
        expect(inputField.isVisible(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.be.false;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(EDITABLE_INPUT_PREVIEW_FIELD)).to.eventually.equal('project8-tenant-id@sirma.bg');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have full email as value', () => {
        expect(inputField.isVisible(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        formWrapper.togglePreviewMode();
        expect(inputField.isVisible(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.be.true;
        expect(inputField.getText(PREVIEW_INPUT_PREVIEW_FIELD)).to.eventually.equal('project8-tenant-id@sirma.bg');
      });
    });
  });
});
