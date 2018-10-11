PluginRegistry.add('advanced-search-criteria', [{
  'order': 10,
  'name': 'seip-advanced-search-string-criteria',
  'type': 'string',
  'operators': ['contains', 'does_not_contain', 'equals', 'does_not_equal', 'starts_with', 'does_not_start_with', 'ends_with', 'does_not_end_with', 'empty'],
  'component': 'seip-advanced-search-string-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-string-criteria'
}, {
  'order': 20,
  'name': 'seip-advanced-search-date-criteria',
  'type': 'dateTime',
  'operators': ['is_after', 'is_before', 'is', 'is_between', 'is_within', 'empty'],
  'component': 'seip-advanced-search-date-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-date-criteria'
}, {
  'order': 30,
  'name': 'seip-advanced-search-codelist-criteria',
  'type': 'codeList',
  'operators': ['in', 'not_in', 'empty'],
  'component': 'seip-advanced-search-codelist-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-codelist-criteria'
}, {
  'order': 40,
  'name': 'seip-advanced-search-relation-criteria',
  'type': 'object',
  'operators': ['set_to', 'not_set_to', 'set_to_query', 'not_set_to_query', 'empty'],
  'component': 'seip-advanced-search-relation-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-relation-criteria'
}, {
  'order': 50,
  'name': 'seip-advanced-search-boolean-criteria',
  'type': 'boolean',
  'operators': ['is', 'is_not'],
  'component': 'seip-advanced-search-boolean-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-boolean-criteria'
}, {
  'order': 60,
  'name': 'seip-advanced-search-numeric-criteria',
  'type': 'numeric',
  'operators': ['equals', 'does_not_equal', 'greater_than', 'less_than', 'is_between', 'empty'],
  'component': 'seip-advanced-search-numeric-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-numeric-criteria'
}, {
  'order': 70,
  'name': 'seip-advanced-search-action-criteria',
  'type': 'action',
  'operators': ['in', 'not_in', 'empty'],
  'component': 'seip-advanced-search-action-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-action-criteria'
}, {
  'order': 80,
  'name': 'seip-advanced-search-authority-criteria',
  'type': 'authority',
  'operators': ['set_to', 'not_set_to', 'empty'],
  'component': 'seip-advanced-search-authority-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-authority-criteria'
},{
  'order': 90,
  'name': 'seip-advanced-search-object-type-criteria',
  'type': 'objectType',
  'operators': ['in'],
  'component': 'seip-advanced-search-object-type-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-object-type-criteria'
}, {
  'order': 100,
  'name': 'seip-advanced-search-keyword-criteria',
  'type': 'fts',
  'operators': ['contains'],
  'component': 'seip-advanced-search-keyword-criteria',
  'module': 'search/components/advanced/criteria/advanced-search-keyword-criteria'
}]);