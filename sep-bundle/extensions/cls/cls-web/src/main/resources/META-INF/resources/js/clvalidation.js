/**
 * Validates datepicker field's date to be later than current date. Field is considered valid if there is no value.
 */
jQuery.validator.addMethod("laterThanCurrentDate", function(value, element) {
	var toDate = $(element).datepicker("getDate");
	var now = new Date();
	return !toDate || toDate > now;
}, "Must be greater than current date.");

/**
 * Validates datepicker field's date to be later than another datepicker field.
 * Field is considered valid if there is no value.
 * 
 * @param fromFieldId the id of the another datepicker field
 */
jQuery.validator.addMethod("laterThanFromDate", function(value, element, fromFieldId) {
	var toDate = $(element).datepicker("getDate");
	var fromDate =  $("#" + fromFieldId).datepicker("getDate");
	return !toDate || toDate > fromDate;
}, "Validity end date must be greater than validity start date.");

/**
 * Custom validator method which validates a field value against regular expression.
 */
jQuery.validator.addMethod("regex", function(value, element, regexp) {
    var re = new RegExp(regexp);
    return this.optional(element) || re.test(value);
}, "Check your input.");

$.extend(true, $.validator.prototype, {addRules: function(locator, rules) {}});
$.extend(true, $.validator.prototype, {silentValid: function() {}});

CLS.initCLEditValidator = function(onSuccess, onError) {
	var rules = {
		cl_valid_from: {
			required: true
		},
		cl_valid_to: {
			laterThanCurrentDate: true,
			laterThanFromDate: "cl_valid_from"
		}
	};

	$("#cl-details-form input[id^='description_']").each(function() {
		var elemId = $(this).attr("id");
		rules[elemId] = {required: true};
	});

	return CLS.initValidator("cl-details-form", rules, undefined, onSuccess, onError);
}

CLS.initCLCreateValidator = function(onSuccess, onError) {
	var rules = {
		cl_id: {
			required: true,
			regex: "^[a-zA-Z][a-zA-Z0-9_]*$",
			remote: {
				url: "/cls/service/codelists?excludeValues=true&responseFields=value",
				type: "GET",
				cache: false,
				dataFilter: function(data) {
					var json = JSON.parse(data);
					var result = json.results;
					if (result) {
						var inputValue = $("#cl_id").val();
						for(var k in result) {
							if (inputValue == result[k].value) {
								return false;
							}
						}
					}
					return true;
				}
			}
		},
		cl_valid_from: {
			required: true
		},
		cl_valid_to: {
			laterThanCurrentDate: true,
			laterThanFromDate: "cl_valid_from"
		}
	};
	
	$("#cl-details-form input[id^='description_']").each(function() {
		var elemId = $(this).attr("id");
		rules[elemId] = {required: true};
	});
	
	var messages = {
		cl_id: {
			regex: "Must start with a letter. No special characters allowed.",
			remote: "There is already a code list with the same ID."
		}
	};
	return CLS.initValidator("cl-details-form", rules, messages, onSuccess, onError);
}

CLS.initCVEditValidator = function(onSuccess, onError) {
	var rules = {
		cv_valid_from_modal: {
			required: true,
			laterThanCurrentDate: true
		},
		cv_valid_to_modal: {
			laterThanCurrentDate: true,
			laterThanFromDate: "cv_valid_from_modal"
		}
	};

	$("#cv-details-form input[id^='description_']").each(function() {
		var elemId = $(this).attr("id");
		rules[elemId] = {required: true};
	});
	return CLS.initValidator("cv-details-form", rules, undefined, onSuccess, onError);
}

CLS.initCVCreateValidator = function(onSuccess, onError) {
	var rules = {
		cv_id_modal: {
			required: true,
			remote: {
				url: "/cls/service/codelists/3/codevalues?responseFields=value",
				type: "GET",
				cache: false,
				dataFilter: function(data) {
					var json = JSON.parse(data);
					var result = json.results;
					if (result) {
						var inputValue = $("#cv_id_modal").val();
						for(var k in result) {
							if (inputValue == result[k].value) {
								return false;
							}
						}
					}
					return true;
				}
			}	
		},			
		cv_valid_from_modal: {
			required: true,
			laterThanCurrentDate: true
		},
		cv_valid_to_modal: {
			laterThanCurrentDate: true,
			laterThanFromDate: "cv_valid_from_modal"
		}
	};

	$("#cv-details-form input[id^='description_']").each(function() {
		var elemId = $(this).attr("id");
		rules[elemId] = {required: true};
	});
	
	var messages = {
		cv_id_modal: {
			remote: "There is already a code value with the same ID."
		}
	};
	return CLS.initValidator("cv-details-form", rules, messages, onSuccess, onError);
}
	
CLS.initValidator = function(formId, rules, messages, onSuccess, onError) {
	$("#" + formId).removeData("validator");
    var validator = $("#" + formId).validate({
        rules: rules,
        messages: messages,
        errorElement: "div",
        errorClass: "invalid-field",
        errorPlacement: function (error, element) {
        	var errorMsg = $(error).text();
        	$(element).tooltip("destroy");
        	$(element).tooltip({ title : errorMsg}).tooltip("show");
        	// Hide tooltips after a short period. Remove timeout if mouse goes over the element!
        	/*window.setTimeout(function() {
        		$(element).tooltip('hide');
        	}, 3000);*/
        },
        highlight: function (element, errorClass) {
        	$(element).removeClass("required-field");
            $(element).addClass(errorClass);
        },
        unhighlight: function (element, errorClass) {
        	$(element).tooltip("destroy");
        	$(element).removeClass(errorClass);
        },
        success: function(label, element) {
        	$(element).tooltip("destroy");
        	$(element).removeClass(this.errorClass);
        },
        
        showErrors: function(errorMap, errorList) {
        	if (validator.showAllErrors) {
        		this.defaultShowErrors();
        	}
        }
    });
    
    validator.addRules =  function(locator, rules) {
    	$(locator).each(function () {
		    $(this).rules('add', rules);
		    $(this).on("change paste keyup", function(evt) {
		    	$("#"+evt.target.id).removeClass("required-field");
	    		validator.element("#"+evt.target.id);
	    		validator.postValidate();
	    	});
		});
    	
    	validator.silentValid();
    };
    /**
     * Removes all rules from form inputs and all tooltips.
     */
    validator.removeAllRules = function() {
    	$("form#" + formId + " :input").each(function(){
   		 	$(this).rules("remove");
   		 	$(this).tooltip("destroy");
    	});
    };
    
    /**
     * Performs silent validation. Validates the form but does not show error messages.
     */
    validator.silentValid = function() {
        // Perform initial validation but don't show errors.
        validator.showAllErrors = false;
        $("#" + formId).valid();
        validator.showAllErrors = true;
        validator.postValidate();
    };

    validator.removeErrors = function() {
    	var rules = this.settings.rules;
    	for (var k in rules) {
	        if (rules.hasOwnProperty(k)) {
	        	$("#"+k).tooltip("destroy");
	        	$("#"+k).removeClass("invalid-field");
	        }
	    }
    };
    
    validator.onSuccess = onSuccess;
    validator.onError = onError;
    
    /**
     * Called after validation. If everything is OK calls onSuccess function. Otherwise calls onError.
     */
    validator.postValidate = function() {
    	if (this.numberOfInvalids() == 0)  {
    		if(this.onSuccess && typeof(this.onSuccess) == "function") {
    			this.onSuccess();
    		}
        } else {
        	if(this.onError && typeof(this.onError) == "function") {
    			this.onError();
    		}
        }
    };
    
    // Add onkeyup events to all fields declared in rules
    for (var k in rules) {
        if (rules.hasOwnProperty(k)) {
	    	$("#"+k).on("change paste keyup", function(evt) {
	    		$("#"+evt.target.id).removeClass("required-field");
	    		validator.element("#"+evt.target.id);
	    		validator.postValidate();
	    	});
        }
    }
    
    validator.silentValid();

    return validator;
}
