'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management controls richtext - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it.skip('should be able to add richtext control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the title field
    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      title.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I select add richtext control action
      let richtextControl = controlsSection.addControl('RICHTEXT');

      // Then I expect to see the new control in the controls section
      expect(richtextControl.getControlTitleText()).to.eventually.equal('Rich text');

      // And I expect the title field to become dirty
      expect(title.isDirty()).to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save model
      fields.getModelControls().getModelSave().click();

      // Then I expect the model to be saved
      expect(title.isDirty()).to.eventually.be.false;

      // And I expect the control to be present
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.true;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it.skip('should be able to remove richtext control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the description field which has a richtext control
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I remove the control
    controlsSection.removeControl('RICHTEXT');

    // Then I expect control to be missing in the controls section
    expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.false;

    // And I expect the description field to become dirty
    expect(description.isDirty()).to.eventually.be.true;

    // And I expect to be able to save or cancel
    ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

    // When I save model
    fields.getModelControls().getModelSave().click();

    // Then I expect control to be missing in the controls section
    expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.false;

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it.skip('should be able to cancel adding of richtext control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the title field
    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      inherited.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // And I have added a default value control
      controlsSection.addControl('RICHTEXT');

      // When I select cancel
      fields.getModelControls().getModelCancel().click();

      // Then I expect the new control to be missing in the section
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.false;

      // And I expect the inherited field to become pristine
      expect(inherited.isDirty()).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });
});