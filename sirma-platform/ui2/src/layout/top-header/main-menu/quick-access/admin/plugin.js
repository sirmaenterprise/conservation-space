PluginRegistry.add('quick-access', {
  'order': 45,
  'name': 'seip-admin',
  'component': 'seip-admin',
  'module': 'layout/top-header/main-menu/quick-access/admin/admin-menu'
});

PluginRegistry.add('admin-menu-items', [
  {
    name: 'openUIAction',
    label: 'menu.admin.browse.libraries',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'libraries'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.configuration',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'tenant-configuration'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.manage.actions.and.roles',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'role-actions-table'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'administration.panel.tab.audit.log',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'audit-log'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.codelist',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'code-lists'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.manage.users',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'user-management'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.manage.groups',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'group-management'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'administration.models.import',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'admin-configuration',
    params: {tool: 'model-import'},
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'administration.models.management',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    state: 'model-management',
    extensionPoint: 'admin-menu-items'
  }
]);