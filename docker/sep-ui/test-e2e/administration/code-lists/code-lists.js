'use strict';

let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;
let Dialog = require('../../components/dialog/dialog');

let fs = require('fs');
let path = require('path');
let hasClass = require('../../test-utils').hasClass;

let Pagination = require('../../search/components/common/pagination');
let AdvancedSearchCriteriaRow = require('../../search/components/advanced/advanced-search').AdvancedSearchCriteriaRow;
let AdvancedSearchStringCriteria = require('../../search/components/advanced/advanced-search').AdvancedSearchStringCriteria;
let CheckboxField = require('../../form-builder/form-control').CheckboxField;

const SANDBOX_URL = '/sandbox/administration/code-lists/';

function getValue(element) {
  return element.getAttribute('value');
}

function isReadonly(element) {
  return element.getAttribute('readonly').then(readonly => {
    return !!readonly;
  });
}

function setValue(element, value) {
  element.clear();
  element.sendKeys(value);
}

/**
 * PO for interacting with the code lists sandbox page.
 * @author Mihail Radkov
 */
class CodeListsSandbox extends SandboxPage {

  open(failing = false, userLanguage = 'en', systemLang = 'en') {
    let hash = `?fail=${failing}&userLang=${userLanguage}&systemLang=${systemLang}`;
    super.open(SANDBOX_URL, hash);
    return this;
  }

  getCodeListsUpload() {
    return new CodeListsUpload($(CodeListsUpload.COMPONENT_SELECTOR));
  }

  getCodeListsExport() {
    return new CodeListsExport($(CodeListsExport.COMPONENT_SELECTOR));
  }

  getManagement() {
    return new CodeListsManagement($(CodeListsManagement.COMPONENT_SELECTOR));
  }
}

/**
 * PO for interacting with the code lists export functionality.
 */
class CodeListsExport extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  exportCodeLists() {
    return this.codeListExportButton.click();
  }

  getExportedFileName() {
    return this.fileSaver.getText();
  }

  waitExportedCodeLists() {
    browser.wait(EC.visibilityOf(this.fileSaver), DEFAULT_TIMEOUT);
  }

  get codeListExportButton() {
    return this.element.$('.export-button')
  }

  get fileSaver() {
    return $('.file-saver');
  }
}

CodeListsExport.COMPONENT_SELECTOR = '.seip-code-lists-export';

/**
 * PO for interacting with the code lists functionality - selecting & uploading files with code lists.
 * @author Mihail Radkov
 */
class CodeListsUpload extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  selectFile(file) {
    this.selectFileInput.sendKeys(path.resolve(__dirname, file));
  }

  /**
   * Clicks to overwrite the code lists and waits until the confirmation dialog is shown.
   * @returns {Dialog}
   */
  overwriteCodeLists() {
    this.overwriteCodeListsButton.click();
    return this.getConfirmationDialog();
  }

  /**
   * Clicks to upload the code lists and waits until the confirmation dialog is shown.
   * @returns {Dialog}
   */
  updateCodeLists() {
    this.updateCodeListsButton.click();
    return this.getConfirmationDialog();
  }

  getConfirmationDialog() {
    let dialog = new Dialog($(Dialog.COMPONENT_SELECTOR));
    dialog.waitUntilOpened();
    return dialog;
  }

  isUploadSuccessful() {
    browser.wait(EC.visibilityOf(this.uploadMessageElement), DEFAULT_TIMEOUT);
    return hasClass(this.uploadMessageElement, 'alert-success');
  }

  get selectFileButton() {
    return this.element.$('.select-file-btn');
  }

  get selectFileInput() {
    return this.element.$('.file-upload-field');
  }

  get overwriteCodeListsButton() {
    return this.element.$('.code-lists-overwrite');
  }

  get updateCodeListsButton() {
    return this.element.$('.code-lists-update');
  }

  get uploadMessageElement() {
    return this.element.$('.upload-status');
  }
}

CodeListsUpload.COMPONENT_SELECTOR = '.seip-code-lists-upload';

/**
 * PO wrapping the management part of the controlled vocabularies.
 *
 * @author Mihail Radkov
 */
class CodeListsManagement extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  addCodeList() {
    this.element.$('.code-list-add-btn').click();
    return this.getCodeLists().then(lists => lists[0]);
  }

  getCodeLists() {
    return this.element.$$(CodeList.COMPONENT_SELECTOR + ':not(.ng-hide)').then((codeListElements) => {
      return Promise.all(codeListElements.map((codeListElement) => {
        return new CodeList(codeListElement);
      }));
    });
  }

  getSearch() {
    return new CodeListsSearch($(CodeListsSearch.COMPONENT_SELECTOR));
  }
}

CodeListsManagement.COMPONENT_SELECTOR = '.code-lists-management';

class CodeListsSearch extends AdvancedSearchCriteriaRow {

  constructor(element) {
    super(element);
  }

  getStringCriteria() {
    return new AdvancedSearchStringCriteria(this.valueColumn, true);
  }
}

CodeListsSearch.COMPONENT_SELECTOR = '.code-lists-search';

/**
 * PO for interacting with single controlled vocabulary.
 *
 * @author Mihail Radkov
 */
class CodeList extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilClosed() {
    browser.wait(EC.invisibilityOf(this.getDetailsElement()), DEFAULT_TIMEOUT);
  }

  getHeader() {
    return new CodeListHeader(this.getHeaderElement());
  }

  open() {
    this.getHeaderElement().click();
    browser.wait(EC.visibilityOf(this.getEditButtonElement()) || EC.visibilityOf(this.getSaveButtonElement()), DEFAULT_TIMEOUT);
    return this;
  }

  getDetails() {
    return new CodeDetails(this.getDetailsElement());
  }

  close() {
    this.getHeaderElement().click();
    this.waitUntilClosed();
  }

  addValue() {
    this.element.$('.code-value-add-btn').click();
    return this.getValues().then(values => values[0]);
  }

  getValues() {
    return this.element.$$(CodeValue.COMPONENT_SELECTOR).then((valueElements) => {
      return valueElements.map((valueElement) => new CodeValue(valueElement));
    });
  }

  getPagination() {
    return new Pagination(this.element.$(Pagination.COMPONENT_SELECTOR));
  }

  edit() {
    this.getEditButtonElement().click();
    return this;
  }

  save() {
    this.getSaveButtonElement().click();
    return this.getConfirmDialog();
  }

  canSave() {
    return this.getSaveButtonElement().isEnabled();
  }

  canEdit() {
    return this.getEditButtonElement().isEnabled();
  }

  cancel() {
    this.element.$('.code-list-cancel').click();
    return this;
  }

  cancelCreate() {
    this.element.$('.code-list-cancel-create').click();
    return this.getConfirmDialog();
  }

  getConfirmDialog() {
    let dialog = new Dialog($(Dialog.COMPONENT_SELECTOR));
    dialog.waitUntilOpened();
    return dialog;
  }

  getHeaderElement() {
    return this.element.$(CodeListHeader.COMPONENT_SELECTOR);
  }

  getDetailsElement() {
    return this.element.$('.code-list-details-section');
  }

  getEditButtonElement() {
    return this.element.$('.code-list-edit');
  }

  getSaveButtonElement() {
    return this.element.$('.code-list-save');
  }
}

CodeList.COMPONENT_SELECTOR = '.code-list';

/**
 * PO for the header of a controlled vocabulary.
 *
 * @author Mihail Radkov
 */
class CodeListHeader extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getId() {
    return this.element.$('.code-list-id').getText();
  }

  getName() {
    return this.element.$('.code-list-name').getText();
  }

}

CodeListHeader.COMPONENT_SELECTOR = '.code-list-header';

/**
 * Page object for interacting with the details for a code list or value.
 *
 * @author Mihail Radkov
 */
class CodeDetails extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getId() {
    return getValue(this.getIdElement());
  }

  setId(id) {
    setValue(this.getIdElement(), id);
  }

  isIdReadonly() {
    return isReadonly(this.getIdElement());
  }

  isIdValid() {
    return this.element.$$(this.getValidationSelector(CodeDetails.CODE_ID_SELECTOR)).count().then(count => count === 0);
  }

  getIdValidationMessages() {
    return new CodeValidationMessages(this.getValidationElement(CodeDetails.CODE_ID_SELECTOR));
  }

  getName() {
    return getValue(this.getNameElement());
  }

  setName(name) {
    setValue(this.getNameElement(), name);
  }

  isNameReadonly() {
    return isReadonly(this.getNameElement());
  }

  isNameValid() {
    return this.element.$$(this.getValidationSelector(CodeDetails.CODE_NAME_SELECTOR)).count().then(count => count === 0);
  }

  getNameValidationMessages() {
    return new CodeValidationMessages(this.getValidationElement(CodeDetails.CODE_NAME_SELECTOR));
  }

  getComment() {
    return getValue(this.getCommentElement());
  }

  setComment(comment) {
    setValue(this.getCommentElement(), comment);
  }

  isCommentReadonly() {
    return isReadonly(this.getCommentElement());
  }

  getExtra(extraId = '1') {
    return getValue(this.getExtraElement(extraId));
  }

  setExtra(extraId, extraValue) {
    setValue(this.getExtraElement(extraId), extraValue);
  }

  isExtraReadonly(extraId) {
    return isReadonly(this.getExtraElement(extraId));
  }

  openDescriptions() {
    this.element.$('.code-descriptions-btn').click();
    return new CodeDescriptionsDialog($(Dialog.COMPONENT_SELECTOR)).waitUntilOpened();
  }

  getIdElement() {
    return this.element.$(CodeDetails.CODE_ID_SELECTOR);
  }

  getNameElement() {
    return this.element.$(CodeDetails.CODE_NAME_SELECTOR);
  }

  getCommentElement() {
    return this.element.$('.code-comment');
  }

  getExtraElement(extraId) {
    return this.element.$(`.code-extra-${extraId}`);
  }

  getValidationElement(fieldSelector) {
    return this.element.$(this.getValidationSelector(fieldSelector));
  }

  getValidationSelector(fieldSelector) {
    return `${fieldSelector} + ${CodeValidationMessages.COMPONENT_SELECTOR}`;
  }
}

CodeDetails.CODE_ID_SELECTOR = '.code-id';
CodeDetails.CODE_NAME_SELECTOR = '.code-name';

/**
 * PO for the dialog listing descriptions in different languages for particular code list.
 *
 * @author Mihail Radkov
 */
class CodeDescriptionsDialog extends Dialog {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.getTable()), DEFAULT_TIMEOUT);
    return this;
  }

  getDescriptions() {
    return this.getTable().$$(CodeDescription.COMPONENT_SELECTOR).then(descriptionElements => {
      return Promise.all(descriptionElements.map((descriptionElement) => {
        return new CodeDescription(descriptionElement);
      }));
    });
  }

  getTable() {
    return this.element.$('.code-descriptions');
  }
}

/**
 * PO for rendering a code description in specific language.
 *
 * @author Mihail Radkov
 */
class CodeDescription extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLanguage() {
    return getValue(this.getLanguageElement());
  }

  isLanguageReadonly() {
    return isReadonly(this.getLanguageElement());
  }

  getName() {
    return getValue(this.getNameElement());
  }

  setName(name) {
    setValue(this.getNameElement(), name);
  }

  isNameReadonly() {
    return isReadonly(this.getNameElement());
  }

  getComment() {
    return getValue(this.getCommentElement());
  }

  setComment(comment) {
    setValue(this.getCommentElement(), comment);
  }

  isCommentReadonly() {
    return isReadonly(this.getCommentElement());
  }

  getLanguageElement() {
    return this.element.$('.code-language');
  }

  getNameElement() {
    return this.element.$('.code-name');
  }

  getCommentElement() {
    return this.element.$('.code-comment');
  }
}

CodeDescription.COMPONENT_SELECTOR = '.code-description';

/**
 * PO for interacting with the details for concrete code value.
 *
 * @author Mihail Radkov
 */
class CodeValue extends CodeDetails {

  isActive() {
    return new CheckboxField(this.element.$('.code-active-ctrl')).isChecked().then(isChecked => !!isChecked);
  }

  toggleActiveState() {
    return this.element.$('.code-active-ctrl').click();
  }

  remove() {
    this.element.$('.code-value-remove-btn').click();
  }

}

class CodeValidationMessages extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  isEmpty() {
    return this.element.$$('.field-empty').count().then(count => count > 0);
  }

  isNotUnique() {
    return this.element.$$('.field-not-unique').count().then(count => count > 0);
  }

  exceedsMaxSize() {
    return this.element.$$('.field-exceeds-size').count().then(count => count > 0);
  }

  invalidCharacters() {
    return this.element.$$('.field-invalid-characters').count().then(count => count > 0);
  }
}

CodeValidationMessages.COMPONENT_SELECTOR = '.code-validation-messages';

CodeValue.COMPONENT_SELECTOR = '.code-value';

module.exports = {
  CodeListsSandbox,
  CodeListsUpload,
  CodeListsSearch,
  CodeListsManagement,
  CodeList,
  CodeListHeader
};