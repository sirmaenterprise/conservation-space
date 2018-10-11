import {SearchResponse} from 'services/rest/response/search-response';
import {HEADER_V2_JSON} from 'services/rest-client';
import {InstanceRestService} from 'services/rest/instance-service';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {PromiseStub} from 'test/promise-stub';
import {RestClient} from 'services/rest-client';
import {stub} from 'test/test-utils';

describe('InstanceRestService', () => {
  let restClient;
  let instanceRestService;

  beforeEach(function () {
    restClient = stub(RestClient);
    restClient.getUrl = (url) => url;

    instanceRestService = new InstanceRestService(restClient, PromiseStub, new RequestsCacheService());
  });

  it('compareVersions() should perform create request with proper arguments', () => {
    let instanceId = 'emf:id';
    let instanceVersionOneId = 'emf:id-v.1';
    let instanceVersionTwoId = 'emf:id-v.2';

    instanceRestService.compareVersions(instanceId, instanceVersionOneId, instanceVersionTwoId);

    let expectedDataParameter = {
      userOperation: 'compareVersions',
      firstSourceId: instanceVersionOneId,
      secondSourceId: instanceVersionTwoId
    };
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/compare-versions');
    expect(restClient.post.getCall(0).args[1]).to.deep.equal(expectedDataParameter);
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  describe('create()', () => {
    it('create() should perform create request with proper arguments ', () => {
      instanceRestService.create({'param': 'value'});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances');
      expect(restClient.post.getCall(0).args[1].param).to.equal('value');
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });

    it('create() should expand config ', () => {
      instanceRestService.create({}, {skipInterceptor: true});
      expect(restClient.post.getCall(0).args[2].skipInterceptor).to.be.true;
    });
  });

  it('update() should perform update request with proper arguments ', () => {
    instanceRestService.update('emf:123456', {'param': 'value'});
    expect(restClient.patch.calledOnce);
    expect(restClient.patch.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.patch.getCall(0).args[1].param).to.equal('value');
    expect(restClient.patch.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('load() should perform load request with proper arguments ', () => {
    restClient.get.returns(PromiseStub.promise((resolve) => {
      resolve({ data: { properties: {} } });
    }));
    instanceRestService = new InstanceRestService(restClient, PromiseStub, new RequestsCacheService());

    instanceRestService.load('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadView() should perform request with proper arguments ', () => {
    instanceRestService.loadView('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/view');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadContextPath() should perform request with proper arguments', () => {
    instanceRestService.loadContextPath('emf:123456');
    expect(restClient.get.calledOnce).to.be.true;
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/context');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  describe('loadBatch()', () => {
    beforeEach(() => {
      restClient.post.returns(PromiseStub.promise((resolve) => {
        resolve({ data: [ ]});
      }));
      instanceRestService = new InstanceRestService(restClient, PromiseStub, new RequestsCacheService());
    });

    it('should perform load request with proper arguments ', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        properties: ['title'],
        allowDeleted: false
      };

      instanceRestService.loadBatch(ids, {params: {properties: ['title']}});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });

    it('should perform load request without specifying properties and load deleted allowed ', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        properties: [],
        allowDeleted: true
      };

      instanceRestService.loadBatch(ids, {params: {deleted: true}});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });

    it('should perform load request without additional configurations', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        properties: [],
        allowDeleted: false
      };

      instanceRestService.loadBatch(ids, {});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });
  });

  it('loadBatchDeleted() should perform load request with proper arguments ', () => {
    restClient.post.returns(PromiseStub.promise((resolve) => {
      resolve({ data: [ ]});
    }));
    instanceRestService = new InstanceRestService(restClient, PromiseStub, new RequestsCacheService());

    let ids = ['emf:123456', 'emf:999888'];
    let payload = {
      instanceIds: ids,
      properties: [],
      allowDeleted: true
    };

    instanceRestService.loadBatchDeleted(ids);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[1]).to.deep.eq(payload);
  });

  it('getAllowedActions() should perform getAllowedActions request with proper arguments ', () => {
    instanceRestService.getAllowedActions('emf:123456', 'document');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/actions?instanceType=document');
  });

  it('getInstanceProperty() should perform request with proper arguments', () => {
    restClient.get.returns(PromiseStub.promise((resolve) => {
      resolve({ data: [] });
    }));
    instanceRestService = new InstanceRestService(restClient, PromiseStub, new RequestsCacheService());

    instanceRestService.getInstanceProperty('emf:123', 'references', 0, 5);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123/object-properties?propertyName=references&offset=0&limit=5');
  });

  it('deleteInstance() should perform delete request with proper arguments ', () => {
    instanceRestService.deleteInstance('emf:123456');
    expect(restClient.deleteResource.calledOnce);
    expect(restClient.deleteResource.getCall(0).args[0]).to.equal('/instances/emf:123456');
    expect(restClient.deleteResource.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getTooltip() should perform request with proper arguments ', () => {
    instanceRestService.getTooltip('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/tooltip');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  describe('preview()', () => {
    it('should make a call to the correct url', () => {
      instanceRestService.preview('emf:123');

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123/content/preview');
    });

    it('should make a call with correct response type', () => {
      instanceRestService.preview('emf:123');

      let expected = {
        responseType: 'arraybuffer'
      };
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[1]).to.contains(expected);
    });
  });

  it('loadModel() should perform request with proper arguments ', () => {
    instanceRestService.loadModel('emf:123456', 'editDetails');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/model');
    expect(restClient.get.getCall(0).args[1].params.operation).to.equal('editDetails');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  describe('loadModels', () => {
    it('should perform request with proper arguments', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        operation: 'editDetails',
        requestedFields: ['title']
      };

      instanceRestService.loadModels(ids, 'editDetails', {params: {properties: ['title']}});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/model/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });

    it('should perform request without specified fields', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        operation: 'editDetails',
        requestedFields: []
      };

      instanceRestService.loadModels(ids, 'editDetails', {params: {}});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/model/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });

    it('should perform request without additional configurations', () => {
      let ids = ['emf:123456', 'emf:999888'];
      let payload = {
        instanceIds: ids,
        operation: 'editDetails',
        requestedFields: []
      };

      instanceRestService.loadModels(ids, 'editDetails', {});
      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/model/batch');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(payload);
      expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
    });
  });

  it('getContentUploadUrl() should provide the instance id in the path', function () {
    const ID = 'test123';
    expect(instanceRestService.getContentUploadUrl(ID)).to.contain(ID + '/content');
  });

  it('createDraft() should perform request with proper arguments ', () => {
    instanceRestService.createDraft('emf:123456', 'content');
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.post.getCall(0).args[1]).to.equal('content');
    expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('loadDraft() should perform request with proper arguments ', () => {
    instanceRestService.loadDraft('emf:123456');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.get.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('deleteDraft() should perform request with proper arguments ', () => {
    instanceRestService.deleteDraft('emf:123456');
    expect(restClient.deleteResource.calledOnce);
    expect(restClient.deleteResource.getCall(0).args[0]).to.equal('/instances/emf:123456/drafts');
    expect(restClient.deleteResource.getCall(0).args[1].headers.Accept).to.equal('application/vnd.seip.v2+json');
  });

  it('getVersions() should perform request with proper arguments', () => {
    instanceRestService.getVersions('emf:123456', 0, 10);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123456/versions');
    expect(restClient.get.getCall(0).args[1].params.offset).to.equal(0);
    expect(restClient.get.getCall(0).args[1].params.limit).to.equal(10);
  });

  describe('loadAuditDataForInstances(identifiers, limit, offset, dataRange)', function () {
    beforeEach(() => {
      restClient.post.returns(Promise.resolve({}));
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
      instanceRestService.cloneProperties('emf:123');
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:123/actions/clone');
    });
  });

  describe('cloneInstance', () => {
    it('should perform POST to the correct service', () => {
      let data = {
        properties: {}
      };
      instanceRestService.cloneInstance('emf:123', data);

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:123/actions/clone');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(data);
    });
  });

  it('getContentPreviewUrl should return correct URL with purpose', () => {
    expect(instanceRestService.getContentPreviewUrl('emf:123456', 'primaryContent')).to.equals('/instances/emf:123456/content/preview?purpose=primaryContent');
  });

  it('getContentDownloadUrl should return correct URL', () => {
    expect(instanceRestService.getContentDownloadUrl('emf:123456')).to.equals('/instances/emf:123456/content?download=true');
  });

  describe('loadInstanceObject', () => {
    it('should load instance and definition models and return InstanceObject instance', () => {
      let instanceModel = getInstanceModel();
      restClient.get.withArgs('/instances/id1', {
        'headers': {
          'Accept': 'application/vnd.seip.v2+json',
          'Content-Type': 'application/vnd.seip.v2+json'
        }
      })
        .returns(PromiseStub.resolve({data: instanceModel}));

      restClient.get.withArgs('/instances/id1/model', {
        'headers': {
          'Accept': 'application/vnd.seip.v2+json',
          'Content-Type': 'application/vnd.seip.v2+json'
        }, 'params': {'operation': 'editDetails'}
      })
        .returns(PromiseStub.resolve({data: getDefinitionModel()}));

      instanceRestService.loadInstanceObject('id1', 'editDetails').then(instanceObject => {
        expect(instanceObject !== undefined).to.be.true;
        expect(instanceObject.getId()).to.equal('id1');
        let models = instanceObject.getModels();
        expect(models.id).to.equal('id1');
        expect(models.validationModel).to.be.present;
        expect(models.viewModel).to.be.present;
        expect(models.headers).to.be.present;
      });
    });
  });

  describe('loadDefaults()', () => {
    it('should perform GET request with proper arguments', () => {
      restClient.get.returns(PromiseStub.resolve({data: {}}));
      instanceRestService.loadDefaults('OT270001', 'emf:1');
      expect(restClient.get.getCall(0).args).to.eql(['/instances/defaults', {
        headers: {
          'Accept': 'application/vnd.seip.v2+json',
          'Content-Type': 'application/vnd.seip.v2+json'
        },
        params: {
          'definition-id': 'OT270001',
          'parent-instance-id': 'emf:1'
        }
      }]);
    });

    it('should decorate response with isNewInstance flag', () => {
      restClient.get.returns(PromiseStub.resolve({data: {}}));
      instanceRestService.loadDefaults('OT270001', 'emf:1').then((response) => {
        expect(response.data.isNewInstance).to.be.true;
      });
    });
  });
});

function getInstanceModel() {
  return {
    'id': 'emf:4abeec55-f4fd-4746-a925-ab6a9d7bd599',
    'definitionId': 'PR0001',
    'readAllowed': true,
    'writeAllowed': true,
    'headers': {
      'breadcrumb_header': '\n<span ><img src=\"/images/instance-icons/projectinstance-icon-16.png\"/></span><span><a class=\"instance-link has-tooltip\" href=\"#/idoc/emf:4abeec55-f4fd-4746-a925-ab6a9d7bd599\" uid=\"1\"><span data-property=\"title\">pr1</span></a></span>',
      'compact_header': '\n<span ><img src=\"/images/instance-icons/projectinstance-icon-16.png\"/></span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"#/idoc/emf:4abeec55-f4fd-4746-a925-ab6a9d7bd599\" uid=\"1\"><span data-property=\"identifier\">1</span>&nbsp;\n(<span data-property=\"type\">Main Project</span>)&nbsp;\n<span data-property=\"title\">pr1</span>&nbsp;\n(<span data-property=\"status\">Submitted</span>)&nbsp;\n</a></span>\n',
      'default_header': '\n<span><img src=\"/images/instance-icons/projectinstance-icon-64.png\"/></span><span><span class=\"truncate-element\"><a class=\"instance-link\" href=\"#/idoc/emf:4abeec55-f4fd-4746-a925-ab6a9d7bd599\" uid=\"1\"><b><span data-property=\"identifier\">1</span>&nbsp;\n(<span data-property=\"type\">Main Project</span>)&nbsp;\n<span data-property=\"title\">pr1</span>&nbsp;\n(<span data-property=\"status\">Submitted</span>)&nbsp;\n</b></a></span><br /><span>\nCreated on:&nbsp;<span data-property=\"createdOn\"><span data-property=\"createdOn\" data-format=\"dd.MM.yyyy, HH:mm\">08.01.2018, 14:19</span></span></span></span>'
    },
    'instanceType': 'projectinstance',
    'properties': {}
  };
}

function getDefinitionModel() {
  return {
    validationModel: {},
    viewModel: {
      fields: []
    }
  };
}
