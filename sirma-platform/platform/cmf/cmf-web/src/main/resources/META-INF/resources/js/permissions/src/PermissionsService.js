angular.module('permissions')
	.service('PermissionsService', ['$http', function($http) {
		var basePermissionsUrl = '/emf/service/permissions';

		return {
			findAllRoles: function() {
				var result = $http.get(basePermissionsUrl + '/roles', { cache: true });
				return  result;
			},

			findAll: function(instanceId, instanceType, includeCalculatedInherited) {
				var config = {
					params: {
						instanceId: instanceId,
						instanceType: instanceType
					}
				};

				if (includeCalculatedInherited) {
					config.params.includeCalculatedInherited = true;
				}

				return $http.get(basePermissionsUrl, config);
			},

			update: function(instanceId, instanceType, permissions) {
				var config = {
						params: {
							instanceId: instanceId,
							instanceType: instanceType
						}
					},
					payload = angular.copy(permissions.special);

				payload.permissions = _.filter(payload.permissions, function filterUnsetRoles(item) {
					return item.role && item.Id;
				});
				payload.inheritedPermissionsEnabled = permissions.inheritedPermissionsEnabled;
				return $http.post(basePermissionsUrl, angular.toJson(payload), config);
			},

			getRestoreEntityCount: function(instanceId, instanceType) {
				var conf = {
					params: {
						instanceType: instanceType
					}
				};

				return $http.get(basePermissionsUrl + '/' + instanceId + '/restore', conf);
			},

			restorePermissions: function(instanceId, instanceType) {
				var conf = {
					params: {
						instanceType: instanceType
					}
				};

				return $http.post(basePermissionsUrl + '/' + instanceId + '/restore', null, conf);
			}
		};
	}]);