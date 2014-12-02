var EMF = EMF || {};

EMF.search = EMF.search || {};

EMF.search.advanced = function(placeholder, config) {
	var _this = this;
	_this.groups = [];
	_this.groups.push = function () {
		var result = Array.prototype.push.apply(this,arguments);
		if (this.length > 1) {
			this[0].element.find('.delete-group-button').show();
		} else if (this.length === 1) {
			this[0].element.find('.delete-group-button').hide();
		}
		return result;
	};	
	_this.groups.splice = function () {
		var result = Array.prototype.splice.apply(this,arguments);
		if (this.length === 1) {
			this[0].element.find('.delete-group-button').hide();
		}			
		return result;
	};
	
	var objectTypeServiceURL = "";
	
	// Fixes a problem with ui.dialog and select2 controls
	$.ui.dialog.prototype._allowInteraction = function(e) {
		var result = !!$(e.target).closest('.ui-dialog, .ui-datepicker, .select2-input').length;
		if ($(e.target).closest('.select2-input').length == 0) {
			$('div.select2-dropdown-open').select2('close');
		}
		return result;
	};

	config = config || {};
	this.config = config;
	config.contextPath = config.contextPath || SF.config.contextPath;
	
	objectTypeServiceURL = config.contextPath + '/service/definition/all-types?addFullURI=true';

	var queryHolder = $('<div/>', {'class': 'dynamic-form-panel'});
	$(placeholder).prepend(queryHolder);

	// Toggler functionality.
	var advancedSearchToggler = $('<span/>', {'class' : 'advanced-search-toggler'});
	advancedSearchToggler.append($('<a />', {'text':'Advanced Search Query', 'class' : 'text-primary' }));
	advancedSearchToggler.append($('<b />', {'class' : 'caret'}));
	advancedSearchToggler.click(function () {
		$($(placeholder).find('.advanced-search-query')[0]).slideToggle();
	});
	queryHolder.append(advancedSearchToggler);

	queryHolder.append($('<textarea />', {rows: 2, 'class': 'advanced-search-query boxsizingBorder', 'id': 'advanced-search-query', 'style':'display: none;', 'autocomplete':'off'}));
	
	var advancedSearchQuery = $($(placeholder).find('.advanced-search-query')[0]);
	
	if (config.onAdvancedSearchChanged && $.isFunction(config.onAdvancedSearchChanged)) {
		advancedSearchQuery.on('change propertychange keyup input paste', config.onAdvancedSearchChanged);
	}

	var advSearchHolder = $('<div/>');
	$(placeholder).prepend(advSearchHolder);

	config.onChanged = function(group) {
		advancedSearchQuery.val(_this.buildSolrQuery()).trigger('change');
	};

	config.onDeleted = function(group) {
		for ( var int = 0; int < _this.groups.length; int++) {
			if (_this.groups[int] == group) {
				_this.groups.splice(int, 1);
			}
		}
		advancedSearchQuery.val(_this.buildSolrQuery()).trigger('change');
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
					if (category != null) {
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
			_this.loadCriteria(config.initialCriteria);
			//_this.groups.push(new EMF.search.advanced.group(advSearchHolder, config));
		}
	});
	$($(placeholder).find('.add-search-group-button')[0]).on('click', function() {
		_this.groups.push(new EMF.search.advanced.group(advSearchHolder, config));
	});
	
	this.buildSolrQuery = function() {
		var query = '';
		if (_this.groups.length > 1) {
			for ( var int = 0; int < _this.groups.length; int++) {
				var groupQuery = _this.groups[int].buildSolrQuery();
				if (groupQuery != '') {
					var moreThanOneCriteria = groupQuery.indexOf(' AND ') > -1 || groupQuery.indexOf(' OR ') > -1;
					query += (query!=''?' OR ':'') + (moreThanOneCriteria?'(':'') +  groupQuery + (moreThanOneCriteria?')':'');
				}
			}
		} else if (_this.groups.length == 1){
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
	
	this.reset = function(doNotAddGroup) {
		for ( var int = 0; int < _this.groups.length; int++) {
			_this.groups[int].element.remove();
		}
		// Clear the array
		while(_this.groups.length > 0) {
			_this.groups.pop();
		}
		if (!doNotAddGroup) {
			_this.groups.push(new EMF.search.advanced.group(advSearchHolder, config));
		}
		advancedSearchQuery.val(_this.buildSolrQuery()).trigger('change');
	};
	
	this.loadCriteria = function(criteria, callback) {
		if (criteria) {
			this.reset(true);
			var criteriaNumber = criteria.length;
			for ( var int = 0; int < criteria.length; int++) {
				var groupConfig = $.extend(true, {}, config);
				groupConfig.defaultObjectType = criteria[int].objectType;
				groupConfig.initialCriteria = criteria[int].criteria;
				groupConfig.initializedCallback = function() {
					// Criteria are loaded asynchronously, so we call this callback after each criteria is loaded. When all are loaded main callback is called.
					if (--criteriaNumber <= 0) {
						criteriaNumber = 0;
						if ($.isFunction(callback)) {
							callback();
						}
					}
				};
				
				_this.groups.push(new EMF.search.advanced.group(advSearchHolder, groupConfig));
			}
		} else {
			this.reset(false);
			if ($.isFunction(callback)) {
				callback();
			}
		}
	};

	return this;
};

EMF.search.advanced.escapeQuerySpecialCharacters = function(value) {
	var escapedValue = '';
	if (value) {
		for ( var int = 0; int < value.length; int++) {
			var c = value[int];
			 if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'
			        || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
			        || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'
			        || /\s/.test(c)) {
				 escapedValue += '\\';
			  }
			 escapedValue += c;
		}
	}
	return escapedValue;
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

	var selectWidth = 'width:' + parent.css('width');
	var selectOptions = {'style':selectWidth};

	var select2Options = $.extend({}, config.select2Options);
	select2Options.width = 'resolve';

	var select = $('<div />', selectOptions);
	parent.append(select);

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

EMF.search.advanced.group = function(placeholder, config) {
	var _this = this;
	
	config.contextPath = config.contextPath || SF.config.contextPath;
	
	var fieldsByTypeServiceURL = config.contextPath + '/service/properties/searchable-fields';
	
	var groupHolder = $('<div />', {'class': 'advanced-search-group'});
	if (!placeholder) {
		//ERROR
	}
	placeholder.append(groupHolder);
	_this.element = groupHolder;
	
	var form = null;
	var onGroupChanged = config.onChanged || function(){};
	var onGroupDeleted = config.onDeleted || function(){};
	
	groupHolder.html('<b>Object type: </b>');
	var formHolder = null;
	
	_this.formSolrQuery = '';
	_this.formCriteria = [];
	
	var objectTypeConfig = {data: config.objectTypes, width: '400px', 'class':'advanced-search-object-type', searchable: true, onChange: function(evt) {
		var url = fieldsByTypeServiceURL;
		if (evt.val != 'all') {
			var params = {};
			var directType = config.objectTypesParents[evt.added.id];
			if (directType) {
				params['forType'] = directType + '_' + evt.added.id;
			} else {
				params['forType'] = evt.added.id + '_';
			}
			url += '?' + $.param(params);
		}

		//formHolder.append(form);
		_this.formSolrQuery = '';
		// Empty form criteria array
		while(_this.formCriteria.length > 0) {
			_this.formCriteria.pop();
		}
		onGroupChanged(_this);
		
		EMF.blockUI.showAjaxLoader();
		$.ajax({
			dataType: "json",
			url:  url,
			async: true,
			complete: function(response) {
				var result = $.parseJSON(response.responseText);
				//TODO: Fix types. Remove type property
				
				result = result.map(function(val){
					var newType = null;
					switch(val.type) {
						case 'text_general':
							newType = 'string';
							break;
						case 'long':
							newType = 'number';
							break;
						case 'tdates':
							newType = 'date';
							break;
						default:
							newType = 'string';
					}
					
					val.type = newType;
					return val;
				});

				formHolder.empty();
				form = new EMF.search.advanced.form(formHolder, {fields:result, onChanged:function(searchForm) {
					var firstRowFirstCell = searchForm.table.find("tr:first").children("td:first");
					if (firstRowFirstCell.attr('class') == 'empty-row') {
						$(evt.target).select2('enable', true);
					} else {
						$(evt.target).select2('enable', false);
					}
					_this.formSolrQuery = searchForm.buildSolrQuery();
					_this.formCriteria = searchForm.buildCriteria(true);
					
					onGroupChanged(_this);
				}});
				//Load initial form criteria. Do it just once!
				if (config.initialCriteria) {					
					for ( var i = 0; i < config.initialCriteria.length; i+=2) {
						var criterion = config.initialCriteria[i];
						var conjunctionOperator = (i==0?null:config.initialCriteria[i-1].operator);
						criterion.conjunctionOperator = conjunctionOperator;
						form.addCriteriaRow(criterion);
					}
					config.initialCriteria = null;
				}
				_this.formSolrQuery = form.buildSolrQuery();
				_this.formCriteria = form.buildCriteria(true);
				onGroupChanged(_this);
				
				if ($.isFunction(config.initializedCallback)) {
					config.initializedCallback();
				}
				
				EMF.blockUI.hideAjaxBlocker();
			}
		});
	}, formatResultCssClass: function(option) {
		if (option.children) {
			return 'select2-result-with-children';
		}
	}};
	
	if (config.defaultObjectType) {
		objectTypeConfig.defaultValue = config.defaultObjectType;
	}

	var objectTypeCombo = EMF.search.advanced.addComboBox(groupHolder, objectTypeConfig);
	
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
    	config.width = 400
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
		var criteria = _this.formCriteria.slice(); // clone form criteria
		var objectType = objectTypeCombo.select2('val');
		if (objectType && objectType != '') {
			return {'objectType':objectType, criteria:criteria};
		}
		return null;
	};
	
	/*_this.buildCriteria = function() {
		var criteria = _this.formCriteria.slice(); // clone form criteria
		var objectType = objectTypeCombo.select2('val');
		if (objectType && objectType != '') {
			var parent = config.objectTypesParents[objectType];

			if (criteria.length > 0) {
				criteria.unshift({operator:'AND'});
			}
			if (criteria.length >= 2) { // if there are at least 2 criterions put them in brackets
				criteria[0] += 1;
				criteria[criteria.length-1]['closeBrackets'] += 1;
			}
			
			if (objectType === 'all') {
				criteria.unshift({openBrackets: 0, field:'type', operator: 'EQUALS', values: ['*'], closeBrackets: 0});
			} else {
				criteria.unshift({openBrackets: 0, field: parent?'type':'rdfType', operator: 'EQUALS', values: [EMF.search.advanced.escapeQuerySpecialCharacters(objectType)], closeBrackets: 0});
			}
		}
		return criteria;
	};*/
	
	_this.buildSolrQuery = function() {
		var query = '';
		var objectType = objectTypeCombo.select2('val');
		
		if (objectType && objectType != '') {
			var parent = config.objectTypesParents[objectType];
			if (objectType === 'all') {
				query += 'type:*';
			} else {
				if (objectType.indexOf(':') != -1) {
					query += 'rdfType:' + EMF.search.advanced.escapeQuerySpecialCharacters(config.shortToFullURI[objectType] || objectType);
				} else {
					query += 'type:' + EMF.search.advanced.escapeQuerySpecialCharacters(objectType);
				}
			}

			if (_this.formSolrQuery != '') {
				var moreThanOneCriteria = _this.formSolrQuery.indexOf(' AND ') > -1 || _this.formSolrQuery.indexOf(' OR ') > -1;
				query += ' AND ' + (moreThanOneCriteria?'(':'') + _this.formSolrQuery + (moreThanOneCriteria?')':'');
			}
		}
		return query;
	};
	
	return _this;
};

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
		    		{id: 'EQUALS' , text: _emfLabels["search.advanced.equals"]},
		    		{id: 'NOT_EQUALS' , text: _emfLabels["search.advanced.notequals"]},
		    		{id: 'CONTAINS' , text: _emfLabels["search.advanced.contains"]},
		    		{id: 'DOES_NOT_CONTAIN' , text: _emfLabels["search.advanced.doesnotcontain"]},
		    		{id: 'STARTS_WITH' , text: _emfLabels["search.advanced.startswith"]},
		    		{id: 'ENDS_WITH' , text: _emfLabels["search.advanced.endswith"]}
		 ],
		 'date': [
		    	 	{id: 'BETWEEN' , text: _emfLabels["search.advanced.between"]}
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
		 ]
	};
	
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
	
	this.addCriteriaRow = function(criterion) {
		var table = this.table;
		if (!criterion) {
			criterion = {};
		}
		//if field is undefined
		var field = criterion.field || fields[0].id;
		
		//Remove empty row if present
		var firstRow = table.find("tr:first");
		if (firstRow.children("td:first").attr('class') == 'empty-row') {
			firstRow.remove();
		}		
		
		var row = $('<tr />', {'class': 'cmf-field-wrapper'});
		table.append(row);

		var conditionColumn = $('<td/>', {'class': 'condition-column'});
		row.append(conditionColumn);
		EMF.search.advanced.addComboBox(conditionColumn, {data: operators, defaultToFirstOption: true, defaultValue: criterion.conjunctionOperator});
		
		var openBracketsColumn = $('<td/>', {'class': 'open-brackets-column'});
		row.append(openBracketsColumn);
		EMF.search.advanced.addComboBox(openBracketsColumn, {data: openBrackets, defaultToFirstOption: true, defaultValue: criterion.openBrackets, onChange: function(evt) {
			if (evt.val > 0) {
				$(evt.target).closest('tr').find('.close-brackets-column div.select2-container').select2('enable', false);
			} else {
				$(evt.target).closest('tr').find('.close-brackets-column div.select2-container').select2('enable', true);
			}
		}});
		
		var fieldColumn = $('<td/>', {'class': 'field-column'});
		row.append(fieldColumn);
		var fieldCombobox = addFieldComboBox(fieldColumn, fields, field);
		fieldCombobox.on('change', {self:this}, onFieldComboChange);
		var selectedField = fieldsLookup[fieldCombobox.val()];
		var selectedFieldType = selectedField['type'];
		
		
		var comparisonColumn = $('<td/>', {'class': 'comparison-column'});
		row.append(comparisonColumn);
		var comparisonComboBox = EMF.search.advanced.addComboBox(comparisonColumn, {data: comparisonOperators[selectedFieldType], defaultToFirstOption: true, defaultValue: criterion.operator});
		comparisonComboBox.on('change', {self:this}, onComparisonComboChange);
		var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');
		
		var inputColumn = $('<td/>', {'class': 'input-column'});
		row.append(inputColumn);
		addInput(inputColumn, selectedField, comparisonOperator, criterion.values);
		
		var closeBracketsColumn = $('<td/>', {'class': 'close-brackets-column'});
		row.append(closeBracketsColumn);
		EMF.search.advanced.addComboBox(closeBracketsColumn, {data: closeBrackets, defaultToFirstOption: true, defaultValue: criterion.closeBrackets, onChange: function(evt) {
			if (evt.val > 0) {
				$(evt.target).closest('tr').find('.open-brackets-column div.select2-container').select2('enable', false);
			} else {
				$(evt.target).closest('tr').find('.open-brackets-column div.select2-container').select2('enable', true);
			}
		}});

		var deleteRowColumn = $('<td/>', {'class': 'delete-row-column'});
		row.append(deleteRowColumn);
		var deleteRowButton = $('<input />', {type: 'button', 'class': 'delete-row-button'});
		deleteRowColumn.append(deleteRowButton);
		row.append(deleteRowColumn);
		deleteRowButton.on('click', function(evt) {
			var cell = $(evt.target);
			var tTable = cell.closest('table');
			cell.closest('tr').remove();
			if (tTable.find("tr").length == 0) {
				addEmptyRow(tTable);
			}
			updateTableLayout(table);
			onChanged(_form);
		});
		
		row.append( $('<td/>', {'class': 'add-row-column'}));
		
		updateTableLayout(table);
	};
	
	function addFieldComboBox(parent, fields, defaultValue) {
		return EMF.search.advanced.addComboBox(parent,{data: fields, defaultValue: defaultValue, defaultToFirstOption: true});
	};
	
	function onFieldComboChange(evt) {
		var row = $(evt.target).closest('tr');
		
		var oldOption = fieldsLookup[evt.removed.id];
		var newOption = fieldsLookup[evt.added.id];
		
		if (oldOption['type'] != newOption['type'] || oldOption['type'] === 'select' || oldOption['type'] === 'autocomplete') {
			var comparisonColumn = row.find('.comparison-column');
			comparisonColumn.empty();
			var comparisonComboBox = EMF.search.advanced.addComboBox(comparisonColumn, {data: comparisonOperators[newOption['type']], defaultToFirstOption: true});
			comparisonComboBox.on('change', {self:this}, onComparisonComboChange);
			var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');
			
			var inputColumn = row.find('.input-column');
			inputColumn.empty();
			addInput(inputColumn, newOption, comparisonOperator);
			
		}
	};
	
	// Handles the change event for comparison columns.
	// TODO: Review!
	function onComparisonComboChange(evt) {
		var row = $(evt.target).closest('tr');
		
		var oldOption = evt.removed.id;
		var newOption = evt.added.id;
		
		if (oldOption != newOption) {
			var inputColumn = row.find('.input-column');
			
			var isSelect2 = (oldOption == 'IN' || oldOption == 'NOT_IN') && (newOption == 'IN' || newOption == 'NOT_IN');
			
			var firstInputValue = '';
			if (isSelect2) {
				firstInputValue = inputColumn.find('div.select2-container').select2('data');
			} else {
				firstInputValue = inputColumn.find('input:first').val();
			}
			
			var field = row.find('.field-column div.select2-container').select2('val');
			var fieldType = fieldsLookup[field];
			
			inputColumn.empty();
			addInput(inputColumn, fieldType, newOption);
			// Preserve old value
			if (isSelect2) {
				inputColumn.find('div.select2-container').select2('data', firstInputValue);
			} else {
				inputColumn.find('input:first').val(firstInputValue);
			}
		}
	};
	
	function addInput(parent, field, comparison, values) {
		if (field['type'] === 'string') {
			addStringInput(parent, values);
		} else if (field['type'] === 'number') {
			if (comparison === 'BETWEEN') {
				addTwoStringInputs(parent, values);
			} else {
				addStringInput(parent, values);
			}
		} else if (field['type'] === 'date') {
			addDateInput(parent, values);
		} else if (field['type'] === 'select') {
			//TODO:
			addSelectInput(parent, field);
		} else if (field['type'] === 'autocomplete') {
			//TODO:
			addAutocompleteInput(parent, field);
		}
	};
	
	function addStringInput(parent, values) {
		var initialValue = values && values[0]?values[0]:'';

		var input = $('<input />', {type: 'text', 'class': 'plain-text', value: initialValue});
		parent.append(input);
		
		return input;
	};
	
	// Adds two inputs to the provided parent element.
	// TODO: Review!
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
		wrapper.append(input)
		parent.append(wrapper);
		
		parent.append(' - ');
		
		wrapper = $('<span />', {'class': 'cmf-relative-wrapper floatRight'});
		input = $('<input />', {type: 'text', 'class': 'plain-text numeric-field', value: initialValue2});
		
		wrapper.append(input)
		parent.append(wrapper);
	};
	
	function addDateInput(parent, values) {
		parent.append(createDateInputField('floatLeft', values && values[0]?values[0]:null));
		parent.append(' - ');
		parent.append(createDateInputField('floatRight', values && values[1]?values[1]:null));
	};
	
	function addSelectInput(parent, field, values) {
		return EMF.search.advanced.addComboBox(parent, {data: field['data'], multiple: field['multiple'], searchable: field['searchable'], defaultValue: values && values[0]?values[0]:null});
	};
	
	function addAutocompleteInput(parent, field, values) {
		
		var autocompleteField = field['autocompleteField'];
		if (!autocompleteField) {
			autocompleteField = field['id'];
		}
		
		return EMF.search.advanced.addSelect2ComboBox(parent, {select2Options:	{
			 multiple: true,
			 minimumResultsForSearch: -1,
			 minimumInputLength: -1,
			 ajax: {
				 url: SF.config.contextPath + '/service/autocomplete/' + autocompleteField,
				 quietMillis: 100, // What is this?
				 data: function (term, page) {
					 return {
						 q: term,
						 limit: autocompleteLimit,
						 offset: (page-1)*autocompleteLimit
					 };
				 },
				 results: function (data, page) {
					 var more = (page * autocompleteLimit) < data.total;
					 return {results: data.results, more: more};
				 }
			 }
		}});
	};
	
	function createDateInputField(inputClass, initialValue) {
		if (initialValue && initialValue === '*') {
			initialValue = null;
		}
		
		var wrapper = $('<span />', {'class': 'cmf-relative-wrapper' + (inputClass?' ' + inputClass:'')});
		
		var datepickerInput = $('<input/>', {type: 'text', 'class': 'date-range advanced-search-date-field'}).datetimepicker({
			onSelect: function(dateText, inst) {
				onChanged(_form);
			}.bind(this),
			onClose: function(dateText, inst) {
				$(this).datetimepicker('setDate', $(this).datetimepicker('getDate'));
			}
		});
		
		if (initialValue != null) {
			datepickerInput.datetimepicker('setDate', new Date(initialValue));
		}
		
		wrapper.append(datepickerInput);
		wrapper.append($('<span/>', {'class': 'ui-icon ui-icon-calendar cmf-calendar-img cmf-date-field-icon'}));
		return wrapper;
	};
	
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
		if (firstRowFirstCell.attr('class') != 'empty-row') {
			// Preserving the width - CMF-9386
			var emptyCondition = $('<div />', {'class':'condition-column'});
			firstRowFirstCell.empty();
			firstRowFirstCell.append(emptyCondition);
		}
	};
	
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
				
				if (values.length > 0) {
					if (criteria.length > 0) { // Add conjunction operator
						criteria.push({operator:conjunctionOperator});
					}
					criteria.push({openBrackets: parseInt(openBrackets), field: solrField, operator: comparisonOperator, values: values, closeBrackets: parseInt(closeBrackets), row: i });
				}
			}
			//var combinedCriteria = combineCriteria(criteria);
		}
		return criteria;
	};

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
				if (lastOpenBracket == null) {
					return {error: 'Too much closing brackets at: ' + criterion.row};
				} else {
					//TODO: check for multiple brackets at lastOpenBracket and firstCloseBracket. If both have more than 1 subtract all additional brackets
					
					var newCriteria = [];
					
					criteria.slice(0,lastOpenBracket).forEach(function(value) {newCriteria.push(value)});
					
					//newCriteria.pushAll(criteria.slice(0,lastOpenBracket));
					
					var group = criteria.slice(lastOpenBracket, firstCloseBracket+1);

					if (group[0].openBrackets > 0 && group[group.length-1].closeBrackets > 0) {
						var additionalBrackets = Math.min(group[0].openBrackets, group[group.length-1].closeBrackets);
						
						group[0].openBrackets-=additionalBrackets;
						group[group.length-1].closeBrackets-=additionalBrackets;
					}
					newCriteria.push({openBrackets: group[0].openBrackets, group: group, closeBrackets: group[group.length-1].closeBrackets});
					criteria.slice(firstCloseBracket+1).forEach(function(value) {newCriteria.push(value)});
					
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
	
	this.buildSolrQuery = function() {
		var criteria = this.buildCriteria();
		return criteriaToSOLRQuery(combineCriteria(criteria));
	};
	
	function getCriteriaValues(row) {
		var selectedField = row.find('.field-column div.select2-container').select2('val');
		var comparisonOperator = row.find('.comparison-column div.select2-container').select2('val');
		var values = [];
		//TODO: Use switch
		if (comparisonOperator == 'BETWEEN') {//TODO: And field is a date.
			var from = row.find('.input-column input:first');
			var to = row.find('.input-column input:last');
			var fieldType = fieldsLookup[selectedField];
			
			if (fieldType['type'] === 'date') {
				var fromDate = from.datetimepicker('getDate');
				var toDate = to.datetimepicker('getDate');
				if (fromDate instanceof Date || toDate instanceof Date) {
					values.push(fromDate?fromDate.toJSON():'*');
					values.push(toDate?toDate.toJSON():'*');
				}
			} else if (fieldType['type'] === 'number') {
				values.push(from.val() ? from.val() : '*');
				values.push(to.val() ? to.val() : '*');
			}
			
		} else if (comparisonOperator == 'IN' || comparisonOperator == 'NOT_IN') {
			var selectedValues = row.find('.input-column div.select2-container').select2('val');
			if (selectedValues) {
				values = $.map(selectedValues, function(el){
					return el; //EMF.search.advanced.escapeQuerySpecialCharacters(el);
				});
			}
		} else {
			var value = row.find('.input-column input').val();
			if (value) {
				values.push(value); //EMF.search.advanced.escapeQuerySpecialCharacters(value));
			}
		}
		return values;
	};

	function clearValidatedBracketsErrors(rows) {
		for (var i=0; i<rows.length; i++) {
			$(rows[i]).find('.open-brackets-column').removeClass('delete-target');
			$(rows[i]).find('.close-brackets-column').removeClass('delete-target');
		}
	};
	
	function validateBrackets(rows) {
		var brackets = [];
		for (var i=0; i<rows.length; i++) {
			var values = getCriteriaValues($(rows[i]));
			if (values.length > 0) {
				var openBrackets = $(rows[i]).find('.open-brackets-column div.select2-container').select2('val');
				if (openBrackets>0) {
					brackets.push({row: i, brackets: openBrackets});
				}
				
				var closeBrackets = $(rows[i]).find('.close-brackets-column div.select2-container').select2('val');

				for (var j=brackets.length-1; j>=0 && closeBrackets>0; j--) {
					var remaining = brackets[j].brackets - closeBrackets;
					if (remaining <= 0) {
						brackets.splice(brackets.length-1, 1); // Most recent open brackets are all closed. Remove
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
	
	function criteriaToSOLRQuery(criteria) {
		var queries = [];
		var fq = '';
		var countQueries = 0;
		for ( var i = 0; i < criteria.length; i+=2) {
			var criterion = criteria[i];
			var conjunctionOperator = (i==0?'':criteria[i-1].operator);
			
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
						singleQuery = criterion.field + ":(" + criterion.values.join(' OR ') + ")";
						break;
					case 'NOT_IN':
						singleQuery = '-' + criterion.field + ":(" + criterion.values.join(' OR ') + ")";
						break;
					case 'EQUALS':
						singleQuery = criterion.field + ':' + criterion.values[0];
						break;
					case 'NOT_EQUALS':
						singleQuery = '-' + criterion.field + ':' + criterion.values[0];
						break;
					case 'CONTAINS':
						singleQuery = criterion.field + ':*' + criterion.values[0] + '*';
						break;
					case 'DOES_NOT_CONTAIN':
						singleQuery = '-' + criterion.field + ':*' + criterion.values[0] + '*';
						break;
					case 'STARTS_WITH':
						singleQuery = criterion.field + ':' + criterion.values[0] + '*';
						break;
					case 'ENDS_WITH':
						singleQuery = criterion.field + ':*' + criterion.values[0];
						break;
					case 'LOWER_THAN':
						singleQuery = criterion.field + ':' + '[* TO ' + criterion.values[0] + ']';
						break;
					case 'GREATER_THAN':
						singleQuery = criterion.field + ':' + '[' + criterion.values[0] + ' TO *]';
						break;
					default:
				}
			}
			
			if (conjunctionOperator === 'OR') {				
				queries.push({fq: fq, countQueries: countQueries});
				fq = singleQuery;
				countQueries = 1;
			} else {
				fq += (conjunctionOperator===''?'':' AND ') + singleQuery;
				countQueries++;
			}
		}

		if (fq != '') {
			queries.push({fq:fq, countQueries:countQueries})
		}
		var result = '';
		if (queries.length == 1) {
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

