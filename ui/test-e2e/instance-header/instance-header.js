'use strict';

var SandboxPage = require('../page-object').SandboxPage;

const INSTANCE_HEADER_PAGE_URL = '/sandbox/instance-header';

class InstanceHeaderSandboxPage extends SandboxPage {

  open() {
    super.open(INSTANCE_HEADER_PAGE_URL);
  }

  changeTitle(title) {
    $('.title-input').click().clear().sendKeys(title);
  }

  loadHeader() {
    return $('.load-header').click();
  }

  getIconHeader() {
    return new InstanceHeader($('#icon_header'));
  }

  getThumbnailHeader() {
    return new InstanceHeader($('#thumbnail_header'));
  }
}

class InstanceHeader {

  constructor(element) {
    this.element = element;
  }

  getHeader() {
    return this.element.$('.instance-data').getText();
  }

  getIconUrl() {
    return this.element.$('.instance-icon img').getAttribute('src');
  }

  getField(selector) {
    return this.element.$(selector);
  }

}

module.exports = {InstanceHeaderSandboxPage, InstanceHeader};