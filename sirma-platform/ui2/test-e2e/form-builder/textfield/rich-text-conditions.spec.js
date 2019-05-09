'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;

describe.skip('RichText control - conditions', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  afterEach(() => {
    browser.executeScript('$(".seip-modal").remove();');
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
      optionalDescription.clear().focusEditor().then(() => {
        optionalDescription.type('Changed text').then(() => {
          expect(optionalDescription.getAsText()).to.eventually.equal('Changed text');

          mandatoryDescription.isEditable();
          mandatoryDescription.isInvalid();
          mandatoryDescription.type('Some text').then(() => {
            mandatoryDescription.isValid();
            expect(mandatoryDescription.getAsText()).to.eventually.equal('Some text');

            readonlyDescription.isReadonly('Some rich text');

            hiddenDescription.isHidden();
          });
        });
      });
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
});