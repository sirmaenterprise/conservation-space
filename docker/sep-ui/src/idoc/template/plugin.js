PluginRegistry.add('actions',
  [{
    name: 'saveAsTemplateAction',
    module: 'idoc/template/save-as-template-action'
  },
  {
    name: 'createTemplateAction',
    module: 'idoc/template/create-template-action'
  },
  {
    name: 'activateTemplateAction',
    module: 'idoc/template/activate-template-action',
    forceRefresh: true
  },
  {
    name: 'setTemplateAsPrimaryAction',
    module: 'idoc/template/set-template-as-primary-action',
    forceRefresh: true
  },
  {
    name: 'editTemplateRuleAction',
    module: 'idoc/template/rules/edit-template-rules-action',
    forceRefresh: true
  },
  {
    name: 'deactivateTemplateAction',
    module: 'idoc/template/deactivate-template-action',
    forceRefresh: true
  },
  {
    name: 'updateExistingObjectsAction',
    module: 'idoc/template/update-existing-objects-action'
  },
  {
    name: 'cloneTemplateAction',
    module: 'idoc/template/clone-template-action',
    forceRefresh: true
  },
  {
    name: 'updateTemplateAction',
    module: 'idoc/template/update-template-action'
  }]
);
