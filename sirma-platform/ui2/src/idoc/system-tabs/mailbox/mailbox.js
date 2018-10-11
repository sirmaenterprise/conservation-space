import {Component, View, Inject, NgElement} from 'app/app';
import {ContextualEscapeAdapter} from 'adapters/angular/contextual-escape-adapter';
import {EmailAccountDelegationService} from 'services/rest/email-account-delegation-service';
import {InstanceList} from 'instance/instance-list';
import {NotificationService} from 'services/notification/notification-service';
import {CreatePanelService} from 'services/create/create-panel-service';
import {DialogService} from 'components/dialog/dialog-service';
import {UserService} from 'services/identity/user-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {EMF_MODIFIED_ON} from 'instance/instance-properties';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {SearchService} from 'services/rest/search-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {Configuration} from 'common/application-config';
import {LogoutEvent} from 'layout/top-header/main-menu/user-menu/settings/logout/logout-event';
import {ObjectMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/object-mailbox-updated-event';
import {PersonalMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/personal-mailbox-updated-event';
import {WindowMessageDispatcher} from 'common/window-message-dispatcher';
import {EmailAttachmentPickerService} from 'idoc/system-tabs/mailbox/picker/email-attachment-picker-service';
import {InstanceShareRestService} from 'services/rest/instance-share-service';
import {ShareFolderService} from 'services/rest/share-folder-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';
import _ from 'lodash';

import './mailbox.css!css';
import template from 'idoc/system-tabs/mailbox/mailbox.html!text';

/**
 * This component wraps an webmail client included with an iframe. The webmail requires specific url parameters to be
 * provided in order proper authentication to be performed. This component is used also for in-dialog email composing
 * triggered by compose email action.
 * User type mailbox and other business objects mailboxes are handled differently due to the need of delegating
 * permissions to the current logged in user to send mails in favor to the business object. Delegated rights are set on
 * mailbox open and are removed on leaving the concrete business object idoc, either navigating away through a link or
 * by closing the browser tab.
 * Multiple opened browser tabs are synchronized through the localStorage in order to be guaranteed that proper clear of
 * the delegated rights will be made. For example, if one object mailbox is opened in two tabs, closing one of them
 * should not clear the delegation.
 * Mailbox provides data to embeded webmail client passing them through MessageEvents. Events are passed back and forth
 * through the mailbox tab and a plugin deployed in the webmail client. There is simple protocol that guarantees that
 * the webmail client is properly initialized before required parameters to be passed with the next message event.
 */
@Component({
  selector: 'seip-mailbox',
  properties: {
    'context': 'context',
    'mailboxViewType': 'mailboxViewType'
  }
})
@View({
  template
})
@Inject(NgElement, ContextualEscapeAdapter, UserService, EmailAccountDelegationService, LocalStorageService,
  Configuration, PromiseAdapter, Eventbus, NotificationService, Logger, WindowMessageDispatcher, EmailAttachmentPickerService, InstanceShareRestService, CreatePanelService, SearchService, DialogService, ShareFolderService)
export class Mailbox {

  constructor($element, contextualEscapeAdapter, userService, emailAccountDelegationService, localStorageService,
              configurationService, promiseAdapter, eventbus, notificationService, logger, windowMessageDispatcher, emailAttachmentPickerService, instanceShareRestService, createPanelService, searchService, dialogService, shareFolderService) {
    this.$element = $element;
    this.contextualEscapeAdapter = contextualEscapeAdapter;
    this.emailAccountDelegationService = emailAccountDelegationService;
    this.notificationService = notificationService;
    this.localStorage = localStorageService;
    this.configurationService = configurationService;
    this.userService = userService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
    this.logger = logger;
    this.windowMessageDispatcher = windowMessageDispatcher;
    this.emailAttachmentPickerService = emailAttachmentPickerService;
    this.instanceShareRestService = instanceShareRestService;
    this.createPanelService = createPanelService;
    this.searchService = searchService;
    this.dialogService = dialogService;
    this.shareFolderService = shareFolderService;
  }

  ngOnInit() {
    let MutationObserver = window.MutationObserver || window.WebKitMutationObserver;
    let observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        this.iframe = _.find(mutation.addedNodes, item => item.nodeName.indexOf('IFRAME') !== -1);
        // initialize client after iframe is present
        if (this.iframe) {
          this.iframe = $(this.iframe);
          observer.disconnect();
          this.afterIFrameInit();
        }
      });
    });
    observer.observe(this.$element[0], {attributes: false, childList: true, characterData: false});
  }

  afterIFrameInit() {
    // Promise.all is replaced because of problems with unit tests. PromiseStub all method returns always same result
    // and chain can not be tested correct
    this.context.getCurrentObject().then((response) => {
      this.currentObject = response;
      return this.userService.getCurrentUser();
    }).then((response) => {
      this.currentUser = response;
      this.userInfo = _.cloneDeep(this.currentUser);

      // window message dispatcher receives data from zimlets in the format of serialized json objects which holds the
      // <zimlet_name> and <data>, afterwards sends the <data> string to the proper handler
      this.subscriptions = [
        this.windowMessageDispatcher.subscribe(Mailbox.DEPLOYED_ZIMLETS.COMPOSE_ZIMLET, this.composeZimletHandler.bind(this)),
        this.windowMessageDispatcher.subscribe(Mailbox.DEPLOYED_ZIMLETS.ATTACHMENT_ZIMLET, this.attachmentZimletHandler.bind(this)),
        this.windowMessageDispatcher.subscribe(Mailbox.DEPLOYED_ZIMLETS.IMPORT_ATTACHMENT_ZIMLET, this.importZimletHandler.bind(this)),
        this.windowMessageDispatcher.subscribe(Mailbox.DEPLOYED_ZIMLETS.MESSAGE_STATUS_HANDLER_ZIMLET, this.messageStatusZimletHandler.bind(this)),
        this.eventbus.subscribe(LogoutEvent, () => this.iframe.remove())
      ];

      // opening an object mailbox requires the current user to be delegated
      // with the privileges to send mails on behalf of the object which mailbox has been opened.
      if (this.currentObject.id === this.currentUser.id) {
        this.isObjectMailbox = false;
        this.initializeEmailClient(this.userInfo.emailAddress);
      } else {
        this.isObjectMailbox = true;
        this.initializeObjectMailbox(this.currentObject);
      }
      return this.userInfo;
    }).then((response) => {
      return this.emailAccountDelegationService.getEmailAccountAttributes(response.emailAddress);
    }).then((response) => {
      this.emailAttributes = response.data.data;
    }).catch((e) => {
      this.logger.error('There was a problem with mailbox initialization:', e);
    });
  }

  initializeEmailClient(loginAccount) {
    let url = `${this.configurationService.get(Mailbox.WEBMAIL_PROTOCOL)}://${this.configurationService.get(Mailbox.WEBMAIL_URL)}/preauth.jsp?account=${loginAccount}`;
    this.zimbraUrl = this.contextualEscapeAdapter.trustAsResourceUrl(url);
  }

  initializeObjectMailbox(objectContext) {
    let mailboxAddress = objectContext.getModels().validationModel[Mailbox.EMAIL_ADDRESS_CONST].value;
    this.composeOverrides = {
      from: this.userInfo.emailAddress, to: [],
      cc: [this.buildComposeRowField(mailboxAddress, objectContext.getModels().validationModel['title'].value)]
    };
    let openInTabCount = this.getOpenInTabCount(objectContext.id);
    if (openInTabCount === 0) {
      this.promiseAdapter.all([
        this.emailAccountDelegationService.delegateRights(this.userInfo.emailAddress, objectContext.id), this.shareFolderService.mountObjectShareFolder(mailboxAddress)])
        .then(() => {
          this.initializeEmailClient(mailboxAddress);
          this.incrementDelegatedRightsCounter(objectContext.id, openInTabCount);
        })
        .catch((error) => {
          this.notificationService.error(error);
        });
    } else {
      this.initializeEmailClient(mailboxAddress);
      this.incrementDelegatedRightsCounter(objectContext.id, openInTabCount);
    }

    window.addEventListener('beforeunload', this.ngOnDestroy.bind(this), false);
  }

  buildComposeRowField(mailboxAddress, objectTitle) {
    return `${mailboxAddress} "${objectTitle}"`;
  }

  getOpenInTabCount(key) {
    return this.localStorage.getJson(Mailbox.DELEGATED_RIGHTS_CONST, {})[key] || 0;
  }

  /**
   * Compose zimlet sends only the zimlet command that needs to be executed.
   *
   * @param zimletData zimlet command
   */
  composeZimletHandler(zimletData) {
    let payload = {
      tenantId: this.userInfo.tenantId,
      webmailUrl: this.configurationService.get(Mailbox.WEBMAIL_URL)
    };
    if (zimletData.command === Mailbox.COMPOSE_ZIMLET_COMMANDS.ZIMBRA_INITIALIZED && this.isObjectMailbox) {
      let objectSpecificProperties = {
        from: this.emailAttributes.emailAddress,
        displayName: this.emailAttributes.displayName,
        delegatedSenderAddress: this.composeOverrides.from,
        to: this.composeOverrides.to,
        cc: this.composeOverrides.cc,
        view: this.mailboxViewType
      };
      $.extend(payload, objectSpecificProperties);
      this.postMessageToMailClient(payload, Mailbox.DEPLOYED_ZIMLETS.COMPOSE_ZIMLET);
    } else if (zimletData.command === Mailbox.COMPOSE_ZIMLET_COMMANDS.ZIMBRA_INITIALIZED && !this.isObjectMailbox) {
      this.postMessageToMailClient(payload, Mailbox.DEPLOYED_ZIMLETS.COMPOSE_ZIMLET);
    } else if (zimletData.command === Mailbox.COMPOSE_ZIMLET_COMMANDS.RELOAD_IFRAME) {
      this.iframe.hide();
      this.showAlert = true;
    }
  }

  /**
   * Attachment zimlet sends the zimlet command and zimbra web client tab id
   * that the request has been made from.
   * @param zimletData <zimlet_command>,<tab_id> formatted string data for OPEN_SEARCH command
   *  and an object with an array of instanceId's and purpose for INITIATE_EXPORT command.
   */
  attachmentZimletHandler(zimletData) {
    if (zimletData.command === Mailbox.ATTACHMENTS_ZIMLET_COMMANDS.INITIALIZED) {
      this.postMessageToMailClient({applicationName: this.configurationService.get(Configuration.APPLICATION_NAME)});
    } else if (zimletData.command === Mailbox.ATTACHMENTS_ZIMLET_COMMANDS.OPEN_SEARCH) {
      let selectedObjects;
      this.emailAttachmentPickerService.selectAttachments().then((selection = []) => {
        if (selection.length === 0) {
          // no selection, skipping the processing
          return this.promiseAdapter.reject();
        }
        selectedObjects = selection;
        let selectedInstanceIds = selection.map(object => object.id);
        return this.instanceShareRestService.shareLinks({ids: selectedInstanceIds});
      }).then(({data}) => {
        let shareLinks = data;
        let shareLinkTemplates = this.createShareLinkTemplates(selectedObjects, shareLinks);
        let payload = {viewId: zimletData.viewId, links: shareLinkTemplates};
        this.postMessageToMailClient(payload, Mailbox.DEPLOYED_ZIMLETS.ATTACHMENT_ZIMLET);
      }).catch(error => {
        if (error) {
          this.logger.error(error);
        }
      });
    } else if (zimletData.command === Mailbox.ATTACHMENTS_ZIMLET_COMMANDS.INITIATE_EXPORT) {
      this.instanceShareRestService.triggerShare(zimletData.sharecodes);
    }
  }

  importZimletHandler(zimletData) {
    if (zimletData.command === Mailbox.IMPORT_ATTACHMENT_ZIMLET_COMMANDS.IMPORT_ATTACHMENT) {
      zimletData.fileObject.name = zimletData.fileName;
      this.searchService.search(this.buildSearchRequest(zimletData.recipients)).promise.then(response => {
        let contexts = response.data.values;
        if (contexts.length === 0) {
          this.openUploadInstanceDialog(zimletData.fileObject, '');
        } else if (contexts.length === 1) {
          this.openUploadInstanceDialog(zimletData.fileObject, contexts[0].id);
        } else if (contexts.length > 1) {
          this.openSelectContextDialog(contexts, zimletData.fileObject);
        }
      });
    }
  }

  messageStatusZimletHandler(zimletData) {
    if (zimletData.command === Mailbox.MESSAGE_STATUS_HANDLER_ZIMLET_COMMANDS.MAILBOX_UNREAD_MESSAGES) {
      if (this.currentObject.id === this.currentUser.id) {
        this.eventbus.publish(new PersonalMailboxUpdatedEvent(zimletData.unreadMessagesCount));
      }
      this.eventbus.publish(new ObjectMailboxUpdatedEvent(zimletData.unreadMessagesCount));
    }
  }

  openSelectContextDialog(contexts, fileObject) {
    delete this.selectedContext;
    this.dialogService.create(InstanceList, this.getResultsConfig(contexts), this.getDialogConfig(fileObject));
  }

  selectionHandler(selectedObject) {
    this.selectedContext = selectedObject.id;
  }

  getResultsConfig(contexts) {
    return {
      config: {
        selectableItems: true,
        singleSelection: true,
        selectionHandler: this.selectionHandler.bind(this),
        selectAll: true,
        deselectAll: true,
        exclusions: [],
        selectedItems: []
      },
      instances: contexts
    };
  }

  getDialogConfig(fileObject) {
    this.dialogConfig = {
      header: 'email.attachment.select.context',
      showClose: true,
      buttons: [
        {
          id: DialogService.OK,
          label: 'dialog.button.ok',
          cls: 'btn-primary',
          disabled: false,
          dismiss: true,
          onButtonClick: () => {
            this.openUploadInstanceDialog(fileObject, this.selectedContext);
            this.dialogConfig.dismiss();
          }
        },
        {
          id: DialogService.CANCEL,
          label: 'dialog.button.cancel',
          dismiss: true,
          onButtonClick: () => {
            this.openUploadInstanceDialog(fileObject, '');
            this.dialogConfig.dismiss();
          }
        }
      ]
    };
    return this.dialogConfig;
  }

  openUploadInstanceDialog(fileObject, parentId) {
    let params = {
      parentId,
      operation: 'create',
      scope: this.$scope,
      fileObject
    };
    this.createPanelService.openUploadInstanceDialog(params);
  }

  buildSearchRequest(recipientAddresses) {
    let searchRequest = {};

    searchRequest.arguments = {
      orderBy: EMF_MODIFIED_ON,
      orderDirection: 'asc',
      properties: ['id', HEADER_DEFAULT],
      pageSize: 0
    };

    let rules = [];
    let recepientFilterRules = recipientAddresses.map(recipientAddress => {
      return SearchCriteriaUtils.buildRule('emf:mailboxAddress', 'string', AdvancedSearchCriteriaOperators.CONTAINS.id, recipientAddress);
    });

    rules.push(SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.OR_CONDITION, recepientFilterRules));


    let searchCriteria = SearchCriteriaUtils.buildCondition('', rules);

    searchRequest.query = {
      tree: searchCriteria
    };
    searchRequest.arguments = {
      filterByWritePermissions: true
    };
    return searchRequest;
  }

  createShareLinkTemplates(selectedObjects, shareLinks) {
    return selectedObjects.map((object, index) => {
      let attachmentTitle = Mailbox.buildAttachmentTitle(object);
      // TODO: images should be linked to the mail somehow and should be public accessible. Base64 encoded
      // and embedded in the email images doesn't show in the mail clients.
      let instanceIconUrl = this.getInstanceIconUrl(object.headers.breadcrumb_header);
      return Mailbox.getShareLinkTemplate(instanceIconUrl, shareLinks[index], attachmentTitle);
    });
  }

  getInstanceIconUrl(instanceHeader) {
    let relativeUrl = $(instanceHeader).find('img').attr('src');
    return this.configurationService.get('ui2.url') + relativeUrl;
  }

  static buildAttachmentTitle(selectedObject) {
    return `${selectedObject.properties['title']}, v.${selectedObject.properties['emf:version']}`;
  }

  static getShareLinkTemplate(icon, shareLink, title) {
    // instance-link attribute is needed because the zimbra plugin dealing with the attachments uses it to trigger the
    // export tasks
    return `<div style="display:inline;" contenteditable="false" instance-link><a href="${shareLink}" class="instance-link"><img src="${icon}" /><span>${title}</span></a></div><br />`;
  }

  reloadMailboxIframe() {
    this.iframe.show();
    this.showAlert = false;
  }

  ngOnDestroy() {
    if (this.isObjectMailbox) {
      let openInTabCount = this.decrementDelegatedRightsCounter();
      if (openInTabCount === 0) {
        this.emailAccountDelegationService.removeRights(this.userInfo.emailAddress, this.context.currentObjectId);
      }
    }

    this.iframe.remove();
    this.subscriptions.forEach((event) => {
      event.unsubscribe();
    });
  }

  getMailClientWindow() {
    if (!this.iframe.get(0) || !this.iframe.get(0).contentWindow) {
      this.iframe = this.$element.find('iframe');
    }
    return this.iframe.get(0).contentWindow;
  }

  /**
   * Sends a message to the mailbox client, using the unified format of <zimlet name>,<data payload> so it can be properly be worked with inside the zimlet.
   * @param zimlet zimlet name
   * @param data payload for the zimlet.
   */
  postMessageToMailClient(data, zimlet) {
    data.topic = zimlet;
    this.getMailClientWindow().postMessage(data, '*');
  }

  incrementDelegatedRightsCounter(objectId, openInTabCount) {
    this.localStorage.mergeValues(Mailbox.DELEGATED_RIGHTS_CONST, {
      [objectId]: ++openInTabCount
    });
  }

  decrementDelegatedRightsCounter() {
    let accountDelegationStorage = this.localStorage.getJson(Mailbox.DELEGATED_RIGHTS_CONST);
    let openInTabCount = accountDelegationStorage[this.context.currentObjectId] - 1;

    if (openInTabCount) {
      accountDelegationStorage[this.context.currentObjectId] = openInTabCount;
    } else {
      delete accountDelegationStorage[this.context.currentObjectId];
    }

    this.localStorage.set(Mailbox.DELEGATED_RIGHTS_CONST, accountDelegationStorage);
    return openInTabCount;
  }
}

Mailbox.EMAIL_ADDRESS_CONST = 'emailAddress';
Mailbox.NAME_CONST = 'name';
Mailbox.USERNAME_CONST = 'username';
Mailbox.TITLE_CONST = 'title';
Mailbox.WEBMAIL_PROTOCOL = 'subsystem.emailintegration.webmail.protocol';
// TODO: Switch to proper name when CMF-22202 is closed.
Mailbox.WEBMAIL_URL = 'subsystem.emailintegration.webmail.url';
// used to keep track of the same mailbox open in different tabs.
Mailbox.DELEGATED_RIGHTS_CONST = 'mailbox.delegated.rights.counter';
Mailbox.COMPOSE_ZIMLET_COMMANDS = {
  ZIMBRA_INITIALIZED: 'ZIMBRA_INITIALIZED',
  RELOAD_IFRAME: 'RELOAD_IFRAME'
};
Mailbox.ATTACHMENTS_ZIMLET_COMMANDS = {
  INITIALIZED: 'ATTACHMENTS_ZIMLET_INITIALIZED',
  OPEN_SEARCH: 'OPEN_SEARCH',
  INITIATE_EXPORT: 'INITIATE_ATTACHMENT_EXPORT'
};
Mailbox.IMPORT_ATTACHMENT_ZIMLET_COMMANDS = {
  IMPORT_ATTACHMENT: 'IMPORT_ATTACHMENT'
};
Mailbox.MESSAGE_STATUS_HANDLER_ZIMLET_COMMANDS = {
  MAILBOX_UNREAD_MESSAGES: 'MAILBOX_UNREAD_MESSAGES'
};
Mailbox.DEPLOYED_ZIMLETS = {
  COMPOSE_ZIMLET: 'com_sep_compose_zimlet',
  ATTACHMENT_ZIMLET: 'com_sep_attachment_zimlet',
  IMPORT_ATTACHMENT_ZIMLET: 'com_sep_import_attachment_zimlet',
  MESSAGE_STATUS_HANDLER_ZIMLET: 'com_sep_message_status_handler_zimlet'
};
