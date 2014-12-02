$(function() {
		$("#nav-upload-btn").parent().addClass("active");
		//handle the click event of the upload button
		$("#upload_btn").click(function() {
			CLS.uploadFile(this.form,'/cls/service/codelists/upload','message');
		});	
		//handle the change event of the browse button
		$("#dataFile").change(function() {
			CLS.validateFileExtension(this);
		});			
});

var CLS = CLS || {};
/**
 * Validates that the file attached to the sender object has an .xls extension 
 * If not, displays an alert dialog.
 * 
 * @param sender  - the sender DOM element
 * @returns {Boolean}
 */
CLS.validateFileExtension = function(sender) {
	var validExts = ['.xls']
    var fileExt = sender.value;
    fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
    if (validExts.indexOf(fileExt) < 0) {	
    //TODO: use EMF.dialog instead of alerts 
      alert("Invalid file selected. Valid files are of " +
               validExts.toString() + " type.");
		sender.value = "";
		return false;
    } else {
    	return true;
    }
}

/**
 * Handles the server response printing the returned message into the given element - 'resultsPanel'
 * Note: classic server response codes can't be used due to the used request approach
 * 
 * @param response - the server response as a raw JSON
 * @param resultsPanelId - the id of the element where the response will be printed 
 */
CLS.handleResponse = function (response, resultsPanelId) {
		var resultsPanel = $("#" + resultsPanelId);
	    try {
	    	var obj = $.parseJSON(response);
	    } catch (e) {
		    //if the json can't be parsed, the response string is a server exception log
	    	resultsPanel.html("<div class=\"alert alert-danger\"> Unhandled server exception:" + 
	    			" (see log for more details)<br>"+ response +"</div>");		
			return;
	    }
	    // TODO: find a workaround to use the server status codes instead of messages
	    //(at the moment, the iFrame approach stops this)
		if(obj.result == "ok") {
			resultsPanel.html("<div class=\"alert alert-success\">"+ obj.resultMessage + "</div>");
		} else if (obj.result == "error") {
			resultsPanel.html("<div class=\"alert alert-danger\">"+ obj.resultMessage + "</div>");	
		}
}

/**
 * Posts the file asynchronously using iFrame
 * Note: a working third-party code. Don't touch it. You can mess it up.
 */
CLS.uploadFile = function (form, action_url, div_id) {
    // Create the iframe...
    var iframe = document.createElement("iframe");
    iframe.setAttribute("id", "upload_iframe");
    iframe.setAttribute("name", "upload_iframe");
    iframe.setAttribute("width", "0");
    iframe.setAttribute("height", "0");
    iframe.setAttribute("border", "0");
    iframe.setAttribute("style", "width: 0; height: 0; border: none;");
 
    // Add to document...
    form.parentNode.appendChild(iframe);
    window.frames['upload_iframe'].name = "upload_iframe";
    iframeId = document.getElementById("upload_iframe");
 
    // Add event...
    var eventHandler = function () {
            if (iframeId.detachEvent) {
            	iframeId.detachEvent("onload", eventHandler);
            } else {
            	iframeId.removeEventListener("load", eventHandler, false);
            }
 			
            // Message from server...
            if (iframeId.contentDocument) {
                content = iframeId.contentDocument.body.getElementsByTagName("PRE")[0].innerHTML;
            } else if (iframeId.contentWindow) {
                content = iframeId.contentWindow.document.body.getElementsByTagName("PRE")[0].innerHTML;
            } else if (iframeId.document) {
                content = iframeId.document.body.getElementsByTagName("PRE")[0].innerHTML;
            }
  			//the server response is handled here
  			CLS.handleResponse(content, div_id);
            // Del the iframe...
            setTimeout('iframeId.parentNode.removeChild(iframeId)', 250);
        }
    
    if (iframeId.addEventListener) {
    	iframeId.addEventListener("load", eventHandler, true);
    }
    if (iframeId.attachEvent) {
    	iframeId.attachEvent("onload", eventHandler);
    }
 
    // Set properties of form...
    form.setAttribute("target", "upload_iframe");
    form.setAttribute("action", action_url);
    form.setAttribute("method", "post");
    form.setAttribute("enctype", "multipart/form-data");
    form.setAttribute("encoding", "multipart/form-data");
 
    // Submit the form and put the ajax loader
    form.submit();
    CLS.util.displayAjaxLoader(div_id, "Uploading...", false);
}