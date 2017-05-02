angular.module('permissions')
	.controller('FacetController', ['$scope', function($scope) {

		$scope.expanded = true;

		$scope.toggle = function() {
			$scope.expanded = !$scope.expanded;
		};

	}]);