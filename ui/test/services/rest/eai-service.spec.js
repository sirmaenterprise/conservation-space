import {EAIService} from 'services/rest/eai-service';
import {HEADER_V2_JSON} from 'services/rest-client';
import {PromiseStub} from 'test/promise-stub';

describe('EAIService', () => {

  var eaiService;
  beforeEach(() => {
    var restClient = {
      get: sinon.spy(() => {
        return PromiseStub.resolve({});
      })
    };
    eaiService = new EAIService(restClient);
  });

  it('should prepare default request configuration', () => {
    expect(eaiService.config).to.exist;
    expect(eaiService.config.headers).to.exist;
    expect(eaiService.config.headers['Accept']).to.equal(HEADER_V2_JSON);
    expect(eaiService.config.headers['Content-Type']).to.equal(HEADER_V2_JSON);
  });

  describe('getModels()', () => {
    it('should call the correct service endpoint for retrieving models based on given system', () => {
      eaiService.getModels('BAM');
      expect(eaiService.restClient.get.calledOnce).to.be.true;
      expect(eaiService.restClient.get.getCall(0).args[0]).to.equal('/integration/BAM/model/search/types');
    });
  });

  describe('getProperties()', () => {
    it('should call the correct service endpoint with the provided parameters', () => {
      var type = 'emfext:Image';
      eaiService.getProperties('BAM', type);
      expect(eaiService.restClient.get.calledOnce).to.be.true;

      var encodedType = encodeURIComponent(type);
      expect(eaiService.restClient.get.getCall(0).args[0]).to.equal(`/integration/BAM/model/search/${encodedType}/properties`);
    });

    it('should convert the properties to a format for the advanced search form', () => {
      var properties = [
        {type: 'datetime', text: 'Date time property'},
        {type: 'text', text: 'Text property'},
        {type: 'any', text: 'Any property'}
      ];
      eaiService.restClient.get = sinon.spy(() => {
        return PromiseStub.resolve({data: properties});
      });

      var expectedProperties = [
        {type: 'dateTime', text: 'Date time property'},
        {type: 'string', text: 'Text property', singleValued: true},
        {type: 'string', text: 'Any property', singleValued: true}
      ];
      eaiService.getProperties('BAM', 'emfext:Image').then(response => {
        expect(response).to.exist;
        expect(response.data).to.deep.equal(expectedProperties);
      });
    });
  });

  describe('getRegisteredSystems()', () => {
    it('should fetch the registered systems from the correct service endpoint', () => {
      eaiService.getRegisteredSystems();
      expect(eaiService.restClient.get.calledOnce).to.be.true;
      expect(eaiService.restClient.get.getCall(0).args[0]).to.equal('/integration');
    });
  });
});