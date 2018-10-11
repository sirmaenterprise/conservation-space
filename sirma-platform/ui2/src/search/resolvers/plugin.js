PluginRegistry.add('search-resolvers', [{
  'order': 10,
  'name': 'contextual-rules-resolver',
  'component': 'contextualRulesResolver',
  'module': 'search/resolvers/contextual-rules-resolver'
}, {
  'order': 20,
  'name': 'dynamic-date-range-resolver',
  'component': 'dynamicDateRangeResolver',
  'module': 'search/resolvers/dynamic-date-range-resolver'
}]);