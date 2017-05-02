var IdocTabs = require('./idoc-tabs').IdocTabs;
var IdocTabsSandboxPage = require('./idoc-tabs').IdocTabsSandboxPage;
var InputField = require('../../form-builder/form-control').InputField;

const idocTabsSandboxPage = new IdocTabsSandboxPage();

const REPEATER = {
  IDOC_TABS: 'tab in idocTabs.config.tabs',
  IDOC_SECTIONS: 'idocTabsStub.tabsConfig.tabs'
};

const CSS = {

  SHOW_NAVIGATION: 'show-navigation',
  SHOW_COMMENTS: 'show-comments',
  CONFIGURE_TAB: 'configureIdocTabs',
  DELETE_TAB: 'deleteIdocTabs',
  PREVIEW_MODE: 'preview-mode',
  EDIT_MODE: 'edit-mode',
  CONFIRM_DELETE: 'seip-btn-idoc-tab-confirm-delete',
  CANCEL_DELETE: 'seip-btn-idoc-tab-cancel-delete',
  TAB_TITLE: '.tab-title'
};

function changeTabTitleInPopup() {
  var inputField = new InputField();
  browser.wait(EC.elementToBeClickable(element(by.css(CSS.TAB_TITLE))), DEFAULT_TIMEOUT);
  inputField.clearValue(CSS.TAB_TITLE).then(() => {
    inputField.setValue(CSS.TAB_TITLE, 'Changed Title');
  });
}

describe('Test idoc tabs', function () {

  describe('regular tabs', function () {
    var idocTabs;
    beforeEach(() => {
      idocTabsSandboxPage.open();
      idocTabs = new IdocTabs();
    });

    it('default selected tab and tab switching', function () {

      expect(idocTabs.getTabByIndex(0).isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(0).getContent().isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(1).isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(1).getContent().isActive()).to.eventually.be.false;

      idocTabs.selectTabByIndex(1);

      expect(idocTabs.getTabByIndex(0).isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(0).getContent().isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(1).isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(1).getContent().isActive()).to.eventually.be.true;
    });

    it('should add new tab with default title and sections configuration to iDoc', function () {
      idocTabs.clickAddTabButton();
      idocTabs.getConfigTabPopup().clickSaveButton();

      expect(idocTabs.getTabsCount()).to.eventually.equal(4);

      var tab = idocTabs.getTabByIndex(3);
      expect(tab.getTabTitle()).to.eventually.equal('Tab4');
      expect(tab.getContent().getNavigationSection().isPresent()).to.become(true);
      expect(tab.getContent().getCommentsSection().isPresent()).to.become(true);
    });

    // This test is temporary skipped because of random fail.
    // As soon as the reason for random fail is found test will be fixed.
    it.skip('should add new tab with title "Changed Title" and hidden comment section to iDoc', function () {
      idocTabs.clickAddTabButton();
      changeTabTitleInPopup();
      idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_COMMENTS);
      idocTabs.getConfigTabPopup().clickSaveButton();

      expect(idocTabs.getTabsCount()).to.eventually.equal(4);
      var tab = idocTabs.getTabByIndex(3);
      expect(tab.getTabTitle()).to.eventually.equal('Changed Title');
      expect(tab.getContent().getNavigationSection().isPresent()).to.become(true);
      expect(tab.getContent().getCommentsSection().isPresent()).to.become(false);
    });

    it('should not add new tab when action is canceled', function () {
      idocTabs.clickAddTabButton();
      idocTabs.getConfigTabPopup().clickCancelButton();

      expect(idocTabs.getTabsCount()).to.eventually.equal(3);
    });

    it('should show add tab button in edit mode and hide it in preview mode', function () {
      element(by.id(CSS.PREVIEW_MODE)).click();
      expect(idocTabs.getAddTabButton().isPresent()).to.become(false);
      element(by.id(CSS.EDIT_MODE)).click();
      expect(idocTabs.getAddTabButton().isDisplayed()).to.become(true);
    });

    it('should change "Tab1" tab title to "Changed title"', function () {
      var tab = idocTabs.getTabByIndex(0);
      tab.changeTabTitle('Changed title');
      expect(tab.getTabTitle()).to.eventually.equal('Changed title');
    });

    it('should resize tab when title is changed', function () {
      var tab = idocTabs.getTabByIndex(0);
      tab.changeTabTitle('Changed title');
      browser.wait(tab.isTabWidthChanged(), DEFAULT_TIMEOUT);
    });

    it('should keep tab active when mode is changed', function () {
      element(by.id(CSS.PREVIEW_MODE)).click();
      var tab = idocTabs.getTabByIndex(0);
      expect(tab.isActive()).to.eventually.be.true;

      element(by.id(CSS.EDIT_MODE)).click();
      expect(tab.isActive()).to.eventually.be.true;
    });

    // This test is temporary skipped because of random fail.
    // As soon as the reason for random fail is found test will be fixed.
    it.skip('should change tab title and visible sections of existing tab after configuration', function () {
      var tab = idocTabs.getTabByIndex(1);
      tab.executeAction(CSS.CONFIGURE_TAB);

      changeTabTitleInPopup();
      idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_NAVIGATION);
      idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_COMMENTS);
      idocTabs.getConfigTabPopup().clickSaveButton();

      expect(tab.getTabTitle()).to.eventually.equal('Changed Title');
      expect(tab.getContent().getNavigationSection().isPresent()).to.become(false);
      expect(tab.getContent().getCommentsSection().isPresent()).to.become(true);
    });

    it('should delete tab when action is confirmed', function () {
      var tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);
      idocTabs.getDeleteTabPopup().executeAction(CSS.CONFIRM_DELETE);

      expect(idocTabs.getTabsCount()).to.eventually.equal(2);
    });

    // for some reason this test fails if put after the next
    it('should not delete tab when action is canceled', function () {
      var tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);
      idocTabs.getDeleteTabPopup().executeAction(CSS.CANCEL_DELETE);

      expect(idocTabs.getTabsCount()).to.eventually.equal(3);
    });

    it('should not delete last tab in iDoc', function () {
      var tab;
      for (var i = 0; i < 2; i++) {
        tab = idocTabs.getTabByIndex(0);
        tab.executeAction(CSS.DELETE_TAB);
        idocTabs.getDeleteTabPopup().executeAction(CSS.CONFIRM_DELETE);
      }
      var tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);

      expect(idocTabs.getNotificationPopup().notificationPopup().isPresent()).to.become(true);
      idocTabs.getNotificationPopup().clickOkButton();
      expect(idocTabs.getTabsCount()).to.eventually.equal(1);
    });

  });

  describe('system tabs', function () {
    it('should not allow configuration and removal', function () {
      idocTabsSandboxPage.open(true);

      var idocTabs = new IdocTabs(true);
      var tab = idocTabs.getTabByIndex(3);
      tab.select();

      expect(tab.getTabMenu().isPresent()).to.eventually.be.false;
    });
  });

});