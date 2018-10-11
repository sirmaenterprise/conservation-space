PluginRegistry.add('advanced-search-criteria', {
  'order': 11,
  'name': 'seip-advanced-search-test-criteria',
  'type': 'test-type',
  'operators': [{
    id:'test-operator',
    label: 'Test operator'
  },{
    id:'test-operator2',
    label: 'Test operator 2'
  }],
  'component': 'seip-advanced-search-test-criteria',
  'module': 'sandbox/search/components/advanced/criteria-extension/advanced-search-test-criteria'
});