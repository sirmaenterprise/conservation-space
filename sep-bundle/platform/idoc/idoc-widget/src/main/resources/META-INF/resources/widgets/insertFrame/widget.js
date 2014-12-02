(function () {
	'use strict';
	
//	widgetManager.registerWidget('insertFrame', {
//		configurable: true,
//		identifiable: true,
//		configController: "FrameWidgetConfigureController",
//		titled: true,
//		configDialogWidth: 650,
//		configDialogHeight: 150
//	});
	
	var _insertFrame = angular.module('insertFrame',['ng']);
	
	/** Controller for the directive initialization */
	_insertFrame.controller('FrameWidgetController', ['$scope','$sce', function($scope, $sce) {
		$scope.init = function() {
			$scope.url = $sce.trustAsResourceUrl($scope.config.url);
		}
	}]);
	
	/** Controller for the directive configuration */
	_insertFrame.controller('FrameWidgetConfigureController', ['$scope', function($scope) {
		$scope.$on('widget-config-open',function(event, config) {
			$scope.configurationDialog = {};
			$scope.configurationDialog.configUrl = config.url;
		});
			
		// On save all made changes are saved in widget configuration
		$scope.$on('widget-config-save',function(event, config) {
			config.url = $scope.configurationDialog.configUrl;
		});
		
		$scope.onchange = function() {
			alert('changed: ' + $scope.configUrl);
		}
	}]);
	
	/** insertFrame directive */
	_insertFrame.directive('insertFrame', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/insertFrame/template.html',
			controller : 'FrameWidgetController',
			link: function(scope, element, attrs) {
				scope.init();
			}
		};
	});

}());