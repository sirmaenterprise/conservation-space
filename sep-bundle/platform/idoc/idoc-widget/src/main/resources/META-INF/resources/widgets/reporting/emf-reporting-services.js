(function() {
	'use strict';

	/**
	 * AngularJS module for all filters, services and factories used in the
	 * reporting functionality.
	 * 
	 * @author Mihail Radkov
	 * @author Vilizar Tsonev
	 */
	var module = angular.module('emfReportingServices', []);

	/**
	 * Filter used to slice an array into parts having the specified length.
	 * Example: If the array is [1,2,3,4,5] and the size is 3 the result would
	 * be [[1,2,3], [4,5]]
	 */
	// TODO: Do we need the cache?
	module.filter('partition', function($cacheFactory) {
		var arrayCache = $cacheFactory('partition');

		var filter = function(arr, size) {
			if (!arr) {
				return;
			}
			var newArr = [];
			for ( var i = 0; i < arr.length; i += size) {
				newArr.push(arr.slice(i, i + size));
			}
			var cachedParts;
			var arrString = JSON.stringify(arr);
			cachedParts = arrayCache.get(arrString + size);
			if (JSON.stringify(cachedParts) === JSON.stringify(newArr)) {
				return cachedParts;
			}
			arrayCache.put(arrString + size, newArr);
			return newArr;
		};

		return filter;
	});
	
	//	TODO: Unite all services into ReportingServices ?!
	
	/**
	 * Factory for retrieving the common semantic properties of specific types.
	 */
	// TODO: Factory or service ??!
	// http://stackoverflow.com/questions/15666048/service-vs-provider-vs-factory?lq=1
	module.factory('SemanticPropertiesService', [ '$http', function($http) {
		var service = {};
		
		/**
		 * Retrieves only the common properties between specific semantic types.
		 * 
		 * @param types -
		 *            array of specific semantic types
		 * @returns a http promise
		 */
		service.loadCommonSemanticProperties = function(types) {
			var config = {};
			if (types) {
				config.params = {};
				config.params.commonOnly 	= true;
				config.params.multiValued	= false;
			}
			if (!_.isEmpty(types)){
				config.params.forType 		= types.join(',');
			}
			// Declared in emf-reporting.js
			return $http.get(EMF.reporting.propertiesServicePath, config);
		};
		
		return service;
	} ]);
	
	/**
	 * Factory for executing faceted searches and utility methods for creating the required configuration.
	 */
	// TODO: Factory or service ??!
	module.factory('FacetedSearchService', [ '$http', function($http) {
		var service = {};

		/**
		 * Builds a default configuration for the faceted search. Initializes query parameters, 
		 * Solr's start, end, gap parameters and the facet field.
		 */
		//TODO: Pass only report. ...
		service.buildFacetedSearchConfig = function (reportConfig, searchConfig) {
			var params = {};
			params.q 			= "*:*";
			params.multiValued 	= false;
			params.includePrefix= false;
			params.field 		= "_sort_" + reportConfig.report.groupBy;
			if(reportConfig.report.groupByType === 'tdates') {
				params.datefield 	= reportConfig.report.groupBy;
				params.gap 			= reportConfig.report.timeInterval;
				params.start 		= service.buildFacetStartDate(searchConfig.createdFromDate);
				params.end 			= service.buildFacetEndDate(searchConfig.createdToDate);
			}
			return { params:params };
		};
		
		
		/**
		 * Provided a start date (as a simple date-picker string), builds a solr facet 
		 * start date parameter.
		 */
		service.buildFacetStartDate = function (from) {
			// TODO: Why are we still setting 31 days back?
			var facetStart = "NOW/DAY-31DAYS";
			if (!_.isEmpty(from)) {
				var dateFrom = service.getDateFromSimpleString(from);
				var daysBack = service.getDifferenceInDays(dateFrom, new Date());
				if (daysBack < 0) {
					facetStart = "NOW/DAY" + daysBack + "DAYS";
				} else {
					facetStart = "NOW/DAY+" + daysBack + "DAYS";
				}
			}
			return facetStart;
		};
		
		/**
		 * Provided an end date (as a simple date-picker string), builds a solr facet 
		 * end date parameter.
		 */
		service.buildFacetEndDate = function (to) {
			var facetEnd = "NOW/HOUR+1DAYS";
			if (!_.isEmpty(to)) {
				var dateTo = service.getDateFromSimpleString(to);
				var daysBack = service.getDifferenceInDays(dateTo, new Date());
				if (daysBack < 0) {
					facetEnd = "NOW/HOUR" + daysBack + "DAYS";
				} else {
					facetEnd = "NOW/HOUR+" + (daysBack + 1) + "DAYS";
				}
			}
			return facetEnd;
		};
		
		/**
		 * Gets the date format pattern from EMF and parses the provided
		 * date string to a date object by using DatePicker's
		 * functionality for parsing dates.
		 * 
		 * @param dateString -
		 *            the provided date string
		 * @returns the parsed string as Date object
		 */
		service.getDateFromSimpleString = function (dateString) {
			var pattern = SF.config.dateFormatPattern;
			return $.datepicker.parseDate(pattern, dateString);
		};

		/**
		 * Gets the difference in days between the given two dates.
		 * 
		 * @param date1 -
		 *            the first date
		 * @param date2 -
		 *            the second date
		 * @returns the difference in days
		 */
		service.getDifferenceInDays = function(date1, date2) {
			var timeDiff = date1.getTime() - date2.getTime();
			return Math.ceil(timeDiff / (1000 * 3600 * 24));
		};
		
		/**
		 * Builds Solr filter query for EMF's URIs from the provided data. 
		 * The provided data should contain dbId field.
		 * Example: '(uri:1-2-3 OR uri:6-7-8)' 
		 * 
		 * @param data
		 *            the provided data
		 * @returns {String} the builded Solr query
		 */	
		service.buildUriFilters = function (data) {
			var filters = "(";
			_.each(data, function(item) {
				if(filters.length > 1) {
					filters += " OR uri:\"" + item.dbId + "\"";
				} else {
					filters += "uri:\"" + item.dbId + "\"";
				}
			});
			return filters += ")";
		};

		/**
		 * Executes a faceted search with the provided data and
		 * configuration. The data will be stringified with JSON.stringify().
		 *  
		 * @param data -
		 *            the provided data
		 * @param config -
		 *            the provided configuration formatted for AngularJS
		 *            HTTP
		 * @returns a http promise
		 */
		service.facetedSearch = function (data, config) {
			var path = EMF.reporting.facetedSearchPath;
			var stringifiedData = JSON.stringify(data);
			return $http.post(path, stringifiedData, config);
		};
		
		return service;
	} ]);
	
	/**
	 * AngularJS factory that provides a service for searching for objects by their IDs.
	 */
	// TODO: Factory or service ??!
	module.factory('ObjectSearchService', [ '$http', function($http) {
		var service = {};

		/**
		 * Executes a POST method to the objects rest service to retrieve
		 * objects by their identity.
		 * 
		 * @param data -
		 *            the objects' identity
		 * @returns a http promise
		 */
		service.searchByIdentity = function (data) {
			var url = EMF.reporting.objectServicePath + "/objectsByIdentity";
			return $http.post(url, JSON.stringify(data));
		};
		
		return service;
	} ]);

	/**
	 * A factory providing common utility methods used by all reporting modules.
	 */
	//TODO: Rearrange the methods.
	module.factory('ReportingFactory', ['$http', 'FacetedSearchService', function($http, FacetedSearchService) {
		/** First, the overture. */
		var factory = {};
		
		//TODO: Move them to the emf-reporting-controllers reportingController ?!
		
		/**
		 * Builds a basic search configuration from the provided configurations and 
		 * filters and then makes a http GET call to the basic search service.
		 * 
		 * @param searchConfig - the search configuration
		 * @param filters -
		 * 				an array of additional filters to be applied to the objects for which 
		 * 				aggregated data will be retrieved. Has the following format:  
		 * 				[{property: 'title', value: 'test project'}, {property: 'description', value: 'some description'}]
		 * 				If no additional filtering is needed, just pass an empty array []
		 * @return an AngularJS http promise from the basic search service
		 */
		//TODO: Test?!
		factory.getBasicSearchPromise = function(searchConfig, filters) {
			var objectFieldsToReturn = [];
			if(!_.isEmpty(filters)) {
				objectFieldsToReturn = factory.extractProperties(filters, 'property');
			}
			var basicSearchConf = {		
					initialSearchArgs: angular.extend(searchConfig, { fields: objectFieldsToReturn, pageSize: 1000 })
			};
			//TODO: Extract the basic search in separate service?!
			var basicSearch =  $({ }).basicSearch(basicSearchConf);
			return $http.get(basicSearch.basicSearch('buildSearchUrl'));
		};
		
		/**
		 * Calls the faceted search service with the provided configurations and basic search result 
		 * data. Builds a Solr filter query from the provided data's database IDs.
		 * 
		 * @param reportConfig - the report's configuration
		 * @param searchConfig - the search configuration
		 * @param data - returned data from a basic search
		 * @return an AngularJS http promise from the faceted search service
		 */
		factory.getFacetedSearchPromise = function (reportConfig, searchConfig, data) {
			var facetedConfig = FacetedSearchService.buildFacetedSearchConfig(reportConfig, searchConfig);
			var uriFilters = FacetedSearchService.buildUriFilters(data);
			return FacetedSearchService.facetedSearch({filter:[uriFilters]}, facetedConfig);
		};
		
		/**
		 * Checks the service response's status code and data. If some of them 
		 * are not correct the result will be false, otherwise true.
		 * 
		 * @param serviceData - 
		 * 					the service response
		 * @return true if correct or false otherwise
		 */
		factory.checkServiceDataStatus = function (serviceData) {
			if(typeof serviceData === 'undefined') {
				return false;
			}
			if(serviceData.status !== 200) {
				return false;
			}
			if(_.isEmpty(serviceData.data)) {
				return false;
			}
			if(!(serviceData.data.facetDates || serviceData.data.facetFields)){
				return false;
			}
			return true;
		};
		
		/**
		 * If the data object contains any facet dates (their length is >0), returns them, otherwise returns 
		 * the facet fields array. That's required because the grouping by date has a 
		 * higher priority than the regular solr faceting.
		 * 
		 * @param data -
		 *            the report's data object with facetFields and facetDates
		 * @return the correct facet array
		 */
		//TODO: Copy the object?!!?			
		factory.getCorrectData = function (data) {
			if(data.facetDates.length > 0) {
				// If the result is NOT aggregated by time
				return data.facetDates;
			} else if (data.facetFields.length > 0) {
				// If the result IS aggregated by time
				return data.facetFields;
			}
		};
		
		/**
		 * Filters the given array of objects according to the given filters.
		 * The filtering is performed as a case-insensitive 'contains' match is
		 * applied to the certain object properties for filtering.
		 * 
		 * @param objectArray 
		 * 					is the array of emf objects (returned by the searches), to be filtered 
		 * @param filters 
		 * 				is the array of filters to be applied to the objects array.
		 * 		  		example: [{property: 'title', value: 'test project'}, {property: 'description', value: 'some description'}]
		 */
		factory.filterObjectsArray = function(objectsArray, filters) {
			var filteredVals = _.filter(objectsArray, function(object) {
				var shouldStay = true;
				_.each(filters, function(filter) {
					var objectProperty = object[filter.property];
					//if the object doesn't have such a field, or the field doesn't contain the filter value, remove it
					if(_.isEmpty(objectProperty) || (objectProperty.toLowerCase().indexOf(filter.value.toLowerCase()) == -1)) {
						shouldStay = false;
					}
				});
				return shouldStay;
			});
			return filteredVals;
		};
		
		/**
		 * Extracts a field from provided array of objects by comparing a given
		 * value with the objects' fields structure and if founded then returns
		 * specified object's field.
		 * 
		 * Example: If we have the following array
		 * [{type:'1',value:'one'},{type:'2',value:'two'},{type:'3',value:'three'}]
		 * and call the function like getProperty('3',array,'type','value') the
		 * result will be "three"
		 * 
		 * @param value -
		 *            the value for comparison
		 * @param array -
		 *            the array of objects
		 * @param source -
		 *            the field to compare with
		 * @param dest -
		 *            the specified field for extracting
		 * @returns the extracted field
		 */
		//TODO: More tests.
		factory.getProperty = function(value, array, source, dest) {
			var result = {};
			//TODO: Add checks ?!
			_.forEach(array, function(val) {
				if (value === val[source]) {
					result = val[dest];
					return false;
				}
			});
			return result;
		};

		/**
		 * Iterates an array and extracts properties from it by specified field.
		 * 
		 * Example: If the array is
		 * [{value:'avada',title:'AvadaKadavra'},{value:'imper',title:'Imperius'}]
		 * and we call the function like extractProperties(array,'value') the
		 * result will be ['avada','imper']
		 * 
		 * @param array -
		 *            the array of elements
		 * @param field -
		 *            the field to be extracted
		 * @return {Array} - an array with the extracted properties
		 */
		//TODO: Rename it to extractFields ?
		factory.extractProperties = function(array, field) {
			var properties = [];
			//TODO: Checks?!
			_.each(array, function(element) {
				properties.push(element[field]);
			});
			return properties;
		};

		/**
		 * Formats the tick label to be suitable for displaying it to the user.
		 * For example, if the user groups by a date field, the label is formatted to be a readable date in 
		 * format dd/MM/yyyy hh:mm.
		 * If the label is an ontology URI, extracts only the field's name at the end of the URI.
		 * 
		 * @param config
		 * 				the widget's configuration, needed to get the groupBy fields
		 * @param tickLabel 
		 * 				the tick label in raw format, as returned by the server
		 * @returns 
		 * 		   the tick label, formatted to be readable by the final user
		 * 		
		 */
		factory.formatTickLabel = function (config, tickLabel) {
			if(_.contains([config.groupByType, config.addGroupByType], "tdates")) {
				return factory.formatTickDate(tickLabel, config.timeInterval);
			} else {
				return factory.parseOntologyURI(tickLabel);
			}
		};
		
		/**
		 * Formats the given ISO date string, according to the given time interval. 
		 * For example, if the interval is given in years or months, the days and 
		 * hours are not included in the date string. If the interval is given in 
		 * hours, everything from year to minute is formatted and included in the 
		 * result string.
		 * Example:
		 * The ISO date: 2014-09-29T10:00:00Z, when grouping by hour (+1HOUR) will
		 * be formatted like: 29/9/2014 10:00
		 * 
		 * 
		 * @param isoDate - the given date string in ISO 8601 format
		 * @param timeInterval - the time interval in Solr format
		 * @return a slash-separated, formatted date string suitable for tick labels
		 */
		factory.formatTickDate = function (isoDate, timeInterval) {
			var date = new Date(isoDate);
			if (timeInterval.indexOf("12MONTH") > -1) {
				return date.getFullYear();
			} else if (timeInterval.indexOf("1MONTH") > -1) {
				return (date.getMonth() + 1);
			} else if (timeInterval.indexOf("7DAY") > -1) {
			    date.setHours(0,0,0);
			    // Set to nearest Thursday: current date + 4 - current day number
			    // Make Sunday's day number 7
			    date.setDate(date.getDate() + 4 - (date.getDay() || 7));
			    // Get first day of year
			    var yearStart = new Date(date.getFullYear(), 0, 1);
			    // Calculate full weeks to nearest Thursday
			    var weekNo = Math.ceil((((date - yearStart) / 86400000) + 1) / 7);
			    // Return array of year and week number
			    return [date.getFullYear(), weekNo];
			} else if (timeInterval.indexOf("1DAY") > -1) {
				return date.getUTCDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear();
			} else if (timeInterval.indexOf("1HOUR") > -1) {
				return date.getUTCDate() + "/" + (date.getMonth() + 1) + "/" + 
				+ date.getFullYear() + " " + (date.getUTCHours()) + ":" + date.getMinutes();
			}
		};
		
		/**
		 * Extracts the exact field name from the Ontology URI string.
		 * Example:
		 * The username is given in format: http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin
		 * Which will result in just 'admin'
		 * 
		 * @param uri -
		 *            the given Ontology string
		 * @returns 
		 * 			  the exact name extracted from the ontology URI
		 */
		factory.parseOntologyURI = function (uri) {
			var name = uri;
			if (uri.indexOf("/") > -1) {
				name = uri.substr(uri.lastIndexOf("/")+1);
				if (name.indexOf("#") > -1) {
					name = name.substr(name.lastIndexOf("#")+1);
				}
			}
			return name;
		};

		// TODO: Move to emf-reporting.js ?!
		/** All available report types. */
		factory.reportTypes = [
			 { value:'aggtable', 	label:_emfLabels["widget.reporting.aggtable"] },
			 { value:'rawtable', 	label:_emfLabels["widget.reporting.rawtable"] },
			 { value:'piechart', 	label:_emfLabels["widget.reporting.piechart"] },
			 { value:'barchart', 	label:_emfLabels["widget.reporting.barchart"] },
			 { value:'linechart',	label:_emfLabels["widget.reporting.linechart"] }];

		// TODO: Move to emf-reporting.js ?!
		/** Time intervals. */
		factory.timeIntervals = [
			{ value:'+12MONTH',	label:_emfLabels["widget.reporting.timeinterval.year"] },
			{ value:'+1MONTH', 	label:_emfLabels["widget.reporting.timeinterval.month"] },
			{ value:'+7DAY', 	label:_emfLabels["widget.reporting.timeinterval.week"] },
			{ value:'+1DAY', 	label:_emfLabels["widget.reporting.timeinterval.day"] },
			{ value:'+1HOUR', 	label:_emfLabels["widget.reporting.timeinterval.hour"] }];

		/** Creates the default report configuration. */
		factory.defaultReportConfig = function() {
			var conf = { };
			conf.type 				= 'piechart',
			conf.groupBy			= 'modifiedBy',
			conf.addGroupBy 		= '',
			// Use only groupBy & addGroupBy ?! and extract the types dynamically ?
			conf.groupByType 		= '',
			conf.addGroupByType		= '',
			conf.timeInterval		= '+1DAY',
			conf.aggregatedTable	= false,
			conf.additionalFilters	= '',
			conf.chartLegend 		= true;
			return conf;
		};

		/** And here comes the crescendo... */
		return factory;
	} ]);

}());