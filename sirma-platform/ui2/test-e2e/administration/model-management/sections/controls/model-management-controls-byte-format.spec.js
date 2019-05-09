'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls byte format - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should load byte format control', () => {
    openPage('en', 'en', 'MX1001');

    let bytes = fields.getField('bytes');
    bytes.showAttributes();

    let controlsSection = fields.getModelControlsSection();
    let control = controlsSection.getControl('BYTE_FORMAT');

    expect(control.getControlTitleText()).to.eventually.equal('Byte format');
  });

});