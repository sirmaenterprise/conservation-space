'use strict';

let IdocPage = require('../idoc-page').IdocPage;
let HelloWidget = require('./hello-widget').HelloWidget;
let HelloWidgetConfigDialog = require('./hello-widget').HelloWidgetConfigDialog;

describe('Base Widget', function () {

  let idocPage = new IdocPage();
  let widgetElement;

  beforeEach(() => {
    //Given I have opened the idoc page for edit and have inserted the hello widget
    idocPage.open(true);
    widgetElement = idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
  });

  it('should allow configuration using the widget actions menu', () => {
    //When I select the configuration action,set a name and save
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.setName('World');
    helloConfigDialog.save();

    //Then I should see the input name
    let helloWidget = new HelloWidget(widgetElement);
    expect(helloWidget.getMessage()).to.eventually.equal('Hello World');
  });

  it('should not be inserted when the initial config dialog is cancelled', () => {
    //When I set the widget name
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.cancel(true);
    //Then I should not see the widget and its wrapper should not be present in the DOM
    expect(widgetElement.isPresent()).to.eventually.be.false;
    expect($('.cke_widget_wrapper').isPresent()).to.eventually.be.false;
  });

  it('should not save configuration when the config dialog is cancelled', () => {
    // And closed the initially opened config dialog
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    //When I select the configuration action,set a name and save
    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().openConfig();
    helloConfigDialog.setName('World');
    helloConfigDialog.cancel();

    //Then I should NOT see the input name
    expect(helloWidget.getMessage()).to.eventually.not.equal('Hello World');
  });

  it('should be removed when the remove button is selected', () => {
    // And closed the initially opened config dialog
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    // And widget appeared in the idoc
    let helloWidget = new HelloWidget(widgetElement);

    // When I select remove widget button
    helloWidget.getHeader().remove();

    // Then I should see neither the widget nor its wrapper
    expect(widgetElement.isPresent()).to.eventually.be.false;
    expect($('.cke_widget_wrapper').isPresent()).to.eventually.be.false;
  });

  it('should not break drag and drop when a widget is removed @slow', () => {
    // Given I have inserted two widgets and a text between them
    new HelloWidgetConfigDialog().save();

    let contentArea = idocPage.getTabEditor(1);
    contentArea.getParagraph(1).type('Drag and drop widget text').type(protractor.Key.ENTER);

    contentArea.getParagraph(2).click();
    widgetElement = contentArea.insertWidget(HelloWidget.WIDGET_NAME);

    new HelloWidgetConfigDialog().save();

    // When I delete the first widget
    new HelloWidget(widgetElement).getHeader().remove();

    // and DnD the second widget above the text
    // scrolling is required because the editor gets focused on load and sometimes overlays the widget buttons
    contentArea.dragAndDropWidget(widgetElement, {x: 5, y: -20});

    // and try to remove the second widget
    new HelloWidget(widgetElement).getHeader().remove();

    // Then the second widget should be removed
    expect(widgetElement.isPresent()).to.eventually.be.false;
  });

  it('should allow editing its title in edit mode', () => {
    // And closed the initially opened config dialog
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    // When I change the title
    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().setTitle('New title');

    // Then I should see the changed title in the hello widget body
    // wait for the title to appear because it's updated async
    browser.wait(EC.textToBePresentInElement(helloWidget.titleElement, 'Title: New title'), DEFAULT_TIMEOUT);
  });

  it('should allow expanding and collapsing a widget', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();
    let helloWidget = new HelloWidget(widgetElement);

    helloWidget.getHeader().isCollapseExpandVisible();
    // We expect that the widget will be expanded by default.
    helloWidget.isExpanded();

    helloWidget.getHeader().collapse();
    helloWidget.isCollapsed();

    idocPage.getActionsToolbar().saveIdoc();
    idocPage.waitForPreviewMode();

    helloWidget.getHeader().isCollapseExpandVisible();
    helloWidget.isCollapsed();

    helloWidget.getHeader().expand();
    helloWidget.isExpanded();
  });

  it('should show correct config header when empty title', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().openConfig();

    helloConfigDialog.isTitlePresent('Hello Widget widget configuration');

    helloConfigDialog.save();
  });

  it('should show correct config header with widget title', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    const TITLE = 'New title';

    //When I change the title
    let helloWidget = new HelloWidget(widgetElement);

    helloWidget.getHeader().setTitle(TITLE);

    helloWidget.getHeader().openConfig();

    helloConfigDialog.isTitlePresent(`Hello Widget widget configuration: ${TITLE}`);

    helloConfigDialog.save();
  });

  it('should properly handle widget expand and collapse in - test with 2 idocs ', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    let helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().collapse();

    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();

    helloWidget.getHeader().isCollapseExpandVisible();

    // open in edit mode
    idocPage.open(true);

    // insert second widget
    let widgetElementTwo =idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
    let helloConfigDialogTwo = new HelloWidgetConfigDialog();
    helloConfigDialogTwo.save();
    let actionsToolbarTwo = idocPage.getActionsToolbar();

    // and save idoc again
    actionsToolbarTwo.saveIdoc();
    idocPage.waitForPreviewMode();

    // Then Second widget should be expanded
    let helloWidgetTwo = new HelloWidget(widgetElementTwo);
    helloWidgetTwo.isExpanded();

    idocPage.open(false);

    // And First widget should still be collapsed
    helloWidget.isCollapsed();
  });
});
