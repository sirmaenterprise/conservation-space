var IdocPage = require('../../idoc-page').IdocPage;
var ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var SaveIdocDialog = require('./../../save-idoc-dialog').SaveIdocDialog;

describe('Idoc model validations', function () {

  var idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true, 'emf:123456');
  });

  // Regression test for CMF-19977
  it('should display all mandatory and invalid fields @slow', () => {
    // Given I have created idoc.
    // And I have inserted ODW configured to show current object with all fields visible.
    var odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectAllProperties();
    widgetConfig.save();
    // And I filled invalid data in the optional 'field5'.
    var odw = new ObjectDataWidget(odwElement);
    odw.getForm().getInputText('field5').setValue(null, 'invalid value');
    // And I cleared the 'field4'.
    odw.getForm().getInputText('field1').clearValue();
    // When I click save action.
    idocPage.getActionsToolbar().saveIdoc(true);
    // Then Save dialog should be opened with 'field4' and 'field5' marked as invalid.
    var saveIdocDialog = new SaveIdocDialog();
    var field4 = saveIdocDialog.getForm().getInputText('field4');
    expect(field4.isMandatory(), 'Field 4 should be mandatory in the save dialog!').to.eventually.be.true;
    var field5 = saveIdocDialog.getForm().getInputText('field5');
    expect(field5.hasError(), 'Field 5 should be marked as invalid in the save dialog!').to.eventually.be.true;
  });
});