(function () {
	'use strict';
	
	widgetManager.registerWidget('radiobutton', {
		configurable: true,
		identifiable: true,
		compileOnSave: false,
		configController: "RadiobuttonWidgetConfigureController",
		titled: true,
		configDialogWidth: 650,
		configDialogHeight: 400
	});
	
	var _radiobutton = angular.module('radiobutton',['ng']);
	
	/** Controller for the directive configuration */
	_radiobutton.controller('RadiobuttonWidgetConfigureController', ['$scope', function($scope) {
		
		$scope.addButton = function() {
			$scope.config.buttons.push($scope.newButton);
			$scope.newButton = { checked: false };
		}

		$scope.removeButton = function(index) {
			$scope.config.buttons.splice(index, 1);
		}
		
		$scope.$on("widget-config-init",function(event, config) {
			if (!config.buttons || !config.buttons.length) {
				config.buttons = [ ];
			}
			
			$scope.newButton = { checked: false };
		});
	}]);
	
	/** Controller for the directive initialization */
	_radiobutton.controller('RadiobuttonWidgetController', ['$scope', function($scope) {
		$scope.placeholder = _emfLabels['widget.radiobutton.default'];
		$scope.groupName = EMF.util.generateUUID();
		
		$scope.init = function() {
			var config = $scope.config;
			if(config.buttons) {
				$scope.allRadiobuttons = angular.copy(config.buttons);
			} else {
				$scope.allRadiobuttons = [];
			}
		}
		
		$scope.updateBtn = function(value) {
			var config = $scope.config;
			for(var key in config.buttons) {
				config.buttons[key].selected = false;
			}
			config.buttons[value].selected = value;
		}
		
		/** Update checkboxes when user change selection but only in edit mode */
		$scope.updateConfig = function(value) {
			
			for(var key in $scope.allRadiobuttons) {
				$scope.allRadiobuttons[key].selected = false;
			}
			$scope.allRadiobuttons[value].selected = value;
			
			$scope.config.buttons = $scope.allRadiobuttons;
			
			$scope.$emit('widget-config-save', $scope.config);
		}
		
		$scope.$on('widget-config-close', $scope.init);
	}]);
	
	/** Radiobutton directive */
	_radiobutton.directive('radiobutton', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/radiobutton/template.html',
			controller : 'RadiobuttonWidgetController',
			link: function(scope, element, attrs) {
				scope.init();
			}
		};
	});

}());