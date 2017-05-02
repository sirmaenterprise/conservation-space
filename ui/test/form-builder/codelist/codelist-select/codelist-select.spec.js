import {CodelistSelect} from 'form-builder/codelist/codelist-select/codelist-select';
import {Codelist} from 'form-builder/codelist/codelist';
import {InstanceModelProperty} from 'models/instance-model';
import {SelectMocks} from '../../../components/select-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('Codelist Select', () => {
  let arrayInstanceModel = new InstanceModelProperty({value: ['CH', 'FM']});
  let singleInstanceModel = new InstanceModelProperty({value: 'CH'});

  arrayInstanceModel.sharedCodelistData = {};
  singleInstanceModel.sharedCodelistData = {};

  let fieldConfig = {
    'placeholder': 'Select value...',
    'defaultToSingleValue': true,
    'multiple': false,
    'allowClear': true,
    dataLoader: () => PromiseStub.resolve({
      data: [
        {value: 'CH', label: 'Chart'},
        {value: 'EM', label: 'Environmental Manual'},
        {value: 'FM', label: 'Form'},
        {value: 'LT', label: 'List'},
        {value: 'PL', label: 'Policy'},
        {value: 'PM', label: 'Program'},
        {value: 'PN', label: 'Plan'},
        {value: 'PR', label: 'Procedure'},
        {value: 'PW', label: 'Process Worksheet'}
      ]
    }),
    dataConverter: (data) => {
      return data.data.map((element) => {
        let select2Data = {id: element.value, text: element.label};
        arrayInstanceModel.sharedCodelistData[select2Data.id] = select2Data.text;
        singleInstanceModel.sharedCodelistData[select2Data.id] = select2Data.text;

        return select2Data;
      });
    },
  };

  beforeEach(() => {
    sinon.stub($, 'contains').returns(true);
  });
  afterEach(() => {
    $.contains.restore();
  });

  it('should append each option element when its a multi-valued codelist', () => {
    let mockElement = SelectMocks.mockElement();
    CodelistSelect.prototype.config = fieldConfig;
    CodelistSelect.prototype.validationModel = arrayInstanceModel;
    let codelistSelect = new CodelistSelect(mockElement);
    arrayInstanceModel.value = ['CH', 'FM', 'PL', 'PR', 'PW'];

    expect(codelistSelect.$element.append.called, 'option has been called').to.be.true;
    expect(codelistSelect.$element.append.args[0][0], 'first call').to.eql(new Option('Chart', 'CH', true, true));
    expect(codelistSelect.$element.append.args[1][0], 'second call').to.eql(new Option('Form', 'FM', true, true));
    expect(codelistSelect.$element.append.args[2][0], 'third call').to.eql(new Option('Policy', 'PL', true, true));
    expect(codelistSelect.$element.append.args[3][0], 'fourth call').to.eql(new Option('Procedure', 'PR', true, true));
    expect(codelistSelect.$element.append.args[4][0], 'sixth call').to.eql(new Option('Process Worksheet', 'PW', true, true));
  });

  it('should append a single option element when single-valued codelist is changed', () => {
    let mockElement = SelectMocks.mockElement();
    CodelistSelect.prototype.config = fieldConfig;
    CodelistSelect.prototype.validationModel = singleInstanceModel;
    let codelistSelect = new CodelistSelect(mockElement);

    singleInstanceModel.value = 'PL';
    expect(codelistSelect.$element.append.calledOnce).to.be.true;
    expect(codelistSelect.$element.append.args[0][0]).to.eql(new Option('Policy', 'PL', true, true));
  });

});