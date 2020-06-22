import {ExportXlsxAction} from 'idoc/actions/export-to-xlsx/export-xlsx-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {IdocMocks} from '../../idoc-mocks';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';
import {AuthenticationService} from 'security/authentication-service';
import {NotificationService} from 'services/notification/notification-service';
import {ExportService} from 'services/rest/export-service';
import {SearchResolverService} from 'services/resolver/search-resolver-service';

describe('ExportXlsxAction', () => {

  let action;
  let currentObject;
  let translateService;
  let authenticationService;
  let notificationService;
  let exportService;
  let searchResolverService;

  beforeEach(() => {
    translateService = stub(TranslateService);
    authenticationService = mockAuthenticaitonService();
    notificationService = stub(NotificationService);
    exportService = getExportService();
    searchResolverService = getSearchResolverService();
    action = new ExportXlsxAction(exportService, translateService, notificationService, searchResolverService, authenticationService, {});
    currentObject = new InstanceObject('emf:123456', IdocMocks.generateModels(), IdocMocks.generateIntialContent());
  });

  it('should execute export xlsx action', () => {
    const data = {
      config: {
        title: '',
        instanceHeaderType: 'compact_header',
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: {'common': ['description']}
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
    sinon.stub(ExportXlsxAction.prototype, 'downloadFile');
    const data = {
      config: {
        title: 'Title',
        instanceHeaderType: 'compact_header',
        selectedObjects: ['emf:123123123', 'emf:9879878'],
        selectedProperties: {'common': ['description']}
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
        selectedProperties: {'common': ['description']},
        selectObjectMode: 'manually',
        columnHeaders: [
          {name: 'compact_header', labels: ['Entity']},
          {name: 'description', labels: ['Description']}
        ]
      }, context: {
        instanceId: 'emf:123123123'
      }
    };

    let expectedResult = {
      selectedObjects: ['emf:123123123', 'emf:9879878'],
      selectedProperties: {'common': ['description']},
      instanceHeaderType: 'compact_header',
      instanceId: 'emf:123123123',
      filename: 'Workbook',
      orderBy: '',
      orderDirection: '',
      selectObjectMode: 'manually',
      criteria: undefined,
      showInstanceId: true,
      selectedHeaders: [
        {name: 'compact_header', labels: ['Entity']},
        {name: 'description', labels: ['Description']}
      ]
    };

    action.execute(currentObject, data);
    expect(action.dataForExport).to.deep.equal(expectedResult);
  });

  it('should not modify the search criteria', () => {
    let searchResolverService = new SearchResolverService();
    sinon.stub(searchResolverService, 'resolve', (criteria) => {
      criteria.id = 1;
      return PromiseStub.resolve({
        data: [{
          object: 'object1'
        }]
      });
    });
    sinon.stub(searchResolverService, 'loadResolvers', () => { return PromiseStub.resolve(); });
    action.searchResolverService = searchResolverService;

    let expectedCriteria = {
      'id': '7fd6bce2-71ef-46da-a80b-fb0ae352d0dc'
    };
    let criteria = {
      'id': '7fd6bce2-71ef-46da-a80b-fb0ae352d0dc'
    };
    const data = {
      config: {
        criteria
      },
      context: {
        instanceId: 'emf:123123123'
      }
    };

    action.execute(currentObject, data);

    expect(data.config.criteria).to.eql(expectedCriteria);
  });

  function mockAuthenticaitonService() {
    let service = stub(AuthenticationService);
    service.getToken.returns(PromiseStub.resolve('token'));
    return service;
  }
});

function getSearchResolverService() {
  let searchResolverService = stub(SearchResolverService);
  searchResolverService.resolve.returns(PromiseStub.resolve({
    data: [{
      object: 'object1'
    }]
  }));

  return searchResolverService;
}

function getExportService() {
  let exportService = stub(ExportService);
  exportService.exportXlsx.returns(PromiseStub.resolve({
    data: {
      status: 200,
      data: 'host/path'
    }
  }));
  return exportService;
}
