;(function() {
	'use strict';

	/** Module with services for side-tabs. */
	var module = angular.module('cmfSideTabsServices', []);

	/**
	 * Service for document operations.
	 */
	module.service('DocumentService', function() {
		return CMF.document;
	});

	/**
	 * Servise for getting the latest version of the current object.
	 */
	module.service('Version', function() {
		this.getData = function(callbackFunc) {
			var data = {
				instanceId : EMF.documentContext.currentInstanceId,
				instanceType : EMF.documentContext.currentInstanceType,
				property : "compact_header"
			};

			REST.getClient('InstanceRestClient').getOriginalInstanceProperties(data, callbackFunc);
		}
	});

	module.service('InstanceService', function(){
		return CMF.instance;
	});
}());