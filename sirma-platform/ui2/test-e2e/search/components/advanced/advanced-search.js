'use strict';

/**
 * Page objects for working with the advanced search components.
 *
 * @author Mihail Radkov
 */

var SaveSearch = require('../saved/save-search').SaveSearch;
var SavedSearchSelect = require('../saved/save-search.js').SavedSearchSelect;
var SingleSelectMenu = require('../../../form-builder/form-control.js').SingleSelectMenu;
var MultySelectMenu = require('../../../form-builder/form-control.js').MultySelectMenu;
var TagSelectMenu = require('../../../form-builder/form-control.js').TagSelectMenu;
var DatetimeField = require('../../../form-builder/form-control.js').DatetimeField;
var InputField = require('../../../form-builder/form-control.js').InputField;
var DynamicDateRange = require('./dynamic-date-range/dynamic-date-range.js').DynamicDateRange;
var SandboxPage = require('../../../page-object').SandboxPage;

// TODO: Implement an abstract page object class that checks if element/selector is provided and waitUntilVisible is implemented ?!
// TODO: Too much waits ?!

const ADVANCED_SEARCH_SELECTOR = '.seip-advanced-search';
const ADVANCED_SEARCH_SECTION_SELECTOR = '.advanced-search-section';
const ADVANCED_SEARCH_CRITERIA_SELECTOR = '.advanced-search-criteria';
const ADVANCED_SEARCH_GROUP_SELECTOR = '.criteria-group';
const ADVANCED_SEARCH_CRITERIA_CONTROL_SELECTOR = '.criteria-controls';
const ADVANCED_SEARCH_CRITERIA_ROW_SELECTOR = '.criteria-row';

/**
 * PO for opening the sandbox page for the advanced search and fetching it.
 */
class AdvancedSearchSandboxPage extends SandboxPage {

  open() {
    super.open('sandbox/search/components/advanced/');
    browser.wait(EC.visibilityOf($('.advanced-search-stub')), DEFAULT_TIMEOUT);
  }

  getAdvancedSearch(selector) {
    var css = selector || ADVANCED_SEARCH_SELECTOR;
    return new AdvancedSearch($(css));
  }

  changeCriteriaSet(criteriaSet) {
    if (criteriaSet === 'empty') {
      $('#empty_criteria').click();
    } else if (criteriaSet === 'predefined') {
      $('#predefined_criteria').click()
    } else if (criteriaSet === 'predefined2') {
      $('#predefined_criteria2').click()
    }
  }

  // TODO: If model is changed then all tests should be updated..
  getTreeModel() {
    var textArea = $('#tree-model');
    return textArea.getAttribute("value").then((value) => {
      return JSON.parse(value);
    });
  }

  toggleEnabledState() {
    return $('#toggle_disabled').click();
  }

  toggleLockedState() {
    return $('#toggle_lock').click();
  }
}

/**
 * Page object for working with the advanced search component - searching, resetting criteria and getting
 * specific sections of it.
 *
 * Cannot be instantiated without a provided wrapper element!
 */
class AdvancedSearch {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.searchButton), DEFAULT_TIMEOUT);
  }

  search() {
    return this.searchButton.click();
  }

  clear() {
    return this.clearButton.click();
  }

  getSection(index) {
    var section = this.element.$(`${ADVANCED_SEARCH_SECTION_SELECTOR}:nth-child(${index + 1})`);
    return new AdvancedSearchSection(section);
  }

  getSavedSearchSelect() {
    return new SavedSearchSelect(this.element.$(SavedSearchSelect.COMPONENT_SELECTOR));
  }

  getSaveSearchGroup() {
    var element = this.element.$(SaveSearch.COMPONENT_SELECTOR);
    return new SaveSearch(element);
  }

  get searchButton() {
    return this.element.$('.seip-search');
  }

  get clearButton() {
    return this.element.$('.btn.clear-criteria');
  }
}

AdvancedSearch.COMPONENT_SELECTOR = '.seip-advanced-search';

/**
 * Page object for working with an advanced search section - changing the object type and getting the section's
 * advanced search criteria.
 *
 * Cannot be instantiated without a provided wrapper element!
 */
class AdvancedSearchSection {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.objectTypeSelect), DEFAULT_TIMEOUT);
  }

  /**
   * Retrieves the criteria controls for the given criteria level and group index.
   * @param level - the criteria level. Starts from 1
   * @param groupIndex - the group index within the criteria level. Starts from 0
   * @returns the specific {@link AdvancedSearchCriteriaControls}
   */
  getCriteriaControlsForGroup(level, groupIndex) {
    // Default values if not provided
    level = level || 1;
    groupIndex = groupIndex || 0;
    return this.getCriteriaGroup(level, groupIndex).then((group) => {
      if (group) {
        return group.all(by.css(ADVANCED_SEARCH_CRITERIA_CONTROL_SELECTOR)).then((controls) => {
          return new AdvancedSearchCriteriaControls(controls[0]);
        });
      }
    });
  }

  /**
   * Retrieves the criteria row for the given criteria level and group index.
   * @param level - the criteria level. Starts from 1
   * @param groupIndex - the index of the group within the criteria level. Starts from 0
   * @param rowIndex - the index of the row within the criteria group. Starts from 0
   * @returns the specific {@link AdvancedSearchCriteriaRow}
   */
  getCriteriaRowForGroup(level, groupIndex, rowIndex) {
    // Default values if not provided
    level = level || 1;
    groupIndex = groupIndex || 0;
    rowIndex = rowIndex || 0;
    return this.getCriteriaRowsForGroup(level, groupIndex).then((rows) => {
      return rows[rowIndex];
    });
  }

  /**
   * Retrieves all criteria rows withing the given criteria level and group index.
   * @param level - the criteria level. Starts from 1
   * @param groupIndex - the group index within the criteria level. Starts from 0
   * @returns an {@link Array} of {@link AdvancedSearchCriteriaRow}
   */
  getCriteriaRowsForGroup(level, groupIndex) {
    var rows = [];
    return this.getCriteriaGroup(level, groupIndex).then((group) => {
      return group.all(by.css(`.criteria-level-${level} > ${ADVANCED_SEARCH_CRITERIA_ROW_SELECTOR}`)).then((rows) => {
        return rows.map((row) => {
          return new AdvancedSearchCriteriaRow(row);
        });
      });
    });
  }

  /**
   * Retrieves specific criteria group based on the provided criteria level and group index.
   * @param level - the criteria level. Starts from 1
   * @param groupIndex - the group index within the criteria level. Starts from 0
   * @returns a {@link WebElement}
   */
  getCriteriaGroup(level, groupIndex) {
    return this.element.all(by.css(`${ADVANCED_SEARCH_GROUP_SELECTOR}.criteria-level-${level}`)).then((groups) => {
      return groups[groupIndex];
    });
  }

  /**
   * Based on the provided levels and indexes it finds the corresponding criteria row and selects the specific
   * property and operator.
   * @param property - the property to be selected
   * @param operator - the property's operator to be selected
   * @param level - the criteria level. Starts from 1. Default is 1 if not provided.
   * @param groupIndex - the index of the group within the criteria level. Starts from 0. Default is 0 if not provided.
   * @param rowIndex - the index of the row within the criteria group. Starts from 0. Default is 0 if not provided.
   * @returns the specific {@link AdvancedSearchCriteriaRow} in which the selection was performed
   */
  selectPropertyAndOperator(property, operator, level, groupIndex, rowIndex) {
    return this.selectProperty(property, level, groupIndex, rowIndex).then((row) => {
      return row.changeOperator(operator).then(() => {
        return row;
      });
    });
  }

  /**
   * Based on the provided levels and indexes it finds the corresponding criteria row and selects the specific property.
   * @param property - the property to be selected
   * @param level - the criteria level. Starts from 1. Default is 1 if not provided.
   * @param groupIndex - the index of the group within the criteria level. Starts from 0. Default is 0 if not provided.
   * @param rowIndex - the index of the row within the criteria group. Starts from 0. Default is 0 if not provided.
   * @returns the specific {@link AdvancedSearchCriteriaRow} in which the selection was performed
   */
  selectProperty(property, level, groupIndex, rowIndex) {
    // Default values if not provided
    level = level ? level : 1;
    groupIndex = groupIndex ? groupIndex : 0;
    rowIndex = rowIndex ? rowIndex : 0;
    return this.getCriteriaRowForGroup(level, groupIndex, rowIndex).then((row) => {
      return row.changeProperty(property).then(() => {
        row.waitForOperatorSelectToRender();
        return row;
      });
    });
  }

  /**
   * Inserts a criteria row in the specified criteria level and group index.
   * @param level - the criteria level. Starts from 1
   * @param groupIndex - the index of the group within the criteria level. Starts from 0
   * @returns {Promise} resolved when the row is inserted
   */
  insertRow(level, groupIndex) {
    // Default values if not provided
    level = level ? level : 1;
    groupIndex = groupIndex ? groupIndex : 0;
    return this.getCriteriaControlsForGroup(level, groupIndex).then((controls) => {
      return controls.addRule();
    });
  }

  /**
   * Adds the object type to the provided value.
   *
   * @param type - the provided value. It should be the type's ID
   */
  addObjectType(type) {
    var menu = new MultySelectMenu(this.objectTypeSelect);
    return menu.selectFromMenu(undefined, type, false);
  }

  /**
   * Remove the object type from the object type menu by it's identifier.
   *
   * @param type the object type's identifier
   * @returns {*}
   */
  removeObjectType(type) {
    var menu = new MultySelectMenu(this.objectTypeSelect);
    return menu.removeFromSelection(undefined, type, false);
  }

  getObjectTypeSelectValue() {
    var menu = new MultySelectMenu(this.objectTypeSelect);
    return menu.getSelectedValue();
  }

  getObjectTypeMenu() {
    return new MultySelectMenu(this.objectTypeSelect);
  }

  get objectTypeSelect() {
    return this.element.$('.object-type-select');
  }
}

/**
 * Page object for working with the criteria controls - and, or, add  & remove.
 *
 * Cannot be instantiated without a provided wrapper element!
 */
class AdvancedSearchCriteriaControls {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.removeGroupButton), DEFAULT_TIMEOUT);
  }

  and() {
    this.andButton.click();
  }

  or() {
    this.orButton.click();
  }

  addRule() {
    this.addRuleButton.click();
  }

  addGroup() {
    this.addGroupButton.click();
  }

  removeGroup() {
    this.removeGroupButton.click();
  }

  get andButton() {
    return this.element.$('.criteria-and');
  }

  get orButton() {
    return this.element.$('.criteria-or');
  }

  get addRuleButton() {
    return this.element.$('.add-rule');
  }

  get addGroupButton() {
    return this.element.$('.add-rules');
  }

  get removeGroupButton() {
    return this.element.$('.remove-rules');
  }
}

/**
 * Page object for working with a criteria row - changing its field, operator etc.
 *
 * Cannot be instantiated without a provided wrapper element!
 */
class AdvancedSearchCriteriaRow {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    this.waitForPropertySelectToRender();
    this.waitForOperatorSelectToRender();
  }

  waitForPropertySelectToRender() {
    SingleSelectMenu.waitForSelectionToRender(this.propertySelect);
  }

  waitForOperatorSelectToRender() {
    SingleSelectMenu.waitForSelectionToRender(this.operatorSelect);
  }

  changeProperty(property) {
    return this.getPropertySelectMenu().selectFromMenu('', property, false);
  }

  changeOperator(operator) {
    return this.getOperatorSelectMenu().selectFromMenu('', operator, false);
  }

  getSelectedPropertyValue() {
    return this.getPropertySelectMenu().getSelectedValue();
  }

  getSelectedOperatorValue() {
    return this.getOperatorSelectMenu().getSelectedValue();
  }

  getPropertySelectValues() {
    return this.getPropertySelectMenu().getMenuValues();
  }

  getOperatorSelectValues() {
    return this.getOperatorSelectMenu().getMenuValues();
  }

  getPropertySelectMenu() {
    return new SingleSelectMenu(this.propertySelect);
  }

  getOperatorSelectMenu() {
    return new SingleSelectMenu(this.operatorSelect);
  }

  // TODO: Don't use this. Use directly the class. Remove the usage of this method.
  getValueExtension(type) {
    if (type === 'string') {
      return new AdvancedSearchStringCriteria(this.valueColumn);
    } else if (type === 'test') {
      return new AdvancedSearchTestCriteria(this.valueColumn);
    }
    throw new Error(`There is no PO for ${type} extension!`);
  }

  get propertySelect() {
    return this.element.$('.criteria-property > div');
  }

  get operatorSelect() {
    return this.element.$('.criteria-operator > div');
  }

  get valueColumn() {
    return this.element.$('.criteria-value');
  }

  get removeButton() {
    return this.element.$('.remove-rule');
  }
}

/**
 * Page object for working with strings in an advanced search criteria. Supports multiple and single criteria
 * selection. The multiple is achieved with a drop down menu while the single with an input field.
 *
 * Cannot be instantiated without a provided wrapper element!
 *
 * @author Mihail Radkov
 */
class AdvancedSearchStringCriteria {
  constructor(element, singleValued) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.singleValued = singleValued;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    if (this.singleValued) {
      browser.wait(EC.visibilityOf(this.inputFieldWrapper), DEFAULT_TIMEOUT);
    } else {
      browser.wait(EC.visibilityOf(this.select), DEFAULT_TIMEOUT);
    }
  }

  enterValue(value) {
    if (this.singleValued) {
      return this.getInput().setValue(undefined, value);
    }
    return this.getMenu().enterValue(value);
  }

  getValue() {
    if (this.singleValued) {
      return this.getInput().getValue();
    }
    return this.getMenu().getSelectedValue();
  }

  isDisabled() {
    if (this.singleValued) {
      return this.getInput().isDisabled();
    }
    return this.getMenu().isDisabled();
  }

  getMenu() {
    return new TagSelectMenu(this.select);
  }

  getInput() {
    // InputField requires a wrapper because internally queries the input element
    return new InputField(this.inputFieldWrapper);
  }

  get select() {
    return this.element.$('.string-criteria-select');
  }

  get inputFieldWrapper() {
    return this.element.$('.single-string-criteria');
  }

}

class AdvancedSearchRelationCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
  }

  waitForRelationValueToBeVisible() {
    browser.wait(EC.visibilityOf(this.relationValue), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.pickerButton), DEFAULT_TIMEOUT);
  }

  waitForManualSelectionToBeVisible() {
    this.waitForRelationValueToBeVisible();
    browser.wait(EC.visibilityOf(this.select), DEFAULT_TIMEOUT);
  }

  waitForAutomaticSelectionToBeVisible() {
    this.waitForRelationValueToBeVisible();
    browser.wait(EC.visibilityOf(this.queryPlaceholder), DEFAULT_TIMEOUT);
  }

  selectValue(value) {
    return this.getValueMenu().selectFromMenuByValue(value);
  }

  getValueMenu() {
    return new MultySelectMenu(this.select);
  }

  openPicker() {
    browser.wait(EC.visibilityOf(this.pickerButton), DEFAULT_TIMEOUT);
    return this.pickerButton.click();
  }

  get relationValue() {
    return this.element.$('.relation-value');
  }

  get select() {
    return this.relationValue.$('.relation-criteria-select');
  }

  get queryPlaceholder() {
    return this.relationValue.$('.user-query-placeholder');
  }

  get pickerButton() {
    return this.relationValue.$('.btn');
  }
}

class AdvancedSearchTestCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
  }

  get testElement() {
    return this.element.$('.seip-advanced-search-test-criteria');
  }
}

class AdvancedSearchDateCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
  }

  /**
   * Checks if all rendered pickers are disabled and if just one if them is not then the criteria is considered
   * to be enabled.
   */
  isDisabled() {
    return this.element.all(by.css('.datetime')).then((pickerElements) => {
      return Promise.all(pickerElements.map((pickerElement) => {
        return new DatetimeField(pickerElement).isDisabled();
      })).then((states) => {
        states.forEach((state) => {
          if (!state) {
            return false;
          }
        });
        return true;
      });
    });
  }

  get datePicker() {
    return this.element.$('.datetime');
  }

  get datePickerFrom() {
    return this.element.$('.date-from .datetime');
  }

  get datePickerTo() {
    return this.element.$('.date-to .datetime');
  }

  get dynamicDateRangeElement() {
    return this.element.$(DynamicDateRange.COMPONENT_SELECTOR);
  }
}

class AdvancedSearchCodelistCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.codeValueSelect), DEFAULT_TIMEOUT);
  }

  isDisplayed() {
    return this.codeValueSelect.isDisplayed();
  }

  isDisabled() {
    return new MultySelectMenu(this.codeValueSelect).isDisabled();
  }

  selectCodeValue(value) {
    return new MultySelectMenu(this.codeValueSelect).selectFromMenuByValue(value);
  }

  getSelectedValues() {
    return new MultySelectMenu(this.codeValueSelect).getSelectedValue();
  }

  get codeValueSelect() {
    return this.element.$('.codelist-criteria-select');
  }
}

class AdvancedSearchBooleanCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.booleanValueSelect), DEFAULT_TIMEOUT);
  }

  isDisplayed() {
    return this.booleanValueSelect.isDisplayed();
  }

  isDisabled() {
    return new MultySelectMenu(this.booleanValueSelect).isDisabled();
  }

  selectBooleanValue(value) {
    return new MultySelectMenu(this.booleanValueSelect).selectFromMenuByValue(value);
  }

  getSelectedValue() {
    return new MultySelectMenu(this.booleanValueSelect).getSelectedValue();
  }

  get booleanValueSelect() {
    return this.element.$('.boolean-criteria-select');
  }
}

class AdvancedSearchNumericCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getAttrib(element, attrib) {
    return element.getAttribute(attrib);
  }

  typeInElement(element, value) {
    element.clear();
    return element.sendKeys(value);
  }

  setSingleInputValue(value) {
    return this.typeInElement(this.singleInputSelect, value);
  }

  getSingleInputValue() {
    return this.getAttrib(this.singleInputSelect, 'value');
  }

  setBetweenLeftInputValue(value) {
    return this.typeInElement(this.betweenLeftInputSelect, value);
  }

  setBetweenRightInputValue(value) {
    return this.typeInElement(this.betweenRightInputSelect, value);
  }

  getBetweenLeftInputValue() {
    return this.getAttrib(this.betweenLeftInputSelect, 'value');
  }

  getBetweenRightInputValue() {
    return this.getAttrib(this.betweenRightInputSelect, 'value');
  }

  isSingleInputDisabled() {
    return this.getAttrib(this.singleInputSelect, 'disabled');
  }

  isBetweenLeftInputDisabled() {
    return this.getAttrib(this.betweenLeftInputSelect, 'disabled');
  }

  isBetweenRightInputDisabled() {
    return this.getAttrib(this.betweenRightInputSelect, 'disabled');
  }

  get singleInputSelect() {
    return this.element.$('.numeric-single-input input:first-child');
  }

  get betweenLeftInputSelect() {
    return this.element.$('.numeric-double-input input:nth-child(1)');
  }

  get betweenRightInputSelect() {
    return this.element.$('.numeric-double-input input:nth-child(2)');
  }
}

class AdvancedSearchObjectTypeCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.codeValueSelect), DEFAULT_TIMEOUT);
  }

  isDisplayed() {
    return this.codeValueSelect.isDisplayed();
  }

  isDisabled() {
    return new SingleSelectMenu(this.codeValueSelect).isDisabled();
  }

  selectOption(value) {
    return new SingleSelectMenu(this.codeValueSelect).selectOption(value);
  }

  getSelectedValue() {
    return new SingleSelectMenu(this.codeValueSelect).getSelectedValue();
  }

  get codeValueSelect() {
    return this.element.$('.object-type-criteria');
  }
}

class AdvancedSearchKeywordCriteria {
  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.inputFieldWrapper), DEFAULT_TIMEOUT);
  }

  getValue() {
    return this.getInput().getValue();
  }

  getInput() {
    // InputField requires a wrapper because internally queries the input element
    return new InputField(this.inputFieldWrapper);
  }

  get inputFieldWrapper() {
    return this.element.$('.advanced-search-keyword-criteria');
  }

}

module.exports = {
  AdvancedSearchSandboxPage,
  AdvancedSearch,
  AdvancedSearchSection,
  AdvancedSearchCriteriaControls,
  AdvancedSearchCriteriaRow,
  AdvancedSearchStringCriteria,
  AdvancedSearchRelationCriteria,
  AdvancedSearchTestCriteria,
  AdvancedSearchDateCriteria,
  AdvancedSearchCodelistCriteria,
  AdvancedSearchBooleanCriteria,
  AdvancedSearchNumericCriteria,
  AdvancedSearchObjectTypeCriteria,
  AdvancedSearchKeywordCriteria,
  ADVANCED_SEARCH_SECTION_SELECTOR
};