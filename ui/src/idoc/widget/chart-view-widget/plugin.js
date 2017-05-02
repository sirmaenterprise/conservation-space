PluginRegistry.add('idoc-widget', {
  'name': 'chart-view-widget',
  'class': 'idoc/widget/chart-view-widget/chart-view-widget/ChartViewWidget',
  'config': 'idoc/widget/chart-view-widget/chart-view-widget-config/ChartViewWidgetConfig',
  'label': 'chart.view.widget.name'
});

PluginRegistry.add('chart-view-charts', {
  'order': 10,
  'name': 'pie-chart',
  'label': 'chart.pie.chart',
  'component': 'seip-pie-chart',
  'module': 'components/charts/pie-chart/pie-chart'
});

PluginRegistry.add('chart-view-charts', {
  'order': 20,
  'name': 'bar-chart',
  'label': 'chart.bar.chart',
  'component': 'seip-bar-chart',
  'module': 'components/charts/bar-chart/bar-chart'
});

PluginRegistry.add('chart-view-charts', {
  'order': 30,
  'name': 'line-chart',
  'label': 'chart.line.chart',
  'component': 'seip-line-chart',
  'module': 'components/charts/line-chart/line-chart'
});
