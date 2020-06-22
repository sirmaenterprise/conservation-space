'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils').ModelTestUtils;

describe('Models management field displayType attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should show/hide control of a field when displayType value is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      expect(items[0].isEditable()).to.eventually.be.true;
      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        attr.getField().selectFromMenu(null, 2, true);
      });
      expect(items[0].isEditable()).to.eventually.be.false;
    });
  });

  it('should reset initial displayType value when \'Cancel changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
        attr.getField().selectFromMenu(null, 2, true);
        expect(attr.getField().getSelectedValue()).to.eventually.equal('HIDDEN');
        fields.getModelControls().getModelCancel().click();
        expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
      });
    });
  });

  it('should save displayType value when \'Save changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
        attr.getField().selectFromMenu(null, 2, true);
        expect(attr.getField().getSelectedValue()).to.eventually.equal('HIDDEN');
        fields.getModelControls().getModelSave().click();
        expect(attr.getField().getSelectedValue()).to.eventually.equal('HIDDEN');
      });
    });
  });

  it('should save displayType value of inherited field when \'Save changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inheritedField = items[0];
      inheritedField.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        expect(inheritedField.isInherited(), 'Field should be inherited!').to.eventually.be.true;
        expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
        attr.getField().selectFromMenu(null, 3, true);
        expect(attr.getField().getSelectedValue()).to.eventually.equal('READ_ONLY');
        expect(attr.isInvalid(), 'Display type attribute should be invalid!').to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError(), 'Validation error should be displayed!').to.eventually.be.true;
      });
      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected(), 'Field should be set as optional!').to.eventually.be.false;
      });
      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        expect(attr.isInvalid(), 'Display type attribute should be valid!').to.eventually.be.false;
      });
      fields.getModelControls().getModelSave().click();
      expect(inheritedField.isInherited(), 'The field should not be inherited!').to.eventually.be.false;
    });
  });

  it('should show/hide error messages correct when display type value is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.false;
      });
      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        attr.getField().selectFromMenu(null, 2, true);
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
      });
      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
      });
      expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    });
  });

  it('should show error messages correct when inherited attribute is reset', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      items[1].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
      });
      details.getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.equal('READ_ONLY');
        attr.getField().selectFromMenu(null, 2, true);
        expect(attr.getField().getSelectedValue()).to.eventually.equal('HIDDEN');
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        attr.getField().selectFromMenu(null, 3, true);
        expect(attr.getField().getSelectedValue()).to.eventually.equal('READ_ONLY');
        expect(attr.isInvalid()).to.eventually.be.true;
      });
      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
      });
    });
  });

  it('should not be able to save after restoring display type from parent and field is invalid', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // restore overridden display attribute
      panel.restoreAttribute('displayType').ok();

      // extract the restored display attribute
      return panel.getAttribute('displayType');
    }).then(attr => {
      // attribute should not be valid after restore
      expect(attr.isInvalid()).to.eventually.be.true;

      // form save control should be disabled but not the cancel control of the form
      expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(fields.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should initially construct and set missing display type attribute by default to HIDDEN', () => {
    openPage('en', 'bg', 'EO1001');
    fields.toggleHidden();
    let field = fields.getField('missingDisplayType');

    field.showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('displayType').then(attr => {
      // display type attribute should be marked as valid
      expect(attr.isInvalid()).to.eventually.be.false;

      // missing display type from data should be by default set to hidden
      expect(attr.getField().getSelectedValue()).to.eventually.equal('HIDDEN');
    });
  });
});