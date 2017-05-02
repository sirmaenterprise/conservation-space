import {
  SavedSearchSelect,
  SAVED_SEARCH_URI,
  OPEN_SAVED_SEARCH_EVENT
} from 'search/components/saved/saved-search-select/saved-search-select';
import {SearchMediator, EVENT_CLEAR} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';

describe('SavedSearchSelect', () => {
  var savedSearchSelect;
  beforeEach(() => {
    SavedSearchSelect.prototype.config = {
      searchMediator: new SearchMediator(undefined, new QueryBuilder({}))
    };
    savedSearchSelect = new SavedSearchSelect(mockElement(), mock$scope, mockTimeout(), mockSearchService(), mockNamespaceService());
    savedSearchSelect.ngOnInit();
  });

  describe('ngOnInit()', () => {
    it('should define search mediator', () => {
      expect(savedSearchSelect.searchMediator).to.be.define;
    });

    it('should assign properties set in the mediator', () => {
      expect(savedSearchSelect.searchMediator.arguments.properties).to.exist;
    });

    it('should define selectConfig', () => {
      expect(savedSearchSelect.config.selectConfig).to.be.define;
    });
  });

  describe('getDefaultConfig()', () => {
    it('should return default config', () => {
      var config = savedSearchSelect.getDefaultConfig();
      expect(config.delay).to.be.equals(500);
      expect(config.multiple).to.be.false;
      expect(config.listeners).to.be.defined;
    });
  });

  describe('savedSearchLoader()', () => {
    it('should return search result', () => {
      var response = savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchService.search.calledOnce).to.be.true;
      expect(response.resolveValue.data.id).to.be.equals('id');
    });

    it('should define search tree', () => {
      expect(savedSearchSelect.searchTree).to.be.undefined;
      savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchTree).to.be.defined;
    });

    it('should define search tree with object type rule only', () => {
      savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchTree.rules.length).to.be.equals(1);
    });

    it('should not redefine the search tree if exist', () => {
      savedSearchSelect.searchTree = {
        id: '',
        rules: [{}]
      };
      savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchTree.rules.length).to.be.equals(1);
    });

    it('should not generate fts rule if there is no term', () => {
      savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchTree.rules[1]).to.be.undefined;
    });

    it('should generate fts rule if there is term', () => {
      savedSearchSelect.savedSearchLoader({
        data: {
          q: 'term'
        }
      });
      expect(savedSearchSelect.searchTree.rules[1].value).to.be.equals('term');
    });

    it('should abort last request before making a new one', () => {
      savedSearchSelect.searchMediator = {
        abortLastSearch: sinon.spy(),
        search: sinon.spy(() => {
          return PromiseStub.resolve({
            response: 'response'
          });
        }),
        queryBuilder: {
          init: () => {}
        }
      };
      savedSearchSelect.savedSearchLoader();
      expect(savedSearchSelect.searchMediator.abortLastSearch.calledOnce).to.be.true;
    });
  });

  describe('savedSearchConverter()', () => {
    it('should return empty result if there are no records', () => {
      var converter = savedSearchSelect.savedSearchConverter({data: {values: []}});
      expect(converter.length).to.be.equals(0);
    });

    it('should return empty result if there are no criteria', () => {
      var records = {
        data: {
          values: [{
            id: 'id',
            properties: {}
          }]
        }
      };
      var converter = savedSearchSelect.savedSearchConverter(records);
      expect(converter.length).to.be.equals(0);
    });

    it('should return converted records', () => {
      var records = {
        data: {
          values: [{
            id: 'id',
            properties: {
              searchCriteria: '{\"criteria\" : {\"id":\"id",\"rules\":[]}}'
            }
          }]
        }
      };
      var converted = savedSearchSelect.savedSearchConverter(records);
      expect(converted[0].id).to.be.equals('id');
      expect(converted[0].criteria.id).to.be.equals('id');
    });
  });

  describe('getSearchTree()', () => {
    it('should define new object tree', () => {
      var tree = savedSearchSelect.getSearchTree();
      expect(tree.id).to.be.defined;
      expect(tree.condition).to.be.defined;
      expect(tree.rules.length).to.be.equals(1);
    });
  });

  describe('openSavedSearch()', () => {
    it('should clear selected search when open new one', () => {
      savedSearchSelect.selectedSearch = {};
      savedSearchSelect.openSavedSearch({params: {data: {}}});
      expect(savedSearchSelect.selectedSearch).to.be.undefined;
    });
  });

  describe('applyClosingListener()', () => {
    it('should catch select2 closing event', () => {
      var spy = savedSearchSelect.$element.on;
      expect(spy.callCount).to.equal(1);
      expect(spy.getCall(0).args[0]).to.be.equals('select2:closing');
    });
  });

  describe('triggerSavedSearchSelect()', () => {
    it('should trigger save search select', () => {
      savedSearchSelect.triggerSavedSearchSelect();
      var spyFind = savedSearchSelect.$element.find;
      var spySelect2 = spyFind.getCall(0).returnValue.select2;
      expect(spyFind.callCount).to.equal(1);
      expect(spySelect2.callCount).to.equal(1);
      expect(spySelect2.getCall(0).args[0]).to.equal('open');
    });
  });
});

function mockSearchService() {
  return {
    search: sinon.spy(() => {
      return {
        promise: PromiseStub.resolve({
          data: {
            id: 'id'
          }
        })
      }
    })
  };
}

function mockNamespaceService() {
  return {
    toFullURI: sinon.spy(() => {
      return PromiseStub.resolve({
        data: {
          SAVED_SEARCH_URI: 'full'
        }
      })
    })
  };
}

function mockTimeout() {
  return (fn) => {
    fn();
  };
}

function mockElement(element) {
  return {
    find: sinon.spy(()=> {
      if (element) {
        return element
      }
      return {
        select2: sinon.spy()
      };
    }),
    on: sinon.spy()
  };
}