import {ExportXlsxAction} from 'idoc/actions/export-to-xlsx/export-xlsx-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'idoc/idoc-context';
import {IdocMocks} from '../../idoc-mocks';

describe('ExportXlsxAction', () => {
  var action;
  var currentObject;
  beforeEach(() => {
    action = new ExportXlsxAction(exportService, translateService, notificationService, searchResolverService, authenticationService, {});
    currentObject = new InstanceObject("emf:123456", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
  });


  it('should execute export xlsx action', () => {
    const data = {
      config: {
        title: '',
        instanceHeaderType: 'compact_header',
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: { 'common': ['description'] }
      }, context: {
        instanceId: 'emf:123123123'
      }
    };
    action.execute(currentObject, data);
    expect(searchResolverService.resolve.called).to.be.true;
    expect(exportService.exportXlsx.called).to.be.true;
  });

  it('should execute export xlsx action with title', () => {
    let spyAfterExportHandler = sinon.stub(ExportXlsxAction.prototype, 'afterExportHandler');
    let spyDownloadFile = sinon.stub(ExportXlsxAction.prototype, 'downloadFile');
    const data = {
      config: {
        title: 'Title',
        instanceHeaderType: 'compact_header',
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: { 'common': ['description'] }
      }, context: {
        instanceId: 'emf:123123123'
      }
    };
    action.execute(currentObject, data);
    expect(searchResolverService.resolve.called).to.be.true;
    expect(exportService.exportXlsx.called).to.be.true;
    expect(spyAfterExportHandler.called).to.be.true;

  });

  it('should check passed params for export', () => {
    const data = {
      config: {
        title: '',
        instanceHeaderType: 'compact_header',
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: { 'common': ['description'] },
        selectObjectMode: 'manually'
      }, context: {
        instanceId: 'emf:123123123'
      }
    };

    let expectedResult = {
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: { 'common': ['description'] },
        instanceHeaderType: 'compact_header',
        instanceId:'emf:123123123',
        filename: 'Workbook.xlsx',
        orderBy: '',
        orderDirection: '',
        selectObjectMode: 'manually',
        criteria: undefined
    };

    action.execute(currentObject, data);
    expect(action.dataForExport).to.deep.equal(expectedResult);
  });

  let translateService = {
    translateInstant: sinon.spy()
  };
  let notificationService = {
    info: sinon.spy()
  };

  let exportService = {
    exportXlsx: sinon.spy(() => {
      return PromiseStub.resolve({
        data: {
          status: 200,
          data: "host/path"
        }
      });
    })
  }

  let searchResolverService = {
    resolve: sinon.spy(() => {
      return PromiseStub.resolve({
        data: [{
          object: 'object1'
        }]
      });
    })
  };

  let authenticationService = {
    getToken: sinon.spy()
  };
});
