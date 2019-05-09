'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let SingleSelectMenu = require('../../../../form-builder/form-control').SingleSelectMenu;

describe('Models management field type attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  describe('Types initialization', () => {

    it('should show ALPHA_NUMERIC attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      let unlimitedText = fields.getField('unlimited');
      unlimitedText.showAttributes();
      verifySelectedTypeOption(unlimitedText, 'ALPHA_NUMERIC_TYPE', 'Буквено-цифрово');
    });

    it('should show ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select title field
        let title = items[0];
        title.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(title, 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'Буквено-цифрово с ограничение');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('an..');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('180');
        });
      });
    });

    it('should show ALPHA_NUMERIC_FIXED_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select fixed length title field
        let fixedLengthTitle = items[7];
        fixedLengthTitle.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(fixedLengthTitle, 'ALPHA_NUMERIC_FIXED_TYPE', 'Буквено-цифрово с фиксирана дължина');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('an');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('180');
        });
      });
    });

    it('should show CODELIST attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select state field
        let state = items[1];
        state.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(state, 'CODELIST', 'Номенклатура');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
          expect(attr.getField().getSelectedValue()).to.eventually.equal('1');
          expect(attr.getField().getSelectedLabel()).to.eventually.equal('1 - Project state');
        });
      });
    });

    it('should show BOOLEAN attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select checkbox field
        let checkbox = items[5];
        checkbox.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(checkbox, 'BOOLEAN', 'Булева стойност');
      });
    });

    it('should show DATE_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select date field
        let date = items[6];
        date.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(date, 'DATE_TYPE', 'Дата');
      });
    });

    it('should show NUMERIC_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select numeric field
        let numericField = items[8];
        numericField.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(numericField, 'NUMERIC_TYPE', 'Цифрово');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('n..');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('10');
        });
      });
    });

    it('should show NUMERIC_FIXED_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select numeric fixed field
        let numericFixedField = items[9];
        numericFixedField.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(numericFixedField, 'NUMERIC_FIXED_TYPE', 'Цифрово с фиксирана дължина');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('n');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('10');
        });
      });
    });

    it('should show FLOATING_POINT_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select floating point field
        let floatingPointField = items[10];
        floatingPointField.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(floatingPointField, 'FLOATING_POINT_TYPE', 'С плаваща запетая');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('n..');
          expect(attr.getFloatingPointLengthRestrictionField().getValue()).to.eventually.equal('10');
          expect(attr.getAfterFloatingPointRestrictionField().getValue()).to.eventually.equal('5');
        });
      });
    });

    it('should show FLOATING_POINT_FIXED_TYPE attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select floating point fixed field
        let floatingPointFixedField = items[11];
        floatingPointFixedField.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(floatingPointFixedField, 'FLOATING_POINT_FIXED_TYPE', 'С плаваща запетая с фиксирана дължина');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('n');
          expect(attr.getFloatingPointLengthRestrictionField().getValue()).to.eventually.equal('10');
          expect(attr.getAfterFloatingPointRestrictionField().getValue()).to.eventually.equal('5');
        });
      });
    });

    it('should show NUMERIC_FIXED_TYPE attribute with double range correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page.
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select numeric fixed field with double range
        let numericDoubleRangeField = items[13];
        numericDoubleRangeField.showAttributes();
        // Then it's type value should be filled correct
        verifySelectedTypeOption(numericDoubleRangeField, 'NUMERIC_FIXED_TYPE', 'Цифрово с фиксирана дължина');
        let details = fields.getModelDetails();
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('n');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('10');
        });
      });
    });

    it('should show inherited and own type attribute values correct', () => {
      openPage('en', 'bg', 'MX1001');

      fields.getRegion('generalDetails').getFields().then(items => {
        // when I select own attribute it's value should be correct
        let date = items[6];
        date.showAttributes();
        verifyAvailableTypeOptions(date, ['DATE_TYPE', 'DATETIME_TYPE']);

        // when I select inherited attribute it's value should be correct
        let description = fields.getField('description');
        description.showAttributes();
        let details = fields.getModelDetails();
        verifySelectedTypeOption(description, 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'Буквено-цифрово с ограничение');
        details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
          expect(attr.getTypeField().getText()).to.eventually.equal('an..');
          expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('180');
        });

        // when I reselect own attribute it's value should be correct
        date = items[6];
        date.showAttributes();
        verifyAvailableTypeOptions(date, ['DATE_TYPE', 'DATETIME_TYPE']);
      });
    });
  });

  describe('Range/Type mapping', () => {

    //  Range - type map:
    // 'http://www.w3.org/2001/XMLSchema#long': [NUMERIC_TYPE, NUMERIC_FIXED_TYPE, FLOATING_POINT_TYPE, FLOATING_POINT_FIXED_TYPE],
    // 'http://www.w3.org/2001/XMLSchema#double': [NUMERIC_TYPE, NUMERIC_FIXED_TYPE, FLOATING_POINT_TYPE, FLOATING_POINT_FIXED_TYPE],
    // 'http://www.w3.org/2001/XMLSchema#float': [FLOATING_POINT_TYPE, FLOATING_POINT_FIXED_TYPE],
    // 'http://www.w3.org/2001/XMLSchema#int': [NUMERIC_TYPE, NUMERIC_FIXED_TYPE],
    // 'http://www.w3.org/2001/XMLSchema#dateTime': [DATE_TYPE, DATETIME_TYPE],
    // 'http://www.w3.org/2001/XMLSchema#boolean': [BOOLEAN],
    // 'http://www.w3.org/2001/XMLSchema#string': [ALPHA_NUMERIC_TYPE, ALPHA_NUMERIC_FIXED_TYPE, ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE, CODELIST]

    it('should filter attributes with range #string correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#string
        let title = items[0];
        title.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(title, ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'CODELIST']);
      });
    });

    it('should filter attributes with range #boolean correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#boolean
        let checkbox = items[5];
        checkbox.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(checkbox, ['BOOLEAN']);
      });
    });

    it('should filter attributes with range #dateTime correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#dateTime
        let date = items[6];
        date.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(date, ['DATE_TYPE', 'DATETIME_TYPE']);
      });
    });

    it('should filter attributes with range #int correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#int
        let numericFixedField = items[9];
        numericFixedField.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(numericFixedField, ['NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE']);
      });
    });

    it('should filter attributes with range #float correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#float
        let floatingPointFixedField = items[11];
        floatingPointFixedField.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(floatingPointFixedField, ['FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE']);
      });
    });

    it('should filter attributes with range #long correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#long
        let floatingPointField = items[10];
        floatingPointField.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(floatingPointField, ['NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE', 'FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE']);
      });
    });

    it('should filter attributes with range #double correct', () => {
      openPage('en', 'bg', 'MX1001');

      // Given I have open model management page
      fields.getRegion('generalDetails').getFields().then(items => {
        // When I select attribute with range http://www.w3.org/2001/XMLSchema#double
        let numericDoubleRangeField = items[13];
        numericDoubleRangeField.showAttributes();
        // Then correct values should be filtered
        verifyAvailableTypeOptions(numericDoubleRangeField, ['NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE', 'FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE']);
      });
    });
  });

  it('should keep model unchanged on load', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;

      let title = items[0];
      title.showAttributes();
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;
    });
  });

  it('should make field invalid if format is wrong', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      title.showAttributes();

      let details = fields.getModelDetails();
      // alpha numeric constraints should be  positive numbers
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        attr.getAlphaNumericRestrictionField().setValue(null, '');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Атрибутът е задължителен! Очаквания шаблон е an.. <число>');
        });
        attr.getAlphaNumericRestrictionField().setValue(null, '50');
        expect(attr.isInvalid()).to.eventually.be.false;
        attr.getAlphaNumericRestrictionField().setValue(null, 'string');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Очаквания шаблон е an.. <число>');
        });
        attr.getAlphaNumericRestrictionField().setValue(null, '100');
        expect(attr.isInvalid()).to.eventually.be.false;
        // out of range
        attr.getAlphaNumericRestrictionField().setValue(null, '9223372036854775807');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Въведената стойност е извън граници и не трябва да превишава 100000');
        });
      });

      let floatingPointField = items[10];
      floatingPointField.showAttributes();
      // floating point constraints should be  positive numbers
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        attr.getFloatingPointLengthRestrictionField().setValue(null, '');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Очаквания шаблон е n.. <число> , <число>');
        });
        attr.getFloatingPointLengthRestrictionField().setValue(null, 5);
        attr.getAfterFloatingPointRestrictionField().setValue(null, '');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Очаквания шаблон е n.. <число> , <число>');
        });
        attr.getFloatingPointLengthRestrictionField().setValue(null, 5);
        attr.getAfterFloatingPointRestrictionField().setValue(null, 2);
        expect(attr.isInvalid()).to.eventually.be.false;

        attr.getFloatingPointLengthRestrictionField().setValue(null, 5);
        attr.getAfterFloatingPointRestrictionField().setValue(null, '');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Очаквания шаблон е n.. <число> , <число>');
        });
        attr.getFloatingPointLengthRestrictionField().setValue(null, 'string');
        attr.getAfterFloatingPointRestrictionField().setValue(null, 5);
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getValidationMessages().getMessages().then(messages => {
          expect(messages[0]).to.eq('Невалидна стойност! Очаквания шаблон е n.. <число> , <число>');
        });
      });
    });
  });

  it('should clear constraints when type is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      title.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 2, true);
      });
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('');
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getAlphaNumericRestrictionField().setValue(null, '5');
      });

      // When I reset initial type option type constraints should be cleared
      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 3, true);
      });
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('');
        expect(attr.isInvalid()).to.eventually.be.true;
      });
    });
  });

  it('should update constraints correct when one of the floating point values is missing', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let floatingPointFixedField = items[11];
      floatingPointFixedField.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 1, true);
      });
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getFloatingPointLengthRestrictionField().getValue()).to.eventually.equal('');
        expect(attr.getAfterFloatingPointRestrictionField().getValue()).to.eventually.equal('');
        attr.getFloatingPointLengthRestrictionField().setValue(null, '3');
        expect(attr.getAfterFloatingPointRestrictionField().getValue()).to.eventually.equal('');
      });
    });
  });

  it('should not mark inherited attribute as dirty if initial value is reset', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inheritedType = items[2];
      inheritedType.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
        attr.getField().selectFromMenu(null, 3, true);
        expect(attr.isDirty()).to.eventually.be.true;
        attr.getField().selectFromMenu(null, 2, true);
        expect(attr.isDirty()).to.eventually.be.false;
      });

    });
  });

  it('should reset initial value when cancel changes button is clicked', () => {
    // Given I have open model management page
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      title.showAttributes();
      // when I change type
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 2, true);
      });
      // Then constraints are changed
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('');
        expect(attr.isInvalid()).to.eventually.be.true;
      });

      // when I click cancel changes button
      fields.getModelControls().getModelCancel().click();

      // Then changes are reset to initial state
      verifySelectedTypeOption(title, 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'Буквено-цифрово с ограничение');
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getTypeField().getText()).to.eventually.equal('an..');
        expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('180');
      });

      // when I change constraint
      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        attr.getAlphaNumericRestrictionField().setValue(null, '50');
        expect(attr.isInvalid()).to.eventually.be.false;
      });

      // when I click cancel changes button
      fields.getModelControls().getModelCancel().click();

      // Then changes are reset to initial state
      verifySelectedTypeOption(title, 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'Буквено-цифрово с ограничение');
    });
  });

  it('should not edit object properties type', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let creator = items[12];
      creator.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        expect(attr.getField().isDisabled()).to.eventually.be.true;
      });
    });
  });

  it('should make field invalid if no codeList is selected', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      title.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 4, true);
      });
      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        expect(attr.isInvalid()).to.eventually.be.true;
        attr.getField().selectFromMenu(null, 3, true);
        expect(attr.isInvalid()).to.eventually.be.false;
      });
    });
  });

  it('should update select fields when code list value is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      expect(country.isEditable()).to.eventually.be.true;

      let field = new SingleSelectMenu(country.getEditControl());
      expect(field.getSelectedValue()).to.eventually.eq('GBR');
      expect(field.getMenuValues()).to.eventually.deep.eq(['GBR', 'BGN', 'AUS', 'FRA', 'USA', 'CAN']);

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        attr.getField().selectFromMenu(null, 3, true);
      });
      expect(field.getMenuValues()).to.eventually.deep.eq(['LOW', 'MEDIUM', 'HIGH']);
    });
  });

  it('should clear selected "Controlled vocabulary" value if newly selected type option is not CODELIST', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.equal('555');
        expect(attr.getField().getSelectedLabel()).to.eventually.equal('555 - Country');
      });

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 2, true);
      });

      details.getBehaviourAttributesPanel().getAttribute('type').then(attr => {
        expect(attr.getAlphaNumericRestrictionField().getValue()).to.eventually.equal('');
        expect(attr.isInvalid()).to.eventually.be.true;
      });

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        attr.getField().selectFromMenu(null, 4, true);
      });

      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.equal(null);
        expect(attr.getField().getSelectedLabel()).to.eventually.equal(null);
      });
    });
  });

  function verifySelectedTypeOption(field, value, label) {
    field.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
      expect(attr.getField().getSelectedValue()).to.eventually.equal(value);
      expect(attr.getField().getSelectedLabel()).to.eventually.equal(label);
    });
  }

  function verifyAvailableTypeOptions(field, values) {
    field.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
      expect(attr.getField().getMenuValues()).to.eventually.deep.eq(values);
    });
  }
});