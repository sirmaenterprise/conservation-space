'use strict';

let IdocPage = require('../../idoc-page').IdocPage;

let ObjectLinkWidget = require('./object-link-widget').ObjectLinkWidget;
let ObjectLinkWidgetConfigDialog = require('./object-link-widget').ObjectLinkWidgetConfigDialog;

describe('ObjectLinkWidget', function () {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  it('should insert single object link widget', () => {
    idocPage.getTabEditor(1).executeWidgetCommand(ObjectLinkWidget.COMMAND);
    let widgetConfig = new ObjectLinkWidgetConfigDialog();
    let search = widgetConfig.getSearch();
    let searchResults = search.getResults();
    searchResults.waitForResults();
    searchResults.clickResultItem(0);
    widgetConfig.save();

    let idocContent = idocPage.getTabEditor(1);
    let widget = new ObjectLinkWidget(idocContent.getWidgetByNameAndOrder(ObjectLinkWidget.NAME, 0));
    expect(widget.getHeader().isDisplayed()).to.eventually.be.true;
  });

  it('should insert multiple object links at once', () => {
    idocPage.getTabEditor(1).executeWidgetCommand(ObjectLinkWidget.COMMAND);
    let widgetConfig = new ObjectLinkWidgetConfigDialog();
    let search = widgetConfig.getSearch();
    let searchResults = search.getResults();
    searchResults.waitForResults();
    for (let i = 0; i < 3; i++) {
      searchResults.clickResultItem(i);
    }

    widgetConfig.save();
    let idocContent = idocPage.getTabEditor(1);
    for (let i = 0; i < 3; i++) {
      let widget = new ObjectLinkWidget(idocContent.getWidgetByNameAndOrder(ObjectLinkWidget.NAME, i));
      expect(widget.getHeader().isDisplayed()).to.eventually.be.true;
    }
  });

  it('should not insert widget if no search result selection', () => {
    let tabEditor = idocPage.getTabEditor(1);
    tabEditor.executeWidgetCommand(ObjectLinkWidget.COMMAND);
    new ObjectLinkWidgetConfigDialog().cancel(true);

    let widgetElement = tabEditor.getWidget(ObjectLinkWidget.NAME);
    expect(widgetElement.isPresent()).to.eventually.be.false;
  });

  it('should not insert widget if the modal dialog is canceled', () => {
    let tabEditor = idocPage.getTabEditor(1);
    tabEditor.executeWidgetCommand(ObjectLinkWidget.COMMAND);
    let widgetConfig = new ObjectLinkWidgetConfigDialog();
    let search = widgetConfig.getSearch();
    let searchResults = search.getResults();
    searchResults.waitForResults();
    searchResults.clickResultItem(0);
    widgetConfig.cancel(true);

    let widgetElement = tabEditor.getWidget(ObjectLinkWidget.NAME);
    expect(widgetElement.isPresent()).to.eventually.be.false;
  });

  it('should keep widget content intact when sanitized instance links are loaded from the backend', () => {
    // emf:234567 has properly sanitized contend from the backend, and is loaded into the ditor.
    idocPage.open(true, 'emf:234567');
    let tabEditor = idocPage.getTabEditor(1);
    let widgetElement = tabEditor.getWidget(ObjectLinkWidget.NAME);
    // widget element is present, but the header won't be compiled
    expect(widgetElement.$('.instance-header').isPresent()).to.eventually.be.true;
  });
});