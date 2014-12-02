EMF = {
	applicationPath: '/emf',
	servicePath: '/emf/service',
	ajaxGUIBlocker: {
		hideAjaxLoader: function() {}
	},
	util: {
		generateUUID: function() {
			return new Date().getTime();
		}
	},
};

EMF.reporting = {
		propertiesServicePath : EMF.servicePath + '/properties/fields-by-type',
		facetedSearchPath : EMF.servicePath + "/search/faceted",
		objectServicePath : EMF.servicePath + "/object-rest"
};

SF = {
	config : {
		dateFormatPattern : 'MM/dd/yy'	
	}
};

_emfLabels = {
	'widget.checkbox.title': 'Checkbox',
	'widget.reporting.timeinterval.year' : 'Year'
};