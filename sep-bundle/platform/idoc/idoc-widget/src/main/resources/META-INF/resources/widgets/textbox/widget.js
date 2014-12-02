(function () {
	'use strict';
	
	widgetManager.registerWidget('textbox', {
		configurable: false,
		identifiable: true,
		compileOnSave: false,
		titled: true
	});
	
	var _radiobutton = angular.module('textbox',['ng']);
	
	/** Controller for the directive initialization */
	_radiobutton.controller('TextboxWidgetController', ['$scope', function($scope) {
		$scope.saveConfig = function() {
			$scope.confAttr = widgetManager.serializeObjectForAttribute($scope.config);
		}
	}]);
	
	/** Textbox directive */
	_radiobutton.directive('textbox', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/textbox/template.html',
			controller : 'TextboxWidgetController',
			link: function(scope, element, attrs) {
				
				if(scope.config.width) {
					element.outerWidth(scope.config.width),
					element.outerHeight(scope.config.height);
				} else { 
					// IE10 can not display css min width/height correct
					element.outerWidth(400),
					element.outerHeight(60);
				}
				
				if (($.browser.msie || !!navigator.userAgent.match(/Trident.*rv\:11\./)) && !scope.previewMode) {
	            	element.resizable({
	            		maxWidth: 615,
	            		minHeight: 50,
	            		minWidth: 400, 
	            		stop: function( event, ui ) {
	            			resize();
	            		} 
	            	});
	            }
				
				element.mouseup(function(event) {
					resize();
				});
				
				var resize = function() {
					var width = element.outerWidth(),
					height = element.outerHeight();
				
					scope.config.width = width;
					scope.config.height = height;
					
					scope.$apply(function() {
						scope.confAttr = widgetManager.serializeObjectForAttribute(scope.config);
					});
				}
			}
		};
	});

}());