(function($) {

    $.fn.upload = function(options) {

    	var plugin = { };

	    var defaults = {
	    	// The default place where the file will be upoaded
	    	defaultUploadPath	: "",
	    	// max allowed file size
	    	maxSize				: EMF.config.maxSize,
	    	targetInstanceId	: null,
	    	targetInstanceType	: null,
	    	dynamicFields		: [],
	    	// if true show the browser (tree)
	    	showBrowser			: true,
	    	// if checkboxes should be rendered in front of the nodes to allow selection
	    	allowSelection		: true,
	    	// false if multi upload is disabled
	    	multipleFiles		: true,
	    	// true if the upload is used in unsaved iDoc/Object
	    	isNewObject			: false,
	    	restrictFileType	: false,
	    	browserConfig		: {
				container		: 'upload-panel-browser',
				node			: EMF.documentContext.rootInstanceId,
				type			: EMF.documentContext.rootInstanceType,
				currentNodeId	: EMF.documentContext.currentInstanceId,
				currentNodeType	: EMF.documentContext.currentInstanceType,
				contextPath		: SF.config.contextPath,
				rootType		: EMF.documentContext.rootInstanceType,
	    		allowSelection	: true,
	    		singleSelection	: true,
	    		changeRoot		: true,
	    		autoExpand		: true,
	    		filters			: null
	    	},
	    	uploadTemplateUrl: EMF.applicationPath + '/upload/upload.tpl.html',
	    	emfServicePath : EMF.servicePath + '/upload/upload',
	    	fileTableTemplate: null
		};

	    var dynamicFields = [];
	    var selectedTypes = [];
	    var allreadyUploeded = [];
    	plugin.settings = $.extend(true, {}, defaults, options);

    	plugin.template = '';

    	// load upload plugin template
    	plugin.loadTemplate = function(placeholder) {
    		$.ajax({
        		type: 'GET',
        		url: plugin.settings.uploadTemplateUrl,
        		complete: function(data) {
        			var templateRaw = _.template(data.responseText);
					var compiledTemplate = templateRaw({
						'chooseFileLabel' : _emfLabels['cmf.document.upload.button.add']
					});

        			plugin.template = $(compiledTemplate);
        			var data = {
            			template : $(plugin.template),
            			settings : plugin.settings
            		}

                	var template = $(data.template).appendTo(placeholder);
        			var formAction = plugin.settings.emfServicePath;
        			template.find('#fileupload').attr('action', formAction);
                	// init plugins and bind handlers
                	plugin.bindHandlers(placeholder);
        		}
    		});
		};

		// Load all document types allowed for selected section
		plugin.loadDocumentTypes = function(element, documentType, sectionId, sectionOf) {

			// Used when uploading new version of document, then choosing document type is not needed
			if (plugin.settings.fileTableTemplate !== null) {
				$('#startUpload').attr("disabled", true);
				$('#0').attr("disabled", true);
				return;
			}

			var docTypesServiceUrl = EMF.servicePath + "/upload/" + documentType + "/" + sectionId;
			if(plugin.settings.docTypesServiceUrl){
				docTypesServiceUrl = plugin.settings.docTypesServiceUrl;
			}

			$.ajax({
				dataType : "json",
				type     : 'GET',
				url      : docTypesServiceUrl,
				async    : true,
				complete : (function(element){
					return function(response) {
						var result = $.parseJSON(response.responseText);
						var startUpload = $('#startUpload');
						// if there's no available document types disable upload button
						if(result.length == 0) {
							startUpload.attr("disabled", true);
							plugin.disableEnableSingleUploadBtns(true);
						} else {
							startUpload.removeAttr("disabled");
							plugin.disableEnableSingleUploadBtns(false);
						}

						// if section selection is mandatory for file upload disable upload button
			    		if(plugin.settings.caseOnly && documentType !== 'sectioninstance') {
			    			startUpload.attr('disabled', true);
			    			plugin.disableEnableSingleUploadBtns(true);
			    		}

						element.find('option').remove();

						// This is because CMF-10072. Really useless in my opinion ...
						var empty = $('<option />');
						empty.appendTo(element);
					    $.each(result || [ ], function(index, option) {
					    	if(option.value && option.label){

					    		var optionElement = $('<option />').attr('value', option.value).attr('data-definition-uid', option.definitionUid).html(option.label);

					    		// if files filter is set allow only one type to be selected
					    		// This is used when upload popup is called from workflow or standalone task
					    		// In this situation dropdown should be filled with only one allowed value
					    		if(plugin.settings.filesFilter) {
					    			var split = plugin.settings.filesFilter.split('(');
					    			if(split[split.length - 1].slice(0,-1) == option.value) {
					    				optionElement.appendTo(element);
					    			}
					    		} else {
					    			optionElement.appendTo(element);
					    		}
					    	}
					    });

					    // If the upload popup is called from task or from relationship widget in the case dashboard
					    // user must select document section before Upload button to be enabled
		    			if(plugin.settings.task && sectionOf != 'documentsSection') {
		    				startUpload.attr('disabled', true);
		    				plugin.disableEnableSingleUploadBtns(true);
		    			} else {
		    				startUpload.attr('disabled', false);
		    				plugin.disableEnableSingleUploadBtns(false);
		    			}

					    // section selection is mandatory for file upload
			    		if(plugin.settings.filesFilter && plugin.settings.caseOnly && sectionOf == 'objectsSection') {
			    			startUpload.attr('disabled', true);
			    			plugin.disableEnableSingleUploadBtns(true);
			    		}

			    		// If for selected section there's no allowed document types upload is disabled
					    if(element.find('option').length == 0) {
					    	startUpload.attr("disabled", true);
					    	plugin.disableEnableSingleUploadBtns(true);
					    }

					    // configure select2 and triger update
			    		var config = {
			    				width: '165px'
			        	};
			    		var mandatoryFields = [];

			    		// This is needed to fix a select2 problem
			    		// http://stackoverflow.com/questions/14483348/query-function-not-defined-for-select2-undefined-error
			    		var select2ElementWithPrefix = "select#" + element.attr('id');
			    		$(select2ElementWithPrefix).select2(config).off('change');
			    		$(select2ElementWithPrefix).select2(config).on('change', function(event, triggeredByProjectsMenu) {

			    			if(plugin.settings.withoutInitParent && !triggeredByProjectsMenu){
			    				plugin.settings.targetInstanceId = '';
			    				EMF.documentContext.currentInstanceId = '';
			    			}

			    			var selectedDefinitionElement = $(event.target);
			    			// The document definition id
			    			var value = selectedDefinitionElement.val();
			    			var definitionUid = selectedDefinitionElement.find('option[value="' + value + '"]').attr('data-definition-uid');

			    			// CMF-13703 - when uploading document in workflow task and the document type is selected then the
			    			// upload buttons become active without selected section
			    			if(!value || (plugin.settings.filesFilter && plugin.settings.caseOnly && !plugin.isSectionSelected())) {
			    				return;
			    			}
			    			plugin.disableEnableUploadAllBtn();
			    			var row = $(event.target).closest('tr');
			    			var index = $(event.target).attr('row-index');
			    			selectedTypes[index] = value;
				    		$(row).find('button.remove').attr("disabled", false);
				    		$(row).find('button.cancel').attr("disabled", false);
				    		var placeholderForRow = '#mandatory-field-placeholder-' + index;
			    			var element = $(placeholderForRow);
			    			if(mandatoryFields && mandatoryFields[index]) {
			    				$(mandatoryFields[index]).unbind().removeData();
			    				$(mandatoryFields[index]).find('div').remove();
			    			}
			    			var itemUploadButtonLocator = "#row_" + index + " #" + index;
			    			var multyUploadButtonLocator = '#startUpload';

			    			if(plugin.settings.withoutInitParent && !triggeredByProjectsMenu){
			    				plugin.disableEnableSingleUploadBtns(false);
			    				plugin.settings.getLocations(definitionUid, select2ElementWithPrefix, plugin.buildParentsDropDown);
			    			}

			    			if(plugin.settings.targetInstanceId || EMF.documentContext.currentInstanceId){
			    				mandatoryFields[index] = element.buildFields({
			    	    			definitionId			: value,
			    	    			currentInstanceType		: 'documentinstance',
			    	    			parentInstanceId		: plugin.settings.targetInstanceId || EMF.documentContext.currentInstanceId,
			    	    			parentInstanceType		: plugin.settings.targetInstanceType || EMF.documentContext.currentInstanceType,
			    	    			row						: "row",
			    	    			styles					: "col-xs-6 col-sm-4",
			    	    			restrictByAttibutes : {
			    						displayType 		: ['editable'],
			    						names 				: ['name','title']
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
			    	    			services			: {
			    	    				codelistService		: REST.getClient('CodelistRestClient'),
			    	    				definitionService	: REST.getClient('DefinitionRestClient')
			    	    			},
			    	    			applicationPath			: EMF.applicationPath,
			    	    			proceedButtonLocators	: [itemUploadButtonLocator, multyUploadButtonLocator],
			    	    			idPrefix: index + '_'
			    	    		});
			    			}
						});
			    		element.select2("val", selectedTypes[element.attr('row-index')]).trigger('change');
						plugin.disableEnableUploadAllBtn();
					}
		    	}(element))
			});
		}

		/**
		 * Builds select2 menu for parent selection, when the upload is done from the main menu (upload button).
		 * Formats select options and add on change event for the menu, where the value of the selected option
		 * (the project) is set as parent for the document. Also if there is no selected option the upload button
		 * is disabled. Also if there is a default location(default project), it will be selected by default.
		 *
		 * @param data
		 * 			object containing the data for the selected options(projects)
		 * @param typeDropdownSelector
		 *          the selector of the definition type drop-down menu
		 */
		plugin.buildParentsDropDown = function(data, typeDropdownSelector) {
			plugin.disableEnableSingleUploadBtns(true);
			$('#upload-panel-browser').empty().append('<div class="locations-container"><label class="upload-required-project">' +
												  	  _emfLabels['cmf.document.upload.browse.projects'] +
												  	  '</label><div class="project-select"></div></div>');

			function formatElements(state) {
				var headerDecoration = $('<span class="icon-cell"><img src="' + EMF.util.objectIconPath(state.type, 16) +
						'" class="header-icon"></span>' + state.text);
				headerDecoration.attr('id', state.id);
				headerDecoration.find('.instance-link').removeClass('has-tooltip').addClass('upload-select-projects');
				return headerDecoration;
			};

			var selectProject = $('.project-select').select2({
				  formatResult    : formatElements,
				  formatSelection : formatElements,
				  placeholder     : _emfLabels['cmf.document.upload.browse.projects.placeholder'],
				  allowClear   	  : true,
				  data         	  : data
			}).on('change', function(evt, autoSelected){
				if(evt.added || autoSelected){
					var selected = evt.added ? evt.added : autoSelected;
					$('#instance').val(selected.type);
					$('#id').val(selected.id);
					plugin.settings.targetInstanceId = selected.id;
					plugin.settings.targetInstanceType = selected.type;
					$(typeDropdownSelector).trigger('change', [true]);
				}else{
					plugin.disableEnableSingleUploadBtns(true);
					$('[id^="mandatory-field-placeholder"]').empty();
					$('#instance').val('');
					$('#id').val('');
					plugin.settings.targetInstanceId = '';
					plugin.settings.targetInstanceType = '';
				}
			});

			selectProject.val('').trigger('change');
			// auto selects default location, if any
			data.forEach(function(index){
				 if(index.isDefaultLocation){
					 selectProject.val(index.id).trigger('change', [index]);
				 }
			});

			//if there is no auto-selected/selected value in the projects menu, we should clear parent id and type
			if(!selectProject.val()){
				$('#instance').val('');
				$('#id').val('');
				plugin.settings.targetInstanceId = '';
				plugin.settings.targetInstanceType = '';
			}
		};

		plugin.isSectionSelected = function () {
			var $checkedSection = $('.x-tree-checkbox-checked');
			if ($checkedSection.length === 0) {
				return false;
			}
			return true;
		}

		// BIG TODO: Find out WHY original plugin formatSize function do not work!!
		plugin.formatFileSize =  function (bytes) {
            if (typeof bytes !== 'number') {
                return '';
            }
            if (bytes >= 1000000000) {
                return (bytes / 1000000000).toFixed(2) + ' GB';
            }
            if (bytes >= 1000000) {
                return (bytes / 1000000).toFixed(2) + ' MB';
            }
            return (bytes / 1000).toFixed(2) + ' KB';
        }

		// Disable or enable single upload button on each row. For all rows.
		plugin.disableEnableSingleUploadBtns = function(status, position) {
			var isUploaded = function(row){
				var uploaded = $(row).find('.progress-bar-success').length;
				if(uploaded !== 0){
					return true;
				}
				return status;
			};
			if(position){
				var currentRow = $('#row_'+position);
				currentRow.find('button.cancel').attr("disabled", isUploaded(currentRow));
				return;
			}
	    	var rows = $("#table-files tr");
	    	$.each(rows, function(index, row) {
	    		$(row).find('button.cancel').attr("disabled", isUploaded($(row)));
	    	});
		}
		plugin.disableEnableUploadAllBtn = function() {
    		var typeDropdowns = $('select.docType');
    		var fieldButtonStatus = [];
    		var startUpload = $('#startUpload');
    		for(var i=0; i<typeDropdowns.length; i++){
    			var currentFieldDisabled = true;
    			if($.trim($(typeDropdowns[i]).val()) === ''){
    				currentFieldDisabled = false;
    			}
    			fieldButtonStatus.push(currentFieldDisabled);
    			plugin.disableEnableSingleUploadBtns(!currentFieldDisabled, i);
    		}
    		if(jQuery.inArray(false, fieldButtonStatus) === -1) {
    			startUpload.removeAttr("disabled");
    		} else {
    			startUpload.attr('disabled', 'disabled');
    		}
    		$('#cancelUpload').removeAttr("disabled");
		};

		// Add object browser to upload panel
		plugin.initBrowser = function() {
			var browserConfig = $.extend(true, {}, plugin.settings.browserConfig, {});
			EMF.CMF.objectsExplorer.init(browserConfig);
			var browser = $('#upload-panel-browser');
			browser.addClass("fade");
			// detect if node is checked
			browser.on('object.tree.selection', function(evt) {

 	        	var selectedItems = evt.selectedNodes;
	        	if(selectedItems.length > 0) {
	        		var firstItem = selectedItems[0];
	        		var items = (firstItem.path).split('/');
	        		var sectionOf = items[items.length - 2];

	        		$('.fields-wrapper').remove();
	        		// reload document type for new selected section and update context path
		        	$.each($('.docType'), function(index, element) {
	        			plugin.loadDocumentTypes($(element), firstItem.nodeType, firstItem.nodeId, sectionOf);
	        		});
		        	$('#contextPath').val('{ "path": ' + JSON.stringify(selectedItems) + '}');

					$('#instance').val(firstItem.nodeType);
					$('#id').val(firstItem.nodeId);

		    		if(plugin.settings.caseOnly) {
		    			$('#startUpload').attr('disabled', 'disabled');
		    			plugin.disableEnableSingleUploadBtns(true);
		    		}
		    		setTimeout(function() {
		    			plugin.placeholder.trigger('object-picker.selection-changed', {});
		    		}, 100);
	        	} else {
	        		$('#contextPath').val('');

		    		if(plugin.settings.caseOnly || plugin.settings.task) {
		    			$('#startUpload').attr('disabled', 'disabled');
		    			plugin.disableEnableSingleUploadBtns(true);
		    		}
	        	}
	        });
		}

		// Init the upload and create upload files table
		plugin.bindHandlers = function(placeholder) {
			$('#docPath').val('{ "path": ' + JSON.stringify(EMF.currentPath) + '}');
			var instanceType;
			var instanceId;
			if(plugin.settings.targetInstanceId && plugin.settings.targetInstanceType) {
				instanceId = plugin.settings.targetInstanceId;
				instanceType = plugin.settings.targetInstanceType;
			} else if(EMF.currentPath.length !== 0){
				var contextPath = EMF.currentPath;
				var lastElement = contextPath[contextPath.length - 1];
				instanceType = lastElement.type;
				instanceId = lastElement.id;
			}

			$('#instance').val(instanceType);
			$('#id').val(instanceId);

			// Because user can add, remove and single upload files many times
			// we have somehow to track which files to be send for upload and which to be skipped
			// this variables helps to keep files queue up to date
			var fileQueue = [];

			//override _formatTime method from $.blueimp.fileupload to display at least one second for uploads taking less
			//since it always shows zero times (see new Date() function in java script)
			 $.blueimp.fileupload.prototype._formatTime = function(seconds) {
				 if (typeof seconds === 'number') {
					 if (seconds < 1) {
						 seconds = 1;
					 }
				 } else {
					 return;
				 }
				 var date = new Date(seconds * 1000),
	                 days = Math.floor(seconds / 86400);
				 days = days ? days + 'd ' : '';
				 return days +
	                ('0' + date.getUTCHours()).slice(-2) + ':' +
	                ('0' + date.getUTCMinutes()).slice(-2) + ':' +
	                ('0' + date.getUTCSeconds()).slice(-2);
			 }

			 //override _renderExtendedProgress not to use data.loaded value, since calculated secods always amount to zero
			 //this way overall calculated upload time will be displayed
			 $.blueimp.fileupload.prototype._renderExtendedProgress = function(data) {
				 return this._formatBitrate(data.bitrate) + ' | ' +
				 	 this._formatTime(
		                    data.total * 8 / data.bitrate
		                ) + ' | ' +
		             this._formatPercentage(
		                    data.loaded / data.total
		                ) + ' | ' +
		             this._formatFileSize(data.loaded) + ' / ' +
		             this._formatFileSize(data.total);
		     }

			$('#fileupload').fileupload({
    			dataType: 'json',
    	        autoUpload: false,
    	        singleFileUploads: false,
    	        maxNumberOfFiles: 1,
				add: function(event, data) {

					$('#upload-panel-browser').removeClass("fade");
					$.each(data.files, function(i, file) {
						fileQueue.push(file);
					});
 					var checkForRow = null;	// The row for which the validation will be made
					$('#table-files tr').each(function(i, row) {
						if(i > 0) {
							// Remove in case that single upload is done
							$(row).remove();
						}
	                });

					$.each(fileQueue, function(index, file) {

						if(file === null) {
							return;
						}
						var errorMessage;
						if (plugin.settings.restrictFileType && !(/\.(gif|jpg|jpeg|tiff|png)$/i).test(file.name)) {
							errorMessage = 'You must select an image file only';
				        }

						// EGOV-4567
						if(!plugin.settings.multipleFiles && index > 0) {
							return false;
						}

						// if file size is ok add it to the file table
						if(!file.size || file.size < plugin.settings.maxSize && !errorMessage) {
							var $upload;
							if (plugin.settings.fileTableTemplate === null) {
								var descriptionLbl = _emfLabels['cmf.document.create.description'];
								$upload = $('<tr class="template-upload" id="row_'+ index +'">'
										   + '<td></td>'
									       + '<td>'
									       +     '<span class="preview"></span>'
									       + '</td>'
									       + '<td style="max-width:520px;">'
									       +	 '<input type="hidden" id="uuid" value="' + EMF.util.generateUUID() + '">'
									       +     '<div class="row"><div class="col-xs-6 col-sm-4 mandatory-field-column "><label class="documentTitle control-label"></label><input class="form-control mandatory-field-size placeholder-fix" type="text" id="fileName" placeholder="' + file.name + '"></div>'
									       +     '<div class="col-xs-6 col-sm-4 mandatory-field-column"><label class="fileNameLabel"></label><span class="name" style="width:158px; display:inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; vertical-align: center;">' + file.name + '</span></div> '
									       +     '<div id="document_type_wrapper" class="col-xs-6 col-sm-4 mandatory-field-column"><label class="documentType"></label><select class="mandatory-field-size docType type-' + index + '" id="type" row-index="' + index + '" chosen="documentType" data-placeholder="Select type"></select></div></div>'
									       +	 '<p><div class="row"><div class="col-xs-6 col-sm-4 mandatory-field-column"><label>'+descriptionLbl+':</label><input class="form-control mandatory-field-size placeholder-fix" type="text" id="description" style="width:520px" placeholder="' + window._emfLabels["upload.document.description"] + '"></div></div></p>'
									       +	 '<p><div id="mandatory-field-placeholder-' + index + '"></div></p>'
									       + '</td>'
									       + '<td style="min-width:100px; padding-left:25px;">'
									       +     '<label class="size"></label>'
									       +    	'<div class="fileupload-progress" style="min-width:100px"><div class="progress progress-striped active" ><div class="progress-bar" style="width:0%; font-weight:bold;"></div></div></div>'
									       + '</td>'
									       + '<td style="min-width:205px; padding-top:20px">'
									       +       '<button class="btn btn-default cancel" id="' + index + '">'
									       +             '<i class="glyphicon glyphicon-ban-circle"></i> '
									       +             '<span>Remove</span>'
									       +         '</button>'
									       +         '<button class="btn btn-default remove">'
									       +             '<i class="glyphicon glyphicon-ban-circle"></i> '
									       +             '<span>Remove</span>'
									       +         '</button>'
									       + '</td>'
									       + '</tr>');
							} else {
								$upload = $(plugin.settings.fileTableTemplate);
								$upload.find('.name').text(file.name);
								var $description = $upload.find('#description');
								$description.keyup(function(event) {
								    if (event.which !== 0 && $description.val().trim().length > 0) {
								    	$('#startUpload').removeAttr("disabled");
								    	$('#0').removeAttr("disabled");
								    } else {
								    	$('#startUpload').attr('disabled', true);
								    	$('#0').attr('disabled', true);
								    }
								});
							}
						} else {
							// show error message in the table
							if(!errorMessage) {
								errorMessage = file.name + ' Exceed max allowed size of ' + plugin.formatFileSize(plugin.settings.maxSize);
						    }
							var $upload = $('<tr class="template-upload">'
								       + '<td>'
								       +     '<span class="preview"></span>'
								       + '</td>'
								       + '<td style="max-width:520px;">'
								       +     '<span style="color: red;">' + errorMessage + '</span> '
								       + '</td>'
								       + '<td>'
								       +     '<p class="size"></p>'
								       + '</td>'
								       + '<td></td>');
						}

						$('#table-header').removeClass('hide');
						if($('#startUpload').attr("disabled") == 'disabled') {
		                    $('#table-files tr').each(function() {
		                    	if($(this).hasClass('template-upload')) {
		                    		$(this).remove();
		                    	}
		                    });
							$('#startUpload').removeAttr("disabled");
							$('#cancelUpload').removeAttr("disabled");
							$('#progress-all-wrapper').addClass("hide");

							$('#progress-all').addClass('progress-striped').addClass('active').find('.progress-bar').removeClass('progress-bar-success').removeClass('progress-bar-danger').css('width', '0%').text('');
						}
						// Form data keep additional fields which have to be send like type, description etc...
					    formData = {
					    		contextPath	: "",
					    		docPath		: "",
					    		instance	: "",
					    		id			: "",
					    		fileName	: "",
			                    type		: "",
			                    description	: "",
			                    uuid		: "",
			                    itemsCount	: ""
			                };

						data.context = $upload;
						$("#table-files").append($upload);
						plugin.loadDocumentTypes($('.type-' + index), instanceType, instanceId, "");
						$('#startUpload').removeClass('hide');
		                $('#cancelUpload').removeClass('hide');

		                $upload.find('#fileName').val(file.name);
			            $upload.find('.size').append(plugin.formatFileSize(file.size));

			            // if user is selected only one file and this file exceed max allowed size disable upload
			            if(index == 0 && file.size > plugin.settings.maxSize) {
			            	$('#startUpload').addClass('hide');
			            }

			    		// single file selection - in some cases we have to allow upload of only one file
			            // removing attribute 'multiple' prevent load more than one file in the files table
			    		if(!plugin.settings.multipleFiles) {
			    			if($('#table-files tr').length == 2) {
			    				$('#file-upload-input').removeAttr('multiple');
			    				$('.fileinput-button').attr('disabled', 'disabled');
			    			}
			    		}

			    		// single file upload
			    		var $singleFileUploadBtn = $upload.find('button.cancel');
			    		$singleFileUploadBtn.html(window._emfLabels["upload.file.upload"]);
			    		$singleFileUploadBtn.off('click').on('click',function (evt) {
			    			checkForRow = $(evt.target).attr('id');
			            	var placeholderForRow = '#mandatory-field-placeholder-' + checkForRow;
		    		    	$(placeholderForRow).trigger({ type: 'validate-mandatory-fields' });
		    		    	if (plugin.settings.fileTableTemplate !== null) {
			            		data.submit();
			            	}
		    		    	plugin.disableEnableUploadAllBtn();
			             })

			            // abort upload when cancel button is clicked
			            var $cancelButton = $upload.find('button.remove');
			    		$cancelButton.html(window._emfLabels["upload.file.remove"]);
			            $cancelButton.off('click');
			            $cancelButton.on('click', function() {

		            	var tableRow = $("#table-files tr").index($upload) - 1;
				    	$.each(fileQueue, function(i) {
				    		if(tableRow === i) {
				    			fileQueue.splice(i, 1);
				    			selectedTypes.splice(i, 1);
				    		}
				    	});
				    	$upload.remove();

		                // if all files are removed hide upload option and clear table
		                if($('#table-files tr').length == 1) {
		                	$('#startUpload').addClass('hide');
				            $('#cancelUpload').addClass('hide');
				            $('#table-header').addClass('hide');
				            $('#upload-panel-browser').addClass("fade");
				            $('.fileinput-button').removeAttr('disabled');
		                }
		            });

		            // Upload plugin doesn't work good for some browsers. Like old versions of IE and Safari
		            // That's why the following checks are needed
		            var msieVersion;
		            var safariVersion;
		            $.browser.chrome = /chrom(e|ium)/.test(navigator.userAgent.toLowerCase());
	                if($.browser.chrome){
		                $('#fileName').on('paste', function(event) {
		                	event.stopPropagation();
		                });
		                $('#description').on('paste', function(event) {
		                	event.stopPropagation();
		                });
	                }
		            if ($.browser.msie ) {
		            	msieVersion = $.browser.version;
		            }

		            if ($.browser.safari && navigator.appVersion.indexOf("Win") != -1) {
		            	safariVersion = $.browser.version;
		            }

		            // image preview can not be shown in IE < 10 and Safari for Windows
		            if(msieVersion != "8.0" && msieVersion != "9.0" && safariVersion != "534.57.2" && $.browser.version != "536.28.10") {
		            	// add thumbnail preview
			            var image = document.createElement('img');
			            image.src = URL.createObjectURL(file);
			            image.width = 80;

			            image.onerror = function () {
			            	this.style.display = "none";
			            }

			            $upload.find('.preview').append(image);
		            }

		            plugin.translateLabels();
					});

					// Wait for all mandatory fields to be validated. That's why the counter is needed
					var counter = 1;
					var rows = $("#table-files tr");
					var row = 0;		// the row which will be validated
					placeholder.off('data-is-valid');
	    			placeholder.on('data-is-valid', function(eventData) {
	       			 	row++;
	            		dynamicFields.push(eventData.fields);
	       			 	if(rows.find('.has-error').length===0 && counter === rows.length-1 || checkForRow){
			            	data.uploadRow = checkForRow;
			            	data.submit().success(function(response) {
			            	});
			            } else {
			            	counter++;
			            	$('#mandatory-field-placeholder-' + row).trigger({ type: 'validate-mandatory-fields' });
			            }
	        		});
	    			// If data is invalid reset and try validation again on the next submit click
	    			placeholder.on('data-is-invalid', function(eventData) {
	    				setTimeout(function() {
	    					counter = 1;
	    					row = 0;
	    					dynamicFields = [];
	    				});
	        		});
		            var uploadAll = $('#startUpload');
		            uploadAll.off('click').on('click',function () {
		            	checkForRow = null;
		            	// call validation for every selected file
		            	var placeholders = $('div[id^=mandatory-field-placeholder-]');
		            	placeholders.each(function(id, elem) {
		            		$(elem).trigger({ type: 'validate-mandatory-fields' });
		            	});
		            	if (plugin.settings.fileTableTemplate !== null) {
		            		plugin.disableEnableUploadAllBtn();
		            		data.submit();
		            	}
		            	return false;
		             });

	                var cancelAllButton = $('#cancelUpload');
	                cancelAllButton.on('click',function () {
	                	data.abort();
	    		    	$('#startUpload').off('click');
	    		        $('#cancelUpload').off("click");
	                	$('#startUpload').attr("disabled", true);
	                    $('#cancelUpload').attr("disabled", true);
	                    var cancelButtons = data.context.find('button.cancel');
	    		        cancelButtons.off('click');
	    		        cancelButtons.attr("disabled", true);
	    		        var rows = $("#table-files tr");

	    		    	$.each(rows, function(index, row) {
	    		    		if(index > 0) {
	    		    			$(row).remove();
	    		    		}
	    		    	});
	    		    	fileQueue = [];
	    		    	$('#startUpload').addClass('hide');
			            $('#cancelUpload').addClass('hide');
			            $('#table-header').addClass('hide');
			            $('.fileinput-button').removeAttr('disabled');
			            $('#upload-panel-browser').addClass("fade");
	                });

			},

			// plugin submit function - send files to server for upload
			submit: function(e, data) {

				var fileName = [];
				var type = [];
				var description = [];
				var uuid = [];
			 	var itemsCount = 0;
			    var dataToStore = [];
			    var isMajorVersion = [];
			    var singleUploadFlag = $.trim(data.uploadRow);

			    // check if it's a sinlge upload, may receive zero
			    if(singleUploadFlag) {
			        var row = $('#row_' + data.uploadRow);
			        var tableRow = $("#table-files tr").index(row) - 1;
			        var cell = row.children();
			        allreadyUploeded.push(row.find('#uuid').val());
			    	$.each(cell.find('*'), function(index, child) {
			    		dataToStore.push(child);
			    	});
			    	$.each(fileQueue, function(i, file) {
			    		if(tableRow === i) {
			    			data.files = [file];
			    			fileQueue[i] = null;
			    		}
			    	});
			    } else {
			    	var notUploaded = [];
			    	$.each(fileQueue, function(i, file) {
			    		if(fileQueue[i] !== null) {
			    			notUploaded.push(file);
			    		}
			    	});
			    	data.files = notUploaded;
			    	dataToStore = data.form[0];
			    }
			    
			    // loop through all fields in the form
			    for(var i=0; i<dataToStore.length; i++){
			    	var field = $(dataToStore[i]);
			    	var uuidField = field.closest('.template-upload').find('#uuid');
			    	// check if the current document is already uploaded
			    	// and skip the field from the form
			    	var a = jQuery.inArray(uuidField.val(), allreadyUploeded);
			    	if(uuidField && !singleUploadFlag && a !== -1){
		    			continue;
			    	}
			    	// field identifier
			    	var fieldId = field.get(0).id;
			    	// field value
			    	var fieldValue = field.val();
			    	if(fieldId === 'type'){
			    		type.push(fieldValue);
			    		itemsCount++;
			    	}else if(fieldId ==='fileName'){
			    		fileName.push(fieldValue || " ");
			    	}else if(fieldId ==='description'){
			    		description.push(fieldValue || " ");
			    	}else if(fieldId ==='uuid'){
			    		uuid.push(fieldValue);
			    	}
			    }
			    if (plugin.settings.fileTableTemplate !== null) {
			    	isMajorVersion.push($('input[name="isMajorVersion"]:checked').val());
			    	fileName.push($('.name').text());
			    }
			    // preventing duplicate mandatory fields
			    dynamicFields = jQuery.unique(dynamicFields);
			    // send data with one request
			    // make sure the complex data is escaped properly
			    data.formData = {
			    		contextPath	: $('#contextPath').val(),
			    		docPath		: $('#docPath').val(),
			    		instance	: $('#instance').val(),
			    		id			: $('#id').val(),
	                    type		: JSON.stringify(type),
	                    fileName	: JSON.stringify(fileName),
	                    description	: JSON.stringify(description),
	                    itemsCount	: itemsCount,
	                    uuid		: JSON.stringify(uuid),
	                    mandatory	: JSON.stringify(dynamicFields),
	                    isMajorVersion : JSON.stringify(isMajorVersion)
	            };
			    // prevent cache conflicts
			    dynamicFields = [];
			    $('#progress-all-wrapper').removeClass("hide");
			},

			// show progress of upload
		    progress: function(e, data) {
		    	var progress = parseInt(data.loaded / data.total * 100, 10);

		    	if($.trim(data.uploadRow)) {
			    	var row = $('#row_' + data.uploadRow);
			    	$(row).find('.progress').find('.progress-bar')
	                .css('width', progress + '%')
	                .text(progress+'%');
			    } else {
			    	var rows = $("#table-files tr");
			    	$.each(rows, function(index, row) {
			    		$(row).find('.progress').find('.progress-bar')
		                .css('width', progress + '%')
		                .text(progress+'%');
			    	});
			    }
		    },

		    // file upload is done
		    done: function(e, data) {
		    	if($.trim(data.uploadRow)) {
			    	var row = $('#row_' + data.uploadRow);
			    	row.find('.progress')
	                .removeClass('active')
	                .removeClass('progress-striped')
	                .addClass('progress')
	                .find('.progress-bar')
	                .addClass('progress-bar-success')
	                .css('width', '100%')
	                .text(window._emfLabels["upload.upload.complete"]);
			    	row.find('button.cancel').attr("disabled", true);
			    	row.find('button.remove').attr("disabled", true);
			    } else {
			    	var rows = $("#table-files tr");
			    	$.each(rows, function(index, row) {
			    		var currentRow = $(row);
			    		currentRow.find('.progress')
			                .removeClass('active')
			                .removeClass('progress-striped')
			                .addClass('progress')
			                .find('.progress-bar')
			                .addClass('progress-bar-success')
			                .css('width', '100%')
			                .text(window._emfLabels["upload.upload.complete"]);
			    		currentRow.find('button.remove').attr("disabled", true);
			    		currentRow.find('button.cancel').attr("disabled", true);
			    	});
			    	var start = $('#startUpload');
			    	var cancel = $('#cancelUpload');
			    	start.off('click');
			    	cancel.off("click");
			    	cancel.attr("disabled", true);
			        start.attr("disabled", true);
			        fileQueue = [];
			        selectedTypes = [];
			    }
		        $('#progress-all')
		        .removeClass('progress-striped')
                .addClass('progress')
                .find('.progress-bar')
                .addClass('progress-bar-success')
                .css('width', '100%')
                .text(window._emfLabels["upload.upload.complete"]);
		        // fire event that upload is successful
		        plugin.fileUploadDone(data.result);
		        // render checkbox in front of uploaded file if needed
		        if(plugin.settings.allowSelection) {
		        	plugin.responseDecorator(data.result, row);
		        }
		    },

		    // upload fail
		    fail: function(e, data) {
		    	if(e.currentTarget) {
		    		return;
		    	}
		    	var rows = $("#table-files tr");
		    	$.each(rows, function(index, row) {
		    		$(row).find('.progress')
	                .removeClass('active')
	                .removeClass('progress-bar-info')
	                .addClass('progress-bar-danger')
	                .find('.progress-bar')
	                .addClass('progress-bar-danger')
	                .css('width', '100%')
	                .text(window._emfLabels["upload.upload.failed"]);
		    	});

		    	$('#progress-all')
		        .removeClass('progress-striped')
                .addClass('progress')
                .find('.progress-bar')
                .addClass('progress-bar-danger')
                .text(window._emfLabels["upload.upload.failed"]);

		    	// fire event that upload failed
		    	plugin.placeholder.trigger("file-upload-fail");
		    },
		    stop: function(e, data) {

		    }

    		});

    		// single file selection
    		if(!plugin.settings.multipleFiles) {
    			$('#file-upload-input').removeAttr('multiple');
    		}

    		// show project browser
    		if(plugin.settings.showBrowser) {

    			if(_.indexOf(["sectioninstance", "objectinstance", "documentinstance"], instanceType) == -1) {
    				// TODO: don't override provided filters
    			    var filters = plugin.settings.browserConfig.filters;
    			    plugin.settings.browserConfig.filters = filters || "instanceType=='sectioninstance'&&purpose==null";
    				plugin.settings.browserConfig.changeRoot = false;
    				plugin.settings.browserConfig.allowRootSelection = false;
    				plugin.settings.task = true;

    				var contextPath = EMF.currentPath;
    				if(instanceType == 'caseinstance') {
    					var root = contextPath[contextPath.length - 1];
    				} else {
    					var root = contextPath[1];
    				}

    				if(root && root.type == 'caseinstance') {
    					plugin.settings.browserConfig.rootNodeText = root.compactHeader || _emfLabels['cmf.root.node.text']; //Case
    					plugin.settings.browserConfig.node = root.id;
    					plugin.settings.browserConfig.type = root.type;
    				}

    				if(contextPath.length == 1) {
    					root = contextPath[0];
    					plugin.settings.browserConfig.rootNodeText = root.compactHeader;
    					plugin.settings.browserConfig.node = root.id;
    					plugin.settings.browserConfig.type = root.type;
    				}
    			}
    			plugin.initBrowser();
    		}

    		plugin.translateLabels();

    		// Somethimes upload in unsaved document is disabled. In this case show error message
    	    if(plugin.settings.isNewObject) {
    	    	$('.upload-wrapper').empty();
    	    	$('.upload-wrapper').append("<div class='upload-error-message'>" + window._emfLabels["upload.error.messge"] + "</div>");
    	    }
		};

		// Translate labels using properties
		plugin.translateLabels = function() {
    		$("#startUpload").html('<i class="glyphicon glyphicon-upload"></i> <span>' + window._emfLabels["upload.start.upload"] + '</span>');
    		$("#cancelUpload").html('<i class="glyphicon glyphicon-ban-circle"></i> <span>' + window._emfLabels["upload.cancel.upload"] + '</span>');

    		var labelBundle = 'upload.add.files';
	        if (!$('#file-upload-input').is('[multiple]')) {
    	       labelBundle = 'upload.add.file';
    	    }
    	    $("#addFiles").html(window._emfLabels[labelBundle]);
    		$(".documentTitle").html(window._emfLabels["upload.document.title"]);
    		$(".fileNameLabel").html(window._emfLabels["upload.file.name"]);
    		$(".documentType").html(window._emfLabels["upload.document.type"]);
    		$("#fileSize").html(window._emfLabels["upload.file.size"]);
		}

		plugin.fileUploadDone = function(data) {
			uploadedFiles = [];

			// collect data
			$.each(data, function(index, item) {
				uploadedFiles.push(item);
			});

			// fire an event with the collected data
			plugin.placeholder.trigger({
				type: 'file-upload-done',
				uploadedFiles: uploadedFiles
			});

			// this event is fired bacause issue - CMF-5569
			$('.idoc-editor').trigger({
				type: 'file-upload-done',
				uploadedFiles: uploadedFiles
			});

			// we need this to redirect correctly after the form is closed, when the upload is done through main menu
			if(plugin.settings.withoutInitParent && uploadedFiles.length > 0){
				var uploadedDocument = uploadedFiles[0];
				EMF.uploadedDocument = uploadedDocument;
				EMF.documentContext.currentInstanceId = uploadedDocument.dbId;
				EMF.documentContext.currentInstanceType = uploadedDocument.type;
			}
		}

		// Add checkbox or radiobuton in front of each uploaded file
		plugin.responseDecorator = function(data, row) {
			var selectedItems = [];
			$.each(data, function(index, item) {

				if(row) {
			    	var rows = row;
				} else {
					var rows = $("#table-files tr").eq(index + 1);
				}

				$firstCell = $('td:first-child', rows);

				var tdCheckbox = $('<td></td>');
				if(plugin.settings.singleSelection) {
					var checkbox = $('<input type="radio" name="picker-radio-group" value="'+ item.dbId +'" />');
				} else {
					var checkbox = $('<input type="checkbox" value="'+ item.dbId +'" />');
				}
				checkbox.data('data', item);

				checkbox.change(function(event) {

					var input = $(event.target);
					var data = $(this).data('data');
					data.op = input.prop('checked') ? 'add' : 'remove';
					data.value = input.val();
					data.srcElement = input;

					// gather all checked file rows
				    if(input.prop('checked')) {
				    	selectedItems.push(data);
				    } else {
				        for(var i = selectedItems.length; i--;) {
				            if(selectedItems[i] === data) {
				            	selectedItems.splice(i, 1);
				            }
				        }
				    }

					var selectionData = {
					    	currentSelection 	: data,
					    	selectedItems		: selectedItems
					};

					$(this).trigger('uploaded.file.selection', selectionData);
				});

				checkbox.appendTo(tdCheckbox);
	    		tdCheckbox.appendTo($firstCell);
			});
		};

    	// build the UI
    	return this.each(function(index) {
			// make sure only one instance of the plugin is initialized for element
    	    var initialized = $.data(this, 'plugin_upload' );
    	    if (!initialized) {
    	    	$.data(this, 'plugin_upload', true);
    	    } else {
    	    	return;
    	    }

    		var placeholder = plugin.placeholder = $(this);

    		// Load the upload template, parse it and append it to the placeholder
    		if (plugin.settings.uploadTemplateUrl) {
    			plugin.loadTemplate(placeholder);
    		} else {
    			// if template is loaded already, we just bind the handlers
    			plugin.bindHandlers(placeholder);
    		}

    		plugin.placeholder

    		// Prevent upload start if "Enter" is clicked
    		placeholder.bind('keydown', function(event) {
    			if(event.keyCode == 13) {
    				event.preventDefault();
    			}
    	    })

    		return this;
    	});
    }

}(jQuery));