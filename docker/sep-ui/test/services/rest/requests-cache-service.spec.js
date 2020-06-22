import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {PromiseStub} from 'test/promise-stub';

describe('RequestsCacheService', () => {
  let requestsService = new RequestsCacheService();

  describe('cache', () => {
    it('should cache multiple requests if same until execution', (done) => {
      let map = spyMap();

      //Used Promise instead PromiseStub to run async without resolve
      let request1 = sinon.spy(function () {
        return new Promise((resolve) => {
        });
      });

      //Used Promise instead PromiseStub to run async without resolve
      let request2 = sinon.spy(function () {
        return new Promise((resolve) => {
        });
      });

      requestsService.cache('url', 'params', map, request1, true);
      requestsService.cache('url', 'params', map, request1, true);
      requestsService.cache('url', 'params', map, request1, true);
      requestsService.cache('url2', 'params2', map, request2, true);
      requestsService.cache('url2', 'params2', map, request2, true);
      requestsService.cache('url2', 'params2', map, request2, true);

      expect(request1.callCount).to.equal(1);
      expect(request2.callCount).to.equal(1);
      expect(map.size).to.equal(2);
      expect(map.set.callCount).to.equal(2);
      done();
    });

    it('should add to the map, execute request and erase it from the map', () => {
      let map = spyMap();
      let request = () => {
        return new PromiseStub.resolve('123');
      };

      requestsService.cache('url', 'params', map, request, true).then((result) => {
        expect(map.size).to.equal(0);
        expect(map.set.callCount).to.equal(1);
        expect(map.delete.callCount).to.equal(1);
        expect(result).to.equal('123');
      });
    });

    it('should not add to the map when hash key exists', () => {
      let map = spyMap();
      let request = () => {
        return new PromiseStub.resolve('123');
      };

      requestsService.cache('url', 'params', map, request, true).then((result) => {
        expect(map.size).to.equal(0);
        expect(map.set.callCount).to.equal(1);
        expect(map.delete.callCount).to.equal(1);
        expect(result).to.equal('123');
      });
    });

    it('should not add to the map if hash key exists', () => {
      let map = spyMap();
      map.set('56cb4ab4', PromiseStub.resolve('request is already added'));

      let request = () => {
        return new PromiseStub.resolve('123');
      };

      requestsService.cache('url', 'params', map, request, true);
      requestsService.cache('url', 'params', map, request, true);
      requestsService.cache('url', 'params', map, request, true);

      expect(map.size).to.equal(1);
      expect(map.set.callCount).to.equal(1);
      expect(map.delete.callCount).to.equal(0);
      expect(map.get('56cb4ab4')).to.eventually.equal('request is already added');
    });
  });
});

function spyMap() {
  let map = new Map();
  sinon.spy(map, 'set');
  sinon.spy(map, 'delete');
  return map;
}