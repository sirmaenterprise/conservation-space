import {Component, View, Inject} from 'app/app';
import 'user/avatar/user-avatar';
import template from './user-avatar.stub.html!text';

@Component({
  selector: 'user-avatar-stub'
})
@View({
  template
})
@Inject()
export class UserAvatarStub {
  constructor() {
    this.currentUser = {
      id: 123
    };
    this.hover = false;
  }
}
