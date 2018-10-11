'use strict';

let IdocTabs = require('./idoc-tabs').IdocTabs;
let IdocTabsSandboxPage = require('./idoc-tabs').IdocTabsSandboxPage;
let InputField = require('../../form-builder/form-control').InputField;
let IdocPage = require('../idoc-page').IdocPage;

const sandboxPage = new IdocTabsSandboxPage();

const CSS = {
  SHOW_NAVIGATION: 'show-navigation',
  SHOW_COMMENTS: 'show-comments',
  LOCKED: 'locked',
  CONFIGURE_TAB: 'configureIdocTabs',
  DELETE_TAB: 'deleteIdocTabs',
  PREVIEW_MODE: 'preview-mode',
  EDIT_MODE: 'edit-mode',
  CONFIRM_DELETE: 'seip-btn-idoc-tab-confirm-delete',
  CANCEL_DELETE: 'seip-btn-idoc-tab-cancel-delete',
  TAB_TITLE: '.tab-title'
};

function changeTabTitleInPopup() {
  let inputField = new InputField();
  browser.wait(EC.elementToBeClickable(element(by.css(CSS.TAB_TITLE))), DEFAULT_TIMEOUT);
  inputField.clearValue(CSS.TAB_TITLE).then(() => {
    inputField.setValue(CSS.TAB_TITLE, 'Changed Title');
  });
}

describe('Idoc tabs', () => {
  let idocTabs;

  describe('regular tabs', () => {
    beforeEach(() => {
      sandboxPage.open();
      idocTabs = new IdocTabs();
    });

    it('should have a default selected tab', () => {
      expect(idocTabs.getTabByIndex(0).isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(0).getContent().isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(1).isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(1).getContent().isActive()).to.eventually.be.false;
    });

    it('should switch tab', () => {
      idocTabs.getTabByIndex(1).select();

      expect(idocTabs.getTabByIndex(0).isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(0).getContent().isActive()).to.eventually.be.false;
      expect(idocTabs.getTabByIndex(1).isActive()).to.eventually.be.true;
      expect(idocTabs.getTabByIndex(1).getContent().isActive()).to.eventually.be.true;
    });

    it('should keep tab active when mode is changed', () => {
      element(by.id(CSS.PREVIEW_MODE)).click();
      let tab = idocTabs.getTabByIndex(0);
      expect(tab.isActive()).to.eventually.be.true;

      element(by.id(CSS.EDIT_MODE)).click();
      expect(tab.isActive()).to.eventually.be.true;
    });

    describe('configure tab action', () => {
      // This test is temporary skipped because of random fail.
      // As soon as the reason for random fail is found test will be fixed.
      it.skip('should change tab title and visible sections of existing tab after configuration', () => {
        let tab = idocTabs.getTabByIndex(1);
        tab.executeAction(CSS.CONFIGURE_TAB);

        changeTabTitleInPopup();
        idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_NAVIGATION);
        idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_COMMENTS);
        idocTabs.getConfigTabPopup().clickSaveButton();

        expect(tab.getTabTitle()).to.eventually.equal('Changed Title');
        expect(tab.getContent().getNavigationSection().isPresent()).to.become(false);
        expect(tab.getContent().getCommentsSection().isPresent()).to.become(true);
      });
    });

    describe('changing tab title', () => {
      it('should change "Tab1" tab title to "Changed title"', () => {
        let tab = idocTabs.getTabByIndex(0);
        tab.changeTabTitle('Changed title');
        expect(tab.getTabTitle()).to.eventually.equal('Changed title');
      });

      it('should resize tab when title is changed', () => {
        let tab = idocTabs.getTabByIndex(0);
        tab.changeTabTitle('Changed title');
        browser.wait(tab.isTabWidthChanged(), DEFAULT_TIMEOUT);
      });
    });

    describe('add tab action', () => {
      it('should show add tab button in edit mode and hide it in preview mode', () => {
        element(by.id(CSS.PREVIEW_MODE)).click();
        expect(idocTabs.getAddTabButton().isPresent()).to.become(false);
        element(by.id(CSS.EDIT_MODE)).click();

        expect(idocTabs.getAddTabButton().isDisplayed()).to.become(true);
      });

      it('should not add new tab when action is canceled', () => {
        idocTabs.clickAddTabButton();
        idocTabs.getConfigTabPopup().clickCancelButton();

        expect(idocTabs.getTabsCount()).to.eventually.equal(3);
      });

      it('should add new tab with default title and sections configuration to iDoc', () => {
        idocTabs.clickAddTabButton();
        idocTabs.getConfigTabPopup().clickSaveButton();

        expect(idocTabs.getTabsCount()).to.eventually.equal(4);

        let tab = idocTabs.getTabByIndex(3);

        expect(tab.getTabTitle()).to.eventually.equal('Tab4');
        expect(tab.getContent().getNavigationSection().isPresent()).to.become(true);
        expect(tab.getContent().getCommentsSection().isPresent()).to.become(true);
      });

      // This test is temporary skipped because of random fail.
      // As soon as the reason for random fail is found test will be fixed.
      it.skip('should add new tab with title "Changed Title" and hidden comment section to iDoc', () => {
        idocTabs.clickAddTabButton();
        changeTabTitleInPopup();
        idocTabs.getConfigTabPopup().selectSection(CSS.SHOW_COMMENTS);
        idocTabs.getConfigTabPopup().clickSaveButton();

        expect(idocTabs.getTabsCount()).to.eventually.equal(4);
        let tab = idocTabs.getTabByIndex(3);
        expect(tab.getTabTitle()).to.eventually.equal('Changed Title');
        expect(tab.getContent().getNavigationSection().isPresent()).to.become(true);
        expect(tab.getContent().getCommentsSection().isPresent()).to.become(false);
      });

      it('should be able to configure a locked tab', () => {
        idocTabs.clickAddTabButton();
        let tabsConfigDialog = idocTabs.getConfigTabPopup();
        tabsConfigDialog.selectSection(CSS.LOCKED);
        tabsConfigDialog.clickSaveButton();
        expect(idocTabs.getTabsCount()).to.eventually.equal(4);

        let tab = idocTabs.getTabByIndex(3);
        tab.executeAction(CSS.CONFIGURE_TAB);
        tabsConfigDialog = idocTabs.getConfigTabPopup();

        browser.wait(EC.presenceOf(tabsConfigDialog), DEFAULT_TIMEOUT);
      });
    });

    // Fixes: https://jira.sirmaplatform.com/jira/browse/CMF-27700
    // Let these testcases to be executed last because they use another sandbox page which interfere with the other tests.
    describe('prevent injections', () => {
      it('should strip html from the tab title text', () => {
        let testData = [
          {providedTitle: '<video><source onerror="alert(1)">', savedTitle: '<video><source onerror="alert(1)">'},
          {
            providedTitle: '<!-<img src="-><img src=x onerror=alert(1)//">',
            savedTitle: '<!-<img src="-><img src=x onerror=alert(1)//">'
          }
        ];

        // Given I have a document with a tab named by default Tab1
        let idocPage = new IdocPage();
        idocPage.open(true);
        idocTabs = new IdocTabs();
        let tab = idocTabs.getTabByIndex(0);

        testData.forEach((data) => {
          // When I change the tab title providing potentially dangerous string
          tab.changeTabTitle(data.providedTitle);
          idocTabs.getTabByIndex(1).select();
          // And I save the document
          idocPage.getActionsToolbar().saveIdoc(false);
          // Then I expect the value to that is saved to be escaped and to prevent script injection
          expect(tab.getTabTitle()).to.eventually.equal(data.savedTitle);
          idocPage.getActionsToolbar().getActionsMenu().editIdoc();
          idocPage.waitForEditMode();
          expect(tab.getTabTitle()).to.eventually.equal(data.savedTitle);
          // And I don't want the tab config to leak in the idoc page
          let editor = idocPage.getTabEditor(1);
          expect(editor.getAsText()).to.eventually.equal('Content tab 0');
        });
      });
    });
  });

  describe('tab delete action', () => {
    beforeEach(() => {
      let idocPage = new IdocPage();
      idocPage.open(true);
      idocTabs = new IdocTabs();
    });

    it('should delete tab when action is confirmed', () => {
      let tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);
      idocTabs.getDeleteTabPopup().executeAction(CSS.CONFIRM_DELETE);
      expect(idocTabs.getTabsCount()).to.eventually.equal(2);
    });

    // for some reason this test fails if put after the next
    it('should not delete tab when action is canceled', () => {
      let tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);
      idocTabs.getDeleteTabPopup().executeAction(CSS.CANCEL_DELETE);
      expect(idocTabs.getTabsCount()).to.eventually.equal(3);
    });

    it('should not delete last tab in iDoc', () => {
      let tab;
      for (let i = 0; i < 2; i++) {
        tab = idocTabs.getTabByIndex(0);
        tab.executeAction(CSS.DELETE_TAB);
        idocTabs.getDeleteTabPopup().executeAction(CSS.CONFIRM_DELETE);
      }
      tab = idocTabs.getTabByIndex(0);
      tab.executeAction(CSS.DELETE_TAB);

      expect(idocTabs.getNotificationPopup().notificationPopup().isPresent()).to.become(true);

      idocTabs.getNotificationPopup().clickOkButton();
      expect(idocTabs.getTabsCount()).to.eventually.equal(1);
    });
  });

  describe('system tabs', () => {
    beforeEach(() => {
      let idocPage = new IdocPage();
      idocPage.open(true);
      idocTabs = new IdocTabs();
    });

    it('should not allow configuration and removal', () => {
      idocTabs = new IdocTabs(true);
      let tab = idocTabs.getTabByIndex(3);
      tab.select();
      expect(tab.getTabMenu().isPresent()).to.eventually.be.false;
    });

    it('should show tab extension if such is registered', () => {
      idocTabs = new IdocTabs(true);
      let tab = idocTabs.getTabByIndex(3);
      browser.wait(EC.presenceOf(tab.getTabExtension()), DEFAULT_TIMEOUT);
    });
  });
});