'use strict';

var IdocPage = require('../idoc-page').IdocPage;

const IDOC_ID = 'emf:123456';
describe.skip('IdocTocHeadingHighlighter', () => {

  var idocPage = new IdocPage();
  var editor;

  beforeEach(function () {
    //Given I have opened the idoc page for edit and have inserted the following content
    idocPage.open(true, IDOC_ID);
    editor = idocPage.getTabEditor(1);
    editor.setContent(`<h1 class="h1-test-class">Heading1</h1><p id="test-content1"></p><h2 class="h2-test-class">Heading2</h2><p id="test-content2">content</p>`);

    //wait until content is inserted because editor's setData is async
    editor.waitForElementInsideContent('#test-content2');

    // simulate save and edit in order to trigger content processing and heading functionality
    idocPage.getActionsToolbar().saveIdoc();

    idocPage.getActionsToolbar().editIdoc();
    idocPage.waitForEditor(1);
  });

  it('should add active class on the nearest top heading element', () => {

    // select content
    var selectedParagraph = editor.getParagraphBySelector('#test-content2');
    selectedParagraph.click();

    editor.waitForElementInsideContent('.h2-test-class.idoc-section-selected');
  });

  it('should remove active class when the selection is changed', () => {

    // make selection
    var selectedParagraph = editor.getParagraphBySelector('#test-content2');
    selectedParagraph.click();

    // change the selection
    selectedParagraph = editor.getParagraphBySelector('#test-content1');
    selectedParagraph.click();

    editor.waitForElementInsideContent('.h1-test-class.idoc-section-selected');
    editor.waitForStalenessOfElementInsideContent('.h2-test-class.idoc-section-selected');

  });

});