"use strict";

var TestUtils = require('../../../test-utils');
var AggregatedTable = require('./widget').AggregatedTable;
var AggregatedTableConfigDialog = require('./widget').AggregatedTableConfigDialog;
var AggregatedTableSandboxPage = require('./widget').AggregatedTableSandboxPage;
var IdocPage = require('../../idoc-page').IdocPage;

const TAB_NUMBER = 1;
const GRID_ON = 'grid-on';
const GRID_OFF = 'grid-off';
const GRID_VERTICAL = 'grid-vertical';
const GRID_HORIZONTAL = 'grid-horizontal';

describe('AggregatedTable', () => {

  var widgetElement;
  var widgetConfig = {};
  var idocPage = new IdocPage();

  it('should create a table with grid by default', () => {
    idocPage.open(true);
    var widget = insertAggregatedtableWidget(idocPage);
    var formContent = widget.widgetElement.$$('.edit .label-hidden').first();
    browser.wait(EC.presenceOf(formContent), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(formContent, GRID_ON)).to.eventually.be.true;
  });

  it('should create a table with no grid', () => {
    idocPage.open(true);
    var widget = insertAggregatedtableWidget(idocPage, GRID_OFF);
    var formContent = widget.widgetElement.$$('.edit .label-hidden').first();
    browser.wait(EC.presenceOf(formContent), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(formContent, GRID_OFF)).to.eventually.be.true;
  });

  it('should create a widget with vertical grid', () => {
    idocPage.open(true);
    var widget = insertAggregatedtableWidget(idocPage, GRID_VERTICAL);
    var formContent = widget.widgetElement.$$('.edit .label-hidden').first();
    browser.wait(EC.presenceOf(formContent), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(formContent, GRID_VERTICAL)).to.eventually.be.true;
  });

  it('should create a widget with horizontal grid', () => {
    idocPage.open(true);
    var widget = insertAggregatedtableWidget(idocPage, GRID_HORIZONTAL);
    var formContent = widget.widgetElement.$$('.edit .label-hidden').first();
    browser.wait(EC.presenceOf(formContent), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(formContent, GRID_HORIZONTAL)).to.eventually.be.true;
  });

  it('should be empty if display is set to automatically and in modelling mode', () => {
    var aggregatedTablePage = new AggregatedTableSandboxPage();
    aggregatedTablePage.open();
    aggregatedTablePage.toggleModellingMode();
    aggregatedTablePage.insertWidget();
    var widgetConfig = new AggregatedTableConfigDialog();
    widgetConfig.waitUntilOpened();
    widgetConfig.selectAutomaticallySelect();

    var objectSelector = widgetConfig.selectObjectSelectTab();
    var search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();

    var reportConfig = widgetConfig.selectReportConfiguration();
    reportConfig.selectOption('Type');

    widgetConfig.save(true);
    var actualWidget = new AggregatedTable($('.' + AggregatedTable.WIDGET_NAME));
    var rowCount = actualWidget.getRowCount();
    expect(rowCount).to.eventually.equal(0);
  });

  function insertAggregatedtableWidget(idocPage, gridConfig) {
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(AggregatedTable.WIDGET_NAME);
    widgetConfig = new AggregatedTableConfigDialog();

    // search and select object
    var objectSelector = widgetConfig.selectObjectSelectTab();
    var search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(0);

    let configTab = widgetConfig.selectReportConfiguration();
    configTab.selectOption('Type');

    if (gridConfig) {
      let optionsTab = widgetConfig.selectDisplayOptionTab();
      if (!optionsTab.isGridOptionSelected(gridConfig)) {
        optionsTab.selectGridOption(gridConfig);
      }
    }

    widgetConfig.save();
    return new AggregatedTable(widgetElement);
  }
});
