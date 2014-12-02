/* OBJ namespace */
var OBJ = OBJ || {};

/**
 * OBJ configuration properties.
 */
OBJ.config = {

};

/**
 * Creates and opens picker dialog to select objects section for clone
 * object functionality.
 *
 * @param action
 * 			The action id that triggers this function.
 * @param objectId
 * 			The object id
 * @param sectionId
 * 			The section id to which current object is attached (owning instance id)
 * @param type
 * 			owning instance type
 * @param contextInstanceId
 * 			The context instance id (if object is attached to a case section, then the case instance is the context)
 */
// TODO: this needs serious refactoring!!!
OBJ.objectsSectionPicker = function(action, objectId, sectionId, type, contextInstanceId) {
	if(EMF.config.debugEnabled) {
		console.log('OBJ.objectsSectionPicker arguments: ', arguments);
	}
	// Remove this element to reset the search
	$('.basic-search-wrapper').remove();

	var _placeholder = $('<div></div>');
	_placeholder.off('basic-search:on-search-action').on('basic-search:on-search-action', function() {
		EMF.ajaxloader.showLoading(_placeholder);
	})
	.off('basic-search:after-search-action').on('basic-search:after-search-action', function() {
		EMF.ajaxloader.hideLoading(_placeholder);
	});

	var config = EMF.search.config();
	config.pickerType = 'documentsSection';
	config.objectTypeOptionsLoader = function() {
		var data = [ { "name": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case", "title": "Case" } ];
		return data;
	};
	config.initialSearchArgs.objectType = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case'];
	// FIXME: This assumes that the context instance is a CaseInstance that for objects can be wrong assumption.
	// An object can be attached to case section, but also to a project or to not have a context at all (object library).
	if (contextInstanceId) {
		// if we should set the actual context instance id, use this line
//		 config.initialSearchArgs.location = [contextInstanceId];
		config.initialSearchArgs.location = ['emf-search-context-current-case'];
	}

	var sectionsServicePath;
	// if clone is not for object show all section that can contain documents, else show only sections
	// that can contain objects.
	// TODO: use CaseInstanceRestService '/sections' getObjectSections service only, no need for different services
	if (type === "documentinstance" ) {
		sectionsServicePath = "/intelligent-document/documentsSections";
	} else {
		sectionsServicePath = "/object-rest/objectsSections";
	}

	config.resultItemDecorators = {};
	config.resultItemDecorators = [ function(element, item) {
		var finalStates = {
			'STOPPED':'',
			'COMPLETED':''
		};
		var table = $('<table class="max-width-style"></table>');
		var sectionRow = $('<tr class=""></tr>');
		var tdTogler = $('<td></td>');
		var escapedDbId = item.dbId ? item.dbId.replace(':', '') : '';
		var togler = $('<div class="standalone-togler" id="toggler_case_' + escapedDbId + '"></div>');
		togler.data('data', item);
		togler.click(function(event, _sectionId, _checked) {
			if (!togler.loaded) {
				EMF.ajaxloader.showLoading(_placeholder);
				$.ajax({
					url: EMF.servicePath + sectionsServicePath,
					data: {'caseId' : item.dbId},
					complete: function(data) {
						var result = $.parseJSON(data.responseText);
						if (result.values) {
							var len = result.values.length;
							for (var i = 0; i < len; i++) {
								var resultItem = result.values[i];

								// Used to automatically select current section
								var checkedValue = _checked ? (resultItem.dbId === _sectionId ? 'checked' : '') : '';
								var imagePath = EMF.util.objectIconPath(resultItem.type, 24);
								var content =  '<tr class="case_' + escapedDbId + '">';
									content += 		'<td></td>';
									content += 		'<td>';
									content += 			'<div class="tree-header default_header" >';
									content += 				'<div class="instance-header ' + resultItem.type + '">';
									content += 					'<span class="icon-cell">';
									// if the case is in final state like 'stopped' and 'completed' we don't want checkboxes for its sections
									if (!item.status || !(item.status in finalStates)) {
										content += 					'<input type="checkbox" class="clone-section-instance" value="' + resultItem.dbId + '" caseId="' + item.dbId + '" ' + checkedValue + '/>';
									}
									content += 					'</span>';
									content += 					'<span class="icon-cell">';
									content += 						'<img class="header-icon" src="' + imagePath + '" \>';
									content += 					'</span>';
									content += 					'<span class="data-cell">' + resultItem.default_header + '</span>';
									content += 				'</div>';
									content += 			'</div>';
									content += 		'</td>'
									content += '</tr>';
								$(content).appendTo(table);
							}
						}

						$('input.clone-section-instance:checkbox').change(function() {
							var selected = $(this);
							// Deselect all checkboxes and select current one if it was checked
							if (selected.is(':checked')) {
								$('input.clone-section-instance:checkbox:checked').attr("checked", false);
								selected.attr("checked", true);
								enablePrimaryButton(true, action);
							} else {
								enablePrimaryButton(false, action);
							}
						});
						EMF.ajaxloader.hideLoading(_placeholder);
					}
				});
				togler.loaded = true;
				togler.addClass('expanded');
			} else {
				// Already loaded. Just trigger hide/show
				var rowClass = '.case_' + escapedDbId;

				if (togler.hasClass('expanded')) {
					togler.removeClass('expanded');
					$(rowClass).hide();
				} else {
					togler.addClass('expanded');
					$(rowClass).show();
				}
			}
		});

		// Fix for CMF-6458
		if(item.type === "caseinstance") {
			togler.appendTo(tdTogler);
		}
		tdTogler.appendTo(sectionRow);

		var tdElement = $('<td></td>');
		element.appendTo(tdElement);
		tdElement.appendTo(sectionRow);

		table.append(sectionRow);

		return table;
	} ];
	_placeholder.basicSearch(config);
	
	$.ajax({
		type: 'GET',
		url: EMF.servicePath + '/object-rest/caseAndSection',
		data: {'sectionId' : sectionId},
		async: true,
		complete: function(data) {
			var result = $.parseJSON(data.responseText);
			if(result.caseData) {
				_placeholder.trigger('basic-search:after-search', {
					values		: [result.caseData],
					resultSize	: 1
				});
				var escapedDbId = result.caseData.dbId ? result.caseData.dbId.replace(':', '') : '';
				$('#toggler_case_' + escapedDbId).trigger('click', [sectionId, true]);
			} else {
				_placeholder.trigger('basic-search:after-search', {
					resultSize	: 0
				});
			}
		}
	});

	if(action === "moveInOtherCase") {
		_placeholder.dialog({
			width: 850,
			height: 600,
			resizable: false,
			title : 'Select target section',
			dialogClass: 'notifyme no-close',
			modal: true,
	        open: function( event, ui ) {
	            $(event.target).parent().removeClass('ui-corner-all').find('.ui-corner-all').removeClass('ui-corner-all');
	            enablePrimaryButton(false, action);
	        },
			close: function(event, ui)
	        {
	            $(this).dialog('destroy').remove();
	        },
	        buttons: [
	        	{
	        	    text     : "Move In Other Case",
	        	    priority : 'primary',
	        	    click    : function() {
	                    var selectedSection = $('input.clone-section-instance:checkbox:checked');
	                    window.location.href = '/emf/object/move-case.jsf?id=' + objectId + '&type=' + type +
	                        ((selectedSection && selectedSection.length > 0)?('&sectionId=' + selectedSection.attr('value')):'');
	                }
	        	},
	        	{
	        	    text     : window._emfLabels["cancel"],
	        	    priority : 'secondary',
	        	    click    : function() {
	                    $(this).dialog('destroy').remove();
	                }
	        	}
	        ]
		});	
	} else {
		_placeholder.dialog({
			width: 850,
			height: 600,
			resizable: false,
			title : 'Select target section',
			dialogClass: 'notifyme no-close',
			modal: true,
	        open: function( event, ui ) {
	            $(event.target).parent().removeClass('ui-corner-all').find('.ui-corner-all').removeClass('ui-corner-all');
	        },
			close: function(event, ui)
	        {
	            $(this).dialog('destroy').remove();
	        },
	        buttons: [
	        	{
	        	    text     : "Clone",
	        	    priority : 'primary',
	        	    click    : function() {
	                    var selectedSection = $('input.clone-section-instance:checkbox:checked');
	                    window.location.href = '/emf/object/clone-object.jsf?id=' + objectId + '&type=' + type +
	                        ((selectedSection && selectedSection.length > 0)?('&sectionId=' + selectedSection.attr('value')):'');
	                }
	        	},
	        	{
	        	    text     : window._emfLabels["cancel"],
	        	    priority : 'secondary',
	        	    click    : function() {
	                    $(this).dialog('destroy').remove();
	                }
	        	}
	        ]
		});	
	}
	
	function enablePrimaryButton(enable, actionId) {
		// when cloning we should allow it be in Object library, so we don't disable the OK 
		// button nevertheless there is no selected object. 
		if (actionId === "clone") {
			return;
		}
		var primaryButton = $(".ui-dialog-buttonpane button[priority=primary]");
		if (enable) {
			primaryButton.attr("disabled", false).removeClass("ui-state-disabled");
		} else {
			primaryButton.attr("disabled", true).addClass("ui-state-disabled");
		}
	}
};

/**
 * Extend picker with pick object functionality. Basically this is initializer
 * for object picker plugin.
 */
EMF.search.picker.object = function(placeholder) {
	var config = EMF.search.config();

	config.pickerType = 'object';

	var selectedItems = EMF.search.selectedItems = {};

	var _placeholder = $(placeholder);
	_placeholder.off('basic-search:on-search-action').on('basic-search:on-search-action', function() {
		EMF.ajaxloader.showLoading(_placeholder);
	})
	.off('basic-search:after-search-action').on('basic-search:after-search-action', function() {
		EMF.ajaxloader.hideLoading(_placeholder);
	});

	config.objectTypeOptionsLoader = function() {
		var data = {
				values : [ {"name":"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DomainObject","title":"Cultural Object"} ]
			}
		return data;
	};

	config.defaultCheckedObjectTypes = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DomainObject'];

	// decorator to add target attributes to links in object picker
	config.resultItemDecorators = {
		'decorateLinks' : function(element, item) {
			element.find('a').attr('target', '_blank');
			return element;
		}
	}

	_placeholder.objectPicker(config);
};

/**
 * For object picker, we remove the multiple attribute of the objectType selector.
 *
 * @param data Holds the basic search template and the plugin settings map.
 */
EMF.search.picker.updateSearchTemplate = function(data) {
	if(data.settings.pickerType === 'object') {
		$(data.template).find('.object-type').removeAttr('multiple');
	}
};

/**
 * Container for actions
 */
OBJ.actions = {
		
	/**
	 * This is a handler for createObject operation triggered from case and section instances.
	 * If provided instanceType is a case, then we open the section picker to allow user
	 * to pick an object section where the object to be created. If the instanceType is a section
	 * then we skip section picker and just pass the selected section instance id to the handler.
	 *  
	 * @param instanceId Current instance id.
	 * @param instanceType Current instance type.
	 * @param contextId Context instance id.
	 * @param contextType Context instance type.
	 * @param sourceId
	 * @param sourceType
	 * @param event
	 */
	createObject: function(opts, event) {
		var instanceId 		= opts.instanceId,
			instanceType 	= opts.instanceType,
			contextId 		= opts.contextId,
			contextType 	= opts.contextType,
			sourceId 		= opts.sourceId,
			sourceType 		= opts.sourceType;

		// when triggered createObject operation for every case instance we should open the section picker 
		if(instanceType && instanceType === 'caseinstance') {
			var sectionType = 'objectsSection';
			EMF.search.picker.caseSections(sectionType, instanceId, instanceType, contextId, contextType, EMF.dialog, sectionSelectedHandler);
		} else if(instanceType && instanceType === 'sectioninstance') {
			// we enter here when createObject is triggered from a case section action
			// instanceId here should be the section instance id
			sectionSelectedHandler(instanceId);
		}
		
		function sectionSelectedHandler(selectedItem) {
			var selectedSectionId = selectedItem.value || selectedItem;
			var hiddenField = $('[id$=createObjectHiddenField]')[0];
			var hiddenActionLink = $('[id$=createObjectHiddenButton]')[0];
			opts.selectedSectionId = selectedSectionId;
			hiddenField.value = JSON.stringify(opts);
			hiddenActionLink.click();
		}
	},

	/**
	 * Upload object operation, generate modal dialog with upload UI elements.
	 *
	 * @param type object type
	 * @param id object identifier
	 */
	upload : function(type, id) {
		// string data used for upload modal dialog
		var dialogData = {
			container : '<div id="upload-to-idoc-dialog"><div class="upload-content-panel"></div></div>',
			content   : '.upload-content-panel',
			title	  : 'objects.upload.dialog.title',
			upload    : 'upload'
		};
		var modalPanel = $(dialogData.container);
		EMF.dialog.popup({
			content				: modalPanel,
			title				: _emfLabels[dialogData.title],
			modal				: true,
			draggable			: true,
			resizable			: true,
			width				: 950,
			height				: 650,
			buttons 			: [{
        		text : window._emfLabels['cmf.btn.close'],
        		priority: 'secondary',
                click : function() {
                	$(this).dialog('destroy').remove();
                }
            }]
		});
		var element = $(dialogData.content);
		// applying upload settings to modal dialog content area
		var upload = element.upload({
    		allowSelection	 	: false,
    		showBrowser		 	: false,
    		isNewObject		 	: false,
    		targetInstanceId 	: id,
			targetInstanceType	: type
    	});
		// store upload settings in element data attribute
		element.data(dialogData.upload, element);
		return false;
	}
};


/**
 * Initialize function for objects module.
 */
OBJ.init = function(opts) {
	OBJ.config = $.extend(true, {}, OBJ.config, opts);
};

// Register PM module
EMF.modules.register('OBJ', OBJ, OBJ.init);