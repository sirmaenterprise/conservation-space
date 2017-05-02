import {View,Component} from 'app/app';
import userMenuTemplate from './user-menu.html!text';

@Component({
  selector: 'seip-user-menu'
})
@View({
  template: userMenuTemplate
})
class UserMenu {
  constructor() {

  }
}