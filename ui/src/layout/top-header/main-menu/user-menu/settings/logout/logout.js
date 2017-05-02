import {View, Component, Inject} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';

@Component({
  selector: 'seip-settings-logout'
})
@View({
  template: '<li><a class="logout-action" href="javascript:void(0);" ng-click="settingsLogout.logout()">{{"user.menu.logout" | translate}}</a></li>'
})
@Inject(AuthenticationService)
export class SettingsLogout {

  constructor(authenticationService) {
    this.authenticationService = authenticationService;
  }

  logout() {
    this.authenticationService.logout();
  }
}