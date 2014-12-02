var host = window.location.hostname;
if (window.location.port.toString().length > 0) {
    host = host + ':' + window.location.port;
}
var href = window.location.href;
var root = href.substring(href.indexOf(host) + host.length);
root = root.substring(0, root.indexOf('/', 1));

svgSpace.rootPath = root;

var filter = null;
var sort = null;
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
    var treeHeight = svgSpace.normalY - $("div.data-annotation-head").height();
    $("div#treePane").css("height", treeHeight);
    $("div#treePane").css("max-height", treeHeight);
}

/**
 * Function that is invoked on page load.
 */
function onLoad() {
    $("#treePane").css("width", svgSpace.fullX - svgSpace.normalX - 30);
    $(".col-36").css("width", svgSpace.fullX - svgSpace.normalX - 30);
    $(".col-36").css("height", svgSpace.normalY);
    setTreePaneHeight();
    $('div.data-annotation .tabs-head li').click(function() {
	setTreePaneHeight();
    });
    $("#fullscreen").click(function() {
	$("#art-prospect-body div.col-36").css("display", "none");
	$("body").css("background-color", "black");
	$("#normalscreen").css("display", "block");
	$("#fullscreen").css("display", "none");
	$("#zoomer").css("width", "80px");
	setFullScreen();
	$("#zoom_panel").css("position", "absolute");
	var top = $("#frontimage").attr("y");
	top += $("#frontimage").attr("height");
	top += 90;
	$("#zoom_panel").css("top", top + "px");
	var left = $("#frontimage").attr("x");
	left += $("#frontimage").attr("width") / 2;
	left += -60;
	$("#zoom_panel").css("left", left + "px");
	$("#zoom_panel").css("width", "150px");
	$("#zoom_panel").css("color", "white");
	$("#zoom_panel").css("background-color", "black");
    });
    $("#normalscreen").click(function() {
	$("#art-prospect-body div.col-36").css("display", "block");
	$("body").css("background-color", "transparent");
	$("#normalscreen").css("display", "none");
	$("#fullscreen").css("display", "");
	$("#zoomer").css("width", "80%");
	setNormalScreen();
	$("#zoom_panel").css("position", "relative");
	$("#zoom_panel").css("top", "0px");
	$("#zoom_panel").css("left", "0px");
	$("#zoom_panel").css("width", "100%");
	$("#zoom_panel").css("color", "black");
	$("#zoom_panel").css("background-color", "transparent");
	$("#" + info_panel).css("visibility", "hidden");
    });
    $("#fromDate").datepicker({
	changeMonth : true,
	changeYear : true
    });
    $("#toDate").datepicker({
	changeMonth : true,
	changeYear : true
    });
    var data = '<ul id="overlayDrag" style="width:100%;">';
    for (i = 0; i < svgSpace.images.length; i++) {
	data += templateNavigation.replace(/%ID%/g, i);
    }
    data += '</ul>';
    $("div.add-overlay").before(data);

    $.comments.rootId = getParam("imageId");
    $.comments.imagePathURL = svgSpace.rootPath + "/view/images";
    $.comments.source = '#svgcontent';
    $.comments.target = '.comments';
    $.comments.commentSelectors = '#nothing';
    $($.comments.target).on('postSaveAction', function() {
	$(".arrow a span").click();
	buildTree();
    });
    $($.comments.target).on('postCancelAction', function() {
	$(".arrow a span").click();
	buildTree();
    });
    $($.comments.target).on('postLoad', function(event, comments) {
	for ( var int = 0; int < comments.length; int++) {
	    if (comments[int].shape && comments[int].shape.svgTag) {
		shapes.push(comments[int]);
		allAnnotationShapes.push(comments[int]);
	    }
	}
    });
    $($.comments.target).on('postUnload', function(event, comments) {
	for ( var i = 0; i < comments.length; i++) {
	    for ( var j = 0; j < shapes.length; j++) {
		if (comments[i] == shapes[j]) {
		    shapes.splice(j, 1);
		}
	    }
	    for ( var j = 0; j < allAnnotationShapes.length; j++) {
		if (comments[i] == allAnnotationShapes[j]) {
		    allAnnotationShapes.splice(j, 1);
		}
	    }
	}
    });
    $.comments.init();
    $.comments.refresh();

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
    $(".comp-1 a,.comp-3 a").click(function() {
	$(".flydown-handle.overlayMenu").addClass("disabled");
	svgSpace.viewAllImages = false;
	zoom(0, 0, 1);
    });

    $(".comp-2 a").click(function() {
	$(".flydown-handle.overlayMenu").removeClass("disabled");
	svgSpace.viewAllImages = true;
	zoom(0, 0, 1);
    });
    $("ul.radioButtons > li > a").click(function() {
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
		args.imageId = getParam("imageId");
		args.imageUri = getParam("image");
		args.zoomLevel = zoomFactor;
		args.viewbox = getStepX() + "," + getStepY() + ","
			+ getViewBoxX() + "," + getViewBoxY();
		openDialog(args);
	    });
    $(".tabs-head>input[type=radio]").click(function() {
	doFilter();
    });
    EMF.effects.flyOut();
    EMF.effects.selectOne();
}

function hideFilterSortContent() {
    $('.tabs .tabs-content').css('display', 'none');
    $('.tabs .tabs-head ul li').removeClass();
}

function prepare() {
    filter = {};
    if ($("#author") != null) {
	filter.author = $("#author").val();
    } else {
	filter.author = "";
    }
    if ($("#keyword") != null) {
	filter.keyword = $("#keyword").val();
    } else {
	filter.keyword = "";
    }
    if ($("#fromDate") != null) {
	filter.fromDate = $("#fromDate").val();
    } else {
	filter.fromDate = "";
    }
    if ($("#toDate") != null) {
	filter.toDate = $("#toDate").val();
    } else {
	filter.toDate = "";
    }
    if ($(".tabs-head input:checked").attr("value") == "current") {
	filter.zoomLevel = zoomFactor;
    } else {
	filter.zoomLevel = $("input:checked").attr("value");
    }
    if ($("#sortByCreation").is(':checked')) {
	sort = $("#sortByCreation").attr("id");
    } else {
	sort = $("#sortByComment").attr("id");
    }
    shapes.splice(0, shapes.length);
    if ($(".tabs-head .activeFilter").size() > 0) {
	$(".tabs-head .activeFilter")[0].click();
    }
}

function openDialog(shape) {
    svgCanvas.setMode("resize");
    $.comments.showCommentBox(null, getParam("imageId"), true, shape);
}

function buildTree() {
    if (this.svgCanvas != undefined) {
	var data = svgCanvas.getSvgString();
	data = clearShapes(data);
	svgCanvas.setSvgString(data);
    }
    $("#treePane").html("");
    var query = {
	imageUri : getParam("image"),
	image : getParam("imageId")
    };
    if (filter != null) {
	query.author = filter.author;
	query.keyword = filter.keyword;
	query.fromDate = filter.fromDate;
	query.toDate = filter.toDate;
	query.zoomLevel = filter.zoomLevel;
    }
    if (sort != null) {
	query.sort = sort;
    }

    var comments = [
	    {
		"content" : "Comment on image with shape.",
		"createdOn" : "20.12.2012, 20:12",
		"createdBy" : "Admin",
		"id" : "1",
		"objectId" : getParam("imageId"),
		"shape" : {
		    "viewbox" : "0,0,2700,1800",
		    "zoomLevel" : 8,
		    "svgTag" : "<rect id='rs_annotation_1' height='555' width='555' y='360' x='180' style='pointer-events:inherit' stroke='#333333' fill='none' fill-opacity='0' stroke-dasharray='' stroke-width='1'/>"
		},
		"children" : [ {
		    "content" : "Reply on image with shape.",
		    "createdOn" : "20.12.2012, 20:12",
		    "createdBy" : "Admin",
		    "id" : "3",
		    "objectId" : getParam("imageId"),
		    "children" : []
		} ]
	    },
	    {
		"content" : "Comment on image without shape.",
		"createdOn" : "20.12.2012, 20:12",
		"createdBy" : "Admin",
		"id" : "2",
		"objectId" : getParam("imageId"),
		"shape" : {
		    "viewbox" : "0,0,2700,1800",
		    "zoomLevel" : 8,
		    "svgTag" : "<ellipse fill='none' stroke='#ffff00' fill-opacity='0' style='pointer-events:inherit' cx='360' cy='90' id='rs_annotation_2' rx='360' ry='90'/>"
		},
		"children" : []
	    } ];

    $.comments.clearData();
    $.comments.loadData(comments);

    var shapeMap = {};
    for (i = 0; i < shapes.length; i++) {
	if (shapes[i].shape.svgTag.length > 0) {
	    var idPrefix = " id='";
	    var currName = shapes[i].shape.svgTag
		    .substring(shapes[i].shape.svgTag.indexOf(idPrefix)
			    + idPrefix.length);
	    currName = currName.substring(0, currName.indexOf("'"));
	    shapeMap[currName] = shapes[i].shape.svgTag;
	}
    }
    var tmp = new Array();
    for ( var prop in shapeMap) {
	tmp.push(shapeMap[prop]);
    }
    addShapes(tmp);

    $.comments.refresh();

    var clickDiv = function() {
	var divId = $(this).attr("id");
	divId = divId.substring(0, divId.indexOf($.comments.commentSuffix));
	var currShape = null;
	for (i = 0; i < shapes.length; i++) {
	    if (shapes[i].id == divId) {
		currShape = shapes[i].shape;
		break;
	    }
	}
	var viewbox = $.trim(currShape.viewbox).split(",");
	var zoomLevel = $.trim(currShape.zoomLevel);
	var x = $.trim(viewbox[0]) / (maxZoom[0] / zoomLevel);
	var y = $.trim(viewbox[1]) / (maxZoom[0] / zoomLevel);
	var width = $.trim(viewbox[2]) / (maxZoom[0] / zoomLevel);
	var height = $.trim(viewbox[3]) / (maxZoom[0] / zoomLevel);
	focusImage(x, y, width, height, zoomLevel * 1);
	var id = $.trim(currShape.svgTag);
	if (id.length > 0) {
	    id = id.substring(id.indexOf(" id='") + 5);
	    id = id.substring(0, id.indexOf("'"));
	    select(id);
	} else {
	    $('[stroke-width="5"]').attr('stroke-width', '');
	}
	$("." + rowSelected).removeClass(rowSelected);
	$(this).addClass(rowSelected);
    };
    $("div#treePane table").unbind('click', clickDiv);
    $("div#treePane table").bind('click', clickDiv);
    EMF.effects.flyOut();
}

function cancelFilter() {
    hideFilterSortContent();
    resetFilter();
    prepare();
    buildTree();
}

function resetFilter() {
    filter = null;
    $("#author").val('');
    $("#keyword").val('');
    $("#fromDate").val('');
    $("#toDate").val('');
}

function doFilter() {
    prepare();
    buildTree();
}

function cancelSort() {
    $("#sortByCreation").click();
    prepare();
    buildTree();
}

function doSort() {
    prepare();
    buildTree();
}

var args = {
    svgTag : "",
    viewbox : "0,0,1024,768",
    zoomLevel : 32,
    imageId : getParam("imageId")
};
svgSpace.viewIndexes = true;
var viewportWidth = getParam("width");
if (viewportWidth == undefined) {
    viewportWidth = $(window).width() * 1;
}
svgSpace.fullX = viewportWidth * 1 - 60;
svgSpace.normalX = viewportWidth * 0.6;
var viewportHeight = getParam("height");
if (viewportHeight == undefined) {
    viewportHeight = $(window).height() * 1;
}
svgSpace.fullY = viewportHeight * 1 - 60;
svgSpace.normalY = viewportHeight * 1 - 150;
if (svgSpace.normalY < 750) {
    svgSpace.normalY = 750;
}
if (svgSpace.fullY < 750) {
    svgSpace.fullY = 750;
}
svgSpace.canWidth = svgSpace.normalX;
svgSpace.canHeight = svgSpace.normalY;
svgSpace.isShapeUnique = function(target) {
    var id1 = "id='" + target + "'";
    var id2 = "id=\"" + target + "\"";
    for (i = 0; i < allAnnotationShapes.length; i++) {
	if (allAnnotationShapes[i].toString().indexOf(id1) > 0
		|| allAnnotationShapes[i].toString().indexOf(id2) > 0) {
	    return false;
	}
    }
    return true;
}
svgSpace.getImageProtocol = function(image) {
    if (/^http:\/\/www\.britishmuseum\.org\/[^\/]+\/([^\/]+).+/i.test(image)) {
	return svgSpace.protocols[2];
    }
    if (/^http:\/\/scale\.ydc2\.yale\.edu\/.+/i.test(image)) {
	return svgSpace.protocols[1];
    }
    return svgSpace.protocols[0];
}
svgSpace.callCustomFuction = function(element) {
    svgCanvas.clearSelection();
    element = $("#" + element.getAttribute("id"))[0];
    var temp = outerHTML(element);
    temp = stripTag(temp);
    $("#pathpointgrip_container").find("*").attr("display", "none");
    args.svgTag = temp.replace(/"/g, "'");
    args.imageId = getParam("imageId");
    args.imageUri = getParam("image");
    args.zoomLevel = zoomFactor;
    args.viewbox = getStepX() + "," + getStepY() + "," + getViewBoxX() + ","
	    + getViewBoxY();
    svgCanvas.addToSelection([ document.getElementById(element
	    .getAttribute("id")) ]);
    $("#menu_panel").css("visibility", "hidden");
    openDialog(args);
}
svgSpace.callSelection = function(mouse_target, evt) {
    var id = "id='" + mouse_target.id + "'";
    $("[stroke-dasharray=\"5,5\"]").attr("stroke-dasharray", "");
    mouse_target.setAttribute("stroke-dasharray", "5,5");
    var isOld = false;
    if (isFullScreen == true) {
	for (i = shapes.length - 1; i >= 0; i--) {
	    if (shapes[i].shape.svgTag.toString().indexOf(id) > 0) {
		var tmpId = shapes[i].id.toString() + $.comments.commentSuffix;
		var items = $("div#treePane table");
		for (j = 0; j < items.length; j++) {
		    if (outerHTML(items[j]).indexOf(tmpId) > 0) {
			$("#" + info_panel).html(outerHTML(items[j]));
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
    }
}
svgSpace.callClick = function(mouse_target, evt) {
    var isMoved = false;
    var id = "id='" + mouse_target.id + "'";
    for (i = shapes.length - 1; i >= 0; i--) {
	if (shapes[i].shape.svgTag.toString().indexOf(id) > 0) {
	    var tmpId = shapes[i].id.toString() + $.comments.commentSuffix;
	    var items = $("div#treePane table");
	    for (j = 0; j < items.length; j++) {
		if (outerHTML(items[j]).indexOf(tmpId) > 0) {
		    if (!isMoved) {
			var clazz = items[j].getAttribute("class");
			if (clazz == null) {
			    clazz = "";
			}
			items[j].setAttribute("class", clazz + " "
				+ rowSelected);
			var size = $("#" + items[j].id).offset().top
				- $("div#treePane").offset().top;
			$("div#treePane").scrollTop(
				$("div#treePane").scrollTop() + size);
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
	if (mouse_target.tagName == 'path'
		&& mouse_target.id.indexOf(rssufix) < 0) {
	    svgCanvas.pathActions.toEditMode(mouse_target);
	} else {
	    svgCanvas
		    .addToSelection([ document.getElementById(mouse_target.id) ]);
	}
    }
}
svgSpace.zoomIn = function() {
    doFilter();
}
svgSpace.zoomOut = function() {
    doFilter();
}
svgSpace.constructZoomer = function(hasToLoadData) {
    if (hasToLoadData == true) {
	buildTree();
    }
    var zoomMap = {};
    for (i = 0; i < shapes.length; i++) {
	if (shapes[i].shape.svgTag.length > 0) {
	    var currName = shapes[i].shape.svgTag
		    .substring(shapes[i].shape.svgTag.indexOf(" id='")
			    + " id='".length);
	    currName = currName.substring(0, currName.indexOf("'"));
	    zoomMap[currName] = shapes[i].shape.zoomLevel;
	}
    }
    var zoomIndexes = new Array();
    for ( var prop in zoomMap) {
	zoomIndexes.push(zoomMap[prop]);
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
svgSpace.images = [ getParam("image") ];
svgSpace.server = getParam("server");
var secondImage = getURLParams("secondImage");
for ( var p = 0; p < secondImage.length; p++) {
    svgSpace.images.push(secondImage[p]);
    svgSpace.opacities.push("0");
    svgSpace.contrasts.push("0");
    svgSpace.brightnesses.push("1");
    svgSpace.visibilities.push(false);
    svgSpace.orders.push(p + 1);
}