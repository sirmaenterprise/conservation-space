'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls - add control', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be able to add DEFAULT_VALUE_PATTERN control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      // When I select the state field
      state.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('');

      // Then I expect the field to become dirty
      expect(state.isDirty(), 'State field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available default_value_pattern control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
    });
  });

  it('should be able to add RICHTEXT control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      // When I select the title field
      title.showAttributes();

      // When I select add RICHTEXT link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      controlsSection.addControl('RICHTEXT');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(0);
      // Then I expect the field to become dirty
      expect(title.isDirty(), 'Title field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available richtext control panel
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.true;
    });
  });

  it('should be able to add RELATED_FIELDS control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      // When I select the state field
      state.showAttributes();

      // When I select add RELATED_FIELDS link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('RELATED_FIELDS');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      let relatedFieldsControl = controlsSection.getControl('RELATED_FIELDS');

      // Then I expect available control params to be empty
      expect(relatedFieldsControl.rerenderField.getSelectedValue()).to.eventually.equal(null);
      expect(relatedFieldsControl.getFilterSourceField(true).getSelectedValue()).to.eventually.deep.eq([]);
      expect(relatedFieldsControl.inclusiveField.isSelected()).to.eventually.be.true;

      // Then I expect the field to become dirty
      expect(state.isDirty(), 'State field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available related_fields control panel
      expect(controlsSection.isControlPresent('RELATED_FIELDS')).to.eventually.be.true;
    });
  });

  it('should be able to add BYTE_FORMAT control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let numeric = items[8];
      // When I select the numeric field
      numeric.showAttributes();

      // When I select add BYTE_FORMAT link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('BYTE_FORMAT');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      // Then I expect the field to become dirty
      expect(numeric.isDirty(), 'Numeric field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available byte_format control panel
      expect(controlsSection.isControlPresent('BYTE_FORMAT')).to.eventually.be.true;
    });
  });

  it('should be able to add PICKER control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    // When I select the resource field
    let resource = fields.getField('resource');
    resource.showAttributes();

    // When I select add PICKER link
    let controlsSection = fields.getModelControlsSection();
    expect(controlsSection.getControlsCount()).to.eventually.equal(2);
    controlsSection.addControl('PICKER');

    // Then I expect available controls count to decrease
    expect(controlsSection.getControlsCount()).to.eventually.equal(1);
    let pickerControl = controlsSection.getControl('PICKER');

    // Then I expect available control params to be empty
    expect(pickerControl.rangeField.getValue()).to.eventually.equal('');
    expect(pickerControl.restrictionsField.getValue()).to.eventually.equal('');

    // Then I expect the field to become dirty
    expect(resource.isDirty(), 'Resource fieldl should be dirty!').to.eventually.be.true;

    // Then I expect to see the available picker control panel
    expect(controlsSection.isControlPresent('PICKER')).to.eventually.be.true;
  });

  it('should be able to add DEFAULT_VALUE_PATTERN control in inherited field', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      // When I select the inherited field
      inherited.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('');

      // Then I expect the field to become dirty
      expect(inherited.isDirty(), 'Inherited field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available default_value_pattern control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
    });
  });

  it('should be able to add DEFAULT_VALUE_PATTERN and RICHTEXT in field', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      // When I select the inherited field
      inherited.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');
      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      // When I select add RICHTEXT link
      controlsSection.addControl('RICHTEXT');
      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(0);

      // Then I expect to see the available default_value_pattern and richtext control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.true;
    });
  });

  it('should enable Save button when new control is added', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let level = items[3];
      // When I select the level field
      level.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.true;
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('');

      // Then I expect the field to become dirty
      expect(level.isDirty(), 'Level field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available default_value_pattern control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // When I select Save button
      fields.getModelControls().getModelSave().click();
      // Then I expect control to be save
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;
      expect(level.isDirty()).to.eventually.be.false;
    });
  });

  it('should remove added control when Cancel button is clicked', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let level = items[3];
      // When I select the title field
      level.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.true;
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('');

      // Then I expect the field to become dirty
      expect(level.isDirty(), 'Level field should be dirty!').to.eventually.be.true;

      // Then I expect to see the available default_value_pattern control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // When I select add Cancel button
      fields.getModelControls().getModelCancel().click();
      // Then I expect initial state to be reseted
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;
      expect(level.isDirty()).to.eventually.be.false;
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
    });
  });

  it('should override inherited field when new control is added', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      // When I select the inherited field
      inherited.showAttributes();

      // When I select add DEFAULT_VALUE_PATTERN link
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(inherited.isInherited()).to.eventually.be.true;
      controlsSection.addControl('DEFAULT_VALUE_PATTERN');

      // Then I expect available controls count to decrease
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(inherited.isInherited()).to.eventually.be.false;

      // Then I expect to see the available default_value_pattern and richtext control panel
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // When I select add Cancel button
      fields.getModelControls().getModelCancel().click();
      // then I expect changes to be reverted
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(inherited.isInherited()).to.eventually.be.true;
    });
  });
});