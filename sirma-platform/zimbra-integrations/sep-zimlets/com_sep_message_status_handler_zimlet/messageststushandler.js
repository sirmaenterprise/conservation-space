/**
 * SEP message status handler zimlet used to hook on different events and extract count of messages for given status
 */
function com_sep_message_status_handler_zimlet() {
}

com_sep_message_status_handler_zimlet.prototype = new ZmZimletBase();
com_sep_message_status_handler_zimlet.prototype.response = AjxRpc.freeRpcCtxt;

com_sep_message_status_handler_zimlet.prototype.init = function () {
  com_sep_message_status_handler_zimlet.prototype.constructor = com_sep_message_status_handler_zimlet;
  com_sep_message_status_handler_zimlet.prototype.topic = 'com_sep_message_status_handler_zimlet';
  com_sep_message_status_handler_zimlet.prototype.sepBase = new com_sep_base_zimlet();
};

AjxRpc.freeRpcCtxt = function (rpcCtxt) {
  var zimlet = com_sep_message_status_handler_zimlet.prototype;
  zimlet.response(rpcCtxt);

  // This is needed to detect when user clicks over folder
  if (zimlet.folderIsClicked) {
    zimlet.updateUnreadMessagesCount();
    zimlet.folderIsClicked = false;
    return;
  }
  // Response parse to json object should be wrapped in try catch because sometimes response contains
  // specific service information which is not in json string format
  try {
    var responseObj = JSON.parse(rpcCtxt.__httpReq.response);
    if (!responseObj || !responseObj.Body) {
      return;
    }
    var responseBody = responseObj.Body;
    var responseName = Object.keys(responseBody)[0];

    // GetMsgResponse is returned when message is clicked for first time
    // NoOpResponse is returned when update current view button is pressed
    // FolderActionResponse is returned when mark all as read on folder is clicked
    if (responseName === 'GetMsgResponse' || responseName === 'NoOpResponse' || responseName === 'FolderActionResponse') {
      zimlet.updateUnreadMessagesCount();
    } else if (responseName === 'MsgActionResponse') {
      // MsgActionResponse is returned when action over message is executed.	
      var operation = responseBody[responseName].action.op;
      if (operation === '!read' || operation === 'read' || operation === 'move' || operation === 'trash' || operation === 'spam' || operation === 'delete') {
        zimlet.updateUnreadMessagesCount();
      }
    }
  } catch (e) {
  }
};

com_sep_message_status_handler_zimlet.prototype.onAction = function (type, action, currentViewId, lastViewId) {	
  if (type === 'treeitem') {
    com_sep_message_status_handler_zimlet.prototype.folderIsClicked = true;
  }
};

com_sep_message_status_handler_zimlet.prototype.updateUnreadMessagesCount = function () {
  // visibleAccounts are all registered accounts (external and internal) which are active at the moment
  var accounts = appCtxt.accountList.visibleAccounts;
  var unreadMessages = 0;
  for (var i = 0; i < accounts.length; i++) {
    var folderTree = appCtxt.getFolderTree(accounts[i]);
    var folders = folderTree.asList({includeRemote: true});
    for (var j = 0; j < folders.length; j++) {
      unreadMessages += folders[j].numUnread;
    }
  }
  this.sepBase.postMessage(this, {
    command: 'MAILBOX_UNREAD_MESSAGES',
    unreadMessagesCount: unreadMessages
  });
};
