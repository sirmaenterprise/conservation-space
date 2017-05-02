'use strict';

var TestUtils = require('../../../test-utils');

var DatatableWidget = require('./datatable-widget').DatatableWidget;
var ObjectDataWidget = require('./../object-data-widget/object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./../object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;
var DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var IdocPage = require('../../idoc-page');

const TAB_NUMBER = 1;
const NEW_FIELD_VALUE = 'new field value';
const GRID_OFF = 'grid-off';
const GRID_VERTICAL = 'grid-vertical';
const GRID_HORIZONTAL = 'grid-horizontal';
const SEARCH_RESULT = '6 Results';

describe('DatatableWidget', function () {

  var widgetElement;
  var widgetConfig = {};

  var idocPage = new IdocPage();

  it('should insert widget displaying only one column with instance headers if no properties are selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured current object with all its fields
    var widget = insertDatatableWidget(idocPage, true, false);
    // Then DTW is inserted in idoc
    // And There is one row in the table and one column
    var columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount).to.eventually.equal(1);
    var rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    // And There is only instance header inside the only column
    var row = widget.getRow(1);
    var cell = row.getCell(1);
    var instanceHeader = cell.getInstanceHeaderCell();
    var headerText = instanceHeader.getHeaderAsText();
    expect(headerText).to.eventually.equal('Header');
  });

  it('should insert widget displaying only one column with default instance headers if no properties are selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured current object with all its fields
    // And the user chooses to display default object headers
    var widget = insertDatatableWidget(idocPage, true, false, 'default_header');
    // Then DTW is inserted in idoc
    // And There is one row in the table and one column
    var columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount).to.eventually.equal(1);
    var rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    // And There is only instance header inside the only column
    var row = widget.getRow(1);
    var cell = row.getCell(1);
    var instanceHeader = cell.getInstanceHeaderCell();
    var headerText = instanceHeader.getHeaderAsText();
    expect(headerText).to.eventually.equal('(iDocument 1 for testing (Default Header))');
  });

  it('should insert widget with object selected and all properties displayed', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    var widget = insertDatatableWidget(idocPage, true, true);
    // Then DTW is inserted in idoc
    // And There is one row in the table with the object fields displayed in columns
    var rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    var columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount, 'Should have 6 columns in the table').to.eventually.equal(6);

    // And The first column contains the instance header for current object
    var row = widget.getRow(1);
    var headerText = row.getCell(1).getInstanceHeaderCell().getHeaderAsText();
    expect(headerText).to.eventually.equal('Header');

    for (var i = 1; i < 5; i++) {
      var cellPosition = i + 1;
      var cell = row.getCell(cellPosition);
      expect(cell.getInputField('#field' + i).getValue()).to.eventually.equal('value' + i);
    }
  });

  it('should insert widget with object selected and all properties displayed without object header column', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    // And I select option not to display objects headers
    var widget = insertDatatableWidget(idocPage, true, true, 'none');
    // Then DTW is inserted in idoc
    // And There is one row in the table with the object fields displayed in columns
    var rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    var columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount, 'Should have 5 columns in the table').to.eventually.equal(5);

    var row = widget.getRow(1);
    for (var i = 1; i < 4; i++) {
      var cell = row.getCell(i);
      expect(cell.getInputField('#field' + i).getValue()).to.eventually.equal('value' + i);
    }
  });

  it('should allow edit of idoc with DTW inside', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);

    // And I have inserted ODW configured for object and showing all its fields
    var odwElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(ObjectDataWidget.WIDGET_NAME);
    var objectDataWidgetConfig = new ObjectDataWidgetConfig();
    selectObjectInODWConfig(objectDataWidgetConfig);
    idocPage.getTabEditor(1).click();

    // And I have inserted DTW configured for object and showing all its fields
    var datatableWidget = insertDatatableWidget(idocPage, true, true);
    // When I edit the object field1 to be 'new field value' trough the DTW first row
    var row = datatableWidget.getRow(1);
    var cell = row.getCell(2);
    var field = cell.getInputField();
    field.setValue(null, NEW_FIELD_VALUE);

    // Then Selected object displayed in ODW has changed its field1 value to 'new field value'
    var objectDataWidget = new ObjectDataWidget(odwElement);
    var odwFieldValue = objectDataWidget.getForm().getInputText('field1').getValue();
    expect(odwFieldValue).to.eventually.equal(NEW_FIELD_VALUE);

    // When I save idoc
    idocPage.getActionsToolbar().saveIdoc();

    // Then DTW is saved and the current object title is 'new field value' in the DTW
    expect(datatableWidget.getRow(1).getCell(2).getInputField().getText()).to.eventually.equal(NEW_FIELD_VALUE);
    var field1 = element(by.css('span#field1'));
    // And The title is 'new field value' in the ODW for selected object
    expect(field1.getText()).to.eventually.equal(NEW_FIELD_VALUE);
  });

  it('should show tooltip when mandatory field is empty', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    var widget = insertDatatableWidget(idocPage, true, true);

    // And clear the value in mandatory field
    var row = widget.getRow(1);
    var cell = row.getCell(3);
    cell.getInputField().clearValue('#field2');

    // select next cell and go back to imitate "mouseover" and force tooltip to be show
    cell = row.getCell(2);
    cell.getInputField().getInputElement().click();
    cell = row.getCell(3);
    cell.getInputField().getInputElement().click();

    // Then red triangle should be shown in the right top corner of the cell
    expect(TestUtils.hasClass(cell.getCell(), 'has-error')).to.eventually.be.true;
    // Tooltip with error message should be shown
    expect(widget.getTooltip().isPresent()).to.eventually.be.true;
  });

  it('should not show tooltip when mandatory field filled with correct data', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    var widget = insertDatatableWidget(idocPage, true, true);

    // And move mouse over different cells
    var row = widget.getRow(1);
    var cell = row.getCell(2);
    cell.getInputField().getInputElement().click();
    cell = row.getCell(3);
    cell.getInputField().getInputElement().click();
    // Then red triangle should be shown in the right top corner of the cell
    expect(TestUtils.hasClass(cell.getCell(), 'has-error')).to.eventually.be.false;
    // Tooltip with error message should be shown
    expect(widget.getTooltip().isPresent()).to.eventually.be.false;
  });

  it('should create a widget with no grid', ()=> {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields and no grid
    var widget = insertDatatableWidget(idocPage, true, true, false, GRID_OFF);
    //get the div below form-wrapper
    //should contain grid-off class
    expect(TestUtils.hasClass(widget.widgetElement.$('.edit .label-hidden'), GRID_OFF)).to.eventually.be.true;
  });

  it('should create a widget with vertical grid', ()=> {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields and vertical grid.
    var widget = insertDatatableWidget(idocPage, true, true, false, GRID_VERTICAL);
    //get the div below form-wrapper
    //should contain the grid-vertical class
    expect(TestUtils.hasClass(widget.widgetElement.$('.edit .label-hidden'), GRID_VERTICAL)).to.eventually.be.true;
  });

  it('should create a widget with horizontal grid', ()=> {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields and horizontal grid
    var widget = insertDatatableWidget(idocPage, true, true, false, GRID_HORIZONTAL);
    //get the div below form-wrapper
    //should contain the grid-horizontal class
    expect(TestUtils.hasClass(widget.widgetElement.$('.edit .label-hidden'), GRID_HORIZONTAL)).to.eventually.be.true;
  });

  it('should show only first page of results', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    var objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    // And select show first page only configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().toggleShowFirstPageOnly();
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is hidden
    var widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getFooter().getResultsCount().getText()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().getPaginationElement().isDisplayed()).to.eventually.be.false;
  });

  it('should show pagination with 2 pages', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    var objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    var widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getFooter().getResultsCount().getText()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().getPaginationElement().isDisplayed()).to.eventually.be.true;

    // When I select second page
    widget.getFooter().pagination.goToPage(2);
    // Then 1 result is listed
    browser.wait(isRowCountChanged(widget, 1), DEFAULT_TIMEOUT);
  });

  it('should load all results', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    var objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    var widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getFooter().getResultsCount().getText()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().getPaginationElement().isDisplayed()).to.eventually.be.true;

    // When I select all results to be display on page
    widget.openConfig();
    widgetConfig = new DatatableWidgetConfigDialog();
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 1, true);
    widgetConfig.save();
    // Then all results are listed
    browser.wait(isRowCountChanged(widget, 6), DEFAULT_TIMEOUT);
  });

  it('should show correct pagination when objects are selected manually', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    var objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    for (var i = 0; i < 6; i++) {
      basicSearch.results.clickResultItem(i);
    }
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    var widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getFooter().getResultsCount().getText()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().getPaginationElement().isDisplayed()).to.eventually.be.true;

    // When I select second page
    widget.getFooter().pagination.goToPage(2);
    // Then 1 result is listed
    browser.wait(isRowCountChanged(widget, 1), DEFAULT_TIMEOUT);
  });

  it('should display table header row by default', () => {
    idocPage.open(true);

    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    basicSearch.results.clickResultItem(0);

    let optionsTab = widgetConfig.selectDisplayOptionTab();
    expect(optionsTab.isDisplayTableHeaderRowSelected()).to.eventually.be.true;
    widgetConfig.save();

    let widget = new DatatableWidget(widgetElement);
    expect(widget.getHeader().isDisplayed()).to.eventually.be.true;
  });

  it('should hide table header row if configured', () => {
    idocPage.open(true);

    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    basicSearch.results.clickResultItem(0);

    let optionsTab = widgetConfig.selectDisplayOptionTab();
    optionsTab.toggleDisplayTableHeaderRow();
    widgetConfig.save();

    let widget = new DatatableWidget(widgetElement);
    expect(widget.getHeader().isDisplayed()).to.eventually.be.false;
  });

  it('should show custom dialog', () => {
    idocPage.open(true);
    let widget = insertDatatableWidget(idocPage, true, false, 'default_header');
    let dialog = widget.openWidget();
    expect(dialog.isSelectObjectsVisable()).to.eventually.be.false;
    expect(dialog.isObjectDetailstabVisable()).to.eventually.be.false;
    expect(dialog.isDisplayOptionsVisable()).to.eventually.be.false;
    expect(dialog.isSearchResultSelectable()).to.eventually.be.false;
  });

  it('should not display broken error message when there is no selection',()=>{
    idocPage.open(true);
    var widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    var objectSelector = widgetConfig.selectObjectSelectTab();
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    widgetConfig.save();
    var widget = new DatatableWidget(widgetElement);

    expect(widget.getErrorMessage()).to.eventually.not.equal('[object Object]');
    expect(widget.getErrorMessage()).to.eventually.not.equal('select.object.none');
  });

  /**
   * Creates a DTW using the menu. Initializes an object and selects all its fields if needed.
   *
   * @param idocPage
   * @param withObject
   * @param withFields
   * @param headerToBeDisplayed
   * @param gridConfig
   * @returns The widget page object.
   */
  function insertDatatableWidget(idocPage, withObject, withFields, headerToBeDisplayed, gridConfig) {
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    if (withObject) {
      // And I execute a search
      var objectSelector = widgetConfig.selectObjectSelectTab();
      var basicSearch = objectSelector.getSearch();
      basicSearch.clickSearch();
      basicSearch.results.waitUntilOpened();
      // And I select the first object to be visualized in the widget
      basicSearch.results.clickResultItem(0);
      if (withFields) {
        widgetConfig.selectObjectDetailsTab().selectProperties(['field1', 'field2', 'field3', 'field4', 'field5']);
      }
    }

    if (headerToBeDisplayed) {
      let optionsTab = widgetConfig.selectDisplayOptionTab();
      if (!optionsTab.isHeaderOptionSelected(headerToBeDisplayed)) {
        optionsTab.selectHeaderToBeDisplayed(headerToBeDisplayed);
      }
    }

    if (gridConfig) {
      let optionsTab = widgetConfig.selectDisplayOptionTab();
      if (!optionsTab.isGridOptionSelected(gridConfig)) {
        optionsTab.selectGridOption(gridConfig);
      }
    }

    // And I select ok from the DTW configuration dialog
    widgetConfig.save();
    return new DatatableWidget(widgetElement);
  }

  function selectObjectInODWConfig(widgetConfig) {
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.MANUALLY);
    var objectSelector = widgetConfig.selectObjectSelectTab();
    var basicSearch = objectSelector.getSearch();
    basicSearch.clickSearch();
    basicSearch.results.waitUntilOpened();
    // And I select the first object to be visualized in the widget
    basicSearch.results.clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();
  }

  function isRowCountChanged(widget, expectedRows) {
    return widget.getRowsCount().then(function (rows) {
      return rows === expectedRows;
    });
  }

});
