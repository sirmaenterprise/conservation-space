$.fn.extjsLogWorkOnTask = function(opts) {
	var HOURS_IN_A_DAY = 8;
	var MINUTES_IN_AN_HOUR = 60;

	var DAY_UNIT = 'd';
	var HOUR_UNIT = 'h';
	var MINUTE_UNIT = 'm';

	var TIME_UNITS = [];
	TIME_UNITS[DAY_UNIT] = 1;
	TIME_UNITS[HOUR_UNIT] = 2;
	TIME_UNITS[MINUTE_UNIT] = 3;

	var DATE_FORMAT = SF.config.dateExtJSFormatPattern;
	var ISO8601DateFormat = 'c';

	var plugin = {};

	var defaults = {
	    // ----------------------------------------
	    //      required arguments
	    // ----------------------------------------
	    // application context path like /emf
		contextPath	  : null,
		// current task id
		taskId	  : null,
		userId	  : null,
        // ----------------------------------------
        //      optional arguments
        // ----------------------------------------
		width		  : null,
		height		  : null,
		preview		  : true,
		debug         : false,
		service       : {
			loadData	 : '/service/task/logWork',
			loadUsers    : '/users'
		},
		labels        : {
			logWork				: _emfLabels['cmf.task.log_work.log_work'],
			log					: _emfLabels['cmf.task.log_work.log'],
			cancel				: _emfLabels['cmf.task.log_work.cancel'],
			timeSpent			: _emfLabels['cmf.task.log_work.time_spent'],
			startDate			: _emfLabels['cmf.task.log_work.start_date'],
			workDescription		: _emfLabels['cmf.task.log_work.work_description'],
			userName			: _emfLabels['cmf.task.log_work.user_name'],
			wrongValue			: _emfLabels['cmf.task.log_work.wrong_value'],
			noRecordsFound		: _emfLabels['cmf.task.log_work.no_records']
		},
		fields		: {
			timeSpent		: 'emf:timeSpent',
			startDate		: 'emf:startDate',
			workDescription	: 'emf:workDescription'
		}
	};

	plugin.options = defaults;

	/**
	 * Define a custom text field with the ability to switch between minutes to input time format and vice versa
	 */
	Ext.define('EMF.form.field.TimeSpent', {
	    extend: 'Ext.form.field.Text',
	    alias: 'widget.timespentfield',

	    rawToValue: function(rawValue) {
	    	return plugin.convertTimeStringToMinutes(rawValue);
	    },

	    valueToRaw: function(value) {
	    	return plugin.convertMinutesToTimeString(value);
	    }
	});

	Ext.define('UserList', {
	    extend: 'Ext.data.Model',
	    fields: [
	        {name:'label', type:'string'},
	        {name:'id', type:'string'}
	    ]
	});

	var userServiceURL = EMF.servicePath + plugin.options.service.loadUsers;

	var userListStore = Ext.create('Ext.data.Store', {
	    model: 'UserList',
	    data: [{ id: EMF.currentUser.id, label: EMF.currentUser.displayName }],
	    proxy: {
	        type: 'ajax',
	        url: userServiceURL,
	        reader: {
	            root: 'items',
	            type: 'json'
	        }
	    }
	});

	/**
	 * A form to create new log work record.
	 */
	Ext.define('EMF.form.LogWork', {
	    extend: 'Ext.form.Panel',
	    alias: 'widget.logworkform',

	    initComponent: function() {
	        this.addEvents('create');

	        userListStore.clearListeners();
	        var selectedValue = {id:EMF.currentUser.id, label:EMF.currentUser.displayName};

	        var userCombobox = new Ext.form.field.ComboBox({
                xtype: 'combobox',
                labelWidth : 110,
                fieldLabel: _emfLabels['cmf.task.log_work.user_name'],
                name: 'userId',
                store: userListStore,
                displayField: 'label',
                valueField: 'id',
                value: EMF.currentUser.id,
                width: 285,
                allowBlank: true,
                typeAhead: true,
                queryMode: 'remote',
                queryParam: 'term',
                minChars: 3,
                forceSelection: true,
                listeners: {
                	change: function(combo, newValue, oldValue, eOpts) {
                		var currentRecord = combo.findRecordByValue(combo.getValue());
                		if (currentRecord) {
                			selectedValue = currentRecord.data;
                		}
                	}
                }
            });
	        Ext.apply(this, {
	            activeRecord: null,
	            frame: false,
	            border: 0,
	            bodyPadding: 5,
	            /*fieldDefaults: {
	                anchor: '100%'
	            },*/
	            items: [{
	            	xtype: 'fieldcontainer',
	            	fieldLabel: plugin.options.labels.timeSpent,
	            	labelSeparator : ': <span style="color:red">*</span>',
	            	labelWidth : 110,
                    combineErrors: false,
                    layout: {
                        type: 'hbox',
                        defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                    },
                    defaults: {
                        hideLabel: true
                    },
                    items : [
                        {xtype : 'timespentfield',
	    	                fieldLabel: plugin.options.labels.timeSpent,
	    	                labelSeparator : ': <span style="color:red">*</span>',
	    	                name: plugin.options.fields.timeSpent,
	    	                validateOnChange : false,
	    	                allowBlank: false,
	    	                width: 170,
	    	                validator : function(value) {
	    	                	var convertedTime = plugin.convertTimeStringToMinutes(value);
	    	                	return convertedTime == null?plugin.options.labels.wrongValue:true;
	    	                }
                    	}, {
                            xtype: 'displayfield',
                            name : plugin.options.fields.timeSpent + '_hint',
                            value: 'e.g. 2d 4h 45m',
                            flex: 1
                        }
                    ]
	            },{
	            	xtype: 'fieldcontainer',
	            	fieldLabel: plugin.options.labels.startDate,
	            	labelSeparator : ': <span style="color:red">*</span>',
	            	labelWidth : 110,
                    combineErrors: false,
                    layout: {
                        type: 'hbox',
                        defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                    },
                    defaults: {
                        hideLabel: true
                    },
                    items : [
                        {xtype : 'datefield',
	    	                fieldLabel: plugin.options.labels.datefield,
	    	                labelSeparator : ': <span style="color:red">*</span>',
	    	                name: plugin.options.fields.startDate,
	    	                validateOnChange : false,
	    	                allowBlank: false,
	    	                width: 170,
	    	                value: new Date(),
	    	                format : DATE_FORMAT
                    	}, {
                            xtype: 'displayfield',
                            name : plugin.options.fields.startDate + '_hint',
                            value: 'e.g. ' + Ext.Date.format(new Date(), DATE_FORMAT),
                            flex: 1
                        }
                    ]
	            },
	            userCombobox,
	            {
	            	xtype     : 'textareafield',
	                fieldLabel: plugin.options.labels.workDescription,
	                name: plugin.options.fields.workDescription,
	                labelWidth : 110,
	                anchor: '100%',
	                allowBlank: true,
	                grow      : false
	            }]
	        });

	        userListStore.on({ load:  function( store, operation, eOpts ) {
		        	if (selectedValue !== undefined) {
		        		store.loadData([selectedValue],true);
		        		// on initialization
		        		if (userCombobox.getValue() !== selectedValue.id && (!userCombobox.getValue() || userCombobox.findRecordByValue(userCombobox.getValue()))) {
		        			userCombobox.setValue(selectedValue.id);
		        		}

		        	}
	        	}
	        });
	        userListStore.load();
	        this.callParent();
	    },

	    setActiveRecord: function(record){
	        this.activeRecord = record;
	        if (record) {
	        	Ext.suspendLayouts();
	            this.getForm().loadRecord(record);
	            Ext.resumeLayouts(true);
	        } else {
	            this.getForm().reset();
	        }
	    },

	    onSave: function(){
	    	var active = this.activeRecord,
	        form = this.getForm();
	        if (form.isValid()) {
	        	if (active) {
		            form.updateRecord(active);
		            this.onReset();
	        	} else {
	        		var submitValues = form.getFieldValues();
	        		var workLogUserId = submitValues['userId'];
	        		var currentUserId = EMF.currentUser.id;
	        		plugin.loggedWorkStore.proxy.extraParams.userId = null;
	        		if(workLogUserId != undefined && currentUserId != workLogUserId){
	        			var descriptionTemplate = _emfLabels['cmf.task.log_work.work_loggedby'] + " user : description";
	        			descriptionTemplate = descriptionTemplate.replace('user', EMF.currentUser.displayName);
	        			descriptionTemplate = descriptionTemplate.replace('description', submitValues[plugin.options.fields.workDescription]);
	        			submitValues[plugin.options.fields.workDescription] = descriptionTemplate;
	        			plugin.loggedWorkStore.proxy.extraParams.userId = workLogUserId;
	        		}
	        		delete submitValues[plugin.options.fields.timeSpent + '_hint'];
	        		this.fireEvent('create', this, submitValues);
	            	form.reset();
	        	}
	            return true;
	        } else {
	        	return false;
	        }
	    },

	    onReset: function(){
	        this.setActiveRecord(null);
	        this.getForm().reset();
	    }
	});

	/**
	 * Logged work data model
	 */
	Ext.define('EMF.model.LogWork', {
		extend: 'Ext.data.Model',
		fields: [ {name: 'id',  type: 'string'},
		          {name: 'userName',  type: 'string'},
		          {name: 'userDisplayName',  type: 'string'},
		          {name: plugin.options.fields.timeSpent,  type: 'int'},
		          {name: plugin.options.fields.startDate,  type: 'date', dateFormat: ISO8601DateFormat},
		          {name: plugin.options.fields.workDescription,  type: 'string'},
		          {name: 'editDetails',  type: 'boolean'},
		          {name: 'delete',  type: 'boolean'},
		          {name: 'userId',  type: 'string'}
		]
	});

	/**
	 * Utility to convert time string of type 2d 4h 25m to minutes
	 */
	plugin.convertTimeStringToMinutes = function (input) {
		var currentUnitIndex = null;
		var result = 0;
		if (input && input.length > 0) {
			var inputArray = input.split(' ');
			for (var i=0; i<inputArray.length; i++) {
				var token = $.trim(inputArray[i]);
				if (token.length > 0) {
					var value = parseFloat(token.substring(0, token.length-1));
					var unit = token.substring(token.length-1);
					// Check if user is inserted only a single time unit and if it doesn't contain suffix. If so treat it as minutes
					if (inputArray.length == 1) {
						var unitNumber = parseFloat(unit);
						// Suffix is number i.e. there is no unit suffix
						if (!isNaN(unitNumber)) {
							value = parseFloat(token);
							unit = MINUTE_UNIT;
						}
					}

					var unitIndex = TIME_UNITS[unit];
					if (isNaN(value) || value % 1 != 0 || value <= 0) {
						//ERROR. Must be a whole positive number
						return null;
					}
					// Is it a valid unit
					if (unitIndex) {
						if (currentUnitIndex == null) {
							currentUnitIndex = unitIndex;
						} else if (unitIndex <= currentUnitIndex) {
							// ERROR. Units hierarchy is not valid or we have duplicate units. Ex.: '2h 3d' or '10m 12m'
							return null;
						}
						if (DAY_UNIT === unit) {
							result += value*HOURS_IN_A_DAY*MINUTES_IN_AN_HOUR;
						} else if (HOUR_UNIT === unit) {
							result += value*MINUTES_IN_AN_HOUR;
						} else if (MINUTE_UNIT === unit) {
							result += value;
						}
					} else {
						//ERROR. Invalid unit. Not d,h or m
						return null;
					}
				}
			}
		} else {
			return null;
		}
		return result;
	};

	/**
	 * Utility to convert minutes to time string of type 2d 4h 25m
	 */
	plugin.convertMinutesToTimeString = function(minutes) {
		var result = "";
		if (!isNaN(minutes) && minutes > 0) {
			var hours = Math.floor(minutes/60);
			var remainingMinutes = minutes%60;

			var days = Math.floor(hours/HOURS_IN_A_DAY);
			var remainingHours = hours%HOURS_IN_A_DAY;

			result = (days>0?" " + days + "d":"") +
			(remainingHours>0?" " + remainingHours + "h":"") +
			(remainingMinutes>0?" " + remainingMinutes + "m":"")
		}

		return $.trim(result);
	};


	/**
	 * Creates the logged work store
	 */
	plugin.getLoggedWorkStore = function() {
		// Create store if not already created.
		if (!plugin.loggedWorkStore) {
			var opts = plugin.options;
			var url = opts.contextPath + opts.service.loadData;

			plugin.loggedWorkStore = Ext.create('Ext.data.Store', {
				model         : 'EMF.model.LogWork',
				remoteFilter  : false,
				remoteSort    : false,
				filters       : [],
				autoSync	  : true,
				autoLoad      : false,
			    proxy: {
			        type: 'rest',
			        url : url, // Your service url
			        reader: {
			            type: 'json',
			            root: 'data' // Important. Root name and model name is same
			        },
			        extraParams : {
			        	taskId : opts.taskId,
			        	userId : opts.userId
			        }
			    },

			    onCreateRecords: function(records, operation, success) {
			    	if (!success) {
			    		this.rejectChanges();
			    	}
			    },

			    onUpdateRecords: function(records, operation, success) {
			    	if (!success) {
			    		this.rejectChanges();
			    	}
			    },

			    onDestroyRecords: function(records, operation, success) {
			    	if (!success) {
			    		this.rejectChanges();
			    	} else {
			    		// Clear removed records because it tries to remove them again on the next sync even if they are successfully removed
			    		this.removed = [];
			    	}
			    }
			});
		}
		return plugin.loggedWorkStore;
	};

	/**
	 * Columns for the property grid.
	 */
	plugin.getColumns = function() {
		var lbl = plugin.options.labels;

		var columns = [
		    {
		        text		: lbl.userName,
		        dataIndex	: 'userDisplayName',
		        width		: 100,
		        renderer: function(value, metaData, record, rowIdx, colIdx, store, view) {
                    return value;
                },
                summaryRenderer: function(value, summaryData, dataIndex) {
                	return _emfLabels['cmf.task.log_work.total_time_spent'];
                }
		    },
		    {
                text		: lbl.timeSpent,
                dataIndex	: plugin.options.fields.timeSpent,
                width		: 80,
                summaryType: 'sum',
                field: {
                    xtype: 'numberfield'
                },
                renderer: function(value, metaData, record, rowIdx, colIdx, store, view) {
                    return plugin.convertMinutesToTimeString(value);
                },
                summaryRenderer: function(value, summaryData, dataIndex) {
                	return plugin.convertMinutesToTimeString(value);
                }
            },
		    {
                text		: lbl.startDate,
                dataIndex	: plugin.options.fields.startDate,
                width		: 80,
                field: {
                    xtype: 'datefield',
                    format : DATE_FORMAT
                },
                renderer: Ext.util.Format.dateRenderer(DATE_FORMAT)
            },
		    {
                text		: lbl.workDescription,
                dataIndex	: plugin.options.fields.workDescription,
                flex		: 2
            }
		];

		if (!plugin.options.preview) {
			columns.push(
					{
						xtype       : 'actioncolumn',
						width       : 21,
						sortable    : false,
						dataIndex	: 'editDetails',
						menuDisabled: true,
						items       : [{
							getClass: function(v, meta, rec) {
								if (v) {
									return 'edit-work';
								} else {
									return 'hide';
								}
							},
							handler: function(grid, rowIndex, colIndex) {
								var rec = grid.getStore().getAt(rowIndex);
								if (rec.data['editDetails']) {
									plugin.showCreateRecordEditor(rec);
								}
							}
						}]
					});

			columns.push(
					{
						xtype       : 'actioncolumn',
						width       : 21,
						sortable    : false,
						dataIndex	: 'delete',
						menuDisabled: true,
						items       : [{
							getClass: function(v, meta, rec) {
								if (v) {
									return 'delete-work';
								} else {
									return 'hide';
								}
							},
							handler: function(grid, rowIndex, colIndex) {
								var rec = grid.getStore().getAt(rowIndex);
								if (rec.data['delete']) {
									var message = _emfLabels['cmf.task.log_work.confirm_delete_log'];
					                EMF.dialog.confirm({
					                    message     : message,
					                    width       : 'auto',
					                    customStyle	: 'warn-message',
					                    okBtn		: _emfLabels['cmf.btn.ok'],
					                    cancelBtn	: _emfLabels['cmf.btn.cancel'],
					                    confirm     : function() {
					                    	grid.getStore().remove(rec);
					                    }
					                });
								}
							}
						}]
					});
		}

        return columns;
	};

	/**
	 * Toolbar to be added on the top of the grid.
	 */
	plugin.getToolbarButtons = function(scope) {
		if (!plugin.options.preview) {
			var btns = [
    			{
    	        	xtype	: 'button',
    	            text	: this.options.labels.logWork,
    	            scope	: scope,
    	            cls		: 'btn btn-sm btn-default',
    	            overCls	: 'extjs-component-btn-over',
    	            handler	: function() { plugin.showCreateRecordEditor(); }
    	        }
	        ];
		} else {
			var btns = null;
		}

		return btns;
	};

	/**
	 * Opens a pop-up dialog to create/edit and save logged work record.
	 */
	plugin.showCreateRecordEditor = function(activeRecord) {
		var logWorkForm = Ext.create('EMF.form.LogWork', {
            manageHeight: false,
            listeners: {
                create: function(form, data){
                	//data[plugin.options.fields.timeSpent] = plugin.convertTimeStringToMinutes(data[plugin.options.fields.timeSpent]);
                	plugin.getLoggedWorkStore().insert(0, data);
                }
            }
		});

		if (activeRecord) {
			logWorkForm.setActiveRecord(activeRecord);
		}

		var formPlaceHolder = document.createElement('div');

		$(formPlaceHolder).dialog({
			width: 'auto',
			resizable: false,
			title : _emfLabels['cmf.task.log_work.log_work'],
			dialogClass: 'notifyme no-close',
			modal: true,
			position: ['center', 'center'],
			close: function(event, ui)
	        {
	            $(this).dialog('destroy').remove();
	        },
	        buttons: [
	        	{
	        	    text     : _emfLabels['cmf.task.log_work.log'],
	        	    priority : 'primary',
	        	    click    : function() {
	        	    	if (logWorkForm.onSave()) {
	        	    		$(this).dialog('destroy').remove();
	        	    	}
	        	    }
	        	},
	        	{
	        	    text     : _emfLabels['cmf.btn.close'],
	        	    priority : 'secondary',
	        	    click    : function() {
                        $(this).dialog('destroy').remove();
                    }
	        	}
	        ]
		});


		if(activeRecord){
			var userListCombobox = logWorkForm.getForm().findField("userId");
			userListCombobox.hide();
		}

	    var main = Ext.create('Ext.container.Container', {
	        width: 500,
	        border: 0,
	        renderTo: formPlaceHolder,
	        layout: {
	            type: 'vbox',
	            align: 'stretch'
	        },
	        items: [logWorkForm]
	    });
	};

	return this.each(function() {
		// make sure only one instance of the plugin is initialized for element
	    var initialized = $.data(this, 'extjsLogWorkOnTask' );
	    if (!initialized) {
	    	$.data(this, 'extjsLogWorkOnTask', true);
	    } else {
	    	return;
	    }

		var element = plugin.target = this;

		plugin.options = $.extend(true, {}, defaults, opts);

		var lbl = plugin.options.labels;

		// create the panel grid
	    plugin.panelGrid = new Ext.grid.GridPanel({
	    	height		: plugin.options.height,
	    	columnLines	: true,
	    	loadMask    : true,
	    	tbar		: plugin.getToolbarButtons(this),
	    	store		: plugin.getLoggedWorkStore(),
	    	columns		: plugin.getColumns(),
	    	cls			: 'extjs-component log-work-wgt-wrapper',
	    	features: [{
	            ftype: 'summary'
	        }],
	    	viewConfig	: {
	            stripeRows: true,
	            emptyText	: lbl.noRecordsFound,
	            deferEmptyText: false
	        },
	        listeners: {
	            // we load the store after the view is ready and don't allow it to auto load
	            viewready: function( _this, eOpts ) {
	                _this.getStore().load();
	            }
	        }
	    });

	    plugin.panelGrid.store.on('datachanged', function(store, options) {
	    	var view = this.getView();
	    	var summaryFeature = null;
	    	if (view.features) {
	    		for ( var int = 0; int < view.features.length; int++) {
	    			if (view.features[int].ftype == 'summary') {
	    				summaryFeature = view.features[int];
	    				break;
	    			}
				}
	    	}
	    	if (summaryFeature) {
		    	if (store.getCount() == 0) {
		    		summaryFeature.toggleSummaryRow(false);
		    	} else {
		    		summaryFeature.toggleSummaryRow(true);
		    	}
		    	view.refresh();
	    	}
 	    }, plugin.panelGrid);

		plugin.widgetContainer = Ext.create('Ext.container.Container', {
			renderTo: element,
			width	: plugin.options.width,
			height	: plugin.options.height,
			items	: [ plugin.panelGrid],
		    layout	: {
		        type: 'anchor'
		    }
		});

	});
};