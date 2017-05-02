"use strict";

var TestUtils = require('../test-utils');
var IdocTabs = require('./idoc-tabs/idoc-tabs').IdocTabs;
var SandboxPage = require('../page-object').SandboxPage;

const IDOC_PAGE_URL = '/sandbox/idoc/idoc-page';

class IdocPage extends SandboxPage {

  constructor(sandboxUrl) {
    super();
    this.sandboxUrl = sandboxUrl;
  }

  open(edit, id) {
    if (typeof edit === 'undefined') {
      throw new Error('Idoc mode must be provided');
    }

    var hash = `${id ? id : ''}?mode=${edit ? 'edit' : 'preview'}`;
    super.open(this.sandboxUrl || IDOC_PAGE_URL, hash);

    this.edit = edit;

    // prevent race conditions in tests with waiting for the editor to get initialized
    if (this.edit) {
      // assume that the idoc page always has at least one tab
      this.waitForEditor(1);
      this.scrollToTop();
    }

    return this;
  }

  isSaved() {
    return $('.idoc-mode-preview').isPresent();
  }

  waitForPreviewMode() {
    browser.wait(EC.presenceOf($('.idoc-mode-preview')), DEFAULT_TIMEOUT, 'Wait for document to go in preview mode');
  }

  waitForEditor(tabNumber) {
    this.getTabEditor(tabNumber);
    return this;
  }

  getTabEditor(tabNumber) {
    return new ContentArea(tabNumber, this.edit);
  }

  getSystemTabContent(tabNumber) {
    return $('.tab-content > :nth-child(' + tabNumber + ') .system-tab-content');
  }

  getEditorToolbar(tabNumber) {
    this.waitForEditor(tabNumber);
    return new EditorToolbar(tabNumber);
  }

  getActionsToolbar() {
    return new ActionsToolbar();
  }

  getIdocTabs(includeSystemTabs) {
    return new IdocTabs(includeSystemTabs);
  }

  setEdit(edit) {
    this.edit = edit;
  }

  scrollToTop() {
    browser.driver.executeScript('$("body")[0].scrollIntoView(true);');
    return this;
  }

}

class ActionsToolbar {
  constructor() {
    this.toolbar = element(by.css('.idoc-context-actions-wrapper'));
    this.saveButton = element(by.css('.seip-btn-save'));
    this.cancelButton = element(by.css('.seip-btn-cancel'));
    this.editButton = element(by.css('.seip-btn-edit'));
    this.templateSelector = $('.idoc-template-selector');
    this.editorToolbar = $('.idoc-editor-toolbar-container');
    browser.wait(EC.presenceOf(this.toolbar), DEFAULT_TIMEOUT);
  }

  /**
   * @param expectingDialog Should be passed true if any object in idoc context has invalid data which normally would
   * cause a modal dialog with form builder to be opened in which case the invoker should handle the dialog by itself.
   */
  saveIdoc(expectingDialog) {
    this.saveButton.click();
    // those are different wariants for checking if the idoc went to preview mode
    //browser.wait(EC.invisibilityOf($('#toast-container')), DEFAULT_TIMEOUT);
    //browser.wait(EC.invisibilityOf(this.editorToolbar), DEFAULT_TIMEOUT);
    if (!expectingDialog) {
      browser.wait(EC.presenceOf(element(by.css('.idoc-mode-preview'))), DEFAULT_TIMEOUT);
    }
  }

  editIdoc() {
    return this.editButton.click();
  }

  cancelSave() {
    return this.cancelButton.click();
  }

  isButtonPresent(btnClass) {
    return this.toolbar.element(by.className(btnClass)).isPresent();
  }

  getTemplateSelector() {
    browser.wait(EC.presenceOf(this.templateSelector), DEFAULT_TIMEOUT);
    return this.templateSelector;
  }
}

class EditorToolbar {
  constructor(tabNumber) {
    this.toolbarElement = element(by.className('cke_toolbar'));
    this.tabNumber = tabNumber;
    this.dialogWindowSelector = ".cke_dialog";
    this.alertInfoSelector = ".alert-info";
    browser.wait(EC.presenceOf(this.toolbarElement), DEFAULT_TIMEOUT);
  }

  getWidgetMenu() {
    return new WidgetMenu(this.tabNumber, this.toolbarElement, 'Widgets');
  }

  getPasteMenu() {
    return new PasteMenu(this.tabNumber, this.toolbarElement, 'Clipboard');
  }

  insertInfoWidget() {
    var button = this.toolbarElement.element(by.css('[title=\'Info widget\']'));
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    //click the toolbar button for inserting an info box
    button.click();
    //wait for the dialog and the info box types to be present
    browser.wait(EC.presenceOf(element(by.css(this.dialogWindowSelector))), DEFAULT_TIMEOUT);
    var dialogBody = browser.driver.findElement(by.css(this.dialogWindowSelector));
    browser.wait(EC.presenceOf(element(by.css(this.alertInfoSelector))), DEFAULT_TIMEOUT);
    var infoBoxType = dialogBody.findElement(by.css(this.alertInfoSelector));
    //select the info box type to be alert-info
    infoBoxType.click();
  }
}

class EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    this.menuElement = toolbarElement.element(by.css('[title=' + menuName + ']'));
    browser.wait(EC.elementToBeClickable(this.menuElement), DEFAULT_TIMEOUT);
    this.tabNumber = tabNumber;
  }

  open() {
    browser.wait(EC.elementToBeClickable(this.menuElement), DEFAULT_TIMEOUT, 'Menu to become clickable');
    this.menuElement.click();
  }

  select(menuName) {
    this.open();
    browser.wait(EC.visibilityOf($('iframe')), DEFAULT_TIMEOUT);
    browser.switchTo().frame(0);
    browser.driver.findElement(by.className('cke_menubutton__' + menuName)).click();
    browser.switchTo().defaultContent();
  }
}

class PasteMenu extends EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    super(tabNumber, toolbarElement, menuName);
  }
}

class WidgetMenu extends EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    super(tabNumber, toolbarElement, menuName);
  }

  selectWidget(widgetName) {
    this.select(widgetName);

    var contentArea = new ContentArea(this.tabNumber, true);
    var widgetElement = contentArea.getWidget(widgetName);
    browser.wait(EC.presenceOf(widgetElement), DEFAULT_TIMEOUT);
    return widgetElement;
  }
}

class ContentArea {
  constructor(tabNumber, edit) {
    this.contentElement = $('.tab-content > :nth-child(' + tabNumber + ') .idoc-editor-area-wrapper .ck-editor-area');
    this.waitUntilLoaded();
  }

  waitUntilLoaded() {
    browser.wait(EC.presenceOf(this.contentElement), DEFAULT_TIMEOUT);
    browser.wait(()=> {
      return TestUtils.hasClass(this.contentElement, 'initialized');
    }, DEFAULT_TIMEOUT);
  }

  insertWidget(name) {
    this.contentElement.click();
    this.executeWidgetCommand(name);
    var widgetElement = this.getWidget(name);
    browser.wait(EC.presenceOf(widgetElement), DEFAULT_TIMEOUT);
    return widgetElement;
  }

  executeWidgetCommand(widgetName) {
    browser.executeScript('CKEDITOR.instances[arguments[0].id].getCommand(arguments[1]).exec()', this.contentElement.getWebElement(), widgetName);
  }

  isFocused() {
    return TestUtils.hasClass(this.contentElement, 'cke_focus');
  }

  type(text) {
    this.contentElement.sendKeys(text);
    return this;
  }

  clear() {
    this.contentElement.clear();
    return this;
  }

  click() {
    this.contentElement.click();
  }

  getWidget(name) {
    return this.contentElement.$('.' + name);
  }

  getWidgetByNameAndOrder(name, order) {
    return this.contentElement.$$('.' + name).get(order);
  }

  getAsText() {
    return this.contentElement.getText();
  }

  getParagraph(number) {
    return new Paragraph(this.contentElement.$('p:nth-of-type(' + number + ')'));
  }

  /**
   * Drags a widget and drops it on a specified position in the editor.
   *
   * @param widgetElement widget to drag.
   * @param dropPosition Object with 'x' and 'y' properties with the position where to drop the widget.
   */
  dragAndDropWidget(widgetElement, dropPosition) {
    const DRAG_HANDLE_COORDINATES = {x: 2, y: -2};

    // for unknown reason a double mouseUp() is required
    browser.actions()
      .mouseMove(widgetElement)
      .mouseMove(widgetElement, DRAG_HANDLE_COORDINATES)
      .mouseDown()
      .mouseMove(widgetElement, dropPosition)
      .mouseUp()
      .mouseUp()
      .perform();
  }
}

class Paragraph {
  constructor(element) {
    this.element = element;
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  type(text) {
    browser.actions()
      .mouseMove(this.element)
      .click()
      .sendKeys(text)
      .perform();

    return this;
  }

  click() {
    browser.wait(EC.elementToBeClickable(this.element), DEFAULT_TIMEOUT);
    this.element.click();
  }
}

module.exports = IdocPage;