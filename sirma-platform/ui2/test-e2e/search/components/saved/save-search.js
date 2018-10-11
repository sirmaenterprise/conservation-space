'use strict';

var PageObject = require('../../../page-object.js').PageObject;
var Dialog = require('../../../components/dialog/dialog');
var SingleSelectMenu = require('../../../form-builder/form-control.js').SingleSelectMenu;

/**
 * Page object for saving and updating saved searches.
 *
 * @author Mihail Radkov
 */
class SaveSearch extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.titleInput), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.saveButton), DEFAULT_TIMEOUT);
  }

  /**
   * Saves or updates the current search criteria with the provided title.
   *
   * The method will either save the search criteria as new instance or if there is any previously loaded search - it
   * will update it if specified.
   *
   * Note: If there is any previous title, it will be cleared.
   *
   * @param title - the search title.
   * @param update - if true it will update the search or create new one if false or undefined
   * @returns Promise resolved when the search is saved and the dialog closed.
   */
  save(title, update) {
    return this.titleInput.clear().then(() => {
      return this.titleInput.sendKeys(title).then(() => {
        return this.saveButton.click();
      }).then(() => {
        var confirmationDialog = new Dialog($('.modal-dialog'));
        confirmationDialog.waitUntilOpened();

        var promise;
        if (update) {
          promise = confirmationDialog.clickButton(SaveSearch.UPDATE_BUTTON_SELECTOR);
        } else {
          promise = confirmationDialog.clickButton(SaveSearch.CREATE_BUTTON_SELECTOR);
        }

        return promise.then(() => {
          confirmationDialog.waitUntilClosed();
        });
      });
    });
  }

  isDisabled() {
    return this.saveButton.getAttribute('disabled').then((disabled) => {
      return !!disabled;
    });
  }

  getTitleValue() {
    return this.titleInput.getAttribute('value');
  }

  get titleInput() {
    return this.element.$('input');
  }

  get saveButton() {
    return this.element.$('.btn');
  }
}
SaveSearch.COMPONENT_SELECTOR = '.save-search';
SaveSearch.CREATE_BUTTON_SELECTOR = '.seip-btn-create_new';
SaveSearch.UPDATE_BUTTON_SELECTOR = '.seip-btn-update';

/**
 * Page object for loading saved searches from the saved search select component.
 *
 * @author Mihail Radkov
 */
class SavedSearchSelect extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.openSelectMenuButton), DEFAULT_TIMEOUT);
  }

  toggleSelectMenu() {
    return this.openSelectMenuButton.click();
  }

  /**
   * Selects a saved search from the menu based on the provided key.
   * TODO: WHat is the key?
   * @param key
   * @returns {*}
   */
  selectSavedSearch(key) {
    return this.toggleSelectMenu().then(() => {
      // The reusable PO cannot be used with this select.
      var menu = new SingleSelectMenu(this.savedSearchSelectElement);
      menu.waitUntilMenuIsOpened();

      var options = $('.select2-results__options');
      return options.$('li > span[data-value="' + key + '"]').click();
    });
  }

  get openSelectMenuButton() {
    return this.element.$('.btn');
  }

  get savedSearchSelectElement() {
    return this.element.$('.select2');
  }

}
SavedSearchSelect.COMPONENT_SELECTOR = '.saved-search-select';

module.exports = {
  SaveSearch,
  SavedSearchSelect
};