PluginRegistry.add('route', {
  'stateName': 'search',
  'url': '/search?metaText',
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

PluginRegistry.add('search-criteria', {
  'name': 'basic',
  'component': 'seip-basic-search-criteria',
  'module': 'search/components/common/basic-search-criteria'
});

PluginRegistry.add('search-criteria', {
  'name': 'advanced',
  'component': 'seip-advanced-search',
  'module': 'search/components/advanced/advanced-search'
});

PluginRegistry.add('search-criteria', {
  'name': 'mixed',
  'component': 'seip-mixed-search-criteria',
  'module': 'search/components/common/mixed-search-criteria'
});

PluginRegistry.add('search-criteria', {
  'name': 'external',
  'component': 'seip-external-search',
  'module': 'external-search/external-search'
});