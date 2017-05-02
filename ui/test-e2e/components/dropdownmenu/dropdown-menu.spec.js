'use strict';
var DropdownMenu = require('./dropdown-menu').DropdownMenu;
var SandboxPage = require('../../page-object').SandboxPage;

const DROPDOWN_MENU_URL = '/sandbox/components/dropdown-menu';

const BUTTON_MENU_TRIGGER_SELECTOR = '.contextual-actions-menu';
const GLYPH_MENU_TRIGGER_SELECTOR = '.menu-with-glyph .dropdownmenu-wrapper';
const BUTTON_MENU_ASYNC_TRIGGER_SELECTOR = '.contextual-actions-async-menu';

var page = new SandboxPage();

describe('Dropdown menu', function () {

  var menu;

  beforeEach(function () {
    page.open(DROPDOWN_MENU_URL);
    menu = new DropdownMenu($(BUTTON_MENU_TRIGGER_SELECTOR));
  });

  it('should has trigger button', function () {
    expect(menu.getTriggerButton().isDisplayed()).to.eventually.be.true;
  });

  it('should has trigger label', function () {
    expect(menu.getTriggerButton().getText()).to.eventually.be.equal('New');
  });

  it('should be displayed action menu on trigger button click', function () {
    menu.open();
  });

  it('should has loading indicator on trigger button click', function () {
    let asyncMenu = new DropdownMenu($(BUTTON_MENU_ASYNC_TRIGGER_SELECTOR));
    asyncMenu.hasLoadingIndicator();
  });

  it('should has actions inside action menu', function () {
    menu.open();
    expect(menu.getActions().count()).to.eventually.be.equal(9);
  });

  it('should has preselected action when model contains selected:true', function () {
    menu.open();
    let activeAction = menu.getActiveAction().isDisplayed();
    expect(activeAction.isDisplayed()).to.eventually.be.true;
    expect(activeAction.getText()).to.eventually.be.equal('Project');
  });

  it('should has action with confirm=true that opens confirmation dialog', function () {
    menu.open();
    menu.getActiveAction().click().then(function () {
      let modal = element(by.css('.modal'));
      browser.wait(EC.visibilityOf(modal), DEFAULT_TIMEOUT);
    });
  });

  it('should has action with custom confirmation message that opens confirmation dialog', function () {
    menu.open();
    let caseAction = menu.getActions().get(1);
    caseAction.click().then(function () {
      let modal = element(by.css('.modal'));
      browser.wait(EC.visibilityOf(modal), DEFAULT_TIMEOUT);
    });
  });

  it('should has action that will invoke handler', function () {
    menu.open();
    let documentAction = menu.getActions().get(2);
    documentAction.click().then(function () {
      let test = element(by.css('.test'));
      browser.wait(EC.visibilityOf(test), DEFAULT_TIMEOUT);
    });
  });

  it('should be positioned correctly near the trigger button', function () {
    let menuWithGlyph = new DropdownMenu($(GLYPH_MENU_TRIGGER_SELECTOR));
    menuWithGlyph.open();
    menuWithGlyph.getTriggerButton().getLocation().then((buttonLocation) => {
        menuWithGlyph.getActionContainer().getLocation().then((menuLocation) => {
          expect(menuLocation.x).to.be.below(buttonLocation.x);
          expect(menuLocation.y).to.be.above(buttonLocation.y);
        });
      }
    );
  });

  it('should show submenu', function () {
    menu.open();
    let submenu = menu.getSubmenu();
    expect(submenu.isDisplayed()).to.eventually.be.false;
    menu.showSubmenu();
    expect(submenu.isDisplayed()).to.eventually.be.true;
  });
});