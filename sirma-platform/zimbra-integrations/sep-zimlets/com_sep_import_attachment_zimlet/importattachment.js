/**
 * SEP import zimlet used to import attachment in the system
 */
function com_sep_import_attachment_zimlet() {
}

com_sep_import_attachment_zimlet.prototype = new ZmZimletBase();
com_sep_import_attachment_zimlet.prototype.constructor = com_sep_import_attachment_zimlet;
com_sep_import_attachment_zimlet.prototype.topic = 'com_sep_import_attachment_zimlet';
com_sep_import_attachment_zimlet.prototype.sepBase = new com_sep_base_zimlet();

com_sep_import_attachment_zimlet.prototype.generateImportAttachmentOption = function (attachment) {
  return "<a href='javascript:' style='text-decoration:underline;' onClick=\"com_sep_import_attachment_zimlet.prototype.prepareBlob('" + attachment.label + "', '" + attachment.url + "')\">Import</a>";
};

com_sep_import_attachment_zimlet.prototype.onFindMsgObjects = function (msg, objMgr) {
  com_sep_import_attachment_zimlet.prototype.recipients = this.sepBase.findAllRecipients(msg._addrs.TO['_array'], msg._addrs.CC['_array']);
  this._msgController = AjxDispatcher.run("GetMsgController");
  var viewType = appCtxt.getViewTypeFromId(ZmId.VIEW_MSG);
  this._msgController._initializeView(viewType);

  msg.attachments.forEach(function(attachment) {
	this._msgController._listView[viewType].addAttachmentLinkHandler(attachment.ct, "Import", this.generateImportAttachmentOption);
  }.bind(this));
};

com_sep_import_attachment_zimlet.prototype.prepareBlob = function (fileName, url) {
  this.getFileBlob(url, function (blob) {
    this.sepBase.postMessage(this, {
      command: 'IMPORT_ATTACHMENT',
      fileName: fileName,
      attachmentUrl: url,
      recipients: this.recipients,
      fileObject: this.blobToFile(blob, fileName)
    });

  }.bind(this));
};

com_sep_import_attachment_zimlet.prototype.blobToFile = function (blob, name) {
  blob.lastModifiedDate = new Date();
  blob.name = name;
  return blob;
};

com_sep_import_attachment_zimlet.prototype.getFileBlob = function (url, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", url);
  xhr.responseType = "blob";
  xhr.addEventListener('load', function () {
    callback(xhr.response);
  });
  xhr.send();
};
