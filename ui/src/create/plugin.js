PluginRegistry.add('route', {
  'stateName': 'create',
  'url': '/create',
  'component': 'create-url-handler',
  'module': 'create/create-url-handler'
});

PluginRegistry.add('create-instance', {
  'order': 10,
  'name': 'instance-create-panel',
  'label': 'instance.create.dialog.tab.create',
  'component': 'instance-create-panel',
  'module': 'create/instance-create-panel'
});

PluginRegistry.add('create-instance', {
  'order': 20,
  'name': 'file-upload-panel',
  'label': 'instance.create.dialog.tab.upload',
  'component': 'file-upload-panel',
  'module': 'create/file-upload-panel'
});