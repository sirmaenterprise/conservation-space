var ImageWidget = require('./image-widget').ImageWidget;
var ImageWidgetConfigDialog = require('./image-widget').ImageWidgetConfigDialog;
var IdocPage = require('../../idoc-page').IdocPage;
var ImageWidgetSandboxPage = require('./image-widget').ImageWidgetSandboxPage;
var Search = require('../../../search/components/search.js').Search;

const TAB_NUMBER = 1;

describe('ImageWidget', function () {

  var idocPage = new IdocPage();
  var widgetPage = new ImageWidgetSandboxPage();

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

    var widget = widgetPage.getWidget();
    var errorMessage = widget.getErrorMessage().getText();
    expect(errorMessage).to.eventually.not.equal('[object Object]');
    expect(errorMessage).to.eventually.not.equal('select.object.none');
  });

  it('should display mirador initialized', () => {
    idocPage.getTabEditor(TAB_NUMBER).insertWidget(ImageWidget.WIDGET_NAME);
    var widgetConfig = new ImageWidgetConfigDialog();

    var search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();
    var results = search.getResults();
    results.waitForResults();
    results.clickResultItem(0);

    widgetConfig.save();

    var idocContent = idocPage.getTabEditor(TAB_NUMBER);

    var imageWidget = idocContent.getWidgetByNameAndOrder(ImageWidget.WIDGET_NAME, 0);
    expect(imageWidget.$('.image-widget-viewer').isDisplayed()).to.eventually.be.true;
    expect(imageWidget.$('.image-widget-viewer').isPresent()).to.eventually.be.true;
  });

  it('should hide annotation section if  config is selected', () => {
    idocPage.getTabEditor(TAB_NUMBER).insertWidget(ImageWidget.WIDGET_NAME);
    var widgetConfig = new ImageWidgetConfigDialog();
    // I insert widget
    var search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();
    var results = search.getResults();
    results.waitForResults();
    results.clickResultItem(0);

    widgetConfig.save();

    var imageWidget = widgetPage.getWidget();
    var imageComments = imageWidget.getCommentsSection();
    // annotation comments should be visible
    expect(imageWidget.getViewer().isDisplayed()).to.eventually.be.true;
    expect(imageComments.hasComments()).to.eventually.be.true;
    // when i hide annotation comments
    widgetConfig = imageWidget.openWidgetConfig();
    search.getCriteria().getSearchBar().search();
    var results = search.getResults();
    results.waitForResults();
    results.clickResultItem(0);

    widgetConfig.toggleWidgetDisplayOptions();

    widgetConfig.toggleHideAnnotations();
    widgetConfig.save();
    imageWidget = widgetPage.getWidget();
    imageComments = imageWidget.getCommentsSection();

    // viewer should be displayed and comments should be removed.
    expect(imageWidget.getViewer().isDisplayed()).to.eventually.be.true;
    expect(imageComments.hasComments()).to.eventually.be.false;
  });
});