"use strict";

/**
 * Directive for displaying basic version information for idocs.
 * The directive depends on three attributes
 * 	created-by: this is the username of the user that created the document
 * 	modified-by: this is the username of the user that was the last to modify the document
 * 	version: the current version of the document
 */
app.directive('documentVersionInfo', ['CodelistService', function(CodelistService) {
   	return {
   		restrict: 'A',
   		templateUrl: EMF.applicationPath + '/templates/documentVersionInfo.tpl.html',
   		link: function(scope, element, attrs) {
   			attrs.$observe('objectType', function(value) {
   				if (value) {
   					// FIXME: read from definition
   					// 7 is the magical number representing the type codelist
   					// this is for objects...
   					CodelistService.loadValue(7, value)
   						.then(
   							function success(response) {
   								var clDescr = response.data;
   								if (clDescr) {
   									scope.objectType = clDescr;
   								} else {
   									// and this is for documents
   									CodelistService.loadValue(210, value)
   										.then(
   											function success(response) {
   												scope.objectType = response.data;
   											}
   										);
   								}
   							}
   						);
   				}
   			});

   			attrs.$observe('createdBy', function(value) {
   				if (value) {
   					scope.createdBy = JSON.parse(value).label;
   				}
   			});
   			
   			attrs.$observe('modifiedBy', function(value) {
   				if (value) {
   					scope.modifiedBy = JSON.parse(value).label;
   				}
   			});
   			
   			attrs.$observe('version', function(value) {
   				scope.version = value;
   			});
   		}
   	}
}]);


app.directive('onBlur', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.on('blur', function() {
				scope.$apply(attrs.onBlur);
			});
		}
	}
});

/*
 * The comments directive. This directive is responsible for refreshing of the sizes of the comments panels. 
 */
app.directive('onResize', function() {
	return function(scope, element, attrs) {
		// REVIEW: the function called on resize event should be debounced!!!
	    $(element).resize(function() {
	    	$('.idoc-comments').trigger('commentResize');
	    });
	}
});

app.directive('stopEventPropagation', function() {
	return function(scope, element, attrs) {
		var events = attrs.stopEventPropagation;
		if (events) {
			element.on(events, function(event) {
				event.stopPropagation();
			});
		}
	}
});

app.directive('preventDefaultHandler', function() {
	return function(scope, element, attrs) {
		var events = attrs.preventDefaultHandler;
		if (events) {
			element.on(events, function(event) {
				event.preventDefault();
			});
		}
	}
});

/**
 * Attribute directive for displaying a label from emf label set.
 */
app.directive('emfLabel', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.html(window._emfLabels[attrs.emfLabel]);
		}
	}
});
