/**
 * Extensions used to plug in the model management page with the rest of the web application.
 */

PluginRegistry.add('route', {
  'stateName': 'model-management',
  'icon': 'fa-briefcase',
  'label': 'administration.models.management',
  'url': '/model-management?model&section',
  'component': 'seip-model-management',
  'module': 'administration/model-management/model-management'
});

PluginRegistry.add('route-interrupter', {
  'order': 30,
  'name': 'modelManagementRouteInterrupter',
  'module': 'administration/model-management/services/model-management-route-interrupter'
});