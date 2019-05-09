'use strict';

let elementToStopMoving = require('../utils/conditions').elementToStopMoving;

class FormControl {

  constructor(control) {
    if (control) {
      this.control = control;
    }
  }

  getElement() {
    return this.control;
  }

  /**
   * Selects an element based on the provided selector or returns the current element if no selector is provided.
   * @param selector
   * @returns {*}
   */
  getControl(selector) {
    if (selector) {
      return $(selector);
    }
    return this.control;
  }

  /**
   * Get an element text.
   * NOTE: The element must be visible.
   *
   * @param selector
   * @returns {!webdriver.promise.Promise.<string>}
   */
  getText(selector) {
    return this.getControl(selector).getText();
  }

  /**
   * Get the text value from an element marked as <b>preview-field</b>.
   * NOTE: The field must be visible!
   *
   * @returns {*}
   */
  getPreviewValue() {
    return this.control.$('.preview-field').getText();
  }

  getPrintValue() {
    return this.control.$('.print-field').getText();
  }

  getHiddenPreviewValue() {
    return browser.executeScript('return arguments[0].innerHTML;', this.control.$('.preview-field'));
  }

  /**
   * Get the element's innerHTML.
   * @param selector
   * @returns {!webdriver.promise.Promise.<string>}
   */
  getHtml(selector) {
    return browser.executeScript('return arguments[0].innerHTML;', this.getControl(selector));
  }

  isVisible(selector) {
    return this.getControl(selector).isDisplayed();
  }

  isPresent(selector) {
    return this.getControl(selector).isPresent();
  }

  /**
   * Returns the mandatory mark text. Not very helpful but can be asserted against.
   * @param selector
   * @returns {*}
   */
  isMandatory(selector) {
    if (selector) {
      return element(by.css(selector)).element(by.css('sup')).getText() === '*';
    } else {
      return this.control.element(by.css('sup')).getText().then((text) => {
        return text === '*';
      });
    }
  }

  hasError() {
    return this.hasCssClass(this.control, 'has-error');
  }

  isPreview() {
    return this.control.$('.preview-field:not(.hidden)').isPresent();
  }

  isPrint(selector) {
    return $(`${selector}.print-field`).isPresent();
  }

  isPrintField() {
    return this.control.$('.print-field').isPresent();
  }

  isEditable() {
    return this.control.$('.form-control:not(.hidden)').isPresent();
  }

  /**
   * Returns messages elements for given field.
   * @param selector
   * @returns {*}
   */
  getMessages(selector) {
    return this.getControl(selector).$$('.messages .message');
  }

  /**
   * Returns the disabled attribute of an input field if any.
   * @param selector
   * @returns {!webdriver.promise.Promise.<?string>}
   */
  isDisabled(selector) {
    return this.getAttributeValue(selector, 'disabled');
  }

  /**
   * Returns the readonly attribute of an input field if any.
   * @param selector
   * @returns {!webdriver.promise.Promise.<?string>}
   */
  isReadOnly(selector) {
    return this.getAttributeValue(selector, 'readonly');
  }

  getAttributeValue(selector, attributeName) {
    return this.getControl(selector).getAttribute(attributeName);
  }

  getElementAttributeValue(element, attributeName) {
    return element.getAttribute(attributeName);
  }

  hasElementAttribute(element, attributeName) {
    return this.getElementAttributeValue(element, attributeName).then((attribute) => {
      return !!attribute;
    });
  }

  waitToShow() {
    browser.wait(EC.visibilityOf(this.control), DEFAULT_TIMEOUT);
  }

  hasCssClass(element, className) {
    return element.getAttribute('class').then((attribute) => {
      return attribute.indexOf(className) !== -1;
    });
  }

  getTooltipIcon() {
    return this.control.$('.fa-info');
  }

  isTooltipIconVisible() {
    browser.wait(EC.visibilityOf(this.getTooltipIcon()), DEFAULT_TIMEOUT, 'Tooltip icon should be visible!');
  }

  isTooltipIconHidden() {
    browser.wait(EC.invisibilityOf(this.getTooltipIcon()), DEFAULT_TIMEOUT, 'Tooltip icon should be hidden!');
  }
}

class InputField extends FormControl {

  constructor(control) {
    super(control);
  }

  getInputElement() {
    let inputElement = this.control.$('.form-control');
    browser.wait(EC.visibilityOf(inputElement), DEFAULT_TIMEOUT, 'Input field should be visible!');
    return inputElement;
  }

  getPreviewElement() {
    let previewElement = this.control.$('.preview-field');
    browser.wait(EC.visibilityOf(previewElement), DEFAULT_TIMEOUT);
    return previewElement;
  }

  /**
   * Get input field value.
   * @param selector
   * @returns {!webdriver.promise.Promise.<?string>}
   */
  getValue(selector) {
    if (selector) {
      return this.getAttributeValue(selector, 'value');
    } else {
      return this.getInputElement().getAttribute('value');
    }
  }

  /**
   * Set an input field value.
   * @param selector
   * @param text
   */
  setValue(selector, text) {
    if (selector) {
      let field = $(selector);
      browser.wait(EC.visibilityOf(field), DEFAULT_TIMEOUT);
      field.sendKeys(text);
      browser.wait(EC.textToBePresentInElementValue(field, text), DEFAULT_TIMEOUT);
    } else {
      let inputElement = this.getInputElement();
      let script = `var field = $(arguments[0]); field.val("${text}"); field.trigger("change")`;
      browser.executeScript(script, inputElement.getWebElement());
      inputElement.click();
    }
  }

  blurField(selector) {
    let element;
    if (selector) {
      element = $(selector);
    } else {
      element = this.getInputElement();
    }
    let script = 'var field = $(arguments[0]); field.trigger("blur");';
    browser.executeScript(script, element.getWebElement());
  }

  /**
   * Clear input field value.
   * @param selector
   * @returns {*|Promise.<!webdriver.promise.Promise.<void>>}
   */
  clearValue(selector) {
    if (selector) {
      return $(selector).clear();
    }
    return this.getInputElement().clear();
  }

  isDisabled(selector) {
    if (selector) {
      return super.isDisabled(selector);
    }
    return this.hasElementAttribute(this.getInputElement(), 'disabled');
  }

  isReadOnly(selector) {
    if (selector) {
      return super.isReadOnly(selector);
    }
    return this.hasElementAttribute(this.getInputElement(), 'readonly');
  }

}

class InputPassword extends InputField {

  constructor(field) {
    super(field);
  }

  isEditable() {
    return this.getInputElement().getAttribute('readonly');
  }

  click() {
    this.getInputElement().click();
  }

}

// Not testable timepicker component because the lack of proper css classes or html attributes to be used as selectors
// If date and datetime should be set, use the #setDate and #setDatetime method which set the value directly to the field.
class DatetimeField extends InputField {

  constructor(field) {
    super(field);
  }

  /**
   * Get the date field value from the underlying input field.
   * @returns a promise that would resolve with the date field value.
   */
  getDate() {
    this.waitForDatePickerToBeInitialized();
    return this.getDateField().getAttribute('value');
  }

  /**
   * Private utility method for getting formatted date or datetime string.
   *
   * @param withTime When time should be added to the date
   * @param separator the date separator
   * @returns {string} For example "07/10/2017" or "07/10/2017 09:26"
   */
  getToday(withTime, separator) {
    var _separator = separator || '/';
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var monthName = date.toLocaleString('en-us', {month: "long"});
    var day = date.getDate();
    var hour = date.getHours();
    var minutes = date.getMinutes();

    var result = monthName + _separator + day + _separator + year;
    if (withTime) {
      result += ' ' + hour + ':' + minutes;
    }
    return result;
  }

  /**
   * Sets the today date or datetime as formatted string in the field directly without using the datepicker component.
   * @param selector
   * @param withTime
   * @param separator the date separator
   */
  setToday(selector, withTime, separator) {
    this.setDate(selector, this.getToday(withTime, separator));
  }

  /**
   * Populates the datetime field with datetime given as plain string containing properly formatted date and time parts.
   * If configured datetime format is not satisfied by provided value, the picker component will reject it.
   *
   * @param selector
   * @param datetime
   */
  setDatetime(selector, datetime) {
    this.setValue(selector, datetime);
  }

  /**
   * Sets given date formatted as string directly in the field without using the datepicker component.
   * @param selector
   * @param datetime
   */
  setDate(selector, datetime) {
    this.setValue(selector, datetime);
  }

  /**
   * Removes the value from the underlying input field.
   * @returns {Promise|Promise.<*>}
   */
  clearDateField() {
    this.waitForDatePickerToBeInitialized();
    return Promise.resolve(this.getDateField().clear());
  }

  /**
   * Returns the underlying input field.
   * @returns {*}
   */
  getDateField() {
    return this.control.$('input');
  }

  getTriggerButton(selector) {
    return $(selector).all(by.css('span')).first();
  }

  waitForDatePickerToBeInitialized() {
    browser.wait(EC.visibilityOf(this.control), DEFAULT_TIMEOUT);
  }

  waitForCalendarToBeVisible() {
    browser.wait(EC.presenceOf($('.bootstrap-datetimepicker-widget')), DEFAULT_TIMEOUT);
  }

  isDisabled() {
    return this.hasElementAttribute(this.control.$('input'), 'disabled');
  }

}

/**
 * Checkbox and radio buttons should be in following format:
 * <label><input type="checkbox" /><i></i></label>
 *
 * The actual input is hidden and shifted away and is represented by an image instead. So the test script should
 * find and click on the wrapping label in order to select the checkbox/radio button.
 */
class CheckboxField extends InputField {

  /**
   * Pass the checkbox wrapper element.
   * @param field
   */
  constructor(field) {
    super(field);
  }

  toggleCheckbox() {
    return this.control.$$('label.checkbox input + i').first().click();
  }

  isChecked() {
    return this.control.$('input').getAttribute('checked');
  }

  isSelected() {
    return this.isChecked().then(checked => {
      return !!checked;
    });
  }

  isPreview() {
    return this.control.$('.state-disabled').isPresent();
  }

  isDisabled() {
    return this.hasElementAttribute(this.control.$('input'), 'disabled');
  }

  isReadOnly() {
    return this.hasElementAttribute(this.control.$('input'), 'readonly');
  }

}

class CheckboxGroup extends FormControl {

}

class SingleSelectMenu extends InputField {

  constructor(field) {
    super(field);
  }

  /**
   * Select a value from single select menu.
   *
   * @param selector
   * @param key If byIndex=true then this argument is considered as 1 (not zero) based index for the options.
   * @param byIndex If searching for the option to be selecte should be by index or by option value.
   * @returns {*}
   */
  selectFromMenu(selector, key, byIndex) {
    return this.getControl(selector).$('.select2-selection__arrow').click().then(() => {
      this.waitUntilMenuIsOpened();
      var options = $('.select2-results__options');
      if (byIndex) {
        return options.$('li:nth-child(' + key + ')').click();
      } else {
        return options.$('li > span[data-value="' + key + '"]').click();
      }
    });
  }

  open() {
    var element = this.control.$('.select2-selection__arrow');
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
    element.click();
    this.waitUntilMenuIsOpened();
  }

  selectOption(name) {
    this.open();

    let option = this.getOptionByName(name);
    browser.wait(EC.elementToBeClickable(option), DEFAULT_TIMEOUT);
    option.click();
  }

  getOptionByName(name) {
    return $('.select2-results__options').element(by.cssContainingText('li', name));
  }

  /**
   * Returns the selected value from a single select field.
   * @param selector
   * @returns {*}
   */
  getSelectedValue(selector) {
    return this.getControl(selector).$$('.seip-select option:checked').then((items) => {
      if (items.length === 0) {
        return null;
      }
      return items[0].getAttribute('value');
    });
  }

  getSelectedLabel() {
    return this.control.$$('.seip-select option:checked').then((items) => {
      if (items.length === 0) {
        return null;
      }
      return items[0].getText();
    });
  }

  clearField(selector) {
    return this.getControl(selector).$('.select2-selection__clear').click();
  }

  toggleMenu(selector) {
    this.getControl(selector).click();
    browser.wait(EC.stalenessOf($('li.select2-results__option.loading-results')), DEFAULT_TIMEOUT);
  }

  getMenuElements() {
    return element.all(by.css('.select2-results li')).then((options) => {
      var valuePromises = options.map((option) => {
        return option.getText();
      });
      return Promise.all(valuePromises).then((values) => {
        return values;
      });
    });
  }

  getMenuValues() {
    return this.control.$$('.seip-select option').then((options) => {
      var valuePromises = options.map((option) => {
        return option.getAttribute('value');
      });
      return Promise.all(valuePromises).then((values) => {
        return values;
      });
    });
  }

  getPlaceholder(selector) {
    return this.getControl(selector).element(by.className('select2-selection__placeholder'));
  }

  isDisabled(selector) {
    return this.hasElementAttribute(this.getControl(selector).$('select'), 'disabled');
  }

  isReadOnly(selector) {
    return this.hasElementAttribute(this.getControl(selector).$('select'), 'readonly');
  }

  isOptionDisabled(name) {
    return this.getOptionByName(name).getAttribute('aria-disabled').then(function (attribute) {
      return !!attribute;
    });
  }

  filter(value) {
    $('.select2-search__field').sendKeys(value);
  }

  waitUntilMenuIsOpened() {
    browser.wait(EC.visibilityOf($('.select2-results__options')), DEFAULT_TIMEOUT);
    browser.wait(EC.stalenessOf($('.select2-results__option.loading-results')), DEFAULT_TIMEOUT);
  }

  static waitForSelectionToRender(element) {
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(element.$('.select2-selection__rendered')), DEFAULT_TIMEOUT);
  }
}

class MultySelectMenu extends SingleSelectMenu {

  constructor(field) {
    super(field);
  }

  /**
   * Remove a value from multy select menu.
   *
   * @param selector
   * @param key If byIndex=true then this argument is considered as 1 (not zero) based index for the options.
   * @param byIndex If searching for the option to be selected should be by index or by option value.
   * @returns {*}
   */
  removeFromSelection(selector, key, byIndex) {
    var selectElement = this.getControl(selector);
    if (byIndex) {
      return selectElement.$(`.select2-selection__choice:nth-child(${key}) .select2-selection__choice__remove`).click();
    } else {
      var selectWebElement = selectElement.getWebElement();
      return browser.executeScript("return $(arguments[0]).find('.seip-select').val()", selectWebElement).then((selection) => {
        selection = selection || [];
        var index = selection.indexOf(key);
        if (index > -1) {
          selection.splice(index, 1);
        }
        return browser.executeScript("$(arguments[0]).find('.seip-select').val(arguments[1]).change()", selectWebElement, selection);
      });
    }
  }

  /**
   * Remove the value from the multi select menu by it's title attribute.
   * @param title the element's title attribute
   * @returns {*}
   */
  removeFromSelectionByTitle(title) {
    return this.control.$(`.select2-selection__choice[title=\"${title}\"] .select2-selection__choice__remove`).click();
  }

  /**
   * Overrides the method from Single select to return selected values as an array.
   * @param selector
   * @returns {*} Returns an array of string values or an empty array if there  is no selection.
   */
  getSelectedValue(selector) {
    var webElement;
    if (selector) {
      webElement = this.getControl(selector).$('select').getWebElement();
    } else {
      webElement = this.control.$('select').getWebElement();
    }
    return browser.executeScript("return $(arguments[0]).val()", webElement).then((values) => {
      return values || [];
    });
  }

  /**
   * Select from a multi select menu by opening it and selecting an option.
   * @param selector
   * @param key
   * @param byIndex
   * @returns {*}
   */
  selectFromMenu(selector, key, byIndex) {
    if (byIndex) {
      return this.getControl(selector).$('.select2-selection').click().then(() => {
        this.waitUntilMenuIsOpened();
        return $('.select2-results__options li:nth-child(' + key + ')').click();
      });
    } else {
      return this.selectFromMenuByValue(key);
    }
  }

  selectFromMenuByValue(value) {
    this.open();
    return $(`.select2-results__option span[data-value='${value}']`).click();
  }

  selectFromMenuByIndex(index) {
    this.open();
    return $('.select2-results__options li:nth-child(' + index + ')').click();
  }

  getAvailableSelectChoices() {
    this.toggleMenu();
    return $$('.select2-results li > span').map((choice) => {
      return choice.getAttribute('data-value');
    });
  }

  open() {
    let field = this.control.$('.select2-search__field');
    field.isPresent().then(present => {
      if (present) {
        field.click();
      } else {
        this.control.$('.select2-selection').click();
      }
    });

    this.waitUntilMenuIsOpened();
  }

  /**
   * Checks if the select menu is opened and closes it if it is.
   *
   * @param selectElement - the select menu element to close
   * @returns a Promise resolved when it is closed
   */
  closeMenu(selectElement) {
    return $$('.select2-results__option').then((options) => {
      if (options.length > 0) {
        return selectElement.$('.select2-selection').click().then(() => {
          browser.wait(EC.not(EC.visibilityOf($('.select2-results__options'))), DEFAULT_TIMEOUT);
        });
      }
    });
  }

}

class TreeSelect extends MultySelectMenu {

  constructor(element) {
    super(element);
  }

  toggleOption(name) {
    let option = this.getOptionByName(name);
    // the toggle button is assigned using css :after and cannot be located using regular selector
    browser.actions()
      .mouseMove(option, {
        x: 2, y: 2
      })
      .mouseDown()
      .mouseUp()
      .perform();
  }

}

class TagSelectMenu extends SingleSelectMenu {

  constructor(field) {
    super(field);
    this.waitUntilVisible();
  }

  waitUntilVisible() {
    var renderedSelection = this.control.$('.select2-selection__rendered');
    browser.wait(EC.visibilityOf(renderedSelection), DEFAULT_TIMEOUT);
  }

  enterValue(value) {
    return this.control.$('input').sendKeys(value, protractor.Key.ENTER);
  }

  /**
   * Gets the visible selected values.
   */
  getSelectedValue() {
    var selectionsPromise = this.control.$$('.select2-selection .select2-selection__choice');
    return selectionsPromise.then((selections) => {
      return Promise.all(selections.map((selection) => {
        return selection.getAttribute('title');
      }));
    });
  }

  isDisplayed() {
    return this.control.$('.select2-selection__rendered').isDisplayed();
  }

}

class RadioButtonGroup extends InputField {

  constructor(field) {
    super(field);
  }

  /**
   * @param selector The group wrapper selector.
   */
  getSelectedValue(selector) {
    return this.getAttributeValue(selector + ' [type=radio]:checked', 'value');
  }

  /**
   * @param selector The group wrapper selector
   * @param value The value to be set.
   */
  selectValue(selector, value) {
    return $(selector + ' [value="' + value + '"] ~ i').click();
  }

  /**
   * Finds out all radio buttons in the group and checks if they are disabled.
   * @param selector The group wrapper selector
   */
  isDisabled(selector) {
    // collect all not disabled fields and the check if found any assume that the group is not properly disabled
    return $$(selector + ' input').filter(function (field) {
      return field.getAttribute('disabled').then(function (disabled) {
        return !!disabled;
      });
    }).then(function (notDisabledFields) {
      return notDisabledFields.length > 0;
    });
  }

}

class Button {

  constructor(element) {
    this.element = element;
  }

  click() {
    return this.element.click();
  }

  isEnabled() {
    return this.element.isEnabled();
  }

  isDisabled() {
    return this.element.isEnabled().then(function (value) {
      return !value;
    });
  }

  isPresent() {
    return this.element.isPresent();
  }
}

class User extends FormControl {
  constructor(field) {
    super(field);
  }

  isHeaderPresent() {
    return this.control.$('.instance-header').isPresent();
  }

  getHeaderText() {
    return this.control.$('.instance-header .instance-data').getAttribute('textContent');
  }
}

class ObjectControl extends FormControl {

  constructor(field) {
    super(field);
  }

  isPreview() {
    this.isSelectInstanceButtonHidden();
    this.isRemoveInstanceButtonHidden();
  }

  clickSelectButton() {
    let element = this.control.$('.select-instance-btn');
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
    element.click();
  }

  clickRemoveButton() {
    let element = this.control.$('.remove-instance-btn');
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
    element.click();
  }

  /**
   * Returns selected objects which are visible. If there are more selected objects which are not visible because the
   * control is collapsed they won't be returned.
   * @returns {wdpromise.Promise<any[]>}
   */
  getSelectedObjects(expectedObjects = 0) {
    if (expectedObjects > 0) {
      browser.wait(() => {
        return this.control.$$('.instance-box').then((count) => {
          return count.length >= expectedObjects;
        });
      }, DEFAULT_TIMEOUT);
    }
    return this.control.$$('.instance-box').then((els) => {
      return els.map((el) => {
        return new ObjectControlItem(el);
      });
    });
  }

  /**
   * Returns selected objects which are in select2.
   * @returns {wdpromise.Promise<any[]>}
   */
  getSelectedObjectsRenderedInSelect2() {
    let renderedSelection = this.control.$('.select2-selection__rendered');
    if(renderedSelection.length) {
      browser.wait(EC.visibilityOf(renderedSelection), DEFAULT_TIMEOUT);

      return renderedSelection.$$('li.select2-selection__choice').then((els) => {
        return els.map((el) => {
          return new ObjectControlItem(el);
        });
      });
    } else {
      return [];
    }
  }

  /**
   * Verifies if given header text is present on given position in the object header value.
   *
   * @param index In object control, visible objects indexes begins from 0.
   * @param header The header text to compare against.
   */
  isHeaderVisible(index, header) {
    this.getSelectedObjects(index + 1).then((selectedObjects) => {
      browser.wait(EC.textToBePresentInElement(selectedObjects[index].element, header), DEFAULT_TIMEOUT);
    });
  }

  /**
   * Returns visible selected objects count.
   *
   * @expectedCount The expected selected objects count which this method would wait for.
   */
  getSelectedObjectsCount(expectedCount) {
    return Promise.all([this.getSelectedObjects(expectedCount), this.getSelectedObjectsRenderedInSelect2()]).then(([selectedObjects, selectedObjectsInSelect2]) => {
      return selectedObjects.length + selectedObjectsInSelect2.length;
    });
  }

  /**
   * Useful function to check if given number of visible selected items will appear in the field. For example in the
   * create dialog, items are suggested asynchronously and if we check them immediately, they might be missing, so
   * better wait for them and this function does just that.
   * @param expectedCount The expected number of visible items to appear in the field.
   */
  waitForSelectedItems(expectedCount) {
    browser.wait(() => {
      return this.getSelectedObjectsCount(expectedCount).then((count) => {
        return expectedCount === count;
      });
    }, DEFAULT_TIMEOUT);
  }

  /**
   * Returns the objects count which are not visible because the component is collapsed.
   */
  getHiddenObjectsCount() {
    this.isShowAllButtonVisible();
    let counterElement = this.getShowAllButton().$('.hidden-objects-count');
    browser.wait(EC.visibilityOf(counterElement), DEFAULT_TIMEOUT, 'Hidden objects counter should be visible!');

    return counterElement.getText().then((text) => {
      return text.trim();
    });
  }

  verifyHiddenObjectsCountIs(count) {
    browser.wait(EC.textToBePresentInElement(this.getShowAllButton(), count + ''), DEFAULT_TIMEOUT, 'Hidden objects count should be present!');
  }

  selectInstance() {
    let button = this.getSelectInstanceButton();
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT, 'Select instance button should be clickable!');
    button.click();
  }

  /**
   * Removes an object from the selection using the remove button.
   * @param index of selected object.
   */
  removeInstance(index) {
    this.getSelectedObjects(1).then((selectedObjects => {
      let instance = selectedObjects[index].getElement();
      browser.wait(EC.visibilityOf(instance), DEFAULT_TIMEOUT);
      browser.actions().mouseMove(instance, {x: 2, y: 2}).perform();
      let removeButton = instance.$('.remove-instance-btn');
      let cond = EC.and(EC.elementToBeClickable(removeButton), elementToStopMoving(removeButton));
      browser.wait(cond, DEFAULT_TIMEOUT);
      removeButton.click();
    }));
  }

  /**
   * Remove the value from the multi select menu by it's title attribute.
   * @param title the element's title attribute
   * @returns {*}
   */
  removeFromSelectionByTitle(title) {
    return this.control.$(`.select2-selection__choice[title=\"${title}\"] .select2-selection__choice__remove`).click();
  }

  removeFromSelectionByIndex(index) {
    return this.control.$(`.select2-selection__choice:nth-child(${index+1}) .select2-selection__choice__remove`).click();
  }

  isRemoveButtonVisible(index) {
    browser.wait(EC.visibilityOf(this.control.$(`.select2-selection__choice:nth-child(${index+1}) .select2-selection__choice__remove`)), DEFAULT_TIMEOUT);
  }

  isRemoveButtonHidden(index) {
    browser.wait(EC.invisibilityOf(this.control.$(`.select2-selection__choice:nth-child(${index+1}) .select2-selection__choice__remove`)), DEFAULT_TIMEOUT);
  }

  isElementSelectedByTitle(title) {
    browser.wait(EC.visibilityOf(this.control.$(`.select2-selection__choice[title=\"${title}\"]`)), DEFAULT_TIMEOUT)
  }

  getInstance(index) {
    return this.getSelectedObjects(index + 1).then((selectedObjects => {
      return selectedObjects[index].getElement();
    }));
  }

  showAll() {
    this.getShowAllButton().click();
  }

  showLess() {
    this.getShowLessButton().click();
  }

  isSelectInstanceButtonVisible() {
    browser.wait(EC.visibilityOf(this.getSelectInstanceButton()), DEFAULT_TIMEOUT, 'Select instance button should be visible!');
  }

  isSelectInstanceButtonHidden() {
    browser.wait(EC.invisibilityOf(this.getSelectInstanceButton()), DEFAULT_TIMEOUT, 'Select instance button should be hidden!');
  }

  getSelectInstanceButton() {
    return this.control.$('.select-instance-btn');
  }

  isRemoveInstanceButtonVisible() {
    browser.wait(EC.visibilityOf(this.getRemoveInstanceButton()), DEFAULT_TIMEOUT, 'Remove instance button should be visible!');
  }

  isRemoveInstanceButtonHidden() {
    browser.wait(EC.invisibilityOf(this.getRemoveInstanceButton()), DEFAULT_TIMEOUT, 'Remove instance button should be hidden!');
  }

  getRemoveInstanceButton() {
    return this.control.$('.remove-instance-btn');
  }

  isShowAllButtonVisible() {
    browser.wait(EC.visibilityOf(this.getShowAllButton()), DEFAULT_TIMEOUT, 'Show all selected objects button should be visible!');
  }

  isShowAllButtonHidden() {
    browser.wait(EC.invisibilityOf(this.getShowAllButton()), DEFAULT_TIMEOUT, 'Show all selected objects button should be hidden!');
  }

  getShowAllButton() {
    return this.control.$('.show-more-objects');
  }

  isShowLessButtonVisible() {
    browser.wait(EC.visibilityOf(this.getShowLessButton()), DEFAULT_TIMEOUT, 'Show less button should be visible!');
  }

  isShowLessButtonHidden() {
    browser.wait(EC.invisibilityOf(this.getShowLessButton()), DEFAULT_TIMEOUT, 'Show less button should be hidden!');
  }

  getShowLessButton() {
    return this.control.$('.show-less-objects');
  }
}

class ObjectControlItem {
  constructor(elem) {
    this.element = elem;
  }

  getHeader() {
    return this.element.$('.instance-box-header').getText();
  }

  getElement() {
    return this.element;
  }
}

/**
 * Represents a region panel inside the generated forms that wraps other fields.
 */
class Region {

  constructor(element) {
    this.element = element;
  }

  getTitle() {
    return this.element.$('.panel-heading').getText();
  }

  getFields() {
    return this.element.$$('.form-group');
  }
}

class Resource extends FormControl {
  constructor(field) {
    super(field);
  }
}

class CodelistList extends FormControl {
  constructor(field) {
    super(field);
  }

  getAvailableOptions() {
    return this.control.$$('input').getAttribute('value');
  }

  getPreviewFields() {
    return this.control.$$('.preview-field label span');
  }

  getPrintFields() {
    return this.control.$$('.print-field label span');
  }

  getSelectedValue() {
    return this.control.$('div.edit-field').$$('input:checked').getAttribute('value');
  }

  /**
   * @param value The value to be set.
   */
  selectValue(value) {
    return this.control.$('div.edit-field').$(`[value="${value}"] + i`).click();
  }

  isDisabled() {
    // collect all not disabled fields and the check if found any assume that the group is not properly disabled
    return this.control.$$('input').filter((field) => {
      return field.getAttribute('disabled').then((disabled) => {
        return !!disabled;
      });
    }).then((notDisabledFields) => {
      return notDisabledFields.length > 0;
    });
  }
}

module.exports.FormControl = FormControl;
module.exports.InputField = InputField;
module.exports.InputPassword = InputPassword;
module.exports.DatetimeField = DatetimeField;
module.exports.CheckboxField = CheckboxField;
module.exports.CheckboxGroup = CheckboxGroup;
module.exports.RadioButtonGroup = RadioButtonGroup;
module.exports.SingleSelectMenu = SingleSelectMenu;
module.exports.MultySelectMenu = MultySelectMenu;
module.exports.TagSelectMenu = TagSelectMenu;
module.exports.Button = Button;
module.exports.User = User;
module.exports.ObjectControl = ObjectControl;
module.exports.ObjectControlItem = ObjectControlItem;
module.exports.Region = Region;
module.exports.Resource = Resource;
module.exports.CodelistList = CodelistList;
module.exports.TreeSelect = TreeSelect;