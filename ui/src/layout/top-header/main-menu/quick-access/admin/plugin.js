PluginRegistry.add('quick-access', {
  'order': 45,
  'name': 'seip-admin',
  'component': 'seip-admin',
  'module': 'layout/top-header/main-menu/quick-access/admin/admin-menu'
});

PluginRegistry.add('admin-menu-items', [
  {
    name: 'openUIAction',
    label: 'menu.admin.audit',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    href: 'remote/audit/audit-log.jsf',
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.codelist',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    href: 'remote/clsearch.jsf',
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.configuration',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    href: '/#/administration#tenantConfig',
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'openUIAction',
    label: 'menu.admin.manage.actions.and.roles',
    handler: 'OpenUIAction',
    module: 'layout/top-header/main-menu/quick-access/open-ui-action',
    href: '/#/administration#roleActionsTable',
    extensionPoint: 'admin-menu-items'
  },
  {
    name: 'browseLibrariesAction',
    label: 'menu.browse.libraries',
    params: {libraryType: 'object'},
    extensionPoint: 'browse-libraries-action',
    module: 'layout/top-header/main-menu/quick-access/browse-libraries-action'
  }
]);