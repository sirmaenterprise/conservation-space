import {PersonalMailboxInfoObserver} from 'idoc/system-tabs/mailbox/services/personal-mailbox-info-observer/personal-mailbox-info-observer';
import {Configuration} from 'common/application-config';
import {PollingUtils} from 'common/polling-utils';
import {UserService} from 'services/identity/user-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {PersonalMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/personal-mailbox-updated-event';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('PersonalMailboxInfoObserver', function () {
  var configurationStub = stub(Configuration);
  var pollingUtilsStrub = stub(PollingUtils);
  var eventbusStub = stub(Eventbus);
  var mailboxInfoServiceStub = stub(MailboxInfoService);
  var userServiceStub = stub(UserService);
  var personalMailboxInfoObserver;

  beforeEach(function () {
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      emailAddress: 'testMail@domain.com',
      mailboxSupportable: true
    }));
    configurationStub.get.returns(10000);
    personalMailboxInfoObserver = new PersonalMailboxInfoObserver(eventbusStub, mailboxInfoServiceStub, userServiceStub, pollingUtilsStrub, configurationStub);
  });

  it('should call infinite poll if mailbox support is enabled', () => {
    personalMailboxInfoObserver.pollingUtils.pollInfinite = sinon.spy();
    personalMailboxInfoObserver.initialize();
    expect(personalMailboxInfoObserver.pollingUtils.pollInfinite.calledOnce);
    expect(personalMailboxInfoObserver.pollingUtils.pollInfinite.getCall(0).args[0]).to.equal('personal_mailbox_unread_messages_count');
    expect(personalMailboxInfoObserver.pollingUtils.pollInfinite.getCall(0).args[2]).to.equal(10000);
  });

  it('should fire PersonalMailboxUpdatedEvent when unread messages count is changes', () => {
    personalMailboxInfoObserver.eventbus.publish = sinon.spy();
    personalMailboxInfoObserver.mailboxInfoService.getUnreadMessagesCount.returns(PromiseStub.resolve({
      data: 7
    }));
    personalMailboxInfoObserver.handleMessagesStatusUpdate();
    expect(personalMailboxInfoObserver.eventbus.publish.calledOnce);
    let eventArguments = personalMailboxInfoObserver.eventbus.publish.getCall(0).args[0].args;
    expect(personalMailboxInfoObserver.eventbus.publish.getCall(0).args[0] instanceof PersonalMailboxUpdatedEvent).to.be.true;
    expect(eventArguments[0]).to.equal(7);
  });

});
