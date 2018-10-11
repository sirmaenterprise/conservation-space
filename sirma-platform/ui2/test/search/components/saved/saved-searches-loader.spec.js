import {
  SavedSearchesLoader,
  SAVED_SEARCH_URI,
  SAVED_SEARCH_PROPERTIES
} from 'search/components/saved/saved-searches-loader';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {DCTERMS_TITLE} from 'instance/instance-properties';

import {stubSearchService} from 'test/services/rest/search-service-mock';

describe('SavedSearchesLoader', () => {

  let loader;
  beforeEach(() => {
    loader = new SavedSearchesLoader(stubSearchService([{id: 'emf:123'}], 1));
  });

  describe('filterSavedSearches()', () => {
    it('should lazily initialize search tree & mediator', () => {
      expect(loader.searchTree).to.be.undefined;
      expect(loader.searchMediator).to.be.undefined;
      loader.filterSavedSearches();
      expect(loader.searchTree).to.be.defined;
      expect(loader.searchMediator).to.be.defined;
    });

    it('should filter saved searches via the search service', () => {
      var filterResponse;
      loader.filterSavedSearches().then(response => filterResponse = response);
      expect(loader.searchService.search.calledOnce).to.be.true;
      expect(filterResponse).to.deep.equal({total: 1, values: [{id: 'emf:123'}]});
    });

    it('should not reinitialize the search tree more than once', () => {
      loader.filterSavedSearches();
      let firstTree = loader.searchTree;
      loader.filterSavedSearches();
      let secondTree = loader.searchTree;
      // Should be the same reference
      expect(firstTree).to.equal(secondTree);
    });

    it('should not generate free text rule if there are no provided filter terms', () => {
      loader.filterSavedSearches();
      expect(loader.searchTree.rules.length).to.equal(1);
    });

    it('should generate free text rule if there are provided filter terms', () => {
      loader.filterSavedSearches('projects');
      expect(loader.searchTree.rules.length).to.equal(2);
      let ftsRule = loader.searchTree.rules[1];
      expect(ftsRule.field).to.equal(DCTERMS_TITLE);
      expect(ftsRule.value).to.equal('projects');
    });

    it('should abort last search request before making a new one', () => {
      loader.initSearchMediator();
      loader.searchMediator.abortLastSearch = sinon.spy();
      loader.filterSavedSearches();
      expect(loader.searchMediator.abortLastSearch.calledOnce).to.be.true;
    });
  });

  describe('initSearchMediator()', () => {
    it('should initialize a search mediator with proper arguments and query builder', () => {
      loader.initSearchMediator();
      expect(loader.searchMediator.arguments).to.deep.equal({properties: SAVED_SEARCH_PROPERTIES});
      // Should be the same reference
      expect(loader.searchMediator.queryBuilder.tree).to.equal(loader.searchTree);
    });

    it('should initialize search tree with object type rule only', () => {
      loader.filterSavedSearches();
      expect(loader.searchTree.rules.length).to.equal(1);
      let typeRule = loader.searchTree.rules[0];
      expect(typeRule.field).to.deep.equal(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
      expect(typeRule.value).to.deep.equal([SAVED_SEARCH_URI]);
    });
  });

});