import {View, Component, Inject} from 'app/app';
import {UserService} from 'services/identity/user-service';
import template from './admin-menu.html!text';

@Component({
  selector: 'seip-admin'
})
@View({
  template: template
})
@Inject(UserService)
export class AdminMenu {
  constructor(userService) {
    this.userService = userService;
    this.config = {
      extensionPoint: 'admin-menu-items',
      triggerLabel: '<i class="fa fa-lg fa-fw fa fa-cogs"></i>',
      wrapperClass: 'admin-menu',
      buttonAsTrigger: false,
      context: {},
      title: 'menu.admin'
    };

    userService.getCurrentUser().then((response)=> {
      this.renderMenu = response.isAdmin;
    });
  }
}