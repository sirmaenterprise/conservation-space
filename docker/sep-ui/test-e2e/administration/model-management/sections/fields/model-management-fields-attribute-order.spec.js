'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field order attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should show error message if order value is not in allowed range', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
        let field = attr.getField();
        field.setValue(null, '5');
        expect(attr.isInvalid()).to.eventually.be.false;

        field.setValue(null, '0');
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;

        field.setValue(null, '100');
        expect(attr.isInvalid()).to.eventually.be.false;

        field.setValue(null, '10001');
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
      });
    });
  });

  it('should move field up and down depending on it\'s order', () => {
    openPage('en', 'bg', 'MX1001');

    // Given I have open generalDetails region.
    // And Type field is at position 2.
    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();
      let details = fields.getModelDetails();

      expect(type.getLabel()).to.eventually.equal('Type');
      details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
        // When I change Type field order to 1
        attr.getField().setValue(null, 1);
        expect(attr.isInvalid()).to.eventually.be.false;
      });
    });

    fields.getRegion('generalDetails').getFields().then(items => {
      let details = fields.getModelDetails();
      // Then the field is moved to position 1
      expect(items[1].getLabel()).to.eventually.equal('Type');
      details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
        // When I remove type field order
        attr.getField().setValue(null, null);
        expect(attr.isInvalid()).to.eventually.be.false;
      });
    });

    fields.getRegion('generalDetails').getFields().then(items => {
      // Then the field is returned to it's initial position - 2
      expect(items[2].getLabel()).to.eventually.equal('Type');
    });
  });

  it('should move region up and down depending on it\'s order', () => {
    openPage('en', 'bg', 'MX1001');

    // Given I have open model managament page.
    // And "Inherited details" region is at position 2.
    fields.getRegions().then(items => {
      expect(items[2].getLabel()).to.eventually.equal('Inherited details');
    });
    fields.getRegion('inheritedDetails').showAttributes();
    let details = fields.getModelDetails();

    details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
      // When I change "Inherited details" region order to 1
      attr.getField().setValue(null, '1');
      expect(attr.isInvalid()).to.eventually.be.false;
    });
    fields.getRegions().then(items => {
      // Then the region is moved to position 1
      expect(items[0].getLabel()).to.eventually.equal('Inherited details');
    });
    details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
      // When I change "Inherited details" region order to 12
      attr.getField().setValue(null, '12');
      expect(attr.isInvalid()).to.eventually.be.false;
    });
    fields.getRegions().then(items => {
      // Then "Inherited details" change it's position leaving the region with smaller order at position 1
      expect(items[1].getLabel()).to.eventually.equal('Inherited details');
    });
  });

  it('should reset initial order value when \'Cancel changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[2].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
        expect(attr.getField().getValue()).to.eventually.equal('');
        attr.getField().setValue(null, '5');
        expect(attr.getField().getValue()).to.eventually.equal('5');
        fields.getModelControls().getModelCancel().click();
        expect(attr.getField().getValue()).to.eventually.equal('');
      });
    });
  });

  it('should save order value when \'Save changes\'button is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[2].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
        expect(attr.getField().getValue()).to.eventually.equal('');
        attr.getField().setValue(null, '5');
        expect(attr.getField().getValue()).to.eventually.equal('5');
        fields.getModelControls().getModelSave().click();
        expect(attr.getField().getValue()).to.eventually.equal('5');
      });
    });
  });
});