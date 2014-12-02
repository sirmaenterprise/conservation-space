(function() {
	'use strict';

	widgetManager.registerComponent('chosen');
	var _module = angular.module('chosen', [ 'ng' ]);

	_module.directive('chosen', function() {
		return {
			restrict : 'A',
			link : function(scope, element, attrs) {
				element.chosen({width: '200px'});
				
				var watchCollectionExpression = '[' + attrs['ngModel'] + ',' + attrs['chosen'] + ']';
				scope.$watchCollection(watchCollectionExpression, function() {
					element.trigger("chosen:updated");
				});
			}
		}
	});
}());