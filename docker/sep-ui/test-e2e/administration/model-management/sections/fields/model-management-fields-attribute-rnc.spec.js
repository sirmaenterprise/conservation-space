'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field rnc attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be visible and editable', () => {
    openPage('en', 'bg', 'MX1001');

    // Observe and edit a non empty rnc attribute
    let identifier = fields.getField('identifier');
    identifier.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('rnc').then(attr => {
      let sourcearea = attr.getField();

      expect(sourcearea.getValue()).to.eventually.equal('${eval(${today.format(dd_MM_yyyy)}_${seq({+projectSequence})})}');
      expect(attr.isDirty()).to.eventually.be.false;

      sourcearea.setValue('^[a-zA-Z0-9]$');

      expect(sourcearea.getValue()).to.eventually.equal('^[a-zA-Z0-9]$');
      expect(attr.isDirty()).to.eventually.be.true;
    });

    // Observe and edit an empty rnc attribute
    let description = fields.getField('description');
    description.showAttributes();
    details.getBehaviourAttributesPanel().getAttribute('rnc').then(attr => {
      let sourcearea = attr.getField();

      expect(sourcearea.getValue()).to.eventually.equal('');
      expect(attr.isDirty()).to.eventually.be.false;

      sourcearea.setValue('^[a-zA-Z0-9]$');

      expect(sourcearea.getValue()).to.eventually.equal('^[a-zA-Z0-9]$');
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should reset to the initial value on edit cancel', () => {
    openPage('en', 'bg', 'MX1001');

    let identifier = fields.getField('identifier');
    identifier.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('rnc').then(attr => {
      let sourcearea = attr.getField();

      sourcearea.setValue('^[a-zA-Z0-9]$');
      fields.getModelControls().getModelCancel().click();

      expect(attr.isDirty()).to.eventually.be.false;
      expect(sourcearea.getValue()).to.eventually.equal('${eval(${today.format(dd_MM_yyyy)}_${seq({+projectSequence})})}');
    });
  });

  it('should save value on save', () => {
    openPage('en', 'bg', 'MX1001');

    let identifier = fields.getField('identifier');
    identifier.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('rnc').then(attr => {
      let sourcearea = attr.getField();

      sourcearea.setValue('^[a-zA-Z0-9]$');
      fields.getModelControls().getModelSave().click();

      expect(attr.isDirty()).to.eventually.be.false;
      expect(sourcearea.getValue()).to.eventually.equal('^[a-zA-Z0-9]$');
    });
  });
});