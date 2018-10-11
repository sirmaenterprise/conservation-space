'use strict';

let DatatableWidgetSandboxPage = require('./datatable-widget').DatatableWidgetSandboxPage;
let DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

describe('Modeling mode in DatatableWidget', () => {

  let page = new DatatableWidgetSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  it('should not render xls export action', () => {
    page.open('mode=preview');
    page.insertWidget();

    let widgetConfig = new DatatableWidgetConfigDialog();
    let objectSelectorTab = widgetConfig.selectObjectSelectTab();
    objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelectorTab.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    widgetConfig.save();

    let datatableWidget = page.getWidget();

    expect(datatableWidget.getWidgetHeader().isActionAvailable('exportXlsxAction')).to.eventually.be.false;
  });

  describe('when in automatic object selection', () => {

    it('should render table columns for all selected properties and no rows', () => {
      // When I inserted DTW
      page.insertWidget();

      // When I configured selection mode to be Automatically
      let widgetConfig = new DatatableWidgetConfigDialog();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().waitForResults();

      // When I selected all properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectAllProperties();
      let labelsPromises = objectDetailsTab.getAllPropertiesLabels();

      // When I save config
      widgetConfig.save();

      // Then I expect DTW widget to be visible
      let datatableWidget = page.getWidget();

      // Then I expect DTW to have visible columns for all selected properties
      let tableHeader = datatableWidget.getHeader();
      let headerPromises = tableHeader.getHeaderLabels();
      Promise.all([labelsPromises, headerPromises]).then((results) => {
        let labels = ['Entity', ...results[0]];
        let headerLabels = results[1];
        labels.forEach((label, index) => {
          expect(label).to.equal(headerLabels[index]);
        });
      });
    });

    it('should render table with single Entity column and no rows', () => {
      // When I inserted DTW
      page.insertWidget();

      // When I configured selection mode to be Automatically
      let widgetConfig = new DatatableWidgetConfigDialog();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().waitForResults();

      // When I selected all properties
      widgetConfig.selectObjectDetailsTab();

      // When I save config
      widgetConfig.save();

      // Then I expect DTW widget to be visible
      let datatableWidget = page.getWidget();

      // Then I expect DTW to have visible column for the only Entity column
      let tableHeader = datatableWidget.getHeader();
      let columnsCount = tableHeader.getColumnsCount();
      expect(columnsCount).to.eventually.equal(1);
      tableHeader.getHeaderLabels().then((headers) => {
        expect(headers).to.eql([ 'Entity' ]);
      });
    });
  });

  describe('when in manual object selection', () => {

    it('should render table columns for all selected properties and one row', () => {
      // When I inserted DTW
      page.insertWidget();

      // When I configured selection mode to be Automatically
      let widgetConfig = new DatatableWidgetConfigDialog();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.MANUALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().clickResultItem(0);

      // When I selected all properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectAllProperties();
      let labelsPromises = objectDetailsTab.getAllPropertiesLabels();

      // When I save config
      widgetConfig.save();

      // Then I expect DTW widget to be visible
      let datatableWidget = page.getWidget();

      // Then I expect DTW to have visible columns for all selected properties
      let tableHeader = datatableWidget.getHeader();
      let headerPromises = tableHeader.getHeaderLabels();
      Promise.all([labelsPromises, headerPromises]).then((results) => {
        let labels = ['Entity', ...results[0]];
        let headerLabels = results[1];
        labels.forEach((label, index) => {
          expect(label).to.equal(headerLabels[index]);
        });
      });

      // Then I expect in DTW to have 1 result row
      expect(datatableWidget.getRowsCount()).to.eventually.equal(1);
    });
  });
});