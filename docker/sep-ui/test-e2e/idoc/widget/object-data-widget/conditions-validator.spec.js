'use strict';

let IdocPage = require('../../idoc-page').IdocPage;
let ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
let SaveIdocDialog = require('./../../save-idoc-dialog').SaveIdocDialog;

const MODIFIED_BY_ID = 'emf:modifiedBy';

describe('Conditions in ODW', () => {

  it('should resolve preview mode properly when there is ENABLED condition applied to object property and "show more" is active', () => {
    let idocPage = new IdocPage();
    idocPage.open(true);

    // Given I have created an object
    // And I have inserted an ODW with modifiedBy property visible
    let odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    browser.wait(EC.visibilityOf($('.object-data-widget .display-options-tab-handler')), DEFAULT_TIMEOUT)
    let widgetConfig = new ObjectDataWidgetConfig();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);

    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(7);

    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(MODIFIED_BY_ID);
    widgetConfig.save();
    let widget = new ObjectDataWidget(odwElement);
    idocPage.getActionsToolbar().saveIdoc(true);

    // When I open the object in preview mode
    // Then I expect to see the modifiedBy property
    let modifiedBy = widget.getForm().getObjectControlField(MODIFIED_BY_ID);

    // And I expect the modifiedBy property to be readonly as the view mode is preview
    modifiedBy.isPreview();

    // When I select "show more"
    widget.toggleShowMoreButton();

    // Then I expect the modifiedBy property to be readonly as the view mode is still preview
    modifiedBy = widget.getForm().getObjectControlField(MODIFIED_BY_ID);
    modifiedBy.isPreview();
  });

  it('should not hide optional fields appeared in save dialog once completed with valid value', () => {
    let idocPage = new IdocPage();
    idocPage.open(true);

    // Given I have opened idoc in edit mode.
    // And I have inserted ODW with one optional and one mandatory field selected.
    let odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);

    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(6);

    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('optionalField');
    propertiesSelector.selectProperty('mandatoryField');
    widgetConfig.save();
    let odw = new ObjectDataWidget(odwElement);

    // And I have completed invalid value in the optional field.
    let optionalField = odw.getForm().getInputText('optionalField');
    optionalField.setValue(null, 'invalid value');

    // And I have removed the value from the mandatory field.
    let mandatoryField = odw.getForm().getInputText('mandatoryField');
    mandatoryField.clearValue();

    // When I select save operation.
    idocPage.getActionsToolbar().saveIdoc(true);

    // Then Save dialog is opened with three fields visible:
    let saveIdocDialog = new SaveIdocDialog();
    // Visible because it has invalid value.
    let invalidOptionalField = saveIdocDialog.getForm().getInputText('optionalField');
    expect(invalidOptionalField.hasError()).to.eventually.be.true;
    // Visible because it's static mandatory without value.
    let staticMandatoryField = saveIdocDialog.getForm().getInputText('mandatoryField');
    expect(staticMandatoryField.isMandatory()).to.eventually.be.true;
    // Visible because it is conditional mandatory without value. When the "mandatoryField" is completed, this becomes optional.
    let conditionalMandatoryField = saveIdocDialog.getForm().getInputText('mandatoryByConditionField');
    expect(conditionalMandatoryField.isMandatory()).to.eventually.be.true;

    // When I fill in valid value in the optional field.
    invalidOptionalField.setValue(null, 'test');

    // Then I expect the field to remain visible in the dialog.
    expect(invalidOptionalField.isVisible()).to.eventually.be.true;

    // When I fill in the "mandatoryField".
    staticMandatoryField.setValue(null, 'test');

    // Then I expect the "conditionalMandatoryField" to become optional and to disappear from the form.
    expect(conditionalMandatoryField.isVisible()).to.eventually.be.false;
  });

  // regression test for CMF-19453
  it('should not be able to save idoc with mandatory field made by MANDATORY condition @slow', () => {
    let idocPage = new IdocPage('/sandbox/idoc/idoc-page/condition-validator');
    idocPage.open(true);

    // Given I selected to create an idoc.
    // And I have inserted ODW configured to show current object with all fields visible.
    let odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('field1');
    propertiesSelector.selectProperty('field2');
    widgetConfig.save();
    // And I have completed field1 with value 'test'.
    let odw = new ObjectDataWidget(odwElement);
    odw.getForm().getInputText('field1').setValue(null, 'test');
    // And The field2 became mandatory.
    let ODWfield2 = odw.getForm().getInputText('field2');
    expect(ODWfield2.isMandatory(), 'Field 2 should be mandatory in ODW!').to.eventually.be.true;
    expect(ODWfield2.hasError(), 'Field 2 should have red border in ODW!').to.eventually.be.true;
    expect(ODWfield2.getMessages().count(), 'Field 2 should have an error message!').to.eventually.equal(1);
    // When I click save action.
    idocPage.getActionsToolbar().saveIdoc(true);
    // Then Save dialog should be opened with the idoc properties.
    let saveIdocDialog = new SaveIdocDialog();
    // And field2 should be marked as mandatory.
    let field2 = saveIdocDialog.getForm().getInputText('field2');
    expect(field2.isMandatory(), 'Field 2 should be mandatory in save dialog!').to.eventually.be.true;
    // When I complete field2 with value 'test'.
    field2.setValue(null, 'test');
    // And I click save action in the save dialog.
    saveIdocDialog.ok();
    // Then The idoc have to be saved.
    idocPage.waitForPreviewMode();
  });

  // regression test for CMF-19609
  it('should apply conditions with a checkbox trigger immediately @slow', () => {
    let idocPage = new IdocPage('/sandbox/idoc/idoc-page/condition-validator');
    idocPage.open(true);

    // Given I have selected to create an idoc
    // And I have inserted an ODW configured to show the current object with a checkboxTrigger and field3 fields visible
    // (the checkbox field is a trigger for conditions applied to field3 and field3 is mandatory=true by default, see
    // idoc-page-stub.data.json where is the model for the sandbox used in this test)
    let odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('checkboxTrigger');
    propertiesSelector.selectProperty('field3');

    // When I save the widget config
    widgetConfig.save();
    // Then ODW is displayed and the field3 should be hidden by condition
    let odw = new ObjectDataWidget(odwElement);
    expect(odw.getForm().getFormElement().$('.field3-wrapper').isPresent(), 'Field 3 should not be present').to.eventually.be.false;

    // When I select the checkboxTrigger field
    odw.getForm().getCheckboxField('checkboxTrigger').toggleCheckbox();

    let field3 = odw.getForm().getInputText('field3');
    // Then The field3 should become visible and readonly
    field3 = odw.getForm().getInputText('field3');
    expect(field3.isVisible(), 'Field 3 should be visible').to.eventually.be.true;
    // And The field3 should become mandatory
    expect(field3.isPreview(), 'Field 3 should be readonly').to.eventually.be.true;

    // When I select the checkboxTrigger field
    odw.getForm().getCheckboxField('checkboxTrigger').toggleCheckbox();
    field3 = odw.getForm().getInputText('field3');
    expect(field3.isVisible(), 'Field 3 should be hidden').to.eventually.be.false;
  });

  // regression for https://ittruse.ittbg.com/jira/browse/CMF-21177
  // When condition makes a field inside hidden region to be mandatory, then the field and the region have to be displayed
  it('should toggle region visibility if MANDATORY condition changes region field status @slow', () => {
    let idocPage = new IdocPage('/sandbox/idoc/idoc-page/condition-validator');
    idocPage.open(true);

    // Given I have selected to create an idoc
    // And I have inserted an ODW configured to show the current object with a selectTriggerMandatoryInHiddenRegion checkbox visible
    // (the checkbox triggers a MANDATORY condition applied to a field in a hidden region)
    let contentArea = idocPage.getTabEditor(1);
    let odwElement = contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('selectTriggerMandatoryInHiddenRegion');
    propertiesSelector.selectProperty('region2Field1');
    // When I save the widget config
    widgetConfig.save();

    // Then ODW is displayed and the region2 and region2Field1 must be hidden
    let odw = new ObjectDataWidget(odwElement);
    let region2Field1 = odw.getForm().getFormElement().$('#region2Field1-wrapper');
    browser.wait(EC.presenceOf(region2Field1), DEFAULT_TIMEOUT);

    // When I select to trigger mandatory condition of region field
    odw.getForm().getCodelistField('selectTriggerMandatoryInHiddenRegion').selectOption('Обикновен документ');
    // Then the region2 and region2field1 must be visible and mandatory
    region2Field1 = odw.getForm().getInputText('region2Field1');
    expect(region2Field1.isVisible(), 'region2Field1 should be visible').to.eventually.equal(true);
    expect(region2Field1.isMandatory(), 'region2Field1 should be mandatory').to.eventually.equal(true);
    expect(odw.getForm().getFormElement().$('#region2-wrapper').isDisplayed(), 'region2 should be visible').to.eventually.equal(true);

    // When I deselect value
    odw.getForm().getCodelistField('selectTriggerMandatoryInHiddenRegion').clearField();
    // !!! when the select menu is cleared its dropdown opens and breaks the tests below so the focus should be moved on
    // some editor content outside the widget in order to force the dropdown to be closed
    let paragraphUnderWidget = contentArea.getParagraph(1);
    paragraphUnderWidget.click();
    // Then the region field must be visible because it is selected in the widget, editable and optional.
    expect(region2Field1.isVisible(), 'region2Field1 should be visible').to.eventually.equal(true);
    expect(region2Field1.isMandatory(), 'region2Field1 should be optional').to.eventually.equal(false);
    expect(odw.getForm().getFormElement().$('#region2-wrapper').isDisplayed(), 'region2 should be visible').to.eventually.equal(true);

    // When I select to trigger hidden condition of region field
    odw.getForm().getCodelistField('selectTriggerMandatoryInHiddenRegion').selectOption('Препоръки за внедряване');
    // Then the region2 and region2field1 must be hidden
    expect(region2Field1.isVisible(), 'region2Field1 should be hidden').to.eventually.equal(false);
    expect(odw.getForm().getFormElement().$('#region2-wrapper').isDisplayed(), 'region2 should be hidden').to.eventually.equal(false);

    // When I select to trigger mandatory condition of region field (I want to check if it becomes visible again)
    odw.getForm().getCodelistField('selectTriggerMandatoryInHiddenRegion').selectOption('Обикновен документ');
    // Then the region2 and region2field1 must be visible and mandatory
    region2Field1 = odw.getForm().getInputText('region2Field1');
    expect(region2Field1.isVisible(), 'region2Field1 should be visible').to.eventually.equal(true);
    expect(region2Field1.isMandatory(), 'region2Field1 should be mandatory').to.eventually.equal(true);
    expect(odw.getForm().getFormElement().$('#region2-wrapper').isDisplayed(), 'region2 should be visible').to.eventually.equal(true);
  });
});