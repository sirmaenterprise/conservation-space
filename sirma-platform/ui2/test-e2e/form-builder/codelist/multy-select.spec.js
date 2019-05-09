'use strict';

let MultySelectMenu = require('../form-control.js').MultySelectMenu;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_SELECT = '#multiSelectEdit';
const EDITABLE_SELECT_PREVIEW_FIELD = EDITABLE_SELECT + '.preview-field';
const EDITABLE_SELECT_WRAPPER = '#multiSelectEdit-wrapper';
const PREVIEW_SELECT = '#multiSelectPreview';
const PREVIEW_SELECT_PREVIEW_FIELD = PREVIEW_SELECT + '.preview-field';
const PREVIEW_SELECT_WRAPPER = '#multiSelectPreview-wrapper';
const DISABLED_SELECT = '#multiSelectDisabled';
const DISABLED_SELECT_PREVIEW_FIELD = DISABLED_SELECT + '.preview-field';
const DISABLED_SELECT_WRAPPER = '#multiSelectDisabled-wrapper';
const HIDDEN_SELECT = '#multiSelectHidden';
const HIDDEN_SELECT_PREVIEW_FIELD = HIDDEN_SELECT + '.preview-field';
const HIDDEN_SELECT_WRAPPER = '#multiSelectHidden-wrapper';
const SYSTEM_SELECT = '#multiSelectSystem';
const SYSTEM_SELECT_WRAPPER = '#multiSelectSystem-wrapper';

const LINKED_SELECT_1_WRAPPER = '#linkedSelectMultipleFields1-wrapper';
const LINKED_SELECT_2_WRAPPER = '#linkedSelectMultipleFields2-wrapper';
const EDITABLE_SELECT_1_WRAPPER = '#editableMultiSelect1';

let page = new SandboxPage();

describe('Multy select', () => {

  let multySelect;
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    multySelect = new MultySelectMenu();
    page.open('/sandbox/form-builder/multy-select');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      expect($(`${EDITABLE_SELECT_WRAPPER} i`).isDisplayed(), 'editable multi-select').to.eventually.be.true;
      expect($(`${PREVIEW_SELECT_WRAPPER} i`).isDisplayed(), 'preview multi-select').to.eventually.be.true;
      expect($(`${DISABLED_SELECT_WRAPPER} i`).isDisplayed(), 'disabled multi-select').to.eventually.be.true;
      expect($(`${HIDDEN_SELECT_WRAPPER} i`).isPresent(), 'hidden multi-select').to.eventually.be.false;
      expect($(`${SYSTEM_SELECT_WRAPPER} i`).isPresent(), 'system multi-select').to.eventually.be.false;
      expect($(`${LINKED_SELECT_1_WRAPPER} ${EDITABLE_SELECT_1_WRAPPER} i`).isPresent(), 'linked multi-select #1').to.eventually.be.false;
      expect($(`${LINKED_SELECT_2_WRAPPER} ${EDITABLE_SELECT_1_WRAPPER} i`).isPresent(), 'linked multi-select #2').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect($(`${EDITABLE_SELECT_WRAPPER} i`).isDisplayed(), 'editable multi-select in preview').to.eventually.be.false;
      expect($(`${PREVIEW_SELECT_WRAPPER} i`).isDisplayed(), 'preview multi-select in preview').to.eventually.be.false;
      expect($(`${DISABLED_SELECT_WRAPPER} i`).isDisplayed(), 'disabled multi-select in preview').to.eventually.be.false;
      expect($(`${HIDDEN_SELECT_WRAPPER} i`).isDisplayed(), 'hidden multi-select in preview').to.eventually.be.false;
      expect($(`${SYSTEM_SELECT_WRAPPER} i`).isPresent(), 'system multi-select in preview').to.eventually.be.false;
      expect($(`${LINKED_SELECT_1_WRAPPER} ${EDITABLE_SELECT_1_WRAPPER} i`).isPresent(), 'linked multi-select #1 in preview').to.eventually.be.false;
      expect($(`${LINKED_SELECT_2_WRAPPER} ${EDITABLE_SELECT_1_WRAPPER} i`).isPresent(), 'linked multi-select #2 in preview').to.eventually.be.false;
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        expect(multySelect.getSelectedValue(EDITABLE_SELECT_WRAPPER)).to.eventually.deep.equal(['CH210001']);
        multySelect.selectFromMenu(EDITABLE_SELECT_WRAPPER, 1, true).then(() => {
          browser.wait(EC.invisibilityOf($('.select2-dropdown')), DEFAULT_TIMEOUT);
          expect(multySelect.getSelectedValue(EDITABLE_SELECT_WRAPPER)).to.eventually.deep.equal(['OT210027', 'CH210001']);
        });
      });

      it('should allow to be cleared', () => {
        expect(multySelect.getSelectedValue(EDITABLE_SELECT_WRAPPER)).to.eventually.deep.equal(['CH210001']);
        multySelect.removeFromSelection(EDITABLE_SELECT_WRAPPER, 1, true).then(() => {
          expect(multySelect.getSelectedValue(EDITABLE_SELECT_WRAPPER)).to.eventually.deep.equal([]);
        });
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        expect(multySelect.getText(PREVIEW_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване, Other');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        expect(multySelect.isDisabled(`${DISABLED_SELECT_WRAPPER} ${DISABLED_SELECT}`)).to.eventually.be.true;
        // check also if the button for clear is not visible
        expect(element(by.css(DISABLED_SELECT_WRAPPER + ' .select2 .select2-selection__choice__remove'))
          .isDisplayed()).to.eventually.be.false;
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        expect(multySelect.isPresent(HIDDEN_SELECT)).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        expect(multySelect.isPresent(SYSTEM_SELECT)).to.eventually.be.false;
      });
    });

    it('should be mandatory', () => {
      expect(new MultySelectMenu($(EDITABLE_SELECT_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and no value is selected', () => {
      multySelect.removeFromSelection(EDITABLE_SELECT_WRAPPER, 1, true);
      multySelect.getMessages(EDITABLE_SELECT_WRAPPER).then((messages) => {
        expect(messages.length).to.equal(1);
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(multySelect.getText(EDITABLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(multySelect.getText(PREVIEW_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване, Other');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(multySelect.getText(DISABLED_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(multySelect.getText(HIDDEN_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване, Other');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        formWrapper.togglePreviewMode();
        browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
        expect(multySelect.isPresent(SYSTEM_SELECT)).to.eventually.be.false;
      });
    });
  });

  // FIXME: Find out why this case doesn't work in the sandbox. Once the field has 2 values applied and the form mode is changed to preview, then it become empty.
  //it('should display the value in preview mode when it is changed', () => {
  //  // Given There is a multiselect field1 that vave selected value "CH210001"
  //  let multiselect1 = new MultySelectMenu($('#multiSelectEdit-wrapper #multiSelectEdit'));
  //  // And I have added another value "DT210099"
  //  multiselect1.selectFromMenu(undefined, 3, true).then(() => {
  //    browser.wait(EC.invisibilityOf($('.select2-dropdown')), DEFAULT_TIMEOUT);
  //    expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['CH210001', 'DT210099']);
  //  });
  //  // When I switch to preview mode
  //  formWrapper.togglePreviewMode();
  //  // Then The field1 should have the following values: "CH210001, DT210099"
  //  expect(multiselect1.getText()).to.eventually.equal('Препоръки за внедряване, Other');
  //});

  it('should update all multiselect fields that are bound to same model value', () => {
    // Given There are two editable multiselect fields bound to one and the same value "CH210001" in validation model
    // When The form is in edit mode
    // Then Both fields should have same value selected "CH210001"
    let multiselect1 = new MultySelectMenu($('#linkedSelectMultipleFields1-wrapper #editableMultiSelect1'));
    expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['CH210001']);
    let multiselect2 = new MultySelectMenu($('#linkedSelectMultipleFields2-wrapper #editableMultiSelect1'));
    expect(multiselect2.getSelectedValue()).to.eventually.deep.equal(['CH210001']);

    // When I select another value "DT210099" from the first field
    multiselect1.selectFromMenu(undefined, 3, true).then(() => {
      browser.wait(EC.invisibilityOf($('.select2-dropdown')), DEFAULT_TIMEOUT);
      // Then Both fields should have same value selected "CH210001, DT210099"
      expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['CH210001', 'DT210099']);
      expect(multiselect2.getSelectedValue()).to.eventually.deep.equal(['CH210001', 'DT210099']);
    });

    // When I select another value "OT210027" from the first field
    multiselect1.selectFromMenu(undefined, 1, true).then(() => {
      browser.wait(EC.invisibilityOf($('.select2-dropdown')), DEFAULT_TIMEOUT);
      // Then Both fields should have same value selected "CH210001, DT210099, OT210027"
      expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['OT210027', 'CH210001', 'DT210099']);
      expect(multiselect2.getSelectedValue()).to.eventually.deep.equal(['OT210027', 'CH210001', 'DT210099' ]);
    });

    // When I remove CH210001 value
    multiselect2.removeFromSelection(undefined, 1, true).then(() => {
      // Then Both fields should have same value selected "DT210099, OT210027"
      expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['CH210001', 'DT210099']);
      expect(multiselect2.getSelectedValue()).to.eventually.deep.equal(['CH210001', 'DT210099']);
    });

    // When I remove DT210099 value
    multiselect2.removeFromSelection(undefined, 1, true).then(() => {
      // Then Both fields should have same value selected "OT210027"
      expect(multiselect1.getSelectedValue()).to.eventually.deep.equal(['DT210099']);
      expect(multiselect2.getSelectedValue()).to.eventually.deep.equal(['DT210099']);
    });

    // When I remove OT210027 value
    multiselect2.removeFromSelection(undefined, 1, true).then(() => {
      // Then Both fields should be empty
      expect(multiselect1.getSelectedValue()).to.eventually.deep.equal([]);
      expect(multiselect2.getSelectedValue()).to.eventually.deep.equal([]);
      // And Both fields should be mandatory
      expect(new MultySelectMenu($('#linkedSelectMultipleFields1-wrapper #editableMultiSelect1-wrapper')).isMandatory()).to.eventually.be.true;
      expect(new MultySelectMenu($('#linkedSelectMultipleFields2-wrapper #editableMultiSelect1-wrapper')).isMandatory()).to.eventually.be.true;
    });
  });
});
