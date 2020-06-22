'use strict';

var ExtensionsPanel = require('../components/extensions-panel/extensions-panel');
var Dialog = require('../components/dialog/dialog');
var SandboxPage = require('../page-object').SandboxPage;
var Search = require('../search/components/search').Search;

const SANDBOX_URL = '/sandbox/picker';

const PICKER_DIALOG_BUTTON_SELECTOR = '#dialog-btn';
const SINGLE_EXTENSION_PICKER_DIALOG_BUTTON_SELECTOR = '#single-extension-dialog-btn';
const SELECTION_INPUT_SELECTOR = '#selected-items';

const SEARCH_EXTENSION = 'seip-object-picker-search';
const BASKET_EXTENSION = 'seip-object-picker-basket';
const RECENT_EXTENSION = 'seip-object-picker-recent';
const CREATE_EXTENSION = 'seip-object-picker-create';
const UPLOAD_EXTENSION = 'seip-object-picker-upload';
const TEST_EXTENSION = 'seip-object-picker-test-extension';

/**
 * PO for the object picker sandbox providing means for interacting with it.
 *
 * @author Mihail Radkov
 */
class ObjectPickerSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    browser.wait(EC.visibilityOf($(PICKER_DIALOG_BUTTON_SELECTOR)), DEFAULT_TIMEOUT);
  }

  openSingleExtensionPickerDialog() {
    $(SINGLE_EXTENSION_PICKER_DIALOG_BUTTON_SELECTOR).click();
    return new ObjectPickerDialog();
  }

  openPickerDialog() {
    $(PICKER_DIALOG_BUTTON_SELECTOR).click();
    return new ObjectPickerDialog();
  }

  getEmbeddedPicker() {
    return new ObjectPicker($(`#object-picker-embedded ${ObjectPicker.COMPONENT_SELECTOR}`));
  }

  getSelectionInputValue() {
    return $(SELECTION_INPUT_SELECTOR).getAttribute('value');
  }
}

/**
 * PO for object picker embedded in a dialog.
 *
 * If no wrapping element is provided, the PO will be initialized with a default one. Useful for cases where the
 * picker is the only one in the page.
 *
 * Important: In case of multiple pickers per page, its necessary to provide a wrapping element to the specific one
 * to avoid assigning the default one referencing to wrong dialog.
 */
class ObjectPickerDialog extends Dialog {

  constructor(element) {
    if (!element) {
      element = $(ObjectPickerDialog.COMPONENT_SELECTOR);
    }
    super(element);
    this.waitUntilOpened();
  }

  /**
   * Retrieves the embedded object picker.
   * @returns {ObjectPicker}
   */
  getObjectPicker() {
    return new ObjectPicker(this.element.$(ObjectPicker.COMPONENT_SELECTOR));
  }

  /**
   * Gets concrete object picker dialog based on the level of nesting.
   *
   * @param level - the level, default is 1 (first level)
   * @returns {ObjectPickerDialog}
   */
  static getPickerDialog(level = 1) {
    let pickerDialogElement = $$(ObjectPickerDialog.COMPONENT_SELECTOR).get(level - 1);
    return new ObjectPickerDialog(pickerDialogElement);
  }
}

ObjectPickerDialog.COMPONENT_SELECTOR = '.picker-modal';

/**
 * PO representing the object picker designed to hide the extension panel implementation.
 */
class ObjectPicker extends ExtensionsPanel {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  getBasketTab() {
    return this.getExtensionTab(BASKET_EXTENSION);
  }

  getBasketBadge() {
    return this.getBasketTab().$('.postfix');
  }

  getUploadTab() {
    return this.getExtensionTab(UPLOAD_EXTENSION);
  }

  clickUploadAllButton() {
    $('.uploadall-btn').click();
  }

  clickSelectButton() {
    $('.seip-btn-ok').click();
  }

  /**
   * Waits for the basket badge count to become a given value
   * @param count the expected count
   */
  waitForBasketCount(count) {
    browser.wait(EC.textToBePresentInElement(this.getBasketBadge(), count), DEFAULT_TIMEOUT);
  }

  getSearch() {
    let searchElement = this.getExtension(SEARCH_EXTENSION);
    return new Search(searchElement);
  }

}

ObjectPicker.COMPONENT_SELECTOR = '.picker';

module.exports = {
  ObjectPickerSandbox,
  ObjectPickerDialog,
  ObjectPicker,
  SEARCH_EXTENSION,
  BASKET_EXTENSION,
  RECENT_EXTENSION,
  CREATE_EXTENSION,
  UPLOAD_EXTENSION,
  TEST_EXTENSION
};