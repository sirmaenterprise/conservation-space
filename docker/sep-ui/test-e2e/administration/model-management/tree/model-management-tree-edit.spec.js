'use strict';

let ModelManagementSandbox = require('../model-management.js').ModelManagementSandbox;

describe('Models management tree - edit', () => {

  let tree;
  let modelData;
  let general;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    tree = sandbox.getModelTree();
    modelData = sandbox.getModelData();
    general = modelData.getGeneralSection();
  }

  it('should properly mark the given tree node model when edit and cancel are executed', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier');
      expect(tree.isNodeModified('entity')).to.eventually.be.true;

      general.getModelControls().getModelCancel().click();

      expect(tree.isNodeModified('entity')).to.eventually.be.false;
    });
  });

  it('should properly mark the given tree node model when edit and save are executed', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier');
      expect(tree.isNodeModified('entity')).to.eventually.be.true;

      general.getModelControls().getModelSave().click();

      expect(tree.isNodeModified('entity')).to.eventually.be.false;
    });
  });

  it('should properly expand given tree node model when navigating', () => {
    openPage('en', 'bg', 'MX1001');

    // collapse entity node
    tree.toggleNode('Entity');

    let fields = modelData.getFieldsSection();
    fields.getField('description').showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().navigateToParent();

    // should select the node to which navigation was performed
    expect(tree.isNodeSelected('entity')).to.eventually.be.true;
  });

  it('should properly mark the given tree node model as selected when navigating', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Abstract');

    let fields = modelData.getFieldsSection();
    fields.toggleInherited();
    fields.getProperty('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#title').showAttributes();
    fields.getModelDetails().getBehaviourAttributesPanel().navigateToParent();

    // should select the node to which navigation was performed
    expect(tree.isNodeSelected('Entity')).to.eventually.be.true;

    // should deselect the node from which navigation was performed
    expect(tree.isNodeSelected('Abstract')).to.eventually.be.false;
  });

  it('should properly resolve the node state after attribute value is modified and reverted', () => {
    openPage('en', 'bg', 'EO1001');

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier');
      expect(tree.isNodeModified('entity')).to.eventually.be.true;

      // reverting changes manually back to old value
      attr.getField().setValue(null, 'EO1001');

      expect(tree.isNodeModified('entity')).to.eventually.be.false;
    });
  });

  it('should modify the name of the node according to user made changes', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, 'New-node-name');

      // name of the node is changed so we need to access it by it's new changed name
      expect(tree.getNode('New-node-name').getText()).to.eventually.eq('New-node-name');
      expect(tree.isNodeModified('New-node-name')).to.eventually.be.true;

      // reverting changes manually back to old value
      attr.getField().setValue(null, 'Entity');

      // name of the node is changed back to old value access by that
      expect(tree.getNode('Entity').getText()).to.eventually.eq('Entity');
      expect(tree.isNodeModified('Entity')).to.eventually.be.false;
    });
  });

  it('should modify and mark multiple models when edited', () => {
    openPage('en', 'bg', 'EO1001');

    general.getClassAttribute('http://purl.org/dc/terms/title').then(attr => {
      attr.getField().setValue(null, 'New-node-name');

      // name of the node is changed so we need to access by changed name
      expect(tree.isNodeModified('New-node-name')).to.eventually.be.true;
    });

    general.getDefinitionAttribute('identifier').then(attr => {
      attr.getField().setValue(null, 'new-identifier');

      // id of the node is changed so we can validate it is modified
      expect(tree.isNodeModified('entity')).to.eventually.be.true;
    });
  });
});