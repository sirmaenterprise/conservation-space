var shapes = [];
var allAnnotationShapes = [];

var templateNavigation = '\
<li id="id%ID%" class="image">\
	<div class="flyout-section">\
		<div style="cursor:default;" id="slider_image_%ID%">\
		</div>\
		<div style="cursor:default;" class="controls">\
			<div class="control">\
				<span class="label">Visibility</span>\
				<div id="image_visibility_%ID%" class="field switch">\
					<label class="cb-enable selected">On</label>\
					<label class="cb-disable">Off</label>\
					<input type="hidden" value="1" name="field2" class="switch-value"/>\
				</div>\
			</div>\
			<div class="control clearfix">\
				<span class="label">Opacity</span>\
				<div class="slider ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all" id="slider_opacity_%ID%">\
					<div class="visibility" style="width:100%;"/>\
					<a href="javascript:void(0);" class="ui-slider-handle ui-state-default ui-corner-all" style="left:100%;"/>\
				</div>\
			</div>\
			<div class="control clearfix">\
				<span class="label">Contrast</span>\
				<div class="slider ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all" id="slider_contrast_%ID%">\
					<div class="visibility" style="width:0%;"/>\
					<a href="javascript:void(0);" class="ui-slider-handle ui-state-default ui-corner-all" style="left:0%;"/>\
				</div>\
			</div>\
			<div class="control clearfix">\
				<span class="label">Brightness</span>\
				<div class="slider ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all" id="slider_brightness_%ID%">\
					<div class="visibility" style="width:100%;"/>\
					<a href="javascript:void(0);" class="ui-slider-handle ui-state-default ui-corner-all" style="left:100%;"/>\
				</div>\
			</div>\
		</div><a class="dark-buttongrad" href="javascript:void(0);">Change Image</a>\
	</div>\
</li>\
';

/**
 * Gets URL parameter.
 *
 * @param strParamName
 *                is the parameter name
 * @param strHref
 *                is the URL
 * @returns URL parameter value
 */
function getParam(strParamName, strHref) {
    strHref = strHref || window.location.href;
    if (strHref.indexOf("?") > -1) {
	var strQueryString = strHref.substr(strHref.indexOf("?") + 1);
	var aQueryString = strQueryString.split("&");
	for ( var iParam = 0; iParam < aQueryString.length; iParam++) {
	    var arr = aQueryString[iParam].split("=");
	    if (arr[0].toLowerCase() == strParamName.toLowerCase()) {
		var aParam = arr[1];
		try {
		    return unescape(decodeURIComponent(aParam));
		} catch (err) {
		    return unescape(aParam);
		}
	    }
	}
    }
    return undefined;
}

/**
 * Gets multiple URL parameters.
 *
 * @param name
 *                is the name of the parameter
 * @returns array of parameters
 */
function getURLParams(name) {
    var i = 0;
    var result = [];
    while (getParam(name + i) != undefined) {
	result.push(getParam(name + i));
	i = i + 1;
    }
    return result;
}

/**
 * Gets outer HTML of a tag.
 *
 * @param node
 *                is the tag data
 * @returns outer HTML
 */
function outerHTML(node) {
    return node.outerHTML || new XMLSerializer().serializeToString(node);
}

/**
 * Sets height of the panel with all comments.
 */
function setTreePaneHeight() {
    var treeHeight = svgSpace.normalY - $(".data-annotation-head").height();
    var height = 40;
    $("#treePane").css("height", treeHeight);
    $("#treePane").css("max-height", treeHeight);
    $("#treePane").css("width", $(".comments-wrapper").width() - 10);
    $(".data-annotation-head").css("height", height);
    $(".annotation.data-annotation").css("height", treeHeight + 2 * height);
    $(".tabs .tabs-content table").css("border-collapse", "separate");
    $(".tabs .tabs-content table").css("border-spacing", "10px");
    $(".tabs-head ul li").css("padding", "0px");
    $(".image-annotation-container").css("padding-bottom", "0px");
}

/**
 * Function that is invoked on page load.
 */
function onLoad() {
    setTreePaneHeight();
    $('div.data-annotation .tabs-head li').click(function() {
    	setTreePaneHeight();
    });
    $("#fullscreen").click(function() {
		$("span.object-details span.object-facets-column").css("display", "none");
		$("#topHeader,#header,.context-data-header").css("display", "none");
		$("#documentRelations").css("display", "none");
		$("div.object-preview").css("width", "98%");
		$("#normalscreen").css("display", "block");
		$("#fullscreen").css("display", "none");
		$("#zoomer").css("width", "80px");
		setFullScreen();
		$("#zoom_panel").css("position", "absolute");
		var top = $("#frontimage").attr("y");
		top += $("#frontimage").attr("height");
		top += 60;
		$("#zoom_panel").css("top", top + "px");
		var left = $("#frontimage").attr("x");
		left += $("#frontimage").attr("width") / 2;
		left += -60;
		$("#zoom_panel").css("left", left + "px");
		$("#zoom_panel").css("width", "150px");
		$("#zoom_panel").css("color", "white");
	});
    
    $("#normalscreen").click(function() {
		$("span.object-details span.object-facets-column").css("display", "block");
		$("#topHeader,#header,.context-data-header").css("display", "block");
		$("#documentRelations").css("display", "block");
		$("div.object-preview").css("width", "");
		$("#normalscreen").css("display", "none");
		$("#fullscreen").css("display", "");
		$("#zoomer").css("width", "80%");
		setNormalScreen();
		$("#zoom_panel").css("position", "relative");
		$("#zoom_panel").css("top", "0px");
		$("#zoom_panel").css("left", "0px");
		$("#zoom_panel").css("width", "100%");
		$("#zoom_panel").css("color", "black");
		$("#" + info_panel).css("visibility", "hidden");
	});
    
    var colorPickerConfig = {
    	color: "#ECC",
    	showAlpha: true,
    	className: "full-spectrum",
    	showInitial: true,
    	showPalette: true,
    	showSelectionPalette: true,
    	maxPaletteSize: 10,
    	preferredFormat: "hex",
    	localStorageKey: "spectrum.iat",
    	change: function() {
    		
    	},
    	palette: [
    	          ["rgb(0, 0, 0)", "rgb(67, 67, 67)", "rgb(102, 102, 102)",
                "rgb(204, 204, 204)", "rgb(217, 217, 217)","rgb(255, 255, 255)"],
                ["rgb(152, 0, 0)", "rgb(255, 0, 0)", "rgb(255, 153, 0)", "rgb(255, 255, 0)", "rgb(0, 255, 0)",
                "rgb(0, 255, 255)", "rgb(74, 134, 232)", "rgb(0, 0, 255)", "rgb(153, 0, 255)", "rgb(255, 0, 255)"], 
                ["rgb(230, 184, 175)", "rgb(244, 204, 204)", "rgb(252, 229, 205)", "rgb(255, 242, 204)", "rgb(217, 234, 211)", 
                "rgb(208, 224, 227)", "rgb(201, 218, 248)", "rgb(207, 226, 243)", "rgb(217, 210, 233)", "rgb(234, 209, 220)", 
                "rgb(221, 126, 107)", "rgb(234, 153, 153)", "rgb(249, 203, 156)", "rgb(255, 229, 153)", "rgb(182, 215, 168)", 
                "rgb(162, 196, 201)", "rgb(164, 194, 244)", "rgb(159, 197, 232)", "rgb(180, 167, 214)", "rgb(213, 166, 189)", 
                "rgb(204, 65, 37)", "rgb(224, 102, 102)", "rgb(246, 178, 107)", "rgb(255, 217, 102)", "rgb(147, 196, 125)", 
                "rgb(118, 165, 175)", "rgb(109, 158, 235)", "rgb(111, 168, 220)", "rgb(142, 124, 195)", "rgb(194, 123, 160)",
                "rgb(166, 28, 0)", "rgb(204, 0, 0)", "rgb(230, 145, 56)", "rgb(241, 194, 50)", "rgb(106, 168, 79)",
                "rgb(69, 129, 142)", "rgb(60, 120, 216)", "rgb(61, 133, 198)", "rgb(103, 78, 167)", "rgb(166, 77, 121)",
                "rgb(91, 15, 0)", "rgb(102, 0, 0)", "rgb(120, 63, 4)", "rgb(127, 96, 0)", "rgb(39, 78, 19)", 
                "rgb(12, 52, 61)", "rgb(28, 69, 135)", "rgb(7, 55, 99)", "rgb(32, 18, 77)", "rgb(76, 17, 48)"]
    	]
	};
    
    var bucketColorPickerConfig = $.extend({ }, colorPickerConfig, {
    	change: function(color) {
    		svgSpace.fillColor = color;
    	}
    });
    var borderColorPickerConfig = $.extend({ }, colorPickerConfig, {
    	change: function(color) {
    		svgSpace.strokeColor = color;
    	}
    });
    
    var fillColorPicker = $('.color-picker.color-picker-fill');
    fillColorPicker.spectrum(bucketColorPickerConfig);
    
    var borderColorPicker = $('.color-picker.color-picker-border');
    borderColorPicker.spectrum(borderColorPickerConfig);
    
    svgSpace.setFillColor(fillColorPicker.spectrum('get'));
    svgSpace.setStrokeColor(borderColorPicker.spectrum('get'));
    
    var tooltipHtml = '<u>Left click</u> to set the fill color.<br /> \
    				   <u>Right click</u> or <u>Shift + Left click</u> to set the border color.';
    
    $('.palette.tooltip').html(tooltipHtml);
    
    $('#tool_stroke_type').select2({
    	width: '100%',
    	height: '24px',
    	minimumResultsForSearch: 10
    });
    
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
    	
    var data = '<ul id="overlayDrag" style="width:100%;">';
    for (i = 0; i < svgSpace.images.length; i++) {
	data += templateNavigation.replace(/%ID%/g, i);
    }
    data += '</ul>';
    $("div.add-overlay").before(data);

//    $.comments.rootId = imageId;
//    $.comments.rootId = EMF.documentContext.currentInstanceId;
//    $.comments.labels = $.extend($.comments.labels, {
//    	deleteTopicConfirmationMessage: 'Are you sure you want to delete this annotation?'
//    });
//	$.comments.rootType = EMF.documentContext.currentInstanceType;
//    $.comments.imagePathURL = svgSpace.rootPath + "/document/images";
//    $.comments.loadService = svgSpace.rootPath + '/service/comment';
//    $.comments.saveService = svgSpace.rootPath + '/service/comment';
//    $.comments.editService = svgSpace.rootPath + '/service/comment';
//    $.comments.removeService = svgSpace.rootPath + '/service/comment/remove';
//    $.comments.source = '#svgcontent';
//    $.comments.target = '.comments';
//    $.comments.tagsPlaceholder = 'Tags';
//    $.comments.viewSubmitButton = false;

    function allowShapeCreation(topic) {
    	svgSpace.createdItem = null;
    }
    
//    $.comments.beforeCreateTopic = function(topic) {
//		if($(".scene3d").size()>0){
//			topic.shape = {};
//		    topic.shape.svgTag = "<x3d/>";
//		    topic.shape.zoomLevel = "1";
//			topic.shape.viewbox = "" + JSON.stringify(animationView.getView());
//		}else{
//	    	if (!topic.shape) {
//	    		return;
//	    	}
//	    	var svgTag = topic.shape.svgTag;
//	    	var elementId = $(svgTag).attr('id');
//	    	var element = document.getElementById(elementId);
//	    	svgTag = outerHTML(element);
//	    	svgTag = stripTag(svgTag);
//		    topic.shape.svgTag = svgTag.replace(/"/g, "'");
//		    topic.shape.zoomLevel = zoomFactor;
//		    topic.shape.viewbox = getStepX() + "," + getStepY()
//			    + "," + getViewBoxX() + "," + getViewBoxY();
//		}
//    }
    
//    $.comments.afterCreateTopic = allowShapeCreation;
//    $.comments.cancelCreateTopic = allowShapeCreation;
//    
//    $.comments.commentSelectors = '#nothing';
//    $($.comments.target).on('postSaveAction', function(event, comment) {
//    	buildTree();
//    });
//    
//    $($.comments.target).on('postCancelAction', function() {
//    	buildTree();
//    });
//    $($.comments.target).on('postLoad', function(event, comments) {
//	for ( var int = 0; int < comments.length; int++) {
//	    if (comments[int].shape && comments[int].shape.svgTag) {
//		shapes.push(comments[int]);
//	    }
//	}
//    });
//    $($.comments.target).on('postUnload', function(event, comments) {
//	var filter = false;
//	for ( var i = 0; i < comments.length; i++) {
//	    for ( var j = 0; j < shapes.length; j++) {
//		if (comments[i] == shapes[j]) {
//		    if (!comments[i].parentId) {
//			filter = true;
//		    }
//		    shapes.splice(j, 1);
//		}
//	    }
//	    for ( var j = 0; j < shapes.length; j++) {
//		if (comments[i] == shapes[j]) {
//		    if (!comments[i].parentId) {
//			filter = true;
//		    }
//		    shapes.splice(j, 1);
//		}
//	    }
//	}
//	if (filter) {
//	    doFilter();
//	}
//    });
//
//    $.comments.init();
//    $.comments.refresh();
//
//    $.ajax({
//	type : 'GET',
//	url : svgSpace.rootPath + '/service/user',
//	data : {},
//	async : false,
//	complete : function(res) {
//	    var users = $.parseJSON(res.responseText);
//	    for ( var i = 0; i < users.length; i++) {
//		$("#author").append(
//			"<option value=\"" + users[i].displayName + "\">"
//				+ users[i].displayName + "</option>");
//	    }
//	}
//    });

    loadImage(0, true);
    $("#overlayDrag").sortable(
	    {
		cursor : 'move',
		opacity : '0.6',
		handle : 'img',
		start : function(event, ui) {
		    var liItems = $("#overlayDrag li").length;
		    var sum = 0;
		    for (i = 0; i < liItems; i++) {
			sum += $("#overlayDrag li#id" + i).height();
		    }
		    if ($("#overlayDrag.validHeight").length == 0) {
			$("#overlayDrag").addClass("validHeight");
			$("#overlayDrag").css("height", sum + "px");
		    }
		},
		stop : function(event, ui) {
		    var rows = $("#overlayDrag").sortable('toArray');
		    for ( var i = 0; i < rows.length; i++) {
			var tmp = rows[i].toString();
			tmp = tmp.substring("id".length) * 1;
			svgSpace.orders[i] = tmp;
		    }
		    svgCanvas.setSvgString(zoomFigures(0, 0, 1, svgCanvas
			    .getSvgString().toString()));
		}
	    });
    $(".comp-1 a").click(function() {
	$(".flydown-handle.overlayMenu").addClass("disabled");
	svgSpace.viewAllImages = false;
	zoom(0, 0, 1);
    });
    $(".comp-2 a").click(function() {
	$(".flydown-handle.overlayMenu").removeClass("disabled");
	svgSpace.viewAllImages = true;
	zoom(0, 0, 1);
    });
    $("ul.radioButtons > li > a").on('mousedown', function() {
		$(this).parent().parent().find("li").removeClass("active");
		$(this).parent().addClass("active");
    });
    $("a#addAnnotation").click(
	    function() {
		if ($('[stroke-width="5"]').length > 0) {
		    var element = $('[stroke-width="5"]')[0];
		    var temp = outerHTML(element);
		    temp = stripTag(temp);
		    args.svgTag = temp.replace(/"/g, "'");
		} else {
		    args.svgTag = "";
		    $('[stroke-width="5"]').attr('stroke-width', '');
		}
		args.imageId = imageId;
		args.imageUri = image;
		args.zoomLevel = zoomFactor;
		args.viewbox = getStepX() + "," + getStepY() + ","
			+ getViewBoxX() + "," + getViewBoxY();
		openDialog(args);
	    });
    $(".tabs-head>input[type=radio]").click(function() {
//	doFilter();
    });
    EMF.effects.flyOut();
    EMF.effects.selectOne();

//    $($.comments.target).on(
//	    'commentGenerationFinish',
//	    function() {
//		$(".rowSelected").removeClass("rowSelected");
//		var selectShape = function() {
//		    var divId = $(this).parents(".comment").attr("id");
//		    divId = divId.substring(0, divId
//			    .indexOf($.comments.commentSuffix));
//		    var currShape = null;
//		    for (i = 0; i < shapes.length; i++) {
//			if (shapes[i].id == divId) {
//			    currShape = shapes[i].shape;
//			    break;
//			}
//		    }
//			if($(".scene3d").size()>0){
//				animationView.animateView([JSON.parse(currShape.viewbox)]);
//			    $(this).parents(".comment").addClass(rowSelected);
//				return;
//			}
//		    if (currShape) {
//			var viewbox = $.trim(currShape.viewbox).split(",");
//			var zoomLevel = $.trim(currShape.zoomLevel);
//			var x = $.trim(viewbox[0]) / (maxZoom[0] / zoomLevel);
//			var y = $.trim(viewbox[1]) / (maxZoom[0] / zoomLevel);
//			var width = $.trim(viewbox[2])
//				/ (maxZoom[0] / zoomLevel);
//			var height = $.trim(viewbox[3])
//				/ (maxZoom[0] / zoomLevel);
//			focusImage(x, y, width, height, zoomLevel * 1);
//			var id = $.trim(currShape.svgTag);
//			if (id.length > 0) {
//			    id = id.substring(id.indexOf(" id='") + 5);
//			    id = id.substring(0, id.indexOf("'"));
//			    select(id);
//			} else {
//			    $('[stroke-width="5"]').attr('stroke-width', '');
//			}
//		    }
//		    $("." + rowSelected).removeClass(rowSelected);
//		};
//		$(".action").click(selectShape);
//	    });

    $("#page").append($(".object-facets-column").find(".comment-box"));
    tinymce
	    .init({
		selector : ".comment-editor",
		plugins : [ "lists link image anchor internal_thumbnail_link" ],
		toolbar : "bold italic | bullist numlist | link image | internal_link | internal_thumbnail_link",
		menubar : false,
		statusbar : false
	    });
    $("#" + info_panel).addClass("tooltip");
    $(CMF.config.mainContainerId).tipTip({
	defaultPosition : 'top',
	tooltipActivator : 'path,line,ellipse,rect'
    });
    
    var showFreeTextEditor = function(svgElementId, mouse_x, mouse_y) { // Annotation tool
		var editorId = svgElementId + '_editor',
			editorElement = $('<div id="' + editorId + '" class="iat-free-text-editor"><div class="emf-tiny-editor" style="width: 100%; height: 100%;"><p><br data-mce-bogus="1" /></p></div></div>'),
			
			bodyElement = $('body'),
			svgImageElement = $('#' + svgElementId),
			initalText;
		
		var svgCanvasOffset = $('#svgcanvas').offset();
		
		if (svgImageElement.length) {
			var imagePosition = svgImageElement.position();
			mouse_x = imagePosition.left;
			mouse_y = imagePosition.top;
			initalText = svgSpace.getFreeTextHtml(svgElementId);
			svgImageElement.hide();
		}
		
		editorElement.css({
		    position:  'absolute',
		    top:       mouse_y + svgCanvasOffset.top,
		    left:      mouse_x + svgCanvasOffset.left
		});
		
		editorElement.appendTo(bodyElement);
		
		var _svgcanvas = svgCanvas;
		var createImageFn = function(elementData) {
			var imageElement = _svgcanvas.addSvgElementFromJson(elementData);
			return imageElement;
		}
		
		tinymce.init({
		    selector: '#' + editorId + ' div',
		    inline: true,
		    toolbar: 'fontselect sizeselect fontsizeselect | bold italic | alignleft alignright alignjustify | forecolor backcolor | insertEditorAsImage cancelInsertEditorAsImage',
		    menubar: false,
		    plugins: "lists paste textcolor svgEditIntegration",
		    forced_root_block : false,
		    force_p_newlines : true,
		    setup: function(editor) {
								    	
		    	editor.svgEdit = {
		    		nextId: svgElementId,
		    		oldImageElement: svgImageElement,
		    		svgSpace: svgSpace,
		    		mousex: mouse_x,
		    		mousey: mouse_y,
		    		createImageFn: createImageFn
			    }
		    	
				editor.on('init', function() {
					if (initalText) {
						editor.setContent(initalText);
					}
					editor.focus(false);
				});
				
				/**
				 * Stops propagation of 'key' events, otherwise they get handled 
				 * by the image editor and nothing happens in tinymce
				 */
				editor.on('keypress keyup keydown', function(event) {
					event.stopPropagation();
				});
		    }
		});
	}

    var editFunc = function() {
		var id = $("[stroke-width='5']").attr("id");
		if (id.indexOf(rsprefix) === 0) {
			svgSpace.createdItem = null;
			svgSpace.editItem = id;
			
			$(".editRegion").css("display", "none");
			$(".saveRegion").css("display", "inline-block");
			$(".cancelRegion").css("display", "inline-block");
			
			var target = $('#' + id);
			if (target.prop('tagName') === 'image') {
				showFreeTextEditor(target.prop('id'));
			}
		}
    };
    
    $(".editRegion").click(editFunc);
    
    var saveFunc = function() {
		$(".region li").removeClass("active");
		
		var _document 	= document,
			id 			= svgSpace.editItem,
			shape;
		
		
		shape = svgSpace.getAnnotationData(_document.getElementById(id));
		
		$(_document)
			.trigger('iat:save-annotation', {
				elementId: id,
				shape: shape
			});
		
		svgSpace.createdItem = null;
		svgSpace.editItem = null;
		svgCanvas.clearSelection();

		$(".editRegion").css("display", "inline-block");
		$(".saveRegion").css("display", "none");
		$(".cancelRegion").css("display", "none");
    };
    $(".saveRegion").click(saveFunc);
    var cancelFunc = function() {
		$(".region li").removeClass("active");
		svgSpace.createdItem = null;
		svgSpace.editItem = null;
		doFilter();
		$(".editRegion").css("display", "inline-block");
		$(".saveRegion").css("display", "none");
		$(".cancelRegion").css("display", "none");
    };
    $(".cancelRegion").click(cancelFunc);

    if ($.browser.msie) {
		setTimeout(function() {
		    $("#zoomer").css("top", "-12px");
		    $("#zoomer a").css("top", "6px");
		}, 1000);
    }
}

function hideFilterSortContent() {
    $('.tabs .tabs-content').css('display', 'none');
    $('.tabs .tabs-head ul li').removeClass();
}

function prepare() {
    var filter = {};
    if ($("#author").length) {
    	filter.user = $("#author").val();
    } else {
    	filter.user = "";
    }
    if ($("#keyword").length) {
    	filter.keyword = $("#keyword").val();
    } else {
    	filter.keyword = "";
    }
    if ($("#fromDate").length) {
    	filter.dateFrom = $("#fromDate").datepicker('getDate');
    } else {
    	filter.dateFrom = "";
    }
    if ($("#toDate").length) {
    	filter.dateTo = $("#toDate").datepicker('getDate');
    } else {
    	filter.dateTo = "";
    }
    if ($("#filterCat").length) {
    	filter.category = $("#filterCat").val();
    } else {
    	filter.category = "";
    }
    var tagsElement = $('.filterPanel .tags-filter');
    if (tagsElement.length) {
    	filter.tags = tagsElement.select2('val');
    } else {
    	filter.tags = [ ];
    }
    if ($("#filterStat").length) {
    	filter.status = $("#filterStat").val();
    } else {
    	filter.status = "";
    }
    if ($(".tabs-head input:checked").attr("value") == "current") {
    	filter.zoomLevel = zoomFactor;
    } else if ($(".tabs-head input:checked").attr("value") == "all") {
    	filter.zoomLevel = "";
    } else {
    	filter.zoomLevel = -1;
    }
    if ($("#sortByCreation").is(':checked')) {
    	filter.sortBy = $("#sortByCreation").attr("id");
    } else {
    	filter.sortBy = $("#sortByComment").attr("id");
    }
    if ($(".tabs-head .activeFilter").size() > 0) {
    	$(".tabs-head .activeFilter")[0].click();
    }
//    $.comments.updateCriteria(filter);
}

function openDialog(shape, initalText) {
    svgCanvas.setMode("resize");
//    $.comments.showCommentBox(imageId, null, true, shape, initalText);
    
    $(document)
    	.trigger('emf-comments:dom:create-topic', {
    		content: {
	    		shape: shape,
	    		content: initalText
    		},
    		popupMode: isFullScreen
    	});
}

function buildTree() {
//    $("#treePane").html("");
////    $.comments.refresh();
//    shapes = [];
//    for ( var commentKey in $.comments.filteredMap) {
//	var comments = $.comments.filteredMap[commentKey];
//	for ( var i = 0; i < comments.length; i++) {
//	    if (comments[i].shape && comments[i].shape.svgTag) {
//		shapes.push(comments[i]);
//	    }
//	}
//    }
//    allAnnotationShapes = [];
//    for ( var commentKey in $.comments.map) {
//	var comments = $.comments.map[commentKey];
//	for ( var i = 0; i < comments.length; i++) {
//	    if (comments[i].shape && comments[i].shape.svgTag) {
//		allAnnotationShapes.push(comments[i]);
//	    }
//	}
//    }
//    $(".arrow a span").click();
//    if (this.svgCanvas != undefined) {
//	var data = svgCanvas.getSvgString();
//	data = clearShapes(data);
//	svgCanvas.setSvgString(data);
//    }
//    var shapeMap = {};
//    for (i = 0; i < shapes.length; i++) {
//	if (shapes[i].shape.svgTag.length > 0) {
//	    var idPrefix = " id='";
//	    var currName = shapes[i].shape.svgTag
//		    .substring(shapes[i].shape.svgTag.indexOf(idPrefix)
//			    + idPrefix.length);
//	    currName = currName.substring(0, currName.indexOf("'"));
//	    shapeMap[currName] = shapes[i].shape.svgTag;
//	}
//    }
//    var tmp = new Array();
//    for ( var prop in shapeMap) {
//	tmp.push(shapeMap[prop]);
//    }
//    addShapes(tmp);
//    var clickDiv = function() {
//	var divId = $(this).attr("id");
//	divId = divId.substring(0, divId.indexOf($.comments.commentSuffix));
//	var currShape = null;
//	for (i = 0; i < shapes.length; i++) {
//	    if (shapes[i].id == divId) {
//		currShape = shapes[i].shape;
//		break;
//	    }
//	}
//	if (currShape != null) {
//	    var viewbox = $.trim(currShape.viewbox).split(",");
//	    var zoomLevel = $.trim(currShape.zoomLevel);
//	    var x = $.trim(viewbox[0]) / (maxZoom[0] / zoomLevel);
//	    var y = $.trim(viewbox[1]) / (maxZoom[0] / zoomLevel);
//	    var width = $.trim(viewbox[2]) / (maxZoom[0] / zoomLevel);
//	    var height = $.trim(viewbox[3]) / (maxZoom[0] / zoomLevel);
//	    focusImage(x, y, width, height, zoomLevel * 1);
//	    var id = $.trim(currShape.svgTag);
//	    if (id.length > 0) {
//		id = id.substring(id.indexOf(" id='") + 5);
//		id = id.substring(0, id.indexOf("'"));
//		select(id);
//	    } else {
//		$('[stroke-width="5"]').attr('stroke-width', '');
//	    }
//	} else {
//	    $('[stroke-width="5"]').attr('stroke-width', '');
//	}
//	$("." + rowSelected).removeClass(rowSelected);
//	$(this).addClass(rowSelected);
//    };
//    $("div#treePane table").unbind('click', clickDiv);
//    $("div#treePane table").bind('click', clickDiv);
//    EMF.effects.flyOut();
}

function cancelFilter() {
    hideFilterSortContent();
    resetFilter();
    prepare();
}

function resetFilter() {
    $("#author").val('');
    $("#keyword").val('');
    $("#fromDate").val('');
    $("#toDate").val('');
    $("#filterCat").val('');
    $("#filterStat").val('');
    $('.tags.tags-filter').select2('val', '');
}

function doFilter() {
    prepare();
//    buildTree();
}

function cancelSort() {
    $("#sortByComment").click();
    prepare();
}

function doSort() {
    prepare();
//    buildTree();
}

svgSpace.setStrokeColor = function(color) {
	svgSpace.strokeColor = color;
	$('.color-picker.color-picker-border').spectrum('set', svgSpace.strokeColor.toHexString());
}

svgSpace.setFillColor = function(color) {
	svgSpace.fillColor = color;
	$('.color-picker.color-picker-fill').spectrum('set', svgSpace.fillColor.toHexString());
}

var args = {
    svgTag : "",
    viewbox : "0,0,1024,768",
    zoomLevel : 32,
    imageId : imageId
};
svgSpace.viewIndexes = true;
var viewportWidth = getParam("width");
if (viewportWidth == undefined) {
    viewportWidth = $(window).width() * 1;
}
svgSpace.fullX = viewportWidth * 1 - 60;
svgSpace.normalX = viewportWidth * 0.66;
var viewportHeight = getParam("height");
if (viewportHeight == undefined) {
    viewportHeight = $(window).height() * 1;
}
svgSpace.fullY = viewportHeight * 1 - 120;
svgSpace.normalY = viewportHeight * 1 - 420;
if (svgSpace.normalY < 450) {
    svgSpace.normalY = 450;
}
if (svgSpace.fullY < 450) {
    svgSpace.fullY = 450;
}
svgSpace.canWidth = svgSpace.normalX;
svgSpace.canHeight = svgSpace.normalY;
svgSpace.isShapeUnique = function(target) {
    var arr = allAnnotationShapes;
    var id1 = "id='" + target + "'";
    var id2 = "id=\"" + target + "\"";
    for (i = 0; i < arr.length; i++) {
	if (arr[i].shape
		&& arr[i].shape.svgTag
		&& (arr[i].shape.svgTag.toString().indexOf(id1) > 0 || arr[i].shape.svgTag
			.toString().indexOf(id2) > 0)) {
	    return false;
	}
    }
    return true;
}
svgSpace.getImageProtocol = function(image) {
    return svgSpace.protocols[2];
}

svgSpace.getAnnotationData = function(element) {
	var args = { },
		outerHtml = stripTag(outerHTML(element));

	args.svgTag = outerHtml.replace(/"/g, "'");
    args.imageId = imageId;
    args.imageUri = image;
    args.zoomLevel = zoomFactor;
    args.viewbox = getStepX() + "," + getStepY() + "," + getViewBoxX() + "," + getViewBoxY();
	return args;
}

svgSpace.callCustomFuction = function(element, initalText) {
    svgCanvas.clearSelection();
    element = $("#" + element.getAttribute("id"))[0];
    var temp = outerHTML(element);
    temp = stripTag(temp);
    $("#pathpointgrip_container").find("*").attr("display", "none");
    args.svgTag = temp.replace(/"/g, "'");
    args.imageId = imageId;
    args.imageUri = image;
    args.zoomLevel = zoomFactor;
    args.viewbox = getStepX() + "," + getStepY() + "," + getViewBoxX() + ","
	    + getViewBoxY();
    svgCanvas.addToSelection([ document.getElementById(element
	    .getAttribute("id")) ]);
    $("#menu_panel").css("visibility", "hidden");
    openDialog(args, initalText);
    svgSpace.createdItem = element.getAttribute("id");
    return args;
}
svgSpace.callSelection = function(mouse_target, evt) {
    var id = "id='" + mouse_target.id + "'";
    var isOld = false;
    // if (isFullScreen == true) {
    for (i = shapes.length - 1; i >= 0; i--) {
	if (shapes[i].shape.svgTag.toString().indexOf(id) > 0) {
	    var tmpId = shapes[i].id.toString() + $.comments.commentSuffix;
	    var items = $("div#treePane .commentHeight .comment");
	    for (j = 0; j < items.length; j++) {
		if (outerHTML(items[j]).indexOf(tmpId) > 0) {
		    $("#" + info_panel).html(
			    $(items[j]).find(".commentView .col-xs-12:first")
				    .html());
		    isOld = true;
		    break;
		}
	    }
	    break;
	}
    }
    if (isOld) {
		$("#" + info_panel).css("visibility", "visible");
		$("#" + info_panel).css("top", evt.clientY);
		$("#" + info_panel).css("left", evt.clientX);
    }
    // }
}

svgSpace.getTopicByShapeId = function(shapeId) {
	var topic;
	$.each(allAnnotationShapes, function() {
		if(this.shape.svgTag.indexOf(shapeId) > 0) {
			topic = this;
			return false;
		}
	});
	return topic;
}

svgSpace.getFreeTextHtml = function(elementId) {
	var text,
		topic = svgSpace.getTopicByShapeId(elementId);
	
	if (topic && topic.content) {
		text = topic.content;
	}
	return text;
}

svgSpace.setEditBtnDisabledState = function(disabled) {
	var btn = $('button.editRegion');
	if (disabled) {
		btn.attr('disabled', 'disabled');
	} else {
		btn.removeAttr('disabled');
	}
}

svgSpace.callClick = function(mouse_target, evt) {
    var isMoved = false;
    var id = "id='" + mouse_target.id + "'";
    for (i = shapes.length - 1; i >= 0; i--) {
		if (shapes[i].shape.svgTag.toString().indexOf(id) > 0) {
		    var tmpId = shapes[i].id.toString() + $.comments.commentSuffix;
		    var items = $("div#treePane .commentHeight .comment");
		    for (j = 0; j < items.length; j++) {
				if (outerHTML(items[j]).indexOf(tmpId) > 0) {
				    if (!isMoved) {
						var clazz = items[j].getAttribute("class");
						if (clazz == null) {
						    clazz = "";
						}
						items[j].setAttribute("class", clazz + " " + rowSelected);
						var size = $(items[j]).offset().top - $("div#treePane").offset().top;
						$("div#treePane").scrollTop($("div#treePane").scrollTop() + size);
						isMoved = true;
				    }
				} else {
				    var classValue = items[j].getAttribute("class");
				    if (classValue != null) {
						classValue = classValue.replace(rowSelected, "");
						items[j].setAttribute("class", classValue);
				    }
				}
		    }
		    break;
		}
    }
    if (!isMoved) {
		if (mouse_target.tagName == 'path' && mouse_target.id.indexOf(rssufix) < 0) {
		    svgCanvas.pathActions.toEditMode(mouse_target);
		} else {
		    svgCanvas.addToSelection([ document.getElementById(mouse_target.id) ]);
		}
    }
}
svgSpace.zoomIn = function() {
    doFilter();
    $(document).trigger('iat:dom:zoom-lvl-changed');
}
svgSpace.zoomOut = function() {
    doFilter();
    $(document).trigger('iat:dom:zoom-lvl-changed');
}
svgSpace.constructZoomer = function(hasToLoadData, map) {
	if (!map) {
		return;
	}
	
    var zoomIndexes = new Array();
    for ( var prop in map) {
    	var shape = map[prop].shape;
    	if (shape && shape.zoomLevel >= 0) {
    		zoomIndexes.push(shape.zoomLevel);
    	}
    }
    
    constructZoomer(zoomIndexes);
}
svgSpace.callDeselection = function(mouse_target, evt) {
    mouse_target.setAttribute("stroke-dasharray", "");
    $("#" + info_panel).css("visibility", "hidden");
}
svgSpace.findTile = function(imageIndex, zoomLevel, x1, y1, x2, y2) {
    return findTile(imageIndex, zoomLevel, x1, y1, x2, y2);
}
if (getParam("opacity") == undefined) {
    svgSpace.opacities = [ "1" ];
} else {
    svgSpace.opacities = [ getParam("opacity") ];
}
if (getParam("contrast") == undefined) {
    svgSpace.contrasts = [ "0" ];
} else {
    svgSpace.contrasts = [ getParam("contrast") ];
}
if (getParam("brightness") == undefined) {
    svgSpace.brightnesses = [ "1" ];
} else {
    svgSpace.brightnesses = [ getParam("brightness") ];
}
if (getParam("visibility") == undefined) {
    svgSpace.visibilities = [ true ];
} else {
    svgSpace.visibilities = [ getParam("visibility") ];
}
if (getParam("order") == undefined) {
    svgSpace.orders = [ "0" ];
} else {
    svgSpace.orders = [ getParam("order") ];
}
svgSpace.images = [ image ];
svgSpace.server = server;
var secondImage = getURLParams("secondImage");
for ( var p = 0; p < secondImage.length; p++) {
    svgSpace.images.push(secondImage[p]);
    svgSpace.opacities.push("0");
    svgSpace.contrasts.push("0");
    svgSpace.brightnesses.push("1");
    svgSpace.visibilities.push(false);
    svgSpace.orders.push(p + 1);
}

//setTimeout(function() {
//    onLoad();
//}, 1000);