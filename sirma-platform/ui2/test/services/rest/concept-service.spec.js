import {ConceptService} from 'services/rest/concept-service';
import {RestClient} from 'services/rest-client';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ConceptService', function() {

  let conceptService;
  let restClient;
  let eventbus;

  beforeEach(function() {
    restClient = stub(RestClient);

    let result = [{
      id: 'iron',
      title: 'Iron',
      ancestors: []
    }];

    restClient.get.returns(PromiseStub.resolve({
      data: result
    }));

    eventbus = stub(Eventbus);

    conceptService = new ConceptService(restClient, new RequestsCacheService(), eventbus);
  });

  it('should fetch concept hierarchy by scheme and broader', function() {
    let scheme = 'materials';
    let broader = 'metal';

    conceptService.getConceptHierarchy(scheme, broader).then(result => {
      expect(result[0].id).to.equals('iron');
    });

    expect(restClient.get.calledOnce).to.be.true;
    expect(restClient.get.getCall(0).args[0]).to.equal('/concept');
    expect(restClient.get.getCall(0).args[1].params).to.eql({
      scheme, broader
    });

    // assert the value gets cached
    conceptService.getConceptHierarchy(scheme, broader).then(result => {
      expect(result[0].id).to.equals('iron');
    });

    expect(restClient.get.calledOnce).to.be.true;
  });

});