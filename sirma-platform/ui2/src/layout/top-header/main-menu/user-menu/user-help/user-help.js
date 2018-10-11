import {View,Component} from 'app/app';
import userHelpTemplate from './user-help.html!text';

@Component({
  selector: 'user-help'
})
@View({
  template: userHelpTemplate
})
class UserHelp {
  constructor() {

  }
}