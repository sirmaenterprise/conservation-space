'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls object type - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should load object type control', () => {
    openPage('en', 'en', 'MX1001');

    let objectType = fields.getField('forObjectType');
    objectType.showAttributes();

    let controlsSection = fields.getModelControlsSection();
    let control = controlsSection.getControl('OBJECT_TYPE_SELECT');

    expect(control.getControlTitleText()).to.eventually.equal('Object type');
  });

  it('should not be able to remove object type control', () => {
    openPage('en', 'en', 'MX1001');

    let objectType = fields.getField('forObjectType');
    objectType.showAttributes();

    let controlsSection = fields.getModelControlsSection();
    expect(controlsSection.isRemoveDisabled('OBJECT_TYPE_SELECT')).to.eventually.be.true;
  });

});