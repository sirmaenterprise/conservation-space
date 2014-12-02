
$(function() {
		$("#nav-search-btn").parent().addClass("active");
		CLS.util.displayAjaxLoader("code-values", "Loading...", false);
		//display all data for the code list at page load
		var codeListID = CLS.util.getURLParameter("clID");
		CLS.util.makeGetJsonRequest('/cls/service/codelists/' +
				codeListID + '?excludeValues=true',CLS.saveAndPopulateFields);

		//attach the date picker widgets
		var dateTimePickerOptions = {
				dateFormat : CLS.util.dateFormat,
				timeFormat: CLS.util.timeFormat,
				showMinute: false,
				beforeShow: function (input, inst) {
			        return !$(input).prop("readonly");
			    }
		};
		//attach the date picker widgets			
		$("#cl_valid_from").datetimepicker(dateTimePickerOptions);
		$("#cl_valid_to").datetimepicker(dateTimePickerOptions);
		$("#cv_valid_from").datetimepicker(dateTimePickerOptions);
		$("#cv_valid_to").datetimepicker(dateTimePickerOptions);
		$("#cv_valid_from_modal").datetimepicker(dateTimePickerOptions);
		$("#cv_valid_to_modal").datetimepicker(dateTimePickerOptions);
		
		$("#cl_display_type").select2();
		$("#cl_sort_by").select2();
		$("#cl_master").select2();
		$("#cv_master_modal").select2();
		
		$("#cl_tenant_name").on("change paste keyup", function(evt) {
			 CLS.updateDropDownValues(
						"/cls/service/codelists?excludeValues=true&responseFields=value&tenantName="
								+ $(evt.target).val(), "cl_master");
    	});
		$("#cv-add-btn").click(function(){
			CLS.toggleCVEdit(false);
			$('#cvDetailsModal').modal('show');
			$("#cv-save-btn").prop("disabled", true);
			CLS.toggleCVDetails(false);
		})
		
		$("#modalCloseButton").click(function(){
			CLS.cancelCVEdit(true);
		})

		//set captions
		$("#details-title").html("Details for codelist '" + codeListID+ "'");
		$("#search-title").html("Search for code values in codelist '" + codeListID+ "'");

		//attach click handler to the code values search button
		$("#cv-search-btn").click(function() {
			CLS.util.displayAjaxLoader("code-values", "Loading...", false);
			CLS.searchCodeValues("/cls/service/codelists/" + codeListID + 
					"/codevalues" + CLS.generateCvParamsList());
			// REVIEW: is this needed?
			document.getElementById('code-values').scrollIntoView();
	 	}); 
		
		// handle the click event of the clear button
		$("#cv-clear-btn").click(function() {
			CLS.util.resetAllInputFields("cv-search-form");
		});	
		$("#nav-back-btn").click(function() {
			javascript:window.location.href='clsearch.jsf';
		});	
		/*
		 * Toggles the edit functionality of the codelist details form.
		 */
		$("#cl-edit-btn").click(function() {
			CLS.toggleCLDetails();
		});	
		/*
		 * Toggles the edit functionality of the codevalue details modal window.
		 */
		$("#cv-edit-btn").click(function() {
			$("#cv-save-btn").prop("disabled", false);
			CLS.toggleCVDetails();
		});	
		/*
		 * Toggles when the save button in the codelist details form is clicked.
		 */
		$("#cl-save-btn").click(function() {
			CLS.saveCLEditDialog();
		});	
		$("#cv-save-btn").click(function(){
			if($("#cv-details-form").valid()) {
				if(CLS.modalCreate){
					CLS.confirmDialog(
						"Confirm operation",
						"Are you sure you want to continue with the operation <b>Save</b>?<br />This will create a new code value.",
						function(){
							CLS.createCV();
							$('#cvDetailsModal').modal('hide');
						},null,400,200,"Yes","No");
				} else {
					CLS.confirmDialog(
						"Confirm operation",
						"Are you sure you want to continue with the operation <b>Save</b>?<br />This will update the codelist data.",
						function(){
							CLS.saveCVEdit();
							$('#cvDetailsModal').modal('hide');
							},null,400,200,"Yes","No");
				}
				$(".ui-widget-overlay").css('z-index',1600);
				$(".ui-dialog").css('z-index',1601);
			}
		})
		
		/*
		 * Removes all rules and tooltips from form validation. 
		 * Otherwise it is possible previous errors to be displayed when open form again for different or new CV. 
		 */
		$('#cvDetailsModal').on('hide.bs.modal', function () {
			  $("#cv-details-form").validate().removeAllRules();
		});
		
		/*
		 * When the modal window of the codevalue details is hidden, any editing
		 * operations on it are cancelled.
		 */
		$('#cvDetailsModal').on('hidden.bs.modal', function () {
			  CLS.toggleCVDetails(true);
		});
//		
//		$("#cl-details-form").submit(function (data,handler){
//			CLS.validateCLData(data,handler);
//		});
		
		$("#cl-cancel-btn").click(function() {
			CLS.cancelCLEdit();
		});	
		
		//handle the click event of the collapse toggle button
		$("#cl-collapse-toggler").click( function () {
			CLS.collapseCLDetails();
		});
		
		$("#cv-search-btn-collapser").click(function() {
			CLS.toggleCVSearch();
		});
		
		$("#cv-collapse-toggler").click(function() {
			CLS.toggleCVAdvancedSearch();
		});
		
		$("#cv-cancel-btn").click(function(){
			CLS.cancelCVEdit();
		});
		
		$('#cvDetailsModal').on('shown.bs.modal', function (e) {
			if (CLS.modalCreate) {
				CLS.initCVCreateValidator(function(){
					$("#cv-save-btn").prop("disabled", false);
				}, function(){
					$("#cv-save-btn").prop("disabled", true);
				});
			} 
		});

});

	//REVIEW: Document the function
	CLS.saveCLEdit = function () {
		if(CLS.validateCLData()){
			var jsonData = CLS.getCLObject()
			//TODO use the ajax executor from emf-core-web?
			//REVIEW: Use done()/fail() instead of success/error?
			$.ajax ({
		        type: "POST",
		        url: "/cls/service/codelists/" + jsonData.value,
		        contentType : 'application/json',
		        async: false,
		        data: JSON.stringify(jsonData),
  				success: function () {
  					CLS.toggleCLDetails();
	        		CLS.util.makeGetJsonRequest('/cls/service/codelists/' +
	        			CLS.util.getURLParameter("clID") + '/?excludeValues=true',CLS.saveAndPopulatefields);
  				},
  				error: function (jqXHR) {
  					//TODO: 
  					CLS.confirmDialog("Error","The update could not be processed.",null,null,400,250,"Ok","");
  				}
		    })
		}
	}

	//REVIEW: Document the function
	CLS.saveCVEdit = function () {
		if(CLS.validateCLData()){
			var jsonData = CLS.getCVObject()
			//TODO use the ajax executor from emf-core-web?
			$.ajax ({
		        type: "POST",
		        url: "/cls/service/codelists/codevalue/" + jsonData.value,
		        contentType : 'application/json',
		        async: false,
		        data: JSON.stringify(jsonData),
				success: function () {
					 var anchor = CLS.util.getURLAnchorValue(window.location.href);
					 
					CLS.searchCodeValues("/cls/service/codelists/" + CLS.currentCodeList.results[0].value  + 
							"/codevalues" + CLS.generateCvParamsList() + (anchor?"#"+anchor:""));
  				},
  				error: function (jqXHR) {
  					//TODO: 
  					CLS.confirmDialog("Error","The update could not be processed.",null,null,400,250,"Ok","");
  				}
		    })
			//TODO: Reload the page?
			//TODO: Display AJAX loadeR?
			//CLS.toggleCLDetails();
		}
	}
	
//REVIEW: Document the function
CLS.createCV = function () {
	if(CLS.validateCLData()){ 
		var jsonData = CLS.getCVObject()
		//TODO use the ajax executor from emf-core-web?
		$.ajax ({
	        type: "PUT",
	        url: "/cls/service/codelists/"+ CLS.currentCodeList.results[0].value + "/codevalues/",
	        contentType : 'application/json',
	        async: false,
	        data: JSON.stringify(jsonData),
			success: function () {
				 var anchor = CLS.util.getURLAnchorValue(window.location.href);
				 
				CLS.searchCodeValues("/cls/service/codelists/" + CLS.currentCodeList.results[0].value  + 
						"/codevalues" + CLS.generateCvParamsList() + (anchor?"#"+anchor:""));
				},
				error: function (jqXHR) {
					//TODO: 
					CLS.confirmDialog("Error","The update could not be processed.",null,null,400,250,"Ok","");
				}
	    })
		//TODO: Reload the page?
		//TODO: Display AJAX loadeR?
		//CLS.toggleCLDetails();
	}
}

	//REVIEW: Document the function
	CLS.getCVObject = function () {
		var object = new Object();
		object.value = $("#cv_id_modal").val();
		object.validFrom = CLS.util.getISODate("#cv_valid_from_modal");
		object.validTo = CLS.util.getISODate("#cv_valid_to_modal");
		object.codeListId = $("#cv_parent_modal").val();
		object.masterValue = $("#cv_master_modal").val();
		object.descriptions = CLS.getDesc("#cv-desc-table-body tr");
		object.order=$("#order_modal").val();
		object.extra1 = $("#cv_extra1_modal").val();
		object.extra2 = $("#cv_extra2_modal").val();
		object.extra3 = $("#cv_extra3_modal").val();
		object.extra4 = $("#cv_extra4_modal").val();
		object.extra5 = $("#cv_extra5_modal").val();
		return object;
	}
	
	//REVIEW: Why is this needed?
	CLS.validateCLData = function () {
		return true;
	}

var CLS = CLS || {};

	/** The current code list in preview. */
	CLS.currentCodeList;
	CLS.currentCodeValue;
	CLS.modalCreate;
	CLS.cvEditing;
	CLS.masterCodeValues;
	
	/**
	 * Saves the data of the currently retrieved code value in a global variable before
	 * populating the fields. The variable is later used when repopulating the
	 * fields, usually after cancelling an edit operation.
	 * 
	 * @param dataObj the code value to be saved in the global variable
	 */
	CLS.saveAndPopulateCVFields = function(dataObj){
		CLS.currentCodeValue=dataObj;
		CLS.populateCVFields(dataObj);
		// Initialize validator
		CLS.initCVEditValidator(function(){
			$("#cv-save-btn").prop("disabled", false);
		}, function(){
			$("#cv-save-btn").prop("disabled", true);
		});
		var validTo = $("#cv_valid_to_modal").val();
		  var valid = CLS.util.checkValidity(validTo);
			  $("#cv-edit-btn").prop("disabled", !valid);
	}

	/**
	 * Displays a modal window with the code value data.
	 * 
	 * @param rawObj is the JSON object with the Code value data 
	 */
	CLS.populateCVFields = function (rawObj) {
		cvObj = rawObj.results[0];
		$("#cv_id_modal").val(cvObj.value);
		CLS.util.setDate(cvObj.validFrom,"#cv_valid_from_modal");
		CLS.util.setDate(cvObj.validTo,"#cv_valid_to_modal");
		
		// Checks the code list validity + the codevalue validity.
		var valid = CLS.util.checkValidity($("#cl_valid_to").val());
		valid = valid && CLS.util.checkValidity($("#cv_valid_to_modal").val());
		$("#cv-edit-btn").prop("disabled", !valid);
		
		var rows;
		 for(var i = 0; i < cvObj.descriptions.length; i++) {
		    var descObj = cvObj.descriptions[i]; 
		    var description="";
		    var comment="";
		    var language="";
		    if(descObj.description !== undefined){
		    	description = descObj.description;
		    }
		    if(descObj.comment !== undefined){
		    	comment=descObj.comment;
		    }
		    if(descObj.language !== undefined){
		    	language=descObj.language;
		    }
		    if(language == "BG" || language == "EN"){
		    	rows +=CLS.generateDescriptionTableRow(description,comment,language, false, true);
		    } else {
		    	 rows +=CLS.generateDescriptionTableRow(description,comment,language, false, false);
		    }
		  }
		 $('#cv-desc-table-body').empty();
		 $('#cv-desc-table-body').append(rows);
		 $("#cv_parent_modal").val(cvObj.codeListId);
		 CLS.populateDropDown(CLS.masterCodeValues, "cv_master_modal", cvObj.masterValue);
		 $("#order_modal").val(cvObj.order);
		 $("#cv_extra1_modal").val(cvObj.extra1);
		 $("#cv_extra2_modal").val(cvObj.extra2);
		 $("#cv_extra3_modal").val(cvObj.extra3);
		 $("#cv_extra4_modal").val(cvObj.extra4);
		 $("#cv_extra5_modal").val(cvObj.extra5);
}

	/**
	 * Handles the click event of each concrete code value row from the table
	 * 
	 * @param codeListId is the ID of the parent code list of this code value
	 * @param cvCode is the code value
	 * @param from is the validity start date (needed to retreive the proper cv version)
	 * @param to is the validity end date
	 */
	CLS.handleCVClick = function (codeListId, cvCode, from, to) {
		CLS.toggleCVEdit(true);
		var dateParams = "";
		if(from !== "undefined") {
			dateParams= "&from=" + from;
		}
		if(to !== "undefined") {
			dateParams= dateParams + "&to=" + to;
		}
		CLS.util.makeGetJsonRequest("/cls/service/codelists/" +
				 codeListId + "/codevalues?id="+cvCode + dateParams, 
				 CLS.saveAndPopulateCVFields);
	}
	
	CLS.saveAndPopulateFields = function(dataObj){
		  CLS.currentCodeList=dataObj;
		  CLS.populateCLFields(dataObj);
		  //CLS.displayCodeValues(dataObj.results[0]); 
			var updateURL="/cls/service/codelists/"+dataObj.results[0].masterValue+"/codevalues?responseFields=value";
			CLS.util.makeGetJsonRequest(updateURL, function(data) {
				CLS.masterCodeValues = data;
			});
		  var validTo = $("#cl_valid_to").val();
		  var valid = CLS.util.checkValidity(validTo);
			  $("#cl-edit-btn").prop("disabled", !valid);
			  $("#cv-add-btn").prop("disabled",!valid);

	}

	/**
	 * Parses the json response and populates all codelist details fields on the
	 * page
	 */
	CLS.populateCLFields = function (dataObj) {
		 codeList = dataObj.results[0];
		 $("#cl_id").val(codeList.value);
		 $("#cl_tenant_name").val(codeList.tenants[0].name);
		 CLS.util.setDate(codeList.validFrom,"#cl_valid_from");
		 CLS.util.setDate(codeList.validTo,"#cl_valid_to");
		 $("#cl_display_type").select2("val", codeList.displayType);
		 $("#cl_sort_by").select2("val", codeList.sortBy);
		 //$("#cl_master").val(codeList.masterValue);
		 $("#cl_extra1").val(codeList.extra1);
		 $("#cl_extra2").val(codeList.extra2);
		 $("#cl_extra3").val(codeList.extra3);
		 $("#cl_extra4").val(codeList.extra4);
		 $("#cl_extra5").val(codeList.extra5);
		//Set widths to make the table non-resizable ! 
		 //divs inside the TD's to make the editable table work in IE...
		 $("#cl-desc-table-body").empty();
		 var rows;
		 for(var i = 0; i < codeList.descriptions.length; i++) {
		    var descObj = codeList.descriptions[i];  
		    var description="";
		    var comment="";
		    var language="";
		    if(descObj.description !== undefined){
		    	description = descObj.description;
		    }
		    if(descObj.comment !== undefined){
		    	comment=descObj.comment;
		    }
		    if(descObj.language !== undefined){
		    	language=descObj.language;
		    }
		    if(language == "BG" || language == "EN"){
		    	rows +=CLS.generateDescriptionTableRow(description,comment,language, false, true);
		    } else {
		    	 rows +=CLS.generateDescriptionTableRow(description,comment,language, false, false);
		    }
		  }
		 $('#cl-desc-table-body').append(rows);
//		 $("#cl_comment").val(CLS.currentCodeList.descriptions[1].description);
		 //list all code values for this CL in a table
		 //Perform initial search for code values
		 var anchor = CLS.util.getURLAnchorValue(window.location.href);
		 CLS.searchCodeValues("/cls/service/codelists/" + codeList.value + 
				 "/codevalues" + CLS.generateCvParamsList() + (anchor?"#"+anchor:""));
		 CLS.updateDropDownValues(
					"/cls/service/codelists?excludeValues=true&responseFields=value&tenantName="
							+ codeList.tenants[0].name, "cl_master", codeList.masterValue);
	}
	

	
	
	CLS.searchCodeValues = function(url) {
		// Obtain current page via url anchor
		var currentPage = CLS.getPaginatorCurrentPage(url);
		CLS.util.makeGetJsonRequest(CLS.appendPaginationParameters(url, currentPage, CLS.itemsOnPage), function(dataObj){
			$("#code-values-paginator").pagination({
				items: dataObj.total,
				currentPage: currentPage,
		        itemsOnPage: CLS.itemsOnPage,
		        cssStyle: "compact-theme",
		        onPageClick: function(pageNumber, event) {
		        	//CLS.util.displayAjaxLoader("code-values", "Loading...", false);
		        	CLS.util.makeGetJsonRequest(CLS.appendPaginationParameters(url, pageNumber, CLS.itemsOnPage), 
		        			function(dataObj) {
		        				CLS.displayCodeValues(dataObj);
		        				$("html, body").animate({ scrollTop: $(document).height() }, 1000);
		        			});
		        }
			});
			CLS.displayCodeValues(dataObj);
		});
	}
	
	CLS.util.setDate = function (date, elementID) {
		 if (date != null) {
			 $(elementID).datetimepicker('setDate', new Date(date));
		 } else {
			 $(elementID).val("");
		 }
	}
	
	CLS.util.formatDate = function (dateString) {
		 if (dateString != null) {
			 var date = new Date(dateString);
			 return $.datepicker.formatDate(CLS.util.dateFormat, date)+" "+$.datepicker.formatTime(CLS.util.timeFormat, { hour:date.getHours()});
		 } else {
			 //TODO: ?
			return "";
		 }
	}
	
	/**
	*	Lists all code values for this Code list in a table.
	**/
	CLS.displayCodeValues = function (dataObj) {
		$("#code-values").html("");
		var table = "<table id='results-table' class='table table-hover table-bordered table-striped'>" + 
	      "<thead><tr class='info'><th style='width: 20%'>Code</th>" + 
	      "<th style='width: 60%'>Description (bg)</th>" + 
	      "<th style='width: 10%'>Valid from</th>" + 
	      "<th style='width: 10%'>Valid to</th>" + 
	      "</tr>" + 
	      "</thead>" + 
	      "<tbody>";
		  //the returned json object may be a Codelist object (with an attribute array 'codeValues')
		  //or the codeValues array itself. Handle the both situations

	      var codeValuesObj = dataObj.results;
	      $("#codevalues-title").html("Code values (" + dataObj.total + ")");
		  for(var i = 0; i < codeValuesObj.length; i++) {
			    var cvObj = codeValuesObj[i];   
			     table+= "<tr data-toggle='modal' data-target='#cvDetailsModal' cl-id="+ 
			     cvObj.codeListId +" cv-id=" +cvObj.value +" cv-from="+ cvObj.validFrom +" cv-to=" +
			     cvObj.validTo + " style='cursor: pointer'>"
				 + "</td><td>" + cvObj.value 
				 + "</td><td>" + cvObj.descriptions[0].description  
				 + "</td><td>" + CLS.util.formatDate(cvObj.validFrom)
				 + "</td><td>" + CLS.util.formatDate(cvObj.validTo)
				 + "</td></tr>";
		  }
		  table+= "</tbody></table>";
		  $("#code-values").append(table);
		  //attach a click handler for each table row
		  $("#results-table").find("tbody").find("tr").click(function() {
			  CLS.handleCVClick($(this).attr('cl-id'),$(this).attr('cv-id'),
					  $(this).attr('cv-from'),$(this).attr('cv-to'));
			  CLS.toggleCVDetails(true);
		  });
	}

	/**
	 * Constructs the string of parameters that gets appended to the URL for the
	 * code values search.
	 */
	CLS.generateCvParamsList = function () {
		var parameters = "?";
		parameters+= CLS.util.generateParam(parameters,"codeListID", "cl_id");
		parameters+= CLS.util.generateParam(parameters,"id", "cv_id");
		parameters+= CLS.util.generateParam(parameters,"description", "cv_desc");
		parameters+= CLS.util.generateParam(parameters,"masterValue", "cv_master");	
		parameters+= CLS.util.generateDateParam(parameters,"from", "cv_valid_from");		
		parameters+= CLS.util.generateDateParam(parameters,"to", "cv_valid_to");
		parameters+= CLS.util.generateParam(parameters,"extra1", "cv_extra1");
		parameters+= CLS.util.generateParam(parameters,"extra2", "cv_extra2");	
		parameters+= CLS.util.generateParam(parameters,"extra3", "cv_extra3");	
		parameters+= CLS.util.generateParam(parameters,"extra4", "cv_extra4");
		parameters+= CLS.util.generateParam(parameters,"extra5", "cv_extra5");
		parameters+= CLS.util.generateParam(parameters,"comment", "cv_comment");
		return encodeURI(parameters);	
	}
	
	CLS.cancelCLEdit = function() {
		CLS.confirmDialog(
			"Confirm operation",
			"Are you sure you want to stop editing?<br /> Your changes will be lost.",
			function() {
				$("#cl-details-form").validate().removeErrors();
				CLS.populateCLFields(CLS.currentCodeList);
				CLS.toggleCLDetails();
		},null,400,200,"Yes","No");
	}
	
	CLS.saveCLEditDialog = function() {
		if($("#cl-details-form").valid()) {
			CLS.confirmDialog(
				"Confirm operation",
				"Are you sure you want to continue with the operation <b>Save</b>?<br />This will update the codelist data.",
				CLS.saveCLEdit,null,400,200,"Yes","No");
		}
	}
	
	CLS.collapseCLDetails = function () {
		CLS.util.toggleDetails($("#cl-collapsed-details"),$("#cl-collapse-toggler"),"Show less details","Show more details");
	}
	
	CLS.toggleCVSearch = function () {
		CLS.util.toggleDetails($("#cv-search-container"),$("#cv-search-btn-collapser"),"Hide code value search","Search code values");
	}

	CLS.toggleCVAdvancedSearch = function () {
		CLS.util.toggleDetails($("#cv-advanced-search"),$("#cv-collapse-toggler"),"Show less search criteria ","Show more search criteria");
	}
	
	/**
	 * Toggles the Code list details form between editable and not editable. If no
	 * state is specified, the state is determined from the disabled property of
	 * the codelist edit button.
	 * 
	 * @param state the state of the details form
	 */
	CLS.toggleCLDetails = function(state) {
		if(arguments.length==0){
			state = $("#cl-edit-btn").prop("disabled");
		}
		if (!state) {
			$("#cl-save-btn").prop("disabled", false);
			CLS.initCLEditValidator(function(){
				$("#cl-save-btn").prop("disabled", false);
			}, function(){
				$("#cl-save-btn").prop("disabled", true);
			});
		}
	
		$("#cl_valid_from").prop("readonly", state);
		$("#cl_valid_to").prop("readonly", state);
		$("#cl_tenant_name").prop("readonly", state);
	
		$("#cl_display_type").prop("disabled", state);
		$("#cl_sort_by").prop("disabled", state);
		$("#cl_master").prop("disabled", state);
	
		$("#cl_desc").prop("readonly", state);
		$("#cl_extra1").prop("readonly", state);
		$("#cl_extra2").prop("readonly", state);
		$("#cl_extra3").prop("readonly", state);
		$("#cl_extra4").prop("readonly", state);
		$("#cl_extra5").prop("readonly", state);
		$("#cl_comment").prop("readonly", state);
		CLS.toggleTable(state, "#cl-desc-table");
		$("#cl-edit-btn").prop("disabled", !state);
		if(state){
			$("#cl-save-btn").hide();
			$("#cl-cancel-btn").hide();
		}else{
			$("#cl-save-btn").show();
			$("#cl-cancel-btn").show();
		}
		
		$("#cl-collapse-toggler").prop("disabled", !state);
	
		if ($("#cl-collapsed-details").is(".collapse")) {
			CLS.collapseCLDetails();
		}
	}
	/**
	 * Toggles the Code value details form between editable and not editable. If no
	 * state is specified, the state is determined from the disabled property of
	 * the codevalue edit button.
	 * 
	 * @param state the state of the details form
	 */
	CLS.toggleCVDetails = function(state) {
		
		if(arguments.length==0){
			state = $("#cv-edit-btn").prop("disabled");
		}
		$("#cv_valid_from_modal").prop("readonly", state);
		$("#cv_valid_to_modal").prop("readonly", state);
	
		$("#cv_master_modal").prop("disabled", state);
		$("#order_modal").prop("readonly", state);
		$("#cv_extra1_modal").prop("readonly", state);
		$("#cv_extra2_modal").prop("readonly", state);
		$("#cv_extra3_modal").prop("readonly", state);
		$("#cv_extra4_modal").prop("readonly", state);
		$("#cv_extra5_modal").prop("readonly", state);
		CLS.toggleTable(state, "#cv-desc-table");
		$("#cv-edit-btn").prop("disabled", !state);
		if(state){
			CLS.cvEditing = false;
			$("#cv-save-btn").hide();
			$("#cv-cancel-btn").hide();
		}else{
			CLS.cvEditing = true;
			$("#cv-save-btn").show();
			$("#cv-cancel-btn").show();
		}
	}
	

	CLS.toggleCVEdit = function(state){
	
		$("#cv_id_modal").val("");
		$("#cv_valid_from_modal").val("");
		$("#cv_valid_to_modal").val("");
		 $('#cv-desc-table-body').empty();
			var rows = "";
			rows += CLS.generateDescriptionTableRow("","","BG", false, true);
			rows += CLS.generateDescriptionTableRow("","","EN", false, true);
			$("#cv-desc-table-body").append(rows);
		 $("#cv_parent_modal").val(CLS.currentCodeList.results[0].value);
		 CLS.populateDropDown(CLS.masterCodeValues, "cv_master_modal");
		 $("#order_modal").val("");
		 $("#cv_extra1_modal").val("");
		 $("#cv_extra2_modal").val("");
		 $("#cv_extra3_modal").val("");
		 $("#cv_extra4_modal").val("");
		 $("#cv_extra5_modal").val("");
		$('#cv_id_modal').prop("disabled",state);
		$('#cv_id_modal').prop("readonly",state);
		if (!state){
			CLS.modalCreate=true;
			$('#cv-edit-btn').hide();
			$("#cv_id_modal").addClass("required-field");
			$("#cv_valid_from_modal").addClass("required-field");
		} else {
			CLS.modalCreate=false;
			CLS.toggleCVDetails(false);
			$('#cv-edit-btn').show();
			$("#cv_id_modal").removeClass("required-field");
			$("#cv_valid_from_modal").removeClass("required-field");
		}
	}
	
	CLS.cancelCVEdit = function(closeModal) {
		if (CLS.modalCreate) {
			CLS.confirmDialog(
				"Confirm operation",
				"Are you sure you want to cancel the operation <b>Create code value</b><br /> Your changes will be lost?",
				function() { 
					$("#cv-details-form").validate().removeErrors();
					$('#cvDetailsModal').modal('hide'); 
				}, 
				null, 400, 200, "Yes", "No");
		} else if (CLS.cvEditing) {
			CLS.confirmDialog(
				"Confirm operation",
				"Are you sure you want to cancel the operation <b>Create code value</b><br /> Your changes will be lost?",
				function() { 
					$("#cv-details-form").validate().removeErrors();
					CLS.toggleCVDetails();
					CLS.populateCVFields(CLS.currentCodeValue);
					if (closeModal) {
						$('#cvDetailsModal').modal('hide');
					}
				}, 
				null, 400, 200, "Yes", "No");
		} else if (closeModal) {
			$('#cvDetailsModal').modal('hide');
		}

		$(".ui-widget-overlay").css('z-index',1600);
		$(".ui-dialog").css('z-index',1601);
	}
	