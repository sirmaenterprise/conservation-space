"use strict";

var SavedSearchSelect = require('../saved/save-search.js').SavedSearchSelect;
var MultySelectMenu = require('../../../form-builder/form-control.js').MultySelectMenu;
var hasClass = require('../../../test-utils').hasClass;
var SandboxPage = require('../../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/search/components/common/basic-search-criteria';

const FREE_TEXT_SEARCH = '.criteria-field-fts';
const SEARCH_BUTTON_SELECTOR = '.btn.seip-search';
const CLEAR_BUTTON_SELECTOR = '.btn.clear-criteria';
const TYPES_SELECT_SELECTOR = '.criteria-field-types';
const RELATIONSHIPS_SELECT_SELECTOR = '.criteria-field-relationships';
const CONTEXT_SELECT_SELECTOR = '.criteria-field-contexts';
const DATE_FROM_PICKER_SELECTOR = '.criteria-field-created-from';
const DATE_TO_PICKER_SELECTOR = '.criteria-field-created-to';
const CREATED_BY_SELECT_SELECTOR = '.criteria-field-created-by';

/**
 * PO for working with the basic search criteria sandbox page and the different criteria forms and results.
 *
 * @author Mihail Radkov
 */
class BasicSearchCriteriaSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    this.getFirstCriteria().waitUntilVisible();
    this.getSecondCriteria().waitUntilVisible();
    this.getThirdCriteria().waitUntilVisible();
    browser.wait(EC.visibilityOf(this.getCriteriaTextArea()), DEFAULT_TIMEOUT);
  }

  getFirstCriteria() {
    return this.getCriteria('#first');
  }

  getSecondCriteria() {
    return this.getCriteria('#second');
  }

  getThirdCriteria() {
    return this.getCriteria('#third');
  }

  getCriteriaTextArea() {
    return $('#results');
  }

  getTypeCriteriaRule() {
    return this.getCriteriaTree().then((criteriaMap) => {
      return criteriaMap.rules[0].rules[0];
    });
  }

  getInnerCriteria() {
    return this.getCriteriaTree().then((criteriaMap) => {
      return criteriaMap.rules[0].rules[1];
    });
  }

  getInnerCriteriaRule(field) {
    return this.getInnerCriteria().then((innerCriteria) => {
      var founded;
      innerCriteria.rules.forEach((innerRule) => {
        if (innerRule.field === field) {
          founded = innerRule;
        }
      });
      return founded;
    });
  }

  getCriteriaTree() {
    return this.getCriteriaTextArea().getAttribute('value').then((criteria) => {
      return JSON.parse(criteria);
    });
  }

  getCriteria(number) {
    return new BasicSearchCriteria($(`${number} ${BasicSearchCriteria.COMPONENT_SELECTOR}`));
  }
}

/**
 * Page object for working with the basic search criteria component.
 *
 * @author Mihail Radkov
 */
class BasicSearchCriteria {

  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without wrapper element!');
    }
    this.element = element;
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.searchButton), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.clearButton), DEFAULT_TIMEOUT);
  }

  clearCriteria() {
    return this.clearButton.click();
  }

  search() {
    return this.searchButton.click();
  }

  getFreeTextValue() {
    return this.freeTextField.getAttribute("value");
  }

  modifyTypeSelectValue(value, remove) {
    return this.modifySelectValue(this.typesSelectElement, value, remove);
  }

  getSelectedValue(element) {
    var menu = new MultySelectMenu(element);
    return menu.getSelectedValue();
  }

  modifySelectValue(element, value, remove) {
    var menu = new MultySelectMenu(element);
    if (remove) {
      return menu.removeFromSelection(undefined, value, false);
    }
    return menu.selectFromMenuByValue(value);
  }

  getAvailableSelectChoices(element) {
    var menu = new MultySelectMenu(element);
    return menu.getAvailableSelectChoices();
  }

  waitForSelectedOption(value) {
    browser.wait(EC.presenceOf($(`option[value="${value}"]`)), DEFAULT_TIMEOUT);
  }

  getSavedSearchSelect() {
    return new SavedSearchSelect(this.element.$(SavedSearchSelect.COMPONENT_SELECTOR));
  }

  get searchButton() {
    return this.element.$(SEARCH_BUTTON_SELECTOR);
  }

  get clearButton() {
    return this.element.$(CLEAR_BUTTON_SELECTOR);
  }

  get freeTextField() {
    return this.element.$(FREE_TEXT_SEARCH);
  }

  get typesSelectElement() {
    var element = this.element.$(TYPES_SELECT_SELECTOR);
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
    return element;
  }

  get relationshipsSelectElement() {
    return this.element.$(RELATIONSHIPS_SELECT_SELECTOR);
  }

  get contextSelectElement() {
    return this.element.$(CONTEXT_SELECT_SELECTOR);
  }

  get dateFromPickerElement() {
    return this.element.$(DATE_FROM_PICKER_SELECTOR);
  }

  get dateToPickerElement() {
    return this.element.$(DATE_TO_PICKER_SELECTOR);
  }

  get createdBySelectElement() {
    return this.element.$(CREATED_BY_SELECT_SELECTOR);
  }
}
BasicSearchCriteria.COMPONENT_SELECTOR = '.seip-basic-search-criteria';

module.exports = {
  BasicSearchCriteriaSandbox,
  BasicSearchCriteria
};