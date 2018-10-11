import {Component, View, Inject, NgElement} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {ObjectMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/object-mailbox-updated-event';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {PollingUtils} from 'common/polling-utils';
import {Configuration} from 'common/application-config';

import './unread-messages-counter.css!css';
import template from 'idoc/system-tabs/mailbox/unread-messages-counter/unread-messages-counter.html!text';

@Component({
  selector: 'seip-mailbox-unread-messages-counter'
})
@View({
  template: template
})
@Inject(Eventbus, IdocContextFactory, MailboxInfoService, NgElement, PollingUtils, Configuration)
export class UnreadMessagesCounter {

  constructor(eventbus, idocContextFactory, mailboxInfoService, $element, pollingUtils, configuration) {
    this.idocContextFactory = idocContextFactory;
    this.mailboxInfoService = mailboxInfoService;
    this.pollingUtils = pollingUtils;
    this.eventbus = eventbus;
    this.$element = $element;

    this.pollInterval = configuration.get(Configuration.MAILBOX_STATUS_POLL_INTERVAL);
  }

  ngOnInit() {
    this.initEventHandlers();
    this.handleMessagesStatusUpdate();
    this.pollerTask = this.pollingUtils.pollInfinite('object_mailbox_unread_messages_count', this.handleMessagesStatusUpdate.bind(this), this.pollInterval, false);
  }

  initEventHandlers() {
    this.objectMailboxUpdatedEvent = this.eventbus.subscribe(ObjectMailboxUpdatedEvent, (data) => {
      this.updateCounter(data[0]);
    });
  }

  handleMessagesStatusUpdate() {
    let context = this.idocContextFactory.getCurrentContext();
    if (!context) {
      return;
    }
    return context.getCurrentObject().then((object) => {
      return this.mailboxInfoService.getUnreadMessagesCount(object.getPropertyValue('emailAddress'), {skipInterceptor: true});
    }).then((response) => {
      this.updateCounter(response.data);
    });
  }

  toggleLoadingIcon(showLoadingIcon) {
    let counter = this.$element.find('a').find('.badge');
    if (showLoadingIcon) {
      counter.hide().end().addClass('fa fa-sm fa-spinner fa-spin');
    } else {
      counter.show().end().removeClass('fa fa-sm fa-spinner fa-spin');
      counter.removeAttr('style');
    }
  }

  refreshCounter(event) {
    // This component is rendered in an idoc tab and if the event propagation is not stopped it will trigger tab switch.
    event.preventDefault();
    event.stopPropagation();
    this.toggleLoadingIcon(true);
    this.handleMessagesStatusUpdate().then(() => {
      this.toggleLoadingIcon(false);
    });
  }

  updateCounter(count) {
    if (parseInt(count) > 0) {
      this.unreadMessages = count;
    } else if (parseInt(count) === 0) {
      delete this.unreadMessages;
    }
  }

  ngOnDestroy() {
    this.objectMailboxUpdatedEvent.unsubscribe();
    this.pollerTask.stop();
  }
}