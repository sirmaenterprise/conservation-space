EMF.mainMenu = {

	constants : {

		url : "/header"

	},

	options : {},

	loadMainMenu : function(config) {
		EMF.mainMenu.options = $.extend(true, {}, EMF.mainMenu.constants,
				config);

		$.ajax({
			method : 'GET',
			dataType : "text",
			url : EMF.mainMenu.options.servicePath + EMF.mainMenu.options.url
		}).done(function(data) {
			var response = data;
			$("#mainMenuForm").append(response);
		});
	}
};
