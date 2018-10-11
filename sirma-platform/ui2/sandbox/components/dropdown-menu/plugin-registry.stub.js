window.PluginRegistry = (function () {
  var registry = {};
  registry.get = function () {
    return [{
      order: 10,
      label: 'Project',
      name: 'createProject',
      component: 'create-project',
      module: 'sandbox/components/dropdown-menu/create-project'
    }, {
      order: 15,
      label: 'Case',
      name: 'createCase',
      component: 'create-case',
      module: 'sandbox/components/dropdown-menu/create-case'
    }, {
      order: 20,
      label: 'Document',
      name: 'createDocument',
      component: 'create-document',
      module: 'sandbox/components/dropdown-menu/create-document'
    }];
  };
  return registry;
})();