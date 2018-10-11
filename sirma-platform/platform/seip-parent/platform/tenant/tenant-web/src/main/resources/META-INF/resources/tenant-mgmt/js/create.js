$(document).ready(function () {

    /**
     * Retrieve the tenant models and populate them in the base models drop-down.
     */
    $.ajax({
        url: '../service/tenant/models',
        type: 'GET',
        success: function (data) {
            $("#base-models-select").select2({
                theme: "bootstrap",
                'data': data
            });
        },
        error: function () {
            $("#base-models-select").select2({
                theme: "bootstrap",
                'data': []
            });
        }
    });


    $("#base-models").click(function () {
        $("#base-models-form").show();
        $("#base-models").addClass('active');

        $("#custom-models-form").hide();
        $("#custom-models").removeClass('active');
    });

    $("#custom-models").click(function () {
        $("#base-models-form").hide();
        $("#base-models").removeClass('active');

        $("#custom-models-form").show();
        $("#custom-models").addClass('active');
    });

    $("#resetbutton").click(function () {
        $("#base-models-select").val(null).trigger("change");
    });

    // When the dms bundle is changed, check if it's a zip file otherwise clear
    // the form (We can't have something other than a zip file here).
    $("#dmsBundle").bind("change", function(e){
    	var file = (e.srcElement || e.target).files[0];
    	var extension = file.name.substring(file.name.lastIndexOf(".")+1);
	    if (extension !== "zip"){
	    	displayStatus("Only .zip files are allowed for the definitions bundle!");
	    	$("#dmsBundle").wrap("<form>").closest("form").get(0).reset();
	    	$("#dmsBundle").unwrap();
	    }
    });
    
    /**
     * Load the tenant data json, extract all label ids from the json and build the tenant creation
     * form.
     */
    $.get("../service/tenant", function (data) {
        var groups = data.data;
        var labelIds = getLabelIds(groups);
        $.ajax({
            url: "../service/label/bundle/multi",
            type: "POST",
            data: JSON.stringify(labelIds),
            contentType: "application/json; charset=utf-8",
            success: function (labels) {
                buildFieldsForm(groups, labels);
            }
        });
    });

    /**
     * Retrieve all label ids from the provided groups in a flattened format.
     *
     * @param groups
     *        the groups that contain the labels
     * @returns the label ids in a flattened format
     */
    function getLabelIds(groups) {
        var labelIds = _.map(groups, function (nested) {
            return _.map(nested.properties, 'label');
        });
        
        var groupLabelIds = _.map(groups, function(group){
        	return group.label;
        });
        
        return _.flatten(_.without(labelIds.concat(groupLabelIds), undefined));
    }

    /**
     * Build the tenant creation form. Two separate groups will be formed - one for the default
     * fields and one for the additional fields. Note that additional fields can't be required. Both
     * groups have subgroups that are formed based on the groups in the tenant init json.
     *
     * @param groups
     *        the groups
     * @param labels
     *        loaded labels
     */
    function buildFieldsForm(groups, labels) {
        var form = $("<div/>");
        var additionalForm = $("<div/>");

        _.forEach(groups, function (value) {
            _.forEach(value.properties, function (field) {
                var group;
                // If the field is a default field, add it to the default form, otherwise add it
                // to the additional fields form.
                if (field['default']) {
                    group = findAndAppendGroup(form, value.id, labels[value.label]);
                } else {
                    group = findAndAppendGroup(additionalForm, value.id, labels[value.label]);
                }

                var row = $("<div class='row top-buffer'>");
                group.append(row);
                var label = $("<div class='col-md-6'><label for='" + field.id + "'>" + labels[field.label] + "</label></div>");
                row.append(label);

                var input = $("<input>");
                input.attr({
                    'type': field.type,
                    'id': field.id,
                    'value': field.value,
                    'pattern': field.validator,
                    'data-toggle': 'tooltip',
                    'title': field.tooltip
                });
                input.addClass('form-control');
                if (field.required === "true" && field['default']) {
                    input.attr('required', true);
                    label.addClass('required');
                }

                var col = $("<div class='col-md-6'>");
                col.append(input);
                row.append(col);
            });
        });

        // Append the forms to the actual dom.
        $("#default-properties").append(form);
        $("#additional-properties .panel-body").append(additionalForm);
    }

    $("#data").submit(function () {
    	var createTenantButton = $("#createTenantButton");
    	createTenantButton.prop("value","Processing...");
    	createTenantButton.prop("disabled", true);

        // grab all form data
        var formData = new FormData();
        var definitions = $('input[name=DMSInitialization_attachment_definitions]')[0].files[0];
        if (definitions !== undefined) {
            formData.append('DMSInitialization_attachment_definitions', definitions);
        }

        var semanticPatches = $('input[name=SemanticDbInitialization_attachment_patches]')[0].files[0];
        if (semanticPatches !== undefined) {
            formData.append('SemanticDbInitialization_attachment_patches', semanticPatches);
        }
        if ($('#base-models-select').val() !== null) {
            formData.append('DMSInitialization_attachment_path', $('#base-models-select').val());
        }

        var tenantModel = {};
        tenantModel.data = getFormData();
        formData.append('tenantmodel', JSON.stringify(tenantModel));
        $.ajax({
            type: "POST",
            url: "../service/tenant",
            data: formData,
            async: false,
            processData: false,
            contentType: false,
            statusCode: {
            	 200: function (data) {
            		queryStatus($("input#tenantId").val());
                 },
                 500: function (data) {
                	onTenantCreationFinished(data.responseText);
                 }
            }
        });
    });

    /**
     * Find the properties group in the given form or create a new one and append it to the given
     * form.
     *
     * @param form
     *        the form to find the group in
     * @param id
     *        the id of the group
     * @param label
     *        the label of the group
     * @return the group
     */
    function findAndAppendGroup(form, id, label) {
    	label = label || id;
        var group = form.find("#" + id);
        if (group.length == 0) {
            group = $("<fieldset class='group well' id='" + id + "'>");
            var title = $("<legend class='group-legend'>" + label + "</legend>");
            group.append(title);
            form.append(group);
        }
        return group;
    }

    /**
     * Query the tenant creation status
     *
     * @param tenantId
     *        the tenant id
     */
    function queryStatus(tenantId) {
        $.ajax({
            type: 'GET',
            url: '../service/tenant/status/' + tenantId,
            statusCode: {
                202: function (data) {
                    displayStatus(data.responseText);
                    setTimeout(function () {
                        queryStatus(tenantId)
                    }, 1000);
                },
                200: function (data) {
                	onTenantCreationFinished(data.responseText);
                },
                500: function (data) {
                	onTenantCreationFinished(data.responseText);
                }
            }
        });
    }

    /**
     * Logic that will be performed when the tenant creation process has finished either
     * due to an error or successful completion. The Create tenant button will be unlocked
     * and the status of the operation will be displayed.
     */
    function onTenantCreationFinished(status){
    	displayStatus(status);
    	var createTenantButton = $("#createTenantButton");
    	createTenantButton.prop("value","Create Tenant");
    	createTenantButton.prop("disabled", false);
    }
    /**
     * Display the tenant creation status in a modal dialog
     *
     * @param status
     *        the tenant creation status
     */
    function displayStatus(status) {
        $("#tenantInitModal").modal("show");
        $("#tenantInitStatus").text(status);
    }

    /**
     * Retrieve both the default and additional form data.
     *
     * @returns the form data in the old format needed for creating a tenant.
     */
    function getFormData() {
        var defaultPropertiesGroups = $("#default-properties .group");
        var additionalPropertiesGroups = $("#additional-properties .group");
        var groups = [];
        _.forEach(defaultPropertiesGroups, function (group) {
            var properties = getGroupProperties(group);
            groups.push({
                'id': group.id,
                'properties': properties
            });
        });

        _.forEach(additionalPropertiesGroups, function (group) {
            var properties = getGroupProperties(group);
            var foundGroup = _.find(groups, {
                'id': group.id
            });
            if (foundGroup !== undefined) {
                foundGroup.properties = foundGroup.properties.concat(properties);
            } else {
                groups.push({
                    'id': group.id,
                    'properties': properties
                });
            }
        });
        return groups;
    }

    /**
     * Retrieve all properties for the given group.
     *
     * @param group
     *        the group
     * @returns the group properties
     */
    function getGroupProperties(group) {
        var properties = [];
        _.forEach($(group).find(".form-control"), function (property) {
            if (property.value !== "") {
                properties.push({
                    'id': property.id,
                    'value': property.value
                });
            }
        });
        return properties;
    }
});
