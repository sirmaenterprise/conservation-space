var IdocPage = require('./idoc-page');

const hasClass = function (element, cls) {
  return element.getAttribute('class').then(function (classes) {
    return classes.split(' ').indexOf(cls) !== -1;
  });
};

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
  BTN_EDIT: 'seip-btn-edit',
  BTN_CANCEL: 'seip-btn-cancel',
  BTN_TOGGLE_HEADER: 'seip-btn-toggle-header',
  IDOC_TABS: 'idoc-tabs'
};

const IDOC_ID = 'emf:123456';

var idocPage = new IdocPage();

describe('Test idoc page', function () {

  /**
   * Expected condition for existence of multiple elements selected with element.all
   * @param elements
   * @returns {Function}
   */
  function existanceOf(elements) {
    return elements.count().then(function (count) {
      return count > 0;
    });
  }

  /**
   * Check which is currently active tab by checking for presence of 'active' class
   * @returns {Promise} which resolves with active tab index
   */
  function getActiveTabIndex() {
    return browser.executeScript('return $(".tab-item").index($(".tab-item.active"));');
  }

  /**
   * Performs check for expected view mode
   * @param mode expected view mode. Use VIEW_MODE constant
   */
  function checkViewMode(mode) {
    var checkMode = getActiveTabIndex().then(function (index) {
      var activeSection = element(by.repeater(REPEATER.IDOC_TABS_CONTENT).row(index));
      var wrapperElement = activeSection.element(by.css('.' + CSS.TAB_SECTIONS + ' > .' + CSS.IDOC_EDITOR_AREA_WRAPPER));
      browser.wait(EC.visibilityOf(wrapperElement), DEFAULT_TIMEOUT);
      expect(wrapperElement.isDisplayed()).to.eventually.be.true;

      var expectedResult = mode === 'edit';
      expect(hasClass(wrapperElement, CSS.CK_EDITOR_EDITABLE), 'Expected view mode to be ' + mode).to.eventually.equal(expectedResult);
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
  function checkSectionContent(index, expectedContent) {
    var sections = element.all(by.repeater(REPEATER.IDOC_TABS_CONTENT));
    browser.wait(existanceOf(sections), DEFAULT_TIMEOUT);
    expect(sections.get(index).element(by.css('.' + CSS.IDOC_EDITOR_AREA_WRAPPER + ' > div')).getText()).to.eventually.equal(expectedContent);
  }

  /**
   * Replace section content with new content
   * @param index section index
   * @param newContent
   */
  function setSectionContent(index, newContent) {
    var sections = element.all(by.repeater(REPEATER.IDOC_TABS_CONTENT));
    browser.wait(existanceOf(sections), DEFAULT_TIMEOUT);
    var editor = sections.get(index).element(by.css('.' + CSS.IDOC_EDITOR_AREA_WRAPPER + ' > div'));
    browser.wait(EC.presenceOf(editor), DEFAULT_TIMEOUT);
    editor.click().clear().sendKeys(newContent);
  }

  /**
   * Sets content for all tabs appending tab's index at the end.
   * @param idocPage
   * @param content text to be set in the tab
   * @returns {*}
   */
  function setTabsContent(idocPage, content) {
    var idocTabs = idocPage.getIdocTabs();
    return idocTabs.getTabs().then((tabs) => {
      for (var i = 0; i < tabs.length; i++) {
        idocTabs.selectTabByIndex(i);
        idocPage.getTabEditor(i + 1).clear().type(content + i);
      }
      return true;
    });
  }

  describe('collapse header', function () {
    it('should hide the header when clicked (only in edit mode)', function () {
      idocPage.open(true);
      var toggleBtn = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.visibilityOf(toggleBtn), DEFAULT_TIMEOUT);
      toggleBtn.click();
      return expect(hasClass(element(by.className(CSS.IDOC_CONTEXT_WRAPPER)), CSS.NG_HIDDEN)).to.eventually.be.true;
    });

    it('should show the header when clicked given it had been hidden (only in edit mode)', function () {
      idocPage.open(true);
      var toggleBtn = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.visibilityOf(toggleBtn), DEFAULT_TIMEOUT);
      toggleBtn.click().then(function () {
        toggleBtn.click().then(function () {
          expect(hasClass(element(by.className(CSS.IDOC_CONTEXT_WRAPPER)), CSS.NG_HIDDEN)).to.eventually.be.false;
        });
      });
    });

    it('should not have toggle header button presented in preview mode', function () {
      idocPage.open(false);
      var toggleButton = element(by.className(CSS.BTN_TOGGLE_HEADER));
      browser.wait(EC.invisibilityOf(toggleButton), DEFAULT_TIMEOUT);
    });
  });

  describe('when creating new idoc', function () {
    it('should have the template selector present', () => {
      idocPage.open(true);
      checkViewMode(VIEW_MODE.EDIT);

      browser.wait(EC.visibilityOf($('.idoc-template-selector')), DEFAULT_TIMEOUT);
    });

    it('opens in edit mode', function () {
      idocPage.open(true);
      checkViewMode(VIEW_MODE.EDIT);
    });

    it('default template is loaded', function () {
      idocPage.open(true);
      var tabs = idocPage.getIdocTabs();

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

    it('cancel when creating idoc', function () {
      idocPage.open(true).getActionsToolbar().cancelSave();
      browser.getCurrentUrl().then(function (url) {
        var baseUrl = url.substr(0, url.indexOf('#'));
        expect(browser.getCurrentUrl()).to.eventually.equal(baseUrl + '#/returnUrl');
      });
    });

    it('save (create) idoc and validates that all tabs are saved and template selector removed', function () {
      // Given I have created an idoc
      idocPage.open(true);
      // Given I have three tabs with some content
      // Given I am on the third tab
      setTabsContent(idocPage, 'Some content for tab ').then(() => {
        // When I save the idoc
        idocPage.getActionsToolbar().saveIdoc();
        return browser.getCurrentUrl();
      }).then((url) => {
        // Then View mode should be set to preview
        checkViewMode(VIEW_MODE.PREVIEW);
        // Then The active (selected) tab should the one that I've been before the save operation
        expect(getActiveTabIndex()).to.eventually.equal(0);
        // Then the tabs content should be saved
        var idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (var i = 0; i < tabs.length; i++) {
            idocTabs.selectTabByIndex(i);
            checkSectionContent(i, 'Some content for tab ' + i);
          }
        });
        // Then The idoc edit buttons should be present
        expect(element(by.className(CSS.IDOC_CONTEXT_ACTIONS_WRAPPER)).element(by.className(CSS.BTN_EDIT)).isPresent()).to.eventually.be.true;
        // Then The idoc cancel buttons should be missing
        expect(element(by.className(CSS.IDOC_CONTEXT_ACTIONS_WRAPPER)).element(by.className(CSS.BTN_CANCEL)).isPresent()).to.eventually.be.false;
        // Then The idoc edit buttons should be missing
        expect(element(by.className(CSS.IDOC_CONTEXT_ACTIONS_WRAPPER)).element(by.className(CSS.BTN_SAVE)).isPresent()).to.eventually.be.false;
        // When I refresh the page
        return browser.getCurrentUrl();
      }).then((url) => {
        // Then I expect the tabs content to be present
        var idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (var i = 0; i < tabs.length; i++) {
            idocTabs.selectTabByIndex(i);
            checkSectionContent(i, 'Some content for tab ' + i);
          }
        });

        // template selector is removed
        return idocPage.getActionsToolbar().templateSelector.isPresent().then(present => expect(present).to.be.false);
      });
    });

    it('should automatically add permissions tab', function () {
      // Given I open the idoc page
      idocPage.open(true);

      // When I save it
      var actions = idocPage.getActionsToolbar();
      actions.saveIdoc();
      browser.wait(idocPage.isSaved, DEFAULT_TIMEOUT);

      // And refresh it
      browser.refresh();

      // And click on the permissions tab
      idocPage.getIdocTabs(true).selectTabByIndex(3);

      // Then I should see the permissions panel
      browser.wait(EC.textToBePresentInElement(idocPage.getSystemTabContent(4).$('.permissions'), 'Permissions'), DEFAULT_TIMEOUT);

      // And the editor toolbar should not be displayed
      browser.wait(EC.not(EC.visibilityOf(idocPage.getActionsToolbar().editorToolbar)), DEFAULT_TIMEOUT);
    });

    it('should hide template selector after clicking save and then edit', () => {
      idocPage.open(true);

      var actions = idocPage.getActionsToolbar();
      var tplSelector = actions.getTemplateSelector();

      //action menu dropdown shouln't be displayed in edit
      expect(element(by.css('.actions-menu')).isDisplayed()).to.eventually.be.false;
      actions.saveIdoc();
      browser.wait(idocPage.isSaved, DEFAULT_TIMEOUT);
      //action menu dropdown should be displayed in preview
      expect(element(by.css('.actions-menu')).isDisplayed()).to.eventually.be.true;
      actions.editIdoc();

      return tplSelector.isPresent().then(present => expect(present).to.be.false);
    });

    it('should focus first row in active editor when new tab is added', function (done) {
      idocPage.open(true);
      idocPage.getIdocTabs().clickAddTabButton();
      idocPage.getIdocTabs().getConfigTabPopup().clickSaveButton();

      expect(idocPage.getTabEditor(1).isFocused()).to.eventually.be.false.notify(done);
      expect(idocPage.getTabEditor(2).isFocused()).to.eventually.be.true.notify(done);
    });

    it('create-edit-update saves all tabs and view modes are properly switched and buttons are properly displayed', () => {
      // Given I have created an idoc
      idocPage.open(true);
      // Given I have created three tabs with some content in the idoc
      return setTabsContent(idocPage, 'Some content for tab ').then(() => {
        // Given I've saved the idoc
        idocPage.getActionsToolbar().saveIdoc();
        return browser.getCurrentUrl();
      }).then((url) => {
        // Given I've opened the idoc for edit
        idocPage.getActionsToolbar().editIdoc();
        // Given I have updated the three tabs content
        return setTabsContent(idocPage, 'Brand new content for tab ').then(() => {
          // When I save the idoc
          idocPage.getActionsToolbar().saveIdoc();
          return true;
        });
      }).then(() => {
        // Then All three tabs have unchanged content
        var idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (var i = 0; i < tabs.length; i++) {
            idocTabs.selectTabByIndex(i);
            checkSectionContent(i, 'Brand new content for tab ' + i);
          }
        });
      });
    });
  });

  describe('when viewing/editing existing idoc', function () {
    it('loading (view of) an existing idoc', function () {
      idocPage.open(false, IDOC_ID);
      expect(idocPage.getIdocTabs().waitUntilTabsPresent().getTabsCount()).to.eventually.equal(3);

      checkViewMode(VIEW_MODE.PREVIEW);

      var idocTabs = idocPage.getIdocTabs();
      idocTabs.getTabs().then((tabs) => {
        for (var i = 0; i < tabs.length; i++) {
          idocTabs.selectTabByIndex(i);
          expect(idocTabs.getTabByIndex(i).getTabTitle()).to.eventually.equal('Tab ' + i);
          expect(idocPage.getTabEditor(i + 1).getAsText()).to.eventually.equal('Content tab ' + i);
        }
      });

      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.false;
    });

    it('cancel when editing idoc', function () {
      idocPage.open(true, IDOC_ID);

      checkViewMode(VIEW_MODE.EDIT);

      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.true;

      idocPage.getActionsToolbar().saveIdoc();
      idocPage.getActionsToolbar().editIdoc();
      idocPage.getTabEditor(1).clear().type('Modified content in section 0');
      idocPage.getActionsToolbar().cancelSave();

      checkViewMode(VIEW_MODE.PREVIEW);
      idocPage.setEdit(false);

      expect(idocPage.getTabEditor(1).getAsText()).to.eventually.equal('Content tab 0');
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_EDIT)).to.eventually.be.true;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_CANCEL)).to.eventually.be.false;
      expect(idocPage.getActionsToolbar().isButtonPresent(CSS.BTN_SAVE)).to.eventually.be.false;
    });

    it('switch to edit and save (update) idoc', function () {
      idocPage.open(false, IDOC_ID);

      idocPage.getActionsToolbar().editIdoc();

      setTabsContent(idocPage, 'Modified content for section ').then(() => {
        idocPage.getActionsToolbar().saveIdoc();
        return true;
      }).then(() => {
        idocPage.setEdit(false);
        var idocTabs = idocPage.getIdocTabs();
        idocTabs.getTabs().then((tabs) => {
          for (var i = 0; i < tabs.length; i++) {
            idocTabs.selectTabByIndex(i);
            expect(idocPage.getTabEditor(i + 1).getAsText()).to.eventually.equal('Modified content for section ' + i);
          }
        });
      });
    });
  });
});
