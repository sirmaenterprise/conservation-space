import {View,Component} from 'app/app';
import menuTemplate from './main-menu.html!text';
import './main-menu.css!';

@Component({
  selector: 'seip-main-menu'
})
@View({
  template: menuTemplate
})
class MainMenu {

}