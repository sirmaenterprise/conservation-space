/**
 * Overrides Gnt.widget.taskeditor.TaskEditor (context menu -> Edit task) and disable "Add new" button in Resources tab if there is resource added.
 * Allows only one resource to be assigned to a task.
 */

Ext.define('PMSch.widget.taskeditor.PMTaskEditor', {
	override : 'Gnt.widget.taskeditor.TaskEditor',

	initComponent : function() {
		this.callParent(arguments);

		var assignmentGrid = this.assignmentGrid;
		var assignmentStore = assignmentGrid.getStore();

		if (!assignmentGrid.addBtn) {
			assignmentGrid.addBtn = assignmentGrid.down('[iconCls="gnt-action-add"]');
		}

		assignmentStore.on('add', function() {
			assignmentGrid.addBtn && assignmentGrid.addBtn.setDisabled(true);
		});

		assignmentStore.on('remove', function() {
			assignmentGrid.addBtn && assignmentGrid.addBtn.setDisabled(false);
		});
	},

	listeners : {
		loadTask : function(_this, task, eOpts) {
			var assignmentGrid = _this.assignmentGrid;
			var assignmentStore = assignmentGrid.getStore();

			if (!assignmentGrid.addBtn) {
				assignmentGrid.addBtn = assignmentGrid.down('[iconCls="gnt-action-add"]');
			}
			assignmentGrid.addBtn && assignmentGrid.addBtn.setDisabled(assignmentStore.getRange().length);
		}
	}
});