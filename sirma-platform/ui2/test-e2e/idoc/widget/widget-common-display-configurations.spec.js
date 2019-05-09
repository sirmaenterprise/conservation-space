'use strict';

let IdocPage = require('../idoc-page').IdocPage;
let HelloWidget = require('./hello-widget').HelloWidget;
let HelloWidgetConfigDialog = require('./hello-widget').HelloWidgetConfigDialog;

const EDIT_MODE = 'EDIT';
const PREVIEW_MODE = 'PREVIEW';

describe('WidgetCommonDisplayConfigurations', () => {
  let idocPage = new IdocPage();
  let widgetElement;

  beforeEach(() => {
    //Given I have opened the idoc page for edit and have inserted the hello widget
    idocPage.open(true);
    widgetElement = idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
  });

  it('should be configured to show header and borders by default', () => {
    let helloConfigDialog = openHellowWidgetConfig(idocPage);
    expect(helloConfigDialog.isShowWidgetHeaderSelected()).to.eventually.be.true;
    expect(helloConfigDialog.isShowWidgetHeaderBordersSelected()).to.eventually.be.true;
    expect(helloConfigDialog.isShowWidgetBordersSelected()).to.eventually.be.true;
    helloConfigDialog.save();

    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();

    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.waitToAppear();

    checkWidgetStyles(helloWidget, true, true, true);
  });

  describe('when widget header options', () => {

    describe('display header ON + display header border ON', () => {

      describe('in EDIT and PREVIEW', () => {

        it('in unlocked tab', () => {
          let helloConfigDialog = openHellowWidgetConfig(idocPage);
          helloConfigDialog.showWidgetHeader();
          helloConfigDialog.showWidgetHeaderBorders();
          helloConfigDialog.save();
          let helloWidget = new HelloWidget(widgetElement);

          // EDIT mode
          isHeaderVisible(true, helloWidget, EDIT_MODE);
          areHeaderBordersVisible(true, helloWidget, EDIT_MODE);
          isCollapseExpandVisible(helloWidget);
          isConfigVisible(helloWidget);
          isDeleteVisible(helloWidget);

          saveIdoc(idocPage);
          helloWidget.waitToAppear();

          // PREVIEW mode
          isHeaderVisible(true, helloWidget, PREVIEW_MODE);
          areHeaderBordersVisible(true, helloWidget, PREVIEW_MODE);
          isCollapseExpandVisible(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
        });

        // it('in locked tab', () => {
        //   let helloConfigDialog = openHellowWidgetConfig(idocPage);
        //   helloConfigDialog.showWidgetHeader();
        //   helloConfigDialog.showWidgetHeaderBorders();
        //   helloConfigDialog.save();
        //
        //   lockTab(idocPage);
        //
        //   let helloWidget = new HelloWidget(widgetElement);
        //
        //   // EDIT mode
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //
        //   saveIdoc(idocPage);
        //   helloWidget.waitToAppear();
        //
        //   // PREVIEW mode
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        // });
      });
    });

    describe('display header ON + display header border OFF', () => {

      describe('in EDIT and PREVIEW', () => {

        it('in unlocked tab', () => {
          let helloConfigDialog = openHellowWidgetConfig(idocPage);
          helloConfigDialog.showWidgetHeader();
          helloConfigDialog.hideWidgetHeaderBorders();
          helloConfigDialog.save();
          let helloWidget = new HelloWidget(widgetElement);

          // EDIT mode
          // TODO check if has title or bgcolor
          isHeaderVisible(true, helloWidget, EDIT_MODE);
          areHeaderBordersVisible(false, helloWidget, EDIT_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigVisible(helloWidget);
          isDeleteVisible(helloWidget);

          saveIdoc(idocPage);
          helloWidget.waitToAppear();

          // PREVIEW mode
          // TODO check if has title or bgcolor
          isHeaderVisible(true, helloWidget, PREVIEW_MODE);
          areHeaderBordersVisible(false, helloWidget, PREVIEW_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
        });

        // it('in locked tab', () => {
        //   let helloConfigDialog = openHellowWidgetConfig(idocPage);
        //   helloConfigDialog.showWidgetHeader();
        //   helloConfigDialog.hideWidgetHeaderBorders();
        //   helloConfigDialog.save();
        //
        //   lockTab(idocPage);
        //
        //   let helloWidget = new HelloWidget(widgetElement);
        //
        //   // EDIT mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //
        //   saveIdoc(idocPage);
        //   helloWidget.waitToAppear();
        //
        //   // PREVIEW mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        // });
      });
    });

    describe('display header OFF + display header border ON', () => {

      describe('in EDIT and PREVIEW', () => {

        it('in unlocked tab', () => {
          let helloConfigDialog = openHellowWidgetConfig(idocPage);
          helloConfigDialog.save();

          let helloWidget = new HelloWidget(widgetElement);
          let header = helloWidget.getHeader();
          header.setTitle('hello widget');
          header.openConfig();
          helloConfigDialog.hideWidgetHeader();
          helloConfigDialog.showWidgetHeaderBorders();
          helloConfigDialog.save();

          // EDIT mode
          isTitleHidden(helloWidget, EDIT_MODE);
          areHeaderBordersVisible(false, helloWidget, EDIT_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigVisible(helloWidget);
          isDeleteVisible(helloWidget);

          saveIdoc(idocPage);
          helloWidget.waitToAppear();

          // PREVIEW mode
          isTitleHidden(helloWidget, PREVIEW_MODE);
          areHeaderBordersVisible(false, helloWidget, PREVIEW_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
        });

        // it('in locked tab', () => {
        //   let helloConfigDialog = openHellowWidgetConfig(idocPage);
        //   helloConfigDialog.hideWidgetHeader();
        //   helloConfigDialog.showWidgetHeaderBorders();
        //   helloConfigDialog.save();
        //
        //   lockTab(idocPage);
        //
        //   let helloWidget = new HelloWidget(widgetElement);
        //
        //   // EDIT mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //
        //   saveIdoc(idocPage);
        //   helloWidget.waitToAppear();
        //
        //   // PREVIEW mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        // });
      });
    });

    describe('display header OFF + display header border OFF', () => {

      describe('in EDIT and PREVIEW', () => {

        it('in unlocked tab', () => {
          let helloConfigDialog = openHellowWidgetConfig(idocPage);
          helloConfigDialog.save();

          let helloWidget = new HelloWidget(widgetElement);
          let header = helloWidget.getHeader();
          header.setTitle('hello widget');
          header.openConfig();
          helloConfigDialog.hideWidgetHeader();
          helloConfigDialog.hideWidgetHeaderBorders();
          helloConfigDialog.save();

          // EDIT mode
          isTitleHidden(helloWidget, EDIT_MODE);
          areHeaderBordersVisible(false, helloWidget, EDIT_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigVisible(helloWidget);
          isDeleteVisible(helloWidget);

          saveIdoc(idocPage);
          helloWidget.waitToAppear();

          // PREVIEW mode
          isTitleHidden(helloWidget, PREVIEW_MODE);
          areHeaderBordersVisible(false, helloWidget, PREVIEW_MODE);
          // actions should be hidden by default and visible on hover
          isCollapseExpandHidden(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
          browser.actions().mouseMove(widgetElement).perform();
          isCollapseExpandVisible(helloWidget);
          isConfigHidden(helloWidget);
          isDeleteHidden(helloWidget);
        });

        // it('in locked tab', () => {
        //   let helloConfigDialog = openHellowWidgetConfig(idocPage);
        //   helloConfigDialog.hideWidgetHeader();
        //   helloConfigDialog.hideWidgetHeaderBorders();
        //   helloConfigDialog.save();
        //
        //   lockTab(idocPage);
        //
        //   let helloWidget = new HelloWidget(widgetElement);
        //
        //   // EDIT mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //
        //   saveIdoc(idocPage);
        //   helloWidget.waitToAppear();
        //
        //   // PREVIEW mode
        //   // actions should be hidden by default and visible on hover
        //   isCollapseExpandHidden(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        //   browser.actions().mouseMove(widgetElement).perform();
        //   isCollapseExpandVisible(helloWidget);
        //   isConfigHidden(helloWidget);
        //   isDeleteHidden(helloWidget);
        // });
      });
    });
  });

  it('should hide widget body borders if configured', () => {
    let helloConfigDialog = openHellowWidgetConfig(idocPage);
    helloConfigDialog.toggleShowWidgetBorders();
    helloConfigDialog.save();
    let helloWidget = new HelloWidget(widgetElement);

    checkWidgetStyles(helloWidget, false, true, true);

    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();

    helloWidget.waitToAppear();

    checkWidgetStyles(helloWidget, false, true, true);
  });

  it('should hide widget header borders if configured', () => {
    let helloConfigDialog = openHellowWidgetConfig(idocPage);
    helloConfigDialog.toggleShowWidgetHeaderBorders();
    helloConfigDialog.save();
    let helloWidget = new HelloWidget(widgetElement);

    checkWidgetStyles(helloWidget, true, false, true);

    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();
    helloWidget.waitToAppear();

    // Header should be visible, but without borders to account for color configuration.
    checkWidgetStyles(helloWidget, true, false, true);

    // Header should be visible on hover
    browser.actions().mouseMove(widgetElement).perform();
    expect(helloWidget.getHeader().isDisplayed()).to.eventually.be.true;
  });

  it('should change header background color when configured', () => {
    let backgroundColor = 'rgb(182, 215, 168)';
    let expectedBackgroundColor = 'rgba(182, 215, 168, 1)';
    let helloConfigDialog = openHellowWidgetConfig(idocPage);
    let colorPicker = helloConfigDialog.getHeaderBackgroundColorPicker();
    colorPicker.selectColor(backgroundColor);
    helloConfigDialog.save();

    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.waitToAppear();
    expect(helloWidget.getHeader().getBackgroundColor()).to.eventually.equals(expectedBackgroundColor);
  });

  it('should change widget background color when configured', () => {
    let backgroundColor = 'rgb(162, 196, 201)';
    let expectedBackgroundColor = 'rgba(162, 196, 201, 1)';
    let helloConfigDialog = openHellowWidgetConfig(idocPage);
    let colorPicker = helloConfigDialog.getWidgetBackgroundColorPicker();
    colorPicker.selectColor(backgroundColor);
    helloConfigDialog.save();

    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.waitToAppear();
    expect(helloWidget.getBackgroundColor()).to.eventually.equals(expectedBackgroundColor);
  });
});

function isConfigVisible(widget) {
  widget.getHeader().isConfigVisible('Widget config option should be visible!');
}

function isConfigHidden(widget) {
  widget.getHeader().isConfigHidden('Widget config option should be hidden!');
}

function isDeleteVisible(widget) {
  widget.getHeader().isDeleteVisible('Widget delete option should be visible!');
}

function isDeleteHidden(widget) {
  widget.getHeader().isDeleteHidden('Widget delete option should be hidden!');
}

function isCollapseExpandVisible(widget) {
  widget.getHeader().isCollapseExpandVisible('Widget collapse/Expand option should be visible!');
}

function isCollapseExpandHidden(widget) {
  widget.getHeader().isCollapseExpandHidden('Widget collapse/Expand option should be hidden!');
}

function isHeaderVisible(isVisible, widget, mode) {
  let headerMsg = `Header should be ${isVisible ? 'visible' : 'hidden'} in ${mode} mode!!`;
  expect(widget.getHeader().isDisplayed(), headerMsg).to.eventually.equal(isVisible);
}

function isTitleHidden(widget, mode) {
  widget.getHeader().isTitleHidden(`Widget title should be invisible in ${mode} mode!`);
}

function areHeaderBordersVisible(areVisible, widget, mode) {
  let headerBordersMsg = `Header borders should be ${areVisible ? 'visible' : 'hidden'} in ${mode} mode!`;
  expect(widget.hasHeaderBorders(), headerBordersMsg).to.eventually.equal(areVisible);
}

function checkWidgetStyles(widget, hasBodyBorders, hasHeaderBorders, isHeaderVisible) {
  let bodyBordersMsg = `Widget body borders should be ${hasBodyBorders ? 'visible' : 'hidden'}!`;
  let headerBordersMsg = `Header borders should be ${hasHeaderBorders ? 'visible' : 'hidden'}!`;
  let headerMsg = `Header should be ${isHeaderVisible ? 'visible' : 'hidden'}!`;
  expect(widget.hasBorders(), bodyBordersMsg).to.eventually.equal(hasBodyBorders);
  expect(widget.hasHeaderBorders(), headerBordersMsg).to.eventually.equal(hasHeaderBorders);
  expect(widget.getHeader().isDisplayed(), headerMsg).to.eventually.equal(isHeaderVisible);
}

function saveIdoc(idocPage) {
  let actionsToolbar = idocPage.getActionsToolbar();
  actionsToolbar.saveIdoc();
  idocPage.waitForPreviewMode();
}

function lockTab(idocPage) {
  let idocTabs = idocPage.getIdocTabs(false);
  let tab = idocTabs.getTabByIndex(0);
  tab.openTabConfiguration().lockTab().save();
}

function openHellowWidgetConfig(idocPage) {
  idocPage.getTabEditor(1).insertWidget('hello-widget');
  return new HelloWidgetConfigDialog();
}
