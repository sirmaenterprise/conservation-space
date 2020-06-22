import {CodelistList} from 'form-builder/codelist/codelist-list/codelist-list';
import {CodelistRestService} from 'services/rest/codelist-service';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test-utils';

describe('CodelistList field', () => {

  it('should select single value', () => {
    let codelistList = getChecklistInstance();
    expect(codelistList.selectedValues.value).to.be.true;
  });

  it('should select multi values', () => {
    let codelistList = getChecklistInstance(['first', 'second']);
    expect(codelistList.selectedValues.first).to.be.true;
    expect(codelistList.selectedValues.second).to.be.true;
  });

  describe('ngOnInit()', () => {
    it('should initialize form control', () => {
      let codelistList = getChecklistInstance();
      codelistList.initElement = sinon.spy();
      codelistList.ngOnInit();
      expect(codelistList.initElement.callCount).to.equals(1);
    });

    it('should subscribe for formViewModel property change', () => {
      let codelistList = getChecklistInstance();
      codelistList.initElement = sinon.spy();
      codelistList.executeCommonPropertyChangedHandler = sinon.spy();
      codelistList.ngOnInit();
      codelistList.fieldViewModel.value = 'newValue';
      expect(codelistList.executeCommonPropertyChangedHandler.called).to.be.true;
    });

    it('should subscribe for validationModel property change', () => {
      let codelistList = getChecklistInstance();
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
      let codelistList = getChecklistInstance();
      codelistList.loadData();
      expect(codelistList.validationModel[codelistList.fieldViewModel.identifier].sharedCodelistData['key']).to.be.equal('label');
    });
  });

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      let codelistList = getChecklistInstance();
      codelistList.formEventEmitter = stub(EventEmitter);
      codelistList.ngAfterViewInit();
      expect(codelistList.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });

});

function getChecklistInstance(validationValues) {
  let fieldsMap = {
    field1: new DefinitionModelProperty({
      identifier: 'field1',
      multivalue: true,
      value: 'val'
    })
  };

  CodelistList.prototype.formWrapper = {
    getViewModel: sinon.stub.returns(fieldsMap),
    fieldsMap,
    formConfig: {
      models: {
        validationModel: new InstanceModel({
          field1: {
            value: validationValues || 'value'
          },
          codelistFilters: {}
        })
      }
    },
    config: {
      formViewMode: 'preview'
    },
    objectDataForm: {}
  };
  CodelistList.prototype.identifier = 'field1';

  let codelistRestService = stub(CodelistRestService);
  codelistRestService.getCodelist.returns(PromiseStub.resolve({data: [{value: 'key', label: 'label'}]}));

  let codelistFilterProvider = stub(CodelistFilterProvider);

  return new CodelistList(IdocMocks.mockScope(), IdocMocks.mockElement(), IdocMocks.mockTimeout(), codelistRestService, codelistFilterProvider);
}