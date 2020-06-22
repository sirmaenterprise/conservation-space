import {Inject, Injectable} from 'app/app';
import {BootstrapService} from 'services/bootstrap-service';
import {UserService} from 'security/user-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {PersonalMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/personal-mailbox-updated-event';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {PollingUtils} from 'common/polling-utils';
import {Configuration} from 'common/application-config';

@Injectable()
@Inject(Eventbus, MailboxInfoService, UserService, PollingUtils, Configuration)
export class PersonalMailboxInfoObserver extends BootstrapService {

  constructor(eventbus, mailboxInfoService, userService, pollingUtils, configuration) {
    super();
    this.eventbus = eventbus;
    this.mailboxInfoService = mailboxInfoService;
    this.userService = userService;
    this.pollingUtils = pollingUtils;
    this.pollingInterval = configuration.get(Configuration.MAILBOX_STATUS_POLL_INTERVAL);
  }

  initialize() {
    this.userService.getCurrentUser().then((response) => {
      if (response.mailboxSupportable) {
        this.pollingUtils.pollInfinite('personal_mailbox_unread_messages_count', this.handleMessagesStatusUpdate.bind(this), this.pollingInterval, false);
      }
    });
  }

  handleMessagesStatusUpdate() {
    this.userService.getCurrentUser().then((response) => {
      return this.mailboxInfoService.getUnreadMessagesCount(response.emailAddress, {skipInterceptor: true});
    }).then((response) => {
      this.eventbus.publish(new PersonalMailboxUpdatedEvent(response.data));
    });
  }
}