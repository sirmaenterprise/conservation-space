import {SearchResponse} from 'services/rest/response/search-response';
import {HEADER_V2_JSON} from 'services/rest-client';
import {InstanceRestService} from 'services/rest/instance-service';

describe('InstanceRestService', function () {
  let restClient;
  let instanceRestService;

  beforeEach(function () {
    restClient = {};
    instanceRestService = new InstanceRestService(restClient);
  });

  it('create() should perform create request with proper arguments ', () => {
    restClient.post = sinon.spy();
    instanceRestService.create({'param': 'value'});
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances');
    expect(restClient.post.getCall(0).args[1].param).to.equal('value');
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('create() should expand config ', () => {
    restClient.post = sinon.spy();
    instanceRestService.create({}, {skipInterceptor: true});
    expect(restClient.post.getCall(0).args[2].skipInterceptor).to.be.true;
  });

  it('update() should perform update request with proper arguments ', () => {
    restClient.patch = sinon.spy();
    instanceRestService.update('emf:123456', {'param': 'value'});
    expect(restClient.patch.calledOnce);
    expect(restClient.patch.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.patch.getCall(0).args[1].param).to.equal('value');
    expect(restClient.patch.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('load() should perform load request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.load('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadView() should perform request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.loadView('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/view');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadContextPath() should perform request with proper arguments', () => {
    restClient.get = sinon.spy();
    instanceRestService.loadContextPath('emf:123456');
    expect(restClient.get.calledOnce).to.be.true;
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/context');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadBatch() should perform load request with proper arguments ', () => {
    restClient.post = sinon.spy();
    let ids = ['emf:123456', 'emf:999888'];
    instanceRestService.loadBatch(ids);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/batch');
    expect(restClient.post.getCall(0).args[1]).to.equal(ids);
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getAllowedActions() should perform getAllowedActions request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.getAllowedActions('emf:123456', 'document');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/actions?instanceType=document');
  });

  it('deleteInstance() should perform delete request with proper arguments ', () => {
    restClient.deleteResource = sinon.spy();
    instanceRestService.deleteInstance('emf:123456');
    expect(restClient.deleteResource.calledOnce);
    expect(restClient.deleteResource.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.deleteResource.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getTooltip() should perform request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.getTooltip('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/tooltip');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  describe('preview()', () => {
    it('should make a call to the correct url', () => {
      restClient.get = sinon.spy();
      instanceRestService.preview('emf:123');

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123/content/preview');
    });

    it('should make a call with correct response type', () => {
      restClient.get = sinon.spy();
      instanceRestService.preview('emf:123');

      var expected = {
        responseType: 'arraybuffer'
      };
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[1]).to.contains(expected);
    });
  });

  it('loadModel() should perform request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.loadModel('emf:123456', 'editDetails');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/model');
    expect(restClient.get.getCall(0).args[1].params.operation).to.equal('editDetails');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadModels() should perform request with proper argguments', () => {
    restClient.post = sinon.spy();
    let ids = ['emf:123456', 'emf:999888'];
    instanceRestService.loadModels(ids, 'editDetails');
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/model/batch');
    expect(restClient.post.getCall(0).args[1]).to.equal(ids);
    expect(restClient.post.getCall(0).args[2].params.operation).to.equal('editDetails');
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getContentUploadUrl() should provide the instance id in the path', function () {
    const ID = 'test123';
    restClient.getUrl = function (url) {
      return url;
    };
    expect(instanceRestService.getContentUploadUrl(ID)).to.contain(ID + '/content');
  });

  it('createDraft() should perform request with proper arguments ', () => {
    restClient.post = sinon.spy();
    instanceRestService.createDraft('emf:123456', 'content');
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.post.getCall(0).args[1]).to.equal('content');
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadDraft() should perform request with proper arguments ', () => {
    restClient.get = sinon.spy();
    instanceRestService.loadDraft('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('deleteDraft() should perform request with proper arguments ', () => {
    restClient.deleteResource = sinon.spy();
    instanceRestService.deleteDraft('emf:123456');
    expect(restClient.deleteResource.calledOnce);
    expect(restClient.deleteResource.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.deleteResource.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getVersions() should perform request with proper arguments', () => {
    restClient.get = sinon.spy();
    instanceRestService.getVersions('emf:123456', 0, 10);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/versions');
    expect(restClient.get.getCall(0).args[1].params.offset).to.equal(0);
    expect(restClient.get.getCall(0).args[1].params.limit).to.equal(10);
  });

  describe('loadAuditDataForInstances(identifiers, limit, offset, dataRange)', function () {
    beforeEach(function () {
      restClient.post = sinon.spy((url, config) => {
        return Promise.resolve({});
      });
    });

    it('should call rest client with provided params', function () {
      instanceRestService.loadAuditDataForInstances([1], 2, 3, 'dateRange');

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.eq('/instances/history/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.eq({
        instanceIds: [1],
        limit: 2,
        offset: 3,
        dateRange: 'dateRange'
      });
      expect(restClient.post.getCall(0).args[2]).to.deep.eq({
        headers: {
          'Accept': HEADER_V2_JSON,
          'Content-Type': HEADER_V2_JSON
        }
      });
    });

    it('should return a SearchResponse object', function (done) {
      instanceRestService.loadAuditDataForInstances(1, 2, 3)
        .then(function (response) {
          expect(response instanceof SearchResponse).to.be.true;
          done();
        })
        .catch(done);
    });
  });

  describe('loadHistory(id, limit, offset)', function () {
    it('should call loadAuditDataForInstances(...)', function () {
      instanceRestService.loadAuditDataForInstances = sinon.spy();

      instanceRestService.loadHistory(1, 2, 3);
      expect(instanceRestService.loadAuditDataForInstances.calledOnce).to.be.true;
      expect(instanceRestService.loadAuditDataForInstances.getCall(0).args[0]).to.deep.eq([1]);
      expect(instanceRestService.loadAuditDataForInstances.getCall(0).args[1]).to.eq(2);
      expect(instanceRestService.loadAuditDataForInstances.getCall(0).args[2]).to.eq(3);
    });
  });

  describe('cloneProperties', () => {
    it('should perform GET to the correct service', () => {
      var getSpy = sinon.spy();
      restClient.get = getSpy;

      instanceRestService.cloneProperties('emf:123');
      expect(getSpy.calledOnce).to.be.true;
      expect(getSpy.getCall(0).args[0]).to.equal('/instances/emf:123/actions/clone');
    });
  });

  describe('cloneInstance', () => {
    it('should perform POST to the correct service', () => {
      var postSpy = sinon.spy();
      restClient.post = postSpy;

      var data = {
        properties: {}
      };
      instanceRestService.cloneInstance('emf:123', data);

      expect(postSpy.calledOnce).to.be.true;
      expect(postSpy.getCall(0).args[0]).to.equal('/instances/emf:123/actions/clone');
      expect(postSpy.getCall(0).args[1]).to.deep.equal(data);
    });
  });
});
