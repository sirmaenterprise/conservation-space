import {RelationshipsService} from 'services/rest/relationships-service';

describe('Relationship Service ', ()=> {
  var restClient = {};
  var service = new RelationshipsService(restClient);
  describe('Find()', ()=> {
    it('should substitute q param with empty string when is undefined', ()=> {
      restClient.get = sinon.spy();
      let opts = {q: undefined};
      service.find(opts);
      expect(restClient.get.calledOnce);
      expect(restClient.get.getCall(0).args[0]).to.equal('/relationships');
      expect(restClient.get.getCall(0).args[1].params.q).to.equal('');
    });
    it('should call the rest service with proper configuration', ()=> {
      restClient.get = sinon.spy();
      let opts = {q: 'test'};
      service.find(opts);
      expect(restClient.get.calledOnce);
      expect(restClient.get.getCall(0).args[0]).to.equal('/relationships');
      expect(restClient.get.getCall(0).args[1].params.q).to.equal('test');
    });
  });
});