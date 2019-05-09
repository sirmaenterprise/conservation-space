'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management controls concept picker - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should load concept picker control params correct', () => {
    openPage('en', 'en', 'MX1001');

    let places = fields.getField('places');
    places.showAttributes();

    let controlsSection = fields.getModelControlsSection();
    let control = controlsSection.getControl('CONCEPT_PICKER');

    expect(control.schemeField.getValue()).to.eventually.equal('emf:CONCEPT-SCHEME-Places');
    expect(control.broaderField.getValue()).to.eventually.equal('emf:CONCEPT-Italy');
    expect(control.hasSchemeTooltip()).to.eventually.be.true;
    expect(control.hasBroaderTooltip()).to.eventually.be.true;
  });

  it.skip('should be able to remove concept picker control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the places field which has a concept picker control
    let places = fields.getField('places');
    places.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I remove the control
    controlsSection.removeControl('CONCEPT_PICKER');

    // Then I expect control to be missing in the controls section
    expect(controlsSection.isControlPresent('CONCEPT_PICKER')).to.eventually.be.false;

    // And I expect the places field to become dirty
    expect(places.isDirty()).to.eventually.be.true;

    // And I expect to be able to save or cancel
    ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

    // When I save model
    ModelTestUtils.saveSection(fields);

    // Then I expect control to be missing in the controls section
    expect(controlsSection.isControlPresent('CONCEPT_PICKER')).to.eventually.be.false;

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it('should be able to edit existing concept picker control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the places field which has a concept picker control
    let places = fields.getField('places');
    places.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // When I edit the scheme field in the control
    let conceptPickerControl = controlsSection.getControl('CONCEPT_PICKER');
    conceptPickerControl.schemeField.setValue(null, 'edited');

    // Then I expect the control to become dirty
    expect(conceptPickerControl.isDirty(), 'CONCEPT_PICKER control should be dirty!').to.eventually.be.true;

    // And I expect the places field to become dirty
    expect(places.isDirty()).to.eventually.be.true;

    // And I expect to be able to save or cancel
    ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

    // When I save the model
    ModelTestUtils.saveSection(fields);

    // Then I expect the places field to become pristine
    expect(places.isDirty()).to.eventually.be.false;

    // And I expect the template property to have the new value
    expect(conceptPickerControl.schemeField.getValue()).to.eventually.equal('edited');

    // And I can't save or cancel anymore
    // ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  it('should be able to cancel changes made in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the places field which has a picker control
    let places = fields.getField('places');
    places.showAttributes();
    let controlsSection = fields.getModelControlsSection();

    // And I have edited the scheme field in the control
    let conceptPickerControl = controlsSection.getControl('CONCEPT_PICKER');
    conceptPickerControl.schemeField.setValue(null, 'edited');

    // When I select cancel
    fields.getModelControls().getModelCancel().click();

    // Then I expect the changes in the control to be reverted
    expect(conceptPickerControl.schemeField.getValue()).to.eventually.equal('emf:CONCEPT-SCHEME-Places');

    // And I expect the control to become pristine
    expect(conceptPickerControl.isDirty(), 'DEFAULT_VALUE_PATTERN control should be pristine!').to.eventually.be.false;

    // And I expect the places field to become pristine
    expect(places.isDirty()).to.eventually.be.false;

    // And I can't save or cancel anymore
    ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
  });

  // TODO: Test: should be able to add concept picker control
  // TODO: Test: should be able to cancel adding of control
});