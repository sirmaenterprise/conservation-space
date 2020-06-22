'use strict';

let SandboxPage = require('../../page-object.js').SandboxPage;
let TreeSelect = require('../../form-builder/form-control').TreeSelect;

class ConceptPickerSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/components/concept-picker');
  }

  getConceptPicker() {
    let browserElement = $('.concept-picker');
    browser.wait(EC.presenceOf(browserElement), DEFAULT_TIMEOUT);
    return new ConceptPicker(browserElement);
  }

  getSelectedValue() {
    return $('.selected-items');
  }

}

class ConceptPicker extends TreeSelect {

  constructor(element) {
    super(element);
  }

}

module.exports.ConceptPickerSandboxPage = ConceptPickerSandboxPage;
module.exports.ConceptPicker = ConceptPicker;