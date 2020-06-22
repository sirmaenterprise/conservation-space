PluginRegistry.add('field-validators', {
  // this validator MUST be the last one being executed
  'order': 10000,
  'name': 'condition',
  'component': 'condition',
  'module': 'form-builder/validation/condition/condition'
});