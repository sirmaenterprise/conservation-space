/**
 * SEP compose zimlet used to override and append data to the composed mail message if needed.
 */
function com_sep_compose_zimlet() {
}

com_sep_compose_zimlet.prototype = new ZmZimletBase();
com_sep_compose_zimlet.prototype.constructor = com_sep_compose_zimlet;
com_sep_compose_zimlet.prototype.topic = 'com_sep_compose_zimlet';
com_sep_compose_zimlet.prototype.sepBase = new com_sep_base_zimlet();

/**
 * Handles recieved data from the parent window. Recieved data format should be <zimlet_id>,<data_json>
 * @param msg recieved message from parent window.
 */
com_sep_compose_zimlet.prototype.messageHandler = function (msg) {
  var recievedMessage = msg.data;
  if (recievedMessage.topic === this.topic) {
    this.from = recievedMessage.from;
    this.displayName = recievedMessage.displayName;
    this.to = recievedMessage.to;
    this.cc = recievedMessage.cc;
    this.bcc = recievedMessage.bcc;

    if (recievedMessage.view === 'compose') {
      AjxDispatcher.run("Compose");
    }
    this.sepBase.postMessage(this, {command: 'ZIMLET_INITIALIZED'});
  }
};

com_sep_compose_zimlet.prototype.init =
  function () {
    window.addEventListener('message', this.messageHandler.bind(this));

    // when session is over page is reloaded to show login dialog
    window.addEventListener('unload', function () {
      this.sepBase.postMessage(this, {command: 'RELOAD_IFRAME'});
    }.bind(this));

    // When more than one tab with mail client is open in browser Zimbra terminate previous session and shows "Permission denied" error.
    // We need to handle this but API don't provide reliable event. That's why we use MutationObserver to detect when error popup is visible on screen.
    MutationObserver = window.MutationObserver || window.WebKitMutationObserver;
    var observer = new MutationObserver(function (mutations) {
      mutations.forEach(function (mutation) {
        if (mutation.target.id === 'ErrorDialog' && $('#' + mutation.target.id).find('.DwtMsgArea').text() === 'Permission denied.') {
          this.sepBase.postMessage(this, {command: 'RELOAD_IFRAME'});
        }
      }.bind(this))
    }.bind(this));
    observer.observe(document.getElementById('z_shell'), {
      subtree: true,
      attributes: true
    });

    this.sepBase.postMessage(this, {command: 'ZIMBRA_INITIALIZED'});
  };

com_sep_compose_zimlet.prototype.onShowView = function (view) {
  if(view === 'CNS-main') {
    var folderTree = appCtxt.getFolderTree();
    var folders = folderTree.asList({includeRemote: true});
    for (var i = 0; i < folders.length; i++) {
      if(folders[i].name === this.tenantId) {
        $('#zti__main_Contacts__' + folders[i].id).remove();
        break;
      }
    }
  }
  if (this.sepBase.isComposeTab(view)) {
    if(this.from) {
      var identitySelect = appCtxt.getCurrentView().identitySelect;
      identitySelect.clearOptions();
      identitySelect.addOption(this.displayName + ' <' + this.from + '>', true);
      identitySelect.disable();
    }

    this.appendAddresses('to');
    this.appendAddresses("cc");
    this.appendAddresses("bcc");
  }
};

com_sep_compose_zimlet.prototype.appendAddresses = function (type) {
  if (this[type] && this[type].length) {
    this[type].forEach(function (mailAddress) {
      appCtxt.getCurrentView().setAddress(type.toUpperCase(), mailAddress);
    });
  }
};

com_sep_compose_zimlet.prototype.isInTenantDomain = function(emailAddress) {
  return emailAddress.indexOf(this.tenantId) !== -1 && emailAddress.indexOf(this.webmailUrl) !== -1;
};

/** 
 * emailAddress is split and filled in the contact properties the same way zimbra do it in backend
 */
com_sep_compose_zimlet.prototype.addNewContact = function(emailAddress) {
  var name = emailAddress.substr(0, emailAddress.indexOf('@'));
  var nameParts = name.split('.');
  var contactAttributes = {};
  contactAttributes[ZmContact.F_folderId] = 13;
  contactAttributes[ZmContact.F_email] = emailAddress;
  contactAttributes[ZmContact.F_firstName] = nameParts[0];
  if(nameParts.length > 1) {
    contactAttributes[ZmContact.F_lastName] = nameParts[nameParts.length - 1];
    var middleName = '';
    for (var i = 1; i < nameParts.length - 1; i++) {
      middleName += nameParts[i] + ' ';
    }
    contactAttributes[ZmContact.F_middleName] = middleName;
  }
  ZmContact.prototype.create(contactAttributes);
};

com_sep_compose_zimlet.prototype.updateContacts = function(recipients) {
  if (this.tenantId && this.webmailUrl) {
    var contactList = AjxDispatcher.run("GetContacts");
	for (var i = 0; i < recipients.length; i++) {
	  var recipient = recipients[i];
	  if(!contactList.getContactByEmail(recipient) && !this.isInTenantDomain(recipient)) {
	    this.addNewContact(recipient);
	  }
	}
  }
};

com_sep_message_status_handler_zimlet.prototype.showSharedContacts = function () {
  // Open 'Show names from:' dropdown in addressbook popup	
  $("#ZmContactPickerSelect_1").click();
  // Select second option 'Personal and Shared Contacts'
  $("#ZmContactPickerSelect_1_Menu_1_option_2").mousedown();
  $("#ZmContactPickerSelect_1_Menu_1_option_2").mouseup();
  // Execute search by click on 'Search' button
  $("#DWT91").click();
};

com_sep_message_status_handler_zimlet.prototype.onAction = function (type, action, currentViewId, lastViewId) {
  if (type === 'button' && (action === 'To:' || action === 'Cc:')) {
	this.showSharedContacts();
  }     
};

/**
 * This method is called by the Zimlet framework when a message is about to be sent.
 * Hooking here allow us to change mail identity by adding send from and reply to address
 * immediately before sending.
 * To fail the error check, and show custom error message the zimlet must return a boolAndErrorMsgArray array with the following syntax:
 * {hasError: <true>, errorMsg:<error msg>, zimletName:<zimlet name>}
 */
com_sep_compose_zimlet.prototype.emailErrorCheck = function (mail, boolAndErrorMsgArray) {
  this.updateContacts(this.sepBase.findAllRecipients(mail._addrs.TO['_array'], mail._addrs.CC['_array']));	
	
  if (this.from) {
    mail.identity = {};
    // set from address and display name
    mail.identity.sendFromAddress = this.from;
    mail.identity.sendFromDisplay = this.displayName;
    // allow "reply to" to be configured and set address and display name
    mail.identity.setReplyTo = true;
    mail.identity.setReplyToAddress = this.from;
    mail.identity.setReplyToDisplay = this.displayName;
    // set sender address. This allow message send from project to be visible in user sent folder
    mail.delegatedSenderAddr = this.delegatedSenderAddress;
    return boolAndErrorMsgArray;
  }
}