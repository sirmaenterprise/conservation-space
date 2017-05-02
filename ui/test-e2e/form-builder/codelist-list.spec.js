var CodelistList = require('./form-control.js').CodelistList;
var FormWrapper = require('./form-wrapper').FormWrapper;

describe('Codelist list', () => {
  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    browser.get('/sandbox/form-builder/codelist/codelist-list');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('in form edit mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        var singleCodelistList = new CodelistList($('#source-wrapper .edit-field'));
        singleCodelistList.selectValue('ENG');
        singleCodelistList.selectValue('INF');

        var multiCodelistList = new CodelistList($('#multivalueField-wrapper .edit-field'));
        multiCodelistList.selectValue('OT210027');
        multiCodelistList.selectValue('DT210099');

        expect(singleCodelistList.getSelectedValue()).to.eventually.deep.equal(['INF']);
        expect(multiCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027', 'DT210099']);
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview with the selected value', () => {
        var codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
        expect(codelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be disabled and has value', () => {
        var singleCodelistList = new CodelistList($('#singlevalueDisabledField-wrapper .edit-field'));
        var multiCodelistList = new CodelistList($('#multivalueDisabledField-wrapper .edit-field'));

        expect(singleCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027']);
        expect(multiCodelistList.getSelectedValue()).to.eventually.deep.equal(['OT210027']);
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        var codelistList = new CodelistList($('#multivalueHiddenField-wrapper .preview-field'));
        expect(codelistList.isVisible('#multivalueHiddenField-wrapper')).to.eventually.be.false;
        expect(codelistList.getSelectedValue()).to.eventually.deep.equal(['CH210001']);
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden and to have value', () => {
        var codelistList = new CodelistList($('#multivalueSystemField-wrapper .preview-field'));
        expect(codelistList.isVisible('#multivalueSystemField-wrapper')).to.eventually.be.false;
        expect(codelistList.getSelectedValue()).to.eventually.deep.equal(['CH210001']);
      });
    });
  });

  describe('in form preview mode', () => {

    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview with the selected value', () => {
        var singleCodelistList = new CodelistList($('#source-wrapper'));
        singleCodelistList.selectValue('ENG');
        singleCodelistList.selectValue('INF');

        var multiCodelistList = new CodelistList($('#multivalueField-wrapper'));
        multiCodelistList.selectValue('OT210027');
        multiCodelistList.selectValue('DT210099');

        formWrapper.togglePreviewMode();
        expect(singleCodelistList.getPreviewFields().get(1).getText()).to.eventually.equal('Infrastructure department');

        var multiCodelistFields = multiCodelistList.getPreviewFields();
        expect(multiCodelistFields.get(0).getText()).to.eventually.equal('Обикновен документ');
        expect(multiCodelistFields.get(2).getText()).to.eventually.equal('Other');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview with the selected value', () => {
        formWrapper.togglePreviewMode();
        var codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
        expect(codelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=DISABLED', () => {
      it('should be visible in preview and to have value', () => {
        formWrapper.togglePreviewMode();
        var singleCodelistList = new CodelistList($('#singlevalueDisabledField-wrapper'));
        var multiCodelistList = new CodelistList($('#multivalueDisabledField-wrapper'));

        expect(singleCodelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
        expect(multiCodelistList.getPreviewFields().get(0).getText()).to.eventually.equal('Обикновен документ');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be hidden and to have value', () => {
        formWrapper.togglePreviewMode();
        var codelistList = new CodelistList($('#multivalueHiddenField-wrapper'));
        expect(codelistList.isVisible('#multivalueHiddenField-wrapper')).to.eventually.be.true;
        expect(codelistList.getPreviewFields().get(1).getText()).to.eventually.equal('Препоръки за внедряване');
      });
    });

    describe('when displayType=SYSTEM', () => {
      it('should be hidden', () => {
        formWrapper.togglePreviewMode();
        var codelistList = new CodelistList($('#multivalueSystemField-wrapper'));
        expect(codelistList.isVisible('#multivalueSystemField-wrapper')).to.eventually.be.false;
      });
    });
  });

  describe('in form print mode', ()=> {
    it('should switch to print', ()=> {
      element(by.id('print-mode')).click();
      var codelistList = new CodelistList($('#multivalueReadOnlyField-wrapper'));
      expect(codelistList.getPrintFields().count()).to.eventually.equal(3);
    });
  });

  it('should change available values of related codelist field', () => {
    var sourceCodelistList = new CodelistList($('#source-wrapper'));
    var targetCodelistList = new CodelistList($('#target-wrapper .print-field'));

    sourceCodelistList.selectValue('ENG');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['MDG']);

    sourceCodelistList.selectValue('INF');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['EDG']);

    sourceCodelistList.selectValue('TSD');
    expect(targetCodelistList.getAvailableOptions()).to.eventually.deep.equal(['MDG', 'EDG']);
  });
});
