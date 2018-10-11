import {DatatableCodelistFilter} from 'idoc/widget/datatable-widget/datatable-filter/datatable-codelist-filter';

const CODEVALUE = {
  value: 'codevalue1',
  label: 'Code value 1'
};

describe('DatatableCodelistFilter', () => {

  let datatableCodelistFilter;

  before(() => {
    datatableCodelistFilter = new DatatableCodelistFilter();
    datatableCodelistFilter.header = {
      uri: 'emf:type'
    };
  });

  describe('isExistingValue', () => {
    it('should return true if there are no aggregted values', () => {
      datatableCodelistFilter.config.aggregated = undefined;
      expect(datatableCodelistFilter.isExistingValue(CODEVALUE)).to.be.true;
    });

    it('should return true if value is amongst aggregated values', () => {
      datatableCodelistFilter.config.aggregated = {
        'emf:type': {
          'codevalue1': 1
        }
      };
      expect(datatableCodelistFilter.isExistingValue(CODEVALUE)).to.be.true;
    });

    it('should return false if value is not amongst aggregated values', () => {
      datatableCodelistFilter.config.aggregated = {
        'emf:type': {
          'anothercodevalue': 1
        }
      };
      expect(datatableCodelistFilter.isExistingValue(CODEVALUE)).to.be.false;
    });
  });
});
