'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field preview empty attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be true by default', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('previewEmpty').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
      });
    });
  });

  it('should reset to the initial value on edit cancel', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('previewEmpty').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.false;

        fields.getModelControls().getModelCancel().click();

        expect(attr.getField().isSelected()).to.eventually.be.true;
      });
    });
  });

  it('should save value on save', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();

      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('previewEmpty').then(attr => {
        attr.getField().toggleCheckbox();
        expect(attr.getField().isSelected()).to.eventually.be.false;

        fields.getModelControls().getModelSave().click();

        expect(attr.getField().isSelected()).to.eventually.be.false;
      });
    });
  });
});