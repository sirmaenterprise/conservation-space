'use strict';
var DropdownMenu = require('./dropdown-menu').DropdownMenu;
var SandboxPage = require('../../page-object').SandboxPage;

const DROPDOWN_MENU_URL = '/sandbox/components/dropdown-menu';

const BUTTON_MENU_TRIGGER_SELECTOR = '.contextual-actions-menu';
const BUTTON_MENU_GLYPH_TRIGGER_SELECTOR = '.contextual-actions-glyph-menu';
const BUTTON_MENU_ASYNC_TRIGGER_SELECTOR = '.contextual-actions-async-menu';
const BUTTON_MENU_SORTED_TRIGGER_SELECTOR = '.contextual-actions-sorted-menu';
const BUTTON_MENU_PLUGIN_TRIGGER_SELECTOR = '.contextual-actions-plugin-menu';

var page = new SandboxPage();
describe('Dropdown menu', function () {

  beforeEach(function () {
    page.open(DROPDOWN_MENU_URL);
  });

  describe('Simple dropdown menu', function () {
    var menu;

    beforeEach(function () {
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

    it('should has actions inside action menu', function () {
      menu.open();
      // actual menu elements (actions) are five & one for the divider
      expect(menu.getActions().count()).to.eventually.be.equal(6);
    });

    it('should has preselected action when model contains selected:true', function () {
      menu.open();
      let activeAction = menu.getActiveAction();
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

    it('should show submenu', function () {
      menu.open();
      let submenu = menu.getSubmenu();
      expect(submenu.isDisplayed()).to.eventually.be.false;
      menu.showSubmenu();
      expect(submenu.isDisplayed()).to.eventually.be.true;
    });
  });

  describe('Special dropdown menu', function () {

    it('should has loading indicator on trigger button click when async menu type', function () {
      let asyncMenu = new DropdownMenu($(BUTTON_MENU_ASYNC_TRIGGER_SELECTOR));
      asyncMenu.hasLoadingIndicator();
    });

    it('should be positioned correctly near the trigger button when glyph menu type', function () {
      let menuWithGlyph = new DropdownMenu($(BUTTON_MENU_GLYPH_TRIGGER_SELECTOR));
      menuWithGlyph.open();
      menuWithGlyph.getTriggerButton().getLocation().then((buttonLocation) => {
          menuWithGlyph.getActionContainer().getLocation().then((menuLocation) => {
            expect(menuLocation.x <= buttonLocation.x).to.be.true;
            expect(menuLocation.y >= buttonLocation.y).to.be.true;
          });
        }
      );
    });

    it('should have all menu elements sorted alphabetically when plugin menu type', function () {
      let menuTree = [{
        label: 'Case'
      }, {
        label: 'Document'
      }, {
        label: 'Project'
      }];
      // assert that the drop down menu has a correct structure
      assertMenuStructure($(BUTTON_MENU_PLUGIN_TRIGGER_SELECTOR), menuTree);
    });

    it('should have all menu elements sorted alphabetically when sort menu type', function () {
      let menuTree = [{
        label: 'Case'
      }, {
        label: 'Document'
      }, {
        label: 'More',
        elements: [{
          label: 'Document'
        }, {
          label: 'More',
          elements: [{
            label: 'Document'
          }]
        }]
      }, {
        label: 'Project'
      }, {
        label: '' // divider
      }, {
        label: 'Task'
      }];
      // assert that the drop down menu has a correct structure
      assertMenuStructure($(BUTTON_MENU_SORTED_TRIGGER_SELECTOR), menuTree);
    });

    function assertMenuStructure(menuElement, menuTree) {
      let menu = new DropdownMenu(menuElement);
      menu.open();
      let actions = menu.getActions();
      actions.each((action, index) => {
        let item = menuTree[index];
        DropdownMenu.getActionLabel(action).then(expectedLabel => {
          // first try to assert action label is as expected
          expect(expectedLabel).to.equal(item.label);

          if(item && item.length) {
            // validate submenu after validation of current item
            DropdownMenu.hasSubMenu(action).then((present) => {
              if (present) {
                // sub menu is present traverse down menu tree
                assertMenuStructure(action, item.elements);
              } else {
                // explicit false assert
                expect(false).to.be.true;
              }
            });
          }
        });
      });
    }
  });

});
