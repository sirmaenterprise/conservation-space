'use strict';

let InstanceCreatePanelSandboxPage = require('./../../../create/instance-create-panel').InstanceCreatePanelSandboxPage;
let SandboxPage = require('../../../page-object').SandboxPage;

let page = new SandboxPage();

describe('Related codelist fields in create dialog', () => {

  let instanceCreatePanelSandboxPage = new InstanceCreatePanelSandboxPage();

  let instanceCreatePanel;

  beforeEach(() => {
    page.open('sandbox/create/instance-create-panel');
    instanceCreatePanelSandboxPage.openCreateDialog();
    instanceCreatePanel = instanceCreatePanelSandboxPage.getInstanceCreatePanel();
  });

  it('should filter related field even master field is not visible', () => {
    // Given I have opened instance create dialog
    // When I select a document which has related department2 and functional2 codelist fields
    instanceCreatePanel.selectSubType('Document with related fields');

    // Then I expect department2 field to be hidden as it is not mandatory
    let form = instanceCreatePanel.getForm();
    let department2 = $('#department2-wrapper');
    browser.wait(EC.stalenessOf(department2), DEFAULT_TIMEOUT);

    // And I expect functional2 field to have filtered value 'Mechanical Design Group' although the department2 field is
    // hidden.
    let functional2 = form.getCodelistField('functional2', false);
    checkAvailableOptions(functional2, ['Mechanical Design Group']);
  });

  it('should filter related field when master field is visible', () => {
    // Given I have opened instance create dialog
    // When I select a document which has related department and functional codelist fields
    instanceCreatePanel.selectSubType('Document with related fields');

    // Then I expect Department field to have default value 'Engeneering department'
    let form = instanceCreatePanel.getForm();
    let department = form.getCodelistField('department', false);
    expect(department.getSelectedValue()).to.eventually.equal('ENG');

    // And I expect Functional field to have filtered value 'Mechanical Design Group'
    let functional = form.getCodelistField('functional', false);
    checkAvailableOptions(functional, ['Mechanical Design Group']);

    // When I clear the Department field
    // browser.pause();
    department.clearField();
    // clearField leaves the menu open and it occasionally may hide other field below or above (depending on which side
    // dropdown was opened), so we need to toggle the menu
    department.toggleMenu();

    // Then I expect the functional field to have unfiltered value ['Mechanical Design Group', 'Electrical Design Group']
    checkAvailableOptions(functional, ['Mechanical Design Group', 'Electrical Design Group']);

    // When I select INF value in the Department field
    department.selectOption('Infrastructure department');

    // Then I expect the functional field to have value EDG
    checkAvailableOptions(functional, ['Electrical Design Group']);

    // When I select TSD value in the Department field
    department.selectOption('Test department');

    // Then I expect the functional field to have unfiltered value ['Mechanical Design Group', 'Electrical Design Group']
    checkAvailableOptions(functional, ['Mechanical Design Group', 'Electrical Design Group']);
  });

  it('should filter related field when it`s not visible (mandatory=false)', () => {
    // Given I have opened instance create dialog
    // When I select a document which has related department3 and functional3 codelist fields
    instanceCreatePanel.selectSubType('Document with related fields');

    // Then I expect department3 field to have default value 'Infrastructure department'
    let form = instanceCreatePanel.getForm();
    let department3 = form.getCodelistField('department3', false);
    expect(department3.getSelectedValue()).to.eventually.equal('INF');

    // And I expect functional3 field to be hidden as its optional
    let functional3Wrapper = $('#functional3-wrapper');
    browser.wait(EC.stalenessOf(functional3Wrapper), DEFAULT_TIMEOUT);

    // When I select show more option
    instanceCreatePanel.toggleShowMore();

    // Then I expect functional3 field to become visible
    let functional3 = form.getCodelistField('functional3', false);

    // And I expect functional3 field to have filtered value 'Electrical Design Group'
    checkAvailableOptions(functional3, ['Electrical Design Group']);
  });

  // regression test for CMF-28937
  it('should filter related field defined in current definition only', () => {
    // Given I have opened instance create dialog
    // When I select a document which has related department and functional codelist fields
    instanceCreatePanel.selectSubType('Document with related fields');

    // Then I expect Department field to have default value 'Engeneering department'
    let form = instanceCreatePanel.getForm();
    let department = form.getCodelistField('department', false);
    expect(department.getSelectedValue()).to.eventually.equal('ENG');

    // And I expect Functional field to have filtered value 'Mechanical Design Group'
    let functional = form.getCodelistField('functional', false);
    checkAvailableOptions(functional, ['Mechanical Design Group']);

    // When I change document type to document which has same department and functional fields but they are not related
    instanceCreatePanel.selectSubType('Document without related fields');

    // Then I expect Functional field to have unfiltered value ['Mechanical Design Group', 'Electrical Design Group']
    let form2 = instanceCreatePanel.getForm();
    let functional2 = form2.getCodelistField('functional', false);
    checkAvailableOptions(functional2, ['Mechanical Design Group', 'Electrical Design Group']);
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