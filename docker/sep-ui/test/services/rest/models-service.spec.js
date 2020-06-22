import {ModelsService} from 'services/rest/models-service';
import {RestClient} from 'services/rest-client';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {
  SELECTION_MODE_BOTH,
  SELECTION_MODE_IN_CONTEXT,
  SELECTION_MODE_WITHOUT_CONTEXT
} from 'components/contextselector/context-selector';

describe('ModelsService', function () {

  let modelsService;
  let restClientStub;

  beforeEach(() => {
    restClientStub = stub(RestClient);
    modelsService = new ModelsService(restClientStub, new RequestsCacheService());
  });

  describe('getModels', function () {
    it('should properly pass request parameters', function () {
      const PURPOSE = 'upload';
      const EXTENSION = 'root-node/current-node';
      const MIME_TYPE = 'text/plain';
      const CONTEXT = 'test123';
      const CLASS_FILTER = ['testClass'];

      restClientStub.get.onCall(0).returns(Promise.resolve({}));

      let modelsService = new ModelsService(restClientStub, new RequestsCacheService());

      modelsService.getModels(PURPOSE, CONTEXT, MIME_TYPE, EXTENSION, CLASS_FILTER);

      let callParams = restClientStub.get.getCall(0).args[1].params;
      expect(callParams.purpose).to.equal(PURPOSE);
      expect(callParams.extension).to.equal(EXTENSION);
      expect(callParams.mimetype).to.equal(MIME_TYPE);
      expect(callParams.contextId).to.equal(CONTEXT);
      expect(callParams.classFilter).to.equal(CLASS_FILTER);
    });

    it('should properly pass request parameters when getting class info', function () {
      const URI = 'sampleURI';

      modelsService.getClassInfo(URI);

      let callParams = restClientStub.get.getCall(0).args[1].params;
      expect(callParams.id).to.equal(URI);
    });
  });

  it('should fetch ontologies', function () {
    let restClient = stub(RestClient);
    let modelsService = new ModelsService(restClient);

    let response = {
      data: [{id: 'ontology1'}]
    };

    restClient.get.withArgs('/models/ontologies').returns(PromiseStub.resolve(response));

    let fetchedOntologies;
    modelsService.getOntologies().then(onotlogies => fetchedOntologies = onotlogies);
    expect(fetchedOntologies).to.eql(response.data);
  });

  it('should provide models import url', function () {
    let modelsService = new ModelsService({
      basePath: ''
    });

    expect(modelsService.getDefinitionImportUrl()).to.equals('/models/import');
  });

  it('should provide ontology import url', function () {
    let modelsService = new ModelsService({
      basePath: ''
    });

    expect(modelsService.getOntologyImportUrl('tenant.test')).to.equals('/tenant/upload/ontology/tenant.test');
  });

  it('should provide currently imported models', () => {
    let response = {
      data: {models: [], definitions: []}
    };

    restClientStub.get.withArgs('/models/imported').returns(PromiseStub.resolve(response));

    let resultData;
    modelsService.getImportedModels().then(result => resultData = result);

    expect(resultData).to.eql(response.data);
  });

  it('should download selected models', () => {
    let downloadRequest = {
      templates: [],
      definitions: []
    };

    let response = {
      data: ['1', '2']
    };

    response.headers = sinon.stub();
    response.headers.withArgs('x-file-name').returns('models.zip');

    restClientStub.post.withArgs('/models/download', downloadRequest).returns(PromiseStub.resolve(response));

    let resultData;
    modelsService.download(downloadRequest).then(result => resultData = result);

    expect(resultData).to.eql({
      data: response.data,
      fileName: 'models.zip'
    });
  });

  describe('getExistingInContextInfo', () => {

    const DEFINITION_ID_BOTH = 'CA_1';
    const DEFINITION_ID_WITHOUT_CONTEXT = 'CA_2';
    const DEFINITION_ID_IN_CONTEXT = 'CA_3';
    const EXISTING_IN_CONTEXT_URL = '/models/existing-in-context';

    it('should return existing in context value of definition with definitionId passed as argument', () => {
      setupResponse(DEFINITION_ID_BOTH, SELECTION_MODE_BOTH);
      setupResponse(DEFINITION_ID_WITHOUT_CONTEXT, SELECTION_MODE_WITHOUT_CONTEXT);
      setupResponse(DEFINITION_ID_IN_CONTEXT, SELECTION_MODE_IN_CONTEXT);

      testData.forEach((data) => {
        modelsService.getExistingInContextInfo(data.definitionId).then((response) => {
          expect(response).to.equal(data.existingInContext);
        });
      });
    });

    let testData = [
      {
        definitionId: DEFINITION_ID_BOTH,
        existingInContext: SELECTION_MODE_BOTH
      },
      {
        definitionId: DEFINITION_ID_WITHOUT_CONTEXT,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT
      },
      {
        definitionId: DEFINITION_ID_IN_CONTEXT,
        existingInContext: SELECTION_MODE_IN_CONTEXT
      }
    ];

    function setupResponse(definitionId, response) {
      restClientStub.get.withArgs(EXISTING_IN_CONTEXT_URL, createRequestParam(definitionId)).returns(PromiseStub.resolve({data:response}));
    }

    function createRequestParam(definitionId) {
      return {
        params: {
          definitionId
        }
      };
    }
  });
});