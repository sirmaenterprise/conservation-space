let DatatableWidgetSandboxPage = require('./datatable-widget').DatatableWidgetSandboxPage;
let DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

describe('Icons in DatatableWidget', function () {

  let page = new DatatableWidgetSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  it('should show icons by default', () => {
    // When I inserted DTW
    page.insertWidget();
    // And I configured selection mode to be Automatically
    let widgetConfig = new DatatableWidgetConfigDialog();
    let objectSelectorTab = widgetConfig.selectObjectSelectTab();
    objectSelectorTab.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let search = objectSelectorTab.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    // And I selected all properties
    let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
    objectDetailsTab.selectAllProperties();
    // And I save config
    widgetConfig.save();
    // Then I expect DTW widget to be visible
    let datatableWidget = page.getWidget();
    // And I expect DTW to show icons
    let row = datatableWidget.getRow(1);
    let cell = row.getCell(1);
    let instanceHeaderCell = cell.getInstanceHeaderCell();
    browser.wait(EC.visibilityOf(instanceHeaderCell.getIcon()), DEFAULT_TIMEOUT, 'Header icon must be shown');
  });

  it('should not show icons if do not show icons in widget option is selected', () => {
    // When I inserted DTW
    page.insertWidget();
    // And I configured selection mode to be Automatically
    let widgetConfig = new DatatableWidgetConfigDialog();
    let objectSelectorTab = widgetConfig.selectObjectSelectTab();
    objectSelectorTab.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    let search = objectSelectorTab.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    // And I selected widget display options
    let displayOptionsTab = widgetConfig.selectDisplayOptionTab();
    displayOptionsTab.toggleDisplayIcons();
    // And I save config
    widgetConfig.save();
    // Then I expect DTW widget to be visible
    let datatableWidget = page.getWidget();
    // And I expect DTW to not show icons
    let row = datatableWidget.getRow(1);
    let cell = row.getCell(1);
    let instanceHeaderCell = cell.getInstanceHeaderCell();
    browser.wait(EC.invisibilityOf(instanceHeaderCell.getIcon()), DEFAULT_TIMEOUT, 'Header icon must be hidden');
  });

});