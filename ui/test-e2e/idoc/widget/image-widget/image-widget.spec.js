var ImageWidget = require('./image-widget').ImageWidget;
var ImageWidgetConfigDialog = require('./image-widget').ImageWidgetConfigDialog;
var IdocPage = require('../../idoc-page');
var Search = require('../../../search/components/search.js');
var SearchResults = require('../../../search/components/common/search-results').SearchResults;

const TAB_NUMBER = 1;

describe('ImageWidget', function () {

  var idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  it('should display error message when no images are selected', () => {
    idocPage.getTabEditor(TAB_NUMBER).insertWidget(ImageWidget.WIDGET_NAME);
    var widgetConfig = new ImageWidgetConfigDialog();
    widgetConfig.save();

    var idocContent = idocPage.getTabEditor(TAB_NUMBER);

    var imageWidget = idocContent.getWidgetByNameAndOrder(ImageWidget.WIDGET_NAME, 0);
    expect(imageWidget.$('.image-widget-viewer').isDisplayed()).to.eventually.be.false;
    expect(imageWidget.$('.image-widget-viewer').isPresent()).to.eventually.be.true;
    var errorMessage = $('.text-danger').getText();
    expect(errorMessage).to.eventually.not.equal('[object Object]');
    expect(errorMessage).to.eventually.not.equal('select.object.none');
  });

  it('should display mirador initialized', () => {
    idocPage.getTabEditor(TAB_NUMBER).insertWidget(ImageWidget.WIDGET_NAME);
    var widgetConfig = new ImageWidgetConfigDialog();

    var search = new Search('.modal .seip-search-wrapper');
    search.waitUntilOpened();
    search.clickSearch();

    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(0);

    widgetConfig.save();

    var idocContent = idocPage.getTabEditor(TAB_NUMBER);

    var imageWidget = idocContent.getWidgetByNameAndOrder(ImageWidget.WIDGET_NAME, 0);
    expect(imageWidget.$('.image-widget-viewer').isDisplayed()).to.eventually.be.true;
    expect(imageWidget.$('.image-widget-viewer').isPresent()).to.eventually.be.true;
  });
});