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