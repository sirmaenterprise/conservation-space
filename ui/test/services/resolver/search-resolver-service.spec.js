import {SearchResolverService} from 'services/resolver/search-resolver-service';

import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('SearchResolverService', () => {

  var searchResolverService;
  beforeEach(() => {
    var pluginsService = mockPluginsService();
    var promiseAdapter = PromiseAdapterMock.mockAdapter();
    searchResolverService = new SearchResolverService(pluginsService, promiseAdapter);
  });

  it('should load the available resolvers if not initialized', (done) => {
    delete searchResolverService.resolvers;
    searchResolverService.resolve({}, {}).then(() => {
      expect(searchResolverService.pluginsService.loadPluginServiceModules.calledOnce).to.be.true;
      expect(searchResolverService.resolvers).to.exist;
      done();
    }).catch(done);
  });

  it('should not load the available resolvers if already initialized', (done) => {
    searchResolverService.resolvers = [mockResolvers()['resolver']];
    searchResolverService.resolve({}, {}).then(() => {
      expect(searchResolverService.pluginsService.loadPluginServiceModules.called).to.be.false;
      done();
    }).catch(done);
  });

  it('should resolve with the provided tree and context', (done) => {
    var tree = {condition: 'OR', rules: []};
    var context = {
      getCurrentObject: ()=> {
      }
    };
    searchResolverService.resolve(tree, context).then(() => {
      expect(searchResolverService.resolvers[0].resolve.calledOnce).to.be.true;
      expect(searchResolverService.resolvers[0].resolve.getCall(0).args[0]).to.equal(tree);
      expect(searchResolverService.resolvers[0].resolve.getCall(0).args[1]).to.equal(context);
      done();
    }).catch(done)
  });

  function mockPluginsService() {
    return {
      loadPluginServiceModules: sinon.spy(() => {
        return Promise.resolve(mockResolvers());
      })
    };
  }

  function mockResolvers() {
    return {
      'resolver': {
        resolve: sinon.spy(() => {
          return Promise.resolve();
        })
      }
    };
  }

});