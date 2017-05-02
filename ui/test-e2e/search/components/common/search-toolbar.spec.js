var SearchToolbar = require('./search-toolbar.js').SearchToolbar;
var SandboxPage = require('../../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/search/components/common/toolbar';
const SELECTOR = '#toolbar';

describe('SearchToolbar', () => {

  var toolbar;
  var page = new SandboxPage();

  beforeEach(()=> {
    page.open(SANDBOX_URL);
    toolbar = new SearchToolbar(SELECTOR);
    toolbar.waitUntilOpened();
  });

  it('should display total results count', () => {
    var resultsCount = toolbar.getCountElement();
    expect(resultsCount.isDisplayed()).to.eventually.equal(true);
    expect(resultsCount.getText()).to.eventually.equal('3');
  });

  it('should change order by property', () => {
    var orderBy = toolbar.getOrderBy();
    expect(orderBy.isDisplayed()).to.eventually.equal(true);

    var orderBySelect = orderBy.$('span.select2-selection');
    orderBySelect.click();

    var selectResults = element.all(by.css('.select2-results li')).then(function (items) {
      expect(items.length).to.equal(2);
      expect(items[0].getText()).to.eventually.equal('Title');
      items[1].click();
    });

    browser.wait(EC.elementToBeClickable(orderBySelect), DEFAULT_TIMEOUT);
    var selection = orderBy.$('span.select2-selection__rendered');
    expect(selection.getAttribute('title')).to.eventually.equal('Status');
  });

  it('should change sort direction', () => {
    var sortBy = toolbar.getSortBy();
    expect(sortBy.isDisplayed()).to.eventually.equal(true);
    sortBy.click();
    expect(sortBy.isDisplayed()).to.eventually.equal(true);
  });
});