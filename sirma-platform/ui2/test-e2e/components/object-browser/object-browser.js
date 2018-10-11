'use strict';

let PageObject = require('../../page-object').PageObject;
let SandboxPage = require('../../page-object.js').SandboxPage;
let isInViewPort = require('../../test-utils.js').isInViewPort;

class ObjectBrowserSandboxPage extends SandboxPage {

  open(id) {
    super.open('/sandbox/components/object-browser', `id=${id}`);
  }

  getSelectedNode() {
    return $('#selected-node').getText();
  }

  getObjectBrowser() {
    return new ObjectBrowser($(ObjectBrowser.COMPONENT_SELECTOR));
  }
}

class ObjectBrowser extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getNode(nodeText) {
    // this yells a warning because there are nested links in the tree
    // hence they have the same text inside and protractor doesn't have a non-async way to get one of
    let link = this.element.element(by.partialLinkText(nodeText));
    browser.wait(EC.visibilityOf(link), DEFAULT_TIMEOUT);
    return new TreeNode(link.element(by.xpath('ancestor::li[1]')));
  }

  search(query) {
    this.searchField.sendKeys(query);
  }

  isSearchEnabled() {
    return this.searchField.isDisplayed();
  }

  get searchField() {
    return this.element.$('.filter-field > .form-control');
  }
}

ObjectBrowser.COMPONENT_SELECTOR = '.object-browser';

class TreeNode {

  constructor(element) {
    this.element = element;
  }

  openObject() {
    this.element.$('.instance-link').click();
  }

  expand() {
    this.element.$('.jstree-ocl').click();
  }

  check() {
    this.element.$('.jstree-checkbox').click();
  }

  isChecked() {
    return this.element.$('.jstree-checked').isPresent();
  }

  isHighlighted() {
    return this.element.$('.instance-link.highlighted').isPresent();
  }

  isExpanded() {
    return this.element.$('.jstree-children').isPresent();
  }

  isDisplayed() {
    return isInViewPort(this.element);
  }

}

module.exports.ObjectBrowserSandboxPage = ObjectBrowserSandboxPage;
module.exports.ObjectBrowser = ObjectBrowser;