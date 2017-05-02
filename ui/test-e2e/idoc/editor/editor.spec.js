'use strict';

var IdocPage = require('../idoc-page');
var HelloWidget = require('../widget/hello-widget').HelloWidget;
var HelloWidgetConfigDialog = require('../widget/hello-widget').HelloWidgetConfigDialog;

describe('Idoc editor', function () {

  const TAB_NUMBER = 1;
  var contentArea;
  var idocPage = new IdocPage();

  beforeEach(function () {
    //Given I have opened the idoc page
    idocPage.open(true);
    contentArea = idocPage.getTabEditor(TAB_NUMBER);
  });

  it('should allow drag and drop of widgets', function () {
    idocPage.scrollToTop();
    //add a few lines to overcome an issue where the editor toolbar overlays the editor area
    contentArea.type(protractor.Key.ENTER).type(protractor.Key.ENTER).type(protractor.Key.ENTER);

    contentArea.type('Drag and drop widget text').type(protractor.Key.ENTER);
    contentArea.insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().save();

    //When I drag the widget and drop it above the text
    var widgetElement = idocPage.getTabEditor(TAB_NUMBER).getWidget(HelloWidget.WIDGET_NAME)

    var paragraphUnderWidget = contentArea.getParagraph(5);
    paragraphUnderWidget.click();

    contentArea.dragAndDropWidget(widgetElement, {x: 0, y: -40}, contentArea.getParagraph(3).element);

    //Then I should see the widget above the text
    expect(contentArea.getAsText()).to.eventually.contain('Hello\nDrag and drop widget text');
  });

  it('should put an empty line after widget insertion', function () {
    // When I insert a widget and type something in the paragraph below it
    contentArea.insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().save();
    contentArea.getParagraph(1).type('World!');

    // Then I should see the text under the widget
    expect(contentArea.getAsText()).to.eventually.contain('Hello\nWorld!');
  });

  it('should put an empty line after info box insertion', function () {
    var textToInsert = 'Text to be inserted after the info box';
    idocPage.getEditorToolbar(TAB_NUMBER).insertInfoWidget();
    contentArea.getParagraph(1).type(textToInsert);
    browser.wait(EC.textToBePresentInElement(contentArea.getParagraph(1).element, textToInsert), DEFAULT_TIMEOUT, 'Text wasn\'t inserted after info box in a reasonable time');
  });

  it('should not allow drag and drop in preview mode', function () {
    // When I insert a widget and type something in the paragraph below it
    contentArea.insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().save();

    idocPage.getActionsToolbar().saveIdoc();

    var widget = new HelloWidget(contentArea.getWidget('hello-widget'));
    widget.hover(widget);
    expect(widget.getDragHandler().isDisplayed()).to.eventually.be.false;
  });

});
