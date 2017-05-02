;(function() {
	'use strict';

	/** Module that holds all controllers for side-tab. */
	var module = angular.module('cmfSideTabsController', []);

	/**
	 * <b>SideTabsController</b> will be invoked on side-tabs initialization.
	 */
	module.controller('SideTabsController', ['$scope', function($scope) {

		$scope.tabs = model;

		angular.forEach($scope.tabs, function(value, key) {
			value.title = _emfLabels[value.title] || 'missing tab label!!!';
			 value.order = key + 1;
		});

		$scope.currentTab = $scope.tabs[0];

		/**
		 * Method that will be executed when the user press specific tab.
		 */
		$scope.updateCurrentTab = function(tab) {
			$(window).trigger('resize.idoc');
			$scope.currentTab = tab;
		};

	}]);

	/**
	 * Controller for generating shear link for documents.
	 */
	module.controller('TabDocumentShareLink', ['$scope', 'DocumentService', 'CMFActions', function($scope, DocumentService, CMFActions) {
		DocumentService.loadShareLink(CMFActions).then(function (response) {
			// require because the request is done outside <i>Angularjs</i> context
			// see http://www.sitepoint.com/understanding-angulars-apply-digest/
			$scope.$apply(function(){
				$scope.link = response.path;
			});
		});
	}]);

	/**
	 * Controller for comments.
	 */
	module.controller('TabComments', ['$scope', function($scope) {
		var currentObject = EMF.currentPath[EMF.currentPath.length - 1];
		$scope.commentsModel = [{
			aboutSection : currentObject.id,
			instanceId : currentObject.id,
			instanceType : currentObject.type
		}];
		$scope.createLabel = _emfLabels['cmf.comment.topic.create'];
		$scope.createTopic = function() {
			$scope.$broadcast('emf-comments:ng:create-topic', {
				objectId : currentObject.id
			});
		};
	}]);

	/**
	 * Controller for comment tool-bar.
	 */
	module.controller('CommentsToolbarController', ['$rootScope', '$scope', function($rootScope, $scope) {
		$scope.createLabel = _emfLabels['cmf.comment.topic.create'];
		$scope.createTopic = function() {
			$rootScope.$broadcast('emf-comments:ng:create-topic', {
				objectId : EMF.currentPath[EMF.currentPath.length - 1].id
			});
		};
	}]);

	/**
	 * Controller for document version history.
	 */
	module.controller('TabVersionHistory', ['$scope', 'DocumentService', 'CMFActions', function($scope, DocumentService, CMFActions) {
		var reservedData = {
			latest : _emfLabels['cmf.version.history.last.version'],
			older : _emfLabels['cmf.version.history.view.older.version'],
			elements : null
		};

		DocumentService.loadDocumentVersionHistory(CMFActions).then(function (response) {
			reservedData.elements = response.data;
			$scope.histories = reservedData;
		});
	}]);

	/**
	 * Controller for document revisions.
	 */
	module.controller('TabDocumentRevisions', ['$scope', 'DocumentService', 'CMFActions', function($scope, DocumentService, CMFActions) {
		$scope.emptyRevisionsMessage = _emfLabels['cmf.document.tab.norevisions.message'];
		DocumentService.loadRevisions(CMFActions).then(function (response) {
		var applyFunc = function() {
				$scope.revisions = response.data;
				$scope.isEmpty = true;
				if ($scope.revisions && $scope.revisions.length !== 0) {
					$scope.isEmpty = false;
				}
			}
			$scope.$apply(applyFunc());
		});
	}]);

	/**
	 * Controller for document Versions.
	 */
	module.controller('TabDocumentVersions', ['$scope', 'Version', function($scope, Version) {
		$scope.config = {};
		$scope.previewMode = true;
		Version.getData(function(responce) {
			$scope.compactheader = responce.compact_header;
		});

		$scope.getHeader = function(){
			return $scope.compactheader;
		};
	}]);

	/**
	 * Controller for property tab
	 */
	module.controller('TabPropertiesFields', [function(){
		// nothing to do at this point
	}]);

}());