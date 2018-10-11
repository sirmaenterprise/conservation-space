'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;
let SaveIdocDialog = require('../../idoc/save-idoc-dialog').SaveIdocDialog;

describe('RichText control', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  describe('in form edit mode', () => {
    describe('when displayType=EDITABLE', () => {
      it('should allow to be edited', () => {
        let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let optionalDescription = widget.getForm().getRichTextField('optionalDescription').isEditable();

        expect(optionalDescription.getAsText()).to.eventually.equal('Some rich text');

        optionalDescription.clear().focusEditor().type('Changed text');

        expect(optionalDescription.getAsText()).to.eventually.equal('Changed text');
      });

      it('should have visible toolbar with plugins: [bold, italic, ordered list, unordered list]', () => {
        let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let optionalDescription = widget.getForm().getRichTextField('optionalDescription');

        optionalDescription.getEditorToolbar().isVisible();

        let toolbar = optionalDescription.getEditorToolbar();
        toolbar.isActionFontSizeVisible();
        toolbar.isActionFontColorVisible();
        toolbar.isActionBackgroundColorVisible();
        toolbar.isActionBoldVisible();
        toolbar.isActionItalicVisible();
        toolbar.isActionOrderedListVisible();
        toolbar.isActionUnorderedListVisible();
      });

      it('should be mandatory and marked as invalid when is empty', () => {
        let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription').isEditable();

        mandatoryDescription.isMandatory();
        mandatoryDescription.isInvalid();

        mandatoryDescription.type('Some text');
        mandatoryDescription.isValid();

        mandatoryDescription.clear().focusEditor().type(' ');
        mandatoryDescription.isMandatory();
        mandatoryDescription.isInvalid();
      });

      it('should set correct font size to list elements', () => {
        let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
        let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription').isEditable();
        mandatoryDescription.clear();
        mandatoryDescription.focusEditor();
        let editorToolbar = mandatoryDescription.getEditorToolbar();
        editorToolbar.orderedList();
        editorToolbar.fontSize('28');
        mandatoryDescription.type('Order list');

        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li style="font-size: 28px;"><span style="font-size:28px;">​​​​​​​Order list</span><br></li></ol>');
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

  describe('text formatting', () => {
    it('should allow bold text', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().bold();
      mandatoryDescription.type('bold text');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>​​​​​​​bold text</strong><br></p>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>bold text</strong><br></p>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>bold text</strong><br></p>');
    });

    it('should allow italic text', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().italic();
      mandatoryDescription.type('italic text');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>​​​​​​​italic text</em><br></p>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>italic text</em><br></p>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>italic text</em><br></p>');
    });

    it('should allow ordered lists', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().orderedList();
      mandatoryDescription.type('item 1').newLine().type('item 2').newLine().tab().type('item 2.1').newLine().type('item 2.2');

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');
    });

    it('should allow unordered lists', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().unorderedList();
      mandatoryDescription.type('item 1').newLine().type('item 2').newLine().tab().type('item 2.1').newLine().type('item 2.2');

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');
    });

    it('should keep unordered lists style', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().unorderedList();
      mandatoryDescription.getEditorToolbar().fontColor('Bright Blue');
      mandatoryDescription.type('colored item 1').newLine();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored item 1</span><br></li><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\">​​​​​​​</span></li></ul>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored item 1</span></li><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"></span></li></ul>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored item 1</span></li><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"></span></li></ul>');
    });

    it('should allow font size', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().fontSize(8);
      mandatoryDescription.type('fontsize 8').newLine();
      mandatoryDescription.getEditorToolbar().fontSize(72);
      mandatoryDescription.type('fontsize 72').newLine();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"font-size:8px;\">fontsize 8</span><br></p><p><span style=\"font-size:72px;\">fontsize 72</span><br></p><p><br></p>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"font-size:8px;\">fontsize 8</span><br></p><p><span style=\"font-size:72px;\">fontsize 72</span><br></p><p><br></p>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"font-size:8px;\">fontsize 8</span><br></p><p><span style=\"font-size:72px;\">fontsize 72</span><br></p><p><br></p>');
    });

    it('should allow text background', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().backgroundColor('Bright Blue');
      mandatoryDescription.type('colored text');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"background-color:#3498db !important;\">​​​​​​​colored text</span><br></p>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"background-color:#3498db !important;\">colored text</span><br></p>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"background-color:#3498db !important;\">colored text</span><br></p>');
    });

    it('should allow text color', () => {
      let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

      mandatoryDescription.getEditorToolbar().fontColor('Bright Blue');
      mandatoryDescription.type('colored text');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">​​​​​​​colored text</span><br></p>');

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored text</span><br></p>');

      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored text</span><br></p>');
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

  describe('displayed in ODW', () => {
    it('conditions should update field state properly: optional-mandatory-readonly-hidden-optional', () => {
      let widget = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let form = widget.getForm();
      let conditionalDescription = form.getRichTextField('conditionalDescription');
      let country = form.getCodelistField('country');

      conditionalDescription.isEditable();

      country.selectOption('България');
      conditionalDescription.isMandatory();

      country.selectOption('Австралия');
      conditionalDescription.isReadonly();

      country.selectOption('САЩ');
      conditionalDescription.isHidden();

      country.clearField();
      country.toggleMenu();
      conditionalDescription.isEditable();

      country.selectOption('Австралия');
      conditionalDescription.isReadonly();

      country.selectOption('България');
      conditionalDescription.isMandatory();
    });
  });

  describe('displayed in DTW', () => {
    it('should be displayed properly in all states', () => {
      let widget = idocPage.insertDTWWithFields(
        ['optionalDescription', 'mandatoryDescription', 'readonlyDescription', 'hiddenDescription'],
        {type: ObjectSelector.MANUALLY, item: 9}
      );
      let form = widget.getRow(1).getForm();
      let optionalDescription = form.getRichTextField('optionalDescription');
      let mandatoryDescription = form.getRichTextField('mandatoryDescription');
      let readonlyDescription = form.getRichTextField('readonlyDescription');
      let hiddenDescription = form.getRichTextField('hiddenDescription');

      optionalDescription.isEditable();
      optionalDescription.clear().focusEditor().type('Changed text');
      expect(optionalDescription.getAsText()).to.eventually.equal('Changed text');

      mandatoryDescription.isEditable();
      mandatoryDescription.isInvalid();
      mandatoryDescription.type('Some text');
      mandatoryDescription.isValid();
      expect(mandatoryDescription.getAsText()).to.eventually.equal('Some text');

      readonlyDescription.isReadonly('Some rich text');

      hiddenDescription.isHidden();
    });

    it('conditions should update field state properly: optional-mandatory-readonly-hidden-optional', () => {
      let widget = idocPage.insertDTWWithFields(
        ['country', 'conditionalDescription'],
        {type: ObjectSelector.MANUALLY, item: 9}
      );
      let form = widget.getRow(1).getForm();
      let conditionalDescription = form.getRichTextField('conditionalDescription');
      let country = form.getCodelistField('country');

      conditionalDescription.isEditable();

      // Mandatory fields in DTW are not marked with asterisk but with a red top-right corner on the table cell, that's
      // why we only check for the .has-error class which is applied to empty mandatory fields.
      country.selectOption('България');
      conditionalDescription.isInvalid();

      country.selectOption('Австралия');
      conditionalDescription.isReadonly();

      country.selectOption('САЩ');
      conditionalDescription.isHidden();

      country.clearField();
      country.toggleMenu();
      conditionalDescription.isEditable();

      country.selectOption('Австралия');
      conditionalDescription.isReadonly();

      country.selectOption('България');
      conditionalDescription.isInvalid();
    });
  });

  describe('displayed in multiple widgets', () => {
    it('should be updated properly when model changes (ODW-ODW)', () => {
      let widget1 = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 0);
      let form1 = widget1.getForm();
      let conditionalDescription1 = form1.getRichTextField('conditionalDescription');
      let country1 = form1.getCodelistField('country');

      let widget2 = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 1);
      let form2 = widget2.getForm();
      let conditionalDescription2 = form2.getRichTextField('conditionalDescription');
      let country2 = form2.getCodelistField('country');

      expect(conditionalDescription1.getAsText()).to.eventually.equal('');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('');
      conditionalDescription1.type('Some text');
      expect(conditionalDescription1.getAsText()).to.eventually.equal('Some text');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('Some text');

      conditionalDescription1.clear();
      country1.selectOption('България');
      conditionalDescription1.isMandatory();
      conditionalDescription2.isMandatory();
      conditionalDescription1.type('Some text');
      conditionalDescription1.isValid();
      conditionalDescription2.isValid();

      country1.selectOption('Австралия');
      conditionalDescription1.isReadonly('Some text');
      conditionalDescription2.isReadonly('Some text');

      country1.selectOption('САЩ');
      conditionalDescription1.isHidden();
      conditionalDescription2.isHidden();

      country2.clearField();
      country2.toggleMenu();
      conditionalDescription1.isEditable();
      conditionalDescription2.isEditable();
      conditionalDescription1.isOptional();
      conditionalDescription2.isOptional();

      conditionalDescription2.clear();
      country2.selectOption('България');
      conditionalDescription1.isMandatory();
      conditionalDescription2.isMandatory();
    });

    it('should be updated properly when model changes (DTW-DTW)', () => {
      let widget1 = idocPage.insertDTWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 0);
      let form1 = widget1.getRow(1).getForm();
      let conditionalDescription1 = form1.getRichTextField('conditionalDescription');
      let country1 = form1.getCodelistField('country');

      let widget2 = idocPage.insertDTWWithFields(['country', 'conditionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 1);
      let form2 = widget2.getRow(1).getForm();
      let conditionalDescription2 = form2.getRichTextField('conditionalDescription');
      let country2 = form2.getCodelistField('country');

      expect(conditionalDescription1.getAsText()).to.eventually.equal('');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('');
      conditionalDescription1.type('Some text');
      expect(conditionalDescription1.getAsText()).to.eventually.equal('Some text');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('Some text');

      conditionalDescription1.clear().focusEditor().type(' ');
      country1.selectOption('България');
      conditionalDescription1.isInvalid();
      conditionalDescription2.isInvalid();
      conditionalDescription1.type('Some text');
      conditionalDescription1.isValid();
      conditionalDescription2.isValid();

      country1.selectOption('Австралия');
      // TODO: this is failing because only one of the fields (in the secon widget) becomes readonly
      // conditionalDescription1.isReadonly('Some text');
      // conditionalDescription2.isReadonly('Some text');

      country1.selectOption('САЩ');
      // TODO: this is failing because only one of the fields (in the secon widget) becomes readonly
      // conditionalDescription1.isHidden();
      // conditionalDescription2.isHidden();

      country2.clearField();
      country2.toggleMenu();
      conditionalDescription1.isEditable();
      conditionalDescription2.isEditable();
      conditionalDescription1.isOptional();
      conditionalDescription2.isOptional();

      conditionalDescription2.clear().focusEditor().type(' ');
      country2.selectOption('България');
      conditionalDescription1.isInvalid();
      conditionalDescription2.isInvalid();
    });

    it('should properly display actual value in new widgets if changed before that', () => {
      // Given I have inserted widget with richtext field
      let widget1 = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 0);

      // And I have changed the default value
      let form1 = widget1.getForm();
      let optionalDescription1 = form1.getRichTextField('optionalDescription');
      optionalDescription1.clear().focusEditor().type('Second value');

      // focus the idoc editor before inserting the next widget because it would not succeed otherwise
      idocPage.getTabEditor(1).click();

      // When I add new widget with the same richtext field
      let widget2 = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 1);

      // Then I expect both fields to have the same value as they share one model
      let form2 = widget2.getForm();
      let optionalDescription2 = form2.getRichTextField('optionalDescription');
      expect(optionalDescription2.getAsText()).to.eventually.equal('Second value');
      expect(optionalDescription1.getAsText()).to.eventually.equal('Second value');

      // When I remove the value from the field

      // !!! Clear it twice because because it doesn't trigger events properly otherwise. First time we set an empty space
      // to allow events to be triggered and then we clear it completely.
      optionalDescription2.clear().type(' ');
      optionalDescription2.clear().type('');
      optionalDescription2.blurEditor();
      idocPage.getTabEditor(1).newLine();

      // When I add third widget with the same richtext field
      let widget3 = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 2);

      // Then I expect all fields to be empty
      let form3 = widget3.getForm();
      let optionalDescription3 = form3.getRichTextField('optionalDescription');
      expect(optionalDescription3.getAsText(), 'opt3').to.eventually.equal('');
      expect(optionalDescription2.getAsText(), 'opt2').to.eventually.equal('');
      expect(optionalDescription1.getAsText(), 'opt1').to.eventually.equal('');
    });
  });

  describe('validation', () => {
    it('should properly validate stripped text', () => {

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

      inDialogMandatoryDescription.type('Mandatory description');
      inDialogConditionalMandatoryDescription.type('Conditional mandatory description');

      saveIdocDialog.ok();
      idocPage.waitForPreviewMode();

      expect(conditionalDescription.getAsText()).to.eventually.equal('Conditional mandatory description');
      widget.toggleShowMoreButton();
      let mandatoryDescription = form.getRichTextField('mandatoryDescription');
      expect(mandatoryDescription.getAsText()).to.eventually.equal('Mandatory description');
    });

    it('should be visible in create dialog when is mandatory', () => {

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

  describe('data sanitization', () => {
    it('should properly escape unwanted tags and scripts', () => {
      let widget = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9});
      let optionalDescription = widget.getForm().getRichTextField('optionalDescription');

      // injection sample 1
      optionalDescription.clear().focusEditor().type('<!-<img src="-><img src=x onerror=alert(1)//">').blurEditor();
      idocPage.getActionsToolbar().saveIdoc(true);
      fillMandatoryFieldOnSave();
      idocPage.waitForPreviewMode();
      expect(optionalDescription.getAsText()).to.eventually.equal('<!-<img src="-><img src=x onerror=alert(1)//">');

      // injection sample 2
      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.waitForEditMode();
      optionalDescription.clear().focusEditor().type('<script>alert(123)</script>');
      idocPage.getActionsToolbar().saveIdoc();
      idocPage.waitForPreviewMode();
      expect(optionalDescription.getAsText()).to.eventually.equal('<script>alert(123)</script>');
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
      mandatoryDescription.clear().focusEditor();

      // Paste the copied layout to the richtext field
      browser.actions().keyDown(protractor.Key.CONTROL).sendKeys('v').perform();
      browser.actions().keyUp(protractor.Key.CONTROL).perform();

      expect(mandatoryDescription.getAsText()).to.eventually.equal('This text should be unwrapped');
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
      optionalDescription.clear().focusEditor().type('This text should be removed on cancel');
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

function fillMandatoryFieldOnSave() {
  let saveIdocDialog = new SaveIdocDialog();
  let mandatoryDescription = saveIdocDialog.getForm().getRichTextField('mandatoryDescription');
  mandatoryDescription.focusEditor().type('Mandatory description');
  saveIdocDialog.ok();
}
