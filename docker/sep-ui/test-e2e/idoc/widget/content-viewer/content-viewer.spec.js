'use strict';

let ContentViewerSandboxPage = require('./content-viewer').ContentViewerSandboxPage;
let ContentViewerConfigDialog = require('./content-viewer').ContentViewerConfigDialog;
let ObjectSelector = require('../object-selector/object-selector.js').ObjectSelector;

function changeSelectionMode(mode) {
  let selector = new ObjectSelector();
  selector.selectObjectSelectionMode(mode);
}

function performSearch() {
  let search = getSearch();
  search.getCriteria().getSearchBar().search();
  search.getResults().waitForResults();
  return search;
}

function getSearch() {
  return new ObjectSelector().getSearch();
}

describe('ContentViewer', function () {

  let page = new ContentViewerSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  it('should display a PDF document from manual search', function () {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = performSearch();

    // When I select a PDF from the search basket
    search.getResults().clickResultItem(0);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a PDF document in it
    let contentViewer = page.getPdfViewer();
    contentViewer.waitForViewer();
    return expect(contentViewer.getPageAsText(1)).to.eventually.contain('Hello world!');
  });

  it('should display a PDF document from automatic search', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    // When I select automatic search with one results
    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    performSearch();

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a PDF document in it
    let contentViewer = page.getPdfViewer();
    contentViewer.waitForViewer();
    return expect(contentViewer.getPageAsText(1)).to.eventually.contain('Hello world!');
  });

  it('should display a video document from manual search', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = performSearch();

    // When I select a video from the search basket
    search.getResults().clickResultItem(2);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a video document in it
    let contentViewer = page.getVideoPlayer();
    contentViewer.waitForViewer();
    expect(contentViewer.isViewerPresent()).to.eventually.be.true;
  });

  it('should display a audio document from manual search', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = performSearch();

    // When I select an audio from the search basket
    search.getResults().clickResultItem(3);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see an audio document in it
    let contentViewer = page.getAudioPlayer();
    contentViewer.waitForViewer();
    expect(contentViewer.isViewerPresent()).to.eventually.be.true;
  });

  it('should display a warning message when a there are multiple automatic results', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    // When I select automatic search with multuple results
    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a warning message
    let contentViewer = page.getPdfViewer();
    expect(contentViewer.getErrorMessage().isPresent()).to.eventually.be.true;

    // And I should not see the PDF viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.false;
  });

  it('should display image document from manual search', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.MULTIPLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = performSearch();

    // When I select an image from the search basket
    search.getResults().clickResultItem(1);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    let contentViewer = page.getImageViewer();
    contentViewer.waitForViewer();

    // Then I should not see a warning message
    expect(contentViewer.getErrorMessage().isPresent()).to.eventually.be.false;

    // And I should see the image viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.true;
  });

  it('should display a warning message when a document is not found', () => {
    // When I select a search with not results
    page.changeSearchDataset(ContentViewerSandboxPage.EMPTY_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.AUTOMATICALLY);

    // And I save the widget configuration
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    // Then I should see a warning message
    let contentViewer = page.getPdfViewer();
    expect(contentViewer.getErrorMessage().isPresent()).to.eventually.be.true;

    // And I should not see the PDF viewer
    expect(contentViewer.isViewerPresent()).to.eventually.be.false;
  });

  it('should save changes made in the configuration dialog', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();

    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = getSearch();
    let searchBar = search.getCriteria().getSearchBar();
    searchBar.typeFreeText('Give me a pdf!').search();

    let results = search.getResults();
    results.clickResultItem(0);

    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();

    let contentViewer = page.getPdfViewer();
    contentViewer.header.openConfig();

    search = getSearch();
    search.waitUntilOpened();

    return expect(search.getCriteria().getSearchBar().getFreeTextValue()).to.eventually.equal('Give me a pdf!');
  });

  it('should cancel changes made in the configuration dialog', () => {
    page.changeSearchDataset(ContentViewerSandboxPage.SINGLE_DATASET);
    page.insertWidget();
    let configDialog = new ContentViewerConfigDialog();
    configDialog.save();
    let contentViewer = page.getPdfViewer();

    contentViewer.getHeader().openConfig();
    changeSelectionMode(ObjectSelector.MANUALLY);

    let search = getSearch();
    let searchBar = search.getCriteria().getSearchBar();
    searchBar.typeFreeText('Give me a pdf!').search();

    let results = search.getResults();
    results.clickResultItem(0);

    configDialog.cancel();

    contentViewer.header.openConfig();

    search = getSearch();
    search.waitUntilOpened();

    return expect(search.getCriteria().getSearchBar().getFreeTextValue()).to.eventually.equal('');
  });
});