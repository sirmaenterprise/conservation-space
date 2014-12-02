var svgSpace = {
    callCustomZoom : function() {
    },
    callCustomFuction : function(target) {
    },
    isShapeUnique : function(target) {
	return true;
    },
    getImageProtocol : function(image) {
	return svgSpace.protocols[2];
    },
    callClick : function(mouse_target, evt) {
    },
    callSelection : function(mouse_target, evt) {
    },
    callDeselection : function(mouse_target, evt) {
    },
    getFreeTextHtml: function(elementId) {
    	
    },
    zoomIn : function() {
    },
    zoomOut : function() {
    },
    constructZoomer : function(hasToReloadZoom) {
    },
    addTile : function(imageIndex, zoomLevel, x, y, url) {
	$(window).trigger("addTile", [ imageIndex, zoomLevel, x, y, url ]);
    },
    findTile : function(imageIndex, zoomLevel, x1, y1, x2, y2) {
	return undefined;
    },
    minimumDragX : 20,
    minimumDragY : 20,
    normalX : 512,
    normalY : 384,
    fullX : 512,
    fullY : 384,
    canWidth : 512,
    canHeight : 384,
    tileWidth : 128,
    tileHeight : 128,
    server : 'http://www.sirma.bg/',
    rootPath : '/imageannotation',
    toolPath : '/imageannotation/tool',
    sizeService : null,
    images : [],
    opacities : [],
    contrasts : [],
    brightnesses : [],
    visibilities : [],
    orders : [],
    viewAllImages : false,
    viewIndexes : false,
    createdItem : null,
    imageProtocol : null,
    protocols : [ 'IIP', 'IIIF', 'HTTP' ]
};

var currentX = 0;
var currentY = 0;
var mouseDownX = 0;
var mouseDownY = 0;
var subViewStepX = 0;
var subViewStepY = 0;

var zoomFactor = 1;
var num_resolutions = 0;

var max_sizeX = [ 0 ];
var max_sizeY = [ 0 ];
var tileSizeX = [ 0 ];
var tileSizeY = [ 0 ];
var maxZoom = [ 1 ];
var minZoom = [ 1 ];

var isCursorUp = true;
var isFullScreen = false;

var sub_view = "frontimage";
var sub_view_rect = "frontimagerect";
var rsprefix = "rs_annotation_";
var rssufix = "_point";
var info_panel = "info_panel";
var curr_color = "curr_color";
var rowSelected = "rowSelected";
var shapesData = [];

function constructZoomer(zoomIndexes) {
    if ($("#zoomer").length > 0) {
		var iterations = 0;
		var currentValue = 0;
		var temp = maxZoom[0];
		var zoomValue = zoomFactor;
		while (temp >= minZoom[0]) {
		    iterations = iterations + 1;
		    temp = temp / 2;
		}
		while (zoomValue >= minZoom[0]) {
		    currentValue = currentValue + 1;
		    zoomValue = zoomValue / 2;
		}
		$("#zoomer").slider();
		$("#zoomer").slider("option", "step", 1);
		$("#zoomer").slider("option", "min", 1);
		$("#zoomer").slider("option", "max", iterations);
		$("#zoomer").slider("option", "value", currentValue);
		$("#zoomer").slider({
		    slide : function(event, ui) {
				$("#svgcanvas").on("refreshed", function() {
				    doFilter();
				});
				var size = minZoom[0];
				var temp = ui.value;
				while (temp > 1) {
				    temp = temp - 1;
				    size = size * 2;
				}
				if (size > zoomFactor) {
				    while (size > zoomFactor) {
					zoomIn();
				    }
				}
				if (size < zoomFactor) {
				    while (size < zoomFactor) {
					zoomOut();
				    }
				}
				$(this).slider("disable");
		    }
		});
		var zoomer = $("#zoomer");
		zoomer.slider("enable");
		zoomer
			.children('.zoom-index')
				.remove();
		
		iterations = iterations - 1;
		
		var stepSize = 100 / iterations;
		var size = minZoom[0];
		for (i = 0; i <= iterations; i++) {
		    var number = 0;
		    for (p = 0; p <= zoomIndexes.length; p++) {
				if (size == zoomIndexes[p]) {
				    number = number + 1;
				}
		    }
		    size = size * 2;
		    if ($("#zoom_index_" + i).length == 0) {
				var numberSize = 0.5;
				var numberDigits = number;
				while (numberDigits >= 10) {
				    numberDigits = numberDigits / 10;
				    numberSize = numberSize * 2;
				}
				var leftValue = i * stepSize - numberSize;
				$("#zoomer").append(
					'<div class="zoom-index" id="zoom_index_' + i
						+ '"style="position:absolute; top:10px; left:'
						+ leftValue + '%;">' + number + '</div>');
		    }
		    $("#zoom_index_" + i).html(number);
		}
    }
}

function refreshFilter(index) {
    var i = svgSpace.contrasts[index];
    var s = svgSpace.brightnesses[index];
    $("div#div_filter" + index).remove();
    $("body")
	    .append(
		    "<div id=\"div_filter"
			    + index
			    + "\"><svg xmlns=\"http://www.w3.org/2000/svg\"><filter id=\"filter"
			    + index
			    + "\"><feComponentTransfer><feFuncR type=\"linear\" slope=\""
			    + s + "\" intercept=\"" + i
			    + "\"/><feFuncG type=\"linear\" slope=\"" + s
			    + "\" intercept=\"" + i
			    + "\"/><feFuncB type=\"linear\" slope=\"" + s
			    + "\" intercept=\"" + i
			    + "\"/></feComponentTransfer></filter></svg></div>");
}

function constructSlider(w) {
    var frontSizeX = 100;
    var frontSizeY = frontSizeX * max_sizeY[w] / max_sizeX[w];
    refreshFilter(w);
    if ($("#slider_opacity_" + w).length > 0) {
	var currentOpacity = 100 * svgSpace.opacities[w];
	$("#slider_opacity_" + w).slider();
	$("#slider_opacity_" + w).slider("option", "value", currentOpacity);
	$("#slider_opacity_" + w).slider("option", "step", 10);
	$("#slider_opacity_" + w).slider("option", "min", 0);
	$("#slider_opacity_" + w).slider("option", "max", 100);
	$("#slider_opacity_" + w)
		.slider(
			{
			    slide : function(event, ui) {
				svgSpace.opacities[event.target.id
					.substring("slider_opacity_".length) * 1] = ui.value / 100.00;
				$("#slider_opacity_" + w + " .visibility").css(
					"width", ui.value + "%");
				zoom(0, 0, 1);
			    }
			});
	$("#slider_opacity_" + w + " .visibility").css("width",
		currentOpacity + "%");
    }
    if ($("#slider_contrast_" + w).length > 0) {
	var currentContrast = 100 * svgSpace.contrasts[w];
	$("#slider_contrast_" + w).slider();
	$("#slider_contrast_" + w).slider("option", "value", currentContrast);
	$("#slider_contrast_" + w).slider("option", "step", 10);
	$("#slider_contrast_" + w).slider("option", "min", 0);
	$("#slider_contrast_" + w).slider("option", "max", 100);
	$("#slider_contrast_" + w).slider(
		{
		    slide : function(event, ui) {
			var indexImage = event.target.id
				.substring("slider_contrast_".length) * 1;
			var contrastValue1 = ui.value / 100.00;
			svgSpace.contrasts[indexImage] = contrastValue1;
			$("#slider_contrast_" + w + " .visibility").css(
				"width", ui.value + "%");
			refreshFilter(indexImage);
			zoom(0, 0, 1);
		    }
		});
	$("#slider_contrast_" + w + " .visibility").css("width",
		currentContrast + "%");
    }
    if ($("#slider_brightness_" + w).length > 0) {
	var currentBright = 100 * svgSpace.brightnesses[w];
	$("#slider_brightness_" + w).slider();
	$("#slider_brightness_" + w).slider("option", "value", currentBright);
	$("#slider_brightness_" + w).slider("option", "step", 10);
	$("#slider_brightness_" + w).slider("option", "min", 0);
	$("#slider_brightness_" + w).slider("option", "max", 100);
	$("#slider_brightness_" + w).slider(
		{
		    slide : function(event, ui) {
			var indexImage = event.target.id
				.substring("slider_brightness_".length) * 1;
			var brightnessValue1 = ui.value / 100.00;
			svgSpace.brightnesses[indexImage] = brightnessValue1;
			$("#slider_brightness_" + w + " .visibility").css(
				"width", ui.value + "%");
			refreshFilter(indexImage);
			zoom(0, 0, 1);
		    }
		});
	$("#slider_brightness_" + w + " .visibility").css("width",
		currentBright + "%");
    }
    if ($("#image_visibility_" + w).length > 0) {
	var visibleId = "#image_visibility_" + w;
	if (svgSpace.visibilities[w] == true) {
	    $(visibleId + " .cb-enable").addClass("selected");
	    $(visibleId + " .cb-disable").removeClass("selected");
	} else {
	    $(visibleId + " .cb-enable").removeClass("selected");
	    $(visibleId + " .cb-disable").addClass("selected");
	}
	$(visibleId + " .cb-enable").click(
		function(event) {
		    svgSpace.visibilities[event.target.parentNode.id
			    .substring("image_visibility_".length) * 1] = true;
		    zoom(0, 0, 1);
		});
	$(visibleId + " .cb-disable")
		.click(
			function(event) {
			    svgSpace.visibilities[event.target.parentNode.id
				    .substring("image_visibility_".length) * 1] = false;
			    zoom(0, 0, 1);
			});
    }
    if ($("#image_delete_" + w).length > 0) {
	$("#image_delete_" + w).click(function(event) {
	    var im = "#image_delete_".length;
	    svgSpace.images.splice(event.target.id.substring(im) * 1, 1);
	    svgSpace.opacities.splice(event.target.id.substring(im) * 1, 1);
	    svgSpace.contrasts.splice(event.target.id.substring(im) * 1, 1);
	    svgSpace.brightnesses.splice(event.target.id.substring(im) * 1, 1);
	    svgSpace.visibilities.splice(event.target.id.substring(im) * 1, 1);
	    svgSpace.orders.splice(event.target.id.substring(im) * 1, 1);
	    zoom(0, 0, 1);
	});
    }
    $("#slider_image_" + w).html(
	    "<img width='" + frontSizeX + "' height='" + frontSizeY + "' src='"
		    + getScaledURL(svgSpace.images[w], frontSizeX) + "' />");
}

function select(id) {
    svgCanvas.clearSelection();
    if (document.getElementById(id) != null) {
	$('[stroke-width="5"]').attr('stroke-width', '');
	document.getElementById(id).setAttribute('stroke-width', '5');
	if (svgSpace.createdItem == id) {
	    svgCanvas.addToSelection([ document.getElementById(id) ], true);
	}
    }
}

function isBigger(shape1, shape2) {
    shape1 = shape1.replace(/'/g, "\"");
    var name1 = shape1.substring(shape1.indexOf(" id=\"") + " id=\"".length);
    name1 = name1.substring(0, name1.indexOf('"'));
    var box1 = svgCanvas.getBBox(document.getElementById(name1));
    shape2 = shape2.replace(/'/g, "\"");
    var name2 = shape2.substring(shape2.indexOf(" id=\"") + " id=\"".length);
    name2 = name2.substring(0, name2.indexOf('"'));
    var box2 = svgCanvas.getBBox(document.getElementById(name2));
    return box1.x <= box2.x && box1.y <= box2.y
	    && box1.x + box1.width >= box2.x + box2.width
	    && box1.y + box1.height >= box2.y + box2.height;
}

function sortShapes(shapes) {
    var result = new Array();
    while (shapes.length > 0) {
	var normalShape = null;
	for (i = 0; i < shapes.length; i++) {
	    var tempShape = shapes[i];
	    var hasSmaller = false;
	    for (j = i + 1; j < shapes.length; j++) {
		if (isBigger(tempShape, shapes[j])) {
		    hasSmaller = true;
		    break;
		}
	    }
	    if (!hasSmaller) {
		normalShape = tempShape;
		shapes.splice(i, 1);
		break;
	    }
	}
	if (normalShape != null) {
	    result.splice(0, 0, normalShape);
	} else {
	    result = shapes;
	    break;
	}
    }
    return result;
}

function addShapes(shapes, sort) {
    var data = clearShapes(svgCanvas.getSvgString());
    if (sort) {
	shapes = sortShapes(shapes);
    }
    shapesData = shapes;
    var result;
    if (data.indexOf("<title") > 0) {
	result = data.substring(0, data.indexOf("<title"));
    } else {
	result = data.substring(0, data.indexOf("</g>"));
    }
    for (i = 0; i < shapes.length; i++) {
	var temp = shapes[i].replace(/'/g, "\"");
	if (temp.indexOf('<ellipse') == 0) {
	    temp = zoomEllipse(temp, 0, 0, 1 / maxZoom[0]);
	    temp = zoomEllipse(temp, -currentX * tileSizeX[0], -currentY
		    * tileSizeY[0], zoomFactor, svgSpace.viewIndexes);
	}
	if (temp.indexOf('<path') == 0) {
	    temp = zoomPath(temp, 0, 0, 1 / maxZoom[0]);
	    temp = zoomPath(temp, -currentX * tileSizeX[0], -currentY
		    * tileSizeY[0], zoomFactor, svgSpace.viewIndexes);
	}
	if (temp.indexOf('<image') == 0) {
	    temp = zoomImage(temp, 0, 0, 1 / maxZoom[0]);
	    temp = zoomImage(temp, -currentX * tileSizeX[0], -currentY
		    * tileSizeY[0], zoomFactor);
	}
	if (temp.indexOf('<line') == 0) {
	    temp = zoomLine(temp, 0, 0, 1 / maxZoom[0]);
	    temp = zoomLine(temp, -currentX * tileSizeX[0], -currentY
		    * tileSizeY[0], zoomFactor, svgSpace.viewIndexes);
	}
	if (temp.indexOf('<rect') == 0) {
	    temp = zoomRect(temp, 0, 0, 1 / maxZoom[0]);
	    temp = zoomRect(temp, -currentX * tileSizeX[0], -currentY
		    * tileSizeY[0], zoomFactor, svgSpace.viewIndexes);
	}
	result = result + temp;
    }
    if (data.indexOf("<title") > 0) {
	result = result + data.substring(data.indexOf("<title"));
    } else {
	result = result + data.substring(data.indexOf("</g>"));
    }
    svgCanvas.setSvgString(result);
    if (!sort) {
	result = addShapes(shapes, true);
    }
    return result;
}

function clearShapes(val) {
    var result;
    result = '';
    if (val.indexOf('<line') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<line'));
	    var afterVal = val.substring(val.indexOf('<line'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) <= 0) {
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<line') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<text') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<text'));
	    var afterVal = val.substring(val.indexOf('<text'));
	    var engTag = '</text>';
	    var val = afterVal.substring(afterVal.indexOf(engTag)
		    + engTag.length);
	    if (val.indexOf('<text') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<rect') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<rect'));
	    var afterVal = val.substring(val.indexOf('<rect'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) <= 0) {
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<rect') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<ellipse') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<ellipse'));
	    var afterVal = val.substring(val.indexOf('<ellipse'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) <= 0) {
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<ellipse') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<path') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<path'));
	    var afterVal = val.substring(val.indexOf('<path'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) <= 0) {
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<path') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<image') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<image'));
	    var afterVal = val.substring(val.indexOf('<image'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) <= 0) {
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<image') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    return result;
}

function focusImage(x, y, width, height, newZoom) {
    var oldZoom = zoomFactor;
    zoomFactor = newZoom * 1;
    x = x * 1 + width / 2 - svgSpace.canWidth / 2;
    if (x < 0) {
	x = 0;
    }
    y = y * 1 + height / 2 - svgSpace.canHeight / 2;
    if (y < 0) {
	y = 0;
    }
    var oldX = currentX;
    currentX = x / tileSizeX[0];
    var oldY = currentY;
    currentY = y / tileSizeY[0];
    currentX = normalizeX(currentX, zoomFactor);
    currentY = normalizeY(currentY, zoomFactor);
    zoom(oldX * (zoomFactor / oldZoom) - currentX, oldY
	    * (zoomFactor / oldZoom) - currentY, zoomFactor / oldZoom);
}

function setFullScreen() {
    isFullScreen = true;
    svgSpace.canWidth = svgSpace.fullX;
    svgSpace.canHeight = svgSpace.fullY;
    document.getElementById('workarea').style.width = svgSpace.canWidth;
    document.getElementById('workarea').style.height = svgSpace.canHeight;
    svgCanvas.setResolution(svgSpace.canWidth, svgSpace.canHeight);
    var oldX = currentX;
    currentX = normalizeX(currentX, zoomFactor);
    var oldY = currentY;
    currentY = normalizeY(currentY, zoomFactor);
    moveSubViewToTop();
    zoom(oldX - currentX, oldY - currentY, 1);
    svgSpace.callCustomZoom();
}

function setNormalScreen() {
    isFullScreen = false;
    svgSpace.canWidth = svgSpace.normalX;
    svgSpace.canHeight = svgSpace.normalY;
    document.getElementById('workarea').style.width = svgSpace.canWidth;
    document.getElementById('workarea').style.height = svgSpace.canHeight;
    svgCanvas.setResolution(svgSpace.canWidth, svgSpace.canHeight);
    var oldX = currentX;
    currentX = normalizeX(currentX, zoomFactor);
    var oldY = currentY;
    currentY = normalizeY(currentY, zoomFactor);
    moveSubView();
    zoom(oldX - currentX, oldY - currentY, 1);
    svgSpace.callCustomZoom();
}

function moveSubViewToTop() {
    var frontImageX = svgSpace.canWidth - $('#' + sub_view).attr('width') * 1;
    var frontImageY = 0;
    $('#' + sub_view_rect).attr(
	    'x',
	    $('#' + sub_view_rect).attr('x') * 1 + frontImageX
		    - $('#' + sub_view).attr('x') * 1);
    $('#' + sub_view_rect).attr(
	    'y',
	    $('#' + sub_view_rect).attr('y') * 1 + frontImageY
		    - $('#' + sub_view).attr('y') * 1);
    $('#' + sub_view).attr('x', frontImageX);
    $('#' + sub_view).attr('y', frontImageY);
}
function moveSubView() {
    var frontImageX = 0;
    var frontImageY = 0;
    if (svgSpace.canWidth > $('#' + sub_view).attr('x') * 1
	    + $('#' + sub_view).attr('width') * 1) {
	frontImageX = $('#' + sub_view).attr('x') * 1;
    } else {
	frontImageX = svgSpace.canWidth - $('#' + sub_view).attr('width') * 1;
    }
    if (svgSpace.canHeight > $('#' + sub_view).attr('y') * 1
	    + $('#' + sub_view).attr('height') * 1) {
	frontImageY = $('#' + sub_view).attr('y') * 1;
    } else {
	frontImageY = svgSpace.canHeight - $('#' + sub_view).attr('height') * 1;
    }
    $('#' + sub_view_rect).attr(
	    'x',
	    $('#' + sub_view_rect).attr('x') * 1 + frontImageX
		    - $('#' + sub_view).attr('x') * 1);
    $('#' + sub_view_rect).attr(
	    'y',
	    $('#' + sub_view_rect).attr('y') * 1 + frontImageY
		    - $('#' + sub_view).attr('y') * 1);
    $('#' + sub_view).attr('x', frontImageX);
    $('#' + sub_view).attr('y', frontImageY);
}

function getZoomSize() {
    return zoomFactor;
}

function getStepX() {
    return currentX * tileSizeX[0] * maxZoom[0] / zoomFactor;
}

function getStepY() {
    return currentY * tileSizeY[0] * maxZoom[0] / zoomFactor;
}

function getViewBoxX() {
    return svgSpace.canWidth * maxZoom[0] / zoomFactor;
}

function getViewBoxY() {
    return svgSpace.canHeight * maxZoom[0] / zoomFactor;
}

function getVisibleBoxX(index) {
    var result = (svgSpace.canWidth * maxZoom[index]) / zoomFactor;
    if (result > max_sizeX[index]) {
	return max_sizeX[index];
    }
    return result;
}

function getVisibleBoxY(index) {
    var result = (svgSpace.canHeight * maxZoom[index]) / zoomFactor;
    if (result > max_sizeY[index]) {
	return max_sizeY[index];
    }
    return result;
}

function getColumnsNumber(index, zoomFactor) {
    return (max_sizeX[index] * zoomFactor)
	    / (tileSizeX[index] * maxZoom[index]);
}

function getRowsNumber(index, zoomFactor) {
    return (max_sizeY[index] * zoomFactor)
	    / (tileSizeY[index] * maxZoom[index]);
}

function getVisibleTilesX(index) {
    return svgSpace.canWidth / tileSizeX[index];
}

function getVisibleTilesY(index) {
    return svgSpace.canHeight / tileSizeY[index];
}

function normalizeX(currentX, zoomFactor) {
    if (currentX > getColumnsNumber(0, zoomFactor) - getVisibleTilesX(0)) {
	currentX = getColumnsNumber(0, zoomFactor) - getVisibleTilesX(0);
    }
    if (currentX < 0) {
	currentX = 0;
    }
    return currentX;
}

function normalizeY(currentY, zoomFactor) {
    if (currentY > getRowsNumber(0, zoomFactor) - getVisibleTilesY(0)) {
	currentY = getRowsNumber(0, zoomFactor) - getVisibleTilesY(0);
    }
    if (currentY < 0) {
	currentY = 0;
    }
    return currentY;
}

function convertToAbsolute(path) {
    var x0, y0, x1, y1, x2, y2;
    var segs = path.pathSegList;
    var x = 0;
    var y = 0;
    var size = segs.numberOfItems;
    for ( var i = 0; i < size; ++i) {
	var seg = segs.getItem(i);
	var c = seg.pathSegTypeAsLetter;
	if (/[MLC]/.test(c)) {
	    if ('x' in seg) {
		x = seg.x;
	    }
	    if ('y' in seg) {
		y = seg.y;
	    }
	} else {
	    if ('x1' in seg) {
		x1 = x + seg.x1;
	    }
	    if ('x2' in seg) {
		x2 = x + seg.x2;
	    }
	    if ('y1' in seg) {
		y1 = y + seg.y1;
	    }
	    if ('y2' in seg) {
		y2 = y + seg.y2;
	    }
	    if ('x' in seg) {
		x += seg.x;
	    }
	    if ('y' in seg) {
		y += seg.y;
	    }
	    switch (c) {
	    case 'm':
		segs.replaceItem(path.createSVGPathSegMovetoAbs(x, y), i);
		break;
	    case 'l':
		segs.replaceItem(path.createSVGPathSegLinetoAbs(x, y), i);
		break;
	    case 'c':
		segs.replaceItem(path.createSVGPathSegCurvetoCubicAbs(x, y, x1,
			y1, x2, y2), i);
		break;
	    case 'z':
		x = x0;
		y = y0;
		break;
	    case 'Z':
		x = x0;
		y = y0;
		break;
	    }
	}
	if (c == 'm' || c == 'M') {
	    x0 = x;
	    y0 = y;
	}
    }
}

function zoom(deltaX, deltaY, zoomParam) {
    var currValue = zoomFactor / minZoom[0];
    var slideValue = 1;
    while (currValue > 1) {
	slideValue = slideValue + 1;
	currValue = currValue / 2;
    }
    $("#zoomer").slider("option", "value", slideValue);
    $(window).trigger("stopPreload");
    svgCanvas.setSvgString(zoomFigures(deltaX * tileSizeX[0], deltaY
	    * tileSizeY[0], zoomParam, svgCanvas.getSvgString()));
    $(window).trigger("startPreload");
    $("#svgcanvas").trigger("refreshed");
}

function zoomEllipse(ellipseVal, deltaX, deltaY, zoomParam, viewIndexes) {
//    ellipseVal = setAttribute(ellipseVal, 0, ' fill-opacity="');
    ellipseVal = calculateFigureSize(ellipseVal, ' rx="', zoomParam);
    ellipseVal = calculateFigureSize(ellipseVal, ' ry="', zoomParam);
    ellipseVal = calculateFigureLocation(ellipseVal, ' cx="', deltaX, zoomParam);
    ellipseVal = calculateFigureLocation(ellipseVal, ' cy="', deltaY, zoomParam);
    if (viewIndexes) {
	ellipseVal = appendEllipseText(ellipseVal);
    }
    return ellipseVal;
}

function zoomPath(rectVal, deltaX, deltaY, zoomParam, viewIndexes) {
    var id = getFigureOffsetParam(rectVal, ' id="');
    var d = getFigureOffsetParam(rectVal, ' d="');
    var absolutePath = document.getElementById(id);
    if (absolutePath) {
	var oldValue = document.getElementById(id).outerHTML;
	convertToAbsolute(absolutePath);
	var newValue = document.getElementById(id).outerHTML;
	// also check using a pattern because in the mean time svg-edit migth
	// have switched to relative paths
	if (oldValue != newValue || /[mlc]/.test(d)) {
	    rectVal = setAttribute(rectVal, absolutePath.getAttribute('d'),
		    ' d="');
	}
    }
    if (rectVal.indexOf(rssufix) < 0) {
	rectVal = setAttribute(rectVal, 0, ' fill-opacity="');
    }
    if (rectVal.indexOf(rssufix) < 0) {
	rectVal = calculatePathLocation(rectVal, ' d="', deltaX, deltaY,
		zoomParam, zoomParam);
    } else {
	rectVal = calculatePathLocation(rectVal, ' d="', deltaX, deltaY,
		zoomParam, 1);
    }
    if (viewIndexes) {
	rectVal = appendPathText(rectVal);
    }
    return rectVal;
}

function zoomImage(imgVal, deltaX, deltaY, zoomParam) {
    imgVal = setAttribute(imgVal, 1, ' opacity="');
    if (imgVal.indexOf(sub_view) >= 0) {
    	imgVal = calculateImageLocation(imgVal, 0, 0, 1);
    } else {
    	if (imgVal.indexOf(rsprefix) > -1) {
    		imgVal = calculateFigureLocation(imgVal, ' x="', deltaX, zoomParam);
    		imgVal = calculateFigureLocation(imgVal, ' y="', deltaY, zoomParam);
    	} else {
    		imgVal = calculateImageLocation(imgVal, deltaX, deltaY, zoomParam);
    	}
    }
    return imgVal;
}

function zoomLine(rectVal, deltaX, deltaY, zoomParam, viewIndexes) {
//    rectVal = setAttribute(rectVal, 0, ' fill-opacity="');
    rectVal = calculateFigureLocation(rectVal, ' x1="', deltaX, zoomParam);
    rectVal = calculateFigureLocation(rectVal, ' y1="', deltaY, zoomParam);
    rectVal = calculateFigureLocation(rectVal, ' x2="', deltaX, zoomParam);
    rectVal = calculateFigureLocation(rectVal, ' y2="', deltaY, zoomParam);
    if (viewIndexes) {
	rectVal = appendLineText(rectVal);
    }
    return rectVal;
}

function zoomRect(rectVal, deltaX, deltaY, zoomParam, viewIndexes) {
//    rectVal = setAttribute(rectVal, 0, ' fill-opacity="');
    rectVal = calculateFigureSize(rectVal, ' width="', zoomParam);
    rectVal = calculateFigureSize(rectVal, ' height="', zoomParam);
    rectVal = calculateFigureLocation(rectVal, ' x="', deltaX, zoomParam);
    rectVal = calculateFigureLocation(rectVal, ' y="', deltaY, zoomParam);
    if (viewIndexes) {
	rectVal = appendRectText(rectVal);
    }
    return rectVal;
}

function zoomFigures(deltaX, deltaY, zoomParam, val) {
    var result = '';
    if (val.indexOf('<text') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<text'));
	    var afterVal = val.substring(val.indexOf('<text'));
	    var endTag = '</text>';
	    val = afterVal.substring(afterVal.indexOf(endTag) + endTag.length);
	    if (val.indexOf('<text') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<ellipse') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<ellipse'));
	    var afterVal = val.substring(val.indexOf('<ellipse'));
	    var ellipseVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    ellipseVal = zoomEllipse(ellipseVal, deltaX, deltaY, zoomParam,
		    svgSpace.viewIndexes);
	    result = result + ellipseVal;
	    val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<ellipse') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<rect') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<rect'));
	    var afterVal = val.substring(val.indexOf('<rect'));
	    var rectVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (rectVal.indexOf(sub_view_rect) >= 0) {
		if (subViewStepX == 0 || subViewStepY == 0) {
		    var img = val;
		    while (img.indexOf('<image') >= 0) {
			img = img.substring(img.indexOf('<image'));
			var imgVal = img.substring(0, img.indexOf('/>') + 2);
			img = img.substring(img.indexOf('/>') + 2);
			if (imgVal.indexOf(sub_view) >= 0) {
			    var frontImageX = imgVal.substring(imgVal
				    .indexOf(' x="') + 4);
			    frontImageX = frontImageX.substring(0, frontImageX
				    .indexOf('"'));
			    var frontImageY = imgVal.substring(imgVal
				    .indexOf(' y="') + 4);
			    frontImageY = frontImageY.substring(0, frontImageY
				    .indexOf('"'));
			    var frontSizeX = imgVal.substring(imgVal
				    .indexOf(' width="') + 8);
			    frontSizeX = frontSizeX.substring(0, frontSizeX
				    .indexOf('"'));
			    var frontSizeY = imgVal.substring(imgVal
				    .indexOf(' height="') + 9);
			    frontSizeY = frontSizeY.substring(0, frontSizeY
				    .indexOf('"'));
			    var stepLeft = frontSizeX * getStepX()
				    / max_sizeX[0];
			    var stepTop = frontSizeY * getStepY()
				    / max_sizeY[0];
			    var subWidth = frontSizeX * getVisibleBoxX(0)
				    / max_sizeX[0];
			    var subHeight = frontSizeY * getVisibleBoxY(0)
				    / max_sizeY[0];
			    rectVal = removeAttribute(rectVal, 'transform');
			    rectVal = calculateSubViewLocation(rectVal,
				    frontImageX * 1 + stepLeft, frontImageY * 1
					    + stepTop, subWidth, subHeight);
			    break;
			}
		    }
		}
	    } else {
		rectVal = zoomRect(rectVal, deltaX, deltaY, zoomParam,
			svgSpace.viewIndexes);
	    }
	    result = result + rectVal;
	    val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<rect') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<line') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<line'));
	    var afterVal = val.substring(val.indexOf('<line'));
	    var rectVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    rectVal = zoomLine(rectVal, deltaX, deltaY, zoomParam,
		    svgSpace.viewIndexes);
	    result = result + rectVal;
	    val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<line') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<path') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<path'));
	    var afterVal = val.substring(val.indexOf('<path'));
	    var rectVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    rectVal = zoomPath(rectVal, deltaX, deltaY, zoomParam,
		    svgSpace.viewIndexes);
	    result = result + rectVal;
	    val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<path') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    result = '';
    if (val.indexOf('<image') >= 0) {
	while (true) {
	    result = result + val.substring(0, val.indexOf('<image'));
	    var afterVal = val.substring(val.indexOf('<image'));
	    var imgVal = afterVal.substring(0, afterVal.indexOf('/>') + 2);
	    if (imgVal.indexOf(rsprefix) >= 0 || imgVal.indexOf(sub_view) >= 0) {
		imgVal = zoomImage(imgVal, deltaX, deltaY, zoomParam);
		result = result + imgVal;
	    }
	    var val = afterVal.substring(afterVal.indexOf('/>') + 2);
	    if (val.indexOf('<image') < 0) {
		result = result + val;
		break;
	    }
	}
    } else {
	result = val;
    }
    val = result;
    if (val.indexOf('xmlns:xlink') < 0) {
	result = val.substring(0, 4)
		+ ' xmlns:xlink="http://www.w3.org/1999/xlink"'
		+ val.substring(4);
	val = result;
    }
    result = val.substring(0, val.indexOf('<g>') + 3);
    result = result
	    + getImages(deltaX, deltaY, zoomParam, currentX, currentY,
		    zoomFactor);
    $(window).trigger("startPreload");
    var afterVal = val.substring(val.indexOf('<g>') + 3);
    if (afterVal.indexOf(sub_view) < 0) {
	var frontSizeX = 160;
	var frontSizeY = 160 * max_sizeY[0] / max_sizeX[0];
	if (max_sizeX[0] < max_sizeY[0]) {
	    frontSizeY = frontSizeX * max_sizeY[0] / max_sizeX[0];
	} else if (max_sizeY[0] < max_sizeX[0]) {
	    frontSizeX = frontSizeY * max_sizeX[0] / max_sizeY[0];
	}
	var frontImageX = svgSpace.canWidth - frontSizeX;
	var frontImageY = 0;
	var newAfterVal = afterVal.substring(0, afterVal.indexOf('</g>'));
	newAfterVal = newAfterVal + '<image x="' + frontImageX + '" y="'
		+ frontImageY + '" width="' + frontSizeX + '" height="'
		+ frontSizeY + '" id="' + sub_view + '" xlink:href="'
		+ getScaledURL(svgSpace.images[0], frontSizeX) + '"/>';
	newAfterVal = newAfterVal + '<rect x="' + frontImageX + '" y="'
		+ frontImageY + '" width="' + frontSizeX + '" height="'
		+ frontSizeY + '" id="' + sub_view_rect
		+ '" fill="#transparent" stroke="#ffff00" fill-opacity="0"/>';
	newAfterVal = newAfterVal
		+ afterVal.substring(afterVal.indexOf('</g>'));
	afterVal = newAfterVal;
    }
    result = result + afterVal;
    return result;
}

function getImagesIIP(deltaX, deltaY, zoomParam, currentX, currentY,
	zoomFactor, disablePreload) {
    var result = '';
    for ( var t = svgSpace.images.length - 1; t >= 0; t--) {
	if (svgSpace.viewAllImages == false && svgSpace.orders[t] != 0) {
	    continue;
	}
	var p = svgSpace.orders[t];
	var zoomIndex = maxZoom[p] / zoomFactor;
	var offsetI = currentX;
	var offsetJ = currentY;
	var iStart = Math.floor(currentX);
	var iEnd = Math.floor(currentX) + 1;
	var jStart = Math.floor(currentY);
	var jEnd = Math.floor(currentY) + 1;
	if (zoomIndex < 1) {
	    offsetI = currentX * zoomIndex;
	    offsetJ = currentY * zoomIndex;
	    iStart = Math.floor(currentX * zoomIndex);
	    iEnd = Math.floor(currentX * zoomIndex) + 1;
	    jStart = Math.floor(currentY * zoomIndex);
	    jEnd = Math.floor(currentY * zoomIndex) + 1;
	}
	var rasterIndex = 1;
	if (zoomIndex < 1) {
	    rasterIndex = rasterIndex / zoomIndex;
	}
	var stepX = tileSizeX[p] * zoomIndex;
	var stepY = tileSizeY[p] * zoomIndex;
	for (i = iStart; i < iEnd + getVisibleTilesX(p) / rasterIndex; i++) {
	    var currWidth = tileSizeX[p] * 1;
	    if (i * stepX > max_sizeX[p] || i < 0) {
		continue;
	    }
	    if ((i + 1) * stepX > max_sizeX[p] / rasterIndex) {
		currWidth = (max_sizeX[p] / rasterIndex - i * stepX)
			/ zoomIndex;
	    }
	    if (zoomIndex < 1) {
		currWidth = currWidth / zoomIndex;
	    }
	    for (j = jStart; j < jEnd + getVisibleTilesY(p) / rasterIndex; j++) {
		var currHeight = tileSizeY[p] * 1;
		if (j * stepY > max_sizeY[p] || j < 0) {
		    continue;
		}
		if ((j + 1) * stepY > max_sizeY[p] / rasterIndex) {
		    currHeight = (max_sizeY[p] / rasterIndex - j * stepY)
			    / zoomIndex;
		}
		if (zoomIndex < 1) {
		    currHeight = currHeight / zoomIndex;
		}
		var fullSize = Math.floor(getColumnsNumber(p, zoomFactor));
		if (getColumnsNumber(p, zoomFactor) > fullSize) {
		    fullSize = fullSize + 1;
		}
		if (zoomIndex < 1) {
		    fullSize = fullSize * zoomIndex;
		}
		var k = j * fullSize + i;
		var index = 0;
		var temp = zoomFactor;
		while (temp > 1) {
		    index = index + 1;
		    temp = temp / 2;
		}
		var iter = zoomIndex;
		while (iter < 1) {
		    index = index - 1;
		    iter = iter * 2;
		}
		if (svgSpace.visibilities[p] == true
			|| svgSpace.viewAllImages == false) {
		    var currOpacity = svgSpace.opacities[p];
		    if (svgSpace.viewAllImages == false) {
			currOpacity = 1;
		    }
		    var rezX = Math.round((i - offsetI) * tileSizeX[p]
			    * rasterIndex);
		    var rezY = Math.round((j - offsetJ) * tileSizeY[p]
			    * rasterIndex);
		    var href = '';
		    href = getTileURLIIP(svgSpace.images[p], index, '0,90',
			    '1', k);
		    result = result + '<image x="' + rezX + '" y="' + rezY
			    + '" width="' + currWidth + '" height="'
			    + currHeight + '" style="filter: url(\'#filter' + p
			    + '\');" id="backimage' + p + i + j
			    + '" xlink:href="' + href + '" opacity="'
			    + currOpacity + '" />';
		    svgSpace.addTile(p, zoomFactor, i, j, href);
		}
	    }
	}
    }
    if (!disablePreload) {
	if (canZoomIn()) {
	    var oldZoom = zoomFactor;
	    var zoomFactor1 = zoomFactor * 2;
	    var oldX = currentX;
	    var currentX1 = currentX * 2 + getVisibleTilesX(0) / 4;
	    var oldY = currentY;
	    var currentY1 = currentY * 2 + getVisibleTilesY(0) / 4;
	    currentX1 = normalizeX(currentX1, zoomFactor1);
	    currentY1 = normalizeY(currentY1, zoomFactor1);
	    getImages(oldX * (zoomFactor1 / oldZoom) - currentX1, oldY
		    * (zoomFactor1 / oldZoom) - currentY1, zoomFactor1
		    / oldZoom, currentX1, currentY1, zoomFactor1, true);
	}
	if (canZoomOut()) {
	    var oldZoom = zoomFactor;
	    var zoomFactor1 = zoomFactor / 2;
	    var oldX = currentX;
	    var currentX1 = (currentX - getVisibleTilesX(0) / 4) / 2;
	    var oldY = currentY;
	    var currentY1 = (currentY - getVisibleTilesY(0) / 4) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor1);
	    currentY1 = normalizeY(currentY1, zoomFactor1);
	    getImages(oldX * (zoomFactor1 / oldZoom) - currentX1, oldY
		    * (zoomFactor1 / oldZoom) - currentY1, zoomFactor1
		    / oldZoom, currentX1, currentY1, zoomFactor1, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX + getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1);
	    var currentY1 = currentY + getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX + getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1);
	    var currentY1 = currentY - getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX - getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1);
	    var currentY1 = currentY + getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX - getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1);
	    var currentY1 = currentY - getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
    }
    return result;
}

function getImagesIIIF(deltaX, deltaY, zoomParam, currentX, currentY,
	zoomFactor, disablePreload) {
    var result = '';
    var canWidth = svgSpace.canWidth;
    if (svgSpace.canWidth == svgSpace.fullX) {
	canWidth = 10 * Math.round(svgSpace.canWidth / 10);
    }
    var canHeight = svgSpace.canHeight;
    if (svgSpace.canHeight == svgSpace.fullY) {
	canHeight = 10 * Math.round(svgSpace.canHeight / 10);
    }
    for ( var t = svgSpace.images.length - 1; t >= 0; t--) {
	if (svgSpace.viewAllImages == false && svgSpace.orders[t] != 0) {
	    continue;
	}
	var p = svgSpace.orders[t];
	var zoomIndex = maxZoom[p] / zoomFactor;
	var x = round(currentX * zoomIndex * tileSizeX[p]);
	var y = round(currentY * zoomIndex * tileSizeY[p]);
	if (svgSpace.visibilities[p] == true || svgSpace.viewAllImages == false) {
	    var currOpacity = svgSpace.opacities[p];
	    if (svgSpace.viewAllImages == false) {
		currOpacity = 1;
	    }
	    var href = '';
	    var currWidth = canWidth;
	    if (currWidth * zoomIndex > max_sizeX - x) {
		currWidth = round((max_sizeX - x) / zoomIndex);
	    }
	    var currHeight = canHeight;
	    if (currHeight * zoomIndex > max_sizeY - y) {
		currHeight = round((max_sizeY - y) / zoomIndex);
	    }
	    var u = currWidth * zoomIndex;
	    var v = currHeight * zoomIndex;
	    var searchResult = svgSpace.findTile(p, zoomFactor, x - currWidth,
		    y - currHeight, x + currWidth, y + currHeight);
	    if (searchResult) {
		var xNew = searchResult.x * 1;
		var yNew = searchResult.y * 1;
		currWidth = canWidth;
		if (currWidth * zoomIndex > max_sizeX - xNew) {
		    currWidth = round((max_sizeX - xNew) / zoomIndex);
		}
		currHeight = canHeight;
		if (currHeight * zoomIndex > max_sizeY - yNew) {
		    currHeight = round((max_sizeY - yNew) / zoomIndex);
		}
		result = result
			+ addImageTile(p, xNew, yNew, currWidth, currHeight, x,
				y, zoomIndex, currOpacity, 0, 0);
		if (xNew > x) {
		    var xNew2 = xNew - canWidth;
		    if (xNew2 < 0) {
			xNew2 = 0;
		    }
		    var currWidth2 = (xNew - xNew2) / zoomIndex;
		    if (yNew > y) {
			var yNew2 = yNew - canHeight;
			if (yNew2 < 0) {
			    yNew2 = 0;
			}
			var currHeight2 = (yNew - yNew2) / zoomIndex;
			result = result
				+ addImageTile(p, xNew2, yNew2, currWidth2,
					currHeight2, x, y, zoomIndex,
					currOpacity, 0, 1);
			result = result
				+ addImageTile(p, xNew, yNew2, currWidth,
					currHeight2, x, y, zoomIndex,
					currOpacity, 1, 0);
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 1, 1);
		    } else if (yNew < y) {
			var yNew2 = yNew + currHeight;
			var currHeight2 = canHeight;
			if (currHeight2 * zoomIndex > max_sizeY - yNew2) {
			    currHeight2 = round((max_sizeY - yNew2) / zoomIndex);
			}
			result = result
				+ addImageTile(p, xNew2, yNew2, currWidth2,
					currHeight2, x, y, zoomIndex,
					currOpacity, 0, 1);
			result = result
				+ addImageTile(p, xNew, yNew2, currWidth,
					currHeight2, x, y, zoomIndex,
					currOpacity, 1, 0);
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 1, 1);
		    } else {
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 0, 1);
		    }
		} else if (xNew < x) {
		    var xNew2 = xNew + currWidth;
		    var currWidth2 = canWidth;
		    if (currWidth2 * zoomIndex > max_sizeX - xNew2) {
			currWidth2 = Math
				.round((max_sizeX - xNew2) / zoomIndex);
		    }
		    if (yNew > y) {
			var yNew2 = yNew - canHeight;
			if (yNew2 < 0) {
			    yNew2 = 0;
			}
			var currHeight2 = (yNew - yNew2) / zoomIndex;
			result = result
				+ addImageTile(p, xNew2, yNew2, currWidth2,
					currHeight2, x, y, zoomIndex,
					currOpacity, 0, 1);
			result = result
				+ addImageTile(p, xNew, yNew2, currWidth,
					currHeight2, x, y, zoomIndex,
					currOpacity, 1, 0);
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 1, 1);
		    } else if (yNew < y) {
			var yNew2 = yNew + currHeight;
			var currHeight2 = canHeight;
			if (currHeight2 * zoomIndex > max_sizeY - yNew2) {
			    currHeight2 = round(
				    (max_sizeY - yNew2) / zoomIndex, 0, 1);
			}
			result = result
				+ addImageTile(p, xNew2, yNew2, currWidth2,
					currHeight2, x, y, zoomIndex,
					currOpacity, 0, 1);
			result = result
				+ addImageTile(p, xNew, yNew2, currWidth,
					currHeight2, x, y, zoomIndex,
					currOpacity, 1, 0);
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 1, 1);
		    } else {
			result = result
				+ addImageTile(p, xNew2, yNew, currWidth2,
					currHeight, x, y, zoomIndex,
					currOpacity, 0, 1);
		    }
		} else if (yNew > y) {
		    var yNew2 = yNew - canHeight;
		    if (yNew2 < 0) {
			yNew2 = 0;
		    }
		    var currHeight2 = (yNew - yNew2) / zoomIndex;
		    result = result
			    + addImageTile(p, xNew, yNew2, currWidth,
				    currHeight2, x, y, zoomIndex, currOpacity,
				    0, 1);
		} else if (yNew < y) {
		    var yNew2 = yNew + currHeight;
		    var currHeight2 = canHeight;
		    if (currHeight2 * zoomIndex > max_sizeY - yNew2) {
			currHeight2 = round((max_sizeY - yNew2) / zoomIndex);
		    }
		    result = result
			    + addImageTile(p, xNew, yNew2, currWidth,
				    currHeight2, x, y, zoomIndex, currOpacity,
				    0, 1);
		}
	    } else {
		result = result
			+ addImageTile(p, x, y, currWidth, currHeight, x, y,
				zoomIndex, currOpacity, 0, 0);
	    }
	}
    }
    if (!disablePreload) {
	if (canZoomIn()) {
	    var oldZoom = zoomFactor;
	    var zoomFactor1 = zoomFactor * 2;
	    var oldX = currentX;
	    var currentX1 = currentX * 2 + getVisibleTilesX(0) / 4;
	    var oldY = currentY;
	    var currentY1 = currentY * 2 + getVisibleTilesY(0) / 4;
	    currentX1 = normalizeX(currentX1, zoomFactor1);
	    currentY1 = normalizeY(currentY1, zoomFactor1);
	    getImages(oldX * (zoomFactor1 / oldZoom) - currentX1, oldY
		    * (zoomFactor1 / oldZoom) - currentY1, zoomFactor1
		    / oldZoom, currentX1, currentY1, zoomFactor1, true);
	}
	if (canZoomOut()) {
	    var oldZoom = zoomFactor;
	    var zoomFactor1 = zoomFactor / 2;
	    var oldX = currentX;
	    var currentX1 = (currentX - getVisibleTilesX(0) / 4) / 2;
	    var oldY = currentY;
	    var currentY1 = (currentY - getVisibleTilesY(0) / 4) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor1);
	    currentY1 = normalizeY(currentY1, zoomFactor1);
	    getImages(oldX * (zoomFactor1 / oldZoom) - currentX1, oldY
		    * (zoomFactor1 / oldZoom) - currentY1, zoomFactor1
		    / oldZoom, currentX1, currentY1, zoomFactor1, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX + getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor);
	    var currentY1 = currentY + getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1, zoomFactor);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX + getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor);
	    var currentY1 = currentY - getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1, zoomFactor);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX - getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor);
	    var currentY1 = currentY + getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1, zoomFactor);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
	{
	    var oldX = currentX;
	    var oldY = currentY;
	    var currentX1 = currentX - getVisibleTilesX(0) / 2;
	    currentX1 = normalizeX(currentX1, zoomFactor);
	    var currentY1 = currentY - getVisibleTilesY(0) / 2;
	    currentY1 = normalizeY(currentY1, zoomFactor);
	    getImages(oldX * (zoomFactor / oldZoom) - currentX1, oldY
		    * (zoomFactor / oldZoom) - currentY1, zoomFactor / oldZoom,
		    currentX1, currentY1, zoomFactor, true);
	}
    }
    return result;
}

function getImagesHTTP(deltaX, deltaY, zoomParam, currentX, currentY,
	zoomFactor, disablePreload) {
    var result = '';
    var singleImageAdded = false;
    for ( var t = svgSpace.images.length - 1; t >= 0; t--) {
	if (svgSpace.viewAllImages == false && svgSpace.orders[t] != 0) {
	    continue;
	}
	var p = svgSpace.orders[t];
	var zoomIndex = maxZoom[p] / zoomFactor;
	var offsetI = currentX;
	var offsetJ = currentY;
	var iStart = Math.floor(currentX);
	var iEnd = Math.floor(currentX) + 1;
	var jStart = Math.floor(currentY);
	var jEnd = Math.floor(currentY) + 1;
	if (zoomIndex < 1) {
	    offsetI = currentX * zoomIndex;
	    offsetJ = currentY * zoomIndex;
	    iStart = Math.floor(currentX * zoomIndex);
	    iEnd = Math.floor(currentX * zoomIndex) + 1;
	    jStart = Math.floor(currentY * zoomIndex);
	    jEnd = Math.floor(currentY * zoomIndex) + 1;
	}
	var rasterIndex = 1;
	if (zoomIndex < 1) {
	    rasterIndex = rasterIndex / zoomIndex;
	}
	var stepX = tileSizeX[p] * zoomIndex;
	var stepY = tileSizeY[p] * zoomIndex;
	iStart = 0;
	iEnd = 0;
	jStart = 0;
	jEnd = 0;
	stepX = stepX * maxZoom[p] / zoomIndex;
	stepY = stepY * maxZoom[p] / zoomIndex;
	for (i = iStart; i < iEnd + getVisibleTilesX(p) / rasterIndex; i++) {
	    var currWidth = tileSizeX[p] * 1;
	    currWidth = currWidth * maxZoom[p] / zoomIndex;
	    if (i * stepX > max_sizeX[p] || i < 0) {
		continue;
	    }
	    if ((i + 1) * stepX > max_sizeX[p] / rasterIndex) {
		currWidth = (max_sizeX[p] / rasterIndex - i * stepX)
			/ zoomIndex;
	    }
	    if (zoomIndex < 1) {
		currWidth = currWidth / zoomIndex;
	    }
	    for (j = jStart; j < jEnd + getVisibleTilesY(p) / rasterIndex; j++) {
		var currHeight = tileSizeY[p] * 1;
		currHeight = currHeight * maxZoom[p] / zoomIndex;
		if (j * stepY > max_sizeY[p] || j < 0) {
		    continue;
		}
		if ((j + 1) * stepY > max_sizeY[p] / rasterIndex) {
		    currHeight = (max_sizeY[p] / rasterIndex - j * stepY)
			    / zoomIndex;
		}
		if (zoomIndex < 1) {
		    currHeight = currHeight / zoomIndex;
		}
		var fullSize = Math.floor(getColumnsNumber(p, zoomFactor));
		if (getColumnsNumber(p, zoomFactor) > fullSize) {
		    fullSize = fullSize + 1;
		}
		if (zoomIndex < 1) {
		    fullSize = fullSize * zoomIndex;
		}
		var k = j * fullSize + i;
		var index = 0;
		var temp = zoomFactor;
		while (temp > 1) {
		    index = index + 1;
		    temp = temp / 2;
		}
		var iter = zoomIndex;
		while (iter < 1) {
		    index = index - 1;
		    iter = iter * 2;
		}
		if (svgSpace.visibilities[p] == true
			|| svgSpace.viewAllImages == false) {
		    var currOpacity = svgSpace.opacities[p];
		    if (svgSpace.viewAllImages == false) {
			currOpacity = 1;
		    }
		    var rezX = Math.round((i - offsetI) * tileSizeX[p]
			    * rasterIndex);
		    var rezY = Math.round((j - offsetJ) * tileSizeY[p]
			    * rasterIndex);
		    var href = '';
		    href = getTileURLHTTP(svgSpace.images[p], index, '0,90',
			    '1', k);
		    if (!singleImageAdded) {
			result = result + '<image x="' + rezX + '" y="' + rezY
				+ '" width="' + currWidth + '" height="'
				+ currHeight + '" style="filter: url(\'#filter'
				+ p + '\');" id="backimage' + p + i + j
				+ '" xlink:href="' + href + '" opacity="'
				+ currOpacity + '" />';
			singleImageAdded = true;
		    }
		    svgSpace.addTile(p, zoomFactor, i, j, href);
		}
	    }
	}
    }
    return result;
}

function round(value) {
    var result = Math.round(value - 0.5);
    return result;
}

function addImageTile(p, xNew, yNew, currWidth, currHeight, x, y, zoomIndex,
	opacity, i, j) {
    u = currWidth * zoomIndex;
    v = currHeight * zoomIndex;
    var xImg = (xNew - x) / zoomIndex;
    var yImg = (yNew - y) / zoomIndex;
    href = getTileURLIIIF(svgSpace.images[p], xNew, yNew, u, v, currWidth,
	    currHeight);
    var result = '<image x="' + xImg + '" y="' + yImg + '" width="' + currWidth
	    + '" height="' + currHeight + '" style="filter: url(\'#filter' + p
	    + '\');" id="backimage' + p + i + j + '" xlink:href="' + href
	    + '" opacity="' + opacity + '" />';
    svgSpace.addTile(p, zoomFactor, xNew, yNew, href);
    return result;
}

function getImages(deltaX, deltaY, zoomParam, currentX, currentY, zoomFactor,
	disablePreload) {
    if (svgSpace.imageProtocol == svgSpace.protocols[0]) {
	return getImagesIIP(deltaX, deltaY, zoomParam, currentX, currentY,
		zoomFactor, disablePreload);
    }
    if (svgSpace.imageProtocol == svgSpace.protocols[1]) {
	return getImagesIIIF(deltaX, deltaY, zoomParam, currentX, currentY,
		zoomFactor, disablePreload);
    }
    return getImagesHTTP(deltaX, deltaY, zoomParam, currentX, currentY,
	    zoomFactor, disablePreload);
}

function calculateSubViewLocation(figureVal, deltaX, deltaY, sizeX, sizeY) {
    var nameSizeX = ' width="';
    if (figureVal.indexOf(nameSizeX) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameSizeX)
		+ nameSizeX.length);
	var x = figureVal.substring(figureVal.indexOf(nameSizeX)
		+ nameSizeX.length);
	var afterName = x.substring(x.indexOf('"'));
	figureVal = beforeName + sizeX + afterName;
    }
    var nameSizeY = ' height="';
    if (figureVal.indexOf(nameSizeY) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameSizeY)
		+ nameSizeY.length);
	var x = figureVal.substring(figureVal.indexOf(nameSizeY)
		+ nameSizeY.length);
	var afterName = x.substring(x.indexOf('"'));
	figureVal = beforeName + sizeY + afterName;
    }
    var nameX = ' x="';
    if (figureVal.indexOf(nameX) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameX)
		+ nameX.length);
	var x = figureVal.substring(figureVal.indexOf(nameX) + nameX.length);
	var afterName = x.substring(x.indexOf('"'));
	figureVal = beforeName + deltaX + afterName;
    }
    var nameY = ' y="';
    if (figureVal.indexOf(nameY) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameY)
		+ nameY.length);
	var y = figureVal.substring(figureVal.indexOf(nameY) + nameY.length);
	var afterName = y.substring(y.indexOf('"'));
	figureVal = beforeName + deltaY + afterName;
    }
    return figureVal;
}

function calculateSubViewDelta(wrapper, wrapperLength, real, realLength, delta) {
    if (wrapper * 1 - real * 1 > delta * 1) {
	return wrapper * 1 - real * 1;
    }
    if (wrapper * 1 + wrapperLength * 1 - real * 1 - realLength * 1 < delta * 1) {
	return wrapper * 1 + wrapperLength * 1 - real * 1 - realLength * 1;
    }
    return delta;
}

function getMetaDataURLIIP(image) {
    return svgSpace.server + "?FIF=" + image
	    + "&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number";
}

function getMetaDataURLIIIF(image) {
    return image + "/info.xml";
}

function getMetaDataURLHTTP(image) {
    return svgSpace.sizeService + "?uri=" + image;
}

function getTileURLIIP(image, resolution, sds, contrast, k) {
    return svgSpace.server + "?FIF=" + image + "&amp;CNT=" + contrast
	    + "&amp;SDS=" + sds + "&amp;JTL=" + resolution + "," + k;
}

function getTileURLIIIF(image, x, y, u, v, sizeX, sizeY) {
    return image + "/" + x + "," + y + "," + u + "," + v + "/!" + sizeX + ","
	    + sizeY + "/0/native.jpg";
}

function getTileURLHTTP(image, resolution, sds, contrast, k) {
    return image;
}

function getScaledURL(image, width) {
    if (svgSpace.imageProtocol == svgSpace.protocols[0]) {
	return svgSpace.server + "?FIF=" + image + "&amp;WID=" + width
		+ "&amp;CVT=JPEG";
    } else if (svgSpace.imageProtocol == svgSpace.protocols[1]) {
	return svgSpace.server + "/full/" + width + ",/0/native.jpg";
    }
    return svgSpace.server;
}

function canZoomIn() {
    if (zoomFactor < maxZoom[0]) {
	return true;
    }
    false;
}

function zoomIn() {
    if (canZoomIn()) {
	var oldZoom = zoomFactor;
	zoomFactor = zoomFactor * 2;
	var oldX = currentX;
	currentX = currentX * 2 + getVisibleTilesX(0) / 4;
	var oldY = currentY;
	currentY = currentY * 2 + getVisibleTilesY(0) / 4;
	currentX = normalizeX(currentX, zoomFactor);
	currentY = normalizeY(currentY, zoomFactor);
	zoom(oldX * (zoomFactor / oldZoom) - currentX, oldY
		* (zoomFactor / oldZoom) - currentY, zoomFactor / oldZoom);
    }
}

function canZoomOut() {
    if (zoomFactor > minZoom[0]) {
	return true;
    }
    false;
}

function zoomOut() {
    if (canZoomOut()) {
	var oldZoom = zoomFactor;
	zoomFactor = zoomFactor / 2;
	var oldX = currentX;
	currentX = (currentX - getVisibleTilesX(0) / 4) / 2;
	var oldY = currentY;
	currentY = (currentY - getVisibleTilesY(0) / 4) / 2;
	currentX = normalizeX(currentX, zoomFactor);
	currentY = normalizeY(currentY, zoomFactor);
	zoom(oldX * (zoomFactor / oldZoom) - currentX, oldY
		* (zoomFactor / oldZoom) - currentY, zoomFactor / oldZoom);
    }
}

function loadImage(p, hasToReloadZoom) {
    if (p == 0) {
	max_sizeX = new Array(svgSpace.images.length);
	max_sizeY = new Array(svgSpace.images.length);
	tileSizeX = new Array(svgSpace.images.length);
	tileSizeY = new Array(svgSpace.images.length);
	minZoom = new Array(svgSpace.images.length);
	maxZoom = new Array(svgSpace.images.length);
    }
    var xmlhttp;
    if (window.XMLHttpRequest) {
	xmlhttp = new XMLHttpRequest();
    } else {
	xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }
    function handleResponseText(response, p) {
		max_sizeX[p] = response.maxWidth * 1;
		max_sizeY[p] = response.maxHeight * 1;
		tileSizeX[p] = response.width * 1;
		tileSizeY[p] = response.height * 1;
		num_resolutions = response.resolutionNumber * 1;
		maxZoom[p] = Math.pow(2, num_resolutions - 1);
		var currZoom = zoomFactor;
		zoomFactor = 1;
		while (getColumnsNumber(0, zoomFactor) * tileSizeX[0] <= svgSpace.canWidth / 2
			&& getRowsNumber(0, zoomFactor) * tileSizeY[0] <= svgSpace.canHeight / 2
			&& canZoomIn()) {
		    zoomFactor = zoomFactor * 2;
		}
		var optimalZoom = zoomFactor;
		minZoom[p] = 1;
		zoomFactor = currZoom;
		if (minZoom[0] > zoomFactor) {
		    zoomFactor = minZoom[0];
		}
		if (p + 1 < svgSpace.images.length) {
		    loadImage(p + 1, false);
		} else {
		    zoomFactor = optimalZoom;
		    var val = svgCanvas.getSvgString().toString();
		    if (val.indexOf('Image Annotation') < 0) {
			var val = '<svg width="'
				+ svgSpace.canWidth
				+ '" height="'
				+ svgSpace.canHeight
				+ '" xmlns="http://www.w3.org/2000/svg"><!-- Image Annotation --><g><title>Image Annotation</title></g></svg>';
		    }
		    $(window).trigger("stopPreload");
		    svgCanvas.setSvgString(zoomFigures(0, 0, 1, val));
		    $(window).trigger("startPreload");
		    for (z = 0; z < svgSpace.images.length; z++) {
			constructSlider(z);
		    }
		}
		/**
		 * We will need custom event when image is loaded.
		 */
		$(window).trigger("imageToBeAnnotatedIsLoaded");
    }

    svgSpace.imageProtocol = svgSpace.getImageProtocol(svgSpace.images[p]);
    if (svgSpace.imageProtocol == svgSpace.protocols[0]) {
	xmlhttp.onreadystatechange = function() {
	    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
		var responseText = xmlhttp.responseText.toString();
		if (responseText != "Not found") {
		    var response = {};
		    var tmp = responseText.split("Max-size");
		    var size = tmp[1].split(" ");
		    response.maxWidth = size[0].substring(1, size[0].length);
		    response.maxHeight = size[1].split("Tile-size")[0].trim();
		    tmp = responseText.split("Tile-size");
		    size = tmp[1].split(" ");
		    response.width = size[0].substring(1, size[0].length);
		    response.height = size[1].split("Resolution-number")[0]
			    .trim();
		    tmp = responseText.split("Resolution-number");
		    response.resolutionNumber = tmp[1].substring(1,
			    tmp[1].length).trim() * 1;
		    handleResponseText(response, p);
		}
	    }
	};
	xmlhttp.open("GET", getMetaDataURLIIP(svgSpace.images[p]), false);
	xmlhttp.send();
    } else if (svgSpace.imageProtocol == svgSpace.protocols[1]) {
	svgSpace.server = svgSpace.images[p];
	xmlhttp.onreadystatechange = function() {
	    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
		var responseText = xmlhttp.responseText.toString();
		if (responseText != "Not found") {
		    var tempWidth = $(responseText).find('width').html();
		    var tempHeight = $(responseText).find('height').html();
		    var resolutionNumber = 1;
		    while (svgSpace.tileWidth < tempWidth
			    || svgSpace.tileHeight < tempHeight) {
			tempWidth = tempWidth / 2;
			tempHeight = tempHeight / 2;
			resolutionNumber = resolutionNumber + 1;
		    }
		    var response = {};
		    response.width = svgSpace.tileWidth;
		    response.height = svgSpace.tileHeight;
		    response.maxWidth = $(responseText).find('width').html();
		    response.maxHeight = $(responseText).find('height').html();
		    response.resolutionNumber = resolutionNumber;
		    handleResponseText(response, p);
		}
	    }
	};
	xmlhttp.open("GET", getMetaDataURLIIIF(svgSpace.images[p]), false);
	xmlhttp.send();
    } else {
	svgSpace.server = svgSpace.images[p];
	if (svgSpace.sizeService != null) {
	    $.ajax({
		type : "GET",
		url : getMetaDataURLHTTP(svgSpace.images[p]),
		data : {},
		dataType : "json",
		cache : false,
		async : false
	    }).done(
		    function(data) {
			var tempWidth = data.width;
			var tempHeight = data.height;
			var resolutionNumber = 1;
			while (svgSpace.canWidth < tempWidth
				|| svgSpace.canHeight < tempHeight) {
			    tempWidth = tempWidth / 2;
			    tempHeight = tempHeight / 2;
			    resolutionNumber = resolutionNumber + 1;
			}
			var response = {};
			response.width = tempWidth;
			response.height = tempHeight;
			response.maxWidth = data.width;
			response.maxHeight = data.height;
			response.resolutionNumber = resolutionNumber;
			handleResponseText(response, p);
		    });
	} else {
	    $("<img/>").attr("src", svgSpace.images[p]).load(
		    function() {
			var tempWidth = this.width;
			var tempHeight = this.height;
			var resolutionNumber = 1;
			while (svgSpace.canWidth < tempWidth
				|| svgSpace.canHeight < tempHeight) {
			    tempWidth = tempWidth / 2;
			    tempHeight = tempHeight / 2;
			    resolutionNumber = resolutionNumber + 1;
			}
			var response = {};
			response.width = tempWidth;
			response.height = tempHeight;
			response.maxWidth = this.width;
			response.maxHeight = this.height;
			response.resolutionNumber = resolutionNumber;
			handleResponseText(response, p);
		    });
	}
    }
}

function calculatePathLocation(figureVal, name, deltaX, deltaY, zoomParam,
	startZoomParam) {
    if (figureVal.indexOf(name) < 0) {
	return figureVal.substring(0, figureVal.indexOf('/>')) + ' ' + name
		+ 'M0,0Z"/>';
    }
    var beforeName = figureVal.substring(0, figureVal.indexOf(name)
	    + name.length);
    var currName = figureVal.substring(figureVal.indexOf(name) + name.length);
    var afterName = currName.substring(currName.indexOf('"'));
    currName = currName.substring(0, currName.indexOf('"'));
    currName = currName.substring(0, currName.length);
    var originalName = currName;
    currName = '';
    var oldRootValueX = null;
    var newRootValueX = null;
    var oldRootValueY = null;
    var newRootValueY = null;
    while (originalName.length > 0) {
	var currentNode = getNextNode(originalName);
	var hasEnd = false;
	if (currentNode.indexOf('z') >= 0 || currentNode.indexOf('Z') >= 0) {
	    currentNode = currentNode.substring(0, currentNode.length - 1);
	    hasEnd = true;
	}
	var result = '';
	var xy = currentNode.substring(1).replace(/^\s+|\s+$/g, "").replace(
		/ /gi, ",").split(',');
	var k;
	if (oldRootValueX == null && newRootValueX == null) {
	    oldRootValueX = xy[0];
	    newRootValueX = xy[0] * zoomParam + deltaX;
	}
	if (oldRootValueY == null && newRootValueY == null) {
	    oldRootValueY = xy[1];
	    newRootValueY = xy[1] * zoomParam + deltaY;
	}
	if (currentNode.indexOf('M') >= 0) {
	    result = result + 'M';
	    k = 0;
	    while (k < xy.length) {
		if (xy.length > 2) {
		    result = result + ' ';
		}
		result = result + (xy[k + 0] * zoomParam + deltaX);
		result = result + ',';
		result = result + (xy[k + 1] * zoomParam + deltaY);
		k = k + 2;
	    }
	} else if (currentNode.indexOf('L') >= 0) {
	    result = result + 'L';
	    k = 0;
	    while (k < xy.length) {
		if (xy.length > 2) {
		    result = result + ' ';
		}
		result = result
			+ newValue(oldRootValueX, newRootValueX, xy[k + 0],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueY, newRootValueY, xy[k + 1],
				startZoomParam);
		k = k + 2;
	    }
	} else if (currentNode.indexOf('C') >= 0) {
	    result = result + 'C';
	    k = 0;
	    while (k < xy.length) {
		if (xy.length > 6) {
		    result = result + ' ';
		}
		result = result
			+ newValue(oldRootValueX, newRootValueX, xy[k + 0],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueY, newRootValueY, xy[k + 1],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueX, newRootValueX, xy[k + 2],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueY, newRootValueY, xy[k + 3],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueX, newRootValueX, xy[k + 4],
				startZoomParam);
		result = result + ',';
		result = result
			+ newValue(oldRootValueY, newRootValueY, xy[k + 5],
				startZoomParam);
		k = k + 6;
	    }
	}
	if (hasEnd == true) {
	    result = result + 'Z';
	}
	currName = currName + result;
	originalName = getLaterNodes(originalName);
    }
    return beforeName + currName + afterName;
}

function newValue(oldRootValue, newRootValue, oldValue, zoom) {
    return newRootValue + (oldValue - oldRootValue) * zoom;
}

function getNextNode(currName) {
    var resultLength = currName.substring(1).length;
    if (currName.substring(1).indexOf('l') != -1
	    && resultLength > currName.substring(1).indexOf('l')) {
	resultLength = currName.substring(1).indexOf('l');
    }
    if (currName.substring(1).indexOf('L') != -1
	    && resultLength > currName.substring(1).indexOf('L')) {
	resultLength = currName.substring(1).indexOf('L');
    }
    if (currName.substring(1).indexOf('c') != -1
	    && resultLength > currName.substring(1).indexOf('c')) {
	resultLength = currName.substring(1).indexOf('c');
    }
    if (currName.substring(1).indexOf('C') != -1
	    && resultLength > currName.substring(1).indexOf('C')) {
	resultLength = currName.substring(1).indexOf('C');
    }
    if (resultLength == -1) {
	return currName;
    }
    return currName.substring(0, resultLength + 1);
}

function getLaterNodes(currName) {
    var resultLength = currName.substring(1).length;
    if (currName.substring(1).indexOf('l') != -1
	    && resultLength > currName.substring(1).indexOf('l')) {
	resultLength = currName.substring(1).indexOf('l');
    }
    if (currName.substring(1).indexOf('L') != -1
	    && resultLength > currName.substring(1).indexOf('L')) {
	resultLength = currName.substring(1).indexOf('L');
    }
    if (currName.substring(1).indexOf('c') != -1
	    && resultLength > currName.substring(1).indexOf('c')) {
	resultLength = currName.substring(1).indexOf('c');
    }
    if (currName.substring(1).indexOf('C') != -1
	    && resultLength > currName.substring(1).indexOf('C')) {
	resultLength = currName.substring(1).indexOf('C');
    }
    if (resultLength == -1) {
	return '';
    }
    return currName.substring(resultLength + 1);
}

function getFigureOffsetParam(figureVal, name) {
    if (figureVal.indexOf(name) < 0) {
	return '';
    }
    var currName = figureVal.substring(figureVal.indexOf(name) + name.length);
    currName = currName.substring(0, currName.indexOf('"'));
    return currName;
}

function isFugureSaved(shape) {
    var currentId = getFigureOffsetParam(shape, ' id="');
    for (i = 0; i < shapesData.length; i++) {
	var figure = shapesData[i];
	figure = figure.replace(/'/g, "\"");
	var id = getFigureOffsetParam(figure, ' id="');
	if (currentId == id) {
	    return true;
	}
    }
    return false;
}

function appendRectText(temp) {
    if (!isFugureSaved(temp)) {
	return temp;
    }
    var x = getFigureOffsetParam(temp, ' x="');
    var y = getFigureOffsetParam(temp, ' y="');
    var id = getFigureOffsetParam(temp, ' id="');
    var stroke = getFigureOffsetParam(temp, ' stroke="');
    var opacity = getFigureOffsetParam(temp, ' fill-opacity="');
    y = y * 1 + 15;
    id = id.split(rsprefix)[1] * 1;
    temp = temp + '<text font-size="18" font-family="serif" fill="' + stroke
	    + '" stroke="' + stroke + '" fill-opacity="' + opacity + '" x="'
	    + x + '" y="' + y + '" id="' + (id + 1000) + '">' + id + '</text>';
    return temp;
}

function appendPathText(temp) {
    if (!isFugureSaved(temp)) {
	return temp;
    }
    var x = 0;
    var y = 0;
    var originalName = getFigureOffsetParam(temp, ' d="');
    while (originalName.length > 0) {
	var currentNode = getNextNode(originalName);
	var xy = currentNode.substring(1).replace(/^\s+|\s+$/g, "").replace(
		/ /gi, ",").split(',');
	if (currentNode.indexOf('M') >= 0) {
	    x = xy[0];
	    y = xy[1];
	    break;
	}
	originalName = getLaterNodes(originalName);
    }
    var id = getFigureOffsetParam(temp, ' id="');
    var stroke = getFigureOffsetParam(temp, ' stroke="');
    id = id.split(rsprefix)[1];
    if (id.indexOf(rssufix) > 0) {
	id = id.split(rssufix)[0];
    }
    id = id * 1;
    temp = temp + '<text font-size="18" font-family="serif" fill="' + stroke
	    + '" stroke="' + stroke + '" x="' + x + '" y="' + y + '" id="'
	    + (id + 1000) + '">' + id + '</text>';
    return temp;
}

function appendEllipseText(temp) {
    if (!isFugureSaved(temp)) {
	return temp;
    }
    var x = getFigureOffsetParam(temp, ' cx="');
    var y = getFigureOffsetParam(temp, ' cy="');
    var id = getFigureOffsetParam(temp, ' id="');
    var stroke = getFigureOffsetParam(temp, ' stroke="');
    var opacity = getFigureOffsetParam(temp, ' fill-opacity="');
    x = x * 1 - 6;
    y = y * 1 + 6;
    id = id.split(rsprefix)[1] * 1;
    temp = temp + '<text font-size="18" font-family="serif" fill="' + stroke
	    + '" stroke="' + stroke + '" fill-opacity="' + opacity + '" x="'
	    + x + '" y="' + y + '" id="' + (id + 1000) + '">' + id + '</text>';
    return temp;
}

function appendLineText(temp) {
    if (!isFugureSaved(temp)) {
	return temp;
    }
    var x = getFigureOffsetParam(temp, ' x1="');
    var y = getFigureOffsetParam(temp, ' y1="');
    var id = getFigureOffsetParam(temp, ' id="');
    var stroke = getFigureOffsetParam(temp, ' stroke="');
    var opacity = getFigureOffsetParam(temp, ' fill-opacity="');
    id = id.split(rsprefix)[1] * 1;
    temp = temp + '<text font-size="18" font-family="serif" fill="' + stroke
	    + '" stroke="' + stroke + '" fill-opacity="' + opacity + '" x="'
	    + x + '" y="' + y + '" id="' + (id + 1000) + '">' + id + '</text>';
    return temp;
}

function calculateFigureLocation(figureVal, name, delta, zoomParam) {
    if (figureVal.indexOf(name) < 0) {
	return figureVal.substring(0, figureVal.indexOf('/>')) + ' ' + name
		+ delta + '"/>';
    }
    var beforeName = figureVal.substring(0, figureVal.indexOf(name)
	    + name.length);
    var currName = figureVal.substring(figureVal.indexOf(name) + name.length);
    var afterName = currName.substring(currName.indexOf('"'));
    currName = currName.substring(0, currName.indexOf('"'));
    currName = currName * zoomParam + delta;
    return beforeName + currName + afterName;
}

function calculateImageLocation(figureVal, deltaX, deltaY, zoomParam) {
    var width = 0;
    if (figureVal.indexOf(' width="') > 0) {
	width = figureVal.substring(figureVal.indexOf(' width="')
		+ ' width="'.length);
	width = width.substring(0, width.indexOf('"'));
    }
    var height = 0;
    if (figureVal.indexOf(' height="') > 0) {
	height = figureVal.substring(figureVal.indexOf(' height="')
		+ ' height="'.length);
	height = height.substring(0, height.indexOf('"'));
    }
    var nameX = ' x="';
    if (figureVal.indexOf(nameX) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameX)
		+ nameX.length);
	var x = figureVal.substring(figureVal.indexOf(nameX) + nameX.length);
	var afterName = x.substring(x.indexOf('"'));
	x = x.substring(0, x.indexOf('"'));
	x = (x * 1 + width / 2) * zoomParam + deltaX * 1 - width / 2;
	figureVal = beforeName + x + afterName;
    }
    var nameY = ' y="';
    if (figureVal.indexOf(nameY) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(nameY)
		+ nameY.length);
	var y = figureVal.substring(figureVal.indexOf(nameY) + nameY.length);
	var afterName = y.substring(y.indexOf('"'));
	y = y.substring(0, y.indexOf('"'));
	y = (y * 1 + height * 1) * zoomParam + deltaY * 1 - height * 1;
	figureVal = beforeName + y + afterName;
    }
    return figureVal;
}

function calculateFigureSize(figureVal, name, size) {
    if (figureVal.indexOf(name) < 0) {
	return figureVal.substring(0, figureVal.indexOf('/>')) + ' ' + name
		+ '0"/>';
    }
    var beforeName = figureVal.substring(0, figureVal.indexOf(name)
	    + name.length);
    var currName = figureVal.substring(figureVal.indexOf(name) + name.length);
    var afterName = currName.substring(currName.indexOf('"'));
    currName = currName.substring(0, currName.indexOf('"'));
    currName = currName * size;
    return beforeName + currName + afterName;
}

function setAttribute(figureVal, value, name) {
    if (figureVal.indexOf(name) < 0) {
	if (figureVal.indexOf('/>') < 0) {
	    return figureVal.substring(0, figureVal.indexOf('></')) + name
		    + value + '"/>';
	}
	return figureVal.substring(0, figureVal.indexOf('/>')) + name + value
		+ '"/>';
    }
    var beforeName = figureVal.substring(0, figureVal.indexOf(name)
	    + name.length);
    var currName = figureVal.substring(figureVal.indexOf(name) + name.length);
    var afterName = currName.substring(currName.indexOf('"'));
    currName = value;
    return beforeName + currName + afterName;
}

function removeAttribute(figureVal, name) {
    if (figureVal.indexOf(name) > 0) {
	var beforeName = figureVal.substring(0, figureVal.indexOf(name));
	name += '="';
	var currName = figureVal.substring(figureVal.indexOf(name)
		+ name.length);
	var end = '"';
	var afterName = currName.substring(currName.indexOf(end) + end.length);
	return beforeName + afterName;
    }
    return figureVal;
}

function stripTag(temp) {
    if (temp.indexOf('<ellipse') == 0) {
	temp = zoomEllipse(temp, currentX * tileSizeX[0] / zoomFactor, currentY
		* tileSizeY[0] / zoomFactor, 1 / zoomFactor);
	temp = zoomEllipse(temp, 0, 0, maxZoom[0]);
    }
    if (temp.indexOf('<path') == 0) {
	temp = zoomPath(temp, currentX * tileSizeX[0] / zoomFactor, currentY
		* tileSizeY[0] / zoomFactor, 1 / zoomFactor);
	temp = zoomPath(temp, 0, 0, maxZoom[0]);
    }
    if (temp.indexOf('<image') == 0) {
	temp = zoomImage(temp, currentX * tileSizeX[0] / zoomFactor, currentY
		* tileSizeY[0] / zoomFactor, 1 / zoomFactor);
	temp = zoomImage(temp, 0, 0, maxZoom[0]);
    }
    if (temp.indexOf('<line') == 0) {
	temp = zoomLine(temp, currentX * tileSizeX[0] / zoomFactor, currentY
		* tileSizeY[0] / zoomFactor, 1 / zoomFactor);
	temp = zoomLine(temp, 0, 0, maxZoom[0]);
    }
    if (temp.indexOf('<rect') == 0) {
	temp = zoomRect(temp, currentX * tileSizeX[0] / zoomFactor, currentY
		* tileSizeY[0] / zoomFactor, 1 / zoomFactor);
	temp = zoomRect(temp, 0, 0, maxZoom[0]);
    }
    var match = temp.match('stroke-dasharray=".+"');
    if (!match) {
    	temp = setAttribute(temp, '', ' stroke-dasharray="');
    }
    temp = setAttribute(temp, '1', ' stroke-width="');
    return temp;
}

function scaleToFitViewArea(svgStructure, maxWidth, maxHeight) {
    var wrapper = $("<div/>").append(svgStructure);
    var width = wrapper.find("svg").width();
    var height = wrapper.find("svg").height();
    var coefficient = 1;
    if (width / maxWidth > coefficient) {
	coefficient = width / maxWidth;
    }
    if (height / maxHeight > coefficient) {
	coefficient = height / maxHeight;
    }
    wrapper.find("svg").attr("viewBox", "0 0 " + width + " " + height);
    wrapper.find("svg").attr("width", width / coefficient);
    wrapper.find("svg").attr("height", height / coefficient);
    return wrapper.html();
}