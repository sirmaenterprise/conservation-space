(function () {
	'use strict';
	
	widgetManager.registerWidget('versions', {
		configurable: false,
		identifiable: true,
		titled: true
	});
	
	var _versions = angular.module('versions',['ng']);
	
	_versions.controller('VersionsWidgetController', ['$rootScope', '$scope','$http', function($rootScope, $scope, $http) {
		// FIXME: move to $rootScope, a service? ...and others alike.
		$scope.applicationPath = EMF.applicationPath;
		
		$scope.init = function() {
			if (idoc.object.id) {

				$http.get(idoc.servicePath + '/' + idoc.object.id + '/versions', { params: { type: idoc.object.type } }).success(function(result) {
					$scope.versions = result.versions;
					if (idoc.objectViewVersion) {
						for (var version in $scope.versions) {
							var v = $scope.versions[version];
							if (idoc.objectViewVersion === v.title) {
								v.selectedClass = 'selected-version';
							}
						}
					}
					if($scope.versions.length) {
						var lastVersion = $scope.versions[$scope.versions.length - 1].title;
						var versionNumber = lastVersion.split('.');
						versionNumber[1]++;
						$rootScope.$broadcast('document-version-changed', versionNumber[0] + "." + versionNumber[1]);
					}
				});
			}
		}
		
		$scope.broadcastRevertToVersion = function(version) {
			$rootScope.$broadcast('document-revert-to-version', version);
		}
		
		$scope.broadcastPreviewVersionEvent = function(version) {
			$rootScope.$broadcast('document-preview-version', version);
		}
	}]);
	
	_versions.directive('versions', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/versions/template.html',
			controller : 'VersionsWidgetController',
			link: function(scope, element, attrs) {
				
				scope.init();
				
				// INTELLIFORMS-335 tooltip balloon stays on page because html is replace
				// and there is no time for the user the trigger the mouseleave event himself
				element.click(function(event) {
					$(event.target).trigger('mouseleave');
				});
			}
		};
	});

}());