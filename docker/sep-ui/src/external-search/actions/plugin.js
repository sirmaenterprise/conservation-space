PluginRegistry.add('actions',[
  {
    name: 'importAction',
    eventId: 'ImportAction',
    type: 'menuitem',
    label: 'external.import',
    handler: 'ImportAction',
    module: 'external-search/actions/import-action'
  },
  {
    name: 'updateIntAction',
    eventId: 'updateInt',
    type: 'menuitem',
    label: 'external.update',
    handler: 'UpdateIntAction',
    module: 'external-search/actions/update-action'
  }
]);