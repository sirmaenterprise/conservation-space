var IdocPage = require('../../idoc-page').IdocPage;
var ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectDataWidgetSandboxPage = require('./object-data-widget.js').ObjectDataWidgetSandboxPage;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var SaveIdocDialog = require('./../../save-idoc-dialog').SaveIdocDialog;
var TestUtils = require('../../../test-utils');

const FIELDS = {
  FIELD_ONE: 'field1',
  FIELD_TWO: 'field2',
  FIELD_THREE: 'field3'
};

describe('ObjectDataWidget', function () {
  var idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  it('should display only selected properties configured in the current widget for given object', () => {
    //Given I have inserted ODW for current object and selected field1 to be visible
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();

    //Given I have inserted a second ODW for current object and selected field2 to be visible
    var idocContent = idocPage.getTabEditor(1);
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_TWO);
    widgetConfig.save();

    // When I save idoc
    idocPage.getActionsToolbar().saveIdoc();

    // Then each ODW should show its preselected fields
    var widget1 = idocContent.getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 0);
    checkFields(widget1, ['presenceOf', 'invisibilityOf', 'invisibilityOf'], allFields);

    var widget2 = idocContent.getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 1);
    checkFields(widget2, ['presenceOf', 'invisibilityOf', 'invisibilityOf'], [FIELDS.FIELD_TWO, FIELDS.FIELD_ONE, FIELDS.FIELD_THREE]);
  });

  it('should bind widgets displaying one and the same objects to same object values model', () => {
    // given I have inserted first ODW for current object and selected field 1 to be visible
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();

    // given I have inserted a second ODW for current object and selected field 1 to be visible
    var idocContent = idocPage.getTabEditor(1);
    idocContent.getParagraph(1).click();
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    propertiesSelector.selectProperty(FIELDS.FIELD_TWO);
    widgetConfig.save();

    // when I change field1 value in the first ODW
    var widget1 = idocContent.getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 0);
    clearField(widget1.$$('#' + FIELDS.FIELD_ONE).first()).sendKeys('One');

    // then the field1 value in second ODW should be the same as those in the first ODW
    var widget2 = idocContent.getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 1);
    expect(widget2.$$('#' + FIELDS.FIELD_ONE).first().getAttribute('value')).to.eventually.equal('One');
  });

  it('can be inserted in idoc when in edit mode', () => {
    //When I insert 'Object Data Widget'
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    widgetConfig.save();

    //Then I should see the inserted widget
    return expect(widgetElement.isPresent()).to.eventually.be.true;
  });

  it('should show new entered data when iDoc is saved ', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();

    browser.wait(EC.visibilityOf(widgetElement.element(by.id(FIELDS.FIELD_ONE))), DEFAULT_TIMEOUT);

    widgetElement.element(by.id(FIELDS.FIELD_ONE)).clear().sendKeys('One');
    widgetElement.element(by.id(FIELDS.FIELD_TWO)).clear().sendKeys('Two');
    widgetElement.element(by.id(FIELDS.FIELD_THREE)).clear().sendKeys('Test');
    idocPage.getActionsToolbar().saveIdoc();

    expect(element(by.id(FIELDS.FIELD_ONE)).getAttribute('value')).to.eventually.equal('One');
    expect(element(by.id(FIELDS.FIELD_TWO)).getAttribute('value')).to.eventually.equal('Two');
    expect(element(by.id(FIELDS.FIELD_THREE)).getAttribute('value')).to.eventually.equal('Test');
  });

  it('should show initial data if iDoc cancel button is pressed ', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();
    var actionsToolbar = idocPage.getActionsToolbar();
    actionsToolbar.saveIdoc();
    actionsToolbar.getActionsMenu().editIdoc();

    idocPage.waitForEditMode();
    browser.wait(EC.presenceOf(widgetElement.$(`input#${FIELDS.FIELD_ONE}`)), DEFAULT_TIMEOUT);

    widgetElement.$(`input#${FIELDS.FIELD_ONE}`).clear().sendKeys('One');
    widgetElement.$(`input#${FIELDS.FIELD_TWO}`).clear().sendKeys('Two');
    widgetElement.$(`input#${FIELDS.FIELD_THREE}`).clear().sendKeys('Test');
    actionsToolbar.cancelSave();

    let conformation = idocPage.getConformationPopup();
    conformation.clickConfirmButton();

    idocPage.waitForPreviewMode();
    browser.wait(EC.presenceOf($(`span#${FIELDS.FIELD_ONE}.preview-field`)), DEFAULT_TIMEOUT);

    expect($(`span#${FIELDS.FIELD_ONE}.preview-field`).getText()).to.eventually.equal('value1');
    expect($(`span#${FIELDS.FIELD_TWO}.preview-field`).getText()).to.eventually.equal('value2');
    expect($(`span#${FIELDS.FIELD_THREE}.preview-field`).getText()).to.eventually.equal('value3');
  });

  it('should show only selected in configuration dialog fields', () => {
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
    widgetConfig.save();

    var widget = idocPage.getTabEditor(1).getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 0);
    checkFields(widget, ['presenceOf', 'presenceOf', 'invisibilityOf'], allFields);
  });

  it('should not show any fields if none are selected in configuration dialog', () => {
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    widgetConfig.save();

    var widget = idocPage.getTabEditor(1).getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 0);
    checkFields(widget, ['invisibilityOf', 'invisibilityOf', 'invisibilityOf'], allFields);
  });

  it('should show all fields if all fields are selected', () => {
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();

    var widget = idocPage.getTabEditor(1).getWidgetByNameAndOrder(ObjectDataWidget.WIDGET_NAME, 0);
    checkFields(widget, ['presenceOf', 'presenceOf', 'presenceOf'], allFields);
  });

  it('should display default configuration properties', () => {
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    var displayOptionsTab = widgetConfig.selectDisplayOptionsTab();
    expect(displayOptionsTab.isSelectedShowFieldsBorder()).to.eventually.be.false;
    widgetConfig.save();
    idocPage.getActionsToolbar().saveIdoc();
    expect(TestUtils.hasClass($('.form-wrapper .preview'), 'input-field-border')).to.eventually.be.false;
    expect(TestUtils.hasClass($('.object-data-widget'), 'no-border')).to.eventually.be.false;
  });

  it('should show borders around properties', () => {
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    var displayOptionsTab = widgetConfig.selectDisplayOptionsTab();
    displayOptionsTab.toggleShowFieldsBorder();
    expect(displayOptionsTab.isSelectedShowFieldsBorder()).to.eventually.be.true;
    widgetConfig.save();
    idocPage.getActionsToolbar().saveIdoc();
    expect(TestUtils.hasClass($('.form-wrapper'), 'with-border')).to.eventually.be.true;
    expect(TestUtils.hasClass($('.panel-default'), 'no-border')).to.eventually.be.false;
  });

  it('should update its configuration after it is already initialized', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    // Check default value
    expect(widgetConfig.selectDisplayOptionsTab().isSelectedShowRegionNames()).to.eventually.be.true;
    widgetConfig.save();
    var widget = new ObjectDataWidget(widgetElement);

    // Change value
    widget.getHeader().openConfig();
    widgetConfig.selectDisplayOptionsTab().toggleShowRegionNames();
    expect(widgetConfig.selectDisplayOptionsTab().isSelectedShowRegionNames()).to.eventually.be.false;
    widgetConfig.save();

    // Check that changed value is preserved
    widget.getHeader().openConfig();
    expect(widgetConfig.selectDisplayOptionsTab().isSelectedShowRegionNames()).to.eventually.be.false;
  });

  it('should not be allowed idoc save with visible invalid data in ODW @slow', () => {
    // Given I have created idoc with completed all mandatory data
    // When I insert ODW for current object and display all properties or just single mandatory one
    var odwElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    // When I save ODW config
    widgetConfig.save();
    // When I open ODW config again
    var odw = new ObjectDataWidget(odwElement);
    odw.getHeader().openConfig();
    // When I save ODW config second time
    widgetConfig.save();
    // When I delete some mandatory property value - it becomes invalid
    odw.getForm().getInputText(FIELDS.FIELD_ONE).clearValue().then(() => {
      // When I click save idoc button
      idocPage.getActionsToolbar().saveIdoc(true);
    });
    // Then Dialog with current object form is opened with the mandatory property marked as invalid
    var saveIdocDialog = new SaveIdocDialog();
    // When I fill in mandatory fields
    var field = saveIdocDialog.getForm().getInputText(FIELDS.FIELD_ONE);
    field.setValue(null, 'new-value');
    // When I press save action in dialog
    saveIdocDialog.ok();

    // Then idoc is saved
    idocPage.waitForPreviewMode();
  });

  it('should deregister old object from context\'s sharedObjectsRegistry if selected object is changed', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();

    // Select Object 1 to be displayed in ODW
    var search = widgetConfig.selectObjectSelectTab().getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();

    clearField(widgetElement.$$('#' + FIELDS.FIELD_TWO).first());
    idocPage.getActionsToolbar().saveIdoc(true);

    var widget = new ObjectDataWidget(widgetElement);
    var saveIdocDialog = new SaveIdocDialog();
    // Id of the first result from the search i.e. the object displayed in the ODW
    expect(saveIdocDialog.hasGroupFor('1')).to.eventually.be.true;
    saveIdocDialog.cancel();

    // Change Object 1 with Object 2 to be displayed in ODW
    // Object 1 should be removed from sharedObjectsRegistry and not displayed in save idoc dialog
    widget.getHeader().openConfig();
    search = widgetConfig.selectObjectSelectTab().getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(1);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();

    clearField(widgetElement.$$('#' + FIELDS.FIELD_ONE).first());
    idocPage.getActionsToolbar().saveIdoc(true);
    saveIdocDialog.waitUntilOpened();
    expect(saveIdocDialog.hasGroupFor('1')).to.eventually.be.false;
    expect(saveIdocDialog.hasGroupFor('2')).to.eventually.be.true;
  });

  it('should deregister object from context\'s sharedObjectsRegistry if widget is deleted', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();

    // Select Object 1 to be displayed in ODW
    var search = widgetConfig.selectObjectSelectTab().getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();

    clearField(widgetElement.$('#' + FIELDS.FIELD_TWO));
    idocPage.getActionsToolbar().saveIdoc(true);

    var widget = new ObjectDataWidget(widgetElement);
    var saveIdocDialog = new SaveIdocDialog();
    saveIdocDialog.waitUntilOpened();
    // Id of the first result from the search i.e. the object displayed in the ODW
    expect(saveIdocDialog.hasGroupFor('1')).to.eventually.be.true;
    saveIdocDialog.cancel();

    widget.getHeader().remove();
    idocPage.getActionsToolbar().saveIdoc(true);
    expect(saveIdocDialog.element.isPresent()).to.eventually.be.false;
  });

  it('should not show link to the selected object by default', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();
    var widget = new ObjectDataWidget(widgetElement);
    expect(widget.isHeaderVisible()).to.eventually.be.false;
  });

  it('should show link to displayed object when option is selected', () => {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    var propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    var displayOptionsTab = widgetConfig.selectDisplayOptionsTab();
    displayOptionsTab.toggleShowDefaultHeader();
    widgetConfig.save();
    var widget = new ObjectDataWidget(widgetElement);
    expect(widget.isHeaderVisible()).to.eventually.be.true;
  });

  var allFields = [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO, FIELDS.FIELD_THREE];

  /**
   * @param context An element in which context to search the fields.
   * @param conditions Expected condition names as array. Every condition is checked against the specified field from the fields argument.
   * @param fields Field ids as array. Every field is checked using the expected condition provided in the conditions argument
   */
  function checkFields(context, conditions, fields) {
    browser.wait(EC.visibilityOf(context), DEFAULT_TIMEOUT);
    fields.forEach(function (fieldId, index) {
      var field = context.element(by.id(fieldId));
      browser.wait(EC[conditions[index]](field), DEFAULT_TIMEOUT);
    });
  }

  /**
   * Clears the value from form text field
   * @param fieldElement which value should be cleared
   */
  function clearField(fieldElement) {
    browser.wait(EC.visibilityOf(fieldElement), DEFAULT_TIMEOUT);
    return fieldElement.clear();
  }
});

describe('ObjectDataWidget showing icons', function () {

  var page = new ObjectDataWidgetSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
    page.toggleModelingMode();
  });

  it('should show icons by default', () => {
    // When I inserted ODW
    page.insertWidget();
    // And I configured object selection to be current object
    let widgetConfig = new ObjectDataWidgetConfig();
    let search = widgetConfig.selectObjectSelectTab().getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperty('objectProperty');
    widgetConfig.save();
    // Then I expect the property icon to be visible
    let widget = page.getWidget();
    let formWrapper = widget.getForm();
    let objectPropertyField = formWrapper.getInstanceHeaderField('#objectProperty-wrapper');
    browser.wait(EC.visibilityOf(objectPropertyField.getIcon()), DEFAULT_TIMEOUT, 'Property Icon must be visible');
  });

  it('should not show icons if do not show icons in widget option is selected', () => {
    // When I inserted ODW
    page.insertWidget();
    // And I configured object selection to be current object
    let widgetConfig = new ObjectDataWidgetConfig();
    let search = widgetConfig.selectObjectSelectTab().getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperty('objectProperty');
    // And I selected widget display options
    let displayOptionsTab = widgetConfig.selectDisplayOptionsTab();
    //And I selected to hide the icons in the widget
    displayOptionsTab.toggleDisplayIcons();
    widgetConfig.save();
    // Then I expect the property icon to be hidden
    let widget = page.getWidget();
    let formWrapper = widget.getForm();
    let objectPropertyField = formWrapper.getInstanceHeaderField('#objectProperty-wrapper');
    browser.wait(EC.invisibilityOf(objectPropertyField.getIcon()), DEFAULT_TIMEOUT, 'Property Icon must be hidden');
  });

});
