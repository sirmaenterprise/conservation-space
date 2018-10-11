'use strict';

let TestUtils = require('../../test-utils');
let NotificationPopup = require('../../components/dialog/notification-popup');
let SandboxPage = require('../../page-object').SandboxPage;

const IDOC_TABS_PAGE_URL = '/sandbox/idoc/idoc-tabs';

const CSS = {
  CONFIGURE_TAB_ACTIONS: 'modal-footer',
  ADD_TAB_DIALOG: 'modal-dialog'
};

class IdocTabsSandboxPage extends SandboxPage {

  open(systemTab) {
    let url = IDOC_TABS_PAGE_URL;

    let hash;
    if (systemTab) {
      hash = 'addSystemTab=true';
    }

    super.open(url, hash);
    return this;
  }
}

class IdocTab {
  constructor(element, index) {
    this.element = element;
    this.index = index;
  }

  getTabInput() {
    return this.element.element(by.tagName('input'));
  }

  getTabTitle() {
    return this.getTabInput().getAttribute('value');
  }

  isTabWidthChanged() {
    return this.getTabInput().getSize().then(function (eleSize) {
      return eleSize.width > 70;
    });
  }

  changeTabTitle(title) {
    browser.wait(EC.elementToBeClickable(this.getTabInput()), DEFAULT_TIMEOUT);
    this.getTabInput().click().clear().sendKeys(title);
  }

  select() {
    browser.wait(EC.elementToBeClickable(this.getTabInput()), DEFAULT_TIMEOUT);
    return this.getTabInput().click();
  }

  getTabMenu() {
    return new TabMenu(this.element.element(by.className('idoc-tabs-menu')));
  }

  getTabExtension() {
    return this.element.element(by.className('unread-messages-counter'));
  }

  executeAction(action) {
    this.select().then(() => {
      let tabMenu = this.getTabMenu();
      tabMenu.open().then(function () {
        tabMenu.chooseAction(action);
      });
    });
  }

  openTabConfiguration() {
    this.executeAction('configureIdocTabs');
    return new ConfigTabPopup();
  }

  isActive() {
    return TestUtils.hasClass(this.element, 'active');
  }

  getContent() {
    return new TabContent(element.all(by.css('.tab-content section')).get(this.index));
  }

}

class TabContent {
  constructor(element) {
    this.element = element;
  }

  getContentArea() {
    //TODO move ContentArea from IdocPage po to new file and use it here
    return this.element.element(by.className('idoc-editor-wrapper'));
  }

  getNavigationSection() {
    return this.element.element(by.className('idoc-navigation-wrapper'));
  }

  getCommentsSection() {
    return this.element.element(by.className('idoc-comments-wrapper'));
  }

  isActive() {
    return TestUtils.hasClass(this.element, 'active');
  }
}

class TabMenu {
  constructor(element) {
    this.element = element;
  }

  open() {
    browser.wait(EC.elementToBeClickable(this.element), DEFAULT_TIMEOUT);
    return this.element.click();
  }

  chooseAction(action) {
    let tabMenuAction = this.element.element(by.className(action));
    browser.wait(EC.elementToBeClickable(tabMenuAction), DEFAULT_TIMEOUT);
    tabMenuAction.click();
  }

  isPresent() {
    return this.element.isPresent();
  }
}

class IdocTabs {

  constructor(includeSystemTabs) {
    this.includeSystemTabs = includeSystemTabs;
  }

  getTabs() {
    let selector = '.idoc-tabs .tab-item';

    if (!this.includeSystemTabs) {
      selector += ':not(.system-tab)';
    }
    return element.all(by.css(selector));
  }

  getTabsCount() {
    return this.getTabs().count();
  }

  getTabByIndex(index) {
    browser.wait(() => {
      return this.getTabs().count().then(function (count) {
        return count > index;
      });
    }, DEFAULT_TIMEOUT, `Wait for tab ${index} to appear`);

    return new IdocTab(this.getTabs().get(index), index);
  }

  getTabById(id) {
    let tabElement = $('.idoc-tabs .tab-item.' + id);
    browser.wait(EC.visibilityOf(tabElement), DEFAULT_TIMEOUT);

    return new IdocTab(tabElement, null);
  }

  getAddTabButton() {
    return element(by.className('idoc-tabs')).element(by.className('fa-plus'));
  }

  clickAddTabButton() {
    browser.wait(EC.elementToBeClickable(this.getAddTabButton()), DEFAULT_TIMEOUT);
    this.getAddTabButton().click();
  }

  waitUntilAddTabDialogIsOpened() {
    browser.wait(EC.visibilityOf(element(by.className(CSS.ADD_TAB_DIALOG))), DEFAULT_TIMEOUT);
    return this;
  }

  waitUntilAddTabDialogIsClosed() {
    browser.wait(EC.invisibilityOf(element(by.className(CSS.ADD_TAB_DIALOG))), DEFAULT_TIMEOUT);
    return this;
  }

  getConfigTabPopup() {
    return new ConfigTabPopup();
  }

  getDeleteTabPopup() {
    return new DeleteTabPopup();
  }

  getNotificationPopup() {
    return new NotificationPopup();
  }

  waitUntilTabsPresent() {
    browser.wait(EC.presenceOf($('.idoc-tabs .tab-item.active')), DEFAULT_TIMEOUT);
    return this;
  }
}

class ConfigTabPopup {
  constructor() {
    this.dialog = $('.modal-dialog');
    browser.wait(EC.visibilityOf(this.dialog), DEFAULT_TIMEOUT, 'Tab configuration should be opened!');
    this.popupFooter = this.dialog.element(by.className(CSS.CONFIGURE_TAB_ACTIONS));
  }

  selectSection(section) {
    let sectionCheckbox = this.dialog.element(by.className(section));
    browser.wait(EC.elementToBeClickable(sectionCheckbox), DEFAULT_TIMEOUT);
    sectionCheckbox.click();
  }

  lockTab() {
    this.selectSection('locked');
    return this;
  }

  clickSaveButton() {
    let saveBtn = this.popupFooter.element(by.className('seip-btn-save'));
    browser.wait(EC.elementToBeClickable(saveBtn), DEFAULT_TIMEOUT);
    saveBtn.click();
    browser.wait(EC.not(EC.presenceOf(element(by.className('modal')))), DEFAULT_TIMEOUT);
  }

  save() {
    this.clickSaveButton();
  }

  clickCancelButton() {
    let cancelBtn = this.popupFooter.element(by.className('seip-btn-cancel'));
    browser.wait(EC.elementToBeClickable(cancelBtn), DEFAULT_TIMEOUT);
    cancelBtn.click();
  }

  isPresent() {
    return this.popupFooter.isPresent();
  }
}

class DeleteTabPopup {

  executeAction(action) {
    let confirmationPopup = element(by.className('modal-dialog'));
    let actionButton = confirmationPopup.element(by.className(action));
    browser.wait(EC.elementToBeClickable(actionButton), DEFAULT_TIMEOUT);
    actionButton.click();
    browser.wait(EC.stalenessOf(confirmationPopup), DEFAULT_TIMEOUT);
  }
}

module.exports = {
  IdocTabs,
  IdocTabsSandboxPage
};