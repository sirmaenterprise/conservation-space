var IdocPage = require('../../idoc-page');
var Widget = require('./../widget').Widget;
var ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var SaveIdocDialog = require('./../../save-idoc-dialog').SaveIdocDialog;

describe('Conditions validator', function () {

  var idocPage = new IdocPage('/sandbox/idoc/idoc-page/condition-validator');
  beforeEach(() => {
    idocPage.open(true);
  });

  // regression test for CMF-19453
  it('should not be able to save idoc with mandatory field made by MANDATORY condition @slow', () => {
    // Given I selected to create an idoc.
    // And I have inserted ODW configured to show current object with all fields visible.
    var odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('field1');
    propertiesSelector.selectProperty('field2');
    widgetConfig.save();
    // And I have completed field1 with value 'test'.
    var odw = new ObjectDataWidget(odwElement);
    odw.getForm().getInputText('field1').setValue(null, 'test');
    // And The field2 became mandatory.
    var ODWfield2 = odw.getForm().getInputText('field2');
    expect(ODWfield2.isMandatory(), 'Field 2 should be mandatory in ODW!').to.eventually.be.true;
    expect(ODWfield2.hasError(), 'Field 2 should have red border in ODW!').to.eventually.be.true;
    expect(ODWfield2.getMessages().count(), 'Field 2 should have an error message!').to.eventually.equal(1);
    // When I click save action.
    idocPage.getActionsToolbar().saveIdoc(true);
    // Then Save dialog should be opened with the idoc properties.
    var saveIdocDialog = new SaveIdocDialog();
    // And field2 should be marked as mandatory.
    var field2 = saveIdocDialog.getForm().getInputText('field2');
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
    // Given I have selected to create an idoc
    // And I have inserted an ODW configured to show the current object with a checkboxTrigger and field3 fields visible
    // (the checkbox field is a trigger for conditions applied to field3 and field3 is mandatory=true by default, see
    // idoc-page-stub.data.json where is the model for the sandbox used in this test)
    var odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('checkboxTrigger');
    propertiesSelector.selectProperty('field3');

    // When I save the widget config
    widgetConfig.save();
    // Then ODW is displayed and the field3 should be hidden by condition
    var odw = new ObjectDataWidget(odwElement);
    var field3 = odw.getForm().getInputText('field3');
    expect(field3.isVisible(), 'Field 3 should be hidden').to.eventually.equal(false);

    // When I select the checkboxTrigger field
    odw.getForm().getCheckboxField('checkboxTrigger').toggleCheckbox();
    // Then The field3 should become visible and readonly
    field3 = odw.getForm().getInputText('field3');
    expect(field3.isVisible(), 'Field 3 should be visible').to.eventually.equal(true);
    // And The field3 should become mandatory
    expect(field3.isPreview(), 'Field 3 should be readonly').to.eventually.equal(true);

    // When I select the checkboxTrigger field
    odw.getForm().getCheckboxField('checkboxTrigger').toggleCheckbox();
    field3 = odw.getForm().getInputText('field3');
    expect(field3.isVisible(), 'Field 3 should be hidden').to.eventually.equal(false);
  });

  // regression for https://ittruse.ittbg.com/jira/browse/CMF-21177
  // When condition makes a field inside hidden region to be mandatory, then the field and the region have to be displayed
  it('should toggle region visibility if MANDATORY condition changes region field status @slow', () => {
    // Given I have selected to create an idoc
    // And I have inserted an ODW configured to show the current object with a selectTriggerMandatoryInHiddenRegion checkbox visible
    // (the checkbox triggers a MANDATORY condition applied to a field in a hidden region)
    var contentArea = idocPage.getTabEditor(1);
    var odwElement = contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty('selectTriggerMandatoryInHiddenRegion');
    propertiesSelector.selectProperty('region2Field1');
    // When I save the widget config
    widgetConfig.save();

    // Then ODW is displayed and the region2 and region2Field1 must be hidden
    var odw = new ObjectDataWidget(odwElement);
    var region2Field1 = odw.getForm().getFormElement().$('#region2Field1-wrapper');
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
    var paragraphUnderWidget = contentArea.getParagraph(1);
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