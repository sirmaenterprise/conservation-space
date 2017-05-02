"use strict";

var FormControl = require('./form-control.js').FormControl;
var InputField = require('./form-control.js').InputField;
var CheckboxField = require('./form-control.js').CheckboxField;
var SingleSelectMenu = require('./form-control.js').SingleSelectMenu;

class FormWrapper {

  constructor(wrapperElement) {
    this.formWrapper = wrapperElement.$('.form-wrapper');
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

  getCheckboxField(name) {
    return new CheckboxField(this.findField(`${name}-wrapper`));
  }

  getCodelistField(name) {
    return new SingleSelectMenu(this.findField(`${name}-wrapper`));
  }

  getPreviewField(name) {
    return new InputField(this.formWrapper.$('#' + name));
  }

  getAllFields() {
    return this.formWrapper.$$('.form-group:not(.hidden)').then((elements) => {
      return elements.map((el, index) => {
        return new FormControl(el);
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

}

module.exports.FormWrapper = FormWrapper;