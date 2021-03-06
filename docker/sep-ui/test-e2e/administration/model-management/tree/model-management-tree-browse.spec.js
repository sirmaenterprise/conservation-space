'use strict';

let ModelManagementSandbox = require('../model-management.js').ModelManagementSandbox;

describe('Models management tree - browsing', () => {

  let tree;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    tree = sandbox.getModelTree();
    modelData = sandbox.getModelData();
  }

  it('should list a tree of models ready to be browsed', () => {
    openPage();
    expect(tree.getNode('Entity').isDisplayed()).to.eventually.be.true;
  });

  it('should have the search option for nodes enabled', () => {
    openPage();
    expect(tree.isSearchEnabled()).to.eventually.be.true;
  });

  it('should have the root node expanded by default', () => {
    openPage();
    expect(tree.getNode('Entity').isExpanded()).to.eventually.be.true;
  });

  it('should provide model id as a query parameter when a node is clicked', () => {
    openPage();
    tree.search('Situation');
    tree.getNode('Situation').openObject();
    expect(sandbox.isModelProvided('http://www.ontotext.com/proton/protontop#Situation')).to.eventually.be.true;
  });

  it('should directly navigate and expand to a provided model', () => {
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Situation');
    expect(tree.getNode('Situation').isDisplayed()).to.eventually.be.true;
  });

  it('should be able to search nodes by name when typing in search field', () => {
    openPage();
    tree.search('Situation');
    expect(tree.getNode('Situation').isDisplayed()).to.eventually.be.true;
  });

  it('should be able to search nodes when arbitrary language is provided', () => {
    openPage('bg');
    tree.search('Ситуация');
    expect(tree.getNode('Ситуация').isDisplayed()).to.eventually.be.true;
  });

  it('should expand the root node when search query is cleared', () => {
    openPage();
    tree.search('Situation');
    expect(tree.getNode('Situation').isDisplayed()).to.eventually.be.true;

    tree.search('');
    expect(tree.getNode('Entity').isExpanded()).to.eventually.be.true;
  });

  it('should display the entire hierarchy inside the tree', () => {
    openPage();
    expandAndAssertTree(tree, {
      id: 'Entity',
      children: [{
        id: 'entity',
      }, {
        id: 'Abstract',
      }, {
        id: 'Happening',
        children: [{
          id: 'Event',
        }, {
          id: 'Situation',
        }, {
          id: 'Time Interval',
        }]
      }]
    });
  });

  it('should list the tree in the specified user language', () => {
    openPage('bg');
    expandAndAssertTree(tree, {
      id: 'Обект',
      children: [{
        id: 'обект',
      }, {
        id: 'Абстракт',
      }, {
        // fallback EN
        id: 'Happening',
        children: [{
          id: 'Събитие',
        }, {
          id: 'Ситуация',
        }, {
          id: 'Времеви интервал',
        }]
      }]
    });
  });

  function expandAndAssertTree(tree, hierarchy) {
    if (Array.isArray(hierarchy)) {
      hierarchy.forEach(node => expandAndAssertTree(tree, node));
    } else {
      let root = tree.getNode(hierarchy.id);
      root.isDisplayed().then(state => {
        expect(state).to.be.true;
        root.isExpanded().then(expanded => !expanded && root.expand());
        hierarchy.children && expandAndAssertTree(tree, hierarchy.children);
      });
    }
  }
});