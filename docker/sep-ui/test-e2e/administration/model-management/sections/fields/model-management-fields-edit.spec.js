'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let CheckboxField = require('../../../../form-builder/form-control').CheckboxField;
let FormControl = require('../../../../form-builder/form-control').FormControl;

describe('Models management fields section - editing', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be able to edit and revert back to normal attributes', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
      attr.getField().setValue(null, 'someone-else');
      canSaveOrCancel();

      attr.getField().setValue(null, 'John Doe');
      cannotSaveOrCancel();
    });
  });

  it('should be able to edit and revert back value when section is opened in non existing language', () => {
    openPage('fr', 'gr', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, 'new-name');
      canSaveOrCancel();

      // should dynamically change the name as the user types in
      expect(property.getLabel()).to.eventually.eq('new-name');

      attr.getField().setValue(null, '');
      cannotSaveOrCancel();

      // should fallback to the default language when present
      expect(property.getLabel()).to.eventually.eq('Title');
    });
  });

  it('should be able to clear option attributes which have no default value and are not mandatory', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.sirma.com/ontologies/2014/11/security#autoAssignParentPermissionRole').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);
      canSaveOrCancel();

      // should assign the selected value from the drop down
      expect(field.getSelectedValue()).to.eventually.eq('MANAGER');

      field.clearField();
      cannotSaveOrCancel();

      // should clear the attribute back to having no value
      expect(field.getSelectedValue()).to.eventually.eq(null);
    });
  });

  it('should not be able to edit attributes when located in inherited model', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Abstract');
    fields.toggleInherited();

    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
      expect(attr.getField().isReadOnly()).to.eventually.be.true;
    });
  });

  it('should be able to navigate to parent when located in inherited model', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Abstract');
    fields.toggleInherited();

    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().navigateToParent();
    expect(sandbox.isModelProvided('http://www.ontotext.com/proton/protontop#Entity')).to.eventually.be.true;
  });

  it('should disable controls after save is performed', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
      attr.getField().setValue(null, 'someone-else');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;

      fields.getModelControls().getModelSave().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
    });
  });

  it('should pop notification after saving changes', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
      attr.getField().setValue(null, 'someone-else');

      fields.getModelControls().getModelSave().click();
      expect(fields.getModelControls().getModelSave().getNotification().isSuccess()).to.eventually.be.true;
    });
  });

  it('should disable controls after cancel is performed', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
      attr.getField().setValue(null, 'someone-else');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;

      fields.getModelControls().getModelCancel().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
    });
  });

  it('should prevent saving if mandatory attributes are empty', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, '');

      expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(attr.isDirty()).to.eventually.be.true;

      // Restore original value -> see that validation is properly triggered
      attr.getField().setValue(null, 'Title');
      expect(attr.isInvalid()).to.eventually.be.false;
    });
  });

  it('should perform validation for multi language attributes even if modified through the values dialog', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
      let titleDialog = attr.getValuesDialog();
      titleDialog.getField(2).setValue(null, '');

      // should mark the invalid value properly in the dialog
      expect(titleDialog.isFieldInvalid(2)).to.eventually.be.true;

      titleDialog.close();

      // should validate the main components after the values dialog has been closed
      expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should not validate values for multi language attributes that are not in the current language', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);
      bgDescription.setValue(null, '');
      titleDialog.close();

      expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should re-validate attributes after form and changes are canceled', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    let property = fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title');
    property.showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
      let titleDialog = attr.getValuesDialog();
      let enDescription = titleDialog.getField(2);
      enDescription.setValue(null, '');
      titleDialog.close();

      expect(attr.isDirty()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;

      fields.getModelControls().getModelCancel().click();

      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
    });
  });

  it('should not be able to edit generic attributes from definition field model', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      // should be collapsed by default, need to expand it
      details.getGeneralAttributesPanel().toggleCollapse();

      // usually editable attributes should now not be editable directly when a definition model is opened
      details.getGeneralAttributesPanel().getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
        expect(attr.getField().isReadOnly()).to.eventually.be.true;
      });

      // usually editable attributes should now not be editable directly when a definition model is opened
      details.getGeneralAttributesPanel().getAttribute('http://purl.org/dc/terms/title').then(attr => {
        expect(attr.getField().isReadOnly()).to.eventually.be.true;
      });
    });
  });

  it('should properly re-validate the current selection after canceling executed changes', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let field = items[1];
      field.showAttributes();

      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      let mandatory = panel.getAttribute('mandatory');
      let display = panel.getAttribute('displayType');

      mandatory.then(attr => {
        // attribute value should be the overridden one
        expect(attr.isDirty()).to.eventually.be.false;

        attr.getField().toggleCheckbox();

        // dirty and invalid due to the display type
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;

        return display;
      }).then(attr => {
        // dirty and invalid due to the mandatory flag
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.isInvalid()).to.eventually.be.true;
      }).then(() => {
        expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
        expect(fields.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

        // canceling changes should restore form to valid
        fields.getModelControls().getModelCancel().click();

        // resolve and get the set of attributes
        return Promise.all([mandatory, display]);
      }).then(attributes => {
        // destruct assign the both attributes
        let [mandatory, display] = attributes;

        // mandatory attribute should not be dirty & valid
        expect(mandatory.isDirty()).to.eventually.be.false;
        expect(mandatory.isInvalid()).to.eventually.be.false;

        // display attribute should not be dirty & valid
        expect(display.isDirty()).to.eventually.be.false;
        expect(display.isInvalid()).to.eventually.be.false;

        // the form is clean
        cannotSaveOrCancel();
      });
    });
  });

  it('should be able to save after single restore attribute operation has been performed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have nothing to save
      cannotSaveOrCancel();

      // restore overridden attributes
      panel.restoreAttribute('order').ok();

      // should mark form as dirty
      canSaveOrCancel();
    });
  });

  it('should be able to mark the field as dirty after restore operation has been performed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have nothing to save
      cannotSaveOrCancel();

      // restore overridden attributes
      panel.restoreAttribute('order').ok();

      // should mark form as dirty
      canSaveOrCancel();

      return field;
    }).then((field) => {
      // field with restored attribute should be dirty
      expect(field.isDirty()).to.eventually.be.true;
    });
  });

  it('should be to save the form after attribute has been edited and then restored', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      let multiValued = panel.getAttribute('multiValued');

      multiValued.then(attr => {
        // should have the field overridden on startup
        expect(field.isInherited()).to.eventually.be.false;

        // attribute value should be the overridden one
        expect(attr.isDirty()).to.eventually.be.false;

        attr.getField().toggleCheckbox();
        expect(attr.isDirty()).to.eventually.be.true;
        expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;

        // restore overridden attributes
        panel.restoreAttribute('multiValued').ok();

        expect(attr.isDirty()).to.eventually.be.false;
        expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      });
    });
  });

  it('should be able to completely restore a single attribute from the parent', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have nothing to save
      cannotSaveOrCancel();

      // should have the restore action visible
      expect(panel.canRestoreAttribute('order')).to.eventually.be.true;

      // restore overridden attributes
      panel.restoreAttribute('order').ok();

      // should mark form as dirty
      canSaveOrCancel();

      // should not allow and remove the restore action
      expect(panel.canRestoreAttribute('order')).to.eventually.be.false;
    });
  });

  it('should restore multiple attributes automatically from the parent', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have nothing to save
      cannotSaveOrCancel();

      // should have actions for all related attributes
      expect(panel.canRestoreAttribute('type')).to.eventually.be.true;
      expect(panel.canRestoreAttribute('value')).to.eventually.be.true;
      expect(panel.canRestoreAttribute('typeOption')).to.eventually.be.true;

      // restore concrete attribute type
      panel.restoreAttribute('typeOption').ok();

      // should mark form as dirty
      canSaveOrCancel();

      // should remove the actions for all related attributes
      expect(panel.canRestoreAttribute('type')).to.eventually.be.false;
      expect(panel.canRestoreAttribute('value')).to.eventually.be.false;
      expect(panel.canRestoreAttribute('typeOption')).to.eventually.be.false;
    });
  });

  it('should be able to save after restore attributes operation has been performed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // should have nothing to save
      cannotSaveOrCancel();

      // restore overridden attributes
      panel.restoreAttributes().ok();

      // should mark form as dirty
      canSaveOrCancel();
    });
  });

  it('should be able to restore inherited attributes when overridden attributes are present in an overridden field', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();
      let type = panel.getAttribute('type');

      type.then(attr => {
        // should have the field overridden on startup
        expect(field.isInherited()).to.eventually.be.false;

        // attribute value should be the overridden one
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getValue()).to.eventually.eq('180');

        // restore all attributes
        panel.restoreAttributes().ok();
        return type;
      }).then(attr => {
        // should be inherited when we restore all attributes
        expect(field.isInherited()).to.eventually.be.true;

        // attribute value should be restored from parent
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getValue()).to.eventually.eq('255');

        // cancel the attribute restore operation
        fields.getModelControls().getModelCancel().click();
        return type;
      }).then(attr => {
        // should be back to being overridden after canceling
        expect(field.isInherited()).to.eventually.be.false;

        // attribute value should be back to the overridden
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getValue()).to.eventually.eq('180');
      });
    });
  });

  it('should be able to restore override inherited attributes and then restore back to inherited', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      panel.getAttribute('type').then(attr => {
        // should not be able to save when form is not touched
        cannotSaveOrCancel();

        // should have the field inherited on startup
        expect(field.isInherited()).to.eventually.be.true;

        // override the inherited type attribute
        attr.getField().setValue(null, '255');

        // attribute value should be the overridden one
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('255');

        // should be able to save when form is modified
        canSaveOrCancel();

        // restore all attributes
        panel.restoreAttributes().ok();
        return panel.getAttribute('type');
      }).then(attr => {
        // should inherit the field directly from the parent
        expect(field.isInherited()).to.eventually.be.true;

        // attribute value should be restored from parent
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getValue()).to.eventually.eq('180');

        // cancel the attribute restore operation
        fields.getModelControls().getModelCancel().click();

        // should not be able to save when changes are canceled
        cannotSaveOrCancel();

        return panel.getAttribute('type');
      }).then(attr => {
        // should be back to being inherited after canceling
        expect(field.isInherited()).to.eventually.be.true;

        // attribute value should be back to the inherited
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getValue()).to.eventually.eq('180');
      });
    });
  });

  it('should be able to decline restore inherited operation execution', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('inheritedDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      panel.getAttribute('type').then(attr => {
        // should not be able to save when form is not touched
        cannotSaveOrCancel();

        // should have the field inherited on startup
        expect(field.isInherited()).to.eventually.be.true;

        // override the inherited type attribute
        attr.getField().setValue(null, '255');

        // attribute value should be the overridden one
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('255');

        // should be able to save when form is modified
        canSaveOrCancel();

        // restore all attributes
        panel.restoreAttributes().cancel();
        return panel.getAttribute('type');
      }).then(attr => {
        // should inherit the field directly from the parent
        expect(field.isInherited()).to.eventually.be.false;

        // attribute value should be restored from parent
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('255');

        // should be able to save
        canSaveOrCancel();
      });
    });
  });

  it('should be able to navigate to generic attributes to edit when located in definition field', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      details.getGeneralAttributesPanel().editGeneralAttributes();
      expect(sandbox.isModelProvided('http://www.ontotext.com/proton/protontop#Entity')).to.eventually.be.true;
    });
  });

  it('should properly resolve hints or tooltips when they are not present in the field', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // title field from this region should have a tooltip
      let field = items[0];
      field.showAttributes();
      fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('tooltip').then(attr => {
        // should have the default tooltip for the field
        expect(field.hasTooltip()).to.eventually.be.true;
        expect(field.getTooltip()).to.eventually.equal('Specifies the name or title of the current field');

        // clear the default field tooltip
        attr.getField().setValue(null, '');

        // should still have a tooltip present for the field
        expect(field.hasTooltip()).to.eventually.be.true;
        expect(field.getTooltip()).to.eventually.equal('Property representing a name or a title');
      });
    });
  });

  it('should be able to edit inherited attributes of non inherited definition fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label').then(attr => {
        // the label of the field should have entry value
        expect(field.getLabel()).to.eventually.eq('Title');

        // enter a value for the actual label
        attr.getField().setValue(null, 'new-label');

        canSaveOrCancel();
        expect(attr.isDirty()).to.eventually.be.true;

        fields.getModelControls().getModelSave().click();

        cannotSaveOrCancel();
        expect(attr.isDirty()).to.eventually.be.false;

        // the label of the field should have a new value
        expect(field.getLabel()).to.eventually.eq('new-label');
      });
    });
  });

  it('should be able to edit the attributes of inherited field for a model definition', () => {
    openPage('en', 'bg', 'MX1001');
    let field = fields.getField('description');

    field.showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('tooltip').then(attr => {
      // should be inherited and tooltip should not be present before the edit
      expect(field.hasTooltip()).to.eventually.be.false;
      expect(field.isInherited()).to.eventually.be.true;

      // enter a value for the actual tooltip
      attr.getField().setValue(null, 'new-tooltip');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;

      fields.getModelControls().getModelSave().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;

      // should not be inherited and tooltip should  be present after the edit
      expect(field.hasTooltip()).to.eventually.be.true;
      expect(field.isInherited()).to.eventually.be.false;
    });
  });

  it('should be able to completely restore inherited region for a model definition', () => {
    openPage('en', 'bg', 'MX1001');

    let region = fields.getRegion('generalDetails');
    region.showAttributes();
    let details = fields.getModelDetails();
    let panel = details.getBehaviourAttributesPanel();

    // form should be clean
    cannotSaveOrCancel();

    // region should have fields contained inside of it
    expect(region.hasFields()).to.eventually.be.true;

    // restore the entire region
    panel.restoreAttributes().ok();

    // form should be dirty
    canSaveOrCancel();

    // after restore all overridden fields are removed
    expect(region.hasFields()).to.eventually.be.false;

    // cancel the restore action changes made to the form
    fields.getModelControls().getModelCancel().click();

    // form should be clean
    cannotSaveOrCancel();

    // should return back fields related to region
    expect(region.hasFields()).to.eventually.be.true;
  });

  it('should be able to completely restore inherited field for a model definition', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      let panel = details.getBehaviourAttributesPanel();

      // form should be clean
      cannotSaveOrCancel();
      expect(field.isInherited()).to.eventually.be.false;

      // restore the field field
      panel.restoreAttributes().ok();

      // field should be completely restored from parent
      expect(field.isInherited()).to.eventually.be.true;

      // form should be dirty
      canSaveOrCancel();
      fields.getModelControls().getModelCancel().click();

      // after canceling changes field is returned back
      expect(field.isInherited()).to.eventually.be.false;
    });
  });

  it('should be able to manually restore the inherited field for a model definition', () => {
    openPage('en', 'bg', 'MX1001');
    let field = fields.getField('description');

    field.showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('tooltip').then(attr => {
      // should be inherited and tooltip should not be present before the edit

      expect(field.isInherited()).to.eventually.be.true;

      // enter a value for the actual tooltip
      attr.getField().setValue(null, 'new-tooltip');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;

      // should have the field overridden when edited
      expect(field.hasTooltip()).to.eventually.be.true;
      expect(field.isInherited()).to.eventually.be.false;

      // restore old value which was empty
      attr.getField().setValue(null, '');
      expect(attr.isDirty()).to.eventually.be.false;

      // should have the field restored as inherited
      expect(field.hasTooltip()).to.eventually.be.false;
      expect(field.isInherited()).to.eventually.be.true;
    });
  });

  it('should be able to edit inherited and overridden attributes and swap back and forward between models', () => {
    openPage('en', 'bg', 'EO1001');
    let tree = sandbox.getModelTree();
    let field = fields.getField('description');

    field.showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label').then(attr => {
      // field is not inherited and is owned by entity
      expect(field.isInherited()).to.eventually.be.false;

      // enter a value for label through dialog
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);
      bgDescription.setValue(null, 'new-entity-label');
      titleDialog.close();

      canSaveOrCancel();

      // change to the child definition
      tree.search('Media');
      tree.getNode('Media').openObject();

      field = fields.getField('description').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      // should be inherited from the entity definition
      expect(field.isInherited()).to.eventually.be.true;

      // enter a value for label through dialog
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);
      bgDescription.setValue(null, 'new-media-label');
      titleDialog.close();

      canSaveOrCancel();
      // should override the field with the new value
      expect(field.isInherited()).to.eventually.be.false;

      // go back to the parent definition
      tree.search('entity');
      tree.getNode('entity').openObject();

      field = fields.getField('description').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      // enter a value for label through dialog
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);

      // should have the initial label entered for the description field
      bgDescription.getValue().then(value => {
        expect(value).to.eq('new-entity-label');
        titleDialog.close();
        canSaveOrCancel();
      });

      // go back to the child definition
      tree.search('Media');
      tree.getNode('Media').openObject();

      field = fields.getField('description').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      // enter a value for label through dialog
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);

      // should have the initial label entered for the description field
      bgDescription.getValue().then(value => {
        expect(value).to.eq('new-media-label');
        titleDialog.close();
        canSaveOrCancel();
      });
    });
  });

  it('should be able to edit inherited attributes and cancel the changes restoring inherited attributes or fields', () => {
    openPage('en', 'bg', 'MX1001');
    let field = fields.getField('description');

    field.showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label').then(attr => {
      expect(field.isInherited()).to.eventually.be.true;
      attr.getField().setValue(null, 'new-label');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;
      expect(field.isInherited()).to.eventually.be.false;

      fields.getModelControls().getModelCancel().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
      expect(field.isInherited()).to.eventually.be.true;
    });
  });

  it('should be able to edit inherited attributes and cancel the changes restoring inherited attributes or regions', () => {
    openPage('en', 'bg', 'MX1001');
    let region = fields.getRegion('inheritedDetails').showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label').then(attr => {
      attr.getField().setValue(null, 'new-label');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;
      expect(region.getLabel()).to.eventually.eq('new-label');

      fields.getModelControls().getModelCancel().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
      expect(region.getLabel()).to.eventually.eq('Inherited details');
    });
  });

  it('should be able to edit inherited attributes and cancel the changes while changes are introduced to the parent', () => {
    openPage('en', 'bg', 'EO1001');
    let tree = sandbox.getModelTree();
    let region = fields.getRegion('inheritedDetails').showAttributes();

    fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label').then(attr => {
      attr.getField().setValue(null, 'Inherited edit from Entity');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;
      expect(region.getLabel()).to.eventually.eq('Inherited edit from Entity');

      // go back to the child definition
      tree.search('Media');
      tree.getNode('Media').openObject();

      region = fields.getRegion('inheritedDetails').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      cannotSaveOrCancel();
      // make sure that inherited region has it's updated label from parent
      expect(region.getLabel()).to.eventually.eq('Inherited edit from Entity');

      // override the region from the child definition with new label
      attr.getField().setValue(null, 'Inherited edit from Media');

      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;
      // region should have new label after the override is performed from media
      expect(region.getLabel()).to.eventually.eq('Inherited edit from Media');

      // go back to the parent definition
      tree.search('entity');
      tree.getNode('entity').openObject();

      region = fields.getRegion('inheritedDetails').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      // cancel the changes made to the region from parent
      fields.getModelControls().getModelCancel().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
      // make sure that inherited region has restored it's old label
      expect(region.getLabel()).to.eventually.eq('Inherited details');

      // go back to the child definition
      tree.search('Media');
      tree.getNode('Media').openObject();

      region = fields.getRegion('inheritedDetails').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel().getAttribute('label');
    }).then(attr => {
      canSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.true;
      // restore from parent is executed but media has already overridden it
      expect(region.getLabel()).to.eventually.eq('Inherited edit from Media');

      // cancel the changes made to the region from media
      fields.getModelControls().getModelCancel().click();

      cannotSaveOrCancel();
      expect(attr.isDirty()).to.eventually.be.false;
      // make sure that region has been restored from parent region
      expect(region.getLabel()).to.eventually.eq('Inherited details');
    });
  });

  it('should execute validation when field attributes are loaded', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.false;
      });

      items[1].showAttributes();
      details.getBehaviourAttributesPanel().getAttribute('mandatory').then(attr => {
        expect(attr.getField().isSelected()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.true;
      });
    });
  });

  it('should disable boolean attribute if it\'s not editable', () => {
    // Boolean attribute should be disabled if it's marked as updateable: false in meta model
    // See also - https://stackoverflow.com/questions/33777710/ng-readonly-not-working-in-angular-checkbox
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[0].showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('disabledField').then(attr => {
        expect(attr.getField().isDisabled()).to.eventually.be.true;
      });
    });
  });

  it('should show the label for the semantic property when field label is cleared or empty', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let field = items[0];
      field.showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('label').then(attr => {
        // clear the label of the field
        attr.getField().setValue(null, '');
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.false;

        canSaveOrCancel();

        // should show the semantic label even when cleared
        expect(field.getLabel()).to.eventually.eq('Title');

        attr.getField().setValue(null, 'New title');
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.isInvalid()).to.eventually.be.false;

        canSaveOrCancel();

        // should show the new label after the edit is performed
        expect(field.getLabel()).to.eventually.eq('New title');
      });
    });
  });


  it('should update selected field value when checkbox is toggled', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let checkbox = items[5].showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          field.toggleCheckbox();
        });
      });
      let control = new CheckboxField(checkbox.getEditControl());
      expect(control.isSelected()).to.eventually.be.true;

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          field.toggleCheckbox();
        });
      });
      expect(control.isSelected()).to.eventually.be.false;
    });
  });

  it('should update selected field value when codelist value is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1].showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          field.selectFromMenu(null, 2, true);
        });
      });
      let control = new FormControl(state.getViewControl());
      expect(control.getText()).to.eventually.eq('Completed');
    });
  });

  function canSaveOrCancel() {
    // form is now dirty and both save and cancel are enabled
    expect(modelData.isFieldsSectionModified()).to.eventually.be.true;
    expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
    expect(fields.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
  }

  function cannotSaveOrCancel() {
    // form not dirty and both save and cancel are disabled
    expect(modelData.isFieldsSectionModified()).to.eventually.be.false;
    expect(fields.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(fields.getModelControls().getModelCancel().isDisabled()).to.eventually.be.true;
  }
});