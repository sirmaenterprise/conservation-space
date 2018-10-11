import {View,Component} from 'app/app';
import topHeaderTemplate from './top-header.html!text';
import 'font-awesome/css/font-awesome.css!';
import './top-header.css!';

@Component({
  selector: 'seip-top-header'
})
@View({
  template: topHeaderTemplate
})
class TopHeader {

}