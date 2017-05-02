import {CodelistList} from 'form-builder/codelist/codelist-list/codelist-list';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('CodelistList', () => {

  it('should select single value', () => {
    var codelistList = getChecklistInstance();
    expect(codelistList.selectedValues.value).to.be.true;
  });

  it('should select multi values', () => {
    var codelistList = getChecklistInstance(['first', 'second']);
    expect(codelistList.selectedValues.first).to.be.true;
    expect(codelistList.selectedValues.second).to.be.true;
  });

  describe('ngOnInit()', () => {
    it('should initialize form control', () => {
      var codelistList = getChecklistInstance();
      codelistList.initElement = sinon.spy();
      codelistList.ngOnInit();
      expect(codelistList.initElement.callCount).to.equals(1);
    });

    it('should subscribe for formViewModel property change', () => {
      var codelistList = getChecklistInstance();
      codelistList.initElement = sinon.spy();
      codelistList.executeCommonPropertyChangedHandler = sinon.spy();
      codelistList.ngOnInit();
      codelistList.fieldViewModel.value = 'newValue';
      expect(codelistList.executeCommonPropertyChangedHandler.called).to.be.true;
    });

    it('should subscribe for validationModel property change', () => {
      var codelistList = getChecklistInstance();
      codelistList.initElement = sinon.spy();
      codelistList.validateForm = sinon.spy();
      codelistList.$scope.$evalAsync = sinon.spy();
      codelistList.ngOnInit();
      codelistList.executeCommonPropertyChangedHandler = sinon.spy();
      codelistList.validationModel[codelistList.fieldViewModel.identifier].value = 'newValue';
      expect(codelistList.validateForm.called).to.be.true;
      expect(codelistList.$scope.$evalAsync.called).to.be.true;
    });
  });

  describe('loadData()', () => {
    it('should load codelist data', () => {
      var codelistList = getChecklistInstance();
      codelistList.loadData();
      expect(codelistList.validationModel[codelistList.fieldViewModel.identifier].sharedCodelistData['key']).to.be.equal('label');
    });
  });
});

function mockCodelistRestService() {
  return {
    getCodelist: sinon.spy(() => {
      return PromiseStub.resolve({data: [{value: 'key', label: 'label'}]});
    })
  };
}

function getChecklistInstance(validationValues) {
  let viewModel = {
    identifier: 'field1',
    multivalue: true,
    value: 'val'
  };
  CodelistList.prototype.form = {};
  CodelistList.prototype.fieldViewModel = new DefinitionModelProperty(viewModel);
  CodelistList.prototype.validationModel = new InstanceModel({
    field1: {
      value: validationValues || 'value',
    },
    codelistFilters: {}
  });
  return new CodelistList(IdocMocks.mockScope(), IdocMocks.mockElement(), IdocMocks.mockTimeout(), mockCodelistRestService());
}