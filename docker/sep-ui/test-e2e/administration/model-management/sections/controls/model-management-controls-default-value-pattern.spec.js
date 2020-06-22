'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management controls default value pattern - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  // Skipped until add/remove functionality is ready
  it.skip('should be able to add default value pattern control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the title field
    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      inherited.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I select add default value control action
      let defaultValueControl = controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect to see the new control in the controls section
      expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');

      // And I expect the inherited field to become dirty
      expect(inherited.isDirty()).to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I edit template in control
      defaultValueControl.templateField.setValue('${default.value.template}');

      // And I save model
      fields.getModelControls().getModelSave().click();

      // Then I expect the model to be saved
      expect(inherited.isDirty()).to.eventually.be.false;

      // And I expect the control to be present
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // And I expect the template property to have the new value
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${default.value.template}');

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  // Skipped until add/remove functionality is ready
  it.skip('should be able to remove default value pattern control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the description field which has a default value pattern control
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I remove the control
    controlsSection.removeControl('DEFAULT_VALUE_PATTERN');

    // Then I expect control to be missing in the controls section
    expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.false;

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

  it('should be able to edit existing default value pattern control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the description field which has a default value pattern control
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I edit the template property in the control
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    defaultValueControl.templateField.setValue('${default.value.template}');

    // Then I expect the control to become dirty
    expect(defaultValueControl.isDirty(), 'DEFAULT_VALUE_PATTERN control should be dirty!').to.eventually.be.true;

    // And I expect the description field to become dirty
    // !!! Currently there is no way to test if field is dirty without deselecting it somehow!!!
    // expect(description.isDirty(), 'Description field should be dirty!').to.eventually.be.true;

    // And I expect to be able to save or cancel
    ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

    // When I save the model
    fields.getModelControls().getModelSave().click();

    // Then I expect the description field to become pristine
    expect(description.isDirty()).to.eventually.be.false;

    // And I expect the template property to have the new value
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${default.value.template}');

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it('should be able to cancel changes made in existing control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the description field which has a default value pattern control
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // And I have edited the template property in the control
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    defaultValueControl.templateField.setValue('${default.value.template}');

    // When I select cancel
    fields.getModelControls().getModelCancel().click();

    // Then I expect the changes in the control to be reverted
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${description}');

    // And I expect control to become pristine
    expect(defaultValueControl.isDirty()).to.eventually.be.false;

    // And I expect the description field to become pristine
    expect(description.isDirty()).to.eventually.be.false;

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it('should show control tooltip of default value pattern', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the description field which has a default value pattern control
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // And I have edited the template property in the control
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    //expect(defaultValueControl.getControlLabel()).to.eventually.eq('');
    expect(defaultValueControl.hasControlTooltip()).to.eventually.be.true;
  });

  // Skipped until add/remove functionality is ready
  it.skip('should be able to cancel adding of control', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // And I have selected the inherited field
    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      inherited.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // And I have added a default value control
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // When I select cancel
      fields.getModelControls().getModelCancel().click();

      // Then I expect the new control to be missing in the section
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.false;

      // And I expect the inherited field to become pristine
      expect(inherited.isDirty()).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });
});