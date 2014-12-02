Ext.define("PMSch.controller.ScheduleController", {

	gantt				: null,
	taskStore			: null,
	resourceStore		: null,
	assignmentStore		: null,
	dependencyStore		: null,
	projectData			: null,
	serviceUrl			: {
		deleteTask		: '/emf/service/schedule/task/delete',
		commitTask		: '/emf/service/schedule/task/commit',
		updateTask		: '/emf/service/schedule/task/update',
		view			: '/emf/service/schedule/task/view',
		cancel			: '/emf/service/schedule/task/cancel',
		evaluateActions : '/emf/service/schedule/actions/evaluate',
		evaluateMultipleActions : '/emf/service/schedule/actions/evaluateMultiple'
	},
	
	// Used to synchronize save operation. After save, the task store should be reloaded only once when both these variables are true.
	deleteCompleted		: false,
	updateCompleted		: false,
	
	constructor	: function(options) {
		Ext.apply(this,options || {});
	},
	
	/**
	 * 
	 */
    reloadData : function() {
        this.dependencyStore.load();
        this.assignmentStore.load();
        this.loadDataSet();
    },
    
    /**
     * 
     */
    extract : function(records) {
        var result = [];
        Ext.each(records, function(record) {
            result.push(record.data);
        });
        return result;
    },
	
    populateDataStores : function (response, options) {
        var scheduleData = Ext.decode(response.responseText);
        var data = scheduleData.data;
        this.scheduleConfig = scheduleData.scheduleConfig;
        
        if (data.dependencies) {
            this.dependencyStore.loadData(data.dependencies);
        }

        if (data.assignments) {
            this.assignmentStore.loadData(data.assignments);
        }

        if (data.resources) {
            this.resourceStore.loadData(data.resources);
        }
        
        // Hack to avoid "load"-ing the store after deleting tasks from it because removed records are not properly updated.
        this.taskStore.removed = [];
        // Now all is in place, continue with tasks
        this.taskStore.setRootNode(data);
    },	
	
    loadDataSet : function () {
    	if (this.gantt) {
    		this.gantt.showLoadingMask();
    	}
    	var _this = this;
    	Ext.Ajax.request({
            url      : this.serviceUrl.view + '?projectId=' + this.projectData.projectId,
            method   : 'GET',
            success  : this.populateDataStores,
            callback : function () {
                // fire event that the schedule data is loaded
            	this.taskStore.fireEvent('load');
            	// check restriction for schedule read-only
            	this.checkProjectRestrictions.checkEditMode(this);
            },
            scope    : this
        });
    },
    
    cancelTasks : function() {
    	var selectedRecords = this.getSelectedTasks();
    	
    	if(selectedRecords && selectedRecords.length > 0) {
    		// extract all tasks based of schedule selection
        	var ids = [];
        	for(rec in selectedRecords) {
        		ids.push(selectedRecords[rec].data.Id);
        	}
        	
        	var data = {recordId : ids};
    		Ext.Ajax.request({
                url      : this.serviceUrl.cancel,
                method   : 'POST',
                jsonData : data,
                callback : function () {
                	this.taskStore.fireEvent('cancel');
                	
                	// Reload only tasks store
                	this.loadDataSet();
                },
                scope    : this
    		}); 
    		
    		//this.taskStore.load();
    	}
    },
	
    deleteData : function() {
    	var records = this.taskStore.getRemovedRecords();
    	// Sort deleted records hierarchically. If a parent is deleted all its children must  be deleted too.
    	records = this.sortSelectionHierarchically(records, true, false);

    	var assignmentsIds = [];
    	var remAssignments = this.assignmentStore.getRemovedRecords();
		Ext.each (remAssignments, function (record) {
			assignmentsIds.push(record.getId());
	    });

		var dependenciesIds = [];
		var remDependencies = this.dependencyStore.getRemovedRecords();
		Ext.each (remDependencies, function (record) {
			dependenciesIds.push(record.getId());
	    });

    	var hasDeletedRecords = records.length != 0 || assignmentsIds.length != 0 || dependenciesIds.length != 0;
    	
    	if(hasDeletedRecords) {
    		// extract all tasks based of schedule selection
        	var extractedTasks = this.extract(records);
        	var ids = [];
        	for(step in extractedTasks) {
        		var task2deleteId = extractedTasks[step].Id;
        		ids.push(task2deleteId);
        		// Deleting a task didn't remove its assignments from assignment store so we pick its assignments for removal manually.
        		this.assignmentStore.each(function(record) {
        			if (record.data.TaskId === task2deleteId) {
        				if (!Ext.Array.contains(assignmentsIds, record.getId())) {
        					assignmentsIds.push(record.getId());
        				} 
        			}
        		});
        	}
        	
        	var data = {
        		tasks : ids,
        		assignments : assignmentsIds,
        		dependencies : dependenciesIds
        	};

        	Ext.Ajax.request({
                url      : this.serviceUrl.deleteTask,
                method   : 'POST',
                jsonData : data,
                callback : function () {
                	this.taskStore.fireEvent('delete');
                	//this.reloadData();
                	this.onDeleteComplete();
                },
                scope    : this
    		}); 
    		
    		// The normal reloadData method 
    		// does not refresh delete records that
    		// received from the task store.
    		// This load override all modified records and update fails. This call is moved after update.
    		//this.taskStore.load();
    	} else {
    		this.onDeleteComplete();
    	}
    	
    	return hasDeletedRecords;
    },	
    
    saveDataSet : function () {
    	var hasDeletedRecords = this.deleteData();
        var resources 	 = this.resourceStore.getModifiedRecords();
        var assignments  = this.assignmentStore.getModifiedRecords();
        var dependencies = this.dependencyStore.getModifiedRecords();
        var tasks		 = this.taskStore.getModifiedRecords();
        
        var hasUpdatedRecords = tasks.length != 0 || dependencies.length != 0 || assignments.length != 0 || resources.length != 0;
        
        if (hasUpdatedRecords) {
	        var data = {
	        	projectId	 : this.projectData.projectId,
	            resources    : this.extract(resources),
	            assignments  : this.extract(assignments),
	            dependencies : this.extract(dependencies),
	            tasks        : this.extract(tasks)
	        };
	
	        Ext.Ajax.request({
	            url      : this.serviceUrl.updateTask,
	            method   : 'POST',
	            jsonData : data,
	            // For now we reload all the data again
	            // success  : this.populateDataStores,
	            callback : function () {
	                this.taskStore.fireEvent('save');
	                // call load to update the schedule
	                //this.reloadData();
	                this.onUpdateComplete();
	            },
	            scope    : this
	        });
        } else if (hasDeletedRecords) {
        	this.onUpdateComplete();
        } else {
        	// Reset flag because we don't have nothing to delete or update therefore there is no need to reload data.
        	this.deleteCompleted = false;
        }
    },
    
    /**
     * Called when delete completes. Either when deleteData exits or in delete request's callback depending if we have something to delete.
     * If update has completed this function calls reloadData otherwise it just sets deleteCompleted to true.
     * Because delete and update are asynchronous this functions ensures that both operation are completed before calling reloadData.
     */
    onDeleteComplete : function() {
    	if (this.updateCompleted) {
    		this.updateCompleted = false;
    		this.reloadData();
    	} else {
    		this.deleteCompleted = true;
    	}
    },
    
    /**
     * Called when update completes. Either when saveDataSet exits or in update request's callback depending if we have something to update.
     * If delete has completed this function calls reloadData otherwise it just sets updateCompleted to true.
     * Because delete and update are asynchronous this functions ensures that both operation are completed before calling reloadData.
     */
    onUpdateComplete : function() {
    	if (this.deleteCompleted) {
    		this.deleteCompleted = false;
    		this.reloadData();
    	} else {
    		this.updateCompleted = true;
    	}
    },

    /**
     *  Commit task or multiple tasks method. Retrieve the data(tasks)
     *  that are selected. Populate AJAX request to REST service
     *  that manage further actions.
     */
    commitSelectedTasks : function() {
    	var _this = this;

    	var clientMessage = {
    		confirm				:  {
    			key				: 'Confirm',
    			value			: 'Are you sure that you want the selected task(s) to be started ?' 
    		},
    		status				: 'Status',
    		taskNotSaved    	: 'Please, save the project schedule, before to commit the task.',
    		missingRequiredData : 'Please, fill the reqired task(s) data, before commit.',
    		alreadyCommited  	: 'The task(s) cannot be commited.',
    		// client positive response string
    		btnYResponse : 'yes'
    	};
    	
    	var selectedTasks = _this.gantt.getView().getSelectionModel().getSelection();
    	var validator = _this.commitValidator(selectedTasks);
    	
    	// Sorts task from parents to children to be committed in order.
    	selectedTasks = this.sortSelectionHierarchically(selectedTasks, false, true);
    	
    	// validate task(s) before commit
     	if(typeof validator != 'object' && validator){
	    	
     		//Confirm client message
    		Ext.Msg.confirm(clientMessage.confirm.key, clientMessage.confirm.value, function(btn) {
    			var confirm = btn;
	    		if(confirm == clientMessage.btnYResponse) {
	         		// Request data
	    	    	_this.showLoadingMask();
	    			var data = {
	                 	projectId	: _this.projectData.projectId, 
	                 	tasks		: _this.extract(selectedTasks)
	                 };
	                 Ext.Ajax.request({
	                	 scope    : _this,
	                     url      : _this.serviceUrl.commitTask,
	                     method   : 'POST',
	                     jsonData : data,
	                     callback : function () {
	                    	 _this.taskStore.fireEvent('commit');
	                    	 _this.reloadData();
	                     }
	                 }); 
	         	} 
	    	}); 
    		
    	}else if(validator.needSave){
    		// Client message that schedule must be saved
     		Ext.Msg.alert(clientMessage.status, clientMessage.taskNotSaved);
     	}else if(validator.needData){
     		// Client message that require data are not listed
     		Ext.Msg.alert(clientMessage.status, clientMessage.missingRequiredData);
     	}else if(validator.committed){
     		// Client message that task(s) are already committed
     		Ext.Msg.alert(clientMessage.status, clientMessage.alreadyCommited);
     	}
    },
    
    /**
     * Method for validating schedule records before committing.
     * 
     * @param tasks - selected task by the user
     * 
     * return boolean or filter object
     */
    commitValidator : function(tasks){
    	
    	var tasks 	 = tasks;
    	var modified = this.taskStore.getModifiedRecords();
    	// validation filters
    	var objRestrict = {
    		needSave  	   	   : false,
    		committed    	   : false,
    		needData           : false
    	};
    	
    	if(modified.length){
    		objRestrict.needSave = true; 
    	}else {
    		// loop through selected tasks
	    	for(var i in tasks){
	    		var task = tasks[i];
	    		var required = {
	    			// required data
	    			assignment   			: task.hasAssignments(),
	    			type	     			: ($.trim(task.data.et).length),
	    			subtype    	 			: ($.trim(task.data.tp).length),
	    			// already committed restriction
	    			// task is already committed
	    			taskCommited   			: ($.trim(task.data.uid).length),
	    			// parent is already committed. Should be committed or should be send for commit now
	    			parentAlreadyCommited 	: ($.trim(task.parentNode.data.uid).length),
	    			// parent is in the list with tasks to commit
	    			parentSendForCommit		: this.isNodeInList(tasks, task.parentNode)
	        	};
	    		// some of the required data is missing
	    		if(!required.assignment || !required.type || !required.subtype){
	    			objRestrict.needData = true;
	    			break;
	    		// task(s) are already committed	
	    		}else if(!(required.parentAlreadyCommited || required.parentSendForCommit) || required.taskcommit){
	    			objRestrict.committed = true;
	     			break;
	     		}
	    		
	    	}
    	}
    	
    	// validation has been passed
    	if(!objRestrict.needSave && !objRestrict.needData && !objRestrict.committed){
    		return true;
    	}
    	
    	return objRestrict;
    },
    
    /**
     * Checks if a node exists in an array of nodes.
     */
    isNodeInList : function(nodes, node) {
    	for (var i=0; i<nodes.length; i++) {
    		if (nodes[i] === node) {
    			return true;
    		}
    	}
    	return false;
    },
    
    checkProjectRestrictions : {
    	
    	/**
    	 * Manage schedule 'read-only' based on the project configuration(received from the back-end).
    	 * This method hide or show component(s).
    	 * 
    	 */
    	checkEditMode : function(scope) {
    	    var projectDisabled = scope.scheduleConfig.disabled;
        	// restriction button based on the schedule edit mode
        	var disableButton = Ext.getCmp('disableEdit');
        	// list with restrict elements
        	var restrictObjectList = [ disableButton ];
        	// find out project schedule - edit mode 
        	if (projectDisabled){
        		// edit mode is disabled and all restrict element will be hide
        		this.restrictionUtil.groupElementVisibilityChange(restrictObjectList, 'hide');
        		
        	} else {
        		// edit mode is enabled and all restrict element will be shown
        		this.restrictionUtil.groupElementVisibilityChange(restrictObjectList, 'show');
        	}
        	// set edit mode
        	scope.gantt.setReadOnly(projectDisabled);
    	},
    
    	restrictionUtil : {
    		
    		/**
    		 * Change visibility for group of schedule objects.
    		 * 
    		 * @param elementObjectList - list with object, buttons, 
    		 * tasks, panels, etc.
    		 * 
    		 * @param operation - takes 'show' or 'hide' string and 
    		 *  activate operation based on it.
    		 */
    		groupElementVisibilityChange : function(elementObjectList, operation){
    			// loop through all elements
    			for(var i in elementObjectList){
    				if ($.trim(operation) == 'show') {
    					// show the element
    					elementObjectList[i].show();
    				} else if( $.trim(operation) == 'hide'){
    					// hide the element
    					elementObjectList[i].hide();
    				}
    			}
    		}
    	} 
    },
    
    getSelectedTasks : function() {
    	var selectedTasks = this.gantt.getView().getSelectionModel().getSelection();
    	return selectedTasks;
    },
    
	/**
	 * Setter for gantt panel.
	 */
	setGantt : function(gantt) {
		this.gantt = gantt;
	},
	
	showLoadingMask : function() {
    	if (this.gantt) {
    		this.gantt.showLoadingMask();
    	}
	},
	
	/**
	 * Order flat selection by hierarchy in a given order 
	 * 
	 * @param selection a flat selection
	 * @param allChildren boolean indicating if all children must be added to the result or only selected ones i.e.
	 * if allChildren == true all top parents will be added and then all their children no matter if they are selected or not
	 * if allChildren == false all top parents will be added and then only those of their children which are also selected
	 * @param descending boolean flag indicating the order of the result. If true parents will always be before their children in the result.
	 */
	sortSelectionHierarchically : function(selection, allChildren, descending) {
		var roots = [];

		if (allChildren) {
			roots = this.getNodesDescendants(selection, descending);
		} else {
			Ext.each(selection, function(record) {
				var index = this.findClosestAncestorIndex(roots, record);
				if (descending) {
					if (index === -1) {
						roots.unshift(record);
					} else {
						roots.splice(index+1, 0 , record);
					}
				} else {
					if (index === -1) {
						roots.push(record);
					} else {
						if (index === 0) {
							roots.unshift(record);
						} else {
							roots.splice(index, 0 , record);
						}
					}
				}
			}, this);
		}
		return roots;
	},
	
	/**
	 * Looks through an array to find the closest ancestor of a given node.
	 * We have a (flat) selection in a tree and we want to find closest selected ancestor.
	 * 
	 * @param array an array of nodes which have parentNode property
	 * @param node a node which closest ancestor we want to find
	 * @returns the index of the ancestor in the array or -1 if not found
	 */
	findClosestAncestorIndex : function(array, node) {
		if (node.parentNode) {
			var index = Ext.Array.indexOf(array, node.parentNode);
			if (index == -1) {
				index = this.findClosestAncestorIndex(array, node.parentNode);
			}
			return index;
		} else {
			return -1;
		}
	},
	
	/**
	 * Creates an array with node's descendants in a given order (recursively)
	 * @param node
	 * @param isDescending
	 * @returns {Array}
	 */
	getNodeDescendants : function(node, isDescending) {
		var descendants = [];			
		if (node.childNodes) {
			Ext.each(node.childNodes, function(childNode) {
				
				if (isDescending) {
					descendants.push(childNode);
					this.getNodeDescendants(childNode, isDescending).forEach(function(descendantNode){descendants.push(descendantNode)});
				} else {
					this.getNodeDescendants(childNode, isDescending).forEach(function(descendantNode){descendants.push(descendantNode)});
					descendants.push(childNode);
				}
			}, this);
		}
		return descendants;
	},
	
	/**
	 * Gets all top level nodes i.e. nodes which parent is not in the array and gets all their children recursively in a given order.
	 * @param array
	 * @param isDescending
	 * @returns {Array}
	 */
	getNodesDescendants : function(array, isDescending) {
		var topNodes = [];
		Ext.each(array, function(node) {
			var index = this.findClosestAncestorIndex(array, node);
			if (index === -1) {
				if (isDescending) {
					topNodes.push(node);
					this.getNodeDescendants(node, isDescending).forEach(function(descendantNode){topNodes.push(descendantNode)});
				} else {
					this.getNodeDescendants(node, isDescending).forEach(function(descendantNode){topNodes.push(descendantNode)});
					topNodes.push(node);
				}
			}
		}, this);
		return topNodes;
	},
	
	/**
	 * Gets all nodes from an array which parents are not in the array 
	 * 
	 * @param array
	 */
	getTopNodes : function(array) {
		var topNodes = [];
		Ext.each(array, function(node) {
			var index = this.findClosestAncestorIndex(array, node);
			if (index === -1) {
				topNodes.push(node);
			}
		}, this);
		return topNodes;
	}
});