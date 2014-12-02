(function() {
	'use strict';

	/**
	 * AngularJS module for all directives used in the reporting functionality.
	 * 
	 * @author Mihail Radkov
	 * @author Vilizar Tsonev
	 */
	var module = angular.module('emfReportingDirectives', []);
	
	
	/**
	 * Directive for rendering a template for the report settings - type, grouping by etc. Depends on 'reportConfig' 
	 * to be provided in the scope in order to function.
	 */
	module.directive('emfReportingSettings', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			controller : 'ReportingController',
			templateUrl	: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting-settings.tpl.html',
			scope : {
				reportConfig : '=conf'
			}
		};
	}]);
	
	
	/**
	 * Directive for rendering a template for the report's filter settings. Depends on 'reportConfig' to be provided 
	 * in the scope in order to function.
	 */
	module.directive('emfReportingFiltersSettings', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			controller : 'ReportingController',
			templateUrl	: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting-filters-settings.tpl.html',
			scope : {
				reportConfig : '=conf'
			}
		};
	}]);
	
	// FIXME: Currently all available and common(group by) properties are stored in the configuration. 
	// Split the reportConfig into two objects? Example: reportConfig = $scope.config.report and reportProperties = [$scope.availableProperties + $scope.groupBy]
	
	/**
	 * Main directive for rendering the report. Acts as mediator for the reporting & iDoc events that observes. The following 
	 * events invokes rendering and filtering: 'widget-config-save' and 'emf:reporting:filtered'. If the report is opened in 
	 * preview mode or some of the previous events are observed the event 'emf:reporting:render' is broadcasted. There is a 
	 * special case when the filtering is applied for Raw Table - then 'emf:reporting:raw-table:filtered' is broadcasted.
	 * Depends on 'reportConfig' to be provided in the scope in order to function. TODO: Such English.
	 */
	module.directive('emfReporting', ['$timeout', function ($timeout) {
		return {
			restrict: 'E',
			replace	: true,
			templateUrl	: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting.tpl.html',
			controller : 'ReportingController',
			scope : {
				reportConfig: '=conf'
			},
			link: function (scope, element, attrs) {
				
				/**
				 * Method for broadcasting the 'emf:reporting:render' event. If the report type is Raw Table 
				 * then just 'emf:reporting:render' event is thrown to the Raw Table carrying any filters. 
				 * If not then the controller calls several services to obtain the aggregated data for the 
				 * chart or/and aggregated table and then 'emf:reporting:render' is thrown.
				 * 
				 * @param userFilters - filters entered by the user
				 */
				scope.broadcastRenderEvent = function(userFilters) {
					if(scope.reportConfig.report.type === 'rawtable') {
						 scope.$broadcast('emf:reporting:render', userFilters);
					} else {
						EMF.ajaxloader.showLoading(element);
						scope.getAggregatedData(userFilters);
					}
					scope.$broadcast('emf:reporting:render-filters');
				};
				
				/**
				 * Observers 'emf:reporting:render' to remove the loading 
				 * mask on getting the aggregated data.
				 */
				scope.$on('emf:reporting:render', function() {
					EMF.ajaxloader.hideLoading(element);
				});
				
				/**
				 * Checks if the report's configuration is initialized. If true then the report is 
				 * rendered right away. The configuration is initialized under the following cases:
				 * 	- the widget is opened in preview or edit mode
				 */
				$timeout(function() {
					if (scope.reportConfig.report) {
						scope.broadcastRenderEvent();
					}
				});
				
				/**
				 * Observers the iDoc's widgets save event and renders the report.
				 */
				scope.$on('widget-config-save', function() {
					scope.broadcastRenderEvent();
				});
				
				/**
				 * Observers the 'emf:reporting:filtered' event. This means that some filtering is 
				 * applied to the report. If it's applied to a Raw Table then a special event is 
				 * thrown. If not then a new aggregated data is retrieved with the applied filters.
				 * 
				 * @param event - the event object
				 * @param userFilters - the applied filters
				 */
				scope.$on('emf:reporting:filtered', function(event, userFilters) {
					if(scope.reportConfig.report.type === 'rawtable') {
						scope.$broadcast('emf:reporting:raw-table:filtered', userFilters);
					} else {
						EMF.ajaxloader.showLoading(element);
						scope.getAggregatedData(userFilters);
					}
				});

			}
		};
	}]);
	
	
	/**
	 * Directive for plotting charts with Flot.js in EMF Reporting. The directive is event driven - the chart 
	 * is rendered only when 'emf:reporting:render' is fired with the service data. Depends on 'reportConfig'
	 * to be provided in the scope in order to function.
	 */
	module.directive('reportingChart', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			templateUrl	: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting-chart.tpl.html',
			controller 	: 'ReportingChartController',
			scope : {
				reportConfig : '=conf'
			},
			link: function(scope, element, attrs) {
				scope.show = true;

				/** 
				 * Observes the 'emf:reporting:render' event. If the carried data is 
				 * legitimate then a new chart is rendered but only if there are results.
				 * TODO: Explain why show is before the drawChart
				 * 
				 * @param event - the event obj
				 * @param serviceData - the carried data from the service
				 */
				scope.$on('emf:reporting:render', function (event, serviceData) {
					if(scope.checkServiceDataStatus(serviceData)) {
						scope.show = true;
						scope.drawChart(serviceData.data, element);
					} else {
						scope.show = false;
					}
				});
				
			}
		};
	}]);
	
	
	/**
	 * Directive for displaying an aggregated table based on the provided report configuration in EMF Reporting.
	 * The directive is event driven - the aggregated table is rendered only when 'emf:reporting:render' is 
	 * fired with the service data. Depends on 'reportConfig' to be provided in the scope in order to function.
	 */
	module.directive('reportingAggregatedTable', [ function () {
		return {
			restrict: 'E',
			replace : true,
			templateUrl: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting-aggtable.tpl.html',
			controller : 'ReportingAggregatedTableController',
			scope : {
				reportConfig : '=conf'
			},
			link: function(scope, element, attrs) {
				scope.show = true;
				
				/** 
				 * Observes the 'emf:reporting:render' event. If the carried data's status is legitimate
				 * then a new aggregated table is rendered but only if there are results.
				 * 
				 * @param event - the event obj
				 * @param serviceData - the carried data from the service
				 */
				scope.$on('emf:reporting:render', function(event, serviceData) {
					if(scope.checkServiceDataStatus(serviceData)){
						scope.aggregatedData = scope.getAggrTableData(serviceData.data);
						scope.show = true;
					} else {
						scope.show = false;
					}
				});
			}
		};
	}]);
	
	
	/**
	 * A directive for the form with additional filters for the reporting. Any applied filtering is emitted to the main 
	 * directive. Depends on 'reportConfig' to be provided in the scope in order to function.
	 */
	module.directive('reportingAdditionalFilters', [ function () {
		return {
			restrict: 'E',
			replace : true,
			templateUrl	: EMF.applicationPath + '/widgets/reporting/templates/emf-reporting-filters.tpl.html',
			controller	: 'ReportingAdditionalFiltersController',
			scope : {
				reportConfig : '=conf'
			},
			link: function(scope, element, attrs) {	
				
				/** The mousedown event is overrided to fix a bug in IE 10 when using 
				 * hide/show/toggle functions on a element (CMF-9266). For some reason 
				 * the event is caught elsewhere if not stopped here. */
				element.mousedown(function(event) {
					event.stopImmediatePropagation();
				});
				
				/** Binds the toggle functionality to the filters' title and them. */
				element.find('.additional-filters-title').click(function (event) {
					element.find('.additional-filters').slideToggle();
				});
				
				/** Attach click handler to the clear button. */
				element.find(".rawtable-clear-btn").click(function() {
					element.find(".filter-textbox").val('');
				});
				
				/** Attach the click handler to the filter button */
				element.find(".rawtable-filter-btn").click(function() {
					var userFilters = scope.buildFilters(element);
					scope.$emit('emf:reporting:filtered', userFilters);
				});
				
				/**
				 * Observes the 'emf:reporting:render:filters' event to know when to render the filters.
				 * Note: emf:reporting:render is not observed because any entered filters before that 
				 * will be cleared (TODO: rework the event logic in this case ?!?).
				 */
				scope.$on("emf:reporting:render-filters", function() {
					scope.updateFilterProperties();
				});
				
				/** TODO: FIXME: This is called because when the main directive broadcasts the 
				 * 'emf:reporting:render-filters' event, this directive is still not  
				 * initialized? Is there any other way ? maybe some waiting in the parent 
				 * directive for its children ? */
				scope.updateFilterProperties();
				
			}
		};
	}]);
	
	
	/**
	 * Directive for rendering ExtJS table to represent raw data. Listens for 'emf:reporting:raw-table:filtered' 
	 * to apply filtering and for 'emf:reporting:render' to render the table. Depends on 'reportConfig' to be 
	 * provided in the scope in order to function. Depending on the search configuration, different ExtJS 
	 * configurations are given to the table.
	 */
	module.directive('reportingRawTable', ['ObjectSearchService', function (ObjectSearchService) {
		return {
			restrict: 'E',
			replace: true,
			controller : 'ReportingRawTableController',
			scope : {
				reportConfig : '=conf'
			},
			link: function(scope, element, attrs) {
				
				//TODO: ? = rawtableelement
				scope.widget = element;
				
				// FIXME: move this to a more global place!!!
				// TODO: What's this?
				Ext.JSON.encodeDate = function(o) {
					return Ext.Date.format(o, '"Y-m-d\\TH:i:s.uP"');
				};
				
				/**
				 * Observers 'emf:reporting:raw-table:filtered' which means that some filters is applied
				 * on the Raw Table by an user. The filters are carried along with the event.
				 * 
				 * @param event - the event obj
				 * @param userFilters - filters entered by the user
				 */
				scope.$on('emf:reporting:raw-table:filtered', function(event, userFilters) {
					scope.filterTable(userFilters);
				});
				
				/**
				 * Listens for 'emf:reporting:render' to render the ExtJS table.
				 */
				scope.$on('emf:reporting:render', function() {
					scope.initialize();
					scope.rebuild();
				});
				
				/**
				 * Renders the ExtJS table. According to the report search configuration (object search or manually selected objects) 
				 * two different ExtJS configurations are build and applied to it.
				 */
				// TODO: Initialize it once and just update the model, columns etc.
				scope.rebuild = function() {
					// XXX: Why remove when we can just change the model?
					element.children().remove();
					
					if (scope.reportConfig.objectSelectionMethod === 'object-search') { 
						var searchConf = {
							// page size is this big because of CMF-6554, we
							// should talk about this...
							initialSearchArgs: angular.extend(scope.reportConfig.searchCriteria, { fields: scope.searchFields, pageSize: 1000 })
						};
						
						// FIXME: This initializes the whole search - drop-down
						// values, templates and all...
						// Here we only need the URL for the search, and should
						// avoid initializing the UI for the search.
						var search =  $({ }).basicSearch(searchConf);
						var extjsConfig = scope.createExtjsAjaxConfig(search);
						$(element).extjsDataGrid(extjsConfig);
						scope.rawTableStore = Ext.data.StoreManager.lookup('rawTableStore');
						
					} else {
						//That validation...
						var objects; objects = [];
						_.forEach(scope.reportConfig.manuallySelectedObjects, function(item) {
							objects.push(_.omit(item, 'compact_header'));
						});
						
						var promise = ObjectSearchService.searchByIdentity({ itemList: objects, fields: scope.searchFields });
						promise.then(function(result) {
							var extjsConfig = scope.createExtjsMemoryConfig(result.data.values);
							$(element).extjsDataGrid(extjsConfig);
							scope.rawTableStore = Ext.data.StoreManager.lookup('rawTableStore');
						});
					}
				};

				//TODO: In controller or in factory?!
				/**
				 * Builds an ExtJS data grid configuration for Ajax data loading.
				 * 
				 * @param searchService - the service for loading data
				 */
				scope.createExtjsAjaxConfig = function (searchService) {
					return {
						enableEditing: false,
						fields: scope.fields,
						columns: scope.columns,
						storeType : 'Ext.data.Store',
						storeConf: {
							model: 'dynamicModel',
							storeId: 'rawTableStore',
							autoLoad: false,
							autoSync: true,
							proxy: {
					            type: 'ajax',
					            api: {
					                read:  searchService.basicSearch('buildSearchUrl')
					            },
					            reader: {
					                type: 'json'
					            }
					        }
						}
					};
				};
				
				/**
				 * Builds an ExtJS data grid configuration with the provided data.
				 * 
				 * @param serviceData - the provided data
				 */
				//TODO: In controller or in factory?!
				scope.createExtjsMemoryConfig = function (serviceData) {
					return {
						enableEditing: false,
						fields: scope.fields,
						columns: scope.columns,
						storeType: 'Ext.data.ArrayStore',
						storeConf: {
							model: 'dynamicModel',
							storeId: 'rawTableStore',
							proxy: {
								type: 'memory',
					            reader: {
					                type: 'json'
					            }
							},
							data: serviceData
						}
					};
				};
					
			}
		};
	}]);
	
	
	/**
	 * An attribute directive for applying select2 styles to an element for single selection. 
	 * It takes two arguments: for specifying the element's width and an event's name to be 
	 * observed. If the event is fired then a change is triggered upon the element.
	 */
	// TODO: Test?
	module.directive('select2one', [ '$timeout', function ($timeout) {
		return {
			restrict : 'A',
			scope : {
				width	:'@',
				observe :'@'
			},
			link : function (scope, element, attrs) {
				
				/** Waits until the element is compiled to apply the select2 style. */
				$timeout(function () {
					var config = {}; 
					config.minimumResultsForSearch = -1;
					
					element.width(scope.width || 250);
					element.select2(config);
					
					scope.$on(scope.observe, function() {
						element.trigger('change');
					});
					
				});
				
			}
		};
	}]);
	
	
	/**
	 * Directive for multi valued input using select2 functionality and style. The select2 options 
	 * are provided as function that returns them when called.
	 */
	// TODO: Test?
	// TODO: Provide the data-placeholder text via the scope?
	module.directive('select2multi', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			template: '<input type="text" data-ng-model="model" data-placeholder="Choose filters ..."/>',
			scope: {
				options	: '&?',
				model	: '=',
				width	: '@',
				observe : '@'
			},
			link: function(scope, element, attrs) {
				
				/** Select2 configuration. */
				var conf = {
					multiple : true,
					data : function () { 
						if (scope.options) {
							var opt = scope.options();
							return { 
								more	: false,
								results	: opt
							};
						} else {
							return [];
						}
					}
				};
				
				/** Applies the provided width or 250 by default. */
				element.width(scope.width || 250);
				
				/** When the user selects something the model is updated. */
				element.select2(conf).on('change', function() {
					scope.$apply(function() {
						scope.model = element.select2('val');
					});
				});
				
				/** When the model is updated this method is triggered. */
				scope.$watch('model', function(value) {
					if (value) {
						if (value.length > 0) {
							element.select2('val', [value]);
						}
					} else {
						element.select2('val', []);
					}
				});
				
				/**
				 * Observes the provided event to the directive. If the
				 * model is existing it is provided to select2 as value.
				 * NOTE: The author thinks that this is a workaround...
				 */
				scope.$on(scope.observe, function() {
					//TODO: Remove those options from the select's model that does not exist.
					if (scope.model) {
						element.select2('val', [scope.model]);
					} else {
						element.select2('val', []);
					}
				});
				
			}
		};
	}]);
	

}());