'use strict';

let SingleSelectMenu = require('../../form-control.js').SingleSelectMenu;
let InputField = require('../../form-control.js').InputField;

let SandboxPage = require('../../../page-object').SandboxPage;

const DEPARTMENT = '#department-wrapper';
const FUNCTIONAL = '#functional-wrapper';
const CONDITIONAL = '#conditional-wrapper';
const TYPE = '#documentISOType-wrapper';
const SUGGEST = '#suggest-wrapper';
const TITLE = '#title-wrapper';
const DESCRIPTION = '#description-wrapper';
const NAME = '#name-wrapper';

describe('Related fields', () => {

  let page = new SandboxPage();

  function openRelatedFieldsPage() {
    page.open('/sandbox/form-builder/related-fields');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  }

  it('should show all available results by default', () => {
    openRelatedFieldsPage();
    let functionalField = new SingleSelectMenu($(FUNCTIONAL));
    checkAvailableOptions(functionalField, ['Mechanical Design Group', 'Electrical Design Group']);

    let isoTypeField = new SingleSelectMenu($(TYPE));
    checkAvailableOptions(isoTypeField, ['Form', 'List']);
  });

  it('should load correct values in related fields', () => {
    openRelatedFieldsPage();
    let departmentField = new SingleSelectMenu($(DEPARTMENT));
    departmentField.selectFromMenu(null, 1, true);

    let functionalField = new SingleSelectMenu($(FUNCTIONAL));
    checkAvailableOptions(functionalField, ['Mechanical Design Group']);

    let isoTypeField = new SingleSelectMenu($(TYPE));
    checkAvailableOptions(isoTypeField, ['Form']);

    departmentField.selectFromMenu(null, 2, true);

    checkAvailableOptions(functionalField, ['Electrical Design Group']);

    checkAvailableOptions(isoTypeField, ['List']);
  });

  it('should reload related field and keep selected value', () => {
    openRelatedFieldsPage();
    let departmentField = new SingleSelectMenu($(DEPARTMENT));
    let functionalField = new SingleSelectMenu($(FUNCTIONAL));
    departmentField.selectFromMenu(null, 1, true);
    functionalField.selectFromMenu(null, 1, true);
    departmentField.selectFromMenu(null, 3, true);
    expect(functionalField.getSelectedValue()).to.eventually.equal('MDG');
  });

  it('should switch document to print view', () => {
    openRelatedFieldsPage();
    let departmentField = new SingleSelectMenu($(DEPARTMENT));
    let functionalField = new SingleSelectMenu($(FUNCTIONAL));

    departmentField.selectFromMenu(null, 1, true);
    functionalField.selectFromMenu(null, 1, true);

    let titleField = new InputField($(TITLE));
    let descriptionField = new InputField($(DESCRIPTION));
    let nameField = new InputField($(NAME));

    titleField.setValue(null, 'Test Title Text');
    descriptionField.setValue(null, 'Test Description Text');
    nameField.setValue(null, 'Test Name Text');

    element(by.css('#viewModeInput input[value="PRINT"')).click();
    expect(departmentField.isPrintField()).to.eventually.be.true;
    expect(functionalField.isPrintField()).to.eventually.be.true;

    expect(titleField.isPrintField()).to.eventually.be.true;
    expect(descriptionField.isPrintField()).to.eventually.be.true;
    expect(nameField.isPrintField()).to.eventually.be.true;

    expect(departmentField.getSelectedValue()).to.eventually.equal('ENG');
    expect(functionalField.getSelectedValue()).to.eventually.equal('MDG');

    expect(titleField.getPrintValue()).to.eventually.equal('Test Title Text');
    expect(descriptionField.getPrintValue()).to.eventually.equal('Test Description Text');
    expect(nameField.getPrintValue()).to.eventually.equal('Test Name Text');
  });

  it('should suggest value for department and filter related fields', () => {
    openRelatedFieldsPage();
    let suggestField = new SingleSelectMenu($(SUGGEST));
    let departmentField = new SingleSelectMenu($(DEPARTMENT));
    let functionalField = new SingleSelectMenu($(FUNCTIONAL));
    let isoTypeField = new SingleSelectMenu($(TYPE));

    suggestField.selectFromMenu(null, 1, true);
    expect(departmentField.getSelectedValue()).to.eventually.equal('ENG');

    checkAvailableOptions(functionalField, ['Mechanical Design Group']);

    checkAvailableOptions(isoTypeField, ['Form']);
  });

  it('should change conditional field visibility depending on corresponding condition', () => {
    openRelatedFieldsPage();
    let conditionalField = new SingleSelectMenu($(CONDITIONAL));
    let suggestField = new SingleSelectMenu($(SUGGEST));
    let departmentField = new SingleSelectMenu($(DEPARTMENT));

    // Conditional field should be hidden when form is loaded
    expect(conditionalField.isPresent()).to.eventually.be.false;

    // Conditional field should be visible and filtered when department is in 'ENG'
    suggestField.selectFromMenu(null, 1, true);
    expect(departmentField.getSelectedValue()).to.eventually.equal('ENG');
    expect(conditionalField.isPresent()).to.eventually.be.true;
    expect(conditionalField.isMandatory()).to.eventually.be.false;
    checkAvailableOptions(conditionalField, ['Mechanical Design Group']);

    // Conditional field should be mandatory and filtered when department is in 'INF'
    suggestField.selectFromMenu(null, 2, true);
    expect(departmentField.getSelectedValue()).to.eventually.equal('INF');
    expect(conditionalField.isPresent()).to.eventually.be.true;
    expect(conditionalField.isMandatory()).to.eventually.be.true;
    checkAvailableOptions(conditionalField, ['Electrical Design Group']);

    // Conditional field should be hidden when department is in 'QLD'
    suggestField.selectFromMenu(null, 4, true);
    expect(departmentField.getSelectedValue()).to.eventually.equal('QLD');
    expect(conditionalField.isVisible()).to.eventually.be.false;
  });

});

function checkAvailableOptions(field, options) {
  field.toggleMenu();
  expect(field.getMenuElements()).to.eventually.have.length(options.length);
  options.forEach((option) => {
    expect(field.getMenuElements()).to.eventually.contain(option);
  });
  field.toggleMenu();
}