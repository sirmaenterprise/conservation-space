PluginRegistry.add('route', {
  'stateName': 'libraries',
  'url': '/libraries',
  'component': 'seip-libraries',
  'module': 'libraries/libraries'
});

PluginRegistry.add('open-library-action', [
  {
    name: 'openLibraryAction',
    module: 'libraries/open-library-action'
  }
]);
