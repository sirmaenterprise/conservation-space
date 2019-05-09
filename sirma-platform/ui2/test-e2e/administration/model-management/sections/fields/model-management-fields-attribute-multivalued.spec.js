'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field multiValued attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should allow multiValued for codelist and object property (concepts) fields only', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // should be valid for codelist fields
      let country = items[4];
      verifyAttribute(country, false);

      // should become invalid for text fields
      let title = items[0];
      verifyAttribute(title, true);

      // should become invalid for boolean fields
      let checkbox = items[5];
      verifyAttribute(checkbox, true);

      // should become invalid for date fields
      let date = items[6];
      verifyAttribute(date, true);
    });

    // should be valid for object property fields
    verifyAttribute(fields.getField('resource'), false);
  });

  it('should be false by default', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.false;
      });
    });
  });

  it('should reset to the initial value on edit cancel', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;

        fields.getModelControls().getModelCancel().click();

        expect(attr.getField().isSelected()).to.eventually.be.false;
      });
    });
  });

  it('should allow to be saved when when is valid', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;

        fields.getModelControls().getModelSave().click();

        expect(attr.getField().isSelected()).to.eventually.be.true;
      });
    });
  });

  it('should disallow save operation when is invalid', () => {
    openPage('en', 'bg', 'MX1001');

    // Given I have selected for edit a boolean type field
    fields.getRegion('generalDetails').getFields().then(items => {
      let checkbox = items[5];
      checkbox.showAttributes();

      let details = fields.getModelDetails();

      // And I have edited an attribute to make the model dirty - save button should be enabled after this
      details.getBehaviourAttributesPanel().getAttribute('tooltip').then(attr => {
        attr.getField().setValue(null, 'tooltip');
      });

      details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
        // When I select the multiValued attribute
        attr.getField().toggleCheckbox();

        // Then I expect save control to be disabled as the attribute is not allowed to be selected for boolean types
        expect(fields.getModelControls().getModelSave().isDisabled(), 'Save control should be disabled!').to.eventually.be.true;

        // When I deselect the attribute
        attr.getField().toggleCheckbox();

        // Then I expect save control to be enabled
        expect(fields.getModelControls().getModelSave().isDisabled(), 'Save control should be disabled!').to.eventually.be.false;
      });
    });
  });

  function verifyAttribute(field, expectingError) {
    field.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
      attr.getField().toggleCheckbox();
      expect(attr.isInvalid(), `Attribute should be ${expectingError ? 'invalid' : 'valid'}!`).to.eventually.be[expectingError];
    });
  }
});