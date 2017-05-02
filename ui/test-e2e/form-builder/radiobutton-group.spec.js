var RadioButtonGroup = require('./form-control.js').RadioButtonGroup;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const EDITABLE_GROUP_WRAPPER = '#radioButtonGroupEditable2-wrapper';
const EDITABLE_GROUP_PREVIEW_FIELD = EDITABLE_GROUP_WRAPPER + ' .preview-field';
const EDITABLE_GROUP_PRINT_FIELD = '#radioButtonGroupEditable2.print-field';
const PREVIEW_GROUP_WRAPPER = '#radioButtonGroupPreview-wrapper';
const PREVIEW_GROUP_PREVIEW_FIELD = PREVIEW_GROUP_WRAPPER + ' .preview-field';
const DISABLED_GROUP_WRAPPER = '#radioButtonGroupDisabled-wrapper';
const DISABLED_GROUP_PREVIEW_FIELD = DISABLED_GROUP_WRAPPER + ' .preview-field';
const HIDDEN_GROUP_WRAPPER = '#radioButtonGroupHidden-wrapper';
const HIDDEN_GROUP_PREVIEW_FIELD = HIDDEN_GROUP_WRAPPER + ' .preview-field';
const SYSTEM_GROUP_WRAPPER = '#radioButtonGroupSystem-wrapper';
const SYSTEM_GROUP_PREVIEW_FIELD = SYSTEM_GROUP_WRAPPER + ' .preview-field';

const SELECTED_OPTION = 'option 1';
const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('Radiobutton group', () => {

  var radiobuttonGroup;
  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    radiobuttonGroup = new RadioButtonGroup();
    page.open('/sandbox/form-builder/radiobutton-group');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'editable radiobutton group').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview radiobutton group').to.eventually.be.true;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'disabled radiobutton group').to.eventually.be.true;
      expect(fields.get(3).element(by.css('i')).isDisplayed(), 'hidden radiobutton group').to.eventually.be.false;

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
        expect(radiobuttonGroup.getSelectedValue(EDITABLE_GROUP_WRAPPER)).to.eventually.equal('COL3');
        radiobuttonGroup.selectValue(EDITABLE_GROUP_WRAPPER, 'COL2');
        expect(radiobuttonGroup.getSelectedValue(EDITABLE_GROUP_WRAPPER)).to.eventually.equal('COL2');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview with the selected value', () => {
        expect(radiobuttonGroup.getHtml(PREVIEW_GROUP_PREVIEW_FIELD)).to.eventually.equal(SELECTED_OPTION);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and has value', () => {
        expect(radiobuttonGroup.getSelectedValue(DISABLED_GROUP_WRAPPER)).to.eventually.equal('COL1');
        expect(radiobuttonGroup.isDisabled(DISABLED_GROUP_WRAPPER)).to.eventually.be.true;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        expect(radiobuttonGroup.isVisible(HIDDEN_GROUP_WRAPPER)).to.eventually.be.false;
        expect(radiobuttonGroup.getHtml(HIDDEN_GROUP_PREVIEW_FIELD)).to.eventually.equal(SELECTED_OPTION);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        expect(radiobuttonGroup.isVisible(SYSTEM_GROUP_WRAPPER)).to.eventually.be.false;
        expect(radiobuttonGroup.getHtml(SYSTEM_GROUP_PREVIEW_FIELD)).to.eventually.equal(SELECTED_OPTION);
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and checked', () => {
        formWrapper.togglePreviewMode();
        expect(radiobuttonGroup.isVisible(EDITABLE_GROUP_WRAPPER)).to.eventually.be.true;
        expect(radiobuttonGroup.getHtml(EDITABLE_GROUP_PREVIEW_FIELD)).to.eventually.equal('option 3');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(radiobuttonGroup.isVisible(PREVIEW_GROUP_WRAPPER)).to.eventually.be.true;
        expect(radiobuttonGroup.getHtml(PREVIEW_GROUP_WRAPPER + ' .preview-field')).to.eventually.equal(SELECTED_OPTION);
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(radiobuttonGroup.isVisible(DISABLED_GROUP_WRAPPER)).to.eventually.be.true;
        expect(radiobuttonGroup.getHtml(DISABLED_GROUP_PREVIEW_FIELD)).to.eventually.equal(SELECTED_OPTION);
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(radiobuttonGroup.isVisible(HIDDEN_GROUP_WRAPPER)).to.eventually.be.true;
        expect(radiobuttonGroup.getHtml(HIDDEN_GROUP_WRAPPER + ' .preview-field')).to.eventually.equal(SELECTED_OPTION);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(radiobuttonGroup.isVisible(SYSTEM_GROUP_WRAPPER)).to.eventually.be.false;
        expect(radiobuttonGroup.getHtml(SYSTEM_GROUP_PREVIEW_FIELD)).to.eventually.equal(SELECTED_OPTION);
      });
    });
  });
  describe('in form print mode', ()=> {
    it('should switch to print', ()=> {
      element(by.id('print-mode')).click();
      expect(radiobuttonGroup.isVisible(EDITABLE_GROUP_PRINT_FIELD)).to.eventually.be.true;
      expect(radiobuttonGroup.getHtml(EDITABLE_GROUP_PRINT_FIELD)).to.eventually.equal('option 3')
    })
  });
});