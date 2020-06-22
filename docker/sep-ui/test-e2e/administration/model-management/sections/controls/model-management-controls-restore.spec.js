'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls - restore inherited', () => {

  let tree;
  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
    tree = sandbox.getModelTree();
  }

  it('should not be able to restore inherited controls when controls are not changed', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    // When I select the description field
    let description = fields.getField('description');
    description.showAttributes();

    // Then I expect the restore controls button to be missing
    let controlsSection = fields.getModelControlsSection();
    expect(description.isInherited()).to.eventually.be.true;
    expect(controlsSection.canRestoreControls()).to.eventually.be.false;
  });

  it('should not be able to restore changed controls in root model', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // When I change the default value control of the description field
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    defaultValueControl.templateField.setValue('edit template');

    // Then I expect that restore inherited button to not be present as the model has nowhere to inherit from
    expect(controlsSection.canRestoreControls()).to.eventually.be.false;
  });

  it('should be able to restore controls from parent model when control is changed', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    // When I edit the default value pattern control of the description field
    let description = fields.getField('description');
    description.showAttributes();
    let controlsSection = fields.getModelControlsSection();
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    defaultValueControl.templateField.setValue('edit template');

    // Then I expect the restore controls button to be present
    expect(description.isInherited(), 'Description field should not be inherited').to.eventually.be.false;
    expect(controlsSection.canRestoreControls(), 'Description controls can be restored').to.eventually.be.true;

    // When I select the restore button
    controlsSection.restoreControls().ok();

    // Then I expect the control value to be inherit from the parent model
    expect(description.isInherited(), 'Description field should be inherited').to.eventually.be.true;
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${description}');
  });

  it('should see restore inherited button for overridden controls', () => {
    // Given I have opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // When I select notes field
    let entityNotes = fields.getField('notes');
    entityNotes.showAttributes();
    let controlsSection = fields.getModelControlsSection();
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');

    // Then I expect to see default value control
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('Additional notes by ${user.name}');

    // When I open the notes field from Media definition
    tree.search('Media');
    tree.getNode('Media').openObject();
    fields.getRegion('specificDetails').getFields().then(items => {
      let mediaNotes = items[1];
      mediaNotes.showAttributes();

      // Then I expect to see default value control for Media definition
      let controlsSection = fields.getModelControlsSection();
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('Additional notes by ${user.lastName}');

      // And I expect to see restore inherited button for controls section
      expect(controlsSection.canRestoreControls(), 'Notes controls can be restored').to.eventually.be.true;
    });
  });

  it('should see restore inherited button after after changed control is saved', () => {
    // Given I have opened the Media definition model
    openPage('en', 'en', 'MX1001');

    // When I edit default value control for title field
    fields.getRegion('generalDetails').getFields().then(items => {
      let mediaTitle = items[0];
      mediaTitle.showAttributes();
      let controlsSection = fields.getModelControlsSection();
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');

      expect(controlsSection.canRestoreControls(), 'Title controls is inherited and can not be restored').to.eventually.be.false;
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('$[emf:type]:DOC_{${seq({+docSequence})}|identifierLabel}_$[emf:createdOn]/$[emf:createdBy.lastName], release/$[chd:releaseVersion]');

      defaultValueControl.templateField.setValue('edit template');

      // And I save model
      fields.getModelControls().getModelSave().click();

      // Then I expect restore inherited button to be visible
      expect(controlsSection.canRestoreControls(), 'Title controls are overridden and can be restored').to.eventually.be.true;
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('edit template');

      // When I select the restore button
      controlsSection.restoreControls().ok();

      expect(controlsSection.canRestoreControls(), 'Title controls is inherited again and can not be restored').to.eventually.be.false;
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('$[emf:type]:DOC_{${seq({+docSequence})}|identifierLabel}_$[emf:createdOn]/$[emf:createdBy.lastName], release/$[chd:releaseVersion]');
    });
  });

  it('should not see restore inherited button for field with controls which has no parent reference', () => {
    // Given I have opened the Media definition model
    openPage('en', 'en', 'MX1001');

    // When I select the type field
    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();

      // Then I should not see the restore inherited button because the field is defined for the first time in current
      // model and there is nowhere to inherit from.
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.canRestoreControls(), 'Type field controls cannot be restored!').to.eventually.be.false;
    });
  });

  it('should restore controls from parent - local control should be removed during restoration', () => {
    // Given I have opened the Media definition model
    openPage('en', 'en', 'MX1001');

    // When I select notes field
    fields.getRegion('specificDetails').getFields().then(items => {
      let mediaNotes = items[1];
      mediaNotes.showAttributes();

      // Then I expect to see 2 controls: richtext and default value - parent definition has the default value control only
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.true;

      // When I select the restore button for the notes field
      controlsSection.restoreControls().ok();

      // Then I expect to see only default value control which is inherited from the parent
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.false;
    });
  });

  it('should restore all available control', () => {
    // Given I have opened definition model
    openPage('en', 'en', 'MX1001');

    fields.getRegion('specificDetails').getFields().then(items => {
      let mediaNotes = items[1];
      mediaNotes.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      // And I expect to see default value and  richtext controls
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.true;

      // And I expect to see restore inherited button for controls section
      expect(controlsSection.canRestoreControls(), 'Notes controls can be restored').to.eventually.be.true;

      // When I select the restore button
      controlsSection.restoreControls().ok();

      // Then I expect to see default value from parent only
      expect(controlsSection.isControlPresent('DEFAULT_VALUE_PATTERN')).to.eventually.be.true;
      expect(controlsSection.isControlPresent('RICHTEXT')).to.eventually.be.false;
    });
  });

});