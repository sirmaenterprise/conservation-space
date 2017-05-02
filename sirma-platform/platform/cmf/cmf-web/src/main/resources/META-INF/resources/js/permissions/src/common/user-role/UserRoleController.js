angular.module('permissions')
	.controller('UserRoleController', ['$scope', 'UserService', 'PermissionsService', function($scope, UserService, PermissionsService) {

		function getUserImageUrl(user) {
			var iconPath = '/emf/images/group-icon-32.png';
			if (user.type === 'user') {
				iconPath = '/emf/images/user-icon-32.png';
			}
			return iconPath;
		}

		function renderSelect2UserResult(item) {
			return '<div style="display: table-row;">' +
						'<div style="display: table-cell;">' +
							'<img style="vertical-align: top;" src="' + getUserImageUrl(item) + '">' +
						'</div>' +
						'<div style="display: table-cell;">' +
							'<div style="margin-top: 5px;">' + item.label + '</div>' +
							'<div style="font-weight: bold;"><small>' + item.value + '</small></div>' +
						'</div>' +
					'</div>';
		}

		function renderSelect2RoleResult(item) {
			return item.label;
		}

		$scope.userSelectorConfig = {
				placeholder : _emfLabels['permissions.select.user'],
				modelTransformer: function(data) {
				return data.id;
				},
			ajax: {
				url: EMF.servicePath + '/users',
				dataType: 'json',
				quietMillis: 250,
				data: function (term, page) {
					return {
						term: term || '',
						includeGroups: true,
						offset: page,
						// config
						limit: 25
					};
				},
				results: function (data, page) {
					var items,
						more,
						selected = $scope.selectedPermissions;

					// remove already selected users
					items = _.reject(data.items, function rejectFilter(item) {
						var find = _.find(selected, { Id: item.id });
						return find !== undefined;
					});

					more = (page * 25) < data.total - (data.items.length - items.length);
					return { results: items, more: more };
				}
			},
			// no selection if this is not here
			id: function(item) {
			   	return item.id;
			},
			initSelection: function(element, callback) {
				var id = $(element).val();

				UserService.find(id)
					.then(
						function success(res) {
							callback(res.data);
						}
					);
			},
			formatResult: renderSelect2UserResult,
			formatSelection: function(data) {
				return data.label;
			}
		};

		$scope.userRoleSelectorConfig = {

			placeholder : _emfLabels['permissions.select.role'],
			modelTransformer: function(data) {
				    return data.value;
			},
			ajax: {
				url: EMF.servicePath + "/permissions/roles",
				dataType: 'json',
				quietMillis: 250,
				data: function (term, page) {
					return {
						q: term
					};
				},
				results: function (data, page) {
					return { results: data };
				}
			},
			// no selection if this is not here
			id: function(item) {
				return item.label;
			},
			initSelection: function(element, callback) {
				var role = $(element).val();

				PermissionsService.findAllRoles()
					.then(
						function success(res) {
							var selected = _.find(res.data, function(item) {
								return item.value === role;
							});

							if(selected){
								callback(selected);
							}
						}
					);
			},


			formatResult: renderSelect2RoleResult,
			formatSelection:   function(data) {
				return data.label;
			}
		};

		$scope.remove = function() {
			$scope.$emit('permissions:ng:remove', $scope.model);
		};

		$scope.getUserImage = function() {
			var iconPath = '/emf/images/group-icon-32.png';
			if ($scope.model.type === 'user') {
				iconPath = '/emf/images/user-icon-32.png';
			}
			return iconPath;
		};

		$scope.$watch('model.username', function(value) {
			if (value) {
				UserService.find(value)
					.then(
						function success(res) {
							$scope.displayName = res.data.displayName;
						}
					);
			}
		});

		$scope.unblockInheritedPermissions = function() {
			if($scope.model.role && $scope.model.Id ) {
				$scope.$emit('permissions:ng:enableInherited');
			}
		};

		$scope.$watch('model.role', function(newValue,oldValue) {
			if (newValue) {
				PermissionsService.findAllRoles()
					.then(
						function success(res) {
							var selected = _.find(res.data, function(item) {
								return item.value === newValue;
							});

						if(selected){
							$scope.roleLabel = selected.label;
						}
					});
			}
		});

	}]);