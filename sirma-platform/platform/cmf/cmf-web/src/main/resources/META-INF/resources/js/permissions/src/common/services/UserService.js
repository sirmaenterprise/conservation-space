angular.module('permissions')
	.service('UserService', ['$http', function($http) {
		var baseUrl = EMF.servicePath + '/users';

		return {
			find: function(username) {
				return $http.get(baseUrl + '/' + username);
			}
		};
	}]);