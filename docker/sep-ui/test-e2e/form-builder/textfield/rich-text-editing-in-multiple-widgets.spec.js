'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;

describe.skip('RichText control - editing in multiple widgets', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  afterEach(() => {
    browser.executeScript('$(".seip-modal").remove();');
  });

  it('should be updated properly when model changes (ODW-ODW)', () => {
    let widget1 = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {
      type: ObjectSelector.MANUALLY,
      item: 9
    }, 0);
    let form1 = widget1.getForm();
    let conditionalDescription1 = form1.getRichTextField('conditionalDescription');
    let country1 = form1.getCodelistField('country');

    let widget2 = idocPage.insertODWWithFields(['country', 'conditionalDescription'], {
      type: ObjectSelector.MANUALLY,
      item: 9
    }, 1);
    let form2 = widget2.getForm();
    let conditionalDescription2 = form2.getRichTextField('conditionalDescription');
    let country2 = form2.getCodelistField('country');

    expect(conditionalDescription1.getAsText()).to.eventually.equal('');
    expect(conditionalDescription2.getAsText()).to.eventually.equal('');
    conditionalDescription1.type('Some text').then(() => {
      expect(conditionalDescription1.getAsText()).to.eventually.equal('Some text');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('Some text');

      conditionalDescription1.clear();
      country1.selectOption('България');
      conditionalDescription1.isMandatory();
      conditionalDescription2.isMandatory();
      conditionalDescription1.type('Some text').then(() => {
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
    });
  });

  it('should be updated properly when model changes (DTW-DTW)', () => {
    let widget1 = idocPage.insertDTWWithFields(['country', 'conditionalDescription'], {
      type: ObjectSelector.MANUALLY,
      item: 9
    }, 0);
    let form1 = widget1.getRow(1).getForm();
    let conditionalDescription1 = form1.getRichTextField('conditionalDescription');
    let country1 = form1.getCodelistField('country');

    let widget2 = idocPage.insertDTWWithFields(['country', 'conditionalDescription'], {
      type: ObjectSelector.MANUALLY,
      item: 9
    }, 1);
    let form2 = widget2.getRow(1).getForm();
    let conditionalDescription2 = form2.getRichTextField('conditionalDescription');
    let country2 = form2.getCodelistField('country');

    expect(conditionalDescription1.getAsText()).to.eventually.equal('');
    expect(conditionalDescription2.getAsText()).to.eventually.equal('');
    conditionalDescription1.type('Some text').then(() => {
      expect(conditionalDescription1.getAsText()).to.eventually.equal('Some text');
      expect(conditionalDescription2.getAsText()).to.eventually.equal('Some text');

      conditionalDescription1.clear().focusEditor().then(() => {
        conditionalDescription1.type(' ').then(() => {
          country1.selectOption('България');
          conditionalDescription1.isInvalid();
          conditionalDescription2.isInvalid();
          conditionalDescription1.type('Some text').then(() => {
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

            conditionalDescription2.clear().focusEditor().then(() => {
              conditionalDescription2.type(' ').then(() => {
                country2.selectOption('България');
                conditionalDescription1.isInvalid();
                conditionalDescription2.isInvalid();
              });
            });
          });
        });
      });
    });
  });

  it('should properly display actual value in new widgets if changed before that', () => {
    // Given I have inserted widget with richtext field
    let widget1 = idocPage.insertODWWithFields(['optionalDescription'], {type: ObjectSelector.MANUALLY, item: 9}, 0);

    // And I have changed the default value
    let form1 = widget1.getForm();
    let optionalDescription1 = form1.getRichTextField('optionalDescription');
    optionalDescription1.clear().focusEditor().then(() => {
      optionalDescription1.type('Second value').then(() => {
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

        // !!! Trigger tab key because clear doesn't trigger events properly otherwise.
        optionalDescription2.clear().tab();
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
  });
});