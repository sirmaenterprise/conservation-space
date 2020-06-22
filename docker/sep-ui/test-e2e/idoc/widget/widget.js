'use strict';

let isCheckboxSelected = require('../../test-utils.js').isCheckboxSelected;
let ColorPicker = require('../../components/color-picker/color-picker');
let TestUtils = require('../../test-utils');

class Widget {

  constructor(widgetElement) {
    this.widgetElement = widgetElement;
    this.header = new WidgetHeader(widgetElement);
  }

  getHeader() {
    return this.header;
  }

  getBody() {
    return this.widgetElement.$('.widget-body');
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
    browser.wait(EC.visibilityOf(this.widgetElement), DEFAULT_TIMEOUT);
  }

  isExpanded() {
    browser.wait(EC.visibilityOf(this.getBody()), DEFAULT_TIMEOUT, 'Widget body should be visible!');
  }

  isCollapsed() {
    browser.wait(EC.invisibilityOf(this.getBody()), DEFAULT_TIMEOUT, 'Widget body should be hidden!');
  }
}

const WIDGET_ACTION_EXPAND = 'expand';
const WIDGET_ACTION_REMOVE = 'remove';
const WIDGET_ACTION_CONFIG = 'config';

class WidgetHeader {

  constructor(widgetElement) {
    this.headerElement = widgetElement.$('.widget-header');
  }

  setTitle(title) {
    let titleInput = this.getTitle();
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
    this.getAction(WIDGET_ACTION_CONFIG).click();
    // wait for the config to appear
    new WidgetConfigDialog();
  }

  getBackgroundColor() {
    return this.headerElement.getCssValue('background-color');
  }

  getAction(actionName) {
    let button = this.headerElement.$(`.${actionName}-button`);
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, `Widget ${actionName} button should be visible!`);
    return button;
  }

  expand() {
    let expandButton = this.getAction(WIDGET_ACTION_EXPAND).$('.fa-plus');
    browser.wait(EC.visibilityOf(expandButton), DEFAULT_TIMEOUT, 'Widget expand button should be visible!');
    expandButton.click();
  }

  collapse() {
    let collapseButton = this.getAction(WIDGET_ACTION_EXPAND).$('.fa-minus');
    browser.wait(EC.visibilityOf(collapseButton), DEFAULT_TIMEOUT, 'Widget collapse button should be visible!');
    collapseButton.click();
  }

  remove() {
    browser.executeScript('$(arguments[0]).click();', this.getAction(WIDGET_ACTION_REMOVE).getWebElement());
  }

  isActionAvailable(actionName) {
    return this.headerElement.$('.' + actionName).isPresent();
  }

  isActionVisible(actionName, visible, msg) {
    let operation = visible ? 'visibilityOf' : 'invisibilityOf';
    browser.wait(EC[operation](this.headerElement.$(`.${actionName}-button`)), DEFAULT_TIMEOUT, msg);
  }

  isCollapseExpandVisible(msg) {
    return this.isActionVisible(WIDGET_ACTION_EXPAND, true, msg);
  }

  isCollapseExpandHidden(msg) {
    return this.isActionVisible(WIDGET_ACTION_EXPAND, false, msg);
  }

  isConfigVisible(msg) {
    return this.isActionVisible(WIDGET_ACTION_CONFIG, true, msg);
  }

  isConfigHidden(msg) {
    return this.isActionVisible(WIDGET_ACTION_CONFIG, false, msg);
  }

  isDeleteVisible(msg) {
    return this.isActionVisible(WIDGET_ACTION_REMOVE, true, msg);
  }

  isDeleteHidden(msg) {
    return this.isActionVisible(WIDGET_ACTION_REMOVE, false, msg);
  }

  isDisplayed() {
    return this.headerElement.isDisplayed();
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
    let okButton = this.dialogElement.$('.seip-btn-ok');
    browser.wait(EC.presenceOf(okButton), DEFAULT_TIMEOUT, 'Save widget config button should be present!');
    browser.wait(EC.elementToBeClickable(okButton), DEFAULT_TIMEOUT, 'Save widget config button should be clickable!');
    okButton.click();
    this.waitUntilClosed();
    this.waitForWidget();
  }

  cancel(withoutInsert) {
    let cancelButton = this.dialogElement.$('.seip-btn-cancel');
    browser.wait(EC.presenceOf(cancelButton), DEFAULT_TIMEOUT, 'Cancel widget config button should be present!');
    browser.wait(EC.elementToBeClickable(cancelButton), DEFAULT_TIMEOUT, 'Cancel widget config button should be clickable!');
    cancelButton.click();
    this.waitUntilClosed();
    if (!withoutInsert) {
      this.waitForWidget();
    }
  }

  isTitlePresent(titleText) {
    let titleElement = this.dialogElement.$('.modal-title');
    browser.wait(EC.textToBePresentInElement(titleElement, titleText), DEFAULT_TIMEOUT, 'Title text should be present in dialog!');
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
      let shouldDeselect = isSelected && !shouldEnable;
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
    browser.wait(EC.visibilityOf(this.dialogElement), DEFAULT_TIMEOUT, 'Widget config dialog should be opened!');
  }

  waitUntilClosed() {
    browser.wait(EC.stalenessOf(this.dialogElement), DEFAULT_TIMEOUT, 'Widget config dialog should be closed!');
  }
}

module.exports.Widget = Widget;
module.exports.WidgetHeader = WidgetHeader;
module.exports.WidgetConfigDialog = WidgetConfigDialog;
