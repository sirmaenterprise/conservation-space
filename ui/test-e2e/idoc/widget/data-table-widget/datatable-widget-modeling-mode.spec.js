'use strict';

var DatatableWidgetSandboxPage = require('./datatable-widget').DatatableWidgetSandboxPage;
var DatatableWidget = require('./datatable-widget').DatatableWidget;
var DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
var DatatableHeader = require('./datatable-widget').DatatableHeader;
var TableHeaderCell = require('./datatable-widget').TableHeaderCell;
var PropertiesSelector = require('../properties-selector/properties-selector.js').PropertiesSelector;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

describe('DatatableWidget in modeling mode', () => {

  var page = new DatatableWidgetSandboxPage();
  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  describe('when in automatic object selection', () => {

    it('should render table columns for all selected properties and no rows', () => {
      // When I inserted DTW
      page.insertWidget();
      // When I configured selection mode to be Automatically
      let widgetConfig = new DatatableWidgetConfigDialog();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      var basicSearch = objectSelectorTab.getSearch();
      basicSearch.clickSearch();
      basicSearch.results.waitUntilOpened();
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
      var basicSearch = objectSelectorTab.getSearch();
      basicSearch.clickSearch();
      basicSearch.results.waitUntilOpened();
      // When I selected all properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
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
  });

  describe('when in manual object selection', () => {

    it('should render table columns for all selected properties and one row', () => {
      // When I inserted DTW
      page.insertWidget();
      // When I configured selection mode to be Automatically
      let widgetConfig = new DatatableWidgetConfigDialog();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.MANUALLY);
      var basicSearch = objectSelectorTab.getSearch();
      basicSearch.clickSearch();
      basicSearch.results.waitUntilOpened();
      basicSearch.results.clickResultItem(0);
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