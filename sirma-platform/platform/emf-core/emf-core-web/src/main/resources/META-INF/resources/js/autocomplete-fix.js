(function() {

	// http://bugs.jqueryui.com/ticket/8439
	// Autocomplete plugin disables custom 'window.onbeforeunload' listener

	var autocomplete = $.fn.autocomplete;					// original fn
	$.fn.autocomplete = function() {						// intercept calls to $().autocomplete()
		var handler = window.onbeforeunload;
		var result = autocomplete.apply(this, arguments);	// delegate
		window.onbeforeunload = handler;					// reset
		return result;
	};
}());