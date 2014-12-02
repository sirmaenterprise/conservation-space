$.comments = {
	EMF : {},
	idoc : {},
	authors : [""],
    /** Comments local storage. */
    map : {},
    /** Filtered comments local storage. */
    filteredMap : {},
    /** URL to service for loading comments. */
    loadService : 'http://www.sirma.bg/',
    /** URL to service for storing comments. */
    saveService : 'http://www.sirma.bg/',
    /** URL to service for editing comments. */
    editService : 'http://www.sirma.bg/',
    /** URL to service for removing comments. */
    removeService : 'http://www.sirma.bg/',
    /** Maximum length of comment of minimized message. */
    contentSize : 30,
    /** Delay for resizing comments. */
    debounceDelayResize : 10,
    /** Delay for refreshing comments. */
    debounceDelayRefresh : 50,
    /** Destination of images. */
    imagePathURL : "/application/images",
    /** Identifier of the main document. */
    rootId : "root",
    /** type of the root entity */
    rootType : "",
    /** Source with data for the main document. */
    rootSource : ".root",
    /** Source with data that could be commented. */
    source : '.editor',
    /** Destination of comments. */
    target : '.comment',
    /** Suffix of comment identifiers. */
    commentSuffix : 'Comments',
    /** Selectors for data that could be commented. */
    commentSelectors : 'h1,h2,h3,h4,h5,h6',
    /** The size of found topics. */
    topicSize : 0,
    /** Parameter for visualization of submit button. */
    viewSubmitButton : true,
    beforeCreateTopic	: $.noop,
    afterCreateTopic	: $.noop,
    cancelCreateTopic	: $.noop,
    statuses : [ {
	    value : 'IN_PROGRESS',
	    text : 'Opened'
	}, {
	    value : 'ON_HOLD',
	    text : 'Closed'
	} ],
	categories : [ {
	    value : 1,
	    text : 'Note to self'
	}, {
	    value : 2,
	    text : 'Editorial question/suggestion'
	}, {
	    value : 3,
	    text : 'Request assistance'
	} ],
    labels: {
    	deleteTopicConfirmationMessage: 'Are you sure you want to delete this topic?',
    	deleteReplyConfirmationMessage: 'Are you sure you want to delete this reply?'
    },
    /** filter criteria. */
    filterCriteria : {
		user : undefined,
		status : undefined,
		keyword : undefined,
		category : undefined,
		zoomLevel : undefined,
		dateFrom : undefined,
		dateTo : undefined,
		sortBy : "sortByComment"
    },
    tagsPlaceholder: 'Tags',
    allTagsOnPage: [],
    select2TagsConf: {
    	placeholder: this.tagsPlaceholder,
    	separator: '|',
		width: '100%',
		multiple: true
	},
    /** Preview mode. Set to true to disable actions. */
    previewMode : false,
    /** Filter to be performed on the server when loading comments. */
    loadFilter : {},
    /** Constant templates. */
    constants : {
	moreTemplate : '<div class="commentPreview btn-default lineContainer action more activePanel" style="text-align:center;height:%HEIGHT%px !important;"><div class="glyphicons sort"/>%DATA%</div>',
	newCommentTemplate : '\
<div class="action comment newComment nav-tabs" style="min-height:33px;height:%HEIGHT%px;">\
	<a href="javascript:$.comments.showCommentBox(\'%ID%\', null, true);" class="btn btn-default dontBlockUI blockUI">%TITLE%</a>\
</div>\
',
	commentTemplate : '\
<div class="comment" id="%ID%%SUFFIX%">\
	<div class="commentPreview btn-default lineContainer" style="overflow: hidden">\
		<div class="action" style="float: right; padding: 0 5px;">\
			<b>%DATE%</b>\
		</div>\
		<div class="action">\
			<div style="float: left; padding: 0 5px;">\
				<span style="vertical-align: middle; margin-right: 10px; font-size: 20px;" class="rotate135 glyphicon glyphicon-tag"></span>\
				<span><b>%AUTHOR% %REPLIER% <span class="unread">%ALL%</span></b></span>\
			</div>\
			<div style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 5px;">\
				%TITLE%\
			</div>\
		</div>\
	</div>\
	<div class="commentView btn-default lineContainer inactive">\
		<div class="action">\
			<div class="stop-click-propagation" style="float: right; padding: 3px;">\
				<div style="vertical-align: middle; display: inline-block; margin: 0 5px;">\
					<a href="#" id="%ID%category">%CATEGORY%</a>\
				</div>\
				<div style="vertical-align: middle; display: inline-block; margin: 0 5px;">\
					<a href="#" id="%ID%status">%STATUS%</a>\
				</div>\
				<div style="display: inline-block; vertical-align: middle; margin: 0 5px;">\
					<div class="glyphicons unshare thumbnail styledButton"> </div>\
					%ACTIONS%\
				</div>\
			</div>\
			<div class="action" style="padding: 3px; overflow: hidden">\
				<span style="vertical-align: middle; margin-right: 10px; font-size: 20px;" class="rotate135 glyphicon glyphicon-tag"></span>\
				<span>%TITLE%</span>\
			</div>\
		</div>\
		<div>\
			<input type="hidden" class="tags" value="%TAGS%" />\
		</div>\
		%REPLIES%\
	</div>\
</div>',
	replyTemplate : '\
<div class="reply thumbnail" id="%ID%%SUFFIX%">\
	<div class="replyPreview lineContainer" style="overflow: hidden">\
		<div class="action" style="float: right; padding: 3px;">\
			<b>%DATE%</b>\
		</div>\
		<div class="action" style="padding: 3px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">\
			<span style="margin-right: 5px;"><span class="glyphicons user thumbnail styledButton"></span> <b>%AUTHOR%</b></span>\
			<span id="short-txt-id">%SHORT_TEXT%</span>\
		</div>\
	</div>\
	<div class="replyView inactive">\
	    <div class="reply-meta-line">\
			<div class="action" style="float: right; padding: 3px;">\
				<b>%DATE%</b>\
				%ACTIONS%\
			</div>\
			<div class="action" style="padding: 3px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">\
				<span style="margin-right: 5px;"><span class="glyphicons user thumbnail styledButton"></span> <b>%AUTHOR%</b></span>\
			</div>\
	    </div>\
	    <div class="row modal-footer" style="word-wrap: break-word;"><div class="col-xs-12">%TEXT%</div></div>\
	</div>\
</div>',
		commentPreviewTemplate : '\
			<div class="comment" id="%ID%%SUFFIX%">\
				<div class="commentPreview btn-default lineContainer" style="overflow: hidden">\
					<div class="action" style="float: right; padding: 0 5px;">\
						<b>%DATE%</b>\
						<span class="icon-cell comment-entity stop-click-propagation">\
							<a class="has-tooltip" href="%LINK%"><img src="%ICON%" class="header-icon"/></a>\
						</span>\
					</div>\
					<div class="action">\
						<div style="float: left; padding: 0 5px;">\
							<span style="vertical-align: middle; margin-right: 10px; font-size: 20px;" class="rotate135 glyphicon glyphicon-tag"></span>\
							<span><b>%AUTHOR% %REPLIER% <span class="unread">%ALL%</span></b></span>\
						</div>\
						<div style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 5px;">\
							%TITLE%\
						</div>\
					</div>\
				</div>\
				<div class="commentView btn-default lineContainer inactive">\
					<div class="action">\
						<div class="action" style="float: right; padding: 3px;">\
							<b>%DATE%</b>\
							<span class="icon-cell comment-entity stop-click-propagation">\
								<a class="has-tooltip" href="%LINK%"><img src="%ICON%" class="header-icon"/></a>\
							</span>\
						</div>\
						<div class="action" style="padding: 3px; overflow: hidden">\
							<span style="vertical-align: middle; margin-right: 10px; font-size: 20px;" class="rotate135 glyphicon glyphicon-tag"></span>\
							<span>%TITLE%</span>\
						</div>\
					</div>\
					<div>\
						<input type="hidden" class="tags" value="%TAGS%" />\
					</div>\
					%REPLIES%\
				</div>\
			</div>',
		replyPreviewTemplate : '\
		<div class="reply thumbnail" id="%ID%%SUFFIX%">\
			<div class="replyPreview lineContainer" style="overflow: hidden">\
				<div class="action" style="float: right; padding: 3px;">\
					<b>%DATE%</b>\
				</div>\
				<div class="action" style="padding: 3px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">\
					<span style="margin-right: 5px;"><span class="glyphicons user thumbnail styledButton"></span> <b>%AUTHOR%</b></span>\
					%SHORT_TEXT%\
				</div>\
			</div>\
			<div class="replyView inactive">\
			    <div class="reply-meta-line">\
					<div class="action" style="float: right; padding: 3px;">\
						<b>%DATE%</b>\
					</div>\
					<div class="action" style="padding: 3px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">\
						<span style="margin-right: 5px;"><span class="glyphicons user thumbnail styledButton"></span> <b>%AUTHOR%</b></span>\
					</div>\
			    </div>\
			    <div class="row modal-footer" style="word-wrap: break-word;"><div class="col-xs-12">%TEXT%</div></div>\
			</div>\
		</div>'
    },

    /**
     * Updates filter criteria.
     *
     * @param criteria
     *                is map with all criteria
     */
    updateCriteria : function(criteria) {
    	$.comments.filterCriteria = criteria;
    },

    /**
     * Filters list of comments with specific filter and sort criteria.
     *
     * @param comments
     *                is array of comments for filtering
     * @param criteria
     *                is set of criteria
     * @return filtered and sorted comments
     */
    filter : function(comments, criteria) {
		var result = [];
		for ( var i = 0; i < comments.length; i++) {
		    var tmp = comments[i];
		    if (criteria.user && criteria.user != "") {
		    	tmp = $.comments.filterByUser(tmp, criteria.user);
		    }
		    if (criteria.status && criteria.status != "") {
		    	tmp = $.comments.filterByStatus(tmp, criteria.status);
		    }
		    if (criteria.keyword && criteria.keyword != "") {
		    	tmp = $.comments.filterByKeyword(tmp, criteria.keyword);
		    }
		    if (criteria.category && criteria.category != "") {
		    	tmp = $.comments.filterByCategory(tmp, criteria.category);
		    }
		    if (criteria.zoomLevel && criteria.zoomLevel != "") {
		    	tmp = $.comments.filterByZoomLevel(tmp, criteria.zoomLevel);
		    }
		    if ((criteria.dateFrom && criteria.dateFrom != "") || (criteria.dateTo && criteria.dateTo != "")) {
		    	tmp = $.comments.filterByDate(tmp, criteria.dateFrom, criteria.dateTo);
		    }
		    if (criteria.tags && criteria.tags.length) {
		    	tmp = $.comments.filterByTags(tmp, criteria.tags);
		    }
		    if (criteria.instanceType && criteria.instanceType != "") {
				tmp = $.comments.filterByInstanceType(tmp, criteria.instanceType);
			    }
		    if (tmp != undefined) {
		    	result.push(tmp);
		    }
		}
		if (!criteria.sortBy || criteria.sortBy == "sortByComment") {
		    result = $.comments.sortByLastCommentDate(result);
		} else {
		    result = $.comments.sortByCreationDate(result);
		}
		return result;
    },

    /**
     * Sorts by creation date.
     *
     * @param comments
     *                is array of comments for sorting
     * @return sorted comments
     */
    sortByCreationDate : function(comments) {
	var result = [];
	for ( var i = 0; i < comments.length; i++) {
	    var isAdded = false;
	    var current = Math.floor(new Date(comments[i].createdOn));
	    for ( var j = 0; j < result.length; j++) {
		var tmp = Math.floor(new Date(result[j].createdOn));
		if (current > tmp) {
		    result.splice(j, 0, comments[i]);
		    isAdded = true;
		    break;
		}
	    }
	    if (!isAdded) {
		result.push(comments[i]);
	    }
	}
	return result;
    },

    /**
     * Sorts by last comment date.
     *
     * @param comments
     *                is array of comments for sorting
     * @return sorted comments
     */
    sortByLastCommentDate : function(comments) {
	var result = [];
	if(comments) {
		for ( var i = 0; i < comments.length; i++) {
		    var isAdded = false;
		    var current = Math.floor(new Date(comments[i].createdOn));
		    if(comments[i].children) {
			    for ( var k = 0; k < comments[i].children.length; k++) {
					var subDate = Math.floor(new Date(
						comments[i].children[k].createdOn));
					if (subDate > current) {
					    current = subDate;
					}
			    }
		    }
		    for ( var j = 0; j < result.length; j++) {
				var tmp = Math.floor(new Date(result[j].createdOn));
				for ( var k = 0; k < result[j].children.length; k++) {
				    var subDate = Math.floor(new Date(
					    result[j].children[k].createdOn));
				    if (subDate > tmp) {
					tmp = subDate;
				    }
				}
				if (current > tmp) {
				    result.splice(j, 0, comments[i]);
				    isAdded = true;
				    break;
				}
		    }
		    if (!isAdded) {
			result.push(comments[i]);
		    }
		}
	}
	return result;
    },

    /**
     * Checks if the keyword is in the text.
     *
     * @param text
     *                is the text for search
     * @param keyword
     *                is the word for search
     * @true if the keyword is the the text of false if it is not
     */
    hasTextKeyword : function(text, keyword) {
	var keywords = $.trim(keyword).split(" ");
	for ( var i = 0; i < keywords.length; i++) {
	    if (text.indexOf(keywords[i]) < 0) {
		return false;
	    }
	}
	return true;
    },

    filterByTags: function(comment, tags) {
    	if (comment && comment.tags) {
	    	var commentTags = comment.tags.split(/\s*\|\s*/);
	    	var keep = false;
	    	$.each(tags, function(index) {
	    		var tag = tags[index];
	    		if (commentTags.indexOf(tag) !== -1) {
	    			keep = true;
	    			// break iteration!!!
	    			return false;
	    		}
	    	});
	    	if (keep) {
	    		return comment;
	    	}
    	}
    },

    /**
     * Filters by zoom level.
     *
     * @param comment
     *                is the comment to be filtered
     * @param zoomLevel
     *                is the zoom level for filtering
     * @return instance of the comment or undefined if the comment is not
     *         created in this zoom level
     */
    filterByZoomLevel : function(comment, zoomLevel) {
	if (comment == undefined) {
	    return comment;
	}
	if (comment.shape && comment.shape.zoomLevel == zoomLevel) {
	    return comment;
	}
	return undefined;
    },

    /**
     * Filters by keyword.
     *
     * @param comment
     *                is the comment to be filtered
     * @param keyword
     *                is the keyword for filtering
     * @return instance of the comment or undefined if there is no such keyword
     */
    filterByKeyword : function(comment, keyword) {
	if (comment == undefined) {
	    return comment;
	}
	var val = "<div>" + comment.content + "</div>";
	if ($.comments.hasTextKeyword($(val).text(), keyword)
		|| $.comments.hasTextKeyword(comment.title, keyword)) {
	    return comment;
	}
	for ( var i = 0; i < comment.children.length; i++) {
	    val = "<div>" + comment.children[i].content + "</div>";
	    if ($.comments.hasTextKeyword($(val).text(), keyword)) {
		return comment;
	    }
	}
	return undefined;
    },

    /**
     * Filters by user.
     *
     * @param comment
     *                is the comment to be filtered
     * @param user
     *                is the user for filtering
     * @return instance of the comment or undefined if there is no such user
     */
    filterByUser : function(comment, user) {
	if (comment == undefined) {
	    return comment;
	}
	if (comment.createdBy == user) {
	    return comment;
	}
	for ( var i = 0; i < comment.children.length; i++) {
	    if (comment.children[i].createdBy == user) {
		return comment;
	    }
	}
	return undefined;
    },

    /**
     * Filters by date.
     *
     * @param comment
     *                is the comment to be filtered
     * @param dateFrom
     *                is the start date for filtering
     * @param dateTo
     *                is the end date for filtering
     * @return instance of the comment or undefined if the date is not in the
     *         interval
     */
    filterByDate : function(comment, dateFrom, dateTo) {
	    if (dateFrom && dateFrom instanceof Date) {
	    	dateFrom = dateFrom.setHours(0,0,0,0);
	    }

	    if (dateTo && dateFrom instanceof Date) {
	    	dateTo = dateTo.setHours(23,59,59,999);
	    }

		if (comment == undefined) {
		    return comment;
		}
		var current = new Date(comment.createdOn);
		current = current.setHours(0,0,0,0);
		if ((!dateFrom || dateFrom <= current) && (!dateTo || current <= dateTo)) {
		    return comment;
		}
		var children = comment.children;
		if (children == undefined) {
			return;
		}
		for ( var i = 0; i < children.length; i++) {
		    current = new Date(children[i].createdOn);
		    current = current.setHours(0,0,0,0);
		    if ((!dateFrom || dateFrom <= current) && (!dateTo || current <= dateTo)) {
		    	return comment;
		    }
		}
		return undefined;
    },

    /**
     * Filters by status.
     *
     * @param comment
     *                is the comment to be filtered
     * @param status
     *                is the status for filtering
     * @return instance of the comment or undefined if there is no such status
     */
    filterByStatus : function(comment, status) {
	if (comment == undefined) {
	    return comment;
	}
	if (comment.status == status) {
	    return comment;
	}
	return undefined;
    },

    /**
     * Filters by category.
     *
     * @param comment
     *                is the comment to be filtered
     * @param category
     *                is the category for filtering
     * @return instance of the comment or undefined if there is no such category
     */
    filterByCategory : function(comment, category) {
	if (comment == undefined) {
	    return comment;
	}
	if (comment.category == category) {
	    return comment;
	}
	return undefined;
    },

    /**
     * Filters by instance type.
     *
     * @param comment
     *                is the comment to be filtered
     * @param instanceType
     *                is the instance type for filtering
     * @return instance of the comment or undefined if there is no such instance type
     */
    filterByInstanceType : function(comment, instanceType) {
	if (comment == undefined) {
	    return comment;
	}
	if (comment.instanceType == instanceType) {
	    return comment;
	}
	return undefined;
    },

    /**
     * Makes HTTP request for loading comments by unique identifier. This is a
     * level of abstraction that is used to be modified by each new instance of
     * the object.
     *
     * @param objectId
     *                is the unique identifier that corresponds to comments from
     *                the database
     * @return returns comments list
     */
    requestComments : function(objectId) {
    	var result = [];
    	var url = $.comments.loadService;
    	if ($.comments.loadForDashlet) {
    		if (!$.comments.loadFilter) {
    			$.comments.loadFilter = {};
    		}

    		if (objectId) {
    			$.comments.loadFilter.instanceId = objectId;
    		}

    		if ($.comments.rootType) {
    			$.comments.loadFilter.instanceType = $.comments.rootType;
    		}
    		$.comments.loadFilter.evalActions = false;
    		$.comments.loadFilter.limit = 100;

    	    $.ajax({
    			type : 'POST',
    			url : url,
    			async : false,
    			data : JSON.stringify($.comments.loadFilter),
    			complete : function(data) {
    			    result = $.parseJSON(data.responseText);
    			}
    	    });
    	} else {
    		if (objectId) {
        	    url += "/" + objectId;
        	}
        	if (objectId) {
        	    $.ajax({
	        		type		: 'GET',
	        		url			: url,
	        		async		: false,
	        		cache		: false,
	        		complete 	: function(data) {
	        		    result = $.parseJSON(data.responseText);
	        		}
        	    });
        	}
    	}
    	if (!result) {
    	    result = [];
    	}

    	$($.comments.target).trigger('postLoad', [ result ]);
    	return result;
    },

    /**
     * Removes comment from the cache.
     *
     * @param id
     *                is the unique identifier that corresponds to comment from
     *                the database
     * @return deleted comment
     */
    removeComment : function(id) {
		var commentArray = [];
		for ( var commentKey in $.comments.map) {
		    var comments = $.comments.map[commentKey];
		    for ( var i = 0; i < comments.length; i++) {
				if (comments[i].id == id) {
				    commentArray.push(comments[i]);
				    comments.splice(i, 1);
				    break;
				}
				if (comments[i].children && comments[i].children.length) {
					for ( var j = 0; j < comments[i].children.length; j++) {
					    if (comments[i].children[j].id == id) {
							commentArray.push(comments[i].children[j]);
							comments[i].children.splice(j, 1);
							break;
					    }
					}
				}
		    }
		}
		$($.comments.target).trigger('postUnload', [ commentArray ]);
		if (commentArray.length == 0) {
		    null;
		}
		return commentArray[0];
    },

    /**
     * Makes HTTP request for removing comments by unique identifier. Code for
     * deletion of comments could be added here. If UNDO/REDO is used it will be
     * better to remove the deletion of the comments.
     *
     * @param id
     *                is the unique identifier that corresponds to comments from
     *                the database
     */
    removeComments : function(id) {
	var comments = $.comments.map[id];
	if (!comments) {
	    comments = [];
	}
	// delete $.comments.map[id];
	$($.comments.target).trigger('postUnload', [ comments ]);
    },

    /**
     * Update comments from the comment map.
     *
     * @param ids
     *                is the array with all unique identifier that corresponds
     *                to comments
     * @return list with all comments for each element
     */
    updateComments : function(ids) {
	var result = [];
	for ( var commentKey in $.comments.map) {
	    var hasKey = false;
	    for ( var int = 0; int < ids.length; int++) {
		if (ids[int] == commentKey) {
		    hasKey = true;
		    break;
		}
	    }
	    if (hasKey == false) {
		$.comments.removeComments(commentKey);
	    }
	}
	for ( var int = 0; int < ids.length; int++) {
	    result.push($.comments.getData(ids[int]));
	}
	return result;
    },

    /**
     * Gets data from the comment map. If there is no such data then the comment
     * map is loaded with data from the server.
     *
     * @param id
     *                is the unique identifier that corresponds to comments that
     *                should be added
     * @param forceReload force reloading of comments data for the given identifier
     *
     * @return returns comments list
     */
    getData : function(id, forceReload) {
		if (!(id in $.comments.map) || forceReload) {
		    $.comments.map[id] = $.comments.requestComments(id);
		}
		var result = $.comments.filter($.comments.map[id], $.comments.filterCriteria);
		$.comments.filteredMap[id] = result;
		$.comments.topicSize = result.length;
	    $($.comments.target).trigger('commentTopicSizeChange');
		return result;
    },

    /**
     * Replaces data from the comment map.
     *
     * @param objectId
     *                is the unique identifier of the commented object
     * @param comment
     *                is the comment that will be replaced in the map
     */
    replaceData : function(objectId, comment) {
	if (!(objectId in $.comments.map)) {
	    var result = [];
	    result.push(comment);
	    $.comments.map[objectId] = result;
	} else if (!(comment.parentId)) {
	    var result = $.comments.map[objectId];
	    for ( var j = 0; j < result.length; j++) {
		if (result[j].id == comment.id) {
		    result[j] = comment;
		}
	    }
	} else {
	    var result = $.comments.map[objectId];
	    for (i = 0; i < result.length; i++) {
		if (result[i].id == comment.parentId) {
		    for ( var j = 0; j < result[i].children.length; j++) {
			if (result[i].children[j].id == comment.id) {
			    result[i].children[j] = comment;
			}
		    }
		    break;
		}
	    }
	}
    },

    /**
     * Updates data from the comment map.
     *
     * @param objectId
     *                is the unique identifier of the commented object
     * @param comment
     *                is the comment that will be added to the map
     */
    updateData : function(objectId, comment) {
		if (!(objectId in $.comments.map)) {
		    var result = [];
		    result.push(comment);
		    $.comments.map[objectId] = result;
		} else if (!(comment.parentId)) {
		    var result = $.comments.map[objectId];
		    result.push(comment);
		} else {
		    var result = $.comments.map[objectId];
		    for (i = 0; i < result.length; i++) {
				if (result[i].id == comment.parentId) {
				    result[i].children.push(comment);
				    break;
				}
		    }
		}
    },

    /**
     * Loads data in the comment map.
     *
     * @param comments
     *                is array with all the comments
     */
    loadData : function(comments) {
	for ( var i = 0; i < comments.length; i++) {
	    var comment = comments[i];
	    var commentList = [];
	    for ( var commentKey in $.comments.map) {
		if (comment.objectId == commentKey) {
		    commentList = $.comments.map[commentKey];
		    break;
		}
	    }
	    commentList.push(comment);
	    $.comments.map[comments[i].objectId] = commentList;
	}
	if (!comments) {
	    comments = [];
	}
	$($.comments.target).trigger('postLoad', [ comments ]);
    },

    /**
     * Removes all comments.
     */
    clearData : function() {
	var comments = [];
	for ( var commentKey in $.comments.map) {
	    var commentList = $.comments.map[commentKey];
	    for ( var i = 0; i < commentList.length; i++) {
		comments.push(commentList[i]);
	    }
	}
	if (!comments) {
	    comments = [];
	}
	$.comments.map = {};
	$($.comments.target).trigger('postUnload', [ comments ]);
    },

    /**
     * Finds comment by the comment identifier.
     *
     * @param commentsList
     *                is array with all comments and their children
     * @param key
     *                is the identifier of the comment
     * @return comment with the same identifier as the passed parameter
     */
    findComment : function(commentsList, key) {
	var result = {};
	for ( var int = 0; int < commentsList.length; int++) {
	    if (commentsList[int].id == key) {
		result = commentsList[int];
		break;
	    }
	    result = $.comments.findComment(commentsList[int].children);
	    if (result.objectId) {
		break;
	    }
	}
	return result;
    },

    /**
     * Loads comments data as properties of comment panel structure.
     */
    loadCommentData : function() {
		var btnClick = function() {
		    var btn = $(this);
		    var parentId = $.data(btn.parents("div.comment").first()[0], "parentId");
		    var objectId = $.data(btn.parents("div.comment").first()[0], "objectId");
		    $.comments.showCommentBox(objectId, parentId, false);
		};
		var btn = $('div.unshare');
		btn.unbind('click');
		btn.click(btnClick);
		var editClick = function(event) {
			event.stopPropagation();
		    var btn = $(this);
		    var comment = $.data(btn.parents("[id$='" + $.comments.commentSuffix + "']:eq(0)")[0], "comment");
		    $.comments.editCommentBox(comment);
		};
		var btn = $('a.edit');
		btn.unbind('click');
		btn.click(editClick);
		var deleteClick = function() {
			var btn = $(this);
			var comment = $.data(btn.parents("[id$='" + $.comments.commentSuffix + "']:eq(0)")[0], "comment");

			var confirmMessage = $.comments.labels.deleteTopicConfirmationMessage;
			if (comment.parentId) {
				confirmMessage = $.comments.labels.deleteReplyConfirmationMessage;
			}
			EMF.dialog.open({
				message: confirmMessage,
				confirm: function() {
					$.comments.deleteComment(comment.id);
				}
			});
		};
		var btn = $('a.delete');
		btn.unbind('click');
		btn.click(deleteClick);
    },

    /**
     * Gets comments panel.
     *
     * @return comments panel
     */
    getActions : function(comment) {
    	if (!comment.actions || !comment.actions.length) {
    		return '';
    	}

    	var result = '<div style="display: inline-block;">\
	    				 <div class="btn-group styledButton stop-click-propagation">\
							<a class="dropdown-toggle" id="dLabel" role="button" data-toggle="dropdown" href="javascript:void(0);">\
								<b class="caret"/>\
							</a>\
	    					<ul class="dropdown-menu left" role="menu" aria-labelledby="dLabel"></ul>\
	    				 </div>\
    				 </div>';

    	var actionsNode = $(result);
    	var actionsList = actionsNode.find('ul');
    	var hasMenuAction = false;

    	$.each(comment.actions, function() {
    		var action = this,
    			cssClass;

    		switch(action.action) {
    			case 'editDetails':
    				cssClass = 'edit';
    				break;
    			case 'delete':
    				cssClass = 'delete';
    				break;
    			default:
    				break;
    		}
    		if (cssClass) {
    			hasMenuAction = true;
    			$('<li><a tabindex="-1" href="javascript:void(0);" class="' + cssClass + '">' + action.label + '</a></li>')
    				.appendTo(actionsList);
    			cssClass = null;
    		}
    	});

    	if (hasMenuAction) {
    		return actionsNode.html();
    	} else {
    		return '';
    	}
    },

    /**
     * Creates comments structure.
     *
     * @param comments
     *                is JSON with all the comments
     * @param id
     *                is the id of the commented element
     * @param height
     *                is the height of the commented element
     * @param isRootSection
     *                is parameter that shows if it is root section comment
     * @return HTML with all the comments
     */
    getComments : function(comments, id, height, isRootSection) {
		var buttonHeight = 33;
		var commentHeight = 24;
		var minimalHeight = 24;
		var result = '';

		if ($.comments.viewSubmitButton) {
		    var h = $("#" + id).height();
		    if (!h) {
		    	h = buttonHeight;
		    }
		    var title = _emfLabels["create.section.comment"];
		    if (isRootSection) {
		    	title = _emfLabels["create.document.comment"];
		    }

		    if(!idoc.isNewObject()) {
		    	result = $.comments.constants.newCommentTemplate.replace(/%ID%/g, id).replace(/%HEIGHT%/g, h).replace(/%TITLE%/g, title);
		    }
		}

		var count = 0;
		var hasMorePanel = false;
		for ( var key in comments) {
			var comment = comments[key];
			count = count + 1;
		    if (count * commentHeight + minimalHeight + buttonHeight > height
		    		&& !hasMorePanel && $.comments.viewSubmitButton) {
				var delta = height - buttonHeight - (count - 1) * commentHeight;
				var data = comments.length - count + 1;
				if (data == 1) {
				    data = "1 more comment";
				} else {
				    data = data + " more comments";
				}
				result = result + $.comments.constants.moreTemplate.replace(/%HEIGHT%/g, delta).replace(/%DATA%/g, data);
				hasMorePanel = true;
		    }
		    var tinyComment = comment.title;
		    if (tinyComment.length > $.comments.contentSize) {
		    	tinyComment = tinyComment.substring(0, $.comments.contentSize) + '...';
		    }
		    var category = comment.category;
		    if (!category) {
		    	category = '';
		    }
		    var status = comment.status;
		    if (!status) {
		    	status = '';
		    } else {
		    	$.each($.comments.statuses, function() {
		    		var statusObject = this;
		    		if (statusObject.value === status) {
		    			status = statusObject.text;
		    			return false;
		    		}
		    	});
		    }
		    var replies = '';
		    if (comment.children && comment.children.length > 0) {
				for ( var i = 0; i < comment.children.length; i++) {
				    var reply = comment.children[i];
				    replies += $.comments.getReply(reply);
				}
		    }
		    var replier = '';
		    // TODO - later it will become '... John Doe'
		    var all = '';
		    if (comment.children.length > 1) {
		    	all = '(' + comment.children.length + ') ';
		    }

		    var commentTemplate = $.comments.previewMode?$.comments.constants.commentPreviewTemplate:$.comments.constants.commentTemplate;

		    var linkTab = null;
		    if (comment.instanceType === 'projectinstance') {
		    	linkTab = EMF.bookmarks.projectTab.details;
		    } else if (comment.instanceType === 'caseinstance') {
		    	linkTab = EMF.bookmarks.caseTab.details;
		    }

			var date = new Date(comment.createdOn);
			var createdOn = EMF.date.getDateTime(date);
		    result += commentTemplate
		    	.replace(/%REPLIES%/g, replies)
		    	.replace(/%AUTHOR%/g, comment.createdBy)
		    	.replace(/%REPLIER%/g, replier)
		    	.replace(/%DATE%/g, createdOn)
		    	.replace(/%SHORT_TITLE%/g, tinyComment)
		    	.replace(/%TITLE%/g, comment.title)
		    	.replace(/%ALL%/g, all)
		    	.replace(/%UNREAD%/g, '')
		    	.replace(/%ICON%/g,comment.icon)
		    	.replace(/%LINK%/g, EMF.bookmarks.buildLink(comment.instanceType, comment.instanceId, linkTab))
		    	.replace(/%STATUS%/g, status)
		    	.replace(/%CATEGORY%/g, category)
			    .replace(/%ID%/g, comment.id)
			    .replace(/%SUFFIX%/g, $.comments.commentSuffix)
			    .replace(/%ACTIONS%/g, $.comments.getActions(comment))
			    .replace(/%TAGS%/g, comment.tags || '');
		    $.data(document.body, comment.id, comment);

		    $('#author option').each(function() {
		        $("#author option[value='"+$(this).attr('value')+"']").hide();
		    });

		    if($.comments.authors.indexOf(comment.createdBy) == -1) {
		    	$.comments.authors.push(comment.createdBy);
		    }

		    $.each($.comments.authors, function( index, value ) {
		    	$("#author option[value='" + value + "']").show();
		    });
		}
		return result;
    },

    /**
     * Gets reply.
     *
     * @param reply
     *                to be used for generation
     * @return reply structure
     */
    getReply : function(reply) {
	var tinyReply = $(reply.content).text();
	if (tinyReply.length > $.comments.contentSize) {
	    tinyReply = tinyReply.substring(0, $.comments.contentSize) + '...';
	}

	var replyTemplate = $.comments.previewMode?$.comments.constants.replyPreviewTemplate:$.comments.constants.replyTemplate;

	var date = new Date(reply.createdOn);
	var createdOn = EMF.date.getDateTime(date);

	var result = replyTemplate.replace(/%ID%/g,
		reply.id).replace(/%SUFFIX%/g, $.comments.commentSuffix)
		.replace(/%TEXT%/g, reply.content).replace(/%AUTHOR%/g,
			reply.createdBy).replace(/%SHORT_TEXT%/g, $(reply.content).text())
		.replace(/%ACTIONS%/g, $.comments.getActions(reply)).replace(
			/%DATE%/g, createdOn);
	return result;
    },

    /**
     * Add reply to the comment structure.
     *
     * @param reply
     *                to be used for generation
     */
    addReply : function(reply) {
		var result = $.comments.getReply(reply);
		var id = "#" + reply.parentId + $.comments.commentSuffix;
		id = id.replace(/:/g, '\\:');
		$(id).find(".commentView").append(result);
		$.comments.storeComment(reply);
		$.comments.addActions($(id));
		$.comments.updateCounter(id);
		$.comments.loadCommentData();
    },

    /**
     * Counts comments.
     *
     * @param comment
     *                is the comment that is traversed
     * @return comment number
     */
    countComments : function(comment) {
	var result = 1;
	for ( var key in comment.children) {
	    result = result + $.comments.countComments(comment.children[key]);
	}
	return result;
    },

    /**
     * Hides additional panel.
     *
     * @param is
     *                the selected element
     */
    hideAdditionalCommentPanel : function(element) {
	element.parents(".commentHeight").find(".more").removeClass(
		"activePanel");
    },

    /**
     * Adds actions.
     *
     * @param doc
     *                is the document where actions will be added
     */
    addActions : function(doc) {
		$(".action", doc).click(function() {
		    $.comments.expand($(this));
		    $(".more").addClass("activePanel");
		    var tab = $(this).parents(".commentHeight");
		    tab.find(".more").removeClass("activePanel");
		});
		$(".commentPreview > .action", doc).click(function() {
		    $(".commentPreview").removeClass("inactive");
		    $(".commentView").addClass("inactive");
		    $(".more").addClass("activePanel");
		    $(this).parent().addClass("inactive");

		    var parentId = $(this).parent().parent().data("parentId");
		    var objectId = $(this).parent().parent().data("objectId");

		    // TODO: Post to server to update last read timestamp. Clear unread from $comments.map because filter restore unread value!

		    $(this).parent().parent().find(".unread-comments").each(function(){
		    	$(this).html("&nbsp;");
		    });

		    $(this).parent().next().removeClass("inactive");
		    $.comments.expand($(this));
		});
		$(".commentView > .action", doc).click(function() {
		    $(this).parent().addClass("inactive");
		    $(this).parent().prev().removeClass("inactive");
		    $.comments.expand($(this));
		});
		$(".replyPreview>.action", doc).click(function() {
		    $(this).parent().addClass("inactive");
		    $(this).parent().next().removeClass("inactive");
		    $.comments.expand($(this));
		});
		$(".replyView .action", doc).click(function() {
		    $(this).parent().parent().addClass("inactive");
		    $(this).parent().parent().prev().removeClass("inactive");
		    var commentBody = $.data($('.comment-box')[0], "comment");
		    if(commentBody) {
			    var commentId = "#" + commentBody.id + $.comments.commentSuffix;

			    if($(this).parent().parent().parent().attr('id') == commentBody.id + $.comments.commentSuffix) {
			    	$(this).parent().parent().parent().find('#short-txt-id').html($(commentBody.content).text());
			    }
		    }
		    $.comments.expand($(this));
		});
		$($.comments.target).focus(function() {
		    $.comments.collapse();
		});
    },

    /**
     * Stores comment data.
     *
     * @comment is the comment
     */
    storeComment : function(comment) {
		var threadId = comment.id + $.comments.commentSuffix;
		threadId = threadId.replace(/:/g, '\\:');
		$.data($("#" + threadId)[0], "comment", comment);
		$.data($("#" + threadId)[0], "parentId", comment.id);
		$.data($("#" + threadId)[0], "objectId", comment.objectId);
    },

    /**
     * Expands element to wrap all elements and collapses others.
     *
     * @element to be expanded
     */
    expand : function(element) {
		if (!$.comments.viewSubmitButton) {
		    return;
		}
		$(".commentHeight").each(function(index) {
		    $(this).css("height", $(this).css("min-height"));
		});
		var sector = element.parents(".commentHeight");
		sector.css("height", "100%");
		$.comments.hideAdditionalCommentPanel(element);
    },

    /**
     * Collapses all elements.
     */
    collapse : function() {
		$(".commentHeight").each(function(index) {
		    $(this).css("height", $(this).css("min-height"));
		});
    },

    /**
     * Refreshes comments panel. Loads comments for the document. Removes
     * useless comments.
     */
    refresh : function() {
		var tops = [];
		var ids = [];
		var key = $.comments.rootId;

		// FIXME: provide parentId trough config
		if($.comments.idoc && $.comments.idoc.object && $.comments.idoc.object.parentId) {
			// CMF-4605 - If key value is set to 0  java.lang.IllegalArgumentException: Malformed uri [0] is thrown
			// and Create Sub Document and somethimes Clone Document doesn't work.
			return;
		}
		if (!(key)) {
		    key = 0;
		}
		tops.push($($.comments.source).parent().offset().top - $($.comments.source).scrollTop());
		ids.push(key);

		$($.comments.commentSelectors, $($.comments.source)).each(function(index) {
			var $this = $(this);
			//create comment blocks only for the visible elements
			if ($this.is(":visible")) {
				var key = $this.attr('id');
				if (!key) {
					key = index;
				}
				tops.push($(this).offset().top);
				ids.push(key);
			}
		});
		tops.push($($.comments.source).offset().top + $($.comments.source).height());
		var rawCommentsList = $.comments.updateComments(ids);
		$($.comments.target).children().remove('[id$="' + $.comments.commentSuffix + '"]');
		var buttonHeight = 33;
		for ( var i = 0; i < tops.length - 1; i++) {
		    var rowId = ids[i] + $.comments.commentSuffix;
		    var delta = tops[i + 1] - tops[i] - 1;
		    if (delta < 0) {
		    	delta = 0;
		    }
		    minDelta = delta;
		    if (minDelta < buttonHeight) {
		    	minDelta = buttonHeight;
		    }
		    var structure = $.comments.getComments(rawCommentsList[i], ids[i], delta, i == 0);
		    var style = '';
		    if ($.comments.viewSubmitButton) {
		    	style = 'height:' + delta + 'px;min-height:' + minDelta + 'px;';
		    }

		    var rowData = $('<div id="' + rowId + '" style="' + style + '" class="commentHeight">' + structure + '</div>');

		    if (!$.comments.previewMode) {
			    $('.filterPanel .tags-filter')
			    	.select2($.comments.select2TagsConf);
			    
			    rowData.find('input.tags')
			    	.select2($.comments.select2TagsConf)
			        .on('change', function(event) {
			        	var tagsSelect = $(this);
			        	var comment = $.data(tagsSelect.parents("[id$='" + $.comments.commentSuffix + "']:eq(0)")[0], "comment");
			        	if (!comment.tags) {
			        		comment.tags = [ ];
			        	} else {
			        		comment.tags = comment.tags.split(/\s*\|\s*/);
			        	}
	
			        	if (event.added) {
			        		var allTags = $.comments.allTagsOnPage;
			        		if (allTags.indexOf(event.added.text) === -1) {
			        			allTags.push(event.added.text);
			        		}
			        		comment.tags.push(event.added.text);
			        	} else {
			        		var removeIndex = comment.tags.indexOf(event.removed.text);
			        		comment.tags.splice(removeIndex, 1);
			        	}
			        	comment.tags = comment.tags.join('|');
	
	
			    	    var url = $.comments.editService + '/' + comment.objectId + '/' + comment.id;
			    	    $.ajax({
				    		type : 'POST',
				    		url : url,
				    		data : comment
			    	    });
			        });
			}

		    $($.comments.target).append(rowData);
		    $(document).ready(function() {
		    	$.comments.addActions(document);
		    });

		    $(window).trigger('updateComments');
		    for ( var j = 0; j < rawCommentsList[i].length; j++) {
				var temp = rawCommentsList[i][j];
				$.comments.storeComment(temp);
				for ( var k = 0; k < temp.children.length; k++) {
				    $.comments.storeComment(temp.children[k]);
				}
		    }
		    $.comments.loadCommentData();
		}
		$($.comments.target).trigger('commentGenerationFinish');
    },

    /**
     * Resizes and updates the absolute position of comment panels.
     */
    resize : function() {
	var tops = [];
	var ids = [];
	var key = $.comments.rootId;
	if (!(key)) {
	    key = 0;
	}
	tops.push($($.comments.source).parent().offset().top
		- $($.comments.source).scrollTop());
	ids.push(key);
	$($.comments.commentSelectors, $($.comments.source)).each(
		function(index) {
		    var key = $(this).attr('id');
		    if (!(key)) {
			key = index;
		    }
		    tops.push($(this).offset().top);
		    ids.push(key);
		});
	tops.push($($.comments.source).offset().top
		+ $($.comments.source).height());
	var divs = $($.comments.target + ' [id$="' + $.comments.commentSuffix
		+ '"]');
	if (divs.length == tops.length - 1) {
	    var buttonHeight = 33;
	    for ( var i = 0; i < tops.length - 1; i++) {
		var delta = tops[i + 1] - tops[i] - 1;
		if (delta < 0) {
		    delta = 0;
		}
		minDelta = delta;
		if (minDelta < buttonHeight) {
		    minDelta = buttonHeight;
		}
		$(divs[i]).css('height', delta);
		$(divs[i]).css('min-height', minDelta);
	    }
	    $($.comments.target).trigger('commentGenerationFinish');
	} else {
	    $.comments.refresh();
	}
    },

    /**
     * Adds comment and refreshes comments panel.
     *
     * @param comment
     *                is the new comment
     */
    addComment : function(comment) {
		var commentsList = $.comments.getData(comment.objectId);
		if (comment.parentId) {
		    var parent = $.comments.findComment(commentsList, comment.parentId);
		    parent.children.push(comment);
		    // $.comments.refresh();
		} else {
		    commentsList.push(comment);
		    $.comments.refresh();
		}
    },

    /**
     * Edits reply.
     *
     * @param reply
     *                is the reply that is edited
     */
    editReply : function(reply) {
    	var replyId = reply.id + $.comments.commentSuffix;
    	replyId = replyId.replace(/:/g, '\\:');
    	$("#" + replyId + " .modal-footer .col-xs-12").html(reply.content);
    },

    /**
     * Saves comment after editing from image annotation viewer.
     *
     * @param data
     *                is the comment data
     */
    saveComment : function(data) {
		var objectId = data.objectId;
		var parentId = data.parentId;
		var url = $.comments.editService;
		if (objectId) {
		    url += '/' + objectId;
		}
		if (parentId) {
		    url += '/' + parentId;
		}
		$.ajax({
		    type : 'POST',
		    url : url,
		    data : data,
		    async : false,
		    complete : function(res) {
				var comment = $.parseJSON(res.responseText);
				$.comments.replaceData(comment.objectId, comment);
				$($.comments.target).trigger('postSaveAction', comment);
		    }
		});
    },

    /**
     * Enables or disables save button status.
     */
    changeSaveButtonStatus : function() {
    	var checkCommentTitle = $('.comment-box .row.titleRow').is(':visible');
    	var editor = $('#mce_0_ifr').contents().find('body').html();

		if (checkCommentTitle) {
			if($('#mce_0_ifr').is(':visible')) {
				if ($('input.comment-title-input').val() && ($(editor).text().trim().length > 0 || editor.indexOf('<img') != -1)) {
					$('.comment-post-btn').removeAttr('disabled');
				} else {
					$('.comment-post-btn').attr('disabled', 'disabled');
				}
			} else {
				if ($('input.comment-title-input').val()) {
					$('.comment-post-btn').removeAttr('disabled');
				} else {
					$('.comment-post-btn').attr('disabled', 'disabled');
				}
			}
		} else {

			if ($(editor).text().length > 0 || editor.indexOf('<img') != -1) {
				$('.comment-post-btn').removeAttr('disabled');
			} else {
				$('.comment-post-btn').attr('disabled', 'disabled');
			}
		}
    },

    /**
     * Visualizes comments box.
     *
     *
     * @param objectId
     *                is the id of the commented data
     *
     * @param parentId
     *                is the id of the thread if comment is reply
     *
     * @param isThread
     *                is parameter that keeps information if comment is thread
     *                of reply
     *
     * @param shape
     *                is image shape data used for image comments
     */
    showCommentBox : function(objectId, parentId, isThread, shape, initalText) {
		var debounce = $.comments.debounceDelayRefresh;
		$('.popUpUser').html(EMF.currentUser.displayName);
		$('input.comment-title-input').defaultInputValue('Enter New Topic');

		if (isThread) {
		    $('select.comment-title-input').css("display", "block");
		    $('.commentRow').css("display", "block");
		    $('input.tags').select2($.comments.select2TagsConf);
		} else {
		    $('select.comment-title-input').css("display", "none");
		    $('.commentRow').css("display", "block");
		    $('input.tags', '.comment-box').select2('destroy');
		}

		if (initalText) {
			$('#mce_0_ifr').contents().find('body').html(initalText)
		}

		$.data($('.comment-box')[0], "objectId", objectId);
		$.data($('.comment-box')[0], "parentId", parentId);

		var postClick = function() {
		    var objectId = $.data($('.comment-box')[0], "objectId");
		    var parentId = $.data($('.comment-box')[0], "parentId");
		    var data = { };

		    if($.comments.rootId) {
		    	data.instanceId = $.comments.rootId;
		    }
		    if($.comments.rootType) {
		    	data.instanceType = $.comments.rootType;
		    }

		    data.category = $('select.comment-title-input').val();
		    data.title = $('input.comment-title-input').val();
		    data.content = $('#mce_0_ifr').contents().find('body').html();
		    data.createdByUsername = EMF.currentUser.username;

		    var tagsContainerElement = $('.comment-box .select2-container.tags');
		    var tagsElement = $('.comment-box input.tags');

		    if (tagsElement.length && tagsContainerElement.is(':visible')) {
		    	var tags = tagsElement.select2('val').join('|');
		    	data.tags = tags;
		    }

		    if (shape) {
		    	data.shape = shape;
		    }
		    var url = $.comments.saveService;
		    if (objectId) {
		    	url += '/' + objectId;
		    }
		    if (parentId) {
		    	url += '/' + parentId;
		    }

		    if (isThread && !data.id) {
		    	$.comments.beforeCreateTopic.call(this, data);
		    }

		    $.ajax({
				type : 'POST',
				url : url,
				data : data,
				async : false,
				complete : function(res) {
				    var comment = $.parseJSON(res.responseText);
				    $.comments.updateData(comment.objectId, comment);
				    if (comment.parentId) {
				    	$($.comments.target).trigger('commentUpdate', [ comment ]);
				    } else {
				    	if (!data.id) {
				    		$.comments.afterCreateTopic.call(this, comment);
				    	}
						var target = $.comments.target;
						$(target).trigger('commentRefresh');
						$(target).trigger('postSaveAction', comment);
				    }
				}
		    });
		    $('.comment-box').hide();
		    $('#mce_0_ifr').contents().find('body').html('');
		    $('.comment-title-input').val('');
		    $('.comment-box')
		    $('.comment-box input.tags').select2('val', '');
		};

		var postBtn = $('.comment-post-btn');
		postBtn.attr("value", "Save");
		postBtn.unbind('click');
		postBtn.click($.debounce(debounce, postClick));

		var cancelClick = function() {
		    $('.comment-box').hide();
		    $('#mce_0_ifr').contents().find('body').html('');
		    $('input.comment-title-input').val('Enter New Topic');
		    $($.comments.target).trigger('postCancelAction');
		    $.comments.cancelCreateTopic.call(this);
		}

		var cancelBtn = $('.comment-cancel-btn');
		cancelBtn.unbind('click');
		cancelBtn.click($.debounce(debounce, cancelClick));

		if (isThread) {
		    $('.panel-heading div:first').html('New Topic');
		    $('.titleRow').show();
		} else {
		    var commentsList = $.comments.getData(objectId);
		    var popUpHeader = '';
		    for ( var i = 0; i < commentsList.length; i++) {
				if (commentsList[i].id == parentId) {
				    popUpHeader = 'Re: ' + commentsList[i].title;
				    break;
				}
		    }
		    $('.panel-heading div:first').html(popUpHeader);
		    $('.titleRow').hide();
		}
		$('.comment-post-btn').attr('disabled', 'disabled');
		$('input.comment-title-input').on('keydown paste', function() {
		    setTimeout(function() {
			$.comments.changeSaveButtonStatus();
		    }, 100);
		});
		$('#mce_0_ifr').contents().find('body').on('keydown paste', function() {
		    setTimeout(function() {
		    	$.comments.changeSaveButtonStatus();
		    }, 100);
		});
		$($('#mce_0_ifr').contents()[0]).on('DOMNodeInserted', function() {
			$.comments.changeSaveButtonStatus();
		});
		$('.comment-box').show();
		$('.comment-box').verticalCenter();
    },

    /**
     * Visualizes comments box for editing comments.
     *
     *
     * @param objectId
     *                is the comment for editing
     */
    editCommentBox : function(comment) {
		var debounce = $.comments.debounceDelayRefresh;
		var objectId = comment.objectId;
		var parentId = comment.parentId;
		var isThread = true;
		if (parentId) {
		    isThread = false;
		}
		$('.popUpUser').html(EMF.currentUser.displayName);
		$('input.comment-title-input').val(comment.title);
		$('#mce_0_ifr').contents().find('body').html(comment.content);
		CMF.utilityFunctions.checkForDeadInstanceLinks($('#mce_0_ifr').contents().find('body'), '/object-rest/exists');
		if (isThread) {
		    $('select.comment-title-input').css("display", "block");
		    $('.commentRow').css("display", "none");
		    $('.topic-category > option').each(function() {
		    	var $this = $(this);
		    	if ($this.val() === comment.category) {
		    		$this.prop('selected', 'selected');
		    	}
		    });
		    $('.comment-box option').each(function() {
		    	var $this = $(this);
		    	if ($this.val() === comment.category) {
		    		$this.prop('selected', 'selected');
		    	}
		    });
		    $('.comment-box input.tags').val(comment.tags || '').select2($.comments.select2TagsConf);
		} else {
		    $('select.comment-title-input').css("display", "none");
		    $('.commentRow').css("display", "block");
		}
		$.data($('.comment-box')[0], "comment", comment);
		var postClick = function() {

		    var comment = $.data($('.comment-box')[0], "comment");
		    if (isThread) {
		    	comment.category = $('.topic-category > option:selected, #category > option:selected').val();
		    }
		    var objectId = $.comments.rootId;
		    var parentId = comment.parentId;
		    comment.title = $('input.comment-title-input').val();
		    comment.content = $('#mce_0_ifr').contents().find('body').html();

		    var url = $.comments.editService;
		    if (objectId) {
		    	url += '/' + objectId;
		    }
		    if (parentId) {
		    	url += '/' + parentId;
		    } else {
		    	url += '/' + comment.id;
		    }

		    $.ajax({
				type : 'POST',
				url : url,
				data : comment,
				async : false,
				complete : function(res) {
				    var comment = $.parseJSON(res.responseText);
				    $.comments.replaceData(comment.objectId, comment);
				    if (comment.parentId) {
						$($.comments.target).trigger('commentEdit', [ comment ]);
				    } else {
				    	$($.comments.target).trigger('commentRefresh');
				    }
				    $($.comments.target).trigger('postSaveAction', comment);
				}
		    });
		    $('.comment-box').hide();
		    $('#mce_0_ifr').contents().find('body').html('');
		    $('.comment-title-input').val('');
		    $('.comment-box input.tags').val('');
		};
		var postBtn = $('.comment-post-btn');
		postBtn.unbind('click');
		postBtn.click($.debounce(debounce, postClick));
		var cancelClick = function() {
		    $('.comment-box').hide();
		    $('#mce_0_ifr').contents().find('body').html('');
		    $('input.comment-title-input').val('Enter New Topic');
		    $('.comment-box input.tags').val('');
		    $($.comments.target).trigger('postCancelAction');
		}
		var cancelBtn = $('.comment-cancel-btn');
		cancelBtn.unbind('click');
		cancelBtn.click($.debounce(debounce, cancelClick));
		if (isThread) {
		    $('.panel-heading div:first').html('New Topic');
		    $('.titleRow').show();
		} else {
		    var commentsList = $.comments.getData(objectId);
		    var popUpHeader = '';
		    for ( var i = 0; i < commentsList.length; i++) {
			if (commentsList[i].id == parentId) {
			    popUpHeader = 'Re: ' + commentsList[i].title;
			    break;
			}
		    }
		    $('.panel-heading div:first').html(popUpHeader);
		    $('.titleRow').hide();
		}
		$('.comment-post-btn').removeAttr('disabled');
		$('input.comment-title-input').on('keydown paste', function() {
		    setTimeout(function() {
			$.comments.changeSaveButtonStatus();
		    }, 100);
		});
		$('#mce_0_ifr').contents().find('body').on('keydown paste', function() {
		    setTimeout(function() {
			$.comments.changeSaveButtonStatus();
		    }, 100);
		});
		$($('#mce_0_ifr').contents()[0]).on('DOMNodeInserted', function() {
			$.comments.changeSaveButtonStatus();
		});
		$('.comment-box').show();
		$('.comment-box').verticalCenter();
		$('.comment-box').draggable();
    },

    /**
     * Deletes comment.
     *
     * @param commentId
     *                is the id of the comment that will be deleted
     */
    deleteComment : function(commentId) {
		$.data($("body")[0], "commentId", commentId);
		$.ajax({
		    type : 'DELETE',
		    url : $.comments.removeService + '/' + commentId,
		    data : {},
		    async : false,
		    complete : function() {
				var commentId = $.data($("body")[0], "commentId");
				var currentId = commentId + $.comments.commentSuffix;
				currentId = currentId.replace(/:/g, '\\:');
				var reply = $.data($("#" + currentId)[0], "comment");
				$.comments.removeComment(commentId);
				if (reply.parentId) {
				    var parentId = reply.parentId + $.comments.commentSuffix;
				    parentId = parentId.replace(/:/g, '\\:');
				    var comment = $.data($("#" + parentId)[0], "comment");
				    $("#" + parentId).find(".commentView>.info .pull-right")
					    .replaceWith($.comments.getActions(comment));
				    $.comments.loadCommentData();
				}
				$("#" + currentId).remove();
				if (reply.parentId) {
				    var parentId = reply.parentId + $.comments.commentSuffix;
				    parentId = parentId.replace(/:/g, '\\:');
				    $.comments.updateCounter("#" + parentId);
				}
		    }
		});
    },

    /**
     * Updates message counter.
     *
     * @param id
     *                is the identifier of comment
     */
    updateCounter : function(id) {
	var count = $(id).find(".commentView .reply").size();
	if (count > 1) {
	    count = "(" + count + ")";
	    $(id).find(".commentPreview .unread").html(count);
	} else {
	    $(id).find(".commentPreview .unread").html("");
	}
    },

    /**
     * Initializes custom events.
     */
    init : function() {
		var delay1 = $.comments.debounceDelayResize;
		var delay2 = $.comments.debounceDelayRefresh;
		
		$.comments.select2TagsConf.tags = function() {
			return $.comments.allTagsOnPage;
		};
		
		$($.comments.target)
			.on('commentResize', $.debounce(delay1, $.comments.resize))
			.on('commentRefresh', $.debounce(delay2, $.comments.refresh))
			.on('commentUpdate', function(event, reply) {
				$.comments.addReply(reply);

				$('.stop-click-propagation').unbind( "click");
				$('.stop-click-propagation')
				.click(function(event) {
					event.stopPropagation();
					var target = $(event.target);
					if (target.is('.dropdown-toggle') || target.parents('.dropdown-toggle').length) {
						var e = jQuery.Event( "click" );
						e.target = event.target;
						e.pageX = event.pageX;
						e.pageY = event.pageY;
						$(document).trigger(e);
					}
				});
			})
			.on('commentEdit', function(event, reply) {
				$.comments.editReply(reply);
			})
			.on('postLoad', function(event, topics) {
				var allTags = [ ];
				$.each(topics, function(index) {
					var topic = topics[index];
					if (topic.tags) {
						var tags = topic.tags.split(/\s*\|\s*/);
						$.each(tags, function(index) {
							var tag = tags[index];
							if ($.inArray(tag, allTags) === -1) {
								allTags.push(tag);
							}
						});
					}
				});
				$.comments.allTagsOnPage = allTags;
			})
			.on('postSaveAction', function(event, comment) {
				if (comment.tags) {
					var allTags = $.comments.allTagsOnPage,
						tags = comment.tags.split(/\s*\|\s*/);
					
					$.each(tags, function(index) {
						var tag = tags[index];
						
						if ($.inArray(tag, allTags) === -1) {
							allTags.push(tag);
						}
					});
				}
			})
			.on('commentGenerationFinish', function() {
				CMF.utilityFunctions.checkForDeadInstanceLinks($(this), '/object-rest/exists');

				$('.stop-click-propagation')
					.click(function(event) {
						event.stopPropagation();
						var target = $(event.target);
						if (target.is('.dropdown-toggle') || target.parents('.dropdown-toggle').length) {
							var e = jQuery.Event( "click" );
							e.target = event.target;
							e.pageX = event.pageX;
							e.pageY = event.pageY;
							$(document).trigger(e);
						}
					});
			})
			.on('commentTopicSizeChange', function(event, triggeredFromFilter){
				CMF.comments.applyCommentTopicSize();
				// check the location from where event is triggered
				if($.type(triggeredFromFilter)==="boolean"){
					CMF.comments.changeCommentFilterButtonStyle(triggeredFromFilter);
				}
			});

		function getValue(text, options) {
			var value;

			$.each(options, function() {
	    		var object = this;
	    		if (object.text === text) {
	    			value = object.value;
	    			return false;
	    		}
	    	});

			if (value) {
				return value;
			} else {
				return options[0].value;
			}
		}

		function getText(value, options) {
			var text;

			$.each(options, function() {
	    		var object = this;
	    		if (object.value === value) {
	    			text = object.text;
	    			return false;
	    		}
	    	});

			if (text) {
				return text;
			} else {
				return options[0].text;
			}
		}

		function updateStatus(value, element) {
		    var obj = $(element).editable('getValue');
		    var id = '';
		    for ( var i in obj) {
				if (obj.hasOwnProperty(i)) {
				    id = i.toString();
				    break;
				}
		    }
		    id = id.substring(0, id.indexOf('status'));
		    var commentData = $.data(document.body, id);
		    var href = window.location.href;
		    var host = window.location.hostname;
		    if (window.location.port.toString().length > 0) {
		    	host = host + ':' + window.location.port;
		    }
		    var root = $.comments.editService;
		    $.ajax({
				type : "POST",
				url : root + '/' + commentData.objectId + '/' + commentData.id,
				dataType : 'json',
				data : {
				    id : commentData.id,
				    objectId : commentData.objectId,
				    parent : commentData.parent,
				    title : commentData.title,
				    content : commentData.content,
				    shape : commentData.shape,
				    createdByUsername : commentData.createdByUsername,
				    category : commentData.category,
				    createdOn : EMF.date.getISODateString(new Date()),
				    status : value,
				    tags: commentData.tags
				},
				success : function(data, status) {
				    $.comments.replaceData(data.objectId, data);
				    $($.comments.target).trigger('commentRefresh');
				}
		    });
		}

		function updateCategory(value, element) {
		    var obj = $(element).editable('getValue');
		    var id = '';
		    for ( var i in obj) {
				if (obj.hasOwnProperty(i)) {
				    id = i.toString();
				    break;
				}
		    }
		    id = id.substring(0, id.indexOf('category'));
		    var commentData = $.data(document.body, id);
		    var href = window.location.href;
		    var host = window.location.hostname;
		    if (window.location.port.toString().length > 0) {
		    	host = host + ':' + window.location.port;
		    }

		    var root = $.comments.editService;
		    var newCategory = $.comments.categories[value-1];
		    $.ajax({
				type : "POST",
				url : root + '/' + commentData.objectId + '/' + commentData.id,
				dataType : 'json',
				data : {
				    id : commentData.id,
				    objectId : commentData.objectId,
				    parent : commentData.parent,
				    title : commentData.title,
				    content : commentData.content,
				    shape : commentData.shape,
				    createdByUsername : commentData.createdByUsername,
				    status : commentData.status,
				    createdOn : EMF.date.getISODateString(new Date()),
				    category : newCategory.text,
				    tags: commentData.tags
				},
				success : function(data, status) {
				    $.comments.replaceData(data.objectId, data);
				    $($.comments.target).trigger('commentRefresh');
				}
		    });
		}

		$.fn.editable.defaults.mode = 'inline';
		$.fn.editable.defaults.customButtons = true;

		var statusSelector = $.comments.target + " [id$='status']";

		$(window).on('updateComments', function() {
		    $(statusSelector).each(function(index) {
				$(this).editable({
				    type : "select",
				    value : getValue($(this).html(), $.comments.statuses),
				    source : $.comments.statuses,
				    validate : function(value) {
				    	updateStatus(value, this);
				    }
				});
		    });
		});

		var categorySelector = $.comments.target + " [id$='category']";

		$(window).on('updateComments', function() {
		    $(categorySelector).each(function(index) {
				$(this).editable({
				    type : "select",
				    value : getValue($(this).html(), $.comments.categories),
				    source : $.comments.categories,
				    validate : function(value) {
				    	updateCategory(value, this);
				    }
				});
		    });
		});
    }
};