'use strict';

let SingleSelectMenu = require('../form-control.js').SingleSelectMenu;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;

const EDITABLE_SINGLE_SELECT = '#singleSelectEdit';
const EDITABLE_SINGLE_SELECT_EDIT_FIELD = EDITABLE_SINGLE_SELECT+ '.edit-field';
const EDITABLE_SINGLE_SELECT_PREVIEW_FIELD = EDITABLE_SINGLE_SELECT + '.preview-field';
const EDITABLE_SINGLE_SELECT_WRAPPER = '#singleSelectEdit-wrapper';
const PREVIEW_SINGLE_SELECT = '#singleSelectPreview';
const PREVIEW_SINGLE_SELECT_PREVIEW_FIELD = PREVIEW_SINGLE_SELECT + '.preview-field';
const PREVIEW_SINGLE_SELECT_WRAPPER = '#singleSelectPreview-wrapper';
const DISABLED_SINGLE_SELECT = '#singleSelectDisabled';
const DISABLED_SINGLE_SELECT_PREVIEW_FIELD = DISABLED_SINGLE_SELECT + '.preview-field';
const DISABLED_SINGLE_SELECT_WRAPPER = '#singleSelectDisabled-wrapper';
const HIDDEN_SINGLE_SELECT = '#singleSelectHidden';
const HIDDEN_SINGLE_SELECT_PREVIEW_FIELD = HIDDEN_SINGLE_SELECT + '.preview-field';
const HIDDEN_SINGLE_SELECT_WRAPPER = '#singleSelectHidden-wrapper';
const SYSTEM_SINGLE_SELECT = '#singleSelectSystem';
const SYSTEM_SINGLE_SELECT_PREVIEW_FIELD = SYSTEM_SINGLE_SELECT + '.preview-field';
const SYSTEM_SINGLE_SELECT_WRAPPER = '#singleSelectSystem-wrapper';

const LINKED_FIELDS_1_WRAPPER = '#linkedFields1-wrapper';
const LINKED_FIELDS_2_WRAPPER = '#linkedFields2-wrapper';
const EDITABLE_FIELD_1_WRAPPER = '#editableField1-wrapper';

const URL = '/sandbox/form-builder/single-select';
let page = new SandboxPage();

describe('Single select', () => {

  let singleSelect;
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    singleSelect = new SingleSelectMenu();
    page.open(URL);
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      expect($(`${EDITABLE_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'editable single-select').to.eventually.be.true;
      expect($(`${PREVIEW_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'preview single-select').to.eventually.be.true;
      expect($(`${DISABLED_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'disabled single-select').to.eventually.be.true;
      expect($(`${HIDDEN_SINGLE_SELECT_WRAPPER} i`).isPresent(), 'hidden single-select').to.eventually.be.false;
      expect($(`${SYSTEM_SINGLE_SELECT_WRAPPER} i`).isPresent(), 'system single-select').to.eventually.be.false;
      expect($(`${LINKED_FIELDS_1_WRAPPER} ${EDITABLE_FIELD_1_WRAPPER} i`).isDisplayed(), 'linked single select #1').to.eventually.be.false;
      expect($(`${LINKED_FIELDS_2_WRAPPER} ${EDITABLE_FIELD_1_WRAPPER} i`).isDisplayed(), 'linked single select #2').to.eventually.be.false;

      page.open(URL, 'mode=PREVIEW');
      expect($(`${EDITABLE_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'editable single-select in preview').to.eventually.be.false;
      expect($(`${PREVIEW_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'preview single-select in preview').to.eventually.be.false;
      expect($(`${DISABLED_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'disabled single-select in preview').to.eventually.be.false;
      expect($(`${HIDDEN_SINGLE_SELECT_WRAPPER} i`).isDisplayed(), 'hidden single-select in preview').to.eventually.be.false;
      expect($(`${SYSTEM_SINGLE_SELECT_WRAPPER} i`).isPresent(), 'system single-select in preview').to.eventually.be.false;
      expect($(`${LINKED_FIELDS_1_WRAPPER} ${EDITABLE_FIELD_1_WRAPPER} i`).isDisplayed(), 'linked single select #1 in preview').to.eventually.be.false;
      expect($(`${LINKED_FIELDS_2_WRAPPER} ${EDITABLE_FIELD_1_WRAPPER} i`).isDisplayed(), 'linked single select #2 in preview').to.eventually.be.false;
    });
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        expect(singleSelect.getSelectedValue(EDITABLE_SINGLE_SELECT_WRAPPER)).to.eventually.equal('CH210001');
        singleSelect.selectFromMenu(EDITABLE_SINGLE_SELECT, 3, true).then(() => {
          expect(singleSelect.getText(EDITABLE_SINGLE_SELECT_EDIT_FIELD)).to.eventually.contain('Препоръки за внедряване');
        });
      });

      it('should allow to be cleared', () => {
        singleSelect.clearField(EDITABLE_SINGLE_SELECT_WRAPPER).then(() => {
          expect(singleSelect.getSelectedValue(EDITABLE_SINGLE_SELECT_WRAPPER)).to.eventually.equal(null);
        });
      });
    });

    describe('when displayType=READ_ONLY', () => {
      // TODO: this doesn't work because currently the model contains only the codes and not the labels
      // and we should find out how to get the labels alongside with them in order to properly render in preview
      it('should be visible in preview mode and to have value', () => {
        expect(singleSelect.getText(PREVIEW_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and to have value', () => {
        expect(singleSelect.isDisabled(DISABLED_SINGLE_SELECT)).to.eventually.be.true;
        expect(singleSelect.getSelectedValue(DISABLED_SINGLE_SELECT_WRAPPER)).to.eventually.equal('CH210001');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        expect(singleSelect.isPresent(HIDDEN_SINGLE_SELECT)).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        expect(singleSelect.isPresent(SYSTEM_SINGLE_SELECT)).to.eventually.be.false;
      });
    });

    it('should be mandatory', () => {
      expect(new SingleSelectMenu($(EDITABLE_SINGLE_SELECT_WRAPPER)).isMandatory()).to.eventually.be.true;
    });

    it('should be invalid if is mandatory and no value is selected', () => {
      singleSelect.clearField(EDITABLE_SINGLE_SELECT_WRAPPER).then(() => {
        singleSelect.getMessages(EDITABLE_SINGLE_SELECT_WRAPPER).then((messages) => {
          expect(messages.length).to.equal(1);
        });
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(singleSelect.getText(EDITABLE_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(singleSelect.getText(PREVIEW_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(singleSelect.getText(DISABLED_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        expect(singleSelect.getText(HIDDEN_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        formWrapper.togglePreviewMode();
        expect(singleSelect.isPresent(SYSTEM_SINGLE_SELECT_PREVIEW_FIELD)).to.eventually.be.false;
      });
    });
  });

  it('should update all select fields that are bound to same model value', () => {
    // Given There are two editable select fields bound to one and the same value "CH210001" in validation model
    // When The form is in edit mode
    // Then Both fields should have same value selected "CH210001"
    let select1 = new SingleSelectMenu($('#linkedFields1-wrapper #editableField1'));
    expect(select1.getSelectedValue()).to.eventually.equal('CH210001');
    let select2 = new SingleSelectMenu($('#linkedFields2-wrapper #editableField1'));
    expect(select2.getSelectedValue()).to.eventually.equal('CH210001');

    // When I change the first field value to "DT210099"
    select1.selectFromMenu(undefined, 3, true).then(() => {
      browser.wait(EC.invisibilityOf($('.select2-dropdown')), DEFAULT_TIMEOUT);
      // Then Both fields should have same value selected "DT210099"
      expect(select1.getSelectedValue()).to.eventually.equal('DT210099');
      expect(select2.getSelectedValue()).to.eventually.equal('DT210099');
    });

    // When I remove DT210099 value from the second field
    select2.clearField(undefined, 1, true).then(() => {
      // Then Both fields should be empty
      expect(select1.getSelectedValue()).to.eventually.equal(null);
      expect(select2.getSelectedValue()).to.eventually.equal(null);
      // And Both fields should be mandatory
      expect(new SingleSelectMenu($('#linkedFields1-wrapper #editableField1-wrapper')).isMandatory()).to.eventually.be.true;
      expect(new SingleSelectMenu($('#linkedFields2-wrapper #editableField1-wrapper')).isMandatory()).to.eventually.be.true;
    });
  });
});
