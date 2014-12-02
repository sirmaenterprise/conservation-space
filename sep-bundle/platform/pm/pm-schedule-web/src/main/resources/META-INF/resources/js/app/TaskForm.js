// Define widget for General Tab into Task Editor
Ext.define('PMSch.TaskForm', {
	extend : 'Gnt.widget.taskeditor.TaskForm',
	
	// retrieving the form by id
	id : 'generalTabForm',
	
	startConfig 			: {
		format : SF.config.dateExtJSFormatPattern
	},
	
	finishConfig 			: {
		format : SF.config.dateExtJSFormatPattern
	},
	
	topSplitter          	: {},
	typeCombo 	         	: null,
	subTypeCombo 	     	: null,
	templates 		     	: {
		beforeLabelTextTpl 	: '<table class="gnt-fieldcontainer-label-wrap"><td width="1" class="gnt-fieldcontainer-label">',
		afterLabelTextTpl  	: '<td><div class="gnt-fieldcontainer-separator"></div></table>'
	},
	customComponents		: ["type","subType","err-label","labelSubType","splitterSubType","labelType","startMode","labelStartMode","splitterStartMode"],
	
	data 					: {
		typeStoreData		: [],
		subTypeStoreData	: []
	},
	storeHolder 	   		: {
		typeStore 	   		: {},
		subTypeStore   		: {}
	},
	task					: {},
	typeToCls				: {
    	'projectinstance'			: 'project',
    	'caseinstance' 				: 'case',	
    	'workflowinstancecontext' 	: 'workflow',	
    	'taskinstance' 				: 'task',	
    	'standalonetaskinstance' 	: 'standalonetask'
	},

	constructor : function(config) {
		this.callParent(arguments);
	},
	
	getSplitter : function(label, splitterId) {
		var splitter = {
				id					: splitterId,
				xtype               : 'fieldcontainer',
				fieldLabel          : label,
				labelAlign          : 'top',
				labelSeparator      : '',
				beforeLabelTextTpl  : this.templates.beforeLabelTextTpl,
				afterLabelTextTpl   : this.templates.afterLabelTextTpl,
				layout              : 'hbox',
				defaults            : {
					labelWidth  : 110,
					flex        : 1,
					allowBlank  : false
				}
		}
		return splitter;
	},
	
	/**
	 * Method that will be invoked after the task is opened for editing.
	 * 
	 * 
	 * @param currentTask -
	 *            the task that is opened for editing
	 */
	openTaskForEdit : function(currentTask) {
		this.populateStartMode(currentTask);
		this.populateTypeStore(currentTask);
	},
	
	/**
	 * Creates start mode component depending on the editted task.
	 * If task is in status Submitted it creates a combo to select start mode, otherwise it only shows a label. 
	 * 
	 * @param currentTask -
	 *            the task that is opened for editing
	 */
	populateStartMode : function(currentTask) {
		Ext.getCmp('generalTabForm').add(this.getSplitter('Start Mode', 'splitterStartMode'));
		if (currentTask.data.s && currentTask.data.s === 'Submitted') {
			var startModeStore = Ext.create('Ext.data.Store', { 
				fields: ['value','label'],
			    data: [
			           { 'value' : 'Auto', 'label' : 'Auto' },
			           { 'value' : 'Manual', 'label' : 'Manual' }
			    ]
			});
		
			// combo for the start mode
			var startModeCombo = Ext.create('Ext.form.field.ComboBox', {
				id 				: 'startMode',
				store			: startModeStore,
				mode			: 'local',
			    fieldLabel		: 'Start Mode',
			    displayField	: 'label',
			    valueField 		: 'value',
			    inputValue 		: 'sm',
			    name			: 'sm',
			    width			: 286,
			    labelWidth		: 110,
			    defaultValue	: 'Auto'
			});
			Ext.getCmp('generalTabForm').add(startModeCombo);
			
			var taskStartModeIndex = startModeStore.findExact('value', currentTask.data.sm);
			
			if(taskStartModeIndex >= 0){
				var record = startModeStore.getAt(taskStartModeIndex);
				startModeCombo.setValue(record);
				startModeCombo.fireEvent('select', startModeCombo, record, taskStartModeIndex);
			}
			
		} else {
			var taskStartModeField = {
			        xtype: 'label',
			        id: 'labelStartMode',
			        text: 'Start Mode: ' + currentTask.data.sm,
			        margins: '0 0 0 10',
			        style  :{ size: '13px', color: '#A8A8A8'}
			    };
			// adding task start mode field
			Ext.getCmp('generalTabForm').add(taskStartModeField);
		}
	},
	
	/**
	 * Method that will be invoked after the task is opened for editing.
	 * Here we will send request to the service and manage data, before
	 * display available task types and definitions.
	 * 
	 * @param currentTask -
	 *            the task that is opened for editing
	 */
	populateTypeStore : function(currentTask) {
		Ext.getCmp('generalTabForm').add(this.getSplitter('Task Types', 'splitterSubType'));
		
		this.task = currentTask;
		
		// service path for retrieving types and definitions
		var allowedChildrenServiceURL = '/emf/service/schedule/task/allowedChildren';
		// data needed for the service method
		var data = {};
		// switch scope
		_this = this;
		// retrieve current task definition for task saved detection
		var taskDefinition = currentTask.raw.tp;
		// retrieve parent task definition
		var parentDefinition = currentTask.parentNode.data.tp;
		// retrieve parent task type
		var parentEntry = currentTask.parentNode.data.et;
		// actual instance
		var actualInstanceId = $.trim(currentTask.data.aiid);
		// CHECK FOR TASK IS SAVED
		if(actualInstanceId){
			// field that will display the type
			var taskTypeField = {
			        xtype: 'label',
			        id: 'labelType',
			        text: 'Selected type: '+currentTask.raw.et,
			        style  :{size: '13px',color: '#A8A8A8', display: 'block'},
			        margins: '0 0 0 10'
			    };
			// field that will display the definition
			var taskSubTypeField = {
			        xtype: 'label',
			        id: 'labelSubType',
			        text: 'Selected definition: '+taskDefinition,
			        margins: '0 0 0 10',
			        style  :{ size: '13px', color: '#A8A8A8'}
			    };
			// adding task type field to the General tab
			Ext.getCmp('generalTabForm').add(taskTypeField);
			// adding task definition to the General tab
			Ext.getCmp('generalTabForm').add(taskSubTypeField);
		  
		    // CHECK FOR NEEDED DATA IN THE PARENT
		} else if(parentDefinition && parentEntry){
			// add parent information
			data = currentTask.parentNode.data;
			var children = currentTask.parentNode.childNodes;
			data.children = [];
            // add children information for this parent
			Ext.each(children, function(child) {
                data.children.push(child.data);
            });
		
			var subTypeHolder = {};
			
			/**
			 * Sending request to the service that will return available
			 * types and definitions for current task(in edit mode).
			 */
			Ext.Ajax.request({
                url      : allowedChildrenServiceURL,
                method   : 'POST',
                jsonData : data,
                async : false,
                success: function(response, opts) {
                	var types = [];
                    var subTypes = {};
                    var responseData = Ext.decode(response.responseText);
                    // available types
                    types = responseData.EntryType;
                    // available definitions
                    subTypes = responseData.type;
                    
                    subTypeHolder = subTypes;
                    // fill up the type store data
                    _this.data.typeStoreData = types;
                 },
                 failure: function(response, opts) {
                	 console.info('PMSch_AllowedChildrens_FAILED',response);
                 }
            });
			
			if(!this.data.typeStoreData){
				return;
			}
			
			// Creating store that will holds data for type combo
			this.storeHolder.typeStore = Ext.create('Ext.data.Store', { 
 				fields: ['name','type'],
 				autoLoad : true,
 				autoSync : true,
 			    data: this.data.typeStoreData
 			}); 
			
			var currentTaskEntryType = this.task.data.et;
			var taskEntryTypeIndex = this.storeHolder.typeStore.findExact('type', currentTaskEntryType);
			
			// Creating combo for task type
			this.typeCombo = Ext.create('Ext.form.field.ComboBox', {
			    fieldLabel: 'Select Type',
			    displayField: 'name',
			    valueField :'type',
			    inputValue : 'et',
			    name: 'et',
			    width: 286,
			    id : 'type',
			    forceSelection : true,
			    fireSelectEvent: true,
			    labelWidth: 110,
			    mode: 'local',
			    store: this.storeHolder.typeStore,
			    // define listeners
			    listeners: {
			        select: function(combo, record, index) {
		        		// selected value from the combo(task type combo)
		        		var selectedType = combo.getValue();
		        		// set entry cls depending on its type
		        		currentTask.data.cls = _this.typeToCls[selectedType];
		        		// fill the data for definition store
		        		_this.data.subTypeStoreData = subTypeHolder[selectedType];
		        		// generate combo based on the selection
		        		_this.generateNestedCombo(_this.task.data.tp);
		        		// load the data from the definition store
		        		_this.storeHolder.subTypeStore.loadData(_this.data.subTypeStoreData,false);
		        		// synchronized the data
			        	_this.storeHolder.subTypeStore.sync();
			        	// add the combo to the General Tab
			        	Ext.getCmp('generalTabForm').add(_this.subTypeCombo);
			        }
			    }
			});
			// add the combo for type selecting into the General Tab
			Ext.getCmp('generalTabForm').add(_this.typeCombo);
			
			if(taskEntryTypeIndex >= 0){
				var record = this.typeCombo.getStore().getAt(taskEntryTypeIndex);
				this.typeCombo.setValue(record);
				this.typeCombo.fireEvent('select', this.typeCombo, record, taskEntryTypeIndex);
			}
		
		// Required data(task type or task definition) available
		} else {
			// define error label for the user
			var errLabel = {
		        xtype: 'label',
		        id : 'err-label',
		        text: 'You must specify the type of parent element first',
		        style  :{
		            size: '13px',
		            color: 'red'
		        },
		        margins: '0 0 0 10'
		    }
			// add the error label to the General Tab
			Ext.getCmp('generalTabForm').add(errLabel);
		}
	},
	
	/**
	 * This method will remove all custom components that are available into
	 * the General Tab of Task Editor.
	 */
	destroyElements : function(){
		// custom components IDs
		var components = this.customComponents;
		for(var step in components){
			// current component object
			var currentComponent = Ext.getCmp(components[step]);
			// check is current component available into the tab
			if(currentComponent != null && currentComponent != undefined){
				// remove
				currentComponent.destroy();
			}
		}
	},
	
	/**
	 * Generate package - combo box and store that will be used for
	 * selecting task definition based on the task type.
	 */
	generateNestedCombo : function(taskTypeValue){
		// retrieve previous combo
		var stCombo = Ext.getCmp('subType');
		var value = undefined;
		
		 	// check is it available
   		 if(stCombo!=null && stCombo!=undefined){
   			 stCombo.destroy();
   		 }
		
		// Creating store for task definition(sub-type)
		this.storeHolder.subTypeStore = Ext.create('Ext.data.Store', { 
				fields: ['name','type'],
				autoLoad : true,
				autoSync : true,
			    data: this.data.subTypeStoreData
		});
		
		value = this.storeHolder.subTypeStore.getAt(0);
		
		if(taskTypeValue){
			var recordIndex = this.storeHolder.subTypeStore.findExact('type', taskTypeValue);
			if(recordIndex >= 0){
				value = taskTypeValue;
			}
		}
		
    	// Creating combo box for task definition
		this.subTypeCombo = Ext.create('Ext.form.field.ComboBox', {
		    fieldLabel: 'Select sub-type',
		    displayField: 'name',
		    valueField :'type',
		    inputValue : 'tp',
		    name: 'tp',
		    width: 286,
		    id : 'subType',
		    labelWidth: 110,
		    mode: 'local',
		    value : value,
		    store: this.storeHolder.subTypeStore
		});
		
	}
});

