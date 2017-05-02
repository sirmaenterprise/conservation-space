angular.module('permissions')
	.directive('permissions', function() {
		return {
			restrict: 'A',
			templateUrl: 'permissions.tpl.html',
			controller: 'PermissionsController'
		};
	});