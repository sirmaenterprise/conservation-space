(function() {
	'use strict';
	
	/**
	 * AngularJS module for all controllers used in the reporting functionality.
	 * 
	 * @author Mihail Radkov
	 * @author Vilizar Tsonev
	 */
	var module = angular.module('emfReportingControllers', []);
	
	/**
	 * Controller for the main directive in EMF Reporting. Contains functionality for populating selects in the 
	 * settings template and getting aggregated data. 
	 */
	module.controller('ReportingController', ['$scope', 'ReportingFactory', function ($scope, ReportingFactory) {
		
		//TODO: Move it in the directive for the report settings?
		/** Gets the available report types from the ReportingFactory. */
		$scope.getReportTypes = function () {
			return ReportingFactory.reportTypes;
		};
		
		//TODO: Move it in the directive for the report settings?
		/** Gets the available time intervals from the ReportingFactory. */
		$scope.getTimeIntervals = function () {
			return ReportingFactory.timeIntervals;
		};
		
		/** Updates the group by type according to what is the group by value. */
		//TODO: Move it in the directive for the report settings?
		$scope.updateGroupByType = function () {
			var type = ReportingFactory.getProperty($scope.reportConfig.report.groupBy, $scope.reportConfig.groupBy, 'value', 'type');
			$scope.reportConfig.report.groupByType = type;
		};
		
		/**
		 * Formats the available properties in format for select2 tags. The properties are grouped.
		 */
		// TODO: Move it in the directive for the report filters settings??
		// TODO: Surely there is a lot easier way for this case ...
		$scope.getGroupedAvailableProperties = function () {
			var grouped = [];
			if (typeof $scope.reportConfig.availableProperties !== 'undefined') {
				_.forEach($scope.reportConfig.availableProperties, function (group) {
					var properties = [];
					_.forEach(group.properties, function (property) {
						properties.push({id:property.name, text:property.title});
					});
					grouped.push({text:group.title, children:properties});
				});
			}
			return grouped;
		};
		
		/**
		 * Retrieves aggregated data according to the report configuration. Depending on the search 
		 * configuration performs one of the following actions:
		 * 
		 * 	1) 	A basic search to retrieve the objects and then filters them and finally calls the 
		 * 		faceted search service with the obtained data to get the aggregated data
		 * 	2) 	Gets the manually selected objects and calls the faceted search service with the IDs to get the 
		 * 		aggregated data
		 * 
		 * @param filters - the user filters
		 */
		//TODO: Test it with Jasmine spies.
		$scope.getAggregatedData = function (filters) {
			var searchConfig = $scope.reportConfig.searchCriteria;
			if ($scope.reportConfig.objectSelectionMethod === 'object-search') {
				var basicSearchPromise = ReportingFactory.getBasicSearchPromise(searchConfig, filters);
				basicSearchPromise.then(function(basicSearchResult) {
					var data = basicSearchResult.data.values;
					if(!_.isEmpty(filters)) {
						data = ReportingFactory.filterObjectsArray(data, filters);
					} 
					$scope.processFacetedSearch(searchConfig, data);
				});
			} else {
				//XXX: the filtering should be applied here too, but manuallySelectedObjects cache does not contain all the needed properties for 
				//the filtering. Perhaps a basic search request should be performed to get them. 
				var data =  $scope.reportConfig.manuallySelectedObjects;
				$scope.processFacetedSearch(searchConfig, data);
			}
		};
		
		/**
		 * Performs a call to the faceted search service with the provided search configuration and data. When the promise 
		 * is received from the service, an event is throw to inform the reporting modules that a new data is retrieved.
		 * 
		 * @param searchConfig - the provided search configuration
		 * @param data - the provided data from the basic search or manually selected objects
		 */
		// TODO: Don't pass the searchConfig ?
		// TODO: Test it with Jasmine spies.
		$scope.processFacetedSearch = function (searchConfig, data) {
			var facetedSearchPromise = ReportingFactory.getFacetedSearchPromise($scope.reportConfig, searchConfig, data);
			facetedSearchPromise.then(function(facetedSearchResult) {
				$scope.$broadcast('emf:reporting:render', facetedSearchResult);
			});
		};
		
	}]);
	
	
	/**
	 * Controller for the aggregated tables in EMF Reporting. Contains functionality for transforming service 
	 * responses into an appropriate format for the template's logic. Depends on ReportingFactory for extracting 
	 * properties and time intervals. Depends on reportConfig to have .report.groupBy & .groupBy.
	 */
	module.controller('ReportingAggregatedTableController', ['$scope', 'ReportingFactory', function($scope, ReportingFactory) {
		
		/**
		 * Checks the service response's status code and data. If some of them 
		 * are not correct the result will be false, otherwise true.
		 * 
		 * @param serviceData - 
		 * 					the service response
		 * @return true if correct or false otherwise
		 */
		// TODO: Move it in the main directive's controller ?!
		$scope.checkServiceDataStatus = function (serviceData) {
			return ReportingFactory.checkServiceDataStatus(serviceData);
		};
		
		/**
		 * Formats the data into an appropriate format for the template's logic. 
		 * Includes also title and group by meta info. 
		 * 
		 * @param data - 
		 * 				the service response's data with facet fields
		 * @return the formatted data
		 */
		// TODO: Test it?
		$scope.getAggrTableData = function (data) {
			var aggTableData = { };
			
			aggTableData.title = ReportingFactory.getProperty($scope.reportConfig.report.groupBy, $scope.reportConfig.groupBy, 'value', 'label');
			if ($scope.reportConfig.report.groupByType === 'tdates') {
				aggTableData.interval = ReportingFactory.getProperty($scope.reportConfig.report.timeInterval, ReportingFactory.timeIntervals, 'value', 'label');
			}
			
			// TODO: Copy the object?!!?
			// XXX: Object.keys() is a workaround because when performing a facet search for a 
			// date field, Solr returns 'edOn' instead of 'modifiedOn' !!!
			var correctValues = ReportingFactory.getCorrectData(data)[0];
			aggTableData.data = correctValues[Object.keys(correctValues)[0]];
			_.forEach(aggTableData.data, function (val) {
				val.name = ReportingFactory.formatTickLabel($scope.reportConfig.report, val.name);
			});
			
			return aggTableData;
		};		
	}]);
	
	
	/**
	 * Controller for the additional filter form in EMF Reporting. Contains functionality for building an object 
	 * that is used in the template to build the filters form. Depends on ReportingFactory for property extraction. 
	 * When the button for filtering is clicked, all filter fields are iterated and any entered text is collected.
	 */
	module.controller('ReportingAdditionalFiltersController', ['$scope', 'ReportingFactory', function ($scope, ReportingFactory) {
		
		/* Used in the template to build the form. */
		$scope.filterProperties = [];

		/**
		 * Updates the array with properties to filter by, according to what 
		 * the user has selected from the additional filters tab. 
		 */
		//TODO: Test it after fixing the title
		$scope.updateFilterProperties = function () {
			$scope.filterProperties = [];
			_.each($scope.reportConfig.report.additionalFilters, function (filter) {
				var title = $scope.getTitle(filter);
				$scope.filterProperties.push({name: filter, title: title});
			});
			$scope.filterProperties = _.uniq($scope.filterProperties);
		};
		
		/** 
		 * Extracts the title of a filter property. 
		 * 
		 * @param title - 
		 * 				the filter's id
		 * @return the title 
		 */
		//TODO: Test it after fixing the title
		$scope.getTitle = function (id) {
			var title = {};
			_.forEach($scope.reportConfig.availableProperties, function (group) {
				var prop = _.find(group.properties, {'name' : id});
				if(!_.isEmpty(prop)) {
					title = prop.title;
					return false;
				}
			});
			return title;
		};
		
		/**
		 * Iterates over all filter fields within the given parent element and builds an array of objects containing
		 * the property names and the values to filter by, which has the following format: 
		 * [{property: 'title', value: 'test project'}, {property: 'description', value: 'some description'}]
		 * 
		 * @param parentElement - 
		 * 						the given parent element with filters
		 * @return the collected filters
		 */
		//TODO: Maybe in the directive?
		$scope.buildFilters = function(parentElement) {
			var filters = [];
			var allFilterFields = parentElement.find(".filter-textbox");
			$.each(allFilterFields, function(index, value) {
				  if($(value).val().length > 0) {
					  filters.push({property: $(value).attr("name"), value: $(value).val()});
				  }
			});
			return filters;
		};
	}]);

	
	/**
	 * Controller for Flot.js charts in EMF Reporting. Depends on ReportingFactory for property extraction and tick labels formatting. 
	 */
	module.controller('ReportingChartController', ['$scope', 'ReportingFactory', function($scope, ReportingFactory) {
	
		/**
		 * Checks the service response's status code and data. If some of them 
		 * are not correct the result will be false, otherwise true.
		 * 
		 * @param serviceData - 
		 * 					the service response
		 * @return true if correct or false otherwise
		 */
		//TODO: Move it in the main directive's controller ?!
		$scope.checkServiceDataStatus = function (serviceData) {
			return ReportingFactory.checkServiceDataStatus(serviceData);
		};

		/**
		 * Plots the chart, given a data object (a server faceted response), and 
		 * a DOM element where the chart will be plotted.
		 * 
		 * @param data
		 * 			 	is a server response object containing facetFields and facetDates that will be used for plotting the chart
		 * @param element 
		 * 			 	is the DOM element where the chart will be plotted
		 */
		$scope.drawChart = function(data, element) {
			var chartType = $scope.reportConfig.report.type;
			var chartElement = element.find('.reporting-chart-container');
			
			chartElement.html("");
			var maxVertical = 0;
			//if the server returns any facet dates, use them for the chart (the user has selected grouping by time interval)
			//otherwise, use the regular faceting with facet fields
			var correctData = ReportingFactory.getCorrectData(data);
			// XXX: Object.keys() is a workaround because when performing a facet search for a 
			// date field, Solr returns 'edOn' instead of 'modifiedOn' !!!
			var correctValues = correctData[0];
			var values = correctValues[Object.keys(correctValues)[0]];
			if(_.isEmpty(values)) {
				$scope.show = false;
				return;
			}

			var barXPosition = 0;
			var chartData = [];
			var lineData = [];
			var ticks = [];
			
			//TODO: REFACTOR
			$.each(values, function(index, item) {
				if (chartType === "piechart") {
					chartData.push({label:  ReportingFactory.formatTickLabel($scope.reportConfig.report, item.name), data: item.count});
				} else if (chartType === "barchart") {
					var tickLabel = ReportingFactory.formatTickLabel($scope.reportConfig.report, item.name);
					chartData.push({label: tickLabel, data: [[barXPosition, item.count]]});
					ticks.push([barXPosition+0.25, tickLabel]); 
					barXPosition += 2;
					if (item.count > maxVertical) {
						maxVertical = item.count;
					}
				} else if (chartType === "linechart") {
					lineData.push([barXPosition, item.count]);
					barXPosition += 2;
					var tickLabel = ReportingFactory.formatTickLabel($scope.reportConfig.report, item.name);
					ticks.push([barXPosition-2, tickLabel]);
					if (item.count > maxVertical) {
						maxVertical = item.count;
					}
				}
			});	
			//the line chart needs only one data series (a simple 1-dimens. array) to be pushed into the chartData.
			//Also, the chartTicks are saved in the scope config, so that the line chart 'plothover' handler can get the 
			//most recent ones. (it doesn't work if they are passed as a parameter to the bindTooltip function)
			if (chartType === "linechart") {
				chartData.push({data: lineData});
				$scope.reportConfig.report.chartTicks = ticks;
			}			
			var chartOptions = $scope.getChartOptions(chartType, barXPosition, maxVertical, ticks, element);
			// TODO: Refactor to pass configuration ?
			chartElement.height(500);
			chartElement.width(500);
			$.plot(chartElement, chartData, chartOptions);
			//attach hover function to the chart nodes/series, showing a tool tip with the label and count
			$scope.bindTooltip(element.find(".chart-tooltip"), chartElement);
		};
		
		/**
		 * Binds a tool tip for the provided chart element that shows information about the selected part 
		 * of the chart. The tool tip is shown at specific DOM element. 
		 * 
		 * @param tooltipElement - 
		 * 						the element where the tool tip is located
		 * @param chartElement -
		 * 						the element where the chart is drawn
		 */
		$scope.bindTooltip = function(tooltipElement, chartElement) {
			// Clearing any previous tool tips.
			tooltipElement.html('');
			chartElement.bind("plothover", function (event, pos, obj) {
				if (!obj) {
			        return;
				}
				var label = "";
				var count = "";
				if($scope.reportConfig.report.type !== 'linechart') {
					//all charts except the line chart take the tooltip labels from the 
					//data series object passed to the function
					label = obj.series.label;
					count = obj.series.data[0][1];
				} else {
					//the line chart takes the tooltip labels from the chartTicks array 
					//which is stored in the scope config
					label = $scope.reportConfig.report.chartTicks[obj.dataIndex][1];
					count = obj.series.data[obj.dataIndex][1]
				}
			    $(tooltipElement).html("");
			    jQuery('<span/>', {
			    	text: label + " (" + count + ")",
			    	css: {
			            fontWeight: 'bold',
			            color: obj.series.color
			        }
				}).appendTo(tooltipElement);
			});
		};
		
		/**
		 * Returns the configuration options needed for drawing the chart. Adjusts them according to the given 
		 * max X and Y value of the bars/lines in the chart.
		 * The max horizontal/vertical parameters may be null when drawing a pie chart.
		 * The ticks array contains the labels drawn under each bar/point and their coordinates.
		 */
		$scope.getChartOptions = function(chartType, maxHorizontal, maxVertical, ticks, element) {
			//define the common properties
			var xaxis = { zoomRange: null, panRange: [0, maxHorizontal], ticks: ticks };
			var yaxis =  {
					 zoomRange: [maxVertical, maxVertical], 
					 panRange: [0,  maxVertical+1],
					 minTickSize: 1,
					//a simple workaround for placing a label on the top of the Y axis
					tickFormatter: function(val, axis) { 
						return val < axis.max ? val : "count";
					}
				};
			var grid = { hoverable: true };
			var zoom = { interactive: true };
			var pan  = { interactive: true, frameRate: 40 };
			
			if(chartType === "barchart") {
				return	{
					bars: {
						show	: true,
						barWidth: 0.5
					},
					legend: {
						show		: $scope.reportConfig.report.chartLegend,
			        	margin		: [-130, 0],
		                position	: "ne",
		                container	: element.find(".legend"),
		                noColumns	: 3
		            },
					xaxis	: xaxis,
					yaxis	: yaxis,
					grid	: grid, 
			        zoom	: zoom,
			        pan		: pan 
				};
			} else if(chartType === "piechart") {
				return {
				    series: {
				        pie: {
				            show	: true,
				            radius	: 0.9,
				            offset: {
				            	top	: -24,
				            	left: -24
				            }
				        }
				    },
				    legend	: {
				    	show		: $scope.reportConfig.report.chartLegend,
			        	margin		: [-105, 0],
		                position	: "se",
		                container	: element.find(".legend"),
		                noColumns	: 3
		            },
			        grid: grid
				};
			} else if(chartType === "linechart") {
				return {
					series: {
						lines: {
							show: true,
							fill: true
						},
			            points: {
			                radius	: 3,
			                show	: true,
			                fill	: true
			            }
			        },
					xaxis	: xaxis,
					yaxis	: yaxis,
					grid	: grid,
					zoom	: zoom,
				    pan		: pan
				};
			}
		};
	}]);
	
	/**
	 * Controller for the Raw Table in EMF Reporting. Initializes the columns and fields of the table and performs filtering on its ExtJS data store.
	 */
	//TODO: Richer java doc
	module.controller('ReportingRawTableController', ['$scope', '$q', 'CodelistService', 'ReportingFactory', function ($scope, $q, CodelistService, ReportingFactory) {

		/** 
		 * Extracts the type of a filter property. 
		 * 
		 * @param title - 
		 * 				the filter's id
		 * @return the type of the given property
		 */
		$scope.getPropertyType = function (id) {
			var type = {};
			_.forEach($scope.reportConfig.availableProperties, function (group) {
				type = ReportingFactory.getProperty(id, group.properties, 'name', 'editType');
				return false;
			});
			return type;
		};
		
		/**
		 * Given an array of field names, generates an array of field objects for 
		 * the ExtJs memory store, having the following format:
		 * [{name: 'title', mapping: 'title'}, 
		 * {name: 'modifiedOn', mapping: 'modifiedOn', type: "date", format: "Y-m-dTH:i:s.uuuP"}]
		 * where the field name and mapping are identical.
		 * 
		 * @param - array with fields names
		 * @return the generated data store fields object
		 */
		$scope.generateDataStoreFields = function(fields) {
			var storeFields = [];
			_.each(fields, function(field) {
				var propertyType = $scope.getPropertyType(field); 
				if (propertyType === 'date' || propertyType === 'datetime' || propertyType === 'dateTime') {
					storeFields.push({name: field, mapping: field, type: 'date', format: SF.config.dateFormatPattern});
				} else {
					storeFields.push({name: field, mapping: field});
				}
			});
			return storeFields;
		};
		
		/**
		 * Filters the results in the raw table according to the given array of filters, where each one is an object containing
		 * the property names and the values to filter by. 
		 * example: [{property: 'title', value: 'test project'}, {property: 'description', value: 'some description'}]
		 */
		$scope.filterTable = function (userFilters) {
			//the filter fields also have to be loaded in the ExtJs memory store for the filtering to work, so make a union between them and the 
			//fields already supposed to be loaded
			var searchConfig = $scope.reportConfig.searchCriteria;
			var allFieldsToLoadInStore = _.union(searchConfig.fields, ReportingFactory.extractProperties(userFilters, 'property'));
			$scope.searchFields = allFieldsToLoadInStore;
			//force the store to load all the needed fields
			$scope.fields = $scope.generateDataStoreFields(allFieldsToLoadInStore);
			$scope.rebuild();
			
			$scope.rawTableStore.clearFilter();
			var filters = [];
			if (userFilters.length === 0) {
				return;
			}
			//build the array of Ext.util.Filter that will be applied to the extjs data store
			_.forEach(userFilters, function (filter) {
				filters.push(new Ext.util.Filter({
					  property: filter.property, 
					  value: filter.value, 
					  anyMatch: true,
					  caseSensitive: false,
					  root: 'data'
				 }));
			});
			$scope.rawTableStore.filter(filters);
		};
		
		/**
		 * 
		 */
		$scope.initialize = function () {
			$scope.fields = [ ];
			$scope.columns = [ ];
			$scope.searchFields = [ ];
			
			$scope.count = 0;
			$scope.order = [];
		
			//build the array of Ext.util.Filter that will be applied to the extjs data store
			_.forEach($scope.userSelectedFilters, function(filter) {
				$scope.filters.push(new Ext.util.Filter({
					  property: filter.property, 
					  value: filter.value, 
					  anyMatch: true,
					  caseSensitive: false,
					  root: 'data'
				 }));
			});	
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
			// TODO: Should this be fixed in the widget's configuration controller ?
			var uniqProperties = _.uniq($scope.reportConfig.selectedProperties, function(property) {
				return property.name;
			});
			
			// iterate the selected properties, if any of them are from
			// codelists - cache them and fire event when ready
			_.forEach(uniqProperties, function(property) {
				$scope.searchFields.push(property.name);

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
						if (typeof value === 'object' && typeof value.label !== 'undefined') {
	                		return value.label;
	                	}
						
	                    return value;
	                },
	                listeners : {
	                	move : function(a, x, y, eOpts ) {
		                	  var data = {
		               			  checked 	: true, 
		               			  editType	: this.initialConfig.editType, 
		               			  editable	: this.initialConfig.editable, 
		                		  name		: this.initialConfig.dataIndex, 
		                		  title		: this.initialConfig.header,
		                		  codelistNumber : this.initialConfig.codelistNumber
		                	  };
		                
		                	  $scope.count ++;
		                	  $scope.order.push(data);
		                	  if ($scope.count === $scope.columns.length - 1) {
		                		  $scope.reportConfig.selectedProperties = $scope.order;
		                		  // FIXME This is wrong here...
		                		  var value = _.escape(JSON.stringify($scope.reportConfig));
		                		  $scope.widget.closest(".widget-reporting").attr('data-config', value);
		                		  $scope.count = 0;
		                		  $scope.order = [];
		                	  }
	                   }
	               }
				};

				if (property.editType === 'date' || property.editType === 'datetime' || property.editType === 'dateTime') {
					$scope.fields.push({ name: property.name, mapping: property.name, type: 'date', format: SF.config.dateFormatPattern});
				} else {
					$scope.fields.push({ name: property.name, mapping: property.name });
				}
				
				$scope.columns.push(col);
				
				// if a given codelist is not cached yet, make a request for the
				// values
				// and store the promise, it will be resolved with the rest
				// later on and values will be cached
				if (property.codelistNumber && 
						!EMF.cache.codelists[property.codelistNumber] && !codelistPromises[property.codelistNumber]) {
					
					var codelistPromise = CodelistService.loadCodelist(property.codelistNumber);
					codelistPromises[property.codelistNumber] = codelistPromise;
				}
			});
			
			/* If there are promises for code list resolve them and cache the values. */
			if (_.keys(codelistPromises).length > 0) {
				$q.all(codelistPromises).then(function(results) {
					_.forIn(results, function(value, clNumber) {
						_.forEach(value.data, function(codeValue) {
							var cached = EMF.cache.codelists[clNumber];
							if (!cached) {
								cached = { };
								EMF.cache.codelists[clNumber] = cached;
							}
							cached[codeValue.value] = codeValue.label;
						});
					});
				});
			}
		};
		
	}]);
	
}());