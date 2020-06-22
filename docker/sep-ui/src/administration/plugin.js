PluginRegistry.add('route', {
  'stateName': 'admin-configuration',
  'icon': 'fa-cogs',
  'label': 'menu.admin',
  'url': '/administration?tool',
  'component': 'seip-admin-configuration',
  'module': 'administration/admin-configuration'
});

PluginRegistry.add('admin-configuration', {
  'order': 10,
  'name': 'tenant-configuration',
  'label': 'administration.panel.tab.tenant.config',
  'component': 'seip-tenant-configuration',
  'module': 'administration/tenant/tenant-configuration'
});

PluginRegistry.add('admin-configuration', {
  'order': 20,
  'name': 'role-actions-table',
  'label': 'administration.panel.tab.manage.actions.per.role',
  'component': 'seip-role-actions-table',
  'module': 'administration/role-actions-table/role-actions-table'
});

PluginRegistry.add('admin-configuration', {
  'order': 30,
  'name': 'audit-log',
  'label': 'administration.panel.tab.audit.log',
  'component': 'seip-audit-log',
  'module': 'administration/audit-log/audit-log'
});

PluginRegistry.add('admin-configuration', {
  'order': 40,
  'name': 'code-lists',
  'label': ' administration.panel.tab.code.lists',
  'component': 'seip-code-lists',
  'module': 'administration/code-lists/code-lists'
});

PluginRegistry.add('admin-configuration', {
  'order': 50,
  'name': 'user-management',
  'label': 'administration.panel.tab.manage.users',
  'component': 'seip-resource-management',
  'module': 'administration/resources-management/resource-management'
});

PluginRegistry.add('admin-configuration', {
  'order': 60,
  'name': 'group-management',
  'label': 'administration.panel.tab.manage.groups',
  'component': 'seip-resource-management',
  'module': 'administration/resources-management/resource-management'
});

PluginRegistry.add('admin-configuration', {
  'order': 70,
  'name': 'model-import',
  'label': 'administration.models.import',
  'component': 'seip-model-import',
  'module': 'administration/model-import/model-import'
});

PluginRegistry.add('admin-configuration', {
  'order': 80,
  'name': 'libraries',
  'label': 'administration.panel.tab.browse.libraries',
  'component': 'seip-libraries',
  'module': 'administration/libraries/libraries'
});

PluginRegistry.add('route-interrupter', {
  'order': 20,
  'name': 'adminToolRouteInterrupter',
  'module': 'administration/admin-tool-route-interrupter'
});
