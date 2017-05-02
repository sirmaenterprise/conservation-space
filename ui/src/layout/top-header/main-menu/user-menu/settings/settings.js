import {View,Component,Inject} from 'app/app';
import {UserService} from 'services/identity/user-service';
import {IconsService} from 'services/icons/icons-service';
import './settings.css!css';
import settingsTemplate from './settings.html!text';


@Component({
  selector: 'seip-settings'
})
@View({
  template: settingsTemplate
})
@Inject(UserService, IconsService)
class Settings {

  constructor(userService, iconsService) {
    this.userIcon = iconsService.getIconForInstance('user',24);
    userService.getCurrentUser().then((user) => {
      this.currentUser = user;
    });
  }

}