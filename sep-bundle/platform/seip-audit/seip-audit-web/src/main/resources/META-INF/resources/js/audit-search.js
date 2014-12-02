Ext.onReady(function () {
	EMF.audit.search.grid.render('results-table');
	EMF.audit.search.grid.disable();
	$("#results-table span").bind('click',false);
});

EMF.audit = EMF.audit || {};

EMF.audit.search = {
	table: null,
	contextItems: null,
	// context items lookup map for faster data extraction
	contextItemsLookup: null,
	relationTypesLookup: null,
	contextLoaded: false,
	relationsLoaded: false,
	dateFormat: SF.config.dateExtJSFormatPattern + ' H:i',

	//the amount of results printed on a single page
	pageSize: 25,

	//the maximum number of rows displayed in the print preview
	maxRowsPrint: 10000,
	
	fieldsByDefault: ['actionid', 'username', 'eventdate', 'objecttype', 'objecttitle'],
	
	typeMapping: {
		caseinstance: 'Case',
		projectinstance: 'Project'
	},
	
	filterQuery : "",
	
	/* Collector query from filter field in the grid */
	collectorQuery : "",
		
	init: function(placeholder) {
		var _this = this;
		this.loadContextItems(function(){
			_this.contextLoaded = true;
			_this.initForm();
		});
		this.loadRelationTypes(function(){
			_this.relationsLoaded = true;
			_this.initForm();
		});
	},
	initForm: function() {
		if(this.contextLoaded && this.relationsLoaded){
			var fieldsUpdated = [{id:'actionid', text: _emfLabels["emf.audit.action"], 'type': 'autocomplete'},
			{id:'actionid_id', text: _emfLabels["emf.audit.actionid"], 'type': 'string', solrField: 'actionid'},
		    {id:'context', text: _emfLabels["emf.audit.context"], 'type': 'select', 'multiple':true,'searchable':true, 'data':this.contextItems},
			//{id:'context', text: _emfLabels["emf.audit.context"], 'type': 'autocomplete'},
			{id:'username', text: _emfLabels["emf.audit.userdisplayname"], 'type': 'autocomplete'},
			{id:'username_id', text: _emfLabels["emf.audit.username"], 'type': 'string', solrField: 'username'},
			{id:'eventdate', text: _emfLabels["emf.audit.eventdate"], 'type': 'date'},
			{id:'objecttype', text: _emfLabels["emf.audit.type"], 'type': 'autocomplete'},
			{id:'objecttype_id', text: _emfLabels["emf.audit.typeid"], 'type': 'string', solrField: 'objecttype'},
			{id:'objectsubtype', text: _emfLabels["emf.audit.subtype"], 'type': 'autocomplete'},
			{id:'objectsubtype_id', text: _emfLabels["emf.audit.subtypeid"], 'type': 'string', solrField: 'objectsubtype'},
			{id:'objectstate', text: _emfLabels["emf.audit.state"], 'type': 'autocomplete'},
			{id:'objectstate_id', text: _emfLabels["emf.audit.stateid"], 'type': 'string', solrField: 'objectstate'},
			{id:'objectpreviousstate', text: _emfLabels["emf.audit.previousstate"], 'type': 'autocomplete', autocompleteField: 'objectstate'},
			{id:'objectpreviousstate_id', text: _emfLabels["emf.audit.previousstateid"], 'type': 'string', solrField: 'objectpreviousstate'},
			{id:'objecttitle', text: _emfLabels["emf.audit.title"], 'type': 'string'},
			{id:'objectid', text: _emfLabels["emf.audit.objectid"], 'type': 'string'}];
	
			new EMF.search.advanced.form('#audit-search', {fields: fieldsUpdated, fieldsByDefault: this.fieldsByDefault, 
				onChanged:function(searchForm){
					$('#audit-query').val(searchForm.buildSolrQuery());
				}
			})
		}
		
		//$('#audit-search').append();
	},
	loadRelationTypes: function(callback){
		var _this = this;
		EMF.blockUI.showAjaxLoader();
		$.ajax({
			dataType: 'json',
			url: '/emf/service/definition/relationship-types',
			async: true,
			complete: function(response) {
				EMF.blockUI.hideAjaxBlocker();
				if (response) {
	    			var result = $.parseJSON(response.responseText);
	    			_this.relationTypesLookup = {};
	    			for ( var int = 0; int < result.length; int++) {
	    				var record = result[int];
	    				//creates an associative array for faster extraction of data
	    				//fix this!
	    				_this.relationTypesLookup[record.name] = record.title;
					}
	    			_this.relationTypes = result;
	    			callback();
				}
    		}
		});
	},
	loadContextItems: function(callback) {
		var _this = this;
		EMF.blockUI.showAjaxLoader();
		$.ajax({
			dataType: 'json',
			url: '/emf/service/search/quick',
			data: {
				max: 30000,
				q: 'instanceType:caseinstance OR instanceType:projectinstance',
				fields: 'instanceType,identifier,title,uri'
			},
			async: true,
			complete: function(response) {
				EMF.blockUI.hideAjaxBlocker();
				if (response) {
	    			var result = $.parseJSON(response.responseText);
	    			_this.contextItemsLookup = {};
	    			for ( var int = 0; int < result.data.length; int++) {
	    				var record = result.data[int];
	    				//creates an associative array for faster extraction of data
	    				//fix this!
	    				var fullUri = record.uri;
	    				if (fullUri) {
	    					record.dbId = 'emf:' + fullUri.substring(fullUri.indexOf('#') + 1);
	    					_this.contextItemsLookup[record.dbId] = record;
	    				}
					}
	    			_this.contextItems = _this.formatContextValues(result.data);
	    			callback();
				}
    		}
		});
	},

	formatContextValues: function(context) {
		var values = [];
		for (var i=0; i<context.length; i++) {
			// if case or project. Other types of entities are skipped
			if (context[i].dbId && this.typeMapping[context[i].instanceType]) {
				var title = context[i].identifier + ' ' + context[i].title + ' (' + this.typeMapping[context[i].instanceType] + ')';
				values.push({id: context[i].dbId, text: title});
			}
		}
		return values;
	},
	
	addFieldComboBox: function(parent, values, defaultValue) {
		return this.addComboBox(parent, values, defaultValue);
	},
	
	addComboBox: function(parent, values, defaultValue, multiple, searchable) {
		var selectOptions = {style:'width:100%;'};
		if (multiple) {
			selectOptions.multiple = '';
		}
		var select = $('<select />', selectOptions);
		
		if (values) {
			values.forEach(function(value){
				var option = $('<option />', value).appendTo(select);
				if (value.value === defaultValue) {
					option.attr('selected', 'selected');
				}
			});
		}
		parent.append(select);
		
		var select2options = {width: 'resolve'};
		if (!searchable) {
			select2options.minimumResultsForSearch = -1;
		}
		select.select2(select2options);
		return select;
	},
	
	createEmptyRow: function(table) {
		var row = $('<tr />');
		var emptyRowColumn = $('<td />', {'class': 'empty-row'});
		emptyRowColumn.on('click', function(){this.addCriteriaRow(this.table);}.bind(this));
		emptyRowColumn.append(_emfLabels["emf.audit.addsearchcriteria"]).appendTo(row);
		$('<td />', {'class': 'delete-row-column'}).appendTo(row);
		$('<td />', {'class': 'add-row-column'}).appendTo(row);
		return row;
	},	
	
	updateTableLayout: function() {
		var rows = this.table.find("tr");
		for (var i=0; i<rows.length; i++) {
			var addRowColumn = $(rows[i]).children(".add-row-column");
			addRowColumn.empty();
			addRowColumn.unbind();
		}
		var addRowColumn = $(rows[rows.length - 1]).children(".add-row-column");
		var addRowButton = $('<input />', {type: 'button', 'class': 'add-row-button'});
		addRowColumn.append(addRowButton);
		addRowButton.on('click', function(){this.addCriteriaRow(this.table);}.bind(this));
		
		var firstRowFirstCell = this.table.find("tr:first").children("td:first");
		if (firstRowFirstCell.attr('class') != 'empty-row') {
			firstRowFirstCell.empty();
		}
	},

	createDateInputField: function(inputClass) {
		var wrapper = $('<span />', {'class': 'cmf-relative-wrapper' + (inputClass?' ' + inputClass:'')});
		
		var datepickerInput = $('<input/>', {type: 'text', 'class': 'date-range cmf-date-field datepicker-input'}).datetimepicker({
			onSelect: function(dateText, inst) {
				this.updateFilterQueryTextarea();
			}.bind(this),
			onClose: function(dateText, inst) {
				$(this).datetimepicker('setDate', $(this).datetimepicker('getDate'));
			}
		});
		/*datepickerInput.blur(function(){
			$(this).datetimepicker('setDate', $(this).datetimepicker('getDate'));
		});*/
		
		wrapper.append(datepickerInput);
		wrapper.append($('<span/>', {'class': 'ui-icon ui-icon-calendar cmf-calendar-img cmf-date-field-icon'}));
		return wrapper;
	},
	
	onFieldComboChange: function(evt) {
		var _this = evt.data.self;
		
		var row = $(evt.target).closest('tr');
		var oldOption = $(evt.removed.element);
		var newOption = $(evt.added.element);
		
		if (oldOption.data('type') != newOption.data('type') || oldOption.data('type') === 'multiple') {
			var comparisonColumn = row.find('.comparison-column');
			comparisonColumn.empty();
			_this.addComboBox(comparisonColumn, _this.comparisonOperators[newOption.data('type')]);
			
			var inputColumn = row.find('.input-column');
			inputColumn.empty();
			_this.addInput(inputColumn, {value: newOption.attr('value'), type: newOption.data('type')});
			
		}
	},
	
	escapeQuerySpecialCharacters: function(value) {
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
	}
};

/**
 * Creates the data store for the grid.
 */
EMF.audit.search.store =  new Ext.data.Store({
	 	id:'store',
	    autoLoad: false,
	    pageSize: EMF.audit.search.pageSize,
	    remoteSort: true,
	    fields:[{name: 'eventDate', type: 'date', dateFormat: 'time'}, 
	            'userName','userDisplayName', 'action', 'actionID','objectTypeLabel', 'objectType', 'objectSubTypeLabel',  
	            'objectSubType', 'objectState', 'objectStateLabel', 'objectPreviousState','objectPreviousStateLabel', 'objectTitle', 'objectID',
	            'objectSystemID', 'objectURL', 'context', 'objectInstanceType', 
    		    {name: 'dateReceived', type: 'date', dateFormat: 'time'}
	    ],
	    sorters: [{
	    	property: "eventDate",
	    	direction: "DESC" 
	    }],
	    proxy: {
	        type: 'ajax',
	        url: '/emf/service/events',
	        simpleSortMode: true,
	        extraParams: {
                query: "*:*",
           },
	        reader: {
	            type: 'json',
	            root: 'records',
	            totalProperty: 'total'
	        }
	    }
});

/**
 * Creates the grid panel and the paging tool bar.
 */
EMF.audit.search.grid = new Ext.grid.GridPanel({
	       store: EMF.audit.search.store,
	       //forceFit: true,
	       columns: [
	   			{id:'eventDate', header: _emfLabels["emf.audit.eventdate"], 
	        	dataIndex: 'eventDate',
	        	width: 110,
	        	renderer: Ext.util.Format.dateRenderer(EMF.audit.search.dateFormat)
	        },
	        {id:'userDisplayName', header: _emfLabels["emf.audit.userdisplayname"], dataIndex: 'userDisplayName', sortable: false},
			{id:'userName', header: _emfLabels["emf.audit.username"], dataIndex: 'userName', hidden:true},	
			{id:'action', header: _emfLabels["emf.audit.action"], dataIndex: 'action', sortable: false},
			{id:'actionID', header: _emfLabels["emf.audit.actionid"], dataIndex: 'actionID', hidden:true},
			{id:'objectTypeLabel', header: _emfLabels["emf.audit.type"], dataIndex: 'objectTypeLabel', sortable:false, sortable: false},
			{id:'objectType', header: _emfLabels["emf.audit.typeid"], dataIndex: 'objectType', hidden:true},
			{id:'objectSubTypeLabel', header: _emfLabels["emf.audit.subtype"], dataIndex: 'objectSubTypeLabel', sortable:false},
			{id:'objectSubType', header: _emfLabels["emf.audit.subtypeid"], dataIndex: 'objectSubType', hidden:true},
			{id:'objectStateLabel', header: _emfLabels["emf.audit.state"], dataIndex: 'objectStateLabel',  sortable:false},
			{id:'objectState', header: _emfLabels["emf.audit.stateid"], dataIndex: 'objectState', hidden:true},
			{id:'objectPreviousStateLabel', header: _emfLabels["emf.audit.previousstate"], hidden:true, dataIndex: 'objectPreviousStateLabel'},
			{id:'objectPreviousState', header: _emfLabels["emf.audit.previousstateid"], hidden:true, dataIndex: 'objectPreviousState', hidden:true},
			{id:'objectTitle', header: _emfLabels["emf.audit.title"], 
				sortable: true, 
				dataIndex: 'objectTitle',
				renderer : function(v, meta, r) {
					var relationTypeLabel = EMF.audit.search.relationTypesLookup[v];				
					if (relationTypeLabel) {
						return "relation of type \""+relationTypeLabel+"\""
					} else {
	               		var objectId = r.data.objectSystemID;
	               		var objectType = r.data.objectInstanceType;
	               		if(objectId && objectType){
	               			var url = EMF.bookmarks.buildLink(objectType, objectId);
	               			var link = '<a href="'+url+'" target="_blank">'+v+'</a>';
	               			return link;
	               		}
					}
               		return v;
           	   }
			},
			{id:'objectID', header: _emfLabels["emf.audit.objectid"], dataIndex: 'objectID'},
			{id:'objectSystemID', header:_emfLabels["emf.audit.objectsystemid"], hidden: true, dataIndex: 'objectSystemID'},
			{id:'objectURL', header: _emfLabels["emf.audit.objecturl"], hidden: true, dataIndex: 'objectURL'},
			{id:'context', header: _emfLabels["emf.audit.context"],  sortable:false,
				dataIndex: 'context',
				renderer : function(v, meta, r) {
					var result = "";
					if (v) {
						var values = v.split(';');
						for ( var int = 0; int < values.length; int++) {
							var value = values[int];
							var fullContextValue = EMF.audit.search.contextItemsLookup[value];
							if (fullContextValue) {
								var title = fullContextValue.identifier + ' ' + fullContextValue.title + ' (' + EMF.audit.search.typeMapping[fullContextValue.instanceType] + ')';
		               			var url = EMF.bookmarks.buildLink(fullContextValue.instanceType, fullContextValue.dbId);
		               			var link = '<a href="'+url+'" target="_blank">'+title+'</a>';
								result += link + (int<values.length-1?', ':'');
							}
						}
					}
					return result;
				},
				flex: 1
			},
			{id:'dateReceived', text:_emfLabels["emf.audit.datereceived"], hidden:true,
				dataIndex: 'dateReceived',
				width: 110,
				renderer: Ext.util.Format.dateRenderer(EMF.audit.search.dateFormat)
		   }], 
	       stripeRows: true,
	       title: _emfLabels["emf.audit.foundactivities"],
	       tbar: [{
	    	   	id:'printBtn',
	            text: _emfLabels["emf.audit.print"],
	            iconCls: 'icon-print',
	            handler : function(){
	            	var config = new Object();
	            	config.title = _emfLabels["emf.audit.print.dialog.title"];
	            	config.message = _emfLabels["emf.audit.print.dialog.msg"];
	            	config.confirm= function() {
	            		EMF.audit.search.showPrintPreview(EMF.audit.search.getColumns(false));
	            	};
	            	config.cancel=function() {
	            		EMF.audit.search.showPrintPreview(EMF.audit.search.getColumns(true));
	            	};
	            	config.width = 400
	            	config.height = 200;
	            	config.okBtn=_emfLabels["emf.audit.print.dialog.yes"];
	            	config.cancelBtn=_emfLabels["emf.audit.print.dialog.no"];
	            	EMF.dialog.confirm(config);      	
	            }
	       },{
	    	   id:'exportToCSVBtn',
	    	   text: _emfLabels["emf.audit.export.csv"],
	    	   iconCls: 'icon-csv',
	    	   handler: function(){
	    		   EMF.audit.search.exportTo("csv");
	    	   }
	       },{
	    	   id:'exportToPDFBtn',
	    	   text: _emfLabels["emf.audit.export.pdf"],
	    	   iconCls: 'icon-pdf',
	    	   handler: function(){
	    		   EMF.audit.search.exportTo("pdf");
	    	   }
	       },{
	    	   id:'pageCountBtn',
	    	   text: '25',
	            menu : {
	            	id:'pageCountMenu',
	                items: [{
	                    text: '10', handler: onItemClick
	                }, {
	                    text: '25', handler: onItemClick
	                }, {
	                    text: '50', handler: onItemClick
	                }, {
	                	text: '100', handler: onItemClick
	                }]
	            }
	       }/*,new Ext.form.TextField({
	    	   id: 'extraQuery',
	    	   width:200
	       }),{
	    	   iconCls: 'icon-search',
	    	   id:'extraQueryBtn',
	    	   handler: function(){
	    		   EMF.audit.search.performSearch(true, true);
	    	   }
	       }*/], 
	   	   bbar: new Ext.PagingToolbar({
	   		   	inputItemWidth:50,
				pageSize: EMF.audit.search.pageSize,
				store: EMF.audit.search.store,
				displayInfo: true,
				displayMsg: _emfLabels["emf.audit.displaymsg"],
				emptyMsg: _emfLabels["emf.audit.norecords"],
			})
});
function onItemClick(item){
	EMF.audit.search.store.pageSize=item.text;
	EMF.audit.search.performSearch(true);
	//There should be an easier way..
	item.parentMenu.ownerButton.btnInnerEl.dom.innerHTML = item.text;
	
}

/**
 * Constructs the table for the print preview taking the values from the given JSON.
 * 
 * @param data is the json data to be used for the table
 * @param columnsToShow is an array containing the columns to show (as objects with id and name)
 * 
 */
EMF.audit.search.constructPrintPreviewTable = function(data, columnsToShow) {
	data = data.records;
	var html = "<table id='print-table'>";
	var thead = "<thead><tr>";
	$.each(columnsToShow, function(i, item) {
		thead+= "<th id='print"+ columnsToShow[i].id +"'>"+ columnsToShow[i].name +"</th>";
	});
	thead+="</tr></thead>"

	var tbody ="<tbody>"
	$.each(data, function(i, jsonItem) {
		tbody+="<tr>";
		$.each(columnsToShow, function(j, columnItem) {
				var currentObj = data[i];
				var valueToShow = "";
				//handle the special cases (date formats, nulls, contexts)
				if(columnsToShow[j].id === "eventDate") {
					valueToShow = Ext.Date.format(new Date(currentObj.eventDate), EMF.audit.search.dateFormat);
				} else if(columnsToShow[j].id === "dateReceived") {
					valueToShow = Ext.Date.format(new Date(currentObj.dateReceived), EMF.audit.search.dateFormat);
				} else if(columnsToShow[j].id === "context") {
					valueToShow = EMF.audit.search.contextToString(EMF.audit.search.escapeNull(currentObj.context));
				} else {
					valueToShow = EMF.audit.search.escapeNull(currentObj[columnsToShow[j].id]);
				}
				tbody+= "<td>" + valueToShow + "</td>";
		});
		tbody+="</tr>";
	});
	tbody+="</tbody>";
	html+= thead + tbody + "</table>";
	return html;
}

/**
 * If the string's content is 'null', converts it to an empty string
 */
EMF.audit.search.escapeNull = function(string) {
	if(string===null) {
		return "";
	}
	return string;
}

/**
 * Converts the context to a readable string for the tables seen by the user.
 *
 */
//TODO code duplicates with the context field renderer of the table grid
EMF.audit.search.contextToString = function(v) {
	var result = "";
	if (v) {
		var values = v.split(';');
		for ( var int = 0; int < values.length; int++) {
			var value = values[int];
			var fullContextValue = EMF.audit.search.contextItemsLookup[value];
			if (fullContextValue) {
				var title = '(' + EMF.audit.search.typeMapping[fullContextValue.instanceType] + ') ' + fullContextValue.identifier + ' ' + fullContextValue.title;
				result += title + (int<values.length-1?', ':'');
			}
		}
	}
	return result;
}
/**
 * Gets the ids of all visble columns of the data grid.
 */
EMF.audit.search.getVisibleColumnIds = function() {
	var buffer = [];
	 Ext.each(EMF.audit.search.grid.columns, function(col, index) {
        if (!col.hidden){
            buffer.push(col.id);
        }                     
    });
	 return buffer;
}

/**
 * Gets an array with columns ids and names. For ex. [{id:'action', name:'Action'}, {id:'context', name:'Context'}]
 * @param onlyVisible if true only visible columns are returned
 */
EMF.audit.search.getColumns = function(onlyVisible) {
	var columns = null;
	if (onlyVisible) {
		columns = EMF.audit.search.grid.query('gridcolumn:not([hidden]):not([isGroupHeader])');
	} else {
		columns = EMF.audit.search.grid.query('gridcolumn:not([isGroupHeader])');
	}
	
	var formattedColumns = [];
	for ( var int = 0; int < columns.length; int++) {
		formattedColumns.push({'id':columns[int].id, 'name':columns[int].text});
	}
	
	return formattedColumns;
}

/**
 * Constructs the table that shows the parameters for the audit search that will be printed
 * 
 * @param total is the total amount of results that will be printed
 */
EMF.audit.search.constructPrintPreviewCriteriaTable = function(total) {
	var html = "<table id='print-criteria-table'>";
	//if there are any filter queries, preview them for print
	var filterQuery = $('#audit-query').val();
	if(filterQuery!="") {
		html+= "<tr><th>"+_emfLabels["emf.audit.print.table.criteria"]+"</th></tr>" + 
		   "<tr><td>"+ filterQuery +"</td></tr>"; 
	}
	/*var extraQuery = Ext.getCmp('extraQuery').getValue()
	if(extraQuery!="") {
		html+= "<tr><th>"+_emfLabels["emf.audit.print.table.filter"]+"</th></tr>" + 
		   "<tr><td>"+ extraQuery +"</td></tr>"; 
	}*/
	//if any sort is applied, preview it for print
	var sorter = EMF.audit.search.store.sorters.getAt(0);
	if(typeof(sorter)!= "undefined") {
		var direction;
		if (sorter.direction == "ASC"){
			direction = "Ascending";
		} else {
			direction = "Descending";
		}
		html+="<tr><th>"+_emfLabels["emf.audit.print.table.sorting"]+"</th></tr>" + 
		   	  "<tr><td>"+_emfLabels["emf.audit.print.table.sorting.field"]+": "+ $("#" + sorter.property+" div span").html()+"</br>"+_emfLabels["emf.audit.table.sorting.direction"]+": "+
		   	  direction +"</td></tr>";
		
	}
	//print the total number of results in the table
	html+="<tr><th>"+_emfLabels["emf.audit.print.table.total"]+"</th></tr>" + 
 	  "<tr><td>"+ total +"</td></tr>";
	html+=  "</table>";
	return html;
}

/**
 * Builds and shows the print preview.
 * @param columnsToShow is an array containing the columns to show
 */
EMF.audit.search.showPrintPreview = function(showAll, columnsToShow) {
	EMF.blockUI.showAjaxLoader();
    var maxRows = EMF.audit.search.maxRowsPrint;
    var filterQuery = $('#audit-query').val();
    //var extraQuery = EMF.search.advanced.escapeQuerySpecialCharacters(Ext.getCmp('extraQuery').getValue());
	var url = "/emf/service/events?query=*:*&start=0&limit=" + maxRows;
	var printableArea = $("<div id='print-preview'>");
	//append the search query (if any) to the url
	if(filterQuery!="") {
		url+= "&fq=" + encodeURIComponent(filterQuery);
	}
	/*if(extraQuery!="") {
		url+= "&fq=" + encodeURIComponent("collector:*"+extraQuery+"*");
	}*/
	//append the sorting params (if any) to the url
	var sorter = EMF.audit.search.store.sorters.getAt(0);
	if(typeof(sorter)!= "undefined") {
		url+="&sort="+ sorter.property + "&dir=" + sorter.direction;
	}
	$.getJSON(url, function() {
	}).done(function(data) {
		//if the results to show are too many, show a notifying dialog
		
		if(data.total > EMF.audit.search.maxRowsPrint) {
			var config = new Object();
        	config.title = _emfLabels["emf.audit.print.toomanyresults.title"];
        	config.message =_emfLabels["emf.audit.print.toomanyresults.msg"];
        	config.width = 400
        	config.height = 200;
        	config.okBtn="Ok";
        	config.notifyOnly = true;
        	EMF.dialog.open(config);
		}
		//build the search criteria table 
		printableArea.append(EMF.audit.search.constructPrintPreviewCriteriaTable(data.total));
		$(printableArea).find("#print-criteria-table").addClass("simple-table");
		$(printableArea).find("#print-criteria-table th").addClass("simple-table-cell head-row");
		$(printableArea).find("#print-criteria-table td").addClass("simple-table-cell");	
		//build the print preview table
		$(printableArea).append(EMF.audit.search.constructPrintPreviewTable(data, showAll, columnsToShow));
		var table = $(printableArea).find("#print-table");
		table.addClass("simple-table");
		$(table).find("th").addClass("simple-table-cell head-row");
		$(table).find("td").addClass("simple-table-cell");
		EMF.blockUI.hideAjaxBlocker();
		EMF.audit.search.openPrintPopup(printableArea.prop('outerHTML'));
		
	}).fail(function() {
		console.log("There was an error sending the request to the server");
	})
};

EMF.audit.search.exportTo = function(format) {
	var config = new Object();
	config.title = _emfLabels["emf.audit.export.title"];
	config.message = _emfLabels["emf.audit.export.msg"];
	config.confirm= function() {
		EMF.audit.search.performExport(format, EMF.audit.search.getColumns(false));
	};
	config.cancel=function() {
		EMF.audit.search.performExport(format, EMF.audit.search.getColumns(true));
	};
	config.width = 400
	config.height = 200;
	config.okBtn="Yes";
	config.cancelBtn="No";
	EMF.dialog.confirm(config);
}

/**
 * Exports the audit search results in the given format - csv or pdf
 * 
 * @param format is the file format to export to - csv/pdf
 * @param columnsToExport columns to be exported
 */
EMF.audit.search.performExport = function(format, columns) {
	var origin = location.protocol +'//'+location.hostname + ':'+ location.port;
	var url = origin + '/emf/service/events/export?query=*:*';

	if (EMF.audit.search.filterQuery!="") {
		url+= "&fq=" + encodeURIComponent(EMF.audit.search.filterQuery);
	}
	
	if (EMF.audit.search.collectorQuery!="") {
			url+= "&fq=" + encodeURIComponent(EMF.audit.search.collectorQuery);
	}
	
	//append the sorting params (if any) to the url
	var sorter = EMF.audit.search.store.sorters.getAt(0);
	if(typeof(sorter)!= "undefined") {
		url+="&sort="+ sorter.property + "&dir=" + sorter.direction;
	}
	url+="&format=" + format;

	url+="&columns=" + encodeURIComponent(JSON.stringify(columns));
	
	window.location= url;
}

/**
 * Performs the search, taking the solr query (Q) from the 'audit-query' text field
 * @param applyFilter true to apply additional filter on the whole grid
 * @param resetCollectorQuery true to reset additional filter with the value form the field. If false old value will be used
 */
EMF.audit.search.performSearch =  function(applyFilter, resetCollectorQuery) {
		var filterQuery = [];
		
		if (!applyFilter) {
			EMF.audit.search.collectorQuery = "";
			EMF.audit.search.filterQuery = $('#audit-query').val();
		} else {
			if (resetCollectorQuery) {
				EMF.audit.search.collectorQuery = "";
				/*var simpleQuery = EMF.search.advanced.escapeQuerySpecialCharacters(Ext.getCmp('extraQuery').getValue());
				if (simpleQuery!=""){
						simpleQuery = "collector:*" + simpleQuery+"*";
						EMF.audit.search.collectorQuery = simpleQuery;
				} else {
					EMF.audit.search.collectorQuery = "";
				}*/
			}
		}
		
		if (EMF.audit.search.filterQuery != "") {
			filterQuery.push(EMF.audit.search.filterQuery);
		}
		
		if (EMF.audit.search.collectorQuery != "") {
			filterQuery.push(EMF.audit.search.collectorQuery);
		}
		
		EMF.audit.search.store.removeAll();
		EMF.audit.search.store.currentPage = 1;
		
		EMF.audit.search.store.proxy.extraParams.query = '*:*';
		EMF.audit.search.store.proxy.extraParams.fq = filterQuery;
		EMF.audit.search.store.load();
		if(EMF.audit.search.grid.disabled){
			EMF.audit.search.grid.enable();
			$("#results-table span").unbind('click',false);
		}
};	
EMF.audit.search.openPrintPopup = function(content)
{
	   var mywindow = window.open('', 'mydiv', 'height=800,width=1200');
       mywindow.document.write('<html><head><title>'+_emfLabels["emf.audit.print.table.title"]+'</title><style>');
       $.get("../css/audit-main.css",function(data){
    	   	mywindow.document.write(data);
    	       mywindow.document.write('</style></head><body >');
    	       mywindow.document.write(content);
    	       mywindow.document.write('</body></html>');  
    	       mywindow.document.close();
    	       mywindow.focus();
    	       mywindow.print();
    	      mywindow.close();
    	});
   
       
};
