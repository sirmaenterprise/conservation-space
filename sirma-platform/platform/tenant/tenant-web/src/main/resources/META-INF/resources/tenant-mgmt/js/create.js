$(document).ready(function () {

    /**
     * Retrieve the tenant models and populate them in the base models drop-down.
     */
    $.ajax({
        url: '../service/tenant/models',
        type: 'GET',
        success: function (data) {
            $("#base-models-select").select2({theme: "bootstrap", 'data': data});
        },
        error: function () {
            $("#base-models-select").select2({theme: "bootstrap", 'data': []});
        }
    });

    $("#logout").click(function() {
    	window.location.replace("../ServiceLogout");
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

        return _.flatten(labelIds);
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
                        group = form.find("#" + value.id);
                        if (group.length == 0) {
                            group = $("<div class='group well' id='" + value.id + "'>");
                            form.append(group);
                        }
                    } else {
                        group = additionalForm.find("#" + value.id);
                        if (group.length == 0) {
                            group = $("<div class='group well' id='" + value.id + "'>");
                            additionalForm.append(group);
                        }
                    }
                    var row = $("<div class='row top-buffer'>");
                    group.append(row);
                    var label = $("<div class='col-md-6'><label for='" + field.id + "'>" + labels[field.label] + "</label></div>");
                    row.append(label);

                    var input = $("<input>");
                    input.attr(
                        {
                            'type': field.type,
                            'id': field.id,
                            'value': field.value,
                            'pattern': field.validator
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
            }
        );

// Append the forms to the actual dom.
        $("#default-properties").append(form);
        $("#additional-properties .panel-body").append(additionalForm);
    }

    $("#data").submit(function () {
        // grab all form data
        var formData = new FormData();
        var definitions = $('input[name=DMSInitialization_attachment_definitions]')[0].files[0];
        if(definitions !== undefined){
        	formData.append('DMSInitialization_attachment_definitions', definitions);
        }
        
        var semanticPatches = $('input[name=SemanticDbInitialization_attachment_patches]')[0].files[0];
        if(semanticPatches !== undefined){
        	formData.append('SemanticDbInitialization_attachment_patches', semanticPatches);
        }
        if ($('#base-models-select').val() !== null) {
            formData.append('DMSInitialization_attachment_path', $('#base-models-select').val());
        }

        var tenantModel = {};
        tenantModel.data = getFormData();
        formData.append('tenantmodel', JSON.stringify(tenantModel));
        var result = $.ajax({
            type: "POST",
            url: "../service/tenant",
            data: formData,
            async: false,
            processData: false,
            contentType: false,
            success: function (response) {
                alert("Successful tenant creation submit!");
            },
            error: function (data) {
                alert(data.responseText);
            }
        });
    });

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
            groups.push({'id': group.id, 'properties': properties});
        });

        _.forEach(additionalPropertiesGroups, function (group) {
            var properties = getGroupProperties(group);
            var foundGroup = _.find(groups, {'id': group.id});
            if (foundGroup !== undefined) {
                foundGroup.properties = foundGroup.properties.concat(properties);
            } else {
                groups.push({'id': group.id, 'properties': properties});
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
                properties.push({'id': property.id, 'value': property.value});
            }
        });
        return properties;
    }
})
;