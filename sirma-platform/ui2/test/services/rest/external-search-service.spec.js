import {ExternalSearchService} from 'services/rest/external-search-service';

describe('ExternalSearchService', () => {

  let service;
  beforeEach(() => {
    var restClient = {get: sinon.spy()};
    service = new ExternalSearchService(restClient, {}, {});
  });

  describe('getSystemConfiguration()', () => {
    it('should call the super getConfiguration()', () => {
      service.getConfiguration = sinon.spy();
      service.getSystemConfiguration('BAM');
      expect(service.getConfiguration.calledOnce).to.be.true;
    });

    it('should perform correct request with the provided system parameter', () => {
      service.getSystemConfiguration('BAM');
      expect(service.restClient.get.calledOnce).to.be.true;
      expect(service.restClient.get.getCall(0).args[1].params).to.deep.equal({context: 'BAM'});
    });
  });

  describe('getServiceUrl()', () => {
    it('should provide correct external search service URL', () => {
      expect(service.getServiceUrl()).to.equal('/search/external');
    });
  });

});
