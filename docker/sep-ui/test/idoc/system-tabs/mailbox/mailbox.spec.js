import {Mailbox} from 'idoc/system-tabs/mailbox/mailbox';
import {EmailAccountDelegationService} from 'services/rest/email-account-delegation-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {NotificationService} from 'services/notification/notification-service';
import {UserService} from 'security/user-service';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {LogoutEvent} from 'layout/top-header/main-menu/user-menu/settings/logout/logout-event';
import {Logger} from 'services/logging/logger';
import {WindowMessageDispatcher} from 'common/window-message-dispatcher';
import {EmailAttachmentPickerService} from 'idoc/system-tabs/mailbox/picker/email-attachment-picker-service';
import {InstanceShareRestService} from 'services/rest/instance-share-service';
import {SearchService} from 'services/rest/search-service';
import {ShareFolderService} from 'services/rest/share-folder-service';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {ObjectMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/object-mailbox-updated-event';
import {stub, MockEventbus} from 'test/test-utils';
import _ from 'lodash';

// TODO: these tests are mess and need serious refactoring as the mailbox component itself

describe('Mailbox', () => {
  let fakeElement = {
    find: () => {
      return {
        eq: () => {
          return {
            remove: () => {
            }, attr: () => {
            }
          }
        }
      }
    },
    attr: () => {
    },
    remove: () => {
    }
  };
  let contextualEscapeAdapterMock = {
    trustAsResourceUrl: (url) => url
  };
  let accountDelegationStub;
  let userServiceStub;
  let notificationService;
  let logger;
  let messageDispatcherStub;
  let configurationServiceStub;
  let emailAttachmentPickerServiceStub;
  let instanceShareRestServiceStub;
  let localStorageStub;
  let searchService;
  let shareFolderService;

  beforeEach(() => {
    accountDelegationStub = stub(EmailAccountDelegationService);
    accountDelegationStub.delegateRights.returns(PromiseStub.resolve());
    accountDelegationStub.getEmailAccountAttributes.returns(PromiseStub.resolve({
      data: {
        data: 'test_email@test.com'
      }
    }));
    userServiceStub = stub(UserService);
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      id: 'test-user-id',
      tenantId: 'test-tenant-id',
      emailAddress: 'test-mail@domain.com'
    }));

    notificationService = stub(NotificationService);
    logger = stub(Logger);
    messageDispatcherStub = stub(WindowMessageDispatcher);
    messageDispatcherStub.subscribe.returns({
      unsubscribe: () => {
      }
    });

    configurationServiceStub = stub(Configuration);
    configurationServiceStub.get.withArgs(Mailbox.WEBMAIL_PROTOCOL).returns('http');
    configurationServiceStub.get.withArgs(Mailbox.WEBMAIL_URL).returns('test.domain.com');
    configurationServiceStub.get.withArgs('ui2.url').returns('test-ui2.url');
    configurationServiceStub.get.withArgs('system.default.host.protocol').returns('http');
    configurationServiceStub.get.withArgs('system.default.host.name').returns('test-ui2-backend.url');
    configurationServiceStub.get.withArgs('system.default.host.port').returns('8080');

    emailAttachmentPickerServiceStub = stub(EmailAttachmentPickerService);
    instanceShareRestServiceStub = stub(InstanceShareRestService);
    localStorageStub = stub(LocalStorageService);
    searchService = stub(SearchService);
    shareFolderService = stub(ShareFolderService);
    shareFolderService.mountObjectShareFolder.returns(PromiseStub.resolve({}));
  });

  let initMailbox = (args = {}) => {
    let mailboxArgs = [
      args.$element || fakeElement, args.contextualEscapeAdapter || contextualEscapeAdapterMock,
      args.userService || userServiceStub,
      args.emailAccountDelegationService || accountDelegationStub,
      args.localStorage || localStorageStub, args.configurationService || configurationServiceStub,
      args.promiseAdapter || PromiseStub,
      args.eventbus || IdocMocks.mockEventBus(),
      args.notificationService || notificationService,
      args.logger || logger,
      args.windowMessageDispatcher || messageDispatcherStub,
      args.emailAttachmentPickerService || emailAttachmentPickerServiceStub,
      args.instanceShareRestService || instanceShareRestServiceStub,
      args.createPanelService || {},
      args.searchService || searchService,
      args.dialogService || {},
      args.shareFolderService || shareFolderService
    ];

    let mailbox = new Mailbox(...mailboxArgs);
    mailbox.iframe = fakeElement;
    mailbox.afterIFrameInit();
    return mailbox;
  };


  describe('user mailbox', () => {
    it('should initialize mailbox view with proper user credential', () => {
      Mailbox.prototype.context = {getCurrentObject: () => PromiseStub.resolve({id: 'test-user-id'})};
      let mailbox = initMailbox();

      expect(mailbox.zimbraUrl).to.equal('http://test.domain.com/preauth.jsp?account=test-mail@domain.com');
      expect(mailbox.isObjectMailbox).to.be.false;
    });

    it('should remove iframe if log out has been initiated', () => {
      Mailbox.prototype.context = {getCurrentObject: () => PromiseStub.resolve({id: 'test-user-id'})};
      let iframeRemoveSpy = sinon.spy();

      let eventbus = new MockEventbus();

      let mailbox = initMailbox({eventbus});
      mailbox.iframe = {
        remove: iframeRemoveSpy,
        attr: () => {
        }
      };

      eventbus.publish(new LogoutEvent());
      expect(iframeRemoveSpy.calledOnce).to.be.true;
    });

    it('should destroy without calling services and unsubscribing from eventbus', () => {
      Mailbox.prototype.context = {getCurrentObject: () => PromiseStub.resolve({id: 'test-user-id'})};
      let eventbus = stub(Eventbus);
      let unsubscribeSpy = {unsubscribe: sinon.spy()};
      eventbus.subscribe.returns(unsubscribeSpy);
      let mailbox = initMailbox({eventbus: eventbus});
      mailbox.ngOnDestroy();

      expect(unsubscribeSpy.unsubscribe.called).to.be.true;
      expect(mailbox.emailAccountDelegationService.delegateRights.called).to.be.false;
    });
  });

  describe('object mailbox', () => {
    let objectModel = {
      validationModel: {
        emailAddress: {value: 'test-object@mail.com'},
        title: {value: 'test-object-title'}
      }
    };

    beforeEach(() => {
      localStorageStub.getJson.returns({});
      localStorageStub.mergeValues.returns({});

      Mailbox.prototype.context = {
        currentObjectId: 'test-object-id',
        sharedObjects: {'test-object-id': {models: objectModel}},
        getCurrentObject: () => PromiseStub.resolve({
          id: 'test-object-id',
          getModels: () => {
            return objectModel
          }
        }),
      };
    });

    describe('#afterIFrameInit', () => {
      it('should initialize with proper configuration properties', () => {
        let mailbox = initMailbox();

        expect(mailbox.zimbraUrl).to.equal('http://test.domain.com/preauth.jsp?account=test-object@mail.com');
        expect(mailbox.isObjectMailbox).to.be.true;
      });
    });

    describe('#initializeObjectMailbox()', () => {
      it('should initialize object mailbox and store proper information in local storage', () => {
        localStorageStub.getJson.returns({"test-object-id": 1});
        initMailbox();

        expect(localStorageStub.mergeValues.getCall(0).args[1]).to.eql({'test-object-id': 2});
        expect(accountDelegationStub.delegateRights.called).to.be.false;
      });

      it('should notify the notification service if account delegation fails', () => {
        accountDelegationStub.delegateRights.returns(PromiseStub.reject('error'));
        initMailbox();
        expect(notificationService.error.called).to.be.true;
        expect(notificationService.error.getCall(0).args[0]).to.equal('error');
      });
    });

    describe('#composeZimletHandler', () => {
      let postmessageSpy = sinon.spy();
      beforeEach(() => {
        postmessageSpy.reset();
        Mailbox.prototype.getMailClientWindow = () => {
          return {postMessage: postmessageSpy}
        };
      });

      it('should send proper arguments to mailbox iframe', () => {
        let mailbox = initMailbox({localStorageService: localStorageStub});
        mailbox.isObjectMailbox = true;
        mailbox.emailAttributes = {
          emailAddress: 'testFrom@mail.com',
          displayName: 'Test'
        };
        mailbox.composeOverrides.cc = ['testCc1@mail.com', 'testCc2@mail.com'];
        mailbox.composeZimletHandler({command: 'ZIMBRA_INITIALIZED'});
        let sentObject = postmessageSpy.getCall(0).args[0];
        expect(sentObject.from).to.equal('testFrom@mail.com');
        expect(sentObject.displayName).to.equal('Test');
        expect(sentObject.delegatedSenderAddress).to.equal('test-mail@domain.com');
        expect(sentObject.cc).to.eql(['testCc1@mail.com', 'testCc2@mail.com']);
        expect(sentObject.topic).to.equal('com_sep_compose_zimlet');
      });

      it('should hide mailbox iframe if session is over', () => {
        let mailbox = initMailbox();
        mailbox.iframe.hide = () => {
        };
        mailbox.iframe.hide = () => {
        };
        mailbox.composeZimletHandler({command: 'RELOAD_IFRAME'});
        expect(mailbox.showAlert).to.be.true;
      });

    });

    describe('#attachmentZimletHandler', () => {

      it('should open picker service when OPEN_SEARCH command is sent and build proper message to zimlet', () => {
        let generatedDownloadLinks =
          ['http://test-ui2-backend.url:8080/emf/service/test-link#1',
            'http://test-ui2-backend.url:8080/emf/service/test-link#2'];

        let zimletMessage = {"command": "OPEN_SEARCH", "viewId": "COMPOSE-1"};
        let mailbox = initMailbox();
        mailbox.postMessageToMailClient = sinon.spy();
        mailbox.emailAttachmentPickerService.selectAttachments.returns(PromiseStub.resolve([{
          id: 'test-object-id',
          headers: {breadcrumb_header: '<span>test_header</span>'},
          properties: {title: 'test_header', 'emf:version': '1.0'}
        }, {
          id: 'test-object-id-2',
          headers: {breadcrumb_header: '<span>test_header_2</span>'},
          properties: {title: 'test_header_2', 'emf:version': '2.42'}
        }]));
        instanceShareRestServiceStub.shareLinks.returns(PromiseStub.resolve({data: generatedDownloadLinks}));

        mailbox.attachmentZimletHandler(zimletMessage);
        let sentLinks = mailbox.postMessageToMailClient.getCall(0).args[0];

        let linksArray = sentLinks.links;
        let [firstLink, secondLink] = [$(linksArray[0]), $(linksArray[1])];
        expect(firstLink.find('a').eq(0).attr('href')).to.equal('http://test-ui2-backend.url:8080/emf/service/test-link#1');
        expect(firstLink.find('span').eq(0).html()).to.equal('test_header, v.1.0');
        expect(secondLink.find('a').eq(0).attr('href')).to.equal('http://test-ui2-backend.url:8080/emf/service/test-link#2');
        expect(secondLink.find('span').eq(0).html()).to.equal('test_header_2, v.2.42')
      });

      it('should not send data when no objects are selected', () => {
        let zimletMessage = {"command": "OPEN_SEARCH", "viewId": "COMPOSE-1"};
        let mailbox = initMailbox();
        mailbox.postMessageToMailClient = sinon.spy();
        mailbox.emailAttachmentPickerService.selectAttachments.returns(PromiseStub.resolve([]));

        mailbox.attachmentZimletHandler(zimletMessage);

        expect(mailbox.postMessageToMailClient.called).to.be.false;
      });

      it('should send proper arguments to export service', () => {
        let zimletMessage = {
          "command": "INITIATE_ATTACHMENT_EXPORT",
          "sharecodes": ["#1", "#2"]
        };
        let mailbox = initMailbox();
        mailbox.attachmentZimletHandler(zimletMessage);

        expect(mailbox.instanceShareRestService.triggerShare.getCall(0).args[0]).to.eql(["#1", "#2"]);
      });
    });

    describe('#importZimletHandler', () => {
      it('should open upload dialog without context', () => {
        let zimletMessage = {
          "command": "IMPORT_ATTACHMENT",
          "fileName": 'test',
          "fileObject": {},
          "recipients": ['case1', 'project2']
        };
        let mailbox = initMailbox();
        mailbox.searchService.search.returns({
          promise: PromiseStub.resolve({
            data: {
              values: []
            }
          })
        });
        let createPanelService = {
          openUploadInstanceDialog: sinon.spy()
        };
        mailbox.createPanelService = createPanelService;
        mailbox.importZimletHandler(zimletMessage);

        expect(createPanelService.openUploadInstanceDialog.callCount).to.equal(1);
        let request = createPanelService.openUploadInstanceDialog.getCall(0).args[0];

        expect(request.parentId).to.equal('');
        expect(request.operation).to.equal('create');
      });

      it('should open upload dialog with set default context', () => {
        let zimletMessage = {
          "command": "IMPORT_ATTACHMENT",
          "fileName": 'test',
          "fileObject": {},
          "recipients": ['case1', 'project2']
        };
        let mailbox = initMailbox();

        mailbox.searchService.search.returns({
          promise: PromiseStub.resolve({
            data: {
              values: [{id: 'testId'}]
            }
          })
        });

        let createPanelService = {
          openUploadInstanceDialog: sinon.spy()
        };
        mailbox.createPanelService = createPanelService;
        mailbox.importZimletHandler(zimletMessage);

        expect(createPanelService.openUploadInstanceDialog.callCount).to.equal(1);
        let request = createPanelService.openUploadInstanceDialog.getCall(0).args[0];

        expect(request.parentId).to.equal('testId');
        expect(request.operation).to.equal('create');
      });

      it('should open select context dialog', () => {
        let zimletMessage = {
          "command": "IMPORT_ATTACHMENT",
          "fileName": 'test',
          "fileObject": {},
          "recipients": ['case1', 'project2']
        };
        let mailbox = initMailbox();

        mailbox.searchService.search.returns({
          promise: PromiseStub.resolve({
            data: {
              values: [{id: 'testId1'}, {id: 'testId2'}]
            }
          })
        });

        let dialogService = {
          create: sinon.spy()
        };
        mailbox.dialogService = dialogService;
        mailbox.importZimletHandler(zimletMessage);

        expect(dialogService.create.callCount).to.equal(1);
        let request = dialogService.create.getCall(0).args[1];
        expect(request.config.selectableItems).to.be.true;
        expect(request.config.singleSelection).to.be.true;
        expect(request.instances).to.eql([{id: 'testId1'}, {id: 'testId2'}]);
      });
    });

    describe('#messageStatusZimletHandler', () => {
      it('should should fire ObjectMailboxUpdatedEvent', () => {
        let zimletMessage = {
          "command": "MAILBOX_UNREAD_MESSAGES",
          "unreadMessagesCount": 6
        };
        let mailbox = initMailbox();
        let spyPublish = sinon.spy(mailbox.eventbus, 'publish');
        mailbox.messageStatusZimletHandler(zimletMessage);
        expect(spyPublish.callCount).to.equal(1);
        let publishPayload = spyPublish.getCall(0).args[0];
        expect(publishPayload instanceof ObjectMailboxUpdatedEvent).to.be.true;
      })
    });

    describe('#postMessageToMailClient', () => {
      it('should post message with correct format', () => {
        let fakeClient = {postMessage: sinon.spy()};
        let mailbox = initMailbox();
        mailbox.getMailClientWindow = () => fakeClient;
        let postMessageData = {data: 'test-data'};
        let postMessageZimlet = 'test-zimlet';

        mailbox.postMessageToMailClient(postMessageData, postMessageZimlet);
        let expectedSentData = _.clone(postMessageData);
        expectedSentData.topic = postMessageZimlet;
        expect(fakeClient.postMessage.getCall(0).args[0]).to.eql((expectedSentData));
      });

    });

    describe('#reloadMailboxIframe', () => {
      it('should reload mailbox iframe and hide worning', () => {
        let mailbox = initMailbox();
        mailbox.iframe.show = () => {
        };
        mailbox.reloadMailboxIframe();
        expect(mailbox.showAlert).to.be.false;
      });
    });

    describe('createShareLinkTemplates', () => {
      it('should build a list with properly formatted link templates', () => {
        let selectedObjects = [
          {
            'properties': {'title': 'Document 1', 'emf:version': '1.11'},
            'headers': {'breadcrumb_header': '<span><img src="/images/instance-icons/document-icon-16.png"/></span>'}
          },
          {
            'properties': {'title': 'Case 1', 'emf:version': '2.22'},
            'headers': {'breadcrumb_header': '<span><img src="/images/instance-icons/caseinstance-icon-16.png"/></span>'}
          }
        ];
        let shareLinks = [
          'http://ip:8080/emf/share/content/emf:dc3shareCode=9z5L1b4b04d=c1530fAt975=5mYa818aV8R9AtWWY',
          'http://ip:8080/emf/share/content/emf:d42shareCode=W=t8efbf181czd5Y=V05LRa1f993m9355t55AWY7A'
        ];
        let mailbox = initMailbox();

        let converted = mailbox.createShareLinkTemplates(selectedObjects, shareLinks);

        expect(converted).to.eql([
          '<div style=\"display:inline;\" contenteditable=\"false\" instance-link><a href=\"http://ip:8080/emf/share/content/emf:dc3shareCode=9z5L1b4b04d=c1530fAt975=5mYa818aV8R9AtWWY\" class=\"instance-link\"><img src=\"test-ui2.url/images/instance-icons/document-icon-16.png\" /><span>Document 1, v.1.11</span></a></div><br />',
          '<div style=\"display:inline;\" contenteditable=\"false\" instance-link><a href=\"http://ip:8080/emf/share/content/emf:d42shareCode=W=t8efbf181czd5Y=V05LRa1f993m9355t55AWY7A\" class=\"instance-link\"><img src=\"test-ui2.url/images/instance-icons/caseinstance-icon-16.png\" /><span>Case 1, v.2.22</span></a></div><br />'
        ]);
      });
    });

    describe('#ngOnDestroy', () => {
      it('should not remove rights if its not the only opened mailbox tab', () => {
        localStorageStub.getJson.returns({"test-object-id": 2});
        let mailbox = initMailbox();

        mailbox.ngOnDestroy();
        expect(accountDelegationStub.removeRights.called).to.be.false;
      });

      it('should remove delegated right if this is the only open window using the beforeUnload handler', () => {
        let mailbox = initMailbox();
        mailbox.ngOnDestroy();
        // if its the only value left, the key is deleted.
        expect(localStorageStub.set.getCall(0).args[1]).to.eql({});
        expect(accountDelegationStub.delegateRights.called).to.be.true;
      });

      it('should only decrement object delegated rights counter if more than one windows are open using the beforeUnload handler', () => {
        localStorageStub.getJson.returns({"test-object-id": 2});
        let mailbox = initMailbox();
        mailbox.afterIFrameInit();
        mailbox.ngOnDestroy();
        expect(localStorageStub.set.getCall(0).args[1]).to.eql({'test-object-id': 1});
        expect(accountDelegationStub.delegateRights.called).to.be.false;
      });

      it('should remove rights if its not the only opened mailbox tab', () => {
        localStorageStub.getJson.returns({"test-object-id": 1});
        let mailbox = initMailbox();
        mailbox.componentDestroyed = true;
        mailbox.ngOnDestroy();

        expect(accountDelegationStub.removeRights.calledOnce).to.be.true;
        expect(accountDelegationStub.removeRights.getCall(0).args).to.eql(['test-mail@domain.com', 'test-object-id']);
      });

      it('should skip all handling if the component has been already destroyed', () => {
        localStorageStub.getJson.returns({"test-object-id": 1});
        let mailbox = initMailbox();
        mailbox.componentDestroyed = true;
        mailbox.ngOnDestroy();

        expect(accountDelegationStub.delegateRights.calledTwice).to.be.false;
        expect(localStorageStub.set.calledTwice).to.be.false;
      });
    });
  });
})
;
