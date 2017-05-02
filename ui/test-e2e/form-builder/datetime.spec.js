var DatetimeField = require('./form-control.js').DatetimeField;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_DATE_FIELD_WRAPPER = '#datefieldEditable-wrapper';
const EDITABLE_DATETIME_FIELD_WRAPPER = '#datetimefieldEditable-wrapper';
const EDITABLE_DATETIME_FIELD = EDITABLE_DATETIME_FIELD_WRAPPER + ' .datetime-field';

const DISABLED_DATE_FIELD_WRAPPER = '#datefieldDisabled-wrapper';

const PREVIEW_DATE_FIELD_WRAPPER = '#datefieldPreview-wrapper';

const HIDDEN_DATE_FIELD_WRAPPER = '#datefieldHidden-wrapper';

const SYSTEM_DATE_FIELD_WRAPPER = '#datefieldSystem-wrapper';

const EXPECTED_DATE = '22.12.15';
const EXPECTED_DATETIME = '22.12.15 00:00';

const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('Datetime field', () => {

  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/datetime');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('date field when form is in edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        var datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATE);
        datetimeField.clearDateField();
        datetimeField.setTodayDateByPicker();
        expect(datetimeField.getDate(), 'switched date').to.eventually.not.equal(EXPECTED_DATE);
      });

      it('should allow to be cleared by the picker', () => {
        var datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        datetimeField.clearDateByPicker();
        expect(datetimeField.getDate()).to.eventually.equal('');
      });

      it('should not contain a timepicker', () => {
        var datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        expect(datetimeField.hasTimePicker(EDITABLE_DATE_FIELD_WRAPPER)).to.eventually.be.false;
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        var datetimeField = new DatetimeField($(PREVIEW_DATE_FIELD_WRAPPER));
        expect(datetimeField.isPreview()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        var datetimeField = new DatetimeField($(DISABLED_DATE_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATE);
        expect(datetimeField.isDisabled()).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        var datetimeField = new DatetimeField($(HIDDEN_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.false;
        expect(datetimeField.getHiddenPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        var datetimeField = new DatetimeField($(SYSTEM_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.false;
        expect(datetimeField.getHiddenPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    it('should be mandatory', () => {
      expect(new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and has no value', () => {
      var datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
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
        var datetimeField = new DatetimeField($(EDITABLE_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        var datetimeField = new DatetimeField($(PREVIEW_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        var datetimeField = new DatetimeField($(DISABLED_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        var datetimeField = new DatetimeField($(HIDDEN_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.true;
        expect(datetimeField.getHiddenPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        var datetimeField = new DatetimeField($(SYSTEM_DATE_FIELD_WRAPPER));
        expect(datetimeField.isVisible()).to.eventually.be.false;
        expect(datetimeField.getHiddenPreviewValue()).to.eventually.equal(EXPECTED_DATE);
      });
    });
  });

  describe('datetime field when form is in edit mode', () => {

    describe('when displayType=EDITABLE', () => {

      it('should allow to be edited', () => {
        var datetimeField = new DatetimeField($(EDITABLE_DATETIME_FIELD_WRAPPER));
        expect(datetimeField.getDate()).to.eventually.equal(EXPECTED_DATETIME);
        datetimeField.clearDateField();
        datetimeField.setDatetime(EDITABLE_DATETIME_FIELD, '25.12.15 12:30');
        expect(datetimeField.getDate()).to.eventually.equal('25.12.15 12:30');
      });

      it('should have a timepicker', () => {
        // will throw NoSuchElementError if not found
        new DatetimeField($(EDITABLE_DATETIME_FIELD_WRAPPER)).openTimePicker();
      });
    });
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable date').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview date').to.eventually.be.true;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'disabled date').to.eventually.be.true;
      expect(fields.get(3).element(by.css('i')).isDisplayed(), 'hidden date').to.eventually.be.false;
      expect(fields.get(4).element(by.css('i')).isDisplayed(), 'system date').to.eventually.be.false;
      expect(fields.get(5).element(by.css('i')).isDisplayed(), 'editable datetime').to.eventually.be.false;
      expect(fields.get(6).element(by.css('i')).isDisplayed(), 'preview datetime').to.eventually.be.true;
      expect(fields.get(7).element(by.css('i')).isDisplayed(), 'disabled datetime').to.eventually.be.true;
      expect(fields.get(8).element(by.css('i')).isDisplayed(), 'hidden datetime').to.eventually.be.false;
      expect(fields.get(9).element(by.css('i')).isDisplayed(), 'system datetime').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(3).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(4).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(5).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(6).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(7).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(8).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(9).element(by.css('i')).isDisplayed()).to.eventually.be.false;
    });
  });
});
