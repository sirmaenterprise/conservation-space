PluginRegistry.add('route', {
  'stateName': 'public',
  'url': '/public/{componentId}',
  'component': 'seip-public-component-wrapper',
  'module': 'layout/public/public-component-wrapper'
});

PluginRegistry.add('public-components', {
  'name': 'accountConfirmation',
  'component': 'seip-account-confirmation',
  'module': 'user/account-confirmation/account-confirmation'
});

PluginRegistry.add('public-components', {
  'name': 'disabledUser',
  'component': 'seip-disabled-user',
  'module': 'user/disabled-user/disabled-user'
});