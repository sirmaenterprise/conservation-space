"use strict";

app.factory('ContentEditorService',['$compile', '$rootScope', '$timeout', function($compile, $rootScope, $timeout) {
	//http://stackoverflow.com/questions/18771651/detect-keyup-event-of-contenteditable-child-whose-parent-is-a-contenteditable-di
	return {
		EDITOR_LOADED_EVENT : 'editorLoaded',

		init : function(elementSelector, scope, readonly, initcallback) {
			var _this = this,
				_placeHolder,
				contentElement;
			
			_this.scope = scope;

			if (!angular.isDefined(readonly)) {
				readonly = false;
			}

			// FYI: this is the toolbar placeholder
			_placeHolder = angular.element('.idoc-main-editor-placeholder');
			if (readonly) {
				_placeHolder.hide();
			} else {
				_placeHolder.show();
			}

			//remove the editor if previously initialized
			if (_this.editor) {
				_this.editor.destroy();
			}

			EMF.editorConfig = {
				setup: function(editor) {
					_this.editor = editor;

					editor.on('Init', function() {
						editor.focus(false);

						scope.$broadcast(_this.EDITOR_LOADED_EVENT);

						if (initcallback) {
							initcallback();
						}

						_this.ready = true;
	    				CMF.utilityFunctions.checkForDeadInstanceLinks($('#' + editor.id), '/object-rest/exists');
					});

					var contentChangeHandler = $.debounce(100, function() {
						idoc.triggerGlobalEvent('document-content-changed', readonly);
						$rootScope.$broadcast('document-state-change', {dirty:true});
						$('.idoc-comments').trigger('commentRefresh');
					});
					
					/**
					 * The 'visual aid' styles that tinymce puts in edit mode break widgets.
					 * These styles are applied to anchors and tables, so after the damage is already done,
					 * we find them and remove these classes from elements that are within a widget.
					 */
					editor.on('VisualAid', function(event) {
						$(event.element)
							.find('.widget .mce-item-table, .widget .mce-item-anchor')
								.removeClass('mce-item-table mce-item-anchor');
					});

					editor.on('BeforeExecCommand', function(event) {
						// Changes made because of IE10, IE11 - CMF-7371
						var sel = editor.selection;
						var range = $(sel.getRng());

						var forbiddedCommandsInsideHeading	 = [ 'InsertUnorderedList', 'InsertOrderedList' ],
							h1to6Pattern					 = /h[1-6]/i,
							command							 = event.command,
							editorSelection					 = editor.selection,
							node							 = $(editorSelection.getNode()),
							tagName							 = node.prop('tagName'),
							rawSelection					 = $(editorSelection.getSel()),
							eventValue						 = event.value,
							widgetClassPattern				 = /\s*widget\s*/,
							selectionRange					 = range,
							selectionStartElement			 = selectionRange[0].startContainer,
							selectionEndElement				 = selectionRange[0].endContainer,
							toggleFormatElement,
							elementsToToggle				 = [ ],
							id,
							_document						 = document,
							blockFormats					 = [ 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'div', 'p', 'address', 'pre' ];
						
						
						if (node.is('.widget') || node.closest('.widget').length) {
							event.stopPropagation();
							event.preventDefault();
							return false;
						}
						
						if (tagName.match(h1to6Pattern) && _.indexOf(forbiddedCommandsInsideHeading, event.command) != -1) {
							event.preventDefault();
						}

						// CMF-5514 skip widget elements when toggling block formats
						if (command === 'mceToggleFormat' && _.indexOf(blockFormats, eventValue) != -1) {
							event.preventDefault();
							
							toggleFormatElement = selectionStartElement;
							while (true) {
								if (toggleFormatElement && (!toggleFormatElement.className || !toggleFormatElement.className.match(widgetClassPattern))) {
									elementsToToggle.push(toggleFormatElement);
								}
								if (!toggleFormatElement || toggleFormatElement === selectionEndElement) {
									break;
								} else {
									toggleFormatElement = toggleFormatElement.nextElementSibling;
								}
							}
							
							_.each(elementsToToggle, function(element) {
								editor.formatter.toggle(eventValue, { }, element);
							});

							editor.nodeChanged();
						}
						
					});
					
					// FIXME: $broadcast from root scope to the widgets 
					// Fix for CMF-5569
					$('.idoc-editor').on('file-upload-done', function(evt) {
						$.each($('.idoc-editor').find('.widget'), function(index, widget) {
							if($(widget).attr('data-name') == "relatedDocuments") {
								var element = $($(widget).find('.relatedDocuments')).children().eq(1);
 								element.trigger('reload-uploaded-files');
							}
						});
					});
					
					// All widgets containing extjs tables are misaligned after onbeforeunload
					// in new object and have to be recreated
					$('.idoc-editor').on('unload-canceled', function(evt) {
						if(idoc.isNewObject()) {
							_this.init(elementSelector, scope, readonly, initcallback);
						}
					});

					editor.on('ExecCommand', function(event) {
						contentChangeHandler();

						// CMF-4417
						if (event.value && !event.value.match(/h[1-6]/i)) {
							var node = $(editor.selection.getNode());
							if (node.hasClass('idoc-heading')) {
								node.removeClass('idoc-heading idoc-visual-heading-selected');
								node.removeAttr('id');
							}
						}
					});

					/**
					 * When the user creates a header (h1, h2, etc) we create an extra row after it,
					 * and then we focus that row.
					 * A new empty row is created when:
					 * 	- there's no next sibling of the header
					 * 	- the next sibling is a header or and element containing widgets
					 * Otherwise a new empty row is not create, but the next sibling
					 * is selected and the cursor is positioned in the beginning of it.
					 *
					 */
					editor.on('ExecCommand', function(event) {
						// CS-393
						if (!event.value || !event.value.match(/h[1-6]/i)) {
							return;
						}

						var header = $(editor.selection.getNode());
						if (!header.is(':header')) {
							header = header.parents(':header');
						}

						if (!header.length) {
							return;
						}

						var next = header.next(),
							hasWidgets = next.find('widget').length !== 0,
							isNextAHeader = next.is(':header');

						// removing BRs because we can't select the row afterwards
						next.siblings('br').remove();

						if (next.length && !isNextAHeader && !hasWidgets) {
							editor.selection.select(next[0]);
						} else {
							var fragment 	= document.createDocumentFragment(),
								newRow 		= document.createElement("P"),
								span 		= document.createElement("SPAN"),
								bogusBr		= document.createElement('BR');

							// timymce needs this bogus br, so that the caret can be moved to the new row
							bogusBr.setAttribute('data-mce-bogus', '1');
							span.appendChild(bogusBr);
							newRow.appendChild(span);

							editor.dom.insertAfter(newRow, header[0]);
							editor.selection.select(newRow);
						}
						editor.selection.collapse(true);
					});
					
					/**
					 * Helper event handler for the NodeChange event handler.
					 * It is responsible for 'remembering' the last arrow key.
					 */
					editor.on('KeyDown', function(event) {
						var keyCode 		= event.keyCode,
							VK 				= tinymce.util.VK,
							editorSelection = editor.selection,
							delKey 			= (VK.DELETE === keyCode),
							backspaceKey 	= (VK.BACKSPACE === keyCode),
							leftKey 		= (VK.LEFT === keyCode),
							rightKey 		= (VK.RIGHT === keyCode),
							downKey 		= (VK.DOWN === keyCode),
							upKey 			= (VK.UP === keyCode),
							currentRange,
							currentSelection;
						
						if (downKey || upKey || leftKey || rightKey) {
							_this.lastArrowKey = keyCode;
						} else {
							_this.lastArrowKey = null;
						}
						
						//fix for FF - prevent typing when the cursor sometimes gets inside the widget
						var insideAWidget = $(editor.selection.getNode()).closest('.widget').length > 0;
						if (insideAWidget && !upKey && !downKey) {
							event.preventDefault();
							event.stopPropagation();
						}
						
//						TODO: think how to handle backspace and delete
//						
//						if (delKey || backspaceKey) {
//							currentRange = editorSelection.getRng();
//							currentSelection = editorSelection.getSel();
//							console.log(currentRange)
//							console.log(currentSelection)
//							
//							currentSelection.modify('move', 'forward', ')
//							event.preventDefault();
//						}
						
						if (event.ctrlKey) {
							/*
							 * CS-381 - this is needed because apparently timymce add one more undo level on ctr+z.
							 * No sure why but it doesn't work very well with widgets. 
							 */
							// CTRL + Z
							if (keyCode === 90 && editor.undoManager.hasUndo()) {
								editor.undoManager.undo();
								event.preventDefault();
							}
						}
					});

					/**
					 * When the editor changes selected node we check that node to see if it is a
					 * node with class 'widget-caret-trap' (or 'widget'), if it is we must move the 
					 * cursor to the prev/next node of the widget.
					 * This handler relys on the KeyDown event handler to 'remember' the arrow keys.
					 */
					editor.on('NodeChange', function(event) {
						var element = $(event.element),
							itsAWidget = element.is('.widget'),
							itsATrap = element.is('.widget-caret-trap'),
							widget,
							nodeToSelect,
							VK = tinymce.util.VK,
							editorSelection = editor.selection;
						
						// http://youtu.be/piVnArp9ZE0
						if (itsATrap || itsAWidget) {
							if (!_this.lastArrowKey) {
								return;
							}
							
							if (itsAWidget) {
								widget = element;
							} else {
								widget = element.closest('.widget')
							}

							switch (_this.lastArrowKey) {
								case VK.DOWN:
								case VK.RIGHT:
									nodeToSelect = widget.next();
									break;
								case VK.UP:
								case VK.LEFT:
									nodeToSelect = widget.prev();
									break;
								default:
									break;
							}
							
							if (nodeToSelect.length) {
								editorSelection.setCursorLocation(nodeToSelect[0], 0);
							}
						}
					});

					editor.on('NodeChange Change', function(e) {
						setTimeout(function() {
							contentChangeHandler();
						}, 0);
					});

					editor.on('MouseDown', function(event) {

						if(widgetManager.isInWidget($(event.target))) {
							if(/chrom(e|ium)/.test(navigator.userAgent.toLowerCase())) {
								_this.getRootElement().removeAttribute('contentEditable');
							} else if($.browser.msie || !!navigator.userAgent.match(/Trident.*rv\:11\./)) {
								
								// Click in content to prevent document "jump" in IE
								$(event.target).click();
								if($(event.target).hasClass("checkbox-widget-label")) {
									$(event.target).click(); 
								}
								if($(event.target).hasClass("widget-actions")) {
									$(event.target).click();
								}

								if(event.target.tagName != 'INPUT') {
									_this.getRootElement().removeAttribute('contentEditable');
								} 
							} else {
								if(event.target.tagName != 'INPUT') {
									_this.getRootElement().removeAttribute('contentEditable');
								}
							}
							
						} else {
							// CMF-7417 - Detect IE10 and IE11 and prevent focus on root element.
							if ($.browser.msie || !!navigator.userAgent.match(/Trident.*rv\:11\./)) {
								if($(event.target).children().length == 1) {
									$(event.target).append('<br>');
									tinymce.activeEditor.selection.setCursorLocation($(event.target), 1);
									$(event.target).focus();
								}
								var rootElement = $(_this.getRootElement());
								
								if(!$(_this.getRootElement()).attr('contentEditable')) {
									rootElement.blur();
									_this.getRootElement().setAttribute('contentEditable', true);
								}
				            } else {
				            	// (facepalm) CMF-7095
								var rootElement = $(_this.getRootElement());
								rootElement.blur();
								_this.getRootElement().setAttribute('contentEditable', true);
								rootElement.focus();
							}
						}
					});
					
					// Workaround...disable drop of the section text in the widgets
					// FIX for CMF-4232 Drag and drop text from document to widget
					editor.on('Drop', function(event) {
						
						// Stops some browsers from redirecting.
						event.stopPropagation();
						
						if(widgetManager.isInWidget($(event.target))) {
							event.preventDefault();
							return false;
						}
						
						var bookmark = editor.selection.getBookmark();
						// TODO: I guess here the content is not yet inserted, so we wait for the next event loop
						// we could listen for the DOMNodeInserted event
						// Any other ideas?
						$timeout(function() {
							// manually add an undo level because tinymce doesn't ondrop event
							editor.undoManager.add();
							editor.selection.moveToBookmark(bookmark);
							_this.compile();
						}, 0);
						
					});
					
					editor.on('PastePreProcess', function(event) {
						var pasteContentJqWrapped = $('<div />').append(event.content),
							versionsWidgetInserted = $(_this.getRootElement()).find('.widget.widget-versions').length > 0;
						
						pasteContentJqWrapped
							.find(':header')
								.removeAttr('id')
								.end()
							.find('.widget')
								.removeClass('ng-scope edit-mode preview-mode')
								.empty();
						
						if (versionsWidgetInserted) {
							pasteContentJqWrapped.find('.widget.widget-versions').remove();
						}
						
						event.content = pasteContentJqWrapped.html();
					});

					/**
					 * BeforeSetContent is called on paste,undo,redo,insertContent.
					 * this handler should remove the ng-scope class from directives,
					 * so that they can be compiled again in the 'SetContent' handler
					 */
					editor.on('BeforeSetContent', function(event) {
						event.content = _this.removeNgScopeClass(event.content);
					});
					
					editor.on('AddUndo Undo Redo', function(event) {
						// TODO: debounce maybe?
						_this.compile();
					});
					
					editor.on('Paste KeyUp Undo Redo', _.debounce(function() {
						$(document).trigger('idoc-clear-draft');
					}, 500));
				},
				selector: elementSelector,
				inline: true,
				readonly: readonly,
				plugins: [
				          "advlist autolink lists link image charmap hr anchor pagebreak",
				          "searchreplace wordcount visualblocks visualchars",
				          "insertdatetime media nonbreaking table directionality",
				          "emoticons template paste textcolor idoc_heading_selection internal_thumbnail_link"
				],
				gecko_spellcheck: true,
				toolbar1: "formatselect | bold italic underline strikethrough removeformat | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | forecolor backcolor",
				toolbar2: "link internal_link | image internal_thumbnail_link | table | charmap emoticons | undo redo",

				menubar: false,
				fixed_toolbar_container: ".idoc-main-editor-placeholder",
				valid_elements : '+*[*]',
				paste_data_images: true,
				forced_root_block: 'div',
				force_br_newlines : false,
				force_p_newlines : false,
				block_formats: 'Normal=p; ' +
							   'Header 1=h1; Header 2=h2; Header 3=h3;' +
							   'Header 4=h4; Header 5=h5; Header 6=h6;' +
							   'Address=address; pre=pre' 
			};

			tinymce.init(EMF.editorConfig);

			// REVIEW: pone da go napravim towa da e directive
			//hurki
			var calculateHeight = function(index, height) {
			    return window.innerHeight - $(this).offset().top;
			};

			var dolayout = function() {
				$('.idoc-content-container').height(calculateHeight);
				$('.idoc-editor').css('min-height', calculateHeight);
			};

			dolayout();

			var thread;
			$(window).off('resize.idoc').on('resize.idoc', function() {
				clearTimeout(thread);
				thread = setTimeout(dolayout, 500);
			});
		},
		
		blur: function() {
			$('.idoc-editor').blur();
		},
		
		focus: function() {
			$('.idoc-editor').focus();
		},

		/** provides the root element of the content */
		getRootElement: function() {
			if (this.editor) {
				return this.editor.dom.getRoot();
			} else {
				throw "the editor has not been initialized";
			}
		},

		/** Sets a new content in the editor */
		setContent : function(content) {
			// This is to keep backward compatibility from the time when widgets were elements
			// In time we could remove this
			var contentWrapper = $('<div class="content-wrapper"></div>').append(content);
			contentWrapper.find('widget')
					.each(function() {
						var $this = $(this);
						while (!$this.parent().is('.content-wrapper')) {
							$this.unwrap();
						}
					})
					.replaceWith(function() {
						var $this = $(this),
							replacement = '<div class="widget" data-widget="noop"';
						
						replacement += ' data-name="' + $this.attr('name') + '"';
						replacement += ' data-config="' + ($this.attr('config') || '') + '"';
						replacement += ' data-value="' + ($this.attr('value') || '') + '"></div>';
						return replacement;
					});
			
			this.editor.setContent(contentWrapper.html());
			this.compile();
			//FIXME tova da go izmestq na po-podhodqshto mqsto ili da se hvyrlq event
			// directive!!
			$('.idoc-editor-table').colResizable({ });
		},
		
		/** Provides the editor content */
		getContent: function(format) {
			format = format || 'html';
			return this.editor.getContent({ format: format });
		},
		
		/**
		 * Remove the ng-scope class to the make the widgets 'uncompiled'
		 */
		removeNgScopeClass: function(content) {
			return content.replace(/ng\-scope/gm, ' ');
		},

		/** Compiles all widgets within the editor */
		compile : function() {
			
			var _this = this;
			$timeout(function() {
				var widgets = angular.element(_this.getRootElement()).find(".widget:not(.ng-scope)");
				$compile(widgets)(_this.scope);
			}, 0);
		},
		
		/** Compiles all widgets within the editor on save and continue*/
		compileOnSave : function() {
			
			// After first save the content of some widgets should be refresh because
			// new information is added - For example (date, creator and title in Object data widget)
			var _this = this;
			$timeout(function() {
				var widgets = angular.element(_this.getRootElement()).find(".widget");
				
				$.each(widgets, function(index, widget) {
					if($(widget).attr("compileonsave")) {
						$compile(widget)(_this.scope);
					}
				});
				
			}, 0);
		},
		
		constructWidgetHTML: function(wrapper, widgetName, config, value, attrs) {
			var elementStringStart = '<' + wrapper,
				elementStringEnd = '><span><br data-mce-bogus="1"></span></' + wrapper + '>',
				classAttr;
			
			attrs = attrs || { };
			attrs['data-name'] = widgetName;
			// widget is the angular trigger for the directive, it just needs to be there,
			// the value of the attribute is of no importance (at least for now, we could put the name there)
			attrs['data-widget'] = 'noop';
			attrs['data-config'] = config || '';
			attrs['data-value'] = value || '';
			
			classAttr = attrs['class'];
			
			if (classAttr) {
				classAttr += classAttr + ' widget';
			} else {
				classAttr = 'widget';
			}
			
			attrs['class'] = classAttr;
			
			_.forOwn(attrs, function(value, key) {
				if (value) {
					elementStringStart += ' ' + key + '="' + value + '"';
				}
			});
			
			return elementStringStart + elementStringEnd;
		},

        /**
         * Compile and insert widget at cursor position
         */
		insertWidget : function(wrapper, widgetName, config, value, attrs) {
			if (!this.editor) {
				return;
			}
			
			var editorSelection = this.editor.selection,
				nodeJq = $(editorSelection.getNode()),
				widgetOrCaretTrap = nodeJq.closest('.widget, .widget-caret-trap'),
				widgetHTML,
				bookmark,
				widgetHTML,
				id,
				next,
				prev;
			
			if (widgetOrCaretTrap.length || nodeJq.is(':header') || nodeJq.parentsUntil('.idoc-editor').is(':header')) {
				return;
			}
			
			id = EMF.util.generateUUID();
			this.editor.execCommand('mceInsertContent', false, '<span id="'+ id +'"><br data-mce-bogus="1"></span>');
			
			bookmark = document.getElementById(id);
			
			next = bookmark.nextSibling,
			prev = bookmark;
			while (!next || (next.nodeType === 3 && !next.textContent)) {
				prev = prev.parentElement;
				if ($(prev).is('.idoc-editor')) {
					break;
				}
				next = prev.nextSibling;
			}
			
			widgetHTML = this.constructWidgetHTML(wrapper, widgetName, config, value, attrs),

			// TODO: construct DOM node directly
			this.editor.dom.insertAfter($('<p><br data-mce-bogus="1"></p>'), bookmark);
			this.editor.dom.insertAfter($(widgetHTML)[0], bookmark);
			this.editor.dom.insertAfter($('<p><br data-mce-bogus="1"></p>'), bookmark);
			
			this.editor.selection.setCursorLocation(next, 0);
		}
	}
}]);

app.factory('IdocPanel', ['ContentEditorService', function(ContentEditorService) {
	this.initialized = false;
	return {
		init : function(previewMode) {
			this.itoc = new $.itoc({ source: '.idoc-editor', target: '.idoc-navigation', eventsObject: idoc.globalEvents, "previewMode": previewMode  });
			this.initialized = true;
		},

		/** Insert widgets palette */
		addPaletteTab : function() {

			var widgets = widgetManager.getWidgets();
			var paletteTab = $('#idoc-widgets');
			paletteTab.empty();
			var list = $("<ul />");
			paletteTab.append(list);
			
			var widgetInsertContentHTML = { };

			// REVIEW:
			// - can we build an html string and then to add it with innerHTML
			// - or we should use documentFragment to build the dom
			// loop throw all available widgets and show them in palette
			for (var i = 0; i < widgets.length; i++) {
				var _widgetName = widgets[i];
				var widget = $('<li class="widget-selected"><a id="' + widgets[i] + '" href="javascript:void(0);" widget-name="' + _widgetName  +'">' + _emfLabels['widget.' + widgets[i] + '.title'] + '</a></li>');
				list.append(widget);
				
				widgetInsertContentHTML[_widgetName] = ContentEditorService.constructWidgetHTML('div', _widgetName);

				(function(widgetName) {
					$("#" + widgetName).click(function(event) {
						// IE10 redirects if not prevented
						event.preventDefault();
						ContentEditorService.insertWidget('div', widgetName);
				    });
					
		            $("#" + widgetName).on('dragstart', function (event) {
		            	var widgetHTML = '<p><br data-mce-bogus="1"></p>' + widgetInsertContentHTML[widgetName] + '<p><br data-mce-bogus="1"></p>';
		                event.originalEvent.dataTransfer.setData('text/html', widgetHTML);
		            });
				})(widgets[i]);
			}

			var accordion = $( "#accordion" ).css('width', $( ".idoc-left-column" ).width() + 40)
				.accordion({
					clearStyle: true,
					autoHeight: false,
					heightStyle: "content",
					collapsible: true,
					event: 'customClick',
					active: 0
				})
				.accordion("refresh")
				.removeClass("hide")
				.data("accordion");

			// disable/enable one of accordion "tabs"
			$('#accordion > .ui-accordion-header').unbind( "click" );
			$('#accordion > .ui-accordion-header').click(function (event, target) {
				var clicked = $( event.currentTarget || target );
				// lazy load objects browser data if not already loaded
				if(clicked.is('#browser') && clicked.next('#objectsExplorerContent').children().length === 0) {
					EMF.CMF.objectsExplorer.init(EMF.objectsBrowserConfig);
				}
				if (! clicked.hasClass("ui-state-disabled")) {
			        $(this).trigger('customClick');
			    }
			});

			$('#accordion').accordion("refresh");
		},

		updatePaletteTab : function(readonly) {
			if (!readonly) {
				$( "#widgets" ).removeClass("ui-state-disabled");
			} else {
				$( "#widgets" ).addClass("ui-state-disabled");
			}

		},

		refreshNavigation : function(previewMode) {
			if (!this.initialized) {
				this.init(previewMode);
			}
			this.itoc.refresh(previewMode);
		},

		refreshLocationPanel : function(documentId) {
			angular.element('.tooltip').remove();
		}
	}
}]);

app.factory('CommentsPanel', function() {
	return {
		init : function() {
			
		},

		load : function() {
			
		},
		refresh : function() {
			
		}
	}
});

app.factory('DraftsService', [ '$http', function($http) {
	var baseURL = EMF.servicePath + '/intelligent-document/drafts';
	
	return {
		create: function(instance) {
			return $http.post(baseURL, instance, { hideAjaxIndicator: true });
		},
		
		find: function(username, instanceId, instanceType) {
			var params = {
				username: username,
				instanceId: instanceId,
				instanceType: instanceType
			};
			return $http.get(baseURL, { params: params });
		},
		
		clear: function(username, instanceId, instanceType) {
			var params = {
					username: username,
					instanceId: instanceId,
					instanceType: instanceType
			};
			return $http.delete(baseURL, { params: params, hideAjaxIndicator: true });
		}
	}
}]);

app.factory('DocumentRestClient', ['$http', function($http) {
	var baseIdocURL = EMF.servicePath + '/intelligent-document';
	var baseCommentURL = EMF.servicePath + '/comment';
	var exportURL = EMF.servicePath + '/export';

	return {
		load: function(id, type, version) {
			var config = { params: { 'type': type, 'version': version } };
			return $http.get(idoc.servicePath + '/' + id, config);
		},

		create: function(object) {
			return $http.put(idoc.servicePath, JSON.stringify(object));
		},

		addRelation: function(object) {
			return $http.post(EMF.servicePath + '/relations/create', JSON.stringify(object));
		},
		
		createSubDocument: function() {
			var config = { params: { 'type': idoc.object.owningInstanceType, 'id': idoc.object.owningInstanceId} };
			return $http.get(baseIdocURL + '/documentTypes/' , config);
		},

		saveContent: function(object) {
			return $http.post(idoc.servicePath, JSON.stringify(object));
		},

		edit: function(id, type) {
			var config = { params: { 'type': type } };
			return $http.post(baseIdocURL + '/' + id + '/edit', null /*no data - error otherwise*/, config);
		},

		deleteDocument: function(id, type) {
			var config = { params: { 'documentId': id, 'type': type } };
			return $http.delete(idoc.servicePath, config);
		},

		revertToVersion: function(id, type, version) {
			return $http.post(baseIdocURL + '/' + id + '/versions/' + version + '/revert', null, { params: { type: type } });
		},

		exportIDoc: function(filename, outputHtml) {
			var config = { params: { 'fileName': filename} };
			return $http.post(exportURL, outputHtml, config);
		},

		loadActions: function(id, type) {
			var config = { params: { 'id': id, 'type': type } };
			return $http.get(idoc.joinServicePath('actions'), config);
		},

		loadComment: function(documentId, content) {
			var result = [];
			$.ajax({type: 'GET', url: baseCommentURL + '/' + documentId, data: content, async: false,
			    	complete: function(data) {
			    	    result = $.parseJSON(data.responseText);
				}
		    });
			return result;
		},

		saveComment: function(objectId, parentId, content) {
			var url = baseCommentURL + '/' + objectId;
			if (parentId) {
				url += '/' + parentId;
			}
			url += '/';
			var result;
			$.ajax({type: 'POST', url: url, data: content, async: false,
			    	complete: function(data) {
			    	    result = $.parseJSON(data.responseText);
				}
		    });
			return result;
		},

		clone: function(id, type) {
			var config = { params: { 'type': type } };
			return $http.get(idoc.servicePath + '/' + id + '/clone', config);
		},

		unlock: function(id, type) {
			var config = { params: { 'type': type } };
			return $http.post(baseIdocURL + '/' + id + '/unlock', null /*no data - error otherwise*/, config);
		},
		
		lock: function(id, type) {
			var config = { params: { 'type': type } };
			return $http.post(baseIdocURL + '/' + id + '/edit', null, config);
		},
		
		download: function(id, type) {
			var config = { params: { 'id':id, 'type': type } };
			return $http.get(baseIdocURL + '/getContentURI', config);
		}
	}
}]);

app.factory('DocumentTemplateSerivce', ['$http', '$q', '$compile', function($http, $q, $compile) {
	var baseDocumentTemplateURL = EMF.servicePath + '/document-template';

	return {
		/** Loads a list of templates for a given definition */
		getTemplates: function(definitionId) {
			// if the definition is is not passed for some reason send dummy id to fetch the default template
			return $http.get(baseDocumentTemplateURL + '/definitions/' + ((definitionId == null || definitionId == "") ? "NONE": definitionId));
		},

		/** Loads the content of a template */
		getContent: function(templateId) {
			return $http.get(baseDocumentTemplateURL + '/content/' + templateId);
		},

		/** Saves a document template on the server */
		save: function(documentType, title, primary, content) {
			var data = {
				content: content,
				title: title,
				primary: primary
			}
			return $http.post(baseDocumentTemplateURL + '/' + documentType, JSON.stringify(data));
		}
	}
}]);