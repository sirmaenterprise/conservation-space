Ext.ns('PM');

Ext.Loader.setConfig({
    enabled: true,
    disableCaching : true,
    paths : {
        'Gnt'   : '../libs/extGantt',
        'Sch'   : '../libs/extSch',
        'PMSch' : '../js/app/'
    }
});

Ext.require([
    'PMSch.GanttPanel',
    'PMSch.model.PMTaskModel',
    'PMSch.model.PMResourceStore',
    'PMSch.controller.ScheduleController'
]);

Ext.onReady(function() {
	//console.time('gnt');
    Ext.QuickTips.init();

    PM.Gantt.init();
    //console.timeEnd('gnt');
});

PM.Gantt = {
		
	scheduleConfig	: {},

    // Initialize application
    init : function(serverCfg) {
        this.gantt = this.createGantt(PM.Sch.projectData);

        var vp = Ext.create("Ext.panel.Panel", {
            layout  : 'fit',
            renderTo: 'scheduleContainer',
            height	: 900,
            items   : this.gantt
        });
    },
    
    createGantt : function(projectData) {
    	
        var taskStore = Ext.create("Gnt.data.TaskStore", {
            model 		: 'PMSch.model.PMTaskModel',
            rootVisible	: true,
            
            // read carefully autoLoad method documentation - there are some useful comments
            // http://docs.sencha.com/extjs/4.1.3/#!/api/Ext.data.JsonStore-cfg-autoLoad
            root: {
            	expanded: false
            },

            proxy : {
                type 	: 'ajax',
                method 	: 'POST',
                url		: '/emf/service/schedule/task/view?projectId=' + projectData.projectId,

                reader : {
                    type: 'json'
                },
                
                writer: {
                    type			: 'json',
                    writeAllFields	: true,
                    encode			: false,
                    allowSingle 	: false,
                    root			: 'data'
                },
                
                listeners: {
                    exception: function(proxy, response, operation){
                    	var error = null;
                    	if (!operation.getError()){
                    		error = Ext.JSON.decode(response.responseText);
                    		error = error.message;
                    	} else {
                    		error = operation.getError().statusText;
                    	}
                    	
                        Ext.MessageBox.show({
                            title: 'REMOTE EXCEPTION',
                            msg: error,
                            icon: Ext.MessageBox.ERROR,
                            buttons: Ext.Msg.OK
                        });
                    }
                }
            }
        });
        
        var dependencyStore = Ext.create("Gnt.data.DependencyStore", {
            autoLoad : true,
            proxy: {
                type 	: 'ajax',
                url		: '/emf/service/schedule/resourse/dependency?projectId=' + projectData.projectId,
                method	: 'GET',
                reader	: {
                    type : 'json'
                }
            }
        });

//        var resourceStore = Ext.create('Gnt.data.ResourceStore', {
//            model : 'Gnt.model.Resource'
//        });
        var resourceStore = Ext.create('Gnt.data.ResourceStore', {
            model : 'PMSch.model.PMResourceStore',
            storeId:'resourceStore'
        });

        var assignmentStore = Ext.create('Gnt.data.AssignmentStore', {
            autoLoad    : true,
            // Must pass a reference to resource store
            resourceStore : resourceStore,
            
            proxy : {
                type 	: 'ajax',
                url 	: '/emf/service/schedule/resourse/assignment?projectId=' + projectData.projectId,
                method	: 'GET',
                reader 	: {
                    type : 'json',
                    root : 'assignments'
                }
            },
            listeners : {
                load : function() {
                    resourceStore.loadData(this.proxy.reader.jsonData.resources);
                },
                // resourceassignment is internal flag used to mark cell dirty
                // We don't have update event. When assignments are changed previous assignments are removed and new are added.
                add : function(store, records, index, eOpts) {
                	var record = records[0];
                	var taskNode = taskStore.getNodeById(record.getTaskId()); 
                	taskNode.modified['resourceassignment'] = true;
                },
                remove : function(store, record, index, isMove, eOpts) {
                	var taskNode = taskStore.getNodeById(record.getTaskId()); 
                	taskNode.modified['resourceassignment'] = true;
                }
            }
        });
        
        var scheduleController = Ext.create("PMSch.controller.ScheduleController", {
        	taskStore		: taskStore,
        	resourceStore	: resourceStore,
        	assignmentStore	: assignmentStore,
        	dependencyStore	: dependencyStore,
        	projectData 	: projectData,
        	scheduleConfig	: this.scheduleConfig
        });

        var gantt = Ext.create("PMSch.GanttPanel", {
        	layout				: 'border',
            region          	: 'center',
            rowHeight       	: 26,
            scheduleController	: scheduleController,
            taskStore       	: taskStore,
            dependencyStore 	: dependencyStore,
            assignmentStore 	: assignmentStore,
            resourceStore   	: resourceStore,
            selModel        	: new Ext.selection.TreeModel({
            	ignoreRightMouseSelection   : false,
            	mode                        : 'MULTI'
            }),

            //snapToIncrement : true,    // Uncomment this line to get snapping behavior for resizing/dragging.
            columnLines     	: false,
            rowLines        	: false,
            
            //startDate       	: projectData.startDate,
            //endDate         	: Sch.util.Date.add(new Date(2020,0,4), Sch.util.Date.WEEK, 20),
            autoFitOnLoad		: true,
            showTodayLine		: true,
            viewPreset      	: 'weekAndDayLetter',
            showTeamPanel 		: true,
            projectData			: projectData
        });
        
        scheduleController.setGantt(gantt);//set
        
        gantt.on({
            dependencydblclick : function(ga, rec) {
                var from    = taskStore.getNodeById(rec.get('From')).get('Name'),
                    to      = taskStore.getNodeById(rec.get('To')).get('Name');

                //Ext.Msg.alert(Ext.String.format('clicked the link between "{0}" and "{1}"', from, to));
            },
            timeheaderdblclick : function(col, start, end) {
                //Ext.Msg.alert('clicked header cell : ' + Ext.Date.format(start, 'Y-m-d') + ' - ' + Ext.Date.format(end, 'Y-m-d'));
            }
        });

        return gantt;
    }
};

