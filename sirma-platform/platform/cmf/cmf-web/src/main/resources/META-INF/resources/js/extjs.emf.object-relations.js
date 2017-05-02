$.fn.extjsObjectRelations = function(opts) {

	var plugin = {};

	var defaults = {
		// ----------------------------------------
		//      required arguments
		// ----------------------------------------
		// application context path like /emf
		contextPath : null,
		// current instance id
		instanceId : null,
		// current instance type
		instanceType : null,
		// ----------------------------------------
		//      optional arguments
		// ----------------------------------------
		definitionId : null,
		width : 'auto',
		height : null,
		preview : true,
		debug : false,
		// which types of relationships to show 'objects' or 'literals'
		mode : 'objects',
		showLess : true,
		chosenProperties : [],
		btnIds : {
			editor : "",
			editorId : "",
			btnSave : "",
			btnCancel : "",
			pickerBtn : "",
			cancelBtn : ""
		},
		service : {
			loadData : '/service/relations/loadData',
			create : '/service/relations/create',
			update : '/service/relations/update',
			deactivate : '/service/relations/deactivate',
			loadRelTypes : '/service/definition/relations-by-type',
			loadRelTypesIntersect : '/service/definition/relations-by-type-intersect',
			codelist : '/service/codelist'
		},
		labels : {
			add : _emfLabels['cmf.relations.addrelation'],
			remove : _emfLabels['cmf.relations.removerelation'],
			cancel : _emfLabels['cancel'],
			save : _emfLabels['save'],
			propertySelector : _emfLabels['cmf.relations.property'],
			filterByType : _emfLabels['cmf.relations.filter.type'],
			filterByObject : _emfLabels['cmf.relations.filter.object'],
			objectPickerTitle : _emfLabels['cmf.relations.objectpicker'],
			objectPickerTrigger : _emfLabels['cmf.relations.objectpicker.trigger'],
			relationsType : _emfLabels['cmf.relations.type'],
			relatedObject : _emfLabels['cmf.relations.relatedobject'],
			pickimage : _emfLabels['cmf.relations.pickimage'],
			updatemsg : _emfLabels['cmf.relations.updatemsg'],
			showMore : _emfLabels['show_more'],
			showLess : _emfLabels['show_less'],
			confirmDeleteRelation : _emfLabels['cmf.relations.confirm_delete']
		}
	};

	/**
	 * Initializes the object picker.
	 */
	plugin.initObjectPicker = function() {
		var selectedItem;
		var selectedItems = {};
		var placeholder = $('<div></div>');
		var config = EMF.search.config();
		config.pickerType = 'object';
		config.popup = true;
		config.labels = {};
		config.labels.popupTitle = plugin.options.labels.objectPickerTitle;
		config.initialSearchArgs = config.initialSearchArgs || {};
		config.browserConfig = {
			allowSelection : true,
			singleSelection : false,
			allowRootSelection : true,
			filters : "instanceType!='sectioninstance'"
		};
		config.dialogConfig = {};
		config.dialogConfig.okBtnClicked = function() {
			EMF.blockUI.showAjaxLoader();
			var data = {};
			if (selectedItems) {
				var items = {};
				for (key in selectedItems) {
					var currentItem = selectedItems[key];
					var item = {};
					item.targetId = plugin.options.instanceId;
					item.targetType = plugin.options.instanceType;
					item.destId = currentItem.dbId;
					item.destType = currentItem.type;
					items[key] = item;
				}

				data = JSON.stringify({
					relType : plugin.selectedProperty,
					selectedItems : items
				});

				if (plugin.options.instanceId) {
					$.ajax({
						contentType : "application/json",
						data : data,
						type : 'POST',
						url : opts.contextPath + plugin.options.service.create
					}).done(function(data, textStatus, jqXHR) {
						if (data && data.messages && data.messages.length) {
							EMF.dialog.open({
								// FIXME: this join with new line doesn't work
								message : data.messages.join('\n'),
								notifyOnly : true,
								width : 'auto'
							});
						}
						plugin.relationsStore.reload();
						plugin.hidePropertyEditor();
						EMF.blockUI.hideAjaxBlocker();
					}).fail(function(data, textStatus, jqXHR) {
						EMF.dialog.open({
							title : ' ' + jqXHR,
							message : data.responseText,
							notifyOnly : true,
							customStyle : 'error-message',
							width : 'auto'
						});
						plugin.hidePropertyEditor();
						EMF.blockUI.hideAjaxBlocker();
					});
				} else {

					for (key in selectedItems) {
						var iconURL = EMF.util.objectIconPath(selectedItems[key].type, 16);
						plugin.relationsStore.insert(0, {
							name : plugin.selectedPropertyTitle,
							value : '<img class="header-icon" src="' + iconURL + '"> ' + selectedItems[key].compact_header
						});
					}
					data = {
						relType : plugin.selectedProperty,
						selectedItems : items
					};

					$(document).trigger('emf-object-relation-added', data);
					plugin.hidePropertyEditor();
					EMF.blockUI.hideAjaxBlocker();
				}
			}
		};

		// decorator to add target attributes to links in object picker
		config.resultItemDecorators = {
			'decorateLinks' : function(element, item) {
				element.find('a').attr('target', '_blank');
				return element;
			}
		};

		config.onBeforeSearch.push(function() {
			EMF.ajaxloader.showLoading(placeholder);
		});
		config.onAfterSearch.push(function() {
			EMF.ajaxloader.hideLoading(placeholder);
		});

		placeholder.on('object-picker.selection-changed', function(event, data) {
			selectedItems = data.selectedItems;
			var isEmpty = true;
			for (key in data.selectedItems) {
				isEmpty = false;
				break;
			}
			if (isEmpty) {
				EMF.search.picker.toggleButtons(true);
			} else {
				EMF.search.picker.toggleButtons(false);
			}
		}).on('object.tree.selection', function(data) {
			selectedItems = data.selectedNodes;
			if (selectedItems.length === 0) {
				EMF.search.picker.toggleButtons(true);
			} else {
				EMF.search.picker.toggleButtons(false);
			}
		}).on('uploaded.file.selection', function(e, data) {
			selectedItems = [data.currentSelection];
			if (selectedItems.length === 0) {
				EMF.search.picker.toggleButtons(true);
			} else {
				EMF.search.picker.toggleButtons(false);
			}
		});

		placeholder.objectPicker(config);

		EMF.search.picker.toggleButtons(true);
	};

	/**
	 * Provides the toolbar buttons.
	 */
	plugin.getToolbarButtons = function(scope) {

		//allow editing (toolbar buttons) only in edit mode
		if (!plugin.options.preview) {

			if (plugin.options.header) {
				var iconURL = EMF.util.objectIconPath(plugin.options.instanceType, 16);
				var objectHeader = '<div style="margin:5px 0px 5px 5px;"><img class="header-icon" src="' + iconURL + '"> ' + plugin.options.header + '</div>'

				var header = Ext.create('Ext.panel.Panel', {
					html : objectHeader
				});

				var panel = Ext.create('Ext.panel.Panel', {
					layout : 'column'
				});

				panel.add({
					xtype : 'button',
					text : this.options.labels.add,
					scope : scope,
					margin : '13 0 10 5',
					cls : 'btn btn-sm btn-default create-relation-btn',
					overCls : 'extjs-component-btn-over',
					focusCls : 'extjs-component-btn-focus',
					handler : plugin.showCreateRecordEditor
				});

				if (plugin.options.mode === 'objects') {
					panel.add(plugin
							.getPropertySelector(plugin.filterRelationsHandler, plugin.options.labels.filterByType, 'relationTypesFilter-' + EMF.util
									.generateUUID()));
					panel.add({
						xtype : 'textfield',
						inputId : 'objectTypeValueFilter',
						flex : 1,
						maxWidth : 400,
						fieldLabel : plugin.options.labels.filterByObject,
						labelAlign : 'right',
						//		    	           labelWidth	  : 0,
						margin : '13 0 10 0',
						allowBlank : true,
						enableKeyEvents : true,
						listeners : {
							keyup : function(field, evt, eOpts) {
								var term = Ext.String.trim(field.getValue());
								var filter = {
									// we should filter by plainText field because the value field can contain
									// html tags that may be invisible for the user
									property : 'plainText',
									value : term,
									anyMatch : true,
									caseSensitive : false
								};
								plugin.filterRelations([filter]);
							},
							afterrender : function(field) {
								if ($.browser.msie || !!navigator.userAgent.match(/Trident.*rv\:11\./)) {
									setTimeout(function() {
										field.focus();
									}, 100);
								}
							}
						}
					});
				}

				var tabPanel = Ext.create('Ext.panel.Panel', {
					items : [header, panel]
				});

				return tabPanel;
			} else {

				  var btnClass = plugin.options.mode === 'literals' ? 'add-fact-btn' : 'create-relation-btn';
				  var btns = [{
					  xtype : 'button',
					  text : this.options.labels.add,
				      scope : scope,
				      cls : 'btn btn-sm btn-default  ' + btnClass,
				      overCls : 'extjs-component-btn-over',
				      focusCls : 'extjs-component-btn-focus',
				      handler : plugin.showCreateRecordEditor
				    }];

				if (plugin.options.mode === 'objects') {
					btns.push(plugin
							.getPropertySelector(plugin.filterRelationsHandler, plugin.options.labels.filterByType, 'relationTypesFilter-' + EMF.util
									.generateUUID()));
					btns.push({
						xtype : 'textfield',
						inputId : 'objectTypeValueFilter',
						flex : 1,
						maxWidth : 400,
						fieldLabel : plugin.options.labels.filterByObject,
						labelAlign : 'right',
						//	    	            labelWidth	   : 0,
						allowBlank : true,
						enableKeyEvents : true,
						listeners : {
							keyup : function(field, evt, eOpts) {
								var term = Ext.String.trim(field.getValue());
								var filter = {
									// we should filter by plainText field because the value field can contain
									// html tags that may be invisible for the user
									property : 'plainText',
									value : term,
									anyMatch : true,
									caseSensitive : false
								};
								plugin.filterRelations([filter]);
							}
						}
					});
				}
				return btns;
			}
		} else {
			//returning null will cause the toolbar to disappear
			return null;
		}
	};

	plugin.showMoreLessFilter = function(rec) {
		if (plugin.options.mode === 'literals' && plugin.options.showLess) {
			return plugin.options.chosenProperties.indexOf(rec.data.linkId) !== -1;
		}
		return true;
	}

	/**
	 * Creates the object relations store.
	 */
	plugin.getRelationsStore = function() {
		Ext.define('EMF.ObjectRelations', {
			extend : 'Ext.data.Model',
			fields : ['name', 'value', 'plainText', 'editType', 'isMultiValued', 'oldValue', 'linkId', 'linkType', 'cls', 'toId', 'toType',
					'actions']
		});

		var opts = plugin.options;
		var url = opts.contextPath + opts.service.loadData;
		url += '?displayEmpty=' + opts.displayEmpty;
		url += '&id=' + opts.instanceId;
		url += '&type=' + opts.instanceType;
		url += '&definitionId=' + opts.definitionId;

		var storeConfig = {
				model : 'EMF.ObjectRelations',
				remoteFilter : false,
				remoteSort : false,
				filters : [plugin.showMoreLessFilter],
				autoLoad : false
			};

		if(opts.extjsData) {
			storeConfig.data = opts.extjsData;
		} else {
			storeConfig.proxy = {
					type : 'ajax',
					url : url
				};
		}

		if (plugin.options.mode === 'literals') {
			if(!opts.extjsData) {
				storeConfig.autoSync = true;
				if (!plugin.options.instanceId || plugin.options.instanceId === EMF.documentContext.currentInstanceId) {
					storeConfig.autoSync = false;
				}


				if (opts.instanceId !== null) {
					storeConfig.proxy.api = {
						create : opts.contextPath + opts.service.create + '?' + $.param({
							instanceId : opts.instanceId,
							instanceType : opts.instanceType
						}),
						update : opts.contextPath + opts.service.create + '?' + $.param({
							instanceId : opts.instanceId,
							instanceType : opts.instanceType
						}),
						destroy : opts.contextPath + opts.service.removeFact + '?' + $.param({
							instanceId : opts.instanceId,
							instanceType : opts.instanceType
						})
					};
				} else {
					storeConfig.proxy.api = {};
				}

				storeConfig.onDestroyRecords = function(record, operation, success) {
					for ( var index in record) {
						$(document).trigger('emf-object-relations-property-delete', {
							instanceId : plugin.options.instanceId,
							instanceType : plugin.options.instanceType,
							linkId : record[index].data.linkId,
							value : record[index].data.value,
							isMultiValued : record[index].data.isMultiValued
						});
					}
				};

				storeConfig.listeners = {
					update : function(store, record, operation, modifiedFieldNames, eOpts) {

						if (!storeConfig.autoSync) {
							$(document).trigger('emf-object-relations-property-change', {
								instanceId : plugin.options.instanceId,
								instanceType : plugin.options.instanceType,
								linkId : record.raw.linkId,
								value : record.data.value,
								oldValue : record.raw.value,
								isMultiValued : record.data.isMultiValued
							});
						} else {
							var data = {
								instanceId : plugin.options.instanceId,
								instanceType : plugin.options.instanceType,
								linkId : record.raw.linkId,
								value : record.data.value,
								oldValue : record.raw.value,
								isMultiValued : record.data.isMultiValued
							};
							$.ajax({
								contentType : "application/json",
								data : JSON.stringify(data),
								type : 'POST',
								url : opts.contextPath + plugin.options.service.create + '?' + $.param({
									instanceId : opts.instanceId,
									instanceType : opts.instanceType
								}),
								complete : function(response) {
									$.extend(true, data, jQuery.parseJSON(response.responseText));
									$(document).trigger('emf-object-relations-property-change', data);
								}
							});
						}

						record.raw.value = record.data.value;
					},
					load : function(store, records, operation, modifiedFieldNames, eOpts) {
						if(!idoc.object.id) {
							$.each(records, function(index, record) {
								if(idoc.object.properties && idoc.object.properties[record.raw.linkId]) {
									record.raw.value = idoc.object.properties[record.raw.linkId];
								}
							});
						}
					}
				};
			}
		}
		plugin.relationsStore = Ext.create('Ext.data.Store', storeConfig);

		return plugin.relationsStore;
	};

	/**
	 * Creates editor for relationship type column
	 */
	plugin.getRelationshipTypeEditor = function(edittedRecord) {
		var relationTypeEditor = null;
		if (plugin.hasAction(edittedRecord.get('actions'), 'editDetails')) {
			var opts = plugin.options;
			var url = plugin.options.contextPath + plugin.options.service.loadRelTypesIntersect + '?' + $.param({
				fromId : opts.instanceId,
				fromType : opts.instanceType,
				rdfType: opts.rdfType,
				toId : edittedRecord.get('toId'),
				toType : edittedRecord.get('toType')
			});

			var relationTypeStore = Ext.create('Ext.data.Store', {
				fields : ['title', 'name'],
				autoLoad : true,
				proxy : {
					type : 'ajax',
					url : url
				},
				listeners : {
					load : function(_this, records, successful, eOpts) {
						if (!successful) {
							EMF.dialog.notify({
								title : 'Error',
								message : 'Failed to load allowed object relation types!',
								customStyle : 'error-message'
							});
						}
					}
				}
			});

			relationTypeEditor = Ext.create('Ext.form.ComboBox', {
				forceSelection : true,
				allowBlank : false,
				store : relationTypeStore,
				queryMode : 'local',
				displayField : 'title',
				valueField : 'name',
				value : edittedRecord.get('linkType')
			});
		}

		return relationTypeEditor;
	};

	/**
	 * Columns for the property grid.
	 */
	plugin.getColumns = function() {
		var columns = [
				{
					text : (plugin.options.mode === 'literals') ? 'Name' : plugin.options.labels.relationsType,
					dataIndex : 'name',
					flex : 1,
					tdCls : 'name-cell',
					getEditor : function(record) {
						var editor = null;
						if (plugin.options.mode === 'literals') {
							editor = EMF.extJsComponents.getCellEditor(plugin.options, record.raw);
						} else {
							editor = plugin.getRelationshipTypeEditor(record);
						}
						return editor;
					},
					renderer : function(value, metaData, rec) {
						// add property name as data attribute in order to allow easy testing
						if (plugin.options.mode === 'literals') {
							metaData.tdAttr = "data-property-name=" + rec.data.linkId;
						} else {
							metaData.tdAttr = "data-property-name=" + rec.data.linkType;
						}
						return value;
					}
				},
				{
					text : (plugin.options.mode === 'literals') ? 'Value' : plugin.options.labels.relatedObject,
					dataIndex : 'value',
					flex : 2,
					tdCls : 'value-cell',
					// we add the item type as css style class in order to allow icons to be displayed
					renderer : function(value, metaData, rec) {
						metaData.css = rec.data.cls;
						var obj = rec.raw.description || rec.raw.value;


						if (rec.data.editType === 'date') {
							if(obj) {
								return EMF.date.format(new Date(obj));
							} else {
								return "";
							}
						}

						if (rec.data.editType === 'datetime' || rec.data.editType === 'dateTime') {
							if(obj) {
								return EMF.date.getDateTime(new Date(obj));
							} else {
								return "";
							}
						}

						// If JSON object find label and render it
						if (typeof obj === 'object') {
							return obj.label;
						}

						return obj;
					},
					getEditor : function(record) {
						var editor = null;
						if (plugin.options.mode === 'literals') {
							editor = EMF.extJsComponents.getCellEditor(plugin.options, record.raw, {
								listeners: { select: function(ele, newValue) {
										record.raw.description = newValue[0].data.label;
									}}
							});
						}
						return editor;
					}
				},
				{
					xtype : 'actioncolumn',
					hidden : false,
					width : 21,
					sortable : false,
					menuDisabled : true,
					tdCls : 'actions-cell',
					items : [{
						// don't allow 'has Child' and 'Part of' relations to be deactivated
						// this function should return css class that will be added to the table cell
						getClass : function(v, meta, rec) {
							if (plugin.options.mode === 'literals') {

								var isMultiValued = rec.raw.isMultiValued;
								if (isMultiValued && !plugin.options.preview && typeof rec.raw.multi === 'undefined') {
									return 'add-fact';
								} else if (rec.raw.multi && !plugin.options.preview) {
									return 'deactivate-rel';
								} else {
									return 'hide';
								}
							}
							if (!rec.raw.isMultiValued && !rec.raw.multi) {
								var actions = rec.get('actions');
								if (plugin.hasAction(actions, 'delete')) {
									return 'deactivate-rel';
								} else {
									return 'hide';
								}
							}
						},
						handler : function(grid, rowIndex, colIndex) {

							var rec = grid.getStore().getAt(rowIndex);

							if (!rec.raw.isMultiValued) {
								var relType = rec.get('linkType');
								var actions = rec.get('actions');
								if (plugin.hasAction(actions, 'delete')) {
									var message = plugin.options.labels.confirmDeleteRelation + ' <b>' + rec.get('name') + '</b>' + '<div class="selected-relation">' + rec
											.get('value') + '</div>';
									EMF.dialog.confirm({
										message : message,
										width : 'auto',
										customStyle : 'warn-message',
										okBtn : 'Yes',
										cancelBtn : 'No',
										confirm : function() {
											var linkId = rec.get('linkId');
											if (linkId) {
												$.ajax({
													url : opts.contextPath + plugin.options.service.deactivate,
													data : {
														relationId : rec.get('linkId'),
														instanceId : opts.instanceId,
														toId : rec.get("toId"),
														relType : rec.get("linkType")
													},
													complete : function(data) {
														plugin.relationsStore.reload();
													},
													fail : function() {
														//console.info('Can\'t deactivate relation!');
													}
												});
											}
										}
									});
								}
							} else {
								if (rec.raw.multi) {
									plugin.relationsStore.removeAt(rowIndex);

									$(document).trigger('emf-object-relations-property-delete', {
										instanceId : plugin.options.instanceId,
										instanceType : plugin.options.instanceType,
										linkId : rec.data.linkId,
										value : rec.data.value,
										isMultiValued : rec.data.isMultiValued
									});

								} else {
									// add row to store
									var newRec = Ext.clone(rec.copy());
									newRec.data.isMultiValued = true;
									newRec.data.value = '';
									newRec.data.oldValue = '';
									newRec.data.name = '';
									newRec.data.multi = true;
									var nextRow = plugin.relationsStore.getAt(rowIndex + 1);

									// add row only if not empty row is already inserted
									if (nextRow.data.name != "" || nextRow.data.value != "") {
										plugin.relationsStore.insert(rowIndex + 1, newRec.data);
									}
								}

							}
						}
					}]
				}];
		return columns;
	};

	plugin.hasAction = function(actions, action) {
		var hasAction = false;
		if (actions) {
			for (var i = 0; i < actions.length; i++) {
				if (actions[i].action === action) {
					hasAction = true;
					break;
				}
			}
		}
		return hasAction;
	};

	plugin.hidePropertyEditor = function() {
		var oldButton = plugin.propertyEditor.getComponent(plugin.options.btnIds.pickerBtn);
		if (oldButton) {
			plugin.propertyEditor.remove(oldButton);
			plugin.propertyEditor.remove(plugin.propertyEditor.getComponent(plugin.options.btnIds.cancelBtn));
			var sel = plugin.propertyEditor.down('combo');
			sel.reset();
		}
		plugin.propertyEditor.hide();
	};

	plugin.add = function(btn, evts) {
		if (plugin.options.debug) {
			console.log(arguments);
		}
	};

	plugin.showCreateRecordEditor = function(btn, evts) {
		if (plugin.options.debug) {
			console.log(arguments);
		}
		plugin.propertyEditor.show();
	};

	/**
	 * Provider for the object picker trigger buttons.
	 */
	plugin.pickerButtons = function(type) {
		Ext.define('ObjectPicker', {
			extend : 'Ext.Button',
			alias : 'widget.objectpicker',
			cls : 'btn btn-sm btn-default',
			overCls : 'extjs-component-btn-over',
			focusCls : 'extjs-component-btn-focus'
		});

		if (!plugin.options.btnIds.pickerBtn) {
			plugin.options.btnIds.pickerBtn = "picker-btn-" + EMF.util.generateUUID();
		}
		if (!plugin.options.btnIds.cancelBtn) {
			plugin.options.btnIds.cancelBtn = "cancel-btn-" + EMF.util.generateUUID();
		}
		var types = {
			'objectPicker' : function() {
				return Ext.create('ObjectPicker', {
					id : plugin.options.btnIds.pickerBtn,
					text : plugin.options.labels.objectPickerTrigger,
					margin : '10 10 10 10',
					handler : function() {
						plugin.initObjectPicker();
					}
				});
			},
			'imagePicker' : function() {
				return Ext.create('ObjectPicker', {
					id : plugin.options.btnIds.pickerBtn,
					text : plugin.options.labels.pickimage,
					margin : '10 10 10 10',
					handler : function() {
					}
				});
			},
			'cancel' : function() {
				return Ext.create('ObjectPicker', {
					id : plugin.options.btnIds.cancelBtn,
					text : plugin.options.labels.cancel,
					margin : '10 10 10 10',
					handler : plugin.hidePropertyEditor
				});
			}
		};

		var selected = types[type];

		return selected() || null;
	};

	/**
	 * Creates the property/relations selector l box.
	 * The store is preloaded when the plugin is initialized
	 * if lazy loading should happen, then set the autoLoad=false,
	 * provide only the service url without the extra parameters and implement
	 * the handler which should trigger the lazy loading
	 *
	 * @param changeHandler
	 * @param label
	 * @param itemId The id to be assigne to the combo box DOM element.
	 */
	plugin.getPropertySelector = function(changeHandler, label, itemId) {
		var localOptions = plugin.options;
		var objectRdfType = localOptions.rdfType ? encodeURIComponent(localOptions.rdfType) : null;
		var url = localOptions.contextPath + localOptions.service.loadRelTypes;
		// because clients can modify the service url with additional parameters, we should
		// check if there aren't added some already by checking for '?' sign
		url += (url.indexOf('?') === -1) ? '?' : '&';
		url += 'forType=' + localOptions.instanceType;
		url += '&id=' + localOptions.instanceId;
		url += '&definitionId=' + localOptions.definitionId;
		url += '&rdfType=' + objectRdfType;
		url += '&displayEmpty=' + opts.displayEmpty;

		var propStore = Ext.create('Ext.data.Store', {
			fields : ['title', 'name'],
			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : url
			},
			listeners : {
				load : function(_this, records, successful, eOpts) {
					if (!successful) {
						EMF.dialog.notify({
							title : 'Error',
							message : 'Failed to load allowed object relation types!',
							customStyle : 'error-message'
						});
					}
				}
			}
		});

		propStore.sort("title", "ASC");

		var propCombo = Ext.create('Ext.form.ComboBox', {
			listConfig: {
				id: itemId + '-bound-list',
		        getInnerTpl: function() {
		            return '<div data-property-name="{name}">{title}</div>';
		        }
			},
			id: itemId + '-wrapper',
			inputId : itemId,
			fieldLabel : label,
			flex : 1,
			maxWidth : 400,
			labelAlign : 'right',
			//		    labelWidth	: 0,
			store : propStore,
			queryMode : 'local',
			displayField : 'title',
			valueField : 'name',
			margin : '10 0 10 0',
			listeners : {
				change : changeHandler
			// !!! Don't delete. See the function documentation.
			//		    	focus: function(comp, evt, eOpts) {
			//		    		// pass additional parameters
			//		    		propStore.getProxy().extraParams = {
			//		    			forType    : plugin.options.instanceType,
			//		    			id         : plugin.options.instanceId
			//		    		};
			//		    		propStore.load();
			//		    	}
			}
		});
		return propCombo;
	};

	/**
	 * Change handler for the combo for selecting of the type for the new relation.
	 */
	plugin.changeRelationTypeHandler = function(combo, newValue, oldValue, eOpts) {
		if (newValue) {
			plugin.selectedProperty = newValue;
			var record = combo.findRecordByValue(newValue);

			if (record) {
				// remove old buttons
				var pickerButton = plugin.propertyEditor.getComponent(plugin.options.btnIds.pickerBtn);
				if (pickerButton) {
					plugin.propertyEditor.remove(pickerButton);
				}
				var cancelButton = plugin.propertyEditor.getComponent(plugin.options.btnIds.cancelBtn);
				if (cancelButton) {
					plugin.propertyEditor.remove(cancelButton);
				}
				plugin.propertyEditor.doLayout();

				// and add the new one
				if (plugin.options.mode === 'objects') {
					// TODO: concrete button should be mapped by some key
					var button = plugin.pickerButtons('objectPicker');
					if (button) {
						plugin.propertyEditor.add(button);
						plugin.propertyEditor.add(plugin.pickerButtons('cancel'));
					}
					plugin.selectedPropertyTitle = record.data.title;
				} else {

					// FIX for https://ittruse.ittbg.com/jira/browse/CMF-5618
					// all fields are duplicated on focus lost so they need to be removed first
					var saveBtn = plugin.propertyEditor.getComponent(plugin.options.btnIds.btnSave);
					if (saveBtn) {
						plugin.propertyEditor.remove(saveBtn);
					}

					var cancelBtn = plugin.propertyEditor.getComponent(plugin.options.btnIds.btnCancel);
					if (cancelBtn) {
						plugin.propertyEditor.remove(cancelBtn);
					}

					var editor = plugin.propertyEditor.getComponent(plugin.options.btnIds.editor);
					if (editor) {
						plugin.propertyEditor.remove(editor);
						plugin.options.btnIds.editorId = null;
					}

					if (!plugin.options.btnIds.editorId) {
						plugin.options.btnIds.editorId = "editor-id-" + EMF.util.generateUUID();
						plugin.options.btnIds.editor = EMF.extJsComponents.getFieldEditor(plugin.options, record.raw, {
							id : plugin.options.btnIds.editorId,
							margin : '13 10 10 10'
						}, plugin.fieldValueChangeHandler);

					}
					if (!plugin.options.btnIds.btnSave) {
						plugin.options.btnIds.btnSave = "btn-save-" + EMF.util.generateUUID();
					}
					if (!plugin.options.btnIds.btnCancel) {
						plugin.options.btnIds.btnCancel = "btn-cancel-" + EMF.util.generateUUID();
					}

					editor = plugin.options.btnIds.editor;
					plugin.propertyEditor.add(editor);

					saveBtn = Ext.create('Ext.Button', {
						text : plugin.options.labels.save,
						id : plugin.options.btnIds.btnSave,
						margin : '13 10 10 10',
						handler : function() {
							var data = {
								instanceId : plugin.options.instanceId,
								instanceType : plugin.options.instanceType,
								linkId : plugin.selectedProperty,
								value : plugin.newLiteralValue,
								isMultiValued : record.raw.isMultiValued
							}

							if (opts.instanceId != null) {
								$.ajax({
									contentType : "application/json",
									data : JSON.stringify(data),
									type : 'POST',
									url : opts.contextPath + plugin.options.service.create + '?' + $.param({
										instanceId : opts.instanceId,
										instanceType : opts.instanceType
									}),
									complete : function(response) {
										$.extend(true, data, jQuery.parseJSON(response.responseText));
										$(document).trigger('emf-object-relations-property-change', data);
										combo.store.reload();
										combo.clearValue();
										plugin.relationsStore.reload();
										plugin.hidePropertyEditor();
										plugin.selectedProperty = null;
										plugin.newLiteralValue = null;
										plugin.propertyEditor.remove(editor);
										plugin.propertyEditor.remove(saveBtn);
										plugin.propertyEditor.remove(cancelBtn);
										plugin.options.btnIds.editorId = '';
										plugin.options.btnIds.btnSave = '';
										plugin.options.btnIds.btnCancel = '';
									}
								});
							} else {

								var newRec = Ext.clone(plugin.relationsStore.getAt(0).copy());
								newRec.data.isMultiValued = record.raw.isMultiValued;
								newRec.data.value = plugin.newLiteralValue;
								newRec.data.name = record.raw.title;
								newRec.data.linkId = record.raw.name;
								newRec.data.editType = record.raw.editType;
								plugin.relationsStore.insert(0, newRec.data);
								plugin.hidePropertyEditor();
								plugin.selectedProperty = null;
								plugin.newLiteralValue = null;
								plugin.propertyEditor.remove(editor);
								plugin.propertyEditor.remove(saveBtn);
								plugin.propertyEditor.remove(cancelBtn);
							}
						}
					});

					plugin.propertyEditor.add(saveBtn);

					cancelBtn = Ext.create('Ext.Button', {
						text : plugin.options.labels.cancel,
						id : plugin.options.btnIds.btnCancel,
						margin : '13 10 10 10',
						handler : function() {
							combo.store.reload();
							combo.clearValue();
							plugin.relationsStore.reload();
							plugin.hidePropertyEditor();
							plugin.selectedProperty = null;
							plugin.newLiteralValue = null;
							plugin.propertyEditor.remove(editor);
							plugin.propertyEditor.remove(saveBtn);
							plugin.propertyEditor.remove(cancelBtn);
							plugin.options.btnIds.editorId = '';
							plugin.options.btnIds.btnSave = '';
							plugin.options.btnIds.btnCancel = '';
						}
					});

					plugin.propertyEditor.add(cancelBtn);
				}
			} else {
				var pickerButton = plugin.propertyEditor.getComponent(plugin.options.btnIds.pickerBtn);
				if (pickerButton) {
					pickerButton.setDisabled(true);
				}

				saveBtn = plugin.propertyEditor.getComponent(plugin.options.btnIds.btnSave);
				if (saveBtn) {
					saveBtn.setDisabled(true);
				}
			}
		}
	};

	/**
	 * Change handler for the combo for selecting codelist values.
	 */
	plugin.fieldValueChangeHandler = function(_this, newValue, oldValue, eOpts) {
		plugin.newLiteralValue = newValue;
	};

	/**
	 * Change handler for the combo used for relations store filtering.
	 */
	plugin.filterRelationsHandler = function(combo, newValue, oldValue, eOpts) {
		var filter = {
			property : 'name',
			value : combo.getRawValue(),
			anyMatch : false,
			caseSensitive : false
		};
		plugin.filterRelations([filter]);
	};

	/**
	 * Base filter function that triggers the filter on the store.
	 *
	 * @param filters
	 *         Filter objects to be applyed to store
	 */
	plugin.filterRelations = function(filters) {
		plugin.relationsStore.filters.clear();
		plugin.relationsStore.filter(filters);
	};

	return this
			.each(function() {
				// make sure only one instance of the plugin is initialized for element
				var initialized = $.data(this, 'extjsObjectRelations');
				if (!initialized) {
					$.data(this, 'extjsObjectRelations', true);
				} else {
					return;
				}

				var element = plugin.target = this;

				plugin.options = $.extend(true, {}, defaults, opts);

				var lbl = plugin.options.labels;

				var gridConfig = {
					height : plugin.options.height,
					columnLines : true,
					loadMask : true,
					width : 'auto',
					tbar : plugin.getToolbarButtons(this),
					store : plugin.getRelationsStore(),
					columns : plugin.getColumns(),
					cls : 'relations-wgt-wrapper',
					viewConfig : {
						stripeRows : true
					},
					listeners : {
						// we load the store after the view is ready and don't allow it to auto load
						viewready : function(_this, eOpts) {
							if (!opts.extjsData) {
								_this.getStore().load();
							}
						},
						beforeedit : function(editor, e) {
							if (plugin.options.mode !== 'literals') {
								var actions = e.record.get('actions');
								return plugin.hasAction(actions, 'editDetails');
							} else {
								if(e.colIdx == 0) {
									return false;
								}
								return e.record.raw.editable;
							}
						},
						edit : function(editor, e, eOpts) {
							if (plugin.options.mode !== 'literals') {
								var message = plugin.options.labels.updatemsg;
								EMF.dialog.confirm({
									message : message,
									width : 'auto',
									confirm : function() {
										var edittedRecord = e.record;
										var items = {};
										var item = {};
										item.targetId = opts.instanceId;
										item.targetType = opts.instanceType;
										item.destId = edittedRecord.get('toId');
										item.destType = edittedRecord.get('toType');
										items[0] = item;

										var data = JSON.stringify({
											relType : edittedRecord.get('name'),
											selectedItems : items
										});

										$.ajax({
											contentType : "application/json",
											data : data,
											type : 'POST',
											url : opts.contextPath + plugin.options.service.update + '?' + $.param({
												relationId : edittedRecord.get('linkId')
											}),
											complete : function(data) {
												plugin.relationsStore.reload();
												plugin.selectedProperty = null;
												plugin.newLiteralValue = null;
											}
										}).fail(function(data, textStatus, jqXHR) {
											EMF.dialog.open({
												title : ' ' + jqXHR,
												message : data.responseText,
												notifyOnly : true,
												customStyle : 'error-message',
												width : 'auto'
											});
										});
									},
									cancel : function() {
										e.grid.getStore().rejectChanges();
									},
									close : function() {
										e.grid.getStore().rejectChanges();
									}
								});
							}
						}
					}
				};

				if (!plugin.options.preview /*&& plugin.options.mode === 'literals'*/) {
					gridConfig.selType = 'cellmodel';
					gridConfig.plugins = [Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit : 1
					})];
				}

				// create the panel grid
				plugin.panelGrid = new Ext.grid.GridPanel(gridConfig);

				// property editor form
				plugin.propertyEditor = new Ext.form.Panel({
					hidden : true,
					layout : {
						type : 'hbox'
					},
					cls : 'relations-wgt-property-editor',
					items : [plugin
							.getPropertySelector(plugin.changeRelationTypeHandler, plugin.options.labels.propertySelector, "relations-type-selector-" + EMF.util
									.generateUUID())],
					defaults : {
						labelAlign : 'top'
					}
				});
				
				// Collapse datatable widget in revision
				if(opts.extjsData) {
					plugin.relationsStore.filterBy(plugin.showMoreLessFilter);
				}
				
				plugin.showMoreLessPanel = new Ext.form.Panel({
					hidden : plugin.options.mode !== 'literals',
					layout : {
						type : 'hbox'
					},
					items : [Ext.create('Ext.Button', {
						id : 'showMoreButton:' + EMF.util.generateUUID(),
						cls: 'show-more-less-toggle-' + plugin.options.mode,
						text : plugin.options.showLess ? plugin.options.labels.showMore : plugin.options.labels.showLess,
						listeners : {
							click : function(button, event) {

								plugin.options.showLess = !plugin.options.showLess;
								
								plugin.relationsStore.filterBy(plugin.showMoreLessFilter);
								if (plugin.options.showLess) {
									button.setText(plugin.options.labels.showMore);
								} else {
									button.setText(plugin.options.labels.showLess);
								}
							}
						}
					})],
					defaults : {
						labelAlign : 'top'
					}
				});

				// CMF-13693 rendering to detached element
				function render() {
					if (!$.contains(document.documentElement, element)) {
						setTimeout(render, 60);
					} else {
						plugin.widgetContainer = Ext.create('Ext.container.Container', {
							renderTo : element,
							width : plugin.options.width,
							height : plugin.options.height,
							cls : 'extjs-component',
							items : [plugin.panelGrid, plugin.propertyEditor, plugin.showMoreLessPanel],
							layout : {
								type : 'anchor'
							}
						});
					}
				}

				render();

				// on window resize, we want to reshape the plugin in order to be propely lied out
				var timer;
				$(window).resize(function() {
					clearTimeout(timer);
					timer = setTimeout(function() {
						plugin.widgetContainer.doLayout();
					}, 100);
				});
			});
};