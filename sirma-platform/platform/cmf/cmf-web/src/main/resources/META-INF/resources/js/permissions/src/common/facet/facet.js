angular.module('permissions')
	.directive('facet', function() {
		return {
			restrict: 'E',
			replace: true,
			scope: true,
			transclude: true,
			templateUrl: 'common/facet/facet.tpl.html',
			controller: 'FacetController',
			link: function(scope, element, attrs) {

				attrs.$observe('title', function(value) {
					if (value) {
						scope.facetTitle = value;
					}
				});

			}
		};
	});