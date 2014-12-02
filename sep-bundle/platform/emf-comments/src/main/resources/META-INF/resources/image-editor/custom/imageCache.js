var cache = {};
var imageBuffer = [];
var preloadEnabled = false;
var maximum = 5;
var current = 0;

function cacheImage() {
    if (current > 0) {
	current--;
    }
    if (preloadEnabled && imageBuffer.length > 0) {
	var srcs = imageBuffer.splice(0, maximum - current);
	for ( var it = 0; it < srcs.length; it++) {
	    var src = srcs[it].toString().replace(/&amp;/g, '&');
	    current++;
	    $("<img/>").attr("src", src).load($.debounce(100, cacheImage));
	}
    }
}

function findTile(imageIndex, zoomLevel, x1, y1, x2, y2) {
    var imageCache = cache[imageIndex];
    if (!imageCache) {
	return undefined;
    }
    var zoomCache = imageCache[zoomLevel];
    if (!zoomCache) {
	return undefined;
    }
    for ( var x in zoomCache) {
	if (x1 <= x * 1 && x * 1 <= x2) {
	    var horizontalCache = zoomCache[x];
	    if (!horizontalCache) {
		return undefined;
	    }
	    for ( var y in horizontalCache) {
		if (y1 <= y * 1 && y * 1 <= y2) {
		    return {
			x : x,
			y : y
		    };
		}
	    }
	}
    }
    return undefined;
}

function addTile(imageIndex, zoomLevel, x, y, url) {
    var imageCache = cache[imageIndex];
    if (!imageCache) {
	imageCache = {};
	cache[imageIndex] = imageCache;
    }
    var zoomCache = imageCache[zoomLevel];
    if (!zoomCache) {
	zoomCache = [];
	imageCache[zoomLevel] = zoomCache;
    }
    var horizontalCache = zoomCache[x];
    if (!horizontalCache) {
	horizontalCache = [];
	zoomCache[x] = horizontalCache;
    }
    if (!horizontalCache[y]) {
	horizontalCache[y] = url;
	imageBuffer.push(url);
    }
}

$(window).on("stopPreload", function() {
    preloadEnabled = false;
});

$(window).on("startPreload", function() {
    preloadEnabled = true;
    cacheImage();
});

$(window).on("addTile", function(event, imageIndex, zoomLevel, x, y, url) {
    addTile(imageIndex, zoomLevel, x, y, url);
});