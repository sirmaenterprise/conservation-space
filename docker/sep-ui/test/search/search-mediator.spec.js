import {SearchMediator, EVENT_BEFORE_SEARCH} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchServiceMock} from 'test/services/rest/search-service-mock';

describe('SearchMediator', () => {

  it('should assign the provided arguments map', () => {
    var map = {'offset': 10};
    var mediator = new SearchMediator({}, {}, undefined, map);
    expect(mediator.arguments).to.deep.equal(map);
  });

  it('should initialize the arguments map if none is provided', () => {
    var mediator = new SearchMediator({}, {});
    expect(mediator.arguments).to.deep.equal({});
  });

  describe('registerListener(eventName, listener)', () => {
    let mediator;
    let testListener1 = () => {
    };
    let testListener2 = () => {
    };

    beforeEach(() => mediator = new SearchMediator({}, {}, {}));

    it('should create a new list of listeners', () => {
      mediator.registerListener('test', testListener1);

      let listeners = mediator.listeners['test'];
      expect(listeners).to.be.ok;
      expect(listeners.length).to.be.eq(1);
      expect(listeners[0]).to.be.eq(testListener1);
    });

    it('should append to an existing list of listeners', () => {
      mediator.registerListener('test', testListener1);
      mediator.registerListener('test', testListener2);

      let listeners = mediator.listeners['test'];
      expect(listeners).to.be.ok;
      expect(listeners.length).to.be.eq(2);
      expect(listeners[0]).to.be.eq(testListener1);
      expect(listeners[1]).to.be.eq(testListener2);
    });
  });

  describe('trigger(eventName, data)', () => {

    it('should invoke registered listeners', () => {
      let mediator = new SearchMediator({}, {}, {});
      let spy = sinon.spy();
      mediator.registerListener('test', spy);

      mediator.trigger('test', 'test data');
      expect(spy.calledOnce);
      expect(spy.getCall(0).args[0]).to.equal('test data');
    });
  });

  describe('addCriteria(rule, parentId)', () => {
    it('should delegate to QueryBuilder and trigger before and after events', () => {
      let query = {add: sinon.spy()};
      let mediator = new SearchMediator(null, query, {});
      let beforeSpy = sinon.spy();
      let afterSpy = sinon.spy();
      let eventData = {rule: 'rule', parentId: 'parent-id', query: query};

      mediator.registerListener('before-search-criteria-changed', beforeSpy);
      mediator.registerListener('search-criteria-changed', afterSpy);

      mediator.addCriteria('rule', 'parent-id');
      expect(query.add.calledOnce);
      expect(query.add.getCall(0).args[0]).to.equal('rule');
      expect(query.add.getCall(0).args[1]).to.equal('parent-id');

      expect(beforeSpy.calledOnce);
      expect(beforeSpy.getCall(0).args[0]).to.deep.equal(eventData);

      expect(afterSpy.calledOnce);
      expect(afterSpy.getCall(0).args[0]).to.deep.equal(eventData);

      expect(afterSpy.calledAfter(beforeSpy));
    });
  });

  describe('removeCriteria(rule)', () => {
    it('should delegate to QueryBuilder and trigger before and after events', () => {
      let query = {remove: sinon.spy()};
      let mediator = new SearchMediator(null, query, {});
      let beforeSpy = sinon.spy();
      let afterSpy = sinon.spy();
      let eventData = {rule: 'rule', query: query};

      mediator.registerListener('before-search-criteria-changed', beforeSpy);
      mediator.registerListener('search-criteria-changed', afterSpy);

      mediator.removeCriteria('rule');
      expect(query.remove.calledOnce);
      expect(query.remove.getCall(0).args[0]).to.equal('rule');

      expect(beforeSpy.calledOnce);
      expect(beforeSpy.getCall(0).args[0]).to.deep.equal(eventData);

      expect(afterSpy.calledOnce);
      expect(afterSpy.getCall(0).args[0]).to.deep.equal(eventData);

      expect(afterSpy.calledAfter(beforeSpy));
    });
  });

  describe('search()', () => {

    it('should delegate to SearchService and trigger before and after events', () => {
      let searchSpy = sinon.spy();
      let service = new SearchServiceMock();
      sinon.spy(service, 'search');
      let mediator = new SearchMediator(service, {});
      let beforeSpy = sinon.spy();
      let afterSpy = sinon.spy();

      mediator.registerListener(EVENT_BEFORE_SEARCH, beforeSpy);
      mediator.registerListener('search', afterSpy);

      mediator.search();

      expect(service.search.calledOnce);
      expect(beforeSpy.calledOnce);
      expect(afterSpy.calledOnce);

      expect(beforeSpy.calledBefore(searchSpy));
      expect(service.search.calledBefore(afterSpy));
    });

    it('should provide a search request to the search service', () => {
      let service = new SearchServiceMock();
      sinon.spy(service, 'search');
      var argumentsMap = {pageSize: 123};
      let mediator = new SearchMediator(service, {}, {}, argumentsMap);
      mediator.searchMode = SearchCriteriaUtils.BASIC_MODE;

      mediator.search();
      expect(service.search.calledOnce).to.be.true;

      var searchRequest = service.search.getCall(0).args[0];
      var expected = {
        query: {},
        arguments: argumentsMap,
        searchMode: SearchCriteriaUtils.BASIC_MODE,
        context: {},
        skipInterceptor: undefined
      };
      expect(searchRequest).to.deep.equal(expected);
    });

    it('should return search results', () => {
      let service = {
        search: () => {
          return {
            promise: new Promise((resolve) => {
              return resolve('search response');
            })
          }
        }
      };
      let mediator = new SearchMediator(service);
      mediator.searchMode = 'search-mode';
      mediator.arguments = {
        pageSize: 25,
        pageNumber: 1
      };

      let expected = {
        response: 'search response',
        query: undefined,
        arguments: mediator.arguments,
        searchMode: mediator.searchMode
      };
      let searchResult = mediator.search();
      return expect(searchResult).to.eventually.deep.equal(expected);
    });

    it('should return empty search results if an error occurs', (done) => {
      let service = {
        search: () => {
          return {
            promise: Promise.reject('search failed')
          }
        }
      };
      let mediator = new SearchMediator(service, {});
      let searchResult = mediator.search();

      searchResult.then((result) => {
        var expectedResponse = {
          data: {
            values: [],
            resultSize: 0
          }
        };
        expect(result.response).to.deep.equal(expectedResponse);
        done();
      }).catch(done);
    });

    it('should return the error if it occurs', (done) => {
      let service = {
        search: () => {
          return {
            promise: Promise.reject('search failed')
          }
        }
      };
      let mediator = new SearchMediator(service, {});
      let searchResult = mediator.search();

      searchResult.then((result) => {
        expect(result.error).to.equal('search failed');
        done();
      }).catch(done);
    });

  });

  describe('abortLastSearch', () => {

    it('should not abort if no search requests are made', () => {
      let mediator = new SearchMediator();
      mediator.abortLastSearch();

      expect(mediator.lastSearchRequest).to.not.exist;
    });

    it('should abort last made search', () => {
      let mediator = new SearchMediator();
      mediator.lastSearchRequest = {
        resolve: sinon.spy()
      };

      mediator.abortLastSearch();

      expect(mediator.lastSearchRequest.resolve.calledOnce).to.be.true;
    });

  });

  describe('SearchMediator static methods', () => {

    it('buildSearchResponse() should properly build search response', () => {
      let searchMode = 'mode';

      let argumentsMap = {
        pageSize: 25,
        pageNumber: 1
      };

      let query = {
        tree: {}
      };

      let response = {
        data: {
          values: [],
          resultSize: 0
        }
      };

      expect(SearchMediator.buildSearchResponse(query, response, argumentsMap, searchMode)).to.deep.eq({
        query: query,
        response: response,
        arguments: argumentsMap,
        searchMode: searchMode
      });
    });

    it('buildEmptySearchResults() should properly build empty search result config', () => {
      expect(SearchMediator.buildEmptySearchResults()).to.deep.eq({
        data: {
          values: [],
          resultSize: 0
        }
      });
    });

    it('isSearchResultEmpty() should properly work on empty values & 0 resultSize', () => {
      expect(SearchMediator.isSearchResultEmpty({
        data: {
          values: [],
          resultSize: 0
        }
      })).to.be.true;
    });

    it('isSearchResultEmpty() should properly work on 0 resultSize', () => {
      expect(SearchMediator.isSearchResultEmpty({
        data: {
          values: [1, 2, 3],
          resultSize: 0
        }
      })).to.be.true;
    });

    it('isSearchResultEmpty() should properly work on empty values', () => {
      expect(SearchMediator.isSearchResultEmpty({
        data: {
          values: [],
          resultSize: 20
        }
      })).to.be.true;
    });


    it('isSearchResultEmpty() should properly work on empty values', () => {
      expect(SearchMediator.isSearchResultEmpty({
        data: {
          values: [1, 2, 3],
          resultSize: 20
        }
      })).to.be.false;
    });

    it('isSearchQueryEmpty() should properly work on empty query', () => {
      expect(SearchMediator.isSearchQueryEmpty({})).to.be.true;
      expect(SearchMediator.isSearchQueryEmpty({tree: {}})).to.be.false;
    });

    it('isSearchPayloadEmpty() should properly work on completely empty response', () => {
      let response = {
        query: {},
        response: {
          data: {
            values: [],
            resultSize: 0
          }
        }
      };

      expect(SearchMediator.isSearchPayloadEmpty(response)).to.be.true;
    });

    it('isSearchPayloadEmpty() should properly work on empty query response', () => {
      let response = {
        query: {},
        response: {
          data: {
            values: [1, 2, 3],
            resultSize: 20
          }
        }
      };

      expect(SearchMediator.isSearchPayloadEmpty(response)).to.be.true;
    });

    it('isSearchPayloadEmpty() should properly work on empty results response', () => {
      let response = {
        query: {
          tree: {}
        },
        response: {
          data: {
            values: [1, 2, 3],
            resultSize: 0
          }
        }
      };

      expect(SearchMediator.isSearchPayloadEmpty(response)).to.be.true;
    });

    it('isSearchPayloadEmpty() should properly work on empty values response', () => {
      let response = {
        query: {
          tree: {}
        },
        response: {
          data: {
            values: [],
            resultSize: 300
          }
        }
      };

      expect(SearchMediator.isSearchPayloadEmpty(response)).to.be.true;
    });

    it('isSearchPayloadEmpty() should properly work on non empty response', () => {
      let response = {
        query: {
          tree: {}
        },
        response: {
          data: {
            values: [1, 2, 3],
            resultSize: 300
          }
        }
      };

      expect(SearchMediator.isSearchPayloadEmpty(response)).to.be.false;
    });

    it('isSearchPayloadEmpty() should properly work on empty object response', () => {
      expect(SearchMediator.isSearchPayloadEmpty({})).to.be.true;
    });

    it('isSearchPayloadEmpty() should properly work on undefined response', () => {
      expect(SearchMediator.isSearchPayloadEmpty()).to.be.true;
    });

  });
});

