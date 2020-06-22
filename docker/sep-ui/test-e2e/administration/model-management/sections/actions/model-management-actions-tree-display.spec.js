'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let ActionsTree = require('./model-actions-section').ActionsTree;

describe('Models management actions section', () => {
  let actionsSection;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);
    modelData = sandbox.getModelData();
  }

  describe('display actions tree inheritance', () => {
    it('should show all definition actions', () => {
      let actions = [
        'lock',
        'Create survey',
        'Export to PDF',
        'Export to Word',
        'Print',
        'Export tab to PDF',
        'Export tab to Word',
        'Print tab',
        'Add thumbnail',
        'Add attachment',
        'Move',
        'Delete',
        'Edit',
        'Manage permissions',
        'saveAsTemplate',
        'Change template',
        'create'
      ];

      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      actions.forEach(action => {
        expect(tree.isPresent(action)).to.be.true;
      });
    });

    it('should show all definition action groups', () => {
      let groups = [
        'Object management',
        'Change status',
        'Update',
        'Add relation'
      ];

      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      groups.forEach(group => {
        expect(tree.isPresent(group)).to.be.true;
      });
    });

    it('should show all inherited actions', () => {
      let actions = [
        'Start',
        'Approve',
        'Stop',
        'Suspend',
        'Restart',
        'Complete',
        'Compose email'
      ];

      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      actions.forEach(action => {
        expect(tree.isPresent(action)).to.be.true;
      });
    });

    it('should not show child definition\'s actions', () => {
      let childDefinitionActions = [
        'lock',
        'createSurvey',
        'exportPDF',
        'exportWord',
        'print',
        'exportTabPDF',
        'exportTabWord',
        'printTab',
        'editDetails',
        'move',
        'delete',
        'addThumbnail',
        'addAttachments',
        'managePermissions',
        'saveAsTemplate',
        'changeTemplate',
        'updateTemplate',
        'create'
      ];

      openPage('en', 'en', 'EO1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      childDefinitionActions.forEach(childDefinitionActionId => {
        tree.nodeIsStale(childDefinitionActionId);
      });
    });
  });

  describe('display actions tree visualization', () => {
    it('should show actions and groups in a root group', () => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();
      let tree = actionsSection.getActionsTree();
      expect(tree.isNodeExpanded(ActionsTree.ROOT_ACTION_GROUP_LABEL)).to.eventually.be.true;
      expect(tree.isNodeExpanded('Object management')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Change status')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Update')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Add relation')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Export')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Export tab')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Empty group')).to.eventually.be.false;
    });

    it('should show empty leaf when no actions available', (done) => {
      openPage('en', 'en', 'MX1000');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();
      let tree = actionsSection.getActionsTree();
      expect(tree.isNodeExpanded(ActionsTree.ROOT_ACTION_GROUP_LABEL)).to.eventually.be.false;
      tree.getNodeChildren(tree.selectNode(ActionsTree.ROOT_ACTION_GROUP_LABEL)).then(nodes => {
        expect(nodes.length).to.eq(0);
        done();
      });
    });

    it('should show actions and groups in a root group translated', () => {
      openPage('bg', 'bg', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();
      let tree = actionsSection.getActionsTree();
      expect(tree.isNodeExpanded('Списък с операции')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Управление на обекти')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Промяна на състояние')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Обнови')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Добавяне на връзка')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Експортиране')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Експортиране на секция')).to.eventually.be.true;
      expect(tree.isNodeExpanded('Празна група')).to.eventually.be.false;
    });
  });

  describe('display actions tree search', () => {
    it('should expand tree when query is empty', () => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();
      let tree = actionsSection.getActionsTree();
      tree.search('no such action');
      expect(tree.isNodeExpanded(ActionsTree.ROOT_ACTION_GROUP_LABEL)).to.eventually.be.false;
      tree.search('');
      expect(tree.isNodeExpanded(ActionsTree.ROOT_ACTION_GROUP_LABEL)).to.eventually.be.true;
    });
  });

  it('should show searched node when tree is queried', () => {
    let actions = [
      'Start',
      'Approve',
      'Stop',
      'Suspend',
      'Restart',
      'Complete'
    ];

    openPage('en', 'en', 'MX1001');
    actionsSection = modelData.getActionsSection();
    actionsSection.waitUntilOpened();
    let tree = actionsSection.getActionsTree();
    actions.forEach(action => {
      tree.search(action);
      expect(tree.isNodeExpanded('Change status')).to.eventually.be.true;
    });
  });

  describe('display actions tree order', () => {
    it('should show all actions', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode(ActionsTree.ROOT_ACTION_GROUP_LABEL)).then(nodes => {
        expect(nodes.length).to.eq(32);
        done();
      });
    });

    it('should order properly actions with same order as described in definition', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode('Add relation')).then(nodes => {
        expect(nodes.length).to.eq(2);
        expect(nodes[0].getText()).to.eventually.equal('Add thumbnail');
        expect(nodes[1].getText()).to.eventually.equal('Add attachment');
        done();
      });
    });

    it('should order properly actions and groups with same order on root level', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode(ActionsTree.ROOT_ACTION_GROUP_LABEL)).then(nodes => {
        expect(nodes.length).to.eq(32);
        expect(nodes[0].getText()).to.eventually.equal('lock');
        expect(nodes[1].getText()).to.eventually.include('Object management');
        done();
      });
    });

    it('should order properly actions and groups with same order inside group', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode('Object management')).then(nodes => {
        expect(nodes.length).to.eq(13);
        expect(nodes[1].getText()).to.eventually.equal('Approve');
        expect(nodes[0].getText()).to.eventually.include('Change status');
        done();
      });
    });

    it('should order properly actions and groups with same order inside nested groups', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode('Update')).then(nodes => {
        expect(nodes.length).to.eq(6);
        expect(nodes[0].getText()).to.eventually.equal('Edit');
        expect(nodes[1].getText()).to.eventually.include('Add relation');
        expect(nodes[2].getText()).to.eventually.equal('Add thumbnail');
        expect(nodes[3].getText()).to.eventually.equal('Add attachment');
        expect(nodes[4].getText()).to.eventually.equal('Move');
        expect(nodes[5].getText()).to.eventually.equal('Delete');
        done();
      });
    });

    it('should order properly actions and groups with no order at the end of the parent level', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.getNodeChildren(tree.selectNode(ActionsTree.ROOT_ACTION_GROUP_LABEL)).then(nodes => {
        expect(nodes.length).to.eq(32);
        expect(nodes[26].getText()).to.eventually.equal('saveAsTemplate');
        expect(nodes[27].getText()).to.eventually.equal('Compose email');
        expect(nodes[28].getText()).to.eventually.equal('Change template');
        expect(nodes[29].getText()).to.eventually.equal('Update template');
        expect(nodes[30].getText()).to.eventually.equal('Start (entity)');
        expect(nodes[31].getText()).to.eventually.equal('Empty group (entity)');
        done();
      });
    });

    it('should display parent when action is inherited', (done) => {
      openPage('en', 'en', 'MX1001');
      actionsSection = modelData.getActionsSection();
      actionsSection.waitUntilOpened();

      let tree = actionsSection.getActionsTree();
      tree.openNode('Start');
      actionsSection.getModelActionAttribute('label').then(attr => {
        expect(attr.getField().getValue()).to.eventually.eq('Start');
        expect(tree.getNode('Start').getText()).to.eventually.eq('Start (entity)');
        done();
      });
    });
  });
});