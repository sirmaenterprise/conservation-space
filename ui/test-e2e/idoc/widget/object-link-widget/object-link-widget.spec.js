'use strict';

var IdocPage = require('../../idoc-page');

var ObjectLinkWidget = require('./object-link-widget').ObjectLinkWidget;
var ObjectLinkWidgetConfigDialog = require('./object-link-widget').ObjectLinkWidgetConfigDialog;

describe('ObjectLinkWidget', function () {

  var idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  it('should insert single object link widget', () => {
    idocPage.getTabEditor(1).executeWidgetCommand(ObjectLinkWidget.COMMAND);
    var widgetConfig = new ObjectLinkWidgetConfigDialog();

    var search = widgetConfig.getSearch();
    search.waitUntilOpened();

    var searchResults = widgetConfig.getSearchResults();
    searchResults.waitUntilOpened();
    searchResults.clickResultItem(0);
    widgetConfig.save();

    var idocContent = idocPage.getTabEditor(1);
    var widget = new ObjectLinkWidget(idocContent.getWidgetByNameAndOrder(ObjectLinkWidget.NAME, 0));
    expect(widget.getHeader().isDisplayed()).to.eventually.be.true;
  });

  it('should insert multiple object links at once', () => {
    idocPage.getTabEditor(1).executeWidgetCommand(ObjectLinkWidget.COMMAND);
    var widgetConfig = new ObjectLinkWidgetConfigDialog();
    var search = widgetConfig.getSearch();
    search.waitUntilOpened();

    var searchResults = widgetConfig.getSearchResults();
    searchResults.waitUntilOpened();
    for (var i = 0; i < 3; i++) {
      searchResults.clickResultItem(i);
    }

    widgetConfig.save();
    var idocContent = idocPage.getTabEditor(1);
    for (var i = 0; i < 3; i++) {
      var widget = new ObjectLinkWidget(idocContent.getWidgetByNameAndOrder(ObjectLinkWidget.NAME, i));
      expect(widget.getHeader().isDisplayed()).to.eventually.be.true;
    }
  });

  it('should not insert widget if no search result selection', () => {
    var tabEditor = idocPage.getTabEditor(1);
    tabEditor.executeWidgetCommand(ObjectLinkWidget.COMMAND);
    new ObjectLinkWidgetConfigDialog().cancel(true);

    var widgetElement = tabEditor.getWidget(ObjectLinkWidget.NAME);
    expect(widgetElement.isPresent()).to.eventually.be.false;
  });

  it('should not insert widget if the modal dialog is canceled', () => {
    var tabEditor = idocPage.getTabEditor(1);
    tabEditor.executeWidgetCommand(ObjectLinkWidget.COMMAND);
    var widgetConfig = new ObjectLinkWidgetConfigDialog();
    var search = widgetConfig.getSearch();
    search.waitUntilOpened();

    var searchResults = widgetConfig.getSearchResults();
    searchResults.waitUntilOpened();
    searchResults.clickResultItem(0);
    widgetConfig.cancel(true);

    var widgetElement = tabEditor.getWidget(ObjectLinkWidget.NAME);
    expect(widgetElement.isPresent()).to.eventually.be.false;
  });
});