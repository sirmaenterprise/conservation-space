(function () {
	'use strict';
	
	// Register widget and all needed params in widget manager
	widgetManager.registerWidget('imageWidget', {
		configurable: true,
		identifiable: true,
		compileOnSave: false,
		configController: "ImageWidgetConfigureController",
		titled: true,
		configDialogWidth: 920,
		configDialogHeight: 630
	});
	
	var _imageWidget = angular.module('imageWidget',['ng']);
	
	/** Controller for the directive configuration */
	_imageWidget.controller('ImageWidgetConfigureController', ['$scope', function($scope) {
		
	}]);
	
	/** Controller for the directive initialization */
	_imageWidget.controller('ImageWidgetController', ['$scope', '$compile', function($scope, $compile) {

		$scope.getSearchConfig = function() {
			var config = { 
				onSearchCriteriaChange: [ $scope.updateConfigWithSearchCriteria ]
			};
			
			if ($scope.config.searchCriteria) {
				config.initialSearchArgs = $scope.config.searchCriteria;
			}  else {
				config.initialSearchArgs  = {
						location			: ["emf-search-context-current-object"] 
				}
			}
			config.initialSearchArgs.mimetype =  "^image/";
			
			config.listeners = {
					'basic-search-initialized': function() {
						$(this).trigger('perform-basic-search');	
					}
			}
			return config;
		}
		
		$scope.getObjectPickerConfig = function() {
			var cfg = $.extend(true, { }, $scope.getSearchConfig());
			cfg.browserConfig = {
				filters	: "(mimetype.indexOf('image')!=-1)",
				singleSelection : true
			},
			cfg.uploadConfig = {
				isNewObject		: idoc.isNewObject()
			},
			cfg.selectDeselectCallback	= $scope.objectSelectDeselectCallback,
			cfg.browserSelectDeselectCallback	= $scope.objectSelectDeselectCallback,
			cfg.uploadSelectDeselectCallback = $scope.objectSelectDeselectCallback,
			cfg.searchFieldsConfig = {
				objectType: { disabled: true }
			},
			cfg.singleSelection = true,
			cfg.selectedItems = { };
			if ($scope.config.imageId) {
				cfg.selectedItems[$scope.config.imageId] = $scope.config.imageId;
			}

			return cfg;
		}
		
		$scope.updateConfigWithSearchCriteria = function(newCriteria) {
			$scope.config.searchCriteria = newCriteria;
		}
		
		/** Called when the user selects an object from the picker */
		$scope.objectSelectDeselectCallback = function(data) {
			$scope.config.imageId = data.currentSelection.dbId;
			$scope.reload = true;
		}
		
		// Recreate SVG. This is needed because browser can not do it automatically. 
		// We have to reset all needed data.
		$scope.recreateSvg = function(data) {
			
			var svgElement = data.svg,
		    	width = svgElement.attr('width'),
		    	height = svgElement.attr('height'),
		    	
		    	wrapper = $("<div/>").append(svgElement),
		    	svgHTML,
		    	coefficient = width / $scope.config.imageWidth;
		
		    svgElement.attr("width", width / coefficient)
		    	.attr("height", height / coefficient)
		    	.attr("style", "display: block; margin: auto");
		    
			var bild = document.createElementNS('http://www.w3.org/2000/svg','image');
			bild.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', data.bgImage.attr('xlink:href'));
			bild.setAttribute("id", data.bgImage.attr('id'));
			bild.setAttribute("x", data.bgImage.attr('x'));
			bild.setAttribute("y", data.bgImage.attr('y'));
			bild.setAttribute("height", data.bgImage.attr('height'));
			bild.setAttribute("width",data.bgImage.attr('width'));
			
			var item = svgElement.find('g');
			item.get(0).replaceChild(bild, svgElement.find('#backimage000').get(0));
			
			// save annotation filter if it exist
			if(typeof data.filterCriteria != "undefined") {
				$scope.config.filterCriteria = data.filterCriteria;
			}
			
			// save annotation level if it exist
			if(typeof data.level != "undefined") {
				$scope.config.level = data.level;
			}
			
			// For some reason safari 6 remove "xlink" - issue CMF-7528
			if($.browser.safari && $.browser.version == "536.28.10") {
				$scope.config.svg = wrapper.html().replace(/href/g, "xlink:href");
			} else {
				$scope.config.svg = wrapper.html(); 
			}

			$scope.$emit('widget-config-save', $scope.config);
		}
		
		$scope.$on("ia-iframe-closed",function(event, data) {
			$scope.recreateSvg(data);
		});
		
		$scope.onAnnotationToolClose = function() {
			$scope.$apply(function() {
				$scope.showAnnotateDialog = false;
			});
		}
		
		$scope.annotationToolDialogOptions = {
			title: _emfLabels["widget.imageWidget.annotation"],
			dialogClass: 'notifyme',
			modal: true,
			draggable: true,
			width: 1300,
			height: 700,
	       	buttons : [{
	           	text : _emfLabels["insert"],
	           	priority: 'primary',
	           	click : function() {
	           		// get all needed info from IA tool
	           		var data = {};
	           		data.svg = $('#iaframe').contents().find('#svgcontent');
	           		data.bgImage = $('#iaframe').contents().find('#backimage000');

	           		var iFrameDocument = document.getElementById('iaframe').contentWindow;
	           		data.filterCriteria = iFrameDocument.$('.emf-comments-filter-wrapper').scope().filterCriteria;
	           		data.level = $('#iaframe').contents().find("input:radio[name=group1]:checked").val();

	           		if($scope.config.hideThumbnail) {
		           		data.svg.find('#frontimage').hide();
		           		data.svg.find('#frontimagerect').hide();
	           		}

            		$scope.$broadcast("ia-iframe-closed", data);
            		$scope.onAnnotationToolClose();
            	}
        	},
        	{
            	text : _emfLabels["reload"],
	           	priority: 'secondary',
	           	click : function() {
	           		
	          		$scope.config.svg = undefined;
	          		$scope.config.level = undefined;
	          		$scope.config.filterCriteria = undefined;

	          		$scope.imageLink = EMF.applicationPath + '/content/' + $scope.config.imageId;
	          		$scope.$emit('widget-config-save', $scope.config);
	        		$('#' + $scope.imageWrapperId).empty();
	        		$('#' + $scope.imageWrapperId).html('<img src="' + $scope.imageLink + '" width="' + $scope.config.imageWidth + '" />');

	        		$scope.onAnnotationToolClose(); 
	           	}
            },
            { 
        		text : window._emfLabels["cancel"],
        		priority: 'secondary',
                click : function() {
                	$scope.onAnnotationToolClose();
                }
            }],
        	open: function() {
        		$('#iaframe').css( "visibility", "hidden");
        		$('#iaframe').load(function(){
        			$('#iaframe').css( "visibility", "visible");
        			var iFrameDocument = document.getElementById('iaframe').contentWindow;

        			iFrameDocument.$('#topHeader').hide();
        			iFrameDocument.$('#header').hide();
        			iFrameDocument.$('#formId\\:contextDataHeader').hide();
        			
	        		if(typeof $scope.config.filterCriteria != "undefined") {
	        			var zoom = $('#iaframe').contents().find('#tool_zoom_in');
	        			setTimeout( function() {
		        			for(var i=1; i < $scope.config.filterCriteria.zoomLevel; i++) {
		        				var img = $(zoom).find('img');
	        					img.trigger("click");
		        			}
		        			
		        			iFrameDocument.$('.emf-comments-filter-wrapper').scope().filterCriteria = $scope.config.filterCriteria;
		        			iFrameDocument.$('.emf-comments-filter-wrapper').scope().applyFilter();
		        			
	        			}, 1000); // the image need time to load and after that be resized...
	        			
	            		if($scope.config.level) {
		            		var radio = $('#iaframe').contents().find("input:radio[name=group1]");
		            		$.each($(radio), function(key, item) {
		            			var btn = $(item);
		            			if($scope.config.level == btn.val()) {
		            				btn.attr('checked', 'checked');
		            				btn.trigger("click");
		            			}
		            		})
	            		}
	        		}
        		});
        	},
            close : function() {
            	$scope.onAnnotationToolClose();
            }
	    };
		
		$scope.openAnnotationTool = function() {
			$scope.showAnnotateDialog = true;
			$scope.annotationToolSrc = EMF.bookmarks.buildLink('documentinstance', $scope.config.imageId);
		}
		
		$scope.$on('widget-config-save',function(event, config) { 
			if($scope.config.imageId) {
				$scope.imageLink = EMF.applicationPath + '/content/' + $scope.config.imageId;

				// Remove the svg and load new image if user choose from config.
				if($scope.reload) {
		      		$scope.config.svg = undefined;
		      		$scope.config.level = undefined;
		      		$scope.config.filterCriteria = undefined;
		      		
			    	$('#' + $scope.imageWrapperId).empty();
			    	$('#' + $scope.imageWrapperId).html('<img data-ng-src="' + $scope.imageLink + '" src="' + $scope.imageLink + '" width="' + $scope.config.imageWidth + '" />');
				}
		    	$scope.reload = false;
			} else {
				$scope.imageLink = "";
			}
		});
	}]);
	
	/** Image widget directive */
	_imageWidget.directive('imageWidget', ['$document', function($document) {
		return {
			restrict: 'E',
			replace: true,
			templateUrl: EMF.applicationPath + '/widgets/imageWidget/template.html',
			controller : 'ImageWidgetController',
			link: function(scope, element, attrs) {
				scope.imageWrapperId = EMF.util.generateUUID();
				
				// TODO: find out WHY? I can not get the width dynamically. I get different values any time!!
				//scope.maxWidth = element.width() - 18;
				scope.maxWidth = 600;
				scope.config.imageWidth = scope.config.imageWidth || scope.maxWidth;
				
				scope.$watch('showAnnotateDialog', function(newValue, oldValue) {
					if (newValue) {
						element.find('#open-ia-tool-dialog').dialog(scope.annotationToolDialogOptions);
					} else {
						angular.element('#open-ia-tool-dialog').dialog("destroy");
					}
				});

				scope.$watch('config.imageWidth', function(newValue, oldValue) {
					
					if(isNaN(newValue)) {
						scope.config.imageWidth = oldValue;
					}
					
 					if (newValue) { 
						// image width should not exceed max allowed width of document
						if(newValue > scope.maxWidth) {
							scope.config.imageWidth = scope.maxWidth; 
						}
						
						if (scope.config.svg) {
							var data = {}; 
							data.svg = $(scope.config.svg);
							data.bgImage = data.svg.find('#backimage000');
							
							if(scope.config.hideThumbnail) {
								data.svg.find('#frontimage').hide();
								data.svg.find('#frontimagerect').hide();
							} else {
								data.svg.find('#frontimage').show();
								data.svg.find('#frontimagerect').show();
							}
							
							scope.recreateSvg(data);
						} else {
							// FIXME: use angular to bind this
							var image = element.find('#' + scope.imageWrapperId).find('img');
							image.attr("width", newValue);
						}
		            	scope.$emit('widget-config-save', scope.config);
		            	element.trigger('widgets:dom:widget-resize');
					}
				});
				
				scope.$watch('config.svg', function(newValue) {
					// FIXME: use angular to bind this
					element.find('#' + scope.imageWrapperId).html(newValue);
				});

				scope.$watch('imageLink', function(value, oldValue) {
					if (value && value !== oldValue) {
						element.trigger('widgets:dom:widget-resize');
					}
				});
			}
		};
	}]);

}());