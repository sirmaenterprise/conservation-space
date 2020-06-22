function ManageController() {

    /**
     * Query the tenant status.
     *
     * @param tenantId
     *        the tenant id
     * @param successCallback
     *        callback function called when the process has been finished
     * @param failCallback
     *        callback function called when the process fails
     */
    function queryStatus(tenantId, successCallback, failCallback) {
        $.ajax({
            type: 'GET',
            url: '../service/tenant/status/' + tenantId,
            statusCode: {
                202: function () {
                    setTimeout(function () {
                        queryStatus(tenantId, successCallback, failCallback);
                    }, 1000);
                },
                200: function () {
                    successCallback(tenantId);
                },
                500: function (data) {
                    showError(tenantId + " deletion failed due to " + data.responseText);
                    failCallback(tenantId);
                }
            }
        });
    }

    /**
     * Update the tenant status.
     *
     * @param row
     *        the row for which the status should be updated
     * @param status
     *        the tenant creation status
     */
    function changeStatus(row, status) {
        $(row).find("#tenantStatus").text(status);
    }

    /**
     * Lock the tenant row, disabling and unchecking all inputs in the row.
     *
     * @param row
     *        row, which should be disabled
     */
    function lockTenant(row) {
        $(row).find(":input").prop("disabled", true);
        $(row).find(":input").prop("checked", false);
    }

    /**
     * Delete the tenants with the given tenant ids. After sending the delete request for each tenant, start polling it's status until the process is finished.
     *
     * @param tenantIds
     *        the tenant ids
     */
    function deleteTenants(tenantIds) {
        _.forEach(tenantIds, function (tenantId) {
            $.ajax({
                type: "DELETE",
                url: "../service/tenant/" + tenantId,
                async: true,
                processData: false,
                contentType: false,
                beforeSend : function() {
                    addProcessIndication(tenantId);
                },
                statusCode: {
                    200: function (data) {
		           		queryStatus(tenantId, markTenantRowAsDeleted, removeProcessIndication);
                    },
                    500: function (data) {
                        showError(data.responseText);
                        removeProcessIndication(tenantId);
                    }
               }
            });
        });
    }
    
    function addProcessIndication(tenantId) {
        $("#process-" + escapeTenantId(tenantId)).append("<div class='loader' />");
        $("#delete").addClass('disabled').attr('title',  'Disabled until tenant ' + tenantId + ' is processed.');
    }
    
    /**
     * Removes the progress indicator from the row and enables tenant delete button.
     */
    function removeProcessIndication(tenantId) {
        $("#process-" + escapeTenantId(tenantId)).empty();
        $("#delete").removeClass('disabled').removeAttr('title');
    }

    function showError(message) {
	    $("#errorMessage").html(message);
	    $("#errorDialog").modal("show");
    }

    /**
     * Mark the tenant row as deleted. Change it's color, lock it and change the status to deleted.
     *
     * @param tenantId
     *        the tenant id
     */
    function markTenantRowAsDeleted(tenantId) {
        removeProcessIndication(tenantId);
        var row = $("#" + escapeTenantId(tenantId));
        row.addClass("danger");
        lockTenant(row);
        changeStatus(row, "DELETED");
    }

    /**
     * Get the selected tenant ids.
     */
    function getSelectedTenantIds() {
        var tenantIds = [];
        var selectedRows = $("#tenant-table-body").find('tr input[type="radio"]:checked').closest("tr");
        _.forEach(selectedRows, function (row) {
            tenantIds.push($(row).find("#tenantId").text());
        });
        return tenantIds;
    }

    /**
     * Escape the tenant id by replacing all '.' with '-'.
     */
    function escapeTenantId(tenantId) {
        return tenantId.replace(/\./g, '-')
    }

    /**
     * Retrieve the tenant models and populate them in the base models
     * drop-down.
     */
    $.ajax({
        url: '../service/tenant/list',
        type: 'GET',
        success: function (data) {
            var tenantTable = $("#tenant-table-body");
            _.forEach(data, function (tenant) {
                var escapedTenantId = escapeTenantId(tenant.tenantId);
                var row = $("<tr data-toggle='tooltip' id='" + escapedTenantId + "'><td><input type='radio' name='tenants' value=''></td>");
                row.append("<td id='tenantId'>" + tenant.tenantId + "</td>");
                row.append("<td>" + tenant.displayName + "</td>");
                if (tenant.description) {
                    row.append("<td>" + tenant.description + "</td>");
                } else {
                    row.append("<td></td>");
                }
                if (tenant.status === "DELETED") {
                    row.addClass('danger');
                    row.attr('title', "This tenant has been deleted and it's tenant id will be reusable on next server restart.");
                    lockTenant(row);
                } else if (tenant.status === "ACTIVE") {
                    row.attr('title', "This tenant is active and usable.");
                    row.addClass('success');
                } else if (tenant.status === "INACTIVE") {
                    row.attr('title', "This tenant is deactivated.");
                    row.addClass('warning');
                }
                row.append("<td id='tenantStatus'>" + tenant.status + "</td>");
                row.append("<td id='process-" + escapedTenantId + "'></td>");
                tenantTable.append(row);

            });

        }
    });

    $("#delete").click(function () {
        var tenantIds = getSelectedTenantIds();
        if (tenantIds.length > 0) {
            $("#confirmationMessage").text("Do you want to proceed with deleting tenants " + tenantIds.join(", ") + "?");
            $("#modalDialog").modal("show");
            //clear out the previous event handlers.
            $("#confirm").off("click");
            $("#confirm").click(function () {
                deleteTenants(tenantIds);
            });
        }
    });

}