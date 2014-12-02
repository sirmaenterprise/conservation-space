/**
 * Extended context menu:
 * - added color picker
 * - indent/outdent task
 */
Ext.define('PMSch.TaskContextMenu', {
    extend: 'Gnt.plugin.TaskContextMenu',
    
    id : 'task-context-menu',
    
    menuConfig : {
    	hideInactive	: true
    },
    
    tasksWriter			: new Ext.data.writer.Writer({
	    writeAllFields: true
	}),
    
    entryToInstanceTypes : {
        'projectinstance'			: 'project',
        'caseinstance' 				: 'case',	
        'workflowinstancecontext' 	: 'workflow',	
        'taskinstance' 				: 'task',	
        'standalonetaskinstance' 	: 'stask'
    },
    
    entryTypes : {
    	'projectinstance'			: 'projectinstance',
    	'caseinstance' 				: 'caseinstance',	
    	'workflowinstancecontext' 	: 'workflowinstancecontext',	
    	'taskinstance' 				: 'taskinstance',	
    	'standalonetaskinstance' 	: 'standalonetaskinstance'
    },
    
    entry_status : {},

    createMenuItems : function() {
        var items = this.callParent(arguments);
        
        return [{
        	itemId: 'openTask',
        	text: 'Open',
        	id	: 'open-task',
        	requiresTask: true,
        	iconCls: 'icon-open',
        	scope: this,
        	handler: function(){
        		var selectedTask = this.rec;
        		
        		// check if there are unsaved changes
        		
        		// if there aren't, then build url and redirect
        		// else show message and return
        		
        		// ex: entity/open.jsf?type=projectinstance&instanceId=56;
        		var type = selectedTask.data.et;
        		var actualInstanceId = selectedTask.data.aiid || selectedTask.raw.aiid;
        		if(type && actualInstanceId) {
        			var url = EMF.bookmarks.buildLink(type, actualInstanceId);
        			//window.location = url;
        			// open selected entry in new browser window
        			window.open( url, type + actualInstanceId + '' );
        		} else {
//                    Ext.MessageBox.show({
//                        title: 'REMOTE EXCEPTION',
//                        msg: 'Task is not yet started',
//                        icon: Ext.MessageBox.ERROR,
//                        buttons: Ext.Msg.OK
//                    });
        		}
        	}
        },
        {
        	itemId: 'editDetails',
        	text: 'Edit task',
        	id:	'edit-task',	
        	requiresTask: true,
        	iconCls: 'icon-edit-task',
        	scope: this,
        	handler: function(){
        		this.grid.taskEditor.showTask(this.rec);
        	}
        },
        {
        	itemId: 'approve',
        	text: 'Commit task(s)',
        	id : 'commit-task',
        	requiresTask: true,
        	iconCls: 'icon-commit-task',
        	scope: this,
        	handler: function(){
        		this.scheduleController.commitSelectedTasks();
        	}
        },
        {
        	itemId: 'stop',
        	requiresTask: true,
        	id : 'stop-task',
        	scope: this,
        	iconCls: 'icon-cancel-task',
        	text: 'Stop task(s)',
        	handler: this.cancelTask
        },
        {
        	itemId: 'delete',
        	handler: this.deleteTask,
        	id : 'delete-task', 
        	requiresTask: true,
        	scope: this,
        	iconCls: 'icon-delete-task',
        	text: 'Delete task(s)'
        },
        {
        	itemId: 'indentTask',
        	text: 'Indent task(s)',
        	id : 'indent-task',
        	requiresTask: true,
        	iconCls: 'icon-indent-task',
        	scope: this,
        	handler: function(){
        		this.changeTaskIndentation(true);
        	}
        },
        {
        	itemId: 'outdentTask',
        	text: 'Outdent task(s)',
        	id : 'outdent-task',
        	requiresTask: true,
        	iconCls: 'icon-outdent-task',
        	scope: this,
        	handler: function(){
        		this.changeTaskIndentation(false);
        	}
        },
        {
        	itemId: 'changeTaskColor',
        	text: 'Change task color',
        	id : 'change-task-color',
        	requiresTask: true,
        	isColorMenu: true,
        	iconCls: 'icon-color-picker',
        	menu: {
        		showSeparator: false,
        		id	: 'color-picker',
        		items: [Ext.create('Ext.ColorPalette',
        		{
        			listeners: {
        				select: function(cp,
        				color){
        					this.rec.set('Color',
        					color);this.hide();
        				},
        				scope: this
        			}
        		})]
        	}
        },
        {
        	itemId: 'editLeftLabel',
        	id:	'edit-left-label',
        	handler: this.editLeftLabel,
        	requiresTask: true,
        	scope: this,
        	iconCls: 'icon-edit-label',
        	itemId: 'editLeftLabel',
        	text: 'Edit left label'
        },
        {
        	itemId: 'editRightLabel',
        	id:'edit-right-label',
        	text: 'Edit right label',
        	handler: this.editRightLabel,
        	requiresTask: true,
        	scope: this,
        	iconCls: 'icon-edit-label'
        },
        {
        	itemId: 'addTaskMenu',
        	text: 'Add...',
        	id: 'add-task',
        	iconCls: 'icon-add-task',
        	menu: {
        		plain: true,
        		id	: 'add-menu',
        		items: [{
        			itemId: 'addTaskAbove',
        			text: 'Task above',
        			handler: function(){
        				this.addTaskAbove(this.addDefaultTaskDetails(this.rec));
        			},
        			requiresTask: true,
        			scope: this,
        			iconCls: 'icon-add-above'
        		},
        		{
        			itemId: 'addTaskBelow',
        			text: 'Task below',
        			handler: function(){
        				this.addTaskBelow(this.addDefaultTaskDetails(this.rec));
        			},
        			scope: this,
        			iconCls: 'icon-add-below'
        		},
        		{
        			itemId: 'addMilestone',
        			id: 'milestone',
        			text: 'Milestone',
        			handler: this.addMilestone,
        			requiresTask: true,
        			scope: this,
        			iconCls: 'icon-add-milestone'
        		},
        		{
        			itemId: 'addChild',
        			text: 'Sub-task',
        			id:	'subtask',
        			handler: function(){
        				var task = this.rec;
        				task.addSubtask(this.addDefaultTaskDetails(task));
        			},
        			requiresTask: true,
        			scope: this
        		},
        		{
        			itemId: 'addSuccessor',
        			text: 'Successor',
        			handler: this.addSuccessor,
        			requiresTask: true,
        			scope: this
        		},
        		{
        			itemId: 'addPredecessor',
        			text: 'Predecessor',
        			handler: this.addPredecessor,
        			requiresTask: true,
        			scope: this
        		}]
        	}
        },
        {
        	itemId: 'deleteDependencyMenu',
        	text: 'Delete dependency',
        	requiresTask: true,
        	iconCls: 'icon-delete-dependency',
        	isDependenciesMenu: true,
        	menu: {
        		plain: true,
        		listeners: {
                    beforeshow  : this.populateDependencyMenu,
                    // highlight dependencies on mouseover of the menu item
                    mouseover   : this.onDependencyMouseOver,
                    // unhighlight dependencies on mouseout of the menu item
                    mouseleave  : this.onDependencyMouseOut,
                    scope       : this
        		}
        	}
        }];
    },
    
    cancelTask: function() {
    	var clientMessage = {
    			confirm				:  {
    				key				: 'Confirm',
    				value			: 'Are you sure that you want the selected task to be stopped ?' 
    			},
    			// client positive response string
    			btnYResponse : 'yes'
        	};

        	//Confirm client message
        	Ext.Msg.confirm(clientMessage.confirm.key, clientMessage.confirm.value, function(btn) {
        		var confirm = btn;
        		if(confirm == clientMessage.btnYResponse) {
        			this.scheduleController.cancelTasks();
        		}
        	}, this);
    },
    
    deleteTask: function() {
    	var clientMessage = {
			confirm				:  {
				key				: 'Confirm',
				value			: 'Are you sure that you want the selected task to be deleted ?' 
			},
			// client positive response string
			btnYResponse : 'yes'
    	};

    	//Confirm client message
    	Ext.Msg.confirm(clientMessage.confirm.key, clientMessage.confirm.value, function(btn) {
    		var confirm = btn;
    		if(confirm == clientMessage.btnYResponse) {
    			// There is a problem when a task and its children are deleted. At the end we have only the task in removed array. 
    			// Also its childNodes property is empty therefore children nodes are lost..
    			// To avoid this we artificially 'deselect' children nodes and delete only their parents
    			 var selected = this.grid.getSelectionModel().selected;
    			 var selectedTopRange = this.scheduleController.getTopNodes(selected.getRange());
    			 this.grid.taskStore.remove(selectedTopRange);
    			//this.self.superclass.deleteTask.call(this);  
    		}
    	}, this);
    },
    
    /**
     * This function will populate the default values for the 
     * task. Usually the default values are populate into the system
     * Model, but in some cases, there are addition data calculations
     * that override them.  
     * 
     * Note: This function can be extended for specific requirements
     * and post-data calculation actions.
     */
    addDefaultTaskDetails : function(task){
    	
    	// default value
    	var defDurationValue = '1';
    	
    	// copy the current task, all core
    	// add actions are based on the current task,
    	// so copy the task
    	var mirrorTask = this.copyTask(task);
    	
    	mirrorTask.setDuration(defDurationValue);
    	
    	return mirrorTask;
    },

    /**
     * !!! overrides the default functionality !!!
     */
    configureMenuItems : function () {
        var rec = this.rec;

        // there can be no record when clicked on the empty space in the schedule
        if (!rec) return;

        this.evaluateActions(rec);
    },
    
    /**
     * !!! overrides the default functionality to allow asynchronous 
     * loading and evaluation of context menu actions !!! 
     */
    activateMenu : function(rec, e) {
        e.stopEvent();

        this.rec = rec;
        this.evaluated = false;
        this.contextMenuEvent = e;
        this.configureMenuItems();
    },
    
    contextMenuEvent : null,
    
    /**
     * Called after schedule actions for selected row are loaded and menu is configured.
     */
    showMenu : function(e) {
  		this.showAt(e.getXY());
    },
    
    /**
     * Calls service to get allowed actions for selected row.
     */
    evaluateActions : function(rec) {
    	var selectedTasks = this.grid.getView().getSelectionModel().getSelection();
    	if (selectedTasks.length > 1) {
    		this.evaluateActionsMultipleSelection(rec, selectedTasks);
    	} else {
    		this.evaluateActionsSingleSelection(rec);
    	}
    },
    
    evaluateActionsSingleSelection : function(rec) {
    	var _this = this;
    	
    	var entryId = rec.data.Id || rec.data.phid;
    	var data = {
    		projectId	: this.scheduleController.projectData.projectId,
    		entry		: {},
    		parentEntry	: null
    	};
    	
    	var parentNode = rec.parentNode;
    	if(parentNode) {
			data.parentEntry = _this.tasksWriter.getRecordData(parentNode);//parentNode.data;
			var currentRowSiblings = parentNode.childNodes;
			if (currentRowSiblings && currentRowSiblings.length > 0) {
				data.parentEntry.children = [];
				Ext.each(currentRowSiblings, function(sibling) {
					data.parentEntry.children.push(_this.tasksWriter.getRecordData(sibling)); //sibling.data);
				});
			}
    	}
    	
    	data.entry = _this.tasksWriter.getRecordData(rec);//rec.data;
    	
    	if(rec.getAllDependencies().length > 0) {
    		data.entry.hasDependencies = true;
    	}
    	
		var currentEntryChildren = rec.childNodes;
		if (currentEntryChildren && currentEntryChildren.length > 0) {
			data.entry.children = [];
			Ext.each(currentEntryChildren, function(child) {
				data.entry.children.push(_this.tasksWriter.getRecordData(child));//child.data);
			});
		}
		
    	Ext.Ajax.request({
            url      : this.scheduleController.serviceUrl.evaluateActions,
            method   : 'POST',
            jsonData : data,
            entryId  : entryId,
            success  : this.buildMenu,
            scope    : this
        });
    },
    
    /**
     * Creates and sends a request with the following format to evaluateMultiple service:
     * {projectId:'', entryId:'', entries:[{entry:{...}, parentEntry:{...}}, {...}, ...]}
     * 
     * entryId is the entry on which the right button is pressed
     * entries is an array with all selected entries
     */
    evaluateActionsMultipleSelection : function(rec, selected) {
    	var _this = this;
    	
    	var entryId = rec.data.Id || rec.data.phid;
    	var data = {
    		projectId	: this.scheduleController.projectData.projectId,
    		entryId		: entryId,
    		entries		: []
        };
    	Ext.each(selected, function(selectedEntry) {
    		var anEntry = {};
    		anEntry.entry = _this.tasksWriter.getRecordData(selectedEntry);
    		anEntry.parentEntry = selectedEntry.parentNode?_this.tasksWriter.getRecordData(selectedEntry.parentNode):selectedEntry.parentNode;
    		data.entries.push(anEntry);
    	});    	
    	Ext.Ajax.request({
            url      : this.scheduleController.serviceUrl.evaluateMultipleActions,
            method   : 'POST',
            jsonData : data, 
            entryId  : entryId,
            success  : this.buildMenu,
            scope    : this
        });
    },
    
    /**
     * Callback function executed after allowed actions for given row are loaded from the server.
     * For every possible schedule action this applies visibility according to whether given action 
     * is returned by the service as allowed.
     */
    buildMenu : function(response, options) {
    	var allActions = {
                'openTask':false,
                'editDetails':false,
                'approve':false,
                'stop':false,
                'delete':false,
                'indentTask':false,
                'outdentTask':false,
                'changeTaskColor':false,
                'editLeftLabel':false,
                'editRightLabel':false,
                'addTaskMenu':false,
           		'addTaskAbove':false,
           		'addTaskBelow':false,
           		'addMilestone':false,
           		'addChild':false,
           		'addSuccessor':false,
           		'addPredecessor':false,
                'deleteDependencyMenu':false
            };
    	
    	var evaluatedActions = Ext.decode(response.responseText)[options.entryId];
    	var len = evaluatedActions.length;
    	if(!evaluatedActions || len == 0) {
    		return;
    	}
    	
    	// apply visibility for menu items according to if given action is evaluated to be 
    	// allowed in context of current entry
    	var hasAtLeastOneAction = false;
    	for ( var actionId in allActions) {
    		var visible = false;
			if (evaluatedActions[actionId]) {
				visible = true;
				hasAtLeastOneAction = true;
			}
			var selector = '[itemId=' + actionId + ']';
			var component = this.query(selector);
			if(component && component.length > 0) {
				component[0].setVisible(visible);
			}
		}
    	
    	if (hasAtLeastOneAction) {
    		this.showMenu(this.contextMenuEvent);
    	}
    },
    
    configColorChangeMenu : function(rec) {
        var colorMenu = this.query('[isColorMenu]')[0].menu.items.first(),
        val = colorMenu.getValue(),
        recVal = rec.get('TaskColor'),
        selectedEl = null;

	    if (colorMenu.el) {
	        if (val && recVal && recVal !== val){
	
	            colorMenu.el.down('a.color-' + val).removeCls(colorMenu.selectedCls);
	
	            if (colorMenu.el.down('a.color-' + recVal)){
	                colorMenu.select(recVal.toUpperCase());
	            }
	        } else if (val && !recVal){
	            colorMenu.el.down('a.color-' + val).removeCls(colorMenu.selectedCls);
	        }
	    }
    },
    
    changeTaskIndentation : function(indent) {
    	// TODO: here we should apply some constraints for the indentation
    	var selectedTasks = this.grid.getView().getSelectionModel().getSelection();
    	if(indent) {
	        Ext.each(selectedTasks, function(task) {
	            task.indent();
	        });
    	} else {
	        Ext.each(selectedTasks, function(task) {
	            task.outdent();
	        });
    	}
    }

});
