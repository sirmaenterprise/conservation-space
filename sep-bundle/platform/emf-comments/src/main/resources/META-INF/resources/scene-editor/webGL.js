var canvas;
var gl;
var glsl;
var glslProgram;

var animationPeriod = 750;
var perspectiveAngle = 45;
var frameFrequency = 45;

var mouseDown = false;
var lastMouseX = null;
var lastMouseY = null;
var mode = 'rotation';
var modes = [ 'zoom', 'translation', 'rotation' ];
var panels = [];

var initialDistance = 0;
var initialLatitude = 0;
var initialLongitude = 0;
var initialCenter = {
    x : 0,
    y : 0,
    z : 0
};

var distance = initialDistance;
var latitude = initialLatitude;
var longitude = initialLongitude;
var center = {
    x : initialCenter.x,
    y : initialCenter.y,
    z : initialCenter.z
};

var lastAnimationTime = 0;
var endAnimationTime = 0;
var animationPanel = {};
var animationFunction = function() {
};

var animationView = {
    currentDistance : 0,
    currentLatitude : 0,
    currentLongitude : 0,
    currentCenter : {
	x : 0,
	y : 0,
	z : 0
    },
    nextDistance : 0,
    nextLatitude : 0,
    nextLongitude : 0,
    nextCenter : {
	x : 0,
	y : 0,
	z : 0
    },
    chainView : [],
    indexChainView : 0,
    getNormalAngle : function(angle) {
	var result = angle;
	while (result > 180) {
	    result = result - 360;
	}
	while (result < -180) {
	    result = result + 360;
	}
	return result;
    },
    animationFunction : function() {
	var timeNow = new Date().getTime();
	var elapsed = endAnimationTime - timeNow;
	if (elapsed < 0) {
	    elapsed = 0;
	}
	elapsed = 1 - elapsed / animationPeriod;
	distance = animationView.currentDistance * (1 - elapsed)
		+ animationView.nextDistance * elapsed;
	latitude = animationView.currentLatitude * (1 - elapsed)
		+ animationView.nextLatitude * elapsed;
	longitude = animationView.currentLongitude * (1 - elapsed)
		+ animationView.nextLongitude * elapsed;
	center.x = animationView.currentCenter.x * (1 - elapsed)
		+ animationView.nextCenter.x * elapsed;
	center.y = animationView.currentCenter.y * (1 - elapsed)
		+ animationView.nextCenter.y * elapsed;
	center.z = animationView.currentCenter.z * (1 - elapsed)
		+ animationView.nextCenter.z * elapsed;
	if (elapsed == 1) {
	    if (animationView.chainView.length > animationView.indexChainView + 1) {
		endAnimationTime = timeNow + animationPeriod;
		var index = animationView.indexChainView + 1;
		animationView.currentDistance = distance;
		animationView.currentLatitude = latitude;
		animationView.currentLongitude = longitude;
		animationView.currentCenter = center;
		animationView.nextDistance = animationView.chainView[index].distance;
		animationView.nextLatitude = animationView.chainView[index].latitude;
		animationView.nextLongitude = animationView.chainView[index].longitude;
		animationView.nextCenter = animationView.chainView[index].center;
		animationView.indexChainView = index;
	    } else {
		animationFunction = function() {
		};
	    }
	}
    },
    animateView : function(data) {
	var index = 0;
	animationView.chainView = data;
	animationView.currentDistance = distance;
	animationView.currentLatitude = latitude;
	animationView.currentLongitude = longitude;
	animationView.currentCenter = center;
	animationView.nextDistance = animationView.chainView[index].distance;
	animationView.nextLatitude = animationView.chainView[index].latitude;
	animationView.nextLongitude = animationView.chainView[index].longitude;
	animationView.nextCenter = animationView.chainView[index].center;
	animationView.indexChainView = index;
	var timeNow = new Date().getTime();
	endAnimationTime = timeNow + animationPeriod;
	animationFunction = animationView.animationFunction;
    },
    getView : function() {
	var data = {
	    distance : distance,
	    latitude : animationView.getNormalAngle(latitude),
	    longitude : animationView.getNormalAngle(longitude),
	    center : {
		x : center.x,
		y : center.y,
		z : center.z
	    }
	};
	return data;
    }
}

var transparent = false;
var lightEnabled = false;
var alpha = 0.5;
var ambientColor = {
    x : 0,
    y : 0,
    z : 0
};
var lightLocation = {
    x : 100,
    y : 100,
    z : 100
};
var lightColor = {
    x : 1,
    y : 1,
    z : 1
};
var positionMatrix = mat4.create();
var viewMatrix = mat4.create();
var viewMatrixStack = [];

// Utility

function isPowerOfTwo(value) {
    return value & (value - 1) == 0;
}

function toRadians(degrees) {
    return degrees * Math.PI / 180;
}

function getLocation(distance, latitude, longitude, center) {
    var x = distance * Math.cos(toRadians(latitude))
	    * Math.cos(toRadians(longitude)) + center.x;
    var y = distance * Math.sin(toRadians(latitude)) + center.y;
    var z = distance * Math.cos(toRadians(latitude))
	    * Math.sin(toRadians(longitude)) + center.z;
    return {
	'x' : x,
	'y' : y,
	'z' : z
    };
}

function loadGLSLData(gl, glslProgram) {
    glsl = {};
    glsl.data = {
	attributes : [],
	uniforms : []
    };
    var enums = {
	0x8B50 : 'FLOAT_VEC2',
	0x8B51 : 'FLOAT_VEC3',
	0x8B52 : 'FLOAT_VEC4',
	0x8B53 : 'INT_VEC2',
	0x8B54 : 'INT_VEC3',
	0x8B55 : 'INT_VEC4',
	0x8B56 : 'BOOL',
	0x8B57 : 'BOOL_VEC2',
	0x8B58 : 'BOOL_VEC3',
	0x8B59 : 'BOOL_VEC4',
	0x8B5A : 'FLOAT_MAT2',
	0x8B5B : 'FLOAT_MAT3',
	0x8B5C : 'FLOAT_MAT4',
	0x8B5E : 'SAMPLER_2D',
	0x8B60 : 'SAMPLER_CUBE',
	0x1400 : 'BYTE',
	0x1401 : 'UNSIGNED_BYTE',
	0x1402 : 'SHORT',
	0x1403 : 'UNSIGNED_SHORT',
	0x1404 : 'INT',
	0x1405 : 'UNSIGNED_INT',
	0x1406 : 'FLOAT'
    };
    var uniforms = gl.getProgramParameter(glslProgram, gl.ACTIVE_UNIFORMS);
    var attributes = gl.getProgramParameter(glslProgram, gl.ACTIVE_ATTRIBUTES);
    for ( var i = 0; i < attributes; i++) {
	var attribute = gl.getActiveAttrib(glslProgram, i);
	attribute.typeName = enums[attribute.type];
	glsl.data.attributes.push(attribute);
    }
    for ( var i = 0; i < uniforms; i++) {
	var uniform = gl.getActiveUniform(glslProgram, i);
	uniform.typeName = enums[uniform.type];
	glsl.data.uniforms.push(uniform);
    }
    for ( var i = 0; i < glsl.data.uniforms.length; i++) {
	var name = glsl.data.uniforms[i].name;
	glsl[name] = gl.getUniformLocation(glslProgram, name);
    }
    for ( var i = 0; i < glsl.data.attributes.length; i++) {
	var name = glsl.data.attributes[i].name;
	glsl[name] = gl.getAttribLocation(glslProgram, name);
	gl.enableVertexAttribArray(glsl[name]);
    }
}

function getRotationCopy(rotations, angle, axis, translation) {
    var copy = [];
    for ( var k = 0; k < rotations.length; k++) {
	var rotation = {};
	rotation.translation = rotations[k].translation;
	rotation.angle = rotations[k].angle;
	rotation.axis = rotations[k].axis;
	copy.push(rotation);
    }
    var rotation = {};
    rotation.translation = translation;
    rotation.angle = angle;
    rotation.axis = axis;
    copy.push(rotation);
    return copy;
}

function findNextStep(panels) {
    for ( var i = 0; i < panels.length; i++) {
	if (panels[i].maximalAngle != panels[i].angle) {
	    return panels[i];
	}
    }
    for ( var i = 0; i < panels.length; i++) {
	var result = findNextStep(panels[i].panels);
	if (result != null) {
	    return result;
	}
    }
    return null;
}

function findPreviousStep(panels) {
    for ( var i = panels.length - 1; i >= 0; i--) {
	var result = findPreviousStep(panels[i].panels);
	if (result != null) {
	    return result;
	}
    }
    for ( var i = panels.length - 1; i >= 0; i--) {
	if (panels[i].minimalAngle != panels[i].angle) {
	    return panels[i];
	}
    }
    return null;
}

function checkExtremumAngle(panel) {
    if (panel.angle < panel.minimalAngle
	    && panel.minimalAngle < panel.maximalAngle) {
	panel.angle = panel.minimalAngle;
    }
    if (panel.angle > panel.minimalAngle
	    && panel.minimalAngle > panel.maximalAngle) {
	panel.angle = panel.minimalAngle;
    }
    if (panel.angle > panel.maximalAngle
	    && panel.minimalAngle < panel.maximalAngle) {
	panel.angle = panel.maximalAngle;
    }
    if (panel.angle < panel.maximalAngle
	    && panel.minimalAngle > panel.maximalAngle) {
	panel.angle = panel.maximalAngle;
    }
}

function updateAngle(panels, delta) {
    for ( var i = 0; i < panels.length; i++) {
	var phase = panels[i].maximalAngle - panels[i].minimalAngle;
	panels[i].angle += phase * delta;
	checkExtremumAngle(panels[i]);
	updateAngle(panels[i].panels, delta);
    }
}

function flattenPanels(panels, history, rotations) {
    rotations = rotations || [];
    for ( var i = 0; i < panels.length; i++) {
	var rotationsCopy = getRotationCopy(rotations, panels[i].angle,
		panels[i].rotationAxis, panels[i].translation);
	var matrix = mat4.create();
	mat4.identity(matrix);
	for ( var k = 0; k < rotationsCopy.length; k++) {
	    var translation = vec3.create(rotationsCopy[k].translation);
	    var axis = vec3.create(rotationsCopy[k].axis);
	    var angle = rotationsCopy[k].angle;
	    var inverse = vec3.create(translation);
	    vec3.scale(inverse, -1, inverse);
	    mat4.translate(matrix, translation);
	    mat4.rotate(matrix, toRadians(angle), axis);
	    mat4.translate(matrix, inverse);
	}
	panels[i].matrix = matrix;
	history.push(panels[i]);
	flattenPanels(panels[i].panels, history, rotationsCopy);
    }
}

function getShader(source, type) {
    var shader = null;
    if (!type) {
	return shader;
    }
    shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
	alert("Unable to compile shader.");
	return null;
    }
    return shader;
}

function resizeCanvas(canvas) {
    var w = canvas.parentNode;
    var x = w.clientWidth;
    var y = w.clientHeight;
    canvas.setAttribute("width", x);
    canvas.setAttribute("height", y);
    return {
	width : x,
	height : y
    };
}

// View matrix

function viewPushMatrix() {
    var copy = mat4.create();
    mat4.set(viewMatrix, copy);
    viewMatrixStack.push(copy);
}

function viewPopMatrix() {
    if (viewMatrixStack.length == 0) {
	throw "Missing matrix.";
    }
    viewMatrix = viewMatrixStack.pop();
}

// Animation

function animation(callback) {
    window.setTimeout(callback, 1000 / frameFrequency);
}

function animate() {
    var timeNow = new Date().getTime();
    if (endAnimationTime > lastAnimationTime) {
	animationFunction();
    }
    lastAnimationTime = timeNow;
}

function perform() {
    animation(perform);
    $("body").trigger("draw");
    animate();
}

// Ray tracing

function intersectTriangle(point1, point2, point3, start, direction) {
    var line1 = vec3.create();
    var line2 = vec3.create();
    var line3 = vec3.create();
    var cross1 = vec3.create();
    var cross2 = vec3.create();
    var length = vec3.create();
    var result = vec3.create();
    line1[0] = point2[0] - point1[0];
    line1[1] = point2[1] - point1[1];
    line1[2] = point2[2] - point1[2];
    line2[0] = point3[0] - point1[0];
    line2[1] = point3[1] - point1[1];
    line2[2] = point3[2] - point1[2];
    line3[0] = start[0] - point1[0];
    line3[1] = start[1] - point1[1];
    line3[2] = start[2] - point1[2];
    vec3.cross(direction, line2, cross1);
    var size1 = vec3.dot(line1, cross1);
    if (size1 > -0.000001 && size1 < 0.000001) {
	return null;
    }
    size1 = 1 / size1;
    var size2 = size1 * vec3.dot(line3, cross1);
    if (size2 < 0 || size2 > 1) {
	return null;
    }
    vec3.cross(line3, line1, cross2);
    var size3 = size1 * vec3.dot(direction, cross2);
    if (size3 < 0 || size2 + size3 > 1) {
	return null;
    }
    var size4 = size1 * vec3.dot(line2, cross2);
    vec3.scale(direction, size4, length);
    vec3.add(start, length, result);
    return result;
}

function intersectPanel(panel, start, direction) {
    var matrix = panel.matrix;
    var closest = null;
    for ( var i = 0; i < panel.elements.length; i++) {
	var element = panel.elements[i];
	for ( var j = 0; j < element.indexes.length / 3; j++) {
	    var index1 = element.indexes[3 * j + 0];
	    var index2 = element.indexes[3 * j + 1];
	    var index3 = element.indexes[3 * j + 2];
	    var point1 = vec3.create([ element.coordinates[3 * index1 + 0],
		    element.coordinates[3 * index1 + 1],
		    element.coordinates[3 * index1 + 2] ]);
	    var point2 = vec3.create([ element.coordinates[3 * index2 + 0],
		    element.coordinates[3 * index2 + 1],
		    element.coordinates[3 * index2 + 2] ]);
	    var point3 = vec3.create([ element.coordinates[3 * index3 + 0],
		    element.coordinates[3 * index3 + 1],
		    element.coordinates[3 * index3 + 2] ]);
	    point1 = mat4.multiplyVec3(matrix, point1);
	    point2 = mat4.multiplyVec3(matrix, point2);
	    point3 = mat4.multiplyVec3(matrix, point3);
	    var result = intersectTriangle(point1, point2, point3, start,
		    direction);
	    if (result != null) {
		if (closest == null) {
		    closest = result;
		} else {
		    var copy1 = vec3.create(closest);
		    vec3.subtract(copy1, start, copy1);
		    var d1 = vec3.length(copy1);
		    var copy2 = vec3.create(result);
		    vec3.subtract(copy2, start, copy2);
		    var d2 = vec3.length(copy2);
		    if (d1 > d2) {
			closest = result;
		    }
		}
	    }
	}
    }
    return closest;
}

function intersectPanels(start, direction) {
    var panel = null;
    var closest = null;
    var panelArray = [];
    flattenPanels(panels, panelArray);
    for ( var i = 0; i < panelArray.length; i++) {
	var result = intersectPanel(panelArray[i], start, direction);
	if (result != null) {
	    if (closest == null) {
		closest = result;
		panel = panelArray[i];
	    } else {
		var copy1 = vec3.create(closest);
		vec3.subtract(copy1, start, copy1);
		var d1 = vec3.length(copy1);
		var copy2 = vec3.create(result);
		vec3.subtract(copy2, start, copy2);
		var d2 = vec3.length(copy2);
		if (d1 > d2) {
		    closest = result;
		    panel = panelArray[i];
		}
	    }
	}
    }
    return panel;
}

function getSelectedPanel(event) {
    var x = event.clientX - canvas.getAttribute("width") / 2;
    var y = canvas.getAttribute("height") / 2 - event.clientY;
    var eye = getLocation(distance, latitude, longitude, center);
    var eyeVector = vec3.create([ eye.x, eye.y, eye.z ]);
    var centerVector = vec3.create([ center.x, center.y, center.z ]);
    var eyeLine = vec3.create();
    vec3.subtract(centerVector, eyeVector, eyeLine);
    var proportion = Math.tan(toRadians(perspectiveAngle / 2));
    var halfHeight = canvas.getAttribute("height") / 2;
    var pixelSize = vec3.length(eyeLine) * proportion / halfHeight;
    var top = getLocation(distance, latitude + 90, longitude, center);
    var topVector = vec3.create([ top.x, top.y, top.z ]);
    vec3.subtract(topVector, [ center.x, center.y, center.z ], topVector);
    vec3.normalize(topVector);
    vec3.scale(topVector, pixelSize * y, topVector);
    var crossOne = vec3.create(eyeLine);
    vec3.normalize(crossOne);
    var crossTwo = vec3.create([ top.x, top.y, top.z ]);
    vec3.subtract(crossTwo, [ center.x, center.y, center.z ], crossTwo);
    vec3.normalize(crossTwo);
    var rightVector = vec3.create();
    vec3.cross(crossOne, crossTwo, rightVector);
    vec3.normalize(rightVector);
    vec3.scale(rightVector, pixelSize * x, rightVector);
    var direction = vec3.create(eyeLine);
    vec3.add(direction, topVector, direction);
    vec3.add(direction, rightVector, direction);
    return intersectPanels(eyeVector, direction);
}

// Textures

function bindTexture(texture) {
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE,
	    texture.image);
    if (isPowerOfTwo(texture.image.width) && isPowerOfTwo(texture.image.height)) {
	gl.generateMipmap(gl.TEXTURE_2D);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER,
		gl.LINEAR_MIPMAP_LINEAR);
    } else {
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    }
    gl.bindTexture(gl.TEXTURE_2D, null);
}

function initTexture(object) {
    object.texture = gl.createTexture();
    object.texture.image = new Image();
    object.texture.image.onload = function() {
	bindTexture(object.texture);
    }
    object.texture.image.src = object.source;
}

function initTextures(panels) {
    for ( var i = 0; i < panels.length; i++) {
	for ( var j = 0; j < panels[i].elements.length; j++) {
	    initTexture(panels[i].elements[j]);
	}
	initTextures(panels[i].panels);
    }
}

function initBuffers(panels) {
    for ( var i = 0; i < panels.length; i++) {
	for ( var j = 0; j < panels[i].elements.length; j++) {
	    var element = panels[i].elements[j];
	    element.coordinatesBuffer = gl.createBuffer();
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.coordinatesBuffer);
	    gl.bufferData(gl.ARRAY_BUFFER,
		    new Float32Array(element.coordinates), gl.STATIC_DRAW);
	    element.coordinatesBuffer.itemSize = 3;
	    element.coordinatesBuffer.numItems = element.coordinates.length
		    / element.coordinatesBuffer.itemSize;
	    element.normalsBuffer = gl.createBuffer();
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.normalsBuffer);
	    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(element.normals),
		    gl.STATIC_DRAW);
	    element.normalsBuffer.itemSize = 3;
	    element.normalsBuffer.numItems = element.normals.length
		    / element.normalsBuffer.itemSize;
	    element.texturesBuffer = gl.createBuffer();
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.texturesBuffer);
	    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(element.textures),
		    gl.STATIC_DRAW);
	    element.texturesBuffer.itemSize = 2;
	    element.texturesBuffer.numItems = element.textures.length
		    / element.texturesBuffer.itemSize;
	    element.indexesBuffer = gl.createBuffer();
	    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, element.indexesBuffer);
	    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(
		    element.indexes), gl.STATIC_DRAW);
	    element.indexesBuffer.itemSize = 1;
	    element.indexesBuffer.numItems = element.indexes.length
		    / element.indexesBuffer.itemSize;
	}
	initBuffers(panels[i].panels);
    }
}

// Actions

function handleMouseUp(event) {
    mouseDown = false;
}

function handleMouseDown(event) {
    mouseDown = true;
    lastMouseX = event.clientX;
    lastMouseY = event.clientY;
}

function handleMouseMove(event) {
    if (!mouseDown) {
	return;
    }
    var newX = event.clientX;
    var newY = event.clientY;
    var deltaX = newX - lastMouseX;
    var deltaY = newY - lastMouseY;
    if (mode == modes[0]) {
	distance += distance * deltaX / canvas.getAttribute("width");
    }
    if (mode == modes[1]) {
	var dY = Math.sqrt(distance) * deltaY / canvas.getAttribute("height");
	var dX = Math.sqrt(distance) * deltaX / canvas.getAttribute("width");
	var top = getLocation(distance, latitude + 90, longitude, center);
	top.x -= center.x;
	top.y -= center.y;
	top.z -= center.z;
	var topVec = vec3.create([ top.x, top.y, top.z ]);
	vec3.normalize(topVec);
	top.x = topVec[0] * dY;
	top.y = topVec[1] * dY;
	top.z = topVec[2] * dY;
	var current = getLocation(distance, latitude, longitude, center);
	current.x -= center.x;
	current.y -= center.y;
	current.z -= center.z;
	var currentVec = vec3.create([ current.x, current.y, current.z ]);
	vec3.normalize(currentVec);
	var rightVec = vec3.create();
	vec3.cross(currentVec, topVec, rightVec);
	var right = {
	    x : rightVec[0] * dX,
	    y : rightVec[1] * dX,
	    z : rightVec[2] * dX
	};
	center.x += top.x;
	center.y += top.y;
	center.z += top.z;
	center.x += right.x;
	center.y += right.y;
	center.z += right.z;
    }
    if (mode == modes[2]) {
	var top = getLocation(distance, latitude + 90, longitude, center);
	if (top.y > center.y) {
	    longitude += 360 * deltaX / canvas.getAttribute("width");
	} else {
	    longitude -= 360 * deltaX / canvas.getAttribute("width");
	}
	latitude += 360 * deltaY / canvas.getAttribute("height");
    }
    lastMouseX = newX;
    lastMouseY = newY;
}

function handleMouseWheel(event) {
    var evt = window.event || event;
    var delta = evt.detail ? evt.detail * (-120) : evt.wheelDelta;
    distance += distance * delta / 3600;
    event.preventDefault();
    return false;
}

function handleDoubleClick(event) {
    animationPanel = getSelectedPanel(event);
    if (animationPanel == null) {
	return;
    }
    if (animationPanel.maximalAngle != animationPanel.angle) {
	var timeNow = new Date().getTime();
	var delta = (animationPanel.maximalAngle - animationPanel.angle);
	delta /= (animationPanel.maximalAngle - animationPanel.minimalAngle);
	endAnimationTime = timeNow + animationPeriod * delta;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    if (animationPanel != null) {
		var delta = animationPanel.maximalAngle;
		delta -= animationPanel.minimalAngle;
		delta *= elapsed / animationPeriod;
		animationPanel.angle += delta;
		checkExtremumAngle(animationPanel);
	    }
	}
    } else {
	var timeNow = new Date().getTime();
	var delta = (animationPanel.angle - animationPanel.minimalAngle);
	delta /= (animationPanel.maximalAngle - animationPanel.minimalAngle);
	endAnimationTime = timeNow + animationPeriod * delta;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    if (animationPanel != null) {
		var delta = animationPanel.minimalAngle;
		delta -= animationPanel.maximalAngle;
		delta *= elapsed / animationPeriod;
		animationPanel.angle += delta;
		checkExtremumAngle(animationPanel);
	    }
	}
    }
}

// Initialization

function documentReady() {
    $(".trans").click(function() {
	$(".trans").removeClass("selected");
	$(this).addClass("selected");
	transparent = $(this).hasClass("transparency");
	lightEnabled = $(this).hasClass("light");
    });
    $(".move").click(function() {
	$(".move").removeClass("selected");
	$(this).addClass("selected");
	for ( var i = 0; i < modes.length; i++) {
	    if ($(this).hasClass(modes[i])) {
		mode = modes[i];
		break;
	    }
	}
    });
    $(".fit").click(function() {
	center.x = initialCenter.x;
	center.y = initialCenter.y;
	center.z = initialCenter.z;
	distance = initialDistance;
    });
    $(".latitude").click(function() {
	var timeNow = new Date().getTime();
	endAnimationTime = timeNow + animationPeriod;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    latitude += 360 * elapsed / animationPeriod;
	};
    });
    $(".longitude").click(function() {
	var timeNow = new Date().getTime();
	endAnimationTime = timeNow + animationPeriod;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    longitude += 360 * elapsed / animationPeriod;
	};
    });
    $(".next_phase").click(function() {
	var timeNow = new Date().getTime();
	var delta = (panels[0].maximalAngle - panels[0].angle);
	delta /= (panels[0].maximalAngle - panels[0].minimalAngle);
	endAnimationTime = timeNow + animationPeriod;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    updateAngle(panels, elapsed / animationPeriod);
	};
    });
    $(".previous_phase").click(function() {
	var timeNow = new Date().getTime();
	var delta = (panels[0].angle - panels[0].minimalAngle);
	delta /= (panels[0].maximalAngle - panels[0].minimalAngle);
	endAnimationTime = timeNow + animationPeriod;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = lastAnimationTime - timeNow;
	    updateAngle(panels, elapsed / animationPeriod);
	};
    });
    $(".next_step").click(function() {
	animationPanel = findNextStep(panels);
	if (animationPanel == null) {
	    return;
	}
	var timeNow = new Date().getTime();
	var delta = (animationPanel.maximalAngle - animationPanel.angle);
	delta /= (animationPanel.maximalAngle - animationPanel.minimalAngle);
	endAnimationTime = timeNow + animationPeriod * delta;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    if (animationPanel != null) {
		var delta = animationPanel.maximalAngle;
		delta -= animationPanel.minimalAngle;
		delta *= elapsed / animationPeriod;
		animationPanel.angle += delta;
		checkExtremumAngle(animationPanel);
	    }
	};
    });
    $(".previous_step").click(function() {
	animationPanel = findPreviousStep(panels);
	if (animationPanel == null) {
	    return;
	}
	var timeNow = new Date().getTime();
	var delta = (animationPanel.angle - animationPanel.minimalAngle);
	delta /= (animationPanel.maximalAngle - animationPanel.minimalAngle);
	endAnimationTime = timeNow + animationPeriod * delta;
	animationFunction = function() {
	    var timeNow = new Date().getTime();
	    var elapsed = timeNow - lastAnimationTime;
	    if (animationPanel != null) {
		var delta = animationPanel.minimalAngle;
		delta -= animationPanel.maximalAngle;
		delta *= elapsed / animationPeriod;
		animationPanel.angle += delta;
		checkExtremumAngle(animationPanel);
	    }
	};
    });
    $(".stop").click(function() {
	endAnimationTime = 0;
	animationFunction = function() {
	};
    });
}

function fitScene(panels) {
    var history = [];
    extractPoints(panels, history);
    var min = [];
    min[0] = history[0][0];
    min[1] = history[0][1];
    min[2] = history[0][2];
    var max = [];
    max[0] = history[0][0];
    max[1] = history[0][1];
    max[2] = history[0][2];
    for ( var i = 1; i < history.length; i++) {
	if (history[i][0] < min[0]) {
	    min[0] = history[i][0];
	}
	if (history[i][1] < min[1]) {
	    min[1] = history[i][1];
	}
	if (history[i][2] < min[2]) {
	    min[2] = history[i][2];
	}
	if (history[i][0] > max[0]) {
	    max[0] = history[i][0];
	}
	if (history[i][1] > max[1]) {
	    max[1] = history[i][1];
	}
	if (history[i][2] > max[2]) {
	    max[2] = history[i][2];
	}
    }
    var boxCenter = [];
    boxCenter[0] = (min[0] + max[0]) / 2;
    boxCenter[1] = (min[1] + max[1]) / 2;
    boxCenter[2] = (min[2] + max[2]) / 2;
    var tan = Math.tan(toRadians(perspectiveAngle / 2));
    var diagonal = (max[0] - min[0]) * (max[0] - min[0]) + (max[1] - min[1])
	    * (max[1] - min[1]) + (max[2] - min[2]) * (max[2] - min[2]);
    diagonal = Math.sqrt(diagonal);
    initialDistance = diagonal / tan;
    initialCenter = {
	x : boxCenter[0],
	y : boxCenter[1],
	z : boxCenter[2]
    };
    distance = initialDistance;
    center = {
	x : initialCenter.x,
	y : initialCenter.y,
	z : initialCenter.z
    };
}

function extractPoints(panels, history) {
    for ( var i = 0; i < panels.length; i++) {
	for ( var j = 0; j < panels[i].elements.length; j++) {
	    var coordinates = panels[i].elements[j].coordinates;
	    for ( var k = 0; k < coordinates.length / 3; k++) {
		var point = [];
		point[0] = coordinates[3 * k + 0] * 1;
		point[1] = coordinates[3 * k + 1] * 1;
		point[2] = coordinates[3 * k + 2] * 1;
		history.push(point);
	    }
	}
	extractPoints(panels[i].panels, history);
    }
}

function getAttribute(element, tag, attribute) {
    return $(element).find(tag).attr(attribute);
}

function loadShape(transform) {
    var panel = {
	"angle" : 0,
	"minimalAngle" : 0,
	"maximalAngle" : 0,
	"translation" : [ 0, 0, 0 ],
	"rotationAxis" : [ 0, 1, 0 ],
	"panels" : [],
	"elements" : []
    };
    var rotation = transform.attr("rotation");
    if (rotation) {
	rotation = rotation.split(" ");
	panel.rotationAxis[0] = rotation[0];
	panel.rotationAxis[1] = rotation[1];
	panel.rotationAxis[2] = rotation[2];
	panel.maximalAngle = rotation[3] * 180 / Math.PI;
    }
    var translation = transform.attr("translation");
    if (translation) {
	translation = translation.split(" ");
	panel.translation[0] = translation[0];
	panel.translation[1] = translation[1];
	panel.translation[2] = translation[2];
    }
    var shapes = transform.children("Shape");
    shapes.each(function(index) {
	var element = {
	    "coordinates" : [],
	    "normals" : [],
	    "textures" : [],
	    "indexes" : [],
	    "source" : ""
	};
	element.source = getAttribute(this, "ImageTexture", "url");
	var t1 = getAttribute(this, "Coordinate", "point").split(", ");
	var t2 = getAttribute(this, "Normal", "vector").split(", ");
	var t3 = getAttribute(this, "TextureCoordinate", "point").split(", ");
	var a1 = getAttribute(this, "IndexedFaceSet", "coordIndex");
	a1 = a1.replace(/, -1/g, "&").replace(/, $/g, "");
	a1 = a1.substring(0, a1.length - 1).split("&");
	a1[0] = ", " + a1[0];
	var a2 = getAttribute(this, "IndexedFaceSet", "normalIndex");
	a2 = a2.replace(/, -1/g, "&").replace(/, $/g, "");
	a2 = a2.substring(0, a2.length - 1).split("&");
	a2[0] = ", " + a2[0];
	var a3 = getAttribute(this, "IndexedFaceSet", "texCoordIndex");
	a3 = a3.replace(/, -1/g, "&").replace(/, $/g, "");
	a3 = a3.substring(0, a3.length - 1).split("&");
	a3[0] = ", " + a3[0];
	var map = {};
	var index = 0;
	for ( var i = 0; i < a1.length; i++) {
	    var coordIndex = a1[i].split(", ");
	    var normalIndex = a2[i].split(", ");
	    var texCoordIndex = a3[i].split(", ");
	    for ( var j = 1; j < coordIndex.length; j++) {
		var p1 = t1[coordIndex[j]].split(" ");
		var p2 = t2[normalIndex[j]].split(" ");
		var p3 = t3[texCoordIndex[j]].split(" ");
		if (map[p1 + '|' + p2 + '|' + p3] != undefined) {
		    element.indexes.push(map[p1 + '|' + p2 + '|' + p3]);
		} else {
		    element.indexes.push(index++);
		    element.coordinates.push(p1[0]);
		    element.coordinates.push(p1[1]);
		    element.coordinates.push(p1[2]);
		    element.normals.push(p2[0]);
		    element.normals.push(p2[1]);
		    element.normals.push(p2[2]);
		    element.textures.push(p3[0]);
		    element.textures.push(p3[1]);
		    map[p1 + '|' + p2 + '|' + p3] = index - 1;
		}
	    }
	}
	panel.elements.push(element);
	var transforms = transform.children("Transform");
	transforms.each(function(index) {
	    panel.panels.push(loadShape($(this)));
	});
    });
    return panel;
}

function initWebGL(resource) {
	var location = resource;
	$.ajax({
	    async : false,
	    dataType : 'xml',
	    url : location,
	    success : function(data) {
		var x3d = $(data).children("X3D");
		var scene = x3d.children("Scene");
		var transforms = scene.children("Transform");
		transforms.each(function(index) {
		    panels.push(loadShape($(this)));
		});
	    }
	});
    fitScene(panels);
    canvas = document.getElementById("glCanvas");
    resizeCanvas(canvas);
    gl = null;
    try {
	gl = canvas.getContext("experimental-webgl");
    } catch (e) {
    }
    if (!gl) {
	alert("Unable to initialize WebGL.");
    }
    var vertexShader;
    $.ajax({
	async : false,
	dataType : 'text',
	url : "../scene-editor/vs.glsl",
	success : function(data) {
	    vertexShader = getShader(data, gl.VERTEX_SHADER);
	}
    });
    var fragmentShader;
    $.ajax({
	async : false,
	dataType : 'text',
	url : "../scene-editor/fs.glsl",
	success : function(data) {
	    fragmentShader = getShader(data, gl.FRAGMENT_SHADER);
	}
    });
    glslProgram = gl.createProgram();
    gl.attachShader(glslProgram, vertexShader);
    gl.attachShader(glslProgram, fragmentShader);
    gl.linkProgram(glslProgram);
    if (!gl.getProgramParameter(glslProgram, gl.LINK_STATUS)) {
	alert("Unable to initialise shaders.");
    }
    gl.useProgram(glslProgram);
    loadGLSLData(gl, glslProgram);
}

function createCanvas(navigation) {
    var style = $("<style>.scene3d .selected {border: 1px solid #000000 !important;}</style>");
    style.appendTo("head");
    var size = 45;
    var margin = 10;
    var div = $("<div/>");
    div.attr("class", "navigation");
    div.css("position", "relative");
    div.css("height", "100%");
    div.css("width", "100%");
    div.appendTo(".scene3d");
    var can = $("<canvas/>");
    can.attr("id", "glCanvas");
    can.appendTo(".navigation");
    var selectedArray = [];
    var vertical = margin;
    $.each(navigation.vertical, function(key1, value1) {
	$.each(value1.data, function(key2, value2) {
	    var img = $("<img/>");
	    img.attr("src", "../scene-editor/" + value2 + ".png");
	    img.attr("class", key1 + " " + value2 + " vertical");
	    img.css("top", vertical + "px");
	    img.css("left", margin + "px");
	    img.appendTo(".navigation");
	    vertical = vertical + size + margin;
	});
	if (value1.selected) {
	    $(".navigation img." + value1.selected).addClass("selected");
	    selectedArray.push(".navigation img." + value1.selected);
	}
	vertical = vertical + margin;
    });
    var horizontal = margin + size + margin;
    $.each(navigation.horizontal, function(key1, value1) {
	$.each(value1.data, function(key2, value2) {
	    var img = $("<img/>");
	    img.attr("src", "../scene-editor/" + value2 + ".png");
	    img.attr("class", key1 + " " + value2 + " horizontal");
	    img.css("left", horizontal + "px");
	    img.css("top", margin + "px");
	    img.appendTo(".navigation");
	    horizontal = horizontal + size + margin;
	});
	if (value1.selected) {
	    $(".navigation img." + value1.selected).addClass("selected");
	    selectedArray.push(".navigation img." + value1.selected);
	}
	horizontal = horizontal + margin;
    });
    $(".navigation img").css("position", "absolute");
    $(".navigation img").css("width", size + "px");
    $(".navigation img").css("height", size + "px");
    $(".navigation img").css("border-radius", margin + "px");
    $(".navigation img").css("border", "1px solid #AAAAAA");
    documentReady();
    for ( var i = 0; i < selectedArray.length; i++) {
	$(selectedArray[i]).click();
    }
}

function init(resource) {
    if ($(".scene3d").size() != 1) {
	return;
    }
    var navigation = {
	vertical : {
	    fit : {
		data : [ "fit" ]
	    },
	    move : {
		data : [ "zoom", "translation", "rotation" ],
		selected : "rotation"
	    },
	    trans : {
		data : [ "opaque", "transparency", "light" ],
		selected : "light"
	    }
	},
	horizontal : {
	    move : {
		data : [ "latitude", "longitude", "next_phase", "next_step",
			"stop", "previous_step", "previous_phase" ]
	    }
	}
    };
    createCanvas(navigation);
    initWebGL(resource);
    initBuffers(panels);
    initTextures(panels);
    perform();
    canvas.onmouseup = handleMouseUp;
    canvas.onmousedown = handleMouseDown;
    canvas.onmousemove = handleMouseMove;
    canvas.ondblclick = handleDoubleClick;
    var mouseWheel = "mousewheel";
    if (/Firefox/i.test(navigator.userAgent)) {
	mouseWheel = "DOMMouseScroll";
    }
    if (document.attachEvent) {
	canvas.attachEvent("on" + mouseWheel, function(event) {
	    handleMouseWheel(event);
	});
    } else if (document.addEventListener) {
	canvas.addEventListener(mouseWheel, function(event) {
	    handleMouseWheel(event);
	}, false);
    }
}

function viewPanels(panels, rotations) {
    rotations = rotations || [];
    for ( var i = 0; i < panels.length; i++) {
	viewPushMatrix();
	var rotationsCopy = getRotationCopy(rotations, panels[i].angle,
		panels[i].rotationAxis, panels[i].translation);
	for ( var k = 0; k < rotationsCopy.length; k++) {
	    var translation = rotationsCopy[k].translation;
	    var angle = rotationsCopy[k].angle;
	    var axis = rotationsCopy[k].axis;
	    var inverse = vec3.create(translation);
	    vec3.scale(inverse, -1, inverse);
	    mat4.translate(viewMatrix, translation);
	    mat4.rotate(viewMatrix, toRadians(angle), axis);
	    mat4.translate(viewMatrix, inverse);
	}
	gl.uniformMatrix4fv(glsl.viewMatrix, false, viewMatrix);
	var normalMatrix = mat3.create();
	mat4.toInverseMat3(viewMatrix, normalMatrix);
	mat3.transpose(normalMatrix);
	gl.uniformMatrix3fv(glsl.normalMatrix, false, normalMatrix);
	for ( var j = 0; j < panels[i].elements.length; j++) {
	    var element = panels[i].elements[j];
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.coordinatesBuffer);
	    gl.vertexAttribPointer(glsl.vertexPosition,
		    element.coordinatesBuffer.itemSize, gl.FLOAT, false, 0, 0);
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.normalsBuffer);
	    gl.vertexAttribPointer(glsl.vertexNormal,
		    element.normalsBuffer.itemSize, gl.FLOAT, false, 0, 0);
	    gl.bindBuffer(gl.ARRAY_BUFFER, element.texturesBuffer);
	    gl.vertexAttribPointer(glsl.textureCoordinate,
		    element.texturesBuffer.itemSize, gl.FLOAT, false, 0, 0);
	    gl.activeTexture(gl.TEXTURE0);
	    gl.bindTexture(gl.TEXTURE_2D, element.texture);
	    gl.uniform1i(glsl.samplerUniform, 0);
	    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, element.indexesBuffer);
	    gl.drawElements(gl.TRIANGLES, element.indexesBuffer.numItems,
		    gl.UNSIGNED_SHORT, 0);
	}
	viewPopMatrix();
	viewPanels(panels[i].panels, rotationsCopy);
    }
}

function draw() {
    var size = resizeCanvas(canvas);
    gl.clearColor(0.9, 0.9, 0.9, 1.0);
    gl.enable(gl.DEPTH_TEST);
    gl.viewport(0, 0, size.width, size.height);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    mat4.perspective(perspectiveAngle, size.width / size.height, 0.1, 1000.0,
	    positionMatrix);
    gl.uniform1i(glsl.lightEnabled, lightEnabled);
    if (lightEnabled) {
	gl.uniform3f(glsl.ambientColor, ambientColor.x, ambientColor.y,
		ambientColor.z);
	gl.uniform3f(glsl.lightLocation, lightLocation.x, lightLocation.y,
		lightLocation.z);
	gl.uniform3f(glsl.lightColor, lightColor.x, lightColor.y, lightColor.z);
    }
    gl.uniform1i(glsl.transparentUniform, transparent);
    if (transparent) {
	gl.enable(gl.BLEND);
	gl.disable(gl.DEPTH_TEST);
	gl.uniform1f(glsl.alphaUniform, alpha);
    } else {
	gl.disable(gl.BLEND);
	gl.enable(gl.DEPTH_TEST);
    }
    mat4.identity(viewMatrix);
    var res1 = getLocation(distance, latitude, longitude, center);
    var eye = vec3.create([ res1.x, res1.y, res1.z ]);
    var centerVector = vec3.create([ center.x, center.y, center.z ]);
    var res2 = getLocation(distance, latitude + 90, longitude, {
	x : 0,
	y : 0,
	z : 0
    });
    var top = vec3.create([ res2.x, res2.y, res2.z ]);
    mat4.lookAt(eye, centerVector, top, viewMatrix);
    gl.uniformMatrix4fv(glsl.positionMatrix, false, positionMatrix);
    viewPanels(panels);
}

$(document).ready(function() {
    $("body").on("draw", draw);
    if (typeof resource == "undefined") {
	init();
    } else {
	init(resource);
    }
});