"use strict";
var isCheckboxSelected = require('../../test-utils.js').isCheckboxSelected;
var ColorPicker = require('../../components/color-picker/color-picker');
var TestUtils = require('../../test-utils');

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

  hasBorders() {
    // select direct child panel of widgetElement
    return this.widgetElement.$('.panel-body').getCssValue('border').then((cssValue) => {
      return cssValue.indexOf('0px') === -1;
    });
  }

  hasHeaderBorders() {
    // select direct child panel of widgetElement
    return this.widgetElement.$('.panel-heading').getCssValue('border').then((cssValue) => {
      return cssValue.indexOf('0px') === -1;
    });
  }

  getDragHandler() {
    return this.widgetElement.element(by.xpath('..')).$('.cke_widget_drag_handler_container');
  }

  getBackgroundColor() {
    return this.widgetElement.$('.widget-body').getCssValue('background-color');
  }

  getErrorMessage() {
    return this.widgetElement.$('.error');
  }

  waitToAppear() {
    browser.wait(() => TestUtils.hasClass(this.widgetElement, 'initialized'), DEFAULT_TIMEOUT, 'Did you use the proper selector: ([widget="${widget-name}"]? Has the widget been properly initialized?');
  }

}

class WidgetHeader {

  constructor(widgetElement) {
    this.headerElement = widgetElement.$('.widget-header');
  }

  setTitle(title) {
    var titleInput = this.headerElement.$('.widget-title-input');
    titleInput.clear();
    titleInput.sendKeys(title);
  }

  getTitle() {
    return this.headerElement.$('.widget-title-input');
  }

  isTitleVisible(msg) {
    browser.wait(EC.visibilityOf(this.getTitle()), DEFAULT_TIMEOUT, msg);
  }

  isTitleHidden(msg) {
    browser.wait(EC.invisibilityOf(this.getTitle()), DEFAULT_TIMEOUT, msg);
  }

  openConfig() {
    this.headerElement.$('.config-button').click();
    // wait for the config to appear
    new WidgetConfigDialog();
  }

  getBackgroundColor() {
    return this.headerElement.getCssValue('background-color');
  }

  isActionAvailable(actionName) {
    return this.headerElement.$('.' + actionName).isPresent();
  }

  isActionVisible(actionName, visible, msg) {
    let operation = visible ? 'visibilityOf' : 'invisibilityOf';
    browser.wait(EC[operation](this.headerElement.$('.' + actionName)), DEFAULT_TIMEOUT, msg);
  }

  isCollapseExpandVisible(msg) {
    return this.isActionVisible('expand-button', true, msg);
  }

  isCollapseExpandHidden(msg) {
    return this.isActionVisible('expand-button', false, msg);
  }

  isConfigVisible(msg) {
    return this.isActionVisible('config-button', true, msg);
  }

  isConfigHidden(msg) {
    return this.isActionVisible('config-button', false, msg);
  }

  isDeleteVisible(msg) {
    return this.isActionVisible('remove-button', true, msg);
  }

  isDeleteHidden(msg) {
    return this.isActionVisible('remove-button', false, msg);
  }

  isDisplayed() {
    return this.headerElement.isDisplayed();
  }

  remove() {
    browser.executeScript('$(arguments[0]).click();', this.headerElement.$('.remove-button').getWebElement());
  }

}

class WidgetConfigDialog {
  constructor(widgetName) {
    this.dialogElement = $('.seip-modal');
    this.waitUntilOpened();
    this.widgetName = widgetName;
    this.widgetSelector = `[widget="${widgetName}"]`;

    this.showWidgetHeaderOprion = $('.show-widget-header');
    this.showWidgetBorders = $('.show-widget-borders');
    this.showWidgetHeaderBordersOption = $('.show-widget-header-borders');
    this.headerBackgroundColorPicker = $('.header-background-color-picker .color-picker');
    this.widgetBackgroundColorPicker = $('.widget-background-color-picker .color-picker');
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

  isShowWidgetHeaderSelected() {
    return isCheckboxSelected(this.showWidgetHeaderOprion.$('input'));
  }

  isShowWidgetBordersSelected() {
    return isCheckboxSelected(this.showWidgetBorders.$('input'));
  }

  isShowWidgetHeaderBordersSelected() {
    return isCheckboxSelected(this.showWidgetHeaderBordersOption.$('input'));
  }

  isOptionSelected(option) {
    return option.$('input').getAttribute('checked');
  }

  toggleOption(option, shouldEnable) {
    this.isOptionSelected(option).then((isSelected) => {
      let shouldDeselect= isSelected && !shouldEnable;
      let shouldSelect = !isSelected && shouldEnable;
      if (shouldDeselect || shouldSelect) {
        option.click();
      }
    });
  }

  showWidgetHeader() {
    this.toggleOption(this.showWidgetHeaderOprion, true);
  }

  hideWidgetHeader() {
    this.toggleOption(this.showWidgetHeaderOprion, false);
  }

  toggleShowWidgetHeader() {
    this.showWidgetHeaderOprion.click();
    return this;
  }

  toggleShowWidgetBorders() {
    this.showWidgetBorders.click();
    return this;
  }

  toggleShowWidgetHeaderBorders() {
    this.showWidgetHeaderBordersOption.click();
    return this;
  }

  showWidgetHeaderBorders() {
    this.toggleOption(this.showWidgetHeaderBordersOption, true);
  }

  hideWidgetHeaderBorders() {
    this.toggleOption(this.showWidgetHeaderBordersOption, false);
  }

  getHeaderBackgroundColorPicker() {
    return new ColorPicker(this.headerBackgroundColorPicker);
  }

  getWidgetBackgroundColorPicker() {
    return new ColorPicker(this.widgetBackgroundColorPicker);
  }

  waitForWidget() {
    new Widget($(this.widgetSelector)).waitToAppear();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.dialogElement), DEFAULT_TIMEOUT);
  }

  waitUntilClosed() {
    browser.wait(EC.not(EC.presenceOf(this.dialogElement)), DEFAULT_TIMEOUT);
  }
}

module.exports.Widget = Widget;
module.exports.WidgetHeader = WidgetHeader;
module.exports.WidgetConfigDialog = WidgetConfigDialog;