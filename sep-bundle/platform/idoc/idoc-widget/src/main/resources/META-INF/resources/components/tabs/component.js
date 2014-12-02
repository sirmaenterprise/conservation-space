(function () {
	'use strict';
	
	widgetManager.registerComponent('tabs');
	var _module = angular.module('tabs', []);
	
	_module.directive('tabs', function() {
		return {
			restrict: 'A',
			link: function(scope, element, attrs) {
				element.tabs();
			}
		}
	});
	
}());