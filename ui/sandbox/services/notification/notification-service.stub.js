import {Component, View, Inject} from 'app/app';
import {NotificationService} from 'services/notification/notification-service';
import notificationTemplate from 'notification-template!text';

@Component({
  selector: 'seip-notification-stub',
})
@View({
  template: notificationTemplate,
})
@Inject(NotificationService)
class Notification {

  constructor(notificationService){
    this.notificationService = notificationService;
  }
  
  notifyShortMessage(type){
    this.notificationService.notify(type, Notification.SHORT_MESSAGE);
  }
  
  notifyLongMessage(type){
    this.notificationService.notify(type, Notification.LONG_MESSAGE);
  }
}

Notification.SHORT_MESSAGE = 'Test Message';
Notification.LONG_MESSAGE = 'Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit amet, consectetur, adipisci[ng] velit, sed quia non numquam [do] eius modi tempora.';