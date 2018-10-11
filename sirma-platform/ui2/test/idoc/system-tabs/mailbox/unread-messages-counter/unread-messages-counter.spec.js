import {UnreadMessagesCounter} from 'idoc/system-tabs/mailbox/unread-messages-counter/unread-messages-counter';
import {PromiseStub} from 'test/promise-stub';
import {Eventbus} from 'services/eventbus/eventbus';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {PollingUtils} from 'common/polling-utils';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {Configuration} from 'common/application-config';
import {stub} from 'test/test-utils';

describe('UnreadMessagesCounter', () => {
  let element = {};
  let [eventbusStub, idocContextFactoryStub, mailboxInfoServiceStub, pollingUtilsStub, configurationStub] = [stub(Eventbus),
    stub(IdocContextFactory), stub(MailboxInfoService), stub(PollingUtils), stub(Configuration)];
  let unreadMessagesCounter;

  beforeEach(function () {
    configurationStub.get.returns(10000);
    unreadMessagesCounter = new UnreadMessagesCounter(eventbusStub, idocContextFactoryStub, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
  });

  it('should call infinite poll when component is initialized', () => {
    unreadMessagesCounter.pollingUtils.pollInfinite = sinon.spy();
    unreadMessagesCounter.ngOnInit();
    expect(unreadMessagesCounter.pollingUtils.pollInfinite.calledOnce);
    expect(unreadMessagesCounter.pollingUtils.pollInfinite.getCall(0).args[0]).to.equal('object_mailbox_unread_messages_count');
    expect(unreadMessagesCounter.pollingUtils.pollInfinite.getCall(0).args[2]).to.equal(10000);
  });

  it('should unsubscribe from events when component is destroyed', () => {
    let spyUnsubscribe = {
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      subscribe: () => {
        return spyUnsubscribe
      }
    };
    let pollingUtilsStub = {
      pollInfinite: () => {
        return {
          stop: sinon.spy()
        };
      }
    };
    unreadMessagesCounter = new UnreadMessagesCounter(eventbus, idocContextFactoryStub, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
    unreadMessagesCounter.ngOnInit();
    unreadMessagesCounter.initEventHandlers();
    unreadMessagesCounter.ngOnDestroy();
    expect(spyUnsubscribe.unsubscribe.calledOnce).to.be.true;
    expect(unreadMessagesCounter.pollerTask.stop.calledOnce).to.be.true;
  });

  describe('handleMessagesStatusUpdate', () => {
    it('should return if there\' no define context', () => {
      mailboxInfoServiceStub.getUnreadMessagesCount.returns(PromiseStub.resolve({
        data: 7
      }));
      unreadMessagesCounter.handleMessagesStatusUpdate();
      expect(unreadMessagesCounter.unreadMessages).to.equal(undefined);
    });

    it('should extract and show unread messages count', () => {
      mailboxInfoServiceStub.getUnreadMessagesCount.returns(PromiseStub.resolve({
        data: 6
      }));

      let currentObject = new InstanceObject('emf:current');
      let context = stub(IdocContext);
      context.getCurrentObject = sinon.spy(() => {
        return PromiseStub.resolve(currentObject);
      });
      let factory = stub(IdocContextFactory);
      factory.getCurrentContext.returns(context);

      unreadMessagesCounter = new UnreadMessagesCounter(eventbusStub, factory, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
      unreadMessagesCounter.ngOnInit();
      unreadMessagesCounter.handleMessagesStatusUpdate();
      expect(unreadMessagesCounter.unreadMessages).to.equal(6);
    });
  });

  describe('updateCounter', () => {
    it('should set unread messages count', () => {
      let unreadMessagesCounter = new UnreadMessagesCounter(eventbusStub, idocContextFactoryStub, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
      unreadMessagesCounter.ngOnInit();
      unreadMessagesCounter.updateCounter(6);
      expect(unreadMessagesCounter.unreadMessages).to.equal(6);
    });

    it('should delete counter if there\'s no unread messages', () => {
      let unreadMessagesCounter = new UnreadMessagesCounter(eventbusStub, idocContextFactoryStub, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
      unreadMessagesCounter.ngOnInit();
      unreadMessagesCounter.updateCounter(0);
      expect(unreadMessagesCounter.unreadMessages).to.equal(undefined);
    });

    it('should not change counter if error code is returned', () => {
      let unreadMessagesCounter = new UnreadMessagesCounter(eventbusStub, idocContextFactoryStub, mailboxInfoServiceStub, element, pollingUtilsStub, configurationStub);
      unreadMessagesCounter.ngOnInit();
      unreadMessagesCounter.unreadMessages = 3;
      unreadMessagesCounter.updateCounter(-1);
      expect(unreadMessagesCounter.unreadMessages).to.equal(3);
    });
  });

});