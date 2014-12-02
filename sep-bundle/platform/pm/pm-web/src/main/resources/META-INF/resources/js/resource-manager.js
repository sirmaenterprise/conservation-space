;
(function($, window, document, undefined) {

	$.widget("PM.resourceManager", {

		//Options to be used as defaults
		options : {
            projectId           : null,
            defaultItemType     : 'user',       // the type that should be selected by default
            defaultUserSortType : 'firstName',
            defaultGroupSortType: 'GROUP_NAME',
            defaultOrder        : 'asc',
		    //itemsList         : null,         // json feed that should be used to populate the left panel
            selectedItemsList   : null,         // json feed that should be used to populate the right panel
            roles               : [],           // array with roles objects
            projectDashboardUrl	: null,			// a bookmark link for current project dashboard
            applicationContext  : null,         // like /emf
            services            : {             // REST service endpoints
                loadItems       : '/service/pm/rm/loadItems',
                loadProjectItems: '/service/pm/rm/loadProjectItems',
                save            : '/service/pm/rm/save',
                dmsServiceProxy : '/service/dms/proxy/'
            },
            itemType            : {
                user    : 'user',
                group   : 'group',
                invert  : function(type) {
                    return (type === this.user) ? this.group : this.user;
                }
            },
            itemsMap            : {             // cache for resource lists
                user    : null,
                group   : null,
                all     : null,
                project : null
            },
            loadingMaskPosition : {             // constants for loading mask position
                INSIDE  : 'inside',             // loading mask to be placed inside the target container
                AFTER   : 'after'               // loading mask to be placed after the target container
            },
            errorCode   : {                     // error codes
                FATAL   : 'fatal',              // should stop all actions on page and clear items from panels
                ERROR   : 'error'               // ...
            },
            labels          : {                 // labels constants
                addUsers    : 'Add users',      // label for the item type selector value
                addGroups   : 'Add groups',     // label for the item type selector value
                save   	    : 'Save',           // default save button text
                cancel 	    : 'Cancel',         // default cancel button text
                addItem     : 'Add >>',         // default add item button text
                removeItem  : '<< Remove',      // default remove item button text
                sortBy      : 'Sort by: ',
                firstName   : 'First name',
                lastName    : 'Family name',
                userId      : 'User name',
                groupName   : 'Group name'
            },
            // callback functions
            afterLoadItems          : null,     // called after items are loaded and allows some filtering to be done on the items list
            afterLoadSelectedItems  : null,     // called after items are loaded and allows some filtering to be done on the items list
            loadAllExternal         : null,     // called before the regular service for loading and can be used to provide
                                                // data from some other place for testing for example
            afterInit               : null,     // fired when the widget is fully initialized
            itemSelected            : null,     // fired right after an item is selected from the right side, and before to be added to the left
                                                // this way some activity can be performed upon selection or selection to be canceled
            roleChange              : null      // this is fired when user changes a resource role, returning false prevents updating the model value
		},

		/**
		 * Setup the widget
		 */
		_create : function() {
			var self = this;

            // base DOM skeleton
		    var widgetDom =
		        '<div class="rm-wrapper"> \
		    		<div class="rm-body clearfix"> \
	                    <div class="rm-body-panel rm-left-panel"> \
		    				<div class="rm-toolbar rm-first-toolbar"></div> \
					    	<div class="rm-toolbar rm-second-toolbar"></div> \
					    	<div class="rm-panel-content rm-left-panel-content"></div> \
		    				<div class="rm-panel-pagination"></div> \
		    			</div> \
	                    <div class="rm-middle-panel"> \
	                        <span class="add-direction-icon">&#160;</span> \
	                    </div> \
	                    <div class="rm-body-panel rm-right-panel"> \
							<div class="rm-toolbar rm-first-toolbar"></div> \
					    	<div class="rm-toolbar rm-second-toolbar"></div> \
					    	<div class="rm-panel-content rm-right-panel-content"></div> \
		    				<div class="rm-panel-pagination"></div> \
		    			</div> \
	                </div> \
	                <div class="rm-footer"> \
                        <input type="button" id="saveButton" value="Save" class="btn btn-primary rm-save-button standard-button operation-button blockUI" /> \
                        <input type="button" id="cancelButton" value="Cancel" class="btn btn-default rm-cancel-button standard-button operation-button blockUI" /> \
	                </div> \
		        </div>';

		    var wrapper = $(widgetDom).appendTo(self.element);

            // url prefix for access resource profile data
            self.dmsProxyUrl = self.options.applicationContext + self.options.services.dmsServiceProxy;

            var errorsPanel = $('<ul class="rm-errors"></ul>').prependTo(self.element);

            // format: [string]
            self.errorMessages = [];

            // assigned resources list is what we have selected in the right panel plus the role name
            // format: { 1:'consumer', 2:'collaborator' }
            self.assignedResources = {};

            // cache some elements for later use
            self.leftPanel = wrapper.find('.rm-left-panel-content');
            self.rightPanel = wrapper.find('.rm-right-panel-content');
            leftFirstToolbar = wrapper.find('.rm-left-panel .rm-first-toolbar');
            rightFirstToolbar = wrapper.find('.rm-right-panel .rm-first-toolbar');

            // create the filter field for the left panel and bind events to it
            self.leftFilterField =
                $('<input type="text" id="filterAllItems" name="filterAllItems" value="" class="rm-filter rm-filter-all-field" autocomplete="off" />')
                .appendTo(leftFirstToolbar)
                .on('keyup.rm.filterAll', function(evt) {
                    var keyCode = $.ui.keyCode;
                    switch(evt.keyCode) {
                        case keyCode.ENTER:
                        case keyCode.NUMPAD_ENTER:
                            //// console.log('enter');
                        default:
                            self._filterList(self.filteredAllItemsList, self.leftFilterField, self.leftPanel, false, 'all-items', 'item-select');
                            break;
                    }
                });

            // create the item selector and bind events to it
            var itemSelectorHtml =
                '<select class="item-type-selector"> \
		    		<option value="user">' + self.options.labels.addUsers + '</option> \
		    		<option value="group">' + self.options.labels.addGroups + '</option> \
		    	</select>';
            var itemSelector = $(itemSelectorHtml).appendTo(leftFirstToolbar)
                .find('option[value="' + self.options.defaultItemType + '"]')
                .attr('selected', 'selected').end()
                .on('change.rm.type', function(evt) {
                    self._switchItemType(evt);
                });
            self.selectedType = self.options.defaultItemType;

            // create sorting type selectors for users and groups
            var groupSortTypeSelectorHtml =
                '<span class="sort-type-selector group"> \
                    <span>' + self.options.labels.sortBy + '</span> \
                    <select> \
                        <option value="GROUP_NAME">' + self.options.labels.groupName + '</option> \
                    </select> \
                </span>';
            self.groupSortTypeSelector = $(groupSortTypeSelectorHtml).appendTo(leftFirstToolbar)
                .find('option[value="' + self.options.defaultGroupSortType + '"]')
                .attr('selected', 'selected').parent('select')
                .on('change.rm.sort', function(evt) {
                    self._sortItems(evt);
                });

            var userSortTypeSelectorHtml =
                '<span class="sort-type-selector user"> \
                    <span>' + self.options.labels.sortBy + '</span> \
                    <select> \
                        <option value="firstName">' + self.options.labels.firstName + '</option> \
                        <option value="lastName">' + self.options.labels.lastName + '</option> \
                        <option value="userId">' + self.options.labels.userId + '</option> \
                    </select> \
                </span>';
            self.userSortTypeSelector = $(userSortTypeSelectorHtml).appendTo(leftFirstToolbar)
                .find('option[value="' + self.options.defaultUserSortType + '"]')
                .attr('selected', 'selected').parent('select')
                .on('change.rm.sort', function(evt) {
                    self._sortItems(evt);
                });

            if(self.selectedType === self.options.itemType.group) {
                self.selectedSortField = self.options.defaultGroupSortType;
                self.userSortTypeSelector.parent().hide();
            } else {
                self.selectedSortField = self.options.defaultUserSortType;
                //self.groupSortTypeSelector.hide();
            }

            // create order switch button
            var orderClass = (self.options.defaultOrder === 'asc') ? 'asc glyphicon-sort-by-alphabet' : 'desc glyphicon-sort-by-alphabet-alt';
            var orderSwitchButtonHtml = '<a href="javascript: void(0);" class="btn btn-default btn-sm glyphicon switch-order-button ' + orderClass + '"> </a>';
            $(orderSwitchButtonHtml).appendTo(leftFirstToolbar)
                .on('click.rm.order', function(evt) {
                    self._switchOrder(evt);
                });

            // create the filter field for the right panel and bind events to it
            self.rightFilterField =
                $('<input type="text" id="filterSelectedItems" name="filterSelectedItems" value="" class="rm-filter rm-filter-selected-field" autocomplete="off" />')
                .appendTo(rightFirstToolbar)
                .on('keyup.rm.filterSelected', function(evt) {
                    var keyCode = $.ui.keyCode;
                    switch(evt.keyCode) {
                        case keyCode.ENTER:
                        case keyCode.NUMPAD_ENTER:
                            //// console.log('enter');
                        default:
                            self._filterList(self.filteredSelectedItemsList, self.rightFilterField, self.rightPanel, true, 'selected-items', 'item-remove');
                            break;
                    }
                });

            // trigger loading of items for the right panel
            self._loadSelectedItems(function() {
                // manually trigger change event on the item type selector
                itemSelector.trigger('change.rm.type');
            });

            // bind centralized click handler
            wrapper.on('click.rm', $.proxy(self._clickHandlerDispatcher, self))
                .on('change.rm.role', $.proxy(self._changeHandlerDispatcher, self));

            // trigger an event that the widget is complete initialized
            self._trigger("afterInit", { widget: self });
		},

        _clickHandlerDispatcher: function(evt) {
            var self = this;
            var target = $(evt.target);
            if(target.is('.item-select')) {
                self._addToSelected(evt);
            }
            else if(target.is('.item-remove')) {
                self._removeFromSelected(evt);
            } else if(target.is('.rm-save-button')) {
                self._save(evt);
            } else if(target.is('.rm-cancel-button')) {
                self._cancelEdit(evt);
            }
            PM.utilityFunctions.initResourceManagerSelectors();
        },

        _changeHandlerDispatcher : function(evt) {
            var self = this;
            var target = $(evt.target);
            if(target.hasClass('rm-role-selector')) {
                self._changeRole(evt, target);
            }
        },

        _sortItems : function(evt) {
            var self = this;
            self.selectedSortField = $(evt.target).val();
            self._showLoading(self.leftPanel, self.options.loadingMaskPosition.INSIDE);
            self._loadAllItems(self.selectedType, self.selectedSortField, true);
        },

        _switchOrder : function(evt) {
            var self = this;
            var switchButton = $(evt.target).closest('.switch-order-button');
            if(switchButton.hasClass('asc')) {
                self.selectedOrder = 'desc';
                switchButton.removeClass('asc glyphicon-sort-by-alphabet').addClass('desc glyphicon-sort-by-alphabet-alt');
            } else {
                self.selectedOrder = 'asc';
                switchButton.removeClass('desc glyphicon-sort-by-alphabet-alt').addClass('asc glyphicon-sort-by-alphabet');
            }
            self._showLoading(self.leftPanel, self.options.loadingMaskPosition.INSIDE);
            self.filteredAllItemsList.reverse();
            self._filterList(self.filteredAllItemsList, self.leftFilterField, self.leftPanel, false, 'all-items', 'item-select');
            self._hideLoading(self.leftPanel);
        },

        _changeRole : function(orgEvt, target) {
            var self = this;

            //var canceled = self._trigger('roleChange', orgEvt, { target : target });
            //if(canceled) return;

            // update resource list
            var row = target.closest('dt');
            var resourceId = row.attr('resourceid');
            var selectedRole = target.val() || '';
            self.assignedResources[resourceId] = selectedRole;
            var found = self._findItemById(self.filteredSelectedItemsList, resourceId);
            if(found) {
                found.item.role = selectedRole;
            }
            row.removeClass('unassigned-role');
            if(!selectedRole) {
                row.addClass('unassigned-role');
            }
        },

         _filterList: function(sourceList, filterField, targetPanel, renderRoleSelector, targetListCssClass, itemHandlerCssClass) {
            var self = this;
            var term = filterField.val().toLowerCase();
            var filteredList = self._filter(sourceList, term);
            
            var paginationPlaceholder = $('.rm-panel-pagination', targetPanel.parent());
            var totalItemsNumber = self._objectSize(filteredList);
    		
			if(filteredList && totalItemsNumber > 0) {
				var currentPage = 1;
				var itemsOnPage = 10;
				
				var paginationData = paginationPlaceholder.data('pagination');
				if (paginationData && paginationData.currentPage) {
					var maxPages = Math.floor(totalItemsNumber/itemsOnPage) + (totalItemsNumber%itemsOnPage>0?1:0);
					currentPage = paginationData.currentPage + 1;
					if (currentPage > maxPages) {
						currentPage = maxPages;
					}
				}
				
    			paginationPlaceholder.pagination({
    				items: totalItemsNumber,
    				currentPage: currentPage,
    				itemsOnPage: itemsOnPage,
    				cssStyle: 'compact-theme',
    				onPageClick: function(number, event) {
    					var start = (number - 1)*this.itemsOnPage;
    					targetPanel.empty().append(self._buildItemsList(self._objectSlice(filteredList, start, start + this.itemsOnPage), targetListCssClass, itemHandlerCssClass, renderRoleSelector));
    				}
    			}).show();
				var start = (currentPage-1)*itemsOnPage;
				targetPanel.empty().append(self._buildItemsList(self._objectSlice(filteredList, start, start + itemsOnPage), targetListCssClass, itemHandlerCssClass, renderRoleSelector));
			} else {
				paginationPlaceholder.hide();
				targetPanel.empty().append(self._buildItemsList(filteredList, targetListCssClass, itemHandlerCssClass, renderRoleSelector));
			}
        },
        
        _objectSize: function(obj) {
            var size = 0, key;
            for (key in obj) {
                if (obj.hasOwnProperty(key)) size++;
            }
            return size;
        },
        
        _objectSlice: function(obj, start, end) {
            var sliced = {};
            var i = 0;
            for (var k in obj) {
            	if (obj.hasOwnProperty(k)) {
	                if (i >= start && i < end) {
	                    sliced[k] = obj[k];
	                }
	                i++;
            	}
            }
            return sliced;
        },

        _escapeRegex: function(value) {
            return value.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
        },

        /**
         * Based on jQuery.grep function but works with associative arrays (maps) instead.
         */
        _filter: function(map, term) {
            var retVal;
            var ret = {};

            // Go through the assoc. array, only saving the items
            // that pass the validator function
            for (var key in map) {
                retVal = ((map[key].itemLabel.toLowerCase().indexOf(term) !== -1) || (map[key].Name.toLowerCase().indexOf(term) !== -1));
                if (retVal) {
                    ret[key] = map[key];
                }
            }

            return ret;
        },

        /**
         * Handler for the change event fired by the item type selector.
         */
		_switchItemType : function(evt) {
			// - if not already loaded, call service to load selected item types
			// - filter items according to already selected in the right panel
			// - replace the left panel content with the selected items
			var self = this;
            var selectedType = self.selectedType = $(evt.target).val();
            var itemsList;

            if(selectedType === self.options.itemType.group) {
                self.selectedSortField = self.options.defaultGroupSortType;
                self.userSortTypeSelector.parent().hide();
                //self.groupSortTypeSelector.show();
            } else {
                self.selectedSortField = self.options.defaultUserSortType;
                self.userSortTypeSelector.parent().show();
                //self.groupSortTypeSelector.hide();
            }

            self._showLoading(self.leftPanel, self.options.loadingMaskPosition.INSIDE);
            self._loadAllItems(selectedType, self.selectedSortField, false);
		},

        /**
         * Function that is used to load all resource items of given type.
         *
         * @param selectedType The type of resources to be loaded.
         * @param selectedSortField The sorting type that is selected for given resource type.
         * @param reload If resources should be reloaded or cached items may be used.
         */
        _loadAllItems : function(selectedType, selectedSortField, reload) {
            if(!selectedType) return;

            var self = this;
            var itemsList;

            // if selected item types are already loaded we get them from the local cache
            if(self.options.itemsMap[selectedType] && !reload) {
                itemsList = self.options.itemsMap[selectedType];
                self._afterLoadAllItems(itemsList, selectedType);
            }
            // client may pass external function that returns a json feed
            else if($.isFunction(self.options.loadAllExternal) && !reload) {
                itemsList = self.options.loadAllExternal(self.selectedType);
                if(itemsList) {
                    self._afterLoadAllItems(itemsList, selectedType);
                }
            }
            // if external loading fails, then call service to fetch the items
            else {
                var endpoint = self.options.applicationContext + self.options.services.loadItems;
                $.ajax({
                    url : endpoint,
                    dataType: 'json',
                    cache: false,
                    data : {
                        type : selectedType,
                        sortingField : selectedSortField
                    }
                })
                .done(function(data, status, jqXHR) {
                    if(data) {
                        self._afterLoadAllItems(data, selectedType);
                    }
                })
                .fail(function(jqXHR, status, error) {
                    self.error = self.options.errorCode.FATAL;
                    self._hideLoading(self.leftPanel);
                    self._setErrorMessage(status, jqXHR.responseText);
                });
            }
        },

        _loadSelectedItems : function(callback) {
            var self = this;
            var selectedItems;

            self._showLoading(self.rightPanel, self.options.loadingMaskPosition.INSIDE);

            // check in cache first
            if(self.options.itemsMap.project) {
                selectedItems = self.options.itemsMap.project;
                self._afterLoadSelectedItems(selectedItems);
                invokeCallback(callback);
            }
            // try to get from custom user provided feed
            else if($.isFunction(self.options.loadSelectedExternal)) {
                var loadedItems = self.options.loadSelectedExternal();
                selectedItems = loadedItems.resources;
                self.options.roles = loadedItems.roles;
                self.options.projectManagerRole = loadedItems.projectManagerRole;
                self._afterLoadSelectedItems(selectedItems);
                invokeCallback(callback);
            }
            // call service to load items
            else {
                var endpoint = self.options.applicationContext + self.options.services.loadProjectItems;
                $.ajax({
                    url : endpoint,
                    dataType: 'json',
                    cache: false,
                    data : {
                        projectId : self.options.projectId
                    }
                })
                .done(function(data, status, jqXHR) {
                    if(data) {
                        self.options.itemsMap.project = data.resources;
                        self.options.roles = data.roles;
                        self.options.projectManagerRole = data.projectManagerRole;
                        self._afterLoadSelectedItems(data.resources);
                    }
                    invokeCallback(callback);
                })
                .fail(function(jqXHR, status, error) {
                    self.error = self.options.errorCode.FATAL;
                    self._hideLoading(self.rightPanel);
                    self._setErrorMessage(status, jqXHR.responseText);
                });
            }

            function invokeCallback(callback) {
                if($.isFunction(callback)) {
                    callback();
                }
            }
        },

        _buildItemsList : function(items, markerClass, handlerTypeClass, renderRoleSelector) {
            var self = this;

            var html = '<dl class="rm-items-list ' + markerClass + '">';
            for(var item in items) {
                var currentItem = items[item];

                var icon = null;
                if (currentItem.iconPath){
                	icon = '<span class="item-icon"><img src=' + self.dmsProxyUrl + currentItem.iconPath + '/></span>';
                } else {
                	icon = '<span class="item-icon ' + currentItem.type + '-icon"></span>';
                }

                // when we build the selected items list we:
                // - add the roles selector
                // - update assignedResources list with initial values as loaded from the server
                var roleSelectorHtml = '';
                var unassignedRoleClass = '';
                if(renderRoleSelector) {
                    self.assignedResources[currentItem.Id] = currentItem.role;

                    var roles = self.options.roles;
                    roleSelectorHtml = '<span class="rm-role-selector-wrapper">';
                    roleSelectorHtml += '<select class="rm-role-selector">';
                    // for resources that have no assigned role, we set an empty option
                    if(!currentItem.role) {
                        roleSelectorHtml += '<option selected="selected" value=""></option>';
                        unassignedRoleClass = 'unassigned-role';
                    }
                    for (var role in roles) {
                        var currentRole = roles[role];
                        if(currentItem.role === currentRole.value) {
                            roleSelectorHtml += '<option selected="selected" value="' + currentRole.value + '">' + currentRole.label + '</option>';
                        } else {
                            roleSelectorHtml += '<option value="' + currentRole.value + '">' + currentRole.label + '</option>';
                        }
                    }
                    roleSelectorHtml += '</select>';
                    roleSelectorHtml += '</span>';
                }

                var rowClasses = currentItem.type + ' ' + currentItem.role + ' ' + unassignedRoleClass + ' row-disabled-' + currentItem.disabled;

                html += '<dt class="' + rowClasses + '" resourceId="' + currentItem.Id + '">';
                html +=   	icon;
                html +=     '<span class="item-data">';
                html +=         '<span class="item-value">' + currentItem.itemLabel + '</span>';
                html +=         '<span class="item-label">' + currentItem.itemValue + '</span>';
                html +=     '</span>';
                html +=     roleSelectorHtml;
                html +=     '<span class="item-handler ' + handlerTypeClass + '">&#160;</span>';
                html += '</dt>';
            }
            html += '</dl>';

            return html;
        },

        /**
         * Private function that should be called after all items are loaded from the cache or from server.
         */
        _afterLoadAllItems : function(loadedItemsList, selectedType) {
            var self = this;
            var filteredItems = loadedItemsList;

            // disable items that are already in the selected items list
            var selectedItemsList = self.filteredSelectedItemsList;
            var selectedItemsListLenght = selectedItemsList.length;
            var filteredItemsListLength = filteredItems.length;
            for ( var i = 0; i < selectedItemsListLenght; i++) {
				var selectedItemId = selectedItemsList[i].Id;
                for ( var j = 0; j < filteredItemsListLength; j++) {
                    var filteredItemId = filteredItems[j].Id;
                    if(selectedItemId === filteredItemId) {
                        filteredItems[j].disabled = true;
                        break;
                    }
                }
			}

            // invoke callback function with items list to allow some custom filtering
            if($.isFunction(self.options.afterLoadItems)) {
                filteredItems = self.options.afterLoadItems(selectedType, loadedItemsList);
            }

            // update cache with the filtered list
            self.options.itemsMap[selectedType] = filteredItems;
            self.filteredAllItemsList = filteredItems;

            var leftPanel = self.leftPanel;
            leftPanel.empty();
            if(filteredItems) {
                self._filterList(self.filteredAllItemsList, self.leftFilterField, self.leftPanel, false, 'all-items', 'item-select');
            }
            self._hideLoading(leftPanel);
        },

        _afterLoadSelectedItems : function(loadedSelectedItemsList) {
            var self = this;
            var filteredItems = loadedSelectedItemsList;

            // invoke callback function with items list to allow some custom filtering
            if($.isFunction(self.options.afterLoadSelectedItems)) {
                filteredItems = self.options.afterLoadSelectedItems(loadedSelectedItemsList);
            }
            // update cache with the filtered list
            self.options.itemsMap.project = filteredItems;
            self.filteredSelectedItemsList = filteredItems;
            var rightPanel = self.rightPanel;
            rightPanel.empty();
            if(filteredItems) {
                self._filterList(self.filteredSelectedItemsList, self.rightFilterField, self.rightPanel, true, 'selected-items', 'item-remove');
                PM.utilityFunctions.initResourceManagerSelectors();
            }
            self._hideLoading(rightPanel);
        },

        /**
         * Called when user clicks on add button. Moves the selected item to the selected
         * items list in the right panel.
         */
        _addToSelected : function(orgEvt) {
            var self = this;
            var selectedItem = $(orgEvt.target).closest('dt');

            // fire event that an item is selected
            //var isCanceld = self._trigger('itemSelected', orgEvt, { selectedItem : selectedItem });
            //if(isCanceld) {
                //return;
            //}

            var selectedItemId = selectedItem.attr('resourceid');
            //var selectedItemObject = self.filteredAllItemsList[selectedItemId];
            var selectedItemObject = self._findItemById(self.filteredAllItemsList, selectedItemId).item;
            // disable it in all items list
            //self.filteredAllItemsList.splice(selectedItemObject.index ,1);
            selectedItemObject.disabled = true;
            // move selected item to the selected items list
            self.filteredSelectedItemsList.push(selectedItemObject);
            // update assignedResources list
            self.assignedResources[selectedItemId] = selectedItemObject.role;

            // call filterList on lists to update model and panels
            self._filterList(self.filteredSelectedItemsList, self.rightFilterField, self.rightPanel, true, 'selected-items', 'item-remove');
            self._filterList(self.filteredAllItemsList, self.leftFilterField, self.leftPanel, false, 'all-items', 'item-select');
        },

        _findItemById : function(list, id) {
            var len = list.length;
            for ( var i = 0; i < len; i++) {
                if(list[i].Id === id) {
                    var found = list[i];
                    return {
                        item: found,
                        index: i
                    };
                }
            }
        },

        _removeFromSelected : function(orgEvt) {
            var self = this;
            var allItemsList = self.leftPanel.find('.all-items');
            var deselectedItem = $(orgEvt.target).closest('dt');

            var deselectedItemId = deselectedItem.attr('resourceid');

            // if selected item for remove is of type different from the type of the currently loaded items
            // we should remove the item from the right but not to add to the left
            var result = self._findItemById(self.filteredSelectedItemsList, deselectedItemId);
            var deselectedObject = result.item;
            var index = result.index;
            // remove deselected item from the selected items list
            self.filteredSelectedItemsList.splice(index, 1);
            var targetListType = self.options.itemType.invert(self.selectedType);
            if(self.options.itemsMap[targetListType]) {
                var found = self._findItemById(self.options.itemsMap[targetListType], deselectedItemId);
                if(found) {
                    found.item.disabled = false
                }
            }

            // enable the item from in all items list
            var found = self._findItemById(self.filteredAllItemsList, deselectedItemId);
            if(found) {
                found.item.disabled = false;
                found.item.role = '';
            }

            // update assignedResources list
            delete self.assignedResources[deselectedItemId];

            // call filterList on lists to update model and panels
            self._filterList(self.filteredAllItemsList, self.leftFilterField, self.leftPanel, false, 'all-items', 'item-select');
            self._filterList(self.filteredSelectedItemsList, self.rightFilterField, self.rightPanel, true, 'selected-items', 'item-remove');
        },

        _save : function(evt) {
            // - validate data
            // - call rest service to save data
            // - data format: { resourceId1="projectmanager", resourceId2="consumer", ... }
            var self = this;
            var assignedResources = self.assignedResources;
            var validationErrors = [];
            var isValid = true;
            var pmRoleCount = 0;
            var projectManagerRole = self.options.projectManagerRole;

            for(var resourceId in assignedResources) {
                var role = assignedResources[resourceId];
                // all assigned resources should have assigned roles in order to allow save operation
                if(!role) {
                    isValid = false;
                    validationErrors.push('There are resources without assigned roles!');
                    break;
                }

                if(role === projectManagerRole) {
                    pmRoleCount++;
                }
            }

            // at least one resource with role project manager should exists in order to
            // allow save operation
            if(pmRoleCount === 0) {
                isValid = false;
                validationErrors.push('At least one resource with role [' + projectManagerRole + '] should be assigned to the project!');
            }

            // console.log(assignedResources, isValid);
            if(!isValid) {
            	EMF.blockUI.hideAjaxBlocker();
                self._setErrorMessage('error', validationErrors);
            } else {
                var endpoint = self.options.applicationContext + self.options.services.save;
                var request = JSON.stringify({
                    projectId : self.options.projectId,
                    assignedResources : assignedResources
                });
                $.ajax({
                    type : 'POST',
                    dataType : 'json',
                    contentType : 'application/json; charset=utf-8',
                    url : endpoint,
                    data : request
                })
                .done(function(data, status, jqXHR) {
                    if(data) {
                    	var redirectUrl = data.redirectUrl;
                        window.location = redirectUrl;
                    }
                })
                .fail(function(jqXHR, status, error) {
                    self.error = self.options.errorCode.FATAL;
                    self._hideLoading(self.rightPanel);
                    self._setErrorMessage(status, jqXHR.responseText);
                });
            }
        },

        _cancelEdit : function(evt) {
        	if(this.options.projectDashboardUrl) {
        		window.location = this.options.projectDashboardUrl;
        	}
        },

        _setErrorMessage : function(status, msg, append) {
            if(!msg) return;

            var self = this;
            var errorsPanel = $('.rm-errors');
            if(!append) {
                self.errorMessages = [];
                errorsPanel.empty();
            }
            self.errorMessages = self.errorMessages.concat(msg);

            $.each(self.errorMessages, function(index, item) {
            	if(item.indexOf('<html') === 0) {
            		// skip errors returned by the server
            	} else {
            		$('<li class="rm-error ' + status + '"></li>').text(item).appendTo(errorsPanel);
            	}
            });
            errorsPanel.fadeIn();
        },

        _showLoading : function(target, position) {
            var self = this;
            var isTargetEmpty = $(target).is(':empty');
            if(isTargetEmpty){
            	return;
            }
            if(position === self.options.loadingMaskPosition.INSIDE) {
                $('<div class="loading-mask">&#160;</div>').insertAfter(target)
                    .css({
                        'position' : 'absolute',
                        'top' : target.offset().top,
                        'left' : target.offset().left,
                        'width' : target.outerWidth() + 'px',
                        'height' : target.outerHeight() + 'px'
                    }).fadeIn();
            } else if(position === self.options.loadingMaskPosition.AFTER) {

            }
        },

        _hideLoading : function(target) {
            target.next('.loading-mask').fadeOut(function() {
                $(this).remove();
            });
        },

		// Destroy an instantiated plugin and clean up
		// modifications the widget has made to the DOM
		destroy : function() {

			// this.element.removeStuff();
			// For UI 1.8, destroy must be invoked from the
			// base widget
			$.Widget.prototype.destroy.call(this);
			// For UI 1.9, define _destroy instead and don't
			// worry about
			// calling the base widget
		},

		// Respond to any changes the user makes to the
		// option method
		_setOption : function(key, value) {
            var self = this;
            var oldValue = self.options[ key ];

			switch (key) {
			case "usersList":
                if($.isArray(value)) {
                    this.options.itemsMap.users = value;
                }
				break;
			case "groupsList":
                if($.isArray(value)) {
                    this.options.itemsMap.groups = value;
                }
				break;
			default:
				// this.options[ key ] = value;
				break;
			}

			// For UI 1.8, _setOption must be manually invoked
			// from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
			// For UI 1.9 the _super method can be used instead
			// this._super( "_setOption", key, value );

            // The widget factory doesn't fire an callback for options changes by default
            // In order to allow the user to respond, fire our own callback
            this._trigger( "setOption", null, {
                option: key,
                original: oldValue,
                current: value
            });
		}
	});

})(jQuery, window, document);
