(
function () {
	'use strict';

	widgetManager.registerWidget('reporting', {
		configurable: true,
		identifiable: true,
		compileOnSave: false,
		configController: "ReportingWidgetConfigureController",
		titled: true,
		configDialogWidth: 960,
		configDialogHeight: 680
	});
	
	var _reporting = angular.module('reporting', ['ng', 'idoc.common.services', 'idoc.common.filters', 'emfReporting']);
	
	/**
	 * Controller responsible for the configuration of the widget.
	 */
	//XXX: Refactor the old datatable widget logic for fetching and displaying available properties! Most of the bugs related to the reporting filters are coming from here...
	_reporting.controller('ReportingWidgetConfigureController', ['$scope', '$http', '$q', 'ObjectPropertiesService', 'ReportingFactory', 'SemanticPropertiesService',  '$timeout', function($scope, $http, $q, ObjectPropertiesService, ReportingFactory, SemanticPropertiesService, $timeout) {
		$scope.config.availableProperties = $scope.config.availableProperties || [ ];
		$scope.config.selectedProperties = $scope.config.selectedProperties || [ ];
		$scope.config.types = {};
		
		$scope.getSearchConfig = function() {
			var config = { 
				onSearchCriteriaChange: [ $scope.updateConfigWithSearchCriteria ],
				onItemAfterSearch: [ $scope.addPropertiesForObject ],
				onAfterSearch : [$scope.saveCurrentSearchCriteria]
			};
			
			if ($scope.config.searchCriteria) {
				config.initialSearchArgs = $scope.config.searchCriteria;
			}
			
			config.listeners = {
					'basic-search-initialized': function() {
						$(this).trigger('perform-basic-search');
					},
					'basic-search:options-loaded' : function(event, data) {
					      if($(event.target).data('name') == 'objectType') {
					    	  var lastParent;
					    	  _.forEach(data.values, function(item) {
									if(item.objectType == 'category'){
										lastParent = item.name;
									} else {
										$scope.config.types[item.name] = lastParent;
									}
					    	  });
					      }
					},'before-search' : function(event, searchArgs) {
						$scope.updateConfigWithSearchCriteria(searchArgs);
						$scope.reloadAvailableProperties($scope.config);
						$scope.updateSelectedPropertiesBeforeSearch();
					}
			};
			return config;
		};
		
		/**
		 * Saves the search criteria after search.
		 */
		$scope.saveCurrentSearchCriteria = function () {
			$scope.oldSearchCriteria = angular.copy($scope.config.searchCriteria);
		};
		
		/**
		 * Compares the old and current object types and if they are different, 
		 * deselects all selected properties for the provided object.
		 * NOTE: This solution was decided after CMF-9282
		 */
		$scope.updateSelectedPropertiesBeforeSearch = function () {
			if($scope.config.searchCriteria && $scope.oldSearchCriteria){
				if(!_.isEqual($scope.config.searchCriteria.objectType, $scope.oldSearchCriteria.objectType)) {
					$scope.config.selectedProperties = [];
					// TODO: Better!
					$scope.config.report.additionalFilters = [];
				}
			}
		};
		
		/**
		 * Builds the configuration for the object picker used in the search tab of the widget.
		 */
		$scope.getObjectPickerConfig = function() {
			var cfg = $.extend(true, { }, $scope.getSearchConfig());
			cfg.selectDeselectCallback = $scope.updateDataBasket;
			cfg.browserSelectDeselectCallback = $scope.updateDataBasket;
			cfg.uploadSelectDeselectCallback = $scope.updateDataBasket;
			cfg.selectedItems = { };
			
			if ($scope.value.manuallySelectedObjects) {
				_.each($scope.value.manuallySelectedObjects, function(item) {
					cfg.selectedItems[item.dbId] = item;
				});
			}
			return cfg;
		};

		/** Reloads the common properties of the selected/founded objects and updates the group by selects by broadcasting an event.*/
		$scope.reloadCommonProperties = function (widgetConfig) {
			var types = [];
			var subTypes = [];
			if (widgetConfig.objectSelectionMethod === 'object-manual-select' && $scope.value.manuallySelectedObjects) {
				_.forEach($scope.value.manuallySelectedObjects, function(item) {
					types.push(item.domainClass);
				});
			} else if (widgetConfig.objectSelectionMethod === 'object-search'){
				if(widgetConfig.searchCriteria.objectType){
					types = widgetConfig.searchCriteria.objectType.slice(0);
				}
				if(widgetConfig.searchCriteria.subType){
					subTypes = widgetConfig.searchCriteria.subType.slice(0);
				}
			}
			_.forEach(types, function (item, index) {
				types[index] = item + "_";
			});
			
			_.forEach(subTypes,	function (item) {
				var subType = $scope.config.types[item];
				if (typeof subType !== 'undefined'){
					types.push(subType + "_" + item);
				}
			});
			
			var uniqTypes = _.uniq(types);
			
			var promise = SemanticPropertiesService.loadCommonSemanticProperties(uniqTypes);
			promise.then(function (result) {
				$scope.config.groupBy = [];
				_.forEach(result.data,function(val) {
					if(val) {
						var title=val.text;
						var value=val.id;
						var type=val.type;
						//Review: move it outside the configuration.
						$scope.config.groupBy.push({value:value, label:title, type:type});
					}
				});
				
				if($scope.config.report.groupBy==='') {
					//REVIEW: Add index checks
					$scope.config.report.groupBy=$scope.config.groupBy[0].value;
					$scope.config.report.addGroupBy=$scope.config.groupBy[1].value;
				}
				//TODO: Why the Timeout?
				$timeout(function() {
					$scope.$broadcast('emf:reporting:group-by-updated');
				}, 0);
			});
		};
		
		/**
		 * Load available properties based on object selection method.
		 */
		$scope.reloadAvailableProperties = function(widgetConfig) {
			$scope.availableProperties = [ ];
			
			if (widgetConfig.objectSelectionMethod === 'object-manual-select' && $scope.value.manuallySelectedObjects) {
				var promises = [ ];
				// collect the promises for all selected object properties
				_.forEach($scope.value.manuallySelectedObjects, function(item) {
					promises.push(ObjectPropertiesService.loadLiterals(item.type, item.dbId));
				});
				
				// process all promises at once
				$q.all(promises).then(function(result) {
					_.forEach(result, function(value) {
						$scope.updateAvailableProperties(value.data);
					});
				});
			} else {
				// load all?
			}
			
			$scope.reloadCommonProperties(widgetConfig);
			
			// XXX: Workaround... The properties should somehow be accessed in the reporting modules.
			$scope.config.availableProperties = $scope.availableProperties;
		};
		
		/**
		 * Called when search criteria is changed with different types
		 */
		$scope.updateAvailableProperties = function(available) {
			for (var index in available) {
				var property = available[index];
				$scope.addAvailableProperty(property, $scope.availableProperties);
			}
		};
		
		/**
		 * Called after a search for each item in the result.
		 */
		$scope.addPropertiesForObject = function(object) {
			ObjectPropertiesService.loadLiterals(object.emfType, object.dbId).then(
				function(result) {
					_.forEach(result.data, function(property) {
							$scope.addAvailableProperty(property, $scope.availableProperties);
					});
					// XXX: Workaround!! This is meant to notify the filters select... Also this is broadcasted multiple times..
					$scope.$broadcast('emf:reporting:properties:updated');
				}
			);
		};
		
		/**
		 * Adds a single property to the provided list of properties if those
		 * not exist yet.
		 */
		$scope.addAvailableProperty = function(property, availableProperties) {
			// find the holder for the domain class to which this property
			// belongs to
			var domainClassPropertiesHolder = _.find(
				availableProperties, 
				function(value) {
					return value.domainClass === property.domainClass;
				}
			);
			
			// if the domain class holder is not found - create a new one and
			// push it to the list
			if (!domainClassPropertiesHolder) {
				var sharpIndex = property.domainClass.lastIndexOf('#');
				var domainClassTitle = property.domainClass.substr(sharpIndex + 1);
					
				domainClassPropertiesHolder = { 
					title: domainClassTitle,
					domainClass: property.domainClass,
					properties: [ ]
				};
				
				availableProperties.push(domainClassPropertiesHolder);
			}
			
			// Check if the property is already added
			var index = _.findIndex(
				domainClassPropertiesHolder.properties,
				function(prop) {
					return prop.name === property.name && prop.type === property.type;
				}
			);
			
			// finally add the property to the properties list of the domain
			// class holder if does not exist in the list
			if (index === -1) {
				domainClassPropertiesHolder.properties.push({
					name: property.name,
					title: property.title || property.label,
					checked: $scope.isPropertySelected(property),
					editType: property.editType,
					editable: property.editable,
					description: property.description,
					codelistNumber: property.codelistNumber
				});
			}
			
		};

		/**
		 * Checks if the property has been selected to be shown in the data
		 * table.
		 */
		$scope.isPropertySelected = function(property) {
			var selected = _.find($scope.config.selectedProperties, function(selected) {
				return selected.name === property.name;
			});
			return typeof selected !== 'undefined';
		};
		
		/** Selects all properties in the second tab. */
		$scope.selectAllProperties = function(checked, holder) {
			_.forEach($scope.availableProperties, function (value) {
				if (holder.domainClass === value.domainClass) {
					_.forEach(value.properties, function (property) {
						property.checked = checked;
						if(!checked) {
							_.remove($scope.config.selectedProperties, function(current) {
								return current.name === property.name && current.type === property.type;
							});
						} else {
							$scope.config.selectedProperties.push(property);
						}
					});
				}
			});
		};
		
		/**
		 * Initializes the widget configuration dialog.
		 */
		$scope.initConfig = function(event, widgetConfig) {
			/** Chart init */
			if(typeof $scope.config.report === 'undefined') {
				$scope.config.report = ReportingFactory.defaultReportConfig();
			}
			
			//TODO: Bug with the page size!
			if(typeof $scope.config.searchCriteria !== 'undefined') {
				$scope.config.searchCriteria.pageSize = 10;
			}
			
			if (!widgetConfig.objectSelectionMethod) {
				widgetConfig.objectSelectionMethod = 'object-search';
				widgetConfig.searchCriteria = { };
			}

			if($scope.config.selectedProperties) {
				$scope.oldSelectedProperties = angular.copy($scope.config.selectedProperties);
			}
			$scope.reloadAvailableProperties(widgetConfig);
		};
		
		$scope.onSaveConfig = function (event) {
			if(jQuery.isEmptyObject($scope.value)) {
				return;
			}
			$scope.$emit('widget-value-save', $scope.value);
		};
		
		$scope.$on('widget-config-init', $scope.initConfig);
		$scope.$on('widget-config-save', $scope.onSaveConfig);
		
		$scope.$on('widget-config-closed', function(event, config) {
			
			$scope.config.searchCriteria = $scope.oldSearchCriteria;
			$scope.config.objectSelectionMethod = $scope.oldObjectSelectionMethod;
			$scope.config.selectedProperties = $scope.oldSelectedProperties;
			
			if(typeof $scope.config.objectSelectionMethod === 'undefined') {
				$scope.config.objectSelectionMethod = 'current-object';
			} else {
				$scope.config.objectSelectionMethod = $scope.oldObjectSelectionMethod;
			}

			widgetManager.saveConfig($scope.widget, $scope.config);
		});
		
		/**
		 * Called when an object is selected/deselected using the objects
		 * picker. This function only cares about the 'add' operation. When an
		 * 'add' operation is performed the function scans the selected objects
		 * array. If the object is selected it is pushed to the array and its
		 * properties are loaded and made available for selection.
		 */
		$scope.updateDataBasket = function(data) {
			data = data.currentSelection;
			$scope.$apply(function() { 
				if (data && data.op === 'add') {
					if (!$scope.value.manuallySelectedObjects) {
						$scope.value.manuallySelectedObjects = [ ];
					}
					
					var foundIndex = _.findIndex(
						$scope.value.manuallySelectedObjects, 
						function(object) {
							return data.dbId === object.dbId && data.type === object.type;
						}
					);
					
					if (foundIndex === -1) {
						$scope.value.manuallySelectedObjects.push({
							compact_header: data.compact_header,
							dbId: data.dbId,
							type: data.type,
							domainClass: data.domainClass
						});
						$scope.reloadAvailableProperties($scope.config);
					}
				} else {
					// TODO should we remove from basket when unchecking in the
					// pickr?
				}
				if ($scope.value.manuallySelectedObjects) {
					$scope.config.manuallySelectedObjects = $scope.value.manuallySelectedObjects;
				}
			});
		};
		
		/**
		 * Removes an object from the data basket and reloads the available
		 * properties list.
		 */
		$scope.removeFromBasket = function(object) {
			var filtered = _.reject(
				$scope.value.manuallySelectedObjects, 
				function(el) { 
					return el.dbId === object.dbId && el.domainClass === object.domainClass; 
				}
			);
			$scope.value.manuallySelectedObjects = filtered;
			if ($scope.value.manuallySelectedObjects) {
				$scope.config.manuallySelectedObjects = $scope.value.manuallySelectedObjects;
			}
			$scope.reloadAvailableProperties($scope.config);
		};
		
		/**
		 * Saves the new search criteria to the config and reloads the available
		 * properties list.
		 */
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = newCriteria;
		};
		
		/**
		 * Change handler for the checkboxes of the properties on the property
		 * selection page.
		 */
		$scope.updateSelectedProperties = function(property) {
			var removed = _.remove($scope.config.selectedProperties, function(current) {
				return current.name === property.name && current.type === property.type;
			});
			if (removed.length === 0) {
				$scope.config.selectedProperties.push(property);
			}
		};
	}]);
	
	
	/**
	 * Main directive that builds the template with directives.
	 */
	_reporting.directive('reporting', [ function () {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/reporting/template.html',
			link: function(scope, element, attrs) {
			}
		};
	}]);
}());