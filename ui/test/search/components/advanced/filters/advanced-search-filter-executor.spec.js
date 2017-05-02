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

  describe('filterOperators()', () => {
    var filterExecutor;
    var resolveSpy;

    beforeEach(() => {
      resolveSpy = sinon.spy();
      filterExecutor = new AdvancedSearchFilterExecutor(mockPromiseAdapter(resolveSpy));
    });

    it('should return null if operators are null', () => {
      filterExecutor.filterOperators({}, {}, null);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.null;
    });

    it('should return empty if operators are empty', () => {
      filterExecutor.filterOperators({}, {}, []);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.empty;
    });

    it('should return what is passes if there are no filters (null)', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb(null);
      };

      filterExecutor.filterOperators({}, {}, 'test');

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.eq('test');
    });

    it('should return what is passes if there are no filters (empty object)', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb({});
      };

      filterExecutor.filterOperators({}, {}, 'test');

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.eq('test');
    });

    it('should filter operators based on the loaded filters', () => {
      filterExecutor.loadFilters = function (ext, cb) {
        cb({
          test: {
            filter: function (cfg, property, operator) {
              return operator.id === 1;
            }
          }
        });
      };

      filterExecutor.filterOperators({}, {}, [{id: 1}, {id: 2}]);

      expect(resolveSpy.calledOnce);
      expect(resolveSpy.getCall(0).args[0]).to.be.deep.eq([{id: 1}]);
    });
  });
});