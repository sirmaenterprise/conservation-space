import {View, Component, Inject} from 'app/app';
import {UserService} from 'security/user-service';
import {TranslateService} from 'services/i18n/translate-service';
import 'components/dropdownmenu/dropdownmenu';
import template from './admin-menu.html!text';

@Component({
  selector: 'seip-admin'
})
@View({template})
@Inject(UserService, TranslateService)
export class AdminMenu {

  constructor(userService, translateService) {
    this.userService = userService;
    this.translateService = translateService;

    this.config = {
      extensionPoint: 'admin-menu-items',
      triggerLabel: '<i class="fa fa-lg fa-fw fa-gear"></i>',
      wrapperClass: 'admin-menu',
      buttonAsTrigger: false,
      context: {},
      tooltip: 'menu.admin',
      sortComparator: this.menuElementsComparator.bind(this)
    };

    userService.getCurrentUser().then((response)=> {
      this.renderMenu = response.isAdmin;
    });
  }

  menuElementsComparator(lhs, rhs) {
    let left = this.translateService.translateInstant(lhs.label);
    let right = this.translateService.translateInstant(rhs.label);
    return left.localeCompare(right);
  }
}
