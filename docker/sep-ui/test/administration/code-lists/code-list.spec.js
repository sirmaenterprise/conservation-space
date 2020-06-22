import {CodeList} from 'administration/code-lists/manage/code-list';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {PREVIEW, EDIT, CREATE} from 'administration/code-lists/manage/code-manage-modes';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {stubConfirmationDialogService} from 'test/components/dialog/confirmation-dialog-service.stub';

describe('CodeList', () => {

  let codeList;
  beforeEach(() => {
    codeList = new CodeList(stubManagementService(), stubConfirmationDialogService());
    codeList.model = getTestModel();
    codeList.onEdit = sinon.spy();
    codeList.onSave = sinon.spy();
    codeList.onCancel = sinon.spy();
    codeList.onCreateCancel = sinon.spy();
    codeList.onChange = sinon.spy();
    codeList.ngOnInit();
  });

  describe('on initialize', () => {
    it('should assign configuration for the descriptions button', () => {
      expect(codeList.descriptionsButtonConfig.renderLabel).to.be.true;
    });

    it('should ensure edit mode is disabled for itself and nested components', () => {
      expect(codeList.mode).to.equal(PREVIEW);
    });

    it('should switch to create mode if the code list model is new', () => {
      codeList = new CodeList(stubManagementService(), stubConfirmationDialogService());
      codeList.model = getTestModel();
      codeList.model.isNew = true;
      codeList.ngOnInit();

      expect(codeList.mode).to.equal(CREATE);
      expect(codeList.renderDetails).to.be.true;
    });
  });

  describe('toggleDetails', () => {
    it('should toggle the rendering of the details section for the current code list', () => {
      expect(codeList.renderDetails).to.be.false;
      codeList.toggleDetails();
      expect(codeList.renderDetails).to.be.true;
      codeList.toggleDetails();
      expect(codeList.renderDetails).to.be.false;
    });
  });

  describe('edit()', () => {
    it('should enable edit mode', () => {
      codeList.edit();
      expect(codeList.mode).to.equal(EDIT);
    });

    it('should clone the code list model and preserve it', () => {
      codeList.edit();
      //model is flagged as modified
      delete codeList.model.isModified;

      expect(codeList.originalModel).to.deep.equal(codeList.model);
      expect(codeList.originalModel).to.not.equal(codeList.model);
    });

    it('should notify for being in the edit state', () => {
      codeList.edit();
      expect(codeList.onEdit.calledOnce).to.be.true;
    });

    it('should properly prepare model state', () => {
      codeList.edit();
      expect(codeList.model.isModified).to.be.true;
    });
  });

  describe('save()', () => {

    beforeEach(() => codeList.edit());

    it('should confirm the operation', () => {
      codeList.save();
      expect(codeList.confirmationDialogService.confirm.calledOnce).to.be.true;
      expect(codeList.mode).to.equal(PREVIEW);
      expect(codeList.codelistManagementService.saveCodeList.calledOnce).to.be.true;
    });

    it('should not disable edit mode if not confirmed', () => {
      codeList.confirmationDialogService = stubConfirmationDialogService(false);
      codeList.save();

      expect(codeList.mode).to.equal(EDIT);
      expect(codeList.codelistManagementService.saveCodeList.called).to.be.false;
    });

    it('should update the code list via the management service after confirmation', () => {
      codeList.model.extras['1'] = 'Extra 1';
      codeList.save();

      expect(codeList.mode).to.equal(PREVIEW);
      expect(codeList.codelistManagementService.saveCodeList.calledOnce).to.be.true;
      expect(codeList.codelistManagementService.saveCodeList.calledWith(codeList.originalModel, codeList.model)).to.be.true;
      expect(codeList.savingChanges).to.be.false;
    });

    it('should not disable edit mode in case of error', () => {
      codeList.codelistManagementService = stubManagementService(true);
      codeList.model.extras['1'] = 'Extra 1';
      codeList.save();

      expect(codeList.mode).to.equal(EDIT);
      expect(codeList.savingChanges).to.be.false;
    });

    it('should notify via component event', () => {
      codeList.save();
      expect(codeList.onSave.calledOnce).to.be.true;
    });

    it('should properly revert model state', () => {
      codeList.model.isModified = true;

      codeList.save();
      expect(codeList.model.isModified).to.not.exist;
    });
  });

  describe('cancel()', () => {
    it('should disable edit mode', () => {
      codeList.edit();
      codeList.cancel();
      expect(codeList.confirmationDialogService.confirm.called).to.be.false;
      expect(codeList.mode).to.equal(PREVIEW);
    });

    it('should revert model state', () => {
      codeList.edit();
      expect(codeList.model.isModified).to.be.true;

      codeList.cancel();
      expect(codeList.model.isModified).to.not.exist;
    });

    it('should restore original model', () => {
      codeList.edit();
      codeList.cancel();
      expect(codeList.confirmationDialogService.confirm.called).to.be.false;
      expect(codeList.originalModel).to.deep.equal(codeList.model);
      expect(codeList.originalModel).to.equal(codeList.model);
    });

    it('should notify for cancelling the changes', () => {
      codeList.edit();
      codeList.cancel();
      expect(codeList.onCancel.calledOnce).to.be.true;
    });

    it('should confirm the cancellation if there are changes', () => {
      codeList.edit();
      codeList.codelistManagementService.areCodeListsEqual.returns(false);
      codeList.cancel();
      expect(codeList.confirmationDialogService.confirm.calledOnce).to.be.true;
      expect(codeList.mode).to.equal(PREVIEW);
    });

    it('should not cancel the changes if the confirmation is rejected', () => {
      codeList.edit();
      codeList.codelistManagementService.areCodeListsEqual.returns(false);
      codeList.confirmationDialogService = stubConfirmationDialogService(false);
      codeList.cancel();
      expect(codeList.confirmationDialogService.confirm.calledOnce).to.be.true;
      expect(codeList.mode).to.equal(EDIT);

      // If there are changes, it should notify only after cancel confirmation
      expect(codeList.onCancel.calledOnce).to.be.false;
    });
  });

  describe('cancelCreate()', () => {
    it('should confirm and notify for cancellation with component event', () => {
      codeList.cancelCreate();
      expect(codeList.onCreateCancel.calledOnce).to.be.true;
      expect(codeList.onCreateCancel.getCall(0).args[0].codeList).to.equal(codeList.model);
    });

    it('should not notify for cancellation if not confirmed', () => {
      codeList.confirmationDialogService = stubConfirmationDialogService(false);
      codeList.cancelCreate();
      expect(codeList.onCreateCancel.called).to.be.false;
    });
  });

  describe('onModelChange()', () => {
    it('should notify that the current code list is updated', () => {
      codeList.onModelChange();
      expect(codeList.onChange.calledOnce).to.be.true;
      expect(codeList.onChange.getCall(0).args[0].codeList).to.equal(codeList.model);
    });
  });

  describe('isSaveDisabled()', () => {
    it('should allow or disallow depending on the state and validation model', () => {
      codeList.savingChanges = false;
      expect(codeList.isSaveDisabled()).to.be.false;

      codeList.savingChanges = true;
      expect(codeList.isSaveDisabled()).to.be.true;

      codeList.savingChanges = false;
      codeList.model.validationModel = {valid: true};
      expect(codeList.isSaveDisabled()).to.be.false;

      codeList.model.validationModel = {valid: false};
      expect(codeList.isSaveDisabled()).to.be.true;
    });
  });

  describe('isFieldInvalid(field)', () => {
    it('should determine if a field is invalid using the validation model', () => {
      expect(codeList.isFieldInvalid('id')).to.be.false;

      codeList.model.validationModel = {};
      codeList.model.validationModel['id'] = {valid: true};
      expect(codeList.isFieldInvalid('id')).to.be.false;

      codeList.model.validationModel['id'] = {valid: false};
      expect(codeList.isFieldInvalid('id')).to.be.true;
    });
  });

  function stubManagementService(fail = false) {
    let serviceStub = stub(CodelistManagementService);
    if (fail) {
      serviceStub.saveCodeList.returns(PromiseStub.reject());
    } else {
      serviceStub.saveCodeList.returns(PromiseStub.resolve());
    }
    serviceStub.areCodeListsEqual.returns(true);
    return serviceStub;
  }

  function getTestModel() {
    return {
      id: '1',
      descriptions: {},
      extras: {},
      values: {}
    };
  }

});