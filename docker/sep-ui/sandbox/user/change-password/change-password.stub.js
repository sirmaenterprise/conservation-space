import {Component, View, Inject} from 'app/app';
import {ChangePasswordService} from 'user/change-password/change-password-service';
import template from 'change-password-template!text';

@Component({
  selector: 'change-password-stub'
})
@View({
  template: template
})
@Inject(ChangePasswordService)
export class ChangePasswordStub {

  constructor(changePasswordService) {
    this.changePasswordService = changePasswordService;
  }

  openDialog() {
    this.changePasswordService.openDialog();
  }

}