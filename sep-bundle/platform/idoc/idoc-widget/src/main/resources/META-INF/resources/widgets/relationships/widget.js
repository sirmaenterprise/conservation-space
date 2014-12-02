// TODO: this widget needs a makeover
(function () {
	'use strict';

	widgetManager.registerWidget('relationships', {
		configurable: true,
		identifiable: true,
		configController: "RelationshipsWidgetConfigureController",
		titled: true,
		compileOnSave: true,
		configDialogWidth: 920,
		configDialogHeight: 630
	});

	var _relationships = angular.module('relationships',['ng', 'idoc.common.filters']);
	
	/** Controller for the directive config */
	_relationships.controller('RelationshipsWidgetConfigureController', ['$scope','$http', function($scope, $http) {

		$scope.getSearchConfig = function() {
			var config = { 
				onSearchCriteriaChange: [ $scope.updateConfigWithSearchCriteria ],
				initialSearchArgs: $scope.config.searchCriteria
			};
			
			if ($scope.config.searchCriteria) {
				config.initialSearchArgs = $scope.config.searchCriteria;
			} else {
				config.initialSearchArgs  = {
						location			: ["emf-search-context-current-object"] 
				}
			}
			config.listeners = {
					'basic-search-initialized': function() {
						$(this).trigger('perform-basic-search');	
					}
			}
			return config;
		}
		 
		$scope.getObjectPickerConfig = function() {
			var cfg = $.extend(true, { }, $scope.getSearchConfig());
			cfg.selectDeselectCallback = $scope.objectSelectDeselectCallback;
			cfg.browserSelectDeselectCallback = $scope.objectSelectDeselectCallback; 
			cfg.uploadSelectDeselectCallback = $scope.objectSelectDeselectCallback; 
			
			cfg.singleSelection = true;
			cfg.selectedItems = { };

			if ($scope.value.selectedObject) {
				cfg.selectedItems[$scope.value.selectedObject.dbId] = $scope.value.selectedObject;
			}
			return cfg;
		}
		
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = newCriteria;
		}
		
		/** Called when the user selects an object from the picker */
		$scope.objectSelectDeselectCallback = function(data) {
			data = data.currentSelection ;
			if (data && data.op === 'add') {
				$scope.value.selectedObject = {
					dbId: data.dbId, 
					type: data.type, 
					header: data.compact_header
				};
			} else {
				$scope.value.selectedObject = null;
			}
		}
		
		$scope.$on('widget-config-init',function(event, config) {
			$scope.currentObjectIconPath = EMF.util.objectIconPath(idoc.object.type, 64);
			$scope.currentObjectHeader = idoc.object.defaultHeader; 
		});
	}]);
	
	/** Controller for the directive initialization */
	_relationships.controller('RelationshipsWidgetController', ['$scope', function($scope) {
		$scope.initialize = function() {
			if (!$scope.config.objectSelectionMethod) {
				$scope.config.objectSelectionMethod = 'current-object';
			}

			if ($scope.config.objectSelectionMethod === 'current-object') {
				$scope.currentObject = {
					dbId: idoc.object.id,
					type: idoc.object.type,
					header: idoc.object.defaultHeader
				}
			} else {
				$scope.currentObject = $scope.value.selectedObject;
			}
		}
		
		$scope.$on('widget-config-save', function(event, config) {

			if($scope.config.objectSelectionMethod === 'current-object') {
				$scope.config.searchCriteria = undefined;
			}
			
			config.widgetTitle = $scope.widgetTitle;
			$scope.$emit('widget-value-save', $scope.value);
			
			if ($scope.value.selectedObject) {
				$scope.currentObject = $scope.value.selectedObject;
			} else {
				$scope.currentObject = {
					dbId: idoc.object.id,
					type: idoc.object.type,
					header: idoc.object.defaultHeader
				}
			}
		});
	}]);

	/** Relationships directive */
	_relationships.directive('relationships', [function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/relationships/template.html',
			controller : 'RelationshipsWidgetController',
			link: function(scope, element, attrs) {

				// Changing this will act as a trigger for rebuilding the relationships table
				scope.currentObject = null;
				scope.$watch('currentObject', function(newValue, oldValue) {
					if (!newValue) {
						return;
					}
					
					// TODO: to be improved with CMF-6869
					element.data('extjsObjectRelations', false);
					element.children().remove();

					var default_header = $('<div class="object-picker">' + newValue.header + '</div>').find("a");
					if(default_header[0]) {
						var header = default_header[0].outerHTML;
					}

					element.extjsObjectRelations({
						contextPath		: SF.config.contextPath,
						instanceId		: newValue.dbId,
						instanceType	: newValue.type,
						header			: header,
						//edit is always allowed
						preview         : false,
						width			: null,
						height			: null,
						labels			: {
							propertySelector	: _emfLabels['cmf.relations.type']
						}
					});
				});
				
				scope.initialize();
			}
		};
	}]);

}());