import {DefinitionService} from 'services/rest/definition-service';
import {HEADER_V2_JSON} from 'services/rest-client';
import {PromiseStub} from 'test/promise-stub';

describe('DefinitionService', () => {

  describe('#getFields()', () => {
    it('should call the correct url', () => {
      let restClientMock = {};
      restClientMock.post = sinon.spy(() => {
        return PromiseStub.resolve({});
      });

      let requestsCacherMock = {};
      requestsCacherMock.cache = sinon.spy(() => {
        return restClientMock.post('/definition/fields', {identifiers: ['emf:Project', 'ET200001']});
      });

      let service = new DefinitionService(restClientMock, requestsCacherMock);

      let identifiers = ['emf:Project', 'ET200001'];

      service.getFields(identifiers);

      expect(restClientMock.post.callCount).to.equal(1);
      expect(restClientMock.post.getCall(0).args[0]).to.equal('/definition/fields');
      expect(restClientMock.post.getCall(0).args[1]).to.deep.equal({identifiers: identifiers});
    });
  });

  describe('#getDefinitions()', () => {
    let restClientMock;
    let requestsCacherMock;
    let service;

    beforeEach(() => {
      restClientMock = {};
      restClientMock.get = sinon.spy();
      requestsCacherMock = {};
      requestsCacherMock.cache = sinon.spy(() => {
        return restClientMock.get('/definitions', {
          headers: {
            'Accept': HEADER_V2_JSON,
            'Content-Type': HEADER_V2_JSON
          },
          params: {
            id: 'definitionId'
          }
        });
      });
      service = new DefinitionService(restClientMock, requestsCacherMock);
    });

    it('should call the correct url', () => {
      service.getDefinitions('definitionId');

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[0]).to.equal('/definitions');
    });

    it('should pass the provided parameters', () => {
      service.getDefinitions('definitionId');

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[1].params.id).to.deep.equal('definitionId');
    });

    it('should use the newer rest api version', () => {
      service.getDefinitions('definitionId');

      var expectedHeaders = {
        'Accept': 'application/vnd.seip.v2+json',
        'Content-Type': 'application/vnd.seip.v2+json'
      };
      expect(restClientMock.get.getCall(0).args[1].headers).to.deep.equal(expectedHeaders);
    });
  });
});
