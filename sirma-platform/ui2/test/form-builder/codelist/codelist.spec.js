import {Codelist} from 'form-builder/codelist/codelist';
import {EventEmitter} from 'common/event-emitter';
import {TranslateService} from 'services/i18n/translate-service';
import {CodelistRestService} from 'services/rest/codelist-service';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import {stub} from 'test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('Codelist field', () => {

  let translateService = stub(TranslateService);
  let codelistRestService = stub(CodelistRestService);
  let codelistFilterProvider = stub(CodelistFilterProvider);

  let fieldsMap = {
    field1: {
      identifier: 'field1',
      codelist: 100,
      multivalue: false,
      disabled: true
    }
  };

  let codelist;

  beforeEach(() => {
    Codelist.prototype.fieldConfig = {};
    Codelist.prototype.validateForm = sinon.stub.returns(true);
    Codelist.prototype.formWrapper =  mockFormWrapper()
      .setValidationModel({
        field1: {}
      }).setFieldsMap(fieldsMap).get();

    Codelist.prototype.identifier = 'field1';

    codelist = new Codelist(codelistRestService, translateService, codelistFilterProvider);
  });

  describe('#getFieldConfig', () => {
    it('should copy model value for [multiple]', () => {
      let fieldConfig = codelist.getFieldConfig();
      expect(fieldConfig.multiple).to.be.false;
    });

    it('should copy model value for [disabled]', () => {
      let fieldConfig = codelist.getFieldConfig();
      expect(fieldConfig.disabled).to.be.true;
    });

    it('should set defaultToSingleValue=true', () => {
      let fieldConfig = codelist.getFieldConfig();
      expect(fieldConfig.defaultToSingleValue).to.be.true;
    });

    it('should set default query to undefined', () => {
      let fieldConfig = codelist.getFieldConfig();
      expect(fieldConfig.q).to.be.undefined;
    });
  });

  it('#convertData should convert codelist data to expected select2 plugin format', () => {
    let converted = codelist.convertData({
      data: [
        {value: 'code1', label: 'Codevalue 1'},
        {value: 'code2', label: 'Codevalue 2'},
        {value: 'code3', label: 'Codevalue 3'}
      ]
    });
    expect(converted).to.deep.equal([
      {id: 'code1', text: 'Codevalue 1'},
      {id: 'code2', text: 'Codevalue 2'},
      {id: 'code3', text: 'Codevalue 3'}
    ]);
  });

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      codelist.formEventEmitter = stub(EventEmitter);
      codelist.ngAfterViewInit();
      expect(codelist.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });
});