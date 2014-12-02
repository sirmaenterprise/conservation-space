/**
 * Contains all utility functions in CLS
 */
var CLS = CLS || {};
CLS.util = {}

/**
 * displays the ajax loader into the given DOM element - 'parentElementId' with
 * the given supporting text into it.
 * 
 * @param parentElementId
 *            is the ID of the parent element where the loader will be displayed
 * @param loadingPrompt
 *            is a text to be displayed under the loader
 * @param append -
 *            if set to true, the ajax loader will be appended to the content of
 *            the container, instead of clearing everything in it
 */
// TODO: Load the gif with the page?
CLS.util.displayAjaxLoader = function(parentElementId, loadingPrompt, append) {
	var loaderHtml = "<img id=\"ajax-loader\" src=\"graphics/ajax-loader.gif\"><br>"
			+ loadingPrompt + "</img><br>";
	if (append) {
		$("#" + parentElementId).append(loaderHtml);
	} else {
		$("#" + parentElementId).html(loaderHtml);
	}
}

/**
 * Cuts all white spaces from the beginning and the end of the string
 */
CLS.util.trim = function(str) {
	return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

/**
 * Performs the ajax GET request to the url and passes the response (as a js
 * object) to the handlerFunction
 * 
 * @param url
 *            is the service's url
 * @param handlerFunction
 *            is a function with one parameter that will handle the response (a
 *            js object)
 * 
 */
CLS.util.makeGetJsonRequest = function(url, handlerFunction) {
	$.getJSON(url, function() {
	}).done(function(data) {
		handlerFunction(data);
	}).fail(function() {
		//REVIEW: maybe popup a CLS dialog with the error here
		console.log("There was an error sending the request to the server");
	})
}

/**
 * Resets all input text fields that are children of the given parent element.
 */
CLS.util.resetAllInputFields = function(parentElementID) {
	$("#" + parentElementID).find(":text").val('');
}

/**
 * Modifies the browser's address bar URL asynchronously (Won't work with IE
 * version below 10).
 * 
 * @param newPageTitle
 *            is the new page title for the browser
 * @param newURL
 *            is the new URL to be set in the address bar
 */
CLS.util.changeBrowserURL = function(newPageTitle, newURL) {
	history.pushState({}, newPageTitle, newURL);
}

/**
 * Gets the value of the given URL parameter.
 * 
 * @param sParam
 *            the parameter to get
 * @returns the value of the given url param
 */
CLS.util.getURLParameter = function(param) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for ( var i = 0; i < sURLVariables.length; i++) {
		var parameterName = sURLVariables[i].split('=');
		if (parameterName[0] == param) {
			return parameterName[1];
		}
	}
}

/**
 * Checks if the URL link has query parameters.
 */
CLS.util.isURLParameterized = function(url) {
	return ((url.indexOf("?") != -1) && ((url.indexOf("=")) != -1));
}

/**
 * Extracts all query parameters from the given URL string
 * 
 * @param string
 *            the source URL string
 * @returns a string containing all query parameters from the url
 */
CLS.util.extractQueryParams = function(string) {
	return string.substring(string.indexOf("?"));
}

/**
 * Extracts only the real URL part, ignoring the query parameters.
 * 
 * @returns the real URL part, without the query parameters
 */
CLS.util.getPureURL = function(string) {
	var endIndex = string.indexOf("?");
	return string.substring(0, endIndex);
}

/**
 * Returns the current date as a string in format: dd/mm/yyyy
 */
CLS.util.getCurrentDate = function() {
	var currentDate = new Date();
	var date = currentDate.getDate();
	if (date < 10) {
		date = "0" + date;
	}
	var month = currentDate.getMonth() + 1;
	if (month < 10) {
		month = "0" + month;
	}
	var year = currentDate.getFullYear();
	return date + "/" + month + "/" + year;
}

/**
 * Generates a URL (query) parameter with the given name, taking its value from
 * the input field with id - inputTextId.
 */
CLS.util.generateParam = function(initialString, paramName, inputTextId) {
	if (document.getElementById(inputTextId).value.length == 0) {
		return "";
	}
	// if the field contains multiple comma-separated params, replace the commas
	// with &paramName=
	var fieldParams = this.trim(document.getElementById(inputTextId).value)
			.replace(/\s*,\s*/g, "&" + paramName + "=");
	if (initialString == "?") {
		return paramName + "=" + fieldParams;
	} else {
		return "&" + paramName + "=" + fieldParams;
	}
}

/**
 * Generates a URL (query) date parameter with the given name, taking its value from
 * the input field with id - elementId and parsing it to ISO 8601
 */
CLS.util.generateDateParam = function (initialString,paramName,elementId) {
	var date = $("#"+elementId);
	if (date.val().length == 0) {
		return "";
	}
	var isoDate = date.datepicker("getDate").toJSON();
	if (initialString == "?") {
		return paramName + "=" + isoDate;
	} else {
		return "&" + paramName + "=" + isoDate;
	}
}

//REVIEW: document the function?
CLS.util.getISODate = function (element) {
	var value = $(element).val();
	if(value.length>0){
		return $(element).datepicker("getDate").toJSON();
	}else{
		return null;
	}
}

/** The date format used in CLS. */
CLS.util.dateFormat = "dd/mm/yy";

/** The time format used in CLS. */
CLS.util.timeFormat = "HH:00";

/**
 * Goes back from the window's history.
 */
CLS.util.goBack = function() {
	window.history.back();
}

/** Items displayed on a single page */
CLS.itemsOnPage = 5;

/**
 * Append additional parameters to the URL used for paginating
 */
CLS.appendPaginationParameters = function(url, pageNumber, itemsOnPage) {
	if (!pageNumber) {
		pageNumber = 1;
	}
	if (!itemsOnPage) {
		itemsOnPage = CLS.itemsOnPage;
	}
	
	var paginationParameters = "";
	if (CLS.util.isURLParameterized(url)) {
		paginationParameters += "&";
	} else {
		paginationParameters += "?";
	}
	paginationParameters += "offset=" + (pageNumber-1)*itemsOnPage + "&limit=" + itemsOnPage;
	
	var hookIndex = url.indexOf("#");
	if (hookIndex != -1) {
		url = url.substr(0, hookIndex) + paginationParameters + url.substr(hookIndex);
	} else {
		url += paginationParameters;
	}

	return url;
}

/**
 * Parses URL and obtain paginator current page from anchor element if such exists
 * @returns page number or 1
 */
CLS.getPaginatorCurrentPage = function(url) {
	var currentPage = 1;
	var pageHash = "#page-";
	var hashIndex = url.indexOf(pageHash);
	if (hashIndex != -1) {
		var pageNum = url.substr(hashIndex + pageHash.length);
		if (!isNaN(pageNum)) {
			currentPage = pageNum;
		}
	}
	return currentPage;
}

/**
 * Gets anchor value from URL if such exists
 * @returns anchor value or null
 */
CLS.util.getURLAnchorValue = function(url) {
	var anchorValue = null;
	var hashIndex = url.indexOf("#");
	if (hashIndex != -1) {
		anchorValue = url.substr(hashIndex+1);
	}
	return anchorValue;
}

/**
 * Toggles a collapsible element. If a button is provided then specific texts are applied to it.
 */
CLS.util.toggleDetails = function(toggler, button, expanded, collapsed) {
	if(button){
		if (toggler.is(".collapse")) {
			if (expanded) {
				button.html(expanded);
			} else {
				button.html("Collapse");
			}
		} else {
			if (collapsed) {
				button.html(collapsed);
			} else {
				button.html("Expand");
			}
		}
	}
	toggler.collapse("toggle");
}

/**
 * Checks the validity by comparing the provided date as string with the current date. If the provided date string is empty its considered valid.
 */
CLS.util.checkValidity = function (endDate) {
	if(endDate==""){
		return true;
	}
	var parts = endDate.split("/");
	var year = parts[2].split(" ");
	var time = year[1].split(":");
	
	var endD = new Date(year[0],parseInt(parts[1])-1,parts[0],time[0],time[1]);
	
	if(endD < new Date()){
		return false;
	}
	return true;
}

CLS.descriptionIdCounter = 0;

//REVIEW: document the function?
CLS.updateDropDownValues = function(url, dropdownId, defaultValue) {
	CLS.util.makeGetJsonRequest(url, function(data) {
		CLS.populateDropDown(data, dropdownId, defaultValue);
	});
}

//REVIEW: document the function?
CLS.populateDropDown = function(data, dropdownId, defaultValue){
	var dropdown = $("#" + dropdownId);
	var currentValue = defaultValue?defaultValue:dropdown.find(":selected").text();
	
	dropdown.html("");
	
	var emptyOption = $('<option/>');
	emptyOption.appendTo(dropdown);

	var results = data.results;
	for (var i in results) {
		var value = results[i].value;
		var option = $('<option/>').text(value).val(value);
		option.appendTo(dropdown);
	}
	
	dropdown.select2("val", currentValue);
}

/**
 * Wraps all code list details into a JS object, that can be easily converted to JSON
 *
 */
CLS.getCLObject = function () {
	var codeList = new Object();
	codeList.value = $("#cl_id").val();
	codeList.validFrom = CLS.util.getISODate("#cl_valid_from");
	codeList.validTo = CLS.util.getISODate("#cl_valid_to");
	codeList.displayType = $("#cl_display_type").val();
	codeList.sortBy = $("#cl_sort_by").val();
	codeList.masterValue = $("#cl_master").val();
	codeList.tenants = CLS.generateTenants();
	codeList.descriptions = CLS.getDesc("#cl-desc-table-body tr");
	codeList.extra1 = $("#cl_extra1").val();
	codeList.extra2 = $("#cl_extra2").val();
	codeList.extra3 = $("#cl_extra3").val();
	codeList.extra4 = $("#cl_extra4").val();
	codeList.extra5 = $("#cl_extra5").val();
	return codeList;
}

/**
 *	Navigates to the details page for the codelist with the given id.
 **/
CLS.goToCodelist = function (codelistId) {
	var newLocation = "cldetails.jsf?clID=" + codelistId;
	window.location.href = newLocation;
}

/**
 * Uses EMF's dialog functionality to create dialogs.
 */
CLS.confirmDialog = function(title, message, ok, cancel,width,height, okLabel, cancelLabel) {
	var config = new Object();
	config.title = title;
	config.message = message;
	if(ok){
		config.confirm=ok;
	}
	if(cancel){
		config.cancel=cancel;
	}
	if (width) {
		config.width = width;
	}
	if (height) {
		config.height = height;
	}
	if (okLabel){
		config.okBtn=okLabel;
	}
	if(cancelLabel){
		config.cancelBtn=cancelLabel;
	}
	return EMF.dialog.confirm(config);
}


/**
 * Generates an array of tenants to be sent to the server (will be used for multi-tenancy support).
 */
CLS.generateTenants = function() {
	var tenants = new Array();
	var tenant = new Object();
	tenant.name = $("#cl_tenant_name").val();
	//REVIEW: do we need at that point the tenant field to be auto-filled?
	tenant.contact = "tenant@tenant.com"
	tenants.push(tenant);
	return tenants;
}

//REVIEW: document the function?
CLS.getDesc = function(table) {
	var descriptions = new Array();
	var rows = $(table);
	for(var i=0;i<rows.length;i++){
		var row =$(rows[i]).find("td input");
		var description = new Object();
		description.description=row[1].value;
		description.comment=row[2].value;
		description.language=row[0].value;
		descriptions.push(description);
	}
	return descriptions;
}
