(function () {
	'use strict';
	
	// Register widget and all needed params in widget manager
	widgetManager.registerWidget('relatedDocuments', {
		configurable: false,
		identifiable: true,
		compileOnSave: false,
		titled: true
	});
	
	var _relatedDocuments = angular.module('relatedDocuments',['ng']);
	
	/** Controller for the directive initialization */
	_relatedDocuments.controller('RelatedDocumentsController', ['$scope', '$compile', function($scope, $compile) {
		 
		$scope.init = function() { 

		}
		
		/**
		 * Upload document to existing document or cultural object
		 */
		$scope.upload = function() {
			// create popup 
			var _dialogView = '<div id="upload-dialog"><div class="upload-content-panel"></div></div>';
			$("body").append($compile(_dialogView)($scope));

			// destroy popup on close
			var _onclose = function() {
				$(this).dialog("destroy");
	        	jQuery('#upload-dialog').remove();
			};

			// fill dialog options
			var dialogOptions = {
					title: _emfLabels['cmf.upload.document.popup.title'],
					modal: true,
					draggable: true,
					width: 1050,
					height: 600,
					dialogClass: "widget-config-dialog notifyme",
		        	buttons : [{
		            	text : window._emfLabels["ok"],
		            	priority: 'primary',
		            	click : function() {
		            		_onclose();
		            	}
		        	}
		        	,{
		        		text : window._emfLabels["cancel"],
		        		priority: 'secondary',
		                click : function() {
		                	_onclose();
		                }
		            }],
					resizable: true,
		            close : function() {
		            	_onclose();
		            }
		     };
			
			jQuery('#upload-dialog').dialog(dialogOptions);
			
			var element = $('.upload-content-panel');
			var upload = element.upload({
				isNewObject		: idoc.isNewObject(),
				allowSelection	: false
			});
			element.data('upload', element);

		}
	}]);
	
	/** Related documents widget directive */
	_relatedDocuments.directive('relatedDocuments', function() {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/relatedDocuments/template.html',
			controller : 'RelatedDocumentsController',
			link: function(scope, element, attrs) {
				if(idoc.object.id == null) { 
					return;
				}
				
				var predefinedSearchCriteria = {
						createdBy			: [], 
						createdFromDate 	: "",
						createdToDate		: "",
						fields				: ["title"], 
						location			: [idoc.object.id],
						metaText			: "",
						objectRelationship	: [],
						objectType			: ["ptop:Document", "emf:Image"],
						orderBy				: "emf:modifiedOn",
						orderDirection		: "desc",
						pageNumber			: 1,
						pageSize			: 1000 
				}
				
				var predefinedProperties = [
				        {checked:true, editType:"text", editable:true, name:"title", title:"Title"},
				        {checked:true, editType:"text", editable:true, name:"description", title:"Description"},
				        {checked:true, editType:"text", editable:false, name:"mimetype", title:"MimeType"},
				        {checked:true, editType:"text", editable:false, name:"createdBy", title:"Created By"},
				        {checked:true, editType:"datetime", editable:false, name:"createdOn", title:"Created On"}
				] 
				
				scope.config.objectSelectionMethod = "object-search";
				scope.config.searchCriteria =  predefinedSearchCriteria;
				scope.config.selectedProperties = predefinedProperties;
			}
		};
	});

}());