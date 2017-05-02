$(function() {
			$("#nav-search-btn").parent().addClass("active");
			CLS.updateSiteTitle();

			var dateTimePickerOptions = {
				dateFormat : CLS.util.dateFormat,
				timeFormat: CLS.util.timeFormat,
				showMinute: false,
				defaultDate: null
			};

			$("#cl_master").select2();
			//attach the date picker widgets
			$("#cl_valid_from").datetimepicker(dateTimePickerOptions);
			$("#cl_valid_to").datetimepicker(dateTimePickerOptions);
			//auto fill valid_from, valid_to with the current date
			$("#cl_valid_from").datetimepicker("setDate", new Date());
			$("#cl_valid_to").datetimepicker("setDate", new Date());
			//populate the master CL drop-down
			 CLS.updateDropDownValues(EMF.config.baseURL + "service/codelists?excludeValues=true&responseFields=value",
					 "cl_master", "");

			//initiate search on page load, after the from/to dates have been populated
			var searchParams = CLS.generateClParamsList();
			var finalURL = EMF.config.baseURL + "service/codelists" + searchParams;
			CLS.util.displayAjaxLoader("search-results", "Loading...", false);
			CLS.searchCodeLists(finalURL);

			//handle the click event of the collapse toggle button
			$("#cl-collapse-toggler").click(function() {
				 CLS.util.toggleDetails($("#cl-advanced-search"),
						$("#cl-collapse-toggler"), "Show less search criteria",
						"Show more search criteria");
			});
			//handle the click event of the search button
			$("#cl-search-btn").click(
					function() {
						//construct the string of query params for the search
						var searchParams = CLS.generateClParamsList();
						var finalURL = EMF.config.baseURL + "service/codelists" + searchParams;
						CLS.util.displayAjaxLoader("search-results", "Loading...",
								false);
						//make the GET request
						CLS.searchCodeLists(finalURL);
						//asynchronously append the generated search params to the current page URL to allow bookmarking
						if (CLS.util.isURLParameterized(finalURL)) {
							var pureURL = CLS.util.getPureURL(document.URL);
							CLS.util.changeBrowserURL("Codelist search results",
									pureURL + searchParams);
						}
			});
			//handle the click event of the clear button
			$("#cl-clear-btn").click(function() {
				CLS.util.resetTextInputsWithin("cl-search-form");
			});
});

var CLS = CLS || {};

/**
 *  Constructs the string of query parameters that gets appended to the URL for the code lists search.
 **/

CLS.generateClParamsList = function () {
	var parameters = "?";
	parameters += "excludeValues=true";
	parameters += CLS.util.generateParam(parameters, "id", "cl_id");
	parameters += CLS.util.generateParam(parameters, "description", "cl_desc");
	parameters += CLS.util.generateDateParam(parameters, "from", "cl_valid_from");
	parameters += CLS.util.generateDateParam(parameters, "to", "cl_valid_to");
	parameters += CLS.util.generateParam(parameters, "masterValue", "cl_master");
	parameters += CLS.util.generateParam(parameters, "extra1", "cl_extra1");
	parameters += CLS.util.generateParam(parameters, "extra2", "cl_extra2");
	parameters += CLS.util.generateParam(parameters, "extra3", "cl_extra3");
	parameters += CLS.util.generateParam(parameters, "extra4", "cl_extra4");
	parameters += CLS.util.generateParam(parameters, "extra5", "cl_extra5");
	parameters += CLS.util.generateParam(parameters, "comment", "cl_comment");
	return encodeURI(parameters);
}

/**
 * Performs initial search for codelists and initialize the paginator
 */
CLS.searchCodeLists = function(url) {
	// Obtain current page via url anchor
	var currentPage = CLS.getPaginatorCurrentPage(url);
	CLS.util.makeGetJsonRequest(CLS.appendPaginationParameters(url, currentPage, CLS.itemsOnPage), function(dataObj){
		$("#search-results-paginator").pagination({
			items: dataObj.total,
			currentPage: currentPage,
	        itemsOnPage: CLS.itemsOnPage,
	        cssStyle: "compact-theme",
	        onPageClick: function(pageNumber, event) {
	        	//CLS.util.displayAjaxLoader("search-results", "Loading...", false);
	        	CLS.util.makeGetJsonRequest(CLS.appendPaginationParameters(url, pageNumber, CLS.itemsOnPage), CLS.listCodeLists);
	        }
		});
		CLS.listCodeLists(dataObj);
	});
}

/**
 * Dynamically constructs a table listing all code list search results.
 *
 * @param dataObj -  a JS object resulting from the server's JSON response
 **/
CLS.listCodeLists = function(dataObj) {
	$("#search-results").html("<h3>Found codelists (" + dataObj.total + ")</h3>");
	var table = "<table id='results-table' class='table table-hover table-bordered table-striped'>"
			+ "<thead><tr><th>Codelist ID</th>"
			+ "<th>Codelist description (en)</th>"
			+ "<th></th></tr></thead><tbody>";

	var results = dataObj.results;
	for (var i = 0; i < results.length; i++) {
		var clObj = results[i];
		table += "<tr>"
				+ "<td class='col-md-4'>" + clObj.value + "</td>" 
				+ "<td class='col-md-6'>" + CLS.util.getDescriptionByLanguage(clObj.descriptions, CLS.LANGUAGE_CODE_EN) + "</td>" 
				+ "<td class='col-md-1'><button data-cl=" + clObj.value + " type='button' class='btn btn-sm btn-primary cl-details-btn'>"
				+ _emfLabels['codevalue.details.button.label'] + "</button></td></tr>";
	}
	table += "</tbody></table>";
	$("#search-results").append(table);
	//attach a click handler for each table row
	$("#results-table").find(".cl-details-btn").click(function() {
		CLS.goToCodelist($(this).attr('data-cl'));
	})
}