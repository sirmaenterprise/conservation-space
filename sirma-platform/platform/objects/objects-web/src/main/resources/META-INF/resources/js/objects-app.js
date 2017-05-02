/* OBJ namespace */
var OBJ = OBJ || {};

/**
 * OBJ configuration properties.
 */
OBJ.config = {

};

/**
 * Initializes and returns result items decorator function for decorating case and sections as hierarchical
 * structure with checkboxes in front of the case sections to allow selection. This decorator allows if any
 * other different selectable types are provided to be rendered checkboxes in front of them too.
 *
 * @param actionsModule
 * 				The CMF.action module.
 * @param servicePath
 * 				Base application service path.
 * @param sectionsServicePath
 * 				REST Service path for loading of sections.
 * @param dialogContent
 * 				The dialog container.
 * @param allowSubmitWithoutSelection
 * 				If action is allowed to be executed without actual selection to be done.
 * @param selectableTypes
 * 				Map with object types that is permitted to be selectable (eg. To have checkboxes in front of them).
 * @param sectionPurpose
 * 				What kind of case sections to be loaded.
 */
EMF.search.picker.caseAndSectionsResultItemsDecorator = function(config) {
	var resultItemDecorator = function(element, item) {
		var defaultSelectableTypes = config.selectableTypes || {};
		// case final states
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
				EMF.ajaxloader.showLoading(config.dialogContent);
				$.ajax({
					url: config.servicePath + config.sectionsServicePath,
					data: {
						instanceId : item.dbId,
						instanceType : 'caseinstance',
						purpose : config.sectionPurpose
					},
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
									content += 				'<div class="instance-header headers-with-icons ' + resultItem.type + '">';
									content += 					'<span class="icon-cell">';
									// if the case is in final state like 'stopped' and 'completed' we don't want checkboxes for its sections
									if (!item.status || !(item.status in finalStates)) {
										content += 					'<input type="checkbox" class="result-item-checkbox" data-type="' + resultItem.type +'" value="' + resultItem.dbId + '" caseId="' + item.dbId + '" ' + checkedValue + '/>';
									}
									content += 					'</span>';
									content += 					'<span class="data-cell">' + resultItem.default_header + '</span>';
									content += 				'</div>';
									content += 			'</div>';
									content += 		'</td>';
									content += '</tr>';
								$(content).appendTo(table);
							}
						}

						EMF.ajaxloader.hideLoading(config.dialogContent);
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
		// this is kind of stupid to bind the change handler here because it should be
		// removed and added on every decorator invocation
		$(config.dialogContent).off('change.section-selection').on('change.section-selection', function(evt) {
			var selected = $(evt.target);
			// Deselect all checkboxes and select current one if it was checked
			if (selected.is(':checked')) {
				$('input.result-item-checkbox:checkbox:checked').attr("checked", false);
				selected.attr("checked", true);
				selected.trigger('object-picker.selection-changed', {
					currentSelection: {
						dbId: selected.attr('value'),
						type: selected.attr('data-type'),
						header: selected.closest('.instance-header').find('.data-cell').html()
					}
				});
				if (!config.allowSubmitWithoutSelection) {
					config.actionsModule.enablePrimaryButton(true);
				}
			} else {
				if (!config.allowSubmitWithoutSelection) {
					config.actionsModule.enablePrimaryButton(false);
				}
			}
		});

		if(item.type === "caseinstance") {
			togler.appendTo(tdTogler);
		}
		tdTogler.appendTo(sectionRow);

		if(item.type !== 'caseinstance') {
			var checkbox = '<input type="checkbox" data-type="' + item.type
				+ '" class="result-item-checkbox" value="' + item.dbId + '" />';
			$('<td></td>').append(checkbox).appendTo(sectionRow);
		}

		$('<td></td>').append(element).appendTo(sectionRow);
		table.append(sectionRow);
		return table;
	};
	return resultItemDecorator;
};

/**
 * Creates and opens picker dialog to select object section where selected object to be cloned.
 *
 * @param opts
 * {
 *        actionId: '#{action.identifier}', currentInstanceId: '#{currentInstance.getId()}',
 *        currentInstanceType:'#{currentInstance.getClass().getSimpleName().toLowerCase()}',
 *        owningInstanceId: '#{currentInstance.getOwningInstance() != null ?
 *        currentInstance.getOwningInstance().getId() : contextInstance.getId()}',
 *        contextInstanceId: '#{documentContext.CaseInstance != null ? documentContext.CaseInstance.getId() : ''}'
 * }
 */
CMF.action.clone = function(opts) {
	var _this = this;
	var allowSubmitWithoutSelection = (opts.currentInstanceType === 'documentinstance') ? false : true;
	if(EMF.config.debugEnabled) {
		console.log('CMF.action.clone arguments: ', arguments);
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
	config.objectTypeOptionsLoader = function() {
		return [ { "name": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case", "title": window._emfLabels['cmf.comment.filter.objecttype.case'] } ];
	};

	config.doSearchAfterInit = true;

	config.initialSearchArgs.objectType = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case'];
	// FIXME: This assumes that the context instance is a CaseInstance that for objects can be wrong assumption.
	// An object can be attached to case section, but also to a project or to not have a context at all (object library).
	if (opts.contextInstanceId) {
		config.initialSearchArgs.location = ['emf-search-context-current-case'];
	}

	// if clone is not for object show all section that can contain documents, else show only sections
	// that can contain objects.
	var sectionsServicePath = "/caseInstance/sections";
	config.resultItemDecorators = [ EMF.search.picker.caseAndSectionsResultItemsDecorator({
		actionsModule: _this,
		servicePath: EMF.servicePath,
		sectionsServicePath: sectionsServicePath,
		dialogContent: _placeholder,
		allowSubmitWithoutSelection: allowSubmitWithoutSelection,
		selectableTypes: null,
		// sections with documents have no purpose
		sectionPurpose: opts.currentInstanceType === 'documentinstance' ? '' : 'objectsSection'
	}) ];
	_placeholder.basicSearch(config);

	$.ajax({
		type: 'GET',
		url: EMF.servicePath + '/caseInstance/caseAndSection',
		data: {'sectionId' : opts.owningInstanceId},
		async: true,
		complete: function(data) {
			var result = $.parseJSON(data.responseText);
			if(result.caseData) {
				_placeholder.trigger('basic-search:after-search', {
					values		: [result.caseData],
					resultSize	: 1
				});
				var escapedDbId = result.caseData.dbId ? result.caseData.dbId.replace(':', '') : '';
				$('#toggler_case_' + escapedDbId).trigger('click', [opts.owningInstanceId, true]);
			} else {
				_placeholder.trigger('basic-search:after-search', {
					resultSize	: 0
				});
			}
		}
	});

	EMF.dialog.open({
		width: 850,
		height: 600,
		content: _placeholder,
		title : 'Select target section',
		open: function() {
			_this.enablePrimaryButton(true, opts.actionId);
		},
		confirm: function() {
            var selectedSection = $('input.result-item-checkbox:checkbox:checked');
            EMF.blockUI.showAjaxLoader();
            window.location.href = EMF.applicationPath + '/object/clone-object.jsf?id=' + opts.currentInstanceId + '&type=' + opts.currentInstanceType +
                ((selectedSection && selectedSection.length > 0)?('&sectionId=' + selectedSection.attr('value')):'');
		}
	});
};

/**
 * Creates and opens picker dialog to select documents section where selected document to be moved.
 *
 * @param opts
 *        in format like below
 *
 * <pre><code>
 * {
 *  action: '#{action.actionId}',
 *  instanceId:'#{currentInstance.getId()}',
 *  instanceType:'#{currentInstance.getClass().getSimpleName().toLowerCase()}',
 *  contextId:'#{contextInstance != null ? contextInstance.getId(): ''}',
 *  contextType:'#{contextInstance != null ? contextInstance.getClass().getSimpleName().toLowerCase(): ''}',
 *  sourceId:'#{currentInstance.getOwningInstance() != null ? currentInstance.getOwningInstance().getId(): ''}',
 *  sourceType:'#{currentInstance.getOwningInstance() != null ? currentInstance.getOwningInstance().getClass().getSimpleName().toLowerCase(): ''}'
 *  }
 * </code></pre>
 */
CMF.action.move = function(opts) {
	var _this = this;
	var allowSubmitWithoutSelection = false;

	if(EMF.config.debugEnabled) {
		console.error('CMF.action.move arguments: ', arguments);
	}
	// Remove this element to reset the search
	$('.basic-search-wrapper').remove();

	var _placeholder = $('<div></div>');
	_placeholder.off('basic-search:on-search-action').on('basic-search:on-search-action', function() {
		EMF.ajaxloader.showLoading(_placeholder);
	})
	.off('basic-search:after-search-action').on('basic-search:after-search-action', function() {
		EMF.ajaxloader.hideLoading(_placeholder);
	})

	var data = {
			instanceId	: opts.instanceId,
			instanceType: opts.instanceType
	};

	// Fill the objectType in picker with the type of current contextParent of moved instance
	REST.getClient('InstanceRestClient').getOwningInstanceSubType(data)
		.then(function (response) {

			var config = EMF.search.config();

			config.objectTypeOptionsLoader = EMF.servicePath + '/definition/all-types?classFilter=emf:Project&classFilter=emf:Case&classFilter=emf:DomainObject&classFilter=emf:Document';
			config.initialSearchArgs.subType = [response.subtype];
			config.initialSearchArgs.filterByWritePermissions = true;
			config.doSearchAfterInit = true;

			config.onSearchCriteriaChange = [function() {
				// If the user remove objectType search criteria put the default
				if(_placeholder.data('EmfBasicSearch').searchArgs.objectType.length === 0 && !_placeholder.data('EmfBasicSearch').searchArgs.subType) {
					_placeholder.data('EmfBasicSearch').searchArgs.objectType = ["emf:Project", "emf:Case", "emf:DomainObject", "emf:Document"];
				}
			}];

			var sectionsServicePath = "/caseInstance/sections";
			config.resultItemDecorators = [ EMF.search.picker.caseAndSectionsResultItemsDecorator({
				actionsModule: _this,
				servicePath: EMF.servicePath,
				sectionsServicePath: sectionsServicePath,
				dialogContent: _placeholder,
				allowSubmitWithoutSelection: allowSubmitWithoutSelection,
				selectableTypes: null,
				sectionPurpose: ''
			}) ];
			_placeholder.basicSearch(config);

			EMF.dialog.open({
				width: 850,
				height: 600,
				content: _placeholder,
				title : 'Select target section',
				open: function() {
					_this.enablePrimaryButton(false, opts.action);
				},
				confirm: function() {
					EMF.blockUI.showAjaxLoader();
					var selectedSection = $('input.result-item-checkbox:checkbox:checked');
					var targetType = selectedSection.attr('data-type');
					var targetId = selectedSection.val();
		    		var data = {
		    			instanceId	: opts.instanceId,
		    			instanceType: opts.instanceType,
		    			contextId	: opts.contextId,
		    			contextType	: opts.contextType,
		    			sourceId	: response.sourceId,
		    			sourceType	: response.sourceType,
		    			targetId	: targetId,
		    			targetType	: targetType
		    		};
		        	REST.getClient('InstanceRestClient').moveInstance(data)
		        		.then(function () {
		        			EMF.blockUI.hideAjaxBlocker();
							EMF.dialog.open({
								title: window._emfLabels['move.document.dialog.title'],
								notifyOnly	: true,
								message: window._emfLabels['move.document.dialog.message'],
								confirm: function() {
							    	window.location.href = EMF.bookmarks.buildLink(opts.instanceType, opts.instanceId);
								}
							});
				    }, function(data, textStatus, jqXHR) {
				    	EMF.blockUI.hideAjaxBlocker();
				        EMF.dialog.open({
				            title       : ' ' + jqXHR,
				            message     : data.responseText,
				            notifyOnly	: true,
				            customStyle : 'warn-message',
				            confirm	: function() {
				            }
				        });
				    });
				}
			});

	}, function(data, textStatus, jqXHR) {
		EMF.blockUI.hideAjaxBlocker();
	    EMF.dialog.open({
	        title       : ' ' + jqXHR,
	        message     : data.responseText,
	        notifyOnly	: true,
	        customStyle : 'warn-message',
	        confirm	: function() {
	        }
	    });
	});
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