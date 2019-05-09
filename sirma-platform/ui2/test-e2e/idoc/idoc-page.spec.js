'use strict';

let IdocPage = require('./idoc-page').IdocPage;
let CSS = require('./idoc-page').IdocPage.CSS;
let VIEW_MODE = require('./idoc-page').IdocPage.VIEW_MODE;
let TestUtils = require('../test-utils');

const IDOC_ID = 'emf:123456';

let idocPage = new IdocPage();

describe('Test idoc page', function () {

  describe('collapse header', function () {
    it('should hide the header when clicked (only in edit mode)', function () {
      idocPage.open(true);
      let toggleBtn = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.visibilityOf(toggleBtn), DEFAULT_TIMEOUT);
      toggleBtn.click();
      return expect(TestUtils.hasClass(element(by.className(CSS.IDOC_CONTEXT_WRAPPER)), CSS.NG_HIDDEN)).to.eventually.be.true;
    });

    it('should show the header when clicked given it had been hidden (only in edit mode)', function () {
      idocPage.open(true);
      let toggleBtn = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.visibilityOf(toggleBtn), DEFAULT_TIMEOUT);
      toggleBtn.click().then(function () {
        toggleBtn.click().then(function () {
          expect(TestUtils.hasClass(element(by.className(CSS.IDOC_CONTEXT_WRAPPER)), CSS.NG_HIDDEN)).to.eventually.be.false;
        });
      });
    });

    it('should not have toggle header button presented in preview mode', function () {
      idocPage.open(false);
      let toggleButton = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.invisibilityOf(toggleButton), DEFAULT_TIMEOUT);
    });
  });

  describe('when operating with idoc', function () {
    it('opens in edit mode', function () {
      idocPage.open(true);
      idocPage.checkViewMode(VIEW_MODE.EDIT);
    });

    it('change url after cancel', function () {
      idocPage.open(true).getActionsToolbar().cancelSave();
      browser.wait(EC.presenceOf(element(by.css('.idoc-mode-preview'))), DEFAULT_TIMEOUT);

      browser.getCurrentUrl().then(function (url) {
        let baseUrl = url.substr(0, url.indexOf('#'));
        expect(browser.getCurrentUrl()).to.eventually.equal(baseUrl + '#/emf:123456?mode=preview');
      });
    });

    it('should focus first row in active editor when new tab is added', () => {
      idocPage.open(true);
      idocPage.getIdocTabs().clickAddTabButton();
      idocPage.getIdocTabs().waitUntilAddTabDialogIsOpened();
      idocPage.getIdocTabs().getConfigTabPopup().clickSaveButton();
      idocPage.getIdocTabs().waitUntilAddTabDialogIsClosed();
      expect(idocPage.getTabEditor(7).isFocused()).to.eventually.be.true;
      expect(idocPage.getTabEditor(1).isFocused()).to.eventually.be.false;
    });

    it('create-edit-update saves all tabs and view modes are properly switched and buttons are properly displayed', () => {
      // Given I have created an idoc
      idocPage.open(true);
      // Given I have created three tabs with some content in the idoc
      return idocPage.setTabsContent('Some content for tab ').then(() => {
        // Given I've saved the idoc
        idocPage.getActionsToolbar().saveIdoc();
        return browser.getCurrentUrl();
      }).then((url) => {
        // Given I've opened the idoc for edit
        idocPage.getActionsToolbar().getActionsMenu().editIdoc();
        idocPage.waitForEditMode();
        // Given I have updated the three tabs content
        return idocPage.setTabsContent('Brand new content for tab ').then(() => {
          // When I save the idoc
          idocPage.getActionsToolbar().saveIdoc();
          return true;
        });
      }).then(() => {
        // Then All three tabs have unchanged content
        let idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (let i = 0; i < tabs.length; i++) {
            idocTabs.getTabByIndex(i).select();
            idocPage.checkSectionContent(i, 'Brand new content for tab ' + i);
          }
        });
      });
    });
  });

  describe('when viewing/editing existing idoc', function () {
    it('loading (view of) an existing idoc', function () {
      idocPage.open(false, IDOC_ID);
      let idocTabs = idocPage.getIdocTabs();
      idocTabs.waitUntilTabsPresent();
      expect(idocTabs.getTabsCount()).to.eventually.equal(3);

      idocPage.checkViewMode(VIEW_MODE.PREVIEW);

      idocTabs.getTabs().then((tabs) => {
        for (let i = 0; i < tabs.length; i++) {
          idocTabs.getTabByIndex(i).select();
          expect(idocTabs.getTabByIndex(i).getTabTitle()).to.eventually.equal('Tab ' + i);
          expect(idocPage.getTabEditor(i + 1).getAsText()).to.eventually.equal('Content tab ' + i);
        }
      });

      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.false;
    });

    it('cancel when editing idoc and exit to preview mode', function (done) {
      idocPage.open(true, IDOC_ID);

      idocPage.checkViewMode(VIEW_MODE.EDIT);

      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.true;

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.getTabEditor(1).clear().type('Modified content in section 0');
      idocPage.getActionsToolbar().cancelSave().then(() => {
        let confirmation = idocPage.getConformationPopup();
        let confirmationElement = confirmation.getConfirmationPopup();
        browser.wait(EC.visibilityOf(confirmationElement), DEFAULT_TIMEOUT);
        confirmation.clickConfirmButton();
        browser.wait(EC.stalenessOf(confirmationElement), DEFAULT_TIMEOUT);

        idocPage.checkViewMode(VIEW_MODE.PREVIEW);
        idocPage.setEdit(false);

        expect(idocPage.getTabEditor(1).getAsText()).to.eventually.equal('Content tab 0');
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.true;
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.false;
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.false;
        done();
      });
    });

    it('should cancel editing idoc and stay in edit mode after confirmation popup answer', function (done) {
      idocPage.open(true, IDOC_ID);

      idocPage.checkViewMode(VIEW_MODE.EDIT);

      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.true;

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.getActionsToolbar().getActionsMenu().editIdoc();
      idocPage.checkViewMode(VIEW_MODE.EDIT);
      idocPage.getTabEditor(1).clear().click().type('Modified content in section 0');
      idocPage.getActionsToolbar().cancelSave().then(() => {
        let confirmation = idocPage.getConformationPopup();
        let confirmationElement = confirmation.getConfirmationPopup();
        browser.wait(EC.visibilityOf(confirmationElement), DEFAULT_TIMEOUT);
        confirmation.clickCancelButton();
        browser.wait(EC.stalenessOf(confirmationElement), DEFAULT_TIMEOUT);

        idocPage.checkViewMode(VIEW_MODE.EDIT);

        expect(idocPage.getTabEditor(1).getAsText()).to.eventually.equal('Modified content in section 0');
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.false;
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.true;
        expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.true;
        done();
      });
    });

    it('switch to edit and save (update) idoc', function () {
      idocPage.open(false, IDOC_ID);

      idocPage.getActionsToolbar().editIdoc();

      idocPage.setTabsContent('Modified content for section ').then(() => {
        idocPage.getActionsToolbar().saveIdoc();
        return true;
      }).then(() => {
        idocPage.setEdit(false);
        let idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (let i = 0; i < tabs.length; i++) {
            idocTabs.getTabByIndex(i).select();
            expect(idocPage.getTabEditor(i + 1).getAsText()).to.eventually.equal('Modified content for section ' + i);
          }
        });
      });
    });
  });

  describe('header actions ', () => {
    it('should have multiple configuable actions', () => {
      idocPage.open(false, IDOC_ID);
      let actions = idocPage.getActionsToolbar().getHeaderActions();
      expect(actions.count()).to.eventually.equal(2);
      expect(actions.count()).to.eventually.not.equal(3);
    });
  });
});
