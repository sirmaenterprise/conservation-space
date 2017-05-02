$( function() {
	$("#nav-clcreate-btn").parent().addClass("active");
	CLS.updateSiteTitle();
	var rows = "";
	rows += CLS.generateDescriptionTableRow("","","EN", false, true);
	$('#cl-desc-table-body').append(rows);
	CLS.toggleTable(false, "#cl-desc-table");

	$("#cl-save-btn").prop("disabled", true);
	// Initialize validator for creating a codelist
	CLS.initCLCreateValidator(function(){
		$("#cl-save-btn").prop("disabled", false);
	}, function(){
		$("#cl-save-btn").prop("disabled", true);
	});

	var dateTimePickerOptions = {
		dateFormat : "dd/mm/yy",
		timeFormat: "HH:00",
		showMinute: false,
		beforeShow: function (input, inst) {
			return !$(input).prop("readonly");
		}
	};

	//attach the date picker widgets
	$("#cl_valid_from").datetimepicker(dateTimePickerOptions);
	$("#cl_valid_to").datetimepicker(dateTimePickerOptions);

	//auto fill the valid-form field with the current date
	$("#cl_valid_from").datetimepicker("setDate", new Date());

	$("#cl_display_type").select2();
	$("#cl_sort_by").select2();
	$("#cl_master").select2();

	$("#cl_id").addClass("required-field");
	$("#cl_valid_from").addClass("required-field");

	$("#cl-cancel-btn").click(function (){
		CLS.confirmDialog(
				"Confirm operation",
				"Are you sure you want to cancel the operation <b>Create code list</b><br /> Your changes will be lost?",
				function() {
					window.history.back();
				},
				null, 400, 200, "Yes", "No");
	});

	$("#cl-save-btn").click(function() {
		CLS.createCodeList();
	});
	//fill the values of the 'CL master' drop-down menu
	 CLS.updateDropDownValues(EMF.config.baseURL + "service/codelists?excludeValues=true&responseFields=value",
			 "cl_master", "");
})

var CLS = CLS || {};

CLS.createCodeList = function () {
	var clId = $("#cl_id").val();
	//REVIEW: The use of success/error is obsolete. Use done()/fail() instead?
	var createCLFunction = function() {
		var jsonData = CLS.getCLObject();
		$.ajax ({
	        type: "PUT",
	        url: EMF.config.baseURL + "service/codelists",
	        contentType : 'application/json',
	        async: false,
	        data: JSON.stringify(jsonData),
				success: function () {
					CLS.goToCodelist(clId);
				},
				error: function (jqXHR) {
					//TODO:
					CLS.confirmDialog("Error","The operation could not be processed.",null,null,400,250,"Ok","");
				}
	    });
	}

	if($("#cl-details-form").valid()) {
		CLS.confirmDialog(
			"Confirm operation",
			"Are you sure you want to continue with the operation <b>Save</b>?<br />This will create the codelist.",
			createCLFunction, null, 400, 200, "Yes", "No");
	}
}

