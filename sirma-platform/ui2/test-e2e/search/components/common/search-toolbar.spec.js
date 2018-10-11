var SearchToolbar = require('./search-toolbar.js').SearchToolbar;
var SearchToolbarSandbox = require('./search-toolbar.js').SearchToolbarSandbox;

describe('SearchToolbar', () => {

  var searchToolbar;
  var page = new SearchToolbarSandbox();

  beforeEach(() => {
    page.open();
    searchToolbar = page.getSearchToolbar();
  });

  it('should display total results count', () => {
    var resultsToolbar = searchToolbar.getResultsToolbar();
    var resultsBounds = resultsToolbar.getBoundsMessageText();
    var resultsCount = resultsToolbar.getCountMessageText();

    expect(resultsBounds.isDisplayed()).to.eventually.be.true;
    expect(resultsBounds.getText()).to.eventually.equal('1 - 5');

    expect(resultsCount.isDisplayed()).to.eventually.be.true;
    expect(resultsCount.getText()).to.eventually.equal('10');
  });

  it('should change the order by when fts is present', () => {
    page.setFtsField();
    var orderToolbar = searchToolbar.getOrderToolbar();
    var orderBy = orderToolbar.getOrderByOption();
    expect(orderBy).to.eventually.eq('Relevance');
  });

  it('should restore the order by to default when fts is cleared', () => {
    var orderToolbar = searchToolbar.getOrderToolbar();

    page.setFtsField();
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('Relevance');

    page.clearFtsField();
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('Modified On');
  });

  it('should restore previous order by when fts is cleared', () => {
    var orderToolbar = searchToolbar.getOrderToolbar();
    orderToolbar.selectOrderByOption(3);

    page.setFtsField();
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('Relevance');

    page.clearFtsField();
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('Type');
  });

  it('should properly set order by option & direction when a saved search is opened', () => {
    page.loadSavedSearch();

    var orderToolbar = searchToolbar.getOrderToolbar();
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('Type');
    expect(orderToolbar.getOrderDirection()).to.eventually.eq('ascending');
  });
});