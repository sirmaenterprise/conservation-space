(function() {
	'use strict';

	widgetManager.registerComponent('bootstrapTooltip');
	var _module = angular.module('bootstrapTooltip', [ 'ng' ]);

	_module.directive('bootstrapTooltip', function() {
		return {
			restrict : 'A',
			link : function(scope, element, attrs) {
				attrs.$observe('bootstrapTooltip', function(newValue) {
					$(element).tooltip({ title: newValue, delay: { show: 500, hide: 100 } });
				});
			}
		}
	});
}());