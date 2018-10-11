PluginRegistry.add('route', {
  stateName: 'recent-objects',
  icon: 'fa-history',
  label: 'menu.recent.objects',
  url: '/recent-objects',
  component: 'seip-recent-objects',
  module: 'recent-objects/recent-objects'
});

PluginRegistry.add('eventbus.global', {
  name: 'recent-objects-listener',
  component: 'recentObjectsListener',
  module: 'recent-objects/recent-objects-listener'
});