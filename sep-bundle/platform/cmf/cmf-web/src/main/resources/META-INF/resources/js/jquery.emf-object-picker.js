(function($) {
	"use strict";
	
	$.fn.objectPicker = function(options) {
	    
	    var plugin = {};

	    var defaults = {
	        // if this picker should be opened in its own modal dialog
	        popup : false,
	        // functions that are registered on this plugin and will be called on 
	        // every result item before to be appended to the dom
	        resultItemDecorators : {},
	        // a callback function that will be called when user select/desects an item
	        // from the list using the checkbox
	        selectDeselectCallback : $.noop(),
	        // a callback function that will be called when user select/desects an item
	        // from brawser using the checkbox
	        browserSelectDeselectCallback : $.noop(),
	        // a callback function that will be called when user select/desects an item
	        // from upload panel using the checkbox
	        uploadSelectDeselectCallback : $.noop(), 
	        // callback that is called to notify clients that the selection list was changed
	        // the callback takes the event and object with currently selected items
	        selectionChangedCallback : $.noop,
	        selectedItems : {},
	        // panels visibility configuration
	        panelVisibility	: {
	        	search : true,
	        	browse : true,
	        	upload : true
	        },
	        // Which tab to be opened by default in picker.
	        // Can be 'search', 'browse' or 'upload'. If not provided, the default is 'search'.
	        // If redefined outside and this is not resolved, then the first visible tab will be considered as default.
	        defaultTab		: 'search',
			// -------------------------------------------------
			//	configuration for objects browser in browser tab
	        // -------------------------------------------------
        	browserConfig	: {
        		container		: null, // should be initialized on browser init
        		node			: EMF.documentContext.rootInstanceId,
        		type			: EMF.documentContext.rootInstanceType,
        		currentNodeId	: EMF.documentContext.currentInstanceId,
        		currentNodeType	: EMF.documentContext.currentInstanceType,
        		contextPath		: SF.config.contextPath,
        		rootType		: EMF.documentContext.rootInstanceType,
        		allowSelection	: true,
        		singleSelection	: false,
        		filters			: null,
        		changeRoot		: true,
        		autoExpand		: true,
        		allowRootSelection: false
        	},
        	// -------------------------------------------------
        	//	configuration for file upload plugin
        	// -------------------------------------------------
        	uploadConfig	: {
        		showBrowser		: true,
        		browserConfig	: {}
        	},
        	// -------------------------------------------------
        	//	configuration for popup panel that wraps the picker
        	// -------------------------------------------------
        	dialogConfig	: {
        		hideButtons	: false,
    	        // callback function called when the ok button of the dialog is selected
    	        okBtnClicked : $.noop(),
    	        // css class to be applied on popup panel
    	        dialogClass : "object-picker-dialog notifyme no-close",
    	        modal : true,
    	        width : 1000,
    	        height : 620,
    	        resizable : true
        	},
	        
	        labels : {
	            popupTitle 	: window._emfLabels['object.picker.header'],
	            okBtn 		: window._emfLabels['ok'],
	            cancelBtn	: window._emfLabels['cancel'],
	            tabSearch	: window._emfLabels['object.picker.tabs.search'],
	            tabBrowse	: window._emfLabels['object.picker.tabs.browse'],
	            tabUpload	: window._emfLabels["upload.upload"]
	        }
	    };
		
		plugin.settings = $.extend(true, {}, defaults, options);
		
		plugin.settings.resultItemDecorators['objectPickerDecorator'] = function(element, item) {
			var table = $('<table></table>');
			var row = $('<tr></tr>');

			var selectorDisabled = item.disabled ? 'disabled="disabled"' : '';
			var selectorChecked = plugin.settings.selectedItems[item.dbId] ? 'checked="checked"' : '';
			
			var tdSelector = $('<td></td>');
			var selector;
			if(plugin.settings.singleSelection) {
				selector = $('<input type="radio" name="picker-radio-group" value="'+ item.dbId +'" ' + selectorDisabled + ' ' + selectorChecked + '/>');
			} else {
				selector = $('<input type="checkbox" value="'+ item.dbId +'" ' + selectorDisabled + ' ' + selectorChecked + '/>');
			}
			
			selector.data('data', item);
			
			selector.change(function(event) {
				var input = $(event.target);
				var data = $(this).data('data');
				data.op = input.prop('checked') ? 'add' : 'remove';
				data.value = input.val();
				data.srcElement = input;
				// if selectionChangedCallback is provided by the client, then collect selected items
				// and invoke callback to notify for the changes in selection
				if(plugin.settings.selectionChangedCallback) {
			        var operation = data.op;
			        if(operation === 'remove' && data.dbId in plugin.settings.selectedItems) {
			            delete plugin.settings.selectedItems[data.dbId];
			        } else if(operation === 'add') {
			        	plugin.settings.selectedItems[data.dbId] = data;
			        }
			        var selectionData = {
			            selectedItems : plugin.settings.selectedItems,  
			            srcElement : data.srcElement,
			            currentSelection : plugin.settings.selectedItems[data.dbId]
			        };
			        plugin.settings.selectionChangedCallback.call(plugin, selectionData);
				}
				//
				if(plugin.settings.selectDeselectCallback) {
					plugin.settings.selectDeselectCallback.call(plugin, selectionData);
				}
			});
			
			// Show tooltip message if the user have no rights
			if(item.disabled) {
				var toolTipMsg = window._emfLabels["warn.not.authorized"];
				selector.attr('title', toolTipMsg);
			}
			
			selector.appendTo(tdSelector);
			tdSelector.appendTo(row);
			
			var tdElement = $('<td></td>');
			element.appendTo(tdElement);
			tdElement.appendTo(row);
			
			table.append(row);
			return table;
		};
		
		plugin.panel = {
			'search': function(options, panels) {
	    		var panel = $('<div id="pickerSearchPanel" class="content-panel full-height search"></div>');

				var basicSearch = panel.basicSearch(options)[0];
				panel.data('basicSearch', basicSearch);
				panel.data('checkedIDs', []);
				
				var checkAllWrapper = $('<div class="select-all-wrapper" />');
				var allCheckbox = $('<input type="checkbox" id="selectAll" name="selectAll" /><label for="selectAll">Select/Deselect all</label>');
				allCheckbox.change(function() {
					var value = $(this).prop('checked');
					$('input[type=checkbox]', $('.basic-search-results-wrapper')).each(function() {
						$(this).prop('checked', value);
						$(this).trigger('change');
					});
				});
				allCheckbox.appendTo(checkAllWrapper);
				checkAllWrapper.appendTo($('.basic-search-criteria', panel));
				
				panel.on('basic-search:after-search', function(event, data) {
					if (data.values && data.values.length > 0) {
						checkAllWrapper.show();
					} else {
						checkAllWrapper.hide();
					}
				})
				.on('selection-changed', function(event) {
					var data = panel.data('checkedIDs');
					data = [];
					$('input:checked', panel).each(function() { 
						data.push($(this).val());
					});
					panel.data('checkedIDs', data);
				});
				panels.append(panel);
				return panel;
			},
			
			'browse': function(options, panels) {
	    		var panel = $('<div id="pickerBrowsePanel" class="content-panel full-height browse"></div>');
	    		panels.append(panel);
	    		var browserConfiguration = $.extend(true, {}, options.browserConfig, {
	    			container: 'pickerBrowsePanel'
	    		});
	    		EMF.CMF.objectsExplorer.init(browserConfiguration);
	    		 
	    		panel.on('object.tree.selection', function(evt) {
	    			if(evt.selectedNodes.length > 0 && plugin.settings.browserSelectDeselectCallback) {
    					evt.selectedNodes[0].dbId = evt.selectedNodes[0].nodeId;
    					evt.selectedNodes[0].op = 'add';
    			        var selectionData = {
    				            currentSelection : evt.selectedNodes[0]
    				        };
    					plugin.settings.browserSelectDeselectCallback.call(plugin, selectionData);
	    			}
	    		}); 
	    		return panel;
			},
			
			'upload': function(options, panels) {
    	    	var panel = $('<div id="pickerUploadPanel" class="content-panel full-height upload"></div>');

				options.uploadConfig.singleSelection = plugin.settings.singleSelection;
    			var upload = panel.upload(options.uploadConfig);
    			panel.data('upload', upload);
    			
    			panel.on('uploaded.file.selection', function(e, data) {
    				if(plugin.settings.uploadSelectDeselectCallback) {
    					plugin.settings.uploadSelectDeselectCallback.call(plugin, data);
    				}
    			});
    			panels.append(panel);
    			return panel;
			}
		};
		
		plugin.tabVisibilityClass = function(tabname) {
			return plugin.settings.panelVisibility[tabname] ? '' : ' hidden';
		};
		
		plugin.switchTab = function(tabs, selectedTab) {
	    	tabs.removeClass('selected');
	    	selectedTab.addClass('selected');
		};
		
		return this.each(function(index) { 
			var $this = $(this);
			var dialogSettings = plugin.settings.dialogConfig;
		    var lbls = plugin.settings.labels;
		    
	    	// called to close the dialog - also cleanses the object picker and its data
	    	var _onDialogClose = function() {
	    		$this.dialog("destroy");
            	$this.empty();
            	$this.data('plugin_objectPicker', false);
            	CMF.utilityFunctions.hideAllIFrames(false);
	    	};
	    	var oncloseHandlers = [];
	    	if(plugin.settings.dialogConfig.oncloseHandler) {
	    		oncloseHandlers.push(plugin.settings.dialogConfig.oncloseHandler);
	    	}
	    	oncloseHandlers.push(_onDialogClose);
	    	var _executeOnClose = function() {
	    		var len = oncloseHandlers.length;
	    		for (var i = 0; i < len; i++) {
					oncloseHandlers[i]();
				}
	    	}
		    
		    if (plugin.settings.popup) {
		    	//$(this).prepend('<input type="hidden" autofocus="autofocus" />');
		    	
		    	var dialogConfiguration = {
    	    	    title        : lbls.popupTitle,
    	    		modal        : dialogSettings.modal,
    	    		width        : dialogSettings.width,
    	    		height       : dialogSettings.height,
    	    		resizable    : dialogSettings.resizable,
    	    		dialogClass  : dialogSettings.dialogClass,
    	    		position	 : ['center', 'center'],
    	          	buttons      : [],
    	            open: function(event, ui) {
    	            	// remove default jquery ui style classes
    	                $(event.target).parent().removeClass('ui-corner-all').find('.ui-corner-all').removeClass('ui-corner-all');
    	                CMF.utilityFunctions.hideAllIFrames(true);
    	            },
    	            close: _executeOnClose
    	    	};
		    	
		    	// add ok and cancel buttons
		    	if(!dialogSettings.hideButtons) {
		    		dialogConfiguration.buttons.push({
    	            	text  : lbls.okBtn,
    	            	priority : 'primary',
    	            	click : function() {
    	            		// call the ok button call back handler
    	            		dialogSettings.okBtnClicked.call();
    	            		_executeOnClose();
    	            	}
    	        	});
		    		dialogConfiguration.buttons.push({
    	        		text  : lbls.cancelBtn,
    	        		'class'	 : 'always-enabled',
    	        		priority : 'secondary',
    	                click : _executeOnClose
    	            });
		    	}
		    	
		    	$this.dialog(dialogConfiguration);
    	    } 
			
			// make sure only one instance of the plugin is initialized for element
    	    var initialized = $.data(this, 'plugin_objectPicker' );
    	    if (!initialized) {
    	    	$.data(this, 'plugin_objectPicker', true);
    	    } else {
    	    	//clear the search for subsequent calls
    	    	$this.data('basicSearch').clear();
    	    	return;
    	    }
    	    
    	    var html = '';
	    	html += '<div class="object-picker full-height">';
    		html += 	'<div class="menu-panel full-height">';
   			html += 		'<ul class="menu-list">';
			html += 			'<li id="pickerSearchTab" name="search" class="picker-tab search' + plugin.tabVisibilityClass('search') + '">' + plugin.settings.labels.tabSearch + '</li>';
			html += 			'<li id="pickerBrowseTab" name="browse" class="picker-tab browse' + plugin.tabVisibilityClass('browse') + '">' + plugin.settings.labels.tabBrowse + '</li>';
			html += 			'<li id="pickerUploadTab" name="upload" class="picker-tab upload' + plugin.tabVisibilityClass('upload') + '">' + plugin.settings.labels.tabUpload + '</li>';
  			html += 		'</ul>';
   			html += 	'</div>';
    		html += 	'<div class="panels"></div>';
	    	html += '</div>';
    	    
    	    var objectPickerContent = $(html);
    	    var tabs = $('.menu-list li', objectPickerContent);
    	    
    	    objectPickerContent.on('click', function(evt) {
    	    	var selectedTab = $(evt.target);
    	    	if (selectedTab.is('.picker-tab')) {
    	    		var selectedTabName = selectedTab.attr('name');
    	    		var panels = $('.panels', objectPickerContent);
    	    		var selectedPanel = panels.find('.' + selectedTabName);
    	    		if (selectedPanel && selectedPanel.length) {
    	    			panels.find('.content-panel').addClass('hidden');
    	    			selectedPanel.removeClass('hidden');
    	    			plugin.switchTab(tabs, selectedTab);
    	    		} else {
    	    			if(plugin.settings.panelVisibility[selectedTabName]) {
    	    				panels.find('.content-panel').addClass('hidden');
    	    				// new panels are added to panels container immediately after creation and before plugings placed 
    	    				// in them to be initialized
    	    				var newPanel = plugin.panel[selectedTabName](plugin.settings, panels);
    	    				plugin.switchTab(tabs, selectedTab);
    	    			}
    	    		}
    	    	}
    	    });
    	    
    	    // activate default the tab if provided or get the first visible tab
    	    var defaultTab = objectPickerContent.find('li[name=' + plugin.settings.defaultTab + ']');
    	    if(defaultTab.length === 0) {
    	    	defaultTab = objectPickerContent.find('.menu-list li:not(.hidden)');
    	    }
    	    defaultTab.first().click();
    	    
    	    $this.append(objectPickerContent);
		});
	}
}(jQuery));