import {CodelistService} from 'services/codelist/codelist-service';
import {PromiseStub} from 'test/promise-stub';

const CODELIST_DATA = {
  codelist1: [{
    value: 'codevalue1',
    label: 'Code value 1'
  }, {
    value: 'codevalue2',
    label: 'Code value 2'
  }],
  codelist2: [{
    value: 'codevalue2',
    label: 'Code value 2'
  }, {
    value: 'codevalue3',
    label: 'Code value 3'
  }, {
    value: 'codevalue4',
    label: 'Code value 4'
  }]
};

describe('CodelistService', () => {

  let codelistService;

  before(() => {
    let codelistRestServiceStub = {
      getCodelist: (codelist) => {
        return PromiseStub.resolve({
          data: CODELIST_DATA[codelist.codelistNumber]
        });
      }
    };
    codelistService = new CodelistService(PromiseStub, codelistRestServiceStub);
  });

  describe('', () => {
    it('should aggregate codevalues for given codelists', () => {
      let aggregatedCodelists = codelistService.aggregateCodelists(['codelist1', 'codelist2']);
      aggregatedCodelists.then((result) => {
        expect(result.length).to.equals(4);
        expect(result[0].value).to.equals('codevalue1');
        expect(result[1].value).to.equals('codevalue2');
        expect(result[2].value).to.equals('codevalue3');
        expect(result[3].value).to.equals('codevalue4');
      });
    });

    it('should aggregate and filter codevalues for given codelists', () => {
      let filterFunc = (codevalue) => {
        return codevalue.value !== 'codevalue3';
      };
      let aggregatedCodelists = codelistService.aggregateCodelists(['codelist1', 'codelist2'], filterFunc);
      aggregatedCodelists.then((result) => {
        expect(result.length).to.equals(3);
        expect(result[0].value).to.equals('codevalue1');
        expect(result[1].value).to.equals('codevalue2');
        expect(result[2].value).to.equals('codevalue4');
      });
    });

    it('should aggregate and convert codevalues for given codelists', () => {
      let aggregatedCodelists = codelistService.aggregateCodelists(['codelist1', 'codelist2'], undefined, CodelistService.convertToSelectValue);
      aggregatedCodelists.then((result) => {
        expect(result.length).to.equals(4);
        expect(result[0].id).to.equals('codevalue1');
        expect(result[1].id).to.equals('codevalue2');
        expect(result[2].id).to.equals('codevalue3');
        expect(result[3].id).to.equals('codevalue4');
      });
    });

    it('should aggregate, filter and convert codevalues for given codelists', () => {
      let filterFunc = (codevalue) => {
        return codevalue.value !== 'codevalue3';
      };
      let aggregatedCodelists = codelistService.aggregateCodelists(['codelist1', 'codelist2'], filterFunc, CodelistService.convertToSelectValue);
      aggregatedCodelists.then((result) => {
        expect(result.length).to.equals(3);
        expect(result[0].id).to.equals('codevalue1');
        expect(result[1].id).to.equals('codevalue2');
        expect(result[2].id).to.equals('codevalue4');
      });
    });
  });

  it('convertToSelectValue should convert codelist value to value suitable for Select component', () => {
    let given = {
      value: 'codevalue1',
      label: 'Code value 1'
    };
    let expected = {
      id: 'codevalue1',
      text: 'Code value 1'
    };
    expect(CodelistService.convertToSelectValue(given)).to.eql(expected);
  });
});
