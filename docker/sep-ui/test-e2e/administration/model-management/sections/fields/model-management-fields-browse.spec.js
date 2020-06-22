'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

let Button = require('../../../../form-builder/form-control').Button;
let InputField = require('../../../../form-builder/form-control').InputField;
let FormControl = require('../../../../form-builder/form-control').FormControl;
let DatetimeField = require('../../../../form-builder/form-control').DatetimeField;
let CheckboxField = require('../../../../form-builder/form-control').CheckboxField;
let MultySelectMenu = require('../../../../form-builder/form-control').MultySelectMenu;
let SingleSelectMenu = require('../../../../form-builder/form-control').SingleSelectMenu;

describe('Models management fields section - browsing', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should have default filter configuration properly configured', () => {
    openPage('en', 'bg', 'MX1001');

    // system and hidden fields are by default disabled
    expect(fields.isSystemFilterEnabled()).to.eventually.be.false;
    expect(fields.isHiddenFilterEnabled()).to.eventually.be.false;

    // inherited fields are shown and keyword filter is empty
    expect(fields.isInheritedFilterEnabled()).to.eventually.be.true;
    expect(fields.isKeywordFilterEmpty()).to.eventually.be.true;
  });

  it('should properly filter fields based on a keyword search', () => {
    openPage('en', 'bg', 'MX1001');
    fields.filterByKeyword('Description');

    expect(fields.isFieldDisplayed('description')).to.eventually.be.true;
    expect(fields.isRegionDisplayed('generalDetails')).to.eventually.be.false;
    expect(fields.isRegionDisplayed('specificDetails')).to.eventually.be.false;
  });

  it('should show a message when no results are found due to filtering', () => {
    openPage('en', 'bg', 'MX1001');
    fields.filterByKeyword('random filter');

    // message that no results are found during the search is displayed
    expect(fields.isNoResultsMessagePresent()).to.eventually.be.true;

    // neither of the fields or regions should be present or visible
    expect(fields.isFieldDisplayed('description')).to.eventually.be.false;
    expect(fields.isRegionDisplayed('generalDetails')).to.eventually.be.false;
    expect(fields.isRegionDisplayed('specificDetails')).to.eventually.be.false;
  });

  it('should properly filter fields based on a display type', () => {
    openPage('en', 'bg', 'MX1001');
    fields.toggleSystem();

    expect(fields.getField('description').isDisplayed()).to.eventually.be.true;
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(16);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('State');
      expect(items[2].getLabel()).to.eventually.eq('System');
      expect(items[3].getLabel()).to.eventually.eq('Type');
      expect(items[4].getLabel()).to.eventually.eq('Level');
      expect(items[5].getLabel()).to.eventually.eq('Country');
      expect(items[6].getLabel()).to.eventually.eq('Checkbox');
      expect(items[7].getLabel()).to.eventually.eq('Date');
      expect(items[8].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[9].getLabel()).to.eventually.eq('Numeric Field');
      expect(items[10].getLabel()).to.eventually.eq('Numeric Fixed Field');
      expect(items[11].getLabel()).to.eventually.eq('Floating Point Field');
      expect(items[12].getLabel()).to.eventually.eq('Floating Point Fixed Field');
      expect(items[13].getLabel()).to.eventually.eq('Creator');
      expect(items[14].getLabel()).to.eventually.eq('Numeric Fixed (Double range)');
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(2);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
      expect(items[1].getLabel()).to.eventually.eq('Notes');
    });
  });

  it('should properly filter fields based on inherited fields', () => {
    openPage('en', 'bg', 'MX1001');
    fields.toggleInherited();

    // inherited field should not be shown when inheritance is toggled
    expect(fields.isFieldDisplayed('description')).to.eventually.be.false;

    // rest of the fields and regions are not inherited and should not be affected
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(15);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('State');
      expect(items[2].getLabel()).to.eventually.eq('Type');
      expect(items[3].getLabel()).to.eventually.eq('Level');
      expect(items[4].getLabel()).to.eventually.eq('Country');
      expect(items[5].getLabel()).to.eventually.eq('Checkbox');
      expect(items[6].getLabel()).to.eventually.eq('Date');
      expect(items[7].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[8].getLabel()).to.eventually.eq('Numeric Field');
      expect(items[9].getLabel()).to.eventually.eq('Numeric Fixed Field');
      expect(items[10].getLabel()).to.eventually.eq('Floating Point Field');
      expect(items[11].getLabel()).to.eventually.eq('Floating Point Fixed Field');
      expect(items[12].getLabel()).to.eventually.eq('Creator');
      expect(items[13].getLabel()).to.eventually.eq('Numeric Fixed (Double range)');
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(2);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
      expect(items[1].getLabel()).to.eventually.eq('Notes');
    });
  });

  it('should properly show all fields when all filters are enabled and no keyword is entered', () => {
    openPage('en', 'bg', 'MX1001');
    // show all the fields
    fields.toggleSystem();
    fields.toggleHidden();

    // base fields are not filtered and all should be visible
    expect(fields.getField('description').getLabel()).to.eventually.eq('Description');
    expect(fields.getField('missingDisplayType').getLabel()).to.eventually.eq('Missing DisplayType');

    // base regions are not filtered and all should be visible
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    // regions are not collapsed since all fields inside are visible due to filtering
    expect(fields.getRegion('generalDetails').isCollapsed()).to.eventually.be.false;
    expect(fields.getRegion('specificDetails').isCollapsed()).to.eventually.be.false;

    // should show system fields from this region as well
    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(16);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('State');
      expect(items[2].getLabel()).to.eventually.eq('System');
      expect(items[3].getLabel()).to.eventually.eq('Type');
      expect(items[4].getLabel()).to.eventually.eq('Level');
      expect(items[5].getLabel()).to.eventually.eq('Country');
      expect(items[6].getLabel()).to.eventually.eq('Checkbox');
      expect(items[7].getLabel()).to.eventually.eq('Date');
      expect(items[8].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[9].getLabel()).to.eventually.eq('Numeric Field');
      expect(items[10].getLabel()).to.eventually.eq('Numeric Fixed Field');
      expect(items[11].getLabel()).to.eventually.eq('Floating Point Field');
      expect(items[12].getLabel()).to.eventually.eq('Floating Point Fixed Field');
      expect(items[13].getLabel()).to.eventually.eq('Creator');
      expect(items[14].getLabel()).to.eventually.eq('Numeric Fixed (Double range)');
    });

    // should show hidden fields from this region as well
    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(3);
      expect(items[0].getLabel()).to.eventually.eq('Hidden');
      expect(items[1].getLabel()).to.eventually.eq('E-mail address');
      expect(items[2].getLabel()).to.eventually.eq('Notes');
    });
  });

  it('should have proper region and fields displayed for selected model', () => {
    openPage('en', 'bg', 'MX1001');

    // inherited fields are not filtered since inherited filter is enabled by default
    expect(fields.getField('description').getLabel()).to.eventually.eq('Description');
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    // system filter is not toggled so system field is filtered
    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(15);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('State');
      expect(items[2].getLabel()).to.eventually.eq('Type');
      expect(items[3].getLabel()).to.eventually.eq('Level');
      expect(items[4].getLabel()).to.eventually.eq('Country');
      expect(items[5].getLabel()).to.eventually.eq('Checkbox');
      expect(items[6].getLabel()).to.eventually.eq('Date');
      expect(items[7].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[8].getLabel()).to.eventually.eq('Numeric Field');
      expect(items[9].getLabel()).to.eventually.eq('Numeric Fixed Field');
      expect(items[10].getLabel()).to.eventually.eq('Floating Point Field');
      expect(items[11].getLabel()).to.eventually.eq('Floating Point Fixed Field');
      expect(items[12].getLabel()).to.eventually.eq('Creator');
      expect(items[13].getLabel()).to.eventually.eq('Numeric Fixed (Double range)');
    });

    // system filter is not toggled so hidden field is filtered
    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(2);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
      expect(items[1].getLabel()).to.eventually.eq('Notes');
    });
  });

  it('should properly order regions', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegions().then(regions => {
      expect(regions[0].getLabel()).to.eventually.eq('Base details');
      expect(regions[1].getLabel()).to.eventually.eq('Specific details');
    });
  });

  it('should properly resolve which are inherited and not inherited fields', () => {
    openPage('en', 'bg', 'MX1001');
    // show all the fields
    fields.toggleSystem();
    fields.toggleHidden();

    expect(fields.getField('description').isInherited()).to.eventually.be.true;
    expect(fields.getField('description').getParent()).to.eventually.eq('(entity)');

    expect(fields.getField('missingDisplayType').isInherited()).to.eventually.be.true;
    expect(fields.getField('missingDisplayType').getParent()).to.eventually.eq('(entity)');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(16);
      items.forEach(item => expect(item.isInherited()).to.eventually.be.false);
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(3);
      expect(items[0].isInherited()).to.eventually.be.false;
      expect(items[1].isInherited()).to.eventually.be.false;
      expect(items[2].isInherited()).to.eventually.be.false;
    });
  });

  it('should properly resolve the type of input fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      expect(title.isEditable()).to.eventually.be.true;

      let input = new InputField(title.getEditControl());
      expect(input.getValue().then(value => value.trim())).to.eventually.eq('Status: ${get[state]}');
    });
  });

  it('should properly resolve fixed length input fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let fixedLengthTitle = items[7];
      expect(fixedLengthTitle.isEditable()).to.eventually.be.true;

      let input = new InputField(fixedLengthTitle.getEditControl());
      expect(input.getValue().then(value => value.trim())).to.eventually.eq('');

      let numericFixed = items[9];
      expect(numericFixed.isEditable()).to.eventually.be.true;

      input = new InputField(numericFixed.getEditControl());
      expect(input.getValue().then(value => value.trim())).to.eventually.eq('');

      let floatingPoinFixed = items[11];
      expect(floatingPoinFixed.isEditable()).to.eventually.be.true;

      input = new InputField(floatingPoinFixed.getEditControl());
      expect(input.getValue().then(value => value.trim())).to.eventually.eq('');
    });
  });

  it('should properly resolve the type of multi select fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      expect(type.isEditable()).to.eventually.be.true;

      let field = new MultySelectMenu(type.getEditControl());
      expect(field.getSelectedValue()).to.eventually.deep.eq([]);
      expect(field.getAvailableSelectChoices()).to.eventually.deep.eq([
        'PR0001', 'PR0002', 'PR0003', 'PR0004', 'PR0005', 'PR0006', 'PR0007'
      ]);
    });
  });

  it('should properly resolve the type of label fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      {
        let state = items[1];
        expect(state.isReadOnly()).to.eventually.be.true;

        let label = new FormControl(state.getViewControl());
        expect(label.getText()).to.eventually.eq('Approved');
      }

      {
        let level = items[3];
        expect(level.isReadOnly()).to.eventually.be.true;

        let label = new FormControl(level.getViewControl());
        expect(label.getText()).to.eventually.eq('');
      }
    });
  });

  it('should properly resolve the type of single select fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      expect(country.isEditable()).to.eventually.be.true;

      let field = new SingleSelectMenu(country.getEditControl());
      expect(field.getSelectedValue()).to.eventually.eq('GBR');
      expect(field.getMenuValues()).to.eventually.deep.eq(['GBR', 'BGN', 'AUS', 'FRA', 'USA', 'CAN']);
    });
  });

  it('should properly resolve the type of checkbox fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let checkbox = items[5];
      expect(checkbox.isEditable()).to.eventually.be.true;

      let field = new CheckboxField(checkbox.getEditControl());
      expect(field.isChecked().then(checked => !!checked)).to.eventually.be.false;
    });
  });

  it('should properly resolve the type of date fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let date = items[6];
      expect(date.isEditable()).to.eventually.be.true;

      let field = new DatetimeField(date.getEditControl());
      field.setDatetime(null, 'August/1/2018 13:35');
      // should take into account the format of the datetime field
      expect(field.getDate()).to.eventually.eq('August/01/2018 13:35');
    });
  });

  it('should properly resolve the type of picker fields', () => {
    openPage('en', 'bg', 'MX1001');

    let resource = fields.getField('resource');
    expect(resource.isEditable()).to.eventually.be.true;

    let button = new Button(resource.getEditControl());
    expect(button.isEnabled()).to.eventually.be.true;
  });

  it('should properly resolve mandatory fields', () => {
    openPage('en', 'bg', 'MX1001');

    expect(fields.getField('description').isMandatory()).to.eventually.be.true;

    fields.getRegion('generalDetails').getFields().then(items => {
      // title, type and state of this region are mandatory
      expect(items[0].isMandatory()).to.eventually.be.true;
      expect(items[1].isMandatory()).to.eventually.be.true;
      expect(items[2].isMandatory()).to.eventually.be.true;

      for (let i = 3; i < items.length; ++i) {
        // the rest of the fields are not mandatory
        expect(items[i].isMandatory()).to.eventually.be.false;
      }
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      // e-mail field from this region is also mandatory
      expect(items[0].isMandatory()).to.eventually.be.true;
    });
  });

  it('should properly resolve data type attribute for semantic properties', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
      expect(attr.getField().isDisabled()).to.eventually.be.true;
      expect(attr.getField().getSelectedLabel()).to.eventually.equal('Data property');
      expect(attr.getField().getSelectedValue()).to.eventually.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DefinitionDataProperty');
    });
  });

  it('should properly resolve domain attribute for semantic properties', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#domain').then(attr => {
      expect(attr.getField().isDisabled()).to.eventually.be.true;
      expect(attr.getField().getSelectedLabel()).to.eventually.equal('Entity');
      expect(attr.getField().getSelectedValue()).to.eventually.equal('http://www.ontotext.com/proton/protontop#Entity');
    });
  });

  it('should properly resolve range attribute for semantic properties', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    // select a semantic property for which the specified model data type is directly provided from the service
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#emailAddress');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#range').then(attr => {
      expect(attr.getField().isDisabled()).to.eventually.be.true;
      expect(attr.getField().getSelectedLabel()).to.eventually.equal('String');
      expect(attr.getField().getSelectedValue()).to.eventually.equal('http://www.w3.org/2001/XMLSchema#string');
    });

    // select a semantic property for which the specified model data type is not provided from the service
    property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#description');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#range').then(attr => {
      expect(attr.getField().isDisabled()).to.eventually.be.true;
      expect(attr.getField().getSelectedLabel()).to.eventually.equal('http://purl.org/dc/terms/MethodOfAccrual');
      expect(attr.getField().getSelectedValue()).to.eventually.equal('http://purl.org/dc/terms/MethodOfAccrual');
    });
  });

  it('should properly resolve hints or tooltips for semantic properties', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    expect(property.getLabel()).to.eventually.eq('Title');
    expect(property.hasTooltip()).to.eventually.be.true;
    expect(property.getTooltip()).to.eventually.equal('Property representing a name or a title');
  });

  it('should properly resolve hints or tooltips when they are present in the field', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      // title field from this region should have a tooltip
      expect(field.getLabel()).to.eventually.eq('Title');
      expect(field.hasTooltip()).to.eventually.be.true;
      expect(field.getTooltip()).to.eventually.equal('Specifies the name or title of the current field');
    });
  });

  it('should properly resolve hints or tooltips when they are not present in the field', () => {
    openPage('en', 'bg', 'MX1001');
    fields.toggleSystem();

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[2];
      // system field from this region should have a tooltip
      expect(field.getLabel()).to.eventually.eq('System');
      expect(field.hasTooltip()).to.eventually.be.true;
      expect(field.getTooltip()).to.eventually.equal('System property');
    });
  });

  // Details

  it('should not have a selected field or region by default', () => {
    openPage('en', 'bg', 'MX1001');
    expect(fields.hasSelectedModel()).to.eventually.be.false;
  });

  it('should allow to select a field and display its general & behavioural attributes', () => {
    openPage('en', 'bg', 'MX1001');

    let descriptionField = fields.getField('description');
    expect(descriptionField.isHighlighted()).to.eventually.be.false;

    descriptionField.showAttributes();
    expect(descriptionField.isHighlighted()).to.eventually.be.true;
    expect(fields.hasSelectedModel()).to.eventually.be.true;

    let descriptionDetails = fields.getModelDetails();
    // See if it is a field
    expect(descriptionDetails.hasGeneralAttributes()).to.eventually.be.true;

    let generalAttributesPanel = descriptionDetails.getGeneralAttributesPanel();
    expect(generalAttributesPanel.getModelTitle()).to.eventually.equals('"Description"');

    let behaviourAttributesPanel = descriptionDetails.getBehaviourAttributesPanel();
    expect(behaviourAttributesPanel.getModelTitle()).to.eventually.equals('"Description"');
  });

  it('should allow to select a region and display its behavioural attributes', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').showAttributes();
    let descriptionDetails = fields.getModelDetails();
    // See if it is a region
    expect(descriptionDetails.hasGeneralAttributes()).to.eventually.be.false;

    let behaviourAttributesPanel = descriptionDetails.getBehaviourAttributesPanel();
    expect(behaviourAttributesPanel.getModelTitle()).to.eventually.equals('"Base details"');
  });

  it('should allow to collapse field attributes panel', () => {
    openPage('en', 'bg', 'MX1001');
    fields.getField('description').showAttributes();
    let descriptionDetails = fields.getModelDetails();

    // general attributes should be collapsed by default for model fields
    let generalAttributesPanel = descriptionDetails.getGeneralAttributesPanel();
    expect(generalAttributesPanel.isCollapsed()).to.eventually.be.true;
    generalAttributesPanel.toggleCollapse();
    expect(generalAttributesPanel.isCollapsed()).to.eventually.be.false;

    // behaviour attributes should not be collapsed by default for model fields
    let behaviourAttributesPanel = descriptionDetails.getBehaviourAttributesPanel();
    expect(behaviourAttributesPanel.isCollapsed()).to.eventually.be.false;
    behaviourAttributesPanel.toggleCollapse();
    expect(behaviourAttributesPanel.isCollapsed()).to.eventually.be.true;
  });

  it('should properly show various detail controls', () => {
    openPage('en', 'bg', 'MX1001');

    // select field for which to validate controls
    fields.getField('description').showAttributes();
    let descriptionDetails = fields.getModelDetails();

    // general attributes should be always able to edit the general attributes
    let generalAttributesPanel = descriptionDetails.getGeneralAttributesPanel();
    expect(generalAttributesPanel.canEditAttributes()).to.eventually.be.true;

    // behaviour attributes should be able to navigate when it is inherited
    let behaviourAttributesPanel = descriptionDetails.getBehaviourAttributesPanel();
    expect(behaviourAttributesPanel.canNavigate()).to.eventually.be.true;
  });

  it('should properly display fields when the page is opened with a non existing user or system language', () => {
    openPage('fr', 'gr', 'MX1001');
    // show all the fields
    fields.toggleSystem();
    fields.toggleHidden();

    // base fields are not filtered and all should be visible
    expect(fields.getField('description').getLabel()).to.eventually.eq('Description');
    expect(fields.getField('missingDisplayType').getLabel()).to.eventually.eq('Property');

    // base regions are not filtered and all should be visible
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    // regions are not collapsed since all fields inside are visible due to filtering
    expect(fields.getRegion('generalDetails').isCollapsed()).to.eventually.be.false;
    expect(fields.getRegion('specificDetails').isCollapsed()).to.eventually.be.false;

    // should show system fields from this region as well
    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(16);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('State');
      expect(items[2].getLabel()).to.eventually.eq('System');
      expect(items[3].getLabel()).to.eventually.eq('Type');
      expect(items[4].getLabel()).to.eventually.eq('Level');
      expect(items[5].getLabel()).to.eventually.eq('Country');
      expect(items[6].getLabel()).to.eventually.eq('Checkbox');
      expect(items[7].getLabel()).to.eventually.eq('Date');
      expect(items[8].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[9].getLabel()).to.eventually.eq('Numeric field');
      expect(items[10].getLabel()).to.eventually.eq('Numeric Fixed field');
      expect(items[11].getLabel()).to.eventually.eq('Floating Point Field');
      expect(items[12].getLabel()).to.eventually.eq('Fixed Length Title');
      expect(items[13].getLabel()).to.eventually.eq('Creator');
      expect(items[14].getLabel()).to.eventually.eq('Numeric Fixed (Double range)');
    });

    // should show hidden fields from this region as well
    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(3);
      expect(items[0].getLabel()).to.eventually.eq('Hidden');
      expect(items[1].getLabel()).to.eventually.eq('E-mail address');
      expect(items[2].getLabel()).to.eventually.eq('Notes');
    });
  });

  it('should have the restore action visible when selection has overridden models present', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      expect(panel.canRestoreAttributes()).to.eventually.be.true;
    });
  });

  it('should have the single restore action visible when selection has overridden models present', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have restore action visible for overridden attributes
      expect(panel.canRestoreAttribute('type')).to.eventually.be.true;
      expect(panel.canRestoreAttribute('order')).to.eventually.be.true;

      // should not have restore action visible for inherited attributes
      expect(panel.canRestoreAttribute('rnc')).to.eventually.be.false;
      expect(panel.canRestoreAttribute('label')).to.eventually.be.false;
    });
  });

  it('should not have the restore action visible when selection is only defined in current model', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[1].showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      expect(panel.canRestoreAttributes()).to.eventually.be.false;
    });
  });

  it('should not have the restore action visible when selection is completely inherited', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      expect(panel.canRestoreAttributes()).to.eventually.be.false;
    });
  });

  it('should not have the restore action visible when selection is not inherited', () => {
    openPage('en', 'bg', 'EO1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      expect(panel.canRestoreAttributes()).to.eventually.be.false;
    });
  });
});