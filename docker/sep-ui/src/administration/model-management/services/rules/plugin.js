PluginRegistry.add('model-management-rule-command', [
  {
    'name': 'NotMatchCommand',
    'component': 'NotMatchCommand',
    'module': 'administration/model-management/services/rules/not-match-command'
  },
  {
    'name': 'InCommand',
    'component': 'InCommand',
    'module': 'administration/model-management/services/rules/in-command'
  },
  {
    'name': 'NotInCommand',
    'component': 'NotInCommand',
    'module': 'administration/model-management/services/rules/not-in-command'
  }
]);