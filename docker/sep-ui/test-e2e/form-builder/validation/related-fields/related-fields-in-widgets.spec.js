'use strict';

let IdocPage = require('../../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../../idoc/widget/object-selector/object-selector.js').ObjectSelector;
let ObjectDataWidget = require('../../../idoc/widget/object-data-widget/object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('../../../idoc/widget/object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;
let DatatableWidget = require('../../../idoc/widget/data-table-widget/datatable-widget.js').DatatableWidget;
let DatatableWidgetConfigDialog = require('../../../idoc/widget/data-table-widget/datatable-widget').DatatableWidgetConfigDialog;

describe('Related codelist fields in widgets', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  it('should filter related fields displayed in same DTW', () => {
    // Given I have configured DTW to show two related codelist fields where department is primary and functional is secondary field
    let widget = insertDTWWithFields(idocPage, ['department', 'functional']);
    let form = widget.getRow(1).getForm();
    let functional = form.getCodelistField('functional');
    let department = form.getCodelistField('department');

    // When The department field has value ENG
    expect(department.getSelectedValue()).to.eventually.equal('ENG');
    checkAvailableOptions(department, ['Engeneering department', 'Infrastructure department', 'Test department', 'Quality department']);

    // Then I expect functional field to have option MDG
    checkAvailableOptions(functional, ['Mechanical Design Group']);

    // When I select INF value in the department field
    department.selectOption('Infrastructure department');

    // Then I expect the functional field to have value EDG
    checkAvailableOptions(functional, ['Electrical Design Group']);

    // When I select the value EDG in the functional field
    functional.selectOption('Electrical Design Group');

    // And I change the value of department field to ENG
    department.selectOption('Engeneering department');

    // Then I expect functional field value to be cleared as its not applicable according to the filter
    expect(functional.getSelectedValue()).to.eventually.equal(null);

    // And The functional field must have available option MDG
    checkAvailableOptions(functional, ['Mechanical Design Group']);
  });

  it('should filter related fields displayed in separated DTW', () => {
    // Given I have configured DTW to show department codelist field which is primary field
    let widget1 = insertDTWWithFields(idocPage, ['department'], 0);
    let form1 = widget1.getRow(1).getForm();
    let department = form1.getCodelistField('department');

    // Then I expect department field to have option ENG
    expect(department.getSelectedValue()).to.eventually.equal('ENG');

    // And I have configured second DTW to show functional codelist field which is secondary related field
    let widget2 = insertDTWWithFields(idocPage, ['functional'], 1);
    let form2 = widget2.getRow(1).getForm();
    let functional = form2.getCodelistField('functional');

    // Then I expect functional field to have option MDG
    checkAvailableOptions(functional, ['Mechanical Design Group']);

    // When I select INF value in the department field
    department.selectOption('Infrastructure department');

    // Then I expect the functional field to have value EDG
    checkAvailableOptions(functional, ['Electrical Design Group']);

    // When I select the value EDG in the functional field
    functional.selectOption('Electrical Design Group');

    // And I change the value of department field to ENG
    department.selectOption('Engeneering department');

    // Then I expect functional field value to be cleared as its not applicable according to the filter
    expect(functional.getSelectedValue()).to.eventually.equal(null);

    // And The functional field must have available option MDG
    checkAvailableOptions(functional, ['Mechanical Design Group']);
  });

  // DTW
  it('should filter related field when the master field is not selected in same DTW', () => {
    // Given I have created an object

    // And I have inserted a DTW configured to show manually selected object
    // And I have selected only the "functional" - the related field to be visible in the widget
    let widget = insertDTWWithFields(idocPage, ['functional']);

    // When I open the "functional" field
    let form = widget.getRow(1).getForm();
    let functional = form.getCodelistField('functional');

    // And I expect the options in "functional" field to be "Electrical Design Group"
    checkAvailableOptions(functional, ['Mechanical Design Group']);
  });

  // ODW
  it('should filter related fields displayed in same ODW', () => {
    // Given I have created an object

    // And I have inserted an ODW configured to show a codelist fields "department" and "functional"
    let widget = insertODWWithFields(idocPage, ['department', 'functional']);

    // When I select value "Engeneering department" in "department" field
    let form = widget.getForm();
    let department = form.getCodelistField('department');
    let functional = form.getCodelistField('functional');
    department.selectOption('Engeneering department');

    // Then I expect the functional field in widget2 to be empty in widget2
    expect(functional.getSelectedValue()).to.eventually.equal(null);

    // And The available options to be "Mechanical Design Group"
    checkAvailableOptions(functional, ['Mechanical Design Group']);

    // And I select value "Infrastructure department" in department field in widget1
    department.selectOption('Infrastructure department');

    // Then I expect the functional field to be cleared in widget2 as it becomes inapplicable according to the value in department field
    expect(functional.getSelectedValue()).to.eventually.equal(null);

    // And I expect the options in functional field to be "Electrical Design Group"
    checkAvailableOptions(functional, ['Electrical Design Group']);
  });

  // ODW-ODW
  it('should filter related fields displayed in separated ODW', () => {
    // Given I have created an object

    // And I have inserted first ODW configured to show a codelist field "department"
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widget1 = new ObjectDataWidget($$('.object-data-widget').get(0));
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    selectProperties(widgetConfig, ['department']);
    widgetConfig.save();

    // And I have inserted second ODW configured to show a related to "department" codelist field "functional"
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widget2 = new ObjectDataWidget($$('.object-data-widget').get(1));
    widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    selectProperties(widgetConfig, ['functional']);
    widgetConfig.save();

    // When I select value "Engeneering department" in department field in widget1
    let form1 = widget1.getForm();
    let department1 = form1.getCodelistField('department');
    let form2 = widget2.getForm();
    let functional2 = form2.getCodelistField('functional');

    department1.selectOption('Engeneering department');

    // Then I expect the functional field in widget2 to be empty in widget2
    expect(functional2.getSelectedValue()).to.eventually.equal(null);

    // And The available options to be "Mechanical Design Group"
    checkAvailableOptions(functional2, ['Mechanical Design Group']);

    // When I select value "Mechanical Design Group" in functional field in widget2
    functional2.selectOption('Mechanical Design Group');

    // And I select value "Infrastructure department" in department field in widget1
    department1.selectOption('Infrastructure department');

    // Then I expect the functional field to be cleared in widget2 as it becomes inapplicable according to the value in department field
    expect(functional2.getSelectedValue()).to.eventually.equal(null);

    // And I expect the options in functional field to be "Electrical Design Group"
    checkAvailableOptions(functional2, ['Electrical Design Group']);
  });

  // ODW-ODW
  it('should continue to filter related fields in separated ODW after one widget is deleted', () => {
    // Given I have created an object

    // And I have inserted first ODW configured to show a codelist field "department"
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widget1 = new ObjectDataWidget($$('.object-data-widget').get(0));
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    selectProperties(widgetConfig, ['department', 'functional']);
    widgetConfig.save();

    // And I have inserted second ODW configured to show a related to "department" codelist field "functional"
    idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widget2 = new ObjectDataWidget($$('.object-data-widget').get(1));
    widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    selectProperties(widgetConfig, ['department', 'functional']);
    widgetConfig.save();

    // When I select value "Engeneering department" in department field in widget1
    let form1 = widget1.getForm();
    let department1 = form1.getCodelistField('department');
    let functional1 = form1.getCodelistField('functional');
    let form2 = widget2.getForm();
    let functional2 = form2.getCodelistField('functional');

    department1.selectOption('Engeneering department');

    // Then I expect the functional field in widget2 to be empty in widget2
    expect(functional2.getSelectedValue()).to.eventually.equal(null);

    // And The available options to be "Mechanical Design Group"
    checkAvailableOptions(functional2, ['Mechanical Design Group']);

    // When I select "Electrical Design Group" in "functional" field
    functional1.selectOption('Mechanical Design Group');

    // And I remove second widget
    widget2.getHeader().remove();

    // And I select "Infrastructure department" in department field
    department1.selectOption('Infrastructure department');

    // Then I expect the functional field in widget1 to be empty
    expect(functional1.getSelectedValue()).to.eventually.equal(null);

    // And The available options to be "Mechanical Design Group"
    checkAvailableOptions(functional1, ['Electrical Design Group']);
  });

});

function insertDTWWithFields(idocPage, fields, widgetOrder = 0) {
  idocPage.getTabEditor(1).insertWidget(DatatableWidget.WIDGET_NAME);
  let widgetConfig = new DatatableWidgetConfigDialog();
  let objectSelector = widgetConfig.selectObjectSelectTab();
  let search = objectSelector.getSearch();
  search.getCriteria().getSearchBar().search();
  search.getResults().clickResultItem(0);
  selectProperties(widgetConfig, fields);
  widgetConfig.save();
  return new DatatableWidget($$('.datatable-widget').get(widgetOrder));
}

function insertODWWithFields(idocPage, fields) {
  idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
  let widgetConfig = new ObjectDataWidgetConfig();
  widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
  selectProperties(widgetConfig, fields);
  widgetConfig.save();
  return new ObjectDataWidget($$('.object-data-widget').get(0));
}

function selectProperties(widgetConfig, propertyNames) {
  let propertiesSelector = widgetConfig.selectObjectDetailsTab();
  propertiesSelector.selectProperties(propertyNames);
}

function checkAvailableOptions(field, options) {
  field.toggleMenu();
  expect(field.getMenuElements()).to.eventually.have.length(options.length);
  options.forEach((option) => {
    expect(field.getMenuElements()).to.eventually.contain(option);
  });
  field.toggleMenu();
}