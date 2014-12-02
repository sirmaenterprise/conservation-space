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
             'PMSch.ResourceAllocationPanel',
             'PMSch.model.PMResourceAllocationModel'
             ]);

Ext.onReady(function() {
	PM.ResourceAllocation.init();
});

PM.ResourceAllocation = {

		scheduleConfig	: {},

		// Initialize application
		init : function(serverCfg) {
			this.gantt = this.createGantt(PM.Sch.resourceAllocationData);

			var vp = Ext.create("Ext.panel.Panel", {
				layout  : 'fit',
				renderTo: 'resourceAllocationContainer',
				height	: 900,
				items   : this.gantt
			});
		},

		createGantt : function(resourceAllocationData) {
			var taskStore = Ext.create("Gnt.data.TaskStore", {
				model 		: 'PMSch.model.PMResourceAllocationModel',
				rootVisible	: false,

				// read carefully autoLoad method documentation - there are some useful comments
				// http://docs.sencha.com/extjs/4.1.3/#!/api/Ext.data.JsonStore-cfg-autoLoad
				root: {
					expanded: false
				},

				proxy : {
					type 	: 'memory'
				},

				loadDataSet : function () {
					var url = '/emf/service/resourceAllocation/task/filter';
						
					Ext.Ajax.request({
						url      : url,
						method   : 'POST',
						jsonData : resourceAllocationData,
						success  : this.populateDataStore,
						callback : function () {
							this.fireEvent('load');
						},
						scope    : this
					});
				},

				populateDataStore : function(response) {
					var data = Ext.decode(response.responseText);
					//this.loadData(data);
					this.setRootNode(data);
				}
			});
			//taskStore.loadDataSet();

			var g = Ext.create("PMSch.ResourceAllocationPanel", {
				layout					: 'border',
				region          		: 'center',
				rowHeight       		: 26,
				selModel        		: new Ext.selection.TreeModel({
					ignoreRightMouseSelection   : false,
					mode                        : 'MULTI'
				}),
				taskStore       		: taskStore,
				resizeHandles			: 'none',
				enableTaskDragDrop		: false,
				enableDependencyDragDrop: false,

				//snapToIncrement : true,    // Uncomment this line to get snapping behavior for resizing/dragging.
				columnLines     		: false,
				rowLines        		: false,

				//startDate       		: resourceAllocationData.startDate,
				//endDate         		: Sch.util.Date.add(new Date(2020,0,4), Sch.util.Date.WEEK, 20),
				autoFitOnLoad			: true,
				showTodayLine			: true,
				viewPreset      		: 'weekAndDayLetter',
				resourceAllocationData	: resourceAllocationData
			});
			return g;
		}
};

