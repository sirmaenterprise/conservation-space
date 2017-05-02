import {Codelist} from 'form-builder/codelist/codelist';
import {Select} from 'components/select/select';

describe('Codelist', () => {

  var translateService = {
    translateInstant: sinon.stub()
  };
  var codelistRestService = {
    getCodelist: sinon.stub()
  };
  var timeout = sinon.spy();
  Codelist.prototype.fieldConfig = {};
  Codelist.prototype.validationModel = {
    field1: {}
  };
  Codelist.prototype.fieldViewModel = {
    identifier: 'field1',
    codelist: 100,
    multivalue: false,
    disabled: true
  };
  Codelist.prototype.validateForm = sinon.stub.returns(true);
  var scopeMock = {
    $watch: sinon.spy()
  };
  Codelist.prototype.form = {};

  it('#getFieldConfig should copy model value for [multiple]', () => {
    var codelist = new Codelist(codelistRestService, translateService, timeout, scopeMock);
    var fieldConfig = codelist.getFieldConfig();
    expect(fieldConfig.multiple).to.be.false;
  });

  it('#getFieldConfig should copy model value for [disabled]', () => {
    var codelist = new Codelist(codelistRestService, translateService, timeout, scopeMock);
    var fieldConfig = codelist.getFieldConfig();
    expect(fieldConfig.disabled).to.be.true;
  });

  it('#getFieldConfig should set defaultToSingleValue=true', () => {
    var codelist = new Codelist(codelistRestService, translateService, timeout, scopeMock);
    var fieldConfig = codelist.getFieldConfig();
    expect(fieldConfig.defaultToSingleValue).to.be.true;
  });

  it('#getFieldConfig should set default query to undefined',()=>{
    var codelist= new Codelist(codelistRestService, translateService, timeout, scopeMock);
    var fieldConfig = codelist.getFieldConfig();
    expect(fieldConfig.q).to.be.undefined;
  });

  it('#convertData should convert codelist data to expected select2 plugin format', () => {
    var codelist = new Codelist(codelistRestService, translateService, timeout, scopeMock);
    var converted = codelist.convertData({
      data: [
        { value: 'code1', label: 'Codevalue 1'},
        { value: 'code2', label: 'Codevalue 2'},
        { value: 'code3', label: 'Codevalue 3'}
      ]
    });
    expect(converted).to.deep.equal([
      { id: 'code1', text: 'Codevalue 1'},
      { id: 'code2', text: 'Codevalue 2'},
      { id: 'code3', text: 'Codevalue 3'}
    ]);
  });
});