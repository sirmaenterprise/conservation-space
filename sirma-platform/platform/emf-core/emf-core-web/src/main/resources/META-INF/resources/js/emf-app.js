var EMF = EMF || {};

// page blocker(ajaxGUILoader) variable
EMF.ajaxGUIBlocker = null;
EMF.shorteningPluginInstance = null;
// for temporary parameters storage
EMF.temp = {};

/**
 * EMF configuration options.
 */
EMF.config = {
	debugEnabled		: false,
	mainContainerId		: '#container',
	events				: {
		EMF_LOGOUT: 'emf.logout',
		SESSION_TIMER_RESET: 'session.timer.reset'
	},
	beforeLogoutHandlers: {},
	// default page blocker is ON
	pageBlockerOFF		: false,
	unsavedDataConfirm	: {
		enabled	: true,
		message	: 'You have unsaved data on this page. Do you really want to leave without save?'
	},
	// used for restricting/allowing <i>on before load message</i>
	// for specific event container
	restrictBeforeLoadMessage : false,
	clearLinks : false,
	servicePaths: {
		INSTANCE					: '/instances/',
		INSTANCE_MOVE				: '/instances/move',
		INSTANCE_DETACH				: '/instances/detach',
		INSTANCE_REVISIONS 			: '/instances/revisions',
		INSTANCE_PROPERTIES 		: '/instances/properties',
		TASK_SUBTASKS				: '/task/subtasks',
		TASK_PROCESSING_DOCUMENTS	: '/task/processing',
		CASE_INSTANCE_SECTIONS		: '/caseInstance/sections',
		DOCUMENT_SHARE_LINK 		: '/document/previewPath',
		DOCUMENT_LOAD_HISTORY 		: '/document/history',
		DOCUMENT_DOWNLOAD_VERSION   : '/document/downloadVersionPath'
	},

	contentType : {
		APP_JSON : 'application/json'
	}
};

/**
 * Utility and generic DOM functions.
 */
EMF.util = {

	/**
	 * Create namespaces automatically.
	 * Example usage:
	 * 1. assign returned value to a local var
	 * var module2 = EMF.util.namespace('EMF.modules.module2');
	 * 2. skip initial 'EMF'
	 * EMF.util.namespace('modules.module51');
	 * 3. long namespace
	 * EMF.util.namespace('once.upon.a.time.there.was.this.long.nested.property');
	 * 4. Pass module root
	 * EMF.util.namespace('modules.module1', CMF)
	 */
	namespace : function (nsString, moduleRoot) {
		var parts = nsString.split('.'),
		parent = moduleRoot || EMF,
		i;
		// strip redundant leading global
		if (parts[0] === "EMF") {
			parts = parts.slice(1);
		}
		for (i = 0; i < parts.length; i += 1) {
			// create a property if it doesn't exist
			if (typeof parent[parts[i]] === "undefined") {
				parent[parts[i]] = {};
			}
			parent = parent[parts[i]];
		}
		return parent;
	},

	navigator : function() {
	    var ua= navigator.userAgent, tem,
	    M= ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
	    if(/trident/i.test(M[1])){
	        tem=  /\brv[ :]+(\d+)/g.exec(ua) || [];
	        return 'IE '+(tem[1] || '');
	    }
	    if(M[1]=== 'Chrome'){
	        tem= ua.match(/\bOPR\/(\d+)/);
	        if(tem!= null) {
	        	return 'Opera '+tem[1];
	        }
	    }
	    M= M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];
	    if((tem= ua.match(/version\/(\d+)/i)) != null) {
	    	M.splice(1, 1, tem[1]);
	    }

	    return M.join(' ');
	},

	getBrowserName : function() {
		return EMF.util.navigator().split(' ')[0];
	},

	getBrowserVersion : function() {
		return EMF.util.navigator().split(' ')[1];
	},

	os : function() {
		// Win XP: Windows NT 5.1
		// Win8: Windows NT 6.2; WoW64
		// Win7: Windows NT 6.1; WOW64
		// os x: Intel Mac OS X 10.9
		// Linux: Linux i686
		return navigator.oscpu;
	},

	/**
	 * Disable/enable a button.
	 *
	 * @param button
	 * 		A button selector or jquery object.
	 * @param disable
	 * 		If button should be disabled or enabled.
	 */
	toggleButton : function(button, disable) {
		if(!disable) {
			$(button).removeAttr('disabled');
		} else {
			$(button).attr('disabled', 'disabled');
		}
	},

	/** Generates UUID - http://stackoverflow.com/a/8809472/1119400 */
	generateUUID : function() {
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
	    });
	    return uuid;
	},

	/** replaces parameters in a string http://joquery.com/2012/string-format-for-javascript **/
	formatString: function() {
	    // The string containing the format items (e.g. "{0}")
	    // will and always has to be the first argument.
		var theString = arguments[0];

		// start with the second argument (i = 1)
		for (var i = 1; i < arguments.length; i++) {
			// "gm" = RegEx options for Global search (more than one instance)
			// and for Multiline search
			var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
			theString = theString.replace(regEx, arguments[i]);
	    }
		return theString;
	},

	/** Escapes : in a string. The id becomes usable in jQuery */
	escapeId: function(str) {
		 if (str) {
			 return str.replace(/(:|\.)/g, '\\$1');
		 } else {
			 return str;
		 }
	},

	/** Unescapes ':' in a string. */
	unEscapeId: function(str) {
		 if (str) {
			 return str.replace(/\\(:|\.)/g, '$1');
		 } else {
			 return str;
		 }
	},

	/** Constructs a jQuery object by provided id */
	jqById: function(id) {
		return $("#" + this.escapeId(id));
	},

	/**
	 * Creates path to icon with given name.
	 */
	iconPath: function(iconName) {
		if (iconName) {
			return EMF.applicationPath + '/javax.faces.resource/' + iconName + '.png.jsf?ln=images';
		}
		return '';
	},

	/**
	 * Creates path to icon with given name and size.
	 * Icon size can be one of: 16, 24, 32, 64.
	 */
	objectIconPath: function(objectType, size) {
		if(objectType && size) {
			return EMF.applicationPath + '/javax.faces.resource/' + objectType + '-icon-' + size + '.png.jsf?ln=images';
		}
		return '';
	},

	/**
	 * Resize textarea in height to wrap whole text inside without vertical scrolbar.
	 */
	textareaResize: function(elementOrSelector) {
		var ta;
		if (typeof elementOrSelector === 'string') {
			ta = $(elementOrSelector);
		} else {
			ta = elementOrSelector;
		}
	    ta.each(function() {
	        var current = $(this);
	        var fontSize = current.css('font-size');
	        var lineHeight = Math.floor(parseInt(fontSize.replace('px','')) * 1.5);
	        // Get the scroll height of the textarea
	        var taHeight = current[0].scrollHeight;
	        var numberOfLines = Math.ceil(taHeight / lineHeight);
	        current.css({
	            height: numberOfLines * lineHeight + 'px'
	        });
	    });
	},

	/**
	 * Escapes a string to be used as literal expression in a regex.
	 */
	escapeRegExp: function(string) {
	    return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
	},

	/**
	 * Finds all iframes on current page and applies visibility modificator.
	 */
	hideAllIFrames: function(hide) {
		var iframes = document.getElementsByTagName('iframe');
		if(iframes.length === 0) {
			return;
		}
		EMF.util._toggleIframe(iframes, hide);
	},

	_toggleIframe: function(iframesList, hide) {
		var len = iframesList.length,
			display = hide ? 'none' : 'block';
		for (var i = 0; i < len; i++) {
			iframesList[i].style.display = display;
		}
	},

	lodash: {
		contains: function(elements, object, props) {
			var filter = object;
			if (props && props.length) {
				filter = _.pick(object, props);
			}
			return _.findIndex(elements, filter) !== -1;
		},

		pick: function(object, predicate, thisObj) {
			var result;
			if (typeof object === 'object') {
				result = { };
				_.forOwn(predicate, function(value, key) {
					var newKey = value || key;
					result[newKey] = object[key];
				});
				return result;
			}
			return _.lodash(object, predicate, thisObj);
		},

		object: function(array, key) {
			var result = { };
			_.each(array, function(item) {
				result[item[key]] = item;
			});
		},

		eachAll: function() {
			var args = Array.prototype.slice.call(arguments),
				callback = args.pop();
			_.each(args, function(array) {
				_.each(array, function(item) {
					callback(item);
				});
			});
		}
	},

	/** Encode given string to base64. */
	base64Encode: function(value) {
		return base64.encode(value);
	},

	/** Decode given base64 string. */
	base64Decode: function(value) {
		return base64.decode(value);
	},

	/**
	 * Extracts the instance type from instance link. Uses regEx pattern to find the type.
	 * The pattern matches to groups, so the result from this function is array with this elements:
	 * <pre>
	 * ["type=someInstnaceType", "someInstanceType"],
	 * </pre>
	 * which means that you have to get the second element of the array, if you want clean instance type.
	 *
	 * @param headerElement
	 * 					the 'a' tag in the instance header
	 * @returns the array with two elements, where the second is the instance type of the element,
	 * 			which 'a' tag is passed or <b>null</b> if 'href' attribute is missing in the passed element
	 */
	getInstanceTypeFromHeaderLink : function(headerElement) {
		var regEx = new RegExp('type=(.*)(?=\&instance)');
		var link = headerElement.attr('href');
		if(link){
			return regEx.exec(link);
		}
		return null;
	}
};


/**
 * DOM manipulation functions.
 */
EMF.dom = {

	/**
	 * Builds a check table from data in format:
	 * data: [{instanceId: '1',data: 'item data text',icon: ''},{...}]
	 */
	buildCheckTable: function(options) {
		if (!options) {
			return '';
		}
		//data, tableClass, tableId, singleSelection
		var config = {
			// If single selection should be allowed then radio buttons will be rendered instead of checkboxes.
			singleSelection	: true,
			tableClass		: 'emf-table',
			tableId			: 'emfTable',
			data			: []
		};
		config = $.extend(true, {}, config, options);
		var selected = config.selected;
		var selectedItemsMap = {};
		for (var i = 0; i < selected.length; i++) {
			selectedItemsMap[selected[i]] = selected[i];
		}
		var selectType = (config.singleSelection) ? 'radio' : 'checkbox';
		var selectorName = (config.singleSelection) ? (config.tableId + 'Radio') : (config.tableId + 'Checkbox');
		var html = '<table class="' + config.tableClass + '">';
		for ( var item in config.data) {
			var current = config.data[item];
			html += '<tr data-identifier="' + current.identifier + '">';
			html += '<td class="check-column">';
			if (current.instanceId in selectedItemsMap) {
				html += '<input type="' + selectType + '" value="' + current.dbId + '" name="' + selectorName + '" checked="checked" />';
			} else {
				html += '<input type="' + selectType + '" value="' + current.dbId + '" name="' + selectorName + '" />';
			}
			html += '</td>';
			html += '<td class="data-column">' + current.default_header + '</td>';
			html += '</tr>';
		}
		html += '</table>';
		return html;
	}
};

/**
 * Date utilities functions.
 */
EMF.date = {

	/**
	 * Formats a date using provided pattern.
	 *
	 * @param date
	 * 			A date object
	 * @param pattern
	 * 			The pattern to be used for date format: for example 'dd.M.yy'. If not provided, a default value
	 * 			will be used instead.
	 * @param appendHint
	 * 			If suffix after formatted date should be appended (for example: "01.20.2014 (few seconds ago)")
	 *
	 * @example EMF.date.format(new Date(), 'dd.M.yy', true)
	 */
	format: function(date, pattern, appendHint) {
		var formatPattern = pattern || SF.config.dateFormatPattern;
		if (formatPattern && date) {
			if($ && $.datepicker) {
				if (!appendHint) {
					return $.datepicker.formatDate(formatPattern, date);
				} else {
					var suffix = EMF.date.timeSince(date);
					var formattedDate = $.datepicker.formatDate(formatPattern, date);
					formattedDate += ' (' + suffix + ')';
					return formattedDate;
				}
			}
		} else {
			console.error('Missing required arguments for EMF.date.format!');
		}
	},

	/**
	 * Formats a date in ISO format.
	 */
    getISODateString: function(date) {
		function pad(n) {
		    return n < 10 ? '0' + n : n;
		}
		return date.getUTCFullYear() + '-' + pad(date.getUTCMonth() + 1) + '-'
			+ pad(date.getUTCDate()) + 'T' + pad(date.getUTCHours()) + ':'
			+ pad(date.getUTCMinutes()) + ':' + pad(date.getUTCSeconds())
			+ 'Z';
	},

	/**
	 * Formats a date with hour.
	 */
	getDateTime: function(date) {
		var formattedDate = this.format(date) + " " + date.getHours() + ":" +  (date.getMinutes() < 10 ? '0':'') + date.getMinutes();
		return formattedDate;
	},

	/**
	 * Formats a date with hour, minutes, seconds and am/pm included.
	 */
	getDateTimeSeconds: function(date) {
		var formattedDate = date.getHours() + ":" +  (date.getMinutes() < 10 ? '0':'') + date.getMinutes()
			+ ":" +  (date.getSeconds() < 10 ? '0':'') + date.getSeconds();
		return formattedDate;
	},

	/**
	 * Calculates a string suffix according to current date time.
	 *
	 * @param date
	 * 			The date according to which to calculate the suffix
	 *
	 * @returns Calculated string suffix.
	 */
    timeSince: function(date) {
		var seconds = Math.floor((new Date() - date) / 1000);
		var interval = Math.floor(seconds / 31536000);
		if (interval >= 1) {
		    if (interval === 1) {
		    	return interval + " year ago";
		    }
		    return interval + " years ago";
		}
		interval = Math.floor(seconds / 2592000);
		if (interval >= 1) {
		    if (interval === 1) {
		    	return interval + " month ago";
		    }
		    return interval + " months ago";
		}
		interval = Math.floor(seconds / 86400);
		if (interval >= 1) {
		    if (interval === 1) {
		    	return interval + " day ago";
		    }
		    return interval + " days ago";
		}
		interval = Math.floor(seconds / 3600);
		if (interval >= 1) {
		    if (interval === 1) {
		    	return interval + " hour ago";
		    }
		    return interval + " hours ago";
		}
		interval = Math.floor(seconds / 60);
		if (interval >= 1) {
		    if (interval === 1) {
			return interval + " minute ago";
		    }
		    return interval + " minutes ago";
		}
		return "few seconds ago";
	}
};

/**
 * Click handlers dispatcher.
 */
EMF.clickHandlerDispatcher = function(evt) {

	var srcTarget = $(evt.target);

	// toggle user menu
	if(srcTarget.is('.user-menu-trigger')) {
		EMF.clickHandlers.toggleUserMenu(srcTarget);
	}
	else {
		$('.user-menu-options').hide();
	}
};

/**
 * Contains handlers for favourites functionality. There are method for add and remove given instance
 * from the user favourites.
 */
EMF.favourites = {

	/**
	 * Handles every click on the add/remove favourite icon (full and empty star in instance headers).
	 * This method is binded to click event (check EMF.init). When one of the two icons is clicked,
	 * this method is called. From the click event, we get the element. The element have data property,
	 * which contains the id of the instance, which is used to find all the favourites icons in the page,
	 * if there are more, then one (details and landing pages) and update it. Also this method builds
	 * the request data for the rest services that will be called, when one of the icons is clicked.
	 *
	 * @param evt
	 * 			the event that is fired, when one of the icons is clicked
	 */
	handler : function(evt){

		var element = $(evt.target);
		var data = EMF.headersIconsUtil.extractInstanceData(element);

		if(element.hasClass('star')){
			EMF.favourites.remove(data, element);
		}else {
			EMF.favourites.add(data);
		}
	},

	/**
 	 * Calls rest service that adds the instance to the current user favourites. If the response for the
	 * service is 'OK', then the icons in the page are updated, if there are more then one. This is called, when
	 * empty star icon is clicked. Creates POST request to the favourites rest service (FavouritesRestService)
	 * with parameters id and type of the instance, which icon is clicked.
	 *
	 * @param data
	 * 			instance data (id & type)
	 */
	add : function(data){
		var iconsData = {
				instanceId : data.instanceId,
				add		   : 'star favourites',
				hint   	   : 'favourite.remove.hint',
				iconClasses: 'dislikes favourites',
				operation  : 'add to favourites'
			};
		EMF.headersIconsUtil.showLoadingIcon(iconsData);
		REST.getClient('FavouritesRestClient').add({
			type : 'POST',
			data : data})
		.done(function(){EMF.headersIconsUtil.responseHandler(iconsData, false, null);})
		.fail(function(){EMF.headersIconsUtil.responseHandler(iconsData, true, null);});
	},

	/**
	 * Calls rest service that removes the instance to the current user favourites. If the response for the
	 * service is 'OK', then the icons in the page are updated, if there are more then one. This is called, when
	 * full star icon is clicked. Creates DELETE request to the favourites rest service (FavouritesRestService)
	 * with path parameters id and type of the instance, which icon is clicked.
	 *
	 * @param data
	 * 			instance data (id & type)
	 * @param element
	 * 			the clicked icon
	 */
	remove : function(data, element){
		var iconsData = {
				instanceId : data.instanceId,
				add		   : 'dislikes favourites',
				hint   	   : 'favourite.add.hint',
				iconClasses: 'star favourites',
				operation  : 'remove from favourites'
			};
		EMF.headersIconsUtil.showLoadingIcon(iconsData);
		REST.getClient('FavouritesRestClient').remove({
			type : 'DELETE',
			data : data})
		.done(function(){EMF.headersIconsUtil.responseHandler(iconsData, false, element);})
		.fail(function(){EMF.headersIconsUtil.responseHandler(iconsData, true, null);});
	}

};

/**
 * Contains handlers for downloads functionality. There are method for add and remove given instance
 * from the user downloads list.
 */
EMF.downloads = {

		/**
		 * Handles every click on the add/remove downloads icon (green and gray download icons in instance headers).
		 * This method is binded to click event (check EMF.init). When one of the two icons is clicked,
		 * this method is called. From the click event, we get the element. The element have data property,
		 * which contains the id of the instance, which is used to find all the downloads icons in the page,
		 * if there are more, then one (details and landing pages) and update it. Also this method builds
		 * the request data for the rest services that will be called, when one of the icons is clicked.
		 *
		 * @param evt
		 * 			the event that is fired, when one of the icons is clicked
		 */
		handler : function(evt){
			var element = $(evt.target);
			var data = EMF.headersIconsUtil.extractInstanceData(element);

			if(element.hasClass('circle_arrow_down')){
				EMF.downloads.remove(data, element);
			}else {
				EMF.downloads.add(data);
			}
		},

		/**
	 	 * Calls rest service that adds the instance to the current user downloads list. If the response for the
		 * service is 'OK', then the icons in the page are updated, if there are more then one, they are also updated.
		 * This is called, when gray downloads icon is clicked. Creates POST request to the downloads rest service
		 * (DownloadsRestService) with parameters id and type of the instance, which icon is clicked.
		 *
		 * @param data
		 * 			instance data (id & type)
		 */
		add : function(data){
			var iconsData = {
					instanceId : data.instanceId,
					add		   : 'circle_arrow_down downloads-list',
					hint   	   : 'downloads.remove.hint',
					iconClasses: 'download downloads-list',
					operation  : 'add to dwonloads'
				};
			EMF.headersIconsUtil.showLoadingIcon(iconsData);
			REST.getClient('DownloadsRestClient').add({
				type : 'POST',
				data : data
			})
			.done(function(){EMF.headersIconsUtil.responseHandler(iconsData, false, null);})
			.fail(function(){EMF.headersIconsUtil.responseHandler(iconsData, true, null);});
		},

		/**
		 * Calls rest service that removes the instance to the current user downloads list. If the response for the
		 * service is 'OK', then the icons in the page are updated, if there are more then one, they are also updated.
		 * This is called, when green downloads icon is clicked. Creates DELETE request to the downloads rest service
		 * (DownloadsRestService) with path parameters id and type of the instance, which icon is clicked.
		 *
		 * @param data
		 * 			instance data (id & type)
		 * @param element
		 * 			the clicked icon
		 */
		remove : function(data, element) {
			var iconsData = {
					instanceId : data.instanceId,
					add		   : 'download downloads-list',
					hint   	   : 'downloads.add.hint',
					iconClasses: 'circle_arrow_down downloads-list',
					operation  : 'remove from downloads'
				};
			EMF.headersIconsUtil.showLoadingIcon(iconsData);
			REST.getClient('DownloadsRestClient').remove({
				type : 'DELETE',
				data : data })
			.done(function(){EMF.headersIconsUtil.responseHandler(iconsData, false, element);})
			.fail(function(){EMF.headersIconsUtil.responseHandler(iconsData, true, null);});
		}

};

/**
 * Contains common functionality, used for processing icons in instance headers.
 */
EMF.headersIconsUtil = {

		/**
		 * Extracts instance data from the icons tag and instance header link. The icon element, that is clicked is
		 * passed. This element contains data property (data-instanceid), from which the id of the instance is extracted
		 * and the type of the instance is extracted from the instance header link. Then the object is build with this
		 * two properties and it is returned. This object is used for request data for the rest calls.
		 *
		 * @param element
		 * 			the icon element, which was clicked
		 * @returns object, which contains instance id and type
		 */
		extractInstanceData : function(element){
			var parent = element.parent();
			var instanceId = parent.attr('data-instanceid');

			var headerLinks = parent.parent().find("a[href*='" + instanceId + "']");
			var parsedHeader = EMF.util.getInstanceTypeFromHeaderLink(headerLinks);
			var instanceType = null;
			if (parsedHeader) {
				instanceType = parsedHeader[1];
			}

			var data = {
				instanceId 	 : instanceId,
				instanceType : instanceType
			};

			return data;
		},

		/**
		 * Handles the response for the rest call. This function is called, when the rest service call response is
		 * successful or not. Depending on, which the parameters are different. If the icons will be changed or not
		 * is determined by the <b>fail</b> parameter. If it is true the icons will be changed back from the loading
		 * icon, if it is false they will be changed to the new icon corresponding to the instance state(if it is
		 * marked or unmarked). Also this method will refresh the dashlet from which the icon is clicked, when the
		 * instance is removed from downloads or favourites and specific filter is selected.
		 *
		 * @param data
		 * 			contains information about the icons, which should be changed and on which instance should be
		 * 			changed. Also contains information for building correct log message.
		 * @param fail
		 * 			determines if the icons should be changed to new one or they should be returned back
		 * @param element
		 * 			the clicked icon
		 */
		responseHandler : function(data, fail, element){
			if(fail){
				EMF.headersIconsUtil.updateIconsOnFail(data);
			}else{
				EMF.headersIconsUtil.updateIconsOnSuccess(data);
				if(element){
					EMF.headersIconsUtil.refreshDashlet(element);
				}
			}
        },

		/**
		 * Updates the icons in instance headers (for favourites and downloads), when some operation is executed. If
		 * there are more then one icons on the page, that have the same data property value, they are also updated
		 * (more then one instance header for the instance that is processed). The titles(tooltips) are also
		 * updated.
		 *
		 * Minimal object example:
		 * <pre>
		 * {
		 * instanceId  : 'emf:xxxxxxxx-xxxxxxxx-xxxx-xxxxxxxxxxxx',
		 * add		   : 'download downloads-list',
		 * hint   	   : 'downloads.add.hint',
		 * }
		 * </pre>
		 *
		 * @param data
		 * 			object containing information for the instance and its icon, which should be updated
		 */
		updateIconsOnSuccess : function(data){
			var iconsInPage = $('[data-instanceid="' + data.instanceId + '"] [class~=roundabout]');
			iconsInPage.removeClass('roundabout roundabout-flash').addClass(data.add).removeAttr('style');
			iconsInPage.attr('title', _emfLabels[data.hint]);
		},

		/**
		 * Returns the icon, which was clicked, if the operation fails. Finds all loading icons and removes
		 * them. After that adds the old icon, which was clicked.
		 *
		 * @param data
		 * 			contains icon class which will be added, after the loading icon is removed and
		 * 			id of the instance, which will be used to find all the headers, which should be updated
		 */
		updateIconsOnFail : function(data){
			var iconsInPage = $('[data-instanceid="' + data.instanceId + '"] [class~=roundabout]');
			iconsInPage.removeClass('roundabout roundabout-flash').addClass(data.iconClasses).removeAttr('style');
		},

		/**
		 * Shows loading icon while the the operation is executed. This will change the clicked icon with icon
		 * for loading. After the operation is done the icons are updated.
		 *
		 * @param data
		 * 			contains class/es which will be removed, also used to find all the icons on the page
		 *			for the given instance and information for the instance and its icon, which should be updated
		 */
		showLoadingIcon : function(data){
			var iconsInPage = $('[data-instanceid="' + data.instanceId + '"] [class~=' + data.iconClasses.split(' ')[0] + ']');
			var css_stayle = iconsInPage.css("right");
			iconsInPage.removeClass(data.iconClasses).addClass('roundabout roundabout-flash').css("right" , css_stayle);
		},

		/**
		 * Finds and updates all visible icons in page. First finds all icons in the page, which should be changed,
		 * then updates their title(hint) and their class in order to show the correct icon. The information for which
		 * class should be removed or added is passed as data object.
		 *
		 * @param data
		 * 			contains the data about, which class should be removed and which should be added, also contains the
		 * 			hint
		 */
		updateAllIcons : function(data){
			var classes = data.remove.split(' ');
			var iconsInPage = $('[class~=' + classes[0] + '][class~=' + classes[1] + ']');
			iconsInPage.removeClass(data.remove).addClass(data.add);
			iconsInPage.attr('title', _emfLabels[data.hint]);
		},

		/**
		 * Refreshes specific dashlet, when instance is removed from downloads or favourites, if specific filter
		 * is selected. This filters are only on my user dashboard so, this method first check, if the user is
		 * currently on the my user dashboard. If the user is the method will continue with the other logic.
		 * The refresh is done by clicking on the refresh icon in the dashlet.
		 *
		 * @param element
		 * 			  the clicked icon
		 */
		refreshDashlet : function(element){
			if($('.user-dashboard')[0]){
				var dashlet = element.closest('.dashboard-panel');
				var selectedFilter = dashlet.find('.filter-menu-tool .select2-container').select2('val');
				if((selectedFilter === 'myFavorites' && element.hasClass('dislikes')) ||
						(selectedFilter === 'myDownloads' && element.hasClass('download'))){
					dashlet.find('.glyphicon-refresh').click();
				}
			}
		}

};

/**
 * Bookmark utils.
 */
EMF.bookmarks = {

	caseTab: {
		dashboard: 'case-dashboard',
		documents: 'case-documents',
		workflow: 'case-workflow',
		objects: 'case-objects',
		details: 'case-details'
	},

	projectTab: {
		details: 'projectinstance'
	},

	/**
	 * Creates a bookmark link.
	 * @param type
	 * 			Instance type (like: caseinstance, projectinstance, sectioninstance,...)
	 * @param id
	 * 			Instance id
	 * @param tab
	 * 			Tab to be selected (for now only for cases).
	 * 			Available tab names: case-dashboard, case-documents, case-workflow, case-relations, case-objects.
	 */
	buildLink: function(type, id, tab) {
		if(!type || !id) {
			return window.location.href;
		}
		var link = SF.config.contextPath + '/entity/open.jsf?type=' + type + '&instanceId=' + id;
		if (tab) {
			link += '&tab=' + tab;
		}
		return link;
	},
	
	/**
	 * Same as EMF.bookmarks.buildLink(...), but also appends a print query param with value true.
	 */
	buildPrintLink: function(type, id) {
		return EMF.bookmarks.buildLink(type, id) + '&print=true';
	}
};

/**
 * Custom ui effects.
 */
EMF.effects = {

	flyOut: function() {
		if ($ && !$.fn.flyOut) {
		    $.fn.flyOut = function() {
				this.each(function() {
				    var toggle = $(this);
				    var flyout = toggle.next('.flyout-menu');
				    if (!toggle.attr("clickAdded") == true) {
				    	toggle.unbind('click');
				    	toggle.click(function(e) {
				    		e.preventDefault();
				    		var classes = $(this).attr("class");
				    		if (classes.indexOf("disabled") < 0) {
								if (!toggle.hasClass('menu-open')) {
								    $('.flyout-menu').hide();
								    $('.flydown-handle').removeClass('menu-open');
								}
								$('.flyout-menu a').click(function() {
								    $('.flyout-menu').hide();
								    $('.flydown-handle').removeClass('menu-open');
								});
								flyout.toggle();
								toggle.toggleClass('menu-open');
				    		}
				    	});
				    }
				    toggle.attr("clickAdded", true);
				});
		    };
		    $('.flydown-handle').flyOut();

		    $(document).mouseup(function(e) {
				var tmp = $(e.target);
				if (tmp.closest(".flyout-menu, .flydown-handle").length === 0 && !tmp.hasClass('flydown-handle')) {
				    $('.flyout-menu').hide();
				    $('.flydown-handle').removeClass('menu-open');
				}
			 });
		}
	},

	selectOne: function() {
	    var enableClick = function(e) {
			e.preventDefault();
			var parent = $(this).parents('.switch');
			$('.cb-disable', parent).removeClass('selected');
			$(this).addClass('selected');
			$('.switch-value', parent).val('1');
			return false;
	    };
	    $(".cb-enable").unbind('click', enableClick);
	    $(".cb-enable").click(enableClick);

	    var disableClick = function(e) {
	    	e.preventDefault();
	    	var parent = $(this).parents('.switch');
	    	$('.cb-enable', parent).removeClass('selected');
	    	$(this).addClass('selected');
	    	$('.switch-value', parent).val('0');
	    	return false;
	    };
	    $(".cb-disable").unbind('click', disableClick);
	    $(".cb-disable").click(disableClick);
	},

	// TODO: this is used only in commenting functionality and should be integrated inside
	accordion: function() {
		if($ && !$.fn.Accordion) {
		    $.fn.Accordion = function() {
				this.each(function(n) {
				    var handle = $(this);
				    var panel = $('.tabs-content').eq(n).hide();
				    handle.click(function(e) {
					e.preventDefault();
					if (!handle.hasClass('activeFilter')) {
					    $('.tabs-content').hide();
					    $('.tabs-head li').removeClass('activeFilter');
					}
					handle.toggleClass('activeFilter');
					panel.toggle();
				    });

				});
		    };
		    $('.tabs-head li').Accordion();
		}
	}
};

/**
 *
 */
EMF.tooltips = {
	initTooltip : function(){
		var tooltipElements = $('[data-toggle~="tooltip"]');
		tooltipElements.tooltip({
	       container: 'body',
	       delay: { show: 500, hide: 100 }
		});
	}
};

/**
 *  Request help form - functionality for sending mail
 *  to current administrator.
 */
EMF.requestHelp = {

	// Initialize from instruments.
	initRequestHelp : function(){
		// TinyMCE editor.
		tinymce.init({
		    selector: "textarea",
		    plugins: [
		        "advlist autolink lists link image charmap anchor"
		    ],
	    	menubar: false,
			statusbar: false
		});
	},

	//TBD: request help validation .
	validateHelpData : function(subject, type, body){
		var subjectLength = $.trim(subject).length;
		if(!subjectLength){
			return false;
		}
		return true;
	},

	// Button actions.
	buttonActions : {
		save : function() {
			var holders = $('.request-help-panel .cmf-field-wrapper');
			var subject = holders.eq(0).find('input');
			var type = holders.eq(1).find('select');
			var description = holders.eq(2).find('iframe');
			description = description.contents().find('body');

			var isFormValid = EMF.requestHelp.validateHelpData(subject.val(), type.val(), description.html());

			if(isFormValid) {
				// generate request service data
				var data = {
					subject : subject.val(),
					type : type.val(),
					description: description.html()
				};

				// send request to the service
				$.ajax({
					url 		: SF.config.contextPath + "/service/helpRequest/",
					type		: 'POST',
					data		: JSON.stringify(data),
					contentType	: EMF.config.contentType.APP_JSON
				}).done(function(data, textStatus, jqXHR) {
	                EMF.dialog.notify({
	                    title        : ' ',
	                    message      : data.responseText || _emfLabels['cmf.help.request.from.message.success'],
	                    customStyle  : 'ok-message',
	                    confirm: function() {
	                    	$('#sendHelpButton').next().click();
	                    	return true;
	                    }
	                });
				}).fail(function(data, textStatus, jqXHR) {
					EMF.dialog.notify({
	                    title        : ' ' + jqXHR,
	                    message      : data.responseText || 'fail',
	                    customStyle  : 'warn-message',
	                    confirm: function() {
	                    	return true;
	                    }
	                });
	            });
			} else {
				subject.parent().find('.error-message').remove().end().end()
					.after('<div class="error-message">' + _emfLabels['cmf.msg.error.requiredField'] + '</div>');
			}
			return false;
		}
	}
};

/**
 * General click handler dispatcher.
 */
EMF.clickHandlers = {

	// toggles user menu
	toggleUserMenu : function(trigger) {
		if (trigger.next('.user-menu-options').is(':visible')) {
			$('.user-menu-options').hide();
		} else {
			$('.user-menu-options').hide();
			trigger.next('.user-menu-options').toggle();
		}
	}

};

/**
 * Basic search and object picker init functions.
 */
EMF.search = {
	config : function() {
		return {
			initialSearchArgs: {
				pageSize: EMF.config.pagerPageSize
			},
			selectionChangedCallback: function(data) {
		        // trigger an event that the selection was changed in order to allow some actions upon that
		        data.srcElement.trigger('object-picker.selection-changed', data);
		    },
		    onBeforeSearch: [],
		    onAfterSearch: []
		};
	},

	basic : {
		init : function(placeholder, config) {
			$(placeholder).basicSearch(config || EMF.search.config());
		}
	},

	picker : {

		/**
		 * Disable/enable all buttons.
		 */
		toggleButtons : function(disabled) {
			if (disabled) {
				$(".ui-dialog-buttonpane button")
					.each(function() {
						var $this = $(this);
						if (!$this.hasClass('always-enabled')) {
							$this.attr("disabled", true).addClass("ui-state-disabled");
						}
					});
			} else {
				$(".ui-dialog-buttonpane button").attr("disabled", false).removeClass("ui-state-disabled");
			}
		},

		/**
		 * Disable/enable button.
		 */
		toggleButton : function(buttonName, disabled) {
			var selector = ".ui-dialog-buttonpane button:contains('" + buttonName + "')";
			if (disabled) {
				$(selector)
					.each(function() {
						var $this = $(this);
						if (!$this.hasClass('always-enabled')) {
							$this.attr("disabled", true).addClass("ui-state-disabled");
						}
					});
			} else {
				$(selector).attr("disabled", false).removeClass("ui-state-disabled");
			}
		}

	},

	selectedItems : {}
};

/**
 * Quick search field handler.
 */
EMF.search.quick = {

	// Construct search url, using the application context.
	searchUrl			: '/search/basic-search.jsf',
	// selectors constant
	SEARCH_RIGHT_MENU	: '#quickSearchInput',
	SEARCH_BUTTON		: '#quickSearchBtn',
	BASIC_SEARCH_BUTTON	: '.search-btn',

	init: function() {
		var searchInput = $(EMF.search.quick.SEARCH_RIGHT_MENU),
			compiledHeaderTemplate = _.template('<div class="tree-header compact_header">\
						    	<div class="instance-header <%= emfType %>">\
						    		<span class="icon-cell">\
						    			<img class="header-icon" src="<%= iconPath %>" width="16">\
						    		</span>\
						    		<span class="data-cell"><%= compactHeader %></span>\
						    	</div>\
						     </div>');

		/**
		 * Catch the event when key is pressed.
		 */
		searchInput
			.keypress(function(event) {
				// if enter pressed do use jQuery to click the search link
				var ENTER = 13;
				if ( event.which === ENTER ) {
					// Prevent default action and substitute for own.
					event.preventDefault();
					EMF.search.quick.executeSearch();
				}
			});

		// refactoring is needed
		searchInput.autocomplete({
			appendTo: '.quick-search',
			delay: 500,
			source: function( request, response ) {
				var term = request.term;

		        $.ajax({
		        	url: EMF.servicePath + '/search/basic',
		        	dataType: "json",
		        	data: {
		        		metaText: term,
		        		pageSize: 20
		        	},
		        	success: function( data ) {
		        		var result = data.values,
		        			values = [ ];

		        		if (result && result.length) {

		        			for(i=0; i < result.length; i++){
		        				var params,
			        			href,
			        			link,
			        			headerJq,
			        			header;

		        				if(result[i].compact_header==undefined){
		        					continue;
		        				}

		        				headerJq = $('<div>' + result[i].compact_header + '</div>');
		        				link = headerJq.find('a');
		        				link.removeClass('has-tooltip');
		        				href = link.attr('href');
		        				params = $.deparam(href.substr(href.indexOf('?') + 1));
		        				header = compiledHeaderTemplate({
		        					emfType: params.type,
		        					iconPath: EMF.util.objectIconPath(params.type, 16),
		        					compactHeader: headerJq.html()
		        				});

		        				values.push({
		        					label: headerJq.text(),
		        					header: header,
		        					value: headerJq.text(),
		        					instanceId: params.instanceId,
		        					instanceEmfType: params.type
		        				});
		        			}

		        			response(values);
		        		}
		        	}
		        });
			},
		    minLength: 3,
		    select: function(event, ui) {
		    	event.preventDefault();
		    	window.location = EMF.bookmarks.buildLink(ui.item.instanceEmfType, ui.item.instanceId);
		    },
		    // prevent value inserted on focus
	        focus: function() { return false; }
		});

		var autocomplete = searchInput.data("ui-autocomplete"),
			itemRenderer = autocomplete._renderItem;

		autocomplete._renderItem = function(ul, item) {
			var renderedItem = itemRenderer(ul, item);
			renderedItem.find('a').html(item.header);
			return renderedItem;
		};

		/**
		 * Add click handler.
		 */
		$(EMF.search.quick.SEARCH_BUTTON).click(function() {
			EMF.search.quick.executeSearch();
		});
	},

	/**
	 * Executes the search. Reads the arguments from the input and sets them as
	 * request parameters to the search URL.
	 */
	executeSearch: function() {
		CMF.utilityFunctions.setRootInstance(EMF.documentContext.rootInstanceId, EMF.documentContext.rootInstanceType, EMF.documentContext.rootInstanceTitle);
		// Get parameters from the input
		var searchParamsValue = $(EMF.search.quick.SEARCH_RIGHT_MENU).val();
		var searchParamsKey = 'metaText';
		// Create new object and serialize it.
		var params = {};
		params[searchParamsKey] = searchParamsValue;
		var serializedParams = $.param(params);
		// build the search url
		var searchWithQuery = EMF.applicationPath + EMF.search.quick.searchUrl + "?" + serializedParams;
		window.location.href = searchWithQuery;
		// trigger search event. The params from the URL are read in the init search method.
		$(EMF.search.quick.BASIC_SEARCH_BUTTON, this.element).click(function() {
			_this.element.trigger('perform-basic-search');
		});
	}
};

/**
 * Inplace ajax loader/blockui.
 */
EMF.ajaxloader = {

	showLoading : function(target) {
		if(!target) {
			return;
		}
		var element = $(target);
		if(element.length === 0) {
			return;
		}
		// don't add loader if already exists
		if(element.siblings('.loading-mask').length > 0) {
			return;
		}

		$('<div class="loading-mask">&#160;</div>').insertAfter(element)
        	.hide()
           	.css({
           		'position' : 'absolute',
           		'top' : element.position().top + 'px',
                'left' : element.position().left + 'px',
                'width' : element.outerWidth() + 'px',
                'height' : element.outerHeight() + 'px',
                'background-image' : 'url("' + SF.config.contextPath + '/images/ajax-loader-cmf.gif")',
                'background-repeat' : 'no-repeat',
                'background-position' : 'center center',
                'background-color' : '#888888',
                'opacity' : '0.3'
           	}).fadeIn();
	},

    hideLoading : function(target) {
       	if(!target) {
       		return;
       	}

        $(target).siblings('.loading-mask').fadeOut(function() {
        	$(this).remove();
        });
    }
};

EMF.shorteningPlugin = {

	init : function(){
		if(!EMF.shorteningPluginInstance){
			var shorteningOptions = {
				 moreLinkStyle 		: 'color: #4F94C9; font-size: 11px;',
				 selector	      	: '.truncate-element'
			};
			EMF.shorteningPluginInstance = $(document.body).EmfShorteningPlugin(shorteningOptions);
		}
	},

	shortElementText : function(){
		EMF.shorteningPluginInstance.elementTransform();
	}
};

/**
 * Notification module. Rises confirmation dialogs for any server error or any message that
 * should be displayed to user and is queued trough the NotificationSupport API.
 * NotificationSupport provides list with messages and renders them inside the html body. The
 * plugin collects them on page load and for every list item builds a popup dialog.
 * According to configuration the dialog may have confirm button. If confirm button is present
 * then it will send ajax request to controller which rises a CDI event with the corresponding
 * item/message id.
 */
EMF.notify = {

	config: {
		debugEnabled: false,
		// if set to true, then no action will be performed on dialog closing
		dontClearMessages: false,
		labels: {},
		buttons: []
	},

    /**
     * Collects data from notifications container filled up by the controller and
     * rises a popup for every message that is found.
     */
	init : function(opts) {
		if (opts) {
			opts.labels = {
				'info-message' : _emfLabels['notification.level.info'],
				'warn-message' : _emfLabels['notification.level.warn'],
				'error-message': _emfLabels['notification.level.error'],
				'fatal-message': _emfLabels['notification.level.fatal']
			};
			EMF.notify.config = $.extend(true, {}, EMF.notify.config, opts);
		}
		var messagesHolder = $('#notifications');

		if(EMF.notify.config.debugEnabled) {
			console.log('Initialized notification plugin. Found [', messagesHolder, '] notifications.');
		}

		var level = messagesHolder.find('.level');
		var labels = EMF.notify.config.labels;

		if (level.length > 0) {
			var scope = this;
			// loop all level message elements
			
			for (var l = 0; l < level.length; l++) {
				var messages = $(level[l]).find('.message');
				// loop all message elements
				for (var z = 0; z < messages.length; z++) {
					var msg = $(messages[z]);
					var uniqueClass = msg.attr('class').split(' ')[1];
					var isModalActive = scope.checkMessageIsActive(uniqueClass);
					if(isModalActive) {
						continue;
					}
					var msgLvl = $(level[l]).attr('lvl');
					var notificationTitle = labels[msgLvl];

					// configuration data for constructing notification modal dialog
					var data = {
						type			 	: 'notifyme',
						title			 	: notificationTitle,
						message			 	: msg,
						messageId		 	: msg.attr('msg-id'),
						level			 	: msgLvl,
						buttonLabel		 	: msg.attr('btn-lbl'),
						modal			 	: (msg.attr('msg-modal') === 'true'),
						pointTo	    	 	: uniqueClass,
						autoCloseTimeout 	: msg.attr('data-autoClose'),
						identifier			: msg.attr('data-identifier'),
						customOKButton	 	: msg.attr('data-customOk'),
						customCancelButton	: msg.attr('data-customCancel')
					};
					// construct modal dialog(notification)
					// and show it to the client
					var content = $(scope.createHtml(data)).appendTo(document.body).find('.notifier-content').append(msg);
					var target = '.' + data.type + ' a';
					content.on ('click', target, function() {
						EMF.notify.close();
					});
					scope.openPopup(content, data);
				// end of message loop
				}
			// end of level elements loop
			};
		}

		// bind a reset function to unload event in order to allow the messages container to be reset
		$(window).off('unload').on('unload', function() {
			$('#notifications').empty();
		});
	},

	checkMessageIsActive : function(styleClass){
		var notifyContentSelector = '.notifier-content.';
		notifyContentSelector += styleClass;
		notifyContentSelector = $(notifyContentSelector);
		return notifyContentSelector.length;
	},

	createHtml : function(data) {
		var pointTo = data.pointTo || '';
		var popup =  '<div class="notifier ' + data.level + '" title=" ">';
			popup += 	'<div class="notifier-content ' + pointTo + '"></div>';
			popup += '</div>';
		return popup;
	},

	close : function() {
		if(!EMF.notify.config.dontClearMessages) {
			$.ajax({
				type: 'DELETE',
				url : SF.config.contextPath + '/service/notification'
			}).done(function() {
				// - successfully cleared on server
				// - clear the notifications component on the page
				$('#notifications').empty();
				if(EMF.notify.config.debugEnabled) {
					console.log('Successfuly cleared notifications.');
				}
			}).fail(function() {
				if(EMF.notify.config.debugEnabled) {
					console.log('Error while clearing notifications!');
				}
			});
		}
		// close the dialog and remove element from the dom
		$( this ).dialog( "destroy" ).remove();
		EMF.util.hideAllIFrames(false);
		return false;
	},
	
	openPopup : function(popup, config) {
		var dialogClass = 'no-close notifyme autoClose-' + config.identifier + ' custom ' + config.level;
		var popupConfig = {
			resizable 		: false,
			height		 	: 'auto',
			width			: 'auto',
			modal 			: config.modal,
			title			: config.title,
			closeOnEscape	: false,
			dialogClass		: dialogClass,
			buttons 		: [],
			open: function(event) {
			    $(event.target).parent().removeClass('ui-corner-all').find('.ui-corner-all').removeClass('ui-corner-all');
			    EMF.util.hideAllIFrames(true);
			},
		
			close: function() {
				EMF.notify.close();
			}
		};
		popupConfig.buttons.push({
			text 	: _emfLabels['notification.ok.button'],
			'data-confirm' : 'confirm',
			priority: 'primary',
			click : function() {
				$(this).dialog("close");
			}
		});
		if (config.messageId) {
			// hide the panel close button if messageId is provided and close the panel
			// trough the action button only
			popupConfig.dialogClass += " no-close";

			// add action button only if there is messageId provided
			popupConfig.buttons.push({
				text 	: config.buttonLabel,
				priority: 'primary',
				click : function() {
					$.ajax({
						url : SF.config.contextPath + '/service/notification/handle',
						data: { messageId: config.messageId }
					}).done(function() {
						// successfully handled on server
						if(EMF.notify.config.debugEnabled) {
							console.log('Successfuly handled notification.');
						}
					}).fail(function() {
						if(EMF.notify.config.debugEnabled) {
							console.log('Error on notification handling on server.');
						}
					});

					EMF.notify.config.dontClearMessages = true;
					// close the dialog and remove element from the dom
					$( this ).dialog( "close" ).remove();
					// clear the notifications from the component
					$('#notifications').empty();
					return false;
				}
			});
		}
		// show dialog
		popupConfig.buttons = EMF.notify.buttonsBuilder(config, popupConfig);
		// show dialog
		popup.dialog(popupConfig);

		if(config.autoCloseTimeout > 0){
			setTimeout(function(){
				$('.autoClose-' + config.identifier + ' .ui-button').click();
			}, config.autoCloseTimeout);
		}
	},

	/**
	 * Builds buttons for the notification popup. It can support custom buttons, passed as a object,
	 * which can be build in another function. This function can be passed in the notification message by name.
	 * The name is set in the data attribute of the message. This builder extracts the this names and then tries to
	 * execute function with the same name. This function must return object with defined format. If function name is
	 * not found in the data attributes, the default button will be build so the notification can be closed. Also if
	 * the passed name is not found or it is not function, the default button will be build, to prevent the case, where
	 * the notification can't be closed.
	 *
	 * Custom button function format:
	 * <pre>
	 * EMF.buildCustomOK = function(){
	 *		return {
 	 * 			text 	: _emfLabels['notification.ok.button'],
 	 *			'data-confirm' : 'confirm',
 	 *			priority: 'primary',
 	 *			click : function() {
 	 *				console.log('I am custom click function.');
	 *			}
	 *		};
	 * };
	 * </pre>
	 */
	buttonsBuilder : function (config ,popupConfig) {
		var buttons = [];

		if(config.customOKButton && config.customCancelButton){
			buttons.push(EMF.notify.executeFunction(config.customOKButton));
			buttons.push(EMF.notify.executeFunction(config.customCancelButton));

		}else if(config.customCancelButton){
			buttons.push(EMF.notificationButtons.buildDefaultOK());
			buttons.push(EMF.notify.executeFunction(config.customCancelButton));

		}else if(config.customOKButton){
			buttons.push(EMF.notify.executeFunction(config.customOKButton));

		}else{
			buttons.push(EMF.notificationButtons.buildDefaultOK());
			if (config.messageId) {
				// hide the panel close button if messageId is provided and close the panel
				// trough the action button only
				popupConfig.dialogClass += " no-close";

				// add action button only if there is messageId provided
				buttons.push({
					text 	: config.buttonLabel,
					priority: 'primary',
					click : function() {
						$.ajax({
							url : SF.config.contextPath + '/service/notification/handle',
							data: { messageId: config.messageId }
						}).done(function() {
							// successfully handled on server
							if(EMF.notify.config.debugEnabled) {
								console.log('Successfuly handled notification.');
							}
						}).fail(function() {
							if(EMF.notify.config.debugEnabled) {
								console.log('Error on notification handling on server.');
							}
						});

						EMF.notify.config.dontClearMessages = true;
						// close the dialog and remove element from the dom
						$( this ).dialog( "close" ).remove();
						// clear the notifications from the component
						$('#notifications').empty();
						return false;
					}
				});
			}
		} // else end
	return buttons;
	},//end of build buttons

	/**
	 * Executes java script function by name. Checks if the name exists, if so it will execute the function and return
	 * the result from it. If the name is not a name of function it will return default close button. This will prevent
	 * the scenario, where is no way of closing the notification.
	 */
	executeFunction : function(functionName){
		if(EMF.notificationButtons.hasOwnProperty(functionName)){
			return EMF.notificationButtons[functionName]();
		}
		// this will build close button if the function is not found, so the notification can be closed
		return EMF.notificationButtons.buildDefaultOK();
	}
};

/**
 * Contains method for button action in notification messages. This object can be extended with custom functions
 * for the buttons and then used in the notifications.
 */
EMF.notificationButtons = {

		/**
		 * Builds default 'OK' button for the notification popup.
		 * This button just close the popup.
		 */
		buildDefaultOK : function(){
			return {
	 			text 	: _emfLabels['notification.ok.button'],
	 			'data-confirm' : 'confirm',
	 			priority: 'primary',
	 			click : function() {
	 				$(this).dialog("close");
				}
			};
		}// end of buildDefaultOK

};

/**
 * Confirmation plugin. Opens a modal/less dialog preconfigured with custom attributes.
 */
EMF.dialog = {

    config: {
    	// A dialog title string.
        title       : null,
        // Can contain html string and if provided, it will be appended to dialog header.
        // This will be added to header only if 'title' is not provided.
        htmlTitle	: null,
        // The dialog content. Can contain html.
        message     : '',
        // can be DOM element or jquery object and if provided, the dialog will wrap this element
        // this way it is possible to keep DOM clean and the element can be constructed with js code
        // and removed when dialog is closed
        content		: null,
        // if set an element with that selector will be used as dialog content
        elementSelector: null,
        // button label
        okBtn       : 'OK',
        // button label
        cancelBtn   : 'Cancel',
        // if only OK button should be rendered
        notifyOnly  : false,
        // if dialog should have no buttons panel
        noButtons	: false,
        modal       : true,
        // If dialog should close on overlay mask click
        closeOnMaskClick: true,
        // TODO: consider width to be 'auto'
        width       : 300,
        // TODO: consider height to be 'auto'
        height      : 'auto',
        resizable   : false,
        // Default dialog css class.
        dialogClass : 'no-close notifyme custom ',
        // A css class to be added to dialog container. It can be used for additional specific styling.
        // There are some predefined css classes that adds some visual styles and header icons.
        // Predefined classes as follows: info-message, warn-message, error-message, fatal-message
        customStyle : '',
        // Dialog default position.
        position    : ['center', 'center'],
        // Default dialog open handler.
        open: function( event, ui ) {},
        // This function may be overriden to provide custom functionality.
        // This is called on dialog close button click.
        // The dialog is closed after this function is executed.
        close: function(event, ui) {},
        // Before close dialog handler.
        beforeClose: $.noop,
        // This function may be overriden to provide custom functionality.
        // This is called on confirm/ok button click.
        // The dialog is closed after this function is executed.
        confirm: $.noop,
        // This function may be overriden to provide custom functionality.
        // This is called on cancel button click.
        // The dialog is closed after this function is executed.
        cancel: $.noop
    },

    init: function(opts) {
    	var config = $.extend(true, {}, EMF.dialog.config, opts);
    	var dialogObject;

    	config.okBtn = _emfLabels['cmf.btn.confirm'];
    	config.cancelBtn = _emfLabels['cmf.btn.cancel'];

    	var buttons = config.buttons || [];
    	if(!config.noButtons) {
			buttons.push({
				id: 'okBtn',
				text    : config.okBtn,
				priority: 'primary',
				click   : function(evt) {
					if (config.confirm(dialogObject, evt) !== false) {
						$(this).dialog('destroy').remove();
					}
				}
			});

    		if(!config.notifyOnly) {
	            buttons.push({
	            	id: 'cancelBtn',
	                text    : config.cancelBtn,
	                priority: 'secondary',
	                click   : function(evt) {
	                    if (config.cancel(dialogObject, evt) !== false) {
	                    	$(this).dialog('destroy').remove();
	                    	EMF.util.hideAllIFrames(false);
	                    }
	                }
	            });
	        }
    	}

        var dialogConfig = {
                title       : config.title,
                modal       : config.modal,
                width       : config.width,
                height      : config.height,
                resizable   : config.resizable,
                dialogClass : config.dialogClass + config.customStyle,
                position    : ['center', 'center'],
                open        : function(evt, ui) {
                	config.open(evt, ui);
                	$(evt.target).parent().removeClass('ui-corner-all').find('.ui-corner-all').removeClass('ui-corner-all');
                	EMF.util.hideAllIFrames(true);
                	if(config.closeOnMaskClick) {
                		$('.ui-widget-overlay').on('click', function () {
                			$(this).siblings('.ui-dialog').find('.ui-dialog-content').dialog('close');
                		});
                	}
                },
                close       : function() {
                    if (config.close() !== false) {
                    	$(this).dialog('destroy').remove();
                    	EMF.util.hideAllIFrames(false);
                    }
                },
                beforeClose : config.beforeClose,
                buttons     : buttons
        };

        // Update dialog after it was created.
        dialogConfig.create = function(event) {
        	// If some custom style is added, then update header with icon element
	        if(config.customStyle) {
	        	$(event.target).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').prepend('<span class="header-icon"></span>');
	        }
	        // Append htmlTitle to dialog header if no title is provided.
	        if(!config.title && config.htmlTitle) {
	        	$(event.target).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').append(config.htmlTitle);
	        }
        };

        var element;
        if (config.elementSelector) {
        	element = config.elementSelector;
        } else if(config.content) {
        	element = config.content;
        } else {
        	element = '<div>' + config.message + '</div>';
        }

        dialogObject = $(element).dialog(dialogConfig);
    },

    /**
     * Standard dialog screen that can be configured according to user needs.
     *
     * @param opts
     * 			Configuration options object that will be merged with default configuration.
     */
    open : function(opts) {
    	EMF.dialog.init(opts);
    },

    /**
     * Dialog preconfigured to work as confirmation.
     *
     * @param opts
     * 			Configuration options object that will be merged with default configuration.
     */
    confirm: function(opts) {
    	opts.notifyOnly = false;
    	EMF.dialog.init(opts);
    },

    /**
     * Dialog preconfigured to work as notification popup. Only 'ok' button will be shown.
     *
     * @param opts
     * 			Configuration options object that will be merged with default configuration.
     */
    notify: function(opts) {
    	opts.notifyOnly = true;
    	EMF.dialog.init(opts);
    },

    /**
     * Opens a dialog without any buttons. Just the close button exists.
     *
     * @param opts
     * 			Configuration options object that will be merged with default configuration.
     */
    popup: function(opts) {
    	opts.noButtons = true;
    	EMF.dialog.init(opts);
    },

	/**
	 * Disable/enable button.
	 *
	 * @param buttonId
	 * 			As in EMF.dialog.config - can be 'okBtn', 'cancelBtn' or another id of a button
	 * @param disabled
	 * 			If button to be disabled or enabled
	 */
	toggleButton : function(buttonId, disabled) {
		var selector = ".ui-dialog-buttonpane #" + buttonId;
		if (disabled) {
			$(selector).attr("disabled", true).addClass("ui-state-disabled");
		} else {
			$(selector).attr("disabled", false).removeClass("ui-state-disabled");
		}
	},

	/**
	 * Resets basic configurations to passed dialog configuration.
	 *
	 * @param config
	 * 			the dialog configuration, which will be reset
	 */
	reset : function(config){
		config.noButtons = false;
		config.content = '';
		config.buttons = [];
	}

};

// REVIEW: for refactoring
EMF.blockUI = {
	// selectors for links that should not trigger the block ui
	forbiddenLinks: [
         '.selector-activator-link',
	     '#sortingOptionsMenu',
	     '.more-actions-link',
	     '.mceButton',
	     '.mceText',
	     '.mceOpen',
	     '.image-annotation-container a',
	     '.image-annotation-controls a',
	     '.discussion-panel a',
	     '.discussion-panel input',
	     '.allowed-action-image-button a',
	     '.tree-header a[href="javascript:void(0)"]',
	     '.relations-wgt-wrapper a.x-btn',
	     '.relations-wgt-property-editor a.x-btn-button'
	],

	initBlockUI : function(){

		// page blocker image suffix
		var imagePath = EMF.applicationPath + "/images/ajax-loader-cmf.gif";

		// page blocker options
		var ajaxLoaderOptions = {

		    // filtering all elements that will invoke the page blocker
			elementFilters : $(),

			// applying the path to the page blocker image
			imagePath : imagePath
		};

		ajaxLoaderOptions.elementFilters = ajaxLoaderOptions.elementFilters
			.add($('a').not(this.forbiddenLinks.join(',')));

		ajaxLoaderOptions.elementFilters = ajaxLoaderOptions.elementFilters
			.add($('input.standard-button').not('.close-dialog-btn'));

		// execute the page blocker
		EMF.ajaxGUIBlocker = $(document.body).EmfAjaxLoaderHandler(ajaxLoaderOptions);
	},

	togglePageBlocker: function(event) {

		// check if page blocker is active
		if (EMF.config.pageBlockerOFF) {
			EMF.config.pageBlockerOFF = false;
			return;
		}

		if(event){
			// detect mouse 'right' buttons key
			var btnMauseValue = event.which;
			// control + click
			var btnCtrlClick = event.ctrlKey;

			if (btnMauseValue === 2 || btnMauseValue === 3 || btnCtrlClick) {
				return;
			}

			// current element that invoke ajax loader
			var invokerElement = $(event.target);

			// check if element should not trigger blockUI
			if(invokerElement.closest('.dontBlockUI').length > 0 || invokerElement.closest('.blockUI').length === 0) {
				return;
			}

			// show page blocker
			EMF.blockUI.showAjaxLoader('#content', 'inplace', 'fullHeight');
		} else {

			// hide page blocker
			EMF.ajaxGUIBlocker.hideAjaxLoader();

			// reload blocker identifiers(ensure this will happened after/if any AJAX operation)
			EMF.ajaxGUIBlocker.reloadBlockerIdentificators();
		}
	},

	// show the ajax loader layer over the selected element at chosen position
	showAjaxLoader : function(element, position, fullHeight) {
		// There's a problem with correct calculation of #content element height
		// The problem is caused by position:absolute which the vertical splitter add to #contentBody
		// and also from the scroll. The parameter fullHeight will be used to show that AjaxLoader have to be applied over the whole document height
		if(fullHeight) {
			EMF.ajaxGUIBlocker.options.fullHeight = true;
		}
		if(position && $(element).offset()) {
			EMF.ajaxGUIBlocker.options.loaderPosition = position;
		} else {
			EMF.ajaxGUIBlocker.options.loaderPosition = 'modal';
			element = 'html';
		}
		EMF.ajaxGUIBlocker.showAjaxLoader(element);
		// hiding the current tooltip after showing ajax blocker
		$('#tiptip_holder').hide();
	},

	/**
	 * Hides the ajax loader layer.
	 *
	 * @param reset If block ui layers counter should be reset inside the plugin.
	 */
	hideAjaxBlocker : function(reset) {
		// TODO: why timeout
		setTimeout(function() {
			EMF.ajaxGUIBlocker.hideAjaxLoader(reset);
		}, 250);
	},

	/**
	 * This method represent small factory for retrieving blocker functionality
	 * based on a valid component.
	 * - If the component is available will be used ajax-loader for the component.
	 * - If the component is not available will be used page blocker
	 */
	selectGUIBlocker: function(component){
		if(component){
			EMF.ajaxloader.showLoading(component);
		}else{
			EMF.blockUI.showAjaxLoader();
		}
	}
};

EMF.paging = {
	/**
	 * Method that remove content from <b>href</b> attribute and allowing
	 * the page to focus top part of the page.
	 */
	removeLinksHref : function(){
		var selector = '.cmf-datatable-scroller a';
		selector = $(selector);
		selector.each(function(){
			var current = this;
			current.href = '';
		});
	}
};

EMF.navigation = {
	/**
	 * 	Add query parameter for every page that we are leaving to know that it is
	 *  history page if user decides to return back.
	 */
	markHistoryPage: function() {
		if (history.replaceState) {
			var location = document.location.href;
			if(location.indexOf('?') === -1) {
				location += '?';
			}
			if (location.indexOf('history=false') !== -1) {
				location = location.replace('history=false', 'history=true');
			} else if (location.indexOf('history=true') === -1) {
				location += '&history=true';
			}
			var state = {
				orgUrl: document.location.href
			};
			history.replaceState(state, null, location);
		}
	},

	markCurrentPage: function() {
		if (history.replaceState) {
			var location = document.location.href;
			if(location.indexOf('?') === -1) {
				location += '?';
			}
			if (location.indexOf('history=false') === -1) {
				location += '&history=false';
			}
			var state = {
				orgUrl: document.location.href
			};
			history.replaceState(state, null, location);
		}
	},

	updateUrl: function() {
		// !!! Commented out because stil there are problems with the implementation.
//		// update address bar with the correct url
//		console.log('window.history.length: ', window.history.length);
//		if (EMF.documentContext.currentInstanceType && EMF.documentContext.currentInstanceId) {
//			var replace = sessionStorage.getItem('emf.replace.url');
//			console.info('push replace:', replace, (replace === null), (replace === 'true'));
//			if (replace === null || replace === 'true') {
//				var url = EMF.bookmarks.buildLink(EMF.documentContext.currentInstanceType, EMF.documentContext.currentInstanceId);
//				var state = {
//					orgUrl: document.location.href
//				};
//				console.info('replace location:', document.location.href, ' | url:', url);
//				history.replaceState(state, null, url);
//				sessionStorage.setItem('emf.replace.url', true);
//			}
//		}
//		window.onpopstate = function(event) {
//			var loc = document.location.href;
//			var orgUrl = event.state && event.state.orgUrl;
//			console.info('event.state:', event.state, ' | location:', loc, ' | orgUrl:', orgUrl);
////			if (!event.state || (orgUrl && orgUrl === loc)) {
////				console.log('BACK');
////				history.back();
////			}
//			sessionStorage.setItem('emf.replace.url', false);
//		};
	//
//		// update address bar with the correct url
//		if (EMF.documentContext.currentInstanceType && EMF.documentContext.currentInstanceId) {
//			var url = EMF.bookmarks.buildLink(EMF.documentContext.currentInstanceType, EMF.documentContext.currentInstanceId);
//			history.replaceState({}, null, url);
//		}
	}
};

/**
 * Registration mechanizm for external modules.
 */
EMF.modules = {

	registered: [],

	register: function(name, namespace, initFunction, order) {
		if (name && $.isPlainObject(namespace) && $.isFunction(initFunction)) {
			if(EMF.config.debugEnabled) {
				console.log('Register module:', arguments);
			}
			var moduleData = {
					name		: name,
					namespace	: namespace,
					init		: initFunction
				};
			var registered = EMF.modules.registered;
			// if order is greated than length of the array with currently registered modules
			// then the new module will be added to the end as if no order is provided
			if (order || (order === 0)) {
				registered.splice(order, 0, moduleData);
			} else {
				EMF.modules.registered.push(moduleData);
			}
		}
	},

	listRegistered: function() {
		var registered = EMF.modules.registered;
		var len = registered.length;
		console.log('Registered modules:');
		for (var i = 0; i < len; i++) {
			console.log(i, '. ', registered[i]);
		}
	},

	init: function() {
		if (EMF.modules.registered.length) {
			var registered = EMF.modules.registered;
			var len = registered.length;
			for (var i = 0; i < len; i++) {
				if(EMF.config.debugEnabled) {
					console.log('Initializing module [', registered[i], ']');
				}
				registered[i].init.call(EMF, EMF.config);
			}
		}
	}
};

EMF.init = function() {

    var b = document.documentElement;
    b.setAttribute('data-useragent',  EMF.util.navigator());
    b.setAttribute('data-os',  EMF.util.os());

	// Bind base click handler dispatcher
	$(EMF.config.mainContainerId).on('click', EMF.clickHandlerDispatcher);

	// Add here functions that should be called on every beginning jquery ajax request
	$(document).ajaxStart(function() {
		$(document).trigger(EMF.config.events.SESSION_TIMER_RESET);
	});

	// Binds favourites icons to handler
	$(document).on('click', '.header-icons .star, .header-icons .dislikes', EMF.favourites.handler);

	// Binds downloads icons to handler
	$(document).on('click', '.header-icons .download, .header-icons .circle_arrow_down', EMF.downloads.handler);

	// Init block ui
	EMF.blockUI.initBlockUI();

	// Reload page blocker identifiers
	EMF.ajaxGUIBlocker.reloadBlockerIdentificators();

	// Tree header
	$.treeHeader();

	EMF.shorteningPlugin.init();

	EMF.shorteningPlugin.shortElementText();

	EMF.effects.accordion();

	EMF.notify.init(EMF.config);

	EMF.navigation.markCurrentPage();

	EMF.modules.init();

	EMF.paging.removeLinksHref();

	if(EMF.config.debugEnabled) {
		EMF.modules.listRegistered();
	}
};


/**
 * Change user password helpers.
 */
EMF.password = {
	settings: {
		url: '/security/user/changepassword',
		onSuccessRedirect: '',
		messagesContainerSelector: '.change-password-messages'
	},

	addMessages: function(messages) {
		var messagesHtml = '',
			len = messages.length,
			container;

		for (var i = 0; i < len; i++) {
			messagesHtml += '<li>' + messages[i] + '</li>';
		}

		container = $(EMF.password.settings.messagesContainerSelector);
		container.children().remove();
		container.append(messagesHtml);
	},

	/**
	 * This function uses the 'argumets' var.
	 * If a single argument is passed and it's an Object then the following structure is assumed:
	 * 	{
	 * 		usermane: '...',
	 * 		oldpass: '...',
	 * 		newpass: '...',
	 * 		confirmnewpass: '...'
	 * 	}
	 *
	 * Where,
	 * 	username is the username of the user we are trying to change the password of,
	 * 	oldpass is the current users password,
	 * 	and newpass and confirmnewpass are the desired change of the password.
	 *
	 * Otherwise three string params must be provided
	 * 	CSS selector of the field containing the user's current password,
	 * 	CSS selector of the field containing the user's desired new password,
	 * 	CSS selector of the field containing the confirmation of the user's desired new password.
	 *
	 * In both cases:
	 * 	if the username is omitted the current user's username is used
	 * 	new password must match ^[\\S]{5,30}$
	 * 	new password and confirm new password must match
	 */
	change: function() {
		var userObject,
			errors = [],
			username = EMF.currentUser.username,
			currentPassword,
			newPassword,
			confirmNewPassword;


		if (arguments.length === 1) {
			userObject = arguments[0];
		} else if (arguments.length === 3) {
			currentPassword = $(arguments[0]).val();
			newPassword = $(arguments[1]).val();
			confirmNewPassword = $(arguments[2]).val();
			userObject = { };

			userObject.oldpass = currentPassword;
			userObject.newpass = newPassword;
			userObject.confirmnewpass = confirmNewPassword;
		}

		if (!userObject.oldpass) {
			errors.push(_emfLabels['change.password.errors.current_required']);
		}

		if (!userObject.newpass || !userObject.newpass.match(/^[\S]{5,30}$/)) {
			errors.push(_emfLabels['change.password.errors.new_required']);
		}

		if (!userObject.confirmnewpass) {
			errors.push(_emfLabels['change.password.errors.new_confirm_required']);
		}

		if (userObject.newpass !== userObject.confirmnewpass) {
			errors.push(_emfLabels['change.password.errors.dont_match']);
		}

		EMF.password.addMessages(errors);

		if (userObject && !errors.length) {
			userObject.username = userObject.username || username;

			$.post(
				EMF.servicePath + EMF.password.settings.url,
				userObject
			)
			.success(function(response) {
				$('.change-password-form').hide();
				$('.change-password-success').show();
			})
			.error(function(response) {
				var data,
					errors = [ ],
					key,
					errorMessages;

				if (response.responseText) {
					data = JSON.parse(response.responseText);
					errorMessages = data.messages;
					if (errorMessages) {
						for (key in errorMessages) {
							if (errorMessages.hasOwnProperty(key)) {
								errors.push(errorMessages[key]);
							}
						}
					} else {
						errors.push(data.message);
					}
					EMF.password.addMessages(errors);
				}
			});
		}
	},

	cancel: function() {
		window.location = EMF.applicationPath + EMF.password.settings.onSuccessRedirect;
	},

	onSuccessOkButton: function() {
		window.location = EMF.applicationPath + EMF.password.settings.onSuccessRedirect;
	}
};

/**
 * Function executed after ajax request from a4j:status component in default templates.
 */
EMF.onAfterAjax = function(evt) {
	if (EMF.blockUI.togglePageBlocker) {
		EMF.blockUI.togglePageBlocker(evt);
	}
	if (SessionTimer) {
		SessionTimer.init(EMF.config);
	}
	if (EMF.notify.init) {
		EMF.notify.init(EMF.config);
	}
	if (EMF.blockUI.initBlockUI) {
		EMF.blockUI.initBlockUI();
	}
	if (CMF.rnc.init) {
		CMF.rnc.init();
	}
	if (EMF.tooltips.initTooltip) {
		EMF.tooltips.initTooltip();
	}
	EMF.paging.removeLinksHref();
	EMF.shorteningPlugin.shortElementText();
	addSaveBtnAttribute();
};

/*
 * Adding onlick to save buttons to disable warning window while in edit mode
 */
// FIXME: move this in a common namespace (at most in EMF if no other more convinient)
function addSaveBtnAttribute() {
	$("input[id$='saveButton'], a[id$='saveButton']").attr('onclick', 'onbeforeunload=null');
	// Don't override existing onclick attribute if any!
	$("input[id$='transitionActionLink'], a[id$='transitionActionLink']").each(function() {
		var current = $(this);
		if(!current.attr('onclick')) {
			current.attr('onclick', 'onbeforeunload=null');
		}
	});
};

EMF.onbeforeload = {
	/**
	 * Registered onbeforeunload handler should be a function that returns boolean value which will
	 * be used to decide whether the confirmation should be rised.
	 */
	handlers : {
		'defaultHandler' : function() {
			return (document.getElementById('formId:saveButton') != null);
		}
	},

	preventBeforeLoadMessage : function(styleClassElement){
		var defaultStyleClassElement =  ".transitions-panel";

		if(styleClassElement && $.trim(styleClassElement) !== ''){
			defaultStyleClassElement = styleClassElement;
		}

		var eventsContainer = $(defaultStyleClassElement);

		eventsContainer.on('click',function(event){
			EMF.config.restrictBeforeLoadMessage = true;
		});
	}
};

EMF.onunload = {

		handlers : {'clearLinksHandler' : function(){
				/**
				 * When cancel the creation of some instance, there are semantic links that
				 * exists for the pre-generated id(before the actual persistence of the
				 * instance), that has to be removed. When the user attempts to leave or
				 * close the creation page, the confirmation dialog is opened. If the user
				 * choose to stay on the page the links are not removed. Else if the user
				 * choose to leave, it is sent service request for removing links
				 * for the id.
				 */
				if (EMF.config.clearLinks) {

					var data = {
						instanceId : EMF.documentContext.currentInstanceId,
						instanceType : EMF.documentContext.currentInstanceType
					};

					$.ajax({
						url : EMF.servicePath + "/instances/removeLinks",
						type : 'GET',
						data : data,
						contentType	: EMF.config.contentType.APP_JSON
					}).done(function() {
						if (EMF.notify.config.debugEnabled) {
							console.log('Links removing was successful.');
						}
					}).fail(function(data) {
						if (EMF.notify.config.debugEnabled) {
							console.log('Links removing was unsuccessful.');
						}
					});
				}
				return true;
			}
		}
};


/**
 * Handler for onbeforeunload event that checks registered handler functions and if any is resolved to boolean#true
 * show a confirmation to notify the user that he is going to leave the page.
 */
window.onbeforeunload = function() {

	// All widgets containing extjs tables are misaligned after onbeforeunload
	// and have to be recreated
	$('.idoc-editor').trigger('unload-canceled');

	EMF.navigation.markHistoryPage();
	var handlers = EMF.onbeforeload.handlers;
	var showConfirm = false;
	for ( var handlerName in handlers) {
		if (handlers.hasOwnProperty(handlerName)) {
			var execute = handlers[handlerName]();
			if (execute) {
				showConfirm = true;
				EMF.config.clearLinks = true;
				break;
			}
		}
	}
	if(EMF.config.restrictBeforeLoadMessage){
		EMF.config.restrictBeforeLoadMessage = false;
		showConfirm = false;
	}
	if (showConfirm) {
		// if there is a save button on the page, we should show confirmation to the user
		/**
		 * Fix for: https://ittruse.ittbg.com/jira/browse/CS-631
		 * The code within the first setTimeout method has a delay of 1ms. This is just to add the
		 * function into the UI queue. Since setTimeout runs asynchronously the Javascript interpreter
		 * will continue by directly calling the return statement, which in turn triggers the browsers
		 * modal dialog. This will block the UI queue and the code from the first setTimeout is not
		 * executed, until the modal is closed. If the user pressed cancel, it will trigger another
		 * setTimeout which fires in about one second. If the user confirmed with ok, the user will
		 * redirect and the second setTimeout is never fired.
		 * Solution taken from: http://stackoverflow.com/questions/4650692/way-to-know-if-user-clicked-cancel-on-a-javascript-onbeforeunload-dialog
		 */
        setTimeout(function() {
            setTimeout(function() {
            	EMF.ajaxGUIBlocker.hideAjaxLoader();
            	SessionTimer.reinit(EMF.config);
            }, 100);
        },1);
        EMF.ajaxGUIBlocker.hideAjaxLoader();
        return _emfLabels['cmf.exitconfirmation'];
	}
};

window.onunload = function(){

	//executing onunload handler
	var handlers = EMF.onunload.handlers;
	for(var handlerName in handlers){
		if (handlers.hasOwnProperty(handlerName)) {
			handlers[handlerName]();
		}
	}

};

/**
 * Start initialization.
 */
$(function() {
	EMF.init();
	addSaveBtnAttribute();
});