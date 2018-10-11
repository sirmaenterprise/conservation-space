'use strict';

let TestUtils = require('../../../test-utils');
let DatatableWidget = require('./datatable-widget').DatatableWidget;
let ObjectDataWidget = require('./../object-data-widget/object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./../object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;
let DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
let IdocPage = require('../../idoc-page').IdocPage;

const TAB_NUMBER = 1;
const NEW_FIELD_VALUE = 'new field value';
const GRID_OFF = 'grid-off';
const GRID_VERTICAL = 'grid-vertical';
const GRID_HORIZONTAL = 'grid-horizontal';
const SEARCH_RESULT = '6 results';

describe('DatatableWidget', function () {

  let widgetElement;
  let widgetConfig = {};

  let idocPage = new IdocPage();

  it('should insert widget displaying only one column with instance headers if no properties are selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured current object with all its fields
    let widget = insertDatatableWidget(idocPage, true, false);
    // Then DTW is inserted in idoc
    // And There is one row in the table and one column
    let columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount).to.eventually.equal(1);
    let rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    // And There is only instance header inside the only column
    let row = widget.getRow(1);
    let cell = row.getCell(1);
    let instanceHeader = cell.getInstanceHeaderCell();
    let headerText = instanceHeader.getHeaderAsText();
    expect(headerText).to.eventually.equal('Header-1');
  });

  it('should insert widget displaying only one column with default instance headers if no properties are selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured current object with all its fields
    // And the user chooses to display default object headers
    let widget = insertDatatableWidget(idocPage, true, false, 'default_header');
    // Then DTW is inserted in idoc
    // And There is one row in the table and one column
    let columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount).to.eventually.equal(1);
    let rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    // And There is only instance header inside the only column
    let row = widget.getRow(1);
    let cell = row.getCell(1);
    let instanceHeader = cell.getInstanceHeaderCell();
    let headerText = instanceHeader.getHeaderAsText();
    expect(headerText).to.eventually.equal('(iDocument 1 for testing (Default Header))');
  });

  it('should insert widget with object selected and all properties displayed', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    let widget = insertDatatableWidget(idocPage, true, true);
    // Then DTW is inserted in idoc
    // And There is one row in the table with the object fields displayed in columns
    let rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    let columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount, 'Should have 6 columns in the table').to.eventually.equal(6);

    // And The first column contains the instance header for current object
    let row = widget.getRow(1);
    let headerText = row.getCell(1).getInstanceHeaderCell().getHeaderAsText();
    expect(headerText).to.eventually.equal('Header-1');

    for (let i = 1; i < 5; i++) {
      let cellPosition = i + 1;
      let cell = row.getCell(cellPosition);
      expect(cell.getInputField('#field' + i).getValue()).to.eventually.equal('value' + i);
    }
  });

  it('should insert widget with object selected and all properties displayed without object header column', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    // And I select option not to display objects headers
    let widget = insertDatatableWidget(idocPage, true, true, 'none');
    // Then DTW is inserted in idoc
    // And There is one row in the table with the object fields displayed in columns
    let rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);
    let columnsCount = widget.getHeader().getColumnsCount();
    expect(columnsCount, 'Should have 5 columns in the table').to.eventually.equal(5);

    let row = widget.getRow(1);
    for (let i = 1; i < 4; i++) {
      let cell = row.getCell(i);
      expect(cell.getInputField('#field' + i).getValue()).to.eventually.equal('value' + i);
    }
  });

  it('should allow edit of idoc with DTW inside', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);

    // And I have inserted ODW configured for object and showing all its fields
    let odwElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(ObjectDataWidget.WIDGET_NAME);
    let objectDataWidgetConfig = new ObjectDataWidgetConfig();
    selectObjectInODWConfig(objectDataWidgetConfig);

    // And I have inserted DTW configured for object and showing all its fields
    let datatableWidget = insertDatatableWidget(idocPage, true, true);
    // When I edit the object field1 to be 'new field value' trough the DTW first row
    let row = datatableWidget.getRow(1);
    let cell = row.getCell(2);
    let field = cell.getInputField();
    field.setValue(null, NEW_FIELD_VALUE);

    // Then Selected object displayed in ODW has changed its field1 value to 'new field value'
    let objectDataWidget = new ObjectDataWidget(odwElement);
    let odwFieldValue = objectDataWidget.getForm().getInputText('field1').getValue();
    expect(odwFieldValue).to.eventually.equal(NEW_FIELD_VALUE);

    // When I save idoc
    idocPage.getActionsToolbar().saveIdoc();

    // Then DTW is saved and the current object title is 'new field value' in the DTW
    expect(datatableWidget.getRow(1).getCell(2).getInputField().getText()).to.eventually.equal(NEW_FIELD_VALUE);
    let field1 = element(by.css('span#field1'));
    // And The title is 'new field value' in the ODW for selected object
    expect(field1.getText()).to.eventually.equal(NEW_FIELD_VALUE);
  });

  it('should show tooltip when mandatory field is empty', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields
    let widget = insertDatatableWidget(idocPage, true, true);

    // And clear the value in mandatory field
    let row = widget.getRow(1);
    let cell = row.getCell(3);
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
    let widget = insertDatatableWidget(idocPage, true, true);

    // And move mouse over different cells
    let row = widget.getRow(1);
    let cell = row.getCell(2);
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
    let widget = insertDatatableWidget(idocPage, true, true, false, GRID_OFF);
    //get the div below form-wrapper
    //should contain grid-off class
    expect(TestUtils.hasClass(widget.widgetElement.$('.edit .label-hidden'), GRID_OFF)).to.eventually.be.true;
  });

  it('should create a widget with vertical grid', ()=> {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields and vertical grid.
    let widget = insertDatatableWidget(idocPage, true, true, false, GRID_VERTICAL);
    //get the div below form-wrapper
    //should contain the grid-vertical class
    expect(TestUtils.hasClass(widget.widgetElement.$('.edit .label-hidden'), GRID_VERTICAL)).to.eventually.be.true;
  });

  it('should create a widget with horizontal grid', ()=> {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I insert DTW with configured object with all its fields and horizontal grid
    let widget = insertDatatableWidget(idocPage, true, true, false, GRID_HORIZONTAL);
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
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    // And select show first page only configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().toggleShowFirstPageOnly();
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is hidden
    let widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getWidgetHeader().getHeaderResults().getTotalResults()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().isPaginationVisible()).to.eventually.be.false;
  });

  it('should show pagination with 2 pages', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    let widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getWidgetHeader().getHeaderResults().getTotalResults()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().isPaginationVisible()).to.eventually.be.true;

    // When I select second page
    widget.getFooter().getPagination().goToPage(2);
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
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    let widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getWidgetHeader().getHeaderResults().getTotalResults()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().isPaginationVisible()).to.eventually.be.true;

    // When I select all results to be display on page
    widget.openConfig();
    widgetConfig = new DatatableWidgetConfigDialog();
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 1, true);
    widgetConfig.save();
    // Then all results are listed
    browser.wait(isRowCountChanged(widget, 6), DEFAULT_TIMEOUT);
  });

  it('should show create item button if widget search is in automatically mode and an object type or subtype is selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    objectSelector.getSearch().getCriteria().getSearchBar().getMultiTypeSelect().selectFromMenuByValue('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    // And I select to display Show/Create button
    let displayOptionsTab = widgetConfig.selectDisplayOptionTab();
    displayOptionsTab.toggleDisplayCreateAction();

    widgetConfig.save();
    //Then the widget should show Create/Upload header action
    let widget = new DatatableWidget(widgetElement);
    expect(widget.getWidgetHeader().getHeaderCreateItem().getButton().isPresent()).to.eventually.be.true;
  });

  it('should not show create item button if widget search is in automatically mode and no object type or subtype is selected', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();

    let displayOptionsTab = widgetConfig.selectDisplayOptionTab();
    displayOptionsTab.toggleDisplayCreateAction();

    widgetConfig.save();
    //Then the widget should not show Create/Upload header action
    let widget = new DatatableWidget(widgetElement);
    expect(widget.getWidgetHeader().getHeaderCreateItem().getButton().isPresent()).to.eventually.be.false;
  });

  it('should not show create item button if widget search is in manually mode', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    // And I select to display Show/Create button
    let displayOptionsTab = widgetConfig.selectDisplayOptionTab();
    displayOptionsTab.toggleDisplayCreateAction();

    widgetConfig.save();
    //Then the widget should not show Create/Upload header action
    let widget = new DatatableWidget(widgetElement);
    expect(widget.getWidgetHeader().getHeaderCreateItem().getButton().isPresent()).to.eventually.be.false;
  });

  it('should show correct pagination when objects are selected manually', () => {
    // Given I am on some object landing page in edit mode
    idocPage.open(true);
    // When I select Data table widget from the widget menu
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    // And I execute a search
    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    for (let i = 0; i < 6; i++) {
      search.getResults().clickResultItem(i);
    }
    // And select configuration with 5 results per page
    widgetConfig.selectDisplayOptionTab().getPageSize().selectFromMenu('.page-size', 2, true);
    widgetConfig.save();

    // Then 6 results are found, 5 results are listed and pagination component is жисибле
    let widget = new DatatableWidget(widgetElement);
    browser.wait(isRowCountChanged(widget, 5), DEFAULT_TIMEOUT);
    expect(widget.getWidgetHeader().getHeaderResults().getTotalResults()).to.eventually.equal(SEARCH_RESULT);
    expect(widget.getFooter().isPaginationVisible()).to.eventually.be.true;

    // When I select second page
    widget.getFooter().getPagination().goToPage(2);
    // Then 1 result is listed
    browser.wait(isRowCountChanged(widget, 1), DEFAULT_TIMEOUT);
  });

  it('should display table header row by default', () => {
    idocPage.open(true);

    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);

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
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);

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
    let widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    widgetConfig.save();
    let widget = new DatatableWidget(widgetElement);

    expect(widget.getErrorMessage()).to.eventually.not.equal('[object Object]');
    expect(widget.getErrorMessage()).to.eventually.not.equal('select.object.none');
  });

  it('should display normal (not striped) rows by default', () => {
    idocPage.open(true);

    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();

    let optionsTab = widgetConfig.selectDisplayOptionTab();
    expect(optionsTab.isStripeRowsSelected()).to.eventually.be.false;
    widgetConfig.save();

    let widget = new DatatableWidget(widgetElement);
    expect(widget.isStriped()).to.eventually.be.false;
    expect(widget.getRow(2).element.getCssValue('background-color')).to.eventually.equals('rgba(0, 0, 0, 0)');
  });

  it('should display striped rows if configured', () => {
    idocPage.open(true);

    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    let objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();

    let optionsTab = widgetConfig.selectDisplayOptionTab();
    optionsTab.toggleStripeRows();
    widgetConfig.save();

    let widget = new DatatableWidget(widgetElement);
    expect(widget.isStriped()).to.eventually.be.true;

    // check that background of second row is different than the background of the first row
    widget.getRow(1).element.getCssValue('background-color').then((firstRowColor) => {
      expect(widget.getRow(2).element.getCssValue('background-color')).to.eventually.not.equals(firstRowColor);
    });
  });

  it('should toggle sort icon', () => {
    idocPage.open(true);

    let widget = insertDatatableWidget(idocPage, true, false);
    let headerCell = widget.getHeader().getTableHeaderCell(0);

    expect(headerCell.hasSortIcon()).to.eventually.be.true;
    expect(headerCell.isSortDefault()).to.eventually.be.true;
    headerCell.toggleSort();
    expect(headerCell.isSortAscending()).to.eventually.be.true;
    headerCell.toggleSort();
    expect(headerCell.isSortDescending()).to.eventually.be.true;
    headerCell.toggleSort();
    expect(headerCell.isSortDefault()).to.eventually.be.true;
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
      let objectSelector = widgetConfig.selectObjectSelectTab();
      let search = objectSelector.getSearch();
      search.getCriteria().getSearchBar().search();
      // And I select the first object to be visualized in the widget
      search.getResults().clickResultItem(0);
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
    let objectSelector = widgetConfig.selectObjectSelectTab();
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    // And I select the first object to be visualized in the widget
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectAllProperties();
    widgetConfig.save();
  }

  function isRowCountChanged(widget, expectedRows) {
    return widget.getRowsCount().then(function (rows) {
      return rows === expectedRows;
    });
  }
});