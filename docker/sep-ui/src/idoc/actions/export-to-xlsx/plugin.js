PluginRegistry.add('widget-control-actions', [{
  name: 'exportXlsxAction',
  icon: 'fa fa-download',
  handler: 'ExportXlsxAction',
  handles: ['datatable-widget'],
  module: 'idoc/actions/export-to-xlsx/export-xlsx-action',
  tooltip: 'widget.action.export.xlsx.tooltip',
  filter: function(data, widgetConfig) {
    return widgetConfig.enableExportToMsExcel && !data.isModeling();
  }
}]);
