"use strict";

class Widget {

  constructor(widgetElement) {
    this.widgetElement = widgetElement;
    this.header = new WidgetHeader(widgetElement);
  }

  getHeader() {
    return this.header;
  }

  hover() {
    browser.actions()
      .mouseMove(this.widgetElement)
      .perform();
  }

  getDragHandler() {
    return this.widgetElement.element(by.xpath('..')).$('.cke_widget_drag_handler_container');
  }

  waitToAppear() {
    browser.wait(EC.visibilityOf(this.widgetElement), DEFAULT_TIMEOUT);
  }

}

class WidgetHeader {

  constructor(widgetElement) {
    this.widgetElement = widgetElement;
  }

  setTitle(title) {
    var titleInput = this.widgetElement.$('.widget-title-input');
    titleInput.clear();
    titleInput.sendKeys(title);
  }

  openConfig() {
    this.widgetElement.$('.config-button').click();
    // wait for the config to appear
    new WidgetConfigDialog();
  }

  remove() {
    browser.executeScript('$(arguments[0]).click();', this.widgetElement.$('.remove-button').getWebElement());
  }

}

class WidgetConfigDialog {
  constructor(widgetName) {
    this.dialogElement = $('.seip-modal');
    this.waitUntilOpened();
    this.widgetName = widgetName;
  }

  save() {
    var okButton = this.dialogElement.$('.seip-btn-ok');
    browser.wait(EC.elementToBeClickable(okButton), DEFAULT_TIMEOUT);
    okButton.click();
    this.waitUntilClosed();
    this.waitForWidget();
  }

  cancel(withoutInsert) {
    var cancelButton = this.dialogElement.$('.seip-btn-cancel');
    browser.wait(EC.elementToBeClickable(cancelButton), DEFAULT_TIMEOUT);
    cancelButton.click();
    this.waitUntilClosed();
    if (!withoutInsert) {
      this.waitForWidget();
    }
  }

  waitForWidget() {
    new Widget($('.' + this.widgetName)).waitToAppear();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.dialogElement), DEFAULT_TIMEOUT);
  }

  waitUntilClosed() {
    browser.wait(EC.not(EC.presenceOf(this.dialogElement)), DEFAULT_TIMEOUT);
  }
}

module.exports.Widget = Widget;
module.exports.WidgetConfigDialog = WidgetConfigDialog;