import {CodeLists} from 'administration/code-lists/code-lists';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {CodelistValidationService} from 'administration/code-lists/services/codelist-validation-service';
import {AdminToolRegistry} from 'administration/admin-tool-registry';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {getCodeLists} from './code-lists.stub';

describe('CodeLists', () => {

  let codeLists;
  beforeEach(() => {
    codeLists = new CodeLists(stubCodelistManagementService(), stub(CodelistValidationService), stub(AdminToolRegistry));
    codeLists.ngOnInit();
  });

  describe('on initialize', () => {
    it('should fetch the available code lists and mark them as visible', () => {
      let expected = getCodeLists();
      expected[0].visible = true;
      expected[1].visible = true;
      expect(codeLists.codelistManagementService.getCodeLists.calledOnce).to.be.true;
      expect(codeLists.codeLists).to.deep.equal(expected);
    });

    it('should be flagged as not adding new code list', () => {
      expect(codeLists.adding).to.be.false;
    });
  });

  describe('onUpload', () => {
    it('should fetch the code lists again', () => {
      codeLists.onUpload();
      expect(codeLists.codelistManagementService.getCodeLists.callCount).to.equal(2);
    });
  });

  describe('onChange', () => {
    it('should validate code lists and set the registry state', () => {
      codeLists.validate = sinon.spy();
      codeLists.setRegistryState = sinon.spy();

      codeLists.onChange();
      expect(codeLists.validate.calledOnce).to.be.true;
      expect(codeLists.setRegistryState.calledOnce).to.be.true;
    });
  });

  describe('canEdit() & canAdd()', () => {
    it('should properly resolve if code list addition is enabled or not', () => {
      codeLists.adding = true;
      codeLists.getRegistryState = () => true;
      expect(codeLists.canAdd()).to.be.false;

      codeLists.adding = false;
      codeLists.getRegistryState = () => false;
      expect(codeLists.canAdd()).to.be.true;
    });

    it('should properly resolve if code list editing is enabled or not', () => {
      codeLists.getRegistryState = () => true;

      expect(codeLists.canEdit({})).to.be.false;
      expect(codeLists.canEdit({isNew: true})).to.be.true;
      expect(codeLists.canEdit({isModified: true})).to.be.true;
    });
  });

  describe('onFilter(ids)', () => {
    it('should flag as visible only the filtered code lists', () => {
      codeLists.onFilter(['2']);
      expect(codeLists.codeLists[0].visible).to.be.false;
      expect(codeLists.codeLists[1].visible).to.be.true;

      codeLists.onFilter(['1', '2']);
      expect(codeLists.codeLists[0].visible).to.be.true;
      expect(codeLists.codeLists[1].visible).to.be.true;

      codeLists.onFilter([]);
      expect(codeLists.codeLists[0].visible).to.be.false;
      expect(codeLists.codeLists[1].visible).to.be.false;
    });
  });

  describe('addCodeList()', () => {
    it('should insert visible code list if all are visible', () => {
      codeLists.addCodeList();
      expect(codeLists.codeLists.length).to.equal(3);
      expect(codeLists.codeLists[0].id).to.equal('3');
      expect(codeLists.codeLists[0].isNew).to.be.true;
      expect(codeLists.codeLists[0].visible).to.be.true;
      expect(codeLists.adding).to.be.true;
    });

    it('should validate the new code list', () => {
      codeLists.addCodeList();
      expect(codeLists.codelistValidationService.validate.calledOnce).to.be.true;
      expect(codeLists.codelistValidationService.validate.calledWith(codeLists.codeLists, codeLists.codeLists[0])).to.be.true;
    });

    it('should register itself in the tool registry with having unsaved changes', () => {
      codeLists.addCodeList();
      expect(codeLists.adminToolRegistry.setState.calledOnce).to.be.true;
      expect(codeLists.adminToolRegistry.setState.calledWith('code-lists', true)).to.be.true;
    });
  });

  describe('onEdit()', () => {
    it('should register itself in the tool registry with having unsaved changes', () => {
      codeLists.onEdit();
      expect(codeLists.adminToolRegistry.setState.calledOnce).to.be.true;
      expect(codeLists.adminToolRegistry.setState.calledWith('code-lists', true)).to.be.true;
    });
  });

  describe('onSave()', () => {
    it('should fetch the code lists again to ensure changes are applied to all', () => {
      codeLists.onSave();
      expect(codeLists.codelistManagementService.getCodeLists.callCount).to.equal(2);
    });

    it('should register itself in the tool registry without having unsaved changes', () => {
      codeLists.onSave();
      expect(codeLists.adminToolRegistry.setState.calledOnce).to.be.true;
      expect(codeLists.adminToolRegistry.setState.calledWith('code-lists', false)).to.be.true;
    });
  });

  describe('onCancel()', () => {
    it('should register itself in the tool registry without having unsaved changes', () => {
      codeLists.onCancel();
      expect(codeLists.adminToolRegistry.setState.calledOnce).to.be.true;
      expect(codeLists.adminToolRegistry.setState.calledWith('code-lists', false)).to.be.true;
    });

    it('should properly restore the given code list when provided with correct arguments', () => {
      let codeList = {id: 1, name: 'test', values: [{id: 1, name: 'value1'}]};
      codeLists.onCancel(codeList, 0);
      // should preserve the actual code list reference
      expect(codeLists.codeLists[0]).to.equal(codeList);
    });

    it('should not restore the given code list when provided with incorrect arguments', () => {
      let codeList = {id: 1, name: 'test', values: [{id: 1, name: 'value1'}]};
      codeLists.onCancel(codeList, -1);
      // should not preserve the actual code list reference
      expect(codeLists.codeLists[0]).to.not.equal(codeList);
    });
  });

  describe('onCreateCancel(code)', () => {
    it('should remove the provided code from the rest', () => {
      codeLists.addCodeList();
      codeLists.onCreateCancel(codeLists.codeLists[0]);
      expect(codeLists.codeLists.length).to.equal(2);
      expect(codeLists.codeLists[0].id).to.equal('1');
      expect(codeLists.codeLists[1].id).to.equal('2');
      expect(codeLists.adding).to.be.false;
    });

    it('should register itself in the tool registry without having unsaved changes', () => {
      codeLists.addCodeList();
      codeLists.onCreateCancel(codeLists.codeLists[0]);
      expect(codeLists.adminToolRegistry.setState.calledTwice).to.be.true;
      expect(codeLists.adminToolRegistry.setState.getCall(0).args[1]).to.be.true;
      expect(codeLists.adminToolRegistry.setState.getCall(1).args[1]).to.be.false;
    });
  });

  function stubCodelistManagementService() {
    let managementStub = stub(CodelistManagementService);
    managementStub.getCodeLists.returns(PromiseStub.resolve(getCodeLists()));
    managementStub.createCodeList.returns({id: '3'});
    return managementStub;
  }

});
