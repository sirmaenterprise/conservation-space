'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management controls section - browse', () => {

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

  it('should be able select fields and see their controls in a control section', () => {
    openPage('en', 'en', 'EO1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      // When I select the title field
      title.showAttributes();

      // Then I expect to see the control section
      let controlsSection = fields.getModelControlsSection();
      expect(controlsSection.sectionTitle).to.eventually.equal('"Title" controls');

      // And I expect to see the allowed control actions list
      expect(controlsSection.getSectionControlLink('RICHTEXT').isDisplayed()).to.eventually.be.true;
      controlsSection.getSectionControlLink('RICHTEXT').getAttribute('title').then(hintText => {
        expect(hintText).to.exist;
        expect(hintText.length > 0).to.be.true;
      });

      // And I expect to see the available default_value_pattern control panel
      let control = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(control.getControlTitleText()).to.eventually.equal('Calculated default value');
      expect(control.hasTooltip()).to.eventually.be.true;
      expect(control.templateField.getValue()).to.eventually.equal('$[emf:type]:DOC_{${seq({+docSequence})}|identifierLabel}_$[emf:createdOn]/$[emf:createdBy.lastName], release/$[chd:releaseVersion]');
    });
  });

  it('should see all possible controls selected for model', () => {
    // Given I've opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // When I select the description field
    let description = fields.getField('description');
    description.showAttributes();

    // Then I expect to see the control section
    let controlsSection = fields.getModelControlsSection();

    // And I expect to see richtext control
    let richtextControl = controlsSection.getControl('RICHTEXT');
    expect(richtextControl.getControlTitleText()).to.eventually.equal('Rich text');

    // And I expect to see default_value_pattern control
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');
    expect(defaultValueControl.hasTooltip()).to.eventually.be.true;
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${description}');
  });

  it('should not see controls section when property is selected', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    let title = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    title.showAttributes();

    expect(fields.isModelControlsSectionVisible()).to.eventually.be.false;
  });

  it('should see empty controls section when the model has no controls', () => {
    openPage('en', 'en', 'EO1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let inherited = items[0];
      // When I select the inherited field
      inherited.showAttributes();

      // Then I expect to see the control section
      let controlsSection = fields.getModelControlsSection();

      // And I expect the section to be empty
      expect(controlsSection.hasVisibleControls()).to.eventually.be.false;

      // And I expect that I could add more controls
      expect(controlsSection.canAddControls()).to.eventually.be.true;
    });
  });

  it('should see controls which are inherited from parent field when the field is inherited', () => {
    // Given I've opened the entity definition model
    openPage('en', 'en', 'MX1001');

    // When I select the description field
    let description = fields.getField('description');
    description.showAttributes();

    // Then I expect to see the control section
    let controlsSection = fields.getModelControlsSection();

    // And I expect to see richtext control
    let richtextControl = controlsSection.getControl('RICHTEXT');
    expect(richtextControl.getControlTitleText()).to.eventually.equal('Rich text');

    // And I expect to see default_value_pattern control
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');
    expect(defaultValueControl.hasTooltip()).to.eventually.be.true;
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('${description}');
  });

  it('should see controls which are inherited from parent field when the field is not inherited', () => {
    // Given I've opened the entity definition model
    openPage('en', 'en', 'EO1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // When I select the title field
      let title = items[0];
      title.showAttributes();

      // Then I expect to see a default value control
      let controlsSection = fields.getModelControlsSection();
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');
      expect(defaultValueControl.hasTooltip()).to.eventually.be.true;
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('$[emf:type]:DOC_{${seq({+docSequence})}|identifierLabel}_$[emf:createdOn]/$[emf:createdBy.lastName], release/$[chd:releaseVersion]');

      // When I open the Media model
      tree.search('Media');
      tree.getNode('Media').openObject();

      return fields.getRegion('generalDetails').getFields();
    }).then(items => {
      // When I select the title field
      let title = items[0];
      title.showAttributes();

      // Then I expect to see the control section
      let controlsSection = fields.getModelControlsSection();

      // And I expect to see default_value_pattern control inherited from the parent entity definition
      let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
      expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');
      expect(defaultValueControl.templateField.getValue()).to.eventually.equal('$[emf:type]:DOC_{${seq({+docSequence})}|identifierLabel}_$[emf:createdOn]/$[emf:createdBy.lastName], release/$[chd:releaseVersion]');
    });
  });

  it('should have one available control for object property', () => {
    // Given I've opened definition model
    openPage('en', 'en', 'MX1001');

    // When I select creator field
    fields.getRegion('generalDetails').getFields().then(items => {
      let creator = items[12];
      creator.showAttributes();

      let controlsSection = fields.getModelControlsSection();

      // Then I expect to see one available control
      expect(controlsSection.getControlsCount()).to.eventually.equal(1);
    });
  });

  it('should properly override and inherit from parent', () => {
    // Given I've opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // When I change the default value control in the description field
    let entityDescription = fields.getField('description');
    entityDescription.showAttributes();
    let entityControlsSection = fields.getModelControlsSection();
    let entityDescriptionControl = entityControlsSection.getControl('DEFAULT_VALUE_PATTERN');
    entityDescriptionControl.templateField.setValue('entity description control changed');

    // Then I expect the default value control for description field in media definition to inherit the new value properly
    tree.search('Media');
    tree.getNode('Media').openObject();
    let mediaDescription = fields.getField('description');
    mediaDescription.showAttributes();
    let mediaControlsSection = fields.getModelControlsSection();
    let mediaDescriptionControl = mediaControlsSection.getControl('DEFAULT_VALUE_PATTERN');
    expect(mediaDescriptionControl.templateField.getValue()).to.eventually.equal('entity description control changed');

    // When I change the default value control for description field in media definition
    mediaDescriptionControl.templateField.setValue('media description control changed');

    // Then I expect control to become local for media definition and changes in parent to not be inherited
    tree.search('entity');
    tree.getNode('entity').openObject();
    entityDescription = fields.getField('description');
    entityDescription.showAttributes();
    entityControlsSection = fields.getModelControlsSection();
    entityDescriptionControl = entityControlsSection.getControl('DEFAULT_VALUE_PATTERN');
    entityDescriptionControl.templateField.setValue('entity description control second change');

    tree.search('Media');
    tree.getNode('Media').openObject();
    mediaDescription = fields.getField('description');
    mediaDescription.showAttributes();
    mediaControlsSection = fields.getModelControlsSection();
    mediaDescriptionControl = mediaControlsSection.getControl('DEFAULT_VALUE_PATTERN');
    expect(mediaDescriptionControl.templateField.getValue()).to.eventually.equal('media description control changed');

    // When I select cancel for media definition
    fields.getModelControls().getModelCancel().click();

    // Then I expect latest changes in parent to be inherited in media definition
    expect(mediaDescriptionControl.templateField.getValue()).to.eventually.equal('entity description control second change');
  });

  it('should remove control when field type is changed', () => {
    // Given I've opened the entity definition model
    openPage('en', 'en', 'EO1001');

    // When I select the description field
    let notes = fields.getField('notes');
    notes.showAttributes();

    // Then I expect to see the control section
    let controlsSection = fields.getModelControlsSection();

    // And I expect to see richtext control
    expect(controlsSection.getSectionControlLink('RICHTEXT').isDisplayed()).to.eventually.be.true;
    let defaultValueControl = controlsSection.getControl('DEFAULT_VALUE_PATTERN');
    expect(defaultValueControl.getControlTitleText()).to.eventually.equal('Calculated default value');
    expect(defaultValueControl.templateField.getValue()).to.eventually.equal('Additional notes by ${user.name}');

    let details = fields.getModelDetails();
    // When I change type option
    details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
      attr.getField().selectFromMenu(null, 1, true);
      // Then I expect available controls to change corresponding to type option
      expect(controlsSection.getSectionControlLink('RICHTEXT').isDisplayed()).to.eventually.be.true;
      expect(controlsSection.getSectionControlLink('DEFAULT_VALUE_PATTERN').isDisplayed()).to.eventually.be.true;

      attr.getField().selectFromMenu(null, 4, true);
      expect(controlsSection.getSectionControlLink('DEFAULT_VALUE_PATTERN').isDisplayed()).to.eventually.be.true;
      expect(controlsSection.getSectionControlLink('RELATED_FIELDS').isDisplayed()).to.eventually.be.true;
    });
  });

});