import {DefinitionService} from 'services/rest/definition-service';

describe('DefinitionService', () => {

  describe('#getTypes()', () => {
    it('should call the correct url', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      service.getTypes();

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[0]).to.equal('/definitions/types');
    });

    it('should pass the provided parameters', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      var params = {classFilter: 'emf:Image'};
      service.getTypes(params);

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[1].params).to.deep.equal(params);
    });

    it('should use the newer rest api version', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      service.getTypes();

      var expectedHeaders = {
        'Accept': 'application/vnd.seip.v2+json',
        'Content-Type': 'application/vnd.seip.v2+json'
      };
      expect(restClientMock.get.getCall(0).args[1].headers).to.deep.equal(expectedHeaders);
    });

  });

  describe('#getFields()', () => {
    it('should call the correct url', () => {
      let restClientMock = {};
      restClientMock.post = sinon.spy();
      let service = new DefinitionService(restClientMock);

      let identifiers = ['emf:Project', 'ET200001'];

      service.getFields(identifiers);

      expect(restClientMock.post.callCount).to.equal(1);
      expect(restClientMock.post.getCall(0).args[0]).to.equal('/definition/fields');
      expect(restClientMock.post.getCall(0).args[1]).to.deep.equal({identifiers: identifiers});
    });
  });


  describe('#getDefinitions()', () => {
    it('should call the correct url', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      service.getDefinitions('definitionId');

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[0]).to.equal('/definitions');
    });

    it('should pass the provided parameters', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      service.getDefinitions('definitionId');

      expect(restClientMock.get.callCount).to.equal(1);
      expect(restClientMock.get.getCall(0).args[1].params.id).to.deep.equal('definitionId');
    });

    it('should use the newer rest api version', () => {
      let restClientMock = {};
      restClientMock.get = sinon.spy();
      let service = new DefinitionService(restClientMock);

      service.getDefinitions('definitionId');

      var expectedHeaders = {
        'Accept': 'application/vnd.seip.v2+json',
        'Content-Type': 'application/vnd.seip.v2+json'
      };
      expect(restClientMock.get.getCall(0).args[1].headers).to.deep.equal(expectedHeaders);
    });
  });
});
