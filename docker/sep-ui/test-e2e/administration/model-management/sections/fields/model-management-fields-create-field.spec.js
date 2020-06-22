'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ModelTestUtils = require('../../model-management-test-utils.js').ModelTestUtils;

describe('Models management fields section - creating fields', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should be able to create a new model field when located inside definition', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    expect(dialog.getPropertySelect().getMenuValues()).to.eventually.deep.eq([
      'http://purl.org/dc/terms/checkboxForCreate',
      'http://www.w3.org/2008/05/skos#datetimeForCreate',
      'http://www.w3.org/2004/02/test/levelForCreate',
      'http://www.w3.org/ns/oa#noRangePropertyForCreate',
      'http://www.w3.org/2008/05/skos#noTypeAndRangePropertyForCreate',
      'http://purl.org/dc/terms/noTypePropertyForCreate',
      'http://www.w3.org/ns/oa#numericForCreate',
      'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#resourceForCreate'
    ]);
  });

  it('should not be able to save when the form is incomplete or invalid', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
  });

  it('should be able to create date time type field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 2, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq(['DATE_TYPE', 'DATETIME_TYPE']);

      field.selectFromMenu(null, 1, true);
      return dialog.getAttribute('value');
    }).then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      attr.getField().then(field => expect(field.getValue()).to.eventually.eq(''));
    });
  });

  it('should be able to select boolean type field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq(['BOOLEAN']);

      field.selectFromMenu(null, 1, true);
      return dialog.getAttribute('value');
    }).then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      attr.getField().then(field => expect(field.isSelected()).to.eventually.be.false);
    });
  });

  it('should be able to select numeric type field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 7, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq([
        'NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE', 'FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE'
      ]);

      field.selectFromMenu(null, 1, true);
      return dialog.getAttribute('type');
    }).then(attr => {
      let field = attr.getField();

      expect(attr.isDirty()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(field.getValue()).to.eventually.equal('');
    });
  });

  it('should be able to select alpha-numeric type field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 3, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq([
        'ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'CODELIST'
      ]);

      field.selectFromMenu(null, 4, true);
      return dialog.getAttribute('codeList');
    }).then(attr => {
      let field = attr.getField();
      field.open();

      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(field.getMenuElements()).to.eventually.deep.eq([
        '1 - Project state',
        '2 - Project type',
        '3 - Level',
        '13 - Language',
        '555 - Country'
      ]);
    });
  });

  it('should be able to create object type field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 8, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq(['URI']);

      field.selectFromMenu(null, 1, true);
      return dialog.getAttribute('value');
    }).then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      attr.getField().then(field => expect(field.getValue()).to.eventually.eq(''));
    });
  });

  it('should be able to select specific code value for code list field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 3, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq([
        'ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'CODELIST'
      ]);

      field.selectFromMenu(null, 4, true);
      return dialog.getAttribute('codeList');
    }).then(attr => {
      let field = attr.getField();

      field.selectFromMenu(null, 3, true);
      return dialog.getAttribute('value');
    }).then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;

      attr.getField().then(field => expect(field.getMenuValues()).to.eventually.deep.eq([
        'LOW', 'MEDIUM', 'HIGH'
      ]));
    });
  });

  it('should be able to create field and add it to the definition fields', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute is touched and should be dirty
      expect(attr.isDirty()).to.eventually.be.true;

      // save and cancel controls on the actual dialog should not be disabled anymore
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // save the dialog and await notification
      ModelTestUtils.saveSection(dialog);
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.true;

      // main section should now be dirty and ready to be saved
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // extract the details and attributes for the created field
      fields.getField('dc:checkboxForCreate').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // save the actual fields section
      ModelTestUtils.saveSection(fields);

      // the field type should not be dirty after save
      details.getAttribute('typeOption').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Булева стойност');
      });
    });
  });

  it('should be able to create field and mark it as dirty after creation', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // attribute is touched and should be dirty
      expect(attr.isDirty()).to.eventually.be.true;

      // save and cancel controls on the actual dialog should not be disabled anymore
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.true;

      // main section should now be dirty and ready to be saved
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);
    }).then(() => {
      // make sure that the created field is marked as dirty in the fields section
      expect(fields.getField('dc:checkboxForCreate').isDirty()).to.eventually.be.true;

      // extract the details and attributes for the created field
      fields.getField('dc:checkboxForCreate').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // save the actual fields section
      ModelTestUtils.saveSection(fields);

      // the field type should not be dirty after save
      details.getAttribute('typeOption').then(attr => {
        expect(attr.isDirty()).to.eventually.be.false;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Булева стойност');
      });
    });
  });

  it('should be able to create field and retain entered code list & code value attributes', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 3, true);

    dialog.getAttribute('typeOption').then(attr => {
      // select the type of field
      let field = attr.getField();
      field.selectFromMenu(null, 4, true);

      // proceed with code list selection
      return dialog.getAttribute('codeList');
    }).then(attr => {
      // select the code list
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // proceed with the value selection
      return dialog.getAttribute('value');
    }).then(attr => {
      // select a value from the provided dropdown representing the code value
      return attr.getField().then(field => field.selectFromMenu(null, 1, true));
    }).then(() => {
      // save and cancel controls on the actual dialog should not be disabled anymore
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);
      expect(fields.isFieldDisplayed('test:levelForCreate')).to.eventually.be.true;

      // extract the details and attributes for the created field
      fields.getField('test:levelForCreate').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // the field type should be a code list
      details.getAttribute('typeOption').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Номенклатура');
      });

      // code list should be of type project state
      details.getAttribute('codeList').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('1 - Project state');
      });

      // selected code value should be approved
      details.getAttribute('value').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        attr.getField().then(field => expect(field.getSelectedLabel()).to.eventually.eq('Approved'));
      });
    });
  });

  it('should be able to create field and retain entered constraint attribute', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 7, true);

    dialog.getAttribute('typeOption').then(attr => {
      // select the type of field
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // proceed with code list selection
      return dialog.getAttribute('type');
    }).then(attr => {
      // select the code list
      let field = attr.getField();
      field.setValue(null, 123);

      // save and cancel controls on the actual dialog should not be disabled anymore
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);
      expect(fields.isFieldDisplayed('owl:numericForCreate')).to.eventually.be.true;

      // extract the details and attributes for the created field
      fields.getField('owl:numericForCreate').showAttributes();
      return fields.getModelDetails().getBehaviourAttributesPanel();
    }).then(details => {
      // the field type should be marked as numeric
      details.getAttribute('typeOption').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedLabel()).to.eventually.eq('Цифрово');
      });

      // constraint should contain the proper value
      details.getAttribute('type').then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getValue()).to.eventually.eq('123');
      });
    });
  });

  it('should be able to cancel create field and not append it to the definition fields', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);
      expect(attr.isDirty()).to.eventually.be.true;

      // save and cancel controls on the actual dialog should not be disabled anymore
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // add the field from the dialog to the definition
      dialog.getModelControls().getModelCancel().click();
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.false;

      // main section should not be dirty when we cancel
      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should auto select details for the newly created field', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);
      expect(fields.hasSelectedModel()).to.eventually.be.true;
    });
  });

  it('should auto remove selection details for the created field after cancel', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // add the field from the dialog to the definition
      dialog.getModelControls().getModelCancel().click();
      expect(fields.hasSelectedModel()).to.eventually.be.false;
    });
  });

  it('should be able to cancel newly created field after create form has been confirmed', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      // select the field type
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save the dialog and await notification
      return ModelTestUtils.saveSection(dialog);
    }).then(() => {
      ModelTestUtils.canSaveOrCancelFieldsSection(modelData, fields);

      // should have the field present in the list & selected
      expect(fields.hasSelectedModel()).to.eventually.be.true;
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.true;

      // trigger the cancel button for the actual section
      fields.getModelControls().getModelCancel().click();

      // should not have the field present in the list & selected
      expect(fields.hasSelectedModel()).to.eventually.be.false;
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.false;

      ModelTestUtils.cannotSaveOrCancelFieldsSection(modelData, fields);
    });
  });

  it('should clear the form after selected type has changed', () => {
    openPage('en', 'bg', 'MX1001');
    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 8, true);

    dialog.getAttribute('label').then(attr => {
      attr.getField().setValue(null, 'created label');
      expect(attr.isDirty()).to.eventually.be.true;
      return attr;
    }).then(attr => {
      dialog.getPropertySelect().selectFromMenu(null, 7, true);
      expect(attr.getField().getValue()).to.eventually.equal('');
      expect(attr.isDirty()).to.eventually.be.false;
    });
  });

  it('should be able to create field and position it based on the specified order', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      // select the type from drop down
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // get the order attribute for change
      return dialog.getAttribute('order');
    }).then(attr => {
      // modify the order of the field
      attr.getField().setValue(null, 1);
      expect(attr.isDirty()).to.eventually.be.true;

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);

      // get all present fields
      return fields.getFields();
    }).then(fieldList => {
      // should have the created field selected and displayed
      expect(fields.hasSelectedModel()).to.eventually.be.true;
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.true;

      // checkbox field should be first in the list of fields for the definition
      expect(fieldList[0].getLabel()).to.eventually.eq('Checkbox (Create)');
    });
  });

  it('should be able to create field and position it when no order is specified', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 1, true);

    dialog.getAttribute('typeOption').then(attr => {
      // select the type from drop down
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // save the dialog and await saving
      ModelTestUtils.saveSection(dialog);

      // get all present fields
      return fields.getFields();
    }).then(fieldList => {
      // should have the created field selected and displayed
      expect(fields.hasSelectedModel()).to.eventually.be.true;
      expect(fields.isFieldDisplayed('dc:checkboxForCreate')).to.eventually.be.true;

      // checkbox field should be placed after all overridden fields but
      // before all inherited fields inside the list of definition fields
      expect(fieldList[26].getLabel()).to.eventually.eq('Checkbox (Create)');
    });
  });

  it('should show all possible types for the field type when semantic range is not specified', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 4, true);

    // warning message is hidden when the property has a type
    expect(dialog.isMissingPropertyType()).to.eventually.be.false;

    // all possible data types for field must be listed
    dialog.getAttribute('typeOption').then(attr => {
      let field = attr.getField();
      expect(field.getMenuValues()).to.eventually.deep.eq([
        'ALPHA_NUMERIC_TYPE',
        'ALPHA_NUMERIC_FIXED_TYPE',
        'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE',
        'NUMERIC_TYPE',
        'NUMERIC_FIXED_TYPE',
        'FLOATING_POINT_TYPE',
        'FLOATING_POINT_FIXED_TYPE',
        'DATE_TYPE',
        'DATETIME_TYPE',
        'BOOLEAN',
        'CODELIST',
      ]);
    });
  });

  it('should be able to save the form after all mandatory fields are properly populated', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 5, true);

    // warning message is shown when the property has no data type
    expect(dialog.isMissingPropertyType()).to.eventually.be.true;

    dialog.getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
      // select the type from drop down
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);
      expect(attr.isDirty()).to.eventually.be.true;

      // the form should not yet enabled because the field type is still not selected
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // get the type attribute for the field
      return dialog.getAttribute('typeOption');
    }).then(attr => {
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);
      expect(attr.isDirty()).to.eventually.be.true;

      // the form controls should be both enabled after all mandatory fields are populated
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should filter available property types based on existing semantic range', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 6, true);

    dialog.getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
      // select the type from drop down
      let field = attr.getField();

      // since the for this semantic property range is object type
      // then only object type can be selected from the drop down
      expect(field.getMenuValues()).to.eventually.deep.eq([
        'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DefinitionObjectProperty'
      ]);
    })
  });

  it('should show a message that semantic type is missing when such property is selected', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 5, true);

    // warning message is shown when the property has no data type
    expect(dialog.isMissingPropertyType()).to.eventually.be.true;

    // should not be able to save the create form, but cancel control should be enabled
    expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
  });

  it('should hide message for missing type after a proper type has been selected', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 5, true);

    // warning message is shown when the property has no data type
    expect(dialog.isMissingPropertyType()).to.eventually.be.true;

    dialog.getAttribute('http://www.w3.org/1999/02/22-rdf-syntax-ns#type').then(attr => {
      // select the type from drop down
      let field = attr.getField();
      field.selectFromMenu(null, 1, true);

      // warning message is hidden when the property type is selected
      expect(dialog.isMissingPropertyType()).to.eventually.be.false;

      // the form should not yet enabled because the field type is still not selected
      expect(dialog.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(dialog.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
    });
  });

  it('should properly resolve default values for create for single valued attributes', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 8, true);

    dialog.getAttribute('displayType').then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.getField().getSelectedValue()).to.eventually.equal('EDITABLE');
    });
  });

  it('should properly resolve default values for create for multi valued attributes', () => {
    openPage('en', 'bg', 'MX1001');

    let dialog = fields.createField();
    dialog.getPropertySelect().selectFromMenu(null, 8, true);

    dialog.getAttribute('definition').then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.getField().getValue()).to.eventually.eq('Field create definition');
    });
  });
});