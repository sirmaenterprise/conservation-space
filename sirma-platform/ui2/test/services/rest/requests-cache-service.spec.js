import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {PromiseStub} from 'test/promise-stub';

describe('RequestsCacheService', () => {
  var requestsService = new RequestsCacheService();

  describe('hash', () => {
    it('should compare equals arrays hash', () => {
      let array1 = [1, 2, 3, '4'];
      let array2 = [1, 2, 3, '4'];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.true;
    });

    it('should compare nested arrays hash', () => {
      let array1 = [1, 2, 3, ['4']];
      let array2 = [1, 2, 3, ['4']];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.true;
    });

    it('should compare random equals arrays hash', () => {
      let array1 = [1, 2, 3, 4];
      let array2 = [3, 2, 1, 4];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.false;
    });

    it('should compare random equals arrays hash with duplicates', () => {
      let array1 = [1, 2, 3, ['4'], ['4']];
      let array2 = [3, 2, 1, ['4'], 1];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.false;
    });

    it('should compare random equals arrays hash with objects', () => {
      let array1 = [1, 2, 3, ['4'], {'prop': [3, 3, 1, 2], 'prop2': 'abc'}];
      let array2 = [3, 2, {'prop2': 'abc', 'prop': [1, 2, 3, 1]}, 1, ['4'], 1];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.false;
    });

    it('should compare random equals arrays hash with null and undefined', () => {
      let array1 = [1, undefined, 3, ['4'], {'prop': [3, 2, 2, 1, null, 1], 'prop2': 'abc'}];
      let array2 = [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1];

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.false;
    });

    it('should compare random equals objects hash with null and undefined', () => {
      let array1 = {'a': [1, undefined, 3, ['4'], {'prop': [3, 2, 2, 1, null, 1], 'prop2': 'abc'}]};
      let array2 = {'a': [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1]};
      let array3 = {'b': [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1]};

      expect(requestsService.getHash(array1, true) === requestsService.getHash(array2, true)).to.be.true;
      expect(requestsService.getHash(array2, true) === requestsService.getHash(array3, true)).to.be.false;
      expect(requestsService.getHash(array1, false) === requestsService.getHash(array2, false)).to.be.false;
    });
  });
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
      map.set('56cb4ab4', 'request is already added');

      let request = () => {
        return new PromiseStub.resolve('123');
      };

      requestsService.cache('url', 'params', map, request, true);
      requestsService.cache('url', 'params', map, request, true);
      requestsService.cache('url', 'params', map, request, true);

      expect(map.size).to.equal(1);
      expect(map.set.callCount).to.equal(1);
      expect(map.delete.callCount).to.equal(0);
      expect(map.get('56cb4ab4')).to.equal('request is already added');
    });
  });
});

function spyMap() {
  let map = new Map();
  sinon.spy(map, 'set');
  sinon.spy(map, 'delete');
  return map;
}