angular.module('permissions')
	.directive('userRole', function() {
		return {
			restrict: 'A',
			replace: true,
			templateUrl: 'common/user-role/user-role.tpl.html',
			controller: 'UserRoleController',
			scope: {
				model: '=userRole',
				selectedPermissions: '='
			},
			link: function(scope, element, attrs) {

				attrs.$observe('editable', function(value) {
					scope.editable = value === 'true';
				});

				attrs.$observe('disableUserSelect', function(value) {
					scope.disableUserSelect = value === 'true';
				});
			}
		};
	});