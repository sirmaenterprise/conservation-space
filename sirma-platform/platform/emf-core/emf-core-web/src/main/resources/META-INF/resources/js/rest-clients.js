var REST = REST || {};

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//
// Every rest client method accepts a configuration object. This object can contain:
// pathParams: [pathparam1, pathparam2]
// queryParams: { param1: paramValue, param2: paramValue }
//
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

/**
 * A pool with rest client instances.
 */
REST.clients = {};
/**
 * Factory for the rest clients. If a rest client is already instantiated, then the instance is
 * reused. Get an instance of given rest client: REST.getClient('InstanceRestClient');
 *
 * @param name
 *        The rest client name.
 */
REST.getClient = function(name) {
	if (!REST.clients[name]) {
		REST.clients[name] = new REST[name]({
			servicePath : EMF.servicePath
		});
	}
	return REST.clients[name];
};

/**
 * Builds service url.
 *
 * @param basePath
 *        Is some path to which additional path and query parameters to be appended.
 * @param opts
 */
REST.buildServiceUrl = function(basePath, opts) {
	var servicePath = basePath;
	servicePath += opts.pathParams ? opts.pathParams.join('/') : '';
	var servicePathLength = servicePath.length;
	if (servicePath.slice(servicePathLength - 1) === '/') {
		servicePath = servicePath.slice(0, servicePathLength - 1);
	}
	servicePath += opts.queryParams ? '?' + $.param(opts.queryParams) : '';
	return servicePath;
};

/**
 * Default error handler.
 */
REST.failHandler = function(jqXHR, textStatus, errorThrown) {
	console.error('Error:', errorThrown, '\nStatus:', textStatus);
};

/**
 * Rest client for the InstanceRestService.
 */
REST.InstanceRestClient = (function() {

	function InstanceRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var config, paths = {
		INSTANCE : '/instances/'
	};

	/**
	 * Load allowed children for given instance.
	 */
	InstanceRestClient.prototype.getAllowedChildren = function(opts) {
		var servicePath = this.config.servicePath + paths.INSTANCE + opts.instanceId + '/allowedChildren', params = {
			instanceType : opts.instanceType,
			childType : 'document'
		};
		var promise = $.ajax({
			url : servicePath,
			data : params
		});
		promise.fail(REST.failHandler);
		return promise;
	};

	InstanceRestClient.prototype.moveInstance = function(opts, failHandler) {
		var servicePath = this.config.servicePath + paths.INSTANCE + 'move';
		var promise = $.ajax({
			type : 'POST',
			url : servicePath,
			contentType : EMF.config.contentType.APP_JSON,
			data : JSON.stringify(opts)
		});
		promise.fail(failHandler || REST.failHandler);
		return promise;
	};

	/**
	 * Get sub type of the owning instance.
	 */
	InstanceRestClient.prototype.getOwningInstanceSubType = function(opts) {
		var servicePath = this.config.servicePath + paths.INSTANCE + 'parentInstanceType';
		var promise = $.ajax({
			url : servicePath,
			data : opts
		});
		promise.fail(REST.failHandler);
		return promise;
	};

	InstanceRestClient.prototype.getOriginalInstanceProperties = function(opts, callbackFunc) {
		var promise = $.ajax({
			url : EMF.servicePath + "/instances/properties/original",
			data : opts
		});
		promise.success(function(responce) {
			callbackFunc(responce);
		});
		promise.fail(REST.failHandler);
		return promise;
	};
	return InstanceRestClient;
})();

/**
 * Rest client for the UploadRestService.
 */
REST.UploadRestClient = (function() {
	function UploadRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var config, paths = {
		UPLOAD : '/upload/'
	};

	/**
	 * Retrieve document types allowed to be uploaded/created in specified section.
	 */
	UploadRestClient.prototype.retrieveAllowedTypes = function(opts) {
		var basePath = this.config.servicePath + paths.UPLOAD;

		var promise = $.ajax({
			url : REST.buildServiceUrl(basePath, opts)
		});
		promise.fail(REST.failHandler);
		return promise;
	};

	return UploadRestClient;
})();

/**
 * Rest client for the CodelistRestService.
 */
REST.CodelistRestClient = (function() {
	function CodelistRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var config, paths = {
		CODELIST : '/codelist/'
	};

	/**
	 * Retrieves codelist values for given codelist
	 */
	CodelistRestClient.prototype.retrieveCodeValues = function(opts) {
		var basePath = this.config.servicePath + paths.CODELIST;
		var promise = $.ajax({
			url : REST.buildServiceUrl(basePath, opts)
		});
		promise.fail(REST.failHandler);
		return promise;
	};

	return CodelistRestClient;
})();

/**
 * Rest client for the DefinitionRestService.
 */
REST.DefinitionRestClient = (function() {
	function DefinitionRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var config, paths = {
		DEFINITION : '/definition/'
	};

	/**
	 * Retrieve fields from given definition.
	 */
	DefinitionRestClient.prototype.fields = function(opts) {
		var basePath = this.config.servicePath + paths.DEFINITION + opts.definitionId + '/fields';
		var promise = $.ajax({
			url : REST.buildServiceUrl(basePath, opts)
		});
		promise.fail(REST.failHandler);
		return promise;
	};

	return DefinitionRestClient;
})();

/**
 * Rest client for the FavouritesRestService.
 */
REST.FavouritesRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function FavouritesRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		FAVOURITES : '/favourites'
	};

	/**
	 * Adds instance to current user favourites.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	FavouritesRestClient.prototype.add = function(opts) {
		var requestData = this.buildServiceRequestData(opts);
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Removes instance from current user favourites.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	FavouritesRestClient.prototype.remove = function(opts) {
		var requestData = this.buildServiceRequestData(opts);
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Builds and returns prepared request object.
	 */
	FavouritesRestClient.prototype.buildServiceRequestData = function(opts){
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : opts.type,
		        data		: JSON.stringify(opts.data),
		        url         : this.config.servicePath + paths.FAVOURITES
		    };
		return requestData;
	};

	return FavouritesRestClient;
})();

/**
 * Rest client for the DownloadsRestService.
 */
REST.DownloadsRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function DownloadsRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		DOWNLOADS  : '/downloads',
		REMOVE_ALL : '/downloads/all',
		ZIP 	   : '/downloads/zip'
	};

	/**
	 * Adds instance to current user downloads list.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	DownloadsRestClient.prototype.add = function(opts) {
		var requestData = this.buildServiceRequestData(opts);
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Removes instance from current user downloads list.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	DownloadsRestClient.prototype.remove = function(opts) {
		var requestData = this.buildServiceRequestData(opts);
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Removes all instances from current user downloads list.
	 */
	DownloadsRestClient.prototype.removeAll = function() {
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'DELETE',
		        url         : this.config.servicePath + paths.REMOVE_ALL
		    };
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Creates archive in DMS with the documents, which are marked for download by the current user.
	 * This service will return the DMS id of the archive, which can be used for other service calls.
	 */
	DownloadsRestClient.prototype.createArchive = function() {
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'POST',
		        url         : this.config.servicePath + paths.ZIP
		    };
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Gets the archive status, using rest call. This service returns object with information about the progress
	 * of the archivation.
	 * <p>
	 * <b>Object information example:</b>
	 * {
	 *	"status": "DONE",
	 *	"done": "66",
	 *	"total": "66",
	 *  "filesAdded": "2",
	 *	"totalFiles": "2"
	 * }
	 * <p>
	 * <b>Possible statuses:</b>
	 * PENDING 					 - The archiving hasn't started yet.
	 * IN_PROGRESS 				 - The archive is not ready for download.
	 * DONE 				     - The archiving is complete and the archive can be downloaded.
	 * MAX_CONTENT_SIZE_EXCEEDED - The file size is too large to be zipped up.
	 * CANCELLED				 - The archiving was stopped or the archive was deleted.
	 *
	 * @param opts
	 * 			contains request data for the service call (id of the archive)
	 */
	DownloadsRestClient.prototype.getArchiveStatus = function(opts) {
		var url = this.config.servicePath + paths.ZIP + opts.zipId + '/status';
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'GET',
		        url         :  url
		    };
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Gets the URL to the completed archive. This service should be called, when the status
	 * of the archive is DONE.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	DownloadsRestClient.prototype.getArchive = function(opts) {
		var url = this.config.servicePath + paths.ZIP + opts.zipId;
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'GET',
		        url         :  url
		    };
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Removes the given archive for the DMS or cancels it creation, can be used either ways.
	 *
	 * @param opts
	 * 			contains request data for the service call (id and type of the instance, type of the request)
	 */
	DownloadsRestClient.prototype.removeArchive = function(opts) {
		var url = this.config.servicePath + paths.ZIP + opts.zipId;
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'DELETE',
		        url         :  url
		    };
		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	/**
	 * Builds and returns prepared request object.
	 *
	 * @param opts
	 * 			contains the request type and data
	 */
	DownloadsRestClient.prototype.buildServiceRequestData = function(opts){
		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : opts.type,
		        data		: JSON.stringify(opts.data),
		        url         : this.config.servicePath + paths.DOWNLOADS
		    };
		return requestData;
	};

	return DownloadsRestClient;
})();

/**
 * Rest client for the RelationsRestService.
 */
REST.RelationsRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function RelationsRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		RELATIONS : '/relations/'
	};

	/**
	 * Creates relation of given type between two objects. Calls RelationsRestService create method.
	 *
	 * Request object format example:
	 * <code>
	 * <pre>
	 * {
	 *  relType       : 'emf:someRelation',
     *  reverseRelType: 'emf:reverseSomeRelation',
     *  operationId   : 'someOperation'(optional: if you want to log some operation in audit),
     *  system        : true(optional: by default is false),
	 *  selectedItems : {
	 *                   someKey_1 : {
	 *                                targetId   : targetInstanceId,
	 *           		              targetType : targetInstanceType,
	 *           		              destId     : destInstanceId,
	 *           		              destType   : destInstanceType
	 *                               },
	 *                   someKey_2 : {...},
	 *                   ...}
	 * }
	 * </pre>
	 * </code>
	 *
	 * @param opts
	 *           object which contains some configurations and data object for the request
	 * @return service response
	 */
	RelationsRestClient.prototype.createRelation = function(opts){

		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'POST',
		        data		: JSON.stringify(opts.data),
		        url         : this.config.servicePath + paths.RELATIONS + 'create'
		};

		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	return RelationsRestClient;
})();

/**
 * Rest client for the DocumentRestService.
 */
REST.DocumentRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function DocumentRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		ATTACH : '/document/'
	};

	/**
	 * Creates relation of given type between two objects. Calls DocumentRestService attach method.
	 *
	 * Request object format example:
	 * <code>
	 * <pre>
	 *  currentInstanceId   : instanceId,
	 * 	currentInstanceType : instanceType,
	 *  selectedItems 		: {
	 *                   		someKey_1 : {
	 *           		              		 dbId : destInstanceId,
	 *           		              		 type : destInstanceType
	 *                               		},
	 *                  	 	someKey_2 : {...},
	 *                   	   ...}
	 * }
	 * </pre>
	 * </code>
	 *
	 * @param opts
	 *           object which contains some configurations and data object for the request
	 * @return service response
	 */
	DocumentRestClient.prototype.attach = function(opts){

		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'POST',
		        data		: JSON.stringify(opts.data),
		        url         : this.config.servicePath + paths.ATTACH
		};

		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	return DocumentRestClient;
})();

/**
 * Rest client for the ObjectRestService.
 */
REST.ObjectRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function ObjectRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		ATTACH : '/object-rest/'
	};

	/**
	 * Creates relation of given type between two objects. Calls ObjectRestService attach method.
	 *
	 * Request object format example:
	 * <code>
	 * <pre>
	 * {
	 *  currentInstanceId   : instanceId,
	 * 	currentInstanceType : instanceType,
	 *  selectedItems 		: {
	 *                   		someKey_1 : {
	 *           		              		 dbId : destInstanceId,
	 *           		              		 type : destInstanceType
	 *                               		},
	 *                  	 	someKey_2 : {...},
	 *                   	   ...}
	 * }
	 * </pre>
	 * </code>
	 *
	 * @param opts
	 *           object which contains some configurations and data object for the request
	 * @return service response
	 */
	ObjectRestClient.prototype.attach = function(opts){

		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'POST',
		        data		: JSON.stringify(opts.data),
		        url         : this.config.servicePath + paths.ATTACH
		};

		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	return ObjectRestClient;
})();

/**
 * Rest client for the DefaultLocationRestService.
 */
REST.DefaultLocationRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function DefaultLocationRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		DEFAULT_LOCATION : '/default-locations/'
	};

	/**
	 * Retrieves locations for given definition type. If there is default location for the passed definition type, its
	 * default location property is passed to the response object. For the others instances in the response this
	 * property is missing.
	 *
	 * <p>
	 * Example response:
	 * <pre>
	 * {locations : [{
	 *                instanceId   : instanceId,
	 * 				  instanceType : instanceType,
	 *                header       : compact_header
	 *               },
	 *               {
	 *                instanceId      : instanceId,
	 * 				  instanceType    : instanceType,
	 *                header          : compact_header,
	 *                defaultLocation : definitionId
	 *               },
	 *               {
	 *                instanceId   : instanceId,
	 * 				  instanceType : instanceType,
	 *                header       : compact_header
	 *               },
	 *               ...
	 *               ]
	 * }
	 * </pre>
	 *
	 * @param opts
	 *           object which contains some configurations and data object for the request
	 * @return service response
	 */
	DefaultLocationRestClient.prototype.getLocations = function(opts){

		var requestData = {
				contentType : EMF.config.contentType.APP_JSON,
		        type        : 'GET',
		        url         : this.config.servicePath + paths.DEFAULT_LOCATION + opts.definitionId
		};

		var promise = $.ajax(requestData);
		promise.fail(REST.failHandler);
		return promise;
	};

	return DefaultLocationRestClient;
})();

/**
 * Rest client for the DateRestService.
 */
REST.DateRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function DateRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		CALCULATE_DATE : '/date/'
	};

	/**
	 * Calculates the date from a starting date and number of days to add/subtract to/from it. 
	 * Calls DateRestService calculate date method.
	 *
	 * @param opts
	 *           object which contains some configurations for the request
	 * @return service response
	 */
	DateRestClient.prototype.calculate = function(opts){
		var basePath = this.config.servicePath + paths.CALCULATE_DATE + opts.operation;
		var promise = $.ajax({
			contentType : EMF.config.contentType.APP_JSON,
	        type        : 'GET',
			url 		: REST.buildServiceUrl(basePath, opts)
		});

		promise.fail(REST.failHandler);
		return promise;
	};

	return DateRestClient;
})();

/**
 * Rest client for the WebNavigationRestService.
 */
REST.WebNavigationRestClient = (function() {

	/**
	 * Client constructor. Collects configurations.
	 *
	 * @param opts
	 * 			the opts object
	 */
	function WebNavigationRestClient(opts) {
		this.config = $.extend(true, this.config, {}, opts);
	}

	var paths = {
		NEW_WEB : '/navigation/new-web'
	};

	/**
	 * Retrieves the link to the new web.
	 *
	 * @param opts
	 *           object which contains some configurations for the request
	 * @return service response
	 */
	WebNavigationRestClient.prototype.getLinkToNewWeb = function(opts){
		var basePath = this.config.servicePath + paths.NEW_WEB;
		var promise = $.ajax({
			contentType : EMF.config.contentType.APP_JSON,
	        type        : 'GET',
			url 		: REST.buildServiceUrl(basePath, opts)
		});

		promise.fail(REST.failHandler);
		return promise;
	};

	return WebNavigationRestClient;
})();