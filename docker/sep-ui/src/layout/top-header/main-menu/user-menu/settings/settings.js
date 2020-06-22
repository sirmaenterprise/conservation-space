import {View, Component, Inject, NgElement, NgScope, NgCompile} from 'app/app';
import {UserService} from 'security/user-service';

import 'user/avatar/user-avatar';
import './settings.css!css';
import settingsTemplate from './settings.html!text';

@Component({
  selector: 'seip-settings'
})
@View({
  template: settingsTemplate
})
@Inject(NgElement, NgScope, NgCompile, UserService)
export class Settings {

  constructor($element, $scope, $compile, userService) {
    this.$element = $element;
    this.$scope = $scope;
    this.$compile = $compile;
    this.userService = userService;
  }

  ngOnInit() {
    this.userService.getCurrentUser().then((user) => {
      this.currentUser = user;
      this.compileAvatar();
    });
  }

  compileAvatar() {
    let avatar = this.$compile('<seip-user-avatar ng-if="settings.currentUser" user="settings.currentUser" size="42"></seip-user-avatar>')(this.$scope.$new());
    this.$element.find('.icons-container').find('.user-avatar').remove().end().prepend(avatar);
  }

}
