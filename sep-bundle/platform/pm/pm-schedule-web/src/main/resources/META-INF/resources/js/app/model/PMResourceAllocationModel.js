Ext.define('PMSch.model.PMResourceAllocationModel', {
	extend : 'Gnt.model.Task',

	// read carefully autoLoad method documentation - there are some useful
	// comments
	// http://docs.sencha.com/extjs/4.1.3/#!/api/Ext.data.JsonStore-cfg-autoLoad
	
	autoLoad 	: false,
	autoSync 	: false,

	// A field in the data-set that will be added as a CSS class to each rendered
	// task element
	nameField	: 'ResourceName',
	/*clsField 	: 'EntryType',*/
	fields 		: [{
		name 	: 'aiid', // Actual instance id
		type 	: 'string'
	},
	{
		name 	: 'Status',
		type 	: 'string'
	}, 
	{
		name 	: 'ProjectTitle',
		type 	: 'string'
	}, 
	{
		name 	: 'ProjectId',
		type 	: 'string'
	}]
});