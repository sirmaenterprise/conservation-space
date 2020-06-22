'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management controls picker - manage', () => {

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
  it.skip('should be able to add picker control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the resource field
    let resource = fields.getField('resource');
    resource.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I select add picker control action
    let pickerControl = controlsSection.addControl('PICKER');

    // Then I expect to see the new control in the controls section
    expect(pickerControl.getControlTitleText()).to.eventually.equal('Object picker');

    // And I expect the resource field to become dirty
    expect(resource.isDirty()).to.eventually.be.true;

    // And I expect to be able to save or cancel
    ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

    // When I edit range field in control
    pickerControl.rangeField.setValue(null, 'emf:User,emf:Group');

    // And I edit restrictions field in control
    pickerControl.restrictionsField.setValue('{"condition":"AND","rules":[]}');

    // And I save model
    ModelTestUtils.saveSection(fields);

    // Then I expect the model to be saved
    expect(resource.isDirty()).to.eventually.be.false;

    // And I expect the control to be present
    expect(controlsSection.isControlPresent('PICKER')).to.eventually.be.true;

    // And I expect the range field to have the new value
    expect(pickerControl.rangeField.getValue()).to.eventually.equal('emf:User,emf:Group');

    // And I expect the restrictions field to have the new value
    expect(pickerControl.restrictionsField.getValue()).to.eventually.equal('{"condition":"AND","rules":[]}');

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  // Skipped until add/remove functionality is ready
  it.skip('should be able to remove picker control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // And I have selected the resource field which has a picker control
      let creator = items[12];
      creator.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I remove the control
      controlsSection.removeControl('PICKER');

      // Then I expect control to be missing in the controls section
      expect(controlsSection.isControlPresent('PICKER')).to.eventually.be.false;

      // And I expect the creator field to become dirty
      expect(creator.isDirty()).to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save model
      ModelTestUtils.saveSection(fields);

      // Then I expect control to be missing in the controls section
      expect(controlsSection.isControlPresent('PICKER')).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should be able to edit existing picker control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // And I have selected the creator field which has a picker control
      let creator = items[12];
      creator.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I edit the range field in the control
      let pickerControl = controlsSection.getControl('PICKER');
      pickerControl.rangeField.setValue(null, 'emf:Project,emf:Case');

      // Then I expect the control to become dirty
      expect(pickerControl.isDirty(), 'PICKER control should be dirty!').to.eventually.be.true;

      // And I expect the creator field to become dirty
      expect(creator.isDirty()).to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save the model
      ModelTestUtils.saveSection(fields);

      // Then I expect the creator field to become pristine
      expect(creator.isDirty()).to.eventually.be.false;

      // Then I expect the control to become pristine
      expect(pickerControl.isDirty(), 'PICKER control should be pristine!').to.eventually.be.false;

      // And I expect the template property to have the new value
      expect(pickerControl.rangeField.getValue()).to.eventually.equal('emf:Project,emf:Case');

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should be able to cancel changes made in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // And I have selected the creator field which has a picker control
      let creator = items[12];
      creator.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // And I have edited the template property in the control
      let pickerControl = controlsSection.getControl('PICKER');
      pickerControl.rangeField.setValue(null, 'emf:Project,emf:Case');

      // When I select cancel
      fields.getModelControls().getModelCancel().click();

      // Then I expect the changes in the control to be reverted
      expect(pickerControl.rangeField.getValue()).to.eventually.equal('emf:Project,emf:Case,emf:Document,emf:Audio,emf:Video,emf:Image,emf:Task,emf:User,emf:Group,chd:CulturalObject,chd:Survey,chd:Sample');

      // And I expect the creator field to become pristine
      expect(creator.isDirty()).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  // Skipped until add/remove functionality is ready
  it.skip('should be able to cancel adding of control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the resource field
    let resource = fields.getField('resource');
    resource.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // And I have added a picker control
    controlsSection.addControl('PICKER');

    // When I select cancel
    fields.getModelControls().getModelCancel().click();

    // Then I expect the new control to be missing in the section
    expect(controlsSection.isControlPresent('PICKER')).to.eventually.be.false;

    // And I expect the inherited field to become pristine
    expect(resource.isDirty()).to.eventually.be.false;

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it('should display tooltips when hover over control labels', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // And I have selected the resource field which has a picker control
      let creator = items[12];
      creator.showAttributes();
      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('PICKER');
      expect(control.hasRangeTooltip()).to.eventually.be.true;
      expect(control.hasRestrictionsTooltip()).to.eventually.be.true;
    });
  });
});