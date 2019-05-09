'use strict';

let ModelData = require('../model-management.js').ModelData;
let Button = require('../../../form-builder/form-control').Button;
let ModelManagementSandbox = require('../model-management.js').ModelManagementSandbox;

describe('Models management page - browsing', () => {

  let sandbox;
  let modelData;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
  }

  it('should show a message prompting to select a model when a model is not selected', () => {
    openPage();
    expect(modelData.isModelSelectMessageDisplayed()).to.be.true;
  });

  it('should show a brief loading message when the model is in the process of loading', () => {
    openPage('en', 'bg', 'EO1001');
    expect(modelData.isModelLoadingMessageDisplayed()).to.be.true;
  });

  it('should display the deploy button as enabled', () => {
    openPage('en', 'bg', 'EO1001');
    expect(modelData.getDeployControl().isEnabled()).to.eventually.be.true;
  });

  it('should display the model section tabs', () => {
    openPage();
    expect(modelData.isDisplayed()).to.eventually.be.true;
    expect(modelData.getTabs().count()).to.eventually.eq(4);
  });

  it('should display general section tab', () => {
    openPage();
    expect(modelData.getTab(ModelData.GENERAL_SECTION).getLabel()).to.eventually.eq('General');
  });

  it('should display fields section tab', () => {
    openPage();
    expect(modelData.getTab(ModelData.FIELDS_SECTION).getLabel()).to.eventually.eq('Fields');
  });

  it('should display models for deployment with proper selection mode', () => {
    openPage();
    expect(modelData.getDeployPanel().getModels().isMultiSelection()).to.eventually.be.true;
  });

  it('should display correct model names & identifiers for deployment', () => {
    openPage();
    modelData.getDeployPanel().getModels().getItems().then(items => {
      expect(items.length).to.eq(4);
      expect(items[0].getName()).to.eventually.eq('Abstract');
      expect(items[1].getName()).to.eventually.eq('Entity');
      expect(items[2].getName()).to.eventually.eq('Media');
      expect(items[3].getName()).to.eventually.eq('No Actions');

      expect(items[0].getIdentifier()).to.eventually.eq('(http://www.ontotext.com/proton/protontop#Abstract)');
      expect(items[1].getIdentifier()).to.eventually.eq('(http://www.ontotext.com/proton/protontop#Entity)');
      expect(items[2].getIdentifier()).to.eventually.eq('(MX1001)');
      expect(items[3].getIdentifier()).to.eventually.eq('(MX1000)');
    });
  });

  it('should display current model as checked if part of the deployment list', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    modelData.getDeployPanel().getModels().getItems().then(items => {
      expect(items.length).to.eq(4);

      expect(items[0].getName()).to.eventually.eq('Abstract');
      expect(items[0].isSelected()).to.eventually.be.false;

      expect(items[1].getName()).to.eventually.eq('Entity');
      expect(items[1].isSelected()).to.eventually.be.true;

      expect(items[2].getName()).to.eventually.eq('Media');
      expect(items[2].isSelected()).to.eventually.be.false;

      expect(items[3].getName()).to.eventually.eq('No Actions');
      expect(items[3].isSelected()).to.eventually.be.false;
    });
  });

  it('should select the type in the deployment list when current model is definition', () => {
    openPage('en', 'bg', 'MX1001');
    modelData.getDeployPanel().getModels().getItems().then(items => {
      expect(items.length).to.eq(4);

      expect(items[0].getName()).to.eventually.eq('Abstract');
      expect(items[0].isSelected()).to.eventually.be.true;

      expect(items[1].getName()).to.eventually.eq('Entity');
      expect(items[1].isSelected()).to.eventually.be.false;

      expect(items[2].getName()).to.eventually.eq('Media');
      expect(items[2].isSelected()).to.eventually.be.true;

      expect(items[1].getName()).to.eventually.eq('Entity');
      expect(items[1].isSelected()).to.eventually.be.false;
    });
  });

  it('should be able to select and deselect all models for deployment', () => {
    openPage();
    let models = modelData.getDeployPanel().getModels();

    models.getItems().then(items => {
      models.selectAll();
      expect(items.length).to.eq(4);
      expect(items[0].isSelected()).to.eventually.be.true;
      expect(items[1].isSelected()).to.eventually.be.true;
      expect(items[2].isSelected()).to.eventually.be.true;
      expect(items[3].isSelected()).to.eventually.be.true;
      return items;
    }).then(items => {
      models.deselectAll();
      expect(items.length).to.eq(4);
      expect(items[0].isSelected()).to.eventually.be.false;
      expect(items[1].isSelected()).to.eventually.be.false;
      expect(items[2].isSelected()).to.eventually.be.false;
      expect(items[3].isSelected()).to.eventually.be.false;
    });
  });

  it('should enable or disable the confirm button on deployment dialog', () => {
    openPage();
    let panel = modelData.getDeployPanel();
    let models = panel.getModels();
    let confirm = new Button(panel.getOkButton());

    models.getItems().then(items => {
      models.selectAll();
      expect(items.length).to.eq(4);
      expect(confirm.isEnabled()).to.eventually.be.true;
      return items;
    }).then(items => {
      models.deselectAll();
      expect(items.length).to.eq(4);
      expect(confirm.isDisabled()).to.eventually.be.true;
    });
  });

  it('should pop notification after deployment has completed', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');

    modelData.getDeployPanel().ok();
    expect(modelData.getDeployControl().getNotification().isSuccess()).to.eventually.be.true;
  });

  it('should display error dialog when save models fail', () => {
    sandbox = new ModelManagementSandbox();
    sandbox.open('en', 'en', 'EO1001', 'FAIL_SAVE_CONFLICT');

    modelData = sandbox.getModelData();
    let generalSection = modelData.getGeneralSection();

    generalSection.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier 1');
      generalSection.getModelControls().getModelSave().click();

      let dialog = modelData.getErrorsDialog();
      expect(dialog.isPresent()).to.eventually.be.true;
      expect(dialog.getTitleText()).to.eventually.eq('Changes to the current model could not be saved. Please, reload page and enter changes again.');
    });
  });

  it('should display error dialog when there are invalid models during save', () => {
    sandbox = new ModelManagementSandbox();
    sandbox.open('en', 'en', 'EO1001', 'FAIL_SAVE');
    modelData = sandbox.getModelData();
    let generalSection = modelData.getGeneralSection();

    generalSection.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier 1');
      generalSection.getModelControls().getModelSave().click();

      let dialog = modelData.getSaveFailedDialog();
      expect(dialog.isPresent()).to.eventually.be.true;
      expect(dialog.getTitleText()).to.eventually.eq('Changes to the current model could not be saved. Please, reload page and enter changes again.');
      dialog.getItems().then(items => {
        let entityNode = items[0];
        expect(entityNode.getName()).to.eventually.eq('Entity');
        expect(entityNode.getIdentifier()).to.eventually.eq('(http://www.ontotext.com/proton/protontop#Entity)');
        let validationLogForEntity = entityNode.getValidationLog();
        validationLogForEntity.getErrors().then(errors => {
          expect(errors.length).to.eq(1);
          expect(errors[0]).to.eventually.eq('model is invalid, there is an error');
        });
        validationLogForEntity.getWarnings().then(warnings => {
          expect(warnings.length).to.eq(1);
          expect(warnings[0]).to.eventually.eq('there is a minor warning');
        });

        let abstractNode = items[1];
        expect(abstractNode.getName()).to.eventually.eq('Abstract');
        expect(abstractNode.getIdentifier()).to.eventually.eq('(http://www.ontotext.com/proton/protontop#Abstract)');
        let validationLogForAbstract = abstractNode.getValidationLog();
        validationLogForAbstract.getWarnings().then(warnings => {
          expect(warnings.length).to.eq(1);
          expect(warnings[0]).to.eventually.eq('there is a minor warning');
        });
        expect(validationLogForAbstract.hasErrors()).to.eventually.be.false;

        let mediaNode = items[2];
        expect(mediaNode.getName()).to.eventually.eq('Media');
        expect(mediaNode.getIdentifier()).to.eventually.eq('(MX1001)');
        let validationLogForMedia = mediaNode.getValidationLog();
        expect(validationLogForMedia.hasWarnings()).to.eventually.be.false;
        validationLogForMedia.getErrors().then(errors => {
          expect(errors.length).to.eq(1);
          expect(errors[0]).to.eventually.eq('model is invalid, there is an error');
        });
      });
    });
  });

  it('should display validation log if available below each model', () => {
    sandbox = new ModelManagementSandbox();
    sandbox.open('en', 'en', 'EO1001', 'SUCCESS_SAVE', 'FAIL_PUBLISH');

    modelData = sandbox.getModelData();
    modelData.getDeployPanel().getModels().getItems().then(items => {

      let validationLogForAbstract = items[0].getValidationLog();
      validationLogForAbstract.getWarnings().then(warnings => {
        expect(warnings.length).to.eq(1);
        expect(warnings[0]).to.eventually.eq('there is a minor warning');
      });
      expect(validationLogForAbstract.hasErrors()).to.eventually.be.false;

      let validationLogForEntity = items[1].getValidationLog();
      validationLogForEntity.getErrors().then(errors => {
        expect(errors.length).to.eq(1);
        expect(errors[0]).to.eventually.eq('model is invalid, there is an error');
      });
      validationLogForEntity.getWarnings().then(warnings => {
        expect(warnings.length).to.eq(1);
        expect(warnings[0]).to.eventually.eq('there is a minor warning');
      });

      let validationLogForMedia = items[2].getValidationLog();
      expect(validationLogForMedia.hasWarnings()).to.eventually.be.false;
      validationLogForMedia.getErrors().then(errors => {
        expect(errors.length).to.eq(1);
        expect(errors[0]).to.eventually.eq('model is invalid, there is an error');
      });
    });
  });

  it('should not be able to select model with errors via select all', () => {
    sandbox = new ModelManagementSandbox();
    sandbox.open('en', 'en', 'EO1001', 'SUCCESS_SAVE', 'FAIL_PUBLISH');

    modelData = sandbox.getModelData();
    let panel = modelData.getDeployPanel();
    let models = panel.getModels();
    models.getItems().then(items => {
      models.selectAll();
      expect(items[0].isSelected()).to.eventually.be.true;
      expect(items[1].isSelected()).to.eventually.be.false;
      expect(items[2].isSelected()).to.eventually.be.false;
      expect(items[3].isSelected()).to.eventually.be.true;
    });
  });

  it('should disable checkbox of model with validation errors', () => {
    sandbox = new ModelManagementSandbox();
    sandbox.open('en', 'en', 'EO1001', 'SUCCESS_SAVE', 'FAIL_PUBLISH');

    modelData = sandbox.getModelData();
    let panel = modelData.getDeployPanel();
    let models = panel.getModels();
    models.getItems().then(items => {
      expect(items[0].getControl().isDisabled()).to.eventually.be.false;
      expect(items[1].getControl().isDisabled()).to.eventually.be.true;
      expect(items[2].getControl().isDisabled()).to.eventually.be.true;
      expect(items[3].getControl().isDisabled()).to.eventually.be.false;
    });
  });

  it('should select model with warnings by default', () => {
    sandbox = new ModelManagementSandbox();
    // open model, which has only warnings
    sandbox.open('en', 'en', 'http://www.ontotext.com/proton/protontop#Abstract',
      'SUCCESS_SAVE', 'FAIL_PUBLISH');

    modelData = sandbox.getModelData();
    let panel = modelData.getDeployPanel();
    let models = panel.getModels();
    models.getItems().then(items => {
      // only Abstract model should be selected
      expect(items[0].isSelected()).to.eventually.be.true;
      expect(items[1].isSelected()).to.eventually.be.false;
      expect(items[2].isSelected()).to.eventually.be.false;
      expect(items[3].isSelected()).to.eventually.be.false;
    });
  });
});