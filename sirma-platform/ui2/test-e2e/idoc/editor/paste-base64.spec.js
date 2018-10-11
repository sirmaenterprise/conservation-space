'use strict';

let IdocPage = require('../idoc-page').IdocPage;

describe('Paste base64 plugin', () => {
  let idocPage = new IdocPage();
  let editor;
  beforeEach(() => {
    idocPage.open(true);
    editor = idocPage.getTabEditor(1);
  });

  it('Should copy and paste image and remove data-embedded-id', () => {
    // the path here is relative to idoc-page url
    editor.setContent(`<img id="testPic" data-embedded-id="emf:f97221f1-ff3e-497c-a08c-e6267cb37622" data-original="../editor/test-image.png" src="../editor/test-image.png" />`);

    let testPic = $('#testPic');
    browser.wait(EC.elementToBeClickable(testPic), DEFAULT_TIMEOUT);

    testPic.click();

    // Use keyboard shortcuts in order to copy and paste image
    browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('c').perform();
    browser.actions().keyUp(protractor.Key.CONTROL).perform();

    // clear in order to have only the pasted image as content
    editor.clear();

    editor.click();

    // Insert the copied image to the editor
    browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('v').perform();
    browser.actions().keyUp(protractor.Key.CONTROL).perform();

    // Assert that data-embedded-id is not presented on the pasted image because the backend should generate new one on save
    testPic = $('img');
    browser.wait(EC.presenceOf(testPic), DEFAULT_TIMEOUT);

    testPic = $('[data-embedded-id]');
    browser.wait(EC.stalenessOf(testPic), DEFAULT_TIMEOUT);

  });

});