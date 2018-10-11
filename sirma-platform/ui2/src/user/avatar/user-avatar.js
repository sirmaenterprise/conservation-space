import {Component, View, Inject, NgElement, NgTimeout} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import template from './user-avatar.html!text';
import './user-avatar.css!';

const DEFAULT_USER_ICON_SIZE = 32;
const THUMBNAIL_URL = '/remote/api/thumbnails/';

@Component({
  selector: 'seip-user-avatar',
  properties: {
    user: 'user',
    size: 'size',
    eventEmitter: 'event-emitter'
  }
})
@View({
  template
})
@Inject(NgElement, AuthenticationService, NgTimeout)
export class UserAvatar {

  constructor($element, authenticationService, $timeout) {
    this.$element = $element;
    this.$timeout = $timeout;
    this.imageElement = $element.find('img');
    this.fontElement = $element.find('i');
    this.token = authenticationService.getToken();
    this.size = this.size || DEFAULT_USER_ICON_SIZE;
  }

  ngOnInit() {
    this.avatarUrl = `${THUMBNAIL_URL}${this.user.id}?jwt=${this.token}`;

    this.imageElement.one('load', () => {
      this.initializeAvatarElements(this.fontElement, this.imageElement);
      this.publishLoadedEvent();
    });

    // default to font icon if user thumbnail is not changed.
    this.imageElement.one('error', () => {
      this.initializeAvatarElements(this.imageElement, this.fontElement);
      this.$element.addClass('default-avatar');
      this.publishLoadedEvent();
    });
  }

  initializeAvatarElements(toRemove, toShow) {
    toRemove.remove();
    toShow.removeClass('hidden');
  }

  publishLoadedEvent() {
    if (this.eventEmitter) {
      this.$timeout(() => {
        this.eventEmitter.publish('loaded');
      }, 0);
    }
  }

  ngOnDestroy() {
    if (this.imageElement) {
      this.imageElement.off();
    }
  }
}
