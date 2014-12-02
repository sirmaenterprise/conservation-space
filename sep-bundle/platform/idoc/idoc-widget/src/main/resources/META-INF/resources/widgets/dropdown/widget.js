(function () {
	'use strict';

	widgetManager.registerWidget('dropdown', {
		configurable: true,
		identifiable: true,
		configController: "DropdownWidgetConfigureController",
		titled: true,
		compileOnSave: false,
		configDialogWidth: 650,
		configDialogHeight: 400
	});

	var _dropdown = angular.module('dropdown',['ng']);

	/** Controller for the directive config */
	_dropdown.controller('DropdownWidgetConfigureController', ['$scope','CodelistService', function($scope, CodelistService) {
		
		$scope.clearValues = function() {
			$scope.config.values = [ ];
			if ($scope.config.loadFrom === 'codelist') {
				$scope.initCodelistSelection();
			}
		}
		
		$scope.addValue = function() {
			$scope.newValue.value = EMF.util.generateUUID();
			$scope.config.values.push($scope.newValue);
			$scope.newValue = { };
		}
		
		$scope.removeValue = function(item) {
			var index = _.findIndex($scope.config.values, function(currentItem) {
				return currentItem.value === item.value;
			});
	
			if (index > -1) {
				$scope.config.values.splice(index, 1);
			}
		}
		
		$scope.addRemove = function(item) {
			if(item.checked) {
				$scope.newValue.value = EMF.util.generateUUID();
				$scope.config.values.push(item);
				$scope.newValue = { };
			} else {
				var index = _.findIndex($scope.config.values, function(currentItem) {
					return currentItem.value === item.value;
				});
				
				if (index > -1) {
					$scope.config.values.splice(index, 1);
				}
			}
		}
		
		$scope.initCodelistSelection = function() {
			CodelistService.loadAllAvailableCodelists().then(function(response) {
				$scope.allCodelistNumbers = response.data;
				
				if(!$scope.config.codelistNumber) {
					$scope.config.codelistNumber = $scope.allCodelistNumbers[0].value;
				}

				$scope.loadCodelist($scope.config.codelistNumber);
			});
		}
		
		$scope.loadCodelist = function(codelistNumber) {
			if (codelistNumber) {
				CodelistService.loadCodelist(codelistNumber).then(function(response) {
					$scope.selectedCodelistValues = response.data;
					_.each($scope.selectedCodelistValues, function(item) {
						var index = _.findIndex($scope.config.values, function(selected) {
							return selected.value === item.value;
						});
						
						if (index !== -1) {
							item.checked = true;
						}
					});

					$scope.config.codelistNumber = codelistNumber;
					$scope.$emit('widget-config-save', $scope.config);
				});

			}
		}
		
		// When user open config panel saved configuration is loaded.
		$scope.$on('widget-config-init', function(event, config) {
			$scope.newValue = { };
			$scope.config.loadFrom = $scope.config.loadFrom || 'codelist'
		
			if ($scope.config.loadFrom === 'codelist') {
				$scope.initCodelistSelection();
			}
		});
	}]);
	
	/** 
	 * Controller for the directive initialization 
	 */
	_dropdown.controller('DropdownWidgetController', ['$scope', function($scope) {
		$scope.placeholder = _emfLabels['widget.dropdown.default'];

		$scope.init = function() {
			
			$scope.config.selected = $scope.config.selected || '';
			$scope.config.values = $scope.config.values || [ ];
			$scope.config.loadFrom = $scope.config.loadFrom || 'codelist';

			var selected = $scope.config.selected;

			if (selected) {
				var selectedObject = _.find($scope.config.values, function(currentItem) {
					return currentItem.value === selected;
				}); 
				
				$scope.selectedObject = selectedObject;
			}
		}
		
		$scope.$on('widget-config-save', function(event, config) {
			
			$scope.values = angular.copy(config.values);
			_.each($scope.values, function(item) {
				if(!item.label) {
					item.label = $scope.placeholder;
				}
			});
		});
	}]);
	
	/** Dropdown directive */
	_dropdown.directive('dropdown', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/dropdown/template.html',
			controller : 'DropdownWidgetController',
			link: function(scope, element, attrs) {
				scope.$watch('config.selected', function(newValue, oldValue) {
					scope.$emit('widget-config-save', scope.config);
				});

				scope.init();
			}
		};
	});

}());