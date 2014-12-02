Ext.define("PMSch.ResourceAllocationPanel", {
    extend : "Gnt.panel.Gantt",
    requires : [
                'PMSch.ResourceAllocationToolbar'
    ],
    rightLabelField : 'Responsible',
    highlightWeekends : true,
    showTodayLine : true,
    loadMask : true,
    cascadeChanges : true,

    initComponent : function() {

        Ext.apply(this, {
            lockedGridConfig : {
                width       : 502,
                title       : 'Resources',
                collapsible : true
            },

            lockedViewConfig  : {
                getRowClass : function(rec) { return rec.isRoot() ? 'root-row' : ''; }
            },

            // Experimental
            schedulerConfig : {
                collapsible : true,
                title : 'Resource Allocation'
            },
            
            leftLabelField : {
                dataIndex : 'ResourceName'
            },
            
            rightLabelField : {
                dataIndex : 'ProjectTitle'
            },
            
            eventRenderer : function (taskRec) {
            	var projectId = this.resourceAllocationData.projectId;
            	if (projectId && projectId != taskRec.data.ProjectId && taskRec.data.leaf) {
	            	return {
	                    style : 'background-color:#F6CED8'
	                };
            	}
            }.bind(this),
            
            // Define the static columns
            columns : [
                       {
                    	   header	: 'Project Title',
                    	   dataIndex: 'ProjectTitle',
                    	   align 	: 'right',
                    	   width 	: 100,
                    	   renderer : function(v, meta, r) {
	                       		var projectId = r.data.ProjectId;
	                       		if(projectId){
	                       			var url = EMF.bookmarks.buildLink('projectinstance', projectId);
	                       			var link = '<a href="'+url+'" target="_blank">'+v+'</a>';
	                       			return link;
	                       		}
	                       		return v;
                       	   }
                       },
                       {
                    	   xtype 	: 'treecolumn',
                    	   header	: 'Resource Name',
                    	   sortable	: true,
                    	   dataIndex: 'ResourceName',
                    	   width	: 200,
                    	   field	: {
                    		   allowBlank	: false
                    	   },
                    	   renderer : function(v, meta, r) {
                    		   if (!r.data.leaf) {
                    			   meta.tdCls = 'sch-gantt-parent-cell';
                    		   } else {
                    			   var url = EMF.bookmarks.buildLink(r.data.cls, r.data.aiid);
	                       		   var link = '<a href="'+url+'" target="_blank">'+v+'</a>';
	                       		   return link;
                    		   }
                    		   return v;
                    	   }
                       },
                       {
                    	   header	: 'Status',
                    	   dataIndex: 'Status',
                    	   width	: 100,
                    	   align 	: 'right'
                       },
                       {
                    	   header	: 'Duration',
                    	   xtype 	: 'durationcolumn',
                    	   width 	: 100
                       }
            ],
            
            plugins : [
                       Ext.create('Ext.grid.plugin.BufferedRenderer')
            ],
            
            tooltipTpl : new Ext.XTemplate(
                    '<strong class="tipHeader">{Name}</strong>',
                    '<table class="taskTip">',
                        '<tr><td>Start:</td> <td align="right">{[values._record.getDisplayStartDate("y-m-d")]}</td></tr>',
                        '<tr><td>End:</td> <td align="right">{[values._record.getDisplayEndDate("y-m-d")]}</td></tr>',
                        '<tr><td>Progress:</td><td align="right">{[ Math.round(values.PercentDone)]}%</td></tr>',
                    '</table>'
             ),
             
             tbar : new PMSch.ResourceAllocationToolbar({
            	gantt : this
             })
        });

        this.callParent(arguments);
        
        this.on('afterlayout', this.triggerLoad, this, { single : true, delay : 100 });
        this.taskStore.on({
        	load	: function() { this.body.unmask(); },
        	save	: this.showLoadingMask,
        	commit	: this.showLoadingMask,
        	scope	: this
        });
    },
    
    showLoadingMask : function() {
    	this.body.mask('Loading...', '.x-mask-loading');
    },
    
	triggerLoad : function() {
	    this.showLoadingMask();
	    this.taskStore.loadDataSet();
	}
});
