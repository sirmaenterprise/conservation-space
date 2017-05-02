import {ImportService} from 'services/rest/import-service';

describe('ImportService', () => {
  let restClient = {};
  let importService = new ImportService(restClient);

  it('readFile should perform post with proper arguments', () => {
    let context = {
      id: 'emf:123123123',
      properties: {
        hasParent: ['emf:987654321']
      }
    };
    let id = '123123123';
    restClient.post = sinon.spy();
    importService.readFile(id, context);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instance/' + id + '/integration/read');
  });

  it('importFile should perform post with proper arguments', () => {
    restClient.post = sinon.spy();
    let context = {
      id: 'emf:123456789',
      properties: {
        hasParent: ['emf:532452344', 'emf:74635345434']
      }
    };
    let id = 'emf:123456789';
    let data = {
      data: [1, 2, 3],
      report: {
        id: 'emf:123456789'
      },
    }
    let dataForImport = {
      data: [1, 2, 3],
      report: 'emf:2344534324',
      context: 'emf:74635345434'
    }
    importService.importFile(id, context, data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instance/' + 'emf:123456789' + '/integration/import');
    expect(restClient.post.getCall(0).args[1].data[2]).to.equal(dataForImport.data[2]);
  });

  it('importFile should perform post with proper arguments and currentObject as context', () => {
    restClient.post = sinon.spy();
    let context = {
      id: 'emf:123456789',
      properties: {
        hasParent: ['emf:532452344', 'emf:74635345434']
      }
    };
    let id = 'emf:123456789';
    let data = {
      data: [1, 2, 3],
      report: {
        id: 'emf:123456789'
      },
    }
    let dataForImport = {
      data: [1, 2, 3],
      report: 'emf:2344534324',
      context: 'emf:123456789'
    }
    importService.importFile(id, context, data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instance/' + 'emf:123456789' + '/integration/import');
    expect(restClient.post.getCall(0).args[1].data[2]).to.equal(dataForImport.data[2]);
  });
});
