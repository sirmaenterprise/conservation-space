'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management actions section - editing', () => {
  let actionsSection;
  let modelData;
  let sandbox;
  let tree;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);
    modelData = sandbox.getModelData();
    actionsSection = modelData.getActionsSection();
    actionsSection.waitUntilOpened();
    tree = actionsSection.getActionsTree();
  }

  describe('Actions', () => {
    it('should remove parent from tree label when action is edited', (done) => {
      openPage('en', 'en', 'MX1001');
      tree.openNode('Start');
      let node = tree.getNode('Start');
      expect(node.getText()).to.eventually.eq('Start (entity)');
      actionsSection.getModelActionAttribute('label').then(attr => {
        attr.getField().setValue(null, 'StartEdited');
        expect(node.getText()).to.eventually.eq('StartEdited');
        done();
      });
    });

    it('should be able to save if attribute is edited', (done) => {
      openPage('en', 'en', 'MX1001');
      let node = "Approve";
      tree.openNode(node);
      actionsSection.getModelActionAttribute('tooltipId').then(attr => {
        expect(attr.getField().getValue()).to.eventually.eq('approve.tooltip');
        cannotSaveOrCancel(node);

        attr.getField().setValue(null, 'edited');
        canSaveOrCancel(node);

        attr.getField().setValue(null, 'approve.tooltip');
        cannotSaveOrCancel("Approve");
        done();
      });
    });

    it('should rearange tree when action order is edited', (done) => {
      openPage('en', 'en', 'MX1001');

      tree.getNodeChildren(tree.selectNode('Change status')).then(nodes => {
        expect(nodes.length).to.eq(5);
        expect(nodes[0].getText()).to.eventually.equal('Approve');
        tree.openNode('Approve');
        return actionsSection.getModelActionAttribute('order').then(attr => {
          return attr.getField().setValue(null, '9');
        })
      }).then(() => {
        tree.getNodeChildren(tree.selectNode('Change status')).then(nodes => {
          expect(nodes.length).to.eq(5);
          expect(nodes[0].getText()).to.eventually.equal('Stop');
          expect(nodes[4].getText()).to.eventually.equal('Approve');
          done();
        });
      });
    });
  });

  describe('Action Groups', () => {
    it('should rearange tree when action group is changed', () => {
      openPage('en', 'en', 'MX1001');
      tree.getNodeChildren(tree.selectNode('Change status')).then(nodes => {
        expect(nodes.length).to.eq(5);
        expect(getNodeIndex(nodes, 'Approve')).to.eventually.equal(0);
        expect(getNodeIndex(nodes, 'Stop')).to.eventually.equal(1);
        tree.openNode('Approve');
        return actionsSection.getModelActionAttribute('group').then(attr => {
          return attr.getField().setValue(null, 'empty');
        })
      }).then(() => {
        tree.getNodeChildren(tree.selectNode('Change status')).then(nodes => {
          expect(nodes.length).to.eq(4);
          expect(getNodeIndex(nodes, 'Stop')).to.eventually.equal(0);
          expect(getNodeIndex(nodes, 'Approve')).to.eventually.equal(-1);
        });
        tree.getNodeChildren(tree.selectNode('Empty group')).then(nodes => {
          expect(nodes.length).to.eq(1);
          expect(nodes[0].getText()).to.eventually.equal('Approve');
        });
      });
    });

    it('should remove parent from tree label when action is edited', () => {
      openPage('en', 'en', 'MX1001');
      tree.openNode('Empty group');
      let node = tree.getNode('Empty group');
      expect(node.getText()).to.eventually.eq('Empty group (entity)');
      actionsSection.getModelActionAttribute('label').then(attr => {
        attr.getField().setValue(null, 'Empty group Edited');
        expect(node.getText()).to.eventually.eq('Empty group Edited');
      });
    });

    it('should be able to save if group attribute is edited', () => {
      openPage('en', 'en', 'MX1001');
      let node = "Update";
      tree.openNode(node);
      actionsSection.getModelActionAttribute('labelId').then(attr => {
        expect(attr.getField().getValue()).to.eventually.eq('objectManagementUpdate.group.label');
        cannotSaveOrCancel(node);

        attr.getField().setValue(null, 'edited');
        canSaveOrCancel(node);

        attr.getField().setValue(null, 'objectManagementUpdate.group.label');
        cannotSaveOrCancel(node);
      });
    });

    it('should rearrange tree when group order is edited', () => {
      openPage('en', 'en', 'MX1001');

      tree.getNodeChildren(tree.selectNode('Update')).then(nodes => {
        expect(nodes.length).to.eq(6);
        expect(nodes[1].getText()).to.eventually.equal('Add relation\nAdd thumbnail\nAdd attachment');
        tree.openNode('Add relation');
        actionsSection.getModelActionAttribute('order').then(attr => {
          return attr.getField().setValue(null, '9');
        })
      }).then(() => {
        tree.getNodeChildren(tree.selectNode('Update')).then(nodes => {
          expect(nodes.length).to.eq(6);
          expect(nodes[3].getText()).to.eventually.equal('Add relation\nAdd thumbnail\nAdd attachment');
        });
      });
    });

    it('should rearrange tree when group parent is changed', () => {
      openPage('en', 'en', 'MX1001');
      tree.getNodeChildren(tree.selectNode('Add relation')).then(nodes => {
        expect(nodes.length).to.eq(2);
        expect(getNodeIndex(nodes, 'Empty group')).to.eventually.equal(-1);
        tree.openNode('Empty group');
        actionsSection.getModelActionAttribute('parent').then(attr => {
          attr.getField().setValue(null, 'objectManagementAddRelation');
        })
      }).then(() => {
        tree.getNodeChildren(tree.selectNode('Add relation')).then(nodes => {
          expect(nodes.length).to.eq(3);
          expect(nodes[2].getText()).to.eventually.equal('Empty group');
        });
      });
    });

    it('should not set old value for group attribute when it gets the parent value', () => {
      openPage('en', 'en', 'MX1001');

      // Stop action`s group attribute is not inherited
      tree.openNode('Stop');
      actionsSection.getModelActionAttribute('group').then(attr => {
        // When I set the value of the parent attribute
        attr.getField().setValue(null, '');

        // Expect the value to be not restored to its old value
        expect(attr.getField().getValue()).to.eventually.equal('');
      });
      // expect the Stop action to be child of the root node
      tree.getNodeChildren(tree.selectNode('Actions list')).then(nodes => {
        expect(getNodeIndex(nodes, 'Stop')).to.eventually.equal(15);
      });
    });
  });

  function canSaveOrCancel(node) {
    expect(modelData.isActionsSectionModified()).to.eventually.be.true;
    expect(actionsSection.getActionsTree().isNodeModified(node)).to.eventually.be.true;
    expect(actionsSection.getModelControls().getModelSave().isDisabled()).to.eventually.be.false;
    expect(actionsSection.getModelControls().getModelCancel().isDisabled()).to.eventually.be.false;
  }

  function cannotSaveOrCancel(node) {
    expect(modelData.isActionsSectionModified()).to.eventually.be.false;
    expect(actionsSection.getActionsTree().isNodeModified(node)).to.eventually.be.false;
    expect(actionsSection.getModelControls().getModelSave().isDisabled()).to.eventually.be.true;
    expect(actionsSection.getModelControls().getModelCancel().isDisabled()).to.eventually.be.true;
  }

  function getNodeIndex(nodes, nodeText) {
    let nodesTextIndex = [];
    for (let i = 0; i < nodes.length; i++) {
      nodesTextIndex.push(nodes[i].getText());
    }
    return Promise.all(nodesTextIndex).then(results => results.indexOf(nodeText));
  }
});