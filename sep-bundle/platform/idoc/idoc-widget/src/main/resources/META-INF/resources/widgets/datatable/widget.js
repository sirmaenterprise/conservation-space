(function () {
	'use strict';

	widgetManager.registerWidget('datatable', {
		configurable: true,
		identifiable: true,
		compileOnSave: false,
		configController: "DatatableWidgetConfigureController",
		titled: true,
		configDialogWidth: 960,
		configDialogHeight: 680
	});
	
	var _datatable = angular.module('datatable', ['ng', 'idoc.common.services', 'idoc.common.filters']);
	
	/**
	 * Controller responsible for the configuration of the widget.
	 */
	_datatable.controller('DatatableWidgetConfigureController', ['$scope', '$http', '$q', 'ObjectPropertiesService', function($scope, $http, $q, ObjectPropertiesService) {
		$scope.config.selectedProperties = $scope.config.selectedProperties || [ ];
		
		$scope.getSearchConfig = function() {
			var config = { 
				onSearchCriteriaChange: [ $scope.updateConfigWithSearchCriteria ],
				onItemAfterSearch: [ $scope.addPropertiesForObject ],
				onAfterSearch : [$scope.saveCurrentSearchCriteria]
			};
			if ($scope.config.searchCriteria && !jQuery.isEmptyObject($scope.config.searchCriteria)) {
				config.initialSearchArgs = $scope.config.searchCriteria;
			} else {
				config.initialSearchArgs  = {
						location			: ["emf-search-context-current-object"] 
				};
			}
			config.listeners = {
					'basic-search-initialized': function() {
						$(this).trigger('perform-basic-search');	
					}, 'before-search' : function(event, searchArgs) {
						$scope.updateConfigWithSearchCriteria(searchArgs);
						$scope.reloadAvailableProperties($scope.config);
						$scope.updateSelectedPropertiesBeforeSearch();
					}
			};
			return config;
		};
		
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
		
		/**
		 * Saves the new search criteria to the config.
		 */
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = newCriteria;
		};
		
		/**
		 * Load available properties based on object selection method.
		 */
		$scope.reloadAvailableProperties = function(widgetConfig) {
			$scope.availableProperties = [ ];
			
			if (widgetConfig.objectSelectionMethod === 'object-manual-select' && $scope.value.manuallySelectedObjects) {
				var promises = [ ];
				// collect the promises for all selected object properties
				_.forEach(
					$scope.value.manuallySelectedObjects, 
					function(item) {
						promises.push(ObjectPropertiesService.loadLiterals(item.type, item.dbId));
					}
				);
				
				// process all promises at once
				$q.all(promises).then(
					function(result) {
						_.forEach(
							result, 
							function(value) {
								$scope.updateAvailableProperties(value.data);
							}
						);
					}	
				);
			} else {
				// load all?
			}
		}
		
		/**
		 * Called when search criteria is changed with different types
		 */
		$scope.updateAvailableProperties = function(available) {
			var newValue = [ ];
			for (var index in available) {
				var property = available[index];
				$scope.addAvailableProperty(property, $scope.availableProperties);
			}
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
		 * Adds a single property to the provided list of properties if those not exist yet.
		 */
		$scope.addAvailableProperty = function(property, availableProperties) {
			// find the holder for the domain class to which this property belongs to
			var domainClassPropertiesHolder = _.find(
				availableProperties, 
				function(value) {
					return value.domainClass === property.domainClass;
				}
			);
			
			// if the domain class holder is not found - create a new one and push it to the list
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
			
			// finally add the property to the properties list of the domain class holder if does not exist in the list
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
		 * Called after a search for each item in the result.
		 */
		$scope.addPropertiesForObject = function(object) {
			ObjectPropertiesService.loadLiterals(object.emfType, object.dbId).then(
				function(result) {
					_.forEach(
						result.data,
						function(property) {
							$scope.addAvailableProperty(property, $scope.availableProperties);
						}
					);
				}
			);
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
				}
			}
		};
		
		/**
		 * Checks if the property has been selected to be shown in the data table.
		 */
		$scope.isPropertySelected = function(property) {
			var selected = _.find(
				$scope.config.selectedProperties, 
				function(selected) {
					return selected.name === property.name;
				}
			);
			return typeof selected !== 'undefined';
		};
		
		/**
		 * Initializes the widget configuration dialog.
		 */
		$scope.initConfig = function(event, widgetConfig) {

			if(typeof $scope.config.searchCriteria != 'undefined') {
				$scope.config.searchCriteria.pageSize = 10;
			}
			
			if (!widgetConfig.objectSelectionMethod) {
				widgetConfig.objectSelectionMethod = 'object-manual-select';
				widgetConfig.searchCriteria = { };
			}

			if($scope.config.selectedProperties) {
				$scope.oldSelectedProperties = angular.copy($scope.config.selectedProperties);
			}
			$scope.reloadAvailableProperties(widgetConfig);
		};
		
		$scope.onSaveConfig = function(event) {
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
		 * Called when an object is selected/deselected using the objects picker.
		 * This function only cares about the 'add' operation.
		 * When an 'add' operation is performed the function scans the selected objects array.
		 * If the object is not selected it is pushed to the array and its properties are 
		 * loaded and made available for selection.
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
					// TODO should we remove from basket when unchecking in the pickr?
				}
			});
		};
		
		/**
		 * Removes an object from the data basket and reloads the available properties list.
		 */
		$scope.removeFromBasket = function(object) {
			var filtered = _.reject(
				$scope.value.manuallySelectedObjects, 
				function(el) { 
					return el.dbId === object.dbId && el.domainClass === object.domainClass; 
				}
			);
			$scope.value.manuallySelectedObjects = filtered;
			$scope.reloadAvailableProperties($scope.config);
		}
		
		/**
		 * Saves the new search criteria to the config and reloads the available properties list.
		 */
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = angular.copy(newCriteria);
		};
		
		/**
		 * Change handler for the checkboxes of the properties on the property selection page.
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
	
	
	_datatable.controller('DatatableWidgetController', ['$scope', '$q', 'CodelistService', function($scope, $q, CodelistService) {
		
		$scope.initialize = function() {
			$scope.fields = [ ];
			$scope.columns = [ ];
			$scope.searchFields = [ ];
			
			$scope.count = 0;
			$scope.order = [];
			
			var context = {
				contextPath: EMF.applicationPath,
				service: {
					codelist: '/service/codelist'
				}
			};
			
			var codelistPromises = { };
			
			// the 'object id' column is always present?
			$scope.columns.push({
				header: _emfLabels["datatable.entity.column"], 
				flex: 2, 
				draggable: false,
				menuDisabled: true,
				renderer: function(val, meta, rec) {
					meta.css = rec.raw.emfType;
					return rec.raw.compact_header;
				}
			});
			
			// Getting the unique properties by their name - this is done so there 
			// will be only unique columns in the table. This is related to CMF-9299
			var uniqProperties = _.uniq($scope.config.selectedProperties, function(property) {
				return property.name;
			});

			// iterate the selected properties, if any of them are from codelists - cache them and fire event when ready
			_.forEach(uniqProperties, function(property) {
				$scope.searchFields.push(property.name);

				var cellEditor = EMF.extJsComponents.getCellEditor(context, property);
				var col = {
					editType: property.editType,
					editable: property.editable,
					codelistNumber: property.codelistNumber,
					header: property.title,
					draggable: !$scope.previewMode,
					dataIndex: property.name,
					processEvent: function () { return false; },
					renderer: function(value, metaData, rec) {
						
						if (!value) {
							return '';
						}

						if (metaData.column.editType === 'date') {
							return EMF.date.format(value);
						}
						
						if (metaData.column.editType === 'datetime' || metaData.column.editType === 'dateTime') {
							return EMF.date.getDateTime(value);
						}
						
						if (metaData.column.codelistNumber) {
							var label = EMF.cache.codelists[metaData.column.codelistNumber][value];
							if(label) {
								return label;
							}
						}
						
						// If JSON object find label and render it
						if (typeof value === 'object' && typeof value.label != 'undefined') {
	                		return value.label;
	                	}
						
	                    return value;
	                },
	                listeners:{
	                	move: function(a, x, y, eOpts ) {
		                	  var data = {
		               			  checked: true, 
		               			  editType: this.initialConfig.editType, 
		               			  editable: this.initialConfig.editable, 
		                		  name: this.initialConfig.dataIndex, 
		                		  title: this.initialConfig.header,
		                		  codelistNumber : this.initialConfig.codelistNumber
		                	  };
		                
		                	  $scope.count ++;
		                	  $scope.order.push(data);
		                	  if($scope.count == $scope.columns.length - 1) {
		                		  $scope.config.selectedProperties = $scope.order;
		                		  // FIXME This is wrong here...
		                		  var value = _.escape(JSON.stringify($scope.config));
		                		  $scope.widget.closest(".widget-datatable").attr('data-config', value);
		                		  $scope.count = 0;
		                		  $scope.order = [];
		                	  }
	                   }
	               },
					getEditor: function(record) {
						if(typeof record.raw[property.name] === 'undefined') {
							return null; 
						} else {
							return cellEditor; 
						}
					}
				}

				if (property.editType === 'date' || property.editType === 'datetime' || property.editType === 'dateTime') {
					$scope.fields.push({ name: property.name, mapping: property.name, type: 'date', format: 'Y-m-dTH:i:s.uuuP' });
				} else {
					$scope.fields.push({ name: property.name, mapping: property.name });
				}
				
				$scope.columns.push(col);
				
				// if a given codelist is not cached yet, make a request for the values 
				// and store the promise, it will be resolved with the rest later on and values will be cached
				if (property.codelistNumber && 
						!EMF.cache.codelists[property.codelistNumber] && !codelistPromises[property.codelistNumber]) {
					
					var codelistPromise = CodelistService.loadCodelist(property.codelistNumber);
					codelistPromises[property.codelistNumber] = codelistPromise;
				}
			});
			
			// if there are promises for codelist resolve them and cache the values
			// when done fire the 'datatable:init-ready' to render the table
			if (_.keys(codelistPromises).length > 0) {
				$q.all(codelistPromises).then(
					function(results) {
						_.forIn(
							results,
							function(value, clNumber) {
								_.forEach(
									value.data,
									function(codeValue) {
										var cached = EMF.cache.codelists[clNumber];
										if (!cached) {
											cached = { };
											EMF.cache.codelists[clNumber] = cached;
										}
										cached[codeValue.value] = codeValue.label;
									}
								);
							}
						);
						$scope.timeSinceLastRebuild = new Date().getTime();
					}
				);
			} else {
				$scope.timeSinceLastRebuild = new Date().getTime();
			}
		}
		
		$scope.$on('widget-config-save', function(event, config) {
			$scope.initialize();
		});
	}]);
	
	_datatable.directive('datatable', [function() {
		return {
			restrict: 'E',
			replace: true,
			controller : 'DatatableWidgetController',
			templateUrl: EMF.applicationPath + '/widgets/datatable/template.html', 
			link: function(scope, element, attrs) {
				scope.widget = element;
				
				// FIXME: move this to a more global place!!!
				Ext.JSON.encodeDate = function(o) {
					return Ext.Date.format(o, '"Y-m-d\\TH:i:s.uP"');
				};

				scope.rebuild = function() {
					element.children().remove();
					
					if (scope.config.objectSelectionMethod === 'object-search') { 
						var searchConf = {
							// page size is this big because of CMF-6554, we should talk about this...
							initialSearchArgs: angular.extend(scope.config.searchCriteria, { fields: scope.searchFields, pageSize: 1000 })
						};
						
						// FIXME: This initializes the whole search - drop-down values, templates and all...
						// Here we only need the URL for the search, and should avoid initializing the UI for the search.
						var search =  $({ }).basicSearch(searchConf);
						$(element).extjsDataGrid({
							enableEditing: !scope.previewMode,
							dataUrl: search.basicSearch('buildSearchUrl'),
							updateUrl: EMF.servicePath + '/object-rest/update',
							fields: scope.fields,
							columns: scope.columns
						});
					} else {
						var objects = [ ];
						_.forEach(scope.value.manuallySelectedObjects, function(item) {
							objects.push(_.omit(item, 'compact_header'));
						});
						
						$.ajax({
							type : 'POST',
							url : EMF.servicePath + '/object-rest/objectsByIdentity',
							data : JSON.stringify({ itemList: objects, fields: scope.searchFields }),
							complete : function(data) {
								var result = $.parseJSON(data.responseText);
								
								$(element).extjsDataGrid({
									enableEditing: !scope.previewMode,
									fields: scope.fields,
									columns: scope.columns,
									storeType: 'Ext.data.ArrayStore',
									storeConf: {
										model: 'dynamicModel',
										proxy: {
											type: 'memory',
								            reader: {
								                type: 'json'
								            },
								            writer: {
								                type: 'json',
								                writeAllFields: true
								            }
										},
										listeners: {
											update: function(store, record, operation, modifiedFieldNames, eOpts) {
												if (record.dirty) {
													$.ajax({
														type: 'POST',
														contentType: 'application/json',
														url: EMF.servicePath + '/object-rest/update',
														data: JSON.stringify(record.data),
														success: function(response) {
															record.commit();
														},
														error: function() {
															// TODO: show a message or something
														}
													});
												}
											}
										},
										data: result.values
									}
								});
							}
						});
					}
				}
				
				element.on('reload-uploaded-files',  function() {
					scope.rebuild();
				});
				
				scope.timeSinceLastRebuild = null;
				scope.$watch('timeSinceLastRebuild', function(newValue, oldValue) {
					if (!newValue) {
						return; 
					}
					
					scope.rebuild();
				});
				
				scope.initialize();
			}
		};
	}]);

}());