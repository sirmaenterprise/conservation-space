function Idoc(opts) {
	var idoc = {};
	
	idoc = $.extend(true, {}, idoc, opts);

	idoc.servicePath = idoc.servicePath + '/intelligent-document';
	idoc.object = {};
	idoc.objectViewVersion = '';
	idoc.operation = '';
	idoc.globalEvents = {};

	idoc.update = function(object) {
		idoc.object = object;
	};

	idoc.joinServicePath = function(/* pass some args to join */) {
		var path = idoc.servicePath;
		for (var i = 0; i < arguments.length; i++) {
			path += '/' + arguments[i];
		}
		return path;
	};

	idoc.isNewObject = function() {
		// REVIEW: typeof is not needed here
		return typeof idoc.object.id === 'undefined' || !idoc.object.id;
	};

	idoc.bindGlobalEvent = function(name, handler) {
		$(idoc.globalEvents).bind(name, handler);
	}

	idoc.triggerGlobalEvent = function(name, data) {
		$(idoc.globalEvents).trigger(name, data);
	};

	idoc.formatDate = function(stringValue, appendHint) {
		return idoc.util.format(stringValue, idoc.sfConfig.dateFormatPattern, appendHint);
	};
	
	idoc.getISODateString = function(date) {
		return idoc.util.getISODateString(date);
	};
	
	idoc.timeSince = function(date) {
		return idoc.util.timeSince(date);
	};
	
	return idoc;
}