'use strict';

let IdocPage = require('../idoc-page').IdocPage;
let FontNameMenu = require('../idoc-page').FontNameMenu;
let FontSizeMenu = require('../idoc-page').FontSizeMenu;
let HelloWidget = require('../widget/hello-widget').HelloWidget;
let HelloWidgetConfigDialog = require('../widget/hello-widget').HelloWidgetConfigDialog;

describe('Idoc editor', function () {

  const TAB_NUMBER = 1;
  let contentArea;
  let idocPage = new IdocPage();

  beforeEach(function () {
    //Given I have opened the idoc page
    idocPage.open(true);
    contentArea = idocPage.getTabEditor(TAB_NUMBER);
  });

  it('should preserve text formatting after save-edit operation', () => {
    // And I typed two lines of text and two empty lines between them
    contentArea.clear().click();
    // execute three line breaks in order to leave 2 empty rows after the first one
    contentArea.type('line 1').newLine().newLine().newLine().type('line 2').selectAll();

    // And I have set the font size to 26px to all text
    let toolbar = idocPage.getEditorToolbar(TAB_NUMBER);
    let fontSizeMenu = toolbar.getFontSizeMenu();
    fontSizeMenu.select(FontSizeMenu.FS_26);

    // And I have set the font size to Arial to all text
    let fontNameMenu = toolbar.getFontNameMenu();
    fontNameMenu.select(FontNameMenu.FN_ARIAL);

    // When I save the idoc
    let actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    browser.wait(idocPage.isSaved, DEFAULT_TIMEOUT);

    // And I open for edit the idoc
    let actionsMenu = actionsToolbar.getActionsMenu();
    actionsMenu.editIdoc();

    // Then I expect the applied styles on all lines to be preserved
    // as well as text where ever its written
    verifyParagraphStyles(contentArea, 1, [
      {tag: 'span', style: 'font-family: Arial, Arimo, Helvetica, sans-serif;'},
      {tag: 'span', style: 'font-size: 26px;'}
    ]);
    expect(contentArea.getParagraph(1).getText()).to.eventually.equal('line 1');

    verifyParagraphStyles(contentArea, 2, [
      {tag: 'span', style: 'font-family: Arial, Arimo, Helvetica, sans-serif;'},
      {tag: 'span', style: 'font-size: 26px;'}
    ]);

    verifyParagraphStyles(contentArea, 3, [
      {tag: 'span', style: 'font-family: Arial, Arimo, Helvetica, sans-serif;'},
      {tag: 'span', style: 'font-size: 26px;'}
    ]);

    verifyParagraphStyles(contentArea, 4, [
      {tag: 'span', style: 'font-family: Arial, Arimo, Helvetica, sans-serif;'},
      {tag: 'span', style: 'font-size: 26px;'}
    ]);
    expect(contentArea.getParagraph(4).getText()).to.eventually.equal('line 2');

    // When I type some text on the empty rows
    let paragraph2 = contentArea.getParagraph(2);
    paragraph2.type('pargraph 2');

    // Then I expect the text to keep the formatting I have set before the save operation
    verifyParagraphStyles(contentArea, 2, [
      {tag: 'span', style: 'font-family: Arial, Arimo, Helvetica, sans-serif;'},
      {tag: 'span', style: 'font-size: 26px;'}
    ]);
  });

  it('should put an empty line after widget insertion', function () {
    // When I insert a widget and type something in the paragraph below it
    contentArea.insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().save();
    contentArea.getParagraph(2).type(protractor.Key.NULL).type('World!');

    // Then I should see the text under the widget
    expect(contentArea.getAsText()).to.eventually.contain('Hello\nWorld!');
  });

  it.skip('should put an empty line after info box insertion', function () {
    let textToInsert = 'Text to be inserted after the info box';
    idocPage.getEditorToolbar(TAB_NUMBER).insertInfoWidget();
    let nextParagraph = contentArea.getParagraph(2);
    browser.wait(EC.presenceOf(nextParagraph.element), DEFAULT_TIMEOUT);
    nextParagraph.click();
    nextParagraph.type(protractor.Key.NULL);
    nextParagraph.type(textToInsert);
    browser.wait(EC.textToBePresentInElement(nextParagraph.element, textToInsert), DEFAULT_TIMEOUT, 'Text wasn\'t inserted after info box in a reasonable time');
  });

  it.skip('should not allow drag and drop in preview mode', function () {
    // When I insert a widget and type something in the paragraph below it
    contentArea.insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().save();

    idocPage.getActionsToolbar().saveIdoc();

    let widget = new HelloWidget(contentArea.getWidget('hello-widget'));
    widget.hover(widget);
    expect(widget.getDragHandler().isDisplayed()).to.eventually.be.false;
  });

});

function verifyParagraphStyles(contentArea, paragraphNumber, styles) {
  let paragraph = contentArea.getActivatedParagraphElement(paragraphNumber);
  let children = paragraph.$$('span');
  const stylesCount = styles.length;

  expect(children.count(), `Applied styles count should be ${stylesCount}`).to.eventually.equal(stylesCount);

  children.each((child, index) => {
    expect(child.getAttribute('style')).to.eventually.equal(styles[index].style);
  });
}