'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;
let SaveIdocDialog = require('../../idoc/save-idoc-dialog').SaveIdocDialog;

describe('RichText control', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  afterEach(() => {
    browser.executeScript('$(".seip-modal").remove();');
  });

  describe('in form edit mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let optionalDescription = widget.getForm().getRichTextField('optionalDescription').isEditable();

        expect(optionalDescription.getAsText()).to.eventually.equal('Some rich text');

        optionalDescription.clear().focusEditor().then(() => {
          optionalDescription.type('Changed text').then(() => {
            expect(optionalDescription.getAsText()).to.eventually.equal('Changed text');
          });
        });
      });

      it('should have visible toolbar with plugins: [bold, italic, ordered list, unordered list]', () => {
        let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let optionalDescription = widget.getForm().getRichTextField('optionalDescription');

        optionalDescription.getEditorToolbar().then((toolbar) => {
          toolbar.isVisible();
        });

        optionalDescription.getEditorToolbar().then((toolbar) => {
          toolbar.isActionFontSizeVisible();
          toolbar.isActionFontColorVisible();
          toolbar.isActionBackgroundColorVisible();
          toolbar.isActionBoldVisible();
          toolbar.isActionItalicVisible();
          toolbar.isActionOrderedListVisible();
          toolbar.isActionUnorderedListVisible();
        });
      });

      it('should be mandatory and marked as invalid when is empty', () => {
        let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription').isEditable();

        mandatoryDescription.isMandatory();
        mandatoryDescription.isInvalid();

        mandatoryDescription.type('Some text').then(() => {
          mandatoryDescription.isValid();

          mandatoryDescription.clear().focusEditor().then(() => {
            mandatoryDescription.type(' ').then(() => {
              mandatoryDescription.isMandatory();
              mandatoryDescription.isInvalid();
            });
          });
        });
      });

      it('should set correct font size to list elements', () => {
        let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription').isEditable();
        mandatoryDescription.clear();
        mandatoryDescription.focusEditor().then(() => {
          mandatoryDescription.getEditorToolbar().then((toolbar) => {
            toolbar.orderedList().then(() => {
              toolbar.fontSize('28').then(() => {
                mandatoryDescription.type('Order list').then(() => {
                  expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li style="font-size: 28px;"><span style="font-size:28px;">​​​​​​​Order list</span><br></li></ol>');
                });
              });
            });
          });

        });
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview mode and to have value', () => {
        let widget = idocPage.insertODWWithFields(['readonlyDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let readonlyDescription = widget.getForm().getRichTextField('readonlyDescription');

        expect(readonlyDescription.getAsText()).to.eventually.equal('Some rich text');
      });

      it('should be readonly', () => {
        let widget = idocPage.insertODWWithFields(['readonlyDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        widget.getForm().getRichTextField('readonlyDescription').isReadonly();
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should not be present', () => {
        let widget = idocPage.insertODWWithFields(['hiddenDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        widget.getForm().isFieldHidden('hiddenDescription');
      });
    });
  });

  describe('in form preview mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should be visible in preview and to have value', () => {
        let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        idocPage.getActionsToolbar().saveIdoc(true);
        fillMandatoryFieldOnSave();
        idocPage.waitForPreviewMode();

        let optionalDescription = widget.getForm().getRichTextField('optionalDescription');
        expect(optionalDescription.getAsText()).to.eventually.equal('Some rich text');
      });
    });

    describe('when displayType=READ_ONLY', () => {
      it('should be visible in preview and to have value', () => {
        let widget = idocPage.insertODWWithFields(['readonlyDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        idocPage.getActionsToolbar().saveIdoc(true);
        fillMandatoryFieldOnSave();
        idocPage.waitForPreviewMode();

        let readonlyDescription = widget.getForm().getRichTextField('readonlyDescription');
        expect(readonlyDescription.getAsText()).to.eventually.equal('Some rich text');
      });
    });

    describe('when displayType=HIDDEN', () => {
      it('should be visible in preview and to have value', () => {
        let widget = idocPage.insertODWWithFields(['hiddenDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        idocPage.getActionsToolbar().saveIdoc(true);
        fillMandatoryFieldOnSave();
        idocPage.waitForPreviewMode();

        let hiddenDescription = widget.getForm().getRichTextField('hiddenDescription');
        expect(hiddenDescription.getAsText()).to.eventually.equal('Some rich text');
      });
    });
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      let widget = idocPage.insertODWWithFields(['optionalDescription', 'mandatoryDescription', 'readonlyDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let form = widget.getForm();
      let optionalDescription = form.getRichTextField('optionalDescription');
      let mandatoryDescription = form.getRichTextField('mandatoryDescription');
      let readonlyDescription = form.getRichTextField('readonlyDescription');

      optionalDescription.isTooltipIconVisible();
      mandatoryDescription.isTooltipIconVisible();
      readonlyDescription.isTooltipIconVisible();

      idocPage.getActionsToolbar().saveIdoc(true);
      fillMandatoryFieldOnSave();
      idocPage.waitForPreviewMode();

      optionalDescription.isTooltipIconHidden();
      mandatoryDescription.isTooltipIconHidden();
      readonlyDescription.isTooltipIconHidden();
    });
  });

  describe('displayed in save and create dialogs', () => {
    it('should be visible in save dialog when is mandatory or invalid', () => {
      let widget = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let form = widget.getForm();
      let conditionalDescription = form.getRichTextField('conditionalDescription');
      let country = form.getCodelistField('country');
      country.selectOption('България');
      conditionalDescription.isInvalid();
      conditionalDescription.isMandatory();

      idocPage.getActionsToolbar().saveIdoc(true);
      let saveIdocDialog = new SaveIdocDialog();
      let inDialogMandatoryDescription = saveIdocDialog.getForm().getRichTextField('mandatoryDescription');
      let inDialogConditionalMandatoryDescription = saveIdocDialog.getForm().getRichTextField('conditionalDescription');

      inDialogMandatoryDescription.isInvalid();
      inDialogMandatoryDescription.isMandatory();
      inDialogConditionalMandatoryDescription.isInvalid();
      inDialogConditionalMandatoryDescription.isMandatory();

      inDialogMandatoryDescription.type('Mandatory description').then(() => {
        inDialogConditionalMandatoryDescription.type('Conditional mandatory description').then(() => {
          saveIdocDialog.ok();
          idocPage.waitForPreviewMode();

          expect(conditionalDescription.getAsText()).to.eventually.equal('Conditional mandatory description');
          widget.toggleShowMoreButton();
          let mandatoryDescription = form.getRichTextField('mandatoryDescription');
          expect(mandatoryDescription.getAsText()).to.eventually.equal('Mandatory description');
        });
      });
    });
  });

  describe('on component destroying event', () => {
    it('should properly remove the toolbar', () => {
      // Initially only the idoc editor's toolbar is present.
      expect($$('.cke_top').count()).to.eventually.equal(1);

      let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      // Another editor toolbar for the inserted richtext field
      expect($$('.cke_top').count()).to.eventually.equal(2);

      widget.toggleShowMoreButton();
      // There should be 4 visible fields with their toolbars and the one for the idoc editor
      expect($$('.cke_top').count()).to.eventually.equal(5);

      widget.toggleShowMoreButton();
      // Idoc editor plus the optionalDescription field toolbars
      expect($$('.cke_top').count()).to.eventually.equal(2);

      widget.getHeader().remove();
      // Only the idoc editor toolbar should be left again
      expect($$('.cke_top').count()).to.eventually.equal(1);
    });
  });

  describe('on Cancel', () => {
    it('should remove unsaved data from field', () => {
      let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let optionalDescription = widget.getForm().getRichTextField('optionalDescription');
      idocPage.getActionsToolbar().saveIdoc(true);
      fillMandatoryFieldOnSave();
      idocPage.waitForPreviewMode();

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();
      optionalDescription.clear().focusEditor().then(() => {
        optionalDescription.type('This text should be removed on cancel').then(() => {
          idocPage.getActionsToolbar().cancelSave();
          let confirmation = idocPage.getConformationPopup();
          let confirmationElement = confirmation.getConfirmationPopup();
          browser.wait(EC.visibilityOf(confirmationElement), DEFAULT_TIMEOUT);
          confirmation.clickConfirmButton();
          browser.wait(EC.stalenessOf(confirmationElement), DEFAULT_TIMEOUT);

          expect(optionalDescription.getAsText()).to.eventually.equal('Some rich text');
        });
      });
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
