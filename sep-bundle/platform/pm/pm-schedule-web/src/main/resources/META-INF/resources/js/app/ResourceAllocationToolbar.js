Ext.define("PMSch.ResourceAllocationToolbar", {
    extend : "Ext.Toolbar",
    gantt : null,
    
    initComponent : function () {
        var gantt = this.gantt;
        
	    var items =  [{
			    xtype : 'buttongroup',
			    title : 'View tools',
			    columns : 6,
			    items : [
			        {
			            iconCls : 'icon-prev',
			            text 	: 'Previous',
			            scope 	: this,
			            handler : function () {
			            	gantt.shiftPrevious();
			            }
			        },
			        {
			            iconCls : 'icon-next',
			            text 	: 'Next',
			            scope 	: this,
			            handler : function () {
			            	gantt.shiftNext();
			            }
			        },
			        {
			            text : 'Expand all',
			            iconCls : 'icon-expandall',
			            scope 	: this,
			            handler : function () {
			            	gantt.expandAll();
			            }
			        },
			        {
			            text 	: 'Collapse all',
			            iconCls : 'icon-collapseall',
			            scope 	: this,
			            handler : function () {
			            	gantt.collapseAll();
			            }
			        },
			        {
			            text : 'Zoom to fit',
			            iconCls : 'zoomfit',
			            scope 	: this,
			            handler : function () {
			            	gantt.zoomToFit();
			            }
			        },
			        {
			            text 		: 'View full screen',
			            iconCls 	: 'icon-fullscreen',
			            disabled 	: !this._fullScreenFn,
			            scope 		: this,
			            handler 	: function () {
			            	this.showFullScreen();
			            }
			        }
			    ]
			},
			{
	            xtype 	: 'buttongroup',
	            title 	: 'View resolution',
	            columns : 4,
	            items : [
	                {
	                    text 	: '6 weeks',
	                    scope 	: this,
	                    handler : function () {
	                    	gantt.switchViewPreset('weekAndMonth');
	                    }
	                },
	                {
	                    text 	: '10 weeks',
	                    scope 	: this,
	                    handler : function () {
	                    	gantt.switchViewPreset('weekAndDayLetter');
	                    }
	                },
	                {
	                    text 	: '1 year',
	                    scope 	: this,
	                    handler : function () {
	                    	gantt.switchViewPreset('monthAndYear');
	                    }
	                },
	                {
	                    text 	: '5 years',
	                    scope 	: this,
	                    handler : function () {
	                        var start = new Date(gantt.getStart().getFullYear(), 0);
	                        gantt.switchViewPreset('monthAndYear', start, Ext.Date.add(start, Ext.Date.YEAR, 5));
	                    }
	                }
	            ]
			}
		];
	    
	    if (gantt.resourceAllocationData.projectId) {
	    	
	    	var viewOtherProjectsButton = Ext.create('Ext.Button', {
				text 			: 'View allocations on other projects',
				iconCls 		: 'togglebutton',
				enableToggle 	: true,
				handler : function (btn) {
					gantt.resourceAllocationData.viewOtherProjects = btn.pressed;
					gantt.triggerLoad();
				}
            });
	    	viewOtherProjectsButton.toggle(gantt.resourceAllocationData.viewOtherProjects, true);
	    	
	    	items.push({
	    		xtype 	: 'buttongroup',
	            title 	: 'Filters',
	            columns : 1,
	            items : [viewOtherProjectsButton]	    		
	    	});
	    }

        Ext.apply(this, {
            items : items
        });

        this.callParent(arguments);
    },
    
    showFullScreen : function () {
        this.gantt.el.down('.x-panel-body').dom[this._fullScreenFn]();
    },
    
	//Experimental, not X-browser
	_fullScreenFn : (function () {
	    var docElm = document.documentElement;
	
	    if (docElm.requestFullscreen) {
	        return "requestFullscreen";
	    }
	    else if (docElm.mozRequestFullScreen) {
	        return "mozRequestFullScreen";
	    }
	    else if (docElm.webkitRequestFullScreen) {
	        return "webkitRequestFullScreen";
	    }
	})()
});