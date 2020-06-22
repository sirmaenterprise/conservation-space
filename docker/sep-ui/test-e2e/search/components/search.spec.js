'use strict';

let SearchSandbox = require('./search.js').SearchSandbox;
let AdvancedSearchKeywordCriteria = require('./advanced/advanced-search').AdvancedSearchKeywordCriteria;

describe('Search', () => {

  let search;
  beforeEach(() => {
    search = new SearchSandbox().open().getSearch();
  });

  function assertResults() {
    let results = search.getResults();
    results.waitForResults();
    expect(results.getResultsCount()).to.eventually.eq(30);
  }

  it('should automatically trigger an initial search', () => {
    assertResults();
  });

  it('should list results when enter is pressed in fts field', () => {
    search.getCriteria().getSearchBar().typeFreeText(protractor.Key.ENTER);
    assertResults();
  });

  it('should list results when search button is clicked', () => {
    search.getCriteria().getSearchBar().search();
    assertResults();
  });

  it('should list results when order by options is changed', () => {
    var order = search.getToolbar().getOrderToolbar();
    order.selectOrderByOption(3);
    assertResults();
  });

  it('should list results when order direction is changed', () => {
    search.getToolbar().getOrderToolbar().toggleOrderDirection();
    assertResults();
  });

  it('should list results when clicking on pagination', () => {
    search.getCriteria().getSearchBar().search();
    assertResults();
    search.getPagination().goToLastPage();
    assertResults();
  });

  it('should clear any search results when Clear is clicked in advanced search', () => {
    search.getCriteria().getSearchBar().toggleOptions().openAdvancedSearch();
    let advancedSearch = search.getCriteria().getAdvancedSearch();

    advancedSearch.search();
    assertResults();

    advancedSearch.clear();
    expect(search.getResults().getResultsCount()).to.eventually.equal(0);
  });

  it('should clear any search results when switching to advanced search', () => {
    search.getCriteria().getSearchBar().toggleOptions().openAdvancedSearch();
    search.getCriteria().getAdvancedSearch().waitUntilVisible();
    expect(search.getResults().getResultsCount()).to.eventually.equal(0);
  });

  it('should clear any search results when switching back from advanced to search bar', () => {
    let criteria = search.getCriteria();
    openAdvancedSearch();
    criteria.getAdvancedSearch().search();
    search.getResults().waitForResults();
    closeAdvancedSearch();
    expect(search.getResults().getResultsCount()).to.eventually.equal(0);
  });

  it('should go back to default ordering when switching from search bar to advanced search and relevancy is enabled', () => {
    search.getCriteria().getSearchBar().typeFreeText('Some text');
    expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Relevance');

    openAdvancedSearch();
    expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Relevance');
  });

  it('should not reset search toolbar back to default ordering when switching from search bar to advanced search and relevancy is disabled', () => {
    var order = search.getToolbar().getOrderToolbar();
    order.selectOrderByOption(3);

    openAdvancedSearch();
    expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Type');
  });

  it('should reset search toolbar back to relevance ordering when switching from advanced to search bar and free text field is not empty', () => {
    search.getCriteria().getSearchBar().typeFreeText('Some text');
    openAdvancedSearch();
    closeAdvancedSearch();
    expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Relevance');
  });

  it('should reset search toolbar back to default ordering when switching from advanced to search bar and fts field is empty', () => {
    openAdvancedSearch();
    closeAdvancedSearch();
    expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified on');
  });

  it('should reset order by to default ordering when coming from advanced search with free text', () => {
    let advancedSearch = openAdvancedSearch();
    let section = advancedSearch.getSection(0);

    return section.getCriteriaRowForGroup(1, 0, 0).then((row) => {
      let keywordCriteria = new AdvancedSearchKeywordCriteria(row.valueColumn);
      keywordCriteria.getInput().setValue(undefined, 'test');
      expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Relevance');

      closeAdvancedSearch();
      expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified on');
    });
  });

  function openAdvancedSearch() {
    let criteria = search.getCriteria();
    criteria.getSearchBar().toggleOptions().openAdvancedSearch();
    return criteria.getAdvancedSearch();
  }

  function closeAdvancedSearch() {
    let criteria = search.getCriteria();
    criteria.closeAdvancedSearch();
    criteria.getSearchBar().waitUntilOpened();
  }
});