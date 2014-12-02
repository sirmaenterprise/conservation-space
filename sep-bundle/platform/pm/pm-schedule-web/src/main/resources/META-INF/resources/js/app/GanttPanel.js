Ext.define("PMSch.GanttPanel", {
    extend : "Gnt.panel.Gantt",
    requires : [
        'Gnt.plugin.TaskContextMenu',
        'Gnt.plugin.TaskEditor',
        'Gnt.column.StartDate',
        'Gnt.column.EndDate',
        'Gnt.column.Duration',
        'Gnt.column.PercentDone',
        'Gnt.column.ResourceAssignment',
        'Sch.plugin.TreeCellEditing',
        'Sch.plugin.Pan',
        'PMSch.Toolbar',
        'PMSch.TaskContextMenu',
        'PMSch.TaskForm',
        'PMSch.widget.PMAssignmentGrid',
        'PMSch.widget.taskeditor.PMTaskEditor'
    ],
    rightLabelField : 'Responsible',
    highlightWeekends : true,
    showTodayLine : true,
    loadMask : true,
    cascadeChanges : true,
    enableProgressBarResize : true,

    initComponent : function() {

        Ext.apply(this, {
            lockedGridConfig : {
                width       : 500,
                title       : 'Tasks',
                collapsible : true,
                listeners : {
                	beforeedit : function(editor, e) {
                		if (e.field === 'sm') {
                			return e.record.data.s && e.record.data.s === 'Submitted';
                		} 
                	}
                }
            },

            lockedViewConfig  : {
                getRowClass : function(rec) { return rec.isRoot() ? 'root-row' : ''; },

                // Enable node reordering in the locked grid
                plugins : {
                    ptype           : 'treeviewdragdrop',
                    containerScroll : true
                }
            },

            // Experimental
            schedulerConfig : {
                collapsible : true,
                title : 'Schedule'
            },

            leftLabelField : {
                dataIndex : 'title',
                editor : { xtype : 'textfield' }
            },

            // Add some extra functionality
            plugins : [
                Ext.create("PMSch.TaskContextMenu", {
                	// allow context menu to be triggered in task grid too
                	triggerEvent 			: 'itemcontextmenu',
                	scheduleController		: this.scheduleController,
                	ignoreParentClicks		: true
                }),
                Ext.create('Sch.plugin.Pan'),
                Ext.create('Sch.plugin.TreeCellEditing', { clicksToEdit: 2 }),
                Ext.create('Gnt.plugin.TaskEditor', {
                	tabsConfig: {
                		items: [{
                			title 	: 'Log work',
                			id	  	: 'logWorkTab',
    			        	overflowY : 'scroll',
                			items: [
                			        {
                			        	xtype: 'container',
                			        	id: 'logWorkContainer'
                			        }
                			        ],
                			listeners : {
                				show : function(tabPanel) {
                					var currentTask = this.taskEditor.taskEditor.task
                					if (currentTask != null) {
                						var taskActualInstanceId = $.trim(currentTask.data.aiid);
                						var loadedTaskActualInstanceId = $('#logWorkContainer').attr('taskId');
                						if (!loadedTaskActualInstanceId || loadedTaskActualInstanceId !== taskActualInstanceId) {
	                						$('#logWorkContainer').attr('taskId', taskActualInstanceId);
	                						$('#logWorkContainer').empty();
	                						$('#logWorkContainer').extjsLogWorkOnTask({
	                							contextPath	: SF.config.contextPath,
	                							taskId			: taskActualInstanceId,
	                							preview		: false});
                						}
                					}
                				}.bind(this)
                			}
                		}]
                	},
                    
            		// extend General Tab for integrating custom form
                    taskFormClass 	: 'PMSch.TaskForm',
                    height			: 400,
                    
                    listeners   : {
                    	//Load Task event
                    	 loadtask : function (taskeditor, task){
                    		 var type = task.data.et;
                    		 var actualInstanceId = task.data.aiid;

                    		 var activeTab = taskeditor.tabs.activeTab;
                    		 var logWorkTab = taskeditor.tabs.child('#logWorkTab');
                    		 // Don't show Log Work tab. Not a valid task.
                    		 if (type && (type == 'taskinstance' || type == 'standalonetaskinstance') &&
                    				 actualInstanceId && actualInstanceId != '') {
                    			 if (!logWorkTab.tab.isVisible()) {
                    				 taskeditor.tabs.child('#logWorkTab').tab.show();
                    			 }
                        		 if (activeTab && activeTab.title == 'Log work') {
	                    			 // Throw show event to reload logged work for the selected task.
	                    			 // There is a bug which sometimes does not render log work tab properly depending when logged work grid is initialized.
	                    			 // i.e. construct the grid when the tab is visible
	                    			 activeTab.fireEvent('show', activeTab);
                        		 }
                    		 } else {
                    			 logWorkTab.tab.hide();
                    			 if (activeTab == logWorkTab) {
                    				 taskeditor.tabs.setActiveTab(0);
                    			 }
                    		 }

                    		 // initialize and load combo boxes
                    		 taskeditor.taskForm.openTaskForEdit(task);
                    		 // change effort label
                    		 Ext.getCmp('generalTabForm').getForm().findField("e").setFieldLabel("Estimated effort");
                    	 },
                    	 afterupdatetask : function (taskeditor, task) {
                             //update records
                    		 Ext.getCmp('generalTabForm').updateRecord();
                    		 // HACK for decreasing the end date,
                    		 // the end date will be changed only 
                    		 // into the model
                    		 taskeditor.task.setEndDate(new Date(taskeditor.task.getEndDate()-1));
                    		 taskeditor.taskForm.destroyElements();
                         },
                         close : function (){
                        	 this.taskEditor.taskForm.destroyElements();
                         }
                    }
                }),
                Ext.create('Ext.grid.plugin.BufferedRenderer')
            ],

            // Define an HTML template for the tooltip
            tooltipTpl : new Ext.XTemplate(
                '<strong class="tipHeader">{Name}</strong>',
                '<table class="taskTip">',
                    '<tr><td>Start:</td> <td align="right">{[values._record.getDisplayStartDate("y-m-d")]}</td></tr>',
                    '<tr><td>End:</td> <td align="right">{[values._record.getDisplayEndDate("y-m-d")]}</td></tr>',
                    '<tr><td>Progress:</td><td align="right">{[ Math.round(values.pd)]}%</td></tr>',
                '</table>'
            ),

            eventRenderer: function(task){
                if (task.get('Color')) {
                    var style = Ext.String.format('background-color: #{0};border-color:#{0}', task.get('Color'));

                    return {
                        // Here you can add custom per-task styles
                        style : style
                    };
                }
            },

            // Define the static columns
            columns : [
                {
            	    xtype : 'sequencecolumn',
            	    align : 'left',
            	    width : 32,
            	    tdCls : 'id'
                },
                {
            	    xtype : 'wbscolumn',
            	    align : 'left',
            	    width : 48,
            	    tdCls : 'id'
                },
                {
                    xtype : 'treecolumn',
                    header: 'Tasks',
                    sortable: true,
                    dataIndex: 'title',
                    width: 200,
                    tdCls : 'title-column',
                    field: {
                        allowBlank: false
                    },
                    renderer : function(v, meta, r) {
                        //if (!r.data.leaf) meta.tdCls = 'sch-gantt-parent-cell';
                    	var defType = r.raw.defType,
                    		combined = v;
                    	if (defType) {
                    		combined = '(' + defType + ') ' + combined;
						}
                        return combined;
                    }
                },
                {
                	header		: 'Task ID',
                	dataIndex	: 'uid',
                	id			: 'task-id',
                	width		: 60,
                	align 		: 'center',
                	renderer : function(v, meta, r){
                		var type = r.data.et;
                		var actualInstanceId = r.data.aiid || r.raw.aiid;
                		if(type && actualInstanceId){
                			var url = EMF.bookmarks.buildLink(type, actualInstanceId);
                			var link = '<a href="'+url+'" target="_blank">'+v+'</a>';
                			return link;
                		}
                		return v;
                	}
                },
                {
                	header		: 'Start date',
                    xtype 		: 'startdatecolumn',
                    id			: 'start-date',
                    width 		: 90,
                    editor		: {
                    	parentGanttPanel : this,
	                    listeners	: {
	                    	specialkey : function(field, e){
	                            if (e.getKey() == e.ENTER) {
	                            	var isValid = this.self.superclass.validateValue.call(this, field.getRawValue());
	                            	if (!isValid) {
	                            		var msgText = Ext.String.format(field.invalidText, field.getRawValue(), field.format);
	                            		this.parentGanttPanel.showErrorMessage(msgText);
	                            	}
	                            }
	                        },
	                        // Not working properly when enter is pressed
	                        validitychange : function( field, isValid, eOpts ) {
	                        	if (!isValid) {
                            		var msgText = Ext.String.format(field.invalidText, field.getRawValue(), field.format);
                            		this.parentGanttPanel.showErrorMessage(msgText);
                            	}
	                        }
	                    }
                    }
                },
                {
                    //hidden : true,
                	header		: 'End date',
                    xtype 		: 'enddatecolumn',
                    id			: 'end-date',
                    width 		: 90,
                    editor		: {
                    	parentGanttPanel : this,
	                    listeners	: {
	                    	specialkey : function(field, e){
	                            if (e.getKey() == e.ENTER) {
	                            	var isValid = this.self.superclass.validateValue.call(this, field.getRawValue());
	                            	if (!isValid) {
	                            		var msgText = Ext.String.format(field.invalidText, field.getRawValue(), field.format);
	                            		this.parentGanttPanel.showErrorMessage(msgText);
	                            	}
	                            }
	                        },
	                        // Not working properly when enter is pressed
	                        validitychange : function( field, isValid, eOpts ) {
	                        	if (!isValid) {
                            		var msgText = Ext.String.format(field.invalidText, field.getRawValue(), field.format);
                            		this.parentGanttPanel.showErrorMessage(msgText);
                            	}
	                        }
	                    }
                    }
                },
                {
                    header      : 'Assignee',
                    id			: 'assignee',
                    width       : 100,
                    xtype       : 'resourceassignmentcolumn',
                    renderer : function(value, metadata, record) {
                    	if (record.modified['resourceassignment'] && metadata.tdCls.indexOf('x-grid-dirty-cell') == -1) {
                    		metadata.tdCls += ' x-grid-dirty-cell';
                    	}
                    	return this.field.getDisplayValue(record);
                    }
                },
                {
                	header		: 'Task Status',
                	 id			: 'task-status',
                    dataIndex	: 's',
                    width 		: 70
                }, 
                {
                	header		: 'Task Type',
                	 id			: 'task-type',
                	dataIndex	: 'et',
                	width		: 100
                },
                {
                	header		: 'Subtype',
                	 id			: 'subtype',
                	dataIndex	: 'tp',
                	width		: 100
                },
                {
                	header		: 'Trigger to start',
                	 id			: 'trigger-start',
                	dataIndex	: 'sm',
                	width		: 100,
                	editor		: {
                		xtype	: 'combo',
                		store	: [
                		    [ 'Auto', 'Auto'],
                		    [ 'Manual', 'Manual']
                		]
                	}
                },
                {
                	xtype 		: 'durationcolumn'
                },
                {
                    xtype 		: 'percentdonecolumn',
                    width 		: 50
                },
                {
                    xtype 		: 'addnewcolumn'
                }
            ],

             // Define the buttons that are available for user interaction
            tbar : new PMSch.Toolbar({ 
            	gantt : this,
            	scheduleController : this.scheduleController 
            })
        });

        this.callParent(arguments);
        
        this.on('afterlayout', this.triggerLoad, this, { single : true, delay : 100 });
        
        // Adds validations for moving (indent, outdent) tasks inside the tree
        this.on('afterrender', function(gantt) {
        	var treeDragAndDropPlugin = null;
        	var plugins = gantt.lockedGrid.getView().plugins;
        	if (plugins) {
        		for (var plugin in plugins) {
        			if (plugins[plugin].ptype === 'treeviewdragdrop') {
        				treeDragAndDropPlugin = plugins[plugin];
        				break;
        			}
        		}
        	}

        	var dropZone = treeDragAndDropPlugin.dropZone;
        	dropZone.isValidDropPoint = Ext.Function.createInterceptor(dropZone.isValidDropPoint, function (node, position, dragZone, e, data) {
        		if (!node || !data.item) {
                    return false;
                }
     
                var view = this.view,
                    targetRecord = view.getRecord(node),
                    targetRecordType = targetRecord.data.et?targetRecord.data.et:'',
                    targetRecordAiid = targetRecord.data.aiid?targetRecord.data.aiid:'',
                    draggedRecords = data.records,
                    dataLength = draggedRecords.length,
                    ln = draggedRecords.length,
                    i, record;
        		
                for (var i in draggedRecords) {
                	var draggedRecord = draggedRecords[i];
                	var draggedRecordType = draggedRecord.data.et?draggedRecord.data.et:'';
                	var draggedRecordAiid = draggedRecord.data.aiid?draggedRecord.data.aiid:'';
                	
                	// Project and workflow task can not be moved
                	if (draggedRecordType === 'project'
                		|| draggedRecordType === 'taskinstance') {
                		return false;
                	} else {
                		var draggedRecordParentId = draggedRecord.parentNode.data.Id;
                		
                		// Dragged task is already created but parent is not
                		if (draggedRecordAiid !== '') {
                			var targetRecordAiid = null;
                			if (position === 'append') {
                				targetRecordAiid = targetRecord.data.aiid;
                			} else {
                				targetRecordAiid = targetRecord.parentNode.data.aiid;
                			}
                			
                			if (targetRecordAiid === '') {
                				return false;
                			}
                		} 
                		
                		// Can't move tasks inside workflow task
                		if (targetRecordType === 'taskinstance') {
                			return false;
                		}
                		
                		// Can't append tasks to workflow
                		if (targetRecordType === 'workflowinstancecontext' 
                			&& position === 'append') {
                			return false;
                		}
                		
                		// Parent of case or workflow can't be changed
                		if (draggedRecordType === 'caseinstance' 
                			|| draggedRecordType === 'workflowinstancecontext' 
                			|| draggedRecordType === 'taskinstance') {
                			
                			var newParentId = null;
                			if (position === 'append') {
                				newParentId = targetRecord.data.Id
                			} else {
                				newParentId = targetRecord.parentNode.data.Id
                			}
                			
                			if (draggedRecordParentId !== newParentId) {
                				return false;
                			}
                		}

                	}
                }

       		
                //return false;
            });
        });
        
        this.taskStore.on({
        	load	: function() { this.body.unmask(); },
        	save	: this.showLoadingMask,
        	commit	: this.showLoadingMask,
        	scope	: this
        });
        
        if (this.showTeamPanel) {
        	this.createAndAddTeamPanel();
        }
        
    },
    
    showLoadingMask : function() {
    	this.body.mask('Loading...', '.x-mask-loading');
    },
    
    showErrorMessage : function(msgText) {
    	Ext.Msg.show({
			title      : 'Error',
			msg        : msgText,
			buttons    : Ext.MessageBox.OK,
			icon       : Ext.MessageBox.ERROR
		});
    },
    
	triggerLoad : function() {
	    this.scheduleController.loadDataSet();
	},
	
	createAndAddTeamPanel : function() {
		var projectId = this.projectData.projectId;
	    this.teamGrid = Ext.create('Ext.grid.Panel', {
	    	store: Ext.data.StoreManager.lookup('resourceStore'),     
	        region: 'east',
            collapsible: true,
            border: false,
            split: true,
            width: 400,
            layout : 'fit',
            title: 'Team',
	        columns: [
	            {text: "Name", 
	            	flex: 1, 
	            	dataIndex: 'Name',
	            	renderer : function(v, meta, r){
	            		var url = SF.config.contextPath + '/project/resource-allocation-open.jsf?projectId=' + projectId + '&usernames=' + r.data.Id;
	            		var link = '<a href="'+url+'">'+v+'</a>';
	            		return link;
                	}
	            },
	            {text: "Role", flex: 1, dataIndex: 'Role'},
	            {text: "Job Title", flex: 1, dataIndex: 'JobTitle'}
	        ]
	    });
	    this.add(this.teamGrid);
	}
});
