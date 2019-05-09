'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management controls related fields - manage', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should display tooltips when hover over labels', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      expect(control.hasRerenderTooltip()).to.eventually.be.true;
      expect(control.hasFilterSourceTooltip()).to.eventually.be.true;
      expect(control.hasCustomTooltip()).to.eventually.be.true;
      expect(control.hasInclusiveTooltip()).to.eventually.be.true;
    });
  });

  it('should collect all fields with type codelist', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      expect(control.rerenderField.getMenuValues()).to.eventually.deep.eq(['type', 'state', 'level', 'country', 'inheritedType']);
      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('inheritedType');
    });
  });

  it('should fill filter source options correct', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      expect(control.getFilterSourceField().getMenuValues()).to.eventually.deep.eq(['extra1', 'extra2', 'extra3']);
      expect(control.getFilterSourceField().getSelectedValue()).to.eventually.equal('extra1');

      control.rerenderField.selectFromMenu(null, 4, true);

      expect(control.getFilterSourceField(true).getMenuValues()).to.eventually.deep.eq(['GBR', 'BGN', 'AUS', 'FRA', 'USA', 'CAN']);
      expect(control.customField.getSelectedValue()).to.eventually.equal('values');
      expect(control.getFilterSourceField(true).getSelectedValue()).to.eventually.deep.eq([]);
    });
  });

  it('should fill custom filter options correct', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      control.rerenderField.selectFromMenu(null, 4, true);
      expect(control.customField.getSelectedValue()).to.eventually.equal('values');
      expect(control.getFilterSourceField(true).getMenuValues()).to.eventually.deep.eq(['GBR', 'BGN', 'AUS', 'FRA', 'USA', 'CAN']);
    });
  });

  it('should fill inclusive correct', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      expect(control.inclusiveField.isSelected()).to.eventually.be.true;
    });
  });

  it('should load custom filters correct', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('type');
      expect(control.customField.getSelectedValue()).to.eventually.equal('values');
      expect(control.getFilterSourceField(true).getSelectedValue()).to.eventually.deep.eq(['PR0002', 'PR0005']);
      expect(control.inclusiveField.isSelected()).to.eventually.be.true;
    });
  });

  it('should reset control properly when cancel button is clicked', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');
      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('inheritedType');
      expect(control.getFilterSourceField().getSelectedValue()).to.eventually.equal('extra1');

      control.rerenderField.selectFromMenu(null, 4, true);

      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('country');
      expect(control.customField.getSelectedValue()).to.eventually.equal('values');

      fields.getModelControls().getModelCancel().click();
      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('inheritedType');
      expect(control.getFilterSourceField().getSelectedValue()).to.eventually.equal('extra1');
    });
  });

  it('should keep model pristine when custom filter is used', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[2];
      type.showAttributes();

      let controlsSection = fields.getModelControlsSection();
      controlsSection.getControl('RELATED_FIELDS');

      expect(type.isDirty()).to.eventually.be.false;
    });
  });

  it('should be able to edit the re-render parameter in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the country field which has a related field control
    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I edit the rerender property in the control
      let control = controlsSection.getControl('RELATED_FIELDS');
      control.rerenderField.selectFromMenu(null, 1, true);

      // Then I expect the control to become dirty
      expect(control.isDirty(), 'RELATED_FIELDS control should be dirty!').to.eventually.be.true;

      // And I expect the country field to become dirty
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save the model
      ModelTestUtils.saveSection(fields);

      // Then I expect the country field to become pristine
      expect(country.isDirty(), 'Country field should not be dirty!').to.eventually.be.false;

      // And I expect the control to become pristine
      expect(control.isDirty(), 'RELATED_FIELDS control should be pristine!').to.eventually.be.false;

      // And I expect the template property to have the new value
      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('type');

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should be able to edit the filter source parameter in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the country field which has a related field control
    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I edit the filter source property in the control
      let control = controlsSection.getControl('RELATED_FIELDS');
      control.getFilterSourceField().selectFromMenu(null, 2, true);

      // Then I expect the control to become dirty
      expect(control.isDirty(), 'RELATED_FIELDS control should be dirty!').to.eventually.be.true;

      // And I expect the country field to become dirty
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save the model
      ModelTestUtils.saveSection(fields);

      // Then I expect the country field to become pristine
      expect(country.isDirty(), 'Country field should not be dirty!').to.eventually.be.false;

      // And I expect the control to become pristine
      expect(control.isDirty(), 'RELATED_FIELDS control should be pristine!').to.eventually.be.false;

      // And I expect the property to have the new value
      expect(control.getFilterSourceField().getSelectedValue()).to.eventually.equal('extra2');

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should be able to edit the inclusive parameter in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the country field which has a related field control
    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // When I edit the inclusive checkbox
      let control = controlsSection.getControl('RELATED_FIELDS');
      control.inclusiveField.toggleCheckbox();

      // Then I expect the control to become dirty
      expect(control.isDirty(), 'RELATED_FIELDS control should be dirty!').to.eventually.be.true;

      // And I expect the country field to become dirty
      expect(country.isDirty(), 'Country field should be dirty!').to.eventually.be.true;

      // And I expect to be able to save or cancel
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // When I save the model
      ModelTestUtils.saveSection(fields);

      // Then I expect the country field to become pristine
      expect(country.isDirty(), 'Country field should not be dirty!').to.eventually.be.false;

      // Then I expect the control to become pristine
      expect(control.isDirty(), 'RELATED_FIELDS control should be pristine!').to.eventually.be.false;

      // And I expect the template property to have the new value
      expect(control.inclusiveField.isSelected()).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should be able to cancel changes made in existing control', () => {
    // Given I have opened the media definition model
    openPage('en', 'en', 'MX1001');

    // And I have selected the description field which has a default value pattern control
    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      let controlsSection = fields.getModelControlsSection();

      // And I have edited the template property in the control
      let control = controlsSection.getControl('RELATED_FIELDS');
      control.rerenderField.selectFromMenu(null, 4, true);

      // When I select cancel
      fields.getModelControls().getModelCancel().click();

      // Then I expect the changes in the control to be reverted
      expect(control.rerenderField.getSelectedValue()).to.eventually.equal('inheritedType');

      // And I expect control to become pristine
      expect(control.isDirty()).to.eventually.be.false;

      // And I expect the country field to become pristine
      expect(country.isDirty()).to.eventually.be.false;

      // And I can't save or cancel anymore
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should reload custom field values when selected codelist is changed', () => {
    openPage('en', 'en', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      country.showAttributes();
      let details = fields.getModelDetails();

      let controlsSection = fields.getModelControlsSection();
      let control = controlsSection.getControl('RELATED_FIELDS');

      control.rerenderField.selectFromMenu(null, 4, true);
      control.getFilterSourceField(true).selectFromMenu(null, 1, true);

      expect(control.getFilterSourceField(true).getMenuValues()).to.eventually.deep.eq(['GBR', 'BGN', 'AUS', 'FRA', 'USA', 'CAN']);

      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        attr.getField().selectOption(1);
        expect(control.getFilterSourceField(true).getMenuValues()).to.eventually.deep.eq(['APPROVED', 'COMPLETED']);
      });
    });

  });

});