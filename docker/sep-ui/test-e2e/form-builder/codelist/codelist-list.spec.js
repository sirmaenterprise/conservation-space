'use strict';

let CodelistList = require('../form-control.js').CodelistList;
let FormWrapper = require('../form-wrapper').FormWrapper;

describe('Codelist list', () => {
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    browser.get('/sandbox/form-builder/codelist/codelist-list');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let singleCodelistList = new CodelistList($('#source-wrapper'));
        singleCodelistList.selectValue('ENG');
        singleCodelistList.selectValue('INF');

        let multiCodelistList = new CodelistList($('#multivalueField-wrapper'));
        multiCodelistList.selectValue('OT210027');
        multiCodelistList.selectValue('DT210099');

        expect(singleCodelistList.getSelectedValue()).to.eventually.deep.equal(['INF']);
        expect(multiCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027', 'DT210099']);
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview with the selected value', () => {
        let codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
        expect(codelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and has value', () => {
        let singleCodelistList = new CodelistList($('#singlevalueDisabledField-wrapper'));
        let multiCodelistList = new CodelistList($('#multivalueDisabledField-wrapper'));

        expect(singleCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027']);
        expect(multiCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027']);
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        let codelistList = new CodelistList($('#multivalueHiddenField-wrapper .preview-field'));
        expect(codelistList.isPresent('#multivalueHiddenField-wrapper')).to.eventually.be.false;
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        let codelistList = new CodelistList($('#multivalueSystemField-wrapper .preview-field'));
        expect(codelistList.isPresent('#multivalueSystemField-wrapper')).to.eventually.be.false;
      });
    });
  });

  describe('in form preview mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview with the selected value', () => {
        let singleCodelistList = new CodelistList($('#source-wrapper'));
        singleCodelistList.selectValue('ENG');
        singleCodelistList.selectValue('INF');

        let multiCodelistList = new CodelistList($('#multivalueField-wrapper'));
        multiCodelistList.selectValue('OT210027');
        multiCodelistList.selectValue('DT210099');

        formWrapper.togglePreviewMode();
        expect(singleCodelistList.getPreviewFields().get(1).getText()).to.eventually.equal('Infrastructure department');

        let multiCodelistFields = multiCodelistList.getPreviewFields();
        expect(multiCodelistFields.get(0).getText()).to.eventually.equal('Обикновен документ');
        expect(multiCodelistFields.get(2).getText()).to.eventually.equal('Other');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview with the selected value', () => {
        formWrapper.togglePreviewMode();
        let codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
        expect(codelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        let singleCodelistList = new CodelistList($('#singlevalueDisabledField-wrapper'));
        let multiCodelistList = new CodelistList($('#multivalueDisabledField-wrapper'));

        expect(singleCodelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
        expect(multiCodelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        let codelistList = new CodelistList($('#multivalueHiddenField-wrapper'));
        expect(codelistList.isVisible('#multivalueHiddenField-wrapper')).to.eventually.be.true;
        expect(codelistList.getPreviewFields().get(1).getText()).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should not be present', () => {
        formWrapper.togglePreviewMode();
        let codelistList = new CodelistList($('#multivalueSystemField-wrapper'));
        expect(codelistList.isPresent('#multivalueSystemField-wrapper')).to.eventually.be.false;
      });
    });
  });

  describe('in form print mode', ()=> {
    it('should switch to print', ()=> {
      element(by.id('print-mode')).click();
      let codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
      expect(codelistList.getPrintFields().count()).to.eventually.equal(3);
    });
  });

  it('should change available values of related codelist field', () => {
    let sourceCodelistList = new CodelistList($('#source-wrapper'));
    let targetCodelistList = new CodelistList($('#target-wrapper div.print-field'));

    sourceCodelistList.selectValue('ENG');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['MDG']);

    sourceCodelistList.selectValue('INF');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['EDG']);

    sourceCodelistList.selectValue('TSD');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['MDG', 'EDG']);
  });
});