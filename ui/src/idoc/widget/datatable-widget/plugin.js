PluginRegistry.add('idoc-widget', {
  'name': 'datatable-widget',
  'class': 'idoc/widget/datatable-widget/datatable-widget/DatatableWidget',
  'config': 'idoc/widget/datatable-widget/datatable-widget-config/DatatableWidgetConfig',
  'label': 'datatable.widget.name'
});

PluginRegistry.add('datatable-toolbar', {
  'disabled': true,
  'order': 10,
  'name': 'seip-datatable-filter',
  'component': 'seip-datatable-filter',
  'module': 'idoc/widget/datatable-widget/datatable-filter'
});