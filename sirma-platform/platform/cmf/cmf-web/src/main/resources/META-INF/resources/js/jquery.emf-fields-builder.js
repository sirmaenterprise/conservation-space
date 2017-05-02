(function($) {
'use strict';
/**
	 * Plugin instance constructor.
	 * @param options
	 *        plugin configuration options
	 * @param element
	 *        DOM element where the fields will be build
	 */
	function EmfFieldsBuilder(options, element) {
		this.defaults = $.extend(true, {
			definitionId 		: null,
			currentInstanceId   : "",
			currentInstanceType : "",
			parentInstanceId	: "",
			parentInstanceType	: "",
			row 				: "",
			styles 				: "",
			renderMandatory 	: true,
			preview 			: false,
			wrapper 			: true,
			skipBtn 	    	: null,
			// restrict definition fields by attributes
			restrictByAttibutes : {
				displayType : [],
				identifiers : []
			},
			// date picker settings
			datePickerSettings : {},
			// keep information for already filled fields. This is needed
			// when the plugin is used for document creation
			predefinedFields : [],
			fieldsAreValid   : true,
			applicationPath  : null,
			services 	     : {
				definitionService : null
			},
			instance : null
		}, options);

		this.settings = $.extend(true, {}, this.defaults, options);
		this.templates = {};
		var applicationPath = this.settings.applicationPath;
		this.templatePaths = {
			text : applicationPath + '/templates/fields/textfield.tpl.html',
			datetime : applicationPath + '/templates/fields/date.tpl.html',
			dropdownmenu : applicationPath + '/templates/fields/codelistfield.tpl.html',
			previewText : applicationPath + '/templates/fields/previewText.tpl.html',
			region : applicationPath + '/templates/fields/region.tpl.html'
		};
		this._init(element);
	};

	EmfFieldsBuilder.prototype = {
		/**
		 * Method that will send request for retrieving all fields to {@link DefinitionRestService}.
		 * If the instance is not created the functionality will try to find the definition model
		 * based on definition identifier and object type else will try to load it by instance identifier
		 * and object type.
		 * @param objectType instance type
		 * @param definitionId definition identifier
		 * @param instanceId instance identifier
		 */
		_loadFields : function(definitionId, currentInstanceId, currentInstanceType, parentInstanceId, parentInstanceType){
			return this.settings.services.definitionService.fields({
				definitionId : definitionId,
				queryParams  : {
					currentInstanceId   : currentInstanceId,
					currentInstanceType : currentInstanceType,
					parentInstanceId	: parentInstanceId,
					parentInstanceType	: parentInstanceType
				}
			});
		},
		/**
		 * This method merge predefined fields parameters to already stored one. This method
		 * can be extend to merge additional properties.
		 * @param fields available definition fields
		 */
		_mergeFieldsProperties : function(fields) {
			var plugin = this;
			for(var i = 0; i < fields.length; i++){
				var field = fields[i];
				if(plugin.settings.predefinedFields[field.name]) {
					field.title = plugin.settings.predefinedFields[field.name];
				}
			}
			return fields;
		},
		/**
		 * Method that will build the fields based on specific fields data. This is an entry point
		 * and here the fields will be restricted and validated before be constructed by the supported builders.
		 *  @param fields current fields
		 *  @param predefinedWrapper predefined wrapper
		 */
		_createFields : function(fields, predefinedWrapper) {
			var plugin = this;
			// merge predefined fields parameters
			fields = plugin._mergeFieldsProperties(fields);
			var mainWrapper = $(plugin.settings.wrapper);
			for(var i = 0; i < fields.length; i++){
				var field = fields[i];
				// check field is supported
				if (!plugin._isFieldSupported(field)) {
					continue;
				}
				var subFields = field.fields;
				// check if the field is region and enter one level down
				// to extract the fields, will be used for creating flat
				// structure of fields, when only the mandatory fields are required
				if(subFields && subFields.length !== 0 && plugin.settings.renderMandatory){
					plugin._createFields(subFields);
					continue;
				}
				// find the builder key based on the field
				var fieldType = plugin._getFieldType(field);
				if(!fieldType){
					console.error("Undefined field type !");
					return;
				}
				field.wrapper_id = plugin.wrapper_id;
				field.style = plugin.settings.styles;
				var fieldWrapper = predefinedWrapper;
				if(!fieldWrapper){
					fieldWrapper = $('<div></div>');
					mainWrapper.append(fieldWrapper);
				}
				plugin._getFieldTemplate(fieldType, plugin.templatePaths[fieldType], {
					data : field,
					preview : plugin.settings.preview,
					scripts : plugin._commonFunctions
				}, fieldWrapper);
			}
			// event that is triggered after the fields are successfully loaded 
			mainWrapper.trigger({
				type: "after-fields-loaded"
			});
		},
		/**
		 * Fields common functions.
		 */
		_commonFunctions : {
			/**
			 * Style class that manage field visibility based on required field data.
			 */
			appendVisibility : function(data){
				if(!data.previewEmpty && $.trim(data.title)===''){
					return 'hidden-field';
				}
				return '';
			}
		},
		/**
		 * Method that will manage validation for supported fields. The method uses
		 * <b>decompose conditional</b> logic.
		 * @param field current field to be validated
		 */
		_isFieldSupported : function(field){
			var plugin = this;
			/**
			 * Validate the filed based on the name.
			 * @param field current field
			 */
			var restrictByNames = function(field){
				var names = plugin.settings.restrictByAttibutes.names || [];
				for(var i = 0; i < names.length; i++){
					var id = names[i];
					if(field.name === id){
						return false;
					}
				}
				return true;
			};
			/**
			 * Validate the filed based on display types.
			 * @param field current field
			 */
			var restrictByDisplayType = function(field){
				var fieldType = field.displayType;
				var restrictedTypes  = plugin.settings.restrictByAttibutes.displayType || [];
				// if the display type of the field is not contains
				// in settings.displayType holder then skip the field
				if(!fieldType || $.inArray(fieldType.toLowerCase(), restrictedTypes) === -1){
					return false;
				}
				return true;
			};
			/**
			 * Validate mandatory fields.
			 * @param field current field
			 */
			var restrictByMandatory = function(field){
				if (plugin.settings.renderMandatory && !field.fields) {
					return field.isMandatory;
				}
				return true;
			};
			if(restrictByNames(field) && restrictByDisplayType(field) && restrictByMandatory(field)){
				return true;
			}
			return false;
		},
		/**
		 * Method that will retrieve field type. This type represent supported builder that will
		 * build the different fields.
		 * @param field current field
		 */
		_getFieldType : function(field){
			var selectedType = undefined;
			// supported types/builder keys
			var type = {
				dropdownmenu : 'dropdownmenu',
				text		 : 'text',
				datetime	 : 'datetime',
				region		 : 'region'
			};
			if(field.fields){
				selectedType = type.region;
			}else if (field.codelist) {
				selectedType = type.dropdownmenu;
			}else if ((field.dataType.name === 'date' || field.dataType.name === type.datetime)) {
				selectedType = type.datetime;
			}else {
				selectedType = type.text;
			}
			return selectedType;
		},
		/**
		 * Load template for given field types. Every template is loaded once and stored in template
		 * cache.
		 */
		_getFieldTemplate : function(type, tmplUrl, tmplData, wrapper) {
			var _this = this, IS_LOADING = 'isloading';

			if (_this.templates[type]) {
				var interval = setInterval(function() {
					var isLoading = _this.templates[type] === IS_LOADING;
					if (!isLoading) {
						_this.builders[type](_this.templates[type], tmplData, wrapper, _this);
						clearInterval(interval);
					}
				}, 100);
			} else {
				_this.templates[type] = IS_LOADING;
				$.ajax({
					url : tmplUrl,
					dataType : 'html',
					method : 'GET',
					success : function(response) {
						_this.templates[type] = response;
						_this.builders[type](response, tmplData, wrapper, _this);
					},
					error : function() {
						throw "Field template can not be loaded.";
					}
				});
			}
		},
		/**
		 * Get values from instance properties based on specific attribute.
		 * @param fieldName field name
		 * @param attribute instance attribute
		 */
		_getAttributeValue : function(fieldName, attribute){
			var plugin = this;
			if(plugin.settings.instance || plugin.settings.predefinedFields[fieldName] || plugin.settings.instance[fieldName]) {
			/**
			 * Extract values from attributes represented as array.
			 */
				var extractValueFromAttributes = function(objects, data){
					var values = [];
					for(var i = 0; i < objects.length; i++){
						values[i] = objects[i][data];
					}
					return values.join();
				};
				// get object by field name
				var object = plugin.settings.instance.cldescription[fieldName] || plugin.settings.predefinedFields[fieldName] || plugin.settings.instance[fieldName];
				// the object is represented as string data
				if (!object) {
					return;
				}
				if(object.constructor === String){
					return object;
					// the object is represented as literal data
				}else if(object.constructor === Object){
					return object[attribute];
					// the object is represented as sequence of data
				}else if(object.constructor === Array){
					return extractValueFromAttributes(object, attribute);
				}
			}
		},
		/**
		 * Supported field builders.
		 */
		builders : {
			/**
			 * Builder for text fields.
			 * @param template current field template
			 * @param field current field data
			 * @param palceholder store location
			 * @param context plugin context
			 */
			text : function(template, field, placeholder, context) {
				field.data.title = context._getAttributeValue(field.data.name, 'title');
				placeholder.append(_.template(template)(field));
				if(field.preview){
					return;
				}
				var element = $('#' + field.data.wrapper_id + "_" + field.data.name);
				element.on('keyup', function() {
					var fieldElement = $(this);
					if (fieldElement.val() === "") {
						fieldElement.closest('div').addClass("has-error");
					} else {
						fieldElement.closest('div').removeClass("has-error");
					}
				});
			},
			/**
			 * Builder for regions. The regions represent group with field.
			 * @param template current field template
			 * @param field current field data
			 * @param palceholder store location
			 * @param context plugin context
			 */
			region : function(template, field, placeholder, context) {
				var region = $(_.template(template)(field));
				context._createFields(field.data.fields, region.find(".region-body"));
				placeholder.append(region);
			},
			/**
			 * Builder for date/datetime.
			 * @param template current field template
			 * @param field current field data
			 * @param palceholder store location
			 * @param context plugin context
			 */
			datetime : function(template, field, placeholder, context) {
				var title = context._getAttributeValue(field.data.name, 'title');
				if($.trim(title)){
					field.data.title = EMF.date.format(new Date(title), context.settings.datePickerSettings.dateFormatPattern, false);
				}
				if(field.preview){
					placeholder.append(_.template(template)(field));
					return;
				}
				placeholder.append(_.template(template)(field));
				var element = $('#' + field.data.wrapper_id + "_" + field.data.name);
				// apply date-picker utility for this element
				element = element.datepicker(context.settings.datePickerSettings);
				element.datepicker('setDate', new Date(field.data.defaultValue));
				element.on('keydown change', function(event) {
					event.preventDefault();
					$(this).closest('div').removeClass("has-error");
				});
			},
			/**
			 * Builder for drop-down menu. The drop-down represent utility for selecting
			 * one element(this element received from code-list).
			 * @param template current field template
			 * @param field current field data
			 * @param palceholder store location
			 * @param context plugin context
			 */
			dropdownmenu : function(template, field, placeholder, context) {
				if(field.preview){
					field.data.title = context._getAttributeValue(field.data.name, 'title');
					placeholder.append(_.template(template)(field));
					return;
				}
				var idPrefix = context.settings.idPrefix;
				var id = field.data.name;
				/**
				 * Method that will prepare the related fields if any.
				 * @param field current field that holds related field information
				 * @param context plugin context
				 * @param id field identifier
				 */
				var getRelatedFieldData = function(field, context, id){
					var identifier = 'RELATED_FIELDS';
					var data = {
						fieldList    : [],
						inclusive    : null,
						filterSource : null
					};
					// check for field related data
					if(field.data.controlDefinition &&
							field.data.controlDefinition.identifier===identifier &&
							field.data.controlDefinition.controlParams.length === 3){
						var controlParams = field.data.controlDefinition.controlParams;
						var relatedFields = controlParams[0].value;
						var fieldList = $.trim(relatedFields).split(',');
						if (context.settings.idPrefix) {
							id = idPrefix + id;
							for (var i = 0; i < fieldList.length; i++) {
								var current = fieldList[i];
								fieldList[i] = idPrefix + current;
							}
						}
						data.fieldList = fieldList;
						data.filterSource = controlParams[1].value || null;
						data.inclusive = controlParams[2].value || null;
					}
					return data;
				};
				var proceedButtonLocators = JSON.stringify(context.settings.proceedButtonLocators);
				var relatedFields = getRelatedFieldData(field, context, id);
				var fieldsList = JSON.stringify(relatedFields.fieldList);
				// construct JSON container with required data
				// for event management
				var codelistFieldConfigData = '{';
					codelistFieldConfigData += 'field: document.querySelectorAll("[id=' + field.data.wrapper_id +'_'+ id + ']")[0],';
					codelistFieldConfigData += 'codelist: ' + field.data.codelist + ',';
					codelistFieldConfigData += 'rerender: ' + fieldsList + ',';
					codelistFieldConfigData += 'inclusive: ' + relatedFields.inclusive + ',';
					codelistFieldConfigData += 'filterSource: "' + relatedFields.filterSource + '",';
					codelistFieldConfigData += 'label: "' + field.data.label + '",';
					codelistFieldConfigData += 'proceedButtonLocators: ' + proceedButtonLocators + ',';
					codelistFieldConfigData += 'menuConfig: { width: "100%", adaptContainerCssClass: function(cssClass) { return "select2container-" + cssClass } }';
					codelistFieldConfigData += '}';

				field.data.codelistFieldConfigData = codelistFieldConfigData;
				placeholder.append(_.template(template)(field));
				placeholder.off('after-default-change-handler').on('after-default-change-handler', function() {
					var currentField = placeholder.find('.mandatory-field');
					if (currentField.length > 0 && currentField.eq(0).val() !== '') {
						placeholder.find('.has-error').removeClass("has-error");
					}
				});
			}
		},

		/**
		 * Validate mandatory field. If the the data is not present then
		 * build an error for the client.
		 */
		_validate : function() {
			var _this = this;
			var fieldStore = [];
			var fields = this.settings.wrapper.find('.mandatory-field');
			var addfieldToStore = function(id, value) {
				fieldStore.push({
					'id' : id,
					'value' : value
				});
			};
			// get field value, if the field is date/time picker
			// extract the value based on plugin attribute
			var getValue  = function(field){
				if (field.hasClass('hasDatepicker')) {
					return field.datepicker('getDate');
				}
				return field.val();
			};
			var notValidFields = [];
			for(var i = 0; i < fields.length; i++){
				var field = $(fields[i]);
				var value = getValue(field);
				// check for required field data
				if($.trim(value)){
					addfieldToStore(field.attr("name"), value);
				}else{
					field.closest('div').addClass("has-error");
					notValidFields.push(field);
				}
			}
			// check for not valid fields
			if(notValidFields.length === 0){
				setTimeout(function() {
					_this.settings.wrapper.trigger({
						type : 'data-is-valid',
						fields : fieldStore
					});
				});
				return;
			}
			// trigger not valid field event with first field
			// in the array
			this.settings.wrapper.trigger({
				type : 'data-is-invalid',
				field : notValidFields[0]
			});
		},
		_init : function(placeholder) {
			var plugin = this;
			this.wrapper_id = placeholder.attr("id");
			this._loadFields(plugin.settings.definitionId, plugin.settings.currentInstanceId, plugin.settings.currentInstanceType,
					plugin.settings.parentInstanceId, plugin.settings.parentInstanceType).then(function(result) {
				plugin.settings.instance = result.properties;
				var data = result.fields;
				if (plugin.settings.renderMandatory) {
					// Create mandatory fields wrapper, and append it to the placeholder
					$('<div class="' + plugin.settings.row + ' fields-wrapper" ></div>').appendTo(placeholder);
					plugin.settings.wrapper = placeholder.find('div');
					placeholder.off('validate-mandatory-fields');
					placeholder.on('validate-mandatory-fields', function() {
						plugin._validate();
					});
					if (data.length === 2 && plugin.settings.skipBtn) {
						plugin.settings.skipBtn.hide();
					} else if (data.length > 2 && plugin.settings.skipBtn) {
						plugin.settings.skipBtn.show();
					}
				}else{
					plugin.settings.wrapper = placeholder;
				}
				plugin._createFields(data);
			});
		}
	};

	$.fn.buildFields = function(options) {
		var pluginName = 'EmfFieldsBuilder';
		var pluginInstance = $.data(this, pluginName);
		if (!pluginInstance) {
			this.data(pluginName, new EmfFieldsBuilder(options, this));
			return this;
		} else {
			return;
		}
	};

}(jQuery));