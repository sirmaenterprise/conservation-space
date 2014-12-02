EMF.ResourceAllocation = {
    	
	// constants
	constants : null,
	
	/**
	 * Init method for ResourceAllocation object.
	 */
	init : function() {
		EMF.ResourceAllocation.constants = {
			searchUsersUrl : EMF.servicePath + '/resources/get/user',
			searchLabelValueUrl: EMF.servicePath + '/label/bundle/'
		};
		EMF.ResourceAllocation.initPickList();
		// attach click events
		$('#resourceAllocationSelectUsersWrapper input[type=submit]').click(EMF.ResourceAllocation.onClickSubmit);
	    $('#resource-allocation-selected-users-link').click(EMF.ResourceAllocation.onClick);
	},
	
	/**
	 * Init method for picklist.
	 */
	initPickList : function() {
		$('#resourceAllocationSelectUsersWrapper').picklist({
			updatePreviewField 	: false,
			pklMode				: 'multy',
			submitValueLink 	: $('#resourceAllocationSelectUsersWrapper input[type=submit]'),
			hiddenField 		: $('#resourceAllocationSelectUsersWrapper input[type="hidden"]'),
			okButtonTitle		: _emfLabels['cmf.btn.proceed'],
			cancelButtonTitle	: _emfLabels['cmf.btn.cancel'],
			headerTitle			: _emfLabels['pm.taskallocation.selectusers'],
			imgResourceService  : "/service/dms/proxy/",
			applicationContext  : EMF.applicationPath
		});
	},
	
	/**
	 * On click event for href.
	 */
	onClick : function(event) {
        event.preventDefault();
        event.stopImmediatePropagation();
        EMF.ResourceAllocation.loadUsers();
	},
	
	/**
	 * Click event for submit input.
	 */
	onClickSubmit : function(event) {
        event.preventDefault();
        event.stopImmediatePropagation();
        var hiddenFieldValue = $('#resourceAllocationSelectUsersWrapper input[type="hidden"]').val();
        if (hiddenFieldValue) {
        	 var href = $('#resource-allocation-selected-users-link').attr("href");
        	 window.location.href = href + "?usernames=" + hiddenFieldValue + "&viewOtherProjects=false";
        }
	},
	
	/**
	 * Method to load users after click event received.
	 */
	loadUsers : function() {
		 $.ajax({
				method : 'GET',
				crossDomain: true,
				url : EMF.ResourceAllocation.constants.searchUsersUrl
			    }).done(function(data) {
			    	$('#resourceAllocationSelectUsersWrapper').trigger({
	    				type : 'openPicklist',
	    				itemsList : data
	    			});
			    });
	}
	
};
