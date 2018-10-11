import {ContentRestService, SERVICE_PATH} from 'services/rest/content-service';

describe('ContentRestService', function () {
  it('should provide a content path for given entity', function () {
    const ID = 'test123';

    var service = new ContentRestService({});
    var contentUrl = service.getContentUrl(ID);

    expect(contentUrl).to.contain(SERVICE_PATH);
    expect(contentUrl).to.contain('/' + ID);
  });

  it('should return service url', function () {
    var service = new ContentRestService({}, {
      basePath: '/api'
    });

    expect(service.getServiceUrl()).to.equal('/api' + SERVICE_PATH);
  });

  it('should perform request with correct url', function () {
    let restClient = {};
    restClient.get = sinon.spy();
    var service = new ContentRestService({}, restClient);

    service.getContent('emf:content-id');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/content/emf:content-id');
  });

  it('should return full embedded image url', function () {
    let embeddedId = 'emf112233';
    let tenantId = 'tenant.com';
    let restClient = {};
    restClient.get = sinon.spy();
    let windowAdapter = mockWindowAdapter();

    let service = new ContentRestService({}, restClient, windowAdapter);

    let result = service.getImageUrl(embeddedId, tenantId);
    let expected = `http://10.131.2.162:5000/remote/api/content/static/emf112233?tenant=tenant.com`;
    expect(result).to.equal(expected);
  });


  function mockWindowAdapter() {
    return {
      location: {
        origin: 'http://10.131.2.162:5000'
      }
    };
  }
});
