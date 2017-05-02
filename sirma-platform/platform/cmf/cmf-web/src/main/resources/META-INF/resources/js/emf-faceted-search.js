;(function() {
	'use strict';

	/** Creating a namespace for the faceted search. */
	EMF = EMF || {};
	EMF.search = EMF.search || {};
	EMF.search.faceted = EMF.search.faceted || {};
	EMF.search.faceted.date = {
		UNSPECIFIED : '*',
		SEPARATOR: ';',
		BEFORE: "before",
		AFTER: "after",
		BETWEEN: "between"
	};

	/** Cache for labels. */
	EMF.cache = EMF.cache || {};
	EMF.cache.labels = EMF.cache.labels || [];

	/** Facets' events constants. */
	EMF.search.faceted.DATA_UPDATED 		   = 'facets:data-updated';
	EMF.search.faceted.SELECTION_UPDATED	   = 'facets:selection-updated';
	EMF.search.faceted.LOAD_FACET_VALUES	   = 'facets:load-facet-values';
	EMF.search.faceted.AVAILABLE_FACETS_LOADED = 'facets:available-facets-loaded';
	EMF.search.faceted.START_FACET_LOADING     = 'facets:start-facet-loading';

	/** Sort constants. */
	EMF.search.faceted.sort= {
			ALPHABETICAL:'alphabetical',
			CHRONOLOGICAL:'chronological',
			MATCH:'match'
	};

	EMF.search.faceted.sort.order = {
			ASCENDING:'ascending',
			DESCENDING:'descending'
	};

	EMF.search.faceted.state = {
			EXPANDED:'expanded',
			COLLAPSED:'collapsed'
	};

	/**
	 * The AngularJS module for the Search Facets functionality in SEIP. To be able to use this module there
	 * are four conditions:
	 *  1) Attach 'facetsListener' directive to the basic search component
	 * 	2) Declare the 'facets' directive inside the basic search
	 * 	3) Bootstrap the module
	 * 	4) The search should be 'main' - no object pickers or widgets
	 * 
	 * @author Mihail Radkov
	 * @author Iskren Borisov
	 */
	var facetsModule = angular.module('facets', []);

	/**
	 * Configures the facets module via AngularJS providers.
	 */
	facetsModule.config(function($logProvider) {
		  $logProvider.debugEnabled(EMF.config.debugEnabled);
	});

	/**
	 * A mediator between the basic search and the search facets. Capsulates the mix up between
	 * jQuery and AngularJS events.
	 */
	facetsModule.directive('facetsListener', ['$http', function ($http) {
		return {
			restrict: 'A',
			link: function (scope, element) { //NOSONAR

				/*
				 * Constants for the basic search events. Sadly, obtaining them via
				 * basicSearch.options.eventNames is not possible when the search is opened with a
				 * context - it is not yet initialized unlike when it's opened normally.
				 */
				var BEFORE_SEARCH			= 'before-search';
				var PERFORM_SEARCH			= 'perform-basic-search';
				var REMOVE_EXTRA_ARGUMENTS	= 'basic-search:remove-extra-arguments';

				/**
				 * Enables or disables the facets based on if the search is a 'main' search - this
				 * means that the search is initialized as part of a page and not in a widget or
				 * object picker.
				 */
				element.on(BEFORE_SEARCH, function(event, searchArgs) {

					//replace the 'current-user' constant with the actual ID of the logged-in user
					if (searchArgs.createdBy) {
						searchArgs.createdBy = searchArgs.createdBy.map(function(value) {
							var pattern = new RegExp(EMF.search.CURRENT_USER, "g");
							return value.replace(pattern, EMF.currentUser.id);
						});
					}
					// When current context is used it have to be takenm from currentPath first
					var contextPath = EMF.currentPath;
					var replacedItems = [];
					if(searchArgs.location !== undefined){
						$.each(searchArgs.location, function(index, value) {
							switch (value) {
							case 'emf-search-context-current-project':
								if (contextPath.length && contextPath[0].type === 'projectinstance') {
									replacedItems.push(contextPath[0].id);
								}
								break;
							case 'emf-search-context-current-case':
								if (contextPath.length >= 1 && contextPath[1].type === 'caseinstance') {
									replacedItems.push(contextPath[1].id);
								}
								break;
							case 'emf-search-context-current-object':
								if (contextPath.length) {
									replacedItems.push(contextPath[contextPath.length - 1].id);
								}
								break;
							default:
								replacedItems.push(value);
								break;
							}
						});
						searchArgs.location = replacedItems;
					}

					var basicSearch = element.data('EmfBasicSearch');
					var isMainSearch = basicSearch.options.isMainSearch;
					var loadAvailableFacets = true;
					if(searchArgs.loadAvailableFacets !== undefined){
						loadAvailableFacets = searchArgs.loadAvailableFacets;
					}
					
					if(searchArgs && isMainSearch) {
						var facetArguments = angular.copy(searchArgs);
						facetArguments.facet = true;
						if (loadAvailableFacets) {
							$http.get(EMF.servicePath + "/search/facets?" + $.param(facetArguments)).then(function(response) {
								scope.message = undefined;
								scope.$broadcast(EMF.search.faceted.AVAILABLE_FACETS_LOADED, response, facetArguments);
							}, function(response) {
								scope.message = response.data;
								scope.facets = [];
							});
						} else {
							scope.$broadcast(EMF.search.faceted.AVAILABLE_FACETS_LOADED, null, facetArguments);
						}
					}
				});

				/**
				 * Listens for the event for removing any extra arguments - in this case removes the
				 * facet arguments from the search criteria. TODO: There must be some better way to
				 * do this...
				 */
				element.on(REMOVE_EXTRA_ARGUMENTS, function(event, searchArgs) {
					if(searchArgs) {
						delete searchArgs.facetArguments;
						delete searchArgs.facetField;
						scope.message = "";
						scope.$apply();
					}
				});

				/**
				 * Listens for the event when a facet is selected to notify the basic search to
				 * trigger a search with the selected facet/facets.
				 */
				scope.$on(EMF.search.faceted.SELECTION_UPDATED, function(event, data) {
					var searchArguments = {
						facet : true,
						facetArguments : data,
						facetField: []
					};
					element.trigger(PERFORM_SEARCH, searchArguments);
				});

				/**
				 * Listens for the event when a facet is selected to notify the basic search to
				 * trigger a search with the selected facet/facets.
				 */
				scope.$on(EMF.search.faceted.LOAD_FACET_VALUES, function(event, data) {
					var searchArguments = {
						facet : true,
						facetField : data,
						loadAvailableFacets : false,
						cancelSearch : true
					};
					element.trigger(PERFORM_SEARCH, searchArguments);
					element.data('EmfBasicSearch').searchArgs.loadAvailableFacets = true;
					element.data('EmfBasicSearch').searchArgs.facetField = [];
				});

			}
		};
	}]);


	/**
	 * A directive that will be the holder of all search facets in the search page.
	 */
	facetsModule.directive('facets', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			controller : 'FacetsController',
			templateUrl	: EMF.applicationPath + '/templates/facets/emf-facets.tpl.html'
		};
	}]);


	/**
	 * A directive that represents one search facet in the search facet holder.
	 */
	facetsModule.directive('facet', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			controller : 'FacetController',
			templateUrl	: EMF.applicationPath + '/templates/facets/emf-facet.tpl.html'
		};
	}]);

	/**
	 * Attribute directive for displaying a label from emf label set. TODO: This exists in the
	 * widgets. Move it in some utility module!!!
	 */
	facetsModule.directive('emfLabel', function() {
		return {
			restrict: 'A',
			link: function(scope, element, attrs) {
				element.html(window._emfLabels[attrs.emfLabel]);
			}
		};
	});

	/**
	 * The controller for the search facets. Provides functionality for grouping dates, slicing
	 * facets and restoring selections.
	 */
	facetsModule.controller('FacetsController', ['$scope', '$q', '$sce', '$log', 'FacetUtils' , '$http',  function ($scope, $q, $sce, $log, FacetUtils, $http) { //NOSONAR

		var dateRanges = FacetUtils.getDefaultDateRanges();

		/**
		 * Returns unescaped html.
		 */
		$scope.toTrusted = function(html) {
		    return $sce.trustAsHtml(html);
		};
		
		/**
		 * Called when the available facets have been loaded. Shows all available facets without
		 * their values.
		 */
		$scope.$on(EMF.search.faceted.AVAILABLE_FACETS_LOADED, function(event, response, facetArguments){ 
			var facetsAlreadyLoaded = $scope.facets !== undefined && $scope.facets.length > 0;
			var hasFacetsWithSelectedValues = facetArguments.facetArguments !== undefined && facetArguments.facetArguments.length>0;
			
			// Basically a corner case to handle facet refresh when facets have already been loaded
			// and one or more facet values have been selected.
			if( facetsAlreadyLoaded && hasFacetsWithSelectedValues && response !== null ){
				// Remove all facets that are not returned from the available facets service from the scope.
				// This is the case when we select a facet value e.g. Project from the rdf  type facet,
				// when we initially had multiple rdf types and we need to remove the ones that are
				// are not applicable to projects.
				$scope.facets = _.filter($scope.facets, function(facet) {
					return _.find(response.data, {id:facet.id});
				});
				
				// Add all facets that are new for the facets in the scope.
				// This is the case when we deselect a facet value e.g. Project from the rdf type facet,
				// when we initially had multiple rdf types, we selected one of them which removed some of the facets,
				// and then we deselected it which should add back all missing facets from before.
				$scope.facets = _.uniq(_.union($scope.facets, response.data), false, function(item) {
					return item.id;
				});
			}
			else if(response !== null){
				$scope.facets = response.data;
			} 
			
			$scope.facets = _.sortBy($scope.facets, function(facet) {
				return facet.order;
			});
			
			if (facetArguments.facetField === undefined || facetArguments.facetField.length === 0){
				//Load the data properties facets in a batch.
				$scope.loadFacets($scope.filterFacetsByPropertyType("definition", $scope.facets), facetArguments, true);
				
				//Load the object properties facets.
				$scope.loadFacets($scope.filterFacetsByPropertyType("object", $scope.facets), facetArguments, false);
			} else {
				$scope.loadFacets(_.where($scope.facets, {facets: facetArguments.facetField[0]}), facetArguments, false);
			}
			
		});
		
		/**
		 * Load the facets into the facetArguments and send request their
		 * values.
		 * 
		 * @param facets -
		 *        the facets to be loaded
		 * @param facetArguments -
		 *        the facet arguments
		 * @param loadInBatch -
		 *        whether each facet should be loaded asynchronous or all of them at once.
		 */
		$scope.loadFacets = function(facets, facetArguments, loadInBatch){
			var facetsToLoad = facetArguments.facetField || [];
			
			_.forEach(facets, function(facet) {
				if (facet.defaultState === undefined) {
					facet.defaultState = facet.state;
				}
				if (facet.defaultState === EMF.search.faceted.state.EXPANDED || FacetUtils.hasSelectedValues(facet)) {
					facetsToLoad.push(facet.id);
					facet.loading = true;
				} else {
					facet.values = [];
					facet.loaded = false;
					facet.state = facet.defaultState;
				}
			});
			
			if(!loadInBatch){
				_.forEach(facetsToLoad, function(facetToLoad){
					facetArguments.facetField = [];
					facetArguments.facetField.push(facetToLoad)
					var promise = $http.get(EMF.servicePath + "/search/faceted?" + $.param(facetArguments));
					promise.then(function(response) {
						$scope.$broadcast(EMF.search.faceted.DATA_UPDATED, response.data, $scope.facets);
					});
				});
			} else {
				facetArguments.facetField = facetsToLoad;
				var promise = $http.get(EMF.servicePath + "/search/faceted?" + $.param(facetArguments));
				promise.then(function(response) {
					$scope.$broadcast(EMF.search.faceted.DATA_UPDATED, response.data, $scope.facets);
				});
			}
			facetArguments.facetField=[];
		}
		
		/**
		 * Filter the list of facets and return only those that have the specified property type.
		 */
		$scope.filterFacetsByPropertyType = function(propertyType, facets) {
			return _.filter(facets, function(facet) {
				return facet.propertyType === propertyType;
			});
		}
		
		/**
		 * Receives search data passed from the basic search. Decides to show or hide facets based
		 * on the configuration for max result size.
		 */
		$scope.$on(EMF.search.faceted.DATA_UPDATED, function(event, data, availableFacets) {
			// Processing only if there are facets in the response. Undefined means that they were
			// not requested at all.
			if(angular.isDefined(data.facets)) {
				$scope.groupDates(data.facets);
				$scope.restoreFacetSelection(data.facets);
				$scope.sliceFacets(data.facets);
				_.forEach(data.facets, function(facet) {
					facet.loaded=true;
					facet.loading=false;
				});

				// Merge them so we can keep the initial order of the facets.
				_.map(availableFacets, function(availableFacet){
				    return _.extend(availableFacet, _.findWhere(data.facets, { id: availableFacet.id }));
				});

			}
		});

		/**
		 * Iterates the facets and if some of them are dates, it groups them into ranges.
		 * 
		 * @param facets -
		 *        the facets to iterate
		 */
		$scope.groupDates = function(facets) {
			_.forEach(facets, function(facet) {
				if(FacetUtils.isDate(facet.solrType)) {
					facet.values = $scope.groupDatesIntoRanges(facet.values);
				}
			});
		};

		/**
		 * Groups the provided facet values in date ranges. Performs a check if the current day is
		 * changed and if so - reloads the ranges. The grouping is separated in 3 steps: 
		 * 	1) Firstly, it finds the exact date range the value belongs to - it does not include the
		 * 	   count in any other ranges for now.
		 *  2) Secondly, creates a map with the current range counts. 
		 *  3) Iterates the date ranges' included ranges and adds counts for them from the map. This
		 *     is done for every date range that has count above 0.
		 *
		 * @param values -
		 *        the provided facet values to group
		 * @return the grouped facet values into date ranges
		 */
		$scope.groupDatesIntoRanges = function(values) {
			var today = FacetUtils.getMidnightDate(new Date());
			var todayGroup = _.find(dateRanges, {id: 'today'});
			// Test that!
			var currentDayChanged = today > todayGroup.start;
			if(currentDayChanged) {
				$log.debug('Current day has changed - reloading new date ranges.');
				dateRanges = FacetUtils.getDefaultDateRanges();
			}

			var ranges = angular.copy(dateRanges);

			_.forEach(values, function(value) {
				var date = new Date(value.id);
				date = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
				var found = false;
				var exactRange = null;
				var minStartDiff = Number.MAX_VALUE;
				var minEndDiff = Number.MAX_VALUE;

				// Finds the exact date range the value belongs to.
				_.forEach(ranges, function(range) {
					var noStartDate = range.start === EMF.search.faceted.date.UNSPECIFIED && (value.id === EMF.search.faceted.date.BEFORE || date <= range.end);
					var noEndDate   = range.end === EMF.search.faceted.date.UNSPECIFIED && (value.id === EMF.search.faceted.date.AFTER || date >= range.start);
					if(noStartDate || noEndDate) {
						// Special case for unspecified start range
						range.count = range.count + value.count;
						found = true;
					} else if(date >= range.start && date <= range.end) {
						/*
						 * Calculates the minimal difference between date ranges to find the exact
						 * date range, because one date could be placed in more than 1 range.
						 */
						var startDiff = Math.abs(date - range.start);
						var endDiff = Math.abs(date - range.end);
						if(startDiff <= minStartDiff && endDiff <= minEndDiff) {
							minStartDiff = startDiff;
							minEndDiff = endDiff;
							exactRange = range;
						}
					}
				});

				if(exactRange) {
					exactRange.count = exactRange.count + value.count;
				} else if (!found && value.id !== EMF.search.faceted.date.BETWEEN) {
					$log.warn('No dage range for ' + value + ' was found!');
				}

			});

			var originalCounts = [];
			_.forEach(ranges, function(range) {
				originalCounts[range.id] = range.count;
			});

			/* New array for all non empty date ranges, so the page slicing could work correctly. */
			var nonEmptyRanges = [];

			_.forEach(ranges, function(range) {
				if(range.count > 0) {
					/*
					 * Iterates the declared includedRanges to include the counts from them to the
					 * current range.
					 */
					_.forEach(range.includedRanges, function(includedRange) {
						var refferedCount = originalCounts[includedRange];

						if(angular.isDefined(refferedCount)) {
							range.count = range.count + refferedCount;
						} else {
							$log.warn('The reffered range "' + includedRange + '" was NOT found to collect the count!');
						}
					});
					nonEmptyRanges.push(range);
				}
			});

			return nonEmptyRanges;
		};

		/**
		 * The selection is returned from the server as an array in every facet.
		 * 
		 * @param facets -
		 *        the facets which selection will be restored
		 */
		$scope.restoreFacetSelection = function(facets) {
			_.forEach(facets, function(facet) {
				_.forEach(facet.selectedValues, function(selection) {
					if(FacetUtils.isDate(facet.solrType)) {
						var separatorIndex = selection.indexOf(EMF.search.faceted.date.SEPARATOR);

						var start = selection.substring(0, separatorIndex);
						var end = selection.substring(separatorIndex + 1, selection.length);

						var facetValue;
						// TODO: This causes a bug in FF-> var facetValue = _.find(facet.values,
						_.forEach(facet.values, function(value) {
							var rangeStart = value.start;
							var rangeEnd = value.end;

							if (rangeStart !== EMF.search.faceted.date.UNSPECIFIED){
								rangeStart = FacetUtils.formatDate(rangeStart);
							}
							if (rangeEnd !== EMF.search.faceted.date.UNSPECIFIED){
								rangeEnd = FacetUtils.formatDate(rangeEnd);
							}

							// TODO: I need something better... really better...
							if(rangeStart === start && rangeEnd === end) {
								facetValue = value;
								return false;
							}
						});

						if(angular.isDefined(facetValue)) {
							facetValue.selected = true;
						} else {
							$log.warn('No date facet value was matched to restore selection!');
						}
					} else {
						var facetValue = _.find(facet.values, {id : selection});
						if(angular.isDefined(facetValue)) {
							facetValue.selected = true;
						} else {
							$log.warn('No facet value was matched to restore selection!');
						}
					}
				});
			});
		};

		/**
		 * After the facets are returned, they are visualized according to their configured page
		 * size - the purpose is to achieve lazy rendering of facet values and to avoid hanging of
		 * the clients browsers.
		 * 
		 * @param facets -
		 *        the facets from the performed search
		 */
		$scope.sliceFacets = function(facets) {
			_.forEach(facets, function(facet) {
				if (facet.values !== undefined){
					if(facet.pageSize < 1) {
						facet.visibleValues = facet.values;
					} else {
						facet.visibleValues = facet.values.slice(0, facet.pageSize);
					}
				}
			});
		};

	}]);


	/**
	 * Controller dedicated for every facet group. If 'performFacetedSearch()' is assigned to
	 * 'ng-change' or just invoked, the controller will collect all selected facets and emits them
	 * to the facet mediator. Also contains logic for showing/hiding the group based on the values
	 * counts.
	 */
	facetsModule.controller('FacetController', ['$scope', 'FacetUtils', function ($scope, FacetUtils) { //NOSONAR
		var expanded = EMF.search.faceted.state.EXPANDED;
		var collapsed = EMF.search.faceted.state.COLLAPSED;

		$scope.toggleValues = function(facet) {

			if(facet.state === collapsed) {
				facet.state = expanded;
				if(!facet.loaded) {
					facet.loading=true;
					$scope.$emit(EMF.search.faceted.LOAD_FACET_VALUES, [facet.id]);
				}
			} else if (!FacetUtils.hasSelectedValues(facet)){
				facet.state = collapsed;
			}
		};

		/**
		 * Decides to show or hide a facet if none of the values have count.
		 */
		$scope.showFacetValues = function(facet) {
			if(facet.state === collapsed) {
				return false;
			}

			if(facet.values === undefined || facet.values.length === 0) {
				return false;
			}
			var notEmpty = false;
			_.forEach(facet.values, function(value) {
				if(value.count > 0) {
					notEmpty = true;
					return false;
				}
			});
			return notEmpty;
		};

		$scope.hasSelectedValues = function(facet){
			return FacetUtils.hasSelectedValues(facet);
		};
		/**
		 * Decides to show or hide the "Show next" button depending on the facet's page size and the
		 * already visualized values.
		 */
		$scope.showButton = function(facet) {
			if(facet.pageSize < 1) {
				return false;
			}
			if(!facet.visibleValues) {
				return false;
			}
			if(facet.visibleValues.length === facet.values.length) {
				return false;
			}
			return true;
		};

		/**
		 * Calculates how much values are left invisible and based on that returns how much can be
		 * shown.
		 */
		$scope.getNextValuesCount = function(facet) {
			var lengthDiff = facet.values.length - facet.visibleValues.length;
			if(facet.pageSize > lengthDiff) {
				return lengthDiff;
			} else {
				return facet.pageSize;
			}
		};

		/**
		 * Handles the rendering of the next batch of facet values depending on the facet's
		 * configuration.
		 */
		$scope.showMoreValues = function(facet) {
			var startIndex = facet.visibleValues.length;
			var endIndex = startIndex + facet.pageSize;

			var moreValues = facet.values.slice(startIndex, endIndex);
			_.forEach(moreValues, function(value) {
				facet.visibleValues.push(value);
			});
		};

		/**
		 * Throws an event to the "facetsListener" to trigger a search based on the selected facet
		 * values.
		 */
		$scope.performFacetedSearch = function() {
			var selectedFacets = $scope.getSelectedFacets($scope.facets);
			$scope.$emit(EMF.search.faceted.SELECTION_UPDATED, selectedFacets);
		};

		/**
		 * Iterates the provided facets and returns only those that are selected. Example result:
		 * ['type:gep11111', 'createdOn:*;2015-01-01T12:00:00.000Z']
		 * 
		 * @param facets -
		 *        the provided facets
		 * @return an array of the selected facets
		 */
		$scope.getSelectedFacets = function(facets) {
			var selectedFacets = [];

			_.forEach(facets, function(facet) {
				_.forEach(facet.values, function(value) {
					if(value.count > 0 && value.selected) {
						if(FacetUtils.isDate(facet.solrType)) {
							var start = value.start;
							if(start !== EMF.search.faceted.date.UNSPECIFIED) {
								var startDate = new Date(value.start);
								start = FacetUtils.formatDate(startDate);
							}

							var end = value.end;
							if(end !== EMF.search.faceted.date.UNSPECIFIED) {
								var endDate = new Date(value.end);
								end = FacetUtils.formatDate(endDate);
							}

							var selection = facet.id + ":" + start + EMF.search.faceted.date.SEPARATOR + end;
							selectedFacets.push(selection);
						} else {
							selectedFacets.push(facet.id + ':' + value.id);
						}
					}
				});
			});

			return selectedFacets;
		};

	}]);

	/**
	 * Factory for common logic in the facet module.
	 */
	facetsModule.factory('FacetUtils', [ function() {//NOSONAR
		var factory = {};

		var dateLabelPrefix = "search.facet.date.";
		var dateTypes 	= ['tdates'];

		factory.isDate = function(object) {
			return _.contains(dateTypes, object);
		};

		/**
		 * Winds back the provided date object to midnight.
		 * 
		 * @param date -
		 *        date object
		 * @return the date back to midnight
		 */
		var getMidnightDate = function(date) {
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			date.setMilliseconds(0);
			return date;
		};
		// Assigned to the factory so it can be used when injected.
		factory.getMidnightDate = getMidnightDate;

		// TODO: Add second, minute & day as parameters ?
		var getOffsetDate = function(msOffset, hourOffset, yearOffset) {
			var date = getMidnightDate(new Date());
			date.setMilliseconds(date.getMilliseconds() + msOffset);
			date.setHours(date.getHours() + hourOffset);
			date.setFullYear(date.getFullYear() + yearOffset);
			return date;
		};

		/**
		 * Constructs a date range based on the provided parameters.
		 */
		var getDateRange = function (order, id, start , end, includedRanges) {
			return {
				order: order,
				id: id,
				start: start,
				end: end,
				includedRanges: includedRanges,
				count: 0,
				text: _emfLabels[dateLabelPrefix + id]
			};
		};

		/**
		 * Check if the facet has any selected values.
		 */
		var hasSelectedValues = function(facet) {
			return _.find(facet.values, function(value) {
				return value.selected;
			});
		};

		factory.hasSelectedValues = hasSelectedValues;

		/**
		 * Add a leading zero to the input and slice only the first two symbols.
		 * Used when formatting the date to an ISO format to add a leading zero to the days, months, hours, etc.
		 *
		 * @param data
		 *        the data
		 * @return the ISO date string
		 */
		var addLeadingZero = function(data){
			return ("0" + data).slice(-2);
		};

		/**
		 * Convert the date to an ISO date without taking into account the timezone.
		 *
		 * @param date
		 *        the date
		 * @return the ISO date string
		 */
		var formatDate = function(date){
			var month = addLeadingZero(date.getMonth()+1);
			var day = addLeadingZero(date.getDate());
			var hours = addLeadingZero(date.getHours());
			var minutes = addLeadingZero(date.getMinutes());
			var seconds = addLeadingZero(date.getSeconds());
			var milliseconds = addLeadingZero(date.getMilliseconds());
			return date.getFullYear() + "-" + month + "-" + day + "T" + hours + ":" + minutes + ":" + seconds + "." + milliseconds + "Z";
		};

		factory.formatDate = formatDate;

		/**
		 * Constructs default date ranges if none were specified.
		 * 
		 * @return its obvious...
		 */
		factory.getDefaultDateRanges = function() {
			return [
				// Past
				getDateRange(-5,	'beyond_last_year',	EMF.search.faceted.date.UNSPECIFIED, 	getOffsetDate(-1, 0, -1), 				[]),
				getDateRange(-4,	'last_year',  		getOffsetDate(0, 0, -1), 				getOffsetDate(-1, 24, 0), 				['last_month', 'last_week', 'yesterday', 'today']),
				getDateRange(-3,	'last_month',  		getOffsetDate(0, -720, 0), 				getOffsetDate(-1, 24, 0), 				['last_week', 'yesterday', 'today']),
				getDateRange(-2,	'last_week', 		getOffsetDate(0, -144, 0), 				getOffsetDate(-1, 24, 0), 				['yesterday', 'today']),
				getDateRange(-1,	'yesterday', 		getOffsetDate(0, -24, 0), 				getOffsetDate(-1, 0, 0), 				[]),

				// Present
				getDateRange(0,		'today', 			getOffsetDate(0, 0, 0), 				getOffsetDate(-1, 24, 0), 				[]),

				// Future
				getDateRange(1,	'tomorrow', 			getOffsetDate(0, 24, 0), 				getOffsetDate(-1, 48, 0), 				[]),
				getDateRange(2,	'next_week', 			getOffsetDate(0, 24, 0), 				getOffsetDate(-1, 144, 0), 				['tomorrow']),
				getDateRange(3,	'next_month', 			getOffsetDate(0, 24, 0), 				getOffsetDate(-1, 720, 0), 				['next_week', 'tomorrow']),
				getDateRange(4,	'next_year', 			getOffsetDate(0, 24, 0), 				getOffsetDate(-1, 0, 1), 				['next_month', 'next_week', 'tomorrow']),
				getDateRange(5,	'beyond_next_year', 	getOffsetDate(0, 0, 1), 				EMF.search.faceted.date.UNSPECIFIED, 	[])
			];
		};

		return factory;
	}]);


	/**
	 * Custom filter that sorts the facet date values according to the specified sorting type and
	 * order.
	 * 
	 * @param items -
	 *        the items to be sorted
	 * @param sort -
	 *        the sorting type
	 * @param sortOrder -
	 *        the sorting order
	 * @return the sorted array
	 */
	facetsModule.filter('sortDateRangeValues', function() {
		return function(items, sort, sortOrder) {
			if (sort === EMF.search.faceted.sort.CHRONOLOGICAL) {
				var filtered = [];
				_.forEach(items, function(item) {
					filtered.push(item);
				});

				filtered.sort(function(a, b) {
					if(angular.isDefined(a.order)){
						return (a.order > b.order ? 1 : -1);
					} else {
						return (a.id > b.id ? 1 : -1);
					}
				});

				if (sortOrder === EMF.search.faceted.sort.order.DESCENDING) {
					filtered.reverse();
				}

				return filtered;
			} else {
				return items;
			}
		};
	});

}());