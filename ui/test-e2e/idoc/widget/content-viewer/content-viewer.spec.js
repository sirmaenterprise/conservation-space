var ContentViewerSandboxPage = require('./content-viewer').ContentViewerSandboxPage;
var ContentViewer = require('./content-viewer').ContentViewer;
var ContentViewerConfigDialog = require('./content-viewer').ContentViewerConfigDialog;

var ObjectSelector = require('../object-selector/object-selector.js').ObjectSelector;
var Search = require('../../../search/components/search.js');
var SearchResults = require('../../../search/components/common/search-results').SearchResults;

function changeSelectionMode(mode) {
  var selector = new ObjectSelector();
  selector.selectObjectSelectionMode(mode);
}

function search() {
  var search = new Search(`.modal ${Search.COMPONENT_SELECTOR}`);
  search.waitUntilOpened();
  search.clickSearch();
}

describe('ContentViewer', function () {

  var page = new ContentViewerSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  it('should display a PDF document from manual search', function () {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    search();

    // When I select a PDF from the search basket
    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(0);

    // And I save the widget configuration
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a PDF document in it
    var contentViewer = page.getWidget();
    contentViewer.waitForViewer();
    return expect(contentViewer.getAsText()).to.eventually.contain('Hello world!');
  });

  it('should display a PDF document from automatic search', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    // When I select automatic search with one results
    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    search();

    // And I save the widget configuration
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a PDF document in it
    var contentViewer = page.getWidget();
    contentViewer.waitForViewer();
    return expect(contentViewer.getAsText()).to.eventually.contain('Hello world!');
  });

  it('should display a warning message when a there are multiple automatic results', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    // When I select automatic search with multuple results
    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    // And I save the widget configuration
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a warning message
    var contentViewer = page.getWidget();
    expect(contentViewer.isAlertPresent()).to.eventually.be.true;

    // And I should not see the PDF viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.false;
  });

  it('should display a warning message when loading an image', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    search();

    // When I select an image from the search basket
    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(1);

    // And I save the widget configuration
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a warning message
    var contentViewer = page.getWidget();
    expect(contentViewer.isAlertPresent()).to.eventually.be.true;

    // And I should not see the PDF viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.false;
  });

  it('should display a warning message when a document is not found', () => {
    // When I select a search with not results
    page.changeSearchDataset(ContentViewerSandboxPage.EMPTY_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    // And I save the widget configuration
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a warning message
    var contentViewer = page.getWidget();
    expect(contentViewer.isAlertPresent()).to.eventually.be.true;

    // And I should not see the PDF viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.false;
  });

  it('should save changes made in the configuration dialog', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    var search = new Search('.modal .seip-search-wrapper');
    search.waitUntilOpened();

    search.criteria.freeTextField.sendKeys('Give me a pdf!');
    search.clickSearch();

    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(0);

    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    var contentViewer = page.getWidget();
    contentViewer.header.openConfig();

    search = new Search('.modal .seip-search-wrapper');
    search.waitUntilOpened();

    return expect(search.criteria.freeTextField.getAttribute('value')).to.eventually.equal('Give me a pdf!');
  });

  it('should cancel changes made in the configuration dialog', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();
    var configDialog = new ContentViewerConfigDialog();
    configDialog.save();
    var contentViewer = page.getWidget();

    contentViewer.getHeader().openConfig();
    changeSelectionMode(ObjectSelector.MANUALLY);

    var search = new Search('.modal .seip-search-wrapper');
    search.waitUntilOpened();

    search.criteria.freeTextField.sendKeys('Give me a pdf!');
    search.clickSearch();

    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(0);

    configDialog.cancel();

    contentViewer.header.openConfig();

    search = new Search('.modal .seip-search-wrapper');
    search.waitUntilOpened();

    return expect(search.criteria.freeTextField.getAttribute('value')).to.eventually.equal('');
  });

});