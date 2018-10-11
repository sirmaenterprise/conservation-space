'use strict';

var IdocPage = require('../idoc-page').IdocPage;
var HelloWidget = require('./hello-widget').HelloWidget;
var HelloWidgetConfigDialog = require('./hello-widget').HelloWidgetConfigDialog;

describe('Widget', function () {

  const WIDGET_TYPE = 'hello-widget';
  var idocPage = new IdocPage();
  var widgetElement;

  beforeEach(function () {
    //Given I have opened the idoc page for edit and have inserted the hello widget
    idocPage.open(true);
    widgetElement = idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
  });

  it('should allow configuration using the widget actions menu', function () {
    //When I select the configuration action,set a name and save
    var helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.setName('World');
    helloConfigDialog.save();

    //Then I should see the input name
    var helloWidget = new HelloWidget(widgetElement);
    return expect(helloWidget.getMessage()).to.eventually.equal('Hello World');
  });

  it('should not be inserted when the initial config dialog is cancelled', function () {
    //When I set the widget name
    var helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.cancel(true);
    //Then I should not see the widget and its wrapper should not be present in the DOM
    expect(widgetElement.isPresent()).to.eventually.be.false;
    expect($('.cke_widget_wrapper').isPresent()).to.eventually.be.false;
  });

  it('should not save configuration when the config dialog is cancelled', function () {
    // And closed the initially opened config dialog
    var helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    //When I select the configuration action,set a name and save
    var helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().openConfig();
    helloConfigDialog.setName('World');
    helloConfigDialog.cancel();

    //Then I should NOT see the input name
    return expect(helloWidget.getMessage()).to.eventually.not.equal('Hello World');
  });

  it('should be removed when the remove button is selected', function () {
    // And closed the initially opened config dialog
    var helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    //When I select the configuration action,set a name and save
    var helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().remove();

    // Then I should see neither the widget nor its wrapper
    expect(widgetElement.isPresent()).to.eventually.be.false;
    expect($('.cke_widget_wrapper').isPresent()).to.eventually.be.false;
  });

  it('should not break drag and drop when a widget is removed @slow', function () {
    // Given I have inserted two widgets and a text between them
    new HelloWidgetConfigDialog().save();

    var contentArea = idocPage.getTabEditor(1);
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

  it('should allow editing its title in edit mode', function () {
    // And closed the initially opened config dialog
    var helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();

    //When I change the title
    const TITLE = 'New title';
    var helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().setTitle(TITLE);

    //Then I should see the change title in the hello widget body

    //wait for the title to appear because it's updated async
    browser.wait(EC.textToBePresentInElement(helloWidget.titleElement, 'Title: New title'), DEFAULT_TIMEOUT);

    expect(helloWidget.getTitle()).to.eventually.equal('Title: New title');
  });

  it('should expand collaps widget', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();
    let button = element(by.css('.expand-button'));
    expect(button).to.be.present;
    // We expect that the widget will be expandet by default.
    let widgetBody = element(by.css('.panel-body'));
    expect(widgetBody).to.be.present;
    button.click();
    expect(widgetBody).to.not.be.present;
    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();
    expect(button).to.be.present;
    expect(widgetBody).to.not.be.present;
    button.click();
    expect(widgetBody).to.be.present;
  });

  it('should show correct config header when empty title', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();
    let button = element(by.css('.config-button'));
    button.click();
    let configHeader = $('.modal-title');
    browser.wait(EC.visibilityOf(configHeader), DEFAULT_TIMEOUT);

    expect(configHeader.getText()).to.eventually.equal('Hello Widget widget configuration');
    helloConfigDialog.save();
  });

  it('should show correct config header with widget title', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();
    //When I change the title
    const TITLE = 'New title';
    var helloWidget = new HelloWidget(widgetElement);
    helloWidget.getHeader().setTitle(TITLE);

    let button = element(by.css('.config-button'));
    button.click();
    let configHeader = $('.modal-title');

    browser.wait(EC.visibilityOf(configHeader), DEFAULT_TIMEOUT);
    expect(configHeader.getText()).to.eventually.equal('Hello Widget widget configuration: ' + TITLE);
    helloConfigDialog.save();
  });

  it('test with 2 idocs ', () => {
    let helloConfigDialog = new HelloWidgetConfigDialog();
    helloConfigDialog.save();
    let button = element(by.css('.expand-button'));
    button.click();
    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    idocPage.waitForPreviewMode();
    expect(button).to.be.present;

    idocPage.open(true);
    idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
    let helloConfigDialogTwo = new HelloWidgetConfigDialog();
    helloConfigDialogTwo.save();
    let actionsToolbarTwo = idocPage.getActionsToolbar();
    actionsToolbarTwo.saveIdoc();
    idocPage.waitForPreviewMode();
    let widgetBodyTwo = element(by.css('.panel-body'));
    expect(widgetBodyTwo).to.be.present;
    idocPage.open(false);
    let widgetBody = element(by.css('.panel-body'));
    expect(widgetBody).to.not.be.present;
  });
});
