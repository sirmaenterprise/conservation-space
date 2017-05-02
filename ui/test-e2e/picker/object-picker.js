'use strict';

var ExtensionsPanel = require('../components/extensions-panel/extensions-panel');
var Dialog = require('../components/dialog/dialog');
var SandboxPage = require('../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/picker';

const PICKER_DIALOG_BUTTON_SELECTOR = '#dialog-btn';
const SINGLE_EXTENSION_PICKER_DIALOG_BUTTON_SELECTOR = '#single-extension-dialog-btn';
const SELECTION_INPUT_SELECTOR = '#selected-items';

const SEARCH_EXTENSION = 'seip-object-picker-search';
const BASKET_EXTENSION = 'seip-object-picker-basket';
const RECENT_EXTENSION = 'seip-object-picker-recent';
const CREATE_EXTENSION = 'seip-object-picker-create';
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
 */
class ObjectPickerDialog extends Dialog {

  constructor() {
    super($(Dialog.COMPONENT_SELECTOR));
    this.waitUntilOpened();
  }

  /**
   * Retrieves the embedded object picker.
   * @returns {ObjectPicker}
   */
  getObjectPicker() {
    var objectPickerElement = this.element.$(ObjectPicker.COMPONENT_SELECTOR);
    return new ObjectPicker(objectPickerElement);
  }
}

/**
 * PO representing the object picker designed to hide the extension panel implementation.
 */
class ObjectPicker extends ExtensionsPanel {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  /**
   * Retrieves the count of the selected items in the picker.
   * @returns {Pomise} resolved with the count as text
   */
  getBasketCount() {
    var basketTab = this.getExtensionTab(BASKET_EXTENSION);
    var badge = basketTab.$('.badge');
    return badge.getText();
  }

}
ObjectPicker.COMPONENT_SELECTOR = '.picker';

module.exports = {
  ObjectPickerSandbox,
  ObjectPicker,
  ObjectPickerDialog,
  SEARCH_EXTENSION,
  BASKET_EXTENSION,
  RECENT_EXTENSION,
  CREATE_EXTENSION,
  TEST_EXTENSION
};