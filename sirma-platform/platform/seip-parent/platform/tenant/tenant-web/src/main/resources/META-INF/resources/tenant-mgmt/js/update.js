function UpdateController() {

    $("#data").submit(function (event) {
        var tenantId = $('#tenants').find('option:selected').val()
        var patches = $('input[name=SemanticDbUpdate_attachment_patches]')[0].files[0];
        var message = "Are you sure you want to update tenant " + tenantId + " with no semantic customization file?";
        if (patches) {
            message = "Are you sure you want to update tenant " + tenantId + " with semantic customization file " + patches.name + "?";
        }
        if (window.confirm(message)) {
            //grab all form data
            var formData = new FormData();

            var models = $('input[name=DMSInitialization_attachment_definitions]')[0].files[0];
            if (models) {
                formData.append('DMSInitialization_attachment_definitions', models);
            }

            formData.append('SemanticDbUpdate_attachment_patches', patches);

            var result = $.ajax({
                type: "POST",
                url: "../service/tenant/" + tenantId,
                data: formData,
                async: false,
                processData: false,
                contentType: false,
                success: function (response) {
                    alert("Successfully updated tenant");
                },
                error: function (response) {
                    alert(response.responseText);
                }
            });
        }
    });

    // get all tenants
    $.get("../api/tenant/list", function (tenantList) {
        var option = '';
        if (tenantList.length > 0) {
            for (var i = 0; i < tenantList.length; i++) {
                option += '<option value="' + tenantList[i].tenantId + '">' + tenantList[i].tenantId + '</option>';
            }
            $('#tenants').append(option);
        } else {
            document.location.href = "../tenant-mgmt/index.html";
            alert('There is no active tenants. Do you want to create one?');
        }
    });

}