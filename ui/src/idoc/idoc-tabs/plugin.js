PluginRegistry.add('idoc-tabs-menu-items',
  [{
    id: 'configureIdocTabs',
    eventId: 'ConfigureIdocTabs',
    type: 'menuitem',
    label: 'idoc.tabs.configure',
    handler: 'ConfigureIdocTabs',
    module: 'idoc/idoc-tabs/configure-idoc-tabs',
    extensionPoint: 'idoc-tabs-menu-items'
  }, {
    id: 'deleteIdocTabs',
    eventId: 'DeleteIdocTabs',
    type: 'menuitem',
    label: 'idoc.tabs.delete',
    handler: 'DeleteIdocTabs',
    module: 'idoc/idoc-tabs/delete-idoc-tabs',
    extensionPoint: 'idoc-tabs-menu-items'
  }]
);