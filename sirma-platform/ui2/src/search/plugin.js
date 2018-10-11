PluginRegistry.add('route', {
  'stateName': 'search',
  'url': '/search?mode&args&tree',
  'icon': 'fa-search',
  'label': 'search.advanced.search',
  'component': 'seip-main-search',
  'module': 'search/components/main-search'
});

PluginRegistry.add('route', {
  'stateName': 'open-search',
  'url': '/search/{id}',
  'component': 'seip-main-search',
  'module': 'search/components/main-search'
});

PluginRegistry.add('main-menu', {
  'order': 20,
  'name': 'seip-quick-search',
  'component': 'seip-quick-search',
  'module': 'search/components/quick-search'
});
