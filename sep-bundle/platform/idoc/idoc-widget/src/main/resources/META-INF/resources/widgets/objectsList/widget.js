(function () {
	'use strict';

	widgetManager.registerWidget('objectsList', {
		configurable: true,
		identifiable: true,
		configController: "ObjectsListWidgetConfigController",
		titled: true,
		configDialogWidth: 960,
		configDialogHeight: 680
	});
	
	var _objectsList = angular.module('objectsList', ['ng', 'idoc.common.services', 'idoc.common.filters']);
	
	//Extend the config controller of the Data Table widget as it provides the same configuration capabilities
	_objectsList.controller('ObjectsListWidgetConfigController', ['$scope', '$controller', function($scope, $controller) {
		$scope.config.viewMode = $scope.config.viewMode || 'detail';
		
		$controller('DatatableWidgetConfigureController', {$scope: $scope});
	}]);
	
	_objectsList.controller('ObjectsListWidgetController', ['$scope', '$http', function($scope, $http) {
		var init = function() {
			$scope.value.data = [];
			
			//FIXME this is taken 1:1 from datatable widget
			if ($scope.config.objectSelectionMethod === 'object-search') { 
				var searchConf = {
					// page size is this big because of CMF-6554, we should talk about this...
					initialSearchArgs: angular.extend($scope.config.searchCriteria, { pageSize: 1000 })
				};
				
				// FIXME: This initializes the whole search - drop-down values, templates and all...
				// Here we only need the URL for the search, and should avoid initializing the UI for the search.
				var search =  $({ }).basicSearch(searchConf);
				
				var url = search.basicSearch('buildSearchUrl');
				
				$http.get(url).success(function(data) {
					$scope.value.data = data.values;
				});
			} else {
				//load objects by id from the basket
				var objects = [ ];
				_.forEach($scope.value.manuallySelectedObjects, function(item) {
					objects.push(_.omit(item, 'compact_header'));
				});
				
				$http.post(EMF.servicePath + '/object-rest/objectsByIdentity', JSON.stringify({ itemList: objects, fields: [] })).success(function(data) {
					$scope.value.data = data.values;
				});
			}
		}
		
		init();
		
		$scope.$on('widget-config-save', function(event, config) {
			init();
		});
	}]);
	
	_objectsList.directive('objectsList', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/objectsList/template.html',
			controller : 'ObjectsListWidgetController'
		};
	});
	
}());