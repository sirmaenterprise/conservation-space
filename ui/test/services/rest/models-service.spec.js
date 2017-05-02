import {ModelsService} from 'services/rest/models-service';

describe('ModelsService', function () {

  describe('getModels', function () {
    it('should properly pass request parameters', function () {
      const PURPOSE = 'upload';
      const EXTENSION = 'root-node/current-node';
      const MIME_TYPE = 'text/plain';
      const CONTEXT = 'test123';
      const CLASS_FILTER = ['testClass'];

      var restClientStub = {};
      restClientStub.get = sinon.stub();
      restClientStub.get.onCall(0).returns(Promise.resolve({}));

      var modelsService = new ModelsService(restClientStub);

      var result = modelsService.getModels(PURPOSE, CONTEXT, MIME_TYPE, EXTENSION, CLASS_FILTER);

      var callParams = restClientStub.get.getCall(0).args[1].params;
      expect(callParams.purpose).to.equal(PURPOSE);
      expect(callParams.extension).to.equal(EXTENSION);
      expect(callParams.mimetype).to.equal(MIME_TYPE);
      expect(callParams.contextId).to.equal(CONTEXT);
      expect(callParams.classFilter).to.equal(CLASS_FILTER);
    });

    it('should properly pass request parameters when getting class info', function () {
      const URI = 'sampleURI';
      var restClientStub = {};
      restClientStub.get = sinon.stub();
      restClientStub.get.onCall(0).returns(Promise.resolve({}));
      var modelsService = new ModelsService(restClientStub);
      var result = modelsService.getClassInfo(URI);

      var callParams = restClientStub.get.getCall(0).args[1].params;
      expect(callParams.id).to.equal(URI);
    });
  });

});