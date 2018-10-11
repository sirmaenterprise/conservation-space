import {InstanceShareRestService} from 'services/rest/instance-share-service'
import {HEADER_V2_JSON} from 'services/rest-client';

describe('InstanceShareService', () => {
  let shareServiceUrl = '/instances/content/share';
  let restClient = {
    get: sinon.spy(),
    post: sinon.spy()
  };
  let instanceShareService = new InstanceShareRestService(restClient);

  beforeEach(() => {
    restClient.get.reset();
    restClient.post.reset();
  });

  describe('#shareLinks', () => {
    it('should send request with proper config', () => {
      let ids = ['test-id-1', 'test-id-2', 'test-id-3'];
      instanceShareService.shareLinks({id: ids});
      let getRequest = restClient.get.getCall(0);
      expect(getRequest.args[0]).to.equal(shareServiceUrl);
      expect(getRequest.args[1]).to.eql
      ({
        headers: {
          'Accept': HEADER_V2_JSON,
          'Content-Type': HEADER_V2_JSON
        },
        params: {
          id: ids,
          contentFormat: 'word'
        }
      });
    });
  });

  describe('#triggerShare', () => {
    it('it should make post request with proper arguments', () => {
      let instanceLinks = ['test-id-1', 'test-id-2', 'test-id-3'];
      instanceShareService.triggerShare(instanceLinks);
      let postRequest = restClient.post.getCall(0);
      expect(postRequest.args[0]).to.equal(shareServiceUrl);
      expect(postRequest.args[1]).to.equal(instanceLinks);
    })
  });
});