'use strict';

var Search = require('./search.js');
var AdvancedSearch = require('./advanced/advanced-search.js').AdvancedSearch;
var MixedSearchCriteria = require('./common/mixed-search-criteria');

var SandboxPage = require('../../page-object').SandboxPage;

describe('Search', () => {
  var search;
  var page = new SandboxPage();

  beforeEach(() => {
    page.open('/sandbox/search/components/search');
    search = new Search(Search.COMPONENT_SELECTOR);
    search.waitUntilOpened();
  });

  it('should list results when enter is pressed in fts field', () => {
    search.pressEnterInsideFTS().results.waitUntilOpened();
    expect(search.results.resultCount).to.eventually.eq((30));
  });

  it('should list results when search button is clicked', () => {
    search.clickSearch().results.waitUntilOpened();
    expect(search.results.resultCount).to.eventually.eq((30));
  });

  it('should list results when order direction is changed', () => {
    search.toggleOrderDirection().results.waitUntilOpened();
    expect(search.results.resultCount).to.eventually.eq((30));
  });

  it('should list results when order by is changed', () => {
    search.selectOrderBy('emf\\:type').results.waitUntilOpened();
    expect(search.results.resultCount).to.eventually.eq((30));
  });

  it('should list results when clicking on pagination', () => {
    search.clickSearch().results.waitUntilOpened();
    search.clickLastPageButton().results.waitUntilOpened();
    expect(search.results.resultCount).to.eventually.eq((30));
  });

  it('should clear any search results when Clear is clicked in basic search', () => {
    search.criteria.search();
    search.results.waitUntilOpened();
    return search.criteria.clearCriteria().then(() => {
      return search.results.resultCount;
    }).then((count) => {
      expect(count).to.equal(0);
      return search.results.getResults();
    }).then((results) => {
      expect(results.length).to.equal(0);
    });
  });

  it('should clear any search results when Clear is clicked in advanced search', () => {
    var mixedSearchCriteria = new MixedSearchCriteria();
    mixedSearchCriteria.clickOption(mixedSearchCriteria.getAdvancedSearchOption());

    var advancedSearch = new AdvancedSearch(mixedSearchCriteria.getAdvancedSearchForm());
    advancedSearch.search();

    search.results.waitUntilOpened();
    return advancedSearch.clear().then(() => {
      return search.results.resultCount;
    }).then((count) => {
      expect(count).to.equal(0);
      return search.results.getResults();
    }).then((results) => {
      expect(results.length).to.equal(0);
    });
  });

  it('should clear any search results when switched from basic to advanced search', () => {
    search.criteria.search();
    search.results.waitUntilOpened();

    var mixedSearchCriteria = new MixedSearchCriteria();
    mixedSearchCriteria.clickOption(mixedSearchCriteria.getAdvancedSearchOption());

    return search.results.resultCount.then((count) => {
      expect(count).to.equal(0);
      return search.results.getResults();
    }).then((results) => {
      return expect(results.length).to.equal(0);
    });
  });


  it('should clear any search results when switched from advanced to basic search', () => {
    // Going first to advanced search
    var mixedSearchCriteria = new MixedSearchCriteria();
    mixedSearchCriteria.clickOption(mixedSearchCriteria.getAdvancedSearchOption());

    var advancedSearch = new AdvancedSearch(mixedSearchCriteria.getAdvancedSearchForm());
    advancedSearch.search();

    mixedSearchCriteria.clickOption(mixedSearchCriteria.getBasicSearchOption());

    return search.results.resultCount.then((count) => {
      expect(count).to.equal(0);
      return search.results.getResults();
    }).then((results) => {
      return expect(results.length).to.equal(0);
    });
  });

});