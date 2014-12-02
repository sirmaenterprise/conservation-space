(function() {
	'use strict'

	var module = angular.module('idoc.common.filters', [ ]);

	/**
	 * Filter that enables binding an arbitrary string as html using ng-bind-html
	 * e.g. <div ng-bind-html="model.unsafeHtml | trustAsHtmlFilter"></div>
	 * Use this filter only when you're know that the html is safe to bind directly
	 * w/o additional sanitization.
	 */
	module.filter('trustAsHtmlFilter', ['$sce', function($sce) {
		return function(unsafeHtml) {
			return $sce.trustAsHtml(unsafeHtml);
		}
	}]);

	/**
	 * Matches a code to it's value from a provided array representing a codelist.
	 */
	module.filter('codelistFilter', function() {
		return function(code, codelist) {
			var value;
			if (!code || !codelist) {
				return code;
			}
			
			value = _.find(codelist, function matchCodeValue(codevalue) {
				return codevalue.value === code;
			});
			
			if (value) {
				return value.label;
			}
			return code;
		}
	});
}())