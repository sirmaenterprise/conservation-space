PluginRegistry.add('advanced-search-operator-filter', [{
  name: 'sub-query-operator-filter',
  component: 'subQueryOperatorFilter',
  module: 'search/components/advanced/filters/sub-query-operator-filter'
}, {
  name: 'excluded-operator-filter',
  component: 'excludedOperatorFilter',
  module: 'search/components/advanced/filters/excluded-operator-filter'
}]);

PluginRegistry.add('advanced-search-property-filter', [{
  name: 'free-text-operator-filter',
  component: 'freeTextPropertyFilter',
  module: 'search/components/advanced/filters/free-text-property-filter'
}]);