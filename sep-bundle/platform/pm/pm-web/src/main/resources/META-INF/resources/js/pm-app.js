/**
 * Create PM global namespace.
 */
var PM = PM || {};

/**
 * PM configuration properties.
 */
PM.config = {

};

/**
 * PM utility functions.
 */
PM.utilityFunctions = {

	initDynamicForm : function(panelClass) {
		CMF.utilityFunctions.initDynamicForm(panelClass);
	},
	
	/**
	 * Loop to all select tags in resource manager page and 
	 * initialize with select2 plugin. This function is used also in
	 * <b>resource-manager.js</b> file for click events and default selected
	 * elements.
	 */
	initResourceManagerSelectors : function(){
		var resourceFormSelectTags = ".rm-container select:visible";
		CMF.utilityFunctions.initDropDown(resourceFormSelectTags);
	}

};

PM.init = function(opts) {
	PM.config = $.extend(true, {}, PM.config, opts);
};

// Register PM module
EMF.modules.register('PM', PM, PM.init);
