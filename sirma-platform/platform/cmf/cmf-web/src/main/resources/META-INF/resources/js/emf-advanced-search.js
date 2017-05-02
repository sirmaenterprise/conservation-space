var EMF = EMF || {};

EMF.search = EMF.search || {};

EMF.search.CURRENT_USER = 'emf-search-current-user';

EMF.search.advanced = function(placeholder, config) {
	config = config || {};
	this.config = config;

	var groupIndex = 0;

	var _this = this;
	_this.groups = [];
	_this.groups.push = function () {
		var result = Array.prototype.push.apply(this,arguments);
		toggleDeleteGroupButton(this);
		return result;
	};
	_this.groups.splice = function () {
		var result = Array.prototype.splice.apply(this,arguments);
		toggleDeleteGroupButton(this);
		return result;
	};
	/**
	 * Show/hide delete group button.
	 */
	function toggleDeleteGroupButton(groups) {
		for ( var int = 0; int < groups.length; int++) {
			if (!groups[int].config.mutable) {
				groups[int].element.find('.delete-group-button').hide();
			} else {
				groups[int].element.find('.delete-group-button').show();
			}
		}

		if (groups.length === 1) {
			groups[0].element.find('.delete-group-button').hide();
		}

	};

	var objectTypeServiceURL = '';

	config.contextPath = config.contextPath || SF.config.contextPath;
	
	//if the nestingLevel is already provided, that means it's a nested search (inside an object picker).
	//In that case, just increment its value. Otherwise, set it to 0 (first, not-nested search)
	if($.isNumeric(config.nestingLevel)) {
		config.nestingLevel = ++config.nestingLevel;
	} else {
		config.nestingLevel = 0;
	}

	objectTypeServiceURL = config.contextPath + '/service/definition/all-types?addFullURI=true';

	var queryHolder = $('<div/>', {'class': 'dynamic-form-panel'});
	$(placeholder).prepend(queryHolder);

	// Toggler functionality.
	var advancedSearchToggler = $('<span/>', {'class' : 'advanced-search-toggler'});
	advancedSearchToggler.append($('<a />', {'text':_emfLabels['search.advanced.search_query'], 'class' : 'text-primary' }));
	advancedSearchToggler.append($('<b />', {'class' : 'caret'}));
	advancedSearchToggler.click(function () {
		$($(placeholder).find('.search-query')[0]).slideToggle();
	});
	queryHolder.append(advancedSearchToggler);

	queryHolder.append($('<textarea />', {rows: 4, 'class': 'search-query advanced-search-query boxsizingBorder', 'autocomplete':'off', 'style':'display: none;'}));

	var advancedSearchQuery = $($(placeholder).find('.search-query')[0]);

	if (config.onAdvancedSearchChanged && $.isFunction(config.onAdvancedSearchChanged)) {
		advancedSearchQuery.on('change propertychange keyup input paste', config.onAdvancedSearchChanged);
	}

	var advSearchHolder = $('<div/>');
	$(placeholder).prepend(advSearchHolder);

	config.onChanged = function(group) {
		var groupQueries = [];
		_this.groups.forEach(function(group, i) {
			groupQueries.push(group.buildSPARQLQuery(i));
		});
		advancedSearchQuery.val(EMF.search.advanced.buildFullSPARQLQuery(groupQueries)).trigger('change');
	};

	config.onDeleted = function(group) {
		for ( var int = 0; int < _this.groups.length; int++) {
			if (_this.groups[int] === group) {
				_this.groups.splice(int, 1);
			}
		}
		config.onChanged();
	};

	$.ajax({
		dataType: "json",
		url: objectTypeServiceURL,
		async: true,
		complete: function(response) {
			var result = $.parseJSON(response.responseText);
			var category = null;
			var objectTypes = [];
			var objectTypesParents = {};
			var shortToFullURI = {};

			objectTypes.push({id: 'all', text: 'All', children:[]});
			result.forEach(function(record) {
				if (record.objectType === 'category') {
					if (category !== null) {
						objectTypes.push(category);
					}
					category = {id: record.name, text: record.title, children: []};
				} else {
					category.children.push({id: record.name, text: record.title});
					objectTypesParents[record.name] = category.id;
				}
				if (record.uri) {
					shortToFullURI[record.name] = record.uri;
				}
			});
			objectTypes.push(category);
			config.objectTypes = objectTypes;
			config.objectTypesParents = objectTypesParents;
			config.shortToFullURI = shortToFullURI;
			_this.loadCriteria(config.initialCriteria, config.mutable, config.onCriteriaLoaded, config.parent);
		}
	});

	$($(placeholder).find('.add-search-group-button')[0]).on('click', function() {
		var groupConfig = $.extend(true, {}, config);
		groupConfig.initialCriteria = null;
		groupConfig.index = groupIndex++;
		groupConfig.mutable = true;
		_this.groups.push(new EMF.search.advanced.group(advSearchHolder, groupConfig));
	});

	this.buildSolrQuery = function() {
		var query = '';
		if (_this.groups.length > 1) {
			for ( var int = 0; int < _this.groups.length; int++) {
				var groupQuery = _this.groups[int].buildSolrQuery();
				if (groupQuery !== '') {
					var moreThanOneCriteria = groupQuery.indexOf(' AND ') > -1 || groupQuery.indexOf(' OR ') > -1;
					query += (query!==''?' OR ':'') + (moreThanOneCriteria?'(':'') +  groupQuery + (moreThanOneCriteria?')':'');
				}
			}
		} else if (_this.groups.length === 1){
			query = _this.groups[0].buildSolrQuery();
		}
		return query;
	};

	this.buildCriteria = function() {
		var criteria = [];
		for ( var int = 0; int < _this.groups.length; int++) {
			var groupCriteria = _this.groups[int].buildCriteria();
			if (groupCriteria !== null) {
				criteria.push(groupCriteria);
			}
		}
		return criteria;
	};

	/**
	 * Builds sanitized criteria, removing all redundant fields which should not be exposed or saved as filters.
	 * For example, dynamic queries' SPARQL is not needed outside of the advanced search.
	 */
	this.buildSanitizedCriteria = function() {
		var sanitizedCriteria = this.buildCriteria();
		sanitizedCriteria.forEach(function(groupCriteria) {
			groupCriteria.criteria.forEach(function(criterion) {
			     if (criterion.dynamicQuery) {
			       delete criterion.dynamicQuery;
			     }
			});
		 });
		return sanitizedCriteria;
	};

		/**
		 * Creates forType string for all types selected in the advanced search.
		 * For example: emf:Case,emf:Project_GEP11111
		 */
	this.buildForType = function() {
		var forType = '';
		for ( var int = 0; int < _this.groups.length; int++) {
			if (forType !== '') {
				forType += ',';
			}
			var objectType = _this.groups[int].element.find('.advanced-search-object-type').select2('val');
			if (objectType && objectType != '') {
				if (objectType === 'all') {
					// All types are selected in at least one group
					return '';
				} else if (objectType.indexOf(':') !== -1) {
					forType += objectType;
				} else {
					var parent = config.objectTypesParents[objectType];
					forType += parent + '_' + objectType;
				}
			}
		}
		return forType;
	};

	this.reset = function(doNotAddGroup, callback) {
		for ( var int = 0; int < _this.groups.length; int++) {
			_this.groups[int].element.remove();
		}
		// Clear the array
		while(_this.groups.length > 0) {
			_this.groups.pop();
		}
		if (!doNotAddGroup) {
			config.index = groupIndex++;
			var groupConfig = $.extend(true, {}, config);
			groupConfig.initializedCallback = function() {
				if ($.isFunction(callback)) {
					callback();
				}
			};
			_this.groups.push(new EMF.search.advanced.group(advSearchHolder, groupConfig));
		}
		config.onChanged();
	};
	this.clear = function(){
		for ( var int = 0; int < _this.groups.length; int++) {
			_this.groups[int].element.find('div').find('.input-column .select2-container').select2('data', null).change();
		}

		config.onChanged();
	};
	this.unlockAll = function() {
		for ( var int = 0; int < _this.groups.length; int++) {
			_this.groups[int].unlock();
			if (_this.groups.length > 1) {
				_this.groups[int].element.find('.delete-group-button').show();
			}
		}
		var deleteGroupButtons = $(placeholder).find('.delete-group-button');
		if(deleteGroupButtons.length > 2){
			deleteGroupButtons.show();
		}
		config.mutable = true;
		config.onChanged();
	};
	this.loadCriteria = function(criteria, mutable, callback, parent) {
		config.mutable = mutable;
		if (criteria) {
			//if any dynamic queries are present in the criteria, put them into the lookup map
			criteria.forEach(function(groupCriteria) {
				if ($.isArray(groupCriteria.criteria)) {
					groupCriteria.criteria.forEach(function(criterion) {
					     if (criterion.dynamicCriteria) {
					          //re-generate the key in order to avoid conflicts if using the old one
					          var key = EMF.search.advanced.DYNAMIC_QUERY + '_' + EMF.search.advanced.dynamicQueriesCounter++;
					          //there could be only one dynamic query (and one selection) per field
					          criterion.values[0] = key;
					          EMF.search.advanced.dynamicQueriesLookup[key] = {
				    					dynamicCriteria : criterion.dynamicCriteria,
				    					dynamicQuery :  criterion.dynamicQuery
				    		  };
					     }
					});
				}
			});
			
			this.reset(true);
			var criteriaNumber = criteria.length;
			for ( var int = 0; int < criteria.length; int++) {
				var groupConfig = $.extend(true, {}, config);
				groupConfig.initialCriteria = criteria[int];
				groupConfig.initializedCallback = function() {
					// Criteria are loaded asynchronously, so we call this callback after each criteria is loaded. When all are loaded main callback is called.
					if (--criteriaNumber <= 0) {
						criteriaNumber = 0;
						//wait until the date ranges are loaded, because otherwise the criteria will be built later than the callback
						$.when(EMF.search.advanced.getDateRangesPromise()).then(function() {
							if ($.isFunction(callback)) {
								callback();
							}
						});
					}
				};
				groupConfig.index = groupIndex++;
				_this.groups.push(new EMF.search.advanced.group(advSearchHolder, groupConfig));
			}
		} else {
			delete config.initialCriteria;
			this.reset(false, callback);
		}

	};

	return this;
};

/**
 * Constant to be used for any relation field.
 */
EMF.search.advanced.ANY_RELATION = '-1';

/**
 * Constant to be used for any field(keyword) field.
 */
EMF.search.advanced.ANY_FIELD = '-2';

/** Contains all dynamic queries selected in the advanced search **/
EMF.search.advanced.dynamicQueriesLookup = {};

EMF.search.advanced.dynamicQueriesCounter = 0;

EMF.search.advanced.DYNAMIC_QUERY = 'dynamicQuery';

EMF.search.advanced.INSTANCE_VARIABLE_NAME = 'instance';

EMF.search.advanced.PERMISSIONS_BLOCK = 'permissions_block';

/**
 * The additional configuration block that will be passed to the back-end along with the query.
 */
EMF.search.advanced.additionalQueryBlock = ' $' + EMF.search.advanced.PERMISSIONS_BLOCK +
											'$' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ';

/**
 * Given a query generated by an inner advanced search, modifies the
 * ?instance subject and the $instance name (in the additional block), appending
 * the postfixes for indicating the nesting level, group and row. That's in order to
 * avoid naming conflicts within a single query and nesting level.
 */
EMF.search.advanced.getReplacedInnerInstanceNames = function(query, indexPostfix) {
	var newInstanceSubject = " ?" + EMF.search.advanced.INSTANCE_VARIABLE_NAME + indexPostfix + " ";
	var newInstanceName = '$' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + indexPostfix + " ";

	return query.replace(/\s\?instance\s/g, newInstanceSubject).replace(/\$instance\s/g, newInstanceName);
}

EMF.search.advanced.escapeQuerySpecialCharacters = function(value) {
	var escapedValue = '';
	if (value) {
		for ( var int = 0; int < value.length; int++) {
			var c = value[int];
			//XXX: USE AN ARRAY
			 if (c === '\\' || c === '+' || c === '-' || c === '!'  || c === '(' || c === ')' || c === ':'
			        || c === '^' || c === '[' || c === ']' || c === '\"' || c === '{' || c === '}' || c === '~'
			        || c === '*' || c === '?' || c === '|' || c === '&'  || c === ';' || c === '/'
			        || /\s/.test(c)) {
				 escapedValue += '\\';
			  }
			 escapedValue += c;
		}
	}
	return escapedValue;
};

/**
* Builds the full, final SPARQL query, according the the passed array of group queries.
*
* @param criteria - flat criteria. Before normalization (combine)
* @returns {String}
*/
EMF.search.advanced.buildFullSPARQLQuery = function(groupQueries) {
	var query = '';
	var appendUnion = false;
	groupQueries.forEach(function(groupQuery) {
		if (groupQuery !== '') {
			query += (appendUnion?'\n UNION \n':'') + groupQuery;
			appendUnion = true;
		}
	});
	if (query.trim().length > 0) {
		//the block with permissions and instance name is appended in the bare end of the query
		query += EMF.search.advanced.additionalQueryBlock + '\n';
	}
	return query;
};

EMF.search.advanced.offsetToDate = function(offset) {
	if(_.isUndefined(offset) || offset === null){
		return;
	}

	// Mignight date
	var date = new Date();
	date.setHours(0, 0, 0, 0);

	date.setMilliseconds(date.getMilliseconds() + offset.msOffset);
	date.setHours(date.getHours() + offset.hourOffset);
	date.setFullYear(date.getFullYear() + offset.yearOffset);
	//TODO: Add minute, day, seconds?!

	return date;
};

EMF.search.advanced.escapeRegExp = function(value) {
	return value.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
};

/**
 * Sets the value's type, according to the attributes
 * of the corresponding searchable property.
 */
EMF.search.advanced.assignValueInternalType = function(value) { //NOSONAR
	// The default type is string.
	value.type = 'string';
	if(value.rangeClass === 'long') {
		value.type = 'number';
	} else if(value.rangeClass === 'dateTime' || value.rangeClass === 'date') {
		value.type = value.rangeClass;
	} else if(value.rangeClass === 'boolean') {
		value.type = 'boolean';
	} else if (value.codeLists && value.codeLists.length > 0) {
		value.type = 'autocomplete';
		value.autocompleteField = 'codelist';
		value.additionalParameters = {'codelistid':value.codeLists[0]};
	} else if (value.rangeClass === 'emf:User') {
		value.type = 'user';
	} else if (value.rangeClass === 'emf:Group') {
		value.type = 'group';
	} else if (value.rangeClass === 'ptop:Agent') {
		value.type = 'agent';
	} else if (value.rangeClass === 'ptop:Entity' || value.propertyType === 'object') {
		value.type = 'pickable';
	}
};

EMF.search.advanced.addComboBox = function(parent, config) {
	var selectWidth = 'width:' + parent.css('width');
	var selectOptions = {'style':selectWidth};

	var select2Options = {};
	select2Options.width = 'resolve';

	if (config.multiple) {
		selectOptions.multiple = '';
	}

	if (config.width) {
		selectOptions.width = config.width;
	}
	if (config['class']) {
		selectOptions['class'] = config['class'];
	}

	var select = $('<div />', selectOptions);
	if (config.data) {
		select2Options.data = config.data;
		if (config.defaultToFirstOption && config.data.length>0 && !config.defaultValue) {
			config.defaultValue = config.data[0].id;
		}
	} else if (config.dataURL) {

		select2Options.ajax = {
			 url: config.dataURL,
			 dataType: 'json',
			 results: function (data, page) {

				 var dataArray = [];
				 var category = null;
				 data.forEach(function(record) {
					 if (record.objectType === 'category') {
						 if (category != null) {
							 dataArray.push(category);
						 }
						 category = {id: record.name, text: record.title, children: []};
					 } else {
						 category.children.push({id: record.name, text: record.title});
					 }
				 });
				 dataArray.push(category);

				 return {results: dataArray};
			 }
		};
	}
	parent.append(select);

	if (config.multiple) {
		select2Options.multiple = config.multiple;
	}

	if (!config.searchable) {
		select2Options.minimumResultsForSearch = -1;
	}

	if (config.formatResultCssClass) {
		select2Options.formatResultCssClass = config.formatResultCssClass;
	}

	select.select2(select2Options);

	if (config.onChange) {
		select.on('change', config.onChange);
	}

	if (config.defaultValue) {
		var event = jQuery.Event( "change" );
		event.added = {'id':config.defaultValue};
		event.val = config.defaultValue;
		select.val(config.defaultValue).trigger(event);
	}

	// Close all other opened select2 elements because of a problem with ui.dialog
	select.on("select2-open", function(evt) {
		var select2object = $('div.select2-dropdown-open:not("#' + $(this).parent().find('div.select2-container').attr('id') + '")');
		select2object.select2('close');
		select2object.blur();

	});

	return select;
};

EMF.search.advanced.addSelect2ComboBox = function(parent, config) {
	var selectWidth = 'width:' + config.width?config.width:parent.css('width');
	var selectOptions = {'style':selectWidth};

	var select2Options = $.extend({}, config.select2Options);
	select2Options.width = 'resolve';

	if (config.width) {
		selectOptions.width = config.width;
	}
	var select = $('<div />', selectOptions);
	parent.append(select);

	select.select2(select2Options);

	if (config.onChange) {
		select.on('change', config.onChange);
	}

	if (config.defaultToFirstOption && config.select2Options.data.length>0 && !config.defaultValue) {
		config.defaultValue = config.select2Options.data[0].id;
	}

	if (config.defaultValue) {
		var event = jQuery.Event( "change" );
		event.added = {'id':config.defaultValue};
		event.val = config.defaultValue;
		select.val(config.defaultValue).trigger(event);
	}

	// Close all other opened select2 elements because of a problem with ui.dialog
	select.on("select2-open", function(evt) {
		var select2object = $('div.select2-dropdown-open:not("#' + $(this).parent().find('div.select2-container').attr('id') + '")');
		select2object.select2('close');
		select2object.blur();
	});

	if (config.mutable === false) {
		select.select2('disable');
	}

	return select;
};

var areAllGroupQuesriesResolved = function(criteria, groupQueries) {
	for (var i = 0; i < criteria.length; i++) {
		if (groupQueries[i] === null) {
			return false;
		}
	}
	return true;
};

var buildSparqlQueryInernal = function(criterion, index, fieldsLookup, nestingLevelInternal, objectTypesParents) {
	var query = '';
	if (criterion.objectType && criterion.objectType !== '') {
		if (criterion.objectType === 'all') {
			//nothing to do
		} else if (criterion.objectType.indexOf(':') !== -1) {
			query += '{  { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' rdf:type ' + criterion.objectType + ' }  }  .\n';
		} else {
			var parent = objectTypesParents[criterion.objectType];
			query += '{  { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' rdf:type ' + parent + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:type "' + criterion.objectType + '" }  }  .\n';
		}
		if (criterion.criteria && criterion.criteria.length > 0) {
			var formSPARQLQuery = EMF.search.advanced.form.sparqlBuilder.criteriaToSPARQLQuery(criterion.criteria, index, fieldsLookup, nestingLevelInternal);
			if (formSPARQLQuery !== '') {
				query += formSPARQLQuery;
			}
		}
	}
	if (query !== '') {
		query = ' { ' + query + ' } ';
	}
	return query;
};

var resolver = function(query, callback, deferred) {
	deferred.resolve(query);
	if (typeof(callback) === 'function') {
		callback(query);
	}
};

var criteriaToSparqlInternal = function(criteria, objectTypesParents, nestingLevelInternal, callback, deferred) {
	var fieldsURL = SF.config.contextPath + '/service/properties/searchable/semantic';
	var groupQueries = [];
	for ( var i = 0; i < criteria.length; i++) {
		groupQueries[i] = null;
		var objectType = criteria[i].objectType;
		var fieldsLookup = {};

		if (objectType !== 'all') {
			var params = {};
			var relationsParams = {};
			var directType = objectTypesParents[objectType];
			if (directType) {
				params['forType'] = directType + '_' + objectType;
				if(objectType.indexOf(':') === -1){
					relationsParams['domainFilter'] = directType;
				} else {
					relationsParams['domainFilter'] = objectType;
				}
			} else {
				params['forType'] = objectType + '_';
				relationsParams['domainFilter'] = objectType;
			}
		}

		$.when($.ajax({dataType: "json", contentType : EMF.config.contentType.APP_JSON, url: fieldsURL, data: params}),
				$.extend({}, criteria[i]),
				i).done(function (fields, criterion, index) {
					var allFields = fields[0].map(function(val) {
						//assign the type of the value (eg. number, date, codelist, user, etc..)
						EMF.search.advanced.assignValueInternalType(val);
						return val;
					});
		
					allFields.unshift({
						type : 'pickable',
						id : EMF.search.advanced.ANY_RELATION
					});
		
					// Adding the any field property in the beginning.
					allFields.unshift({
						type : 'string',
						id : EMF.search.advanced.ANY_FIELD
					});
		
					for ( var j = 0; j < allFields.length; j++) {
						fieldsLookup[allFields[j].id] = allFields[j];
					}

					groupQueries[index] = buildSparqlQueryInernal(criterion, index, fieldsLookup, nestingLevelInternal, objectTypesParents);

					if (areAllGroupQuesriesResolved(criteria, groupQueries)) {
						resolver(EMF.search.advanced.buildFullSPARQLQuery(groupQueries), callback, deferred);
					}
				});
	}
};

EMF.search.advanced.buildSPARQLQueryFromCriteria = function(criteria, callback, nestingLevel) {

	var nestingLevelInternal;
	if($.isNumeric(nestingLevel)) {
		nestingLevelInternal = nestingLevel;
	} else {
		nestingLevelInternal = 1;
	}

	var deferred = $.Deferred();

	//The dateRanges have to be ready and initialized before the query is built. That's why we need that promise here too.
	var promises = [EMF.search.advanced.getDateRangesPromise()];

	criteria.forEach(function(groupCriteria, groupIndex) {
		groupCriteria.criteria.forEach(function(criterion, rowIndex) {
	     if (criterion.dynamicCriteria) {
	        var promise = EMF.search.advanced.buildSPARQLQueryFromCriteria(criterion.dynamicCriteria, '', (nestingLevelInternal + 1)).done(function(query) {
	          var indexPostfix = groupIndex + '_' + rowIndex + '_' + (nestingLevelInternal + 1);
	          criterion.dynamicQuery = EMF.search.advanced.getReplacedInnerInstanceNames(query, indexPostfix);
	        });
	        promises.push(promise);
	      }
	    });
	});

	$.when.apply($, promises).done(function() {

		if (criteria === null || criteria.length === 0) {
			resolver('', callback, deferred);
			return;
		}

		var objectTypeServiceURL = SF.config.contextPath + '/service/definition/all-types?addFullURI=true';

		$.ajax({
			dataType: "json",
			contentType : EMF.config.contentType.APP_JSON,
			url: objectTypeServiceURL,
			complete: function(response) {
				var result = $.parseJSON(response.responseText);
				var categoryId = null;
				var objectTypesParents = {};
	
				result.forEach(function(record) {
					if (record.objectType === 'category') {
						categoryId = record.name;
					} else {
						objectTypesParents[record.name] = categoryId;
					}
				})
				criteriaToSparqlInternal(criteria, objectTypesParents, nestingLevelInternal, callback, deferred);
	  		   }
	  	   });
	 });
	return deferred;
};


EMF.search.advanced.group = function(placeholder, config) {
	var _this = this;

	this.config = config;

	config.contextPath = config.contextPath || SF.config.contextPath;
	config.initialCriteria = config.initialCriteria || {"objectType":"all","criteria":[{"openBrackets":0,"field":"-2","operator":"CONTAINS","values":[],"closeBrackets":0,"row":0}]};
	config.defaultObjectType = config.initialCriteria.objectType;
	config.initialCriteria = config.initialCriteria.criteria;

	var fieldsByTypeServiceURL = config.contextPath + '/service/properties/searchable/semantic';
	// TODO: this exists also in EMF.search.advanced.group
	var autocompleteLimit = 100;

	var groupHolder = $('<div />', {'class': 'advanced-search-group'});
	if (!placeholder) {
		//ERROR
	}
	placeholder.append(groupHolder);
	_this.element = groupHolder;

	_this.form = null;
	var form = null;
	var onGroupChanged = config.onChanged || function(){};
	var onGroupDeleted = config.onDeleted || function(){};

	groupHolder.html('<b>' + _emfLabels['search.advanced.object_type'] + '</b>');
	var formHolder = null;

	_this.formSolrQuery = '';
	_this.formSPARQLQuery = '';
	_this.formCriteria = [];

	var objectTypeConfig = {
		width: '400px',
		mutable: config.mutable,
		select2Options: {
			data: config.objectTypes,
			'containerCssClass': 'advanced-search-object-type',
			formatResultCssClass: function(option) {
				if (option.children) {
					return 'select2-result-with-children';
				}
			}
		},

		onChange: function(evt) {

			var url = fieldsByTypeServiceURL;

			if (evt.val !== 'all') {
				var params = {};
				var relationsParams = {};
				var directType = config.objectTypesParents[evt.added.id];
				if (directType) {
					params['forType'] = directType + '_' + evt.added.id;
					if(evt.added.id.indexOf(':') === -1){
						relationsParams['domainFilter'] = directType;
					} else {
						relationsParams['domainFilter'] = evt.added.id;
					}

				} else {
					params['forType'] = evt.added.id + '_';
					relationsParams['domainFilter'] = evt.added.id;
				}
				url += '?' + $.param(params);

			}
			_this.formSolrQuery = '';
			_this.formSPARQLQuery = '';
			// Empty form criteria array
			while(_this.formCriteria.length > 0) {
				_this.formCriteria.pop();
			}
			onGroupChanged(_this);

			EMF.blockUI.showAjaxLoader();
			$.when($.ajax({dataType: "json", url: url, async: true})).done(function (properties) {

				var mappedProperties = properties.map(function(val) {
					//assign the type of the value (eg. number, date, codelist, user, etc..)
					EMF.search.advanced.assignValueInternalType(val);

					if(val.title) {
						val.text = val.title;
					}
					// Autosuggest TODO: Optimization
					if (val.solrType && val.type === 'string') {
						val.select2Options = {
							createSearchChoice: function(term, data) {
								if ($(data).filter(function() {
									return this.text.localeCompare(term) === 0;
								}).length===0) {
									return {id:term, text:term};
								}
							},
							ajax: {
								url: EMF.servicePath + "/search/quick?",
								dataType: "json",
								quietMillis: 250,
								data: function(term, page) {
									var solrQuery = EMF.search.advanced.escapeQuerySpecialCharacters(val.id) + ':*';
									if(term && term.length > 0) {
										solrQuery += EMF.search.advanced.escapeQuerySpecialCharacters(term) + '*';
									}
									if(evt.added.id !== 'all') {
										// TODO: Extract the code blocks below as another method to be reused everywhere.
										var directType = config.objectTypesParents[evt.added.id];
										if (directType) {
											solrQuery += ' AND type:' + EMF.search.advanced.escapeQuerySpecialCharacters(evt.added.id);
										} else {
											if (evt.added.id.indexOf(':') !== -1) {
												var fullUri = config.shortToFullURI[evt.added.id];
												solrQuery += ' AND rdfType:' + EMF.search.advanced.escapeQuerySpecialCharacters(fullUri || evt.added.id);
											} else {
												solrQuery += ' AND rdfType:' + EMF.search.advanced.escapeQuerySpecialCharacters(evt.added.id);
											}
										}
									}
									return {
										max: autocompleteLimit,
										q: solrQuery,
										fields: EMF.search.advanced.escapeQuerySpecialCharacters(val.id)
									};
								},
								results: function(data, page) {
									var results = _.map(data.data, function(suggest) {
										return {
											id: suggest[val.id],
											text: suggest[val.id]
										}
									});
									// TODO: Uniq them while mapping ?
									return {
										results : _.uniq(results, 'id')
									};
								}
							}
						};
					}

					return val;
				});

				mappedProperties.unshift({
					type : 'pickable',
					text :_emfLabels['search.advanced.anyrelation'],
					id : EMF.search.advanced.ANY_RELATION
				});

				// Adding the any field property in the beginning.
				mappedProperties.unshift({
					type : 'string',
					text :_emfLabels['search.advanced.anyfield'],
					id : EMF.search.advanced.ANY_FIELD
				});

				var results = mappedProperties;

				formHolder.empty();
				form = new EMF.search.advanced.form(formHolder, {fields:results, nestingLevel:config.nestingLevel, onChanged:function(searchForm) {
					var firstRowFirstCell = searchForm.table.find("tr:first").children("td:first");
					_this.formSolrQuery = searchForm.buildSolrQuery();
					_this.formSPARQLQuery = searchForm.buildSPARQLQuery(config.index);
					_this.formCriteria = searchForm.buildCriteria(true);

					onGroupChanged(_this);
				}});
				//Load initial form criteria. Do it just once!
				config.initialCriteria = config.initialCriteria || [{"objectType":"all","criteria":[{"openBrackets":0,"field":"-2","operator":"CONTAINS","values":[],"closeBrackets":0,"row":0}]}];
				if (config.initialCriteria) {
					for ( var i = 0; i < config.initialCriteria.length; i+=2) {
						var criterion = config.initialCriteria[i];
						var conjunctionOperator = (i===0?null:config.initialCriteria[i-1].operator);
						criterion.conjunctionOperator = conjunctionOperator;
						form.addCriteriaRow(criterion, config.mutable);
					}
					config.initialCriteria = null;
				}
				_this.formSolrQuery = form.buildSolrQuery();
				_this.formSPARQLQuery = form.buildSPARQLQuery(config.index);
				_this.formCriteria = form.buildCriteria(true);
				onGroupChanged(_this);

				if ($.isFunction(config.initializedCallback)) {
					config.initializedCallback();
					delete config.initializedCallback;
				}

				EMF.blockUI.hideAjaxBlocker();
			});
		}
	};

	if (config.defaultObjectType) {
		objectTypeConfig.defaultValue = config.defaultObjectType;
	}


	var objectTypeCombo = EMF.search.advanced.addSelect2ComboBox(groupHolder, objectTypeConfig);

	$('<img/>', {'src':config.contextPath + '/images/delete.png', 'class': 'delete-group-button'})
	.appendTo(groupHolder)
	.on('click', function() {
    	var config = new Object();
    	config.title = "Delete group";
    	config.message = "Selected group and its criteria will be removed. Do you want to continue?";
    	config.confirm= function() {
    		groupHolder.remove();
    		onGroupDeleted(_this);
    	};
    	config.width = 400;
    	config.height = 200;
    	config.okBtn="Yes";
    	config.cancelBtn="No";
    	EMF.dialog.confirm(config);
	})
	.mouseover(function(){
		groupHolder.addClass('delete-target');
	})
	.mouseout(function(){
		groupHolder.removeClass('delete-target');
	});

	formHolder = $('<div />').appendTo(groupHolder);

	_this.buildCriteria = function() {
		// clone form criteria
		var criteria = _this.formCriteria.slice();
		var objectType = objectTypeCombo.select2('val');
		if (objectType && objectType !== '') {
			return {'objectType':objectType, criteria:criteria};
		}
		return null;
	};

	_this.buildSolrQuery = function() {
		var query = '';
		var objectType = objectTypeCombo.select2('val');

		if (objectType && objectType !== '') {
			if (objectType === 'all') {
				query += 'type:*';
			} else {
				if (objectType.indexOf(':') !== -1) {
					query += 'rdfType:' + EMF.search.advanced.escapeQuerySpecialCharacters(config.shortToFullURI[objectType] || objectType);
				} else {
					query += 'type:' + EMF.search.advanced.escapeQuerySpecialCharacters(objectType);
				}
			}

			if (_this.formSolrQuery !== '') {
				var moreThanOneCriteria = _this.formSolrQuery.indexOf(' AND ') > -1 || _this.formSolrQuery.indexOf(' OR ') > -1;
				query += ' AND ' + (moreThanOneCriteria?'(':'') + _this.formSolrQuery + (moreThanOneCriteria?')':'');
			}
		}
		return query;
	};

	/**
	 * @param index - group number in the search. Used to generate unique names for variables
	 */
	_this.buildSPARQLQuery = function(index) {

		var query = '';
		var objectType = objectTypeCombo.select2('val');

		if (objectType && objectType != '') {
			if (objectType === 'all') {
				//This is redundant
				//objectType = '?all' + index;
				//query += '{  { ?instance rdf:type ' + objectType + ' }  }  .\n';
			} else if (objectType.indexOf(':') !== -1) {
				query += '{  { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' rdf:type ' + objectType + ' }  }  .\n';
			} else {
				var parent = config.objectTypesParents[objectType];
				query += '{  { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' rdf:type ' + parent + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:type "' + objectType + '" }  }  .\n';
			}
			if (_this.formSPARQLQuery !== '') {
				query += _this.formSPARQLQuery;
			}
		}
		if (query !== '') {
			query = ' { ' + query + ' }';
		}
		return query;
	};

	_this.unlock = function() {
		$(_this.element.find('.advanced-search-object-type')).select2('enable');
		$(_this.element.find('div')[4]).find('.select2-container-disabled').select2('enable');
		$(_this.element.find('div')[4]).find('.delete-row-button').show();
		config.mutable = true;
	};

	return _this;
};

EMF.search.advanced.dateRanges = [];

/**
 * Retrieves the possible date ranges for the "within" comparison operator, updating the value of EMF.search.advanced.dateRanges.
 * Returns a jQuery promise in order to avoid concurrency issues when using the dateRanges.
 */
EMF.search.advanced.getDateRangesPromise = function() {
	var deferred = jQuery.Deferred();
	if(EMF.search.advanced.dateRanges.length > 0) {
		deferred.resolve();
	} else {
		var searchConfigServiceURL 	= EMF.servicePath + '/search/configuration/advanced';
		$.ajax({
			dataType: "json",
			url: searchConfigServiceURL,
			complete: function(response) {
				var result = $.parseJSON(response.responseText);
				EMF.search.advanced.dateRanges = _.sortBy(result.dateRanges, "order");
				deferred.resolve();
			}
		});
	}
	return deferred.promise();
};

/**
 * Defines advanced search form. This is only the part inside groups as used in the audit log.
 * @param placeholder - jQuery selector for holder element for the form
 * @param config - config object for the form. Can contain the following keys:
 *
 * 	fields: array with available fields for the form. Each field is an object which can have the following keys:
 * 		id: id of the field. Must be unique. It will be used as solr field when generating the query unless solrField is defined
 * 		text: label to be displayed for the field
 * 		type: type of the field. Based on type depends what will be the values in comparison combo and what input fields will be displayed. Possible values are - string, autocomplete, date, number, select
 * 		solrField: optional. Solr field to be used for this field when building solr query. If not set field id is used by default
 * 		autocompleteField: optional. If field type is autocomplete this field can be set to be used for autocomplete functionality. If not set field id will be used by default
 * 		data: optional. Array of data items for fields of type 'select'
 *
 * 	fieldsByDefault: field ids (rows) to be displayed when the form is initialized
 *
 * 	onChanged: function to be called when changing something on the form (this is passed as parameter)
 *
 */
EMF.search.advanced.form = function(placeholder, config) {
	var _form = this;

	if (!placeholder) {
		//ERROR
	}

	var autocompleteLimit = 100;

	var fieldsLookup = {};
	var operators = [
         {id:'AND', text: _emfLabels["search.advanced.and"]},
         {id:'OR', text: _emfLabels["search.advanced.or"]}
	];

	var comparisonOperators = {
		'string' : [
		            {id: 'CONTAINS' , text: _emfLabels["search.advanced.contains"]},
		            {id: 'DOES_NOT_CONTAIN' , text: _emfLabels["search.advanced.doesnotcontain"]},
		    		{id: 'EQUALS' , text: _emfLabels["search.advanced.equals"]},
		    		{id: 'NOT_EQUALS' , text: _emfLabels["search.advanced.notequals"]},
		    		{id: 'STARTS_WITH' , text: _emfLabels["search.advanced.startswith"]},
		    		{id: 'DOES_NOT_START_WITH' , text: _emfLabels["search.advanced.doesnotstartwith"]},
		    		{id: 'ENDS_WITH' , text: _emfLabels["search.advanced.endswith"]},
		    		{id: 'DOES_NOT_END_WITH' , text: _emfLabels["search.advanced.doesnotendwith"]}
		 ],
		 'date': [
		          	{id: 'BETWEEN' , text: _emfLabels["search.advanced.between"]},
		          	{id: 'IS' , text: _emfLabels["search.advanced.is"]},
		          	{id: 'IS_AFTER' , text: _emfLabels["search.advanced.isafter"]},
		          	{id: 'IS_BEFORE' , text: _emfLabels["search.advanced.isbefore"]},
		    	 	{id: 'IS_WITHIN' , text: _emfLabels["search.advanced.iswithin"]}
		 ],
		 'select': [
		            {id: 'IN' , text: _emfLabels["search.advanced.in"]},
		            {id: 'NOT_IN' , text: _emfLabels["search.advanced.notin"]}
		 ],
		 'number': [
		    		{id: 'EQUALS' , text: _emfLabels["search.advanced.equals"]},
		    		{id: 'NOT_EQUALS' , text: _emfLabels["search.advanced.notequals"]},
		    		{id: 'LOWER_THAN' , text: _emfLabels["search.advanced.lowerthan"]},
		    		{id: 'GREATER_THAN' , text: _emfLabels["search.advanced.higherthan"]},
		    		{id: 'BETWEEN' , text: _emfLabels["search.advanced.between"]}
		 ],
		 'autocomplete': [
		            {id: 'IN' , text: _emfLabels["search.advanced.in"]},
		            {id: 'NOT_IN' , text: _emfLabels["search.advanced.notin"]}
		 ],
		 'pickable': [
		            {id: 'SET_TO' , text: _emfLabels["search.advanced.setto"]},
		            {id: 'NOT_SET_TO' , text: _emfLabels["search.advanced.notsetto"]},
		            {id: 'SET_TO_SOME_BUT_NOT_TO' , text: _emfLabels["search.advanced.settosomebutnotto"]}
	     ],
	     'boolean' : [
			        {id: 'IS' , text: _emfLabels["search.advanced.is"]},
			        {id: 'IS_NOT' , text: _emfLabels["search.advanced.isnot"]}
	     ],
	     'user': [
  		            {id: 'IN' , text: _emfLabels["search.advanced.in"]},
  		            {id: 'NOT_IN' , text: _emfLabels["search.advanced.notin"]}
  		 ],
	     'group': [
		            {id: 'IN' , text: _emfLabels["search.advanced.in"]},
		            {id: 'NOT_IN' , text: _emfLabels["search.advanced.notin"]}
		 ],
	     'agent': [
		            {id: 'IN' , text: _emfLabels["search.advanced.in"]},
		            {id: 'NOT_IN' , text: _emfLabels["search.advanced.notin"]}
		 ]
	};
	comparisonOperators.dateTime = comparisonOperators.date;
	
	var openBrackets = [
  	     {id:0, text: ' '},
  	     {id:1, text: '('},
  	     {id:2, text: '(('},
  	     {id:3, text: '((('}
  	];

  	var closeBrackets = [
	     {id:0, text: ' '},
	     {id:1, text: ')'},
	     {id:2, text: '))'},
	     {id:3, text: ')))'}
	];

	if (config) {
		var fields = config.fields || [];
		for ( var int = 0; int < fields.length; int++) {
			fieldsLookup[fields[int].id] = fields[int];
		}
		var fieldsByDefault = config.fieldsByDefault || [];
		var onChanged = config.onChanged || function(){};
	} else {
		// ERROR!
	}

	var form = $('<form />', {'class': 'advanced-search-form dynamic-form-panel'});
	$(placeholder).append(form);
	this.element = form;

	this.table = $('<table />', {cellspacing: '0', cellpadding: '0', style: 'width: 100%'});
	form.append(this.table);

	function addEmptyRow(table) {
		var row = $('<tr />');
		table.append(row);
		var emptyRowColumn = $('<td />', {'class': 'empty-row', colspan: 6});
		emptyRowColumn.on('click', function(){_form.addCriteriaRow();});
		emptyRowColumn.append('Add search criteria').appendTo(row);
		$('<td />', {'class': 'delete-row-column'}).appendTo(row);
		$('<td />', {'class': 'add-row-column'}).appendTo(row);

		updateTableLayout(table);
		return row;
	};

	/**
	 * Creates and appends new crtieria row to the form.
	 *
	 * @param criterion - optional. JSON object used to initialize the row
	 */
	this.addCriteriaRow = function(criterion, mutable) {
		var table = this.table;
		if (!criterion) {
			criterion = {};
		}
		//if field is undefined
		var field = criterion.field || fields[0].id;

		//Remove empty row if present
		var firstRow = table.find("tr:first");
		if (firstRow.children("td:first").attr('class') === 'empty-row') {
			firstRow.remove();
		}

		var row = $('<tr />', {'class': 'cmf-field-wrapper'});
		table.append(row);

		var conditionColumn = $('<td/>', {'class': 'condition-column'});
		row.append(conditionColumn);

		EMF.search.advanced.addSelect2ComboBox(conditionColumn, {
			select2Options:{data: operators, minimumResultsForSearch: -1},
			defaultToFirstOption: true,
			defaultValue: criterion.conjunctionOperator,
			mutable: mutable
		});

		var openBracketsColumn = $('<td/>', {'class': 'open-brackets-column'});
		row.append(openBracketsColumn);

		var openBracketsSelectConfig = {
			select2Options: {data: openBrackets, minimumResultsForSearch: -1},
			defaultToFirstOption: true,
			defaultValue: criterion.openBrackets,
			mutable: mutable,
			onChange: function(evt) {
				if (evt.val > 0) {
					$(evt.target).closest('tr').find('.close-brackets-column div.select2-container').select2('enable', false);
				} else {
					$(evt.target).closest('tr').find('.close-brackets-column div.select2-container').select2('enable', true);
				}
			}
		};

		EMF.search.advanced.addSelect2ComboBox(openBracketsColumn, openBracketsSelectConfig);

		var fieldColumn = $('<td/>', {'class': 'field-column'});
		row.append(fieldColumn);

		var fieldCombobox = EMF.search.advanced.addSelect2ComboBox(fieldColumn, {
			select2Options : {
				data: fields
			},
			mutable: mutable,
			defaultValue: field,
			defaultToFirstOption: true
		});
		fieldCombobox.on('change', {self:this}, onFieldComboChange);
		var selectedField = fieldsLookup[fieldCombobox.val()];
		var selectedFieldType = selectedField['type'];

		var comparisonColumn = $('<td/>', {'class': 'comparison-column'});
		row.append(comparisonColumn);

		var comparisonComboBox = EMF.search.advanced.addSelect2ComboBox(comparisonColumn, {
			select2Options : {data: comparisonOperators[selectedFieldType], minimumResultsForSearch: -1},
			defaultToFirstOption: true,
			defaultValue: criterion.operator,
			mutable: mutable
		});
		comparisonComboBox.on('change', {self:this}, onComparisonComboChange);
		var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');

		var inputColumn = $('<td/>', {'class': 'input-column'});
		row.append(inputColumn);
		addInput(inputColumn, selectedField, comparisonOperator, criterion.values);

		var closeBracketsColumn = $('<td/>', {'class': 'close-brackets-column'});
		row.append(closeBracketsColumn);

		var closeBracketsSelectConfig = {
			select2Options: {data: closeBrackets, minimumResultsForSearch: -1},
			defaultToFirstOption: true,
			defaultValue: criterion.closeBrackets,
			mutable: mutable,
			onChange: function(evt) {
				if (evt.val > 0) {
					$(evt.target).closest('tr').find('.open-brackets-column div.select2-container').select2('enable', false);
				} else {
					$(evt.target).closest('tr').find('.open-brackets-column div.select2-container').select2('enable', true);
				}
			}
		};
		EMF.search.advanced.addSelect2ComboBox(closeBracketsColumn, closeBracketsSelectConfig);

		var deleteRowColumn = $('<td/>', {'class': 'delete-row-column'});

		row.append(deleteRowColumn);
		var deleteRowButton = $('<input />', {type: 'button', 'class': 'delete-row-button'});
		deleteRowColumn.append(deleteRowButton);
		row.append(deleteRowColumn);
		deleteRowButton.on('click', function(evt) {
			var cell = $(evt.target);
			var tTable = cell.closest('table');
			cell.closest('tr').remove();
			if (tTable.find("tr").length === 0) {
				addEmptyRow(tTable);
			}
			updateTableLayout(table);
			onChanged(_form);
		});

		var addRowButton = $('<td/>', {'class': 'add-row-column'});
		row.append(addRowButton);
		if(mutable === false ) {
			deleteRowButton.hide();
		}
		updateTableLayout(table);
	};

	function onFieldComboChange(evt) {
		var row = $(evt.target).closest('tr');

		var oldOption = fieldsLookup[evt.removed.id];
		var newOption = fieldsLookup[evt.added.id];

		if (oldOption['type'] !== newOption['type'] || oldOption['type'] === 'select' || oldOption['type'] === 'autocomplete') {
			var comparisonColumn = row.find('.comparison-column');
			comparisonColumn.empty();
			var comparisonComboBox = EMF.search.advanced.addSelect2ComboBox(comparisonColumn, {
				select2Options: {data: comparisonOperators[newOption['type']], minimumResultsForSearch: -1},
				defaultToFirstOption: true,
				mutable: config.mutable
			});
			comparisonComboBox.on('change', {self:this}, onComparisonComboChange);
			var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');

			var inputColumn = row.find('.input-column');
			inputColumn.empty();
			addInput(inputColumn, newOption, comparisonOperator);
		} else {
			// Comparison is not changed. We recreate input column in order to apply any special field configs
			var values = getCriteriaValues($(row));
			var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');

			var inputColumn = row.find('.input-column');
			inputColumn.empty();
			addInput(inputColumn, newOption, comparisonOperator, values);
		}
	};


	/**
	 * Function executed when comparison value is changed. Used to change inputs
	 * in input column based on field and comparison. Also tries to
	 * preserve values from old input to new if possible.
	 *
	 * @param evt
	 */
	function onComparisonComboChange(evt) {
		var oldOption = evt.removed.id;
		var newOption = evt.added.id;
		if (oldOption !== newOption) {
			var row = $(evt.target).closest('tr');
			var selectedField = row.find('.field-column div.select2-container').select2('val');
			var field = fieldsLookup[selectedField];
			var inputColumn = row.find('.input-column');
			if (isDate(field['type'])) {
				if (newOption !== 'IS_WITHIN' && oldOption !== 'IS_WITHIN') {
					var firstInputValue = null;
					if (oldOption === 'IS') {
						firstInputValue = inputColumn.find('input:first').datepicker('getDate');
					} else {
						firstInputValue = inputColumn.find('input:first').datetimepicker('getDate');
					}

					inputColumn.empty();
					addInput(inputColumn, field, newOption);

					if (newOption === 'IS') {
						inputColumn.find('input:first').datepicker('setDate', firstInputValue);
					} else {
						inputColumn.find('input:first').datetimepicker('setDate', firstInputValue);
					}
				} else {
					inputColumn.empty();
					addInput(inputColumn, field, newOption);
				}
			} else {
				var isOldElementSelect = inputColumn.find('div.select2-container').length;

				var firstInputValue = null;
				if (isOldElementSelect) {
					firstInputValue = inputColumn.find('div.select2-container').select2('data');
				} else {
					firstInputValue = inputColumn.find('input:first').val();
				}
				inputColumn.empty();
				addInput(inputColumn, field, newOption);

				var isNewElementSelect = inputColumn.find('div.select2-container').length;

				// Preserve old value
				// Check that new element is the same as the old one
				if (isNewElementSelect && isOldElementSelect) {
					// Both elements are selects
					inputColumn.find('div.select2-container').select2('data', firstInputValue);
				} else if (!isNewElementSelect && !isOldElementSelect){
					// Both elements are inputs
					inputColumn.find('input:first').val(firstInputValue);
				}
			}
		}
	};

	/**
	 * Adds inputs to the input column depending on the field and comparison. For example one HTML input element for fields of type string. Two HTML input elements for fields of type number and BETWEEN comparison, etc.
	 *
	 * @param parent - placeholder element for the inputs (table cell)
	 * @param field - selected field in the field column (from the form config.fields array)
	 * @param comparison - selected comparison in the comparison column
	 * @param values - optional. Initial values to be set in the generated inputs
	 */
	function addInput(parent, field, comparison, values) {
		if (field['type'] === 'string') {
			addTagSelectInput(parent, field, values);
		} else if (field['type'] === 'number') {
			if (comparison === 'BETWEEN') {
				addTwoStringInputs(parent, values);
			} else {
				addStringInput(parent, values);
			}
		} else if (isDate(field['type'])) {
			var dateOnly = field['type'] === 'date';
			if (comparison === 'BETWEEN') {
				addTwoDateInput(parent, values, dateOnly);
			} else if (comparison === 'IS_WITHIN') {
				addDateOffsetInput(parent, values);
			} else {
				if (comparison === 'IS') {
					addDateInput(parent, values, true);
				} else {
					addDateInput(parent, values, dateOnly);
				}
			}
		} else if (field['type'] === 'select') {
			addSelectInput(parent, field);
		} else if (field['type'] === 'autocomplete') {
			addAutocompleteInput(parent, field, values);
		} else if (field['type'] === 'pickable') {
			addPickerField(parent, values);
		} else if (field['type'] === 'boolean') {
			addBooleanInput(parent, values);
		} else if (field['type'] === 'user' || field['type'] === 'group' || field['type'] === 'agent') {
			addUserInput(parent, field, values);
		}
	};

	/**
	 * Adds input for user and group properties
	 * @param parent
	 * @param field
	 * @param values
	 * @returns
	 */
	function addUserInput(parent, field, values) {
		var includeUsers = true;
		var includeGroups = true;
		if (field['type'] === 'user') {
			includeGroups = false;
		} else if (field['type'] === 'group') {
			includeUsers = false;
		}

		var select2Options = {
			multiple : true,
			minimumResultsForSearch : -1,
			minimumInputLength : -1,
			mutable : config.mutable,
			ajax : {
				url : EMF.servicePath + '/users',
				quietMillis : 250,
				contentType : EMF.config.contentType.APP_JSON,
				data : function(term, page) {
					return {
						term : term || '',
						includeGroups : includeGroups,
						includeUsers : includeUsers,
						offset : page,
						limit : autocompleteLimit
					};
				},
				results : function(data, page) {
					var more = (page * autocompleteLimit) < data.total;
					var currentUser = {
						id: EMF.search.CURRENT_USER,
						label: _emfLabels["search.current.user"],
						type: "user",
						value: _emfLabels["search.current.user"]
					};
					//insert the "current user" in the beginning of the users list
					data.items.unshift(currentUser);
					return {
						results : data.items,
						more : more
					};
				}
			},
			formatResult : function(item) {
				var iconPath = '/emf/images/group-icon-32.png';
				if (item.type === 'user') {
					iconPath = '/emf/images/user-icon-32.png';
				}
				return '<div style="display: table-row;">'
						+ '<div style="display: table-cell;">'
						+ '<img style="vertical-align: top;" src="'
						+ iconPath + '">' + '</div>'
						+ '<div style="display: table-cell;">'
						+ '<div style="margin-top: 5px;">' + item.label
						+ '</div>' + '<div style="font-weight: bold;"><small>'
						+ item.value + '</small></div>' + '</div>' + '</div>';
			},
			formatSelection: function(data) {
				return data.label;
			},
			initSelection: function(element, callback) {
				if (values) {
					 $.ajax({
			            	type: 'POST',
			            	contentType : EMF.config.contentType.APP_JSON,
			                url	: EMF.servicePath + '/users/multi',
			                data: JSON.stringify(values)
			            })
			            .done(function(data) {
							if (data && data.length > 0) {
								var currentSelection = userInput.select2('data');
								var newSelection = $.map(currentSelection, function(item, index){
									for ( var int2 = 0; int2 < data.length; int2++) {
										if (data[int2].id === item.id) {
											return data[int2];
										}
									}
										return item;
								});

								callback(newSelection);
							}
			            });
				}
			}
		};

		var userInput = EMF.search.advanced.addSelect2ComboBox(parent, {select2Options:select2Options});
		// Trigger initSelection on the select2
		userInput.select2('val', []);

		if (values) {
			// Temporary set values to their ids. After initSelection ajax call they will be replaced with their labels
			userInput.select2('data', $.map(values, function(element) {
				var elementLabel = element;
				//if there is any 'current-user' selection in the field, put it a label from the bundle
				if(element === EMF.search.CURRENT_USER) {
					elementLabel = _emfLabels["search.current.user"];
				}
				return {id: element, text: element, label: elementLabel, value: element};
			}));
		}

		return userInput;
	};

	/**
	 * Adds single HTML input element.
	 * @param parent
	 * @param values
	 * @returns
	 */
	function addStringInput(parent, values) {
		var initialValue = values && values[0]?values[0]:'';

		var input = $('<input />', {type: 'text', 'class': 'plain-text', value: initialValue});
		parent.append(input);

		return input;
	};

	/**
	 *  Adds two inputs to the provided parent element.
	 */
	function addTwoStringInputs(parent, values) {
		var initialValue1 = values && values[0]?values[0]:'';
		if (initialValue1 === '*') {
			initialValue1 = '';
		}

		var initialValue2 = values && values[1]?values[1]:'';
		if (initialValue2 === '*') {
			initialValue2 = '';
		}

		var wrapper = $('<span />', {'class': 'cmf-relative-wrapper floatLeft'});
		var input = $('<input />', {type: 'text', 'class': 'plain-text numeric-field', value: initialValue1});
		wrapper.append(input);
		parent.append(wrapper);

		parent.append(' - ');

		wrapper = $('<span />', {'class': 'cmf-relative-wrapper floatRight'});
		input = $('<input />', {type: 'text', 'class': 'plain-text numeric-field', value: initialValue2});

		wrapper.append(input);
		parent.append(wrapper);
	};

	/**
	 * Adds a select2 element with support for tags.
	 *
	 * @param parent - the parent
	 * @param field -
	 * @param values - the initial values
	 * @returns
	 */
	function addTagSelectInput(parent, field, values) {
		var initialValues = values?values:[];
		var config = {
			select2Options : {
				tags : initialValues,
				formatNoMatches: function() {
			        return '';
			    }
			}
		};
		// External config is with higher priority so it will override internal
		// config if there is a collision
		$.extend(config.select2Options, field.select2Options);

		var select = EMF.search.advanced.addSelect2ComboBox(parent, config);
		select.select2('data', $.map(initialValues, function(element) {
			return {id: element, text: element};
		}));
	};

	function addBooleanInput(parent, values) {
		var config = {
			select2Options: {
				data: [{'id':'true', 'text':'true'}, {'id':'false', 'text':'false'}, {'id':'-1', 'text':'not specified'}],
				minimumResultsForSearch: -1
			},
			defaultValue: values && values[0]?values[0]:null
		};
		return EMF.search.advanced.addSelect2ComboBox(parent, config);
	};

	/**
	 * Adds single date input for fields of type date.
	 * @param parent
	 * @param values
	 * @param dateOnly - boolean flag specifying does time precision has to be omitted
	 */
	function addDateInput(parent, values, dateOnly) {
		parent.append(createDateInputField('width-100-percent', values && values[0]?values[0]:null, dateOnly));
	};

	/**
	 * Adds two date inputs for fields of type date.
	 * @param parent
	 * @param values
	 * @param dateOnly - boolean flag specifying does time precision has to be omitted
	 */
	function addTwoDateInput(parent, values, dateOnly) {
		parent.append(createDateInputField('floatLeft', values && values[0]?values[0]:null, dateOnly));
		parent.append(' - ');
		parent.append(createDateInputField('floatRight', values && values[1]?values[1]:null, dateOnly));
	};

	function addDateOffsetInput(parent, values) {

		var selectData = [];
		$.when(EMF.search.advanced.getDateRangesPromise()).then(function() {
			_.forEach(EMF.search.advanced.dateRanges, function(dateRange) {
				selectData.push({id: dateRange.id, text:dateRange.label});
			});

			var config = {
				select2Options: {
					data: selectData,
					multiple:  false,
					minimumResultsForSearch: -1
				},
				defaultValue: values && values[0]?values[0]:null
			};

			var input = EMF.search.advanced.addSelect2ComboBox(parent, config);
			input.addClass('default-font-size');
			return input;
		});
	};

	/**
	 * Adds combobox input for fields of type select. Data config property should be added for the field
	 *
	 * @param parent
	 * @param field
	 * @param values
	 * @returns
	 */
	function addSelectInput(parent, field, values) {

		var config = {
			select2Options: {
				data: field['data'],
				defaultValue: values && values[0]?values[0]:null,
				multiple:  field['multiple'],
				mutable: config.mutable
			}
		};

		if (!field['searchable']) {
			config.select2Options.minimumResultsForSearch = -1;
		}

		return EMF.search.advanced.addSelect2ComboBox(parent, config);
	};

	/**
	 * Adds autocomplete combobox input
	 * @param parent - placeholder element for the input (table cell)
	 * @param field
	 * @param values - values to be selected by default in the field
	 * @returns
	 */
	function addAutocompleteInput(parent, field, values) {
		var autocompleteField = field['autocompleteField'];
		if (!autocompleteField) {
			autocompleteField = field['id'];
		}

		var select2Options = {
			 multiple: true,
			 minimumResultsForSearch: -1,
			 minimumInputLength: -1,
			 mutable: config.mutable,
			 ajax: {
				 url: SF.config.contextPath + '/service/autocomplete/' + autocompleteField,
				// What is this?
				 quietMillis: 100,
				 data: function (term, page) {
					 var params = {
						 q: term,
						 limit: autocompleteLimit,
						 offset: (page-1)*autocompleteLimit
					 };
					 if (field.additionalParameters) {
						 $.extend(params, field.additionalParameters);
					 }
					 return params;
				 },
				 results: function (data, page) {
					 var more = (page * autocompleteLimit) < data.total;
					 return {results: data.results, more: more};
				 }
			 },
			 containerCssClass: field['cssClass'] || '',
			 dropdownCssClass: field['cssClass'] || ''
		};

		if(field['escapeHtml'] === false) {
			select2Options.escapeMarkup = function(m) {
				 return m;
			};
		}

		var autocompleteInput = EMF.search.advanced.addSelect2ComboBox(parent, {select2Options:	select2Options});

		if (values) {
			autocompleteInput.select2('data', $.map(values, function(element) {
				return {id: element, text: element};
			}));

			// Load labels if field values are from codelist
			if (field.codeLists && field.codeLists.length > 0) {
				var valuesRequestData = $.map(values, function(value) {
					return {
				        "code": value,
				        "indicator": "codelist",
				        "indicatorValue": field.codeLists[0]
					};
				});
				$.ajax({
					url : SF.config.contextPath + '/service/label/search',
					type : "POST",
					data : JSON.stringify(valuesRequestData),
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					success : function(response) {
						var currentSelection = autocompleteInput.select2('data');
						var newSelection = $.map(currentSelection, function(item, index){
							for ( var int2 = 0; int2 < response.length; int2++) {
								if (response[int2].code === item.id) {
									item.text = response[int2].label;
									return item;
								}
							}
							return item;
						});
						autocompleteInput.select2('data', newSelection);
					}
				});
			}
		}

		return autocompleteInput;
	};

	/**
	 * Creates date input
	 * @param inputClass - css class for the date input (floatLeft, floatRight)
	 * @param initialValue - date to be set by default
	 * @param dateOnly - boolean flag specifying does time precision has to be omitted
	 * @returns span element with datepicker input
	 */
	function createDateInputField(inputClass, initialValue, dateOnly) {
		if (initialValue && initialValue === '*') {
			initialValue = null;
		}

		var datePickerOptions = {
			dateFormat : SF.config.dateFormatPattern,
			onSelect : function() {
				onChanged(_form);
			}.bind(this)
		};

		var initialDate = null;
		if (initialValue !== null) {
			initialDate = new Date(initialValue);
		}

		var wrapper = $('<span />', {'class': 'cmf-relative-wrapper' + (inputClass?' ' + inputClass:'')});
		var datepickerInput = null;
		if (dateOnly) {
			datePickerOptions.onClose = function() {
				$(this).datepicker('setDate', $(this).datepicker('getDate'));
			};
			datepickerInput = $('<input/>', {type: 'text', 'class': 'date-range advanced-search-date-field'}).datepicker(datePickerOptions);
			datepickerInput.datepicker('setDate', initialDate);
		} else {
			datePickerOptions.onClose = function() {
				$(this).datetimepicker('setDate', $(this).datetimepicker('getDate'));
			};
			datepickerInput = $('<input/>', {type: 'text', 'class': 'date-range advanced-search-date-field'}).datetimepicker(datePickerOptions);
			datepickerInput.datetimepicker('setDate', initialDate);
		}

		wrapper.append(datepickerInput);
		wrapper.append($('<span/>', {'class': 'ui-icon ui-icon-calendar cmf-calendar-img cmf-date-field-icon'}));
		return wrapper;
	};

	/**
	 * Checks if the field is a date or dateTime.
	 * 
	 * @param field
	 *        the field
	 */
	function isDate(field){
		return field === 'date' || field === 'dateTime';
	}
	
	/**
	 * Creates a multiselect field with a picker.
	 *
	 * @param parent - the parent html element
	 * @param values - any previously selected values
	 */
	function addPickerField(parent, values) {
		
		var select = $('<div />', {'class':'adv-search-picker-select'});

		var pickerButton = $('<div />', {
			'class' : 'btn btn-xs btn-default pull-right adv-search-picker-button',
			'text' : _emfLabels['search.advanced.select']
		});

		var selectWrapper = $('<td />', {'class':'adv-search-picker-select-wrapper headers-with-icons'});
		selectWrapper.append(select);
		parent.append(selectWrapper);

		var tooltipLabel = $('<span />', {'class':'tooltip', 'html' : _emfLabels['search.advanced.clickOnSelect']});
		if (values && values[0] && values[0].indexOf(EMF.search.advanced.DYNAMIC_QUERY) > -1) {
			selectWrapper.addClass('has-tooltip');
			selectWrapper.append(tooltipLabel);
		}

		var pickerButtonWrapper = $('<td />', {'class' : 'adv-search-picker-button-wrapper'} );
		pickerButtonWrapper.append(pickerButton);
		parent.append(pickerButtonWrapper);

		pickerButton.click(function () {
			openPicker(select);
		});

		// The valid items to be extracted from the current context.
		var validContextItems = [{id:'emf-search-context-current-project', 	type: 'projectinstance',	text: _emfLabels['search.advanced.current.project']},
		                         {id:'emf-search-context-current-case', 	type: 'caseinstance', 		text: _emfLabels['search.advanced.current.case']},
		                         {id:'emf-search-context-current-object', 	type: 'objectinstance',		text: _emfLabels['search.advanced.current.object']},
		                         {id:'emf-search-context-current-object', 	type: 'documentinstance',	text: _emfLabels['search.advanced.current.object']}];

		// Find the current context
		var context = $.map(EMF.currentPath, function(contextItem) {
			var value = _.find(validContextItems, {type: contextItem.type});
			if (value) {
				value.uri = contextItem.id;
				return value;
			}
		});

		// The notorious any object.
		context.push({id:'any_object', 	type:'any_object', text: _emfLabels['search.advanced.anyobject']});
		// Adding the additional option to select another object via object picker
		context.push({id:'select_item', text: _emfLabels['cmf.relations.objectpicker.trigger']});

		var selectConfig = {
			escapeMarkup: function(m) {
				return m;
			},
			query: function(query) {
				query.callback({results: context});
			},
			multiple: true
		};

		select.select2(selectConfig).on('select2-selecting', function(event) {
			if(event.choice.id === "select_item") {
				event.preventDefault();
				// If it's not closed, it'll remain open.
				select.select2('close');
				openPicker(select);
			}
		});

		if(values && values.length > 0) {
			// The filter contains some previously selected items.
			loadPreviousRelations(values, select);
		} else {
			// No items at all.
			select.select2('data', []);
		}

		/**
		 * Loading previously selected relations.
		 *
		 * @param values - the previously selected URIs
		 * @param select - the select in which the relations will be shown
		 */
		function loadPreviousRelations(values, select) {
			var ids = [];
			var data = [];
			var initialData = [];
			var dynamicQueryText = _emfLabels["search.advanced.userDefinedQuery"];
			var contextNotFoundLabel = _emfLabels["search.advanced.context.not.found"];

			values.forEach(function(value) {
				// Check if it is a context
				var contextItem = _.find(context, {id: value});
				if(contextItem) {
					initialData.push(contextItem);
					data.push(contextItem);
				} else if (value.indexOf(':') > -1 ) {
					initialData.push({id: value, text: value});
					ids.push(value);
				} else if (value.indexOf(EMF.search.advanced.DYNAMIC_QUERY) > -1) {
					var entry = {}
					entry.text = dynamicQueryText;
	    			entry.id = value;
	    			initialData[0] = entry;
				} else {
					//if it is not an URI, dynamic query, or existing context, put a label
					//telling that the context is missing
					contextItem = _.find(validContextItems, {id: value});
					var selectionLabel = contextNotFoundLabel + " ("  + contextItem.text + ")";
	    			initialData[0] = {id: "", text: selectionLabel};
				}
			});
			select.select2('data', initialData);
			// TODO: Don't construct the params this way...
			if (ids.length > 0) {
				var url = '/emf/service/autocomplete/labels/header';
				$.get(url, {values: ids}, function (records) {
					_.forEach(records, function(value) {
						data.push({id:value.value,text:value.label});
					});
					select.select2('data', data).trigger('change');
				});
			}
		}

		/**
		 * Opens an object picker.
		 *
		 * @param select
		 */
		function openPicker(select) {
			var advancedSearchCriteria = {};
			var advancedSearchQuery = '';
			var selectedItem;
		    var selectedItems 	= {};
		    var placeholder 	= $('<div></div>');
		    var pickerConfig			= EMF.search.config();
		    pickerConfig.pickerType  	= 'object';
		    pickerConfig.popup       	= true;
		    pickerConfig.labels		= {};
		    pickerConfig.labels.popupTitle = _emfLabels['cmf.relations.objectpicker.trigger'];
		    pickerConfig.nestingLevel = config.nestingLevel;
		    pickerConfig.initialSearchArgs = config.initialSearchArgs || {};
		    //TODO: Check for previous initialSearchArgs? I think that there is no need..
		    pickerConfig.initialSearchArgs.fields= ['breadcrumb_header'];
		    pickerConfig.browserConfig = {
		   		allowSelection		: true,
		    	singleSelection		: false,
		    	allowRootSelection	: true,
				filters				: "instanceType!='sectioninstance'",
				header				: "breadcrumb_header"
		    };
		    pickerConfig.listeners = {
				'basic-search-initialized': function() {
					$(this).trigger('perform-basic-search');
				},
				'before-search': function(event, args) {
					advancedSearchCriteria = args.advancedSearchCriteria;
					advancedSearchQuery = args.queryText;
				}
		    };

		    var data = select.select2('data');
		    var hasDynamicQuery = (data[0] && data[0].id.indexOf(EMF.search.advanced.DYNAMIC_QUERY) > -1);
		    //if there is already a dynamic query in the field, restore the search in the opened picker
		    if(hasDynamicQuery) {
		    	var key = data[0].id;
		    	pickerConfig.initialSearchArgs.searchType = 'advanced';
		    	pickerConfig.initialSearchArgs.advancedSearchCriteria = EMF.search.advanced.dynamicQueriesLookup[key].dynamicCriteria;
		    	pickerConfig.selectionMethod = 'objectSearch';
		    }
		    pickerConfig.dialogConfig = {};
		    pickerConfig.dialogConfig.okBtnClicked = function(plugin) {
		    	var pickerSelectionMethod = plugin.selectionMethod;
		    	if(pickerSelectionMethod === "objectManualSelect") {
		    		//if any previous tooltip for dynamic queries is present, remove it
		    		selectWrapper.removeClass('has-tooltip').find('span:last-child').remove();
		    		//Check if a dynamic query is present in the field, and reset it
		    		if(hasDynamicQuery) {
		    			delete EMF.search.advanced.dynamicQueriesLookup[data[0].id];
		    			data = [];
		    		}
		    		_.forEach(selectedItems, function (item) {
		    			var entry = {};
		    			entry.text = item.breadcrumb_header;
		    			entry.id = item.dbId;
		    			data.push(entry);
		    		});
		    	} else if (pickerSelectionMethod === "objectSearch") {
		    		selectWrapper.addClass('has-tooltip');
					selectWrapper.append(tooltipLabel);
		    		var key = EMF.search.advanced.DYNAMIC_QUERY + '_' + EMF.search.advanced.dynamicQueriesCounter++;
		    		var entry = {};
	    			entry.text = _emfLabels["search.advanced.userDefinedQuery"];
	    			entry.id = key;
	    			//only one dynamic query is allowed per criteria row
	    			data = [entry];
	    			EMF.search.advanced.dynamicQueriesLookup[key] = {
	    					dynamicCriteria : advancedSearchCriteria,
	    					dynamicQuery : advancedSearchQuery
	    			};
		    	}
		    	select.select2('data', data).trigger('change');
		    };

		    // decorator to add target attributes to links in object picker
			pickerConfig.resultItemDecorators = {
				'decorateLinks' : function(element, item) {
					element.find('a').attr('target', '_blank');
					return element;
				}
			};

			pickerConfig.onBeforeSearch.push(function() {
				EMF.ajaxloader.showLoading(placeholder);
			});
			pickerConfig.onAfterSearch.push(function() {
				EMF.ajaxloader.hideLoading(placeholder);
			});

		    placeholder.on('object-picker.selection-changed', function(event, data) {
				selectedItems = data.selectedItems;
				var isEmpty = true;
				for(key in data.selectedItems) {
					isEmpty = false;
					break;
				}
		    	if (isEmpty) {
		    		EMF.search.picker.toggleButtons(true);
				} else {
					EMF.search.picker.toggleButtons(false);
				}
		    }).on('object.tree.selection', function(data) {
		    	selectedItems = data.selectedNodes;
		    	if (selectedItems.length === 0) {
		    		EMF.search.picker.toggleButtons(true);
				} else {
					EMF.search.picker.toggleButtons(false);
				}
		    }).on('uploaded.file.selection', function(e, data) {
		    	selectedItems = [data.currentSelection];
		    	if (selectedItems.length === 0) {
		    		EMF.search.picker.toggleButtons(true);
				} else {
					EMF.search.picker.toggleButtons(false);
				}
		    });

		    placeholder.objectPicker(pickerConfig);

		    EMF.search.picker.toggleButtons(true);
		};

	}

	/**
	 * Updates table layout. Basically moves add row button to the last row or display "Add search criteria" row if there are no rows.
	 * Also removes conjunction operator (AND, OR) from the first row.
	 *
	 * @param table
	 */
	function updateTableLayout(table) {
		var rows = table.find("tr");
		for (var i=0; i<rows.length; i++) {
			var addRowColumn = $(rows[i]).children(".add-row-column");
			addRowColumn.empty();
			addRowColumn.unbind();
			// Preserving the width - CMF-9386
			addRowColumn.append($('<div />', {'class':'add-row-column'}));
		}
		var addRowColumn = $(rows[rows.length - 1]).children(".add-row-column");
		addRowColumn.empty();
		var addRowButton = $('<input />', {type: 'button', 'class': 'add-row-button'});
		addRowColumn.append(addRowButton);
		addRowButton.on('click', function(){_form.addCriteriaRow();});

		var firstRowFirstCell = table.find("tr:first").children(".condition-column");
		if (firstRowFirstCell.attr('class') !== 'empty-row') {
			// Preserving the width - CMF-9386
			var emptyCondition = $('<div />', {'class':'condition-column'});
			firstRowFirstCell.empty();
			firstRowFirstCell.append(emptyCondition);
		}
	};


	/**
	 * Builds criteria JSON for the form.
	 *
	 * @param ignoreValidation -
	 *            ignore brackets validation and empty values validation and
	 *            creates criteria as is. Used when build criteria for saved
	 *            filters in order to save the full form view
	 */
	this.buildCriteria = function(ignoreValidation) {
		var rows = this.table.find('tr');

		var criteria = [];
		var combinedCriteria = [];

		var validationError = validateBrackets(rows);

		this.table.find('.open-brackets-column div').removeClass('has-error');
		this.table.find('.close-brackets-column div').removeClass('has-error');

		if (validationError != null) {
			$(rows[validationError.row]).find('.' + validationError.brackets + '-brackets-column div').addClass('has-error');
		}
		if(validationError == null || ignoreValidation){
			var hasCriteria = rows.length >= 1 && $(rows[0]).children("td:first").attr('class') !== 'empty-row';
			if (hasCriteria) {
				for (var i=0; i<rows.length; i++) {
					var selectedField = $(rows[i]).find('.field-column div.select2-container').select2('val');

					var solrField = null;
					if (fieldsLookup[selectedField]) {
						solrField = fieldsLookup[selectedField].solrField;
						if (!solrField) {
							solrField = selectedField;
						}
					}

					var comparisonOperator = $(rows[i]).find('.comparison-column div.select2-container').select2('val');

					var openBrackets = $(rows[i]).find('.open-brackets-column div.select2-container').select2('val') || 0;
					var closeBrackets = $(rows[i]).find('.close-brackets-column div.select2-container').select2('val') || 0;

					var conditionSelect = $(rows[i]).find('.condition-column div');
					// AND, OR
					var conjunctionOperator = '';
					if (conditionSelect.length > 0) {
						conjunctionOperator = conditionSelect.select2('val');
					}

					var values = getCriteriaValues($(rows[i]));

					if (values.length > 0 || ignoreValidation) {
						if (criteria.length > 0) {
							// Add conjunction operator
							criteria.push({operator:conjunctionOperator});
						}
						var criterion = {openBrackets: parseInt(openBrackets), field: solrField, operator: comparisonOperator, values: values, closeBrackets: parseInt(closeBrackets), row: i };	
						var key = values[0];
						var dynamicCriteriaJson = EMF.search.advanced.dynamicQueriesLookup[key];
						if (dynamicCriteriaJson) {
							criterion.dynamicCriteria = dynamicCriteriaJson.dynamicCriteria;
							criterion.dynamicQuery = dynamicCriteriaJson.dynamicQuery;
						}
						criteria.push(criterion);
					}
				}
			}
		}
		return criteria;
	};

	/**
	 * Converts flat structure criteria into hierarchical one grouped by brackets.
	 *
	 * @param criteria
	 * @returns
	 */
	function combineCriteria(criteria) {
		var lastOpenBracket = null;
		var firstCloseBracket = null;
		for(var i=0; i<criteria.length; i+=2) {
			var criterion = criteria[i];
			if (criterion.openBrackets>0) {
				lastOpenBracket = i;
			}
			if (criterion.closeBrackets>0) {
				firstCloseBracket = i;
				if (lastOpenBracket === null) {
					return {error: 'Too much closing brackets at: ' + criterion.row};
				} else {
					//TODO: check for multiple brackets at lastOpenBracket and firstCloseBracket. If both have more than 1 subtract all additional brackets

					var newCriteria = [];

					criteria.slice(0,lastOpenBracket).forEach(function(value) {newCriteria.push(value);});

					//newCriteria.pushAll(criteria.slice(0,lastOpenBracket));

					var group = criteria.slice(lastOpenBracket, firstCloseBracket+1);

					if (group[0].openBrackets > 0 && group[group.length-1].closeBrackets > 0) {
						var additionalBrackets = Math.min(group[0].openBrackets, group[group.length-1].closeBrackets);

						group[0].openBrackets-=additionalBrackets;
						group[group.length-1].closeBrackets-=additionalBrackets;
					}
					newCriteria.push({openBrackets: group[0].openBrackets, group: group, closeBrackets: group[group.length-1].closeBrackets});
					criteria.slice(firstCloseBracket+1).forEach(function(value) {newCriteria.push(value);});

					//newCriteria.pushAll(criteria.slice(firstCloseBracket+1));
					return combineCriteria(newCriteria);
				}
			}
		}

		if (lastOpenBracket != null) {
			return {error: 'Too much open brackets at: ' + criteria[lastOpenBracket].row};
		}
		return criteria;
	};

	/**
	 * Builds solr query for this form.
	 */
	this.buildSolrQuery = function() {
		var criteria = this.buildCriteria();
		return criteriaToSOLRQuery(combineCriteria(criteria));
	};

	this.buildSPARQLQuery = function(index) {
		var query = '';
		var criteria = this.buildCriteria();
		return EMF.search.advanced.form.sparqlBuilder.criteriaToSPARQLQuery(combineCriteria(criteria), index, fieldsLookup, config.nestingLevel);
	};

	/**
	 * Creates array of values for given table row.
	 * @param row - row from the criteria table
	 * @returns {Array} - array with values depending of the number of inputs
	 */
	function getCriteriaValues(row) {
		var selectedField = row.find('.field-column div.select2-container').select2('val');
		var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');
		var values = [];
		var field = fieldsLookup[selectedField];
		if (field) {
			if (field['type'] === 'number') {
				if (comparisonOperator === 'BETWEEN') {
					var from = row.find('.input-column input:first');
					var to = row.find('.input-column input:last');
					if (from.val() || to.val()) {
						values.push(from.val() ? from.val() : '*');
						values.push(to.val() ? to.val() : '*');
					}
				} else {
					var value = row.find('.input-column input').val();
					if (value) {
						values.push(value);
					}
				}
			} else if (isDate(field['type'])) {
				if (comparisonOperator === 'BETWEEN') {
					var from = row.find('.input-column input:first');
					var to = row.find('.input-column input:last');
					var fromDate = from.datetimepicker('getDate');
					var toDate = to.datetimepicker('getDate');
					if (fromDate instanceof Date || toDate instanceof Date) {
						values.push(fromDate?fromDate.toJSON():'*');
						values.push(toDate?toDate.toJSON():'*');
					}
				} else if (comparisonOperator === 'IS_WITHIN') {
					values = extractSelect2Values(row);
				} else if (comparisonOperator === 'IS') {
					var selectedDate = row.find('.input-column input').datepicker('getDate');
					if (selectedDate instanceof Date) {
						values.push(selectedDate.toJSON());
					}
				} else {
					var selectedDate = row.find('.input-column input').datetimepicker('getDate');
					if (selectedDate instanceof Date) {
						values.push(selectedDate.toJSON());
					}
				}
			} else {
				if(row.find('.input-column div.select2-container').length) {
					values = extractSelect2Values(row);
				} else {
					var value = row.find('.input-column input').val();
					if (value) {
						values.push(value);
					}
				}

			}
		}
		return values;
	};

	/**
	 * Extracts the select2 values from the provided advanced search row.
	 *
	 * @param row the provided row
	 * @returns the values
	 */
	function extractSelect2Values(row) {
		var selectedValues = row.find('.input-column div.select2-container').select2('val');
		if (selectedValues) {
			if (typeof selectedValues === 'string') {
				return [selectedValues];
			} else {
				return $.map(selectedValues, function(el){
					return el;
				});
			}
		}
		return [];
	};

	/**
	 * Removes validation error css styles.
	 * @param rows
	 */
	function clearValidatedBracketsErrors(rows) {
		for (var i=0; i<rows.length; i++) {
			$(rows[i]).find('.open-brackets-column').removeClass('delete-target');
			$(rows[i]).find('.close-brackets-column').removeClass('delete-target');
		}
	};

	/**
	 * Validates brackets and returns first found error.
	 * @param rows
	 * @returns - error JSON object with error message. Type of brackers - open or close and the row where error occur
	 */
	function validateBrackets(rows) {
		var brackets = [];
		for (var i=0; i<rows.length; i++) {
			var values = getCriteriaValues($(rows[i]));
			if (values && values.length > 0) {
				var openBrackets = $(rows[i]).find('.open-brackets-column div.select2-container').select2('val');
				if (openBrackets>0) {
					brackets.push({row: i, brackets: openBrackets});
				}

				var closeBrackets = $(rows[i]).find('.close-brackets-column div.select2-container').select2('val');

				for (var j=brackets.length-1; j>=0 && closeBrackets>0; j--) {
					var remaining = brackets[j].brackets - closeBrackets;
					if (remaining <= 0) {
						// Most recent open brackets are all closed. Remove
						brackets.splice(brackets.length-1, 1);
						closeBrackets = Math.abs(remaining);
					} else {
						closeBrackets = 0;
						brackets[j].brackets = remaining;
					}
				}

				if (closeBrackets > 0) {
					return {error: "Too many closing brackets", brackets:'close', row:i };
				}
			}
		}

		if (brackets.length>0) {
			return {error: "Too many open brackets", brackets:'open', row:brackets[brackets.length-1].row };
		}
	};

	/**
	 * Converts normalized criteria into SOLR query.
	 * @param criteria
	 * @returns {String}
	 */
	function criteriaToSOLRQuery(criteria) {
		var queries = [];
		var fq = '';
		var countQueries = 0;
		for ( var i = 0; i < criteria.length; i+=2) {
			var criterion = criteria[i];
			var conjunctionOperator = (i===0?'':criteria[i-1].operator);

			var singleQuery = '';
			if (criterion.group) {
				singleQuery = '(' + criteriaToSOLRQuery(criterion.group) + ')';
			} else {
				var fieldType = fieldsLookup[criterion.field];
				if (fieldType['type'] === 'string' || fieldType['type'] === 'select' || fieldType['type'] === 'autocomplete') {
					criterion.values.forEach(function(value, index, theArray) {
						theArray[index] = EMF.search.advanced.escapeQuerySpecialCharacters(value);
					});
				}

				switch(criterion.operator) {
					case 'BETWEEN':
						singleQuery = criterion.field + ':' + '[' + criterion.values[0] + ' TO ' + criterion.values[1] + ']';
						break;
					case 'IN':
						singleQuery = criterion.field + ':(' + criterion.values.join(' OR ') + ')';
						break;
					case 'NOT_IN':
						singleQuery = '-' + criterion.field + ':(' + criterion.values.join(' OR ') + ')';
						break;
					case 'EQUALS':
						singleQuery = criterion.field + ':(' + criterion.values.join(' OR ') + ')';
						break;
					case 'NOT_EQUALS':
						singleQuery = '-' + criterion.field + ':(' + criterion.values.join(' OR ') + ')';
						break;
					case 'CONTAINS':
						singleQuery = criterion.field + ':(*' + criterion.values.join('* OR *') + '*)';
						break;
					case 'DOES_NOT_CONTAIN':
						singleQuery = '-' + criterion.field + ':(*' + criterion.values.join('* OR *') + '*)';
						break;
					case 'STARTS_WITH':
						singleQuery = criterion.field + ':(' + criterion.values.join('* OR ') + '*)';
						break;
					case 'DOES_NOT_START_WITH':
						singleQuery = '-' + criterion.field + ':(' + criterion.values.join('* OR ') + '*)';
						break;
					case 'ENDS_WITH':
						singleQuery = criterion.field + ':(*' + criterion.values.join(' OR *') + ')';
						break;
					case 'DOES_NOT_END_WITH':
						singleQuery = '-' + criterion.field + ':(*' + criterion.values.join(' OR *') + ')';
						break;
					case 'LOWER_THAN':
						singleQuery = criterion.field + ':' + '[* TO ' + criterion.values[0] + ']';
						break;
					case 'GREATER_THAN':
						singleQuery = criterion.field + ':' + '[' + criterion.values[0] + ' TO *]';
						break;
					case 'IS':
						singleQuery = createISQuery(criterion);
						break;
					case 'IS_NOT':
						// not specified
						if (criterion.values[0] === '\\-1') {
							singleQuery = criterion.field + ':*';
						} else {
							singleQuery =  '-' + criterion.field + ':(' + criterion.values.join(' OR ') + ')';
						}
						break;
					case 'IS_AFTER':
						singleQuery = criterion.field + ':' + '[' + criterion.values[0] + ' TO *]';
						break;
					case 'IS_BEFORE':
						singleQuery = criterion.field + ':' + '[* TO ' + criterion.values[0] + ']';
						break;
					case 'IS_WITHIN':
						var range = _.find(EMF.search.advanced.dateRanges, {id: criterion.values[0]});
						if(_.isUndefined(range)) {
							console.error(criterion.values[0] + ' has no date range!');
							break;
						}

						var start = EMF.search.advanced.offsetToDate(range.startOffset);
						var end = EMF.search.advanced.offsetToDate(range.endOffset);

						var startDate = '*';
						if(!_.isUndefined(start)) {
							startDate = '"' + start.toJSON() + '"';
						}

						var endDate = '*';
						if(!_.isUndefined(end)) {
							endDate = '"' + end.toJSON() + '"';
						}

						singleQuery = criterion.field + ':' + '[' + startDate + ' TO ' + endDate + ']';
						break;
					default:
				}
			}

			if (conjunctionOperator === 'OR') {
				queries.push({fq: fq, countQueries: countQueries});
				fq = singleQuery;
				countQueries = 1;
			} else {
				// Either first line (no conjunctionOperator) or AND
				fq += (conjunctionOperator===''?'':' AND ') + singleQuery;
				countQueries++;
			}
		}

		if (fq != '') {
			queries.push({fq:fq, countQueries:countQueries});
		}
		var result = '';
		if (queries.length === 1) {
			result = queries[0].fq;
		} else {
			for ( var int = 0; int < queries.length; int++) {
				if (result != '') {
					result += ' OR ';
				}
				if (queries[int].countQueries > 1) {
					result += '(' + queries[int].fq + ')';
				} else {
					result += queries[int].fq;
				}
			}
		}
		return result;
	};

	/**
	 * Creates an IS criteria based on the field type and add it to the query.
	 *
	 * @param criterion
	 *        the criterion
	 */
	function createISQuery(criterion){
		var fieldType = fieldsLookup[criterion.field];
		var singleQuery = null;
		if (isDate(fieldType['type'])) {
			var exactDate = new Date(criterion.values[0]);
			exactDate.setDate(exactDate.getDate() + 1);
			singleQuery = criterion.field + ':' + '[' + criterion.values[0] + ' TO ' + exactDate.toJSON() + ']';
		} else {
			// not specified
			if (criterion.values[0] === '\\-1') {
				singleQuery = '-' + criterion.field + ':*';
			} else {
				singleQuery = criterion.field + ':(' + criterion.values.join(' OR ') + ')';
			}
		}
		return singleQuery;
	};
	
	if (fieldsByDefault && fieldsByDefault.length > 0) {
		fieldsByDefault.forEach(function(field) {
			this.addCriteriaRow({field: field});
		}.bind(this));
	} else {
		addEmptyRow(this.table);
	}

	if (onChanged) {
		form.on('change propertychange keyup input paste', function(){ onChanged(_form); });
	}

	return this;
};

/**
 * SPARQL builder for advanced search form (only the form inside of a single group).
 */
EMF.search.advanced.form.sparqlBuilder = (function () {

	/**
	 * Looks for and remove any object relation (any_object) from relations values array
	 *
	 * @param values all selected relations
	 * @returns {Boolean} true if any object relation was found in the values array. False otherwise
	 */
	function extractAnyObjectRelation(values) {
		var hasAnyObject = false;
		for ( var i = 0; i < values.length && !hasAnyObject; i++) {
			hasAnyObject = values[i] === 'any_object';
			if (hasAnyObject) {
				values.splice(i, 1);
			}
		}
		return hasAnyObject;
	};

	/**
	 * Replace all values from current context with their actual URI-s
	 *
	 * @param values - values extracted fro the given field
	 * @returns values array with context items replace with their actual URI-s
	 */
	function replaceContextValues(values) {
		var replaced = [];
		for ( var i = 0; i < values.length; i++) {
			var value = values[i];
			switch (value) {
			case 'emf-search-context-current-project':
				if (EMF.currentPath.length && EMF.currentPath[0].type === 'projectinstance') {
					replaced.push(value = EMF.currentPath[0].id);
				}
				break;
			case 'emf-search-context-current-case':
				if (EMF.currentPath.length >= 1 && EMF.currentPath[1].type === 'caseinstance') {
					replaced.push(value = EMF.currentPath[1].id);
				}
				break;
			case 'emf-search-context-current-object':
				if (EMF.currentPath.length) {
					replaced.push(value = EMF.currentPath[EMF.currentPath.length - 1].id);
				}
				break;
			default:
				replaced.push(value);
				break;
			}
		}
		return replaced;
	};

	/**
	 * Function to escape special characters in semantic URIs
	 * @param uri
	 * @param isShort true for short URI, false otherwise
	 * @returns escaped URI
	 */
	function escapeURI(uri, isShort) {
		var separator = isShort?':':'#';
		var pos = uri.indexOf(separator);
		if (pos > 0) {
			uri = uri.substr(0, pos) + separator + encodeURIComponent(uri.substr(pos+1));
		}
		return uri;
	};

	return {
		stringToSPARQL: function(criterion, field, index) {

			var variableName = '?var' + index;
			var fieldURI = field.uri;
			if (field.id === EMF.search.advanced.ANY_FIELD) {
				fieldURI = variableName + '_0';
			}
			var query = '{ ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + fieldURI + ' ' + variableName;

			var union = ' || ';
			// Wether to include instances for which given property does not exist
			var includeNotExisting = false;
			var regexPrefix = '';
			var regexSuffix = '';
			switch(criterion.operator) {
				case 'IN':
				case 'EQUALS':
					regexPrefix = '^';
					regexSuffix = '$';
					break;
				case 'CONTAINS':
					// does not change
					break;
				case 'STARTS_WITH':
					regexPrefix = '^';
					break;
				case 'ENDS_WITH':
					regexSuffix = '$';
					break;
				case 'DOES_NOT_CONTAIN':
					includeNotExisting = true;
					union = ' && ';
					regexPrefix = '^((?!';
					regexSuffix = ').)*$';
					break;
				case 'NOT_IN':
				case 'NOT_EQUALS':
					includeNotExisting = true;
					union = ' && ';
					regexPrefix = '^(?!';
					regexSuffix = '$).*$';
					break;
				case 'DOES_NOT_START_WITH':
					includeNotExisting = true;
					union = ' && ';
					regexPrefix = '^(?!';
					regexSuffix = ').*$';
					break;
				case 'DOES_NOT_END_WITH':
					includeNotExisting = true;
					union = ' && ';
					regexPrefix = '^.*(?<!';
					regexSuffix = ')$';
					break;
				default:
			}

			var filterQuery = ' FILTER(';
			for ( var i = 0; i < criterion.values.length; i++) {
				var regexValue = criterion.values[i];
				regexValue = EMF.search.advanced.escapeRegExp(regexValue);
				// Escape SPARQL special symbols (quotes and slashes)
				regexValue = regexValue.replace(/["\\]/g, '\\\$&');
				if (i > 0) {
					filterQuery += union;
				}
				regexValue = regexPrefix + regexValue + regexSuffix;
				filterQuery += 'regex(lcase(' + variableName + '), "' + regexValue.toLowerCase() + '")';
			}
			filterQuery += ')';
			query += filterQuery + ' }';
			if (includeNotExisting && field.id !== EMF.search.advanced.ANY_FIELD) {
				var checkVariableName = variableName + 'Check';
				query = '{ {OPTIONAL { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + fieldURI + ' ' + variableName + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME
						+ ' emf:isDeleted ' + checkVariableName + ' }  FILTER (!bound(' + checkVariableName + ')) } \nUNION\n ' + query + ' }';
			}
			return query;
		},

		objectFullURIToSPARQL: function(criterion, field, index) {
			// just an unique name to avoid naming conflicts
			var variableName = '?var' + index;
			criterion.values.forEach(function(val, index, theArray){
				theArray[index] = ' { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + variableName + ' <' + escapeURI(val, false) +'> } ';
			});
			var query = ' { ' + criterion.values.join(' UNION\n') + ' }';
			if ('NOT_IN' === criterion.operator) {
				var checkVariableName = variableName + 'Check';
				query = ' OPTIONAL { ' + query + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' } FILTER (!bound(' + checkVariableName + '))';
			}
			return query;
		},

		objectShortURIToSPARQL: function(criterion, field, index) {
			var variableName = field.uri;
			//the original criterion values should not be modified, so copy the object instead
			var criterionInternal = $.extend(true, {}, criterion);

			criterionInternal.values.forEach(function(val, index, theArray){
				if (val === EMF.search.CURRENT_USER) {
					//replace the constant value with the actual URI of the currently logged user
					val = EMF.currentUser.id;
				}
				theArray[index] = ' { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + escapeURI(val, false) + ' } ';
			});
			var query = ' { ' + criterionInternal.values.join(' UNION\n') + ' } ';
			if ('NOT_IN' === criterion.operator) {
				var checkVariableName = '?var' + index + field.uri.replace(':', '_') + 'Check';
				query = ' OPTIONAL { ' + query + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' } FILTER (!bound(' + checkVariableName + '))';
			}
			return query;
		},

		dateToSPARQL: function(criterion, field, index) {
			var variableName = '?var' + index;
			var query = query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName + ' . ';
			switch(criterion.operator) {
				case 'BETWEEN':
					if (criterion.values.length === 2) {
						if (criterion.values[0] !== '*') {
							query += this.dateStringToSPARQLQuery(variableName, criterion.values[0], '>=');
						}
						if (criterion.values[1] !== '*') {
							query += this.dateStringToSPARQLQuery(variableName, criterion.values[1], '<');
						}
					}
					break;
				case 'IS':
					var exactDate = new Date(criterion.values[0]);
					exactDate.setDate(exactDate.getDate() + 1);

					query += this.dateStringToSPARQLQuery(variableName, criterion.values[0], '>=');
					query += this.dateToSPARQLQuery(variableName, exactDate, '<');

					break;
				case 'IS_AFTER':
					query += this.dateStringToSPARQLQuery(variableName, criterion.values[0], '>');
					break;
				case 'IS_BEFORE':
					query += this.dateStringToSPARQLQuery(variableName, criterion.values[0], '<');
					break;
				case 'IS_WITHIN':
					var range = _.find(EMF.search.advanced.dateRanges, {id: criterion.values[0]});
					var start = EMF.search.advanced.offsetToDate(range.startOffset);
					var end = EMF.search.advanced.offsetToDate(range.endOffset);

					if(!_.isUndefined(start)) {
						query += this.dateToSPARQLQuery(variableName, start, '>=');
					}

					if(!_.isUndefined(end)) {
						query += this.dateToSPARQLQuery(variableName, end, '<=');
					}
					break;
				default:
			}
			return query;
		},

		dateStringToSPARQLQuery: function(field, dateString, comparison) {
			return ' FILTER (' + field + ' ' + comparison + ' xsd:dateTime("' + dateString + '")) ';
		},

		dateToSPARQLQuery: function(field, date, comparison) {
			return this.dateStringToSPARQLQuery(field, date.toJSON(), comparison);
		},

		relationToSPARQL: function(criterion, field, index) {
			var variableName = '?var' + index;
			var hasAnyObject = extractAnyObjectRelation(criterion.values);
			var query = '', relationVariable, relationTypeQuery = '';
			if (criterion.field === EMF.search.advanced.ANY_RELATION) {
				// Keep it as a variable
				relationVariable = variableName + '_rel';
				relationTypeQuery = relationVariable + ' a owl:ObjectProperty .\n';
			} else {
				relationVariable = field.uri;
			}

			var objectSearchMode = criterion.dynamicCriteria ? true : false;
			criterion.values = criterion.values.map(function(val) {
				if (val.indexOf(EMF.search.advanced.DYNAMIC_QUERY) > -1) {
					var innerInstance = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + index;
					//if the inner query contains ?instance, append it the nesting level indication for uniqueness in the final query
					var innerQuery = EMF.search.advanced.getReplacedInnerInstanceNames(criterion.dynamicQuery, index);
					val = innerInstance + ' } . ' + innerQuery;
				}
				return val;
			});

			var actualValues = replaceContextValues(criterion.values);
			actualValues.forEach(function(val, index, theArray){
				theArray[index] = ' { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + relationVariable + ' ' + escapeURI(val, false) +' } ';
			});
			
			var checkVariableName = variableName + 'Check';

			//when we have dynamic queries, additional opening bracket is needed after
			//the 'OPTIONAL' clause in order to have a properly formatted query
			var additionalOpeningBracket = "";
			if(objectSearchMode) {
				additionalOpeningBracket = "{ ";
			}
			switch(criterion.operator) {
				case 'SET_TO':
					if (hasAnyObject) {
						query = relationTypeQuery + '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + relationVariable + ' ' + variableName;
					} else {
						query += relationTypeQuery + '{ ' + actualValues.join(' UNION\n');
						//When manually selected, the query needs one more closing parenthesis
						if(!objectSearchMode) {
							query += ' }';
						}
					}
					break;
				// There is no relation of the specified type (or any relation) to the specified objects
				case 'NOT_SET_TO':
					if (hasAnyObject) {
						query = 'OPTIONAL { ' + additionalOpeningBracket + relationTypeQuery + ' ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + relationVariable + ' ' + variableName + ' . ?'
								+ EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' .  } FILTER(!bound(' + checkVariableName + '))';
					} else {
						query += 'OPTIONAL { ' + additionalOpeningBracket + actualValues.join(' UNION\n') + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName
								+ ' .  } FILTER(!bound(' + checkVariableName + '))';
					}
					break;
				// There is at least one relation of the specified type (or any relation) but not to the specified objects
				case 'SET_TO_SOME_BUT_NOT_TO':
					if (hasAnyObject) {
						query = 'OPTIONAL { ' + additionalOpeningBracket + relationTypeQuery + ' ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + relationVariable + ' ' + variableName
								+ ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' .  } FILTER(!bound(' + checkVariableName + '))';
					} else {
						query = relationTypeQuery + '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + relationVariable + ' ' + variableName + ' .\n';
						query += 'OPTIONAL { ' + additionalOpeningBracket + actualValues.join(' UNION\n') + ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' .  } FILTER(!bound(' + checkVariableName + '))';
					}
					break;
				default:
					break;
			}
			return query;
		},

		booleanToSPARQL: function(criterion, field, index) {
			var variableName = '?var' + index;
			var query = '';
			var value = criterion.values[0];
			switch(criterion.operator) {
				case 'IS':
					if (value === '-1') {
						var checkVariableName = variableName + 'Check';
						query = 'OPTIONAL { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName
								+ ' . ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' emf:isDeleted ' + checkVariableName + ' .  } FILTER(!bound(' + checkVariableName + '))';
					} else {
						query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' "' + value + '"^^xsd:boolean';
					}
					break;
				case 'IS_NOT':
					if (value === '-1') {
						query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName;
					} else {
						query = 'MINUS { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' '  + field.uri + ' "' + value + '"^^xsd:boolean }';
					}
					break;
			    default:
			}
			return query;
		},

		numberToSPARQL: function(criterion, field, index) {
			var variableName = '?var' + index;
			var query = '';
			switch(criterion.operator) {
				case 'EQUALS':
					query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' "' + criterion.values[0] + '"^^xsd:long';
					break;
				case 'NOT_EQUALS':
					query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName + '. \n' +
						'MINUS { ?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' "' + criterion.values[0] + '"^^xsd:long }';
					break;
				case 'LOWER_THAN':
					query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName + '. \n' +
						'FILTER (' + variableName + ' < ' + criterion.values[0] + ')';
					break;
				case 'GREATER_THAN':
					query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName + '. \n' +
						'FILTER (' + variableName + ' > ' + criterion.values[0] + ')';
					break;
				case 'BETWEEN':
					if (criterion.values.length === 2) {
						query = '?' + EMF.search.advanced.INSTANCE_VARIABLE_NAME + ' ' + field.uri + ' ' + variableName + ' .\n FILTER (';
						var addAnd = false;
						if (criterion.values[0] !== '*') {
							query += variableName + ' > ' + criterion.values[0];
							addAnd = true;
						}
						if (criterion.values[1] !== '*') {
							if (addAnd) {
								query += ' && ';
							}
							query += variableName + ' < ' + criterion.values[1];
						}
						query += ')';
					}
					break;
			    default:
			}
			return query;
		},

		/**
		 * Builds a SPARQL sub-query corresponding to the passed criterion object.
		 *
		 * @param criterion
		 * 					is the criterion object to be processed
		 * @param field
		 * 				is the field
		 * @param indexPostfix
		 * 					is the unique postfix that will be appended to the query
		 */
		buildSingleQuery : function(criterion, field, indexPostfix) {
			switch(field.type) {
				case 'string':
					return this.stringToSPARQL(criterion, field, indexPostfix);
					break;
				case 'date':
				case 'dateTime':
					return this.dateToSPARQL(criterion, field, indexPostfix);
					break;
				case 'pickable':
					return this.relationToSPARQL(criterion, field, indexPostfix);
					break;
				case 'autocomplete':
					if ('object' === field.subType) {
						return this.objectFullURIToSPARQL(criterion, field, indexPostfix);
					} else {
						return this.stringToSPARQL(criterion, field, indexPostfix);
					}
					break;
				case 'boolean':
					return this.booleanToSPARQL(criterion, field, indexPostfix);
					break;
				case 'number':
					return this.numberToSPARQL(criterion, field, indexPostfix);
					break;
				case 'user':
				case 'group':
				case 'agent':
					return this.objectShortURIToSPARQL(criterion, field, indexPostfix);
					break;
				default:
					return '';
				break;
			}
		},

		criteriaToSPARQLQuery : function(criteria, index, fieldsLookup, nestingLevel) {
			var queries = [];
			var fq = '';
			var countQueries = 0;

			for ( var i = 0; i < criteria.length; i+=2) {
				var criterion = criteria[i];
					var singleQuery = '';
					var conjunctionOperator = (i===0?'':criteria[i-1].operator); 
					if (criterion.group) {
						// If we have a hierarchical criterion object with a 'group' attribute, call this method recursively
						//to process the criteria rows of the group
						singleQuery = ' { ' + this.criteriaToSPARQLQuery(criterion.group, index, fieldsLookup, nestingLevel) + ' } ';
					} else if (criterion.values && criterion.values.length > 0) {
							var field = fieldsLookup[criterion.field];
							//the purpose of the index is to provide unique suffix for the query's subject and predicate
							//in order to avoid naming conflicts with more complex queries
							var indexPostfix = index + '_' + i + '_' + (nestingLevel+1);
							singleQuery = this.buildSingleQuery(criterion, field, indexPostfix);
					}
					if (conjunctionOperator === 'OR') {
						queries.push({fq: fq, countQueries: countQueries});
						fq = singleQuery;
						countQueries = 1;
					} else {
						// Either first line (no conjunctionOperator) or AND. Append it to the current fq
						fq += (conjunctionOperator===''?'':' .\n ') + singleQuery;
						countQueries++;
					}
			}

			if (fq != '') {
				queries.push({fq:fq, countQueries:countQueries});
			}
			var result = '';
			if (queries.length === 1) {
				result =  ' { ' + queries[0].fq + ' } ';
			} else {
				for ( var int = 0; int < queries.length; int++) {
					if (result != '') {
						result += '\n UNION \n';
					}
					//if (queries[int].countQueries > 1) {
						result += ' { ' + queries[int].fq + ' } ';
					//}
				}
			}
			return result;
		}
	}
})();
