'use strict';

let TestUtils = require('../test-utils');
let IdocTabs = require('./idoc-tabs/idoc-tabs').IdocTabs;
let SandboxPage = require('../page-object').SandboxPage;
let ActionsMenu = require('./actions-menu/actions-menu');
let HeaderContainer = require('../header-container/header-container').HeaderContainer;
let Sidebar = require('../idoc/sidebar/sidebar').Sidebar;
let ConfirmationPopup = require('../components/dialog/confirmation-popup');
let DatatableWidget = require('./widget/data-table-widget/datatable-widget.js').DatatableWidget;
let DatatableWidgetConfigDialog = require('./widget/data-table-widget/datatable-widget').DatatableWidgetConfigDialog;
let ObjectDataWidget = require('./widget/object-data-widget/object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./widget/object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;
let ObjectSelector = require('./widget/object-selector/object-selector.js').ObjectSelector;

const IDOC_PAGE_URL = '/sandbox/idoc/idoc-page';

const REPEATER = {
  IDOC_TABS: 'tab in idocTabs.config.tabs',
  IDOC_TABS_CONTENT: 'tab in idocPage.tabsConfig.tabs'
};

const VIEW_MODE = {
  EDIT: 'edit',
  PREVIEW: 'preview'
};

const CSS = {
  ACTIVE: 'active',
  NG_HIDDEN: 'ng-hide',
  IDOC_NAVIGATION_WRAPPER: 'idoc-navigation-wrapper',
  IDOC_EDITOR_AREA_WRAPPER: 'idoc-editor-area-wrapper',
  IDOC_COMMENTS_WRAPPER: 'idoc-comments-wrapper',
  TAB_SECTIONS: 'tab-sections',
  CK_EDITOR_EDITABLE: 'editable',
  IDOC_CONTEXT_WRAPPER: 'idoc-context-wrapper',
  IDOC_CONTEXT_ACTIONS_WRAPPER: 'idoc-context-actions-wrapper',
  BTN_SAVE: 'seip-btn-save',
  BTN_EDIT: 'seip-action-editDetails',
  BTN_CANCEL: 'seip-btn-cancel',
  BTN_TOGGLE_HEADER: 'seip-btn-toggle-header',
  IDOC_TABS: 'idoc-tabs'
};

class IdocPage extends SandboxPage {

  constructor(sandboxUrl) {
    super();
    this.sandboxUrl = sandboxUrl;
  }

  open(edit, id) {
    if (typeof edit === 'undefined') {
      throw new Error('Idoc mode must be provided');
    }

    let hash = `${id ? id : 'emf:123456'}?mode=${edit ? 'edit' : 'preview'}`;
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

  waitForEditMode() {
    browser.wait(EC.presenceOf($('.idoc-mode-edit')), DEFAULT_TIMEOUT, 'Wait for document to go in edit mode');
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

  getSidebar() {
    return new Sidebar($('.sidebar'));
  }

  getConformationPopup() {
    return new ConfirmationPopup();
  }

  setEdit(edit) {
    this.edit = edit;
  }

  scrollToTop() {
    browser.driver.executeScript('$("body")[0].scrollIntoView(true);');
    return this;
  }

  /**
   * Performs check for expected view mode
   * @param mode expected view mode. Use VIEW_MODE constant
   */
  checkViewMode(mode) {
    let checkMode = this.getActiveTabIndex().then(function (index) {
      let activeSection = element(by.repeater(REPEATER.IDOC_TABS_CONTENT).row(index));
      let wrapperElement = activeSection.element(by.css('.' + CSS.TAB_SECTIONS + ' > .' + CSS.IDOC_EDITOR_AREA_WRAPPER));
      browser.wait(EC.visibilityOf(wrapperElement), DEFAULT_TIMEOUT);
      expect(wrapperElement.isDisplayed()).to.eventually.be.true;

      let expectedResult = mode === 'edit';
      expect(TestUtils.hasClass(wrapperElement, CSS.CK_EDITOR_EDITABLE), 'Expected view mode to be ' + mode).to.eventually.equal(expectedResult);
      // check that div with ck-editor is present (edit div)
      expect(element(by.id('idoc-editor-toolbar')).isDisplayed()).to.eventually.equal(expectedResult);
      return true;
    });
    browser.wait(checkMode, DEFAULT_TIMEOUT);
  }

  /**
   * Validates section content against expected content
   * @param index section index starting from 0
   * @param expectedContent
   */
  checkSectionContent(index, expectedContent) {
    let sections = element.all(by.repeater(REPEATER.IDOC_TABS_CONTENT));
    browser.wait(TestUtils.existenceOf(sections), DEFAULT_TIMEOUT);
    expect(sections.get(index).element(by.css('.' + CSS.IDOC_EDITOR_AREA_WRAPPER + ' > div')).getText()).to.eventually.equal(expectedContent);
  }

  /**
   * Replace section content with new content
   * @param index section index
   * @param newContent
   */
  setSectionContent(index, newContent) {
    let sections = element.all(by.repeater(REPEATER.IDOC_TABS_CONTENT));
    browser.wait(TestUtils.existenceOf(sections), DEFAULT_TIMEOUT);
    let editor = sections.get(index).element(by.css('.' + CSS.IDOC_EDITOR_AREA_WRAPPER + ' > div'));
    browser.wait(EC.presenceOf(editor), DEFAULT_TIMEOUT);
    editor.click().clear().sendKeys(newContent);
  }

  /**
   * Sets content for all tabs appending tab's index at the end.
   * @param idocPage
   * @param content text to be set in the tab
   * @returns {*}
   */
  setTabsContent(content) {
    let idocTabs = this.getIdocTabs();
    return idocTabs.getTabs().then((tabs) => {
      for (let i = 0; i < tabs.length; i++) {
        idocTabs.getTabByIndex(i).select();
        this.getTabEditor(i + 1).clear().click().type(content + i);
      }
      return true;
    });
  };

  /**
   * Check which is currently active tab by checking for presence of 'active' class
   * @returns {Promise} which resolves with active tab index
   */
  getActiveTabIndex() {
    return browser.executeScript('return $(".tab-item").index($(".tab-item.active"));');
  }

  insertDTWWithFields(fields, selection, orderOnPage = 0) {
    this.getTabEditor(1).insertWidget(DatatableWidget.WIDGET_NAME);
    let widgetConfig = new DatatableWidgetConfigDialog();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    if (selection.type === ObjectSelector.MANUALLY) {
      let search = objectSelector.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().clickResultItem(selection.item);
    } else if (selection.type === ObjectSelector.AUTOMATICALLY) {
      // implement when needed
    }
    this.selectProperties(widgetConfig, fields);
    widgetConfig.save();
    return new DatatableWidget($$('.datatable-widget').get(orderOnPage));
  }

  insertODWWithFields(fields, selection, orderOnPage = 0) {
    this.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    if (selection.type === ObjectSelector.CURRENT_OBJECT) {
      objectSelector.selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    } else if (selection.type === ObjectSelector.MANUALLY) {
      let search = objectSelector.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().clickResultItem(selection.item);
    }
    this.selectProperties(widgetConfig, fields);
    widgetConfig.save();
    return new ObjectDataWidget($$('.object-data-widget').get(orderOnPage));
  }

  selectProperties(widgetConfig, propertyNames) {
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperties(propertyNames);
  }

  insertLayout(layoutNum) {
    $('.cke_button__addlayout').click();
    browser.wait(EC.visibilityOf($('.cke_dialog_contents_body')), DEFAULT_TIMEOUT);
    $$('div.container-fluid').get(layoutNum - 1).click();
    browser.wait(EC.visibilityOf($('.layoutmanager')), DEFAULT_TIMEOUT);
  }
}

class ActionsToolbar {
  constructor() {
    this.toolbar = element(by.css('.idoc-context-actions-wrapper'));
    this.saveButton = element(by.css('.seip-btn-save'));
    this.cancelButton = element(by.css('.seip-btn-cancel'));
    this.editButton = element(by.css('.seip-action-editDetails'));
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

  getHeaderContainer() {
    return new HeaderContainer($('.idoc-context-wrapper'));
  }

  getHeaderActions() {
    return this.toolbar.all(by.css('.btn'));
  }

  editIdoc(draft) {
    this.editButton.click();
    if (!draft) {
      browser.wait(EC.presenceOf(element(by.css('.idoc-mode-edit'))), DEFAULT_TIMEOUT);
    }
  }

  cancelSave() {
    return this.cancelButton.click();
  }

  isButtonPresent(btnClass) {
    return this.toolbar.element(by.className(btnClass)).isPresent();
  }

  //When creating new idoc and then saving it the router stub doesn't relaod the page so it isn't persisted.
  //The header actions are shown only if the idoc is persisted, so in order to edit the idoc we should use the actions menu.
  getActionsMenu() {
    return new ActionsMenu(element(by.css('.idoc-context-actions-wrapper')));
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
    this.dialogWindowSelector = '.cke_dialog';
    this.alertInfoSelector = '.alert-info';
    browser.wait(EC.presenceOf(this.toolbarElement), DEFAULT_TIMEOUT);
  }

  getFontSizeMenu() {
    return new FontSizeMenu(this.tabNumber, this.toolbarElement, 'Font Size');
  }

  getFontNameMenu() {
    return new FontNameMenu(this.tabNumber, this.toolbarElement, 'Font Name');
  }

  getHeadingMenu() {
    return new HeadingMenu(this.tabNumber, this.toolbarElement, 'Paragraph Format');
  }

  getUndoRedoToolbar() {
    return new UndoRedoActionsMenu(this.toolbarElement);
  }

  getWidgetMenu() {
    return new WidgetMenu(this.tabNumber, this.toolbarElement, 'Widgets');
  }

  getPasteMenu() {
    return new PasteMenu(this.tabNumber, this.toolbarElement, 'Clipboard');
  }

  getTextOptionsToolbar() {
    return new EditorToolbarOptions(this.toolbarElement);
  }

  insertInfoWidget() {
    let button = this.toolbarElement.element(by.css('[title=\'Info widget\']'));
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    //click the toolbar button for inserting an info box
    button.click();
    //wait for the dialog and the info box types to be present
    browser.wait(EC.presenceOf(element(by.css(this.dialogWindowSelector))), DEFAULT_TIMEOUT);
    let dialogBody = browser.driver.findElement(by.css(this.dialogWindowSelector));
    browser.wait(EC.presenceOf(element(by.css(this.alertInfoSelector))), DEFAULT_TIMEOUT);
    let infoBoxType = dialogBody.findElement(by.css(this.alertInfoSelector));
    //select the info box type to be alert-info
    infoBoxType.click();
    browser.wait(EC.presenceOf($('.info-widget')), DEFAULT_TIMEOUT);
  }
}

class EditorToolbarMenu {

  constructor(tabNumber, toolbarElement, menuName) {
    this.menuElement = toolbarElement.element(by.css(`[title='${menuName}']`));
    browser.wait(EC.elementToBeClickable(this.menuElement), DEFAULT_TIMEOUT, `Editor's toolbar ${menuName} menu should be clickable!`);
    this.tabNumber = tabNumber;
  }

  open() {
    browser.wait(EC.elementToBeClickable(this.menuElement), DEFAULT_TIMEOUT, 'Menu to become clickable');
    this.menuElement.click();
  }

  select(menuName) {
    this.open();
    browser.wait(EC.visibilityOf($('iframe')), DEFAULT_TIMEOUT, 'Menu iframe wrapper should be visible!');
    browser.switchTo().frame(0);
    this.activate(menuName);
    browser.switchTo().defaultContent();
  }

  activate(menuName) {
    browser.driver.findElement(by.className('cke_menubutton__' + menuName)).click();
  }
}

class FontSizeMenu extends EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    super(tabNumber, toolbarElement, menuName);
  }

  activate(menuName) {
    browser.driver.findElement(by.css(`[title='${menuName}']`)).click();
  }
}
FontSizeMenu.FS_DEFAULT = '(Default)';
FontSizeMenu.FS_8 = '8';
FontSizeMenu.FS_9 = '9';
FontSizeMenu.FS_10 = '10';
FontSizeMenu.FS_11 = '11';
FontSizeMenu.FS_12 = '12';
FontSizeMenu.FS_14 = '14';
FontSizeMenu.FS_16 = '16';
FontSizeMenu.FS_18 = '18';
FontSizeMenu.FS_20 = '20';
FontSizeMenu.FS_22 = '22';
FontSizeMenu.FS_24 = '24';
FontSizeMenu.FS_26 = '26';
FontSizeMenu.FS_28 = '28';
FontSizeMenu.FS_36 = '36';
FontSizeMenu.FS_48 = '48';
FontSizeMenu.FS_72 = '72';

class FontNameMenu extends EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    super(tabNumber, toolbarElement, menuName);
  }

  activate(menuName) {
    browser.driver.findElement(by.css(`[title='${menuName}']`)).click();
  }
}
FontNameMenu.FN_DEFAULT = '(Default)';
FontNameMenu.FN_OPEN_SANS = 'Open Sans*';
FontNameMenu.FN_ARIMO = 'Arimo*';
FontNameMenu.FN_CALADEA = 'Caladea*';
FontNameMenu.FN_CARLITO = 'Carlito*';
FontNameMenu.FN_COUSINE = 'Cousine*';
FontNameMenu.FN_TINOS = 'Tinos*';
FontNameMenu.FN_ARIAL = 'Arial';
FontNameMenu.FN_COMIC_SANS_MS = 'Comic Sans MS';
FontNameMenu.FN_COURIER_NEW = 'Courier New';
FontNameMenu.FN_GEORGIA = 'Georgia';
FontNameMenu.FN_LUCIDA_SANS_UNICODE = 'Lucida Sans Unicode';
FontNameMenu.FN_TAHOMA = 'Tahoma';
FontNameMenu.FN_TIMES_NEW_ROMAN = 'Times New Roman';
FontNameMenu.FN_TREBUCHET_MS = 'Trebuchet MS';
FontNameMenu.FN_VERDANA = 'Verdana';

class HeadingMenu extends EditorToolbarMenu {
  constructor(tabNumber, toolbarElement, menuName) {
    super(tabNumber, toolbarElement, menuName);
  }

  activate(menuName) {
    browser.driver.findElement(by.css(`[title='${menuName}']`)).click();
  }
}
HeadingMenu.HEADING1 = 'Heading 1';

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

    let contentArea = new ContentArea(this.tabNumber, true);
    let widgetElement = contentArea.getWidget(widgetName);
    browser.wait(EC.presenceOf(widgetElement), DEFAULT_TIMEOUT);
    return widgetElement;
  }
}

class EditorToolbarOptions {
  constructor(toolbarElement) {
    this.replaceMenuButton = toolbarElement.element(by.css('.cke_button__replace'));
    this.boldMenuButton = toolbarElement.element(by.css('.cke_button__bold'));
    this.italicMenuButton = toolbarElement.element(by.css('.cke_button__italic'));
    this.strikeTextButton = new ListEditorMenu(toolbarElement.element(by.css('.cke_button__strike_icon')), '[title="Options"]');
    this.insertNumberedListButton = toolbarElement.element(by.css('.cke_button__numberedlist'));
    this.bulletedListButton = toolbarElement.element(by.css('.cke_button__bulletedlist'));
    this.horizontalRuleButton = toolbarElement.element(by.css('.cke_button__horizontalrule'));
    this.blockquoteButton = toolbarElement.element(by.css('.cke_button__blockquote'));
    this.justifyMenu = toolbarElement.element(by.css('.cke_button__justify'));
    this.pageBreakButton = toolbarElement.element(by.css('.cke_button__pagebreak'));
  }

  boldText() {
    this.boldMenuButton.click();
  }

  italicText() {
    this.italicMenuButton.click();
  }

  strikeText(listTitle) {
    this.strikeTextButton.selectFromMenu(listTitle);
  }

  insertNumberedList() {
    this.insertNumberedListButton.click();
  }

  insertbulletedList() {
    this.bulletedListButton.click();
  }

  insertHorizontalRule() {
    this.horizontalRuleButton.click();
  }

  insertBlockQuote() {
    this.blockquoteButton.click();
  }

  insertPageBreak() {
    this.pageBreakButton.click();
  }
}

class ListEditorMenu {
  constructor(menuButton, menuSelector) {
    this.menuButton = menuButton;
    this.menuSelector = menuSelector;
  }

  selectFromMenu(menuItemName) {
    this.menuButton.click();
    browser.wait(EC.visibilityOf(element(by.css('iframe'))), DEFAULT_TIMEOUT);
    browser.driver.switchTo().frame(element(by.tagName('iframe')).getWebElement());
    browser.driver.findElement(by.css(`[title="${menuItemName}"]`)).click();
    browser.switchTo().defaultContent();
  }
}

class UndoRedoActionsMenu {
  constructor(toolbar) {
    this.undoBtn = toolbar.element(by.css('.cke_button.cke_button__undo'));
    this.redoBtn = toolbar.element(by.css('.cke_button.cke_button__redo'));
  }

  getUndoButton() {
    return this.undoBtn;
  }

  getRedoButton() {
    return this.redoBtn;
  }

  undo(contentElement) {
    browser.executeScript(`CKEDITOR.instances[arguments[0].id].execCommand('undo')`, contentElement.getWebElement());
  }

  redo(contentElement) {
    return browser.executeScript(`CKEDITOR.instances[arguments[0].id].execCommand('redo')`, contentElement.getWebElement());
  }
}


class ContentArea {
  constructor(tabNumber) {
    this.contentSelector = `.tab-content > :nth-child(${tabNumber}) .idoc-editor-area-wrapper > .ck-editor-area`;
    this.waitUntilLoaded();
  }

  waitUntilLoaded() {
    browser.wait(EC.presenceOf($(this.contentSelector)), DEFAULT_TIMEOUT, 'Content area should be present!');
    browser.wait(() => {
      // CKEDITOR may change the dom element while it initializes and the reference might be lost
      return TestUtils.hasClass($(this.contentSelector), 'initialized');
    }, DEFAULT_TIMEOUT, 'CKEditor should be initialized!');
    this.contentElement = $(this.contentSelector);
  }

  getContentElement() {
    return this.contentElement;
  }

  insertWidget(name) {
    // dismiss any previous opened dialogs
    $('body').sendKeys(protractor.Key.ESCAPE);

    this.executeWidgetCommand(name);
    let widgetElement = this.getWidget(name);
    browser.wait(EC.presenceOf(widgetElement), DEFAULT_TIMEOUT);
    return widgetElement;
  }

  widgetsCount() {
    return element.all(by.css(this.contentSelector + ' [widget]')).count();
  }

  executeWidgetCommand(widgetName) {
    browser.executeScript('CKEDITOR.instances[arguments[0].id].getCommand(arguments[1]).exec()', this.contentElement.getWebElement(), widgetName);
  }

  setContent(content) {
    browser.executeScript('CKEDITOR.instances[arguments[0].id].setData(arguments[1])', this.contentElement.getWebElement(), content);
    // undo plugin must be updated manually
    browser.executeScript('CKEDITOR.instances[arguments[0].id].fire("saveSnapshot")', this.contentElement.getWebElement());
  }

  isFocused() {
    return TestUtils.hasClass(this.contentElement, 'cke_focus');
  }

  type(text) {
    this.contentElement.sendKeys(text);
    browser.executeScript('CKEDITOR.instances[arguments[0].id].fire("saveSnapshot")', this.contentElement.getWebElement());

    return this;
  }

  newLine() {
    this.contentElement.sendKeys(protractor.Key.ENTER);
    browser.executeScript('CKEDITOR.instances[arguments[0].id].fire("saveSnapshot")', this.contentElement.getWebElement());

    return this;
  }

  clear() {
    this.contentElement.clear();
    return this;
  }

  selectAll() {
    this.contentElement.sendKeys(protractor.Key.chord(protractor.Key.CONTROL, 'a'));
    return this;
  }

  click() {
    this.contentElement.click();
    return this;
  }

  getWidget(name) {
    return this.contentElement.$('.' + name);
  }

  getWidgetByNameAndOrder(name, order) {
    return this.contentElement.$$('.' + name).get(order);
  }

  getContentElement() {
    return this.contentElement;
  }

  getAsText() {
    return this.contentElement.getText();
  }

  getParagraph(number) {
    return new Paragraph(this.contentElement.$('p:nth-of-type(' + number + ')'));
  }

  getActivatedParagraphElement(number) {
    let paragraph = this.getParagraph(number);
    paragraph.click();
    return paragraph.getElement();
  }

  getParagraphBySelector(selector) {
    return new Paragraph(this.contentElement.$(selector));
  }

  waitForElementInsideContent(selector) {
    browser.wait(EC.presenceOf(this.contentElement.$(selector)), DEFAULT_TIMEOUT);
  }

  waitForStalenessOfElementInsideContent(selector) {
    browser.wait(EC.stalenessOf(this.contentElement.$(selector)), DEFAULT_TIMEOUT);
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

  getElement() {
    return this.element;
  }

  type(text) {
    browser.actions()
      .mouseMove(this.element)
      .click()
      .sendKeys(text)
      .perform();

    return this;
  }

  getText() {
    return this.element.getText();
  }

  click() {
    browser.wait(EC.elementToBeClickable(this.element), DEFAULT_TIMEOUT);
    this.element.click();
  }
}

IdocPage.VIEW_MODE = VIEW_MODE;
IdocPage.CSS = CSS;
ListEditorMenu.TEXT_STYLE = {
  STRIKE: "Strike",
  UNDERLINE: "Underline (Ctrl+U)",
  SUBSCRIPT: "Subscript",
  SUPERSCRIPT: "Superscript",
  REMOVE_FORMAT: "Remove format"
};

ListEditorMenu.JUSTIFY = {
  OUTDENT: "Outdent",
  JUSTIFY_LEFT: "Justify left",
  JUSTIFY_RIGHT: "Justify right",
  JUSTIFY_CENTER: "Justify"
};

module.exports.IdocPage = IdocPage;
module.exports.ActionsToolbar = ActionsToolbar;
module.exports.FontSizeMenu = FontSizeMenu;
module.exports.FontNameMenu = FontNameMenu;
module.exports.HeadingMenu = HeadingMenu;
module.exports.ListEditorMenu = ListEditorMenu;