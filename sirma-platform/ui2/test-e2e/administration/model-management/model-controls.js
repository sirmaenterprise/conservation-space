'use strict';

let PageObject = require('../../page-object').PageObject;
let Notification = require('../../components/notification').Notification;
let Button = require('../../form-builder/form-control').Button;

/**
 * PO representing common model controls.
 *
 * @author Svetlozar Iliev
 */
class ModelControls extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getModelSave() {
    return new ModelControl(this.modelActions.$('.model-save'));
  }

  getModelCancel() {
    return new ModelControl(this.modelActions.$('.model-cancel'));
  }

  get modelActions() {
    return this.element.$('.model-actions > .controls');
  }
}

ModelControls.COMPONENT_SELECTOR = '.model-controls';

/**
 * PO representing a single model control.
 *
 * @author Svetlozar Iliev
 */
class ModelControl extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  isDisabled() {
    return this.control.isDisabled();
  }

  isEnabled() {
    return this.control.isEnabled();
  }

  isLoading() {
    return this.button.$('.fa-spinner').isDisplayed();
  }

  click() {
    this.control.click();
  }

  getLabel() {
    this.button.getText();
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }

  get control() {
    return new Button(this.button);
  }

  get button() {
    return this.element.$('.btn');
  }
}

module.exports = {
  ModelControls,
  ModelControl
};