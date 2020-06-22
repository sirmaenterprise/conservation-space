'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls - remove control', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be able to remove newly added control', () => {
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
      expect(state.isDirty(), 'State field should be dirty!').to.eventually.be.true;
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // When I remove control
      controlsSection.removeControl('DEFAULT_VALUE_PATTERN');
      // Then I expect available controls count to increase
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(state.isDirty(), 'State field should be dirty!').to.eventually.be.false;
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.false;
    });
  });

  it('should be able to remove existing control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      // When I select the country field
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);

      // When I remove control
      controlsSection.removeControl('RELATED_FIELDS');
      // Then I expect available controls count to increase
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;
      expect(controlsSection.isControlPresent('RELATED_FIELDS')).to.eventually.be.false;
    });
  });

  it('should be able to cancel remove control action', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      // When I select the type field
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;

      // When I remove control
      controlsSection.removeControl('RELATED_FIELDS');
      // Then I expect available controls count to increase
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;
      expect(controlsSection.isControlPresent('RELATED_FIELDS')).to.eventually.be.false;
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.true;

      // When I click cancel button
      fields.getModelControls().getModelCancel().click();
      // Then I expect control to be restored
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.false;
      expect(fields.getModelControls().getModelCancel().isEnabled()).to.eventually.be.false;
    });
  });

  it('should enable Save button when control is removed', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      // When I select the country field
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;

      // When I remove control
      controlsSection.removeControl('RELATED_FIELDS');
      // Then I expect available controls count to increase
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;
      expect(controlsSection.isControlPresent('RELATED_FIELDS')).to.eventually.be.false;
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.true;

      // When I click cancel button
      fields.getModelControls().getModelSave().click();
      // Then I expect control to be saved
      expect(controlsSection.getControlsCount()).to.eventually.equal(2);
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.false;
      expect(fields.getModelControls().getModelSave().isEnabled()).to.eventually.be.false;
    });
  });

  it('should not be able to remove the only one inherited control', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      // When I select the title field
      title.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.isRemoveDisabled('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;

      // When I add control
      controlsSection.addControl('RICHTEXT');

      // Then I expect remove button to be visible
      expect(controlsSection.isRemoveDisabled('DEFAULT_VALUE_PATTERN')).to.eventually.be.false;
    });
  });
});