import {SearchService} from 'services/rest/search-service';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {HEADER_V2_JSON} from 'services/rest-client';

import {PromiseStub} from 'test/promise-stub';

describe('SearchService', function () {
  let getSpy = sinon.spy();
  let postSpy = sinon.spy(() => {
    return PromiseStub.resolve();
  });
  let service;

  beforeEach(() => {
    getSpy.reset();
    postSpy.reset();

    service = new SearchService({
      get: getSpy,
      post: postSpy
    }, {
      // Search resolver
      resolve: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    }, {
      // Promise adapter
      resolve: sinon.spy(() => {
        return PromiseStub.resolve();
      }),
      defer: () => {
        return {
          promise: () => {}
        }
      }
    },
      new RequestsCacheService()
    );
  });

  describe('getConfiguration()', () => {
    it('should call the correct service endpoint', () => {
      service.getConfiguration();
      expect(getSpy.calledOnce).to.be.true;
      expect(getSpy.getCall(0).args[0]).to.equal('/search/configuration');
    });

    it('should apply provided parameters to the request', () => {
      var params = {'query-param': 'query-param-value'};
      service.getConfiguration(params);
      expect(getSpy.getCall(0).args[1]).to.exist;
      expect(getSpy.getCall(0).args[1].params).to.deep.equal(params);
    });
  });

  describe('search(searchRequest)', () => {
    it('should not fail if no builder is provided', () => {
      service.search();

      expect(postSpy.calledOnce);
      expect(postSpy.getCall(0).args[0]).to.eq('/search');
    });

    it('should not fail if no parameters are provided', () => {
      service.search({});
      expect(postSpy.getCall(0).args[0]).to.eq('/search');
    });

    it('should initiate search by default', ()=> {
      service.search({});

      expect(postSpy.calledOnce);
      expect(postSpy.getCall(0).args[0]).to.eq('/search');
    });

    it('should not start query url part if empty', () => {
      var request = {query: {tree: {condition: 'AND'}}};
      service.search(request);

      expect(postSpy.calledOnce);
      expect(postSpy.getCall(0).args[0]).to.eq('/search');
    });

    it('should not fail if no tree is provided', () => {
      service.search({query: undefined});

      expect(postSpy.calledOnce);
      expect(postSpy.getCall(0).args[0]).to.eq('/search');
    });

    it('should call the rest with the provided parameters', () => {
      var parameters = {
        offset: 10
      };
      var request = {query: {}, arguments: parameters};
      service.promiseAdapter.defer = () => {
        return {
          promise: 'test'
        }
      };

      var expected = {
        params: parameters,
        headers: {
          'Accept': HEADER_V2_JSON,
          'Content-Type': HEADER_V2_JSON
        },
        timeout: 'test',
        skipInterceptor: false
      };

      service.search(request);
      expect(postSpy.getCall(0).args[2]).to.deep.equal(expected);
    });

    it('should sanitize the given query tree for search', () => {
      var request = {
        query: {
          tree: {
            id: '1',
            value: 'one',
            $$hashKey: '123'
          }
        }
      };
      service.search(request);
      expect(postSpy.getCall(0).args[1].id).to.exist;
      expect(postSpy.getCall(0).args[1].$$hashKey).to.not.exist;
    });

    it('should resolve the search tree before performing the search request', () => {
      var request = {
        query: {
          tree: {
            condition: 'OR',
            rules: []
          }
        },
        context: {
          getCurrentObject: () => {
          }
        }
      };
      service.search(request);
      expect(service.searchResolverService.resolve.calledOnce).to.be.true;
      expect(service.searchResolverService.resolve.getCall(0).args[0]).to.equal(request.query.tree);
      expect(service.searchResolverService.resolve.getCall(0).args[1]).to.equal(request.context);
    });

    it('should not resolve the search tree if the search request is empty', () => {
      service.search();
      expect(service.searchResolverService.resolve.called).to.be.false;
    });

    it('should not resolve the search tree if the search request has no query', () => {
      service.search({});
      expect(service.searchResolverService.resolve.called).to.be.false;
    });

    it('should not resolve the search tree if the search request has no search tree', () => {
      service.search({
        query: {}
      });
      expect(service.searchResolverService.resolve.called).to.be.false;
    });
  });
});
