(function() {
	'use strict'

	var module = angular.module('idoc.common.services', [ ]);

	module.factory('ObjectPropertiesService', ['$http', function($http) {
		return {
			loadLiterals: function(type, id, definitionId) {
				var conf = { 
					params: { 
						forType: type,
						id: id,
						definitionId: definitionId
					} 
				};
				return $http.get(EMF.servicePath + '/definition/literals', conf);
			},
			
			loadSemanticProperties: function(types) {
				var conf = { params: { } };
				if (types) {
					conf.params.forType = types.join(',');
				}
				
				return $http.get(EMF.servicePath + '/definition/properties', conf);
			},
			/** Retrieves only the common properties between specific types. */
			loadCommonSemanticProperties: function(types) {
				var conf = { params: { } };
				if (types) {
					conf.params.forType = types.join(',');
					conf.params.commonOnly=true;
					conf.params.multiValued=false;
				} 
				
				return $http.get(EMF.servicePath + '/properties/fields-by-type', conf);
			},
			
			loadDefinitionProperties: function(type, id) {
				var promise = $http.get(
					idoc.joinServicePath('instance', id, 'properties'), 
					{ params: { type: type } }
				);
				return promise;
			},
			
			/**
			 * Load the properties for a set of objects. 
			 */
			bulkLoadDefinitionProperties: function(objects) {
				var requestConfig = {
					params: {
						objects: JSON.stringify(objects)
					}
				}
				return $http.get(idoc.joinServicePath('properties'), requestConfig);
			}
		}
	}]);
	
	module.factory('CodelistService', ['$http', '$q', function($http, $q) {
		var codelistCache = { };
		return {
			loadValue: function(codelistNumber, code) {
				return $http.get(EMF.servicePath + '/codelist/' + codelistNumber + '/values/' + code);
			},
			
			loadCodelist: function(codelistNumber) {
				return $http.get(EMF.servicePath + '/codelist/' + codelistNumber, { cache: true });
			},
			
			loadAllAvailableCodelists: function() {
				return $http.get(EMF.servicePath + '/codelist/codelists');
			}
		}
	}]);
	
}())