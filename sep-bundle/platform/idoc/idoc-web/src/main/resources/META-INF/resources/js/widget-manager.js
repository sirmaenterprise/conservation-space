'use strict';

/**
 * Manages the widget (module) registration within the widgets angular module.
 * Each widget should register its module in order to be processed by angular.
 */
window.widgetManager = function() {

	// holds mapping of widget configuration mapped by widget name
	var widgets = {};
	var widgetsNames = [];
	
	var components = [];
	
	var defaultWidgetSettings = {
		configurable: false,
		titled: false,
		inline: false,
		singleton: false,
		hideIfEmpty: false,
		identifiable: false,
		compileOnSave: true,
		configDialogWidth: "auto",
		configDialogHeight: "auto"
	}

	return {
		registerComponent: function(componentName) {
			components.push(componentName);
		},
		
		getComponents: function() {
			return components;
		},
		
		/**
		 * Adds a widget to the widget manager.
		 * @param name widget name
		 * @param settings widget configuration as JSON object. Following properties are available:
		 * - configurable - if true the widget allows additional configuration using config dialog. Default: false
		 * - configController - name of angularjs controller managing widget config dialog
		 * - titled - when true the widget will support title input and will display the title in preview mode. Default: false
		 * - inline - if true, the widget should be displayed as inline element. Default: false
		 * - singleton - only one instance of the widget could be added. Default: false
		 * - identifiable - if true, the widget will have unique id. Default: false
		 * - compileOnSave - if true the widget will be recompiled after save and continue action
		 * - hideIfEmpty - if true in preview mode the widget will not be displayed if $scope.isWidgetEmpty() return true. Default: false
		 * - configDialogWidth - initial width of the config dialog. Default: "auto".
		 * - configDialogHeight - initial height of the config dialog. Default: "auto".
		 * - 
		 */
		registerWidget : function(name, widgetSettings) {
			var settings = {};
			// apply default settings
			angular.extend(settings , defaultWidgetSettings);
			// apply concrete widget settings
			angular.extend(settings , widgetSettings);
			
			widgetsNames.push(name);
			widgets[name] = settings;
		},
		
		toSnakeCase : function(widgetName) {
			return widgetName.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase();
		},

		getWidgets : function() {
			return widgetsNames;
		},
		
		getWidgetSettings : function(name) {
			return widgets[name];
		},
		
		serializeObjectForAttribute: function(object) {
			if (!object) {
				return '';
			}
			return _.escape(JSON.stringify(object));
		},
		
		deserializeFromAttributeValue: function(value, create) {
			if (value) {
				return JSON.parse(_.unescape(value));
			} else if (create) {
				return { };
			}
			return null;
		},

		/**
		 *  Checks if an element is inside a widget. Returns true if in widget, false otherwise.
		 *  @param element - the element to check
		 */
		isInWidget: function(element) {
			return element.closest('.widget').length > 0;
		}
	}
}();

var _componentsModule = angular.module('components', widgetManager.getComponents());
var _widgetsModule = angular.module("widgets", widgetManager.getWidgets());

/**
 * Controller for the root widget directive.
 */
_widgetsModule.controller('WidgetController', ['$scope', function($scope) {
	
	$scope.deleteWidget = function() {
		$scope._deleted = true;
	}
	
	$scope.saveWidgetTitle = function() {
		var title = $scope.config.widgetTitle,
			oldConfig = widgetManager.deserializeFromAttributeValue($scope.confAttr, true);
		
		oldConfig.widgetTitle = title
		$scope.confAttr = widgetManager.serializeObjectForAttribute(oldConfig);
		$scope.readonlyTitle = true;
	}
	
	$scope.toggleConfigDialog = function(opened) {
		if (opened) {
			$scope.$broadcast('widget-config-open', $scope.dialogSettings, $scope.config);
		} else {
			$scope.$broadcast('widget-config-close');
		}
		$scope.showConfigDialog = opened;
	}
	
	$scope.$on('widget-config-save', function(event, configObject) {
		// this will trigger a $watch expression in the directive which will save the value to the attribute
		$scope.confAttr = widgetManager.serializeObjectForAttribute(configObject);
		$scope.config = configObject;
	});
	
	$scope.$on('widget-config-revert', function() {
		$scope.config = widgetManager.deserializeFromAttributeValue($scope.confAttr, true);
	});

	$scope.$on('widget-value-save', function(event, valueObject) {
		// this will trigger a $watch expression in the directive which will save the value to the attribute
		$scope.valueAttr = widgetManager.serializeObjectForAttribute(valueObject);
		$scope.value = valueObject;
	});
}]);

/**
 * Directive that compiles a widget by inserting the widget tag provided by the
 * name attribute of the <widget> tag.
 */
_widgetsModule.directive('widget', ['$timeout', function($timeout) {
	return {
		restrict : 'A',
		scope: true,
		controller: 'WidgetController',
		link : function(scope, element, attrs) {
			// Init
			var id = element.attr('id');
			if (!id) {
				id = EMF.util.generateUUID();
				element.attr('id', id);
			}

			scope.readonlyTitle = true;
			scope.confAttr = attrs.config || '{ }';
			scope.valueAttr = attrs.value || '{ }';

			scope.config = widgetManager.deserializeFromAttributeValue(scope.confAttr, true);
			scope.value = widgetManager.deserializeFromAttributeValue(scope.valueAttr, true);
			
			scope.widgetName = attrs.name;
			scope.widgetDirectiveName = widgetManager.toSnakeCase(scope.widgetName);
			scope.settings = widgetManager.getWidgetSettings(scope.widgetName);
			scope.showConfigDialog = false;
			
			if (scope.settings.compileOnSave) {
				element.attr('compileOnSave', true);
			}
			
			if (scope.settings.configurable) {
				scope.dialogSettings = {
					title: window._emfLabels["widget." + scope.widgetName + ".title"],
					modal: true,
					draggable: true,
					resizable: false,
					width: scope.settings.configDialogWidth || 800,
					height: scope.settings.configDialogHeight || 500,
					dialogClass: "widget-config-dialog notifyme",
					buttons : [
					    {
					      	text : window._emfLabels["ok"],
					       	id : "widget_config_save_btn",
					       	priority: 'primary',
					       	click : function() {
					       		scope.$apply(function() {
					       			scope.$broadcast('widget-config-save', scope.config);
					       			scope.toggleConfigDialog(false);
					       		});
					       	}
					    },
					    {
					    	text : window._emfLabels["cancel"],
					    	id : "widget_config_cancel_btn",
					    	priority: 'secondary',
					    	click : function() {
					    		scope.$apply(function() {
					    			scope.$broadcast('widget-config-revert');
					    			scope.toggleConfigDialog(false);
					    		});
					    	}
					    }
					],
					close : function() {
						scope.toggleConfigDialog(false);
						scope.$broadcast('widget-config-revert');
					}
				};
			}
			
//			Don't put contenteditable=false on the root element, 
//			so that tinymce can fall into the traps (.widget-caret-trap CS-571)
//			element.attr('contenteditable', false);
			
			element.attr('spellcheck', false);
			element.addClass('widget widget-' + scope.widgetDirectiveName);
			
			// Watch expressions
			scope.$watch('previewMode', function(newValue) {
				if (newValue) {
					element.removeClass('edit-mode');
					element.addClass('preview-mode');
				} else {
					element.removeClass('preview-mode');
					element.addClass('edit-mode');
				}
			});

			scope.$watch('confAttr', function(newValue) {
				element.attr('data-config', newValue);
			});
			
			scope.$watch('valueAttr', function(newValue) {
				element.attr('data-value', newValue);
			});
			
			scope.$watch('_deleted', function(value) {
				if (value) {
					$timeout(function() {
						element.remove();
					}, 0);
				}
			});
			
			// Event handlers
			element.on('paste keydown', function(event) {
				// if event is key down check for ctrl + v
				// something funky is happening o on V down and the event is not stopped.
				if (event.type === 'paste' || (event.type === 'keydown' && event.keyCode === 86 && event.ctrlKey)) {
					event.stopPropagation();
				}
			});
			
			element.on('keypress keydown keyup', function(event) {
				event.stopPropagation();
				
				// enter key CMF-6917
				// Enter should be allowed in textbox widget - CMF-7135
				if (event.keyCode === 13 && event.target.tagName != 'TEXTAREA') { 
					event.preventDefault();
				}
			})

			element.click(function(event) {
				// we need to transfer the click event on the menu to the document,
				// otherwise widget menu won't open/close
				if (!scope.previewMode) {
					var e = $.Event( "click" );
					e.target = event.target;
					e.pageX = event.pageX;
					e.pageY = event.pageY;
					$(document).trigger(e);
				}
			});
			
			element.on('dragover dragenter drop', function(event) {
				event.stopPropagation();
				event.preventDefault();
				return false;
			});
			
			element.on('click', function(event) {
				event.stopPropagation();
			});
		},
		template: function(element, attrs) {
			var nameAttr = attrs.name,
				name = widgetManager.toSnakeCase(nameAttr),
				template,
				settings = widgetManager.getWidgetSettings(nameAttr);

				template = '<div class="widget-caret-trap user-select-none" style="font-size: 1px; visibility: hidden;">\
								<span class="widget-caret-trap" style="font-size: 1px; color:#EEE;">trap</span>\
							</div>\
							<div class="widget-title-and-actions-wrapper user-select-none" contenteditable="false">\
								<div data-ng-show="!previewMode" class="widget-actions-wrapper">\
									<div  class="widget-actions-wrapper-btn btn-group">\
										<button type="button" class="widget-actions btn btn-default dropdown-toggle" data-toggle="dropdown">\
											<span class="caret"></span>\
										</button>\
										<ul class="dropdown-menu" role="menu">\
											<li ng-if="settings.configurable" >\
												<a href="javascript:void(0);" role="button" prevent-default-handler="click" ng-click="toggleConfigDialog(true)">\
													<span emf-label="widgets.btn.config"/>\
												</a>\
											</li>\
											<li>\
												<a href="javascript:void(0);" role="button" prevent-default-handler="click" ng-click="deleteWidget()" >\
													<span emf-label="widgets.btn.delete"/>\
												</a>\
											</li>\
										</ul>\
									</div>\
								</div>\
								<div class="widget-title-wrapper">\
									<div class="user-select-none" data-ng-show="previewMode">{{config.widgetTitle}}</div>\
									<span class="widget-caret-trap" style="font-size: 1px; color:#EEE;">trap</span>\
									<input class="widget-title-input" data-ng-class="{ \'widget-title-input-readonly\': readonlyTitle }" data-ng-readonly="readonlyTitle || previewMode" \
											data-ng-click="readonlyTitle = false" data-ng-hide="previewMode" data-ng-blur="saveWidgetTitle()" data-ng-model="config.widgetTitle" placeholder="Widget title" />\
									<span class="widget-caret-trap" style="font-size: 1px; color:#EEE;">trap</span>\
								</div>\
							</div>\
							<div class="widget-body-wrapper" contenteditable="false">\
								<div class="user-select-none" widget-config-dialog="%widgetName%"></div>\
								<%widgetDirectiveName%></%widgetDirectiveName%>\
							</div>\
							<div class="widget-caret-trap user-select-none" style="font-size: 1px; visibility: hidden;">\
								<span class="widget-caret-trap" style="font-size: 1px; color:#EEE;">trap</span>\
							</div>';
				template = template
							.replace(/%widgetDirectiveName%/gmi, name)
							.replace(/%widgetName%/gmi, nameAttr);
			return template;
		}
	};
}]);

_widgetsModule.controller('WidgetConfigDialogController', ['$scope', '$http', function($scope, $http) {
	
	$scope.getTemplate = function(successFn) {
		$http.get(EMF.applicationPath + '/widgets/' + $scope.widgetName + '/config.html')
			.success(function(response) {
				// TODO: cache, $templateCache maybe?
				var template = '<div data-ng-controller="' + $scope.settings.configController + '">' + response + '</div>';
				successFn.call(this, template);
			});
	}
	
}]);

_widgetsModule.directive('widgetConfigDialog', ['$compile', function($compile) {
	return {
		restrict: 'A',
		controller: 'WidgetConfigDialogController',
		link: function(scope, element, attrs) {
			scope.widgetName = attrs.widgetConfigDialog;
			
			scope.$on('widget-config-open', function(event, dialogConfiguration, config) {
				scope.getTemplate(function(template) {
					var compiled = $compile(template)(scope);
					element.append(compiled);
					scope.$broadcast('widget-config-init', config);
					element.dialog(dialogConfiguration);
					$(".ui-dialog-titlebar-close").hide();
				});
			});
			
			scope.$on('widget-config-close', function() {
				element.dialog("destroy");
				// removing the element removes the scope as well
				element.children().remove();
			});
		}
	}
}]);

_widgetsModule.directive('disableContentEditable', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.on('click', function() {
				element.attr('contenteditable', true);
			});
			
			scope.$on('remove-content-editable', function() {
				element.removeAttr('contenteditable');
			});
		}
	}
});
