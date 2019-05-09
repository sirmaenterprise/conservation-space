'use strict';

let PageObject = require('../../../../page-object').PageObject;
let ObjectBrowser = require('../../../../components/object-browser/object-browser').ObjectBrowser;
let ModelControls = require('../../../../administration/model-management/model-controls').ModelControls;
let hasClass = require('../../../../test-utils').hasClass;

/**
 * Wrapper for the actions tab body. Provides access to rendered actions panels and operates controls in the section.
 *
 * @author T. Dossev
 */
class ModelActionsSection extends PageObject {

  constructor(element, attributeExtractor) {
    super(element);
    this.attributeExtractor = attributeExtractor;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getActionsTree() {
    return new ActionsTree($(ActionsTree.COMPONENT_SELECTOR));
  }

  getModelActionAttribute(id) {
    return this.attributeExtractor.getModelAttribute(id);
  }

  getModelControls() {
    return new ModelControls(this.element.$('.model-controls'));
  }
}

/**
 * PO for interacting with the actions tree functionality.
 *
 * @author T. Dossev
 */
class ActionsTree extends ObjectBrowser {

  isNodeExpanded(node) {
    return this.getNode(node).isExpanded();
  }

  isPresent(node) {
    browser.wait(EC.visibilityOf(this.getNode(node).element), DEFAULT_TIMEOUT);
    return true;
  }

  nodeIsStale(nodeId) {
    browser.wait(EC.stalenessOf($(`#${ActionsTree.ROOT_ACTION_GROUP} #${nodeId}`)), DEFAULT_TIMEOUT);
  }

  selectNode(node) {
    return this.getNode(node);
  }

  openNode(node) {
    return this.getNode(node).openObject();
  }

  getNodeChildren(node) {
    return node.element.$$('li');
  }

  isNodeModified(node) {
    return hasClass(this.getNode(node).getAnchor(), 'modified-node');
  }
}

ActionsTree.ROOT_ACTION_GROUP = 'ROOT_ACTION_GROUP_NODE';
ActionsTree.COMPONENT_SELECTOR = '.model-actions-tree.object-browser';
ActionsTree.ROOT_ACTION_GROUP_LABEL = 'Actions list';

ModelActionsSection.COMPONENT_SELECTOR = '.section.model-actions';
ModelActionsSection.ATTRIBUTE_EXTRACTOR_SELECTOR = 'actions';

module.exports = {
  ModelActionsSection,
  ActionsTree
};