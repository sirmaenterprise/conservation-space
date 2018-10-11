import {View, Component, Inject} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {LogoutEvent} from './logout-event';

@Component({
  selector: 'seip-settings-logout'
})
@View({
  template: '<li><a class="logout-action" href="javascript:void(0);" ng-click="settingsLogout.logout()">{{"user.menu.logout" | translate}}</a></li>'
})
@Inject(AuthenticationService, Eventbus)
export class SettingsLogout {

  constructor(authenticationService, eventBus) {
    this.authenticationService = authenticationService;
    this.eventBus = eventBus;
  }

  logout() {
    this.eventBus.publish(new LogoutEvent());
    this.authenticationService.logout();
  }
}