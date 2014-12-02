(function () {
	'use strict';
	
	widgetManager.registerWidget("checkbox", {
		configurable: true,
		identifiable: true,
		configController: "CheckboxWidgetConfigureController",
		titled: true,
		compileOnSave: false,
		configDialogWidth: 650,
		configDialogHeight: 400
	});
	
	var _checkbox = angular.module('checkbox', ['ng']);
	
	/** Controller for the directive configuration */
	_checkbox.controller("CheckboxWidgetConfigureController", ["$scope", function($scope) {
		
		$scope.placeholder = _emfLabels['widget.checkbox.placeholder'];
		
		// add new checkbox to the group
		$scope.addCheckbox = function() {
			$scope.config.checked.push($scope.newCheckbox);
			$scope.newCheckbox = { };
		}

		// remove checkbox from group
		$scope.removeCheckbox = function(index) {
			$scope.config.checked.splice(index, 1);
		}
		
		// When user open config panel saved configuration is loaded.
		// (or empty checkbox if there's no saved config)
		$scope.$on("widget-config-init",function(event, config) {
			if (!config.checked || !config.checked.length) {
				config.checked = [ ];
			}
			
			$scope.newCheckbox = { };
		});
	}]);
	
	/** Controller for the directive initialization */
	_checkbox.controller('CheckboxWidgetController', ['$scope', function($scope) {
		$scope.placeholder = _emfLabels["widget.checkbox.default"];
		
		/** Init all checkboxes when the form is loaded for first time	 */
		$scope.init = function() {
			var config = $scope.config;
			if(config.checked) {
				$scope.allCheckboxes = angular.copy(config.checked);
			} else {
				$scope.allCheckboxes = [];
			}
		}
		
		/** Update checkboxes when user change selection but only in edit mode */
		$scope.updateConfig = function() {
			$scope.config.checked = angular.copy($scope.allCheckboxes);
			$scope.$emit('widget-config-save', $scope.config); 
		}
		
		$scope.$on('widget-config-close', $scope.init);
	}]);
	
	/** Checkbox directive */
	_checkbox.directive("checkbox", function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/checkbox/template.html',
			controller : "CheckboxWidgetController" ,
			link: function(scope, element, attrs) {
				scope.init();
			}
		};
	});

}());