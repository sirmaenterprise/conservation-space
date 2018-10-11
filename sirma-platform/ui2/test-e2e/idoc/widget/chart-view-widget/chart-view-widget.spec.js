'use strict';

var IdocPage = require('../../idoc-page').IdocPage;
var ChartViewWidget = require('./chart-view-widget').ChartViewWidget;
var ChartViewWidgetSandboxPage = require('./chart-view-widget').ChartViewWidgetSandboxPage;
var ChartViewWidgetConfigDialog = require('./chart-view-widget').ChartViewWidgetConfigDialog;
var PieChart = require('../../../components/charts/pie-chart').PieChart;
var BarChart = require('../../../components/charts/bar-chart').BarChart;
var LineChart = require('../../../components/charts/line-chart').LineChart;
var ObjectSelector = require('../object-selector/object-selector.js').ObjectSelector;

describe('ChartViewWidget', () => {
  it('should display pie chart', () => {
    var idocPage = new IdocPage();
    idocPage.open(true);
    var chartViewWidget = insertWidget(idocPage, 'Type', 'Pie chart');
    var pieChart = new PieChart(chartViewWidget.getChartParent());
    expect(pieChart.getNumberOfArcs()).to.eventually.equals(3);
    expect(pieChart.getTotalResults()).to.eventually.equals('Total results: 3');
    expect(pieChart.getTitle()).to.eventually.equals('Group by: Type');
  });

  it('should display bar chart', () => {
    var idocPage = new IdocPage();
    idocPage.open(true);
    var chartViewWidget = insertWidget(idocPage, 'Type', 'Bar chart');
    var barChart = new BarChart(chartViewWidget.getChartParent());
    expect(barChart.getNumberOfBars()).to.eventually.equals(3);
    expect(barChart.getTotalResults()).to.eventually.equals('Total results: 3');
    expect(barChart.getTitle()).to.eventually.equals('Group by: Type');
  });

  it('should display line chart', () => {
    var idocPage = new IdocPage();
    idocPage.open(true);
    var chartViewWidget = insertWidget(idocPage, 'Type', 'Line chart');
    var chart = new LineChart(chartViewWidget.getChartParent());
    expect(chart.isLineVisible()).to.eventually.be.true;
    expect(chart.getTotalResults()).to.eventually.equals('Total results: 3');
    expect(chart.getTitle()).to.eventually.equals('Group by: Type');
  });

  it('should clear existing chart if no group by is selected', () => {
    let sandboxPage = new ChartViewWidgetSandboxPage();
    sandboxPage.open();
    sandboxPage.toggleWidget();

    let widgetConfig = new ChartViewWidgetConfigDialog();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    var search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(0);
    search.getResults().clickResultItem(1);

    let chartConfigTab = widgetConfig.selectChartConfiguration();
    chartConfigTab.selectGroupBy('Type');
    widgetConfig.save();

    let chartViewWidget = new ChartViewWidget($('.chart-view-widget'));
    chartViewWidget.waitToAppear();
    browser.wait(EC.presenceOf(sandboxPage.getChartWrapper()), DEFAULT_TIMEOUT);
    expect(sandboxPage.getErrorMessageWrapper().isPresent()).to.eventually.be.false;

    chartViewWidget.getHeader().openConfig();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    widgetConfig.save();
    browser.wait(EC.stalenessOf(sandboxPage.getChartWrapper()), DEFAULT_TIMEOUT);
    expect(sandboxPage.getErrorMessageWrapper().isPresent()).to.eventually.be.true;
  });

  it('should display empty widget', () => {
    var sandboxPage = new ChartViewWidgetSandboxPage();
    sandboxPage.open();
    sandboxPage.toggleModellingMode();
    sandboxPage.toggleWidget();

    var widgetConfig = new ChartViewWidgetConfigDialog();
    var objectSelector = widgetConfig.selectObjectSelectTab();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    var search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();

    let chartConfigTab = widgetConfig.selectChartConfiguration();
    chartConfigTab.selectGroupBy('Type');
    widgetConfig.save();

    expect(sandboxPage.getChartWrapper().isPresent()).to.eventually.be.false;
    expect(sandboxPage.getErrorMessageWrapper().isPresent()).to.eventually.be.false;
  });

  function insertWidget(idocPage, groupBy, chartType) {
    var widgetElement = idocPage.getTabEditor(1).insertWidget(ChartViewWidget.WIDGET_NAME);
    var widgetConfig = new ChartViewWidgetConfigDialog();

    // search and select object
    var objectSelector = widgetConfig.selectObjectSelectTab();
    var search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(0);

    let chartConfigTab = widgetConfig.selectChartConfiguration();
    chartConfigTab.selectGroupBy(groupBy);
    chartConfigTab.selectChartType(chartType);

    widgetConfig.save();
    return new ChartViewWidget(widgetElement);
  }
});
