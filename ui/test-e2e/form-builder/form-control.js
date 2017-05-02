"use strict";

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

  getHiddenPreviewValue() {
    return this.control.$('.preview-field').getInnerHtml();
  }

  /**
   * Get the element's innerHTML.
   * @param selector
   * @returns {!webdriver.promise.Promise.<string>}
   */
  getHtml(selector) {
    return this.getControl(selector).getInnerHtml();
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
    return this.control.$('.preview-field').isPresent();
  }

  isPrint(selector) {
    return $(`${selector}.print-field`).isPresent();
  }

  isEditable() {
    return this.control.$('.form-control').isPresent();
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

  getAttributeValue(selector, attributeName) {
    return this.getControl(selector).getAttribute(attributeName);
  }

  waitToShow() {
    browser.wait(EC.visibilityOf(this.control), DEFAULT_TIMEOUT);
  }

  hasCssClass(element, className) {
    return element.getAttribute('class').then((attribute) => {
      return attribute.indexOf(className) !== -1;
    });
  }
}

class InputField extends FormControl {

  constructor(control) {
    super(control);
  }

  getInputElement() {
    var inputElement = this.control.$('input');
    browser.wait(EC.visibilityOf(inputElement), DEFAULT_TIMEOUT);
    return inputElement;
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
      $(selector).sendKeys(text);
      browser.wait(EC.textToBePresentInElementValue($(selector), text), DEFAULT_TIMEOUT);
    } else {
      var inputElement = this.getInputElement();
      var script = 'var field = $(arguments[0]); field.val("' + text + '"); field.trigger("change")';
      browser.executeScript(script, inputElement.getWebElement());
      inputElement.click();
      browser.wait(EC.textToBePresentInElementValue(inputElement, text), DEFAULT_TIMEOUT);
    }
  }

  blurField(selector) {
    var script = 'var field = $(arguments[0]); field.trigger("blur");';
    browser.executeScript(script, $(selector).getWebElement());
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
    if(selector) {
      return super.isDisabled(selector);
    }
    return this.getInputElement().getAttribute('disabled').then((attribute) => {
      return !!attribute;
    });
  }

}

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
   * Sets today's date using the picker control.
   */
  setTodayDateByPicker() {
    this.waitForDatePickerToBeInitialized();
    this.openCalendar();
    this.selectToday();
  }

  /**
   * Selects the button in the picker that sets the today's date.
   * NOTE: The picker has to be opened first.
   */
  selectToday() {
    var buttonSelector = $('[data-action=today]');
    browser.wait(EC.visibilityOf(buttonSelector), DEFAULT_TIMEOUT);
    buttonSelector.click();
  }

  /**
   * Selects the button in the picker that clears the field.
   * NOTE: The picker has to be opened first.
   */
  selectClear() {
    var buttonSelector = $('[data-action=clear]');
    browser.wait(EC.visibilityOf(buttonSelector), DEFAULT_TIMEOUT);
    buttonSelector.click();
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

  // Not testable timepicker component because the lack of proper css classes or html attributes to be used as selectors
  // If datetime should be set, use the #setDatetime method.
  setTimeByPicker(selector, hour, minute) {
    //this.waitForDatePickerToBeInitialized(selector);
    //var timePickerToggle = $('[data-action=togglePicker]');
    //timePickerToggle.click();
    //browser.wait(EC.visibilityOf($('.timepicker-picker')), DEFAULT_TIMEOUT);
    //$('.timepicker-hour').click();
    //browser.wait(EC.visibilityOf($('.timepicker-hours')), DEFAULT_TIMEOUT);
    //$('[data-action=selectHour]')
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
   * Clears the date field using the picker.
   */
  clearDateByPicker() {
    this.waitForDatePickerToBeInitialized();
    this.openCalendar();
    this.selectClear();
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
    browser.wait(EC.visibilityOf($('.bootstrap-datetimepicker-widget')), DEFAULT_TIMEOUT);
  }

  openCalendar() {
    this.waitForDatePickerToBeInitialized();
    this.control.$('.fa-calendar').click();
    this.waitForCalendarToBeVisible();
  }

  /**
   * Switches the picker from date to time selection mode.
   */
  openTimePicker() {
    this.openCalendar();
    var timePickerToggle = $('[data-action=togglePicker]');
    browser.wait(EC.visibilityOf(timePickerToggle), DEFAULT_TIMEOUT);
    timePickerToggle.click();
    browser.wait(EC.visibilityOf($('.timepicker-picker')), DEFAULT_TIMEOUT);
  }

  hasTimePicker() {
    this.openCalendar();
    return $('[data-action=togglePicker]').isPresent().then((present) => {
      return !!present;
    });
  }

  isDisabled() {
    return this.control.$('input').getAttribute('disabled').then((attribute) => {
      return !!attribute;
    });
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
    return this.control.$('label.checkbox').click();
  }

  isChecked() {
    return this.control.$('input').getAttribute('checked');
  }

  isPreview() {
    return this.control.$('.state-disabled').isPresent();
  }

  isDisabled() {
    return this.control.$('input').getAttribute('disabled');
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
    this.control.$('.select2-selection__arrow').click();
    this.waitUntilMenuIsOpened();
  }

  selectOption(name) {
    this.open();

    this.getOptionByName(name).click();
  }

  getOptionByName(name) {
    var options = $('.select2-results__options');
    var item = options.element(by.cssContainingText('li', name));
    browser.wait(EC.elementToBeClickable(item), DEFAULT_TIMEOUT);
    return item;
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
    return element.all(by.css('.select2-results li'));
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
    return this.getControl(selector).$('select').getAttribute('disabled').then((attribute) => {
      return !!attribute;
    });
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
    var webElement = this.getControl(selector).$('select').getWebElement();
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
    return this.control.$('.select2-selection').click().then(() => {
      this.waitUntilMenuIsOpened();
      return $(`.select2-results__option span[data-value='${value}']`).click();
    });
  }

  getAvailableSelectChoices() {
    this.toggleMenu();
    return $$('.select2-results li > span').map((choice) => {
      return choice.getAttribute('data-value');
    });
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
    return $(selector + ' [value="' + value + '"]').element(by.xpath('..')).click();
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
    this.element.click();
  }

  isEnabled() {
    return this.element.isEnabled();
  }

  isDisabled() {
    return this.element.isEnabled().then(function (value) {
      return !value;
    });
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
    super(field)
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
    return this.control.$$('input:checked').getAttribute('value');
  }

  /**
   * @param value The value to be set.
   */
  selectValue(value) {
    return this.control.$('[value="' + value + '"]').element(by.xpath('..')).click();
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
module.exports.Region = Region;
module.exports.Resource = Resource;
module.exports.CodelistList = CodelistList;
