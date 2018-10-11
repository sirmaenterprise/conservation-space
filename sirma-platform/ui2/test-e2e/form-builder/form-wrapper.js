"use strict";

let FormControl = require('./form-control.js').FormControl;
let InputField = require('./form-control.js').InputField;
let CheckboxField = require('./form-control.js').CheckboxField;
let SingleSelectMenu = require('./form-control.js').SingleSelectMenu;
let MultySelectMenu = require('./form-control.js').MultySelectMenu;
let DatetimeField = require('./form-control.js').DatetimeField;
let ObjectControl = require('./form-control.js').ObjectControl;
let RichText = require('./textfield/rich-text.js').RichText;
let StaticInstanceHeader = require('../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;

class FormWrapper {

  /**
   * Constructs a FormWrapper instance using the provided element. In case the ".form-wrapper" element is provided
   * directly, then the second boolean parameter "useProvidedElement" must be set to be true. Otherwise its optional and
   * in this case it will be assumed that provided element is an ancestor of the ".form-wrapper" and this constructor
   * would try to find the later.
   * @param wrapperElement The ".form-wrapper" element or its ancestor.
   * @param useProvidedElement A boolean flag which shows if the provided element should be used as is or the
   * ".form-wrapper" should be selected whiting the context of the argument.
   */
  constructor(wrapperElement, useProvidedElement) {
    if(useProvidedElement) {
      this.formWrapper = wrapperElement;
    } else {
      this.formWrapper = wrapperElement.$('.form-wrapper');
    }
  }

  /**
   * Wait until the .form-content is visible to guarantee the form builder
   * has been built correctly
   */
  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.formWrapper.$('.form-content')), DEFAULT_TIMEOUT);
  }

  /**
   * Switches the form view mode to preview.
   */
  togglePreviewMode() {
    element(by.id('togglePreview')).click();
    this.checkViewMode('.preview');
  }

  /**
   * Switches the form view mode to edit.
   */
  toggleEditMode() {
    element(by.id('togglePreview')).click();
    this.checkViewMode('.edit');
  }

  checkViewMode(mode) {
    browser.wait(EC.presenceOf($(mode)), DEFAULT_TIMEOUT);
  }

  getFormElement() {
    return this.formWrapper;
  }

  getFieldWrapper(name) {
    return this.formWrapper.$(`#${name}-wrapper`);
  }

  isFieldVisible(name) {
    browser.wait(EC.visibilityOf(this.getFieldWrapper(name)), DEFAULT_TIMEOUT, `Form field with selector ${name} should be visible!`);
  }

  isFieldHidden(name) {
    browser.wait(EC.invisibilityOf(this.getFieldWrapper(name)), DEFAULT_TIMEOUT, `Form field with selector ${name} should be hidden!`);
  }

  /**
   * @param selector The id of the field as in the view model.
   * @returns {*}
   */
  findField(selector) {
    browser.wait(EC.presenceOf(this.formWrapper.element(by.id(selector))), DEFAULT_TIMEOUT);
    return this.formWrapper.element(by.id(selector));
  }

  /**
   * Find out input field wrapper by its id in the context of the current form.
   * @param name The name of the field same as in the view model.
   * @returns {*|InputField}
   */
  getInputText(name) {
    return new InputField(this.findField(`${name}-wrapper`));
  }

  getRichTextField(name) {
    return new RichText(this.findField(`${name}-wrapper`));
  }

  getCheckboxField(name) {
    return new CheckboxField(this.findField(`${name}-wrapper`));
  }

  getCodelistField(name, multivalue) {
    if (multivalue) {
      return new MultySelectMenu(this.findField(`${name}-wrapper`));
    }
    return new SingleSelectMenu(this.findField(`${name}-wrapper`));
  }

  getDateField(name) {
    return new DatetimeField(this.findField(`${name}-wrapper`));
  }

  getObjectControlField(name) {
    return new ObjectControl(this.findField(`${name}-wrapper`));
  }

  getInstanceHeaderField(name) {
    return new StaticInstanceHeader($(`${name} .instance-header`));
  }

  getPreviewField(name) {
    return new InputField(this.formWrapper.$('#' + name));
  }

  getAllFields() {
    return this.formWrapper.$$('.form-group:not(.hidden)').then((elements) => {
      return elements.map((el) => {
        return new FormControl(el);
      });
    });
  }

  getMandatoryFieldsCount() {
    return this.getAllFields().then((fields) => {
      let promises = [];
      fields.forEach((field) => {
        promises.push(field.isMandatory());
      });
      return Promise.all(promises).then((results) => {
        return results.filter((result) => {
          return result === true;
        }).length;
      });
    });
  }

  getAllFieldLabels() {
    return this.formWrapper.$$('.form-group:not(.hidden)').then((fields) => {
      let promises = fields.map((el) => {
        return el.getText();
      });
      return Promise.all(promises).then((text) => {
        return text;
      });
    });
  }

  /**
   * NOTE: Use this method only in the form builder controls related tests as the model value in the sandboxes there is
   * displayed inside output fields for every field displayed in the form and the value is taken from those output fields.
   * They are not present in other sandboxes where the form builder is used.
   * @param propertyName
   * @return A promise resolving with the property value as presented in the model.
   */
  getPropertyValue(propertyName) {
    return $(`#${propertyName}-model`).getAttribute('value');
  }

}

module.exports.FormWrapper = FormWrapper;