(function($) {

    $.fn.upload = function(options) {

    	var plugin = { };
    	
	    var defaults = {
	    	defaultUploadPath	: "",	// The default place where the file will be upoaded
	    	maxSize				: EMF.config.maxSize,	// max allowed file size
	    	targetInstanceId	: null,
	    	targetInstanceType	: null,
	    	showBrowser			: true,	// if true show the browser (tree)
	    	allowSelection		: true,	// if checkboxes should be rendered in front of the nodes to allow selection
	    	multipleFiles		: true, // false if multi upload is disabled 
	    	isNewObject			: false,// true if the upload is used in unsaved iDoc/Object
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
	    	uploadTemplateUrl: EMF.applicationPath + '/upload/upload.tpl.html'
		};
	    
    	plugin.settings = $.extend(true, {}, defaults, options);
    	
    	plugin.template = '';
 
    	// load upload plugin template
    	plugin.loadTemplate = function(placeholder) {
    		$.ajax({
        		type: 'GET',
        		url: plugin.settings.uploadTemplateUrl,
        		complete: function(data) {
        			plugin.template = data.responseText;
        			var data = {
            			template : $(plugin.template),
            			settings : plugin.settings
            		}

                	var template = $(data.template).appendTo(placeholder);
        			var formAction = EMF.servicePath + '/upload/upload';
        			template.find('#fileupload').attr('action', formAction);
                	// init plugins and bind handlers
                	plugin.bindHandlers(placeholder);
        		}
    		});
		};

		// Load all document types allowed for selected section
		plugin.loadDocumentTypes = function(element, documentType, sectionId, sectionOf) {
			$.ajax({
				dataType: "json",
				type: 'GET',
				url: EMF.servicePath + "/upload/" + documentType + "/" + sectionId ,
				async: true,
				complete: (function(element) {
					return function(response) {
						var result = $.parseJSON(response.responseText);

						// if there's no available document types disable upload button
						if(result.length == 0) {
							$('#startUpload').attr("disabled", true);
							plugin.disableEnableSingleUploadBtns(true);
						} else {
							$('#startUpload').removeAttr("disabled");
							plugin.disableEnableSingleUploadBtns(false);
						}
						
						// if section selection is mandatory for file upload disable upload button
			    		if(plugin.settings.caseOnly && documentType != 'sectioninstance') {
			    			$('#startUpload').attr('disabled', true);
			    			plugin.disableEnableSingleUploadBtns(true);
			    		}
						
						element.find('option').remove();
					    $.each(result || [ ], function(index, option) {
					    	if(option.value && option.label){

					    		var optionElement = $('<option />').attr('value', option.value).html(option.label);
				
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
		    				$('#startUpload').attr('disabled', true);
		    				plugin.disableEnableSingleUploadBtns(true);
		    			} else {
		    				$('#startUpload').attr('disabled', false);
		    				plugin.disableEnableSingleUploadBtns(false);
		    			}
		    			
					    // section selection is mandatory for file upload
			    		if(plugin.settings.filesFilter && plugin.settings.caseOnly && sectionOf == 'objectsSection') {
			    			$('#startUpload').attr('disabled', true);
			    			plugin.disableEnableSingleUploadBtns(true);
			    		}
			    		
			    		// If for selected section there's no allowed document types upload is disabled
					    if(element.find('option').length == 0) {
					    	$('#startUpload').attr("disabled", true);
					    	plugin.disableEnableSingleUploadBtns(true);
					    }
					    
					    // configure chosen and triger update
			    		var config = {
			    				width: '165px'
			        	};
			    		element.chosen(config).change(function(event) {
			    			var value = $(event.target).val();
			    		});
			    		element.trigger("chosen:updated");
					}
		    	}(element)) 
			});
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

		// Disable or enable single upload button on each row
		plugin.disableEnableSingleUploadBtns = function(status) {
	    	var rows = $("#table-files tr");
	    	$.each(rows, function(index, row) {
	    		$(row).find('button.remove').attr("disabled", status);
	    		$(row).find('button.cancel').attr("disabled", status);
	    	});
		}
		
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

	        		// reload document type for new selected section and update context path
		        	plugin.loadDocumentTypes($('.docType'), firstItem.nodeType, firstItem.nodeId, sectionOf);
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
			} else {
				var contextPath = EMF.currentPath;
				var lastElement = contextPath[contextPath.length - 1];
				instanceType = lastElement.type;
				instanceId = lastElement.id;
			}

			$('#instance').val(instanceType);
			$('#id').val(instanceId);

			// Because user can add, remove and single upload files many times
			// we have somehow to track which files to be send for upload and which to be skiped
			// this variables helps to keep files queue up to date
			var fileQueue = [];	
			var orgFiles = [];
		 	var concFilesCopy = [];
		 	
    		$('#fileupload').fileupload({
    			dataType: 'json',
    	        autoUpload: false,
    	        singleFileUploads: false,
    	        maxNumberOfFiles: 1,
				add: function(event, data) {
					
					$('#upload-panel-browser').removeClass("fade"); 
					fileQueue.push(data);
					$.each(data.files, function(index, file) {
						
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
							var $upload = $('<tr class="template-upload" id="row_'+ index +'">'
									       + '<td>'
									       +     '<span class="preview"></span>'
									       + '</td>' 
									       + '<td style="max-width:520px;">'
									       +	 '<input type="hidden" id="uuid" value="' + EMF.util.generateUUID() + '">'
									       +     '<p><input class="ui-corner-all ui-textfield placeholder-fix" type="text" id="fileName" style="width:170px" placeholder="' + file.name + '"> '
									       +     '<span class="name" style="width:160px; display:inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; vertical-align: center;">' + file.name + '</span> ' 
									       +     '<select class="docType" id="type" chosen="documentType" data-placeholder="Select type"></select></p>'
									       +	 '<div><input class="ui-corner-all ui-textfield placeholder-fix" type="text" id="description" style="width:510px" placeholder="' + window._emfLabels["upload.document.description"] + '"></div>'
									       + '</td>'
									       + '<td>'
									       +     '<p class="size"></p>'
									       +    	'<div class="fileupload-progress" style="min-width:120px"><div class="progress progress-striped active" ><div class="progress-bar" style="width:0%; font-weight:bold;"></div></div></div>'
									       + '</td>'
									       + '<td style="min-width:200px;">'
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
					plugin.loadDocumentTypes($('.docType'), instanceType, instanceId, ""); 
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
		            
		    		var $uploadBtn = $upload.find('button.cancel');
		    		$uploadBtn.html(window._emfLabels["upload.file.upload"]);
		    		$uploadBtn.off('click');
		    		$uploadBtn.on('click',function () {
		    			data.uploadRow = $uploadBtn.attr('id');
		    			data.submit().success(function(response) {
		            	});
		             })
		        
		            // abort upload when cancel button is clicked
		            var $cancelButton = $upload.find('button.remove');
		    		$cancelButton.html(window._emfLabels["upload.file.remove"]);
		            $cancelButton.off('click');
		            $cancelButton.on('click', function() {
		                $upload.remove();
		               
		                // if all files are removed hide upload option and clear table
		                if($('#table-files tr').length == 1) {
		                	$('#startUpload').addClass('hide');
				            $('#cancelUpload').addClass('hide');
				            $('#table-header').addClass('hide');
				            $('#upload-panel-browser').addClass("fade");
				            $('.fileinput-button').removeAttr('disabled');
		                }
		                
		                $('#progress-all').addClass('progress-striped').addClass('active').find('.progress-bar').removeClass('progress-bar-success').removeClass('progress-bar-danger');
		                var rows = $("#table-files tr");
		                $.each(rows, function(index, row) {
				    		$(row).find('.progress').addClass('progress-striped').addClass('active').find('.progress-bar').removeClass('progress-bar-success').removeClass('progress-bar-danger');
				    	});
		            });
		            
		            // Upload plugin doesn't work good for some browsers. Like old versions of IE and Safari
		            // That's why the following checks are needed
		            var msieVersion;
		            var safariVersion;
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
		            
					});
					
		            var $uploadAll = $('#startUpload');
		            $uploadAll.off('click');
		            $uploadAll.on('click',function () {
		            	data.uploadRow = null;
		            	data.submit().success(function(response) {
		            	});
		             })

	                var $cancelButton = $('#cancelUpload');
	                $("#cancelUpload").on('click',function () {
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
			 	var concFiles = [];
  
			 	// Concat all files from table and prepare the fileQueue
			    $.concat || $.extend({
			    	concat: function(b,c) {
			    		var a=[];
			    		for(x in arguments) {
			    			a=a.concat(arguments[x]);
			    		}
			    		return a;
			    	}
			    });

			    if(orgFiles.length != 0) {
			    	 concFiles =  orgFiles;
			    } else {
			    	 concFiles = data.files;
			    }
			    
			    if(concFilesCopy.length == 0) {
			    	concFilesCopy = concFiles.slice();
			    }
			    
			    $.each(fileQueue, function(index, file) {
			    	if(index < fileQueue.length - 1) {
			    		concFiles = $.concat(concFiles, file.files);
			    	}
			    });

			    var dataToStore = [];
			    // check if it's a single upload
			    if(data.uploadRow) {
			        var row = $('#row_' + data.uploadRow);
			        var cell = row.children();
			    	$.each(cell.find('*'), function(index, child) {
			    		dataToStore.push(child);
			    	});
			    	data.files = [concFiles[data.uploadRow]];
			    	var index = concFilesCopy.indexOf(concFiles[data.uploadRow]);
			    	concFilesCopy.splice(index, 1);
				    orgFiles = concFiles;
			    } else {
			    	data.files = concFilesCopy;
			    	dataToStore = data.form[0];
			    }
				
			    // Gather all needed data
			    $.each(dataToStore, function(index, field) {
			    	if($(field).get(0).id == 'type') {
			    		type.push($(field).val());
			    		itemsCount++; 
			    	}
			    		
			    	if($(field).get(0).id == 'fileName') {
			    		fileName.push($(field).val() || " ");
			    	}

			    	if($(field).get(0).id == 'description') {
			    		description.push($(field).val() || " ");
			    	}
			    	
			    	if($(field).get(0).id == 'uuid') {
			    		uuid.push($(field).val());
			    	}
			    	
			    });

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
	                    uuid		: JSON.stringify(uuid)
	            };
			  
			    $('#progress-all-wrapper').removeClass("hide");
			},

			// show progress of upload
		    progress: function(e, data) {
		    	var progress = parseInt(data.loaded / data.total * 100, 10);
		    	
		    	if(data.uploadRow) {
			    	var row = $('#row_' + data.uploadRow);
			    	$(row).find('.progress').find('.progress-bar')
	                .css('width', progress + '%')
	                .text(progress+'%');
			    	$('#startUpload').attr("disabled", true);
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
		    	
		    	if(data.uploadRow) {
			    	var row = $('#row_' + data.uploadRow);
		    		$(row).find('.progress')
	                .removeClass('active')
	                .removeClass('progress-striped')
	                .addClass('progress')
	                .find('.progress-bar')
	                .addClass('progress-bar-success')
	                .css('width', '100%')
	                .text(window._emfLabels["upload.upload.complete"]);
			    	data.files = orgFiles;
			    	$('#startUpload').attr("disabled", false);
			    	$(row).find('button.cancel').attr("disabled", true);
			    	$(row).find('button.remove').attr("disabled", true);
			    } else {
			    	var rows = $("#table-files tr");
			    	$.each(rows, function(index, row) {
			    		$(row).find('.progress')
			                .removeClass('active')
			                .removeClass('progress-striped')
			                .addClass('progress')
			                .find('.progress-bar')
			                .addClass('progress-bar-success')
			                .css('width', '100%')
			                .text(window._emfLabels["upload.upload.complete"]);
			    		$(row).find('button.remove').attr("disabled", true);
			    		$(row).find('button.cancel').attr("disabled", true);
			    	});
			        $('#startUpload').off('click');
			        $('#cancelUpload').off("click");
			        $('#cancelUpload').attr("disabled", true);
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
    					plugin.settings.browserConfig.rootNodeText = _emfLabels['cmf.root.node.text']; //Case
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
    		$("#addFiles").html(window._emfLabels["upload.add.files"]);
    		$("#documentTitle").html(window._emfLabels["upload.document.title"]);
    		$("#fileName").html(window._emfLabels["upload.file.name"]);
    		$("#documentType").html(window._emfLabels["upload.document.type"]);
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
		}
		
		// Add checkbox or radiobuton in front of each uploaded file
		plugin.responseDecorator = function(data, row) {
			var selectedItems = [];
			$.each(data, function(index, item) {
				
				// first row is header we don't need checkbox here
				if(index == 0) {
					var rows = $("#table-files tr").eq(index);
					$firstCell = $('th:first-child', rows);
					var th = $('<th></th>');
		    		th.insertBefore($firstCell); 
				}

				if(row) {
			    	var rows = $("#table-files tr");
			    	$.each(rows, function(index, tblRow) {
			    		if(index !=0 && $(tblRow).attr('id') != row.attr('id')) {
			    			$firstCell = $('td:first-child', tblRow);
			    			var cell = $('<td></td>');
			    			cell.insertBefore($firstCell); 
			    		}
			    	});
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
	    		tdCheckbox.insertBefore($firstCell); 
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

    		return this;
    	});
    }

}(jQuery));