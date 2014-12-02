/**
 * Override of Gnt.widget.AssignmentGrid in order to set single selection mode.
 * Allows only one resource to be assigned to a task.
 */

Ext.define('PMSch.widget.PMAssignmentGrid', {
	override : 'Gnt.widget.AssignmentGrid',
	constructor : function (config) {
		this.store = Ext.create("Ext.data.JsonStore", {
			model: Ext.define('Gnt.model.AssignmentEditing', {
				extend : 'Gnt.model.Assignment',
				fields : ['ResourceName']
			})
		});

		this.columns = [{ xtype : 'resourcenamecolumn' }];

		if (!this.readOnly) {
			this.plugins = this.buildPlugins();
		}

		Ext.apply(this, {
			selModel: {
				allowDeselect : true,
				selType: 'rowmodel',
				mode: 'SINGLE',

				// Hack to keep records selected when tabbing in the cells
				selectByPosition : function(position) {
					var record = this.store.getAt(position.row);
					this.select(record, true);
				}
			}
		});

		this.callSuper(arguments);
	}
});