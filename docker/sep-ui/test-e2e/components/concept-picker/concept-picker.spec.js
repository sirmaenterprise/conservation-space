'use strict';

let ConceptPickerSandboxPage = require('./concept-picker').ConceptPickerSandboxPage;

describe('Concept picker', function () {

  let picker;
  let page;

  beforeEach(function () {
    page = new ConceptPickerSandboxPage();
    page.open();
    picker = page.getConceptPicker();
  });

  it('should provide concepts for selection and update the model on selection', function () {
    picker.selectOption('Concrete');

    browser.wait(EC.textToBePresentInElement(page.getSelectedValue(), '["concrete","metal"]'), DEFAULT_TIMEOUT);
  });

});