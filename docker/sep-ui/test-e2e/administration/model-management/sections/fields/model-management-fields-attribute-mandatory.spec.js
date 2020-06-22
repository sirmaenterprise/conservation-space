'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field mandatory attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should show error message if field with displayType not EDITABLE is set as mandatory', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[3].showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.false;
        expect(attr.isInvalid()).to.eventually.be.false;
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      });
    });
  });

  it('should show error message if checkbox field is set as mandatory', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[5].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.false;
        expect(attr.isInvalid()).to.eventually.be.false;
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      });
    });
  });

  it('should show/hide mandatory-mark of a field when mandatory attribute value is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[4].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.false;
        expect(items[4].isMandatory()).to.eventually.be.false;
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(items[4].isMandatory()).to.eventually.be.true;
      });
    });
  });

  it('should reset initial mandatory attribute value when \'Cancel changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[4].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(items[4].isMandatory()).to.eventually.be.true;
        fields.getModelControls().getModelCancel().click();
        expect(attr.getField().isSelected()).to.eventually.be.false;
        expect(items[4].isMandatory()).to.eventually.be.false;
      });
    });
  });

  it('should save mandatory attribute value when \'Save changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[4].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(items[4].isMandatory()).to.eventually.be.true;
        fields.getModelControls().getModelSave().click();
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(items[4].isMandatory()).to.eventually.be.true;
      });
    });
  });
});