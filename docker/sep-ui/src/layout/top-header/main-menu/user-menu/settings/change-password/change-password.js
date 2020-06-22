import {View,Component,Inject} from 'app/app';
import {ChangePasswordService} from 'user/change-password/change-password-service';

@Component({
  selector: 'seip-settings-change-password'
})
@View({
  template: '<li><a class="change-password-action" href="javascript: void(0)" ng-click="changePassword.openDialog()">{{::"user.menu.change.password" | translate}}</a></li>'
})
@Inject(ChangePasswordService)
export class ChangePassword {

  constructor(changePasswordService) {
    this.changePasswordService = changePasswordService;
  }

  openDialog() {
    this.changePasswordService.openDialog();
  }

}