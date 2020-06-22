'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management fields section - creating properties', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be able to create a new model property through a definition model', () => {
    openPage('en', 'en', 'MX1001');

    let tree = sandbox.getModelTree();
    let dialog = fields.createProperty();

    // save control should initially be disabled since the form is not yet valid
    expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.setValue(null, 'new property');

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      return dialog;
    }).then(dialog => {
      // should be able to save the dialog since all mandatory fields are correctly entered
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should be able to create a new model property through a class model', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    // save control should initially be disabled since the form is not yet valid
    expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.setValue(null, 'new property');

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.false;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.true;

      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute should not be dirty initially
      expect(attr.isDirty()).to.eventually.be.true;

      // attribute is mandatory but is also empty
      expect(attr.isInvalid()).to.eventually.be.false;

      return dialog;
    }).then(dialog => {
      // should be able to save the dialog since all mandatory fields are correctly entered
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should be able to save the section after semantic property create has been performed', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      return ModelTestUtils.saveSection(dialog);
    }).then(() => {
      // should be able to save the fields section after dialog is confirmed
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // perform the save of the section
      ModelTestUtils.saveSection(fields);

      // extract the details and attributes for the created semantic property
      fields.getProperty('http://www.ontotext.com/proton/protontop#newProperty').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      //after performing the section save attributes are no longer dirty

      details.getAttribute('http://purl.org/dc/terms/title').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
      });

      details.getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
      });

      details.getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
      });

      details.getAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#uri').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
      });
    });
  });

  it('should not be able to save the create form when the property is not unique', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      return dialog;
    }).then(dialog => {
      // duplicate message for the property name should be displayed
      expect(dialog.isDuplicateMessageVisible()).to.eventually.be.true;

      // should not be able to save the form when a duplicate property name is entered
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      return dialog.getPropertyAttribute('http://purl.org/dc/terms/title');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new-unique-name');

      // duplicate message for the property should no longer be displayed
      expect(dialog.isDuplicateMessageVisible()).to.eventually.be.false;

      // should be able to save the form when the property name is no more duplicated
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should add the field to the definitions and the property to the class when creating from a class', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Abstract');

    let tree = sandbox.getModelTree();
    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      ModelTestUtils.saveSection(dialog);

      // three nodes should be modified - two definitions & a class
      expect(tree.isNodeModified('Abstract')).to.eventually.be.true;
      expect(tree.isNodeModified('No Actions')).to.eventually.be.true;
      expect(tree.isNodeModified('Media')).to.eventually.be.true;
    });
  });

  it('should add the field to the definition and the property to the class when creating from a definition', () => {
    openPage('en', 'en', 'MX1001');

    let tree = sandbox.getModelTree();
    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      ModelTestUtils.saveSection(dialog);

      // property is added to the class and field to the definition
      expect(tree.isNodeModified('Abstract')).to.eventually.be.true;
      expect(tree.isNodeModified('Media')).to.eventually.be.true;

      // definition on the same level should remain intact and not dirty
      expect(tree.isNodeModified('No Actions')).to.eventually.be.false;
    });
  });

  it('should add created model to the list of semantic properties for a class', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      return ModelTestUtils.saveSection(dialog);
    }).then(() => {
      expect(fields.hasSelectedModel()).to.eventually.be.true;
      expect(fields.isPropertyDisplayed('http://www.ontotext.com/proton/protontop#newProperty')).to.eventually.be.true;

      // extract the details and attributes for the created semantic property
      fields.getProperty('http://www.ontotext.com/proton/protontop#newProperty').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // after confirming the create dialog attributes are shown as dirty

      details.getAttribute('http://purl.org/dc/terms/title').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('new property');
      });

      details.getAttribute('http://www.w3.org/2000/01/rdf-schema#creator').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('John Doe');
      });

      details.getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Data property');
      });

      details.getAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#uri').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('http://www.ontotext.com/proton/protontop#newProperty');
      });
    });
  });

  it('should add created model to the list of fields for a definition', () => {
    openPage('en', 'en', 'MX1001');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // next attribute is part of the field stack
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      return ModelTestUtils.saveSection(dialog);
    }).then(() => {
      expect(fields.hasSelectedModel()).to.eventually.be.true;
      expect(fields.isFieldDisplayed('proton:newProperty')).to.eventually.be.true;

      // extract the details and attributes for the created field
      fields.getField('proton:newProperty').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // the name attribute should be correct
      details.getAttribute('name').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('proton:newProperty');
      });

      // the field type should be alpha numeric type
      details.getAttribute('typeOption').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Alpha Numeric');
      });

      // the uri attribute should be correct
      details.getAttribute('uri').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('http://www.ontotext.com/proton/protontop#newProperty');
      });
    });
  });

  it('should properly resolve target models when creating a property through a class', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Abstract');

    let dialog = fields.createProperty();
    expect(dialog.isDestinationMessageVisible()).to.eventually.be.true;
    expect(dialog.getDestinationModels()).to.eventually.eq('No Actions, Media');
  });

  it('should reflect changes in the audit attribute for the property', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#auditEvent').then(attr => {
      // semantic uri attribute is empty by default initially
      expect(attr.getField().getValue()).to.eventually.eq('');

      // extract the property title to enter a proper value for it
      return dialog.getPropertyAttribute('http://purl.org/dc/terms/title');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // entering value for the title should be reflected directly to the audit attribute of the semantic property
      return dialog.getPropertyAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#auditEvent');
    }).then(attr => {
      // semantic audit attribute should be affected by title being automatically generated to a given format
      expect(attr.getField().getValue()).to.eventually.eq('+addNewProperty|-removeNewProperty|changeNewProperty');

      return dialog;
    });
  });

  it('should reflect changes in the URI attributes for both properties & fields', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#uri').then(attr => {
      // semantic uri attribute is empty by default initially
      expect(attr.getField().getValue()).to.eventually.eq('');

      // next, get the field uri attribute
      return dialog.getFieldAttribute('uri');
    }).then(attr => {
      // field uri attribute is empty by default initially
      expect(attr.getField().getValue()).to.eventually.eq('');

      // next, get the field name attribute
      return dialog.getFieldAttribute('name');
    }).then(attr => {
      // field name attribute is empty by default initially
      expect(attr.getField().getValue()).to.eventually.eq('');

      // extract the property title to enter a proper value for it
      return dialog.getPropertyAttribute('http://purl.org/dc/terms/title');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // entering value for the title should be reflected directly to the uri attribute of the semantic property
      return dialog.getPropertyAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#uri');
    }).then(attr => {
      // semantic uri attribute should be affected by title
      expect(attr.getField().getValue()).to.eventually.eq('http://www.ontotext.com/proton/protontop#newProperty');

      // entering value for the title should be reflected directly to the uri attribute of the field
      return dialog.getFieldAttribute('uri');
    }).then(attr => {
      // field uri attribute should be affected by title
      expect(attr.getField().getValue()).to.eventually.eq('http://www.ontotext.com/proton/protontop#newProperty');

      // entering value for the title should be reflected directly to the name attribute of the field
      return dialog.getFieldAttribute('name');
    }).then(attr => {
      // field uri attribute should be affected by title
      expect(attr.getField().getValue()).to.eventually.eq('proton:newProperty');

      return dialog;
    });
  });

  it('should reflect changes in the title of both properties & fields', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Entity');

    let dialog = fields.createProperty();

    // initially field & property titles should be empty
    expect(dialog.getFieldTitle()).to.eventually.eq('""');
    expect(dialog.getPropertyTitle()).to.eventually.eq('""');

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // field has no entered title it defaults to the title of the property
      expect(dialog.getFieldTitle()).to.eventually.eq('"new property"');
      expect(dialog.getPropertyTitle()).to.eventually.eq('"new property"');

      // get the concrete field label attribute
      return dialog.getFieldAttribute('label');
    }).then(attr => {
      // field should keep it's default title from property until changed
      expect(dialog.getFieldTitle()).to.eventually.eq('"new property"');

      let field = attr.getField();
      field.setValue(null, 'Concrete field name');

      // changing the label of the field should be reflected in the form
      expect(dialog.getFieldTitle()).to.eventually.eq('"Concrete field name"');
    });
  });

  it('should display message that no results are found after cancel created field', () => {
    openPage('en', 'en', 'http://www.ontotext.com/proton/protontop#Abstract');

    // model should not have fields
    expect(fields.isNoResultsMessagePresent()).to.eventually.be.true;

    let dialog = fields.createProperty();

    dialog.getPropertyAttribute('http://purl.org/dc/terms/title').then(attr => {
      let field = attr.getField();
      field.setValue(null, 'new property');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/2000/01/rdf-schema#creator');
    }).then(attr => {
      let field = attr.getField();
      field.setValue(null, 'John Doe');

      // go to the next mandatory attribute that needs to be filled up
      return dialog.getPropertyAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);
      return dialog.getFieldAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save section with all attributes filled
      return ModelTestUtils.saveSection(dialog);
    }).then(() => {
      // there should be one field
      expect(fields.isNoResultsMessagePresent()).to.eventually.be.false;

      // should be able to save the fields section after dialog is confirmed
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // perform the cancel of the section
      fields.getModelControls().getModelCancel().click();

      // should show the 'No results' message
      expect(fields.isNoResultsMessagePresent()).to.eventually.be.true;
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should resolve creatable default values for both single and multi attribute types', () => {
    openPage('en', 'en', 'MX1001');

    let dialog = fields.createProperty();

    // displayType value should be resolved from meta data
    dialog.getFieldAttribute('displayType').then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
    });

    // definition value should be resolved from meta data
    dialog.getFieldAttribute('definition').then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.getField().getValue()).to.eventually.eq('Field create definition');
    });
  });
});