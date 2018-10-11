'use strict';

let DatetimeField = require('../form-control.js').DatetimeField;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_DATE_FIELD_WRAPPER = '#datefieldEditable-wrapper';
const DISABLED_DATE_FIELD_WRAPPER = '#datefieldDisabled-wrapper';
const PREVIEW_DATE_FIELD_WRAPPER = '#datefieldPreview-wrapper';
const HIDDEN_DATE_FIELD_WRAPPER = '#datefieldHidden-wrapper';
const SYSTEM_DATE_FIELD_WRAPPER = '#datefieldSystem-wrapper';

const EDITABLE_DATETIME_FIELD_WRAPPER = '#datetimefieldEditable-wrapper';
const EDITABLE_DATETIME_FIELD = EDITABLE_DATETIME_FIELD_WRAPPER + ' .datetime-field';
const DISABLED_DATETIME_FIELD_WRAPPER = '#datetimefieldDisabled-wrapper';
const PREVIEW_DATETIME_FIELD_WRAPPER = '#datetimefieldPreview-wrapper';
const HIDDEN_DATETIME_FIELD_WRAPPER = '#datetimefieldHidden-wrapper';
const SYSTEM_DATETIME_FIELD_WRAPPER = '#datetimefieldSystem-wrapper';

const EXPECTED_DATE = '22.12.15';
const EXPECTED_DATETIME = '22.12.15 00:00';

let page = new SandboxPage();

describe('Datetime field', () => {

  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/datetime');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('when form is in edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATE);
        datetimeField.clearDateField();
        datetimeField.setToday();
        expect(datetimeField.getDate(), 'switched date').to.eventually.not.equal(EXPECTED_DATE);
      });

      it('should allow to be cleared', () => {
        let datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        datetimeField.clearDateField();
        expect(datetimeField.getDate()).to.eventually.equal('');
        // And should have error for empty mandatory field.
        expect(datetimeField.hasError()).to.eventually.be.true;
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        let datetimeField = new DatetimeField($(PREVIEW_DATE_FIELD_WRAPPER));
        expect(datetimeField.isPreview()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        let datetimeField = new DatetimeField($(DISABLED_DATE_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATE);
        expect(datetimeField.isDisabled()).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        let datetimeField = new DatetimeField($(HIDDEN_DATE_FIELD_WRAPPER));
        expect(datetimeField.isPresent()).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        let datetimeField = new DatetimeField($(SYSTEM_DATE_FIELD_WRAPPER));
        expect(datetimeField.isPresent()).to.eventually.be.false;
      });
    });

    it('should be mandatory', () => {
      expect(new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      let datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
      datetimeField.clearDateField();
      datetimeField.getMessages(EDITABLE_DATE_FIELD_WRAPPER).then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('date field when form is in preview mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let datetimeField = new DatetimeField($(PREVIEW_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let datetimeField = new DatetimeField($(DISABLED_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let datetimeField = new DatetimeField($(HIDDEN_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getHiddenPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        formWrapper.togglePreviewMode();
        let datetimeField = new DatetimeField($(SYSTEM_DATE_FIELD_WRAPPER));
        expect(datetimeField.isPresent()).to.eventually.be.false;
      });
    });
  });

  describe('datetime field when form is in edit mode', () => {

    describe('when displayType=EDITABLE', () => {

      it('should allow to be edited', () => {
        let datetimeField = new DatetimeField($(EDITABLE_DATETIME_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATETIME);
        datetimeField.clearDateField();
        datetimeField.setDatetime(EDITABLE_DATETIME_FIELD, '25.12.15 12:30');
        expect(datetimeField.getDate()).to.eventually.equal('25.12.15 12:30');
      });

    });
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      expect($(`${EDITABLE_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'editable date').to.eventually.be.true;
      expect($(`${PREVIEW_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'preview date').to.eventually.be.true;
      expect($(`${DISABLED_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'disabled date').to.eventually.be.true;
      expect($(`${HIDDEN_DATE_FIELD_WRAPPER} i`).isPresent(), 'hidden date').to.eventually.be.false;
      expect($(`${SYSTEM_DATE_FIELD_WRAPPER} i`).isPresent(), 'system date').to.eventually.be.false;

      expect($(`${EDITABLE_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'editable datetime').to.eventually.be.false;
      expect($(`${PREVIEW_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'preview datetime').to.eventually.be.true;
      expect($(`${DISABLED_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'disabled datetime').to.eventually.be.true;
      expect($(`${HIDDEN_DATETIME_FIELD_WRAPPER} i`).isPresent(), 'hidden datetime').to.eventually.be.false;
      expect($(`${SYSTEM_DATETIME_FIELD_WRAPPER} i`).isPresent(), 'system datetime').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect($(`${EDITABLE_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'editable date in preview').to.eventually.be.false;
      expect($(`${PREVIEW_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'preview date in preview').to.eventually.be.false;
      expect($(`${DISABLED_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'disabled date in preview').to.eventually.be.false;
      expect($(`${HIDDEN_DATE_FIELD_WRAPPER} i`).isDisplayed(), 'hidden date in preview').to.eventually.be.false;
      expect($(`${SYSTEM_DATE_FIELD_WRAPPER} i`).isPresent(), 'system date in preview').to.eventually.be.false;

      expect($(`${EDITABLE_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'editable datetime in preview').to.eventually.be.false;
      expect($(`${PREVIEW_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'preview datetime in preview').to.eventually.be.false;
      expect($(`${DISABLED_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'disabled datetime in preview').to.eventually.be.false;
      expect($(`${HIDDEN_DATETIME_FIELD_WRAPPER} i`).isDisplayed(), 'hidden datetime in preview').to.eventually.be.false;
      expect($(`${SYSTEM_DATETIME_FIELD_WRAPPER} i`).isPresent(), 'system datetime in preview').to.eventually.be.false;
    });
  });
});
