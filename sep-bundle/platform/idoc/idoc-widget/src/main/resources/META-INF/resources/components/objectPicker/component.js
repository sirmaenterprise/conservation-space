(function () {
	'use strict';
	
	widgetManager.registerComponent('objectPicker');
	
	
	var _module = angular.module('objectPicker', ['ng']);
	_module.controller('ObjectPickerController', ['$scope', function($scope) {
		
		$scope.onObjectSelected = function(data) {
			$scope.$emit('object-selected', data);
			$scope.$broadcast('object-selected', data);
		}
	}]);
	
	_module.directive('objectPicker', function() {
		return {
			restrict: 'A',
			controller: 'ObjectPickerController',
			link: function(scope, element, attrs) {
				$(element).objectPicker(scope.getObjectPickerConfig());
			}
		}
	});
}());