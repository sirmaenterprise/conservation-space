import {View, Component, Inject, NgElement} from 'app/app';
import {UserService} from 'services/identity/user-service';
import {Router} from 'adapters/router/router';
import {Eventbus} from 'services/eventbus/eventbus';
import {PersonalMailboxUpdatedEvent} from 'idoc/system-tabs/mailbox/events/personal-mailbox-updated-event';
import {MailboxInfoService} from 'services/rest/mailbox-info-service';

import './mail-menu.css!css';
import template from './mail-menu.html!text';

@Component({
  selector: 'seip-mail-menu'
})
@View({
  template: template
})
@Inject(UserService, Router, Eventbus, MailboxInfoService, NgElement)
export class MailMenu {

  constructor(userService, router, eventbus, mailboxInfoService, $element) {
    this.router = router;
    this.userService = userService;
    this.mailboxInfoService = mailboxInfoService;
    this.$element = $element;
    this.eventbus = eventbus;
  }

  ngOnInit() {
    this.userService.getCurrentUser().then((currentUser) => {
      // user must be mailboxSupportable and have an email address for icon to be displayed
      if (currentUser.mailboxSupportable && currentUser.emailAddress) {
        this.renderMenu = true;
        this.initEventHandlers();
        // execute it once to show notifications immediately and then start the polling
        this.loadData(currentUser.emailAddress);
      }
      this.userId = currentUser.id;
    });
  }

  initEventHandlers() {
    this.personalMailboxUpdatedEvent = this.eventbus.subscribe(PersonalMailboxUpdatedEvent, (data) => {
      this.updateCounter(data[0]);
    });
  }

  toggleLoadingIcon(showLoadingIcon) {
    let counter = this.$element.find('.unread-messages-counter a').find('.badge');
    if (showLoadingIcon) {
      counter.hide().end().addClass('fa fa-sm fa-spinner fa-spin');
    } else {
      counter.show().end().removeClass('fa fa-sm fa-spinner fa-spin');
      counter.removeAttr('style');
    }
  }

  refreshCounter(event) {
    event.preventDefault();
    event.stopPropagation();
    this.loadData();
  }

  loadData(emailAddress) {
    this.toggleLoadingIcon(true);
    this.mailboxInfoService.getUnreadMessagesCount(emailAddress, {skipInterceptor: true}).then((response) => {
      this.updateCounter(response.data);
      this.toggleLoadingIcon(false);
    }).catch(() => {
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
    this.personalMailboxUpdatedEvent.unsubscribe();
  }
}
