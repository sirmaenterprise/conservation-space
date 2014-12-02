/**
 * EMF basic search jquery plugin.
 * Configuration:
 *
 * 	contextPath: string
 * 		base path to use for rest calls and the retrieval of the template for the search UI
 *
 * 	searchTemplateUrl: string
 * 		path to the template for the search UI (w/o the context path)
 *
 * 	search: string
 * 		path to the REST service that performs the search
 *
 * 	locationOptionsReload: string or function
 * 		path to the REST service that retrieves the available context objects, or a function.
 * 		The function must return an array of objects in the format of { name: 'object_key', title: 'Object Title' }
 *
 * 	objectTypeOptionsLoader: string or function
 * 		path to the REST service that retrieves the available object types, or a function.
 * 		The function must return an array of objects in the format of { name: 'object_key', title: 'Object Title' }
 *
 * 	objectRelationOptionsLoader: string or function
 * 		path to the REST service that retrieves the available object relationships, or a function.
 * 		The function must return an array of objects in the format of { name: 'object_key', title: 'Object Title' }
 *
 * 	orderByFieldsLoader: string or function
 * 		path to the REST service that retrieves the available 'order by' fields, or a function.
 * 		The function must return an array of objects in the format of { name: 'object_key', title: 'Object Title' }
 *
 * 	searchUsersUrl: string
 * 		path to the REST service used to retrieve user names when searching by 'createdBy' field.
 *
 * 	resultItemDecorators: array of functions
 * 		Each function is called after a search has been performed and a result must be rendered.
 * 		Each decorator receives the raw item data as well as the DOM element.
 * 		Each decorator must return the changed DOM element.
 *
 * 	defaultsIfEmpty: boolean
 * 		If true, before the search is performed empty search args will be replaced with their default values
 *
 * 	onSearchCriteriaChange: array of functions
 * 		Each function is called whenever a change in the search criteria occurs passing the changed search criteria object.
 *
 * 	onAfterSearch: array of functions
 * 		Each function is called after a search has been performed. No arguments are passed to the functions.
 *
 * 	onItemAfterSearch: array of functions
 * 		Each function is called for each object in the result. The raw object data is passed to the function.
 *
 * 	onBeforeSearch: array of functions
 * 		Each function is called just before the search url is invoked. An object containing the search criteria is passed to the function.
 * 			{ args: { ... } }
 *
 * 	initialSearchArgs: object
 * 		Object containing the initial search arguments. Useful when there's a need to restore a previously saved search.
 *
 *
 * 	Functions:
 * 		usage - $('.selector').basicSearch('functionName', [arg1, arg2, ..., argN])
 *
 * 	getSearchArgs:
 * 		returns the search arguments as an object
 *
 * 	resetSearch:
 * 		replaces the search criteria with the initial
 *
 *
 *
 * @param $ jQuery object to use.
 */
(function($) {
	'use strict'

	/**
	 * Plugin instance constructor.
	 * @param options plugin configuration options
	 * @param element DOM element on which the search was called on
	 */
	function EmfBasicSearch(options, element) {
		var contextPath = options.contextPath || SF.config.contextPath;

		this.element = element;
		this.options = $.extend(true, {
			debugEnabled				: EMF.config.debugEnabled,
			// default settings
			contextPath					: contextPath,
			searchTemplateUrl			: contextPath + '/search/basic-search.tpl.html',

			fieldNameToSelectorMapping	: {
				'objectType'			: '.object-type',
				'objectRelationship'	: '.object-relationship',
				'location'				: '.location-select',
				'orderDirection'		: '.order-direction',
				'createdFromDate'		: '.created-from',
				'createdToDate'			: '.created-to',
				'createdBy'				: '.created-by',
				'metaText'				: '.search-meta',
				// Keep orderBy at last position because it triggers search when
				// changed in order to ensure that all fields are already set
				// before performing the search
				'orderBy' 				: '.order-by'
			},

			initialSearchArgs: {
				pageNumber				: 1,
				pageSize				: 10,
				createdBy				: [ ],
				createdByValue			: [ ],
				orderBy					: 'dcterms:title',
				orderDirection			: 'desc',
				location				: [ ],
				objectRelationship		: [ ],
				objectType				: [ ],
				createdFromDate			: '',
				createdToDate			: '',
				metaText				: '',
				searchType				: 'basic'
			},

			searchFieldsConfig			: { },

			dateFormatPattern			: SF.config.dateFormatPattern,
			firstDay					: SF.config.firstDay,
			monthNames 					: SF.config.monthNames,
			monthNamesShort				: SF.config.monthNamesShort,
			dayNames					: SF.config.dayNames,
			dayNamesMin 				: SF.config.dayNamesMin,

			search						: contextPath + '/service/search/basic',
			locationOptionsReload		: contextPath + '/service/search/quick',
			objectTypeOptionsLoader		: contextPath + '/service/definition/all-types',
			objectRelationOptionsLoader	: contextPath + '/service/relationships',
			searchUsersUrl				: contextPath + '/service/search/users',
			solrSearch					: contextPath + '/service/search/solr',
			executor					: contextPath + '/service/executor',
			orderByFieldsLoader			: function() {
				return [
				        { name: 'Title', value: 'dcterms:title'},
				        { name: 'Type', value: 'emf:type'},
				        { name: 'Modified On', value: 'emf:modifiedOn'},
				        { name: 'Modified By', value: 'emf:modifiedBy'},
				        { name: 'Created On', value: 'emf:createdOn'},
				        { name: 'Created By', value: 'emf:createdBy'}
				];
			},

			resultItemDecorators		: [ ],

			onSearchCriteriaChange		: [ ],
			onAfterSearch				: [ ],
			onItemAfterSearch			: [ ],
			onBeforeSearch				: [ ],

			listeners					: { }
		}, options);

		this.searchArgs = $.extend(true, { }, this.options.initialSearchArgs);
		this.filterId = null;
		this.init();
	}

	EmfBasicSearch.prototype = {
		eventNames: {
			TEMPLATE_LOADED: 'basic-search:template-loaded',
			OPTIONS_LOADED: 'basic-search:options-loaded',
			AFTER_SEARCH: 'basic-search:after-search',
			AFTER_SEARCH_ACTION: 'basic-search:after-search-action'
		},

		_defaultFieldController: {
			disabled: function() {
				if (arguments.length) {
					if (arguments[0] === true) {
						this.field.prop('disabled', 'disabled');
					} else {
						this.field.removeProp('disabled');
					}
				} else {
					return this.field.prop('disabled') ? true : false;
				}
			},

			value: function() {
				if (arguments[0] || typeof arguments[0] === 'string') {
					this.field.val(arguments[0]);
					this.field.trigger('change');
				} else {
					return this.field.val();
				}
			}
		},

		_orderDirectionController: {
			value: function() {
				if (arguments[0] || typeof arguments[0] === 'string') {
					this.searchArgs.orderDirection = (arguments[0] === 'asc') ? 'asc' : 'desc';
				} else {
					return this.searchArgs.orderDirection;
				}
			}
		},

		_dateFieldController: {
			disabled: function() {
				if (arguments.length) {
					if (arguments[0] === true) {
						this.field.datepicker('disable');
					} else {
						this.field.datepicker('enable');
					}
				} else {
					return this.field.prop('disabled') ? true : false;
				}
			},

			value: function() {
				if (arguments[0] || typeof arguments[0] === 'string') {
					this.field.val(arguments[0]);
					this.field.trigger('change');
					this.field.datepicker('refresh');
				} else {
					return this.field.val();
				}
			}
		},

		_autocompleteFieldController: {
			disabled: function() {
				if (arguments.length) {
					if (arguments[0] === true) {
						this.field.prop('disabled', 'disabled');
					} else {
						this.field.removeProp('disabled');
					}
				} else {
					return this.field.prop('disabled') ? true : false;
				}
			},

			value: function() {
				if (arguments[0] && arguments[1] && arguments[0].length === arguments[1].length) {
					if (arguments[0].length > 0) {
						arguments[0].push('');
						arguments[1].push('');
					}
					this.field.val(arguments[1].join(', '));
					this.field.get(0).label = arguments[0].join(', ');
					this.field.trigger('autocompletechange');
				} else if (arguments[0]) {
					if (arguments[0].length > 0){
						arguments[0].push('');
					}
					this.field.val(arguments[0].join(', '));
					this.field.trigger('autocompletechange');
				} else {
					return this.field.val().replace(/,\s*$/, '').split(/\s*,\s*/);
				}
			}
		},

		_locationElementController: {
			disabled: function() {

			},

			value: function() {
				if (arguments[0]) {
					if(arguments[0][0]) {
						if(arguments[1] && arguments[0].length === arguments[1].length) {
							var data = [];
							for ( var int = 0; int < arguments[0].length; int++) {
								data.push({name: arguments[0][int], title:arguments[1][int]});
							}
							this.field.select2("data", data).trigger('change');
						} else {
							this.field.select2('val', arguments[0]);
						}
					} else {
						// Reset in main menu is still not working correct
						this.field.select2("data", null).trigger('change');
					}

				} else {
					return this.field.select2('val');
				}
			},

			init: function(config) {
				this.field.select2(config);
				var select2container = this.field.select2("container");
				select2container.attr('data-name', this.field.attr('data-name'));
			}
		},

		_chosenSelectController: {
			disabled: function() {
				if (arguments.length) {
					if (arguments[0] === true) {
						this.field.prop('disabled', 'disabled');
					} else {
						this.field.removeProp('disabled');
					}
					this.field.trigger('chosen:updated');
				} else {
					return this.field.prop('disabled') ? true : false;
				}
			},

			value: function(value) {
				if (value) {
					this.field.children().removeProp('selected');

					if (typeof value === 'string') {
						this.field.children('[value="' + value + '"]').prop('selected', 'selected');
					} else {
						var _this = this;
						$.each(value, function() {
							var child = _this.field.children('[value="' + this + '"]');
							child.prop('selected', 'selected');
						});
					}

					this.field.trigger('change');
					this.field.trigger('chosen:updated');
				} else {
					var multiple = this.field.prop('multiple') ? true : false;
					var selected = this.field.prop('multiple') ? [ ] : '';

					var selectedSubTypes;
					var selectedSubTypeElements = this.field.find('.sub-type:selected');
					if (selectedSubTypeElements.length) {
						selectedSubTypes = multiple ? [ ] : '';
						selectedSubTypeElements.each(function() {
							var $this = $(this);
							if (selectedSubTypes.push) {
								selectedSubTypes.push($this.val());
							} else {
								selectedSubTypes = $this.val();
								return false;
							}
						});
					}

					this.field.find(':selected:not(.sub-type)').each(function() {
						var $this = $(this);
						if (selected.push) {
							selected.push($this.val());
						} else {
							selected = $this.val();
							return false;
						}
					});

					if (selectedSubTypes) {
						return {
							selected: selected,
							selectedSubTypes: selectedSubTypes
						};
					}

					return selected;
				}
			}
		},

		init: function() {
			var _this = this;

			_this.element.on(_this.eventNames.TEMPLATE_LOADED, function(data) {
				// register these here to avoid race conditions like CMF-9391
				
				_this.element.on('perform-basic-search', function() {
					_this.searchArgs.pageNumber = 1;
					_this._doSearch();
				});
				
				_this.element.trigger('basic-search-initialized');
				
			});
			
			_this.element.on(_this.eventNames.OPTIONS_LOADED, this._onOptionsLoaded);
			_this.loadTemplate();
			
			var listeneres = _this.options.listeners;
			if (listeneres) {
				for (var key in listeneres) {
					var value = listeneres[key];

					if (typeof value === 'function') {
						_this.element.on(key, value);
					} else if (({}).toString.call(value) === '[object Array]') {
						for (var index in value) {
							_this.element.on(key, value[index]);
						}
					}
				}
			}

		},

		/**
		 * Check if query string contains some search arguments and initialize the search fields
		 * with provided search criteria. If there are search criteria passed trough the query string,
		 * then we execute the search after fields and arguments initialization.
		 */
		applySearchArguments: function() {
			var _this = this,
				// get available search criteria fields
				fieldNames = _this.options.fieldNameToSelectorMapping,
				// get query string parameters
				queryParameters = $.deparam(location.search) || {},
				// flag to show if search arguments are updated trough values passed as query parameters
				hasUpdatedSearchArguments = false;

			for ( var parameter in queryParameters) {
				if(queryParameters.hasOwnProperty(parameter)) {
					var parameterValue = queryParameters[parameter];
					if(parameter in fieldNames) {
						// for metaText field we should have plain text
						if(parameter === 'metaText') {
							// if there is a metaText we should log a search event
							_this.searchArgs.log = true;
							_this.options.initialSearchArgs[parameter] = parameterValue;
							_this.searchArgs[parameter] = parameterValue;
						} else {
							_this.options.initialSearchArgs[parameter] = [parameterValue];
							_this.searchArgs[parameter] = [parameterValue];
						}
						hasUpdatedSearchArguments = true;
					}
				}
			}
			
			if (hasUpdatedSearchArguments) {
				_this.element.on('basic-search-initialized', function() {
					_this.element.trigger('perform-basic-search');
					_this.searchArgs.log = false;
				});
			}
		},

		getParameterByName: function(name) {
		    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
		    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
		        results = regex.exec(location.search);
		    var res = (results == null) ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
		    return res;
		},

		getSearchArgs: function() {
			return this.searchArgs;
		},

		/**
		 * Load the template and append it to the placeholder element.
		 * After the template has been loaded and inserted into the DOM
		 * an event is fired indicating that the template was loaded.
		 */
		loadTemplate: function() {
			var _this = this;
			$.ajax({
        		type: 'GET',
        		url: _this.options.searchTemplateUrl,
        		complete: function(data) {
        			_this.template = data.responseText;
        			var templateAsElement = $(_this.template);

                	$(templateAsElement).appendTo(_this.element);

                	_this.applySearchArguments();

        			_this._initAdvancedSearch();
        			_this._initSPARQLSearch();
        			_this._initSearchFields();
        			_this._initSaveFilterForm();

        			_this.element.trigger(_this.eventNames.TEMPLATE_LOADED, {
        				template : templateAsElement,
        				settings : _this.options
        			});
        		}
    		});
		},

		/**
		 * Reset the search with the initial criteria.
		 * @param newSearchArgs if present used instead initial criteria to initialize search
		 */
		resetSearch: function(newSearchArgs) {

			var _this = this;
			
			if (this.searchArgs.searchType === 'basic') {
				// FIXME: Duplicated code!
				var resetSearchArgs = newSearchArgs || {
					pageNumber				: 1,
					pageSize				: 10,
					createdBy				: [ ],
					createdByValue			: [ ],
					orderBy					: 'dcterms:title',
					orderDirection			: 'desc',
					location				: [ ],
					objectRelationship		: [ ],
					objectType				: [ ],
					createdFromDate			: '',
					createdToDate			: '',
					metaText				: '',
					searchType				: 'basic'
				};
				$.each(this.options.fieldNameToSelectorMapping, function(name, selector) {
					var controller = _this._controllers(name);
					if (controller) {
						controller.value(resetSearchArgs[name], resetSearchArgs[name + 'Value']);
					}
				});
			} else if (this.searchArgs.searchType === 'advanced') {
				this.element.data('EmfAdvancedSearch').loadCriteria(newSearchArgs, function() {
	    			_this.searchArgs.pageNumber = 1;
	    			_this._doSearch();
				});

			} else if (this.searchArgs.searchType === 'sparql') {
				if (newSearchArgs && newSearchArgs.queryText) {
					$('.sparql-search-query', this.element).val(newSearchArgs.queryText);
				} else {
					$('.sparql-search-query', this.element).val('');
				}
    			this.searchArgs.pageNumber = 1;
    			this._doSearch();
			}

			_this.element
				.find('.basic-search-results-wrapper .basic-search-results')
					.children()
					.remove();

			_this.element.find('.basic-search-pagination').hide();
		},

		_controllers: function(controllerName) {
			var selector = this.options.fieldNameToSelectorMapping[controllerName];
			return $(selector, this.element).data('controller');
		},

		/**
		 * Builds the actual search url that will be used for the search (w/ the search arguments).
		 * Useful when there's a need to invoke the search without the UI.
		 */
		buildSearchUrl: function() {
			// FIXME: use this function in _doSearch()
			var searchData = {};
			searchData.args = this.searchArgs;
			
			searchData.url = this.options.search;
			
			/*if (this.searchArgs.searchType === 'basic') {
				searchData.url = this.options.search;
			} else if (this.searchArgs.searchType === 'advanced') {
				searchData.url = this.options.solrSearch;
			} else if (this.searchArgs.searchType === 'sparql') {
				searchData.url = this.options.search;
			}*/

			var url = searchData.url;

			this._onBeforeSearch(searchData);
			url += '?' + $.param(searchData.args);
			return url;
		},
		
		/**
		 * Returns a new searchArgs object with a copy of common (for all search types) search arguments
		 */
		_copyCommonSearchArgs: function(searchArgs) {
			return {
				orderBy: searchArgs.orderBy,
				orderDirection:	searchArgs.orderDirection,
				pageNumber:	searchArgs.pageNumber,
				pageSize: searchArgs.pageSize,
				searchType: searchArgs.searchType,
				log: searchArgs.log	
			};
		},

		_doSearch: function(resultHandler) {
			var _this = this;

			var searchData = {};
			searchData.url = this.options.search;
			if (this.searchArgs.searchType === 'basic') {
				searchData.args = $.extend({}, this.searchArgs);
				delete searchData.args.fq;
				delete searchData.args.advancedSearchCriteria;
				delete searchData.args.queryText;				
			} else if (this.searchArgs.searchType === 'advanced') {
				searchData.args = this._copyCommonSearchArgs(this.searchArgs);
				searchData.args.fq = this.searchArgs.fq;
				searchData.args.advancedSearchCriteria = this.searchArgs.advancedSearchCriteria;
			} else if (this.searchArgs.searchType === 'sparql') {
				searchData.args = this._copyCommonSearchArgs(this.searchArgs);
				//searchData.args.queryText = $('.sparql-search-query', this.element).val();
				searchData.args.queryText = this.searchArgs.queryText;
				searchData.args.location = this.searchArgs.location;
			}

			this.element.trigger('before-search', searchData.args);

			// do some magic before the search, like replacing the context constants with real object URIs
			this._onBeforeSearch(searchData);
			$.each(this.options.onBeforeSearch, function() {
				this.call(_this, searchData);
			});
			EMF.blockUI.showAjaxLoader();
			$.ajax({ dataType: "json", url: searchData.url, data: searchData.args, async: true,
				complete: function(response) {
					var parsed = $.parseJSON(response.responseText);
					if (resultHandler) {
						resultHandler.call(_this, parsed);
					}
					_this.element.trigger(_this.eventNames.AFTER_SEARCH, parsed);
					// the next event is for external use
					_this.element.trigger(_this.eventNames.AFTER_SEARCH_ACTION, parsed);
					$.each(_this.options.onAfterSearch, function() {
						this.call(_this);
					});
					EMF.shorteningPlugin.shortElementText();
					EMF.blockUI.hideAjaxBlocker();
		    	},
		    	error: function(jqXHR, textStatus) {
		    		console.error(jqXHR, textStatus);
		    	}
			});

		},

		_afterSearchHandler: function(event, data) {
			var _this = this;
			_this._renderItems.call(_this, data);
			var paginationPlaceholder = $('.basic-search-pagination', _this.element);

			if(data.values && data.values.length > 0) {
    			paginationPlaceholder.pagination({
    				items: data.resultSize,
    				currentPage: _this.searchArgs.pageNumber,
    				itemsOnPage: _this.searchArgs.pageSize,
    				cssStyle: 'compact-theme',
    				onPageClick: function(number, event) {
    					_this.searchArgs.pageNumber = number;
    					_this._doSearch();
    				}
    			}).show();
			} else {
				paginationPlaceholder.hide();
			}
		},

		_defaultItemRenderer: function(resultItem) {
    		var imagePath = EMF.util.objectIconPath(resultItem.type, 64);
    		var content = '';
    			content += '<div class="tree-header default_header">';
    			content += 		'<div class="instance-header ' + resultItem.type + '">';
    			content += 			'<span class="icon-cell">';
    			content += 				'<img class="header-icon" src="' + resultItem.icon + '" width="64" />';
    			content += 			'</span>';
    			content += 			'<span class="data-cell">' + resultItem.default_header + '</span>';
    			content += 		'</div>';
    			content += '</div>';

			return $(content);
		},

		_renderItems: function(data) {
			var _this = this;
			var viewSearchResults = $('.basic-search-results-wrapper > .basic-search-results', this.element);
			viewSearchResults.children().remove();
			$('#results-count').empty();

			var resultsCountLabel = _emfLabels['basicSearch.xFound'];
    		var resultSize = data.resultSize;
    		var resultCountMessage = EMF.util.formatString(resultsCountLabel, 0);
    		if (resultSize >= 1000) {
    			resultCountMessage = _emfLabels['basicSearch.tooManyResults'];
    		} else if (resultSize > 0) {
    			resultCountMessage = EMF.util.formatString(resultsCountLabel, resultSize);
    		}

    		$('#results-count').append(resultCountMessage);
    		$('#order').css('display', 'inline');


			if(data.values && data.values.length > 0) {
				var itemRenderer = _this.options.resultItemRenderer || _this._defaultItemRenderer;
				$.each(data.values, function(index) {
					var item = data.values[index];
					var itemElement = itemRenderer.call(_this, item);

					$.each(_this.options.resultItemDecorators, function() {
						itemElement = this.call(_this, itemElement, item);
					});

					$.each(_this.options.onItemAfterSearch, function() {
						this.call(_this, item);
					});

					itemElement.appendTo(viewSearchResults);
				});
			}
		},

		_onBeforeSearch: function(searchData) {
			if (!EMF.currentPath) {
				return;
			}

			if (searchData.args.location) {
				var newArgs = $.extend(true, { }, searchData.args);

				var replacedItems = [ ];
				var contextPath = EMF.currentPath;
				
				var currentObject = null;

				$.each(newArgs.location, function(index, value) {
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
							currentObject = contextPath[contextPath.length - 1].id;
							replacedItems.push(currentObject);
						}
						break;
					default:
						replacedItems.push(value);
						break;
					}
				});
				newArgs.location = replacedItems;
				
				if (newArgs.searchType === 'sparql' && newArgs.queryText && currentObject != null) {
					newArgs.queryText = newArgs.queryText.replace(new RegExp(EMF.util.escapeRegExp('*currentObject*'), 'g'), currentObject);
				}
				
				searchData.args = newArgs;
			}

			if (this.options.defaultsIfEmpty) {
				var objectTypes = searchData.args.objectType;
				if (!objectTypes || !objectTypes.length) {
					searchData.args.objectType = this.options.initialSearchArgs.objectType;
				}
			}
		},

		/**
		 * Init elements with data and event handlers.
		 */
		_initSearchFields: function() {
			var _this = this;

			var contextObjects = _this.searchArgs.location;
			var locationElement = $(this.options.fieldNameToSelectorMapping.location, this.element),
				locationElementController = $.extend(true, { field: locationElement }, _this._locationElementController);

			if (contextObjects && contextObjects.length) {
				locationElement.val(_this.searchArgs.location.join(','));
			}
			locationElement.data('controller', locationElementController);
			locationElementController.init({
				// this will allow select2 to send empty search to server
			    minimumInputLength: 0,
			    // it's needed, otherwise search field will be hided by select2
			    minimumResultsForSearch: 0,
				multiple: true,
				width: 200,
				id: function(item) { return item.uri || item.name; },
				initSelection : function (element, callback) {
					var data = [ ],
						query = '?uris=',
						uris,
						params;

					var predefinedKeys = [ 'emf-search-context-current-project', 'emf-search-context-current-case', 'emf-search-context-current-object' ];
					var predefinedContexts = { };
		    		if (EMF.currentPath) {
						var contextPath = EMF.currentPath;
						if (contextPath.length && contextPath[0].type === 'projectinstance') {
							predefinedContexts[predefinedKeys[0]] = { 'name' : predefinedKeys[0], 'title': 'Current Project' };
						}

						if (contextPath.length > 1 && contextPath[1].type === 'caseinstance') {
							predefinedContexts[predefinedKeys[1]] = { 'name' : predefinedKeys[1], 'title': 'Current Case' };
						}

						if (contextPath.length) {
							predefinedContexts[predefinedKeys[2]] = { 'name' : predefinedKeys[2], 'title': 'Current Object' };
						}
					}

					if (contextObjects && contextObjects.length) {

						var predef = [];
						uris = [];

						_.each(contextObjects, function(uri) {
							if (uri.indexOf(':') === -1) {
								predef.push(uri);
							} else {
								uris.push(uri);
							}
						});

						if (uris.length) {
							params = {
					        	q: 'uri:("' + uris.join('" OR "') + '")',
						    	max: 20,
						    	sort: 'score',
						    	dir: 'desc',
						    	fields:'title,uri'
					        };
							$.ajax({
								url: EMF.servicePath + '/search/quick?' + $.param(params),
								dataType: 'json',
								success: function(response) {
									var data = response.data || [ ];

									if (predef.length) {
										_.each(predef, function(value) {
											var item = predefinedContexts[value];
											if (item) {
												data.push(item);
											}
										});
									}

									if (data.length) {
										callback(data);
									}
								}
							});
						} else if (predef.length) {
							var data = [ ];
							_.each(predef, function(value) {
								var item = predefinedContexts[value];
								if (item) {
									data.push(item);
								};
							});
							callback(data);
						}
					}
			    },
			 	ajax: {
					quiteTime: 500,
					url: _this.options.locationOptionsReload,
					dataType: 'json',
					data: function (term, page) { 
			        	if (!term && term.length < 3) {
			        		term = '*';
			        	}	
						return {
							q: term,
							qtype: 'term',
				          	max: 20,
				          	sort: 'score',
				            dir: 'desc',
				            fields:'title,uri,rdfType'
						}
					},
					results: function (data, page) {
						var result = data.data || [ ];
						if (result.length) {
							locationElement.data('allContexts', result);
						}
						return { results: _.values(predefinedContexts).concat(result) };
					}
				},
				formatResult: function(item) {
					return item.title;
				},
				formatSelection: function(item) {
					return item.title;
				}
			});

			var locationElement = $(this.options.fieldNameToSelectorMapping.location, this.element),
			locationElementController = $.extend(true, { field: locationElement }, _this._locationElementController);
			locationElement.data('controller', locationElementController);

    		var objectTypeElement = $(this.options.fieldNameToSelectorMapping.objectType, this.element);
    		objectTypeElement.data('controller', $.extend(true, { field: objectTypeElement }, _this._chosenSelectController));

    		var objectRelationshipElement = $(this.options.fieldNameToSelectorMapping.objectRelationship, this.element);
    		objectRelationshipElement.data('controller', $.extend(true, { field: objectRelationshipElement }, _this._chosenSelectController));

    		var orderByElement = $(this.options.fieldNameToSelectorMapping.orderBy, this.element);
    		orderByElement.data('controller', $.extend(true, { field: orderByElement }, _this._chosenSelectController));

    		var orderDirectionElement = $(this.options.fieldNameToSelectorMapping.orderDirection, this.element);
    		orderDirectionElement.data('controller', $.extend(true, { field: orderDirectionElement, searchArgs: _this.searchArgs }, _this._orderDirectionController));

    		var createdFromElement = $(this.options.fieldNameToSelectorMapping.createdFromDate, this.element);
    		createdFromElement.data('controller', $.extend(true, { field: createdFromElement }, _this._dateFieldController));

    		var createdToElement = $(this.options.fieldNameToSelectorMapping.createdToDate, this.element);
    		createdToElement.data('controller', $.extend(true, { field: createdToElement }, _this._dateFieldController));

    		var createdByElement = $(this.options.fieldNameToSelectorMapping.createdBy, this.element);
    		createdByElement.data('controller', $.extend(true, { field: createdByElement }, _this._autocompleteFieldController));

    		var searchMetaFieldElement = $(this.options.fieldNameToSelectorMapping.metaText, this.element);
    		searchMetaFieldElement.data('controller', $.extend(true, { field: searchMetaFieldElement }, _this._defaultFieldController));

    		var resetSearchElement = $('.reset-btn', this.element);

    		var changeHandler = function(event, data) {
    			var controller = $(event.target).data('controller');
    			_this._onSearchCriteriaChange(event, controller.value());
    		};

    		var inputValueChangeHandler = function(event) {
    			_this._onSearchCriteriaChange(event, $(event.target).val());
    		};

    		locationElement
    			.on('change', changeHandler)
    			.change(function() {
    				objectRelationshipElement.trigger('filter-relationships');
    			});
    		
    		objectTypeElement
    			.on('change', changeHandler)
    			.change(function() {
    				objectRelationshipElement.trigger('filter-relationships');
    			});
    		
    		objectRelationshipElement
    			.on('change', changeHandler)
    			.on('filter-relationships', function(event, data) {
    				// http://giphy.com/gifs/Qqq8b2K5qsg8M
    				
    				var domainClasses = objectTypeElement.data('controller').value(),
    					selectedContexts = locationElement.data('controller').value(),
    					rangeClasses = [],
    					allContexts = locationElement.data('allContexts'),
    					domainFilter = '',
    					rangeFilter = '',
    					filterUrl = _this.options.objectRelationOptionsLoader;
    				
    				// jeeesus...
    				if (Object.prototype.toString.call(domainClasses) !== '[object Array]') {
    					var subTypes = [].concat(domainClasses.selectedSubTypes),
    						fixedSubTypes = [];
    					
    					_.each(subTypes, function(subType) {
    						var clType = subType.indexOf(':') === -1;
    						if (clType) {
    							var parentType = objectTypeElement.children('option[value="' + subType + '"]').attr('parent');
    							
    							if (parentType) {
    								fixedSubTypes.push(encodeURIComponent(parentType));
    							}
    						} else {
    							fixedSubTypes.push(subType);
    						}
    					});
    					domainClasses = domainClasses.selected.concat(fixedSubTypes);
    				}
    				
    				_.each(domainClasses, function(item) {
    					domainFilter += 'domainFilter=' + encodeURIComponent(item) + '&';
    				});
    				
    				_.each(selectedContexts, function(uri) {
    					var item = _.find(allContexts, function(context) {
    						return context.uri.indexOf(uri) > -1;
    					});
    					
    					if (item) {
    						_.each(item.rdfType, function(type) {
    							if (rangeClasses.indexOf(type) === -1) {
    								// we get the full uri of the types here
    								rangeClasses.push(encodeURIComponent(type));
    							}
    						});
    					}
    				});
    				
    				_.each(rangeClasses, function(item) {
    					rangeFilter += 'rangeFilter=' + encodeURIComponent(item) + '&';
    				});
    				
    				if (domainFilter || rangeFilter) {
    					filterUrl += '?' + domainFilter + rangeFilter;
    				}
    				
    				$.get(
    					filterUrl, 
    					function success(response) {
    						var controller = objectRelationshipElement.data('controller'),
    							value = controller.value();
    						
    						objectRelationshipElement
    							.children()
    								.remove()
    							.end()
    							.trigger(_this.eventNames.OPTIONS_LOADED, {
    								values: response,
    								options: _this.options
    							});
    						
    						controller.value(value);
    					}
    				);
    			});
    		
    		orderByElement.on('change', changeHandler);
    		$( ".order-by" ).change(function() {
    			_this._doSearch();
    		});

    		orderDirectionElement.on('click', function(event) {
    			_this.searchArgs.pageNumber = 1;
    			orderDirectionElement.find('span').toggleClass('hide');
    			_this._onSearchCriteriaChange(event, _this.searchArgs.orderDirection === 'desc' ? 'asc' : 'desc');
    			_this._doSearch();
    		});

    		var chosenConfigMultiSelect = {
    	    	width: '200px'
    	    };
    		var chosenConfigSingleSelect = {
    			width: '130px',
        		disable_search: true
        	};

    		var predefinedContexts = [ ];
    		if (EMF.currentPath) {
				var contextPath = EMF.currentPath;
				if (contextPath.length && contextPath[0].type === 'projectinstance') {
					predefinedContexts.push({ 'name' : 'emf-search-context-current-project', 'title': 'Current Project' });
				}

				if (contextPath.length > 1 && contextPath[1].type === 'caseinstance') {
					predefinedContexts.push({ 'name' : 'emf-search-context-current-case', 'title': 'Current Case' });
				}

				if (contextPath.length) {
					predefinedContexts.push({ 'name' : 'emf-search-context-current-object', 'title': 'Current Object' });
				}
			}

    		var rootInstance = sessionStorage.getItem('emf.rootInstance');
			// Check if rootInstance exists in sessionStorage and consult backend if the instance
			// is not in deleted state. If not, then use it as context instance in basic search
    		// !!! Identical code is used in basic-search.xhtml
    		if (rootInstance) {
    			var rootInstanceObject = JSON.parse(rootInstance);
				$.ajax({
					url 		: SF.config.contextPath + "/service/instances/status",
					type		: 'GET',
					async		: false,
					data		: {
						instanceId: rootInstanceObject.id,
						instanceType: rootInstanceObject.type
					}
				}).done(function(data) {
					if(data && data.status !== 'DELETED') {
						predefinedContexts.push({ 'name' : rootInstanceObject.id, 'title': rootInstanceObject.title });
					}
				}).fail(function(data, textStatus, jqXHR) {
					console.error(arguments);
	            });
    		}

    		$.each(predefinedContexts, function() {
    			var option = $('<option value="' + this.name + '">' + this.title + '</option>');

    			//locationElement.append(option);
    			if (_this.searchArgs.location && $.inArray(this.name, _this.searchArgs.location) !== -1 ) {
    				locationElement.select2("data", { name: this.name, title: this.title });
    				option.attr('selected', 'selected');
	    			var eventData = { selected: option.value };
	    			locationElement.trigger('change', eventData);
	    		}
    		});
    		//_this._loadOptions(_this.options.locationOptionsReload, locationElement, chosenConfigMultiSelect, predefinedContexts);
    		_this._loadOptions(_this.options.objectRelationOptionsLoader, objectRelationshipElement, chosenConfigMultiSelect);
    		_this._loadOptions(_this.options.objectTypeOptionsLoader, objectTypeElement, chosenConfigMultiSelect);
    		_this._loadOptions(_this.options.orderByFieldsLoader, orderByElement, chosenConfigSingleSelect);

    		var datePickerSettings = {
    			changeMonth		: true,
        		dateFormat		: _this.options.dateFormatPattern,
        		firstDay 		: _this.options.firstDay,
        		monthNames 		: _this.options.monthNames,
        		monthNamesShort	: _this.options.monthNamesShort,
        		dayNames		: _this.options.dayNames,
        		dayNamesMin 	: _this.options.dayNamesMin,
        		numberOfMonths	: 1
    		};

    		createdFromElement.datepicker($.extend(datePickerSettings, {
    			onClose: function(selectedDate) {
    				createdToElement.datepicker("option", "minDate", selectedDate);
    			}
    		}));
    		// FIXME: use $( ".selector" ).datepicker( "getDate" ); in datepicker change handlers
    		createdFromElement.on('change', inputValueChangeHandler);
    		createdFromElement.val(_this.options.initialSearchArgs.createdFromDate);

    		createdToElement.datepicker($.extend(datePickerSettings, {
    			onClose: function(selectedDate) {
    		    	createdFromElement.datepicker("option", "maxDate", selectedDate);
    		    }
    		}));
    		createdToElement.on('change', inputValueChangeHandler);
    		createdToElement.val(_this.options.initialSearchArgs.createdToDate);

    		createdByElement.bind('keydown', function(event) {
		    	if (event.keyCode === $.ui.keyCode.TAB && $(this).data('ui-autocomplete').menu.active) {
		    		event.preventDefault();
		        }
		    	//this.label = this.value;
		    }).autocomplete({
		    	source: function(request, response) {
		            $.getJSON(_this.options.searchUsersUrl, { term: _this._extractLast(request.term) }, response);
		        },
		        search: function() {
		        	// custom minLength
		        	var term = _this._extractLast( this.value );
		        	if ( term.length < 2 ) {
		        		return false;
		        	}
		        },
		        select: function(event, ui) {
		        	var terms = _this._split(this.value);
		        	// remove the current input
		        	terms.pop();
		        	// add the selected item
		        	terms.push(ui.item.label);
		        	// add placeholder to get the comma-and-space at the end
		        	terms.push( "" );
		        	this.value = terms.join( ", " );

		        	if(this.label) {
		        		terms = _this._split(this.label);
		        	} else {
		        		terms = [];
		        	}
		        	// remove the current input
		        	terms.pop();
		        	terms.push(ui.item.value);
		        	terms.push( "" );
		        	this.label = terms.join( ", " );

		        	return false;
		        },
		        focus: function() {
		        	// prevent value inserted on focus
		        	return false;
		        }
		    });
    		createdByElement.on('autocompletechange', function(event, ui) {
    			var _value = $(this).val();
    			var _label = this.label;
    			var array = {};
    			array.value = [];
    			array.label = [];

    			if (_value) {
    				array.value = _value.replace(/,\s*$/, '').split(/\s*,\s*/);
    				if(_label) {
    					array.label = _label.replace(/,\s*$/, '').split(/\s*,\s*/);
    				} else {
    					array.label = _value.replace(/,\s*$/, '').split(/\s*,\s*/);
    				}
    			}
    			_this._onSearchCriteriaChange(event, array);
	        });
    		createdByElement.val((_this.options.initialSearchArgs.createdByValue || []).join(', '));

    		searchMetaFieldElement.on('change', inputValueChangeHandler);
    		searchMetaFieldElement.val(_this.options.initialSearchArgs.metaText);

    		_this.element.on(_this.eventNames.AFTER_SEARCH, function(event, data) {
    			_this._afterSearchHandler(event, data);
    		});

    		$('.search-btn', this.element).click(function() {
    			_this.searchArgs.log = true;
    			_this.element.trigger('perform-basic-search');
    			_this.searchArgs.log = false;
    		});
    		
    		$("input[name='search-type-switch']", this.element).change(function(e){
    			_this.searchArgs.searchType = $(this).val();
    			_this._switchSearchForms();
    		});

    		resetSearchElement.click(function() {
    			_this.resetSearch();
    			_this._resetSaveFilterForm();
    		});

    		// TODO: this could be move to the controllers as a configure function
    		// configure each field in the search e.g. set disabled property
    		$.each(_this.options.fieldNameToSelectorMapping, function(name, selector) {
    			if (_this.options.searchFieldsConfig[name]) {
    				_this._controllers(name).disabled(_this.options.searchFieldsConfig[name].disabled);
    			}
    		});

    		_this._switchSearchForms();
		},
		
		/**
		 * Switch between different search forms based on searchArgs.searchType property
		 */
		_switchSearchForms: function() {
			var basicSearchEl = $($(this.element).find('.basic-search-criteria')[0]);
			var advancedSearchEl = $($(this.element).find('.advanced-search')[0]);
			var sparqlSearchEl = $($(this.element).find('.sparql-search')[0]);
			
			basicSearchEl.hide();
			advancedSearchEl.hide();
			sparqlSearchEl.hide();
			
			if (this.searchArgs.searchType === 'basic') {
				basicSearchEl.show();
			} else if (this.searchArgs.searchType === 'advanced') {
				advancedSearchEl.show();
			} else if (this.searchArgs.searchType === 'sparql') {
				sparqlSearchEl.show();
			}
			
			$("input[name='search-type-switch'][value='" + this.searchArgs.searchType + "']", this.element).prop('checked', true);
			
			if (this.options.isMainSearch) {
				window.document.title = _emfLabels['cmf.tab.' + this.searchArgs.searchType + '.search'];
			}
		},

		/**
		 * Initialize create/update/load filter form.
		 */
		_initSaveFilterForm: function() {
			var _this = this;

			var existingFilters = [];
			$('.load-search', _this.element).select2({
				width:'200px',
				height:'32px',
				placeholder: _emfLabels["cmf.basicsearch.loadfilter"],
				allowClear: true,
				data: existingFilters
			}).on('change', function(newValue, reset) {
				if (newValue.val === '' || reset) {
					_this.filterId = null;
					$('.update-search-button', _this.element).hide();
				} else {
					$('.update-search-button', _this.element).show();
					var title = newValue.added.text;
					var shortUri = newValue.val;
					EMF.blockUI.showAjaxLoader();
					$.ajax({
						url: EMF.servicePath + '/savedfilters/' + encodeURI(shortUri), 
						type: "GET",
						contentType: "application/json",
						success: function(response) {
							_this.filterId = shortUri;

							var searchNameEl = $('.search-name', _this.element);
							searchNameEl.val(title);
							searchNameEl.trigger('change');

							if (response.description) {
								$('.search-description', _this.element).val(response.description);
							} else {
								$('.search-description', _this.element).val('');
							}

							var type = response.filterType;
							var criteria = $.parseJSON(response.filterCriteria);
							if (criteria) {
								_this.searchArgs.searchType = type;
								
								$("input[name='search-type-switch'][value='" + _this.searchArgs.searchType + "']", _this.element)
								.prop('checked', true)
								.trigger('change');
								
								if (_this.searchArgs.searchType === 'basic') {
									if (!criteria.objectType) {
										criteria.objectType = [];
									}
									// Add subTypes to objectType array to properly initialize form
									if (criteria.subType) {
										criteria.subType.forEach(function(entry){
											criteria.objectType.push(entry);
										});
									}
								}
								_this.resetSearch(criteria);
							}
						},
						error: function() {
							EMF.dialog.open({
								message      : _emfLabels["cmf.basicsearch.errorloadfilter"],
								notifyOnly   : true,
								width		 : 'auto'
							});
						},
						complete: function() {
							EMF.blockUI.hideAjaxBlocker();
						}
					});
				}
			});

			var data = {
					max: 10000,
					q: 'instanceType:savedfilter',
					returnShortURI: true,
					fields: 'title,uri'
			};

			EMF.blockUI.showAjaxLoader();
			$.get('/emf/service/search/quick', data, function (response) {
				var formattedData = response.data.map(function(el){
					return {id:el.uri, text:el.title};
				});
				for ( var int = 0; int < formattedData.length; int++) {
					existingFilters.push(formattedData[int]);
				}
				EMF.blockUI.hideAjaxBlocker();
			});


			$('.search-name', this.element).on('change propertychange keyup input paste', function(evt) {
				var newValue = $.trim($(evt.target).val());
				if (newValue === '') {
					$('.update-search-button', _this.element).prop('disabled', true);
					$('.save-search-button', _this.element).prop('disabled', true);
				} else {
					$('.update-search-button', _this.element).prop('disabled', false);
					$('.save-search-button', _this.element).prop('disabled', false);
				}
			});
			$('.search-name', this.element).trigger('change');

			$('.save-search-button', this.element).on('click', function() {
				var data = {};
				data.operations = [{
					operation: "createFilter",
					properties: buildFilterProperties()
				}];

				EMF.blockUI.showAjaxLoader();
				$.ajax({
			        url: _this.options.executor,
			        type: "POST",
			        contentType: "application/json",
			        data: JSON.stringify(data),
			        success: function(response) {
			        	if (response && response.operations) {
			        		var operation = response.operations[0];
			        		if (operation.responseState.status === 'COMPLETED') {
			        			_this.filterId = operation.response.id;
			        			existingFilters.push({id: operation.response.id, text: operation.response.title});
			        			$('.load-search', _this.element).select2("data", {id: operation.response.id, text: operation.response.title});
			        			$('.update-search-button', _this.element).show();
			        		}
			        	}
			        },
			        error: function(response) {
			        	var result = $.parseJSON(response.responseText);
			        	var errorMsg = _emfLabels["cmf.basicsearch.errorcreatefilter"];
			        	if (result && result.operations) {
			        		var operation = result.operations[0];
			        		if (operation.response && operation.response.errorMsg) {
			        			errorMsg = operation.response.errorMsg;
			        		}
			        	}
                		EMF.dialog.open({
                			message      : errorMsg,
                			notifyOnly   : true,
                			width		 : 'auto'
                		});
			        },
			        complete: function(response) {
			        	EMF.blockUI.hideAjaxBlocker();
			        }
			    });
			});

			$('.update-search-button', this.element).on('click', function() {
				var data = {};
				data.operations = [{
					operation: "updateFilter",
					id: _this.filterId,
					properties: buildFilterProperties()
				}];

				EMF.blockUI.showAjaxLoader();
				$.ajax({
			        url: _this.options.executor,
			        type: "POST",
			        contentType: "application/json",
			        data: JSON.stringify(data),
			        success: function(response) {
			        	if (response && response.operations) {
			        		var operation = response.operations[0];
			        		if (operation.responseState.status === 'COMPLETED') {
			        			_this.filterId = operation.response.id;
			        			existingFilters.forEach(function(item, i) {
			        				if (item.id === operation.response.id) {
			        					existingFilters[i].text = operation.response.title;
			        				}
			        			});
			        			$('.load-search', _this.element).select2("data", {id: operation.response.id, text: operation.response.title});
			        		}
			        	}
			        },
			        error: function(response) {
			        	var result = $.parseJSON(response.responseText);
			        	var errorMsg = _emfLabels["cmf.basicsearch.errorupdatefilter"];
			        	if (result && result.operations) {
			        		var operation = result.operations[0];
			        		if (operation.response && operation.response.errorMsg) {
			        			errorMsg = operation.response.errorMsg;
			        		}
			        	}
                		EMF.dialog.open({
                			message      : errorMsg,
                			notifyOnly   : true,
                			width		 : 'auto'
                		});
			        },
			        complete: function(response) {
			        	EMF.blockUI.hideAjaxBlocker();
			        }
			    });
			});

			function buildFilterProperties() {
				var properties = {};
				properties.filterType = _this.searchArgs.searchType;
				properties.title = $('.search-name', _this.element).val();
				properties.description = $('.search-description', _this.element).val();

				var criteria = null;
				if (_this.searchArgs.searchType === 'basic') {
					criteria = _this.searchArgs;
				} else if (_this.searchArgs.searchType === 'advanced') {
					var advSearch = _this.element.data('EmfAdvancedSearch');
					criteria = advSearch.buildCriteria();
				} else if (_this.searchArgs.searchType === 'sparql') {
					criteria = {
							queryText: $('.sparql-search-query', _this.element).val(),
							location: _this.searchArgs.location,
							orderBy: _this.searchArgs.orderBy,
							orderDirection:	_this.searchArgs.orderDirection,
							pageNumber:	_this.searchArgs.pageNumber,
							pageSize: _this.searchArgs.pageSize,
							searchType: _this.searchArgs.searchType
					};
				}
				properties.filterCriteria = JSON.stringify(criteria);
				return properties;
			};
		},
		
		/**
		 * Initialize advanced search form and load initial criteria if any
		 */
		_initAdvancedSearch: function() {
			var _this = this;
			var advSearchCriteria = (_this.searchArgs.searchType === 'advanced')?_this.searchArgs.advancedSearchCriteria:null;
			_this.element.data('EmfAdvancedSearch', new EMF.search.advanced($(_this.element).find('.advanced-search')[0], {'onAdvancedSearchChanged': function(evt) {
				_this.searchArgs.fq = $(evt.target).val();
				_this.searchArgs.advancedSearchCriteria = _this.element.data('EmfAdvancedSearch').buildCriteria();
			}, 'initialCriteria': advSearchCriteria}));
		},
		
		/**
		 * Initialize SPARQL search form and load initial query if any
		 */
		_initSPARQLSearch: function() {
			var _this = this;
    		var sparqlQueryEl = $('.sparql-search-query', this.element);
    		if (sparqlQueryEl) {
    			sparqlQueryEl.val(this.searchArgs.queryText);
    			sparqlQueryEl.on('change propertychange keyup input paste', function(evt) {
    				_this.searchArgs.queryText = $(evt.target).val();
    			});
    		}
		},

		_resetSaveFilterForm: function() {
			var searchNameEl = $('.search-name', this.element);
			searchNameEl.val('');
			searchNameEl.trigger('change');

			var searchDescriptionEl = $('.search-description', this.element);
			searchDescriptionEl.val('');
			searchDescriptionEl.trigger('change');

			var loadSearchEl = $('.load-search', this.element);
			loadSearchEl.val('').trigger('change', true);
		},

		addSearchCriteriaListener: function(listener) {
			this.options.onSearchCriteriaChange.push(listener);
		},

		addItemAfterSearchListener: function(listener) {
			this.options.onItemAfterSearch.push(listener);
		},

		addBeforeSearchListener: function(listener) {
			this.options.onBeforeSearch.push(listener);
		},

		_loadOptions: function(urlOrFunction, element, chosenConfig, preloaded) {
			var _this = this;
			// this is needed because otherwise IE10 throws Invalid Calling Object error
			var preloaded = preloaded;
			// disable the dropdown until options are loaded
			element.prop('disabled', true);
	    	element.chosen(chosenConfig);

	    	$('.chosen-choices').addClass('form-control');

	    	var selectName = element.attr('data-name');
	    	var initialValue = _this.options.initialSearchArgs[selectName];
	    	if (typeof urlOrFunction === 'string') {
	    		$.ajax({
	    			dataType: "json",
	    			url: urlOrFunction,
	    			async: true,
	    			complete: function(response) {
		    			var result = $.parseJSON(response.responseText), 
		    				eventData;
		    				
		    			// TODO: Do we really need this if/else here and why?
		    			if (result.values) {
		    				result.initialValue = initialValue;
		    				result.preloaded = preloaded;
		    				result.options = _this.options;
		    				eventData = {
		    					values: result,
		    					initialValue: initialValue,
		    					options: _this.options,
		    					preloaded: preloaded
		    				};
		    			} else {
		    				eventData = {
		    					values: result,
		    					initialValue: initialValue,
		    					options: _this.options,
		    					preloaded: preloaded
			    			};
		    			}
	    				element.trigger(_this.eventNames.OPTIONS_LOADED, eventData);
		    		}
	    		});
	    	} else if (typeof urlOrFunction === 'function') {
	    		var values = urlOrFunction.call(_this);
	    		element.trigger(_this.eventNames.OPTIONS_LOADED, {
	    			values: values,
	    			initialValue: initialValue,
					options: _this.options,
					preloaded: preloaded
	    		});
	    	}
		},

		_onOptionsLoaded: function(event, data) {
			var element 		= $(event.target),
				arraySearchFn 	= function(item) {
					return item.name === this.name;
				},
				values = data.values || [ ];

			element.data('allOptions', values);
			
			// Create the select options, also check for default selected
       		$.each(values, function(index, option) {
       			if(option.name && option.title) {
       				if (data.preloaded && _.findIndex(data.preloaded, arraySearchFn, option) !== -1) {
       					return true;
       				}
       				var optionElement = null;
       				// We must separate objectType from subType !!
		    		if(option.subType) {
		    			optionElement = $('<option class="sub-type" />');
		    			optionElement.attr('value', option.name);
		    			optionElement.attr('parent', option.parent);
		    			optionElement.html(option.title);
		    		} else {
		    			optionElement = $('<option />').attr('value', option.name).html(option.title);
		    		}

		    		if(typeof option.objectType !== 'undefined') {
			    		optionElement.addClass(option.objectType);
			    		optionElement.attr("type", option.objectType);
		    		}

		    		optionElement.appendTo(element);
		    		if (data.initialValue && $.inArray(option.name, data.initialValue) !== -1) {
		    			optionElement.attr('selected', 'selected');
		    			var eventData = { selected: option.name };
		    			element.trigger('change', eventData);
		    		}

	    		}
       		});

       		// after the element is ready for use - enable it once again
       		var name = element.attr('data-name');
       		var searchFieldConfig = data.options.searchFieldsConfig[name];
       		if (!searchFieldConfig || searchFieldConfig.disabled === false) {
       			element.prop('disabled', false);
       		}
       		element.trigger("chosen:updated");
		},

		_onSearchCriteriaChange: function(domEvent, data) {
			var _this = this;
			var element = $(domEvent.target);

			var elementName = element.attr('data-name');

			setTimeout(function() {
				var chosenSelector = $("#" + element.attr('id') + '_chosen').find('.chosen-choices');
				if(data.length === 0) {
					chosenSelector.removeClass("highlight-element");
					chosenSelector.css("border-color", "");
					element.removeClass("highlight-element");
				} else if(data.value && data.value.length === 0) {
					element.removeClass("highlight-element");
				} else {
					element.addClass("highlight-element");
					chosenSelector.css("border-color", "#66afe9");
					chosenSelector.addClass("highlight-element");
				}
			}, 300); // FIXME: ?!@#

			// Value is an object when we have selected a codelist sub-type, otherwise it's an array
			if (elementName === 'objectType') {

				// ({}).toString.call is needed insted toString.call because otherwise IE10 throws Invalid Calling Object error
				if ( ({}).toString.call(data) === '[object Array]') {
					_this.searchArgs.subType = undefined;
					_this.searchArgs.objectType = data;
				} else {
					_this.searchArgs.objectType = data.selected;
					_this.searchArgs.subType = data.selectedSubTypes;
				}
			} else if(elementName === 'createdBy') {
				_this.searchArgs[elementName] = data.label;
				_this.searchArgs.createdByValue = data.value;
			} else {
				_this.searchArgs[elementName] = data;
			}

			$.each(_this.options.onSearchCriteriaChange, function() {
				this.call(_this, _this.searchArgs);
			});
		},

		_extractLast: function(term) {
    		return this._split(term).pop();
    	},

    	_split: function(val) {
    		return val.split(/,\s*/);
    	}
	}

    $.fn.basicSearch = function(opt) {
		var args = Array.prototype.slice.call(arguments, 1);
    	var pluginInstance = this.data('EmfBasicSearch');
    	if (!pluginInstance) {
    		opt.listeners = opt.listeners || {};
    		this.data('EmfBasicSearch', new EmfBasicSearch(opt, this));
    		return this;
    	} else {
    		if (pluginInstance[opt]) {
    			return pluginInstance[opt].apply(pluginInstance, args);
    		} else {
    			var controller = pluginInstance._controllers.call(pluginInstance, opt);
    			if (controller) {
    				return controller;
    			}
    		}
    	}

    	function addListener(listeners, name, handler) {
    		var value = listeners[name];
    		if (listeners[name]) {
				if (typeof value === 'function') {
					var valueArr = [];
					valueArr.push(value);
					valueArr.push(handler);
					listeners[name] = valueArr;
				} else if (({}).toString.call(value) === '[object Array]') {
					value.push(handler);
				}
    		} else {
    			listeners[name] = handler;
    		}
    	}
    }
}(jQuery));

/**
 * Init for the basic search.
 */
EMF.util.namespace('EMF.search').init = function() {
	var searchConfig = EMF.search.config(),
		rootInstance = sessionStorage.getItem('emf.rootInstance'),
		queryParameters = $.deparam(location.search.substr(1)) || {},
		library = queryParameters['library'],
		libraryTitle = queryParameters['libraryTitle'],
		libraryLink = window.location.href,
		objectType = queryParameters['objectType'];
		
	// flag to indicate that search is opened from the menu not from a picker
	searchConfig.isMainSearch = true;
	// hide toolbar if not in library
	if(!library || library !== 'object') {
		$('.context-data-header').hide();
	} else {
		$('.library-title').attr('data-value', objectType).text(libraryTitle).parent().attr('href', libraryLink);
		// project as context for basic search may be set and not cleared if we come to object library
		// immediately after opening a project, then basic search and then some object library from the main menu
		CMF.utilityFunctions.clearRootInstance();
	}

	// Check if rootInstance exists in sessionStorage and consult backend if the instance
	// is not in deleted state. If not, then use it as context instance in basic search
	// !!! Identical code is used in jquery.emf-basic-search.js
	if (rootInstance) {
		var rootInstanceObject = JSON.parse(rootInstance);
		$.ajax({
			url : SF.config.contextPath + "/service/instances/status",
			type: 'GET',
			data: {
				instanceId	: rootInstanceObject.id,
				instanceType: rootInstanceObject.type
			}
		}).done(function(data) {
			if(data) {
				if(data.status !== 'DELETED') {
					searchConfig.initialSearchArgs = {
						location: [ rootInstanceObject.id, rootInstanceObject.title ]
					};
				}
			}
		}).fail(function(data, textStatus, jqXHR) {
			console.error(arguments);
        }).always(function() {
			EMF.search.basic.init('.basic-search-placeholder', searchConfig);
        });
	} else {
		EMF.search.basic.init('.basic-search-placeholder', searchConfig);
	}
};