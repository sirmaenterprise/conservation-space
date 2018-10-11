import {MailMenu} from 'layout/top-header/main-menu/quick-access/mail-menu/mail-menu';
import {UserService} from 'services/identity/user-service';
import {PromiseStub} from 'test/promise-stub';
import {Eventbus} from 'services/eventbus/eventbus';
import {PersonalMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/personal-mailbox-updated-event';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {stub} from 'test/test-utils';

describe('MailMenu', () => {
  let [userServiceStub, routerStub, eventbusStub, mailboxInfoServiceStub] = [stub(UserService), {}, stub(Eventbus), stub(MailboxInfoService)];
  let element = {
    find: () => {
      return {
        find: () => {
          return {
            hide: () => {
              return {
                end: () => {
                  return {
                    addClass: () => {},
                  };
                }
              };
            },
            show: () => {
              return {
                end: () => {
                  return {
                    removeClass: () => {},
                  };
                }
              };
            },
            removeAttr: () => {}
          };
        }
      };
    }
  };

  mailboxInfoServiceStub.getUnreadMessagesCount.returns(PromiseStub.resolve({
    data: 7
  }));

  it('should visualize if is mailboxsupportable and has email address', () => {
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      emailAddress: 'testMail@domain.com',
      mailboxSupportable: true
    }));
    let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub, mailboxInfoServiceStub, element);
    mailMenu.ngOnInit();
    expect(mailMenu.renderMenu).to.be.true;
  });

  it('should not visualize if is mailboxsupprtable but does not have email address', () => {
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      mailboxSupportable: true
    }));
    let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub);
    mailMenu.ngOnInit();
    expect(mailMenu.renderMenu).to.be.undefined;
  });

  it('should not visualize if there is email address but is not mailboxsupportable (probably support is disabled)', () => {
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      emailAddress: 'testMail@domain.com',
      mailboxSupportable: false
    }));
    let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub);
    expect(mailMenu.renderMenu).to.be.undefined;
  });

  it('should unsubscribe from events when component is destroyed', () => {
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      emailAddress: 'testMail@domain.com',
      mailboxSupportable: true
    }));
    let spyUnsubscribe = {
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      subscribe: () => {
        return spyUnsubscribe;
      }
    };

    let mailMenu = new MailMenu(userServiceStub, routerStub, eventbus, mailboxInfoServiceStub, element);
    mailMenu.ngOnInit();
    mailMenu.ngOnDestroy();
    expect(spyUnsubscribe.unsubscribe.calledOnce).to.be.true;
  });


  describe('updateCounter', () => {
    it('should set unread messages count', () => {
      let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub);
      mailMenu.updateCounter(6);
      expect(mailMenu.unreadMessages).to.equal(6);
    });

    it('should delete counter if there\'s no unread messages', () => {
      let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub);
      mailMenu.updateCounter(0);
      expect(mailMenu.unreadMessages).to.equal(undefined);
    });

    it('should not change counter if error code is returned', () => {
      let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub);
      mailMenu.unreadMessages = 3;
      mailMenu.updateCounter(-1);
      expect(mailMenu.unreadMessages).to.equal(3);
    });
  });

  describe('refreshCounter', () => {
    it('should refresh unread messages count', () => {
      let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub, mailboxInfoServiceStub, element);
      let event = {
        preventDefault: () => {},
        stopPropagation: () => {}
      };
      mailMenu.refreshCounter(event);
      expect(mailMenu.unreadMessages).to.equal(7);
    });

    it('should hide load icon if error occure', () => {
      mailboxInfoServiceStub.getUnreadMessagesCount.returns(PromiseStub.reject({err: '123'}));
      let mailMenu = new MailMenu(userServiceStub, routerStub, eventbusStub, mailboxInfoServiceStub, element);
      let handlePreviewSpy = sinon.spy(mailMenu, 'toggleLoadingIcon');

      mailMenu.unreadMessages = 2;
      let event = {
        preventDefault: () => {},
        stopPropagation: () => {}
      };
      mailMenu.refreshCounter(event);

      expect(mailMenu.unreadMessages).to.equal(2);
      expect(handlePreviewSpy.withArgs(true).calledOnce).to.be.true;
      expect(handlePreviewSpy.withArgs(false).calledOnce).to.be.true;
    });
  });

});