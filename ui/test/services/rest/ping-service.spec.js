import {PingService, SERVICE_URL} from 'services/rest/ping-service';

describe('PingService', () => {

  describe('ping()', () => {

    it('should make a http call to ping service', () => {
      var restClient = {get: sinon.spy()};
      var service = new PingService(restClient);

      service.ping();

      expect(restClient.get.called).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.eq(SERVICE_URL);
    });
  });
});