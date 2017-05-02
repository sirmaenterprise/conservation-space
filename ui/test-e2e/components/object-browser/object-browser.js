"use strict";

var SandboxPage = require('../../page-object.js').SandboxPage;

class ObjectBrowserSandboxPage extends SandboxPage {
  open(id) {
    super.open('/sandbox/components/object-browser', `id=${id}`);
  }

  getSelectedNode() {
    return $('#selected-node').getText();
  }

  getObjectBrowser() {
    var browserElement = $('.object-browser');
    browser.wait(EC.presenceOf(browserElement), DEFAULT_TIMEOUT);
    return new ObjectBrowser(browserElement);
  }
}

class ObjectBrowser {

  constructor(browserElement) {
    this.browserElement = browserElement;
  }

  getNode(nodeText) {
    // this yells a warning because there are nested links in the tree
    // hence they have the same text inside and protractor doesn't have a non-async way to get one of
    var link = this.browserElement.element(by.partialLinkText(nodeText));
    browser.wait(EC.visibilityOf(link), DEFAULT_TIMEOUT);
    var nodeElement = link.element(by.xpath('ancestor::li[1]'));
    return new TreeNode(nodeElement);
  }

}

class TreeNode {

  constructor(element) {
    this.element = element;
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
    return this.element.$(".instance-link.highlighted").isPresent();
  }
}

module.exports.ObjectBrowserSandboxPage = ObjectBrowserSandboxPage;
module.exports.ObjectBrowser = ObjectBrowser;