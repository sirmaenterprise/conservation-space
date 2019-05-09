'use strict';

let IdocPage = require('../idoc-page').IdocPage;
let CSS = require('../idoc-page').IdocPage.CSS;
let VIEW_MODE = require('../idoc-page').IdocPage.VIEW_MODE;

let idocPage = new IdocPage('/sandbox/idoc/template/idoc-change-template');

describe('Test idoc change template', () => {

  it('should have the template selector present', () => {
    idocPage.open(true);

    idocPage.checkViewMode(VIEW_MODE.EDIT);

    browser.wait(EC.visibilityOf($('.idoc-template-selector')), DEFAULT_TIMEOUT);
  });

  it('should load default template', () => {
    idocPage.open(true, '', '/sandbox/idoc/template/idoc-change-template');
    let tabs = idocPage.getIdocTabs();

    // check number of tabs and default (active) tab
    expect(tabs.getTabsCount()).to.eventually.equal(1);
    expect(tabs.getTabByIndex(0).isActive()).to.eventually.be.true;

    // check default (active) sections (tab content)
    expect(tabs.getTabByIndex(0).getContent().isActive()).to.eventually.be.true;

    // check when navigation column is present depending on the tab configuration
    expect(tabs.getTabByIndex(0).getContent().getNavigationSection().isPresent()).to.eventually.be.true;

    // check when comments column is present depending on the tab configuration
    expect(tabs.getTabByIndex(0).getContent().getCommentsSection().isPresent()).to.eventually.be.true;
  });

  it('should hide template selector and save all tabs after idoc save/create', () => {
    // Given I have created an idoc
    idocPage.open(true, '', '/sandbox/idoc/template/idoc-change-template');
    // Given I have three tabs with some content
    // Given I am on the third tab
    idocPage.setTabsContent('Some content for tab ').then(() => {
      // When I save the idoc
      idocPage.getActionsToolbar().saveIdoc();
      return browser.getCurrentUrl();
    }).then((url) => {
      // Then View mode should be set to preview
      idocPage.checkViewMode(VIEW_MODE.PREVIEW);
      // Then The active (selected) tab should the one that I've been before the save operation
      expect(idocPage.getActiveTabIndex()).to.eventually.equal(0);
      // Then the tabs content should be saved
      let idocTabs = idocPage.getIdocTabs();
      idocTabs.getTabs().then((tabs) => {
        for (let i = 0; i < tabs.length; i++) {
          idocTabs.getTabByIndex(i).select();
          idocPage.checkSectionContent(i, 'Some content for tab ' + i);
        }
      });

      // Then The idoc cancel buttons should be missing
      expect(element(by.className(CSS.IDOC_CONTEXT_ACTIONS_WRAPPER)).element(by.className(CSS.BTN_CANCEL)).isPresent()).to.eventually.be.false;
      // Then The idoc edit buttons should be missing
      expect(element(by.className(CSS.IDOC_CONTEXT_ACTIONS_WRAPPER)).element(by.className(CSS.BTN_SAVE)).isPresent()).to.eventually.be.false;
      // When I refresh the page
      return browser.getCurrentUrl();
    }).then(() => {
      // Then I expect the tabs content to be present
      let idocTabs = idocPage.getIdocTabs();
      idocTabs.getTabs().then((tabs) => {
        for (let i = 0; i < tabs.length; i++) {
          idocTabs.getTabByIndex(i).select();
          idocPage.checkSectionContent(i, 'Some content for tab ' + i);
        }
      });

      // template selector is removed
      return idocPage.getActionsToolbar().templateSelector.isPresent().then(present => expect(present).to.be.false);
    });
  });

  it('should hide template selector after clicking save and then edit', () => {
    idocPage.open(true);
    let actions = idocPage.getActionsToolbar();
    let tplSelector = actions.getTemplateSelector();
    let actionsMenu = actions.getActionsMenu();
    //action menu dropdown shouln't be displayed in edit
    expect(actionsMenu.isDisplayed()).to.eventually.be.false;
    actions.saveIdoc();
    browser.wait(idocPage.isSaved, DEFAULT_TIMEOUT);
    //action menu dropdown should be displayed in preview
    expect(actionsMenu.isDisplayed()).to.eventually.be.true;
    actionsMenu.editIdoc();

    return tplSelector.isPresent().then(present => expect(present).to.be.false);
  });
});