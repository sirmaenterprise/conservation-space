Ext.define('PMSch.model.PMTaskModel', {

	extend : 'Gnt.model.Task',

	// read carefully autoLoad method documentation - there are some useful
	// comments
	// http://docs.sencha.com/extjs/4.1.3/#!/api/Ext.data.JsonStore-cfg-autoLoad
	autoLoad : false,
	autoSync : false,

	// A field in the data-set that will be added as a CSS class to each rendered
	// task element
	nameField: 'title',
	defType: 'defType',
	clsField : 'et',
	startDateField : 'sd',
	endDateField : 'ed',
	durationField : 'd',
	durationUnitField : 'du',
	baselineStartDateField : 'bsd',
	baselineEndDateField : 'bed',
	baselinePercentDoneField : 'bpd',
	phantomIdField : 'phid',
	phantomParentIdField : 'phpid',
	effortField : 'e',
	effortUnitField : 'eu',
	calendarIdField : 'c',
	noteField : 'n',
	percentDoneField : 'pd',
	manuallyScheduledField : 'ms',
	schedulingModeField : 'schm',
	
	fields : [ {
		name : 'et', //'EntryType',
		type : 'string'
	}, {
		name : 'tp', //'type',
		type : 'string'
	}, {
		name : 'aiid', //'ActualInstanceId',
		type : 'string'
	}, {
		name : 'Color',
		type : 'string'
	}, {
		name : 's', //'status',
		type : 'string',
		//TODO: access from the code-list 
		defaultValue : 'Submitted'
	}, {
		name : 'uid', //'identifier',
		type : 'string'
	}, {
		name : 'sm', //'StartMode',
		type : 'string',
		defaultValue : 'Auto'
	}, {
		name: 'index', 
		type: 'int'
	}]
});