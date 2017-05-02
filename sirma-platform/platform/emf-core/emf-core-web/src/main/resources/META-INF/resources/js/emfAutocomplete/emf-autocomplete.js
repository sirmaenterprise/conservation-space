(function() {
	'use strict';

	/**
	 * AngularJS module for auto-complete/multi-select combo box, designed for selecting possible values of properties
	 * where system codes are used (ex: priority, status, user display names).
	 * Needs only a code list number to be passed (or 0 for user display names).
	 * codelist, limit, userid, multiple and tagging properties are passed in the config attribute to the directive.
	 * Depends on angular-ui-select and ngSanitize.
	 *
	 * @author Vilizar Tsonev
	 */
	var module = angular.module('emfAutocomplete', ['ngSanitize', 'ui.select']);

	/** The path to the auto-complete rest service **/
	module.constant('emfAutocompleteServicePath', EMF.servicePath + "/autocomplete");

	/** enum indicating the possible values of the user property to load into the data model as IDs. */
	module.userIdAttributes = {
			DEFAULT: "fullUri",
			FULL_URI: "fullUri",
			SHORT_URI: "shortUri",
			USER_NAME: "userName"
	};

	/**
	 * The directive for the auto-complete fields.
	 */
	module.directive('emfAutocomplete', [ function () {
		return {
			restrict: 'E',
			replace	: true,
			controller : 'emfAutocompleteController',
			templateUrl	: EMF.applicationPath + '/js/emfAutocomplete/templates/emf-autocomplete.tpl.html',
			scope : {
				model: '=',
				options: '=?',
				config: '=?'
			},
			link: function(scope, element, attrs) {
				//if config is not provided, create it and set some default values
				if(!scope.config) {
					scope.config = {};
					scope.config.multiple = false;
					scope.config.tagging = false;
				}
				//assign the mandatory configs to the scope, giving them default values, if undefined
				scope.multiple = scope.config.multiple || false;
				scope.tagging = scope.config.tagging || false;
				
				//if options are not provided, try to retrieve them from CL
				if(angular.isUndefined(scope.options)) {
					scope.initAutocompleteData();
				} else {
					scope.autocompleteData = scope.options;
				}
			}
		};
	}]);

	/**
	 * The controller for the auto-complete field.
	 */
	module.controller('emfAutocompleteController', ['$scope', '$http', 'emfAutocompleteServicePath', function($scope, $http, emfAutocompleteServicePath) {

		/** The data model for the combo box. All entries will be stored here **/
		$scope.autocompleteData = [];
		
		/** Indicates if the options drop-down is fully initialized. Used to avoid concurrency issues **/
		$scope.optionsInitialized = false;
		
		/**
		 * Builds the rest service path to retrieve users for the auto-complete, according to the userAttribute parameter.
		 * In some cases user full URIs are preferred to be loaded in the data model as IDs, while in other
		 * cases short URIs or user names are more appropriate, depending on the implementation.
		 *
		 * @param userAttribute is the user id attribute to load into the model, null-safe.
		 * @param config is the configuration object for the GET method
		 */
		var getUsersAutocompleteServicePath = function(userAttribute, config) {
			if(angular.isUndefined(userAttribute)) {
				userAttribute = module.userIdAttributes.DEFAULT;
			}
			if(userAttribute === module.userIdAttributes.FULL_URI) {
				config.params.returnFullUris = true;
				return emfAutocompleteServicePath + "/usernamebyuri";
			} else if (userAttribute === module.userIdAttributes.SHORT_URI) {
				return emfAutocompleteServicePath + "/usernamebyuri";
			} else if (userAttribute === module.userIdAttributes.USER_NAME) {
				return emfAutocompleteServicePath + "/username";
			}
		};

		/**
		 * Calls the rest service and populates the data model for the auto-complete options.
		 */
		$scope.initAutocompleteData = function() {
			if (!$scope.tagging) {
				//reset the old data (if any)
				$scope.autocompleteData = [];
				var url = "";
				var config = {};
				config.params = {};
				config.params.limit = $scope.config.limit || 100;
				if($scope.searchTerm) {
					config.params.q = $scope.searchTerm;
				}
				if ((!angular.isUndefined($scope.config.codelist)) && ($scope.config.codelist!== 0)) {
					config.params.codelistid = $scope.config.codelist;
					url = emfAutocompleteServicePath + "/codelist";
				} else {
					url = getUsersAutocompleteServicePath($scope.config.userId, config);
				}
				$http.get(url, config).then(function(response) {
					//when single-select, put an empty item for resetting the combo-box
					if(!$scope.multiple) {
						$scope.autocompleteData.push({id:"", text:""});
					}
					$scope.autocompleteData = $.merge($scope.autocompleteData, response.data.results);
					if(!$scope.optionsInitialized) {
						$scope.optionsInitialized = true;
					}
				});
			}
		};

		/**
		 * Transforms the free-text selected item into a format suitable for the data model.
		 */
		$scope.transformTagToModel = function (newTag) {
			 var item = {
			     id: newTag,
			     text: newTag
			 };
			 return item;
		};
		
		/**
		 * Triggered when the user types into the auto-complete.
		 */
		$scope.handleChange = function(text) {
			//if it is a user auto-complete, re-populate the drop-down options
			//according to the user partial name typed in the input field
			if (($scope.config.codelist === 0) && ($scope.optionsInitialized)) {
				$scope.searchTerm = text;
				//re-populate, considering the search term
				$scope.initAutocompleteData();
			}
		};
	}]);
}());