Ext.require([
	'Ext.grid.*',
	'Ext.data.*',
	'Ext.util.*',
	'Ext.state.*'
]);

$.fn.extjsDataGrid = function(options) {
	var jqPlugin = { };

	options.fields.push({
		name: 'dbId',
		type: 'string'
	});
	options.fields.push({
		name: 'type',
		type: 'string'
	});
	
	// TODO: is there a way to move the definition out and then create an instance and pass it to the store
	Ext.define('dynamicModel', {
		extend: 'Ext.data.Model',
		fields: options.fields
	});

	jqPlugin.plugins = [];
	if (options.enableEditing) {
		jqPlugin.plugins.push(
			Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			})
		);
	}

	return this.each(function() {
		var element = this;

		// TODO: how to override pagination
		var store = null;
		if (options.storeType && options.storeConf) {
			store = Ext.create(options.storeType, options.storeConf);
		} else {
			store = Ext.create('Ext.data.Store', {
				model: 'dynamicModel',
				autoLoad: false,
				autoSync: true,
				proxy: {
		            type: 'ajax',
		            api: {
		                read:  options.dataUrl,
		                update: options.updateUrl
		            },
		            reader: {
		                type: 'json'
		            },
		            writer: {
		                type: 'json',
		                writeAllFields: true
		            }
		        }
			});
		}

		var grid = Ext.create('Ext.grid.Panel', {
			columnLines: true,
			loadMask: true,
			store: store,
			plugins: jqPlugin.plugins,
			columns: options.columns,
			cls: 'relations-wgt-wrapper',
			viewConfig	: {
				stripeRows: true
			},
			selModel: {
				selType: 'cellmodel'
			},
			listeners: {
				viewready: function( _this, eOpts ) {
					_this.getStore().load();
				},
				beforeedit: function(editor, e) {
					return e.column.editable;
				}
			}
		});
		
		Ext.create('Ext.container.Container', {
			renderTo: element,
			items	: [ grid ],
		    layout	: {
		        type: 'anchor'
		    }
		});
	});
};