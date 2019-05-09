'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;
let SaveIdocDialog = require('../../idoc/save-idoc-dialog').SaveIdocDialog;

describe('RichText control - sanitization', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  afterEach(() => {
    browser.executeScript('$(".seip-modal").remove();');
  });

  it('should properly escape unwanted tags and scripts', () => {
    let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let optionalDescription = widget.getForm().getRichTextField('optionalDescription');

    // injection sample 1
    optionalDescription.clear().focusEditor().then(() => {
      optionalDescription.type('<!-<img src="-><img src=x onerror=alert(1)//">').then(() => {
        optionalDescription.blurEditor();
        idocPage.getActionsToolbar().saveIdoc(true);
        fillMandatoryFieldOnSave();
        idocPage.waitForPreviewMode();
        expect(optionalDescription.getAsText()).to.eventually.equal('<!-<img src="-><img src=x onerror=alert(1)//">');

        // injection sample 2
        idocPage.getActionsToolbar().getActionsMenu().editIdoc();
        idocPage.waitForEditMode();
        optionalDescription.clear().focusEditor().then(() => {
          optionalDescription.type('<script>alert(123)</script>').then(() => {
            idocPage.getActionsToolbar().saveIdoc();
            idocPage.waitForPreviewMode();
            expect(optionalDescription.getAsText()).to.eventually.equal('<script>alert(123)</script>');
          });
        });
      });
    });
  });

  it('should properly process content on paste ', () => {

    // Insert layout (content with .data-cke-widget-editable class)
    let editor = idocPage.getTabEditor(1);
    editor.setContent(`<div class="container-fluid layout-container"><div class="row layout-row"><div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 layout-column ">
            <div class="layout-column-one layout-column-editable cke_widget_editable" contenteditable="true" data-cke-widget-editable="layoutColumn1" data-cke-enter-mode="1">
            <p>This text should be unwrapped</p></div></div><div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 layout-column">
            <div class="layout-column-two layout-column-editable cke_widget_editable" contenteditable="true" data-cke-widget-editable="layoutColumn2" data-cke-enter-mode="1">
            <p><br></p></div></div></div></div>`);

    // Wait element to be clickable in editor
    let layout = $('.cke_widget_editable');
    browser.wait(EC.elementToBeClickable(layout), DEFAULT_TIMEOUT);

    // Use keyboard shortcuts in order to select and cut layout
    browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('a').perform();
    browser.actions().keyUp(protractor.Key.CONTROL).perform();
    browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('x').perform();
    browser.actions().keyUp(protractor.Key.CONTROL).perform();

    // Insert widget with richtext field
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');
    mandatoryDescription.clear().focusEditor().then(() => {
      // Paste the copied layout to the richtext field
      browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('v').perform();
      browser.actions().keyUp(protractor.Key.CONTROL).perform();

      expect(mandatoryDescription.getAsText()).to.eventually.equal('This text should be unwrapped');
    });
  });
});

function fillMandatoryFieldOnSave() {
  let saveIdocDialog = new SaveIdocDialog();
  let mandatoryDescription = saveIdocDialog.getForm().getRichTextField('mandatoryDescription');
  mandatoryDescription.focusEditor().then(() => {
    mandatoryDescription.type('Mandatory description').then(() => {
      saveIdocDialog.ok();
    });
  });
}
