PluginRegistry.add('widget-header-extensions', {
  'order': 10,
  'name': 'seip-datatable-header-results',
  'component': 'seip-datatable-header-results',
  'module': 'idoc/widget/datatable-widget/header-extensions/datatable-header-results',
  'widgetName': 'datatable-widget'
});

PluginRegistry.add('widget-header-extensions', {
  'order': 20,
  'name': 'seip-datatable-header-filter',
  'component': 'seip-datatable-header-filter',
  'module': 'idoc/widget/datatable-widget/header-extensions/datatable-header-filter',
  'widgetName': 'datatable-widget',
  'widgetModes': ['preview']
});

PluginRegistry.add('widget-header-extensions', {
  'order': 30,
  'name': 'seip-datatable-header-create-item',
  'component': 'seip-datatable-header-create-item',
  'module': 'idoc/widget/datatable-widget/header-extensions/datatable-header-create-item',
  'widgetName': 'datatable-widget',
  'widgetModes': ['preview', 'edit', 'edit-locked']
});
