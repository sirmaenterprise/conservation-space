'use strict';

let InstanceCreatePanelSandboxPage = require('./../../../create/instance-create-panel').InstanceCreatePanelSandboxPage;
let ObjectControl = require('./../../form-control.js').ObjectControl;
let Search = require('../../../search/components/search.js').Search;
let Dialog = require('../../../components/dialog/dialog');
let SandboxPage = require('../../../page-object').SandboxPage;

let page = new SandboxPage();

describe('Calculation validator', function () {

  let instanceCreatePanelSandboxPage = new InstanceCreatePanelSandboxPage();

  let instanceCreatePanel;

  beforeEach(() => {
    page.open('sandbox/form-builder/validation/calculation');
    instanceCreatePanelSandboxPage.openCreateDialog();
    instanceCreatePanel = instanceCreatePanelSandboxPage.getInstanceCreatePanel();
  });

  describe('for richtext fields', () => {
    it('should suggest values', () => {
      instanceCreatePanel.selectSubType('Document with RICHTEXT fields');
      let form = instanceCreatePanel.getForm();
      let mandatoryDescription = form.getRichTextField('mandatoryDescription');

      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><i>Document with RICHTEXT fields</i> <strong>(Infrastructure department)</strong>, created by: <span style="color:#c0392b;">John Doe</span> / <span style="color:#c0392b;">11/11/2017</span>.</p>');
      // I expect field to be valid as it's mandatory but has value
      mandatoryDescription.isValid();

      let name = form.getInputText('name');
      name.clearValue();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><i></i> <strong>(Infrastructure department)</strong>, created by: <span style="color:#c0392b;">John Doe</span> / <span style="color:#c0392b;">11/11/2017</span>.</p>');
      name.setValue(null, 'Document with suggested value');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><i>Document with suggested value</i> <strong>(Infrastructure department)</strong>, created by: <span style="color:#c0392b;">John Doe</span> / <span style="color:#c0392b;">11/11/2017</span>.</p>');

      let department = form.getCodelistField('department');
      department.selectOption('Test department');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><i>Document with suggested value</i> <strong>(Test department)</strong>, created by: <span style="color:#c0392b;">John Doe</span> / <span style="color:#c0392b;">11/11/2017</span>.</p>');
      department.clearField();
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><i>Document with suggested value</i> <strong>()</strong>, created by: <span style="color:#c0392b;">John Doe</span> / <span style="color:#c0392b;">11/11/2017</span>.</p>');
    });

    it('should stop suggesting when user changes the field manually', () => {
      instanceCreatePanel.selectSubType('Document with RICHTEXT fields');
      let form = instanceCreatePanel.getForm();
      let mandatoryDescription = form.getRichTextField('mandatoryDescription');

      mandatoryDescription.clear().focusEditor().type('New document title');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p>New document title</p>');

      let name = form.getInputText('name');
      name.setValue(null, 'Document with suggested value');
      expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p>New document title</p>');
    });
  });

  describe('for text type fields', () => {
    it('should suggest values object model', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Recommendations for deployment');
      // Then Title field should be evaluated with suggested values
      // The Title field value template is as follows: $[emf:name] ($[emf:department]), created by: $[emf:owner.title] / $[emf:createdOn].
      let form = instanceCreatePanel.getForm();
      let titleField = form.getInputText('title');
      // $[emf:owner.title] and $[emf:createdOn] are evaluated on backend initially
      browser.wait(EC.textToBePresentInElementValue(titleField.getValue(), ' (Infrastructure department), created by: John Doe / 11/11/2017.'), DEFAULT_TIMEOUT);
      // When I fill in the name field with value 'Test document'
      let nameField = form.getInputText('name');
      nameField.setValue(null, 'Test document');
      // Then I expect the Title field value to be updated dynamically
      browser.wait(EC.textToBePresentInElementValue(titleField.getValue(), 'Test document (Infrastructure department), created by: John Doe / 11/11/2017.'), DEFAULT_TIMEOUT);
      // When I select 'Infrastructure department' from the Department codelist field
      let departmentField = form.getCodelistField('department', false);
      departmentField.selectOption('Quality department');
      // Then I expect the Title field value to be updated dynamically
      browser.wait(EC.textToBePresentInElementValue(titleField.getValue(), 'Test document (Quality department), created by: John Doe / 11/11/2017.'), DEFAULT_TIMEOUT);
    });

    it('should stop suggesting when user changes the suggested field value', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Recommendations for deployment');
      // When I fill in the name field with value 'Test document'
      let form = instanceCreatePanel.getForm();
      let titleField = form.getInputText('title');
      let nameField = form.getInputText('name');
      nameField.setValue(null, 'Test document');
      // Then I expect the Title field value to be updated dynamically
      browser.wait(EC.textToBePresentInElementValue(titleField.getValue(), 'Test document (Infrastructure department), created by: John Doe / 11/11/2017.'), DEFAULT_TIMEOUT);
      // When I change the Title field value
      let titleElement = $('#title');
      titleElement.sendKeys('test');
      // Then Suggestions are stopped
      nameField.setValue(null, 'My document');
      browser.wait(EC.textToBePresentInElementValue(titleField.getValue(), 'Test document (Infrastructure department), created by: John Doe / 11/11/2017.test'), DEFAULT_TIMEOUT);
    });
  });

  describe('for codelist type fields', () => {
    it('should suggest value according to the binding', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Codelists document');
      // When I fill the second single select dropdown
      let form = instanceCreatePanel.getForm();
      let departmentField1 = form.getCodelistField('department1', false);
      form.getCodelistField('department2', false).selectOption('Infrastructure department');
      // Then I expect the first single select dropdown to be filled with suggested value
      expect(departmentField1.getSelectedValue()).to.eventually.deep.equal('INF');
      // When I fill the second multiple select dropdown
      let departmentField3 = form.getCodelistField('department3', true);
      form.getCodelistField('department4', true).selectFromMenuByIndex(1);
      form.getCodelistField('department4', true).selectFromMenuByIndex(2);
      // Then I expect the first multiple select dropdown to be filled with suggested values
      expect(departmentField3.getSelectedValue()).to.eventually.deep.equal(['ENG', 'INF']);
    });

    it('should stop suggest when user changes the suggested codelist field value', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      // When I fill first multiple select dropdown
      instanceCreatePanel.selectSubType('Codelists document');
      let form = instanceCreatePanel.getForm();
      let departmentField3 = form.getCodelistField('department3', true);
      form.getCodelistField('department3', true).selectFromMenuByIndex(1);
      // When I fill the second multiple select dropdown
      departmentField3 = form.getCodelistField('department3', true);
      form.getCodelistField('department4', true).selectFromMenuByIndex(1);
      form.getCodelistField('department4', true).selectFromMenuByIndex(2);
      // Then I expect the first multiple select dropdown to remine unchanged
      expect(departmentField3.getSelectedValue()).to.eventually.deep.equal(['ENG']);
    });

    it('should ignore suggested value if it\'s not in filtered codelist resultset', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      // When I fill first multiple select dropdown
      instanceCreatePanel.selectSubType('Document with related fields');
      let form = instanceCreatePanel.getForm();
      let functionalField4 = form.getCodelistField('functional4', true);
      // Then I expect suggested vallue to be filled in dropdown
      expect(functionalField4.getSelectedValue()).to.eventually.deep.equal('MDG');
      // When I change department value and functional is filtered
      form.getCodelistField('department4', true).selectFromMenuByIndex(2);
      // Then I expect dropdown to be empty because suggested value is not in filtered resultset
      expect(functionalField4.getSelectedValue()).to.eventually.deep.equal([]);
    });
  });

  describe('for datetime type fields', () => {
    it('should suggest value according to the binding', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Common document');
      // When I fill created date field
      let form = instanceCreatePanel.getForm();
      let createdDate = form.getDateField('createdDate');
      createdDate.setDatetime('#createdDate-wrapper .datetime-field', 'January/25/2012');
      createdDate.getDateField().sendKeys(protractor.Key.TAB);
      browser.wait(EC.textToBePresentInElementValue(createdDate.getDate(), 'January/25/2012'), DEFAULT_TIMEOUT);
      // Then I expect start date to be filled with suggested values
      let startDate = form.getDateField('startDate');
      browser.wait(EC.textToBePresentInElementValue(startDate.getDate(), 'January/25/2012'), DEFAULT_TIMEOUT);
    });

    it('should stop suggest when user changes the suggested date value', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Common document');
      // When I fill start date field
      let form = instanceCreatePanel.getForm();
      let startDate = form.getDateField('startDate');
      startDate.setDatetime('#startDate-wrapper .datetime-field', 'January/28/2012');
      // When I fill created date field
      let createdDate = form.getDateField('createdDate');
      createdDate.setDatetime('#createdDate-wrapper .datetime-field', 'January/25/2012');
      createdDate.getDateField().sendKeys(protractor.Key.TAB);
      browser.wait(EC.textToBePresentInElementValue(createdDate.getDate(), 'January/25/2012'), DEFAULT_TIMEOUT);
      // Then I expect start date to not be changed
      browser.wait(EC.textToBePresentInElementValue(startDate.getDate(), 'January/28/2012'), DEFAULT_TIMEOUT);
    });
  });

  describe('for picker type fields', () => {
    it('should suggest value according to the binding', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Pickers document');
      let form = instanceCreatePanel.getForm();
      let owner = form.getInstanceHeaderField('#owner-wrapper');
      // Then I expect owner to have default value filled from related picker
      browser.wait(EC.textToBePresentInElement(owner.getHeader(), 'John'), DEFAULT_TIMEOUT);
      // When I change value in creator picker
      new ObjectControl($('#creator-wrapper')).clickSelectButton();

      searchAndSelectResult(1);

      new Dialog($('.modal-dialog.modal-lg.ui-draggable')).ok();

      // Then I expect owner to be updated
      browser.wait(EC.textToBePresentInElement(owner.getHeader(), 'Object #2 VERY long and big Breadcrumb header'), DEFAULT_TIMEOUT);
    });

    it('should stop suggest if user deletes the value from the field', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Pickers document');
      let form = instanceCreatePanel.getForm();
      let owner = form.getInstanceHeaderField('#owner-wrapper');

      // When I delete owner
      let objectControl = new ObjectControl($('#owner-wrapper'));
      objectControl.removeInstance(0);
      // When I change value in creator picker
      new ObjectControl($('#creator-wrapper')).clickSelectButton();

      searchAndSelectResult(0);

      new Dialog($('.modal-dialog.modal-lg.ui-draggable')).ok();

      // Then I expect owner to be empty
      browser.wait(EC.stalenessOf(owner.getHeader()), DEFAULT_TIMEOUT);
    });

    it('should stop suggest if user selects another value in the field', () => {
      // Given I have opened instance create dialog
      // When I select a document subtype
      instanceCreatePanel.selectSubType('Pickers document');
      let form = instanceCreatePanel.getForm();
      let owner = form.getInstanceHeaderField('#owner-wrapper');

      // When I change value in owner picker
      new ObjectControl($('#owner-wrapper')).clickSelectButton();

      searchAndSelectResult(0);

      new Dialog($('.modal-dialog.modal-lg.ui-draggable')).ok();

      // When I change value in creator picker
      new ObjectControl($('#creator-wrapper')).clickSelectButton();

      searchAndSelectResult(1);

      new Dialog($('.modal-dialog.modal-lg.ui-draggable')).ok();

      // Then I expect owner to remain unchanged
      browser.wait(EC.textToBePresentInElement(owner.getHeader(), 'Object #1 Breadcrumb header'), DEFAULT_TIMEOUT);
    });

    // Regression scenario for https://jira.sirmaplatform.com/jira/browse/CMF-27553
    // When user removes the bound field's value, the source field is assigned a null value which brakes the picker.
    it('should allow field value to be changed if user has removed the value from the bound field', () => {
      // Given I have opened instance create dialog
      // And I have selected a document subtype
      instanceCreatePanel.selectSubType('Pickers document');
      browser.wait(EC.presenceOf($('#creator-wrapper')), DEFAULT_TIMEOUT);
      let owner = new ObjectControl($('#owner-wrapper'));
      let creator = new ObjectControl($('#creator-wrapper'));

      // When I remove the value from the creator field
      creator.removeInstance(0);

      // Then I expect the value from the owner field to be cleared too
      expect(owner.getSelectedObjectsCount()).to.eventually.equal(0);

      // And I expect to be able to select some new value for owner
      owner.clickSelectButton();
      searchAndSelectResult(1);
      new Dialog($('.modal-dialog.modal-lg.ui-draggable')).ok();
      expect(owner.getSelectedObjectsCount()).to.eventually.equal(1);
    });
  });

  function searchAndSelectResult(index) {
    let search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();

    let results = search.getResults();
    results.waitForResults();
    results.clickResultItem(index);
  }

});