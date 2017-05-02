import {Component, View, Inject} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import 'components/alt-src';
import template from './user-avatar.html!text';
import './user-avatar.css!';

@Component({
  selector: 'seip-user-avatar',
  properties: {
    user: 'user',
    size: 'size'
  }
})
@View({
  template
})
@Inject(AuthenticationService)
export class UserAvatar {

  constructor(authenticationService) {
    this.token = authenticationService.getToken();
    this.size = this.size || 32;
  }

  get avatarUrl() {
    return `/remote/api/thumbnails/${this.user.id}?jwt=${this.token}`;
  }
}