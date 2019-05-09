'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management general section - editing', () => {

  let general;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    general = modelData.getGeneralSection();
  }

  it('should mark the section as modified when changes to the form are being made', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier');
      expect(modelData.isGeneralSectionModified()).to.eventually.be.true;

      // Revert the modified values
      general.getModelControls().getModelCancel().click();

      // Should not mark the section as modified
      expect(modelData.isGeneralSectionModified()).to.eventually.be.false;
    });
  });

  it('should mark the section as modified when editing through language dialog', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      let dialog = attr.getValuesDialog();
      let enTitle = dialog.getField(2);

      // enter new value through the popup
      enTitle.setValue(null, 'New-Title');
      expect(modelData.isGeneralSectionModified()).to.eventually.be.true;

      // restore the old title value
      enTitle.setValue(null, 'Entity');
      expect(modelData.isGeneralSectionModified()).to.eventually.be.false;
    });
  });

  it('should mark the section as not modified after saving changes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'some-value');

      // before save the section should be properly marked as modified
      expect(modelData.isGeneralSectionModified()).to.eventually.be.true;

      general.getModelControls().getModelSave().click();

      // after save section should not be marked as modified
      expect(modelData.isGeneralSectionModified()).to.eventually.be.false;
    });
  });

  it('should pop notification after saving changes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'some-value');

      general.getModelControls().getModelSave().click();
      expect(general.getModelControls().getModelSave().getNotification().isSuccess()).to.eventually.be.true;
    });
  });

  it('should have both cancel and save changes button action controls disabled when no changes are made', () => {
    openPage('en', 'bg', 'EO1001');
    cannotSaveOrCancel();
  });

  it('should have both cancel and save changes button action controls enabled when changes are made', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'some-value');
      canSaveOrCancel();
    });
  });

  it('should disable cancel and save when attribute is returned back to original value', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'some-value');
      canSaveOrCancel();

      attr.getField().setValue(null, 'EO1001');
      cannotSaveOrCancel();
    });
  });

  it('should disable section from controls while saving', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'some-value');
      canSaveOrCancel();

      general.getModelControls().getModelSave().click();
      cannotSaveOrCancel();
    });
  });

  it('should have attributes disabled as provided in the meta model', () => {
    openPage('en', 'bg', 'EO1001');
    general.getDefinitionAttribute('label')
      .then(attr => expect(attr.getField().isReadOnly()).to.eventually.be.true);
  });

  it('should be able to clear special multi valued attributes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, '');
      // should properly modify the name of the class accordingly
      expect(general.getClassName()).to.eventually.equal('');
    });
  });

  it('should be able to edit special multi valued attributes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, 'new-label');
      // should properly modify the name of the class accordingly
      expect(general.getClassName()).to.eventually.equal('new-label');
    });
  });

  it('should be able to edit standard multi valued attributes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/description').then(attr => {
      let dialog = attr.getValuesDialog();
      let enDescription = dialog.getField(2);

      // enter new value through the popup dialog
      enDescription.setValue(null, 'new-description');
      expect(enDescription.getValue()).to.eventually.eq('new-description');

      canSaveOrCancel();
    });
  });

  // Validation

  it('should prevent saving if class mandatory attributes are empty', () => {
    // Open a semantic model -> see that validation works without a definition model
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, '');
      expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(general.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(attr.isDirty()).to.eventually.be.true;

      // Restore original value -> see that validation is properly triggered
      attr.getField().setValue(null, 'Entity');
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.isDirty()).to.eventually.be.false;
    });
  });

  it('should perform validation for multi language attributes even if modified through the values dialog', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      let titleDialog = attr.getValuesDialog();
      titleDialog.getField(2).setValue(null, '');

      // should mark the invalid value properly in the dialog
      expect(titleDialog.isFieldInvalid(2)).to.eventually.be.true;

      titleDialog.close();

      // should validate the main components after the values dialog has been closed
      expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should not validate values for multi language attributes that are not in the current language', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      let titleDialog = attr.getValuesDialog();
      let bgDescription = titleDialog.getField(0);
      bgDescription.setValue(null, '');
      titleDialog.close();

      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should validate attribute when another attribute which is affecting it has been edited', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    general.getClassAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isCreatable').then(attr => {
      attr.getField().toggleCheckbox();
      expect(attr.getField().isSelected()).to.eventually.be.true;

      general.getClassAttribute('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#category').then(attr => {
        expect(attr.isInvalid()).to.eventually.be.true;
        expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;
        expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      });
    });
  });

  it('should prevent saving if definition mandatory attributes are empty', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      // Another value to enable the buttons
      attr.getField().setValue(null, 'new value');
      expect(attr.isInvalid()).to.eventually.be.false;
      canSaveOrCancel();

      // Clear the value to disable the save button, cancel should be possible tho
      attr.getField().setValue(null, '');
      expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
      expect(general.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;

      // Should have validation errors
      expect(attr.isDirty()).to.eventually.be.true;
      expect(attr.isInvalid()).to.eventually.be.true;
      expect(attr.getValidationMessages().hasValidationRuleError()).to.eventually.be.true;

      // Should enable save after valid value is provided
      attr.getField().setValue(null, 'valid value');
      expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
      expect(attr.isDirty()).to.eventually.be.true;
    });
  });

  it('should re-validate after changes are cancelled', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, '');
      expect(attr.isDirty()).to.eventually.be.true;
      expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;

      // Revert the modified values
      general.getModelControls().getModelCancel().click();

      // Should enable save after revert
      expect(attr.isDirty()).to.eventually.be.false;
      expect(attr.isInvalid()).to.eventually.be.false;
    });
  });

  function canSaveOrCancel() {
    // form is now dirty and both save and cancel are enabled
    expect(modelData.isGeneralSectionModified()).to.eventually.be.true;
    expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
    expect(general.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
  }

  function cannotSaveOrCancel() {
    // form not dirty and both save and cancel are disabled
    expect(modelData.isGeneralSectionModified()).to.eventually.be.false;
    expect(general.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(general.getModelControls().getModelCancel().isDisabled()).to.eventually.be.true;
  }
});