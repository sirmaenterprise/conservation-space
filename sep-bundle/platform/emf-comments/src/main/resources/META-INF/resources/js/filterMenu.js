/**
 * Function that is invoked on page load.
 */
function onLoad() {
	var datePickerSettings = {
		changeMonth		: true,
		changeYear 		: true,
   		dateFormat		: SF.config.dateFormatPattern,
   		firstDay 		: SF.config.firstDay,
   		monthNames 		: SF.config.monthNames,
   		monthNamesShort	: SF.config.monthNamesShort,
   		dayNames		: SF.config.dayNames,
   		dayNamesMin 	: SF.config.dayNamesMin,
   		numberOfMonths	: 1
	};
	
	var fromDate = $("#fromDate");
	var toDate = $("#toDate");

	fromDate.datepicker($.extend({ 
		onClose: function(selectedDate) {
			toDate.datepicker("option", "minDate", selectedDate);
		}
	}, datePickerSettings));
	
	toDate.datepicker($.extend({ 
		onClose: function(selectedDate) {
			fromDate.datepicker("option", "maxDate", selectedDate);
		}
	}, datePickerSettings));
    
    $.ajax({
		type : 'GET',
		url : EMF.applicationPath + '/service/user',
		data : {},
		async : false,
		complete : function(res) {
		    var users = $.parseJSON(res.responseText);
		    for ( var i = 0; i < users.length; i++) {
		/*	$("#author").append(
				"<option value=\"" + users[i].displayName + "\">"
					+ users[i].displayName + "</option>");*/
		    }
		}
    });
    $(".tabs-head>input[type=radio]").click(function() {
    	doFilter();
    });
    EMF.effects.flyOut();
    EMF.effects.selectOne();
   
}

function hideFilterSortContent() {
    $('.tabs .tabs-content').css('display', 'none');
    //$('.tabs .tabs-head ul li').removeClass();
}

function prepare() {
    var filter = {};
    if ($("#author") != null) {
	filter.user = $("#author").val();
    } else {
	filter.user = "";
    }
    if ($("#keyword") != null) {
	filter.keyword = $("#keyword").val();
    } else {
	filter.keyword = "";
    }
    if ($("#fromDate").length) {
    	filter.dateFrom = $("#fromDate").datepicker("getDate");
    } else {
    	filter.dateFrom = "";
    }
    if ($("#toDate").length) {
    	filter.dateTo = $("#toDate").datepicker("getDate");
    } else {
    	filter.dateTo = "";
    }
    if ($("#filterCat") != null) {
	filter.category = $("#filterCat").val();
    } else {
	filter.category = "";
    }
    if ($("#filterStat") != null) {
	filter.status = $("#filterStat").val();
    } else {
	filter.status = "";
    }
    var tagsElement = $('.filterPanel .tags-filter');
    if (tagsElement.length) {
    	filter.tags = tagsElement.select2('val');
    } else {
    	filter.tags = [ ];
    }
    if ($("#filterInstanceType") != null) {
	filter.instanceType = $("#filterInstanceType").val();
    } else {
	filter.instanceType = "";
    }
    if ($("#sortByCreation").is(':checked')) {
	filter.sortBy = $("#sortByCreation").attr("id");
    } else {
	filter.sortBy = $("#sortByComment").attr("id");
    }
    if ($(".tabs-head .activeFilter").size() > 0) {
	$(".tabs-head .activeFilter")[0].click();
    }
    $.comments.updateCriteria(filter);
   
}

function buildTree() {
    $(".idoc-comments").html("");
    $.comments.refresh();
    EMF.effects.flyOut();
}

function cancelFilter() {
    hideFilterSortContent();
    //buildTree();
    $($.comments.target).trigger('commentTopicSizeChange', false);
}

function resetFilter() {
    $("#author").val('');
    $("#keyword").val('');
    $("#fromDate").val('');
    $("#toDate").val('');
    $("#filterCat").val('');
    $("#filterStat").val('');
    $("#filterInstanceType").val('');
    $(".tags-filter .select2-search-choice-close").click();
   // buildTree();
    $($.comments.target).trigger('commentTopicSizeChange', false);
}

function doFilter() {
    prepare();
    buildTree();
    $($.comments.target).trigger('commentTopicSizeChange', true);
}

function cancelSort() {
    $("#sortByComment").click();
    prepare();
   // buildTree();
}

function doSort() {
    prepare();
    buildTree();
}

/**
 * Sets from date field value
 * @param daysFromToday - an integer indicating how many days from today should be the date. Can be positive or negative or zero for today.
 */
function commentsFilterSetFromDate(daysFromToday) {
	if (!daysFromToday) {
		daysFromToday = 0;
	}
	if ($("#fromDate").datepicker()) {
		$("#fromDate").datepicker('setDate', daysFromToday);
	}
	prepare();
}

onLoad();