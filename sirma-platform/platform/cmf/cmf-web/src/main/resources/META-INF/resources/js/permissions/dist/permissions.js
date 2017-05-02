(function() { angular.module('permissions', [ 'ui.select2']);
angular.module('permissions')
	.directive('permissions', function() {
		return {
			restrict: 'A',
			templateUrl: 'permissions.tpl.html',
			controller: 'PermissionsController'
		};
	});
angular.module('permissions')
	.controller('PermissionsController', ['$rootScope', '$scope', 'PermissionsService', 'PermissionsConfig', function($rootScope, $scope, PermissionsService, PermissionsConfig) {

		$scope.specialPermissionEditMode = false;


		$scope.init = function() {

			$scope.forId = PermissionsConfig._value('forId');
			$scope.forType = PermissionsConfig._value('forType');

			PermissionsService.findAll($scope.forId, $scope.forType, true)
				.then(
					function success(res) {
						$scope.permissions = res.data;

						$scope.permissionsSpecialFacet = _emfLabels['permissions.special.permissions'];
						$scope.permissionsInheritedFacet = _emfLabels['permissions.inheritet'];
						$scope.permissionsManagmentFacet = _emfLabels['permissions.managment'];



						$scope.editMode = false;
						$scope.blockInheritedPermissions = false;
						$scope.initialInheritedPermissions = res.data.inherited;
						$scope.calculatedInherited = res.data.calculatedInherited;

						if($scope.initialInheritedPermissions.permissions.length === 0
								&& !$scope.initialInheritedPermissions.allOther) {
							$scope.permissions.inheritedPermissionsEnabled = false;
						}

						if($scope.permissions.special.permissions.length === 0) {
							$scope.blockInheritedPermissions = true;
							$scope.permissions.inheritedPermissionsEnabled = true;
							$scope.refreshInheritedPermissions();
						}
						// Check if user have saved "Other User" information and show it if so
						if(res.data.special.allOther) {
							$scope.permissions.allOtherUsersEnabled = true;
						}
						if($scope.permissions.isRoot) {
							$scope.showAllSections = false;
							$scope.facetLabel =  _emfLabels["permissions.permissions"];
						} else {
							$scope.showAllSections = true;
							$scope.facetLabel =  _emfLabels["permissions.special.permissions"];
						}
					}
				);
		};

		$scope.refreshSpecialPermissions = function() {
			if (!$scope.permissions.allOtherUsersEnabled) {
				$scope.permissions.special.allOther = null;
			} else {
				var defaultAllOther = {
						Id: 'sec:SYSTEM_ALL_OTHER_USERS',
						Name: 'SYSTEM_ALL_OTHER_USERS',
						itemLabel: _emfLabels["permisions.system.all"],
						itemValue: 'SYSTEM_ALL_OTHER_USERS',
						role: 'NO_PERMISSION'
					};
				$scope.permissions.special.allOther = defaultAllOther;
			}

			$scope.blockInheritedPermissions = false;
			if($scope.permissions.allOtherUsersEnabled) {
				return;
			}
			if($scope.permissions.special.permissions.length === 0) {
				$scope.blockInheritedPermissions = true;
				$scope.permissions.inheritedPermissionsEnabled = true;
				$scope.refreshInheritedPermissions();
			} else {
				if(!$scope.permissions.special.permissions[0].Id || !$scope.permissions.special.permissions[0].role) {
					$scope.blockInheritedPermissions = true;
					$scope.permissions.inheritedPermissionsEnabled = true;
				}
			}
		};

		$scope.refreshInheritedPermissions = function() {
			if (!$scope.permissions.inheritedPermissionsEnabled) {
				$scope.permissions.inherited = { };
				return;
			}
			$scope.permissions.inherited = $scope.calculatedInherited;
		};

		$scope.editPermissions = function() {
			$scope.editMode = true;
			
			// Edit permission button should be hidden in project library
			var isProjectLibrary = $scope.instanceId.indexOf('emf:project') > -1;
			if(isProjectLibrary) {
				$scope.permissions.restoreAllowed = false;
			}
		};

		$scope.restorePermissions = function() {
			function restorePermissionsSuccess() {
				$scope.init();
			}

			function restoreEntityCountSuccess(response) {
				var count = response.data.result;
				EMF.dialog.confirm({
					modal: true,
					closeOnMaskClick: false,
					title: _emfLabels['permissions.restore.confirm.title'],
					message: EMF.util.formatString(_emfLabels['permissions.restore.confirm.message'], count),
					confirm: function confirmRestorePermissions() {
						PermissionsService.restorePermissions($scope.forId, $scope.forType)
						.then(restorePermissionsSuccess);
					}
				});
			}

			PermissionsService.getRestoreEntityCount($scope.forId, $scope.forType)
				.then(restoreEntityCountSuccess);
		}

		$scope.addSpecialPermission = function() {
			var special = $scope.permissions.special.permissions;
			if (!special) {
				special = [ ];
				$scope.permissions.special.permissions = special;
			}
			special.unshift({ });
		};

		$scope.savePermissions = function() {
			var showErrorMessge = true;
			if($scope.permissions.isRoot) {
				_.each($scope.permissions.special.permissions, function(item) {
					if(item.role === "MANAGER") {
						showErrorMessge = false;
						return;
					}
				});

			} else {
				showErrorMessge = false;
			}

			if(showErrorMessge) {
				EMF.dialog.open({
					 "title": _emfLabels['permissions.restore.error.title'],
					 "message": _emfLabels['permissions.restore.error.message'],
					 notifyOnly: true
				});
				return;
			}


			PermissionsService.update($scope.forId, $scope.forType, $scope.permissions)
				.then(
					function success(res) {
						$scope.permissions = res.data;
						if(res.data.special.allOther) {
							$scope.permissions.allOtherUsersEnabled = true;
						}
						$rootScope.$broadcast('permissions:ng:changed');

						$scope.editMode = false;
					}
				);
		};

		$scope.cancelEdit = function() {
			$scope.init();
		};

		$scope.$on('permissions:ng:remove', function(event, user) {
			var permissions = $scope.permissions.special.permissions || [ ],
				index = _.findIndex(permissions, function(item) {
					return item.Id === user.Id;
				});

			if (index > -1) {
				permissions.splice(index, 1);
			}

			if(!$scope.permissions.allOtherUsersEnabled && $scope.permissions.special.permissions.length == 0) {
				$scope.blockInheritedPermissions = true;
				$scope.permissions.inheritedPermissionsEnabled = true;
				$scope.refreshInheritedPermissions();
			}
		});

		$scope.$on('permissions:ng:enableInherited', function() {
			$scope.blockInheritedPermissions = false;
		});

		$scope.init();
	}]);
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
angular.module("permissions").run(["$templateCache", function($templateCache) {$templateCache.put("permissions.tpl.html","<div class=\"instance-permissions\" style=\"padding-right: 1px;\">\n	<div ng-if=\"showAllSections\">\n	<label emf-label=\"permissions.type\"></label> <span class=\"permission-model\">{{ permissions.permissionModel | permissionType }}</span>\n	</div>\n	<div>\n		<button type=\"button\" class=\"btn btn-sm btn-primary edit-permissions\" ng-click=\"editPermissions()\" ng-hide=\"!permissions.editAllowed || editMode\">\n			<span emf-label=\"permissions.edit.permissions\" ></span>\n		</button>\n\n		<button type=\"button\" class=\"btn btn-sm btn-danger restore-permissions\" ng-click=\"restorePermissions()\" ng-hide=\"!permissions.restoreAllowed || !editMode\">\n			<span emf-label=\"permissions.restore.permission\"></span>\n		</button>\n\n		<button type=\"button\" class=\"btn btn-sm btn-primary save-permissions\" ng-click=\"savePermissions()\" ng-show=\"editMode\">\n				<span emf-label=\"permissions.save.permission\"></span>\n		</button>\n		\n		<button type=\"button\" class=\"btn btn-sm btn-default cancel-edit-permissions\" ng-click=\"cancelEdit()\" ng-show=\"editMode\">\n		<span emf-label=\"permissions.cancel\"></span>\n		</button>\n	</div>\n\n	<facet class=\"facet-special-permissions\" title=\"{{permissionsSpecialFacet}}\">\n\n		<button type=\"button\" class=\"btn btn-sm btn-default add-user-group-btn\" ng-click=\"addSpecialPermission()\" ng-show=\"editMode\">\n			<span class=\"glyphicon glyphicon-plus\" aria-hidden=\"true\"  emf-label=\"permissions.add.user.group\"></span>\n		</button>\n		\n		<div ng-repeat=\"permission in permissions.special.permissions\" \n			class=\"user-role-assignment\" \n			user-role=\"permission\" \n			selected-permissions=\"permissions.special.permissions\" \n			editable=\"{{editMode}}\" \n			style=\"margin-bottom: 10px;\">\n		</div>\n		\n		<div ng-if=\"editMode && showAllSections\">		\n				<input type=\"checkbox\" \n					ng-model=\"permissions.allOtherUsersEnabled\"\n					ng-disabled=\"!permissions.editAllowed || !editMode\"\n					ng-change=\"refreshSpecialPermissions()\" />\n					<label emf-label=\"permissions.enable.users\" style=\"width: inherit;\"></label>\n		</div>\n		<div class=\"user-role-assignment all-other-users-section\"\n		 ng-if=\"permissions.special.allOther\" user-role=\"permissions.special.allOther\" editable=\"{{editMode}}\" disable-user-select=\"true\"></div>\n	</facet>\n	\n	<facet class=\"facet-inherited-permissions\" title=\"{{permissionsInheritedFacet}}\" ng-if=\"showAllSections\">\n			<input type=\"checkbox\"\n				ng-model=\"permissions.inheritedPermissionsEnabled\"\n				ng-disabled=\"!permissions.editAllowed || !editMode || blockInheritedPermissions\"\n				ng-change=\"refreshInheritedPermissions()\" />\n			<label style=\"width: inherit;\" emf-label=\"permissions.enable.inherit.permissions\"></label>	\n\n		<div ng-repeat=\"permission in permissions.inherited.permissions\" class=\"user-role-assignment\" user-role=\"permission\" style=\"margin-bottom: 10px;\"></div>\n\n		<div class=\"user-role-assignment\" ng-if=\"permissions.inherited.allOther\" user-role=\"permissions.inherited.allOther\"></div>\n	</facet>\n	\n	<facet class=\"facet-management-permissions\" title=\"{{permissionsManagmentFacet}}\" ng-if=\"showAllSections\">\n		<div ng-repeat=\"permission in permissions.management.permissions\" class=\"user-role-assignment\" user-role=\"permission\" style=\"margin-bottom: 10px;\"></div>\n	</facet>\n</div>");
$templateCache.put("common/facet/facet.tpl.html","<div class=\"facet\">\n	\n	<div class=\"facet-header expanded\">\n		<span class=\"facet-title\">{{facetTitle}}</span>\n	</div>\n	\n	<div class=\"facet-content\" ng-transclude=\"noop\">\n		<!-- the content of the facet will be inserted here -->\n	</div>\n\n</div>");
$templateCache.put("common/user-role/user-role.tpl.html","<div class=\"row\">\n\n	<div class=\"col-md-6 user-assignment\">\n		<input style=\"width: 100%;\" ng-if=\"editable && !disableUserSelect\" type=\"hidden\" ui-select2=\"userSelectorConfig\" ng-model=\"model.Id\" ng-change=\"unblockInheritedPermissions()\"\n		/>\n\n		<div ng-if=\"!editable || disableUserSelect\">\n			<div style=\"float: left;\">\n				<img alt=\"user image\" ng-src=\"{{ getUserImage(model) }}\" />\n			</div>\n			<div style=\"float: left;\">\n				<div>{{ model.itemLabel }}</div>\n				<div style=\"font-weight: bold;\"><small>{{ model.itemValue }}</small></div>\n			</div>\n		</div>\n	</div>\n\n	<div class=\"col-md-4 role-assignment\">\n\n		<input style=\"width: 100%;\" \n			ng-if=\"editable\" \n			type=\"hidden\" \n			ui-select2=\"userRoleSelectorConfig\" \n			ng-model=\"model.role\"\n			ng-change=\"unblockInheritedPermissions()\" />\n\n		<div ng-if=\"!editable\">{{ roleLabel }}</div>\n	</div>\n\n	<div class=\"col-md-1\">\n		<button type=\"button\" class=\"btn btn-xs btn-default remove-assignment-btn\" ng-if=\"editable\" ng-click=\"remove()\">\n			<span class=\"glyphicon glyphicon-minus\" aria-hidden=\"true\"></span>\n		</button>\n	</div>\n\n</div>");}]);
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
angular.module('permissions')
	.controller('FacetController', ['$scope', function($scope) {

		$scope.expanded = true;

		$scope.toggle = function() {
			$scope.expanded = !$scope.expanded;
		};

	}]);
angular.module('permissions')
	.filter('permissionType', function() {
		return function(permissionModel) {
			var type = '';

			switch (permissionModel) {
				case 'INHERITED':
					type =  _emfLabels['permissions.value.inherited'];
					break;
				case 'SPECIAL':
					type = _emfLabels['permissions.value.special'];
					break;
				case 'SPECIAL_INHERITED':
					type = _emfLabels['permissions.value.inherited.special'];
					break;
				default:
					type = permissionModel;
					break;
			};

			return type;
		};
	});
angular.module('permissions')
	.provider('PermissionsConfig', function() {
		var _this = this,
			accessFns = {
				_value: function() {
					var argsLen = arguments && arguments.length,
						argValue;
					if (!argsLen) {
						return;
					}

					if (argsLen === 1) {
						argValue = _this.config[arguments[0]];
						if (typeof argValue === 'function') {
							return argValue();
						}
						return argValue;
					} else if (argsLen === 2) {
						_this.config[arguments[0]] = arguments[1];
					}
				}
			};

		_this.config = { };

		this.$get = function() {
			return _this.config;
		};

		this.extend = function(conf) {
			angular.extend(_this.config, conf, accessFns);
		};
	});
angular.module('permissions')
	.service('UserService', ['$http', function($http) {
		var baseUrl = EMF.servicePath + '/users';

		return {
			find: function(username) {
				return $http.get(baseUrl + '/' + username);
			}
		};
	}]);
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

	}]); }());