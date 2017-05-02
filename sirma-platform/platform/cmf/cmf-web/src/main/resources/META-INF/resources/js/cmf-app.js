/**
 * Create SF global namespace.
 */
var SF = SF || {};
// configuration properties
SF.config = SF.config || {};
SF.tagsDecorator = null;
SF.AjaxLoaderHandler = null;
SF.defaultTooltipPosition = 'top';

/** Init function executed on document.ready */
SF.init = function() {};

/** Create CMF global namespace. */
var CMF = CMF || {};

/** Create CMF under EMF namespace. */
EMF.CMF = EMF.CMF || {};

CMF.cl = {
	fields: {},

	/**
	 * Initilize select2 plugin for all codelist fields.
	 *
	 * @param opts
	 *        in format
	 *
	 * <pre>
	 * {
	 * 	field : document.querySelectorAll('[id$=status]')[0],
	 * 	codelist : 102,
	 * 	rerender : ['field1', 'field2'],
	 * 	inclusive : true,
	 * 	filterSource : 'extra1',
	 *  customFilters : ['filter1', 'filter2'],
	 *  label: 'some label' // the field label
	 * }
	 * </pre>
	 *
	 * @returns
	 */
	init: function (opts) {
		var codelist = opts.codelist;
		var rerender = opts.rerender;
		var codevaluesService = EMF.servicePath + '/codelist/' + codelist;
		var field = $(opts.field);
		var fieldId = field.attr('id').split('_')[1];
		var errorTooltipMessage = _emfLabels['cmf.msg.error.wrongValue'];

		/**
		 * Appends required request parameters to the uri for loading of codelist values.
		 * Filter is set as data in current processed field.
		 */
		var getCodevaluesService = function() {
			var filterData = field.data('filterData');
			var url = codevaluesService;
			if (filterData) {
				url += '?' +  $.param(filterData);
			}
			return url;
		};

		/**
		 * Toggles the save button disabled attribute and sets/removed a tooltip error data
		 * if passed.
		 *
		 *  @param tooltip an object with tooltip data in format
		 *  <pre>
		 *  { fieldValue: '', fieldLabel: '' }
		 *  </pre>
		 */
		var updateSaveButton = function(tooltip) {
			if (!tooltip || !tooltip.fieldValue) {
				return;
			}
			var proceedButtonLocators = opts.proceedButtonLocators || ['[id$=saveButton]'];
			for (var i = 0; i < proceedButtonLocators.length; i++) {
				var buttonLocator = proceedButtonLocators[i];
				var proceedButton = $(buttonLocator);
				var tooltipElement = proceedButton.siblings('.tooltip');
				// add tooltip container if not exists yet
				if (tooltipElement.length === 0) {
					// if button locator is provided we can't assume it has a wrapper, so we wrap it
					// we also add an overlay above the button that should play as mouseover event trigger
					// which is needed in case we want to show tooltips on jquery dialog buttons where
					// some of the events doesn't work
					if (opts.proceedButtonLocators) {
						var wrapper = $('<div class="custom-button-wrapper"></div>');
						proceedButton.wrap(wrapper);
						proceedButton.after('<div class="overlay"></div>');
					}
					tooltipElement = $('<span class="tooltip"></span>');
					proceedButton.after(tooltipElement);
				}

				var tooltipDataElement = tooltipElement.find('.' + tooltip.fieldValue);
				// if there is provided label, assume that a new tooltip entry must be added
				// otherwise it is removed by css class as provided with the tooltip.fieldValue property
				if (tooltip.fieldLabel) {
					// don't duplicate same entry
					if (tooltipDataElement.length === 0) {
						var label = CMF.trim(tooltip.fieldLabel);
						var errorMessage = errorTooltipMessage.replace('{1}', label);
						tooltipElement.append('<span class="tooltip-data field-error ' + tooltip.fieldValue + '">' + errorMessage + '</span>');
					}
				} else {
					tooltipDataElement.remove();
				}
				// toggle save button according to whether there are field errors or not
				var hasFieldErrorTooltipData = tooltipElement.find('.tooltip-data.field-error').length > 0;
				// don't enable the save button if it is disabled by an rnc rule
				var isDisabledByRncRule = proceedButton.hasClass('rnc-disabled');
				if (!hasFieldErrorTooltipData && !isDisabledByRncRule) {
					// has-tooltip maybe should not be removed!!!
					proceedButton.removeAttr('disabled').parent().removeClass('has-tooltip').find('.overlay').css({'display':'none'});
				} else {
					proceedButton.attr('disabled', 'disabled').parent().addClass('has-tooltip').find('.overlay').css({'display':'inline-block'});
				}
			}
		};

		/**
		 * Toggle the visual status of a field.
		 *
		 * @param target
		 * 			The target field
		 * @param invalid
		 * 			If the target field should be set with error status
		 */
		var setInvalidStatus = function(target, invalid) {
			if (invalid) {
				target.addClass('error');
			} else {
				target.removeClass('error');
			}
		}

		// store all codelist fields in order for later access when the page is loaded and
		// initial change is triggered
		var fieldData = {
			field: field,
			cl: codelist,
			rerender: opts.rerender,
			filterSource: opts.filterSource,
			inclusive: opts.inclusive,
			value: field.val()
		};
		CMF.cl.fields[fieldId] = fieldData;

		// select2 plugin initialization

		var menuConfig = {
			width: 110,
			allowClear: true,
			minimumInputLength : 0,
			minimumResultsForSearch: EMF.config.comboAutocompleteMinimumItems,
			placeholder : _emfLabels['cmf.dropdown.placeholder'],
			dropdownCssClass: function() {
				return fieldId ? fieldId + '-dropdown' : '';
			},
			formatResult: function(item, elem) {
				// add the actual value as data-value attribute to allow easy testing
				elem.attr('data-value', item.value);
				return item.label;
			},
			formatResultCssClass: function(item) {
				return item.value;
			},
			id : function(item) {
				return item.value;
			},
			initSelection : function(element, callback) {
				var currentElement = $(element);
				var id = currentElement.val();
				if (id !== '') {
					$.ajax(getCodevaluesService(), {
						dataType : 'json',
						// the codelist rest service accepts additional parameter
						// that tells if the result should be returned mapped by codelist
						// keys or as an array
						data: {
							mapped: true,
							customFilters: opts.customFilters
						}
					}).done(function(data) {
						if (data) {
							var entry = data[id];
							if(entry) {
								var label = entry.descriptions[entry.ln];
								// unmark the field if the value exists in the new set
								setInvalidStatus(currentElement, false);
								// enable save button and remove the tooltip
								updateSaveButton({
									fieldValue: id
								});
								// execute the callback to apply new value
								callback({
									label: label, value: entry.value
								});
							} else {
								setInvalidStatus(currentElement, true);
								updateSaveButton({
									fieldValue: id,
									fieldLabel: opts.label
								});
							}
						}
					});
				}
			},
			ajax : {
				quiteTime : 800,
				url : getCodevaluesService,
				dataType : 'json',
				data : function(term, page) {
					return {
						customFilters: opts.customFilters,
						q: term
					};
				},
				results : function(data, page) {
					return {
						results : data
					};
				}
			},
			formatSelection : function(item) {
				return item.label;
			}
		};

		menuConfig = $.extend(true, menuConfig, opts.menuConfig);

		field.select2(menuConfig)
		.on('change', function(evt) {
			var target = $(evt.target);
			target.trigger({
				type : 'before-default-change-handler'
			});
			var selected = evt.added || '';
			if (selected) {
				var passedFilterData = evt.filterData;
				var targetFilterData = target.data('filterData');
				var actualFilterData = passedFilterData || targetFilterData;
				if (!actualFilterData) {
					actualFilterData = {
						filterBy: selected.value,
						filterSource: fieldData.filterSource,
						inclusive: fieldData.inclusive
					}
				}
				var len = rerender.length;
				for (var i = 0; i < len; i++) {
					var filterData = {
						filterBy: selected.value,
						filterSource: actualFilterData.filterSource,
						inclusive: actualFilterData.inclusive
					};
					var torender = $('input[id$=' + rerender[i] + ']').eq(0);
					torender.data('filterData', filterData);
					torender.trigger('change');
				}

				setInvalidStatus(target, false);
				var oldValue = target.data('oldValue') || '';
				updateSaveButton({
					fieldValue: oldValue
				});
			}
			CMF.rnc.init();
			target.trigger({
				type : 'after-default-change-handler'
			});
		})
		// collect the old value before change to replace with new one
		// this is needed for tooltip error entries removal for example
		.on('select2-selecting', function(evt) {
			var target = $(evt.target);
			var oldValue = target.val();
			target.data('oldValue', oldValue);
		})
		.on('select2-opening', function(evt) {
			$(evt.target).removeClass('done-loading').addClass('is-loading');
		})
		.on('select2-loaded', function(evt) {
			$(evt.target).removeClass('is-loading').addClass('done-loading');
		})
		.on('select2-clearing', function(evt) {
			var target = $(evt.target);
			var oldValue = target.val();
			target.data('oldValue', oldValue);
		})
		.on('select2-removed', function(evt) {
			var field = $(evt.target);
			var fieldValue = '';
			var fieldId = field.attr('id').split('_')[1];
			var fieldData = CMF.cl.fields[fieldId];
			var filterData = {
					filterBy: fieldValue,
					filterSource: fieldData.filterSource,
					inclusive: fieldData.inclusive
				};

			var oldValue = field.data('oldValue') || '';
			updateSaveButton({
				fieldValue: oldValue
			});

			field.trigger({
				type: 'change',
				added: {
					value: fieldValue
				},
				filterData: filterData
			});
		});
	},

	/**
	 * Onload this function is executed to trigger filtering for all related fields.
	 */
	triggerFiltering: function() {
		var fields = CMF.cl.fields;
		for (var key in fields) {
			var fieldData = fields[key];
			var fieldValue = fieldData.value;
			var field = fieldData.field;
			// omit fields that have no related fields for rerendering
			if (!fieldData.rerender || fieldData.rerender.length === 0) {
				continue;
			}
			var filterData = {
				filterBy: fieldValue,
				filterSource: fieldData.filterSource,
				inclusive: fieldData.inclusive
			};
			field.trigger({
				type: 'change',
				added: {
					value: fieldValue
				},
				filterData: filterData
			});
		}
		CMF.rnc.init();
	}
};

/**
 * CMF configuration properties. This configuration will be merged with these from EMF
 */
CMF.config = {
	dateFieldSelector			: '.cmf-date-field',
	dateTimeFieldSelector		: '.cmf-datetime-field',
	dateRangeStartFieldSelector	: '.cmf-date-range-start',
	dateRangeEndFieldSelector	: '.cmf-date-range-end',
	userLink					: '.logout-link.user-link',
	logoutLink					: '.logout-link.cmf-user-logout',
	// flag that prevent multiple action invocation
	lockBindingActions 			: false,
	objectsBrowserDefaults		: {},
	objectsBrowserFilters		: {
		DOCUMENTS: 			"instanceType=='documentinstance'",
		IMAGES: 			"(mimetype.indexOf('image')!=-1)",
		OBJECTS: 			"instanceType=='objectinstance'",
		TASKS:				"instanceType=='standalonetaskinstance' || instanceType=='taskinstance'",
		SECTIONS: 			"instanceType=='sectioninstance'",
		DOCUMENT_SECTIONS: 	"instanceType=='sectioninstance'&&purpose==null",
		DOCUMENT_SECTION_AND_IDOC_AND_OBJECT: "(instanceType=='sectioninstance'&&purpose==null)||(instanceType=='documentinstance'&&purpose=='iDoc')||(instanceType=='objectinstance')"
	}
};

/**
 * CMF main click handler. Dispatches invocation according to the
 * event source target.
 *
 * @param evt The event being fired.
 */
CMF.clickHandlerDispatcher = function(evt) {

	var srcTarget = $(evt.target);

	// decorate simple links
	CMF.clickHandlers.simpleLinksClickHandler(evt, srcTarget);

	// Clicking on facet header causes the facet body to be expanded/collapsed
	if(srcTarget.is('.facet-header, .facet-header .facet-title')) {
		CMF.clickHandlers.facetToggle(srcTarget);
	}
	// clicking on region label should expand/collapse the region
	else if(srcTarget.is('.dynamic-form-panel .group-label')) {
		CMF.clickHandlers.generatedFormRegionToggle(srcTarget);
	}
	// Open/close the sorting options menu
	else if(srcTarget.is('#sortingOptionsMenu, #sortingOptionsMenu img, #sortingOptionsMenu span')) {
		CMF.clickHandlers.menuToggle(srcTarget);
	}
	// when is clicked on the custom dropdown menu
	else if(srcTarget.is('.selector-activator-link, .selector-activator-link span, .selector-activator-link img')) {
		CMF.clickHandlers.toggleActivatorMenus(evt);
	}
	// Open popup menu with action links for operations
	else if(srcTarget.is('.more-actions-link, .more-actions-link span, #more-actions-link img')) {
		CMF.clickHandlers.showHiddenActions(srcTarget);
	}
	else if(srcTarget.is('.facet-content-input-text')) {
		CMF.clickHandlers.selectInputText(srcTarget);
	}
	// handle clicks on case section collapse/expand icon
	else if(srcTarget.is('.section-togler')) {
		var sectionToggleParent = srcTarget.parent('.section-row');
		sectionToggleParent.toggleClass('expanded');
		sectionToggleParent.parent().find('.section-items-list').slideToggle();
	}
	else if(srcTarget.is('.main-menu .menu-item, .main-menu .menu-item-trigger, .main-menu .menu-item .icon, .main-menu .caret')) {
		var submenu = srcTarget.closest('.menu-item').find('.submenu');
		if(submenu.length > 0) {
			CMF.clickHandlers.mainMenuToggle(submenu);
		}
	}
	else if(srcTarget.is('.workflow-tab-toggler')) {
		srcTarget.closest('.group-header').find('.render-workflow-link').click();
	}
	else {
		// close opened menus if any
		CMF.clickHandlers.closeOpenMenus();
	}
};

/**
 * Allowed actions menu handler. Used for instance and dashlet actions menus.
 */
CMF.actionsMenu = {
	MENU_HIDE_INTERVAL: 1000,

	init: function() {
		$(CMF.config.mainContainerId).on('click.actionmenu', function(evt) {
			var srcTarget = $(evt.target);
			// lazy loaded actions menu opening is triggered by ajax complete event
			// here we just change the cursor
			if (srcTarget.is('.dropdown-toggle.lazy.actions-menu-trigger,.dropdown-toggle.lazy.actions-menu-trigger .caret')) {
				CMF.actionsMenu.changeCursor(srcTarget, 'progress');
			}
			// we have some static actions: my projects and my objects dashlet
			else if(srcTarget.is('.dropdown-toggle.static.actions-menu-trigger,.dropdown-toggle.static.actions-menu-trigger .caret')) {
				CMF.actionsMenu.changeCursor(srcTarget, 'progress');
				CMF.actionsMenu.toggleAllowedActions(srcTarget, evt);
			}
		});
	},

	changeCursor : function(element, cursorType) {
		if(element) {
			var current = element.jquery ? element[0] : element;
			current.style.cursor = cursorType;
			document.getElementsByTagName('html')[0].style.cursor = cursorType;
		}
	},

	/**
	 * 	Method that manage action list dialog. Will be invoked when the client
	 *  click on the action button for additional actions visualization.
	 *
	 *  Additional information:
	 *  1. Prevent action applied from bootstrap core.
	 *  2. Cross-browser fixes(offset).
	 */
	toggleAllowedActions : function(element, evt) {
		if (evt.type !== 'complete') {
			// prevent some issues with bootstrap and angular events
			evt.preventDefault();
			evt.stopPropagation();
		}
		CMF.clickHandlers.closeOpenMenus();
		var contentHolder  		    = $('#content');
		var clonActionClassPointer  = "cloned-allowed-actions loaded-actions";
		var actionButton   	  	    = $(element);
		var parent  	   		    = actionButton.closest('div');
		var defaultStyles = {
			'width'	   : 'auto',
			'min-width': '200px',
			'max-width': '400px'
		};

		// calculate parent offset from top, needed for further
		// action list positioning, also fix IE8 offset
		var scrollOffset = CMF.clickHandlers.findOverflowedParent(actionButton);
		var actions = parent.children('ul:eq(0)').clone();
		var actionsOffset = CMF.clickHandlers.findTotalOffset(actionButton[0]);

		scrollOffset = (scrollOffset == null) ? 0 : scrollOffset.scrollTop();
		actions.css(defaultStyles);
		actions.addClass(clonActionClassPointer);
		contentHolder.after(actions);

		// action list position fixes, based on action button
		var topFix = 14;
		var leftFix = 18;
		if(actionButton.length && actionButton.prev().length){
			topFix = 20;
			leftFix = 23;
			scrollOffset = 0;
		}
		var positionStyles = {
			'top'      : (actionsOffset.top + topFix) - scrollOffset,
			'left'     : (actionsOffset.left + leftFix) - actions.width()
		};
		actions.css(positionStyles);

		var timer;
		actions.show(10, function() {
			CMF.actionsMenu.changeCursor(element, 'default');
		}).on('mouseleave', function() {
			clearTimeout(timer);
			var currentMenu = $(this);
			timer = setTimeout(function() {
				currentMenu.remove();
			}, CMF.actionsMenu.MENU_HIDE_INTERVAL);
		}).on('mouseenter', function() {
			clearTimeout(timer);
		});
	}
};

/**
 * Comments
 */
CMF.comments = {
	init : function(config) {
		$.comments.EMF = EMF || {};
		$.comments.rootId = EMF.documentContext.currentInstanceId;
		$.comments.rootType = EMF.documentContext.currentInstanceType;
		$.comments.parentId = '';
		$.comments.rootSource = '.idoc-main-editor-placeholder';
		$.comments.imagePathURL = EMF.applicationPath + '/images';
		$.comments.loadService = EMF.servicePath + '/comment';
		$.comments.saveService = EMF.servicePath + '/comment';
		$.comments.editService = EMF.servicePath + '/comment';
		$.comments.removeService = EMF.servicePath + '/comment/remove';
		$.comments.source = '.comments-actions';
		$.comments.target = '.comments-panel';
		$.comments.viewSubmitButton = false;
		$.comments.commentSelectors = '#null';

		$.extend($.comments, config);

		$.comments.init();
		$.comments.refresh();

		tinymce.init({
			selector: ".comment-editor",
			plugins: [
				"lists link image anchor internal_thumbnail_link"
			],
			toolbar: "bold italic | bullist numlist | link image | internal_link internal_thumbnail_link",
			menubar: false,
			statusbar: false
		});

		$.ajax({
			type : 'GET',
			url : EMF.servicePath + '/user',
			data : {},
			async : false,
			complete : function(res) {
			    var users = $.parseJSON(res.responseText);
			    for ( var i = 0; i < users.length; i++) {
				$("#author").append(
					"<option value=\"" + users[i].displayName + "\">"
						+ users[i].displayName + "</option>");
			    }
			}
		});
	},

	/**
	 * Open dialog with a form for new comment.
	 */
	showCommentBox: function() {
		$("#annotation_layouts").select2({
            createSearchChoice: function(term, data) {
            	var result = $(data).filter(function() {
            		return this.text.localeCompare(term) === 0;
            	});
				if (result.length === 0) {
					return {id:term, text:term};
				}
			},
            multiple: true,
            data: [
				{id: 0, text: 'story'},
				{id: 1, text: 'bug'},
				{id: 2, text: 'task'}
			]
        });
		$.comments.showCommentBox(EMF.documentContext.currentInstanceId, null, true);
	},

	/**
	 * Reloads comments from the server.
	 */
	reload : function() {
		var rootId = $.comments.loadFilter?$.comments.loadFilter.instanceId:'';
		$.comments.getData(rootId, true);
		buildTree();
	},

	/**
	 * Apply number of records for topic in the comment dashlet's headers. Method is invoke
	 * on event <b>commentTopicSizeChange</b>.
	 *
	 * @param selector class name for counter container.
	 */
	applyCommentTopicSize : function(selector){
		config = {
			selector : {
				commentCount   : '.comments-counter'
			},
			text 	 : {
				results 	   : ' result(s) found.',
				errorContainer : 'Not available comment counter container !'
			}
		};
		// check for available selector and use the default if is not available
		var topicCounterSelector = selector ? selector : config.selector.commentCount;
		var topicCounterHolder = $(topicCounterSelector);
		if(topicCounterHolder){
			var topicNumber = $.comments.topicSize + config.text.results;
			topicCounterHolder.html(topicNumber);
		}else{
			console.error(config.text.errorContainer);
		}

	},

	/**
	 * Change filter button(from comment panels) styles and text.
	 *
	 * @param fromFilterButton boolean value that indicate the
	 *        invoking location.
	 */
	changeCommentFilterButtonStyle : function(fromFilterButton){
		var filterNewStyleClass = "comment-modify-filter";
		var filterButtonSelector = ".dashboard-panel .filterBtn";

		var filterLabel = _emfLabels['cmf.comment.filter.menu'];
		var filterModifyLabel = _emfLabels['cmf.comment.filter.modify.menu'];

		var filterButton = $(filterButtonSelector);

		filterButton.removeClass(filterNewStyleClass);
		filterButton.eq(0).html(filterLabel);

		if(fromFilterButton){
			filterButton.addClass(filterNewStyleClass);
			filterButton.eq(0).html(filterModifyLabel);
		}
	}
};

/**
 * Click handler functions.
 */
CMF.clickHandlers = {

	// Handler for the expand/collapse functionality of the facets.
	facetToggle : function(elem) {
		var facetHeader = elem.closest('.facet-header');
		facetHeader.toggleClass('expanded').parent().find('.facet-content').slideToggle(function() {
			var expanded = facetHeader.siblings(".facet-content").is(':visible');
			if (expanded) {
				facetHeader.children("a").css('display','inline');
			} else {
				facetHeader.children("a").css('display','none');
			}
		});
	},

	// Handler for open/close of the sorting menu
	mainMenuToggle : function(submenu) {
		if (submenu.is(':visible')) {
			submenu.hide();
		} else {
			$('.menu-item .submenu').hide();
			submenu.toggle();
		}
	},

	// generated form region toggle
	generatedFormRegionToggle : function(regionLabel) {
		regionLabel.toggleClass('collapsed').siblings('.region-body, .radio-button-panel, .tasktree-panel').toggle();
		return false;
	},

	// Handler for open/close of the sorting menu
	menuToggle : function(elem) {
		elem.closest('div').find('ul').toggle();
	},

	// Handler for showing/hiding hidden action buttons
	showHiddenActions: function(elem) {
		elem.closest('ul').siblings('.hidden-actions').show().on('mouseleave', function() {
				var actions = $(this);
				setTimeout(function() {
					actions.hide();
				}, 200);
			});
	},

	selectInputText : function(elem) {
		elem.select();
	},


	closeOpenMenus : function(){
		$('.items-list.rf-ulst, .submenu').hide();
		var cloneObjectActions = $('.cloned-allowed-actions');
		if(cloneObjectActions.length){
			cloneObjectActions.remove();
		}
	},

	//toggles single drop-down menu on user dash board
	toggleActivatorMenus : function(event) {
		var target = $(event.target);
		var targetMenu = target.closest('div').find('ul');
		var targetMenuVisible = targetMenu.is(':visible');
		CMF.clickHandlers.closeOpenMenus();
		targetMenu.mouseleave(function(){
			$(this).hide();
			return;
		});
		targetMenu.click(function(){
			$(this).hide();
		});
		if (targetMenuVisible){
			targetMenu.hide();
		} else {
			targetMenu.show();
		}
	},

	/**
	 * Help method for calculating specific element offset based on
	 * their parent.
	 */
	findTotalOffset : function(object) {
		var offsetLeft = offsetTop = 0;
		if (object.offsetParent) {
			do {
				offsetLeft += object.offsetLeft;
				offsetTop += object.offsetTop;
		    } while (object = object.offsetParent);
		 }
		return {left : offsetLeft, top : offsetTop};
	},

	/**
	 * 	Check for parent that has <code>overflow</code>.
	 *  Needed for further offset calculation.
	 */
	findOverflowedParent : function(element){
		var parent = element.parent();
		 if(parent.is(document)){
			 return null;
		 }
		 if(parent.css('overflowY') === 'auto'){
			 return parent;
		 }
		 return CMF.clickHandlers.findOverflowedParent(parent);
	},

	/**
	 * Every link click goes trough this function. Checks if the link is a simple link(not a jsf link)
	 * then set additional parameters to the link and redirect to that location.
	 *
	 * @param evt
	 * 			The click event.
	 * @param element
	 * 			The event target.
	 */
	simpleLinksClickHandler : function(evt, element) {
		var linkParent = element.closest('a');
		var link;
		var isSimpleLink = false;
		if (linkParent.length > 0) {
			link = linkParent.eq(0);
			var linkId = link.attr('id');
			if(!linkId || linkId.indexOf(':') === -1) {
				isSimpleLink = true;
			}
			var linkHref = link.attr('href');
			var linkTarget = link.attr('target');
			var isJsLink = false;
			//javascript:void(0)
			if((linkHref && linkHref.indexOf('javascript') !== -1) || (linkHref && linkTarget)) {
				isJsLink = true;
			}
			if (isSimpleLink && !isJsLink) {
				evt.preventDefault();
				var params = $.deparam(location.search.substr(1));
				var windowId = params['windowId'];
				linkHref = link.attr('href');
				if(linkHref) {
					linkHref += (linkHref.indexOf('?') === -1) ? '?' : '&';
					linkHref += 'windowId=' + windowId;
					linkHref += '&simpleLink=true';
					window.location.href = linkHref;
				}
			}
		}
	}
};

/**
 * Keyup handler functions.
 */
CMF.keyUpHandlers = {

	toggleSaveLinkButton : function(elem, button, hasSelectedCase) {
		var elemValue = CMF.trim($(elem).eq(0).val());
		// enable button
		if(elemValue && hasSelectedCase) {
			$(button).removeAttr('disabled');
		}
		// disable the button
		else {
			$(button).attr('disabled', 'disabled');
		}
	},

	toggleSaveButton : function(elem, button) {
		var elemValue = CMF.trim($(elem).eq(0).val());
		// enable button
		if(elemValue) {
			$(button).removeAttr('disabled');
		}
		// disable the button
		else {
			$(button).attr('disabled', 'disabled');
		}
	}
};

/**
 * Util for initializing specific components for search pages.
 */
CMF.searchPages = {

	init : function(){
		// Initialize select2 drop-downs
		CMF.searchPages.initSelectDropDown();

		// Initialize date ranges if any.
		CMF.utilityFunctions.initDateRange(CMF.config.dateRangeStartFieldSelector, CMF.config.dateRangeEndFieldSelector);

		// Initialize date fields if any.
		CMF.utilityFunctions.initDateFields();
	},

	initSelectDropDown : function(){
		// initialize dropdown menus inside search pages
		$(".search-page select").select2({
			width : function(){
				_width = "300px";
				if(this && this.element){
					var parent = this.element.parent();
					if(parent.hasClass('search-page-selector')) {
						_width = "170px";
					}
				}
				return _width;
			},
			allowClear: true,
			minimumResultsForSearch: EMF.config.comboAutocompleteMinimumItems,
			placeholder: _emfLabels['cmf.dropdown.placeholder']
		});
	}
},

/**
 *  Util for managing vertical splitter. At the beginning we check vertical splitter is
 *  supported by the concret page by identifier.
 */
CMF.verticalSplitter = {

	attachSplitterTo : function(containerClass){
		var splitAreaIdentifier = '#facets';
		splitAreaIdentifier = $(splitAreaIdentifier);
		var isPageSupportSplitter = splitAreaIdentifier.length > 0 ? true : false;
		if(isPageSupportSplitter){
			// if left container is empty - hide it and terminate scroller initialization
			if($.trim(splitAreaIdentifier.html()) === ''){
				splitAreaIdentifier.hide();
				return;
			}
		}

		if(!$.trim(containerClass).length){
			// some debug messages here
			return;
		}

		// if there is a facet panel on the page, then initialize the splitter plugin
		if(isPageSupportSplitter) {
			$(containerClass).splitter({
				type: "v",
				outline: true,
				minLeft: 250,
				sizeLeft: 250,
				// FIXME: debug for the reason of the 'too much recursion' error when this is set to be true
				// We actually need this enabled in order to allow auto resize from the cookie store when the page
				// is opened again or other page with splitter is opened.
				// http://stackoverflow.com/questions/10097458/splitter-js-wont-work-with-new-versions-of-jquery
				// http://stackoverflow.com/questions/5321284/fix-for-jquery-splitter-in-ie9/10505994#10505994
				// https://github.com/e1ven/jQuery-Splitter
				// Confluense uses same plugin too and they manage to fix this issue, but their version don't work for us too!
				resizeToWidth: false,
				cookie: "emf.vsplitter",
				cookiePath: '/emf',
				accessKey: 'I'
			});
		}
	}
},

/**
 * CMF utility functions.
 */
CMF.utilityFunctions = {

	/**
	 * Function that shows a closing reason popup if the current instance is a case (isCase == true) or removes the blockUI.
	 *
	 * @param isCase true if the current instance is a case
	 * @param component the closing reason popup
	 */
	showClosingReasonPopup: function(isCase, component) {
		if (isCase) {
			component.show();
		} else {
			EMF.blockUI.hideAjaxBlocker();
		}
	},

	/**
	 * Function that detects <b>iframe</b> by given identifier and based on specified flag,
	 * changing the visibility.
	 *
	 * @param iframeSelector iframe identifier
	 * @param hide current display flag
	 */
	hideIframe: function(iframeSelector, hide) {
		if(!iframeSelector) {
			return;
		}
		var iframeElement = document.getElementById(iframeSelector);

		if(iframeElement == undefined){
			return;
		}

		EMF.util._toggleIframe([iframeElement], hide);
	},

	/**
	 * Function based on <b>hideIframe</b>, that applying additional styles to the modal dialogs
	 * after visibility is changed.
	 *
	 * @param iframeSelector iframe identifier
	 * @param hide current display flag
	 */
	hideIframeAndManageDialogDimensions : function(iframeSelector, hide){

		CMF.utilityFunctions.hideIframe(iframeSelector, hide);

		var newVersionDialogSelector = '[id*="NewVersion"].modal-panel';
		var newVersionDialog = $(newVersionDialogSelector);

		// apply static width for new version dialog
		// NOTE: remove this after change dialogs
		newVersionDialog.addClass('new-version-width-fix');

		var searchModelDialogSelector = ".search-modal-dialog:last";
		var searchModelDialog = $(searchModelDialogSelector);

		// apply static width for search modal dialog
		// NOTE: remove this after change dialogs
		searchModelDialog.addClass('search-modal-fix');
	},

	/**
	 * Method that will center !!!richfaces!!! modal dialogs(confirmations) at viewport of the screen.
	 */
	centerModalDialog : function(identifier){
		var id = $.trim(identifier);
		// assure that identifier is available
		if(!id){
			return;
		}

		id += '_container';
		var component = EMF.util.jqById(id);

		var win = $(window);
		// calculate top position for modal dialog
		var top = ((win.height() - component.outerHeight()) / 2);
		// calculate left position for modal dialog
		var left = ((win.width() - component.outerWidth()) / 2);

		component.css({
			position : 'fixed',
				 top : top + 'px',
				left : left + 'px'
		});

		// flag that will prevent screen to be
		// scrolled top
		return false;
	},

	/**
	 * Populate sessionStorage with rootInstance object data to be used in basic search as context.
	 */
	setRootInstance: function(id, type, title) {
		if (id) {
			var rootInstance = JSON.stringify({
				id		:	id,
				type	:	type,
				title	:	title
			});
			sessionStorage.setItem('emf.rootInstance', rootInstance);
		} else {
			sessionStorage.removeItem('emf.rootInstance');
		}
	},

	clearRootInstance: function() {
		sessionStorage.removeItem('emf.rootInstance');
	},

	checkForDeadInstanceLinks: function(container, serviceUrl) {
		if (!serviceUrl) {
			return;
		}

		var typeAndIdPairs = [ ];

		// find all links to instances within the specified container
		$(container).find('a.instance-link').each(function() {
			var link = $(this)
				instanceId = link.attr('data-instance-id'),
				instanceType = link.attr('data-instance-type');

			// add each link's instance-id and instance-type to a array.
			// this array will be sent to a rest service to check which ones are deleted
			if (instanceId && instanceType) {
				typeAndIdPairs.push({
					instanceId	: instanceId,
					instanceType: instanceType
				});
			}
		});

		var ajaxCompleteCallback = function(response) {
			var deleted = JSON.parse(response.responseText);
			if (deleted && deleted.values) {
				var deletedLinks = [ ];

				$.each(deleted.values, function() {
					var pair = this,
						link;
					link = container.find('a.instance-link[data-instance-id="' + pair.instanceId + '"]');
					deletedLinks.push(link);
				});

				$.each(deletedLinks, function() {
					var link = $(this);

					if (link.is('.internal-thumbnail')) {
						var overlay = link.find('.not-allowed-overlay');
						if (!overlay.length) {
							link.append('<span class="not-allowed-overlay"></span>');
						}
					} else {
						link.css({
							textDecoration: 'line-through'
						});
					}

					link.addClass('deleted-instance-link')
						.off('click')
						.on('click', function(event) {
							// prevent bubbling and executing of handlers
							event.preventDefault();
							event.stopImmediatePropagation();
						});
				});
			}
		};

		// If there are links to instance make a request to see which ones are deleted
		if (typeAndIdPairs.length) {
			$.ajax({
				type	: 'POST',
				url		: EMF.servicePath + serviceUrl,
				data	: JSON.stringify(typeAndIdPairs),
				complete: ajaxCompleteCallback
			});
		}
	},

	/**
	 *  Method that manage downloading file by proxy URL. The function will also
	 *  search for preview frame element and will reloaded it.
	 */
	downloadDocumentAfterEvent : function(elementURI){
		if (!elementURI) {
			return;
		}
		var previewFrame = $('#previewFrame');
		var previewURL = null;
		if(previewFrame && previewFrame.length){
			previewURL = previewFrame.attr('src');
		}
		var hiddenIFrameID = 'hiddenDownloader',
		iframe = document.getElementById(hiddenIFrameID);
	    if (iframe === null) {
	        iframe = document.createElement('iframe');
	        iframe.id = hiddenIFrameID;
	        iframe.style.display = 'none';
	        document.body.appendChild(iframe);
	    }
	    iframe.src = elementURI;
	    if(previewURL){
	    	previewFrame.attr('src', previewURL);
	    }
	},

	/**
	 * Escapes the colon ':' signs from provided string and ads a '#' prefix to
	 * make it usable as jQuery selector.
	 *
	 * @param id
	 *            A string that should be escaped and prefixed.
	 * @returns {String}
	 */
	escId : function( id ) {
		return '#' + id.replace(/(:|\.)/g, '\\$1');
	},

	/**
	 * Mimic disabled behaviour for a field.
	 * @param target The field to be disabled.
	 * @returns {Boolean}
	 */
	initDisabledField: function(target) {
    	target.on('focus.cmf', function() {
			$(this).blur();
			return false;
		});
		return false;
	},

	toggleDisabledAttribute : function(selector, shouldDisable) {
		var elem = $(selector);
		var isDisabled = elem.attr('disabled') !== undefined;
		if (isDisabled || shouldDisable === false) {
			elem.removeAttr('disabled');
		} else {
			elem.attr('disabled', 'disabled');
		}
	},

	toggleReadonly : function(selector, shouldDisable) {
		var elem = $(selector);
		var isDisabled = elem.attr('readonly') !== undefined;
		if (isDisabled || shouldDisable === false) {
			elem.removeAttr('readonly');
		} else {
			elem.attr('readonly', 'readonly');
		}
	},

    resetPanel : function(fields, buttons) {
        if(fields) {
            $(fields).val('');
        }
        if(buttons) {
            $(buttons).attr('disabled', 'disabled');
        }
    },

	resetReasonPanel : function(fieldSelector, submitButtonSelector) {
		$(fieldSelector).val('');
		$(submitButtonSelector).attr('disabled', 'disabled');
	},

	resetVersionRevertPanel : function() {
		$('.revert-version-type :radio').eq(0).attr('checked', 'checked');
		$('.revert-description').val('');
	},

	initDynamicFormDropdowns : function() {
		var dynamicFormSelectTags = '.dynamic-form-panel select';
		CMF.utilityFunctions.initDropDown(dynamicFormSelectTags, null, function() {
			// is this needed, after rnc init change locations ?
//			CMF.rnc.init();
		});
	},

	initDropdownsForSearchDialogs : function(){
		var documentMoveSelector = '.search-modal-dialog select';
		var options = {width:'307px'};
		CMF.utilityFunctions.initDropDown(documentMoveSelector, options);
	},

	initDropdownsForRequestHelp : function(){
		var requestHelpSelector = '.request-help-panel select';
		CMF.utilityFunctions.initDropDown(requestHelpSelector);
	},

	/**
	 * Method that will be used like core init for select2 plugin.
	 * Here we can add additional plugin options that will be merge
	 * with the default.
	 *
	 * Attribute <b> options </b> is not required.
	 */
	initDropDown : function(elements, options, callback){

	  var settings = {
			width : function(){
				_width = "110px";
				if(this && this.element){
					var parent = this.element.parent();
					if(parent.hasClass('rm-role-selector-wrapper')) {
						_width = "170px";
					}
				}
				return _width;
			},
			minimumResultsForSearch: EMF.config.comboAutocompleteMinimumItems,
			placeholder : _emfLabels['cmf.dropdown.placeholder'],
			allowClear	: true
		};

		// check for additional settings
		if(options){
			// merge them with the default
			settings = $.extend( {}, settings, options);
		}

		$(elements).select2(settings);
		if (callback) {
			callback();
		}
	},

	/**
     * Initializes date fields in the page if any. Any date range field should be skipped
	 * because they are initialized separately.
     */
	initDateFields : function() {
		var fields = $( CMF.config.dateFieldSelector + ', ' + CMF.config.dateTimeFieldSelector)
                        .filter(function(index) {
                            return !$(this).hasClass('date-range');
                        });
		fields.datetimepicker({
			changeMonth		: true,
			changeYear		: true,
			showButtonPanel : true,
			yearRange		: SF.config.yearRange,
			timeText 		: SF.config.timeText,
			hourText 		: SF.config.hourText,
			minuteText 		: SF.config.minuteText,
			currentText 	: SF.config.currentText,
			closeText 		: SF.config.closeText,
			firstDay 		: SF.config.firstDay,
			dateFormat		: SF.config.dateFormatPattern,
			monthNames 		: SF.config.monthNames,
			monthNamesShort	: SF.config.monthNamesShort,
			dayNames		: SF.config.dayNames,
			dayNamesMin 	: SF.config.dayNamesMin,
			separator 		: ', ',
			// shows timepicker if necessary
			beforeShow : function() {
		    	if ($(this).is(CMF.config.dateTimeFieldSelector)) {
		    		$(this ).datetimepicker( 'option', 'showTimepicker', true );
		    	} else {
		    		$(this ).datetimepicker('option', 'showTimepicker', false );
		    	}
		    },
		    onClose : function(dateText, inst) {
		    	// run the rnc check when datepicker is closed
		    	CMF.rnc.init();
		    }
		});
	},

	initDynamicForm : function(panelClass) {
		var selector = '.dynamic-form-panel';
		if (panelClass) {
			selector = '.' + panelClass;
		}
		if($(selector).children().length > 0) {
			CMF.oncompleteHandlers.initGeneratedForm();
		}

	},

	/**
	 * On search pages if there is a field for content search, then input in that
	 * field should be restricted:
	 * - it is not allowed to have only '*' in the field
	 * - allow search to be performed only if there are more then 2 symbols in the field
	 */
	filterSearchArgumentsData : function() {

		// restrict field search only for specific pages
		if($('.document-search-panel').length > 0) {

			var keywordField = $('.keyword-argument');

			// if there is keyword search field on the page
			if (keywordField.length > 0) {

				keywordField.on('keyup', function() {
					var field = $(this);
					var value = field.val();
					// not striped value length
					var len1 = value.length;
					// remove all '*' and '?' characters
					value = value.replace(/[\*\?]/g,'');
					// striped value length
					var len2 = value.length;
					// striped and not striped value lengths must be greater then 2 characters
					// not striped value can be empty (zero length)
					if ((len1 === 0) || ((len1 > 2) && (len2 > 2))) {
						enableButtons();
					} else {
						disableButtons();
					};
				});
			};
		}

		function disableButtons() {
			var btns = $('.search-page .btn');
			var blocker = $('.search-page .button-blocker');
			if(blocker && blocker.length === 0) {
				btns.each(function() {
					var currentBtn = $(this);
					var overlay = $('<div class="button-blocker"></div>').css({
						'width': currentBtn.outerWidth() + 'px',
						'height': currentBtn.outerHeight() + 'px',
		          		'top' : currentBtn.position().top + 'px',
		                'left' : currentBtn.position().left + 'px',
		                'background-color' : '#888888',
		                'position' : 'absolute',
		                'opacity' : '0.3'
					});
					currentBtn.after(overlay);
				});
			}
		}

		function enableButtons() {
			$('.search-page .btn').next('.button-blocker').remove();
		};
	},

	/**
	 * Initializes a date range fields.
	 */
	initDateRange : function(startDateFieldSelector, endDateFieldSelector) {

		var startDateFields = $(startDateFieldSelector);
		var endDateFields = $(endDateFieldSelector);

		for(var i = 0; i < startDateFields.length; i++) {
			var startDateFieldId = CMF.utilityFunctions.escId(startDateFields[i].id);
			var endDateFieldId = CMF.utilityFunctions.escId(endDateFields[i].id);

			initStartDate(startDateFieldId, endDateFieldId);
			initEndDate(startDateFieldId, endDateFieldId);
		}

		function initStartDate(startDateField, endDateField) {
			$( startDateField ).datetimepicker({
				showTimepicker 		: false,
				showButtonPanel 	: true,
				timeText 			: SF.config.timeText,
				hourText 			: SF.config.hourText,
				minuteText 			: SF.config.minuteText,
				currentText 		: SF.config.currentText,
				closeText 			: SF.config.closeText,
				firstDay 			: SF.config.firstDay,
				changeMonth			: true,
				changeYear 			: true,
				yearRange			: SF.config.yearRange,
				dateFormat			: SF.config.dateFormatPattern,
				monthNames 			: SF.config.monthNames,
				monthNamesShort		: SF.config.monthNamesShort,
				dayNames			: SF.config.dayNames,
				dayNamesMin 		: SF.config.dayNamesMin,
				onSelect: function( selectedDate, pickerInst ) {
					$(endDateField).datetimepicker( 'option', 'minDate', selectedDate );
				},
                // we use on beforeShow to set up the range fields in case there are some initial values in the model
				beforeShow: function( input, pickerInst ) {
                    var startDateInitialValue = $(input).val();
                    if(startDateInitialValue) {
                        $(endDateField).datetimepicker( 'option', 'minDate', startDateInitialValue );
                    }
                    var endDateInitialValue = $(endDateField).val();
                    if(endDateInitialValue) {
                        $(startDateField).datetimepicker( 'option', 'maxDate', endDateInitialValue );
                    }
				},
                onClose : function(dateText, inst) {
                    // run the rnc check when datepicker is closed
                    CMF.rnc.init();
                }
			});
		};

		function initEndDate(startDateField, endDateField) {
			$( endDateField ).datetimepicker({
				showTimepicker 	: false,
				showButtonPanel : true,
				timeText 		: SF.config.timeText,
				hourText 		: SF.config.hourText,
				minuteText 		: SF.config.minuteText,
				firstDay 		: SF.config.firstDay,
				currentText 	: SF.config.currentText,
				closeText 		: SF.config.closeText,
				changeMonth		: true,
				changeYear 		: true,
				yearRange		: SF.config.yearRange,
				dateFormat		: SF.config.dateFormatPattern,
				monthNames 		: SF.config.monthNames,
				monthNamesShort	: SF.config.monthNamesShort,
				dayNames		: SF.config.dayNames,
				dayNamesMin 	: SF.config.dayNamesMin,
				onSelect: function( selectedDate, pickerInst ) {
					$(startDateField).datetimepicker( 'option', 'maxDate', selectedDate );
				},
                // we use on beforeShow to set up the range fields in case there are some initial values in the model
				beforeShow: function( input, pickerInst ) {
                    var endDateInitialValue = $(input).val();
                    if(endDateInitialValue) {
                        $(startDateField).datetimepicker( 'option', 'maxDate', endDateInitialValue );
                    }
                    var startDateInitialValue = $(startDateField).val();
                    if(startDateInitialValue) {
                        $(endDateField).datetimepicker( 'option', 'minDate', startDateInitialValue );
                    }
				},
                onClose : function(dateText, inst) {
                    // run the rnc check when datepicker is closed
                    CMF.rnc.init();
                }
			});
		};
	},

	// This uses richfaces modal panel!!!
	riseConfirmation : function(message, confirmationPanelObj) {
		if (message !== '') {
			confirmationPanelObj.show();
			return false;
		}
		return true;
	},

	/**
	 * Method that hide element or number of elements based of the children.
	 * Accept element or array of element.
	 */
	hideElementsByChildrenLen : function(elements) {
		if(!elements) {
			return;
		}
		var el = elements;
		var hasChildren;
		var childPattern = ' > *';
		if($.isArray(el)){
			for(var i in el) {
				hasChildren = $(el[i]+childPattern).length;
				if(!hasChildren){
					$(el[i]).hide();
				}
			}
		} else {
			hasChildren = $(el + childPattern).length;
			if(!hasChildren) {
				$(el).hide();
			}
		}
	},

	/**
	 * Method that adds onChange event handlers to daysField and startDateField.
	 */
	initCalculatedDateFieldsOnChangeHandlers : function(daysField, startDateField, calculatedDateField, calculationType, mindWorkDays) {
		// if there is a value in the input (when an existing case is opened in edit mode) recalculate the value
		var resultDateFields = $("input[id$='" + calculatedDateField + "']");
		var resultDate = resultDateFields.length > 0 && resultDateFields[0].value;
		if (resultDate) {
			CMF.onchangeHandlers.changedFieldForDateCalculation(daysField, startDateField, calculatedDateField, calculationType, mindWorkDays);
		}
		
		$("input[id$='" + daysField + "']").bind('change', function() {
			CMF.onchangeHandlers.changedFieldForDateCalculation(daysField, startDateField, calculatedDateField, calculationType, mindWorkDays);
			}
		);
		$("input[id$='" + startDateField + "']").datepicker(
			'option' , {
				onClose : function(){
					CMF.onchangeHandlers.changedFieldForDateCalculation(daysField, startDateField, calculatedDateField, calculationType, mindWorkDays);
				}
			}
		);
	},
	
	colleaguesUtils : {

			avatarGenerator : function(colleagueData){
				var wrap = $(colleagueData.wrapper);
				var defaultAvatar = '/images/user-icon-32.png';
				var groupAvatar = '/images/group-icon-32.png';
				var dmsProxy = '/service/dms/proxy/';

				if(colleagueData.avatar){
					defaultAvatar = dmsProxy+decodeURIComponent(colleagueData.avatar);
				}else if(colleagueData.type != 'user'){
					defaultAvatar = groupAvatar;
				}

				var path = colleagueData.context+"/"+defaultAvatar;
				var avatarIcon = CMF.utilityFunctions.colleaguesUtils.createIcon(path);

				wrap.html(avatarIcon);
			},

			createIcon : function(path, style){
				var defStyle = 'width:32px; height:32px;';
				if(style){
					defStyle = defStyle + style;
				}
				return '<img src="' + path + '" class="header-icon" style="' + defStyle + '" />';
			}
		},

		/**
		 * Retrieves link to the new web, which opens the create instance dialog. If there is a instance in the context
		 * it is passed, so it can be used as context(parent) for the instance that will be created.
		 */
		openUI2 : function() {
			var requestData = {
				'instance-id' 	:  EMF.documentContext.currentInstanceId,
				'instance-type' :  EMF.documentContext.currentInstanceType
			};

			REST.getClient('WebNavigationRestClient').getLinkToNewWeb({queryParams:requestData})
				.done( function(data) {
					window.location.href = data.newWebLink;
				});
			return false;
		}

};

/**
 * Oncomplete event handlers.
 */
CMF.oncompleteHandlers = {

	initGeneratedForm : function() {
		$(function() {
			CMF.utilityFunctions.initDateFields();
			CMF.utilityFunctions.initDynamicFormDropdowns();
			CMF.rnc.init();
		});
	},

	bindClickHandlerToProcessDiagramPanel : function(processDiagram) {
		if(processDiagram && processDiagram.length) {
			processDiagram.on('click', function() {
				var modalPanel = $('<div class=".process-diagram-holder"></div>');
				$(this).clone().appendTo(modalPanel);
				EMF.dialog.popup({
					content	: modalPanel,
					title	: _emfLabels['cmf.view.process.header.label'],
					width	: 'auto',
					height	: 'auto'
				});
			});
		}
	}
};

/**
 * Onchange event handlers.
 */
CMF.onchangeHandlers = {

	selectChange : function(selectElem, elemToChange){
		var select = $(selectElem);
		if($.trim(select.val()) === '') {
			CMF.utilityFunctions.toggleDisabledAttribute(elemToChange, true);
		} else {
			CMF.utilityFunctions.toggleDisabledAttribute(elemToChange, false);
		}
	},

	/**
	 * Calculates the date based on the number of days, start date and the type of operation.
	 */
	changedFieldForDateCalculation : function(daysField, startDateField, calculatedDateField, calculationType, mindWorkDays) {
		var numberOfDays = Math.abs($("input[id$='" + daysField + "']")[0].value);
		var startDate = $("input[id$='" + startDateField + "']").datepicker('getDate');
		var resultDateSpan = $("span[id$='" + calculatedDateField + "_preview']")[0];
		var resultDateInput = $("input[id$='" + calculatedDateField + "']")[0];
		var resultDate = new Date();
		var calculatedDate = "";
		
		//validation of numberOfDays
		if (!(/\D/.test(numberOfDays))) {
			if (mindWorkDays === "true") {		
				var queryParams	= {
					startDate 	 	: EMF.date.format(startDate, SF.config.dateFormatPattern, false),
					numberOfDays 	: numberOfDays
				};
				REST.getClient('DateRestClient').calculate({
						queryParams : queryParams,
						operation	: calculationType
					})
					.done(function(data){
						calculatedDate = data.calculatedDate;
						resultDateSpan.innerHTML = calculatedDate;
						resultDateInput.value = calculatedDate;
					});
			} else {
				switch (calculationType) {
					case "SUB":
						resultDate.setTime(startDate.getTime() - numberOfDays*24*60*60*1000);
						break;
					default:
						resultDate.setTime(startDate.getTime() + numberOfDays*24*60*60*1000);
						break;
				};
				calculatedDate = EMF.date.format(resultDate, SF.config.dateFormatPattern, false);
				resultDateSpan.innerHTML = calculatedDate;
				resultDateInput.value = calculatedDate;
			}
		} 
	}

};


/**
 * Handler functions for document create panel.
 */
CMF.fileCreateEventHandlers = {

	/**
	 * Shows the manual title field if the selected document type is the free-style document (default
	 * document type - defaultDocumetType).
	 *
	 * Note: This function is not use for the moment. Waithing to be approved by the BA.
	 */
	showHideManualTitle: function(fileTypeField, manualTitleField, defaultDocumetType) {
		if (fileTypeField.value === "" || fileTypeField.value !== defaultDocumetType) {
			$(manualTitleField).parent().hide();
		} else {
			$(manualTitleField).parent().show();
		}
	},

	initFileCreationDialog: function(fileTypeField, manualTitleField, fileNameField, createButton, defaultDocumetType) {
		this.enableOrDisableCreateButton(fileTypeField, fileNameField, createButton);
		$(fileNameField).keyup(function() {
			CMF.fileCreateEventHandlers.enableOrDisableCreateButton(fileTypeField, fileNameField, createButton);
		});
	},

	fileTypeSelectedhandler : function(selectedItem, fileTypeField, manualTitleField, fileNameField,
			createButton, selectedKey, selectedValue, defaultDocumetType) {

		fileTypeField.value = selectedKey;
		//update item selection with the selected value
		$('.selector-activator-link span').text(selectedValue);
		CMF.clickHandlers.menuToggle($(selectedItem));
		this.enableOrDisableCreateButton(fileTypeField, fileNameField, createButton);

		return false;
	},

	/**
	 * Enables/Disables the 'create' button if the required fields (document type and file name)
	 * are filled/not filled.
	 */
	enableOrDisableCreateButton: function(fileTypeField, fileNameField, createButton) {
		if (fileTypeField.value !== "" && fileNameField.value !== "") {
			$(createButton).removeAttr("disabled");
		} else {
			$(createButton).attr("disabled","disabled");
		}
	}

};

CMF.resourcePicker = {

	/**
	 * Load resources and trigger picker to open.
	 *
	 * @param opts Configuration options.
	 * @param triggerFunction A callback function to be executed if resources are successfully loaded.
	 */
	open: function(opts, triggerFunction) {
		CMF.resourcePicker.loadUsers(opts, triggerFunction || CMF.resourcePicker.triggerOpen);
	},

	/**
	 * Trigger event to open the picker.
	 */
	triggerOpen: function(selector, data) {
		$(selector).trigger({
			type : 'openPicklist',
			itemsList : data
		});
	},

	/**
	 * Calls a rest service to load resources.
	 *
	 * @param opts Configuration options.
	 * @param callback A function to be executed after resources are loaded.
	 */
	loadUsers: function(opts, callback) {
		var triggerSelector = opts.triggerSelector,
			keywordsParam = opts.keywords || {};
		$.ajax({
	        url: EMF.servicePath + '/resources/' + opts.type,
	        data: {
	        	filtername : opts.filtername || '',
	        	keywords   : JSON.stringify(keywordsParam),
	        	active : true
	        }
	    })
	    .done(function(data) {
	    	if (data) {
	    		callback(triggerSelector, data);
			}
	    })
	    .fail(function() {
	    	console.error('Failed to load resources!');
	    });
	}
};

/**
 * Handler functions for task reassign panel.
 */
CMF.taskReassignEventHandlers = {

	openPicker: function(opts) {
		var SELECTOR_TYPE_ID = '#';
		// escape the identifier and apply jquery type selector
		opts.triggerSelector = SELECTOR_TYPE_ID + EMF.util.escapeId(opts.triggerSelector);
		if(opts.data){
			// restricted assignees
			CMF.taskReassignEventHandlers.openPicklist(opts.triggerSelector, opts.data);
		}else{
			// filtered assignees
			CMF.resourcePicker.open(opts, CMF.taskReassignEventHandlers.openPicklist);
		}
	},

	// NOTE: need more generic scope for this function
	openPicklist : function(selector, data) {
		$(selector).trigger({
			type : 'openPicklist',
			itemsList : data
		});
	},

	userSelectedHandler : function(selectedItemLink, selectedUserField, selectedKey, selectedValue) {
		selectedUserField.value = selectedKey;
		$('.update-task-button').removeAttr('disabled');
		CMF.clickHandlers.menuToggle($(selectedItemLink));
		$('.selector-activator-link span').text(selectedValue);
		return false;
	}
};

/**
 * View state modification. Utility scope for incompatibility fixes between Richfaces-4.x and JSF-2.2.<p>
 * For more information, please visit: <br />
 * http://stackoverflow.com/questions/21726718/can-i-use-fixviewstate-js-from-omnifaces-with-richfaces-a4jajax-rf4jsf2-2-mo?answertab=active#tab-top
 */
CMF.viewstate = {
	reset : function(){
		$('#viewStateContainer').html('<span id="javax.faces.ViewState"></span>');
	}
};

/**
 * Managing user assignees after transition execution or custom command button.
 */
CMF.taskAssigneesSelectionEventHandlers = {

	/**
	 * Open picklist for selecting assignees for the next task in workflow. The resources
	 * will be loaded based on restriction or custom filter described in the definition.
	 *
	 * @param selector - selector for picklist container, can be indetifier or style class
	 * @param data - loaded resources(assignees)
	 * @param filtername - custom filter name
	 * @param type - picklist type(users/group/all)
	 */
	openPicklist : function(selector, data, filtername, type) {
		var CONSTANTS = {
			MATCHER_IDENTIFIERS : '^#',
			PICKLIST_EVENT      : 'openPicklist'
		};
		// escape the selector(in this case will be element identifier)
		if (selector.match(CONSTANTS.MATCHER_IDENTIFIERS)) {
			selector = EMF.util.escapeId(selector);
		}
		// check for already loaded
		// resources(user or/and groups)
		if($.trim(data)){
			$(selector).trigger({
				type : CONSTANTS.PICKLIST_EVENT,
				itemsList : data
			});
		}else if($.trim(type) && $.trim(filtername)){
			// open picklist by custom filter
			CMF.resourcePicker.open({
				type: type,
				filtername: filtername,
				keywords: {},
				triggerSelector: selector
			});
		}
	}, // end of method

	/**
	 * Prepare data before trigger opening the picklist. The resource will be
	 * extracted from button event.
	 *
	 * @param event - button event
	 * @param styleClass - selector for picklist container
	 */
	preparePicklistData : function(event, styleClass){
		CMF.viewstate.reset();
		CMF.taskAssigneesSelectionEventHandlers.openPicklist(styleClass, event.data);
	}
};

CMF.incomingDocuments = {

	selectDocument : function(triggerLink) {
		var row = $(triggerLink).closest('.pad-1').find('.item-row');
		if(row.hasClass('selected-row')) {
			row.removeClass('selected-row');
		} else {
			row.addClass('selected-row');
		}
	},

	resetPanel : function(listElement) {
		var element = $(listElement);
		element.parent().addClass('add-popup-container');
		element.find('.selected-row').removeClass('selected-row');
	}

};

/**
 * Functions used by entity browser.
 */
CMF.entityBrowser = {

	selectSection : function(triggerLink, inCase) {
		var $triggerLink = $(triggerLink);
		if(inCase) {
			$(triggerLink).closest('.case-sections-list').find('.selected-row').removeClass('selected-row')
				.end().end().parent().addClass('selected-row');
		} else {
			var currentItem = $triggerLink.parent();
			var isCurrentSelected = currentItem.hasClass('selected-row');
			if(isCurrentSelected) {
				currentItem.removeClass('selected-row');
			} else {
				$triggerLink.closest('.case-list').find('.selected-row').removeClass('selected-row');
				currentItem.addClass('selected-row');
			}
		}
	},

	selectCase : function(triggerLink) {
		$(triggerLink).closest('.case-list').find('.selected-row').removeClass('selected-row')
			.end().end().parent().addClass('selected-row');
	}

};

/**
 * Changes the disabled attribute of element.
 *
 * @param selector
 * 			The element selector.
 * @param shouldDisable
 * 			Whether to disable or enable the element.
 */
CMF.toggleDisabledAttribute = function(selector, shouldDisable) {
	var elem = $(selector);
	var isDisabled = elem.attr('disabled') !== undefined;
	if (isDisabled || shouldDisable == false) {
		elem.removeAttr('disabled');
	} else {
		elem.attr('disabled', 'disabled');
	}
};

/**
 * Functions that will manage discussion into cases
 * and projects
 */
CMF.discussionActions = {

	/**
	 * Scroll down the list with messages for current case.
	 */
	scrollBottomFromTop : function(){
		CMF.discussionActions.enableDisableMessageButton();
		var element = $('.discussion-body');
		element.animate({ scrollTop: element.find('ul').outerHeight() }, "fast");
	},

	/**
	 * Disable button for adding messages into case discussion zone.
	 */
	enableDisableMessageButton : function(){
		var messageField = $('.comment-field');
		var messageButton = $('.post-comment-button');
		// remove white spaces and validate
		if($.trim(messageField.val()) === '') {
			// disable button
			messageButton.attr('disabled','disabled');
		} else {
			// enable button
			messageButton.removeAttr('disabled');
		}
	}
};

/**
 * Confirmation handler.
 *
 * @param message
 * 			The message to show in the confirmation window.
 * @param actionName
 * 			The action name to confirm operation for.
 * @returns {Boolean}
 */
CMF.confirm = function(message, actionName) {
	if (message !== '') {
		if(actionName) {
			message += '[' + actionName + ']';
		}
		if (! confirm(message)) {
			return false;
		}
	}
	return true;
};



/**
 * Checks if there are facets on the page and if found some, then make it
 * expanded or collapsed according to whether the marker 'expanded' class
 * is set on the header or not.
 */
CMF.initFacetsStatus = function() {
	var facetHeaders = $('.facet-header');
	if(facetHeaders) {
		facetHeaders.each(function() {
			var currentHeader = $(this);
			if(currentHeader.hasClass('expanded')) {
				currentHeader.siblings('.facet-content').show();
				currentHeader.children().show();
			} else {
				currentHeader.siblings('.facet-content').hide();
				currentHeader.children().hide();
			}
		});
	}
};

CMF.trim = function(str) {
    return str.replace(/^\s+|\s+$/g, "");
};

CMF.rnc = {
	init : function() {
		if($('.dynamic-form-panel').length > 0) {
			CMF.RNC = CMF.RNC || {};
			$('.dynamic-form-panel').RNC({
				rnc			: CMF.RNC,
				formId		: 'formId',
				saveButtonId: 'saveButton',
				debugEnabled: CMF.rncDebugMode || EMF.config.debugEnabled
			});

		}
	}
};

/**
 * Configure the object picker for documents attach operation.
 *
 * @param currentInstanceId
 * 			The target instance id to which the documents should be attached
 * @param currentInstanceType
 * 			The target instance type to which the documents should be attached
 */
EMF.search.picker.attachDocument = function(currentInstanceId, currentInstanceType) {

	// Document picker configuration
	var pickerConfig = {
			defaultsIfEmpty			: true,
			currentInstanceId		: currentInstanceId,
			currentInstanceType		: currentInstanceType,
			defaultObjectTypes		: ['emf:Document', 'emf:Image'],
			objectTypeOptionsLoader : EMF.servicePath + '/definition/all-types?classFilter=emf:Image&classFilter=emf:Document',
			filters					: CMF.config.objectsBrowserFilters.DOCUMENTS,
			service					: '/document/',
			labels: {
				headerTitle: _emfLabels['cmf.document.picker.title.label']
			}
		};

	EMF.search.picker.attach.defaultAttachOperation(pickerConfig);
};

/**
 * Configure the object picker for objects attach operation.
 *
 * @param currentInstanceId
 * 			The target instance id to which the objects should be attached
 * @param currentInstanceType
 * 			The target instance type to which the objects should be attached
 */
EMF.search.picker.attachObject = function(currentInstanceId, currentInstanceType) {
	var pickerConfig = {
			defaultsIfEmpty		: true,
			currentInstanceId	: currentInstanceId,
			currentInstanceType	: currentInstanceType,
			filters				: CMF.config.objectsBrowserFilters.OBJECTS,
			service				: '/object-rest/',
			labels: {
			}
	};

	var searchCfg = EMF.config.search;
	pickerConfig.defaultObjectTypes = searchCfg.availableTypesFilter;

	if (searchCfg) {
		var filter = searchCfg.availableTypesFilter;
		if (filter && filter.length) {
			var filterLength = filter.length,
				url = EMF.servicePath + '/definition/all-types?';
			while(filterLength--) {
				url += 'classFilter=' + filter[filterLength] + '&';
			}
			url += 'skipDefinitionTypes=true';
			pickerConfig.objectTypeOptionsLoader = url;
		}
	}
	EMF.search.picker.attach.defaultAttachOperation(pickerConfig);
};

/**
 * Attach instance (document, object) action handler.
 * - configure the picker
 * - open picker to allow user to search for documents or objects for attach
 * - user selects document or object
 * - check if document or object already exists in the case
 * - notify the user if object already exists in case and don't proceed with the operation
 * - perform attach operation
 *
 * @param opts
 */
EMF.search.picker.attach = {

	/**
	 * Used for attach operation on document and object instances. Builds their object picker,
	 * with specific configurations. Additional configurations can be passed.
	 *
	 * @param opts
	 * 			 contains specific configurations about document and object pickers, which will be used in the picker
	 */
	defaultAttachOperation : function(opts) {
	    var selectedItems 	= {};
	    var config			= EMF.search.picker.attach.getDefaultAttachConfig(opts);
	    var placeholder 	= config.placeholder;

		var buildRequestData = function(selectedItems) {
			var request = {};
			for(var key in selectedItems) {
				if(selectedItems.hasOwnProperty(key)){
					request[key] = {
						   dbId : selectedItems[key].dbId,
						   type : selectedItems[key].type
						}
				}
			}
			return JSON.stringify({
				'currentInstanceId' : opts.currentInstanceId,
				'currentInstanceType': opts.currentInstanceType,
				'selectedItems' : request
			});
		}

	    config.dialogConfig.okBtnClicked = function() {
	    	EMF.blockUI.showAjaxLoader();
	        if (selectedItems) {
	    		// stringifying whole selectedItems object gives error in Chrome
	    		var requestData = buildRequestData(selectedItems);
				$.ajax({
					type: 'POST',
					url: EMF.servicePath + opts.service,
					contentType: "application/json",
					data: requestData
				})
				.done(function() {EMF.search.picker.attach.successfulOperationHandler()})
	            .fail(function(data, jqXHR) {EMF.search.picker.attach.unsuccessfulOperationHandler(data, jqXHR)});
	        }
	    };

	    placeholder.on('object-picker.selection-changed', function(event, data) {
			selectedItems = data.selectedItems;
			var isEmpty = jQuery.isEmptyObject(data.selectedItems);
	    	EMF.search.picker.toggleButtons(isEmpty);
	    }).on('object.tree.selection', function(data) {
	    	selectedItems = data.selectedNodes;
	    	if (selectedItems.length === 0) {
	    		EMF.search.picker.toggleButtons(true);
			} else {
				EMF.search.picker.toggleButtons(false);
			}
	    });

	    placeholder.objectPicker(config);
	    EMF.search.picker.toggleButtons(true);
	},

	/**
	 * AttachTo operation handler. Builds object picker with specific configuration.
	 *
	 * @param opts
	 * 			object containing information about the instance, which will be attached 
	 */
	attachToOperation : function(opts){
		var objectTypes = EMF.config.attachTo.selectedTypes || [ ];
		var typesFilter = EMF.config.attachTo.typesFilter || [ ];

		var url = EMF.servicePath + '/definition/all-types';
		if (typesFilter && typesFilter.length) {
			var len = typesFilter.length;
			url += '?';
			while(len--) {
				url += 'classFilter=' + typesFilter[len] + '&';
			}
		}
		
		opts.objectTypeOptionsLoader = url;
		opts.labels = {
			headerTitle: _emfLabels['object.picker.header']
		}
		opts.defaultsIfEmpty = true;

		var config = EMF.search.picker.attach.getDefaultAttachConfig(opts);
		var placeholder = config.placeholder;
		config.defaultObjectTypes = objectTypes;
		config.initialSearchArgs.objectType = objectTypes;
		config.initialSearchArgs.filterByWritePermissions = true;
		config.doSearchAfterInit = true;
		config.panelVisibility = {
				browse : false,
				upload : false
		}

		var selectedItems = EMF.search.selectedItems = {};

		var sectionsServicePath = "/caseInstance/sections";
		config.resultItemDecorators = [ EMF.search.picker.caseAndSectionsResultItemsDecorator({
			actionsModule: EMF.search.picker.attach,
			servicePath: EMF.servicePath,
			sectionsServicePath: sectionsServicePath,
			dialogContent: placeholder,
			allowSubmitWithoutSelection: true,
			selectableTypes: null,
			// sections with documents have no purpose
			sectionPurpose: opts.currentInstanceType === 'documentinstance' ? '' : 'objectsSection'
		}) ];

		/**
		 * When OK button is clicked, request object will be build and passed to rest client, which will create
		 * the relation between the current instance(on which the action is executed) and the picked instance.
		 */
		config.dialogConfig.okBtnClicked = function() {
			EMF.blockUI.showAjaxLoader();
			if (selectedItems) {
				var selectedItem = $('.result-item-checkbox:checked');
				var destInstanceId = selectedItem.val();
				var destInstanceType = selectedItem.attr('data-type');

				var requestItem = {
						dbId : opts.currentInstanceId,
						type : opts.currentInstanceType
				}

				var requestData = {
						currentInstanceId   : destInstanceId,
						currentInstanceType : destInstanceType,
						selectedItems       : {item1 : requestItem}
				}
						 
				var client = 'DocumentRestClient';
				if(opts.currentInstanceType === 'objectinstance'){
					client = 'ObjectRestClient';
				}

				REST.getClient(client).attach({data : requestData})
				.done(function(){EMF.search.picker.attach.successfulOperationHandler()})
				.fail(function(data, jqXHR){EMF.search.picker.attach.unsuccessfulOperationHandler(data, jqXHR)});
			}
		}

		placeholder.objectPicker(config);
	},

	/**
	 * Returns default configurations for attach/attach to picker.
	 * - gets EMF.search.config();
	 * - sets visibility of the upload panel to <b>false</b>
	 * - sets config.onBeforeSearch and config.onAfterSearch to show and hide loader
	 * - sets default configuration for browserConfig with passed filters like opts.filter
	 * - in opts can be passed configs, which will be set for the picker configs:
	 * 			     opts		  	   |    picker.config
	 *       - labels.headerTitle 	   -> labels.popupTitle
	 *       - pickerType         	   -> pickerType
	 *       - defaultsIfEmpty    	   -> defaultsIfEmpty
	 *       - defaultObjectTypes 	   -> initialSearchArgs.objectType
	 *       - objectTypeOptionsLoader -> objectTypeOptionsLoader
	 *       - filters				   -> browserConfig.filters
	 *
	 * @param opts
	 * 			object containing additional configurations and information
	 * @return configuration object with default configurations for the attach object picker
	 */
	getDefaultAttachConfig : function(opts){
		 var config			= EMF.search.config();
		 config.placeholder = $('<div></div>');
		 config.popup       = true;
		 config.labels		= {};
		 config.labels.popupTitle = opts.labels.headerTitle;
		 config.pickerType  = opts.pickerType;

		 config.defaultsIfEmpty = opts.defaultsIfEmpty;
		 config.initialSearchArgs.objectType = opts.defaultObjectTypes;

		 if (opts.objectTypeOptionsLoader) {
		    config.objectTypeOptionsLoader = opts.objectTypeOptionsLoader;
		 }
		 config.initialSearchArgs = config.initialSearchArgs || {};
		 config.browserConfig = {
				allowSelection	: true,
				singleSelection	: false,
				filters			: opts.filters
		    };
		 config.panelVisibility = {
		        upload: false
		 };
		 config.dialogConfig = {};

		 // decorator to add target attributes to links in object picker
		 config.resultItemDecorators = {
		 	'decorateLinks' : function(element) {
		 		element.find('a').attr('target', '_blank');
		 		return element;
		 	}
		 };

		 config.onBeforeSearch.push(function() {
		 	EMF.ajaxloader.showLoading(config.placeholder);
		 });

		 config.onAfterSearch.push(function() {
		 	EMF.ajaxloader.hideLoading(config.placeholder);
		 });
		 
		 return config;
	},

	/**
	 * Default function, which is called, when the result from the rest service call for
	 * attach/attachTo operations is successful. What this function does is, remove loader from the page
	 * and reload the current page.
	 */
	successfulOperationHandler : function(){
		 EMF.blockUI.hideAjaxBlocker();
         var location = window.location.href;
         window.location.href = location;
	},

	/**
	 * Default function, which is called, when the result from the rest service call for the some
	 * attach/attachTo operation is unsuccessful. This function opens notification with message, why
	 * the request fails.
	 */
	unsuccessfulOperationHandler : function(data, jqXHR) {
		EMF.blockUI.hideAjaxBlocker();
        EMF.dialog.open({
            title        : ' ' + jqXHR,
            message      : data.responseText,
            notifyOnly   : true,
            customStyle  : 'warn-message',
            close: function() {
            	EMF.blockUI.hideAjaxBlocker();
            },
            confirm: function() {
            	EMF.blockUI.hideAjaxBlocker();
            }
        });
    }
};

/**
 * Handler for add primary image operation.
 *
 * - we should check if selected object has primary image already
 * - if there is one, then we should notify the user because its not allowed
 *   to have more than one primary image for object
 * - if there is no primary image associated to the object, then we proceed and
 *   open object picker to allow the user to search for an image
 */
EMF.search.picker.image = function(opts, evt) {
	if (!opts.instanceId || !opts.instanceType) {
		console.error('Missing required arguments for add thumbnail operation: ', opts);
		return;
	}

	var service = EMF.servicePath + '/thumbnail',
		selectedItems = [],
		requestData = {
			instanceId	: opts.instanceId,
			instanceType: opts.instanceType,
			checkOnly	: true
		};

	// check if the object has primary image or not
    $.ajax({
        url: service,
        data: requestData,
     // fix IE-11 - caching ajax requests
		cache   : false,
		dataType:	'text'
    })
    .done(function(data) {
    	if (data === 'true') {
    		// notify for fail and allow user to try again
    		EMF.dialog.open({
    			title       : '',
    			message     : _emfLabels['cmf.imagepicker.thumbnail.change.warn'],
    			okBtn       : _emfLabels['cmf.btn.confirm'],
    			cancelBtn   : _emfLabels['cmf.btn.no'],
    			customStyle : 'warn-message',
    			width		: 'auto',
    			confirm     : function() {
    				addThumbnail(opts);
    			}
    		});
		} else if(data === 'false') {
			// proceed with add primary image
			addThumbnail(opts);
		}
    })
    .fail(function(data, textStatus, jqXHR) {
    	console.error('Failed to check if instance has thumbnail!');
    });

    // init the image picker
    function addThumbnail(opts) {
        var selectedItem,
        	placeholder = $('<div></div>'),
        	config = EMF.search.config(),
        	data;

        config.pickerType		= 'image';
        config.popup			= true;
        config.labels			= {};
        config.singleSelection	= true;
        config.labels.popupTitle= _emfLabels['cmf.imagepicker.window.title'];
        config.dialogConfig = {};
        config.dialogConfig.okBtnClicked = function() {
            if(selectedItem) {
                var documentId = selectedItem.dbId;
                if (documentId) {
                    EMF.blockUI.showAjaxLoader();
                    data = { documentId	: documentId };
                    data = $.extend({}, data, opts);
                    $.ajax({
                    	type: 'POST',
                    	contentType : EMF.config.contentType.APP_JSON,
                        url	: service,
                        data: JSON.stringify(data)
                    })
                    .done(function(data) {
                        // primary image is added
                        // close picker
                        // reload page
                    	window.location.href = window.location.href;
                    })
                    .fail(function(data, textStatus, jqXHR) {
                        // notify and proceed
                    	EMF.blockUI.hideAjaxBlocker();
                        EMF.dialog.open({
                            title        : ' ' + jqXHR,
                            message      : data.responseText,
                            notifyOnly   : true,
                            customStyle  : 'warn-message',
                            width		 : 'auto'
                        });
                    });
                }
            }
        };

        config.initialSearchArgs = config.initialSearchArgs || { };
        config.initialSearchArgs.mimetype = '^image/';
        config.searchFieldsConfig = {
        	objectType: { disabled: true }
        };
        config.browserConfig = {
       		allowSelection	: true,
        	singleSelection	: true,
        	filters			: CMF.config.objectsBrowserFilters.IMAGES
        };
        config.panelVisibility = {
           	search: true,
           	browse: true,
           	upload: false
        };

        placeholder.on('object-picker.selection-changed', function(event, data) {
            // check if there is only one selected item and enable the OK button
            selectedItems[0] = data.currentSelection;
            selectedItem = data.currentSelection;
            if(selectedItem) {
            	EMF.search.picker.toggleButtons(false);
            }
        }).on('object.tree.selection', function(data) {
        	selectedItems = data.selectedNodes;
        	if (selectedItems.length === 0) {
        		EMF.search.picker.toggleButtons(true);
			} else {
				selectedItem = data.selectedNodes[0];
				EMF.search.picker.toggleButtons(false);
			}
        });

        placeholder.objectPicker(config);

        EMF.search.picker.toggleButtons(true);
    }
};

/**
 * Handler for add primary image operation. One object can have many primary objects
 */
EMF.search.picker.primaryImage = function(opts) {
	if (!opts.instanceId || !opts.instanceType) {
		console.error('Missing required arguments for add primary image operation: ', opts);
		return;
	}

	var service = EMF.servicePath + '/primaryImage',
		selectedItems = [],
		placeholder = $('<div></div>'),
		config = EMF.search.config(),
		data;

	EMF.search.picker.primaryImage.configurations(config);

    config.dialogConfig.okBtnClicked = function() {
    	if(selectedItems) {
	        var images = [];

	        // iterate all selected items and get needed data
	        $.each(selectedItems, function(index, value) {
	        	images.push(value.dbId);
			});

	        if (images) {
	        	EMF.blockUI.showAjaxLoader();
	        	data = { images	: images };
	            data = $.extend({}, data, opts);
	            $.ajax({
	            	type: 'POST',
	            	contentType : EMF.config.contentType.APP_JSON,
	                url	: service,
	                data: JSON.stringify(data)
	            })
	            .done(function(data) {
	               	// primary image is added
	                // close picker
	                // reload page
	            	window.location.href = window.location.href;
	            })
	            .fail(function(data, textStatus, jqXHR) {
	              	// notify and proceed
	                EMF.blockUI.hideAjaxBlocker();
	                EMF.dialog.open({
	                   	title        : ' ' + jqXHR,
	                    message      : data.responseText,
	                    notifyOnly   : true,
	                    customStyle  : 'warn-message',
	                    width		 : 'auto'
	                });
	            });
	       }
        }
    };

    config.searchFieldsConfig = {
    		objectType: { disabled: true }
    };

    placeholder.on('object-picker.selection-changed', function(event, data) {
    	// check if there is selected item and enable the OK button
    	selectedItems = data.selectedItems;

        if(!jQuery.isEmptyObject(selectedItems)) {
        	EMF.search.picker.toggleButtons(false);
        } else {
			EMF.search.picker.toggleButtons(true);
		}
    }).on('object.tree.selection', function(data) {
    	selectedItems = data.selectedNodes;

        if(!jQuery.isEmptyObject(selectedItems)) {
        	EMF.search.picker.toggleButtons(false);
        } else {
			EMF.search.picker.toggleButtons(true);
		}
    }).on('uploaded.file.selection', function(e, data) {
    	selectedItems = data.selectedItems;

        if(!jQuery.isEmptyObject(selectedItems)) {
        	EMF.search.picker.toggleButtons(false);
        } else {
			EMF.search.picker.toggleButtons(true);
		}
    });

    // Add handler for upload
    placeholder.objectPicker(config);

    EMF.search.picker.toggleButtons(true);
};

/**
 * Extracted the setting of the simple configurations for EMF.search.picker.primaryImage.
 */
EMF.search.picker.primaryImage.configurations = function(config){

	config.pickerType		= 'image';
	config.popup			= true;
	config.labels			= {};
	config.singleSelection	= false;
	config.labels.popupTitle= _emfLabels['cmf.imagepicker.window.title'];
	config.dialogConfig = {};

    config.initialSearchArgs = config.initialSearchArgs || { };
    config.initialSearchArgs.mimetype = '^image/';

    config.browserConfig = {
    		allowSelection	: true,
        	singleSelection	: true,
        	filters			: CMF.config.objectsBrowserFilters.IMAGES
    };

    config.uploadConfig	= {
    		allowSelection		: true,
    		restrictFileType	: true
    };

    config.panelVisibility = {
         	search: true,
           	browse: true,
           	upload: true
    };
};

/**
 * @param sectionType
 * 				The purspose of the sections that should be displayed.
 * @param instanceId
 * 				The instance id for which to be loaded sections.
 * @param instanceType
 * 				The instance type for which to be loaded sections.
 * @param dialogHanler
 * 				The dialog handler reference.
 * @param callback
 * 				The callback function to be executed when the user picks a section.
 */
EMF.search.picker.caseSections = function(sectionType, instanceId, instanceType, contextId, contextType, dialogHanler, callback) {
	if (sectionType === undefined || !instanceId || !instanceType) {
		console.error('Missing required arguments for sections loading!');
		return;
	}
	var requestData = {
		purpose		: sectionType,
		instanceId	: instanceId,
		instanceType: instanceType,
		contextId 	: contextId,
		contextType : contextType
	};

	var url = EMF.servicePath + '/caseInstance/sections';
    $.ajax({
        url: url,
        data: requestData
    })
    .done(loadSectionsHandler)
    .fail(failLoadSections);

    /**
     * Build case sections html to be displayed inside a picker dialog.
     */
    function loadSectionsHandler(data) {
    	var caseData = data.caseData;
    	var caseHeader = '<div class="tree-header default_header">';
    		caseHeader += 	'<div class="instance-header caseinstance">';
    		caseHeader +=		'<span class="icon-cell">';
    		caseHeader +=			'<img class="header-icon" src="' + caseData.icon + '" width="24">';
    		caseHeader +=		'</span>';
    		caseHeader +=		'<span class="data-cell">' + caseData.header + '</span>';
    		caseHeader +=	'</div>';
    		caseHeader +='</div>';
    	var tableHtml = EMF.dom.buildCheckTable({
			singleSelection	: true,
			tableClass		: 'table table-hover section-picker headers-with-icons',
			tableId			: 'caseSections',
			data			: data.values,
			selected		: [ instanceId ]
		});

    	dialogHanler.open({
    		message: caseHeader + tableHtml,
    		title: 'Section picker',
    		resizable: true,
    		width: 'auto',
    		height: 'auto',
    		confirm: function() {
    			if(callback && $.isFunction(callback)) {
    				callback(selectedItem);
    			}
    		}
    	});

    	// disable ok button until user makes a selection
    	dialogHanler.toggleButton('okBtn', true);
    	$('.section-picker input').on('change', function(evt) {
    		selectedItem = $(evt.target)[0];
    		if(selectedItem) {
    			dialogHanler.toggleButton('okBtn', false);
    		}
    	});
    };

    /**
     * Handle cases where case or sections can not be loaded successfully.
     */
    function failLoadSections(data, textStatus, jqXHR) {
    	dialogHanler.open({
            title       : ' ' + jqXHR,
            message     : data.responseText,
            customStyle : 'warn-message',
            notifyOnly	: true
        });
    }
}

/**
 *
 */
CMF.resourceAllocation = {
	openPicklist : function(event) {
		$('#resourceAllocationSelectUsersWrapper').trigger({
			type : 'openPicklist',
			itemsList : event.data
		});
	}
};

/**
 * Upload documents in task.
 */
CMF.fileUploadInTask = {
	init: function(currentInstanceId, currentInstanceType, currentPath, event) {
		var picker;
		// get document type from outgoing documents table
		var row = (event.source.id).split(':')[2];
		var column = $('#formId\\:outgoingDocuments').find('.link-column');
		var filesFilter = $($(column)[row]).find('.default-type').html();

		if (!currentInstanceId && !currentInstanceType && !currentPath) {
			console.log('Missing required arguments for file upload init!');
			return;
		}
		// get the target instance data for objects browser
		// we seek CaseInstance
		var browserRootInstanceId,
			browserRootInstanceType;
		var currentPathArray = currentPath;
		for (var i = 0; i < currentPathArray.length; i++) {
			var current = currentPathArray[i];
			if (current.type === 'caseinstance') {
				browserRootInstanceId = current.id;
				browserRootInstanceType = current.type;
				break;
			}
		}
		if (!browserRootInstanceId && !browserRootInstanceType) {
			console.log('Missing required arguments for file upload init!');
			return;
		}

        var placeholder		= $('<div></div>');
        var config			= EMF.search.config();
        config.popup       	= true;
        config.labels		= {
        		cancelBtn	: _emfLabels['cmf.btn.close'],
            	popupTitle	: _emfLabels['cmf.document.upload.new.document.title.label']
        };
        config.uploadConfig	= {
        	targetInstanceId	: browserRootInstanceId,
        	targetInstanceType	: browserRootInstanceType,
    		showBrowser			: true,
    		multipleFiles		: false,
    		caseOnly			: true,
    		filesFilter			: filesFilter,
    		browserConfig		: {
    			changeRoot			: false,
    			rootNodeText		: _emfLabels['cmf.root.node.text'], //Case
    			node				: browserRootInstanceId,
    			type				: browserRootInstanceType,
        		allowSelection		: true,
        		singleSelection		: true,
        		allowRootSelection 	: false,
        		autoExpand			: true,
        		filters				: CMF.config.objectsBrowserFilters.DOCUMENT_SECTIONS
            }
    	};
        config.panelVisibility = {
        	search: false,
        	browse: false,
        	upload: true
        };
        config.defaultTab = 'upload';
        config.dialogConfig = config.dialogConfig || {};

        placeholder.on('file-upload-done', function(evt) {
        	if (evt.uploadedFiles) {
        		var uploadedFiles = evt.uploadedFiles;
                var items = {};
                for(key in uploadedFiles) {
                	if (uploadedFiles.hasOwnProperty(key)) {
                		var currentItem = uploadedFiles[key];
                		var item = {};
                		item.targetId   = currentInstanceId;
                		item.targetType = currentInstanceType;
                		item.destId     = currentItem.dbId;
                		item.destType   = currentItem.type;
                		items[key] = item;
                	}
                }
                var data = {
                	relType         : 'emf:outgoingDocuments',
                	reverseRelType	: 'emf:outgoingDocuments',
                	system			: true,
                	selectedItems   : items
                };
			}

            $.ajax({
                contentType : "application/json",
                data        : JSON.stringify(data),
                type        : 'POST',
                url         : SF.config.contextPath + '/service/relations/create'
            }).done(function() {
            	$('.update-model-button').click();
            }).fail(function() {
		        EMF.dialog.notify({
		            title       : _emfLabels['notification.level.error'],
		            message     : _emfLabels['cmf.workflow.task.upload.document.error.cannot_link'],
		            customStyle : 'warn-message'
		        });
            });
        });

        picker = placeholder.objectPicker(config);

        // hide the OK button from the footer
        $('.ui-dialog-buttonset').children().eq(0).remove();
	}
};

/**
 * Handler for upload documents from main menu (Upload button next to quick search). Initializing the upload form and
 * some configurations needed in order the form to work properly without predefined target(parent instance).
 * The target instance is calculated later, when the definition type of the document is selected.
 */
CMF.fileUploadMainMenu = {

		/**
		 * Initializes additional configurations and builds the upload form. Uses the CMF.fileUploadNew.init.
		 */
		init : function(){
			var configs = {
				withoutInitParent  : true,
				docTypesServiceUrl : EMF.servicePath + '/upload/all-types',
				multipleFiles      : false,
				getLocations	   : CMF.fileUploadMainMenu.extractLocations,
				onCloseHandler	   : CMF.fileUploadMainMenu.onCloseHandler
			};
			CMF.fileUploadNew.init('','', configs);
			setTimeout(function(){$('#startUpload').remove();},1000);
		},

		/**
		 * Handles the extraction of default location for the document that will be upload.
		 * Calls rest service, which should return the information, if there is a default location for the selected
		 * definition type, proper header is build and passed to the upload plugin, if there isn't default location
		 * and there are more possible target they are passed to the plugin, where it builds select menu of them.
		 *
		 * @param definitionId
		 * 				the definition id of the document, which will be uploaded.
		 * @param element
		 * 				element with will be passed to the callback function
		 * @param callback
		 * 				function which will be executed after the rest request is done
		 */
		extractLocations : function(definitionId, element, callback){
			var result = [];
			REST.getClient('DefaultLocationRestClient').getLocations({definitionId : definitionId})
			.done(function(data){
				data.locations.forEach(function(index){
					var menuData = { id : index.instanceId,
							         text : index.header,
							         type : index.instanceType,
							         isDefaultLocation : index.isDefaultLocation
							       };
					result.push(menuData);
				});

				callback(result, element);
			});
		},

		/**
		 * Redirects the user to the currently uploaded file.
		 */
		onCloseHandler : function(){
			if(EMF.uploadedDocument){
				window.location.href =
	        		EMF.bookmarks.buildLink(EMF.documentContext.currentInstanceType, EMF.documentContext.currentInstanceId);
			}
		}

};

/**
 * Initializes the file upload for case section upload.
 */
CMF.fileUploadNew = {

	/**
	 * @param targetInstanceId
	 * 			The identifier of the instance to which the uploaded file should be attached.
	 * @param targetInstanceType
	 * 			The type of the instance to which the uploaded file should be attached.
	 * @param additionalUploadConfigs
	 * 			object containing additional configurations, which are passed to the upload plugin.
	 * 			Mainly used to setup and prepare the upload form to work without initial target instance.
	 * 			This properties are merged with the default upload configurations and could override them.
	 */
	init: function(targetInstanceId, targetInstanceType, additionalUploadConfigs) {
		if(!targetInstanceId && !targetInstanceType && !additionalUploadConfigs.withoutInitParent) {
			console.error('Missing required attributes to initiate file upload!');
			return;
		}
        var selectedItem;
        var selectedItems	= [];
        var placeholder		= $('<div></div>');
        var config			= EMF.search.config();
        config.popup       	= true;
        config.labels		= {
        	cancelBtn	: _emfLabels['cmf.btn.upload.close'],
        	popupTitle	: _emfLabels['cmf.document.upload.new.document.title.label']
        };
        config.uploadConfig	= {
        	targetInstanceId	: targetInstanceId,
        	targetInstanceType	: targetInstanceType,
    		showBrowser			: false,
    		allowSelection		: false,
    		browserConfig		: {
        		allowSelection		: true,
        		singleSelection		: true,
        		allowRootSelection 	: false,
        		filters				: CMF.config.objectsBrowserFilters.SECTIONS
            }
    	};
        config.uploadConfig = $.extend(true, {}, config.uploadConfig, additionalUploadConfigs);
        config.panelVisibility = {
        	search: false,
        	browse: false,
        	upload: true
        };
        config.defaultTab = 'upload';
        // when the panel is closed manualy, we should reload the page in order to show actual data
        config.dialogConfig = config.dialogConfig || {};

        config.dialogConfig.oncloseHandler = CMF.fileUploadNew.defaultOnCloseHandler;
        if (additionalUploadConfigs.withoutInitParent) {
        	config.dialogConfig.oncloseHandler = additionalUploadConfigs.onCloseHandler;
        }

        placeholder.objectPicker(config);
        // hide the panel close button from header
        $(".ui-dialog-titlebar-close").hide();
        // hide the OK button from the footer
        $('.ui-dialog-buttonset').children().eq(0).remove();
	},

	/**
	 * Default on close handler for the upload form. This is set, when there are no additional configuration about it.
	 */
	defaultOnCloseHandler : function(){
       	// case tab should be opened only if upload is done in case. For project redirect to the project
    	if(EMF.documentContext.currentInstanceType == 'caseinstance') {
        	window.location.href =
        		EMF.bookmarks.buildLink(EMF.documentContext.currentInstanceType, EMF.documentContext.currentInstanceId, EMF.bookmarks.caseTab.documents);
    	} else {
        	window.location.href =
        		EMF.bookmarks.buildLink(EMF.documentContext.currentInstanceType, EMF.documentContext.currentInstanceId);
    	}
	}
};

/**
 * Initializes the file upload for new version of document.
 */
CMF.fileUploadNewVersion = {

	/**
	 * @param targetInstanceId
	 * 			The identifier of the instance to which the uploaded file should be attached.
	 * @param targetInstanceType
	 * 			The type of the instance to which the uploaded file should be attached.
	 */
	init: function(targetInstanceId, targetInstanceType) {
		if(!targetInstanceId && !targetInstanceType) {
			console.error('Missing required attributes to initiate file upload!');
			return;
		}
        var selectedItem;
        var selectedItems	= [];
        var placeholder		= $('<div></div>');
        var config			= EMF.search.config();
        config.popup       	= true;
        config.labels		= {
        	cancelBtn				: _emfLabels['cmf.btn.close'],
        	popupTitle				: _emfLabels['cmf.document.upload.new.version.document.title.label']
        };
        config.uploadConfig	= {
        	emfServicePath		: EMF.servicePath + '/upload/upload-new-version',
			uploadTemplateUrl	: EMF.applicationPath + '/upload/upload-new-version.tpl.html',
			fileTableTemplate	: '<tr class="template-upload" id="row_0">'
		       + '<td>'
		       +     '<span class="preview"></span>'
		       + '</td>'
		       + '<td style="max-width:420px;">'
		       +     '<div class="col-xs-16 col-sm-14 mandatory-field-column"><label class="fileNameLabel"></label><span class="name" style="width:420px; display:inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; vertical-align: center;"></span></div> '
		       +     '<div class="col-xs-16 col-sm-14 mandatory-field-column"><label>'+ window._emfLabels["cmf.document.upload.new.document.versionType.label"] +'</label><br><input id="minorVersion" type="radio" checked="checked" name="isMajorVersion" value="false" /><label for="minorVersion" style="font-weight:normal"> ' + window._emfLabels["cmf.document.upload.new.document.versionType.minor"] + '</label> <input id="majorVersion" type="radio" name="isMajorVersion" value="true" /><label for="majorVersion" style="font-weight:normal"> ' + window._emfLabels["cmf.document.upload.new.document.versionType.major"] + '</label></div>'
		       +	 '<p><div class="row"><div class="col-xs-12 col-sm-14 mandatory-field-column required-field"><label>'+ window._emfLabels["cmf.document.upload.document.description.label"] +'</label><input class="form-control mandatory-field-size placeholder-fix" type="text" id="description" style="width:420px" placeholder="' + window._emfLabels["upload.document.description"] + '"></div></div></p>'
		       +	 '<p><div id="mandatory-field-placeholder-0"></div></p>'
		       + '</td>'
		       + '<td style="min-width:100px; padding-left:25px;">'
		       +     '<label class="size"></label>'
		       +    	'<div class="fileupload-progress" style="min-width:100px"><div class="progress progress-striped active" ><div class="progress-bar" style="width:0%; font-weight:bold;"></div></div></div>'
		       + '</td>'
		       + '<td style="min-width:205px; padding-top:20px">'
		       +       '<button class="btn btn-default cancel" id="0">'
		       +             '<em class="glyphicon glyphicon-ban-circle"></em> '
		       +             '<span>Upload</span>'
		       +         '</button>'
		       +         '<button class="btn btn-default remove">'
		       +             '<em class="glyphicon glyphicon-ban-circle"></em> '
		       +             '<span>Remove</span>'
		       +         '</button>'
		       + '</td>'
		       + '</tr>',
        	targetInstanceId	: targetInstanceId,
        	targetInstanceType	: targetInstanceType,
    		showBrowser			: false,
    		allowSelection		: false,
			multipleFiles		: false,
    		browserConfig		: {
        		allowSelection		: true,
        		singleSelection		: true,
        		allowRootSelection 	: false,
        		filters				: CMF.config.objectsBrowserFilters.SECTIONS
            }
        };
        config.panelVisibility = {
        	search: false,
        	browse: false,
        	upload: true
        };
        config.defaultTab = 'upload';
        // when the panel is closed manualy, we should reload the page in order to show actual data
        config.dialogConfig = config.dialogConfig || {};
        config.dialogConfig.oncloseHandler = function() {
        	location.reload();
        };

        placeholder.objectPicker(config);
        // hide the panel close button from header
        $(".ui-dialog-titlebar-close").hide();
        // hide the OK button from the footer
        $('.ui-dialog-buttonset').children().eq(0).remove();
	}
};

/**
 * Instance actions handlers. Just actions that are executed onclick/oncomplete triggered by action
 * buttons (from the action menu) should go here.
 */
CMF.action = {

	init: function() {
		$(document).on('action_event', function(evt) {
			if (evt && evt.actionEvent) {
				CMF.action.event[evt.actionEvent](evt);
			}
		});
	},

	getServicePath: function(servicePathConstant) {
		var path = EMF.config.servicePaths[servicePathConstant];
		if (!path) {
			console.error('Requested service path [' + servicePathConstant + '] is not defined as a constant. Check EMF.config.servicePaths!');
			return;
		}
		return EMF.servicePath + path;
	},

	enablePrimaryButton: function(enable) {
		var primaryButton = $(".ui-dialog-buttonpane button[priority=primary]");
		if (enable) {
			primaryButton.attr("disabled", false).removeClass("ui-state-disabled");
		} else {
			primaryButton.attr("disabled", true).addClass("ui-state-disabled");
		}
	},

	confirmation: function(opts) {
		if(!opts.message || !opts.confirmAction) {
			return;
		}
		var title = opts.title || ' ';
		var customStyle = opts.customStyle || 'warn-message';
		var closeAction = opts.closeAction || function() {
        	return false;
        };
        EMF.dialog.confirm({
            title       : title,
            message     : opts.message,
            customStyle	: customStyle,
            close		: closeAction,
            confirm		: opts.confirmAction,
            height		: 'auto'
        });
	},

	operationFail: function(data, textStatus, jqXHR) {
		EMF.blockUI.hideAjaxBlocker();
		EMF.dialog.open({
			title        : ' ' + jqXHR,
			message      : data.responseText,
			notifyOnly   : true,
			customStyle  : 'warn-message',
			width		 : 'auto'
		});
	},

	/**
	 * Open dialog where user to select a document type from allowed children list.
	 * When user confirms selection then selected definition type and description are
	 * populated in provided opts object and resulting json is set in a hidden field bound
	 * to the NewDocumentAction bean. Then a hidden action link is triggered to execute
	 * new document create process realized in NewDocumentAction.createNewDocument action.
	 */
	createDocument: function(evt, opts) {
		var selectedDefinitionType,
	    _dialogView = '';
	    _dialogView += '<div id="createIdocDialog" class="form-group">';
	    _dialogView += '<label>' + _emfLabels['cmf.document.create.type'] + '</label>';
        _dialogView += '<select id="instanceAllowedChildrens" class="form-control" data-placeholder="' + _emfLabels['cmf.document.upload.fileType.selector.label'] + '" />';
        _dialogView += '<label>' + _emfLabels['cmf.document.create.description'] + '</label>';
        _dialogView += '<textarea id="fileDescription" class="form-control"></textarea>';
        _dialogView += '<p id="mandatory-field-placeholder" ></p>';
        _dialogView += '</div>';
	    EMF.dialog.open({
	        title            : _emfLabels['cmf.document.create.popup.title'],
	        content     	 : _dialogView,
	        width			 : 'auto',
	        height			 : 'auto',
	        closeOnMaskClick : false,
	        open: function(event) {
	        	var promise = REST.getClient('UploadRestClient').retrieveAllowedTypes({
	        		pathParams: [opts.targetInstanceType, opts.targetInstanceId],
	        		queryParams: {filterByPurpose: opts.targetInstanceType === 'sectioninstance'}
	        	});
	        	promise.done(function(data) {
	        		var children = data,
	        			len = children.length,
	        			options = '<option></option>';
	        		for (var i = 0; i < len; i++) {
	        			var current = children[i];
						options += '<option value="' + current.value + '">' + current.label + '</option>';
					}
	        		var typeSelector = $(event.target).find('#instanceAllowedChildrens');
	        		var mandatoryFields;
	        		var element = $('#mandatory-field-placeholder');
	        		element.on('after-fields-loaded', function(){
	                	var wrapper = element.children().first();
	                	var hasMandatory = wrapper.children().length > 0;
			    		EMF.dialog.toggleButton('btnSkip', !hasMandatory);
	                });
    	        	element.on('data-is-valid', function(eventData) {
    	        		createObject(eventData.fields);
    	        	});
	        		typeSelector.append(options);
	        		typeSelector.select2({
	        			minimumResultsForSearch: EMF.config.comboAutocompleteMinimumItems,
	        			allowClear: false,
						formatResultCssClass: function(object) {
							return object.id;
						}
	        		})
	        		.on('change', function(evt) {
	        			selectedDefinitionType = evt.val;
		    			if(element.children().length > 0) {
		    				element.empty();
		    			}
			    		if(selectedDefinitionType) {
			    			mandatoryFields = element.buildFields({
			    	    		definitionId			: selectedDefinitionType,
			    	    		currentInstanceType		: opts.instanceType,
			    	    		parentInstanceId		: opts.targetInstanceId,
			    	    		parentInstanceType		: opts.targetInstanceType,
			    	    		skipBtn					: $('#btnSkip'),
			    	    		// restrict fields by attributes
			    	    		restrictByAttibutes : {
			    					displayType 		: ['editable'],
			    				    names		  		: ['name','title']
				        		},
				        		// date-time picker settings
				        		datePickerSettings  : {
			        				changeMonth 		: true,
			        				dateFormat 			: SF.config.dateFormatPattern,
			        				firstDay 			: SF.config.firstDay,
			        				monthNames 			: SF.config.monthNames,
			        				monthNamesShort 	: SF.config.monthNamesShort,
			        				dayNames 			: SF.config.dayNames,
			        				dayNamesMin 		: SF.config.dayNamesMin,
			        				numberOfMonths 		: 1
			        			},
			        			// used services
				        		services			: {
			    	    			definitionService	: REST.getClient('DefinitionRestClient')
			    	    		},
			    	    		applicationPath			: EMF.applicationPath,
			    	    		proceedButtonLocators	: [".ui-dialog button[priority='primary']"]
			    	    	});
	        			}
	        			EMF.dialog.toggleButton('okBtn', $.trim(selectedDefinitionType) === '');
	        		});
	        	});
	        },
	        confirm: function() {
	        	var element = $('#mandatory-field-placeholder');
	        	element.trigger({ type: 'validate-mandatory-fields' });
	        	return false;
	        },
	        buttons: [{
					text 	: _emfLabels['cmf.btn.skip'],
					id		: 'btnSkip',
					priority: 'secondary',
					click : function() {
						createObject();
						return false;
					}
				}]
	    });

	    function createObject(fieldsValues) {
	    	var newDocumentDataField = $('[id$=newDocumentData]')[0];
        	var executeCreateDocumentLink = $('[id$=executeCreateDocument]')[0];
        	var documentData = opts;
        	documentData.selectedDefinitionType = selectedDefinitionType;
        	documentData.description = $('#createIdocDialog #fileDescription').val();
        	documentData.fieldsValues = fieldsValues;
        	newDocumentDataField.value = JSON.stringify(documentData);
        	executeCreateDocumentLink.click();
	    }
	    EMF.dialog.toggleButton('btnSkip', true);
	    EMF.dialog.toggleButton('okBtn', true);
	},

	/**
	 * Publish action handler is executed by onclick on publish action button. Calls a rest
	 * service to execute the operation and displays the result returned from the service.
	 * The result can contain a link of the created revision.
	 *
	 * @param evt Is the onclick event.
	 * @param opts Configuration options
	 * {
	 * 	id: 'currentInstanceId',
	 * 	type: 'currentInstanceType',
	 *  content: '<div>test</div>' <- optional
	 * }
	 */
	publish: function(evt, opts) {
		if(EMF.config.debugEnabled) {
			console.log('CMF.action.publish arguments: ', arguments);
		}
		EMF.blockUI.showAjaxLoader();
		$.ajax({
			type		: 'POST',
			url			: CMF.action.getServicePath('INSTANCE_REVISIONS'),
			contentType : 'application/json',
			data		: JSON.stringify(opts)
		})
		.done(function(data) {
			window.location.reload();
		})
		.fail(CMF.action.operationFail);
	},

	/**
	 * Get/check if given task instance has subtasks.
	 */
	completeTask: function(evt, opts) {
		if(EMF.config.debugEnabled) {
			console.log('Get subtasks: ', opts);
		}
		if (!opts.instanceId || !opts.instanceType || !opts.checkonly) {
			console.error('Missing required arguments for get subtasks operation: ', arguments);
		}
		var actionTarget = $(evt.target);
		var service = CMF.action.getServicePath('TASK_SUBTASKS');
		var data = {
			instanceId	: opts.instanceId,
			instanceType: opts.instanceType,
			checkonly	: opts.checkonly
		};
		$.ajax({
			type		: 'GET',
			url			: service,
			data		: data
		})
		.done(function(data) {
			if (data && (data.skip || !data.hasSubtasks)) {
				executeTransition();
			} else {
				CMF.action.confirmation({
					message      : opts.confirmationMessage || _emfLabels['cmf.task.confirm_complete'],
					confirmAction: executeTransition
				});
			}
		})
		.fail(CMF.action.operationFail);

		function executeTransition() {
			EMF.blockUI.showAjaxLoader();
			actionTarget.next('input').click();
		}
	},

	/**
	 * Handles detach operation. Gets parameters from action-button-template implementations. First builds confirm message,
	 * if the notification is confirmed, POST request is build with the needed parameters and InstnaceRestService.detach
	 * service is called to perform the operation. For now the operation is performed on object and document instances.
	 * @param evt
	 * 			the event
	 * @param opts
	 * 			the parameters passed through the template of the button
	 */
	detach: function(evt, opts) {
		if (!opts.attachedInstanceId || !opts.attachedInstanceType || !opts.targetId || !opts.targetType || !opts.operationId) {
	        console.error('Missing required attributes for document detach operation:', arguments);
		}
		// because this function is called onclick, we should prevent the default action and handle submit manually
		if(evt) {
			evt.preventDefault();
		}

		var confirmTitle = _emfLabels['cmf.action.' + opts.operationId];
		confirmTitle = opts.confirmation+' '+confirmTitle + ' ?';

		CMF.action.confirmation({
			message      : confirmTitle,
			confirmAction: executeDetach
		});

		function executeDetach() {
			EMF.blockUI.showAjaxLoader();
			var service = CMF.action.getServicePath('INSTANCE_DETACH');
			var data = {
					targetId	: opts.targetId,
					targetType	: opts.targetType,
					operationId	: opts.operationId,
					linked: [{
						instanceId	: opts.attachedInstanceId,
						instanceType: opts.attachedInstanceType
					}]
			};

			$.ajax({
				type		: 'POST',
				contentType : "application/json",
				url			: service,
				data		: JSON.stringify(data)
			})
			.done(function(data) {
				var newLocation = window.location.href;
				// if a document\object landing page is opened, then we should redirect to case documents\objects tab instead of reload
				if ((EMF.documentContext.currentInstanceType === 'documentinstance') || (EMF.documentContext.currentInstanceType === 'objectinstance')) {
					newLocation = EMF.bookmarks.buildLink(opts.targetType, opts.targetId, null);
				}
				window.location.href = newLocation;
			})
			.fail(CMF.action.operationFail);
			return true;
		}
		return false;
	},

	/**
	 * Removes all instances marked for download from the current user downloads list. This function is called
	 * when the action for remove all from downloads is selected, from my documents dashlet action menu.
	 * <p />
	 * First the confirmation message is build, if the user confirms it, the remove operation is executed. The
	 * operation represents REST service call, using DownloadsRestClient. If the service is executed successfully
	 * all the icons, which are visible in the page, are updated (unmarked icon is set).
	 */
	removeAllFromDownload : function(){

		var confirmTitle = _emfLabels['cmf.action.remove.downloads.confirm.msg'];
		CMF.action.confirmation({
			message      : confirmTitle,
			confirmAction: executeRemoveAllFromDownloads
		});

		function executeRemoveAllFromDownloads() {

			var iconsData = {
					add	   : 'roundabout roundabout-flash',
					remove : 'circle_arrow_down downloads-list',
					hint   : 'downloads.add.hint'
				};
			//show loading icon
			EMF.headersIconsUtil.updateAllIcons(iconsData);

			REST.getClient('DownloadsRestClient').removeAll({type : 'DELETE'})
			.done(function() {
				var iconsData = {
						add	   : 'download downloads-list',
						remove : 'roundabout roundabout-flash',
						hint   : 'downloads.add.hint'
					};
				// remove 'marked for download' icon & refreshes the dashlet
				EMF.headersIconsUtil.updateAllIcons(iconsData);
				$('.documents-panel .glyphicon-refresh').click();
	        }).fail(function() {
	        	var iconsData = {
						remove : 'roundabout roundabout-flash',
						add    : 'circle_arrow_down downloads-list',
						hint   : 'downloads.remove.hint'
					};
	        	// shows back 'marked for download' icon
	        	EMF.headersIconsUtil.updateAllIcons(iconsData);
	        });
		}
	},

	/**
	 * This method is called, when download as zip action is clicked. It will create archive and when it is done,
	 * the archive will be passed to the browser through link, which will trigger the download functions of the
	 * browser.
	 */
	downloadAsZip : function(){
		$.when(CMF.downloads.createZip()).done(function(data){
			var noteRef = data.nodeRef;
			var zipId = noteRef.substring(noteRef.lastIndexOf('/'), noteRef.lenght);
			var requestInterval = setInterval(function(){CMF.downloads.getZipStatus(zipId)}, 2500);
			CMF.downloads.buildProgressBarDialog(requestInterval, zipId);
			CMF.downloads.getZipStatus(zipId);
		});
	},

	/**
	 * Managing print operation for concrete object.
	 *
	 * @param type object type
	 * @param id object identifier
	 */
	executePrint : function(type, id){
		var instanceLink = EMF.bookmarks.buildPrintLink(type, id);
		IDocPrint.print.executePrint(instanceLink);
	},

	/**
	 * Managing export operation for concrete object.
	 *
	 * @param type object type
	 * @param id object identifier
	 */
	executeExport : function(type, id){
		EMF.blockUI.showAjaxLoader();

		// help data for current method
		var dataBOX = {
			pdfSuffix : '.pdf',
			exportParam : '/export?fileName='
		};

		// generate instance link
		var instanceLink =	EMF.bookmarks.buildPrintLink(type, id);
		// file name for current exported document
		var fileName = id + dataBOX.pdfSuffix;
		$.ajax({
			type: 'POST',
			url : EMF.servicePath + dataBOX.exportParam + fileName,
			contentType: 'application/json',
			processData: false,
			data: instanceLink
		}).done(function(data) {
			var ifrm = document.createElement("IFRAME");
			ifrm.setAttribute("src", EMF.servicePath + '/export/' + data.fileName);
			ifrm.style.display = "block";
			$("body").append(ifrm);
			EMF.blockUI.hideAjaxBlocker();
		}).fail(function() {
			if(EMF.notify.config.debugEnabled) {
				console.log('Error on exporting object on server.');
			}
			// if error just hide the blocker
			EMF.blockUI.hideAjaxBlocker();
		});
	},

	/**
	 * - Call service to fetch case with its sections
	 * - Build check table
	 * - Open dialog with the table for the user to pick a target instance section
	 * - On accept, send selected section and instance data to rest service which should move the instance
	 * - After rest call completes, build navigation link and refresh the page
	 *
	 * @param instanceId Selected instance id.
	 * @param instanceType Selected instance type.
	 * @param contextId The context instance id.
	 * @param contextType The context instance type.
	 * @param sourceId Source instance (from where to move) id.
	 * @param sourceType Source instance (from where to move) type.
	 */
	moveInstance: function(opts, event) {
		if(EMF.config.debugEnabled) {
			console.log('Move instance arguments: ', opts);
		}
		if (!opts.instanceId || !opts.instanceType) {
			console.warn('Missing instanceId or instanceType arguments for instance move operation: ', arguments);
			return;
		}
		if (opts.sourceType && opts.sourceType !== 'sectioninstance') {
			console.warn('We doesn\'t support object/document move in context different than section instance for now!');
			return;
		}
		if (opts.instanceType !== 'objectinstance' && opts.instanceType !== 'documentinstance') {
			console.warn('This function [moveInstance] doesn\'t support instances different than objectinstance and documentinstance!');
			return;
		}

		// document sections have no purpose
		var sectionType = '';
		if (opts.instanceType === 'objectinstance') {
			sectionType = 'objectsSection';
		}
		var instanceId	= (opts.sourceId ? opts.sourceId : opts.instanceId);
		var instanceType = (opts.sourceType ? opts.sourceType : opts.instanceType);
		var contextId =  opts.contextId;
		var contextType = opts.contextType;
		EMF.search.picker.caseSections(sectionType, instanceId, instanceType, contextId, contextType, EMF.dialog, moveInstance);

	    /**
	     * Handle instance move.
	     */
	    function moveInstance(selectedItem) {
	    	if (selectedItem.value) {
	    		EMF.blockUI.showAjaxLoader();
	    		var targetId = selectedItem.value;
	    		var targetType = 'sectioninstance';
	    		var data = {
	    			instanceId	: opts.instanceId,
	    			instanceType: opts.instanceType,
	    			contextId	: opts.contextId,
	    			contextType	: opts.contextType,
	    			sourceId	: opts.sourceId,
	    			sourceType	: opts.sourceType,
	    			targetId	: targetId,
	    			targetType	: targetType
	    		};

	        	REST.getClient('InstanceRestClient').moveInstance(data)
	        		.then(function() {
	        			window.location.reload();
			    }, function(data, textStatus, jqXHR) {
			    	EMF.blockUI.hideAjaxBlocker();
			        EMF.dialog.open({
			            title       : ' ' + jqXHR,
			            message     : data.responseText,
			            notifyOnly	: true,
			            customStyle : 'warn-message'
			        });
			    });
	    	}
	    }
	},
	
	/**
	 * Action handler for 'AttachTo' operation. It is called, when Attach To action is executed.
	 *
	 * @param event
	 * 				the event
	 * @param currentInstanceId
	 * 				the id of the instance on which the action is executed
	 * @param currentInstanceType
	 * 				the type of the instance on which the action is executed
	 * @param operationId
	 * 				the id of the action(in this case 'attachTo')
	 */
	attachTo: function(currentInstanceId, currentInstanceType, operationId, event){
		if(event){
			event.preventDefault();
		}

		var opts = {
			currentInstanceId  : currentInstanceId,
			currentInstanceType: currentInstanceType,
			operationId		   : operationId
		}
		EMF.search.picker.attach.attachToOperation(opts);
	}
};

/**
 * Document related functions.
 */
CMF.document = {
	/**
	 * Extract document version path by version number.
	 */
	downloadDocumentVersion : function(id, version, cmfActions){
		if(EMF.config.debugEnabled) {
			console.log('CMF.document.downloadDocumentVersion arguments: ', arguments);
		}
		var urlPath = CMF.action.getServicePath('DOCUMENT_DOWNLOAD_VERSION');
		// required data
		var data = {
			instanceId  : id,
			version 	: version
		};
		return $.ajax({
			type		: 'GET',
			url			: urlPath,
			data		: data
		}).fail(cmfActions.operationFail);
	},
	/**
	 * Retrieve document share link based on instance identifier and instance type.
	 */
	loadShareLink : function(cmfActions) {
		if(EMF.config.debugEnabled) {
			console.log('CMF.document.loadShareLink arguments: ', arguments);
		}
		var urlPath = CMF.action.getServicePath('DOCUMENT_SHARE_LINK');
		// required data
		var data = {
			instanceId   : EMF.documentContext.currentInstanceId,
			instanceType : EMF.documentContext.currentInstanceType
		};
		return $.ajax({
			type		: 'GET',
			url			: urlPath,
			data		: data
		}).fail(cmfActions.operationFail);
	},

	/**
	 * Retrieve all document version histories based on instance identifier and instance type.
	 */
	loadDocumentVersionHistory : function(cmfActions){
		if(EMF.config.debugEnabled) {
			console.log('CMF.document.loadDocumentVersionHistory arguments: ', arguments);
		}
		var urlPath = CMF.action.getServicePath('DOCUMENT_LOAD_HISTORY');
		// required data
		var data = {
			instanceId   : EMF.documentContext.currentInstanceId,
			instanceType : EMF.documentContext.currentInstanceType
		};
		return $.ajax({
			type		: 'GET',
			url			: urlPath,
			data		: data
		}).fail(cmfActions.operationFail);
	},

	/**
	 * Retrieve document revisions based on instance identifier and instance type.
	 */
	loadRevisions : function(cmfActions){
		if(EMF.config.debugEnabled) {
			console.log('CMF.document.loadRevisions arguments: ', arguments);
		}
		var urlPath = CMF.action.getServicePath('INSTANCE_REVISIONS');
		// required data
		var data = {
			instanceId   : EMF.documentContext.currentInstanceId,
			instanceType : EMF.documentContext.currentInstanceType
		};
		return $.ajax({
			type		: 'GET',
			url			: urlPath,
			data		: data
		}).fail(cmfActions.operationFail);
	}
};

CMF.downloads = {

		/**
		 * Calls rest service, which creates archive with documents marked for download from the current user.
		 *
		 * @return service response
		 */
		createZip : function(){
			return REST.getClient('DownloadsRestClient').createArchive();
		},

		/**
		 * Calls rest service, which returns the state of given archive. Depending on service response and archive
		 * state, the method behavior will be different. If the service response is 200(OK):
		 * <p>
		 * - if archive state is "MAX_CONTENT_SIZE_EXCEEDED", in the progress dialog will appear message, that the
		 * content is too large to be archived. <br />
		 * - if the archive state is "PENDING" or "IN_PROGRESS", the progress bar in the dialog will be updated. The
		 * information for the progress will be get from the response object properties 'filesAdded' and 'totalFiles'.
		 *  <br />
		 * - if the archive state is "DONE", the progress bar in the dialog will be updated to 100% and will be
		 * call rest service, which will return the archive. Also the dialog will be closed.
		 *
		 * @param zipId
		 * 			 the DMS id of the created archive
		 */
		getZipStatus : function(zipId){
			REST.getClient('DownloadsRestClient').getArchiveStatus({zipId:zipId})
			.done(function(data){
				if(data.status === 'DONE'){
					CMF.downloads.updateProgressBar(data.filesAdded, data.totalFiles);
					CMF.downloads.getArchive(zipId);
				} else if(data.status === 'MAX_CONTENT_SIZE_EXCEEDED'){
					CMF.downloads.largeContentErrorMessage();
				} else {
					CMF.downloads.updateProgressBar(data.filesAdded, data.totalFiles);
				}
			}).fail(CMF.action.operationFail);
		},

		/**
		 * Calls rest service, which builds URL to the completed archive. If the service response 200(OK)
		 * the link will be opened, which will trigger the browser download function. After short delay the
		 * progress dialog will be closed.
		 *
		 * @param zipId
		 * 			 the DMS id of the archive
		 */
		getArchive : function(zipId){
			REST.getClient('DownloadsRestClient').getArchive({zipId:zipId})
			.done(function(data){
				CMF.utilityFunctions.downloadDocumentAfterEvent(data.link[0]);
				setTimeout(function(){$('.progress-dialog-btn').click()}, 1000);
			}).fail(CMF.action.operationFail);
		},

		/**
		 * Calls rest service which cleans up the DMS node, which contains the archive.
		 *
		 * @param zipId
		 * 			 the DMS id of the archive
		 */
		removeArchive : function(zipId){
			REST.getClient('DownloadsRestClient').removeArchive({zipId:zipId});
		},

		/**
		 * Builds progress dialog, which shows the progress of archivation, when download as zip action is selected.
		 * The dialog contains label and progress bar. The button is used to cancel the operation and to clear the
		 * interval, in which the request for archive progress is made. The button also closes the dialog and calls
		 * the service, which removes the archive from the DMS.
		 *
		 * @param requestInterval
		 * 				the interval in which the request is made for archive status and the progress is updated
		 * @param zipId
		 * 			 	the DMS id of the archive
		 */
		buildProgressBarDialog : function(requestInterval, zipId){
			var dialogConfig = EMF.dialog.config;
			dialogConfig.content = '<div><label class="downloads-progress-bar-label" for"downloads-progress-bar-container"></label>' +
								   '<div id="downloads-progress-bar-container" class="downloads-progress-bar-container">' +
								   '<div class="downloads-progress-bar"></div></div></div>';
			dialogConfig.buttons = [];
			dialogConfig.buttons.push({
	        	id: 'cancelBtn',
	            text    : dialogConfig.cancelBtn,
	            priority: 'secondary',
	            'class'	: 'progress-dialog-btn',
	            click   : function() {
	            	$(this).dialog('close');
	            	EMF.dialog.reset(EMF.dialog.config);
	            	clearInterval(requestInterval);
	            	CMF.downloads.removeArchive(zipId);
	            }
	        });
			dialogConfig.closeOnMaskClick = false;
			EMF.dialog.popup(dialogConfig);
		},

		/**
		 * Updates the progress bar and the label in progress dialog, when download as zip action is selected.
		 * Finds the elements and updates them, with the new values, which are passed to the function.
		 *
		 * @param ready
		 * 			 shows how many files are archived already
		 * @param whole
		 * 			 shows the total count of the files, which will be archived
		 */
		updateProgressBar : function(ready, whole){
			var label = EMF.util.formatString(_emfLabels['downloads.progress.dialog.label'], ready, whole);
			$('.downloads-progress-bar-label')[0].innerHTML = label;
			var element = $('.downloads-progress-bar-container');
			var procent = Math.round((ready/whole) * 100);
			var progress = procent * element.width() / 100;
			element.find('.downloads-progress-bar').animate({ width: progress }, 500).html(procent + "% ");
		},

		/**
		 * Builds label, which is shown in to progress dialog, when the content of the archive is too large.
		 */
		largeContentErrorMessage : function(){
			var errorMsg = _emfLabels['downloads.progress.dialog.error.label'];
			$('.downloads-progress-bar-container')[0].parentElement.interHTML =
				'<label class="downloads-progress-bar-error">'
				+ errorMsg + '</label>';
		}
};

/**
 *
 */
CMF.layoutUtils = {
	defaults: {
		defaultMainMenuHeight: 67,
		defaultHeaderHeight: 71
	},

	init: function(opts) {
		_this = CMF.layoutUtils;
		_this.config = $.extend(true, {}, _this.defaults, opts);
		_this.config.calculatedMainMenuHeight = $('#topHeader').outerHeight();
		_this.config.calculatedHeaderHeight = $('#header').outerHeight();
		_this.config.totalPageHeaderHeight = _this.config.calculatedMainMenuHeight + _this.config.calculatedHeaderHeight;
		_this.config.pageHeight = $(window).height();
		_this.config.contentHeight = _this.config.pageHeight - _this.config.totalPageHeaderHeight;
		$('.left-facets-panel, .content-body').css({
			'height' : _this.config.contentHeight + 'px'
		});
	}
};

/**
 * Hotkey handlers. Enables some keyboard hotkey functions.
 */
CMF.hotkeys = {

	init: function() {
		$(window).on('keydown.cmf', function(event) {
			if (event.ctrlKey || event.metaKey) {
				var key = String.fromCharCode(event.which).toLowerCase();
				if(key) {
					var handler = CMF.hotkeys.handlers['ctrl_' + key];
					handler && handler(event);
				}
			}
		});
	},

	handlers: {
		'ctrl_s': function(event) {
			event.preventDefault();
			var saveButton = $("[id$='saveButton']");
			if (saveButton.length === 1) {
				saveButton.click();
			}
		},

		// in order for this to work the menu should be preloaded first
		'ctrl_e_disabled': function(event) {
			event.preventDefault();
			var editActionButton = $('.context-data-header a.editDetails');
			if (editActionButton.length === 1) {
				editActionButton.click();
			}
		}
	}
};


CMF.infoFunctions = {

	execute: function(functionName, opts) {
		if (!CMF.infoFunctions[functionName]) {
			console.error('There is no function [' + functionName + ']!');
			return;
		}
		CMF.infoFunctions[functionName].call(this, opts);
	},

	isOverDue: function() {},

	/**
	 *  opts = {duedate: datestring, completed:true/false, id:dbid}
	 */
	toggleOverdueBanner: function(opts) {
		var firstDate = opts.duedate,
			banner,
			isOverdue = false;
		if (firstDate) {
			isOverdue = (new Date(firstDate).getTime() < new Date().getTime());
		}
		if(isOverdue && !opts.completed) {
			banner = $('[data-banner-id="' + opts.id + '"]');
			if(banner.length > 0) {
				banner.text(_emfLabels['cmf.workflow.task.overdue.warning']);
			}
		}
	}
};

/**
 * Used for the version tab to allow actions on uploaded documents.
 */
CMF.versionWidget = {

		init : function() {
		var jsonData = {
			"instanceID" : EMF.documentContext.currentInstanceId,
			"instanceType" : EMF.documentContext.currentInstanceType
		}
		$(document).on("dom:document-preview-version", function(event, version) {
			jsonData["version"] = version;
			document.getElementById('formId:previewVersionHidden').value = JSON.stringify(jsonData);
			document.getElementById('formId:previewVersionHiddenButton').click();
		}).on("dom:document-revert-to-version", function(event, version) {
			jsonData["version"] = version;
			document.getElementById('formId:revertVersionHidden').value = JSON.stringify(jsonData);
			document.getElementById('formId:revertVersionHiddenButton').click();
		}).on("dom:document-download-version", function(event, version) {
			var result = CMF.document.downloadDocumentVersion(jsonData['instanceID'], version, CMF.action).then(function(data){
				CMF.utilityFunctions.downloadDocumentAfterEvent(SF.config.baseURL+data.path);
			});
		});
	}
};

CMF.init = function(opts) {
	CMF.config = $.extend(true, {}, CMF.config, opts);

	$.proxy(SF.init(), this);

	if(!CMF.config.lockBindingActions){
		CMF.config.lockBindingActions = true;
		// Bind base click handler dispatcher
		$(CMF.config.mainContainerId).on('click', CMF.clickHandlerDispatcher);
		CMF.actionsMenu.init();
	}

	// Bind browser window blocker to links and buttons
	$(CMF.config.mainContainerId).on('click', '.cmf-container a, .cmf-container input[type=submit], .cmf-container input.operation-button', EMF.blockUI.togglePageBlocker);

	CMF.action.init();

	CMF.hotkeys.init();

	// Initialize the tooltips plugin.
	$(document.body).tipTip({});

	CMF.searchPages.init();

	CMF.initFacetsStatus();

	CMF.utilityFunctions.filterSearchArgumentsData();

	CMF.utilityFunctions.hideElementsByChildrenLen('.context-data-header .tree-header');

	CMF.verticalSplitter.attachSplitterTo('.content-row');
	// TODO: will be used soon
//	CMF.layoutUtils.init();

	CMF.cl.triggerFiltering();

	Ext.Date.defaultFormat = SF.config.dateExtJSFormatPattern;
};

/**
 * Used for preventing the double refreshing details dashlet in project and case dashboard.
 */
CMF.detailsDashlet = {
	dashletAlreadyLoaded : false,

	/**
	 * Refreshes dashlet by clicking the given selectors.
	 *
	 * @param panelSelector
	 *        the selector for the dashlet panel
	 * @param refreshLink
	 *        the selector for the refresh link in the dashlet panel, that will be clicked
	 */
	refresh : function(panelSelector, refreshLink) {
		if (!this.dashletAlreadyLoaded) {
			$(panelSelector).find(refreshLink).click();
			this.dashletAlreadyLoaded = true;
		}
	}
};

// Register CMF module
EMF.modules.register('CMF', CMF, CMF.init);