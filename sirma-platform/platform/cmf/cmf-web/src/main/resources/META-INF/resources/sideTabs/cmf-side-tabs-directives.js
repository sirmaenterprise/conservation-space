;(function() {
'use strict';
	/** Module holds all directives for side-tabs. */
	var module = angular.module('cmfSideTabsDirectives', ['widgets']);

	/**
	 * Main directive for tabs that represent initialization point for <b>Side tabs</b> module.<br />
	 * The directive will extract the required data from the model and will generate tab and tab
	 * content(from template). This elements will be compiled and applied in the DOM. The elements
	 * holds specific angular-js attributes, the compilation will allow this technology to recognize
	 * them.
	 */
	module.directive('sideTabs', function($timeout, $compile) {
		return {
			restrict : 'EA',
			templateUrl : EMF.applicationPath + '/sideTabs/templates/cmf-side-tabs.tpl.html',
			controller : 'SideTabsController',
			link : function(scope, element, attrs) {
				var util = {
					/**
					 * Based on the tab meta-data will generate and place tab content. In this case
					 * will be include template that will be identified with specific controller.
					 *
					 * @param controller -
					 *        tab controller received from the model
					 * @param template -
					 *        template path for specific tab
					 */
					getTabContentHolder : function(controller, template) {
						var templateUri = EMF.applicationPath + template;
						var tabContent = '<div class="test" ng-controller="' + controller + '">';
						if ($.trim(template)) {
							// escaped quotes are needed, because after the element is compiled there are unexpected token error
							tabContent += '<div ng-include src="\'' + templateUri + '\'"></div>';
						}
						tabContent += '</div>';
						return tabContent;
					},
					/**
					 * Generate container based on directive. In case the model return JSON that
					 * holds tab data that will be executed as directive.
					 */
					getDirectiveAsContainer : function(directive) {
						return '<div>' + directive + '</div>';
					},
					/**
					 * Generate container based on expression. In case the model return JSON that
					 * holds tab data that will be executed as expression.
					 */
					getExpressionAsContainer : function(expression) {
						return '<div>' + expression + '</div>';
					}
				};

				// selector for tab's content wrapper
				scope.contentWrapperSelector = attrs.contentWrapperSelector || '.side-tabs .content-wrapper';
				var tabsContentWrapper = angular.element(scope.contentWrapperSelector);
				var compiled = $compile('<div style="position: relative;" id="{{tab.id}}" ng-repeat="tab in tabs"></div>')(scope);
				tabsContentWrapper.append(compiled);

				/**
				 * When Save and Continue action is preformed recreate and display the content of the tab.
				 */
				$(document).on('idoc-saved:dom:update', function() {
					if(scope.currentTab.id === 'versions') {
						angular.element('#' + scope.currentTab.id).empty();
						angular.element('.tab-toolbar.' + scope.currentTab.id).empty();
						scope.currentTab.compiled = false;
						scope.loadTabData(scope.currentTab);
					}
				});

				/**
				 * Listen for change of specific scope entry <b>currentTab</b>. Every time, when
				 * this scope entry is changed/modified, the method will try to recreate and display
				 * the content of the tab.
				 */
				scope.$watch('currentTab', function(tabMetadata) {
					scope.loadTabData(tabMetadata);
				});

				/**
				 * Recreate and display the content of the tab.
				 */
				scope.loadTabData = function(tabMetadata) {
					var domNode, jqNode, compiledToolbar;
					if (!tabMetadata || tabMetadata.compiled) {
						return;
					}
					// Tabs based on specific controller and template
					if (tabMetadata.controller) {
						domNode = util.getTabContentHolder(tabMetadata.controller, tabMetadata.template);
					}
					// Tabs based on specific directive
					if (tabMetadata.directive) {
						domNode = util.getDirectiveAsContainer(tabMetadata.directive);
					}
					// Tabs based on specific expression
					if (tabMetadata.expression) {
						domNode = util.getExpressionAsContainer(tabMetadata.expression);
					}
					// The tabs may have and a tool-bar
					if (tabMetadata.toolbar) {
						compiledToolbar = $compile(tabMetadata.toolbar)(scope);
					}
					jqNode = $(domNode);
					// In some cases the element with <i>tabMetadata.id</i> is not available
					// in the DOM, so we need to 'pause' the process and apply at the end of the
					// queue:
					// the problem appears for permission and comment tabs
					$timeout(function() {
						angular.element('#' + tabMetadata.id).append(jqNode);
						$compile(jqNode)(scope);
						angular.element('.tab-toolbar.' + tabMetadata.id).append(compiledToolbar);
						tabMetadata.compiled = true;
					}, 0);
				};
				
				scope.$watch('tabs', function(value) {
					if (!value) {
						return;
					}
					$timeout(function() {
						$('.side-tabs').tabs();
					}, 0);
				});
			}
		};
	});

	/**
	 * This directive generate user or group image.
	 */
	module.directive('avatarImage', [function() {
		return {
			restrict : 'A',
			scope : {
				member : '='
			},
			link : function(scope, element) {
				var data = {
					template : '<img src="{1}" />',
					proxy : '/service/dms/proxy/',
					defaultUserImg : '/images/user-icon-32.png',
					defaultGroupImg : '/images/group-icon-32.png'
				};
				var contextPath = SF.config.contextPath;
				var avatarPath = scope.member.image;
				var fullPath = contextPath + data.defaultUserImg;

				if ($.trim(avatarPath)) {
					fullPath = contextPath + data.proxy + avatarPath;
				} else if ($.trim(scope.member.type) !== 'USER') {
					fullPath = contextPath + data.defaultGroupImg;
				}

				data.template = data.template.replace("{1}", fullPath);
				element.replaceWith(data.template);
			}
		};
	}]);

	/**
	 * Directive for retrieving the default user preview.
	 */
	module.directive('avatar', [function() {
		return {
			restrict : 'E',
			controller : 'TabVersionHistory',
			link : function(scope, element) {
				var defaultPath = SF.config.contextPath + '/images/user-icon-32.png';
				var avatar = '<img src="' + defaultPath + '" />';
				element.replaceWith(avatar);
			}
		}
	}]);

	module.directive('stopClickPropagation', [function() {
		// CMF-10253 used on the tabs wrapper list, because IE does funny things
		return function(scope, element) {
			element.click(function(event) {
				event.stopPropagation();
			});
		};
	}]);

	/**
	 * Directive for document revision. Invoked multiple times, based on the supported revision
	 * number. <br />
	 * Catch <i>JSON</i> data returned from <b>InstanceRestService.getRevisions(...)</b> and
	 * construct <i>DOM</i> object based on them.
	 */
	module.directive('revision', function() {
		return {
			restrict : 'A',
			scope : {
				revision : '='
			},
			controller : 'TabDocumentRevisions',
			link : function(scope, element) {
				// We don`t want undefined revisions to enter
				if (scope.revision) {
					var contextInstanceId = EMF.documentContext.currentInstanceId;

					var TPL = {
							RECORD : '<div class="tree-header compact_header revision-record headers-with-icons"></div>'
					};

					var header = scope.revision.compact_header;
					var status = scope.revision.status;
					var id = scope.revision.id;
					var container = $(TPL.RECORD);

					if (contextInstanceId === id) {
						container.addClass('active');
					}
					
					var header = $(header);
					var headerWrapper = $('<div class="instance-header current">');
					headerWrapper.append(header);
					
					container.append(headerWrapper);

					var iconsContainer = $("<div class='icons-container'>");
					container.append(iconsContainer);
					
					// apply the container in the DOM
					element.replaceWith(container);
				}
			}
		}
	});

	/**
	 * Property tab directive. This directive will trigger definition field loading.
	 * Based on the definition will be generated JSON data that holds all fields.
	 * The property tab will be located at:<p>
	 *
	 *  - intelligent document landing page;
	 *  - workflow task landing page;
	 *  - document landing page;
	 *  - domain object landing page;
	 */
	module.directive('properties', function(){
	  return {
	        restrict: 'E',
	        link: function(scope, element) {
	        	var container = $("<div id='properties-tab-body' class='dynamic-form-panel'></div>");
	        	element.replaceWith(container);
	        	container.buildFields({
	        		currentInstanceId		: EMF.documentContext.currentInstanceId,
	        		currentInstanceType		: EMF.documentContext.currentInstanceType,
	        		preview					: true,
	        		renderMandatory 		: false,
	        		restrictByAttibutes : {
    					displayType 		: ['editable','read_only','hidden','delete']
	        		},
	        		services			: {
	        			definitionService	: REST.getClient('DefinitionRestClient')
    	    		},
	        		applicationPath			: EMF.applicationPath
	        	});
	        }
	  }});

	/**
	 * Directive for the versions tab, that gets the last version of the document or object.
	 * It is responsible for the compiling of the link to the last version.
	 */
	module.directive('lastVersion', ['$compile', function($compile) {
		return {
			restrict : "EA",
			scope : {
				lastVersion : '=',
				header : '&'
			},
			controller : 'TabDocumentVersions',
			link : function(scope, element) {
				element.replaceWith($compile( "<span emf-label='cmf.document.tab.versions.current'> </span><span class='tree-header compact_header'><span class='instance-header current'>" + scope.header() + "</span></span>")(scope));
			}
		};
	}]);

}());