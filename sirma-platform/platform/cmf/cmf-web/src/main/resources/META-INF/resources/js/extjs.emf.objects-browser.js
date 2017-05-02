/**
 * Objects browser implementation on top of ExtJs Tree Panel.
 */
EMF.CMF.objectsExplorer = {

	config : {

		// ----------------------------------------
		// required arguments
		// ----------------------------------------
		// container id
		container : null,
		// root node id
		node : null,
		// root node type
		type : null,
		// application context path
		contextPath : null,
		currentNodeId : null,
		currentNodeType : null,

		// ----------------------------------------
		// optionals
		// ----------------------------------------
		// If selector for the root (projects or cases) should be enabled.
		changeRoot : false,
		// Number of items to be loaded in the roots selector dropdown.
		rootItemsLimit : 30,
		// Default root type if not specified.
		rootType : 'caseinstance',
		// The minimum signs required to be entered before the roots selector to trigger remote
		// loading.
		minSignsForSearch : 2,
		// If checkboxes should be rendered in front of the nodes to allow selection.
		allowSelection : false,
		// If the root node should be selectable trough a checkbox if allowSelection is enabled.
		allowRootSelection : false,
		// Used with 'allowSelection' and shows if only a single node can be selected in the tree.
		singleSelection : true,
		// If the tree should expand itself to the currentInstance.
		autoExpand : false,
		// TODO: not implemented yet - should expand the whole tree.
		// Filters to be used for deciding whether given entry should have a checkbox in front.
		// This is used in combination with 'allowSelection'. If allowSelection=false, then no
		// filtering is applied.
		autoExpandAll : false,
		// Something like this:
		// "(instanceType=='sectioninstance'&&purpose==null)||(instanceType=='documentinstance'&&purpose=='iDoc')||(instanceType=='objectinstance')"
		filters : null,
		// If links rendered inside tree should be clickable. If not, then the links are replaced
		// with span tags.
		clickableLinks : true,
		// When a link is clicked the target should be opened in a new browser tab.
		clickOpenWindow : true,
		// The width of the panel that wraps the tree. Can be number or percent.
		panelWidth : '100%',
		// The width of the roots combo box.
		rootSelectorWidth : 400,
		// If not set, then height is not fixed.
		panelHeight : null,
		// If tooltips should be enabled.
		tooltipsEnabled : true,
		// Default root node text.
		rootNodeText : null,
		rootInstanceHeader : null,
		// Default root node tooltip.
		rootNodeTooltip : 'Root',
		// If the root node should be expanded by default.
		rootExpanded : true,
		// The id of the combo box for root selection.
		rootSelectorId : 'objectBrowserRootSelector',
		// If there are should be a label in from of the root selector combo box.
		renderSelectorLabel : true,
		// Time interval for checking before next search for given node to be executed.
		autoExpandInterval : 150,
		// Number of times to be repeated searching for given node in the DOM when expanding the
		// tree.
		autoExpandIntervalCount : 20,
		// If the root node should be visible or not.
		rootVisible : true,
		// If false will render '+' sign with lines.
		useArrows : true,
		manualExpand : false,
		// Debug mode.
		debug : false,
		// default header or passed by config
		header: "",

		service : {
			load : '/service/objects/browse/tree',
			roots : '/service/objects/browse/roots'
		}
	},

	init : function(opts) {
		var config = $.extend({}, EMF.CMF.objectsExplorer.config, opts);
		config = $.extend({}, config, CMF.config.objectsBrowserDefaults);
		config.labels = {
			noMatchesFound : _emfLabels['objects.browser.noMatchesFound'],
			searching : _emfLabels['objects.browser.searching'],
			rootSelectorLabel : _emfLabels['objects.browser.browseProjects']
		};
		if (config.debug) {
			console.log('Objects explorer config:', config);
		}
		var initErrorMessage = '';
		if (!config.contextPath) {
			initErrorMessage += '\nRequest context path should be provided for the Objects explorer!';
		}
		if (!config.container) {
			initErrorMessage += '\nContainer element should be provided for the Objects explorer!';
		}
		// When the browser is loaded with no context, then we should allow the plugin to be
		// initialized
		// if (!config.node) {
		// initErrorMessage += '\nRoot node id should be provided for the Objects explorer!';
		// }
		// if (!config.type) {
		// initErrorMessage += '\nRoot node type should be provided for the Objects explorer!';
		// }
		// deny objects explorer initializing if any of required arguments is missing
		if (initErrorMessage !== '') {
			console.error(initErrorMessage, '\n Browser config:', opts);
			return;
		}

		if (config.tooltipsEnabled) {
			Ext.QuickTips.init();
		}

		config.containerSelector = '#' + config.container;

		// create container for the browser
		config.containerPanel = $(config.containerSelector);

		// set current root as default to be loaded by default in the tree
		var currentRoot = {
			instanceId : config.node,
			instanceType : config.type,
			header : config.rootNodeText || config.rootInstanceHeader,
			name : ''
		};
		config.roots = [currentRoot];

		// render the root (project selector)
		if (config.changeRoot) {
			EMF.CMF.objectsExplorer.loadRoots(config);
			EMF.CMF.objectsExplorer.buildTree(config, config.node, config.type);
		}
		// if no project selector is visible, then render tree according to root instance that is
		// opened
		else {
			EMF.CMF.objectsExplorer.buildTree(config, config.node, config.type);
		}
	},

	/**
	 * Load data for browser roots selector.
	 */
	loadRoots : function(config) {
		// build the dropdown
		var container = config.containerPanel;
		if (config.renderSelectorLabel) {
			$('<label for="' + config.rootSelectorId + '" class="ob-root-selector-label">' + config.labels.rootSelectorLabel + '</label>')
					.appendTo(container);
		}
		var rootSelector = $('<input type="hidden" id="' + config.rootSelectorId + '" data-select-name="rootSelector" class="ob-root-selector" />')
				.appendTo(container);
		var rootType = config.rootType;
		var rootItemsLimit = config.rootItemsLimit;
		var requestHeader = config.header ? config.header : "";
		var url = config.contextPath + config.service.roots;
		rootSelector.select2({
			formatNoMatches : config.labels.noMatchesFound,
			formatSearching : config.labels.searching,
			inherit_select_classes : true,
			width : config.rootSelectorWidth,
			// 0 because to trigger loading on first click! config.minSignsForSearch,
			minimumInputLength : 0,
			minimumResultsForSearch : 0,
			allowClear : true,
			id : function(item) {
				return item.instanceId;
			},
			initSelection : function(element, callback) {
				var id = $(element).val();
				if (id !== "") {
					$.ajax(url, {
						data : {
							rootType : rootType,
							limit : rootItemsLimit,
							term : term,
							header: requestHeader
						},
						dataType : 'json'
					}).done(function(data) {
						callback(data);
					});
				}
			},
			ajax : {
				quiteTime : 500,
				url : url,
				dataType : 'json',
				data : function(term, page) {
					if (!term && term.length < config.minSignsForSearch) {
						return {
							header: requestHeader
						};
					}
					return {
						rootType : rootType,
						limit : rootItemsLimit,
						term : term,
						header: requestHeader
					};
				},
				results : function(data, page) {
					var result = config.roots = data || [];
					return {
						results : result
					};
				}
			},
			formatResult : function(item) {
				return item.header;
			},
			formatSelection : function(item) {
				return item.header;
			}
		}).on('change', function(evt) {
			var selected = evt.added || '';
			if (selected) {
				EMF.ajaxloader.showLoading(config.containerSelector);
				EMF.CMF.objectsExplorer.buildTree(config, selected.instanceId, selected.instanceType);
			}
		});
	},

	/**
	 * Builds the tree using as root the instance for provided id.
	 */
	buildTree : function(config, selectedRootId, selectedRootType) {
		// we must have a selected instance to build the tree
		if (!selectedRootId && !selectedRootType) {
			return;
		}
		var rootNodeText = config.rootNodeText || config.rootInstanceHeader;
		var rootNode;
		if (config.roots) {
			rootNode = EMF.CMF.objectsExplorer.getRootData(config, selectedRootId);
		}
		// skip tree building due to missing root node
		if (config.changeRoot && (!rootNode || !selectedRootType)) {
			return false;
		}
		var rootConfig = {
			expanded : config.rootExpanded && config.autoExpand,
			text : rootNode ? rootNode.header : rootNodeText,
			// in case we have no context root we use the selection from root selector
			id : selectedRootId || config.node,
			// in case we have no context root we use the selection from root selector
			dbId : selectedRootId || config.node,
			cls : selectedRootType,
			// this is workaround that prevents the store to autoload
			// http://docs.sencha.com/extjs/4.2.2/#!/api/Ext.data.AbstractStore-cfg-autoLoad
			children : []
		};

		// When the tree should allow selection we should set the 'checked' property in
		// order the checkboxes to appear.
		// The checked property should not be present in the root configuration if the
		// checkbox should not be visible
		// The root node should be selectable only if node selection is enabled and is allowed root
		// to be selected
		// or there is a filter that matches the root node type
		if (config.allowSelection && (/* selectedRootType in config.instanceFilter || */config.allowRootSelection)) {
			rootConfig.checked = false;
		}

		// if tree is not built in this container, we proceed and build it
		if ($(config.containerSelector + ' .objects-tree').length === 0) {
			config.store = getStore();
			config.panel = createPanel();
			config.store.load({
				node : config.store.getRootNode()
			});
			EMF.ajaxloader.hideLoading(config.containerSelector);
		}
		// if tree is already built in this container, we only update it
		else {
			rootConfig.id = selectedRootId;
			var selectedRootNode = EMF.CMF.objectsExplorer.getRootData(config, selectedRootId);
			if (selectedRootNode) {
				rootConfig.text = selectedRootNode.header;
				config.store.setRootNode(rootConfig);
				config.store.load({
					node : config.store.getRootNode()
				});
			}
			EMF.ajaxloader.hideLoading(config.containerSelector);
		}

		// handle splitter resize event and trigger layout for tree panel
		$(document).on('splitterresize', function(evt) {
			config.panel.doLayout();
		});

		/**
		 * Builds the tree store.
		 */
		function getStore() {
			var url = config.contextPath + config.service.load;
			var store = Ext.create('Ext.data.TreeStore', {
				layout : 'fit',
				autoLoad : false,
				root : rootConfig,
				proxy : {
					type : 'ajax',
					url : url
				},
				listeners : {
					load : function(theStore, node, records, successful, eOpts) {
						if (config.autoExpand) {
							// var data = theStore.proxy.reader.jsonData;
							if (records && records.length > 0) {
								var currentNodePath = records[0].raw.cnPath;
								var pathArray = currentNodePath.split('/');
								if (pathArray && pathArray.length > 0) {
									var rootPath = pathArray.slice(0, 1)[0];
									pathArray = pathArray.splice(1);
									lazyExpand(config, theStore.ownerTree.getRootNode(), pathArray, rootPath);
									// mark current node
									config.panel.getSelectionModel().select(theStore.getNodeById(currentNodePath));
								}
							}
						}
						// reset the flag after every load/expand
						config.manualExpand = false;
					}
				}
			});

			store.getProxy().extraParams = {
				type : config.type
			};
			return store;
		}

		/**
		 * Creates the tree panel.
		 */
		function createPanel() {
			var listeners = {};
			// We handle this event in order to allow extra parameters to be added to the request
			// that the store performs.
			listeners.beforeload = function(store, operation, eOpts) {
				if (config.debug) {
					console.info('Objects explorer: beforeload handler arguments', arguments);
				}
				var selectedNodeType = operation.node.raw.cls || config.type;

				store.getProxy().extraParams = {
					type : selectedNodeType,
					currId : config.currentNodeId,
					currType : config.currentNodeType,
					allowSelection : config.allowSelection,
					filters : config.filters,
					clickOpenWindow : config.clickOpenWindow,
					clickableLinks : config.clickableLinks,
					manualExpand : config.manualExpand,
					header: config.header
				};
			};

			// we handle cellclick in order to know when the tree is manually expanded by the user
			listeners.cellclick = function() {
				config.manualExpand = true;
			};

			// Fired when a checkbox for a node is un/checked
			// - If the tree is in singleSelection mode, we traverse the whole tree to uncheck all
			// the other nodes
			// and keep the checked state of the last checked node. We are interested only of nodes
			// that already
			// have the 'checked' property and the others are ignored.
			// - If the tree is not in singleSelection mode, we don't modify the checked state of
			// the other nodes
			// - Collect the selected nodes in array and fire an event with that array to allow
			// clients to react
			// on the selection changes.
			if (config.allowSelection) {
				listeners.checkchange = function(node, checked, eOpts) {
					var container = $('#' + config.container);
					if (container.length > 0) {
						var records = [], selectedNodes = [];

						if (config.singleSelection) {
							// traverse down the tree from the root node
							cascadeCheck(config.store.getRootNode(), false);
							// after the tree is cascaded and all the checkboxes are unchecked, we
							// update the
							// checked property of the last checked one
							if (checked) {
								node.set('checked', checked);
								records = [node];
							}
						} else {
							records = config.panel.getChecked();
						}
						// collect the checked nodes data
						Ext.Array.each(records, function(rec) {
							var imagePath = EMF.util.objectIconPath(rec.raw.cls, 64);
							var data = {
								// TODO: check where is used and remove this property - dbId should
								// be used instead
								nodeId : rec.raw.dbId,
								dbId : rec.raw.dbId,
								path : rec.data.id,
								nodeType : rec.raw.cls,
								type : rec.raw.cls,
								icon : imagePath,
								default_header : rec.raw.text,
								compact_header : rec.raw.text,
								breadcrumb_header : rec.raw.text
							};
							selectedNodes.push(data);
						});
						// fire an event with the collected data
						container.trigger({
							type : 'object.tree.selection',
							selectedNodes : selectedNodes
						});
					}
				};
			}

			// On item expand we invoke filter function to remove items that do not match required
			// type
			// Register the handler only if filter is provided
			if (config.instanceFilter) {
				// bind listener for node expanding that will filter children by instance type
				listeners.itemexpand = function(_this, eOpts) {
					EMF.CMF.objectsExplorer.filterNodesBy(_this, config.instanceFilter);
					EMF.ajaxloader.hideLoading(config.containerSelector);
				};
			} else {
				listeners.itemexpand = function(_this, eOpts) {
					EMF.ajaxloader.hideLoading(config.containerSelector);
				};
			}

			listeners.itemcollapse = function(_this, eOpts) {
				EMF.ajaxloader.hideLoading(config.containerSelector);
			};

			listeners.beforeitemexpand = function(_this, eOpts) {
				EMF.ajaxloader.showLoading(config.containerSelector);
			};

			Ext.define('CMF.tree.Panel', {
				extend : 'Ext.tree.Panel',
				store : config.store,
				border : false,
				bodyBorder : false,
				minWidth : 400,
				width : config.panelWidth,
				height : config.panelHeight,
				renderTo : config.container,
				rootVisible : config.rootVisible,
				useArrows : config.useArrows,
				cls : 'objects-tree',
				emptyText : "No records found.",
				listeners : listeners
			});

			return Ext.create('CMF.tree.Panel', {});
		}

		/**
		 * Traverses down the tree from provided node and sets the checked property on all nodes
		 * that already have one.
		 *
		 * @param node
		 *        A node from which to start the traversing.
		 * @param checked
		 *        The value to be set on the every node.
		 */
		function cascadeCheck(node, checked) {
			Ext.suspendLayouts();
			node.cascadeBy(function(currentNode) {
				if (currentNode.get('checked') !== null) {
					currentNode.set('checked', checked);
				}
			});
			Ext.resumeLayouts(true);
		}

		/**
		 * Recursively expand the tree to a given node. Every node has an id that is actually the
		 * path in the tree to that node. The path/branch to be expanded is given in the pathArray
		 * argument. On every recursive call from the path array is taken an item and is appended to
		 * the currentPath that is actually compared to child node ids when searching for next one
		 * to be expanded. Because when store is loaded, the DOM is updated asynchronously and may
		 * not be available at the time of expanding, we wrap the expand invocation in an interval
		 * to loop until the DOM is ready or given count elapses.
		 *
		 * @param config
		 *        The plugin configuration object.
		 * @param rootNode
		 *        The node being expanded.
		 * @param pathArray
		 *        The node ids that covers the path to the current node.
		 * @param currentPath
		 *        Combined ids of the nodes that covers the path to current node.
		 */
		function lazyExpand(config, rootNode, pathArray, currentPath) {
			if (pathArray.length > 0) {
				EMF.ajaxloader.showLoading(config.containerSelector);
				currentPath += '/' + pathArray.slice(0, 1);
				pathArray = pathArray.splice(1);
				var currentNode = null;
				var count = 0;
				var interval = setInterval(function() {
					currentNode = rootNode.findChild('id', currentPath);
					if (currentNode || count === config.autoExpandIntervalCount) {
						clearInterval(interval);
						if (currentNode && !currentNode.isExpanded()) {
							currentNode.expand(false);
							lazyExpand(config, currentNode, pathArray, currentPath);
						}
						EMF.ajaxloader.hideLoading(config.containerSelector);
					}
					count++;
				}, config.autoExpandInterval);
			}
		}

		return true;
	},

	filterNodesBy : function(currentNode, instanceFilters) {
		var expand = true;
		var view = currentNode.getOwnerTree().getView();
		currentNode.eachChild(function(node) {
			// console.info('filterNodesBy: node:', node, '| type:', node.raw.cls, '| term:', type,
			// '| notmatch:', (node.raw.cls !== type), '| removing:', (node.raw.leaf &&
			// (node.raw.cls !== type)),
			// '| leaf:', node.raw.leaf);
			if (node.raw.leaf && node.raw.cls in instanceFilters) {
				var uiNode = view.getNodeByRecord(node);
				if (uiNode) {
					Ext.get(uiNode).setDisplayed('none');
				}
				expand = false;
			}
		});
		return expand;
	},

	/**
	 * Finds out a root data object by id in the loaded array.
	 */
	getRootData : function(config, rootId) {
		var roots = config.roots, len = roots.length - 1;
		for (var i = len; i >= 0; --i) {
			if (roots[i].instanceId === rootId) {
				return roots[i];
			}
		}
	}
};

/**
 * Add basic filtering to Ext.tree.Panel. Add as a mixin: mixins: { treeFilter:
 * 'MyApp.lib.TreeFilter' } Example taken from: https://gist.github.com/colinramsay/1789536 Another
 * filter example: http://www.sencha.com/forum/showthread.php?245120-Tree-filtering
 */
Ext.define('CMF.ExtJSTreeFilter', {
	filterByText : function(text) {
		this.filterBy(text, 'text');
	},

	/**
	 * Filter the tree on a string, hiding all nodes expect those which match and their parents.
	 *
	 * @param The
	 *        term to filter on.
	 * @param The
	 *        field to filter on (i.e. 'text').
	 */
	filterBy : function(text, by) {

		this.clearFilter();

		var view = this.getView(), me = this, nodesAndParents = [];

		// Find the nodes which match the search term, expand them.
		// Then add them and their parents to nodesAndParents.
		this.getRootNode().cascadeBy(function(tree, view) {
			var currNode = this;
			var txt = currNode.data[by].toString().toLowerCase();
			var found = txt.indexOf(text.toLowerCase()) > -1;
			if (currNode && currNode.data[by] && txt.indexOf(text.toLowerCase()) > -1) {
				me.expandPath(currNode.getPath());

				while (currNode.parentNode) {
					nodesAndParents.push(currNode.id);
					currNode = currNode.parentNode;
				}
			}
		}, null, [me, view]);

		// Hide all of the nodes which aren't in nodesAndParents
		this.getRootNode().cascadeBy(function(tree, view) {
			var uiNode = view.getNodeByRecord(this);
			if (uiNode && !Ext.Array.contains(nodesAndParents, this.id) && !(this.raw.cls === 'projectinstance')) {
				Ext.get(uiNode).setDisplayed('none');
			}
		}, null, [me, view]);
	},

	clearFilter : function() {
		var view = this.getView();

		this.getRootNode().cascadeBy(function(tree, view) {
			var uiNode = view.getNodeByRecord(this);

			if (uiNode) {
				Ext.get(uiNode).setDisplayed('table-row');
			}
		}, null, [this, view]);
	}
});