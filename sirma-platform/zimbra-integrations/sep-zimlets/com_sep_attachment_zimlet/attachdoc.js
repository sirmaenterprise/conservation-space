/**
 * SEP Attachment zimlet implementation. Adds an additional button to the add attachment dropdown.
 * When clicked it opens a search picker and appends download links to the selected objects in the mail message.
 */
function com_sep_attachment_zimlet() {
};

com_sep_attachment_zimlet.prototype = new ZmZimletBase();
com_sep_attachment_zimlet.prototype.constructor = com_sep_attachment_zimlet;
com_sep_attachment_zimlet.prototype.topic = 'com_sep_attachment_zimlet';
com_sep_attachment_zimlet.prototype.sepBase = new com_sep_base_zimlet();


/**
 * Initializes {@AttachmentMgr} and initializes attachment if compose view is opened directly via action.
 */
com_sep_attachment_zimlet.prototype.init = function () {
  this.attachmentMgr = new AttachmentMgr();
  this.appViewMgr = function () {
    return appCtxt.getAppViewMgr();
  };
  window.addEventListener('message', this.messageHandler.bind(this));

  this.sepBase.postMessage(this, {command: 'ATTACHMENTS_ZIMLET_INITIALIZED'});
  if (this.sepBase.isComposeTab(this.appViewMgr().getCurrentViewId())) {
    this.initializeAttachment(this.appViewMgr().getCurrentViewId());
  }
};

/**
 * Adds selection listener to the send button.
 *@see ZmZimletBase
 */
com_sep_attachment_zimlet.prototype.initializeToolbar =
  function (app, toolbar, controller, viewId) {
    if (this.sepBase.isComposeTab(viewId) && !this.attachmentMgr.isRegistered(viewId)) {
      var sendButton = toolbar.getButton('SEND_MENU') || toolbar.getButton('SEND');
      if (!sendButton) {
        return;
      }

      sendButton.addSelectionListener(new AjxListener(this, function () {

        var editorBody = appCtxt.getCurrentView().getHtmlEditor().getBodyField();
        var instanceLinks = $(editorBody).contents().find('div[instance-link]');
        if (instanceLinks.length > 0) {
          var shareLinks = [];
          instanceLinks.each(function () {
            var insanceLink = $(this).find('.instance-link').eq(0)
            // only sharecode is needed from the href, which is always last in the url.
            shareLinks.push(insanceLink.attr('href').split('shareCode=')[1]);
          });

          this.sepBase.postMessage(this, {
            command: 'INITIATE_ATTACHMENT_EXPORT',
            sharecodes: shareLinks
          });
        }
      }.bind(this), controller));
    }
  };

/**
 * Created and appends {@DwtMenuItem} to the attachments dropdown with custom finctionality.
 * Executes when compose window is open
 * @param viewId id of the compose window.
 */
com_sep_attachment_zimlet.prototype.initializeAttachment = function (viewId) {
  this.attachmentMgr.initializeComposeView(viewId);

  var attButton = appCtxt.getCurrentView()._attButton;
  var attMenu = attButton.getMenu();
  var menuItem = DwtMenuItem.create({parent: attMenu, id: 'SEP_ATTACHMENT_BTN'});

  menuItem.setText("Attach from " + this.applicationName);
  menuItem.addSelectionListener(new AjxListener(this, function () {
    this.sepBase.postMessage(this, {
      command: 'OPEN_SEARCH',
      viewId: viewId
    });
  }));
};

/**
 * Handles recieved messages from the ui.
 * Message format is a pojo wih an attribute topic, which is the zimlet name.
 * First its checked if the recieved data is intended for this zimlet, then registers the recieved data
 * to the tab id. After that the data is appended to the mail message.
 * @param msg recieved message from the parent window.1
 */
com_sep_attachment_zimlet.prototype.messageHandler = function (msg) {
  var recievedMessage = msg.data;
  if (recievedMessage.applicationName) {
    this.applicationName = recievedMessage.applicationName;
    return;
  }
  if (recievedMessage.topic === this.topic && this.attachmentMgr.isRegistered(recievedMessage.viewId)) {
    var composeView = appCtxt.getCurrentView();
    var links = '';

    recievedMessage.links.forEach(function (attachmentLink) {
      links += attachmentLink + ' '
    });
    // added because the caret position was lost after insert
    links += '&nbsp;';
    var span = document.createElement('span');

    composeView.getHtmlEditor().getEditor().execCommand('mceInsertContent', true, links);

    var attachmentLinks = composeView.getHtmlEditor().getEditor().getBody().querySelectorAll('[instance-link] a');
    for (var i = 0; i < attachmentLinks.length; i++) {
      attachmentLinks[i].onclick = function (evt) {
        evt.preventDefault();
      }
    }
  }
};

/**
 * Initializes attachment functionality if the opened window is a newly opened compose window.
 * @param viewId windowId
 * @param isNewView if the opened window is freshly opened, closed or just this view tab is clicked from the ui.
 */
com_sep_attachment_zimlet.prototype.onShowView = function (viewId, isNewView) {
  if (this.sepBase.isComposeTab(viewId) && !this.attachmentMgr.isRegistered(viewId)) {
    this.initializeAttachment(viewId);
    return;
  }

  var lastViewId = this.appViewMgr().getLastViewId();
  // closing a tab returns a null flag.
  if (isNewView === null && this.sepBase.isComposeTab(lastViewId)) {
    this.attachmentMgr.removeView(lastViewId);
  }
};

/**
 *  @class
 * Attachment Manager, used to manage opened compose view tabs
 * so that selected object download links are to be appended on the correct view.
 * This is needed because when a compose window is closed and then opened again,
 * there will be more than one "Attach from SEP" dropdown items on a compose form.
 * @constructor
 */
AttachmentMgr = function () {
  this.registeredViews = {};
};

AttachmentMgr.prototype = AttachmentMgr;
AttachmentMgr.prototype.constructor = AttachmentMgr;

/**
 * Registers a new view to be managed by the manager, or just simply reinitializes one.
 * Executed when a compose view is opened or view is initialized in compose view.
 * @param viewId newly opened compose view id.
 */
AttachmentMgr.prototype.initializeComposeView = function (viewId) {
  Object.defineProperty(this.registeredViews, viewId, {
    writable: false,
    enumerable: false
  });
};

AttachmentMgr.prototype.getTabData = function (viewId) {
  return this.registeredViews[viewId];
};

AttachmentMgr.prototype.removeView = function (viewId) {
  delete this.registeredViews[viewId];
};

/**
 * Checks if view is registered.
 * @param viewId
 * @returns {*} true if its
 */
AttachmentMgr.prototype.isRegistered = function (viewId) {
  return this.registeredViews.hasOwnProperty(viewId);
};
