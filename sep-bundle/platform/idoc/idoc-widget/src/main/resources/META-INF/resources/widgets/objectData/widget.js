(function () {
	'use strict';
	
	//FIXME big duplication with datatable widget

	widgetManager.registerWidget('objectData', {
		configurable: true,
		identifiable: true,
		compileOnSave: true,
		configController: "ObjectDataWidgetConfigureController",
		titled: true,
		configDialogWidth: 940,
		configDialogHeight: 630
	});
	
	var _objectData = angular.module('objectData', ['ng', 'idoc.common.filters']);
	
	/** Controller for the directive config */
	_objectData.controller('ObjectDataWidgetConfigureController', ['$scope','$http', 'ObjectPropertiesService', function($scope, $http, ObjectPropertiesService) {
		
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
				};
			}
			config.listeners = {
					'basic-search-initialized': function() {
						$(this).trigger('perform-basic-search');	
					}
			};
			return config;
		};
		
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
		};
		
		$scope.selectAllProperties = function(checked) {
			for (var index in $scope.availableProperties) {
				var property = $scope.availableProperties[index].properties;
				if(checked) { 
					$scope.config.showLess = [];
				}
				for (var i in property) {
					if(checked) {
						$scope.config.showLess.push(property);
					}
					property[i].checked = checked;
					$scope.updateSelectedProperties(property[i]);
				}
			}
		};
		
		$scope.addPropertiesForObject = function(object) {
			ObjectPropertiesService.loadLiterals(object.type, object.dbId, idoc.object.definitionId).then(
				function(result) {
					$scope.updateAvailableProperties(result.data, $scope.availableProperties);
				}
			);
		};
		
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = newCriteria;
		};
		
		/** Called when the user selects a property in the object details tab */
		$scope.updateSelectedProperties = function(property) {
			var removed = _.remove($scope.config.showLess, function(current) {
				return current.name === property.name;
			});
			if (removed.length === 0 ) {
				$scope.config.showLess.push(property);
			}
		};
		
		$scope.$on('widget-config-closed', function(event, config) {
			if(typeof $scope.config.objectSelectionMethod === 'undefined') {
				$scope.config.objectSelectionMethod = 'current-object';
			} else {
				$scope.config.objectSelectionMethod = $scope.oldObjectSelectionMethod;
			}
			
			widgetManager.saveConfig($scope.widget, $scope.config);
		});
		
		/** Called when the user selects an object from the picker */
		$scope.objectSelectDeselectCallback = function(data) {
			data = data.currentSelection;
			if (data && data.op === 'add') {
				$scope.value.selectedObject = {
					dbId: data.dbId, 
					type: data.type 
				};
				$scope.reloadAvailableProperties($scope.config);
			} else {
				$scope.value.selectedObject = null;
			}
			$scope.config.showLess = [];
		};
		
		$scope.$on('widget-config-init', function(event, config) {
			$scope.availableProperties = [ ];
			$scope.currentObjectIconPath = EMF.util.objectIconPath(idoc.object.type, 64);
			$scope.currentObjectHeader = idoc.object.defaultHeader;
		});
		
		$scope.reloadAvailableProperties = function() {
			var selectedObject = { dbId: idoc.object.id, type: idoc.object.type };
			if ($scope.config.objectSelectionMethod === 'current-object') {
				selectedObject = { dbId: idoc.object.id, type: idoc.object.type };
			} else {
				if($scope.value.selectedObject) {
					selectedObject = $scope.value.selectedObject;
				}
			}

			ObjectPropertiesService.loadLiterals(selectedObject.type, selectedObject.dbId, idoc.object.definitionId).then(
				function(result) {
					$scope.availableProperties = [ ];
					$scope.updateAvailableProperties(result.data, $scope.availableProperties);
				}
			);
		};
		
		$scope.updateAvailableProperties = function(available, toUpdate) {
			for (var index in available) {
				var property = available[index];
				var domainClassPropertiesHolder = _.find(toUpdate, function(value) {
					return value.domainClass === property.domainClass;
				});
				if (!domainClassPropertiesHolder) {
					var sharpIndex = _.findLastIndex(property.domainClass.split(""), function(ch) {
						return ch === '#';
					});
					var domainClassTitle = property.domainClass.substr(sharpIndex + 1);
					
					domainClassPropertiesHolder = { 
						title: domainClassTitle,
						domainClass: property.domainClass,
						properties: [ ]
					};
					toUpdate.push(domainClassPropertiesHolder);
				}
				var checked = _.find($scope.config.showLess, function(selected) {
					return selected.name === property.name;
				});
				var existingProperty = _.find(domainClassPropertiesHolder.properties, 
					function(value) {
						return value.name === property.name;
					}
				);
				if (!existingProperty) {
					domainClassPropertiesHolder.properties.push({
						name: property.name,
						title: property.title,
						checked: (typeof checked !== 'undefined'),
						type: property.type,
						description: property.description
					});
				}
			}
		}
	}]);
	
	/** Controller for the running directive */
	_objectData.controller('ObjectDataWidgetController', ['$scope', '$http', function($scope, $http) {

		$scope.initialize = function() {
			$scope.config.showLess = $scope.config.showLess || $scope.defaultProperties;
			$scope.config.objectSelectionMethod = $scope.config.objectSelectionMethod || 'current-object';
			
			$scope.setSelectedPropertiesShortNames();
			$scope.setCurrentObject();
		};
		
		$scope.showHideEmpty = function(){
			$scope.config.displayEmptyDetails = $scope.displayEmptyDetails;
		}
		
		$scope.setSelectedPropertiesShortNames = function(properties) {
			if (properties) {
				$scope.selectedPropertiesShortNames = properties;
			} else {
				$scope.selectedPropertiesShortNames = [ ];
				_.forEach(
						$scope.config.showLess,
						function(item) {
							$scope.selectedPropertiesShortNames.push(item.name);
						}	
				);
			}
		};
		
		$scope.setCurrentObject = function(object) {
			$scope.error = null;
			
			if (object) {
				$scope.currentObject = object;
			} else {
				if ($scope.config.objectSelectionMethod === 'current-object') {
					$scope.currentObject = {
						dbId: idoc.object.id,
						type: idoc.object.type
					};
				} else if ($scope.config.objectSelectionMethod === 'object-search') {
					$scope.currentObject = $scope.value.selectedObject
				} else if ($scope.config.objectSelectionMethod === 'object-from-context') {
					var searchConf = {
						initialSearchArgs: $scope.config.searchCriteria
					};
					
					// FIXME: this initializes the whole search, but we only need to build the url
					var search =  $({ }).basicSearch(searchConf);
					var searchUrlWithParams = search.basicSearch('buildSearchUrl');
					
					$http.get(searchUrlWithParams).then(
						function(result) {
							var values = result.data.values,
								resultSize = values.length;
							
							if (resultSize === 1) {
								$scope.value.selectedObject = { dbId: values[0].dbId, type: values[0].type };
								$scope.currentObject = $scope.value.selectedObject;
							} else if (resultSize === 0) {
								$scope.error = 'No objects found using automatic object selection from current context!';
								$scope.value.selectedObject = null;
								$scope.currentObject = null;
							} else {
								$scope.error = 'More than one objects found using automatic object selection from current context!';
								$scope.value.selectedObject = null;
								$scope.currentObject = null;
							}
						}
					);
				}
			}
		};
		
		$scope.$on('widget-config-close', function() {
			$scope.setSelectedPropertiesShortNames();
			$scope.setCurrentObject();
		});
		
		$scope.$on('widget-config-save', function(event, config) {
			$scope.$emit('widget-value-save', $scope.value);
		});
	}]);
	
	_objectData.directive('objectData', ['$document', function($document) {
		return {
			restrict: 'E',
			templateUrl: EMF.applicationPath + '/widgets/objectData/template.html',
			replace: true,
			controller : 'ObjectDataWidgetController',
			link: function(scope, element, attrs) {
				if(scope.config.displayEmptyDetails) {
					scope.displayEmptyDetails = scope.config.displayEmptyDetails;
				} else {
					scope.displayEmptyDetails = {};
					scope.displayEmptyDetails.checked = true;
				}
				
				scope.defaultProperties = [
				                		    {checked: true, name: "title", title: "Title"}, 
				                			{checked: true, name: "description", title: "Description"},  
				                			{checked: true, name: "createdBy", title: "createdBy"}
				                		];
			
				scope.currentObject = null;
				scope.$watch('currentObject', function(newValue, oldValue) {
					var newValueObject = widgetManager.deserializeFromAttributeValue(scope.valueAttr);
					if (!newValue) {
						newValueObject.selectedObject = undefined;
						scope.$emit('widget-value-save', newValueObject);
						return;
					} else {
						newValueObject.selectedObject = newValue;
						scope.$emit('widget-value-save', newValueObject);
					}
					
					if(scope.config.showLess.length == 0) {
						scope.config.showLess = scope.defaultProperties;
						scope.setSelectedPropertiesShortNames();
					};
					var placeholder = element.find('.object-data-placeholder');
					
					// TODO: to be improved with CMF-6869
					placeholder.data('extjsObjectRelations', false);
					placeholder.children().remove();
					if (!scope.error) {
						placeholder.extjsObjectRelations({
							contextPath		: SF.config.contextPath,
							instanceId		: newValue.dbId,
							instanceType	: newValue.type,
							displayEmpty	: scope.displayEmptyDetails.checked,
							definitionId	: idoc.object.definitionId,
							preview			: scope.previewMode,
							mode			: 'literals',
							chosenProperties: scope.selectedPropertiesShortNames,
							service       : {
								loadData	 : '/service/object-rest/literals',
								loadRelTypes : '/service/definition/literals?unsetOnly=true&excludeSystem=true',
								create		 : '/service/object-rest/literals',
								removeFact	 : '/service/object-rest/remove'
							},
							labels: {
								add: 'Add Fact'
							}
						});
					}
				});
				
				scope.initialize();
				
				// align comments on expand collapse
				element.click(function() {
					$document.trigger('widgets:dom:widget-resize');
				});
			}
		};
	}]);
}());