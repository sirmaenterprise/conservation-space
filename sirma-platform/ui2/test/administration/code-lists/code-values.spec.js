import {CodeValues} from 'administration/code-lists/manage/code-values';
import {Configuration} from 'common/application-config';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {stub} from 'test/test-utils';

describe('CodeValues', () => {

  let values;
  beforeEach(() => {
    values = new CodeValues(mock$scope(), stubConfiguration(), stubManagementService());
    values.codeList = {
      values: [{id: 'APPROVED'}, {id: 'DRAFT'}]
    };
    values.onChange = sinon.spy();
    values.ngOnInit();
  });

  it('should prepare proper configuration for values pagination', () => {
    expect(values.paginationConfig.pageSize).to.equal(3);
    expect(values.paginationConfig.total).to.equal(2);
    expect(values.paginationConfig.page).to.equal(1);
    expect(values.paginationConfig.showFirstLastButtons).to.be.true;
  });

  it('should update the pagination after the values length is changed', () => {
    values.paginationConfig.page = 2;
    values.codeList.values.push({id: 'DELETED'});
    values.$scope.$digest();
    expect(values.paginationConfig.total).to.equal(3);
    expect(values.paginationConfig.page).to.equal(1);
  });

  it('should be able to insert new code value', () => {
    values.addCodeValue();
    expect(values.codeList.values.length).to.equal(3);
    expect(values.codeList.values[0].id).to.equal('DELETED');
    expect(values.codeList.values[0].isNew).to.be.true;
    // Should notify for changes
    expect(values.onChange.calledOnce).to.be.true;
  });

  it('should be able to remove new code value', () => {
    values.removeNewValue(values.codeList.values[0]);
    expect(values.codeList.values.length).to.equal(1);
    expect(values.codeList.values[0].id).to.equal('DRAFT');

    values.removeNewValue(values.codeList.values[0]);
    expect(values.codeList.values.length).to.equal(0);

    // Should notify for changes
    expect(values.onChange.calledTwice).to.be.true;
  });

  it('should notify for changes after the model of a value is changed', () => {
    let value = values.codeList.values[0];
    values.onModelChange(value);
    expect(value.isModified).to.be.true;
    expect(values.onChange.calledOnce).to.be.true;
  });

  it('should determine if value\'s field is invalid using the validation model of the value', () => {
    let value = values.codeList.values[0];
    expect(values.isValueFieldInvalid(value, 'id')).to.be.false;

    value.validationModel = {};
    value.validationModel['id'] = {valid: true};
    expect(values.isValueFieldInvalid(value, 'id')).to.be.false;

    value.validationModel['id'] = {valid: false};
    expect(values.isValueFieldInvalid(value, 'id')).to.be.true;
  });

  function stubConfiguration() {
    let configuration = stub(Configuration);
    configuration.get.withArgs(Configuration.SEARCH_PAGE_SIZE).returns(3);
    return configuration;
  }

  function stubManagementService() {
    let managementStub = stub(CodelistManagementService);
    managementStub.createCodeValue.returns({id: 'DELETED'});
    return managementStub;
  }

});