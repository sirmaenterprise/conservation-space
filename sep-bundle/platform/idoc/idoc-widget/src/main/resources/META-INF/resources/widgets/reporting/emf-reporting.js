;(function() {
	'use strict';
	
	/** Creating a name space for the reporting functionality. */
	EMF.reporting = EMF.reporting || {};
	
	/** Path to the properties service. */
	EMF.reporting.propertiesServicePath = EMF.servicePath + '/properties/searchable-fields';
	
	/** Path to the faceted search service. */
	EMF.reporting.facetedSearchPath = EMF.servicePath + "/search/faceted";
	
	/** Path to the object search service. */
	EMF.reporting.objectServicePath = EMF.servicePath + "/object-rest";
	
	/**
	 * AngularJS module for the reporting functionality in EMF. The logic is separated in different modules that are 
	 * depended in this one. If someone wants to use it, the module should be depended in the same manner in their
	 * AngularJS application. The separation is designed for re-usability.
	 */
	var module = angular.module('emfReporting', [ 'emfReportingDirectives', 'emfReportingControllers', 'emfReportingServices']);
	
}());