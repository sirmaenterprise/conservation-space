(function () {
	'use strict';
	
	widgetManager.registerComponent('basicSearch');
	
	
	var _module = angular.module('basicSearch', ['ng']);
	_module.controller('BasicSearchController', ['$scope', function($scope) {
		
	}]);
	
	_module.directive('basicSearch', function() {		
		return {
			restrict: 'A',
			controller: 'BasicSearchController',
			link: function(scope, element, attrs) {
				$(element).basicSearch(scope.getSearchConfig());
			}
		}
	});
}());