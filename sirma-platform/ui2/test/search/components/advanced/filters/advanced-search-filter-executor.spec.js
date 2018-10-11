import {AdvancedSearchFilterExecutor} from 'search/components/advanced/filters/advanced-search-filter-executor';

function mockPluginService(loadPluginServiceModulesSpy, data) {
  return {
    loadPluginServiceModules: (extPoint, order) => {
      loadPluginServiceModulesSpy(extPoint, order);
      return {
        then: (cb) => {
          cb(data);
        }
      }
    }
  }
}

function mockPromiseAdapter(resolveSpy) {
  return {
    promise: (callback) => {
      callback(resolveSpy);
    }
  }
}

describe('AdvancedSearchFilterExecutor', () => {

  describe('loadFilters()', () => {

    it('should call plugin service and call the provided callback', () => {
      var spy = sinon.spy();
      var callbackSpy = sinon.spy();

      new AdvancedSearchFilterExecutor(null, mockPluginService(spy)).loadFilters('test', callbackSpy);

      expect(callbackSpy.calledOnce).to.be.true;
      expect(spy.calledOnce).to.be.true;
      expect(spy.getCall(0).args[0]).to.eq('test');
      expect(spy.getCall(0).args[1]).to.eq('component');
    });
  });

  describe('filter()', () => {
    var filterExecutor;
    var resolveSpy;

    beforeEach(() => {
      resolveSpy = sinon.spy();
      filterExecutor = new AdvancedSearchFilterExecutor(mockPromiseAdapter(resolveSpy));
    });

    it('should call loadFilters with proper extension point', () => {
      filterExecutor.loadFilters = sinon.spy();
      filterExecutor.filter({}, {}, [1,2,3], 'test');

      expect(filterExecutor.loadFilters.calledOnce);
      expect(filterExecutor.loadFilters.getCall(0).args[0]).to.eq('test');
    });

    it('should return null if collection is null', () => {
      filterExecutor.filter({}, {}, null);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.null;
    });

    it('should return empty if collection is empty', () => {
      filterExecutor.filter({}, {}, []);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.empty;
    });

    it('should return what is passes if there are no filters (null)', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb(null);
      };

      filterExecutor.filter({}, {}, 'test');

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.eq('test');
    });

    it('should return what is passes if there are no filters (empty object)', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb({});
      };

      filterExecutor.filter({}, {}, 'test');

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.eq('test');
    });

    it('should filter collection based on the loaded filters', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb({
          test: {
            filter: function (cfg, property, operator) {
              return operator.id === 1;
            }
          }
        });
      };

      filterExecutor.filter({}, {}, [{id: 1}, {id: 2}]);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.deep.eq([{id: 1}]);
    });
  });
});