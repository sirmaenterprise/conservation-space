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
			doSearchAfterInit			: false,
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
				orderBy					: 'emf:modifiedOn',
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
			// Property used to enable search form toggler in basic search. If set to
			// true then basic search will be shown collapsed with a clickable
			// link to expand/collapse it.
			toggleSearchForm            : false,
			dateFormatPattern			: SF.config.dateFormatPattern,
			firstDay					: SF.config.firstDay,
			monthNames 					: SF.config.monthNames,
			monthNamesShort				: SF.config.monthNamesShort,
			dayNames					: SF.config.dayNames,
			dayNamesMin 				: SF.config.dayNamesMin,

			search						: contextPath + '/service/search/basic',
			query						: contextPath + '/service/search/query',
			locationOptionsReload		: contextPath + '/service/search/quick',
			objectTypeOptionsLoader		: contextPath + '/service/definition/all-types',
			objectRelationOptionsLoader	: contextPath + '/service/relationships',
			searchUsersUrl				: contextPath + '/service/users',
			solrSearch					: contextPath + '/service/search/solr',
			executor					: contextPath + '/service/executor',
			orderByFieldsLoader			: function() {
				return [
				        { name: _emfLabels['cmf.basicsearch.sort_results.modified_on'], value: 'emf:modifiedOn'},
						{ name: _emfLabels['cmf.basicsearch.sort_results.modified_by'], value: 'emf:modifiedBy'},
				        { name: _emfLabels['cmf.basicsearch.sort_results.title'], value: 'dcterms:title'},
				        { name: _emfLabels['cmf.basicsearch.sort_results.type'], value: 'emf:type'},
				        { name: _emfLabels['cmf.basicsearch.sort_results.created_on'], value: 'emf:createdOn'},
				        { name: _emfLabels['cmf.basicsearch.sort_results.created_by'], value: 'emf:createdBy'}
				];
			},

			resultItemDecorators		: [ ],

			onSearchCriteriaChange		: [ ],
			onAfterSearch				: [ ],
			onItemAfterSearch			: [ ],
			onBeforeSearch				: [ ],

			listeners					: { },
			libraryContext				: null,
			filterId                    : null,
			doSearchAfterLoadSavedSearch  : true
		}, options);

		this.searchArgs = $.extend(true, { }, this.options.initialSearchArgs);
		// To be resolved once the search is fully initialized
		this.isInitialized = $.Deferred();
		this.init();
	}

	EmfBasicSearch.prototype = {
		eventNames: {
			TEMPLATE_LOADED: 'basic-search:template-loaded',
			OPTIONS_LOADED: 'basic-search:options-loaded',
			AFTER_SEARCH: 'basic-search:after-search',
			AFTER_SEARCH_ACTION: 'basic-search:after-search-action',
			PERFORM_SEARCH: 'perform-basic-search',
			SEARCH_INITIALIZED: 'basic-search-initialized',
			BEFORE_SEARCH: 'before-search',
			REMOVE_EXTRA_ARGUMENTS: 'basic-search:remove-extra-arguments',
			SWITCH_SEARCH_TYPE: 'basic-search:switch-search-type'
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
				if ((typeof(arguments[0]) !== 'undefined') && (arguments[0].length > 0)) {
					this.field.val(arguments[0]);
					var convertedDate = new Date(arguments[0]);
					this.field.datepicker('setDate', convertedDate);
					this.field.trigger('change');
					this.field.datepicker('refresh');
				} else if((typeof(arguments[0]) !== 'undefined') && (arguments[0].length === 0)) {
					this.field.datepicker('setDate', null);
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
					var USERS_SEPARATOR = ", ";
					//when restoring old field selection, replace the "current-user" constant with its label from the bundle
					arguments[1] = arguments[1].map(function(user) {
						var pattern = new RegExp(EMF.search.CURRENT_USER, "g");
						return user.replace(pattern, _emfLabels["search.current.user"]);
					});
					var value = arguments[1].join(USERS_SEPARATOR);
					this.field.val(value);
					this.field.get(0).label = arguments[0].join(USERS_SEPARATOR);
					this.field.trigger('autocompletechange');
				} else if (arguments[0]) {
					if (arguments[0].length > 0){
						arguments[0].push('');
					}
					this.field.val(arguments[0].join(USERS_SEPARATOR));
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
							var data = [],
								_val = [];
							for ( var int = 0; int < arguments[0].length; int++) {
								_val.push(arguments[0][int]);
								data.push({name: arguments[0][int], title:arguments[1][int]});
							}
							this.field
								.select2("data", data)
								.select2('val', _val)
								.trigger('change');
						} else {
							this.field.select2('val', arguments[0]);
						}
					} else {
						// Reset in main menu is still not working correct
						this.field.select2("data", null).trigger('change');
					}

				} else {
					// CMF-16453 In library (All) and other saved filters predefined context is not calculated correct
					var index = this.field.select2('val').indexOf("");
					if (index > -1){
						var predefinedValues = this.field.select2('val');
						predefinedValues.splice(index, 1);
						if(this.field.select2('data').length !== predefinedValues.length) {
							predefinedValues.shift();
						}
					}
					return predefinedValues || this.field.select2('val');
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
							child.prop('selected', true);
						});
					}

					this.field.trigger('change');
					this.field.trigger('chosen:updated');
				} else {
					var multiple = this.field.prop('multiple') ? true : false;
					var selected = this.field.prop('multiple') ? [ ] : '';

					var selectedSubTypes;
					var selectedSubTypeElements = this.field.find('.sub-type:selected:not(.sub-class)');
					if (selectedSubTypeElements && selectedSubTypeElements.length) {
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

					this.field.find(':selected:not(.sub-type), .sub-class:selected').each(function() {
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
			var predefinedKeys = [ 'emf-search-context-current-project', 'emf-search-context-current-case', 'emf-search-context-current-object' ];
			var predefinedContexts = { };
			var rootInstanceJSON = sessionStorage.getItem('emf.rootInstance');

			if (rootInstanceJSON) {
				var rootInstance = JSON.parse(rootInstanceJSON);
				predefinedContexts[rootInstance.id] = {
					name: rootInstance.id,
					uri: rootInstance.id,
					instanceType: rootInstance.type
				}
			}

    		if (EMF.currentPath) {
				var contextPath = EMF.currentPath;
				if (contextPath.length && contextPath[0].type === 'projectinstance') {
					predefinedContexts[predefinedKeys[0]] = {
						'name' : predefinedKeys[0],
						'title': _emfLabels["cmf.basicsearch.current.project"],
						'uri': contextPath[0].id,
						'rdfType': contextPath[0].rdfType,
						'instanceType': contextPath[0].type
					};
				}

				if (contextPath.length > 1 && contextPath[1].type === 'caseinstance') {
					predefinedContexts[predefinedKeys[1]] = {
						'name' : predefinedKeys[1],
						'title': _emfLabels["cmf.basicsearch.current.case"],
						'uri': contextPath[1].id,
						'rdfType': contextPath[1].rdfType,
						'instanceType': contextPath[1].type
					};
				}

				if (contextPath.length) {
					predefinedContexts[predefinedKeys[2]] = {
						'name' : predefinedKeys[2],
						'title': _emfLabels["cmf.basicsearch.current.object"],
						'uri': contextPath[contextPath.length - 1].id,
						'rdfType': contextPath[contextPath.length - 1].rdfType,
						'instanceType': contextPath[contextPath.length - 1].type
					};
				}
			}

    		_this.predefinedContexts = predefinedContexts;

			_this.element.on(_this.eventNames.TEMPLATE_LOADED, function(data) {
				if (_this.options.toggleSearchForm) {
					var basicSearchWrapperSelector = '#basic-search-wrapper';
					var searchToggle = '<span id="basic-search-toggler" class="basic-search-toggler">' +
									      '<a class="text-primary">' + _emfLabels['cmf.basicsearch.search_form'] + '</a>' +
						               '<strong id="search-caret" class="caret-right"></strong></span>';
					$(basicSearchWrapperSelector).before(searchToggle);

					$('#basic-search-toggler', _this.element).click(function () {
						$(basicSearchWrapperSelector).slideToggle('slow', function() {
							// This is done to override the overflow style, that is set from jquery.
							// If we don't do this the dropdown from basic search will be under the
							// wrapper and won't be usable.
							$(basicSearchWrapperSelector).css('overflow','visible');
							var element = document.getElementById("search-caret");
							if (element.className == "caret"){
								element.className = "caret-right";
							}else {
								element.className = "caret";
							}
						});
					});
				}

				// register these here to avoid race conditions like CMF-9391
				_this.element.on(_this.eventNames.PERFORM_SEARCH, function(event, extraArguments) {
					_this.searchArgs.pageNumber = 1;
					if(extraArguments) {
						_this.searchArgs = $.extend({}, _this.searchArgs, extraArguments);
					} else {
						_this.element.trigger(_this.eventNames.REMOVE_EXTRA_ARGUMENTS,  _this.searchArgs);
					}
					_this._doSearch();
				});
				if (_this.searchArgs.searchType === 'advanced') {
					EMF.search.advanced.buildSPARQLQueryFromCriteria(_this.searchArgs.advancedSearchCriteria, function(queryText) {
						_this.searchArgs.queryText = queryText;
						_this.element.trigger(_this.eventNames.SEARCH_INITIALIZED, _this.searchArgs);
						_this.isInitialized.resolve();
						if (_this.options.doSearchAfterInit) {
							_this.element.trigger('perform-basic-search');
						}
					}, 2);
				} else {
					_this.element.trigger(_this.eventNames.SEARCH_INITIALIZED, _this.searchArgs);
					_this.isInitialized.resolve();
					if (_this.options.doSearchAfterInit) {
						_this.element.trigger('perform-basic-search');
					}
				}
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
			hasUpdatedSearchArguments = false,
			filterId = queryParameters['filterId'];
			// This if handles logic when filter id is given through the URL, which is used in document library.
			// Here is not needed to trigger basic search because document library takes care of this during
			// search initialization.
			if (filterId) {
				_this.options.filterId = filterId;
			} else {
				_.forOwn(queryParameters, function(value, key) {
					// because deparam not gather properly the parameters from the URL
					key = key.replace('?', '');
					if(key in fieldNames) {
					    _this.searchArgs[key] = value;
						hasUpdatedSearchArguments = true;
					}
				});

				if (hasUpdatedSearchArguments) {
					_this.element.on(_this.eventNames.SEARCH_INITIALIZED, function() {
						_this.element.trigger(_this.eventNames.PERFORM_SEARCH);
					});
				}
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
        			_this.template = _.template(data.responseText);

        			var compiledTemplate = _this.template({
        				'savedFilters' 						: _emfLabels['cmf.basicsearch.savedfilters'],
        				'savedFilters_filtername' 			: _emfLabels['cmf.basicsearch.filtername'],
        				'savedFilters_description' 			: _emfLabels['cmf.basicsearch.description'],
        				'savedFilters_update_btn' 			: _emfLabels['cmf.basicsearch.update'],
        				'savedFilters_createnew_btn' 		: _emfLabels['cmf.basicsearch.createnew'],
        				'addLibrary_btn' 					: _emfLabels['cmf.basicsearch.btn.add_library'],
        				'removeLibrary_btn' 				: _emfLabels['cmf.basicsearch.btn.remove_library'],
        				'basicSearch' 						: _emfLabels['cmf.basicsearch.basic_search'],
        				'advancedSearch' 					: _emfLabels['cmf.basicsearch.advanced_search'],
        				'enterSearchTerms' 					: _emfLabels['cmf.basicsearch.enter_search_terms'],
        				'searchTooltip' 					: _emfLabels['cmf.search.containstext.tooltip'],
        				'search_btn' 						: _emfLabels['cmf.btn.search'],
        				'reset_btn' 						: _emfLabels['cmf.btn.reset'],
        				'addAdditionalFilters' 				: _emfLabels['cmf.basicsearch.add_additional_filters'],
        				'objectType' 						: _emfLabels['cmf.basicsearch.object_type'],
        				'objectRelationship' 				: _emfLabels['cmf.basicsearch.object_relationship'],
        				'searchContext' 					: _emfLabels['cmf.basicsearch.search_context'],
        				'dateCreatedFrom' 					: _emfLabels['cmf.basicsearch.date_created_from'],
        				'dateCreatedTo' 					: _emfLabels['cmf.basicsearch.date_created_to'],
        				'createdBy' 						: _emfLabels['cmf.basicsearch.created_by'],
        				'addGroup_btn' 						: _emfLabels['search.advanced.btn.add_group'],
        				'sortResults' 						: _emfLabels['cmf.basicsearch.sort_results']
        			});

        			var templateAsElement = $(compiledTemplate);

                	$(templateAsElement).appendTo(_this.element);

                	// if the basic search toggler is enabled then hide the form on load.
                	if (_this.options.toggleSearchForm) {
                		$('#basic-search-wrapper').hide();
                	}

                	_this.applySearchArguments();
        			_this._initSearchFields();
        			_this._initSaveFilterForm();

        			_this.element.trigger(_this.eventNames.TEMPLATE_LOADED, {
        				template : templateAsElement,
        				settings : _this.options
        			});

        			$("input[name='search-type-switch']", _this.element).attr('disabled', false);
        		}
    		});
		},

		/**
		 * Reset the search with the initial criteria.
		 * @param newSearchArgs if present used instead initial criteria to initialize search
		 */
		resetSearch: function(newSearchArgs, mutable, doSearchAfterReset, doResetSavedSearch) {
			var _this = this
			if (mutable === undefined){
				mutable = true;
			}
			if(newSearchArgs){
				if (!mutable){
					$('.update-search-button', _this.element).hide();
				} else {
					$('.update-search-button', _this.element).show();
				}
			}
			$('.basic-search-facets div').nextAll().remove();
			_this.element.find('#results-count').empty();
			_this.element.find('#order').css('display', 'none');
			// use basic search controllers to reset and adv. search metaText
			var resetSearchArgs = newSearchArgs || _this.options.initialSearchArgs;
			_this.searchArgs.location = resetSearchArgs.location;
			_this.element.trigger(_this.eventNames.REMOVE_EXTRA_ARGUMENTS, resetSearchArgs);
			$.each(this.options.fieldNameToSelectorMapping, function(name, selector) {
				var controller = _this._controllers(name);
				if (controller) {
					controller.value(resetSearchArgs[name], resetSearchArgs[name + 'Value']);
				}
			});
			if (_this.searchArgs.searchType === 'basic') {
				if (doSearchAfterReset) {
					_this._doSearch();
				}
			}
			if (_this.searchArgs.searchType === 'advanced') {
				var initialConfig = {
						initialCriteria: newSearchArgs,
						mutable: mutable,
						onCriteriaLoaded: function() {
							_this.searchArgs.pageNumber = 1;
							if (doSearchAfterReset) {
								_this._doSearch();
							}
						},
						parent: this
					};
				// If search is already initialized then load new criteria
				if (this._initAdvancedSearch(initialConfig)) {
					// Old SPARQL search
					if (newSearchArgs && newSearchArgs.queryText) {
						this.element.data('EmfAdvancedSearch').loadCriteria(null, mutable, function() {
							$('.search-query', _this.element).val(newSearchArgs.queryText);
			    			_this.searchArgs.pageNumber = 1;
			    			if (doSearchAfterReset) {
								_this._doSearch();
							}
						}, _this);
					} else {
						if(newSearchArgs){
							this.element.data('EmfAdvancedSearch').loadCriteria(newSearchArgs, mutable, function() {
				    			_this.searchArgs.pageNumber = 1;
				    			if (doSearchAfterReset) {
									_this._doSearch();
								}
							}, _this);
						} else {
							if (mutable === false){
								this.element.data('EmfAdvancedSearch').clear();
							} else {
								this.element.data('EmfAdvancedSearch').loadCriteria(null, mutable, function() {
					    			_this.searchArgs.pageNumber = 1;
					    			if (doSearchAfterReset) {
										_this._doSearch();
									}
								}, _this);
							}
						}
					}
				}
			}
			_this.element.find('.basic-search-results-wrapper .basic-search-results').children().remove();
			_this.element.find('.basic-search-pagination').hide();

			if(doResetSavedSearch) {
				_this.element.find('.load-search').select2('val', '');
				_this.element.find('.search-name').val('').change();
				_this.element.find('.search-description').val('').change();
			}
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

			var url = searchData.url;

			this._onBeforeSearch(searchData);
			var advancedSearchCriteria = searchData.args.advancedSearchCriteria;
			delete searchData.args.advancedSearchCriteria;
			url += '?' + $.param(searchData.args);
			searchData.args.advancedSearchCriteria = advancedSearchCriteria;
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
				fields: searchArgs.fields,
				metaText: searchArgs.metaText,
				// TODO: Move in the facet module somehow!
				facetArguments: searchArgs.facetArguments,
				loadAvailableFacets: searchArgs.loadAvailableFacets,
				facetField: searchArgs.facetField,
				cancelSearch: searchArgs.cancelSearch
			};
		},

		/**
		 * Creates forType string for all types selected in the search.
		 * For example: emf:Case,emf:Project_GEP11111
		 */
		buildForType: function() {
			var forType = '';
			if (this.searchArgs.searchType === 'basic') {
				var objectTypeElement = $(this.options.fieldNameToSelectorMapping.objectType, this.element);
				var objectTypeValue = objectTypeElement.data('controller').value();
				if ( Object.prototype.toString.call( objectTypeValue ) === '[object Array]' ) {
					forType = objectTypeValue.join();
				} else {
					var allTypes = [].concat(objectTypeValue.selected);
					_.each(objectTypeValue.selectedSubTypes, function(subType) {
						if (subType.indexOf(':') === -1) {
							var parentType = $(objectTypeElement.children('option[value="' + subType + '"]').prevAll('.category')[0]).attr('value');
							if (parentType) {
								allTypes.push(parentType + '_' + subType);
							}
						} else {
							allTypes.push(subType);
						}
					});
					forType = allTypes.join();
				}
			} else if (this.searchArgs.searchType === 'advanced') {
				forType = this.element.data('EmfAdvancedSearch').buildForType();
			}
			return forType;
		},

		_doSearch: function(resultHandler) {
			var _this = this;
			var searchData = {};
			searchData.url = this.options.search;
			if(this.options.searchByQuery) {
				searchData.url = this.options.query;
			}
			if (this.searchArgs.searchType === 'basic') {
				searchData.args = $.extend({}, this.searchArgs);
				delete searchData.args.fq;
				delete searchData.args.advancedSearchCriteria;
				delete searchData.args.queryText;
			} else if (this.searchArgs.searchType === 'advanced') {
				searchData.args = this._copyCommonSearchArgs(this.searchArgs);
				searchData.args.queryText = this.searchArgs.queryText;
				searchData.args.advancedSearchCriteria = this.searchArgs.advancedSearchCriteria;
			}
			searchData.args.forType = this.buildForType();

			this.element.trigger(_this.eventNames.BEFORE_SEARCH, searchData.args);

			if (!searchData.args.cancelSearch){
				// do some magic before the search, like replacing the context constants with real object URIs
				this._onBeforeSearch(searchData);
				$.each(this.options.onBeforeSearch, function() {
					this.call(_this, searchData);
				});
				EMF.blockUI.showAjaxLoader('.basic-search-placeholder', 'inplace', 'fullHeight');
				//if the created from/to dates are date objects, convert them to formatted strings for the search args
				if(searchData.args.createdFromDate instanceof Date) {
					searchData.args.createdFromDate = $.datepicker.formatDate(SF.config.dateFormatPattern, searchData.args.createdFromDate);
				}
				if(searchData.args.createdToDate instanceof Date) {
					searchData.args.createdToDate = $.datepicker.formatDate(SF.config.dateFormatPattern, searchData.args.createdToDate);
				}
				// It is unnecessary to send the criteria to the server
				var advancedSearchCriteria = searchData.args.advancedSearchCriteria;
				delete searchData.args.advancedSearchCriteria;
				_this._callSearchService(searchData, resultHandler);

				searchData.args.advancedSearchCriteria = advancedSearchCriteria;
			}
			this.searchArgs.cancelSearch = false;
		},

		_callSearchService: function(searchData, resultHandler) {
			var _this = this;
			$.ajax({ dataType: "json", url: searchData.url, data: searchData.args, async: true,
				complete: function(response) {
					var parsed = $.parseJSON(response.responseText);
					if (resultHandler) {
						resultHandler.call(_this, parsed);
					}
					parsed.searchType = searchData.args.searchType;
					_this.element.trigger(_this.eventNames.AFTER_SEARCH, parsed);
					// the next event is for external use
					_this.element.trigger(_this.eventNames.AFTER_SEARCH_ACTION, parsed);
					$.each(_this.options.onAfterSearch, function() {
						this.call(_this, parsed);
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

			// Hide crieria fields if needed
			if(_this.options.hideCriteriaFields) {
				_this._hideCriteriaFields();
			}

			var paginationPlaceholder = $('.basic-search-pagination', _this.element);

			if(data.values && data.values.length > 0) {
    			paginationPlaceholder.pagination({
    				items: data.resultSize,
    				prevText: _emfLabels['cmf.search.pagination.prev'],
    				nextText: _emfLabels['cmf.search.pagination.next'],
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

		_hideCriteriaFields: function() {
			$('.saved-filters-toggler').hide();
			$('.basic-search-toggler').hide();
			$('.order-message').hide();
			$('.chosen-container').hide();
			$('.order-direction').hide();
		},

		_defaultItemRenderer: function(resultItem) {
			var hasThumbnailMarker = (resultItem.icon.substring(0, 5) === 'data:') ? 'has-thumbnail' : '';
    		var content = '';
    			content += '<div class="tree-header default_header">';
    			content += 		'<div class="instance-header ' + resultItem.type + ' ' + hasThumbnailMarker + '">';
    			content += 			'<span class="icon-cell">';
    			content += 				'<img class="header-icon" src="' + resultItem.icon + '" />';
    			content += 			'</span>';
    			content += 			'<span class="data-cell">' + resultItem.default_header + '</span>';
    			content += 		'</div>';
    			content += '</div>';

    		// If basic search is used in document library then we need to open
    		// links in different tabs according to the requirement so we modify
    		// the instance headers with target = '_blank' before they are opened
    		var instanceLinkLocator =	'a';
    		var pathname = window.location.pathname;
        	if (pathname.indexOf("document-library") >= 0) {
        		var tmp = $(content);
        		tmp.find(instanceLinkLocator).attr('target', '_blank');
        		content = $(tmp, instanceLinkLocator).prop('outerHTML');
        	}

			return $(content);
		},

		_renderItems: function(data) {
			var _this = this;
			var viewSearchResults = $('.basic-search-results-wrapper > .basic-search-results', this.element);
			viewSearchResults.children().remove();
			this.element.find('#results-count').empty();

    		var resultSize = data.resultSize;
    		var resultCountMessage = _emfLabels['basicSearch.noResultsFound'];
    		if (resultSize && resultSize >= EMF.config.searchResultMaxSize) {
    			resultCountMessage = _emfLabels['basicSearch.tooManyResults'];
    		} else if (resultSize && resultSize > 0) {
    			resultCountMessage = EMF.util.formatString(_emfLabels['basicSearch.xFound'], resultSize);
    		}

			this.element.find('#results-count').append(resultCountMessage);
    		this.element.find('#order').css('display', 'inline');

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
			//replace the 'current-user' constant with the actual ID of the logged-in user
			if (searchData.args.createdBy) {
				searchData.args.createdBy = searchData.args.createdBy.map(function(value) {
					var pattern = new RegExp(EMF.search.CURRENT_USER, "g");
					return value.replace(pattern, EMF.currentUser.id);
				});
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

				searchData.args = newArgs;
			}

			if (this.options.defaultsIfEmpty) {
				var objectTypes = searchData.args.objectType;
				var subTypes = searchData.args.subType;
				if ((!objectTypes || !objectTypes.length) && !subTypes) {
					searchData.args.objectType = this.searchArgs.objectType;
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
				if (typeof contextObjects === 'string') {
					locationElement.val(contextObjects);
				} else {
					locationElement.val(contextObjects.join(','));
				}
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
							predefinedContexts[predefinedKeys[0]] = { 'name' : predefinedKeys[0], 'title': _emfLabels["cmf.basicsearch.current.project"], 'instanceType': contextPath[0].type };
						}

						if (contextPath.length > 1 && contextPath[1].type === 'caseinstance') {
							predefinedContexts[predefinedKeys[1]] = { 'name' : predefinedKeys[1], 'title': _emfLabels["cmf.basicsearch.current.case"], 'instanceType': contextPath[1].type };
						}

						if (contextPath.length) {
							predefinedContexts[predefinedKeys[2]] = { 'name' : predefinedKeys[2], 'title': _emfLabels["cmf.basicsearch.current.object"] };
						}
					}

		    		var locations = _this.searchArgs.location;
					if (locations && locations.length) {
						var predef = [],
							uris = [];

						_.each(locations, function(uri) {
							if (uri.indexOf(':') === -1) {
								predef.push(uri);
							} else {
								uris.push(uri);
							}
						});

						if (uris.length) {
							params = {
					        	q: 'id:("' + uris.join('" OR "') + '")',
						    	max: 20,
						    	sort: 'score',
						    	dir: 'desc',
						    	fields:'title,id,instanceType'
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
					url: _this.options.search,
					dataType: 'json',
					data: function (term, page) {
			        	if (!term && term.length < 3) {
			        		term = '*';
			        	}
						return {
							metaText: term,
				          	pageSize: 20
						}
					},
					results: function (data, page) {
						var result = data.values || [ ];

						if (result.length) {
							var allContexts = locationElement.data('allContexts') || { };
							result.forEach(function(item) {
								allContexts[item.dbId] = item;
							});
							locationElement.data('allContexts', allContexts);
						}
						return { results: _.values(predefinedContexts).concat(result) };
					}
				},
				formatResult: function(item) {
					if(item.dbId) {
						var itemWrapper = '<div>';
						itemWrapper += 		'<img class="header-icon" src="' + EMF.util.objectIconPath(item.type, 16) + '" width="16" />';
						itemWrapper += 		'<span class="data-cell"> ' + $(item.compact_header).find('a').text() + '</span>';
			    		itemWrapper += '</div>';
			    		return $(itemWrapper);
					} else {
						return item.title;
					}
				},
				formatSelection: function(item) {
		    		if(item.dbId) {
						return $(item.compact_header).find('a').text();
					} else {
						return item.title; 
					}
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

    		// Without this location field stays always open in object pickers, break some acceptance test
    		// and stay can not be closed even after picker is closed.
    		locationElement.focusout(function() {
    			setTimeout(function() {
			        if($(':focus')[0]) {
			        	locationElement.select2("close");
			        }
				}, 0);
    		});

    		objectTypeElement
				.on('change', changeHandler)
				.change(function(event, params) {
					objectRelationshipElement.trigger('filter-relationships');
					if(params && params.deselected) {
						//this prevents the element from losing focus after deselecting
						$(this).trigger('chosen:activate');
					}
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
    								fixedSubTypes.push(parentType);
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
    					var item = _this.predefinedContexts[uri],
    						instanceType;

    					if (item) {
    						instanceType = item.instanceType;
    					} else {
    						if (allContexts) {
    							item = allContexts[uri];
        						if (item) {
        							instanceType = item.type;
        						}
    						}
    					}

    					if (instanceType) {
    						rangeClasses.push(uri + '|' + instanceType);
    					}
    				});

    				_.each(rangeClasses, function(item) {
    					rangeFilter += 'rangeFilterIds=' + encodeURIComponent(item) + '&';
    				});

    				if (domainFilter || rangeFilter) {
    					filterUrl += '?' + domainFilter + rangeFilter;
    				}

    				$.get(
    					filterUrl,
    					function success(response) {
    						var controller = objectRelationshipElement.data('controller'),
    							value = controller.value();

    						objectRelationshipElement.trigger(_this.eventNames.OPTIONS_LOADED, {
    							values: response,
    							options: _this.options
    						});

    						controller.value(value);
    					}
    				);
    			});

    		orderByElement.on('change', changeHandler);
    		$(".order-by").change(function(event, selection) {
    			if (selection) {
    				_this._doSearch();
    			}
    		});

    		orderDirectionElement.on('click', function(event) {
    			_this.searchArgs.pageNumber = 1;
    			orderDirectionElement.find('span').toggleClass('hide');
    			_this.searchArgs.orderDirection = _this.searchArgs.orderDirection === 'desc' ? 'asc' : 'desc';
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
					predefinedContexts.push({ 'name' : 'emf-search-context-current-project', 'title': _emfLabels["cmf.basicsearch.current.project"], 'instanceType': contextPath[0].type });
				}

				if (contextPath.length > 1 && contextPath[1].type === 'caseinstance') {
					predefinedContexts.push({ 'name' : 'emf-search-context-current-case', 'title': _emfLabels["cmf.basicsearch.current.case"], 'instanceType': contextPath[1].type });
				}

				if (contextPath.length) {
					predefinedContexts.push({ 'name' : 'emf-search-context-current-object', 'title': _emfLabels["cmf.basicsearch.current.object"], 'instanceType': contextPath[contextPath.length - 1].type });
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
						predefinedContexts.push({ 'name' : rootInstanceObject.id, 'title': rootInstanceObject.title, 'instanceType': rootInstanceObject.type });
					}
				}).fail(function(data, textStatus, jqXHR) {
					console.error(arguments);
	            });
    		}

    		$.each(predefinedContexts, function() {
    			var option = $('<option value="' + this.name + '">' + this.title + '</option>');

    			if (_this.searchArgs.location && _this.options.isMainSearch && ($.inArray(this.name, _this.searchArgs.location) < 0)) {
    				locationElement.select2("data", { name: this.name, title: this.title });
    				option.attr('selected', 'selected');
	    			var eventData = { selected: option.value };
	    			locationElement.trigger('change', eventData);
	    		}
    		});
    		_this._loadOptions(_this.options.objectRelationOptionsLoader, objectRelationshipElement, chosenConfigMultiSelect);

    		var initial = (_this.searchArgs.objectType || []).concat(_this.searchArgs.subType || []);

    		_this._loadOptions(_this.options.objectTypeOptionsLoader, objectTypeElement, chosenConfigMultiSelect, null, initial);
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
    		createdFromElement.bind("paste", function(e) {
    	          e.preventDefault();
    	    });
    		createdFromElement.val(_this.searchArgs.createdFromDate);

    		createdToElement.datepicker($.extend(datePickerSettings, {
    			onClose: function(selectedDate) {
    		    	createdFromElement.datepicker("option", "maxDate", selectedDate);
    		    }
    		}));
    		createdToElement.on('change', inputValueChangeHandler);
    		createdToElement.bind("paste", function(e) {
  	          e.preventDefault();
    		});
    		createdToElement.val(_this.searchArgs.createdToDate);

    		createdByElement.bind('keydown', function(event) {
		    	if (event.keyCode === $.ui.keyCode.TAB && $(this).data('ui-autocomplete').menu.active) {
		    		event.preventDefault();
		        }
		    }).autocomplete({
		    	source : function(request, response) {
		    		function customSuccessHandles(data, textStatus, jqXHR) {
		    			var currentUser = {
								id: EMF.search.CURRENT_USER,
								label: _emfLabels["search.current.user"],
								type: "user",
								value: EMF.search.CURRENT_USER
							};
						data.items.unshift(currentUser);
		    			response(data.items, textStatus, jqXHR);
		    		}
		            return $.getJSON(_this.options.searchUsersUrl, { term: _this._extractLast(request.term) }, customSuccessHandles);
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
		        	//this mapping is needed later when building the search args from the text input
		        	this.usersLookup = this.usersLookup || {};
		        	//seems weird, but since it is a plain text input, there is no other way...
		        	this.usersLookup[ui.item.label] = ui.item.id;
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
    				var _thisElement = this;
    				if(_label && !this.usersLookup) {
    					array.label = _label.replace(/,\s*$/, '').split(/\s*,\s*/);
    					this.usersLookup = {};
    					//re-construct the lookup map from the saved filter data. Map the user display names to their IDs.
    					_.forEach(array.value, function(userDisplayName, index) {
    						_thisElement.usersLookup[userDisplayName] = array.label[index];
    	        		});
    				} else if (this.usersLookup) {
    					var userIDs = [];
    					//get all split user display names from the input and map them to their user IDs
        				//which will be stored into the final search arguments
        				_.forEach(array.value, function(displayName) {
        					var userName = _thisElement.usersLookup[displayName];
        					if (userName) {
        						userIDs.push(userName);
        					}
    	        		});
        				array.label = userIDs;
    				} else {
    					array.label = _value.replace(/,\s*$/, '').split(/\s*,\s*/);
    				}
    			}
    			_this._onSearchCriteriaChange(event, array);
	        });
    		createdByElement.val((_this.searchArgs.createdByValue || []).join(', '));

    		searchMetaFieldElement.on('change', inputValueChangeHandler);
    		searchMetaFieldElement.val(_this.searchArgs.metaText);

    		_this.element.on(_this.eventNames.AFTER_SEARCH, function(event, data) {
    			_this._afterSearchHandler(event, data);
    		});

    		$('.search-btn', this.element).click(function() {
    			_this.element.trigger(_this.eventNames.PERFORM_SEARCH);
    		});

    		$('.search-meta', this.element).keyup(function(event) {
    			if(event.keyCode === 13) {
        			_this.element.trigger(_this.eventNames.PERFORM_SEARCH);
    			}
    		});

    		$("input[name='search-type-switch']", this.element).change(function(e, skipInitialization){
    			_this.searchArgs.searchType = $(this).val();
    			_this._switchSearchForms(skipInitialization);
    		});

    		resetSearchElement.click(function() {
    			_this.resetSearch(undefined, true, false, true);
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
		 * Switch between different search forms based on searchArgs.searchType property.
		 *
		 * @param skipInitialization
		 *            skip search initialization. This is used when a
		 *            saved filter is loaded because initialization is
		 *            called at later stage
		 */
		_switchSearchForms: function(skipInitialization) {
			var basicSearchEl = $($(this.element).find('.basic-search-criteria')[0]);
			var advancedSearchEl = $($(this.element).find('.advanced-search')[0]);

			basicSearchEl.hide();
			advancedSearchEl.hide();

			if (this.searchArgs.searchType === 'basic') {
				basicSearchEl.show();
			} else if (this.searchArgs.searchType === 'advanced') {
				advancedSearchEl.show();
				if (!skipInitialization) {
					this._initAdvancedSearch();
				}
			}
			this.element.trigger(this.eventNames.SWITCH_SEARCH_TYPE, this.searchArgs.searchType);
			$("input[name='search-type-switch'][value='" + this.searchArgs.searchType + "']", this.element).prop('checked', true);

		},

		/**
		 * Initialize create/update/load filter form.
		 */
		_initSaveFilterForm: function() {
			var _this = this,
				existingFilters = {
					'basic': {
						text: _emfLabels["search.filters.group.basic"],
						type: 'basic',
						children: []
					},
					'advanced': {
						text: _emfLabels["search.filters.group.advanced"],
						type: 'advanced',
						children: []
					}
				},
				savedFilters = $('.saved-filters', _this.element).hide(),
				savedSearchesDropdown = $('.load-search', _this.element),
				searchNameField = $('.search-name', _this.element),
				searchDescrField = $('.search-description', _this.element),
				updateSearchButton = $('.update-search-button', _this.element),
				saveSearchButton = $('.save-search-button', _this.element),
				addLibraryButton = $('.add-library-button', _this.element).hide(),
				removeLibraryButton = $('.remove-library-button', _this.element).hide(),
				shownAjaxLoadersCount = 0;

			function buildFilterProperties() {
				var properties = {
					filterType : _this.searchArgs.searchType,
					title : searchNameField.val(),
					description : searchDescrField.val()
				};

				if (_this.searchArgs.searchType === 'basic') {
					var createdToSelector = _this.options.fieldNameToSelectorMapping.createdToDate;
					var createdFromSelector = _this.options.fieldNameToSelectorMapping.createdFromDate;
					//The dates here must be passed as empty strings to the server, otherwise the server
					//tries to parse the dates as 'null' and throws an exception.
					var createdFrom = $(createdFromSelector).datepicker("getDate");
					if (createdFrom === null){
						createdFrom = "";
					}
					var createdTo = $(createdToSelector).datepicker("getDate");
					if (createdTo === null){
						createdTo = "";
					}
					_this.searchArgs.createdFromDate = createdFrom;
					_this.searchArgs.createdToDate = createdTo;
					properties.filterCriteria = JSON.stringify(_this.searchArgs);
				} else if (_this.searchArgs.searchType === 'advanced') {
					var criteria = _this.element.data('EmfAdvancedSearch').buildSanitizedCriteria();
					// if have metaText args then add it to saved search criteria
					if (_this.searchArgs.metaText && _this.searchArgs.metaText.length > 0) {
						criteria.push({'metaText' : _this.searchArgs.metaText});
					}
					properties.filterCriteria = JSON.stringify(criteria);
				}
				return properties;
			}

			function createOperationData(operation, filterId) {
				return {
					operations : [{
						id : filterId,
						operation : operation,
						properties : buildFilterProperties()
					}]
				};
			}

			function operationExecutorErrorHandler(errorMsg) {
				return function(response) {
					var result = $.parseJSON(response.responseText),
						operation;

					if (result && result.operations) {
						operation = result.operations[0];
						if (operation.responseState && operation.responseState.message) {
							errorMsg = operation.responseState.message;
						}
					}

					EMF.dialog.open({
						message : errorMsg,
						notifyOnly : true,
						width : 'auto'
					});
				};
			}

			function operationExecutorSuccessHandler(opCode, type) {
				return function(response) {
					var operation,
						operationResponse,
						filterItem,
						emfAdvancedSearch = _this.element.data('EmfAdvancedSearch');

					if (!response || !response.operations) {
						return;
					}

					operation = response.operations[0];
					operationResponse = operation.response;
					if (operation.responseState.status !== 'COMPLETED') {
						return;
					}

					_this.options.filterId = operationResponse.id;
					filterItem = {
						id : operationResponse.id,
						text : operationResponse.title,
						mutable : operationResponse.mutable,
						breadcrumb_header : operationResponse.breadcrumb_header
					}

					if (opCode === 'createFilter') {
						_this.searchArgs.mutable = operationResponse.mutable;
						if (operationResponse.mutable !== false && emfAdvancedSearch) {
							emfAdvancedSearch.unlockAll();
						}
						updateSearchButton.show();
						updateSearchButton.prop('disabled', false);
						_this.searchArgs.canUpdate = true;
					} else if (opCode === 'updateFilter') {
		        		// Removing the previous option in case the type was changed.
		        		_.forEach(existingFilters, function(group) {
		        			_.remove(group.children, {
		        				id : _this.options.filterId
		        			});
		        		});
					}

					existingFilters[type].children.push(filterItem);
					savedSearchesDropdown.select2("data", {
						id : operationResponse.id,
						breadcrumb_header : operationResponse.breadcrumb_header
					});

					updateLibraryButtons(_this.options.libraryContext, _this.options.filterId);
				};
			}

			function updateLibraryButtons(libraryContext, filterId) {
				var libActionsUrl,
					checkHasLibraryUrl;

				if (libraryContext) {
					libActionsUrl = EMF.servicePath + '/instances/' + encodeURI(libraryContext.instanceId) +
						'/actions?instanceType=' + libraryContext.instanceType + '&placeholder=library';
					checkHasLibraryUrl = EMF.servicePath + '/libraries/' +
												encodeURI(filterId) + '/attachments/' +
												encodeURI(libraryContext.instanceId) + '?instanceType=' + libraryContext.instanceType;

					EMF.blockUI.showAjaxLoader();

					$.ajax({
						url: checkHasLibraryUrl,
						type: 'HEAD',
						complete: function(jqXHR) {
							var detached = jqXHR.status === 404;

							$.ajax({
								url: libActionsUrl,
								type: "GET",
								contentType: "application/json",
								success: function(response) {
									response.actions.forEach(function(action) {
										var actionId = action.action;
										switch (actionId) {
										case 'addLibrary':
											addLibraryButton.val(action.label);
											detached ? addLibraryButton.show() : addLibraryButton.hide();
											break;
										case 'removeLibrary':
											removeLibraryButton.val(action.label);
											detached ? removeLibraryButton.hide() : removeLibraryButton.show();
											break;
										default:
											break;
										}
									});
								},
								complete: EMF.blockUI.hideAjaxBlocker
							});
						}
					});

				} else {
					addLibraryButton.hide();
					removeLibraryButton.hide();
				}
			}

			function addLibraryClickHandler() {
				var url = EMF.servicePath + '/libraries/' + _this.options.filterId + '/attachments';

				//Extract the information needed to add filter and reuse parameters to redirect to the newly added filter
				var theCurrentInstanceId = _this.options.libraryContext.instanceId;
				var instanceType = _this.options.libraryContext.instanceType;
				var loadedFilterId =_this.options.filterId;

				EMF.blockUI.showAjaxLoader();
				$.ajax({
					url: url,
					type: 'PUT',
					contentType: "application/json",
					data: JSON.stringify({ instanceId : theCurrentInstanceId, instanceType :instanceType }),
					success: function() {
						addLibraryButton.hide();
						removeLibraryButton.show();
						EMF.blockUI.hideAjaxBlocker;
					},
					complete: function() {
						 document.getElementById("libraryLinksForm:refreshLibrary").click();
					}
				});
			}

			/**
			 * Calls a rest that detaches a filter with specific id from the library.
			 */
			function removeLibraryClickHandler() {
				var url = EMF.servicePath + '/libraries/' + _this.options.filterId + '/detach';
				EMF.blockUI.showAjaxLoader();
				$.ajax({
					url: url,
					type: 'PUT',
					contentType: "application/json",
					data: JSON.stringify({
						instanceId: _this.options.libraryContext.instanceId,
						instanceType: _this.options.libraryContext.instanceType
					}),
					success: function() {
						addLibraryButton.show();
						removeLibraryButton.hide();
						document.getElementById("libraryLinksForm:refreshLibrary").click();
					},
					complete: EMF.blockUI.hideAjaxBlocker
				});
			}

			addLibraryButton.click(addLibraryClickHandler);
			removeLibraryButton.click(removeLibraryClickHandler);

			$('.saved-filters-toggler', _this.element)
				.click(function () {
					savedFilters.slideToggle();
					var element = document.getElementById("saved-caret");
					if (element.className == "caret"){
						element.className = "caret-right";
					}else {
						element.className = "caret";
					}
				})
				.find('.text-primary')
					.text(_emfLabels["search.filters.toggler"]);

			savedSearchesDropdown.select2({
				width:'200px',
				height:'32px',
				placeholder: _emfLabels["cmf.basicsearch.loadfilter"],
				allowClear: true,
				data: _.values(existingFilters),
				containerCssClass: 'headers-with-icons',
				dropdownCssClass: 'headers-with-icons saved-filters-dropdown-item',
				escapeMarkup: function(m) {
					 return m;
				},
				formatSelection: function(item) {
					return item.breadcrumb_header || item.text;
				},
				formatResult: function(item) {
					var filterLabel = item.breadcrumb_header || item.text;
						// TODO: see if solr can allways return true here, or use custom service
					if (item.mutable === undefined){
						var mutable = true;
					} else {
						var mutable = item.mutable;
					}
					if(!mutable) {
						// TODO: Create an angular directive that displays glypicons !
						filterLabel =  "<span aria-hidden='true' class='glyphicon glyphicon-star' style='margin-right: 4px'></span>" + filterLabel;
					}
					return filterLabel;
				}
			}).change(function(evt, reset) {
				var shortUri = $(evt.target).select2('val');

				var emfAdvancedSearch = _this.element.data('EmfAdvancedSearch'),
					libraryContext = _this.options.libraryContext,
					libActionsUrl,
					checkHasLibraryUrl;

				if(!_this.searchArgs.mutable &&  emfAdvancedSearch){
					emfAdvancedSearch.unlockAll();
					_this.searchArgs.mutable = true;
				}

				if (!shortUri || reset) {
					searchNameField.val('').change();
					searchDescrField.val('').change();

					_this.options.filterId = null;
					updateSearchButton.hide();
					addLibraryButton.hide();
					removeLibraryButton.hide();
					return;
				}

				updateSearchButton.show();
				var loadFilter = $.ajax({
					url: EMF.servicePath + '/savedfilters/' + encodeURI(shortUri),
					type: "GET",
					contentType: "application/json",
					error: function() {
						EMF.dialog.open({
							message      : _emfLabels["cmf.basicsearch.errorloadfilter"],
							notifyOnly   : true,
							width		 : 'auto'
						});
					}
				});

				$.when(loadFilter).then(function(response) {
					var criteria = $.parseJSON(response.filterCriteria);
					if (response.filterType === 'basic' && criteria.location && criteria.location.length > 0) {
						var requestData = {};
						requestData.fields = 'title,id,rdfType,instanceType';
						var payloadData = {};
						payloadData.ids = [];

						criteria.location.forEach(function(uri){
							uri = uri.substr(uri.indexOf('#') + 1);
							payloadData.ids.push('\"*' + uri + '\"');
						});

						EMF.blockUI.showAjaxLoader();
						$.ajax({
							url: EMF.servicePath + '/search/find/id?' + $.param(requestData),
							type: "POST",
							contentType: "application/json",
							data: JSON.stringify(payloadData),
							success: function(contextResponse) {
								var result = contextResponse.data || [ ];
								if (result.length) {
									var locationElement = $(_this.options.fieldNameToSelectorMapping.location, _this.element);
									var allContexts = locationElement.data('allContexts') || { };
									result.forEach(function(item) {
										allContexts[item.uri] = item;
									});
									locationElement.data('allContexts', allContexts);
								}
								loadSavedFilterData(shortUri, response);
							},
							error: function() {
								EMF.dialog.open({
									message      : _emfLabels["cmf.basicsearch.errorloadfilter"],
									notifyOnly   : true,
									width		 : 'auto'
								});
							},
							complete: EMF.blockUI.hideAjaxBlocker
						});
					} else {
						loadSavedFilterData(shortUri, response);
					}
				});

				updateLibraryButtons(libraryContext, shortUri);
			});

			// Loading the filters from Solr.
			EMF.blockUI.showAjaxLoader();
			$.get('/emf/service/search/basic', {
				objectType:[ "emf:SavedFilter" ],
				fields : ["title", "filterType", "mutable", "breadcrumb_header"],
				searchType:"basic",
				maxSize: 10000,
				pageSize: 10000
			}, function(response) {
				if (response.values) {
					response.values.forEach(function (item) {
					var filterType = item.filterType;
					if (filterType) {
						existingFilters[filterType].children.push({
							id : item.dbId,
							text : item.title,
							mutable : item.mutable,
							breadcrumb_header : item.breadcrumb_header
						});
						if (_this.options.filterId !== null && _this.options.filterId === item.dbId) {
							_this.loadSavedFilter(_this.options.filterId);
						}
					}
					});
				}
				EMF.blockUI.hideAjaxBlocker();
			});

			searchNameField
				.on('change propertychange keyup input paste', function(evt) {
					var newValue 	= $.trim($(evt.target).val()),
						disabled	= (newValue === '');

					updateSearchButton.prop('disabled', disabled || !_this.searchArgs.canUpdate);
					saveSearchButton.prop('disabled', disabled);
				})
				.change();

			saveSearchButton.click(function() {
				var data = createOperationData('createFilter', null);

				EMF.blockUI.showAjaxLoader();
				$.ajax({
					url : _this.options.executor,
					type : "POST",
					contentType : "application/json",
					data : JSON.stringify(data),
					success : operationExecutorSuccessHandler('createFilter', data.operations[0].properties.filterType),
					error : operationExecutorErrorHandler(_emfLabels["cmf.basicsearch.errorcreatefilter"]),
					complete : EMF.blockUI.hideAjaxBlocker
				});
			});

			updateSearchButton.click(function() {
				var data = createOperationData('updateFilter', _this.options.filterId);

				EMF.blockUI.showAjaxLoader();
				$.ajax({
					url : _this.options.executor,
					type : "POST",
					contentType : "application/json",
					data : JSON.stringify(data),
					success : operationExecutorSuccessHandler('updateFilter', data.operations[0].properties.filterType),
					error : operationExecutorErrorHandler(_emfLabels["cmf.basicsearch.errorupdatefilter"]),
					complete : EMF.blockUI.hideAjaxBlocker
				});
			});

			function loadSavedFilterData(shortUri, response) {
				var searchDescription 	= response.description || '',
				searchName 	            = response.title || '',
				type 					= response.filterType,
				criteria 				= $.parseJSON(response.filterCriteria);
				_this.searchArgs.canUpdate = response.canUpdate;
				
				if (response.mutable === undefined){
					var mutable = true;
				} else {
					var mutable = response.mutable;
				}

				_this.options.filterId = shortUri;
				searchNameField.val(searchName).trigger('change');
				searchDescrField.val(searchDescription);

				if (criteria) {
					_this.searchArgs.searchType = type;
					_this.searchArgs.mutable = mutable;

					$("input[name='search-type-switch'][value='" + _this.searchArgs.searchType + "']", _this.element)
						.prop('checked', true)
						// Adds additional parameter for skipInitialization.
						// Initialization is done in resetSearch function
						.trigger('change', true);

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
						_this.resetSearch(criteria, mutable, _this.options.doSearchAfterLoadSavedSearch, false);
					} else {
						var promises = [];
						// var for saved metaText Criteria
						var metaTextCriteria = null;
						criteria.forEach(function(groupCriteria) {
							// check because metaText Criteria is not an array
							if ($.isArray(groupCriteria.criteria)) {
								groupCriteria.criteria.forEach(function(criterion) {
								 if (criterion.dynamicCriteria) {
									var promise = EMF.search.advanced.buildSPARQLQueryFromCriteria(criterion.dynamicCriteria, null, 1).done(function(result) {
									  criterion.dynamicQuery = result;
									});
									promises.push(promise);
								  }
								});
							} else {
								// assign metaText Criteria
								metaTextCriteria = groupCriteria;
							}
						});
						if (metaTextCriteria != null) {
							// remove the metaText Criteria from Advanced Search Criteria Array
							var metaTextCriteriaIndex = criteria.indexOf(metaTextCriteria);
							criteria.splice(metaTextCriteriaIndex, 1);
							// add the metaText Criteria to default searchArgs
							_this.searchArgs.metaText = metaTextCriteria.metaText;
							criteria.metaText = metaTextCriteria.metaText;
						} else {
							// reset the metaText Criteria in default searchArgs, because we may have loaded other saved search before that
							_this.searchArgs.metaText = "";
							criteria.metaText = "";
						}
						$.when.apply($, promises).done(function() {
							_this.resetSearch(criteria, mutable, _this.options.doSearchAfterLoadSavedSearch, false);
						});
					}
				}
			}
		},

		loadSavedFilter : function(uri) {
			$('.load-search', this.element).select2('val', uri).trigger('change');
		},

		/**
		 * Initialize advanced search form and load initial query if any
		 *
		 * @returns true if search is already initialized
		 */
 		_initAdvancedSearch: function(config) {
 			config = config || {};
			var _this = this;
			var isNotInitialized = _this.element.data('EmfAdvancedSearch') === undefined;
			// initialize advanced search the first time only
			if (isNotInitialized) {
				var advSearchCriteria = config.initialCriteria;
				if (_this.searchArgs.searchType === 'advanced' &&
						_this.searchArgs.advancedSearchCriteria &&
						_this.searchArgs.advancedSearchCriteria.length > 0) {
					advSearchCriteria = _this.searchArgs.advancedSearchCriteria;
				};
				config.initialCriteria = advSearchCriteria;
				config.nestingLevel = _this.options.nestingLevel;
				var initialConfig = $.extend({'onAdvancedSearchChanged': function(evt) {
					_this.searchArgs.queryText = $(evt.target).val();
					_this.searchArgs.advancedSearchCriteria = _this.element.data('EmfAdvancedSearch').buildCriteria();
				}}, config);
				_this.element.data('EmfAdvancedSearch', new EMF.search.advanced($(_this.element).find('.advanced-search .advanced-search-criteria')[0], initialConfig));
			}
			return !isNotInitialized;
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

		_loadOptions: function(urlOrFunction, element, chosenConfig, preloadedOptions, initial) {
			var _this = this,
			// this is needed because otherwise IE10 throws Invalid Calling Object error
				preloaded = preloadedOptions;

			// disable the dropdown until options are loaded
			element.prop('disabled', true);
	    	element.chosen(chosenConfig);

	    	$('.chosen-choices').addClass('form-control');

	    	var selectName = element.attr('data-name');
	    	var initialValue = initial || _this.searchArgs[selectName];
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
			element.children().remove();

			// Create the select options, also check for default selected
       		$.each(values, function(index, option) {

       			var optionElement,
       				text = null,
       				attrs = {
       					'class': ''
       				};

       			if(option && option.name && option.title) {
       				if (data.preloaded && _.findIndex(data.preloaded, arraySearchFn, option) !== -1) {
       					return true;
       				}
       				// We must separate objectType from subType !!
		    		if(option.subType) {
		    			attrs['class'] = 'sub-type';
		    			if (option.subClass) {
		    				attrs['class'] += ' sub-class';
		    			}

		    			attrs.parent = option.parent;
		    		}

		    		attrs.value = option.name;
		    		text = option.title;

		    		if(typeof option.objectType !== 'undefined') {
		    			attrs['class'] += ' ' + option.objectType;
		    			attrs.type = option.objectType;
		    		}
	    		} else if (option.name && option.value) {
	    			// http://i436.photobucket.com/albums/qq81/dianaAwesomeo/blam.gif
	    			text = option.name;
	    			attrs.value = option.value;
	    		}

       			if (text) {
       				optionElement = $('<option></option>', attrs)
       					.html(text)
       					.appendTo(element);

       				if (data.initialValue && $.inArray(option.name, data.initialValue) !== -1) {
    		   			optionElement.attr('selected', 'selected');
    		    		element.trigger('change', { selected: option.name });
    		    	}
       			}
       		});

       		// after the element is ready for use - enable it once again
       		var name = element.attr('data-name');
       		var searchFieldConfig = data.options.searchFieldsConfig[name];
       		if (!searchFieldConfig || !searchFieldConfig.disabled) {
       			element.prop('disabled', false);
       		}
       		element.trigger("chosen:updated");
		},

		_onSearchCriteriaChange: function(domEvent, data) {
			var _this = this;
			var element = $(domEvent.target);
			var searchableTypes = [];
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
				_.each(_this.searchArgs.objectType, function(type) {
					searchableTypes.push(type);
				});
				_.each(data.selectedSubTypes, function(subType) {
					searchableTypes.push($(element.children('option[value="' + subType + '"]').prevAll('.category')[0]).attr('value'));
				});
				_this.searchArgs.searchableTypes = searchableTypes;
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
EMF.util.namespace('EMF.search').init = function(externalConfig) {
	var searchConfig = $.extend(true, EMF.search.config(), externalConfig),
		rootInstance = sessionStorage.getItem('emf.rootInstance'),
		queryParameters = $.deparam(location.search.substr(1)) || {},
		library = queryParameters['library'],
		libraryTitle = queryParameters['libraryTitle'],
		libraryLink = window.location.href,
		objectType = queryParameters['objectType'];

	// flag to indicate that search is opened from the menu not from a picker
	searchConfig.isMainSearch = true;
	if($.isArray(objectType)){
		objectType = objectType.join();
	}
	// hide toolbar if not in library
	if(!library || (library !== 'object-library' && library !== 'project-library')) {
		$('.context-data-header').hide();
	} else {

		if(library === 'project-library') {
			$('[id="object"]').remove();
			$('[id="formId:createObjectButton"]').remove();
		} else {
			$('[id="project"]').remove();
			$('[id="formId:newProjectButton"]').remove();
		}

		$('.library-title').attr('data-value', objectType).text(libraryTitle).parent().attr('href', libraryLink);
		// project as context for basic search may be set and not cleared if we come to object library
		// immediately after opening a project, then basic search and then some object library from the main menu
		CMF.utilityFunctions.clearRootInstance();
	}

	// for project search
	if((objectType && objectType === "emf:Project")){
		var libraryLabel = _emfLabels['pm.search.library.label'];
		$('[id="object"]').remove();
		$('[id="formId:createObjectButton"]').remove();
		$('.library-title').attr('data-value', objectType).text(libraryLabel).parent().attr('href', window.location.href);
		$('.context-data-header').show();
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
					searchConfig.searchArgs = {
						location: [ rootInstanceObject.id ]
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
/**
 * Namespace that will be used for document-library and folder pages. This is an entry
 * point for initializing the basic search.
 * <p>
 * This method also loads the facets next basic-search results.
 */
EMF.util.namespace('EMF.search.library').init = function(externalConfig) {
	var config = $.extend(true, externalConfig, {
		libraryContext: {
			instanceId: EMF.documentContext.rootInstanceId,
			instanceType: EMF.documentContext.rootInstanceType
		},
		toggleSearchForm: true
	});

	if(config.isMainSearch) {
		  EMF.search.basic.init(basicSearchPlaceHolder, config);
	 } else {
		  EMF.search.init(config);
	 }
	// initalize the facets for basic-search
	angular.element(document).ready(function() {
	      angular.bootstrap(document, ['facets']);
	});
	// after search executed event id
	$('#contentBody').on('basic-search:after-search-action', function() {
		// set styles
		$('.basic-search-wrapper').css('margin-left', '280px');
		$('.basic-search-facets').css('width', '250px');
	});
};