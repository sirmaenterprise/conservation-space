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