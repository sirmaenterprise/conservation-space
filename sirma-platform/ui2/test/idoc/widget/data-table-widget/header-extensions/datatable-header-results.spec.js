import {DatatableHeaderResults} from 'idoc/widget/datatable-widget/header-extensions/datatable-header-results';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {BASKET_EXTENSION} from 'services/picker/picker-service';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {WidgetControl} from 'idoc/widget/widget';

describe('DatatableHeaderResults', () => {

  let datatableHeaderResults;

  before(() => {
    DatatableHeaderResults.prototype.config = {
      expanded: false
    };
    datatableHeaderResults = new DatatableHeaderResults(stub(ObjectSelectorHelper), undefined, IdocMocks.mockScope());
    datatableHeaderResults.control = stub(WidgetControl);
  });

  it('should properly configure for fullscreen mode for automatically select mode', ()=> {
    let expectedConfig = {
      renderOptions: false,
      renderCriteria: false,
      hideWidgerToolbar: false,
      instanceHeaderType: HEADER_DEFAULT,
      orderBy: 'emf:type',
      orderDirection: 'asc',
      orderByCodelistNumbers: '7'
    };
    datatableHeaderResults.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    datatableHeaderResults.orderBy = 'emf:type';
    datatableHeaderResults.orderDirection = 'asc';
    datatableHeaderResults.orderByCodelistNumbers = '7';
    expect(datatableHeaderResults.createConfig()).to.contain(expectedConfig);
  });

  it('should properly configure for fullscreen mode for manually select mode', ()=> {
    let expectedConfig = {
      renderOptions: false,
      renderCriteria: false,
      hideWidgerToolbar: false,
      instanceHeaderType: HEADER_DEFAULT,
      orderBy: 'emf:type',
      orderDirection: 'asc',
      orderByCodelistNumbers: '7'
    };
    datatableHeaderResults.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
    let fullscreenConfig = datatableHeaderResults.createConfig();
    expect(fullscreenConfig).to.contain(expectedConfig);
  });

  describe('getTotalResults', () => {
    it('should return total results if select object mode is manually', () => {
      datatableHeaderResults.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      datatableHeaderResults.config.selectedObjects = [];
      datatableHeaderResults.getTotalResults();
      expect(datatableHeaderResults.totalResults).to.equals('');
      datatableHeaderResults.config.selectedObjects = [1, 2, 3];
      datatableHeaderResults.getTotalResults();
      expect(datatableHeaderResults.totalResults).to.equals(3);
    });

    it('should return total results if select object mode is automatically', () => {
      datatableHeaderResults.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      datatableHeaderResults.objectSelectorHelper.getAutomaticSelection.returns(PromiseStub.resolve({total: 7}));
      datatableHeaderResults.getTotalResults();
      expect(datatableHeaderResults.totalResults).to.equals(7);
    });
  });

  it('should subscribe for orderChanged event', () => {
    datatableHeaderResults.ngOnInit();
    expect(datatableHeaderResults.control.subscribe.callCount).to.equals(1);
    expect(datatableHeaderResults.control.subscribe.args[0][0]).to.equals('orderChanged');
  });
});
