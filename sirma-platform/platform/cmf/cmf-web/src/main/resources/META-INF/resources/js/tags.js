;(function() {
	'use strict';

	/** Creating a namespace for the tags. */
	EMF = EMF || {};
	EMF.tags = EMF.tags || {};
	EMF.tags.autocompleteLimit = 25;
	EMF.tags.hasTagRelationType = 'emf:hasTag';
	EMF.tags.mode = {
		preview: 'preview',
		edit: 'edit'
	};

	/**
	 * Module for displaying, attaching and detaching emf:Tag.
	 *
	 * @author Mihail Radkov
	 */
	var tagsModule = angular.module('emfTags', []);

	var autocompleteLimit = EMF.tags.autocompleteLimit;

	/**
	 * The configuration is extracted here to avoid sonar issues.
	 */
	var tagSelectConfig = {
		multiple : true,
		minimumResultsForSearch : -1,
		minimumInputLength : -1,
		width : '100%',
		dataType : "json",
		placeholder : _emfLabels["tags.placeholder"],
		ajax : {
			url : EMF.servicePath + '/search/basic',
			quietMillis : 250,
			data : function(term, page) {
				var query;
				if(term) {
					query = 'title:"' + term + '"';
				}
				return {
					objectType:["emf:Tag"],
					fq		  :query,
					offset	  : page,
					limit	  : autocompleteLimit
				};
			},
			results : function(response, page) {
				var more = (page * autocompleteLimit) < response.resultSize;
				var results = [];
				_.forEach(response.values, function(tag) {
					results.push({
						id: tag.dbId,
						text: tag.breadcrumb_header
					});
				});
				return {
					results : results,
					more : more
				};
			}
		},
		containerCssClass: 'headers-with-icons',
		dropdownCssClass: 'headers-with-icons tags-menu-item',
		escapeMarkup: function(m) {
			 return m;
		}
	};

	/**
	 * Directive that represents the tags. Contains functionality for interacting with select2 and basically acts as a wrapper for it.
	 */
	tagsModule.directive('emfTags', [ function() {
		return {
			restrict: 'AE',
			replace	: true,
			controller : 'EmfTagsController',
			templateUrl	: EMF.applicationPath + '/templates/tags/tags.tmpl.html',
			link: function (scope, element) {
				// TODO: Angular constant?
				var previewMode = EMF.tags.mode.preview;
				var editMode = EMF.tags.mode.edit;

				var saveOptions = element.find('.save-options');
				var editButton = element.find('.edit-btn');
				var tagsInput = element.find('.tags-input');

				// Needed in the controller. TODO: not very good workaround
				scope.tagsInput = tagsInput;
				scope.element = element;

				scope.tags = [];
				scope.oldTags = [];

				tagsInput.select2(tagSelectConfig);

				/**
				 * Watches any data changes to the select2 component and sets the currently chosen tags
				 * into the scope. With this, the scope is always up to date with the selection.
				 */
				tagsInput.change(function(evt) {
					scope.tags = $(evt.target).select2('data');
				});

				/**
				 * Watches any changes to the tags in the scope and sets them as value to the
				 * select2 component. Useful when they are changed somewhere in the code.
				 */
				scope.$watch('tags', function(newValue) {
					tagsInput.select2('data', newValue);
				});

				scope.$watch('mode', function(newValue) {
					if(newValue === previewMode) {
						tagsInput.select2('disable');
						editButton.removeClass('hidden');
						saveOptions.addClass('hidden');
					} else if (newValue === editMode) {
						scope.oldTags = tagsInput.select2('data');
						tagsInput.select2('enable');
						editButton.addClass('hidden');
						saveOptions.removeClass('hidden');
					}
				});

				scope.getLabel = function(key) {
					return _emfLabels[key];
				};

				// Triggers preview mode.
				scope.mode = previewMode;

				scope.loadCurrentTags(tagsInput);
			}
		};
	}]);

	/**
	 * Controller for the tags functionality. Contains methods for the basic operations like load, edit, cancel, save.
	 */
	tagsModule.controller('EmfTagsController', ['$scope', '$q', 'TagService', function ($scope, $q, TagService) {
		var hasTagRelationType = EMF.tags.hasTagRelationType;
		var previewMode = EMF.tags.mode.preview;
		var editMode = EMF.tags.mode.edit;

		/**
		 * Fetches all relations to the current instance and displays all that are tags.
		 */
		$scope.loadCurrentTags = function() {
			EMF.ajaxloader.showLoading($scope.element);
			TagService.loadRelations().success(function(relations) {
				var tags = [];
				if(relations) {
					_.forEach(relations, function(relation) {
						if(relation.linkType === hasTagRelationType) {
							tags.push({
								id: relation.toId,
								text: relation.breadcrumb_header,
								linkId: relation.linkId
							});
						}
					});
				}

				$scope.tags = tags;
				EMF.ajaxloader.hideLoading($scope.element);
			});
		};

		/**
		 * Changes the current mode to editing.
		 */
		$scope.goToEditMode = function() {
			if($scope.mode === previewMode) {
				$scope.mode = editMode;
			}
		};

		/**
		 * Makes a diff of the added and removed tags. Once the services has
		 * been called, it waits for all promises to be resolved.
		 */
		$scope.accept = function() {
			EMF.ajaxloader.showLoading($scope.element);

			var added 	= _.difference($scope.tags, $scope.oldTags);
			var removed = _.difference($scope.oldTags, $scope.tags);
			var promises = [];

			if(added && added.length > 0) {
				promises.push($scope.addTags(added));
			}

			if(removed && removed.length > 0) {
				var removePromises = $scope.removeTags(removed);
				_.forEach(removePromises, function(promise) {
					promises.push(promise);
				});
			}

			$q.all(promises).then(function() {
				$scope.loadCurrentTags($scope.tagsInput);
				$scope.mode = previewMode;
				EMF.ajaxloader.hideLoading($scope.element);
			});
		};

		/**
		 * Cancels the change set.
		 */
		$scope.cancel = function() {
			$scope.mode = previewMode;
			$scope.tags = $scope.oldTags;
		};

		/**
		 * Based on the provided array of tags, constructs a POST request
		 * for attaching them to the current instance.
		 *
		 * @param tags - the array of tags
		 * @return a promise
		 */
		$scope.addTags = function(tags) {
			return TagService.addTagRelations(tags);
		};

		/**
		 * Detaches specified tags from the current instance.
		 *
		 * @param tags - the array of tags to remove
		 * @return an array of promises
		 */
		$scope.removeTags = function(tags) {
			var promises = [];
			_.forEach(tags, function(tag) {
				promises.push(TagService.deleteTagRelation(tag));
			});
			return promises;
		};
	}]);

	/**
	 * Factory for dealing with attach/detach operations with the relationships REST service and tags.
	 */
	tagsModule.factory('TagService', ['$http', function($http) {
		var tagService = {};
		var loadServicePath	= EMF.servicePath + '/relations/loadData';
		var addRelationPath	= EMF.servicePath + '/relations/create';
		var deactivatePath 	= EMF.servicePath + '/relations/deactivate';
		var hasTagRelationType = EMF.tags.hasTagRelationType;

		var getCurrentInstance = function() {
			var length = EMF.currentPath.length;
			return EMF.currentPath[length - 1];
		};

		/**
		 * Loads all relations to the current instance and returns their
		 * breadcrumb headers for later use in the tags component.
		 */
		tagService.loadRelations = function() {
			var currentInstance = getCurrentInstance();
			var config = {
				params: {
					id: currentInstance.id,
					type: currentInstance.type,
					fields: 'breadcrumb_header',
					linkId: 'emf:hasTag'
				}
			};
			return $http.get(loadServicePath, config);
		};

		/**
		 * Attaches the provided tags to the current instance.
		 */
		tagService.addTagRelations = function(tags) {
			var currentInstance = getCurrentInstance();
			var data = {
				relType: hasTagRelationType,
				toFixed: true,
				selectedItems: {}
			};
			_.forEach(tags, function(tag) {
				data.selectedItems[tag.id] = {
					destId: tag.id,
					destType: 'objectinstance',
					targetId: currentInstance.id,
					targetType: currentInstance.type
				};
			});
			return $http.post(addRelationPath, data);
		};

		/**
		 * Detaches the provided tag from the current instance.
		 */
		tagService.deleteTagRelation = function(tag) {
			var currentInstance = getCurrentInstance();
			var request = {
				params: {
					relationId : tag.linkId,
					relType : hasTagRelationType,
					instanceId : currentInstance.id,
					toId : tag.id
				}
			};
			return  $http.get(deactivatePath, request);
		}

		return tagService;
	}]);

}());