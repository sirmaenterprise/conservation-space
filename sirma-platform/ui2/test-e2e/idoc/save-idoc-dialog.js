"use strict";

var Dialog = require('./../components/dialog/dialog');
var FormWrapper = require('./../form-builder/form-wrapper').FormWrapper;

class SaveIdocDialog extends Dialog {

  constructor() {
    super($('.modal'));
  }

  getForm() {
    return new FormWrapper(this.element);
  }

  hasGroupFor(instanceId) {
    return this.element.$(`#invalidObjectsList .panel[data-object-id='${instanceId}']`).isPresent();
  }
}

module.exports.SaveIdocDialog = SaveIdocDialog;